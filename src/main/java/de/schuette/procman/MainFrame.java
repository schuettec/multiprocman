package de.schuette.procman;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.schuette.procman.console.AnsiColorTextPane.ExportType;
import de.schuette.procman.console.AutoScrollToBottomListener;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.console.SearchFieldListener;
import de.schuette.procman.themes.ThemeUtil;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainFrame extends JFrame implements WindowListener, ProcessListener, SearchFieldListener {

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
	private JButton btnNewButton;
	private JPanel panel;
	private JButton btnNewButton_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ThemeUtil.setLookAndFeel();
					final MainFrame frame = new MainFrame();
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					ProcessDescriptor descriptor = new ProcessDescriptor();
					descriptor.setCommand("ping", "google.de", "-n", "10000");
					descriptor.setCharset(Charset.forName("ibm850"));
					descriptor.setTitle("Ping google.de");
					frame.addProcessController(new ProcessController(descriptor).start());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		new JFXPanel();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setPreferredSize(new Dimension(480, 640));
		setSize(new Dimension(480, 640));
		setLocationRelativeTo(null);

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
		footer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		tglScrollToBottom = new JToggleButton(scrollToBottomAction);
		tglScrollToBottom.setIcon(new ImageIcon(Resources.getScrollLock()));
		tglScrollToBottom.setSelectedIcon(new ImageIcon(Resources.getScrollFree()));
		this.autoScrollToBottomToggleModel = new ListeningToggleButtonModel();
		tglScrollToBottom.setModel(autoScrollToBottomToggleModel);
		tglScrollToBottom.setIcon(new ImageIcon(Resources.getScrollLock()));

		footer.add(tglScrollToBottom);

		this.processes = new DefaultListModel<ProcessController>();

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		this.processList = new JList<>(processes);
		processList.setVisibleRowCount(1);
		processList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.processList.setCellRenderer(new ConsolePreviewCellRenderer());
		this.processList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				ProcessController selectedValue = processList.getSelectedValue();
				if (selectedValue != null) {
					selectConsole(selectedValue);
				}
			}

		});
		panel.setLayout(new BorderLayout(0, 0));
		processList.setDragEnabled(true);
		processList.setDropMode(DropMode.INSERT);
		processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		processList.setTransferHandler(new TransferHandler() {
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
					String s = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
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
		panel.add(scrollPane);

		toolBar = new JToolBar();
		toolBar.setRollover(true);
		panel.add(toolBar, BorderLayout.SOUTH);
		toolBar.setFloatable(false);

		btnNewButton_1 = new JButton(new AbstractAction(null, new ImageIcon(Resources.getSave())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		btnNewButton_1.setToolTipText("Save output");
		toolBar.add(btnNewButton_1);

		btnNewButton = new JButton(new AbstractAction(null, new ImageIcon(Resources.getClear())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearConsole();
			}

		});
		btnNewButton.setToolTipText("Clear console");
		toolBar.add(btnNewButton);
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
						currentProcess.start();
						setCursor(defaultCursor);
					}
				});
			}

		});
		btnRestart.setToolTipText("Restart");
		toolBar.add(btnRestart);

		btnStop = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStop())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentProcess.stop(false);
			}

		});
		btnStop.setToolTipText("Stop");
		toolBar.add(btnStop);

		btnStopForcibly = new JButton(new AbstractAction(null, new ImageIcon(Resources.getStopForcibly())) {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentProcess.stopForce(false);
			}

		});
		btnStopForcibly.setToolTipText("Stop forcibly");
		toolBar.add(btnStopForcibly);

		// Build menu

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNewProcess = new JMenuItem("New...");
		mnFile.add(mntmNewProcess);

		separator_2 = new JSeparator();
		mnFile.add(separator_2);

		mntmSave = new JMenuItem(new AbstractAction("Save As...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}

		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
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
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mnFile.add(mntmExit);

		mnView = new JMenu("View");
		menuBar.add(mnView);

		chckbxmntmFind = new JCheckBoxMenuItem(new AbstractAction("Find") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chckbxmntmFind.isSelected()) {
					currentProcess.getConsoleScroller().startSearch();
				} else {
					currentProcess.getConsoleScroller().finishSearch();
				}
			}
		});
		chckbxmntmFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		mnView.add(chckbxmntmFind);

		chckbxmntmAutoScrollTo = new JCheckBoxMenuItem("Auto scroll to bottom");
		chckbxmntmAutoScrollTo.setModel(autoScrollToBottomToggleModel);
		mnView.add(chckbxmntmAutoScrollTo);
	}

	private void selectConsole(ProcessController selectedValue) {
		if (this.currentProcess != null) {
			// Deregister everything
			currentProcess.getConsoleScroller().removeAutoScrollToBottomListener(autoScrollToBottomToggleModel);
			currentProcess.getConsoleScroller().removeSearchFieldListener(this);
		}

		BorderLayout layout = (BorderLayout) contentPane.getLayout();
		Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
		if (centerComponent != null) {
			contentPane.remove(centerComponent);
		}

		ScrollableAnsiColorTextPaneContainer consoleScroller = selectedValue.getConsoleScroller();
		contentPane.add(consoleScroller, BorderLayout.CENTER);
		consoleScroller.addAutoScrollToBottomListener(autoScrollToBottomToggleModel);
		consoleScroller.addSearchFieldListener(this);

		this.currentProcess = selectedValue;

		this.revalidate();
		this.repaint();
	}

	public void addProcessController(ProcessController processController) {
		boolean activateFirst = this.processes.isEmpty();
		processController.addProcessListener(this);
		this.processes.addElement(processController);
		if (activateFirst) {
			this.processList.setSelectedIndex(0);
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
		ProcessController.shutdown();
		this.setVisible(false);
		this.dispose();

		try {
			javafx.application.Platform.exit();
		} catch (Exception e) {
		}
	}

	private void clearConsole() {
		currentProcess.getTextPane().setText("");
	}

	private void saveAs() {
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Resource File");
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter(TEXT_FILES, "*.txt"),
						new ExtensionFilter(HTML_FILES, "*.html", "*.htm"),
						new ExtensionFilter(RICH_TEXT_FILES, "*.rtf"));
				File selectedFile = fileChooser.showSaveDialog(null);
				ExtensionFilter extension = fileChooser.getSelectedExtensionFilter();

				if (selectedFile != null) {
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

					currentProcess.getTextPane().saveAs(selectedFile, exportType);
				}
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

	@Override
	public void processUpdate(ProcessController controller) {
		this.processList.repaint();
		if (currentProcess == controller) {
			switch (controller.getState()) {
			case NOT_STARTED:
				btnStop.setEnabled(false);
				btnStopForcibly.setEnabled(false);
				btnRestart.setEnabled(true);
				break;
			case RUNNING:
				btnStop.setEnabled(true);
				btnStopForcibly.setEnabled(true);
				btnRestart.setEnabled(true);
				break;
			case STOPPED_OK:
			case STOPPED_ALERT:
				btnStop.setEnabled(false);
				btnStopForcibly.setEnabled(false);
				btnRestart.setEnabled(true);
				break;
			case STOPPING:
				btnStop.setEnabled(false);
				btnStopForcibly.setEnabled(false);
				btnRestart.setEnabled(false);
				break;
			case ABANDONED:
				break;
			}
		}

	}

}
