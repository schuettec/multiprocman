package com.github.schuettec.multiprocman.git;

import static com.github.schuettec.multiprocman.git.GitManager.LOCAL_PREFIX;
import static com.github.schuettec.multiprocman.git.GitManager.REMOTE_PREFIX;
import static com.github.schuettec.multiprocman.git.GitManager.isLocalBranch;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

public class GitBranchSelection extends JDialog {

    private static final Font MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final int WARN_COL = 0;
    private static final int TITLE_COL = 1;
    private static final int BRANCH_COL = 2;
    private static final int PULL_COL = 3;

    protected static final String[] columnNames = { "Ready", "Application", "Branch selection", "Pull after checkout" };

    private final JPanel contentPanel = new JPanel();
    private JScrollPane scrollPane;
    private JTable table;

    private List<BranchSelection> descriptors;
    private JButton okButton;
    private JButton cancelButton;
    private boolean wasCancelled;

    /**
     * Create the dialog.
     */
    public GitBranchSelection() {
        this.descriptors = new LinkedList<>();
        ThemeUtil.setLookAndFeel();
        setIconImage(Resources.getApplicationIcon());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(640, 480));
        this.setSize(new Dimension(640, 480));
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

        ThemeUtil.loadWindow(this);
        ThemeUtil.installListeners(this);

        setModal(true);
        setTitle("Git branch selection");
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JLabel lblSomeSelectedLaunch = new JLabel(
                "<html>Some selected launch configurations have enable Git support. Please choose the Git branch to work with and confirm the launch operation.</html>");

        scrollPane = new JScrollPane();
        {
            okButton = new JButton("OK");
            okButton.setEnabled(false);
            okButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    performOkay();
                }

            });
            okButton.setPreferredSize(new Dimension(91, 23));
            okButton.setActionCommand("OK");
            getRootPane().setDefaultButton(okButton);
        }
        {
            cancelButton = new JButton("Cancel");
            cancelButton.setPreferredSize(new Dimension(91, 23));
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    performCancel();
                }
            });
        }

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkPreconditions();
            }
        });
        btnRefresh.setPreferredSize(new Dimension(91, 23));

        JButton btnFetchAll = new JButton("Fetch all");
        btnFetchAll.setMinimumSize(new Dimension(91, 23));
        btnFetchAll.setMaximumSize(new Dimension(91, 23));
        btnFetchAll.setPreferredSize(new Dimension(91, 23));
        btnFetchAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    GitException exception = null;
                    for (BranchSelection desc : descriptors) {
                        desc.clearCache();
                        try {
                            desc.fetch();
                        } catch (GitException e) {
                            if (isNull(exception)) {
                                exception = e;
                            } else {
                                exception.addSuppressed(e);
                            }
                        }
                    }
                    table.repaint();
                    if (nonNull(exception)) {
                        ExceptionDialog.showException(GitBranchSelection.this, exception,
                                "Error while performing GIT operations.");
                    }

                } finally {
                    btnFetchAll.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPanel.createSequentialGroup().addContainerGap()
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_contentPanel.createSequentialGroup()
                                        .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnFetchAll)
                                        .addPreferredGap(ComponentPlacement.RELATED, 220, Short.MAX_VALUE)
                                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblSomeSelectedLaunch, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE))
                        .addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPanel.createSequentialGroup().addComponent(lblSomeSelectedLaunch).addGap(8)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnFetchAll))
                        .addGap(3)));

        table = new JTable(new ComboBoxTableModel());

        scrollPane.setViewportView(table);
        contentPanel.setLayout(gl_contentPanel);

        JComboBox jComboBox = new JComboBox();
        jComboBox.setRenderer(new ComplexCellRenderer());
        ComboBoxCellEditor editor = new ComboBoxCellEditor(jComboBox);

        // Assign the editor to the second column
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(BRANCH_COL).setCellRenderer(new BranchCellRenderer());
        tcm.getColumn(BRANCH_COL).setCellEditor(editor);

        // Set row heighht
        table.setRowHeight(20);

        table.getColumnModel().getColumn(WARN_COL).setPreferredWidth(20);

        table.getColumnModel().getColumn(BRANCH_COL).setPreferredWidth(300);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        String KEY = "okay";
        this.getRootPane().getActionMap().put(KEY, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                performOkay();
            }
        });
        InputMap im = this.getRootPane().getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KEY);

        KEY = "cancel";
        this.getRootPane().getActionMap().put(KEY, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                performOkay();
            }
        });
        im = this.getRootPane().getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY);

    }

    private void checkPreconditions() {
        GitException exception = null;

        boolean enable = true;
        for (BranchSelection desc : descriptors) {
            desc.clearCache();
            try {
                if (!desc.isSaveToCheckout()) {
                    enable = false;
                }
            } catch (GitException e) {
                if (isNull(exception)) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        table.repaint();
        if (nonNull(exception)) {
            ExceptionDialog.showException(this, exception, "Error while performing GIT operations.");
            okButton.setEnabled(false);
        } else {
            okButton.setEnabled(enable);
        }
    }

    public boolean showBranchSelection(Component parent) {
        this.setLocationRelativeTo(parent);
        this.validate();
        checkPreconditions();
        this.setVisible(true);
        return this.wasCancelled;
    }

    public boolean isEmpty() {
        return descriptors.isEmpty();
    }

    public void addProcessDescriptor(ProcessDescriptor pd) {
        try {
            BranchSelection selection = new BranchSelection(pd);
            selection.setSelectedBranch(pd.getCurrentBranch());
            descriptors.add(selection);
        } catch (GitException e) {
            ExceptionDialog.showException(this, e, e.getMessage());
        }
    }

    public boolean hasTasksToShow() {
        return !descriptors.isEmpty();
    }

    private void performCancel() {
        this.wasCancelled = true;
        this.dispose();
    }

    class BranchCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component toReturn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setupBranchLabel((String) value, (JLabel) toReturn);
            return toReturn;
        }
    }

    public static void setupBranchLabel(String branchName, JLabel renderer) {
        renderer.setFont(MONOSPACED);

        if (nonNull(branchName)) {
            if (isLocalBranch(branchName)) {
                renderer.setIcon(new ImageIcon(Resources.getLocal()));
                renderer.setText(branchName.replace(LOCAL_PREFIX, ""));
            } else if (GitManager.isRemoteBranch(branchName)) {
                renderer.setIcon(new ImageIcon(Resources.getRemote()));
                renderer.setText(branchName.replace(REMOTE_PREFIX, ""));
            } else {
                renderer.setIcon(null);
                renderer.setText(branchName);
            }
        }
    }

    class ComboBoxCellEditor extends DefaultCellEditor {
        // Declare a model that is used for adding the elements to the `ComboBox`
        private DefaultComboBoxModel model;

        public ComboBoxCellEditor(JComboBox combobox) {
            super(combobox);
            this.model = (DefaultComboBoxModel) ((JComboBox) getComponent()).getModel();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            BranchSelection selection = descriptors.get(row);
            List<String> obtainedList;
            try {
                obtainedList = selection.getAllBranches();
                model.removeAllElements();
                for (int i = 0; i < obtainedList.size(); i++) {
                    model.addElement(obtainedList.get(i));
                }
                model.setSelectedItem(selection.getCurrentBranch());
            } catch (GitException e) {
                ExceptionDialog.showException(GitBranchSelection.this, e, e.getMessage());
            }

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
            BranchSelection processDescriptor = descriptors.get(row);
            if (column == WARN_COL) {
                try {
                    if (processDescriptor.isSaveToCheckout()) {
                        return new ImageIcon(Resources.getOk());
                    } else {
                        return new ImageIcon(Resources.getWarning());
                    }
                } catch (GitException e) {
                    return e.getMessage();
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
                descriptors.get(row).setSelectedBranch((String) value);
                checkPreconditions();
                fireTableRowsUpdated(row, row);
            }
            if (column == PULL_COL) {
                descriptors.get(row).setPullAfterCheckout((boolean) value);
                checkPreconditions();
                fireTableRowsUpdated(row, row);
            }
        }
    }

    class ComplexCellRenderer implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

            setupBranchLabel((String) value, renderer);

            return renderer;
        }

    }

    private void performOkay() {
        ProgressView pv = new ProgressView(descriptors);
        wasCancelled = pv.showProgressAndPerform(GitBranchSelection.this);
        GitBranchSelection.this.setVisible(false);
    }
}
