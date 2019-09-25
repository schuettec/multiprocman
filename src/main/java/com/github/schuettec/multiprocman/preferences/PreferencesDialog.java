package com.github.schuettec.multiprocman.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.github.schuettec.multiprocman.Resources;

public class PreferencesDialog extends JDialog {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel environmentPanel = new JPanel();
	private JTabbedPane tabbedPane;
	private JTable tblEnv;
	private DefaultTableModel variables;
	private Preferences preferences;

	/**
	 * Create the dialog.
	 *
	 * @param parent
	 *
	 * @param category
	 */
	private PreferencesDialog(Preferences preferences, Component parent) {
		this.preferences = preferences;
		setIconImage(Resources.getApplicationIcon());
		setModal(true);
		setTitle("Preferences");
		this.setPreferredSize(new Dimension(540, 580));
		this.setSize(new Dimension(540, 580));
		this.setLocationRelativeTo(parent);
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
				performCancel();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});

		getContentPane().setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Global variables", environmentPanel);

		JLabel lblNewLabel_1 = new JLabel(
		    "<html>Variables defined here can be used in launch configurations. This helps to create more portable launch configurations since path's can be configured globally for a specific system.</html>");

		JScrollPane scrollPane_1 = new JScrollPane();

		JLabel lblGlobalVariablesAre = new JLabel(
		    "<html>Global variables are used like user-defined variables. The latter are evaluated at first, so that user-defined variables override global variables.</html>");
		GroupLayout gl_environmentPanel = new GroupLayout(environmentPanel);
		gl_environmentPanel.setHorizontalGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_environmentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(gl_environmentPanel.createSequentialGroup()
		                .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
		                .addContainerGap())
		            .addComponent(lblNewLabel_1)
		            .addGroup(Alignment.TRAILING, gl_environmentPanel.createSequentialGroup()
		                .addComponent(lblGlobalVariablesAre, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
		                .addContainerGap()))));
		gl_environmentPanel.setVerticalGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_environmentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(lblNewLabel_1)
		        .addGap(8)
		        .addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 383, GroupLayout.PREFERRED_SIZE)
		        .addPreferredGap(ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
		        .addComponent(lblGlobalVariablesAre)
		        .addContainerGap()));

		tblEnv = new JTable();
		this.variables = new DefaultTableModel(new Object[][] {}, new String[] {
		    "Variable name", "Value"
		});
		tblEnv.setModel(variables);
		scrollPane_1.setViewportView(tblEnv);

		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		scrollPane_1.setRowHeaderView(toolBar);

		JButton btnPlus = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlus())) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				variables.addRow(new String[] {
				    "", ""
				});
			}
		});
		toolBar.add(btnPlus);

		JButton btnMinus = new JButton(new AbstractAction(null, new ImageIcon(Resources.getMinus())) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tblEnv.getSelectedRow();
				if (row == -1) {
					JOptionPane.showMessageDialog(PreferencesDialog.this, "Please select the variable to remove first.",
					    "No selection", JOptionPane.WARNING_MESSAGE);
				} else {
					variables.removeRow(row);
					if (variables.getRowCount() > 0) {
						tblEnv.setRowSelectionInterval(variables.getRowCount() - 1, variables.getRowCount() - 1);
					}
				}
			}
		});
		toolBar.add(btnMinus);
		environmentPanel.setLayout(gl_environmentPanel);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(new AbstractAction("OK") {

					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						performOk();
					}

				});
				okButton.setPreferredSize(new Dimension(91, 23));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
				getRootPane().getActionMap()
				    .put("OK", okButton.getAction());
			}
			{
				JButton cancelButton = new JButton(new AbstractAction("Cancel") {

					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						performCancel();
					}

				});
				cancelButton.setPreferredSize(new Dimension(91, 23));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL");
				getRootPane().getActionMap()
				    .put("CANCEL", cancelButton.getAction());
			}
		}

		initializeData(preferences);

		setVisible(true);
	}

	private void initializeData(Preferences preferences) {
		preferences.getGlobalVariables()
		    .entrySet()
		    .stream()
		    .forEach(entry -> this.variables.addRow(new Object[] {
		        entry.getKey(), entry.getValue()
				}));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private void performCancel() {
		dispose();
	}

	private void performOk() {
		Map<String, String> variableMap = new Hashtable<>();
		int rowCount = this.variables.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Object key = this.variables.getValueAt(i, 0);
			Object value = this.variables.getValueAt(i, 1);
			variableMap.put(key.toString(), value.toString());
		}
		preferences.setGlobalVariables(variableMap);

		preferences.save();
		dispose();
	}

	public static void showPreferences() {
		Preferences preferences = Preferences.loadFromPreferences();
		new PreferencesDialog(preferences, null);
	}
}
