package livefilereader;

import static java.lang.Math.max;
import static java.lang.Math.round;

import java.awt.FontMetrics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane;
import com.github.schuettec.multiprocman.themes.ThemeUtil;
import com.github.schuettec.multiprocman.themes.console.AnsiColorTextPaneTheme;

public class ReaderController implements ProcessCallback {

	private JScrollBar lineScroller;
	private AnsiColorTextPane textView;

	private int currentLine = 0;
	private int viewLines = 0;
	private int lines = 0;
	private File outputFile;

	private ReentrantLock lock = new ReentrantLock();
	private RandomAccessFile input;
	private Charset charset;

	private ComponentListener resizeListener = new ComponentListener() {

		@Override
		public void componentShown(ComponentEvent e) {
			determineMaxLines();
			updateScrollBar();
			updateText();
		}

		@Override
		public void componentResized(ComponentEvent e) {
			determineMaxLines();
			updateScrollBar();
			updateText();
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
			if (currentLine != e.getValue() || lines <= viewLines) {
				currentLine = e.getValue();
				updateText();
			}
		}
	};
	private ProcessOutputInfo fileInfo;

	public ReaderController() {
		this.lineScroller = new JScrollBar(JScrollBar.VERTICAL);
		this.lineScroller.addAdjustmentListener(scrollerListener);
		this.textView = new AnsiColorTextPane();
		ThemeUtil.theme(textView, AnsiColorTextPaneTheme.class);

		this.textView.addComponentListener(resizeListener);
		this.textView.setEditable(false);

	}

	public void close() {
		try {
			this.input.close();
		} catch (IOException e) {
		}
	}

	protected void updateText() {
		int readableLines = lines - 1 - currentLine;
		int numberOfLines = Math.min(viewLines, readableLines);
		System.out
		    .println("Requesting curLine: " + currentLine + " lines to read " + numberOfLines + " max lines: " + lines);
		String content = readLinesFromFile(currentLine, numberOfLines);
		String parsed = parseBackspace(content);
		Document document = textView.getDocument();
		try {
			document.remove(0, document.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textView.appendANSI(parsed, true);
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

	protected String readLinesFromFile(int fromLine, int linesToRead) {
		try {
			lock.tryLock(1000, TimeUnit.SECONDS);
			try {
				return synchronizedReadLinesFromFile(fromLine, linesToRead);
			} finally {
				lock.unlock();
			}
		} catch (InterruptedException e) {
			return "Could not read application error due to another process accessing the output.";
		}
	}

	private String synchronizedReadLinesFromFile(int fromLine, int linesToRead) {
		try {
			int startOffsets = fileInfo.getLineByteOffset(fromLine);
			int endOffsets = fileInfo.getLineByteOffset(fromLine + linesToRead);
			input.seek(startOffsets);
			if (endOffsets - startOffsets < 0) {
				System.out.println("HÄÄÄÄ");
			}
			byte[] data = new byte[endOffsets - startOffsets];
			input.read(data, 0, data.length);
			return new String(data);
		} catch (IOException e) {
			return new StringBuilder().append("Could not read application output.")
			    .append(ExceptionUtils.getStackTrace(e))
			    .toString();
		}
	}

	private void determineMaxLines() {
		FontMetrics fontMetrics = textView.getFontMetrics(textView.getFont());
		viewLines = textView.getHeight() / fontMetrics.getHeight();
	}

	private void updateScrollBar() {
		BoundedRangeModel model = this.lineScroller.getModel();
		model.setMinimum(0);
		model.setExtent(max(1, (int) round(viewLines / 2.0)));
		model.setMaximum(Math.max(0, lines - viewLines));
	}

	@Override
	public void output(int lines) {
		this.lines = lines;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (lines < viewLines) {
						updateText();
					}
					updateScrollBar();
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

	@Override
	public void started(ProcessOutputInfo fileInfo, File outputFile, Charset charset) {
		this.outputFile = outputFile;
		this.fileInfo = fileInfo;
		try {
			this.input = new RandomAccessFile(outputFile, "r");
		} catch (FileNotFoundException e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ExceptionDialog.showException(textView, e, "Cannot open file for capturing application output.");
				}
			});
		}
		this.charset = charset;
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

	@Override
	public void asciiCode(int line, int ascii) {
		if ((ascii == 0x8) && isCaptured(line)) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						// Remove last character
						try {

							System.out.println("Remove @ Length: " + textView.getDocument()
							    .getLength());

							textView.getDocument()
							    .remove(textView.getDocument()
							        .getLength() - 1, 1);
						} catch (BadLocationException e) {
							e.printStackTrace();
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
						// Remove last character
						try {
							AttributeSet aset = StyleContext.getDefaultStyleContext()
							    .addAttributes(SimpleAttributeSet.EMPTY, new SimpleAttributeSet());
							int len = textView.getDocument()
							    .getLength();
							textView.getDocument()
							    .insertString(len, string, aset);
						} catch (BadLocationException e) {
							e.printStackTrace();
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
	}

	private boolean isCaptured(int line) {
		return currentLine <= line && currentLine + viewLines >= line;
	}

}
