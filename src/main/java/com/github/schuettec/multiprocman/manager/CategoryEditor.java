package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.github.schuettec.multiprocman.FileChooserCallback;
import com.github.schuettec.multiprocman.FileUtil;
import com.github.schuettec.multiprocman.Resources;
import javafx.stage.FileChooser.ExtensionFilter;

public class CategoryEditor extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtDescription;
	private Category category;

	/**
	 * Create the dialog.
	 * 
	 * @param category
	 */
	private CategoryEditor(Category category) {
		setIconImage(Resources.getApplicationIcon());
		setModal(true);
		setTitle("Category");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(430, 200));
		this.setSize(new Dimension(325, 221));
		this.setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblIcon = new JLabel(new ImageIcon(Resources.getFolder()));
		lblIcon.setSize(new Dimension(24, 24));
		lblIcon.setPreferredSize(new Dimension(24, 24));
		JLabel lblForIcon = new JLabel("Category icon: ");
		lblForIcon.setHorizontalAlignment(SwingConstants.RIGHT);
		lblForIcon.setHorizontalTextPosition(SwingConstants.RIGHT);

		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);

		txtName = new JTextField();
		txtName.setColumns(10);

		JLabel lblNewLabel = new JLabel("Description:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		txtDescription = new JTextField();
		txtDescription.setColumns(10);

		JButton btnNewButton = new JButton(new AbstractAction("Find...") {

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
							JOptionPane.showMessageDialog(CategoryEditor.this, "Error while loading the selected image file.",
							    "I/O error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
					}
				});
			}
		});

		JLabel lblPleaseFillIn = new JLabel(
		    "<html>Please fill in the desired data for the new category. When selecting a category icon use 24x24px images, otherwise the images will be scaled automatically.</html>");

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addContainerGap()
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblPleaseFillIn)
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
		                    .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
		                        .addComponent(lblForIcon, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
		                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                        .addComponent(lblName, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 78,
		                            GroupLayout.PREFERRED_SIZE))
		                    .addComponent(lblNewLabel))
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		                    .addGroup(gl_contentPanel.createSequentialGroup()
		                        .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                            GroupLayout.PREFERRED_SIZE)
		                        .addGap(93)
		                        .addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE))
		                    .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
		                        .addComponent(txtDescription, Alignment.LEADING)
		                        .addComponent(txtName, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 206,
		                            GroupLayout.PREFERRED_SIZE)))))
		        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		    .addGroup(gl_contentPanel.createSequentialGroup()
		        .addGap(8)
		        .addComponent(lblPleaseFillIn)
		        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addGap(11)
		                .addComponent(lblIcon, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		            .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		                .addComponent(lblForIcon, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
		                .addComponent(btnNewButton)))
		        .addPreferredGap(ComponentPlacement.RELATED)
		        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
		            .addComponent(lblName)
		            .addGroup(gl_contentPanel.createSequentialGroup()
		                .addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                    GroupLayout.PREFERRED_SIZE)
		                .addPreferredGap(ComponentPlacement.RELATED)
		                .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
		                    .addComponent(txtDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
		                        GroupLayout.PREFERRED_SIZE)
		                    .addComponent(lblNewLabel))))
		        .addContainerGap()));
		contentPanel.setLayout(gl_contentPanel);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(new AbstractAction("OK") {

					@Override
					public void actionPerformed(ActionEvent e) {
						CategoryEditor.this.category.setName(txtName.getText()
						    .trim());
						CategoryEditor.this.category.setDescription(txtDescription.getText()
						    .trim());
						CategoryEditor.this.category.setIcon((ImageIcon) lblIcon.getIcon());
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
						CategoryEditor.this.category = null;
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

		if (isNull(category)) {
			this.category = new Category();
		} else {
			this.category = category;
			lblIcon.setIcon(category.getIcon());
			txtName.setText(category.getName());
			txtDescription.setText(category.getDescription());
		}

		setVisible(true);
	}

	public static Category newCategory() {
		CategoryEditor editor = new CategoryEditor(null);
		return editor.getCategory();
	}

	public static void editCategory(Category category) {
		new CategoryEditor(category);
	}

	private Category getCategory() {
		return this.category;
	}

}
