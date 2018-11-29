package com.github.schuettec.multiprocman.manager;

import static com.github.schuettec.multiprocman.manager.PromptVariable.isValidVariableName;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import com.github.schuettec.multiprocman.Counter;
import com.github.schuettec.multiprocman.FileChooserCallback;
import com.github.schuettec.multiprocman.FileUtil;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;

import javafx.stage.FileChooser.ExtensionFilter;

public class ApplicationEditor extends JDialog {

	private final JPanel terminatePanel = new JPanel();
	private final JPanel mainPanel = new JPanel();
	private final JPanel environmentPanel = new JPanel();
	private final JPanel counterPanel = new JPanel();
	private ProcessDescriptor processDescriptor;
	private JTextField txtTitle;
	private JTextField txtWorkingDir;
	private JComboBox<Charset> comboBox;
	private JTextArea txtCommand;
	private JTabbedPane tabbedPane;
	private JTable tblEnv;
	private DefaultTableModel envVariables;
	private JTable tblExpressions;
	private DefaultTableModel expressions;
	private JCheckBox chckbxEnablesExperimentalAscii;
	private JLabel lblIcon;
	private JPanel pnlColor;
	private JCheckBox chckbxSubsitution;
	private DefaultComboBoxModel<Charset> charsets;
	private JTextArea txtTermCommand;
	private JRadioButton rdbtnUseCustomCommand;
	private JCheckBox chckbxTermUseVariableSubst;
	private JCheckBox checkEnableGitSupport;
	private JCheckBox checkPullAfterCheckout;
	private JTable tblVariables;
	private DefaultTableModel promptVariables;

	/**
	 * Create the dialog.
	 *
	 * @param parent
	 *
	 * @param category
	 */
	private ApplicationEditor(ProcessDescriptor process, Component parent) {
		setIconImage(Resources.getApplicationIcon());
		setModal(true);
		setTitle("Application");
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
		tabbedPane.add("Launch", mainPanel);
		tabbedPane.add("Terminate", terminatePanel);

		JRadioButton rdbtnUseDefaultProcess = new JRadioButton("Use default process termination signal");
		rdbtnUseDefaultProcess.setSelected(true);
		rdbtnUseCustomCommand = new JRadioButton("Use custom command to terminate application");
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnUseDefaultProcess);
		group.add(rdbtnUseCustomCommand);

		txtTermCommand = new JTextArea();

		JScrollPane scrollPane_3 = new JScrollPane();

		JButton btnTermFind = new JButton(new AbstractAction("Find...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtil.showFileChooser(FileUtil.Type.OPEN, new FileChooserCallback() {

					@Override
					public void fileSelected(File file, ExtensionFilter extension) {
						txtTermCommand.setText(file.getAbsolutePath());
					}
				});
			}
		});

		JButton btnTermInsertVariable = new JButton(insertVariableAction(txtTermCommand));

		JButton btnTermShowSubstitution = new JButton(showSubstitutionAction(txtTermCommand));

		chckbxTermUseVariableSubst = new JCheckBox("Enable environment variable substitution");
		GroupLayout gl_terminatePanel = new GroupLayout(terminatePanel);
		gl_terminatePanel.setHorizontalGroup(gl_terminatePanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_terminatePanel.createSequentialGroup()
		        .addGroup(gl_terminatePanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(Alignment.TRAILING, gl_terminatePanel.createSequentialGroup()
		                .addContainerGap(103, Short.MAX_VALUE)
		                .addComponent(btnTermShowSubstitution)
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addComponent(btnTermInsertVariable)
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addComponent(btnTermFind, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE))
		            .addGroup(gl_terminatePanel.createSequentialGroup()
		                .addGap(29)
		                .addGroup(gl_terminatePanel.createParallelGroup(Alignment.LEADING)
		                    .addComponent(chckbxTermUseVariableSubst, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
		                    .addComponent(scrollPane_3, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)))
		            .addGroup(gl_terminatePanel.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(rdbtnUseCustomCommand, GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))
		            .addGroup(gl_terminatePanel.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(rdbtnUseDefaultProcess, GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)))
		        .addContainerGap()));
		gl_terminatePanel.setVerticalGroup(gl_terminatePanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_terminatePanel.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(rdbtnUseDefaultProcess)
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addComponent(rdbtnUseCustomCommand)
		        .addGap(5)
		        .addComponent(scrollPane_3, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
		        .addGap(3)
		        .addComponent(chckbxTermUseVariableSubst)
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addGroup(gl_terminatePanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(btnTermFind)
		            .addComponent(btnTermInsertVariable)
		            .addComponent(btnTermShowSubstitution))
		        .addGap(19)));

		scrollPane_3.setViewportView(txtTermCommand);
		terminatePanel.setLayout(gl_terminatePanel);
		tabbedPane.add("Environment", environmentPanel);

		JPanel pnlVariables = new JPanel();
		tabbedPane.addTab("Variables", null, pnlVariables, "Variables and promts");

		JLabel lblVariablesDefinedOn = new JLabel(
		    "<html>Variables defined on this page can be used in the execution command. A variable can be configured to trigger an input prompt when the application is launched.</html>");
		lblVariablesDefinedOn.setVerticalAlignment(SwingConstants.TOP);

		JScrollPane scrollPane_4 = new JScrollPane();
		GroupLayout gl_pnlVariables = new GroupLayout(pnlVariables);
		gl_pnlVariables.setHorizontalGroup(gl_pnlVariables.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_pnlVariables.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_pnlVariables.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblVariablesDefinedOn, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
		            .addComponent(scrollPane_4, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
		        .addContainerGap()));
		gl_pnlVariables.setVerticalGroup(gl_pnlVariables.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_pnlVariables.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(lblVariablesDefinedOn, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addComponent(scrollPane_4, GroupLayout.PREFERRED_SIZE, 408, GroupLayout.PREFERRED_SIZE)
		        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		this.promptVariables = new DefaultTableModel(new Object[][] {}, new String[] {
		    "Name", "Message", "Default", "Prompt", "Selection", "Selection values (CSV)"
		}) {
			@Override
			public Class getColumnClass(int column) {
				return getValueAt(0, column).getClass();
			}
		};
		tblVariables = new JTable(promptVariables);
		tblVariables.getColumnModel()
		    .getColumn(0)
		    .setCellEditor(new IntegerEditor());
		scrollPane_4.setViewportView(tblVariables);

		JToolBar toolbarVariables = new JToolBar();
		toolbarVariables.setRollover(true);
		toolbarVariables.setOrientation(SwingConstants.VERTICAL);
		toolbarVariables.setFloatable(false);
		scrollPane_4.setRowHeaderView(toolbarVariables);

		JButton btnPlusVariables = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				promptVariables.addRow(new Object[] {
				    "", "", "", true, false, ""
				});
			}
		});
		btnPlusVariables.setToolTipText("Add new variables.");
		toolbarVariables.add(btnPlusVariables);

		JButton btnMinusVariables = new JButton(new AbstractAction(null, new ImageIcon(Resources.getMinus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tblVariables.getSelectedRow();
				if (row == -1) {
					JOptionPane.showMessageDialog(ApplicationEditor.this, "Please select the variable to remove first.",
					    "No selection", JOptionPane.WARNING_MESSAGE);
				} else {
					promptVariables.removeRow(row);
					if (promptVariables.getRowCount() > 0) {
						tblVariables.setRowSelectionInterval(promptVariables.getRowCount() - 1, promptVariables.getRowCount() - 1);
					}
				}
			}
		});
		btnMinusVariables.setToolTipText("Remove variable.");
		toolbarVariables.add(btnMinusVariables);

		pnlVariables.setLayout(gl_pnlVariables);
		tabbedPane.add("Counter expressions", counterPanel);

		JLabel lblyouCanAdd = new JLabel(
		    "<html>You can add regular expression patterns that will be counted while watching the application output. Up to 6 counter expressions are supported per application. Use the Java Regular Expression syntax for your counter expressions.</html>");

		JScrollPane scrollPane_2 = new JScrollPane();
		GroupLayout gl_counterPanel = new GroupLayout(counterPanel);
		gl_counterPanel.setHorizontalGroup(gl_counterPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_counterPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_counterPanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(gl_counterPanel.createSequentialGroup()
		                .addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
		                .addContainerGap())
		            .addGroup(gl_counterPanel.createSequentialGroup()
		                .addComponent(lblyouCanAdd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                .addGap(0)))));
		gl_counterPanel.setVerticalGroup(gl_counterPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_counterPanel.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(lblyouCanAdd)
		        .addGap(8)
		        .addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
		        .addContainerGap()));

		tblExpressions = new JTable();
		this.expressions = new DefaultTableModel(new Object[][] {}, new String[] {
		    "Name", "Regular expressions", "Color"
		});
		tblExpressions.setModel(expressions);
		TableColumn column = tblExpressions.getColumnModel()
		    .getColumn(2);
		column.setCellEditor(new ColorChooserCellEditor());
		column.setCellRenderer(new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			    int row, int column) {
				JLabel label = new JLabel();
				Color color = (Color) value;
				label.setForeground(color);
				label.setBackground(color);
				label.setOpaque(true);
				return label;
			}
		});
		scrollPane_2.setViewportView(tblExpressions);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		toolBar_1.setRollover(true);
		toolBar_1.setOrientation(SwingConstants.VERTICAL);
		scrollPane_2.setRowHeaderView(toolBar_1);

		JButton btnPlusExpression = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				expressions.addRow(new Object[] {
				    "", "", Color.GREEN
				});
			}
		});
		btnPlusExpression.setToolTipText("Add new counter expression.");
		toolBar_1.add(btnPlusExpression);

		JButton btnMinusExpression = new JButton(new AbstractAction(null, new ImageIcon(Resources.getMinus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tblExpressions.getSelectedRow();
				if (row == -1) {
					JOptionPane.showMessageDialog(ApplicationEditor.this, "Please select the expression to remove first.",
					    "No selection", JOptionPane.WARNING_MESSAGE);
				} else {
					expressions.removeRow(row);
					if (expressions.getRowCount() > 0) {
						tblExpressions.setRowSelectionInterval(expressions.getRowCount() - 1, expressions.getRowCount() - 1);
					}
				}
			}
		});
		btnMinusExpression.setToolTipText("Remove counter expression.");
		toolBar_1.add(btnMinusExpression);
		counterPanel.setLayout(gl_counterPanel);

		JLabel lblNewLabel_1 = new JLabel(
		    "<html>The environment variables are inherited from this application. To override the environment variables for this application, fill the following key/value map. The variables are for this application runtime only.</html>");

		JScrollPane scrollPane_1 = new JScrollPane();
		GroupLayout gl_environmentPanel = new GroupLayout(environmentPanel);
		gl_environmentPanel.setHorizontalGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_environmentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(gl_environmentPanel.createSequentialGroup()
		                .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
		                .addContainerGap())
		            .addComponent(lblNewLabel_1))));
		gl_environmentPanel.setVerticalGroup(gl_environmentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_environmentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(lblNewLabel_1)
		        .addGap(8)
		        .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
		        .addContainerGap()));

		tblEnv = new JTable();
		this.envVariables = new DefaultTableModel(new Object[][] {}, new String[] {
		    "Variable name", "Value"
		});
		tblEnv.setModel(envVariables);
		scrollPane_1.setViewportView(tblEnv);

		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		scrollPane_1.setRowHeaderView(toolBar);

		JButton btnPlus = new JButton(new AbstractAction(null, new ImageIcon(Resources.getPlus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				envVariables.addRow(new String[] {
				    "", ""
				});
			}
		});
		toolBar.add(btnPlus);

		JButton btnMinus = new JButton(new AbstractAction(null, new ImageIcon(Resources.getMinus())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tblEnv.getSelectedRow();
				if (row == -1) {
					JOptionPane.showMessageDialog(ApplicationEditor.this, "Please select the variable to remove first.",
					    "No selection", JOptionPane.WARNING_MESSAGE);
				} else {
					envVariables.removeRow(row);
					if (envVariables.getRowCount() > 0) {
						tblEnv.setRowSelectionInterval(envVariables.getRowCount() - 1, envVariables.getRowCount() - 1);
					}
				}
			}
		});
		toolBar.add(btnMinus);
		environmentPanel.setLayout(gl_environmentPanel);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		lblIcon = new JLabel(new ImageIcon(Resources.getTerminal()));
		lblIcon.setSize(new Dimension(24, 24));
		lblIcon.setPreferredSize(new Dimension(24, 24));
		JLabel lblForIcon = new JLabel("Category icon: ");
		lblForIcon.setHorizontalAlignment(SwingConstants.RIGHT);
		lblForIcon.setHorizontalTextPosition(SwingConstants.RIGHT);

		JLabel lblName = new JLabel("Title:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel lblNewLabel = new JLabel("Command:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton btnFindIcon = new JButton(new AbstractAction("Find...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtil.showFileChooser(FileUtil.Type.OPEN, l -> {
					String[] suffixes = ImageIO.getReaderFileSuffixes();
					List<String> suffixList = new ArrayList<>(suffixes.length);
					for (String suffix : suffixes) {
						suffixList.add("*." + suffix);
					}
					l.add(new ExtensionFilter("Supported image type", suffixList));
				}, new FileChooserCallback() {

					@Override
					public void noFile() {
					}

					@Override
					public void fileSelected(File file, ExtensionFilter extension) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
									BufferedImage read = ImageIO.read(file);
									BufferedImage after = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
									Graphics2D g = (Graphics2D) after.getGraphics();
									double ratio = read.getHeight() / (double) read.getWidth();
									int newHeight = (int) Math.round(24 * ratio);
									g.drawImage(read, 0, 0, 24, newHeight, null);
									lblIcon.setIcon(new ImageIcon(after));
								} catch (IOException e1) {
									JOptionPane.showMessageDialog(ApplicationEditor.this, "Error while loading the selected image file.",
									    "I/O error", JOptionPane.ERROR_MESSAGE);
									e1.printStackTrace();
								}
							}
						});
					}
				});
			}
		});

		JLabel lblPleaseFillIn = new JLabel(
		    "<html>Please fill in the desired data for the new category. When selecting a category icon use 24x24px images, otherwise the images will be scaled automatically.</html>");

		JLabel lblWorkingDirectory = new JLabel("Working directory:");
		lblWorkingDirectory.setHorizontalAlignment(SwingConstants.RIGHT);

		txtTitle = new JTextField();
		txtTitle.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();

		txtWorkingDir = new JTextField();
		txtWorkingDir.setColumns(10);

		JButton btnFindWorkingDir = new JButton(new AbstractAction("Find...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtil.showDirectoryChooser(new FileChooserCallback() {

					@Override
					public void noFile() {
					}

					@Override
					public void fileSelected(File file, ExtensionFilter extension) {
						txtWorkingDir.setText(file.getAbsolutePath());
					}
				});
			}
		});

		JLabel lblColorFor = new JLabel("Color:");
		lblColorFor.setHorizontalAlignment(SwingConstants.RIGHT);

		this.pnlColor = new JPanel();
		pnlColor.setBackground(Color.GREEN);
		pnlColor.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));

		JButton btnSelectColor = new JButton(new AbstractAction("Select color...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(ApplicationEditor.this, "Choose a color", Color.GREEN);
				if (nonNull(color)) {
					pnlColor.setBackground(color);
				}
			}
		});

		JLabel lblCharset = new JLabel("Charset:");
		lblCharset.setHorizontalAlignment(SwingConstants.RIGHT);

		comboBox = new JComboBox();
		SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
		this.charsets = new DefaultComboBoxModel<Charset>();
		for (Charset c : availableCharsets.values()) {
			charsets.addElement(c);
		}
		int defaultIndex = charsets.getIndexOf(Charset.defaultCharset());
		comboBox.setModel(charsets);
		comboBox.setSelectedIndex(defaultIndex);

		JButton btnFindApplication = new JButton(new AbstractAction("Find...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtil.showFileChooser(FileUtil.Type.OPEN, new FileChooserCallback() {

					@Override
					public void fileSelected(File file, ExtensionFilter extension) {
						txtTitle.setText(file.getName());
						txtCommand.setText(file.getAbsolutePath());
						if (nonNull(file.getParent())) {
							txtWorkingDir.setText(file.getParent());
						}
					}
				});
			}
		});

		JButton btnDefaultIcon = new JButton(new AbstractAction("Default icon") {

			@Override
			public void actionPerformed(ActionEvent e) {
				lblIcon.setIcon(new ImageIcon(Resources.getTerminal()));
			}
		});

		this.chckbxEnablesExperimentalAscii = new JCheckBox(
		    "<html>Enables support for backspace ASCII code (used in formatted console outputs).</html>");
		chckbxEnablesExperimentalAscii.setVerticalAlignment(SwingConstants.TOP);

		txtCommand = new JTextArea();
		JButton btnInsertVariable = new JButton(insertVariableAction(txtCommand));

		chckbxSubsitution = new JCheckBox("Enable environment variable substitution");
		JButton btnShowSubstitution = new JButton(showSubstitutionAction(txtCommand));

		GroupLayout gl_contentPanel = new GroupLayout(mainPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                    .addComponent(lblPleaseFillIn, GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                            .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
		                                .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
		                                    Short.MAX_VALUE)
		                                .addComponent(lblForIcon, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
		                            .addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
		                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                            .addGroup(gl_contentPanel.createSequentialGroup()
		                                .addPreferredGap(ComponentPlacement.RELATED)
		                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                                    .addGroup(gl_contentPanel.createSequentialGroup()
		                                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                                            GroupLayout.PREFERRED_SIZE)
		                                        .addPreferredGap(ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
		                                        .addComponent(btnDefaultIcon)
		                                        .addPreferredGap(ComponentPlacement.RELATED)
		                                        .addComponent(btnFindIcon, GroupLayout.PREFERRED_SIZE, 98,
		                                            GroupLayout.PREFERRED_SIZE))
		                                    .addComponent(txtTitle, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)))
		                            .addGroup(gl_contentPanel.createSequentialGroup()
		                                .addGap(6)
		                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
		                                    .addGroup(gl_contentPanel.createSequentialGroup()
		                                        .addComponent(btnShowSubstitution)
		                                        .addPreferredGap(ComponentPlacement.RELATED)
		                                        .addComponent(btnInsertVariable, GroupLayout.PREFERRED_SIZE, 111,
		                                            GroupLayout.PREFERRED_SIZE)
		                                        .addPreferredGap(ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
		                                        .addComponent(btnFindApplication, GroupLayout.PREFERRED_SIZE, 90,
		                                            GroupLayout.PREFERRED_SIZE)
		                                        .addPreferredGap(ComponentPlacement.RELATED)))))))
		                .addGap(10))
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                    .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
		                        .addComponent(lblColorFor, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
		                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                        .addComponent(lblCharset, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 95,
		                            Short.MAX_VALUE))
		                    .addComponent(lblWorkingDirectory, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE))
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                    .addComponent(txtWorkingDir, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
		                    .addComponent(chckbxEnablesExperimentalAscii, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
		                    .addComponent(comboBox, 0, 389, Short.MAX_VALUE)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(chckbxSubsitution, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
		                        .addPreferredGap(ComponentPlacement.UNRELATED)
		                        .addComponent(btnFindWorkingDir, GroupLayout.PREFERRED_SIZE, 88,
		                            GroupLayout.PREFERRED_SIZE))
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
		                        .addComponent(btnSelectColor)))
		                .addContainerGap()))));
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addGap(8)
		        .addComponent(lblPleaseFillIn)
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		            .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
		                .addGroup(gl_contentPanel.createSequentialGroup()
		                    .addGap(11)
		                    .addComponent(lblIcon, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		                    .addComponent(btnFindIcon)
		                    .addComponent(btnDefaultIcon)))
		            .addComponent(lblForIcon, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE)
		            .addComponent(lblName))
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblNewLabel)
		            .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(btnShowSubstitution)
		            .addComponent(btnInsertVariable)
		            .addComponent(btnFindApplication))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(txtWorkingDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE)
		            .addComponent(lblWorkingDirectory))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(chckbxSubsitution)
		            .addComponent(btnFindWorkingDir))
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE)
		            .addComponent(lblCharset))
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		            .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		                .addComponent(btnSelectColor)
		                .addComponent(lblColorFor))
		            .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addComponent(chckbxEnablesExperimentalAscii, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
		        .addGap(0)));

		scrollPane.setViewportView(txtCommand);
		mainPanel.setLayout(gl_contentPanel);

		JPanel pnlGit = new JPanel();
		tabbedPane.addTab("Git support", null, pnlGit, null);

		JLabel lblNewLabel_2 = new JLabel(
		    "<html>On this page you can enable Git support for your application launcher. If enabled, the execution directory of a launcher is interpreted as a Git repository. Further options are available to change branches for execution and pull before launching.</html>");

		checkEnableGitSupport = new JCheckBox("Enable Git support");
		checkEnableGitSupport.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				checkPullAfterCheckout.setEnabled(checkEnableGitSupport.isSelected());
			}
		});

		checkPullAfterCheckout = new JCheckBox("Pull after checkout");
		GroupLayout gl_pnlGit = new GroupLayout(pnlGit);
		gl_pnlGit.setHorizontalGroup(gl_pnlGit.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_pnlGit.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_pnlGit.createParallelGroup(Alignment.LEADING)
		            .addComponent(checkEnableGitSupport, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
		            .addComponent(lblNewLabel_2, GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
		            .addGroup(gl_pnlGit.createSequentialGroup()
		                .addGap(21)
		                .addComponent(checkPullAfterCheckout, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)))
		        .addContainerGap()));
		gl_pnlGit.setVerticalGroup(gl_pnlGit.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_pnlGit.createSequentialGroup()
		        .addContainerGap()
		        .addComponent(lblNewLabel_2, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addComponent(checkEnableGitSupport)
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addComponent(checkPullAfterCheckout)
		        .addContainerGap(369, Short.MAX_VALUE)));
		pnlGit.setLayout(gl_pnlGit);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(new AbstractAction("OK") {

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

		setProcessDescriptor(process);

		setVisible(true);
	}

	private AbstractAction showSubstitutionAction(JTextComponent textComp) {
		return new AbstractAction("Show env. variable substitution") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ApplicationEditor.this,
				    "<html><body><p style='width: 640px;'>The following command will be substituted:<br/><tt>"
				        + ProcessDescriptor.fillEnvironmentVariables(textComp.getText())
				        + "</tt></p>The following workind directory will be substituted:<br/><tt>"
				        + ProcessDescriptor.fillEnvironmentVariables(txtWorkingDir.getText())
				        + "</tt><br/><b>Note: Only enviornment variables have been replaced.</body></html>",
				    "Command substitution", JOptionPane.INFORMATION_MESSAGE);
			}
		};
	}

	private AbstractAction insertVariableAction(final JTextArea textComponent) {
		return new AbstractAction("Insert variable") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Vector keys = new Vector<>(System.getenv()
				    .keySet());
				JComboBox jcd = new JComboBox(keys);

				// create a JOptionPane
				JOptionPane jop = new JOptionPane("Please Select a variable to insert:", JOptionPane.QUESTION_MESSAGE,
				    JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);

				// add combos to JOptionPane
				jop.add(jcd);

				JDialog diag = new JDialog(ApplicationEditor.this, "Select variable");

				JPanel contentPane = new JPanel(new BorderLayout());
				contentPane.add(jop, BorderLayout.CENTER);
				JButton okButton = new JButton(new AbstractAction("OK") {

					@Override
					public void actionPerformed(ActionEvent e) {
						diag.dispose();
						String variable = (String) jcd.getSelectedItem();
						String placeholder = ProcessDescriptor.getEnvironmentVariablePlaceholder(variable);
						textComponent.insert(placeholder, textComponent.getCaretPosition());
					}
				});
				JPanel buttonPanel = new JPanel();
				buttonPanel.add(okButton);
				contentPane.add(buttonPanel, BorderLayout.SOUTH);

				// create a JDialog and add JOptionPane to it
				diag.setModal(true);
				diag.setContentPane(contentPane);
				diag.pack();
				diag.setLocationRelativeTo(ApplicationEditor.this);
				diag.setVisible(true);

			}
		};
	}

	private void setProcessDescriptor(ProcessDescriptor process) {
		if (isNull(process)) {
			this.processDescriptor = new ProcessDescriptor();
		} else {
			this.processDescriptor = process;
			lblIcon.setIcon(processDescriptor.getIcon());
			txtTitle.setText(processDescriptor.getTitle());
			txtCommand.setText(processDescriptor.getCommand());
			if (processDescriptor.hasExecutionDirectory()) {
				txtWorkingDir.setText(processDescriptor.getExecutionDirectory());
			}
			pnlColor.setBackground(processDescriptor.getColor());
			int charsetIndex = charsets.getIndexOf(processDescriptor.getCharset());
			comboBox.setSelectedIndex(charsetIndex);

			if (processDescriptor.hasEnvironmentVariables()) {
				Map<String, String> environment = processDescriptor.getEnvironment();
				Iterator<Entry<String, String>> it = environment.entrySet()
				    .iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String key = entry.getKey();
					String value = entry.getValue();
					if (!key.trim()
					    .isEmpty()) {
						envVariables.addRow(new String[] {
						    key.trim(), value
						});
					}
				}
			}

			if (processDescriptor.hasCounters()) {
				for (Counter c : processDescriptor.getCounters()) {
					expressions.addRow(new Object[] {
					    c.getName(), c.getRegexp(), c.getColor()
					});
				}
			}

			chckbxEnablesExperimentalAscii.setSelected(processDescriptor.isSupportAsciiCodes());
			chckbxSubsitution.setSelected(processDescriptor.isVariableSubstitution());

			rdbtnUseCustomCommand.setSelected(processDescriptor.isUseTerminationCommand());
			chckbxTermUseVariableSubst.setSelected(processDescriptor.isTerminationVariableSubstitution());
			txtTermCommand.setText(processDescriptor.getTerminationCommand());

			checkEnableGitSupport.setSelected(processDescriptor.isEnableGitSupport());
			checkPullAfterCheckout.setSelected(processDescriptor.isPullAfterCheckout());

			checkPullAfterCheckout.setEnabled(checkEnableGitSupport.isSelected());

			if (processDescriptor.hasPromptVariables()) {
				for (PromptVariable pv : processDescriptor.getPromptVariables()) {
					promptVariables.addRow(new Object[] {
					    pv.getName(), pv.getMessage(), pv.getDefaultValue(), pv.isPrompt(), pv.isSelection(),
					    pv.getSelectionValuesAsCSV()
					});
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private void performCancel() {
		ApplicationEditor.this.processDescriptor = null;
		dispose();
	}

	private void performOk() {
		ApplicationEditor.this.processDescriptor.setIcon((ImageIcon) this.lblIcon.getIcon());
		String title = txtTitle.getText()
		    .trim();
		if (title.isEmpty()) {
			JOptionPane.showMessageDialog(ApplicationEditor.this, "Title must not be empty!", "Title not set",
			    JOptionPane.WARNING_MESSAGE);
			return;
		}
		ApplicationEditor.this.processDescriptor.setTitle(title);
		String command = txtCommand.getText()
		    .trim();
		if (command.isEmpty()) {
			JOptionPane.showMessageDialog(ApplicationEditor.this, "Command must not be empty!", "Command not set",
			    JOptionPane.WARNING_MESSAGE);

		}
		ApplicationEditor.this.processDescriptor.setCommand(command);
		String workingDir = txtWorkingDir.getText();
		if (workingDir.trim()
		    .isEmpty()) {
			ApplicationEditor.this.processDescriptor.setExecutionDirectory(null);
		} else {
			ApplicationEditor.this.processDescriptor.setExecutionDirectory(workingDir.trim());
		}
		ApplicationEditor.this.processDescriptor.setColor(pnlColor.getBackground());
		ApplicationEditor.this.processDescriptor.setCharset((Charset) comboBox.getSelectedItem());

		Map<String, String> vars = new HashMap<>();
		for (int i = 0; i < envVariables.getRowCount(); i++) {
			String key = (String) envVariables.getValueAt(i, 0);
			String value = (String) envVariables.getValueAt(i, 1);
			vars.put(key, value);
		}
		if (!vars.isEmpty()) {
			ApplicationEditor.this.processDescriptor.setEnvironment(vars);
		}

		List<Counter> counters = new LinkedList<>();
		for (int i = 0; i < expressions.getRowCount(); i++) {
			String name = ((String) expressions.getValueAt(i, 0)).trim();
			String expression = ((String) expressions.getValueAt(i, 1)).trim();
			Color color = (Color) expressions.getValueAt(i, 2);
			if (!name.isEmpty() && !expression.isEmpty()) {
				Counter counterDescriptor = new Counter(name, expression, color);
				boolean valid = counterDescriptor.testRegexp();
				if (!valid) {
					return;
				}
				counters.add(counterDescriptor);
			}
		}
		ApplicationEditor.this.processDescriptor.setCounters(counters);
		ApplicationEditor.this.processDescriptor.setSupportAsciiCodes(chckbxEnablesExperimentalAscii.isSelected());
		ApplicationEditor.this.processDescriptor.setVariableSubstitution(chckbxSubsitution.isSelected());

		ApplicationEditor.this.processDescriptor.setUseTerminationCommand(rdbtnUseCustomCommand.isSelected());
		ApplicationEditor.this.processDescriptor
		    .setTerminationVariableSubstitution(chckbxTermUseVariableSubst.isSelected());
		ApplicationEditor.this.processDescriptor.setTerminationCommand(txtTermCommand.getText());

		ApplicationEditor.this.processDescriptor.setEnableGitSupport(checkEnableGitSupport.isSelected());
		ApplicationEditor.this.processDescriptor.setPullAfterCheckout(checkPullAfterCheckout.isSelected());

		List<PromptVariable> toSet = new LinkedList<>();
		for (int i = 0; i < promptVariables.getRowCount(); i++) {
			String name = ((String) promptVariables.getValueAt(i, 0)).trim();
			String message = ((String) promptVariables.getValueAt(i, 1)).trim();
			String defaultValue = ((String) promptVariables.getValueAt(i, 2)).trim();
			boolean prompt = (boolean) promptVariables.getValueAt(i, 3);
			boolean selection = (boolean) promptVariables.getValueAt(i, 4);
			String selectionValues = (String) promptVariables.getValueAt(i, 5);
			toSet.add(new PromptVariable(name, message, defaultValue, prompt, selection,
			    PromptVariable.parseSelectionValues(selectionValues)));
		}
		ApplicationEditor.this.processDescriptor.setPromptVariables(toSet);

		dispose();
	}

	public static ProcessDescriptor newProcess(Component parent) {
		ApplicationEditor editor = new ApplicationEditor(null, parent);
		return editor.getProcessDescriptor();
	}

	public static void editProcessDescriptor(ProcessDescriptor processDescriptor, Component parent) {
		new ApplicationEditor(processDescriptor, parent);
	}

	private ProcessDescriptor getProcessDescriptor() {
		return this.processDescriptor;
	}

	class IntegerEditor extends DefaultCellEditor {
		JTextField textField;
		private boolean DEBUG = false;

		public IntegerEditor() {
			super(new JTextField());

			setClickCountToStart(2);

			textField = (JTextField) getComponent();
			textField.setBorder(new LineBorder(Color.BLACK));

			textField.setHorizontalAlignment(JTextField.TRAILING);

			// React when the user presses Enter while the editor is
			// active. (Tab is handled as specified by
			// JFormattedTextField's focusLostBehavior property.)
			textField.getInputMap()
			    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");

			textField.getActionMap()
			    .put("check", new AbstractAction() {
				    @Override
				    public void actionPerformed(ActionEvent e) {
					    if (!isValidVariableName(textField.getText())) {
						    revert();
					    }
					    textField.postActionEvent(); // stop editing
				    }
			    });
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			return true;
		}

		// Override to invoke setValue on the formatted text field.
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			JTextField ftf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			ftf.setText((String) value);
			return ftf;
		}

		// Override to check whether the edit is valid,
		// setting the value if it is and complaining if
		// it isn't. If it's OK for the editor to go
		// away, we need to invoke the superclass's version
		// of this method so that everything gets cleaned up.
		@Override
		public boolean stopCellEditing() {
			if (!isValidVariableName(textField.getText())) {
				revert();
			}

			return super.stopCellEditing();
		}

		protected void revert() {
			Toolkit.getDefaultToolkit()
			    .beep();
			textField.selectAll();
			StringBuilder b = new StringBuilder("Variable names may not include the following characters: ");
			String[] deniedStrings = PromptVariable.deniedStrings();
			for (int i = 0; i < deniedStrings.length; i++) {
				b.append("'")
				    .append(deniedStrings[i])
				    .append("'");
				if (i < deniedStrings.length - 1) {
					b.append(", ");
				}
			}
			JOptionPane.showMessageDialog(ApplicationEditor.this, b.toString(), "Invalid expression",
			    JOptionPane.ERROR_MESSAGE);
			textField.setText(PromptVariable.escape(textField.getText()));
		}

	}
}
