package com.github.schuettec.multiprocman;

import static com.github.schuettec.multiprocman.git.GitManager.LOCAL_PREFIX;
import static com.github.schuettec.multiprocman.git.GitManager.REMOTE_PREFIX;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.schuettec.multiprocman.ProcessController.State;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane.ExportType;
import com.github.schuettec.multiprocman.console.AutoScrollToBottomListener;
import com.github.schuettec.multiprocman.console.ScrollableAnsiColorTextPaneContainer;
import com.github.schuettec.multiprocman.console.SearchFieldListener;
import com.github.schuettec.multiprocman.consolepreview.ConsolePreview;
import com.github.schuettec.multiprocman.git.GitBranchSelection;
import com.github.schuettec.multiprocman.git.GitException;
import com.github.schuettec.multiprocman.git.GitManagerImpl;
import com.github.schuettec.multiprocman.manager.ProcessManager;
import com.github.schuettec.multiprocman.themes.ThemeUtil;

import javafx.stage.FileChooser.ExtensionFilter;

public class MainFrame extends JFrame implements WindowListener, ProcessListener, SearchFieldListener {

	private static final String WINDOW_TITLE = "Running applications";

	private static final String TEXT_FILES = "Text Files";

	private static final String RICH_TEXT_FILES = "Rich Text Files";

	private static final String HTML_FILES = "HTML Files";

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	AbstractAction scrollToBottomAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JToggleButton source = (JToggleButton) e.getSource();
			ScrollableAnsiColorTextPaneContainer consoleScroller = currentProcess.getConsoleScroller();
			boolean autoScroll = source.isSelected();
			consoleScroller.setAutoScrollToBottom(autoScroll);
			consoleScroller.scrollToBottom();
		}
	};

	private static class ListeningToggleButtonModel extends JToggleButton.ToggleButtonModel
	    implements AutoScrollToBottomListener {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void autoScrollToBottomProperty(Boolean value) {
			this.setSelected(value);
		}
	};

	private JPanel contentPane;
	private JPanel footerContainer;
	private JSeparator separator;
	private JPanel footer;
	private JToggleButton tglScrollToBottom;

	private JList<ProcessController> processList;
	private DefaultListModel<ProcessController> processes;

	private ProcessController currentProcess;

	private ListeningToggleButtonModel autoScrollToBottomToggleModel;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmSave;
	private JMenuItem mntmExit;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JMenuItem mntmNewProcess;
	private JMenu mnView;
	private JCheckBoxMenuItem chckbxmntmAutoScrollTo;
	private JCheckBoxMenuItem chckbxmntmFind;
	private JToolBar toolBar;
	private JButton btnStop;
	private JButton btnStopForcibly;
	private JButton btnRestart;
	private JButton btnClear;
	private JPanel panel;
	private JButton btnSave;
	private JButton btnClose;

	private List<Component> defaultToolbarButtons;
	private JToolBar allProcessesToolbar;

	private JMenuItem mntmNewFromSearchProcess;
	private JPanel pnlGitInfo;
	private JLabel lblCurrentBranch;
	private JPanel pnlInfo;
	private JLabel lblOutputSoFar;
	private JLabel lblAppOutput;

	private static class Holder {
		private static final MainFrame INSTANCE = new MainFrame();
	}

	public static MainFrame getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Create the frame.
	 */
	private MainFrame() {
		setTitle(WINDOW_TITLE);
		setIconImage(Resources.getApplicationIcon());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setPreferredSize(new Dimension(480, 640));
		ThemeUtil.loadWindow(this);
		ThemeUtil.installListeners(this);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		footerContainer = new JPanel();
		footerContainer.setBorder(null);
		contentPane.add(footerContainer, BorderLayout.SOUTH);
		footerContainer.setLayout(new BorderLayout(0, 0));

		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(0, 3));
		separator.setOrientation(SwingConstants.VERTICAL);
		footerContainer.add(separator, BorderLayout.NORTH);

		footer = new JPanel();
		footerContainer.add(footer, BorderLayout.CENTER);

		tglScrollToBottom = new JToggleButton(scrollToBottomAction);
		tglScrollToBottom.setPreferredSize(new Dimension(38, 20));
		tglScrollToBottom.setIcon(new ImageIcon(Resources.getScrollLock()));
		tglScrollToBottom.setSelectedIcon(new ImageIcon(Resources.getScrollFree()));
		this.autoScrollToBottomToggleModel = new ListeningToggleButtonModel();
		tglScrollToBottom.setModel(autoScrollToBottomToggleModel);
		footer.setLayout(new BorderLayout(5, 5));
		tglScrollToBottom.setIcon(new ImageIcon(Resources.getScrollLock()));

		footer.add(tglScrollToBottom, BorderLayout.EAST);

		pnlGitInfo = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnlGitInfo.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		pnlGitInfo.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		footer.add(pnlGitInfo, BorderLayout.WEST);

		JLabel lblCurrentBranchTitle = new JLabel(new ImageIcon(Resources.getBranch()));
		pnlGitInfo.add(lblCurrentBranchTitle);

		lblCurrentBranch = new JLabel("");
		pnlGitInfo.add(lblCurrentBranch);

		pnlInfo = new JPanel();
		pnlInfo.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		FlowLayout flowLayout_1 = (FlowLayout) pnlInfo.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		flowLayout_1.setVgap(0);
		flowLayout_1.setHgap(0);
		footer.add(pnlInfo, BorderLayout.CENTER);

		lblOutputSoFar = new JLabel("Output so far: ");
		pnlInfo.add(lblOutputSoFar);

		lblAppOutput = new JLabel();
		lblAppOutput.setText("0 bytes");
		pnlInfo.add(lblAppOutput);

		this.processes = new DefaultListModel<ProcessController>();

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		this.processList = new JList<ProcessController>(processes);
		this.processList.setVisibleRowCount(1);
		this.processList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.processList.setCellRenderer(new ConsolePreviewCellRenderer());

		this.processList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				ProcessController selectedValue = processList.getSelectedValue();
				if (nonNull(selectedValue)) {
					MainFrame.this.setTitle(WINDOW_TITLE + " - " + selectedValue.getProcessDescriptor()
					    .getTitle());
					selectConsole(selectedValue);
				}
			}

		});
		panel.setLayout(new BorderLayout(0, 0));
		this.processList.setDragEnabled(true);
		this.processList.setDropMode(DropMode.INSERT);
		this.processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.processList.setTransferHandler(new TransferHandler() {
			private int index;
			private boolean beforeIndex = false; // Start with `false` therefore if it is removed from or added to the
			// list it still works

			@Override
			public int getSourceActions(JComponent comp) {
				return MOVE;
			}

			@Override
			public Transferable createTransferable(JComponent comp) {
				index = processList.getSelectedIndex();
				return new StringSelection(String.valueOf(index));
			}

			@Override
			public void exportDone(JComponent comp, Transferable trans, int action) {
				if (action == MOVE) {
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
					String s = (String) support.getTransferable()
					    .getTransferData(DataFlavor.stringFlavor);
					JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
					ProcessController processController = processes.get(Integer.parseInt(s));
					processes.add(dl.getIndex(), processController);
					processList.setSelectedIndex(dl.getIndex());
					beforeIndex = dl.getIndex() < index ? true : false;
					return true;
				} catch (Exception e) {
				}
				return false;
			}
		});

		JScrollPane scrollPane = new JScrollPane(processList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(ConsolePreview.WIDTH, ConsolePreview.HEIGHT));
		panel.add(scrollPane);

		allProcessesToolbar = new JToolBar(JToolBar.VERTICAL);
		// toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		allProcessesToolbar.setRollover(true);
		allProcessesToolbar.setFloatable(false);
		panel.add(allProcessesToolbar, BorderLayout.EAST);
		buildAllProcessesToolbar();

		toolBar = new JToolBar();
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		panel.add(toolBar, BorderLayout.SOUTH);

		buildProcessToolbar();

		// Build menu

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNewProcess = new JMenuItem(new AbstractAction("New...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProcessManager.getInstance()
				    .setVisible(true);
			}
		});
		mntmNewProcess.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		mnFile.add(mntmNewProcess);

		mntmNewFromSearchProcess = new JMenuItem(new AbstractAction("New from search...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				new FindLauncherDialog(MainFrame.this);
			}
		});
		mntmNewFromSearchProcess
		    .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mnFile.add(mntmNewFromSearchProcess);

		separator_2 = new JSeparator();
		mnFile.add(separator_2);

		mntmSave = new JMenuItem(new AbstractAction("Save As...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}

		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mnFile.add(mntmSave);

		separator_1 = new JSeparator();
		mnFile.add(separator_1);

		mntmExit = new JMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						exit();
					}
				});
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		mnFile.add(mntmExit);

		mnView = new JMenu("View");
		menuBar.add(mnView);

		chckbxmntmFind = new JCheckBoxMenuItem(new AbstractAction("Find") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chckbxmntmFind.isSelected()) {
					currentProcess.getConsoleScroller()
					    .startSearch();
				} else {
					currentProcess.getConsoleScroller()
					    .finishSearch();
				}
			}
		});
		chckbxmntmFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		mnView.add(chckbxmntmFind);

		chckbxmntmAutoScrollTo = new JCheckBoxMenuItem("Auto scroll to bottom");
		chckbxmntmAutoScrollTo.setModel(autoScrollToBottomToggleModel);
		mnView.add(chckbxmntmAutoScrollTo);

		this.defaultToolbarButtons = Arrays.asList(new Component[] {
		    btnClose, btnSave, btnClear, btnStop, btnStopForcibly, btnRestart
		});
	}

	private void buildAllProcessesToolbar() {
		btnClear = new JButton(new AbstractAction(null, new ImageIcon(Resources.getClear())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Enumeration<ProcessController> elements = processes.elements();
				while (elements.hasMoreElements()) {
					ProcessController pc = elements.nextElement();
					pc.clearConsole();
				}
			}

		});
		btnClear.setToolTipText("Clear console");
		allProcessesToolbar.add(btnClear);
		allProcessesToolbar.addSeparator();

		btnRestart = new JButton(new AbstractAction(null, new ImageIcon(Resources.getRestart())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						Enumeration<ProcessController> elements = processes.elements();
						while (elements.hasMoreElements()) {
							ProcessController pc = elements.nextElement();
							Cursor defaultCursor = Cursor.getDefaultCursor();
							Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
							setCursor(waitCursor);
							pc.stop(true);
							clearConsole();
							pc.start();
							setCursor(defaultCursor);
						}
					}
				});
			}

		});
		btnRestart.setToolTipText("Restart");
		allProcessesToolbar.add(btnRestart);

		btnStop = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStop())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Enumeration<ProcessController> elements = processes.elements();
				while (elements.hasMoreElements()) {
					ProcessController pc = elements.nextElement();
					pc.stop(false);
				}
			}

		});
		btnStop.setToolTipText("Stop");
		allProcessesToolbar.add(btnStop);

		btnStopForcibly = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStopForcibly())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Enumeration<ProcessController> elements = processes.elements();
				while (elements.hasMoreElements()) {
					ProcessController pc = elements.nextElement();
					pc.stopForce(false);
				}
			}

		});
		btnStopForcibly.setToolTipText("Stop forcibly");
		allProcessesToolbar.add(btnStopForcibly);
		allProcessesToolbar.addSeparator();
		addGitToToolbarForAll();
		allProcessesToolbar.addSeparator();
		btnClose = new JButton(new AbstractAction(null, new ImageIcon(Resources.getX())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				List<ProcessController> toClose = new LinkedList<>();
				Enumeration<ProcessController> elements = processes.elements();
				while (elements.hasMoreElements()) {
					ProcessController pc = elements.nextElement();
					if (pc.getState() != State.RUNNING) {
						toClose.add(pc);
					}
				}
				for (ProcessController pc : toClose) {
					removeProcessController(pc);
				}
			}

		});
		btnClose.setToolTipText("Close view.");
		allProcessesToolbar.add(btnClose);
	}

	private void buildProcessToolbar() {
		btnSave = new JButton(new AbstractAction(null, new ImageIcon(Resources.getSave())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		btnSave.setEnabled(false);
		btnSave.setToolTipText("Save output");
		toolBar.add(btnSave);

		btnClear = new JButton(new AbstractAction(null, new ImageIcon(Resources.getClear())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearConsole();
			}

		});
		btnClear.setEnabled(false);
		btnClear.setToolTipText("Clear console");
		toolBar.add(btnClear);
		toolBar.addSeparator();

		btnRestart = new JButton(new AbstractAction(null, new ImageIcon(Resources.getRestart())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						Cursor defaultCursor = Cursor.getDefaultCursor();
						Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
						setCursor(waitCursor);
						currentProcess.stop(true);
						clearConsole();
						currentProcess.start();
						setCursor(defaultCursor);
					}
				});
			}

		});
		btnRestart.setEnabled(false);
		btnRestart.setToolTipText("Restart");
		toolBar.add(btnRestart);

		btnStop = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStop())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentProcess.stop(false);
			}

		});
		btnStop.setEnabled(false);
		btnStop.setToolTipText("Stop");
		toolBar.add(btnStop);

		btnStopForcibly = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStopForcibly())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentProcess.stopForce(false);
			}

		});
		btnStopForcibly.setEnabled(false);
		btnStopForcibly.setToolTipText("Stop forcibly");
		toolBar.add(btnStopForcibly);
		toolBar.addSeparator();

		btnClose = new JButton(new AbstractAction(null, new ImageIcon(Resources.getX())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				ProcessController selected = processList.getSelectedValue();
				if (nonNull(selected)) {
					removeProcessController(selected);
				}
			}

		});
		btnClose.setEnabled(false);
		btnClose.setToolTipText("Close view.");
		toolBar.add(btnClose);
	}

	@Override
	public void dispose() {
		ThemeUtil.deinstallListeners(this);
		super.dispose();
	}

	private void selectConsole(ProcessController selectedValue) {
		if (this.currentProcess != null) {
			// Deregister everything
			currentProcess.getConsoleScroller()
			    .removeAutoScrollToBottomListener(autoScrollToBottomToggleModel);
			currentProcess.getConsoleScroller()
			    .removeSearchFieldListener(this);
		}

		BorderLayout layout = (BorderLayout) contentPane.getLayout();
		Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
		if (centerComponent != null) {
			contentPane.remove(centerComponent);

		}

		clearProcessToolbar();
		disableProcessToolbar();

		if (nonNull(selectedValue)) {
			ScrollableAnsiColorTextPaneContainer consoleScroller = selectedValue.getConsoleScroller();
			contentPane.add(consoleScroller, BorderLayout.CENTER);
			consoleScroller.addAutoScrollToBottomListener(autoScrollToBottomToggleModel);
			consoleScroller.addSearchFieldListener(this);

			addGitToToolbarOnDemand(selectedValue);
			addCounterToToolbar(selectedValue.getCounterExpressions());
		}

		ProcessDescriptor processDescriptor = selectedValue.getProcessDescriptor();
		boolean enableGitSupport = processDescriptor.isEnableGitSupport();
		pnlGitInfo.setVisible(enableGitSupport);
		if (enableGitSupport) {
			try {
				String currentBranch = processDescriptor.getCurrentBranch();
				String presentableBranchName = currentBranch;
				if (currentBranch.startsWith(LOCAL_PREFIX)) {
					presentableBranchName = currentBranch.replace(LOCAL_PREFIX, "") + " (local)";
				} else if (currentBranch.startsWith(REMOTE_PREFIX)) {
					presentableBranchName = currentBranch.replace(LOCAL_PREFIX, "") + " (remote)";
				}
				lblCurrentBranch.setText(presentableBranchName);
			} catch (GitException e) {
				lblCurrentBranch.setText("N/A");
			}
		}

		this.currentProcess = selectedValue;
		processCurrentState();

		this.revalidate();
		this.repaint();
	}

	private void addGitToToolbarForAll() {
		allProcessesToolbar.add(new AbstractAction("Git") {

			@Override
			public void actionPerformed(ActionEvent e) {
				GitBranchSelection branchSelection = new GitBranchSelection();
				Enumeration<ProcessController> elements = processes.elements();
				while (elements.hasMoreElements()) {
					ProcessDescriptor descriptor = elements.nextElement()
					    .getProcessDescriptor();
					if (descriptor.isEnableGitSupport()) {
						branchSelection.addProcessDescriptor(descriptor);
					}
				}
				if (branchSelection.hasTasksToShow()) {
					branchSelection.showBranchSelection(MainFrame.this);
				}
			}
		});
	}

	private void addGitToToolbarOnDemand(ProcessController pc) {
		ProcessDescriptor processDescriptor = pc.getProcessDescriptor();
		if (processDescriptor.isEnableGitSupport()) {
			toolBar.add(new AbstractAction("Git") {

				@Override
				public void actionPerformed(ActionEvent e) {
					GitBranchSelection branchSelection = new GitBranchSelection();
					if (processDescriptor.isEnableGitSupport()) {
						branchSelection.addProcessDescriptor(processDescriptor);
					}
					if (branchSelection.hasTasksToShow()) {
						branchSelection.showBranchSelection(MainFrame.this);
					}
				}
			});
		}
	}

	private void addCounterToToolbar(CounterExpressions counterExpressions) {
		List<JButton> buttons = counterExpressions.getButtons();
		for (JButton b : buttons) {
			toolBar.add(b);
		}
	}

	private void clearProcessToolbar() {
		Component[] components = toolBar.getComponents();
		for (Component c : components) {
			if (!defaultToolbarButtons.contains(c)) {
				toolBar.remove(c);
			}
		}
	}

	public void addProcessController(ProcessController processController) {
		boolean activateFirst = this.processes.isEmpty();
		processController.addProcessListener(this);
		this.processes.addElement(processController);
		if (activateFirst) {
			this.processList.setSelectedIndex(0);
		}
	}

	private void removeProcessController(ProcessController processController) {
		processController.removeProcessListener(this);
		this.processes.removeElement(processController);
		if (this.processes.isEmpty()) {
			ProcessManager.getInstance()
			    .setVisible(true);
			setVisible(false);
			dispose();
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		exit();
	}

	private void exit() {
		if (ProcessController.hasActiveProcesses()) {
			int answer = JOptionPane.showConfirmDialog(this, "There are running processes. Do you want to stop them?",
			    "Active processes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				_exit();
			} else {
				return;
			}
		}
		_exit();
	}

	private void _exit() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ProcessController.shutdown();
				ThemeUtil.stopJavaFX();
				GitManagerImpl.closeAll();
				setVisible(false);
				dispose();
				System.exit(0);
			}
		});
	}

	private void clearConsole() {
		currentProcess.clearConsole();
	}

	private void saveAs() {
		FileUtil.showFileChooser(FileUtil.Type.SAVE, l -> {
			l.add(new ExtensionFilter(TEXT_FILES, "*.txt"));
			l.add(new ExtensionFilter(HTML_FILES, "*.html", "*.htm"));
			l.add(new ExtensionFilter(RICH_TEXT_FILES, "*.rtf"));
		}, new FileChooserCallback() {

			@Override
			public void fileSelected(File file, ExtensionFilter extension) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ExportType exportType = null;
						switch (extension.getDescription()) {
							default:
							case TEXT_FILES:
								exportType = ExportType.TEXT;
								break;
							case HTML_FILES:
								exportType = ExportType.HTML;
								break;
							case RICH_TEXT_FILES:
								exportType = ExportType.RTF;
								break;
						}
						currentProcess.getTextPane()
						    .saveAs(file, exportType);
					}
				});
			}
		});

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void searchFieldOpen() {
		chckbxmntmFind.setSelected(true);
	}

	@Override
	public void searchFieldClosed() {
		chckbxmntmFind.setSelected(false);
	}

	private void disableProcessToolbar() {
		chckbxmntmFind.setEnabled(false);
		btnClose.setEnabled(false);
		btnSave.setEnabled(false);
		btnClear.setEnabled(false);
		btnStop.setEnabled(false);
		btnStopForcibly.setEnabled(false);
		btnRestart.setEnabled(false);
	}

	@Override
	public void processUpdate(ProcessController controller) {
		this.processList.repaint();
		refreshInfoBar(controller);
		processCurrentState();

	}

	private void refreshInfoBar(ProcessController controller) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Statistics statistics = controller.getStatistics();
				lblAppOutput.setText(statistics.overallOutbutAmountPresentable());
			}
		});
	}

	private void processCurrentState() {
		if (nonNull(currentProcess)) {
			switch (currentProcess.getState()) {
				case NOT_STARTED:
					btnClose.setEnabled(true);
					chckbxmntmFind.setEnabled(false);
					btnSave.setEnabled(false);
					btnClear.setEnabled(false);
					btnStop.setEnabled(false);
					btnStopForcibly.setEnabled(false);
					btnRestart.setEnabled(true);
					break;
				case RUNNING:
					btnClose.setEnabled(false);
					chckbxmntmFind.setEnabled(true);
					btnSave.setEnabled(true);
					btnClear.setEnabled(true);
					btnStop.setEnabled(true);
					btnStopForcibly.setEnabled(true);
					btnRestart.setEnabled(true);
					break;
				case STOPPED_OK:
				case STOPPED_ALERT:
					btnClose.setEnabled(true);
					chckbxmntmFind.setEnabled(true);
					btnSave.setEnabled(true);
					btnClear.setEnabled(true);
					btnStop.setEnabled(false);
					btnStopForcibly.setEnabled(false);
					btnRestart.setEnabled(true);
					break;
				case STOPPING:
					btnClose.setEnabled(false);
					chckbxmntmFind.setEnabled(true);
					btnSave.setEnabled(true);
					btnClear.setEnabled(true);
					btnStop.setEnabled(false);
					btnStopForcibly.setEnabled(false);
					btnRestart.setEnabled(false);
					break;
				case ABANDONED:
					btnClose.setEnabled(true);
					break;
			}
		}
	}

}
