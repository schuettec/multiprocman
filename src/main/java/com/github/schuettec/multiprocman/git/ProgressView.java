package com.github.schuettec.multiprocman.git;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class ProgressView extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private GitWorker worker;

	/**
	 * Create the dialog.
	 */
	public ProgressView(List<BranchSelection> branchSelections) {

		this.worker = new GitWorker(branchSelections);

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

				int size = branchSelections.size();
				Integer[][] data = new Integer[size][1];
				for (int i = 0; i < size; i++) {
					data[i][0] = 0;
				}
				table = new JTable(data, new String[] {
				    "Progress"
				});
				table.getColumnModel()
				    .getColumn(0)
				    .setCellRenderer(new ProgressCellRender());
				scrollPane.setViewportView(table);
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

	public class ProgressCellRender extends JProgressBar implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		    int row, int column) {
			int progress = 0;
			if (value instanceof Float) {
				progress = Math.round(((Float) value) * 100f);
			} else if (value instanceof Integer) {
				progress = (int) value;
			}
			setValue(progress);
			return this;
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

			for (BranchSelection b : branchSelections) {
				try {
					b.checkoutBranch(ProgressView.this, b.getSelectedBranch());
				} catch (Exception e) {
					ExceptionDialog.showException(ProgressView.this, e,
					    "Error while checking out branch %s for launcher %s. Aborting the launch operation.",
					    b.getSelectedBranch(), b.getTitle());
					worker.cancel(false);
					break;
				}
			}

			ProgressView.this.dispose();
			return null;
		}

	}

}
