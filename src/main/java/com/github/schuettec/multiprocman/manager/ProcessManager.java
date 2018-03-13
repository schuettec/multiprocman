package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.schuettec.multiprocman.MainFrame;
import com.github.schuettec.multiprocman.ProcessController;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class ProcessManager extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JList<Category> lstCategories;
	private JList<ProcessDescriptor> lstProcesses;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ThemeUtil.setLookAndFeel();
			ProcessManager dialog = new ProcessManager();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Action newCategory = new AbstractAction(null, new ImageIcon(Resources.getFolderPlus())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Category newCategory = CategoryEditor.newCategory();
			if (nonNull(newCategory)) {
				categories.addElement(newCategory);
			}
		}
	};
	private Action editCategory = new AbstractAction(null, new ImageIcon(Resources.getEdit())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = lstCategories.getSelectedIndex();
			if (selectedIndex == -1) {
				JOptionPane.showMessageDialog(ProcessManager.this, "Please select the category to edit first.", "No selection",
				    JOptionPane.WARNING_MESSAGE);
			} else {
				CategoryEditor.editCategory(categories.get(selectedIndex));
				categories.saveToPreferences();
				lstCategories.repaint();
			}

		}
	};
	private Action removeCategory = new AbstractAction(null, new ImageIcon(Resources.getFolderMinus())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = lstCategories.getSelectedIndex();
			if (selectedIndex == -1) {
				JOptionPane.showMessageDialog(ProcessManager.this, "Please select the category to remove first.",
				    "No selection", JOptionPane.WARNING_MESSAGE);
			} else {
				categories.remove(selectedIndex);
			}
		}
	};

	private Action newApplication = new AbstractAction(null, new ImageIcon(Resources.getFolderPlus())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = lstCategories.getSelectedIndex();
			if (selectedIndex == -1) {
				JOptionPane.showMessageDialog(ProcessManager.this,
				    "Please select the category to assign the new application first.", "No selection",
				    JOptionPane.WARNING_MESSAGE);
			} else {
				ProcessDescriptor process = ApplicationEditor.newProcess();
				if (nonNull(process)) {
					currentCategory.getProcessTemplates()
					    .addElement(process);
					categories.saveToPreferences();
					lstProcesses.repaint();
				}
			}
		}
	};
	private Action editApplication = new AbstractAction(null, new ImageIcon(Resources.getEdit())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			ProcessDescriptor value = lstProcesses.getSelectedValue();
			if (isNull(value)) {
				JOptionPane.showMessageDialog(ProcessManager.this, "Please select the application to edit first.",
				    "No selection", JOptionPane.WARNING_MESSAGE);
			} else {
				ApplicationEditor.editProcessDescriptor(value);
				categories.saveToPreferences();
				lstProcesses.repaint();
			}

		}
	};
	private Action removeApplication = new AbstractAction(null, new ImageIcon(Resources.getFolderMinus())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			ProcessDescriptor value = lstProcesses.getSelectedValue();
			if (isNull(value)) {
				JOptionPane.showMessageDialog(ProcessManager.this, "Please select the application to remove first.",
				    "No selection", JOptionPane.WARNING_MESSAGE);
			} else {
				DefaultListModel<ProcessDescriptor> processes = (DefaultListModel<ProcessDescriptor>) lstProcesses.getModel();
				processes.removeElement(value);
				categories.saveToPreferences();
				lstProcesses.repaint();
			}
		}
	};

	private Categories categories;
	private Category currentCategory;

	/**
	 * Create the dialog.
	 */
	public ProcessManager() {
		setIconImage(Resources.getApplicationIcon());
		setTitle("Application manager");
		ThemeUtil.loadWindowState(this);
		ThemeUtil.installListeners(this);
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (!MainFrame.getInstance()
				    .isVisible()) {
					ThemeUtil.stopJavaFX();
				}
				dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			this.categories = new Categories();
		}
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setContinuousLayout(true);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			JScrollPane scrollPane = new JScrollPane();
			splitPane.setLeftComponent(scrollPane);
			scrollPane.setPreferredSize(new Dimension(200, 2));
			lstCategories = new JList<>(categories);
			lstCategories.setDragEnabled(true);
			lstCategories.setDropMode(DropMode.INSERT);
			lstCategories.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int selectedIndex = lstCategories.getSelectedIndex();
					if (selectedIndex != -1) {
						Category category = categories.get(selectedIndex);
						currentCategory = category;
						DefaultListModel<ProcessDescriptor> processTemplates = category.getProcessTemplates();
						lstProcesses.setModel(processTemplates);
						lstProcesses.repaint();
					}
				}
			});

			lstCategories.setTransferHandler(new TransferHandler() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;
				private int index;
				private boolean beforeIndex = false;

				@Override
				public int getSourceActions(JComponent comp) {
					return MOVE;
				}

				@Override
				public Transferable createTransferable(JComponent comp) {
					index = lstCategories.getSelectedIndex();
					return new StringSelection(String.valueOf(index));
				}

				@Override
				public void exportDone(JComponent comp, Transferable trans, int action) {
					if (action == MOVE) {
						if (beforeIndex)
							categories.remove(index + 1);
						else
							categories.remove(index);
					}
				}

				@Override
				public boolean canImport(TransferHandler.TransferSupport support) {
					return support.isDataFlavorSupported(DataFlavor.stringFlavor);
				}

				@Override
				public boolean importData(TransferHandler.TransferSupport support) {
					try {
						String s = (String) support.getTransferable()
						    .getTransferData(DataFlavor.stringFlavor);
						JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
						Category category = categories.get(Integer.parseInt(s));
						categories.add(dl.getIndex(), category);
						lstCategories.setSelectedIndex(dl.getIndex());
						beforeIndex = dl.getIndex() < index ? true : false;
						return true;
					} catch (Exception e) {
					}
					return false;
				}
			});
			lstCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstCategories.setCellRenderer(new CategoryCellRenderer());
			scrollPane.setViewportView(lstCategories);
			{
				JToolBar toolBar = new JToolBar();
				toolBar.setFloatable(false);
				toolBar.setOrientation(SwingConstants.VERTICAL);
				scrollPane.setRowHeaderView(toolBar);
				{
					JButton btnNewCategory = new JButton(newCategory);
					btnNewCategory.setToolTipText("New category..");
					toolBar.add(btnNewCategory);
				}
				{
					JButton btnEditCategory = new JButton(editCategory);
					btnEditCategory.setToolTipText("Edit category");
					toolBar.add(btnEditCategory);
				}
				{
					JButton btnRemoveCategory = new JButton(removeCategory);
					btnRemoveCategory.setToolTipText("Remove category");
					toolBar.add(btnRemoveCategory);
				}
			}
			{
				JPanel panel = new JPanel();
				scrollPane.setColumnHeaderView(panel);
				panel.setLayout(new BorderLayout(5, 5));
				{
					JLabel lblNewLabel_1 = new JLabel("Categories:");
					lblNewLabel_1.setLabelFor(lstCategories);
					panel.add(lblNewLabel_1, BorderLayout.WEST);
				}
				{
					JToolBar toolBar = new JToolBar();
					toolBar.setRollover(true);
					toolBar.setFloatable(false);
					panel.add(toolBar, BorderLayout.CENTER);
					{
						JButton btnRunCategory = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlay())) {

							@Override
							public void actionPerformed(ActionEvent e) {

								int selectedIndex = lstCategories.getSelectedIndex();
								if (selectedIndex == -1) {
									JOptionPane.showMessageDialog(ProcessManager.this, "Please select the category to start first.",
									    "No selection", JOptionPane.WARNING_MESSAGE);
								} else {
									MainFrame mainFrame = MainFrame.getInstance();
									mainFrame.setVisible(true);
									Category selected = lstCategories.getSelectedValue();
									DefaultListModel<ProcessDescriptor> processTemplates = selected.getProcessTemplates();
									ProcessDescriptor[] array = new ProcessDescriptor[processTemplates.size()];
									processTemplates.copyInto(array);
									startAll(mainFrame, Arrays.asList(array)
									    .iterator());
									dispose();
								}
							}
						});
						btnRunCategory.setToolTipText("Run all applications in category.");
						toolBar.add(btnRunCategory);
					}
				}
			}
			{
				JScrollPane scrollPane_1 = new JScrollPane();
				splitPane.setRightComponent(scrollPane_1);
				{
					lstProcesses = new JList<>();
					lstProcesses.setVisibleRowCount(-1);
					lstProcesses.setLayoutOrientation(JList.HORIZONTAL_WRAP);
					lstProcesses.setCellRenderer(new ProcessCellRenderer());
					lstProcesses.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							super.mouseClicked(e);
							if (e.getClickCount() > 1) {
								runSelectedApplication();
							}
						}
					});
					scrollPane_1.setViewportView(lstProcesses);
				}
				{
					JToolBar toolBar = new JToolBar();
					toolBar.setOrientation(SwingConstants.VERTICAL);
					toolBar.setFloatable(false);
					scrollPane_1.setRowHeaderView(toolBar);
					{
						JButton btnNewProcess = new JButton(newApplication);
						btnNewProcess.setToolTipText("New Application");
						toolBar.add(btnNewProcess);
					}
					{
						JButton btnEditApplication = new JButton(editApplication);
						btnEditApplication.setToolTipText("Edit application");
						toolBar.add(btnEditApplication);
					}
					{
						JButton btnRemoveApplication = new JButton(removeApplication);
						btnRemoveApplication.setToolTipText("Remove application");
						toolBar.add(btnRemoveApplication);
					}
				}
				{
					JPanel panel = new JPanel();
					scrollPane_1.setColumnHeaderView(panel);
					panel.setLayout(new BorderLayout(5, 5));
					{
						JLabel lblApplications = new JLabel("Applications:");
						lblApplications.setLabelFor(lstProcesses);
						panel.add(lblApplications, BorderLayout.WEST);
					}
					{
						JToolBar toolBar = new JToolBar();
						toolBar.setRollover(true);
						toolBar.setFloatable(false);
						panel.add(toolBar, BorderLayout.CENTER);
						{
							JButton btnRunApplication = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlay())) {

								@Override
								public void actionPerformed(ActionEvent e) {
									runSelectedApplication();
								}

							});
							btnRunApplication.setToolTipText("Run selected applications.");
							toolBar.add(btnRunApplication);
						}
					}
				}
			}
		}
		lstCategories.setSelectedIndex(0);
		lstProcesses.setSelectedIndex(0);
		setVisible(true);
	}

	@Override
	public void dispose() {
		ThemeUtil.deinstallListeners(this);
		super.dispose();
	}

	private void runSelectedApplication() {
		int selectedIndex = lstProcesses.getSelectedIndex();
		if (selectedIndex == -1) {
			JOptionPane.showMessageDialog(ProcessManager.this, "Please select the application to start first.",
			    "No selection", JOptionPane.WARNING_MESSAGE);
		} else {
			MainFrame mainFrame = MainFrame.getInstance();
			mainFrame.setVisible(true);
			List<ProcessDescriptor> selected = lstProcesses.getSelectedValuesList();
			Iterator<ProcessDescriptor> iterator = selected.iterator();
			startAll(mainFrame, iterator);
			dispose();
		}
	}

	private void startAll(MainFrame mainFrame, Iterator<ProcessDescriptor> iterator) {
		while (iterator.hasNext()) {
			ProcessDescriptor descriptor = iterator.next();
			ProcessController c = new ProcessController(descriptor);
			mainFrame.addProcessController(c);
			c.start();
		}
	}

	private void modifyApplications(Consumer<DefaultListModel<ProcessDescriptor>> applicationsModificator) {
		if (nonNull(currentCategory)) {
			applicationsModificator.accept(currentCategory.getProcessTemplates());
			categories.saveToPreferences();
		}
	}

}
