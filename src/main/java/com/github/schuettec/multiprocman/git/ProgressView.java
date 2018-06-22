package com.github.schuettec.multiprocman.git;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.eclipse.jgit.lib.ProgressMonitor;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class ProgressView extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private GitWorker worker;
	private DefaultListModel<GitMonitor> listModel;

	private AtomicInteger waitingFor = new AtomicInteger();
	private JList list;

	/**
	 * Create the dialog.
	 */
	public ProgressView(List<BranchSelection> branchSelections) {

		this.worker = new GitWorker(branchSelections);
		this.waitingFor.set(branchSelections.size());

		ThemeUtil.setLookAndFeel();
		setIconImage(Resources.getApplicationIcon());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(480, 640));
		this.setSize(new Dimension(480, 640));
		ThemeUtil.loadWindow(this);
		ThemeUtil.installListeners(this);
		setModal(true);
		setTitle("Performing Git checkout");

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				this.listModel = new DefaultListModel<GitMonitor>();
				this.list = new JList(listModel);
				list.setCellRenderer(new ProgressCellRender());
				scrollPane.setViewportView(list);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setPreferredSize(new Dimension(91, 23));
				cancelButton.setActionCommand("Abort");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						worker.cancel(true);
						try {
							worker.get();
						} catch (Exception e1) {
						}
						ProgressView.this.dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		worker.execute();
	}

	public boolean showProgressAndPerform(Component parent) {
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
		return worker.isCancelled();
	}

	public class ProgressCellRender implements ListCellRenderer {

		private JLabel title;
		private JProgressBar progressBar;
		private JPanel panel;
		private BorderLayout layout;

		public ProgressCellRender() {
			super();
			this.layout = new BorderLayout();
			this.panel = new JPanel(layout);
			this.progressBar = new JProgressBar();
			this.title = new JLabel();
			panel.add(progressBar, BorderLayout.CENTER);
			panel.add(title, BorderLayout.SOUTH);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
		    boolean cellHasFocus) {

			GitMonitor monitor = (GitMonitor) value;
			progressBar.setMaximum(monitor.getTotalWork());
			progressBar.setValue(monitor.getCompleted());
			progressBar.setStringPainted(true);
			title.setText(monitor.getTitle());
			return panel;
		}

	}

	public class GitMonitor implements ProgressMonitor {

		protected String title = "";
		protected int totalWork = 100;
		protected int completed = 0;

		public int getCompleted() {
			return completed;
		}

		public String getTitle() {
			return title;
		}

		public int getTotalWork() {
			return totalWork;
		}

		@Override
		public void start(int totalTasks) {

		}

		@Override
		public void beginTask(String title, int totalWork) {
			this.title = title;
			this.totalWork = totalWork;
			list.repaint();
		}

		@Override
		public void update(int completed) {
			this.completed = completed;
			list.repaint();
		}

		@Override
		public void endTask() {
			int jobsWaiting = waitingFor.decrementAndGet();
			if (jobsWaiting <= 0) {
				ProgressView.this.dispose();
			}
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

	}

	public class GitWorker extends SwingWorker<Void, List<BranchSelection>> {

		private List<BranchSelection> branchSelections;

		public GitWorker(List<BranchSelection> branchSelections) {
			super();
			this.branchSelections = branchSelections;
		}

		@Override
		protected Void doInBackground() throws Exception {

			for (int i = 0; i < branchSelections.size(); i++) {
				BranchSelection b = branchSelections.get(i);
				try {
					GitMonitor monitor = new GitMonitor();
					listModel.addElement(monitor);
					b.checkoutBranch(ProgressView.this, b.getSelectedBranch(), monitor);
				} catch (Exception e) {
					ExceptionDialog.showException(ProgressView.this, e,
					    "Error while checking out branch %s for launcher %s. Aborting the launch operation.",
					    b.getSelectedBranch(), b.getTitle());
					worker.cancel(false);
					break;
				}
			}

			return null;
		}

	}

}
