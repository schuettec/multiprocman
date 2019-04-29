package com.github.schuettec.multiprocman.process;

import static java.util.Objects.isNull;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.process.captor.FileReader;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

public class ReaderController implements ProcessCallback {

	private EventListenerSupport<ViewFrameListener> viewFrameListeners;

	private JScrollBar lineScroller;
	private AnsiColorTextPane textView;

	private int currentLine = 0;
	private int viewLines = 0;
	private int lines = 0;
	private File outputFile;

	private boolean currentlyScrolling = false;

	private boolean jumpToLastLine = true;
	private volatile boolean ignoreAdjustmentListener = false;
	private FileReader fileReader;

	private ComponentListener resizeListener = new ComponentListener() {

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			int viewLinesOld = viewLines;
			determineMaxLines();

			if (viewLinesOld != viewLines) {
				updateViewFrame(false);
				viewFrameListeners.fire()
				    .viewFrameChanged(viewLines);
			}
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
	};

	private AdjustmentListener scrollerListener = new AdjustmentListener() {

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if (ignoreAdjustmentListener) {
				return;
			}

			int newLine = e.getValue();
			setJumpToLastLine(newLine + lineScroller.getModel()
			    .getExtent() >= lineScroller.getMaximum());
			setCurrentLine(newLine);
			updateViewFrame(false);
		}
	};

	private void setCurrentLine(int line) {
		currentLine = line;
		updateScrollBar();
	}

	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			currentlyScrolling = true;
			jumpToLastLine = false;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			currentlyScrolling = false;
		}

	};

	public ReaderController() {
		this.viewFrameListeners = new EventListenerSupport<>(ViewFrameListener.class);
		this.lineScroller = new JScrollBar(JScrollBar.VERTICAL);
		MouseWheelScroller mouseWheelListener = new MouseWheelScroller(lineScroller);
		this.lineScroller.addMouseWheelListener(mouseWheelListener);
		this.lineScroller.addAdjustmentListener(scrollerListener);
		this.lineScroller.addMouseListener(mouseListener);
		updateScrollBar();

		this.textView = new AnsiColorTextPane();
		this.textView.addMouseWheelListener(mouseWheelListener);
		this.textView.setWrapLines(false);
		ThemeUtil.theme(textView, AnsiColorTextPaneTheme.class);

		this.textView.addComponentListener(resizeListener);
		this.textView.setEditable(false);
	}

	public void addListener(ViewFrameListener listener) {
		viewFrameListeners.addListener(listener);
	}

	public void removeListener(ViewFrameListener listener) {
		viewFrameListeners.removeListener(listener);
	}

	private void updateViewFrame(boolean async) {
		if (viewLines == 0 || lines == 0) {
			return;
		}

		if (isViewFrameUnderflow()) {
			// View frame underflow!
			// If there are not enough lines in the output to fill the view frame:
			setCurrentLine(Math.max(0, lines - 1 - viewLines));
			int lastViewableLine = (lines - 1);
			toViewFrame(async, currentLine, lastViewableLine);
		} else {
			// No view frame underflow.
			int lastViewableLine = currentLine + viewLines;
			if (lastViewableLine >= lines) {
				lastViewableLine = lines - 1;
				currentLine = lastViewableLine - viewLines;
			}

			toViewFrame(async, currentLine, lastViewableLine);
		}
		updateScrollBar();
	}

	private boolean isViewFrameUnderflow() {
		return lines - 1 <= viewLines;
	}

	public void close() {
		fileReader.close();
	}

	/**
	 * @return Returns the output capturing file size or <code>null</code> if the file is inaccessible.
	 */
	public Long getCaptureFileSize() {
		if (isNull(fileReader)) {
			return null;
		} else {
			return fileReader.getFileSize();
		}
	}

	private void toViewFrame(boolean async, int fromLine, int toLine) {
		String content = fileReader.readLinesFromFile(fromLine, toLine);
		String parsed = parseBackspace(content);
		if (async) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						refreshTextView(parsed);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			refreshTextView(parsed);
		}
	}

	private void refreshTextView(String parsed) {
		textView.clear();
		textView.appendANSI(parsed, true);
	}

	@Override
	public void output(int lines, String line) {
		this.lines = lines;
		if (!currentlyScrolling || isCaptured(lines - 1)) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						updateScrollBar();

						if (isCaptured(lines - 1) && lines >= viewLines) {
							// Delete as many rows as needed to add the new line while not overstepping the viewLines.
							int linesInView = textView.getText()
							    .split("\n").length - 1;
							if (linesInView >= viewLines) {
								textView.removeFirstLine();
							}
							currentLine++;
						}
						textView.appendANSI(line, true);

						_jumpToLastLine();
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void _jumpToLastLine() {
		if (jumpToLastLine) {
			updateScrollBar();
			ignoreAdjustmentListener = true;
			BoundedRangeModel model = this.lineScroller.getModel();
			setCurrentLine(lines - viewLines);
			ignoreAdjustmentListener = false;
		}
	}

	@Override
	public void append(String string) {
		// If last line is currently captured
		if (isCaptured(lines - 1)) {
			// append the content
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						string.chars()
						    .forEach(c -> {
							    if (c == 0x8) {
								    textView.backspace(1);
							    } else {
								    textView.appendANSI(String.valueOf((char) c), true);
							    }
						    });
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isCaptured(int line) {
		if (line < 0) {
			return true;
		}
		if (jumpToLastLine) {
			return true;
		}
		return currentLine <= line && currentLine + viewLines >= line;
	}

	private String parseBackspace(String content) {
		// TODO: Check if ASCII codes should be parsed
		if (content.contains(String.valueOf((char) 0x8))) {
			StringBuilder b = new StringBuilder();
			content.chars()
			    .forEach(c -> {
				    if (c == 0x8) {
					    b.delete(b.length() - 1, b.length());
				    } else {
					    b.append((char) c);
				    }
			    });
			return b.toString();
		} else {
			return content;
		}
	}

	private void determineMaxLines() {
		FontMetrics fontMetrics = textView.getFontMetrics(textView.getFont());
		viewLines = (textView.getHeight() - textView.getInsets().top - textView.getInsets().bottom)
		    / fontMetrics.getHeight();

		Container parent = SwingUtilities.getUnwrappedParent(textView.getParent());
		if (parent instanceof JScrollPane) {
			JScrollPane jScrollPane = (JScrollPane) parent;
			if (jScrollPane.getHorizontalScrollBar()
			    .isVisible()) {
				viewLines -= 1;
			}
		}

	}

	private void updateScrollBar() {
		if (!currentlyScrolling) {
			ignoreAdjustmentListener = true;
			BoundedRangeModel model = this.lineScroller.getModel();
			model.setMinimum(0);
			model.setValue(currentLine);
			model.setExtent(viewLines);
			int max = Math.max(0, lines - 1);
			model.setMaximum(max);
			ignoreAdjustmentListener = false;
		}
	}

	@Override
	public void started(ProcessOutputInfo fileInfo, File outputFile, Charset charset) {
		this.outputFile = outputFile;
		try {
			this.fileReader = new FileReader(charset, fileInfo);
		} catch (FileNotFoundException e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ExceptionDialog.showException(textView, e, "Cannot open file for capturing application output.");
				}
			});
		}
	}

	@Override
	public void cannotWriteOutput(File outputFile, Exception cause) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				cause.printStackTrace();
			}
		});
	}

	@Override
	public void cannotStartProcess(Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				e.printStackTrace();
			}
		});
	}

	public JScrollBar getLineScroller() {
		return lineScroller;
	}

	public AnsiColorTextPane getTextView() {
		return textView;
	}

	@Override
	public void jumpToLastLine(int lines) {
		this.lines = lines;
		if (jumpToLastLine) {
			jumpToLastLine();
		}
	}

	public void jumpToLastLine() {
		_jumpToLastLine();
		updateViewFrame(true);
	}

	public boolean isJumpToLastLine() {
		return jumpToLastLine;
	}

	public void setJumpToLastLine(boolean jumpToLastLine) {
		this.jumpToLastLine = jumpToLastLine;
		viewFrameListeners.fire()
		    .autoScrollStateChanged(this.jumpToLastLine);
	}

	public String getLastLines(int linesCount) {
		int fromLine = Math.max(0, lines - 1 - linesCount);
		int toLine = Math.max(0, lines - 1);
		return fileReader.readLinesFromFile(fromLine, toLine);
	}

}
