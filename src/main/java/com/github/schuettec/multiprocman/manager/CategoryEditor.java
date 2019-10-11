package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;

import java.awt.BorderLayout;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.github.schuettec.multiprocman.FileChooserCallback;
import com.github.schuettec.multiprocman.FileUtil;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;

import javafx.stage.FileChooser.ExtensionFilter;

public class CategoryEditor extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JTextField txtName;
    private JTextField txtDescription;
    private Category category;
    private JTextPane txtUrl;
    private JLabel lblLastUpdate;
    private JLabel lblIcon;

    /**
     * Create the dialog.
     *
     * @param category
     * @param owner
     */
    private CategoryEditor(Category category, Component owner) {
        setIconImage(Resources.getApplicationIcon());
        setModal(true);
        setTitle("Category");
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
        this.setPreferredSize(new Dimension(330, 236));
        this.setSize(new Dimension(402, 396));
        setLocationRelativeTo(owner);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        this.lblIcon = new JLabel(new ImageIcon(Resources.getFolder()));
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
                                    JOptionPane.showMessageDialog(CategoryEditor.this,
                                            "Error while loading the selected image file.", "I/O error",
                                            JOptionPane.ERROR_MESSAGE);
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

        JLabel lblUrl = new JLabel("URL:");
        lblUrl.setHorizontalAlignment(SwingConstants.RIGHT);

        JScrollPane scrollPane = new JScrollPane();

        JLabel lblTheUrlCan = new JLabel(
                "<html>The URL can be used to import the XML representation of a category from a webserver or any other source that is accessible via URL.</html>");

        JLabel lblNewLabel_1 = new JLabel("Last update:");

        lblLastUpdate = new JLabel("date");

        JButton btnUpdate = new JButton(new AbstractAction("Update") {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateFromUrl();
            }
        });
        btnUpdate.setPreferredSize(new Dimension(91, 23));

        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(gl_contentPanel
                        .createParallelGroup(Alignment.LEADING)
                        .addComponent(lblPleaseFillIn, GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                        .addGroup(gl_contentPanel.createSequentialGroup().addGroup(gl_contentPanel
                                .createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
                                        .addComponent(lblForIcon, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblName, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 78,
                                                GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblNewLabel)
                                .addComponent(lblUrl, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblNewLabel_1)).addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                                        .addGroup(gl_contentPanel.createSequentialGroup()
                                                .addComponent(lblLastUpdate, GroupLayout.DEFAULT_SIZE, 187,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnUpdate,
                                                        GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(gl_contentPanel.createSequentialGroup()
                                                .addComponent(lblIcon, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(91).addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 91,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtDescription, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                                        .addComponent(txtName, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                                        .addComponent(lblTheUrlCan, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))))
                        .addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPanel.createSequentialGroup().addGap(8).addComponent(lblPleaseFillIn)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
                                .addComponent(lblForIcon, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                                .addGroup(gl_contentPanel.createSequentialGroup().addGap(11)
                                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                                .addComponent(btnNewButton)
                                                .addComponent(lblIcon, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                gl_contentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblName)
                                        .addGroup(gl_contentPanel.createSequentialGroup()
                                                .addComponent(txtName, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(txtDescription, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblNewLabel))))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblUrl)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTheUrlCan)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblNewLabel_1).addComponent(lblLastUpdate))
                                .addComponent(btnUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE))
                        .addGap(16)));

        txtUrl = new JTextPane();
        scrollPane.setViewportView(txtUrl);
        contentPanel.setLayout(gl_contentPanel);

        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton(new AbstractAction("OK") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        refreshCategory(lblIcon);
                        dispose();
                    }

                });
                okButton.setPreferredSize(new Dimension(91, 23));
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
                getRootPane().getActionMap().put("OK", okButton.getAction());
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
                getRootPane().getActionMap().put("CANCEL", cancelButton.getAction());
            }
        }

        if (isNull(category)) {
            this.category = new Category();
        } else {
            setCategory(category);
        }

        setVisible(true);
    }

    public static Category newCategory(Component owner) {
        CategoryEditor editor = new CategoryEditor(null, owner);
        return editor.getCategory();
    }

    public static void editCategory(Category category, Component owner) {
        new CategoryEditor(category, owner);
    }

    private Category getCategory() {
        return this.category;
    }

    private void performCancel() {
        category = null;
        dispose();
    }

    private void refreshCategory(JLabel lblIcon) {
        this.category.setName(txtName.getText().trim());
        this.category.setDescription(txtDescription.getText().trim());
        this.category.setIcon((ImageIcon) lblIcon.getIcon());
        this.category.setUrl(txtUrl.getText());
    }

    private void updateFromUrl() {
        Category loadedCategory = Categories.loadCategory(txtUrl.getText());
        this.category.setName(loadedCategory.getName().trim());
        this.category.setDescription(loadedCategory.getDescription().trim());
        this.category.setIcon(loadedCategory.getIcon());
        this.category.setLastModified();

        // Do not update url to prevent external systems to switch location.

        this.category.setUrl(txtUrl.getText());

        DefaultListModel<ProcessDescriptor> processTemplates = loadedCategory.getProcessTemplates();
        this.category.getProcessTemplates().clear();
        for (int i = 0; i < processTemplates.size(); i++) {
            ProcessDescriptor pd = processTemplates.get(i);
            this.category.getProcessTemplates().addElement(pd);
        }

        setCategory(this.category);
    }

    private void setCategory(Category category) {
        this.category = category;
        lblIcon.setIcon(category.getIcon());
        txtName.setText(category.getName());
        lblLastUpdate.setText(SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
                .format(category.getLastModified()));
        txtDescription.setText(category.getDescription());
        txtUrl.setText(category.getUrl());
    }
}
