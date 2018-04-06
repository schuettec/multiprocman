package com.github.schuettec.multiprocman.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.AppendListener;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.TextView;

public class ScrollableAnsiColorTextPaneContainer extends JScrollPane implements TextView, MouseWheelListener {

	private static final String CMD_SHOW_SEARCH = "show-search";
	private static final String CMD_HIDE_SEARCH = "hide-search";

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private EventListenerSupport<AutoScrollToBottomListener> autoScrollToBottomListeners;
	private EventListenerSupport<SearchFieldListener> searchFieldListeners;

	private AnsiColorTextPane textPane;

	private boolean autoScrollToBottom = true;

	private boolean isAtBottom;

	boolean ignoreAdjustment = false;

	private JTextField txtSearch;
	private WordSearch wordSearch;

	private JPanel pnlSearch;
	private KeyListener searchKeyListener = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					wordSearch.search(((JTextField) e.getSource()).getText());
				}
			});
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}
	};

	private AppendListener searchOnAppend = (c, s) -> {
		wordSearch.search(txtSearch.getText());
	};

	private AbstractAction startSearchAction = new AbstractAction() {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			startSearch();
		}

	};
	private AbstractAction finishSearchAction = new AbstractAction(null, new ImageIcon(Resources.getX())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			finishSearch();
		}

	};

	private AbstractAction prevOccurrence = new AbstractAction(null, new ImageIcon(Resources.getUp())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			wordSearch.gotoPrevOccurrence();
		}

	};
	private AbstractAction nextOccurrence = new AbstractAction(null, new ImageIcon(Resources.getDown())) {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			wordSearch.gotoNextOccurrence();
		}

	};

	private boolean oldAutoscrollToBottom;
	private boolean searchFieldOpen;

	public ScrollableAnsiColorTextPaneContainer(AnsiColorTextPane ansiColorTextPane) {
		super();
		this.textPane = ansiColorTextPane;
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		setViewportView(textPane);
		this.autoScrollToBottomListeners = new EventListenerSupport<>(AutoScrollToBottomListener.class);
		this.searchFieldListeners = new EventListenerSupport<>(SearchFieldListener.class);

		this.pnlSearch = new JPanel();

		JLabel lblSearch = new JLabel("Search:");
		lblSearch.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSearch.setPreferredSize(new Dimension(55, 14));
		pnlSearch.setLayout(new BorderLayout(0, 0));
		pnlSearch.add(lblSearch, BorderLayout.WEST);

		JPanel panel_1 = new JPanel();
		pnlSearch.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		txtSearch = new JTextField();
		txtSearch.setPreferredSize(new Dimension(150, 20));
		panel_1.add(txtSearch);

		wordSearch = new WordSearch(textPane);

		Component verticalStrut = Box.createVerticalStrut(20);
		verticalStrut.setPreferredSize(new Dimension(0, 6));
		panel_1.add(verticalStrut, BorderLayout.NORTH);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		verticalStrut_1.setPreferredSize(new Dimension(0, 6));
		panel_1.add(verticalStrut_1, BorderLayout.SOUTH);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(5, 0));
		panel_1.add(horizontalStrut, BorderLayout.WEST);

		JPanel panel = new JPanel();
		pnlSearch.add(panel, BorderLayout.EAST);

		JButton btnPrev = new JButton(prevOccurrence);
		panel.add(btnPrev);

		JButton btnNext = new JButton(nextOccurrence);
		panel.add(btnNext);

		JButton btnClose = new JButton(finishSearchAction);
		panel.add(btnClose);

		getVerticalScrollBar().addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				setAutoScrollToBottom(false);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

	}

	@Override
	public void addNotify() {
		super.addNotify();
		this.addMouseWheelListener(this);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
		    CMD_SHOW_SEARCH);
		getActionMap().put(CMD_SHOW_SEARCH, startSearchAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CMD_HIDE_SEARCH);
		getActionMap().put(CMD_HIDE_SEARCH, finishSearchAction);

		scrollToBottomOnDemand();
	}

	@Override
	public void removeNotify() {
		super.addNotify();
		this.removeMouseWheelListener(this);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		getActionMap().remove(CMD_SHOW_SEARCH);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		getActionMap().remove(CMD_HIDE_SEARCH);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Font oldFont = textPane.getFont();
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			if (e.isControlDown()) {
				int totalScrollAmount = e.getUnitsToScroll();
				Font deriveFont = oldFont.deriveFont((float) oldFont.getSize() - totalScrollAmount);
				textPane.setFont(deriveFont);
			} else {
				userScrolled();
			}
		}
	}

	public void addAutoScrollToBottomListener(AutoScrollToBottomListener listener) {
		autoScrollToBottomListeners.addListener(listener);
		autoScrollToBottomListeners.fire()
		    .autoScrollToBottomProperty(autoScrollToBottom);
	}

	public void removeAutoScrollToBottomListener(AutoScrollToBottomListener listener) {
		autoScrollToBottomListeners.removeListener(listener);
	}

	public void addSearchFieldListener(SearchFieldListener listener) {
		searchFieldListeners.addListener(listener);
		fireSearchFieldState();
	}

	public void removeSearchFieldListener(SearchFieldListener listener) {
		searchFieldListeners.removeListener(listener);
		fireSearchFieldState();
	}

	@Override
	public boolean isAutoScrollToBottom() {
		return autoScrollToBottom;
	}

	@Override
	public void setAutoScrollToBottom(boolean autoScrollToBottom) {
		this.autoScrollToBottom = autoScrollToBottom;
		if (autoScrollToBottom) {
			scrollToBottom();
		}
		autoScrollToBottomListeners.fire()
		    .autoScrollToBottomProperty(autoScrollToBottom);
	}

	@Override
	public void scrollToBottom() {
		final JScrollBar vBar = getVerticalScrollBar();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (vBar.isVisible())
					vBar.setValue(vBar.getMaximum() - vBar.getModel()
					    .getExtent());
				repaint();
			}
		});
	}

	protected void scrollToBottomOnDemand() {
		if (autoScrollToBottom) {
			scrollToBottom();
		}
	}

	private void userScrolled() {
		JScrollBar scroller = getVerticalScrollBar();
		if (scroller.isVisible()) {
			int value = scroller.getValue();
			int maxValue = scroller.getMaximum() - scroller.getModel()
			    .getExtent();
			isAtBottom = (value == maxValue);
		} else {
			isAtBottom = true;
		}
		setAutoScrollToBottom(isAtBottom);
	}

	@Override
	public void append(Color c, String s, boolean isSupportAsciiCodes) {
		ignoreAdjustment = true;
		textPane.append(c, s, isSupportAsciiCodes);
		scrollToBottomOnDemand();
		ignoreAdjustment = false;
	}

	@Override
	public void appendANSI(String s, boolean isSupportAsciiCodes) {
		ignoreAdjustment = true;
		textPane.appendANSI(s, isSupportAsciiCodes);
		scrollToBottomOnDemand();
		ignoreAdjustment = false;
	}

	private void fireSearchFieldState() {
		if (searchFieldOpen) {
			searchFieldListeners.fire()
			    .searchFieldOpen();
		} else {
			searchFieldListeners.fire()
			    .searchFieldClosed();
		}
	}

	public void startSearch() {
		this.oldAutoscrollToBottom = isAutoScrollToBottom();
		setAutoScrollToBottom(false);
		setColumnHeaderView(pnlSearch);
		txtSearch.requestFocus();
		txtSearch.addKeyListener(searchKeyListener);
		textPane.addAppendListener(searchOnAppend);
		searchFieldListeners.fire()
		    .searchFieldOpen();
		this.searchFieldOpen = true;
	}

	public void finishSearch() {
		setAutoScrollToBottom(oldAutoscrollToBottom);
		setColumnHeaderView(null);
		txtSearch.removeKeyListener(searchKeyListener);
		textPane.removeAppendListener(searchOnAppend);
		wordSearch.clear();
		searchFieldListeners.fire()
		    .searchFieldClosed();
		this.searchFieldOpen = false;
	}

}
