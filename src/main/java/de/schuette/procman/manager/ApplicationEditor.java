package de.schuette.procman.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import de.schuette.procman.FileChooserCallback;
import de.schuette.procman.FileUtil;
import de.schuette.procman.ProcessDescriptor;
import de.schuette.procman.Resources;
import de.schuette.procman.themes.ThemeUtil;
import javafx.stage.FileChooser.ExtensionFilter;

public class ApplicationEditor extends JDialog {

	private final JPanel mainPanel = new JPanel();
	private final JPanel environmentPanel = new JPanel();
	private ProcessDescriptor processDescriptor;
	private JTextField txtTitle;
	private JTextField txtWorkingDir;
	private JComboBox<Charset> comboBox;
	private JTextArea txtCommand;
	private JTabbedPane tabbedPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ThemeUtil.setLookAndFeel();
			ProcessDescriptor d = new ProcessDescriptor();
			d.setCharset(Charset.forName("ibm850"));
			ApplicationEditor.editProcessDescriptor(d);
			System.out.println(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * 
	 * @param category
	 */
	private ApplicationEditor(ProcessDescriptor process) {
		setIconImage(Resources.getApplicationIcon());
		setModal(true);
		setTitle("Application");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(430, 200));
		this.setSize(new Dimension(385, 476));
		this.setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Application", mainPanel);
		tabbedPane.add("Environment", environmentPanel);
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

		JButton btnFindWorkingDir = new JButton("Find...");

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
						txtCommand.setText(file.getName());
						txtTitle.setText(file.getName());
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

		GroupLayout gl_contentPanel = new GroupLayout(mainPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblPleaseFillIn)
		            .addGroup(gl_contentPanel.createSequentialGroup()
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
		                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                            GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
		                        .addComponent(btnDefaultIcon)
		                        .addPreferredGap(ComponentPlacement.RELATED)
		                        .addComponent(btnFindIcon, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE))
		                    .addComponent(txtTitle, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)))
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
		                    .addComponent(lblColorFor, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
		                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(lblWorkingDirectory, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
		                        Short.MAX_VALUE)
		                    .addComponent(lblCharset, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
		                    .addComponent(btnFindWorkingDir, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                        .addComponent(btnSelectColor))
		                    .addComponent(txtWorkingDir)
		                    .addComponent(comboBox, 0, 259, Short.MAX_VALUE)))
		            .addComponent(btnFindApplication, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 98,
		                GroupLayout.PREFERRED_SIZE))
		        .addContainerGap()));
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
		                .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
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
		            .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                .addComponent(pnlColor, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
		                .addGroup(gl_contentPanel.createSequentialGroup()
		                    .addComponent(btnFindWorkingDir)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(btnSelectColor))))
		        .addContainerGap()));

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
						dispose();
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
						ApplicationEditor.this.processDescriptor = null;
						dispose();
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

		}

		setVisible(true);
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
