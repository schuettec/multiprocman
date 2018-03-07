package de.schuette.procman;

import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
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

import de.schuette.procman.console.AnsiColorTextPane;
import de.schuette.procman.console.AutoScrollToBottomListener;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.themes.Theme;
import de.schuette.procman.themes.console.AnsiColorTextPaneTheme;

public class MainFrame extends JFrame implements Appendable {

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
					final MainFrame frame = new MainFrame();
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.addConsole(new AnsiColorTextPane());
					frame.setVisible(true);

					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							AtomicInteger index = new AtomicInteger();
							while (true) {
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										Random r = new Random();
										int i = index.incrementAndGet();
										Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
										frame.append(c, i + " " + "Hallo\n");
										c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
										frame.append(c,
												i + " " + "Duuuuuuuuuuuuuuuuuuu\nESEEEEEEEEEEEEEEEEEEEEEEEEEEL");
										c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
										frame.append(c, i + " " + "MÖÖÖÖÖÖÖÖÖÖÖÖÖP\n");
										c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
										frame.append(c, i + " " + "TEST\n");
									}
								});
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								if (index.get() == 35) {
									return;
								}
							}
						}

					});
					t.start();
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
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

	public void addConsole(AnsiColorTextPane textPane) {
		boolean activateFirst = this.processes.isEmpty();
		theme(textPane);
		ProcessController processController = new ProcessController(textPane);
		this.processes.addElement(processController);
		if (activateFirst) {
			this.processList.setSelectedIndex(0);
		}
	}

	private void theme(AnsiColorTextPane console) {
		theme(console, AnsiColorTextPaneTheme.class);
	}

	private <C extends JComponent> void theme(C component, Class<? extends Theme<C>> service) {
		requireNonNull(component, "The component to be themed may not be null.");
		requireNonNull(service, "The theme type may not be null.");
		ServiceLoader<? extends Theme<C>> themeLoader = ServiceLoader.load(service);
		Iterator<? extends Theme<C>> it = themeLoader.iterator();
		if (it.hasNext()) {
			Theme<C> theme = it.next();
			theme.apply(component);
		}
	}

	@Override
	public void append(Color c, String s) {
		currentProcess.getConsoleScroller().append(c, s);
		this.processList.repaint();
	}

	@Override
	public void appendANSI(String s) {
		currentProcess.getConsoleScroller().appendANSI(s);
		this.processList.repaint();
	}

}
