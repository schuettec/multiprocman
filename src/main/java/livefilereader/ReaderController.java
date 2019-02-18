package livefilereader;

import static java.lang.Math.max;
import static java.lang.Math.round;

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
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

import livefilereader.captor.FileReader;

public class ReaderController implements ProcessCallback {

	private JScrollBar lineScroller;
	private AnsiColorTextPane textView;

	private int currentLine = 0;
	private int viewLines = 0;
	private int lines = 0;
	private File outputFile;

	private boolean currentlyScrolling = false;

	private boolean jumpToLastLine = true;
	private boolean ignoreAdjustmentListener = false;

	private ComponentListener resizeListener = new ComponentListener() {

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			// The componentResized event is also thrown if the component width changes (long lines that are not wrapped).
			// Only update text if the number of view lines changes.
			int viewLinesOld = viewLines;
			determineMaxLines();
			if (viewLinesOld != viewLines) {
				updateScrollBar();

				if (lines < viewLines) {
					currentLine = 0;
					updateTextImmidiately();
				} else if (currentLine > lines - viewLines) {
					currentLine = Math.max(0, lines - viewLines);
					updateTextImmidiately();
				} else {
					updateText();
				}
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

			if (e.getValue() == (lineScroller.getMaximum() - lineScroller.getModel()
			    .getExtent())) {
				jumpToLastLine = true;
			} else if (currentLine != e.getValue()) {
				// Only update if the currentLine changed or if
				currentLine = e.getValue();
				updateText();
			}
		}
	};

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
	private FileReader fileReader;

	public ReaderController() {
		this.lineScroller = new JScrollBar(JScrollBar.VERTICAL);
		this.lineScroller.addAdjustmentListener(scrollerListener);
		this.lineScroller.addMouseListener(mouseListener);
		this.textView = new AnsiColorTextPane();
		this.textView.setWrapLines(false);
		ThemeUtil.theme(textView, AnsiColorTextPaneTheme.class);

		this.textView.addComponentListener(resizeListener);
		this.textView.setEditable(false);
	}

	protected void updateText() {
		// Only update text if the last line is not captured.
		if (!isCaptured(lines - 1)) {
			updateTextImmidiately();
		}
	}

	private void updateTextImmidiately() {
		System.out.println("UPDATING TEXT");
		String content = fileReader.readLinesFromFile(currentLine, Math.min(viewLines, lines));
		String parsed = parseBackspace(content);
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

						// Delete as many rows as needed to add the new line while not overstepping the viewLines.
						if (isCaptured(lines - 1)) {
							int linesInView = textView.getText()
							    .split("\n").length - 1;

							int toRemove = Math.max(0, linesInView - viewLines);

							for (int r = 0; r < toRemove; r++) {
								textView.removeFirstLine();
								currentLine++;
							}
							textView.appendANSI(line, true);
						}
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
		jumpToLastLine();
	}

	private void jumpToLastLine() {
		if (jumpToLastLine) {
			ignoreAdjustmentListener = true;
			BoundedRangeModel model = this.lineScroller.getModel();
			model.setMinimum(0);
			model.setExtent(max(1, (int) round(viewLines / 2.0)));
			model.setMaximum(Math.max(0, lines - viewLines));
			model.setValue(lines - viewLines);
			ignoreAdjustmentListener = false;
		}
	}

	@Override
	public void append(String string) {
		// If last line is currently captured
		if (isCaptured(lines - 1)) {
			// append the content
			try {
				System.out.println("append: " + string);
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
	}

	private void updateScrollBar() {
		if (!currentlyScrolling) {
			ignoreAdjustmentListener = true;
			BoundedRangeModel model = this.lineScroller.getModel();
			model.setMinimum(0);
			// model.setExtent(max(1, (int) round(Math.max(0, lines - viewLines) / 2.0)));
			model.setExtent(1);
			model.setMaximum(Math.max(0, lines - viewLines));
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

	@Override
	public void exited() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// DO SOMETHING
			}
		});
	}

	public JScrollBar getLineScroller() {
		return lineScroller;
	}

	public JTextPane getTextView() {
		return textView;
	}

}
