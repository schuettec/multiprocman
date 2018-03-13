package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.github.schuettec.multiprocman.Counter;
import com.github.schuettec.multiprocman.FileChooserCallback;
import com.github.schuettec.multiprocman.FileUtil;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;

import javafx.stage.FileChooser.ExtensionFilter;

public class ApplicationEditor extends JDialog {

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
	private DefaultTableModel variables;
	private JTable tblExpressions;
	private DefaultTableModel expressions;
	private JCheckBox chckbxEnablesExperimentalAscii;

	/**
	 * Create the dialog.
	 *
	 * @param category
	 */
	private ApplicationEditor(ProcessDescriptor process) {
		setIconImage(Resources.getApplicationIcon());
		setModal(true);
		setTitle("Application");
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
		this.setPreferredSize(new Dimension(430, 200));
		this.setSize(new Dimension(487, 593));
		this.setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Application", mainPanel);
		tabbedPane.add("Environment", environmentPanel);
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

			@Override
			public void actionPerformed(ActionEvent e) {
				variables.addRow(new String[] {
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

		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel lblIcon = new JLabel(new ImageIcon(Resources.getTerminal()));
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

		JPanel pnlColor = new JPanel();
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
		DefaultComboBoxModel<Charset> charsets = new DefaultComboBoxModel<Charset>();
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

		JLabel lblAsciiCodeSupport = new JLabel("ASCII code support:");

		this.chckbxEnablesExperimentalAscii = new JCheckBox(
		    "<html>Enables experimental ASCII code support for formatted application output.</html>");

		GroupLayout gl_contentPanel = new GroupLayout(mainPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		            .addComponent(btnFindWorkingDir, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
		            .addComponent(lblPleaseFillIn, Alignment.LEADING)
		            .addGroup(Alignment.LEADING, gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addGap(1)
		                        .addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
		                            Short.MAX_VALUE))
		                    .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(lblForIcon, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
		                .addGap(2)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addPreferredGap(ComponentPlacement.RELATED)
		                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                            GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
		                        .addComponent(btnDefaultIcon)
		                        .addPreferredGap(ComponentPlacement.RELATED)
		                        .addComponent(btnFindIcon, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE))
		                    .addComponent(txtTitle, GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)))
		            .addGroup(Alignment.LEADING, gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
		                    .addComponent(lblColorFor, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
		                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(lblWorkingDirectory, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
		                        Short.MAX_VALUE)
		                    .addComponent(lblCharset, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
		                    .addComponent(lblAsciiCodeSupport))
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(chckbxEnablesExperimentalAscii, GroupLayout.DEFAULT_SIZE, 338,
		                            Short.MAX_VALUE)
		                        .addPreferredGap(ComponentPlacement.RELATED))
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
		                        .addComponent(btnSelectColor))
		                    .addComponent(txtWorkingDir, 341, 341, 341)
		                    .addComponent(comboBox, 0, 341, Short.MAX_VALUE)))
		            .addComponent(btnFindApplication, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE))
		        .addGap(0)));
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
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addComponent(btnFindApplication))
		            .addComponent(lblNewLabel))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(lblCharset)
		            .addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblWorkingDirectory)
		            .addComponent(txtWorkingDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                GroupLayout.PREFERRED_SIZE))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGap(29)
		                .addComponent(lblColorFor))
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addComponent(btnFindWorkingDir)
		                .addGap(3)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                    .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(btnSelectColor))))
		        .addPreferredGap(ComponentPlacement.UNRELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		            .addComponent(lblAsciiCodeSupport)
		            .addComponent(chckbxEnablesExperimentalAscii, GroupLayout.PREFERRED_SIZE, 30,
		                GroupLayout.PREFERRED_SIZE))
		        .addGap(7)));

		txtCommand = new JTextArea();
		scrollPane.setViewportView(txtCommand);
		mainPanel.setLayout(gl_contentPanel);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(new AbstractAction("OK") {

					@Override
					public void actionPerformed(ActionEvent e) {
						performOk(lblIcon, pnlColor);
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

		if (isNull(process)) {
			this.processDescriptor = new ProcessDescriptor();
		} else {
			this.processDescriptor = process;
			lblIcon.setIcon(processDescriptor.getIcon());
			txtTitle.setText(processDescriptor.getTitle());
			txtCommand.setText(processDescriptor.getCommand());
			if (processDescriptor.hasExecutionDirectory()) {
				File executionDirectory = processDescriptor.getExecutionDirectory();
				txtWorkingDir.setText(executionDirectory.getAbsolutePath());
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
						variables.addRow(new String[] {
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

		}

		setVisible(true);
	}

	private void performCancel() {
		ApplicationEditor.this.processDescriptor = null;
		dispose();
	}

	private void performOk(JLabel lblIcon, JPanel pnlColor) {
		ApplicationEditor.this.processDescriptor.setIcon((ImageIcon) lblIcon.getIcon());
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
			ApplicationEditor.this.processDescriptor.setExecutionDirectory(new File(workingDir.trim()));
		}
		ApplicationEditor.this.processDescriptor.setColor(pnlColor.getBackground());
		ApplicationEditor.this.processDescriptor.setCharset((Charset) comboBox.getSelectedItem());

		Map<String, String> vars = new HashMap<>();
		for (int i = 0; i < variables.getRowCount(); i++) {
			String key = (String) variables.getValueAt(i, 0);
			String value = (String) variables.getValueAt(i, 1);
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

		dispose();
	}

	public static ProcessDescriptor newProcess() {
		ApplicationEditor editor = new ApplicationEditor(null);
		return editor.getProcessDescriptor();
	}

	public static void editProcessDescriptor(ProcessDescriptor processDescriptor) {
		new ApplicationEditor(processDescriptor);
	}

	private ProcessDescriptor getProcessDescriptor() {
		return this.processDescriptor;
	}
}
