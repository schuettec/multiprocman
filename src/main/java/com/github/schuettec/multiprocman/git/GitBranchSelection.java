package com.github.schuettec.multiprocman.git;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class GitBranchSelection extends JDialog {

	private static final Font MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private static final int WARN_COL = 0;
	private static final int TITLE_COL = 1;
	private static final int BRANCH_COL = 2;
	private static final int PULL_COL = 3;

	protected static final String[] columnNames = {
	    "", "Application", "Branch selection", "Pull before"
	};

	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTable table;

	private List<BranchSelectionResult> descriptors;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try {
			GitBranchSelection dialog = new GitBranchSelection();
			ProcessDescriptor pd = new ProcessDescriptor();
			pd.setTitle("A");
			dialog.addProcessDescriptor(pd);

			pd = new ProcessDescriptor();
			pd.setTitle("B");
			dialog.addProcessDescriptor(pd);

			pd = new ProcessDescriptor();
			pd.setTitle("C");
			dialog.addProcessDescriptor(pd);

			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public GitBranchSelection() {
		this.descriptors = new LinkedList<>();
		ThemeUtil.setLookAndFeel();
		setIconImage(Resources.getApplicationIcon());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(700, 480));
		ThemeUtil.loadWindow(this);
		ThemeUtil.installListeners(this);

		setModal(true);
		setTitle("Git branch selection");
		setBounds(100, 100, 452, 475);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblSomeSelectedLaunch = new JLabel(
		    "<html>Some selected launch configurations have enable Git support. Please choose the Git branch to work with and confirm the launch operation.</html>");

		scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		            .addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
		            .addComponent(lblSomeSelectedLaunch, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE))
		        .addContainerGap()));
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addComponent(lblSomeSelectedLaunch)
		        .addGap(8)
		        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
		        .addContainerGap()));

		table = new JTable(new ComboBoxTableModel());
		scrollPane.setViewportView(table);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setPreferredSize(new Dimension(91, 23));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setPreferredSize(new Dimension(91, 23));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		// Create the combo box editor
		JComboBox comboBox = new JComboBox();
		comboBox.setEditable(true);
		ComboBoxCellEditor editor = new ComboBoxCellEditor();

		// Assign the editor to the second column
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(BRANCH_COL)
		    .setCellRenderer(new MonospacesCellRenderer());
		tcm.getColumn(BRANCH_COL)
		    .setCellEditor(editor);

		// Set row heighht
		table.setRowHeight(20);

		table.getColumnModel()
		    .getColumn(WARN_COL)
		    .setPreferredWidth(14);

		table.setPreferredScrollableViewportSize(table.getPreferredSize());
	}

	public boolean isEmpty() {
		return descriptors.isEmpty();
	}

	public boolean addProcessDescriptor(ProcessDescriptor pd) {
		return descriptors.add(new BranchSelectionResult(pd));
	}

	public void clear() {
		descriptors.clear();
	}

	class MonospacesCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		    int row, int column) {
			Component toReturn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			toReturn.setFont(MONOSPACED);
			return toReturn;
		}
	}

	class ComboBoxCellEditor extends DefaultCellEditor {
		// Declare a model that is used for adding the elements to the `ComboBox`
		private DefaultComboBoxModel model;

		public ComboBoxCellEditor() {
			super(new JComboBox());
			this.model = (DefaultComboBoxModel) ((JComboBox) getComponent()).getModel();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			BranchSelectionResult selection = descriptors.get(row);
			List<String> obtainedList = selection.getAllBranches();
			model.removeAllElements();
			for (int i = 0; i < obtainedList.size(); i++) {
				model.addElement(obtainedList.get(i));
			}
			model.setSelectedItem(selection.getCurrentBranch());
			Component toReturn = super.getTableCellEditorComponent(table, value, isSelected, row, column);
			toReturn.setFont(MONOSPACED);
			return toReturn;
		}
	}

	class ComboBoxTableModel extends AbstractTableModel {

		// Implementation of TableModel interface
		@Override
		public int getRowCount() {
			return descriptors.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			BranchSelectionResult processDescriptor = descriptors.get(row);
			if (column == WARN_COL) {
				if (processDescriptor.isSaveToCheckout()) {
					return "OK";
				} else {
					return new ImageIcon(Resources.getWarning());
				}
			} else if (column == TITLE_COL) {
				return processDescriptor.getTitle();
			} else if (column == BRANCH_COL) {
				return processDescriptor.getSelectedBranch();
			} else if (column == PULL_COL) {
				return processDescriptor.isPullBeforeCheckout();
			} else {
				return "undefined";
			}
		}

		@Override
		public Class getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == BRANCH_COL || column == PULL_COL;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == BRANCH_COL) {
				descriptors.get(row)
				    .setSelectedBranch((String) value);
				fireTableRowsUpdated(row, row);
			}
			if (column == PULL_COL) {
				descriptors.get(row)
				    .setPullBeforeCheckout((boolean) value);
				fireTableRowsUpdated(row, row);
			}
		}
	}

}
