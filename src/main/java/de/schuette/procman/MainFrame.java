package de.schuette.procman;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.schuette.procman.console.AutoScrollToBottomListener;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.themes.ThemeUtil;

public class MainFrame extends JFrame implements WindowListener, ProcessListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	AbstractAction scrollToBottomAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JToggleButton source = (JToggleButton) e.getSource();
			ScrollableAnsiColorTextPaneContainer consoleScroller = currentProcess.getConsoleScroller();
			consoleScroller.setAutoScrollToBottom(source.isSelected());
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

					// Thread t = new Thread(new Runnable() {
					//
					// @Override
					// public void run() {
					// AtomicInteger index = new AtomicInteger();
					// while (true) {
					// EventQueue.invokeLater(new Runnable() {
					// @Override
					// public void run() {
					// Random r = new Random();
					// int i = index.incrementAndGet();
					// Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
					// frame.append(c, i + " " + "Hallo\n");
					// c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
					// frame.append(c,
					// i + " " + "Duuuuuuuuuuuuuuuuuuu\nESEEEEEEEEEEEEEEEEEEEEEEEEEEL");
					// c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
					// frame.append(c, i + " " + "MÖÖÖÖÖÖÖÖÖÖÖÖÖP\n");
					// c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
					// frame.append(c, i + " " + "TEST\n");
					// }
					// });
					// try {
					// Thread.sleep(1000);
					// } catch (InterruptedException e) {
					// }
					// if (index.get() == 35) {
					// return;
					// }
					// }
					// }
					//
					// });
					// t.start();
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
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setLocationByPlatform(true);
		setPreferredSize(new Dimension(480, 640));
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
		this.autoScrollToBottomToggleModel = new ListeningToggleButtonModel();
		tglScrollToBottom.setModel(autoScrollToBottomToggleModel);
		footer.add(tglScrollToBottom);

		this.processes = new DefaultListModel<ProcessController>();
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

		contentPane.add(new JScrollPane(processList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.NORTH);

	}

	private void selectConsole(ProcessController selectedValue) {
		if (this.currentProcess != null) {
			// Deregister everything
			currentProcess.getConsoleScroller().removeAutoScrollToBottomListener(autoScrollToBottomToggleModel);
		}

		BorderLayout layout = (BorderLayout) contentPane.getLayout();
		Component centerComponent = layout.getLayoutComponent(BorderLayout.CENTER);
		if (centerComponent != null) {
			contentPane.remove(centerComponent);
		}

		ScrollableAnsiColorTextPaneContainer consoleScroller = selectedValue.getConsoleScroller();
		contentPane.add(consoleScroller, BorderLayout.CENTER);
		consoleScroller.addAutoScrollToBottomListener(autoScrollToBottomToggleModel);

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
	public void processStarted() {
	}

	@Override
	public void processStopped(int exitValue) {
	}

	@Override
	public void processAbandoned() {
	}

	@Override
	public void processUpdate() {
		this.processList.repaint();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (ProcessController.hasActiveProcesses()) {
			int answer = JOptionPane.showConfirmDialog(this, "There are running processes. Do you want to stop them?",
					"Active processes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				ProcessController.shutdown();
				this.dispose();
			}
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}
