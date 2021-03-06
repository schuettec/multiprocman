package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.schuettec.multiprocman.FileChooserCallback;
import com.github.schuettec.multiprocman.FileUtil;
import com.github.schuettec.multiprocman.FindLauncherDialog;
import com.github.schuettec.multiprocman.MainFrame;
import com.github.schuettec.multiprocman.ProcessController;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.git.GitBranchSelection;
import com.github.schuettec.multiprocman.git.GitManagerImpl;
import com.github.schuettec.multiprocman.preferences.PreferencesDialog;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

import javafx.stage.FileChooser.ExtensionFilter;

public class ProcessManager extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static class Holder {
        private static final ProcessManager INSTANCE = new ProcessManager();
    }

    public static ProcessManager getInstance() {
        return Holder.INSTANCE;
    }

    private final JPanel contentPanel = new JPanel();
    private JList<Category> lstCategories;
    private JList<ProcessDescriptor> lstProcesses;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ThemeUtil.setLookAndFeel();
            if (args.length > 0) {
                String launcherName = args[0];
                Categories categories = new Categories();
                List<ProcessDescriptor> results = categories.findProcessDescriptor(launcherName);
                if (results.size() > 1) {
                    new FindLauncherDialog(null, launcherName);
                } else {
                    ProcessDescriptor processDescriptor = results.get(0);
                    ProcessManager.startAll(null, new Categories(), Arrays.asList(processDescriptor));
                    return;
                }
            } else {
                ProcessManager dialog = ProcessManager.getInstance();
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Action findAction = new AbstractAction("Search launcher") {

        @Override
        public void actionPerformed(ActionEvent e) {
            new FindLauncherDialog(ProcessManager.this);
        }
    };

    private Action newCategory = new AbstractAction(null, new ImageIcon(Resources.getFolderPlus())) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            Category newCategory = CategoryEditor.newCategory(ProcessManager.this);
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
                JOptionPane.showMessageDialog(ProcessManager.this, "Please select the category to edit first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
            } else {
                CategoryEditor.editCategory(categories.get(selectedIndex), ProcessManager.this);
                categories.saveToPreferences();
                lstCategories.repaint();
            }

        }
    };

    private Action importCategories = new AbstractAction(null, new ImageIcon(Resources.getImport())) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            FileUtil.showFileChooser(FileUtil.Type.OPEN, extensions -> {
                extensions.add(new ExtensionFilter("Categories", Arrays.asList("*.categories")));
            }, new FileChooserCallback() {
                @Override
                public void fileSelected(File file, ExtensionFilter selectedFilter) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            List<Category> toAdd = Categories.importCategories(file);
                            for (Category c : toAdd) {
                                categories.addElement(c);
                            }
                        }
                    });
                }
            });
        }
    };

    private Action exportCategories = new AbstractAction(null, new ImageIcon(Resources.getExport())) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = lstCategories.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(ProcessManager.this, "Please select the categories to export first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
            } else {
                FileUtil.showFileChooser(FileUtil.Type.SAVE, extensions -> {
                    extensions.add(new ExtensionFilter("Categories", Arrays.asList("*.categories")));
                }, new FileChooserCallback() {
                    @Override
                    public void fileSelected(File file, ExtensionFilter selectedFilter) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                List<Category> selection = lstCategories.getSelectedValuesList();
                                Categories.exportCategories(selection, file);
                            }
                        });
                    }
                });
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
                int sure = JOptionPane.showConfirmDialog(ProcessManager.this, "Are you sure to remove this category?",
                        "Remove category", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (sure == JOptionPane.YES_OPTION) {
                    categories.remove(selectedIndex);
                }
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
                ProcessDescriptor process = ApplicationEditor.newProcess(ProcessManager.this);
                if (nonNull(process)) {
                    currentCategory.getProcessTemplates().addElement(process);
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
                ApplicationEditor.editProcessDescriptor(value, ProcessManager.this);
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
                int sure = JOptionPane.showConfirmDialog(ProcessManager.this,
                        "Are you sure to remove this application?", "Remove application", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (sure == JOptionPane.YES_OPTION) {
                    DefaultListModel<ProcessDescriptor> processes = (DefaultListModel<ProcessDescriptor>) lstProcesses
                            .getModel();
                    processes.removeElement(value);
                    categories.saveToPreferences();
                    lstProcesses.repaint();
                }

            }
        }
    };

    private Action importApplications = new AbstractAction(null, new ImageIcon(Resources.getImport())) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = lstCategories.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(ProcessManager.this, "Please select the categories to import into.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
            } else {
                FileUtil.showFileChooser(FileUtil.Type.OPEN, extensions -> {
                    extensions.add(new ExtensionFilter("Launch configurations", Arrays.asList("*.launch")));
                }, new FileChooserCallback() {
                    @Override
                    public void fileSelected(File file, ExtensionFilter selectedFilter) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Category category = categories.get(selectedIndex);
                                List<ProcessDescriptor> toAdd = Categories.importApplications(file);
                                for (ProcessDescriptor e : toAdd) {
                                    category.getProcessTemplates().addElement(e);
                                }
                                categories.saveToPreferences();
                            }
                        });
                    }
                });
            }
        }
    };

    private Action exportApplications = new AbstractAction(null, new ImageIcon(Resources.getExport())) {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = lstProcesses.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(ProcessManager.this, "Please select the applications to export first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
            } else {
                FileUtil.showFileChooser(FileUtil.Type.SAVE, extensions -> {
                    extensions.add(new ExtensionFilter("Launch configurations", Arrays.asList("*.launch")));
                }, new FileChooserCallback() {
                    @Override
                    public void fileSelected(File file, ExtensionFilter selectedFilter) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {

                                List<ProcessDescriptor> selection = lstProcesses.getSelectedValuesList();
                                Categories.exportApplications(selection, file);
                            }
                        });
                    }
                });
            }
        }
    };

    private Categories categories;
    private Category currentCategory;

    /**
     * Create the dialog.
     */
    private ProcessManager() {
        setIconImage(Resources.getApplicationIcon());
        setTitle("Application manager");
        setPreferredSize(new Dimension(640, 480));
        ThemeUtil.loadWindow(this);
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
                if (!MainFrame.getInstance().isVisible()) {
                    ThemeUtil.stopJavaFX();
                    GitManagerImpl.closeAll();
                    dispose();
                    System.exit(0);
                }
                setVisible(false);
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

            lstCategories.setDragEnabled(true);
            lstCategories.setDropMode(DropMode.INSERT);
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
                        String s = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
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
                {
                    JButton button = new JButton(importCategories);
                    button.setToolTipText("Import category");
                    toolBar.add(button);
                }
                {
                    JButton button = new JButton(exportCategories);
                    button.setToolTipText("Export category");
                    toolBar.add(button);
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
                        JButton btnRunCategory = new JButton(
                                new AbstractAction(null, new ImageIcon(Resources.getPlay())) {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {

                                        int selectedIndex = lstCategories.getSelectedIndex();
                                        if (selectedIndex == -1) {
                                            JOptionPane.showMessageDialog(ProcessManager.this,
                                                    "Please select the category to start first.", "No selection",
                                                    JOptionPane.WARNING_MESSAGE);
                                        } else {
                                            Category selected = lstCategories.getSelectedValue();
                                            DefaultListModel<ProcessDescriptor> processTemplates = selected
                                                    .getProcessTemplates();
                                            ProcessDescriptor[] array = new ProcessDescriptor[processTemplates.size()];
                                            processTemplates.copyInto(array);
                                            startAllOrHandleCancel(array);
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

                    lstProcesses.setDragEnabled(true);
                    lstProcesses.setDropMode(DropMode.INSERT);

                    lstProcesses.setTransferHandler(new TransferHandler() {
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
                            index = lstProcesses.getSelectedIndex();
                            return new StringSelection(String.valueOf(index));
                        }

                        @Override
                        public void exportDone(JComponent comp, Transferable trans, int action) {
                            if (action == MOVE) {
                                DefaultListModel<ProcessDescriptor> processes = currentCategory.getProcessTemplates();
                                if (beforeIndex)
                                    processes.remove(index + 1);
                                else
                                    processes.remove(index);
                            }
                        }

                        @Override
                        public boolean canImport(TransferHandler.TransferSupport support) {
                            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                        }

                        @Override
                        public boolean importData(TransferHandler.TransferSupport support) {
                            try {
                                String s = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                                int newIndex = Integer.parseInt(s);
                                DefaultListModel<ProcessDescriptor> processes = currentCategory.getProcessTemplates();
                                ProcessDescriptor pd = processes.get(newIndex);
                                processes.add(dl.getIndex(), pd);
                                lstProcesses.setSelectedIndex(dl.getIndex());
                                beforeIndex = dl.getIndex() < index ? true : false;
                                return true;
                            } catch (Exception e) {
                            }
                            return false;
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
                    {
                        JButton button = new JButton(importApplications);
                        button.setToolTipText("Import applications.");
                        toolBar.add(button);
                    }
                    {
                        JButton button = new JButton(exportApplications);
                        button.setToolTipText("Export applications.");
                        toolBar.add(button);
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
                            JButton btnRunApplication = new JButton(
                                    new AbstractAction(null, new ImageIcon(Resources.getPlay())) {

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
        {
            JPopupMenu popupMenu = new JPopupMenu();
            addPopup(lstProcesses, popupMenu);
            {
                JMenuItem newApp = new JMenuItem(new TitledAction("New...", newApplication));
                JMenuItem editApp = new JMenuItem(new TitledAction("Edit...", editApplication));
                JMenuItem removeApp = new JMenuItem(new TitledAction("Remove", removeApplication));
                popupMenu.add(newApp);
                popupMenu.add(editApp);
                popupMenu.add(removeApp);
            }
        }
        {
            JPopupMenu popupMenu = new JPopupMenu();
            addPopup(lstCategories, popupMenu);
            JMenuItem newCat = new JMenuItem(new TitledAction("New...", newCategory));
            JMenuItem editCat = new JMenuItem(new TitledAction("Edit...", editCategory));
            JMenuItem removeCat = new JMenuItem(new TitledAction("Remove", removeCategory));
            popupMenu.add(newCat);
            popupMenu.add(editCat);
            popupMenu.add(removeCat);
        }
        {
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);
            {
                JMenu mnFile = new JMenu("Windows");
                menuBar.add(mnFile);
                {
                    JMenuItem mntmPreferences = new JMenuItem(new AbstractAction("Preferences") {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            PreferencesDialog.showPreferences();
                        }
                    });
                    mnFile.add(mntmPreferences);
                }
            }
        }

        String KEY = "find";
        getRootPane().getActionMap().put(KEY, findAction);
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), KEY);

        im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), KEY);

        setVisible(true);

    }

    @Override
    public void dispose() {
        ThemeUtil.deinstallListeners(this);
        super.dispose();
    }

    private void startAllOrHandleCancel(ProcessDescriptor[] array) {
        boolean cancelled = startAll(this, this.categories, Arrays.asList(array));
        if (cancelled) {
            JOptionPane.showMessageDialog(ProcessManager.this, "The launch was cancelled.", "Launch cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            setVisible(false);
        }
    }

    private void runSelectedApplication() {
        int selectedIndex = lstProcesses.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(ProcessManager.this, "Please select the application to start first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
        } else {
            List<ProcessDescriptor> selected = lstProcesses.getSelectedValuesList();
            startAllOrHandleCancel(selected.toArray(new ProcessDescriptor[selected.size()]));
        }
    }

    public static boolean startAll(Component parent, Categories categories, Collection<ProcessDescriptor> descriptors) {
        GitBranchSelection branchSelection = new GitBranchSelection();
        Iterator<ProcessDescriptor> iterator = descriptors.iterator();
        while (iterator.hasNext()) {
            ProcessDescriptor descriptor = iterator.next();
            if (descriptor.hasPromptVariables()) {
                boolean promptVariablesCancelled = descriptor.promptVariables(parent);
                if (promptVariablesCancelled) {
                    return true;
                } else {
                    // Save categories because prompt variables last value may has changed.
                    categories.saveToPreferences();
                }
            }

            if (descriptor.isEnableGitSupport()) {
                branchSelection.addProcessDescriptor(descriptor);
            }
        }
        if (branchSelection.hasTasksToShow()) {
            boolean cancelled = branchSelection.showBranchSelection(parent);
            if (cancelled) {
                return cancelled;
            }
        }

        MainFrame mainFrame = MainFrame.getInstance();
        boolean started = _startAll(parent, mainFrame, descriptors);
        if (started) {
            mainFrame.setVisible(true);
            return false;
        }
        return true;
    }

    /**
     * Starts all {@link ProcessDescriptor}s and returns <code>true</code> if there was added at least one started
     * process. <code>false</code> is returned if there was no process started and the main frame should be closed.
     *
     */
    public static boolean _startAll(Component parent, MainFrame mainFrame, Collection<ProcessDescriptor> descriptors) {
        boolean added = false;
        Iterator<ProcessDescriptor> iterator = descriptors.iterator();
        while (iterator.hasNext()) {
            ProcessDescriptor descriptor = iterator.next();
            ProcessController c = new ProcessController(descriptor);
            boolean started = c.start();
            if (started) {
                mainFrame.addProcessController(c);
                added = true;
            }
        }
        return added;
    }

    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                Object source = e.getSource();
                if (source instanceof JList) {
                    JList list = (JList) source;
                    list.setSelectedIndex(list.locationToIndex(e.getPoint())); // select the item
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

}
