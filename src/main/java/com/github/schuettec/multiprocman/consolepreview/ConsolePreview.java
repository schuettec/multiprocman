package com.github.schuettec.multiprocman.consolepreview;

import static com.github.schuettec.multiprocman.AnsiColors.getANSIColor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.plaf.BorderUIResource;

import com.github.schuettec.multiprocman.CounterExpressions;
import com.github.schuettec.multiprocman.CounterState;
import com.github.schuettec.multiprocman.ProcessController;
import com.github.schuettec.multiprocman.ProcessController.State;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.ProcessListener;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.process.ProcessCallback;

public class ConsolePreview extends JPanel implements ProcessListener, ProcessCallback {

	private static final int MAX_COUNTER_VALUE = 99;
	private static final int COUNTER_DIMENSIONS = 18;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int INITIAL_X = 1;
	private static final int INITIAL_Y = 1;
	private static final int X_INC = 1;
	private static final int Y_INC = 3;

	class Segment {
		int length;
		Color color;
	}

	public static final int TITLE_SPACE_X = 14;
	public static final int WIDTH = 120;
	public static final int HEIGHT = 140;
	public static final int MAX_COUNTERS = 6;

	private BufferedImage bufferedImage;

	private int curX = INITIAL_X;

	private boolean selected = false;

	private State processState;
	private ProcessController controller;
	private BufferedImage titleImage;

	private Color cReset = Color.getHSBColor(0.000f, 0.000f, 1.000f);

	private Color colorCurrent = cReset;
	private String remaining = "";

	public ConsolePreview(ProcessController controller) {
		super();
		this.controller = controller;
		initialize();
	}

	private void initialize() {
		toUnselectedBorder();
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Insets insets = this.getInsets();
		this.titleImage = new BufferedImage(HEIGHT - insets.top - insets.bottom, TITLE_SPACE_X, BufferedImage.TYPE_INT_RGB);
		clearImage(bufferedImage, Color.BLACK);
		drawTitleImage();
	}

	private void toUnselectedBorder() {
		this.setBorder(BorderUIResource.getRaisedBevelBorderUIResource());
	}

	private void toSelectedBorder() {
		this.setBorder(BorderUIResource.getLoweredBevelBorderUIResource());
	}

	private void clearImage(BufferedImage bufferedImage, Color clearColor) {
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.setColor(clearColor);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
	}

	@Override
	public void clear() {
		clearImage(bufferedImage, Color.BLACK);
	}

	private void drawTitleImage() {
		ProcessDescriptor descriptor = controller.getProcessDescriptor();
		clearImage(titleImage, descriptor.getColor());
		Graphics2D g2d = (Graphics2D) titleImage.getGraphics();
		g2d.setColor(Color.BLACK);
		String title = descriptor.getTitle();
		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D stringBounds = fontMetrics.getStringBounds(title, g2d);
		double titleWidth = stringBounds.getWidth();
		int x = (int) ((titleImage.getWidth() - titleWidth) / 2.0);
		g2d.drawString(title, x, titleImage.getHeight() - fontMetrics.getDescent());
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (selected) {
			toSelectedBorder();
		} else {
			toUnselectedBorder();
		}

		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Insets insets = this.getInsets();
		if (!selected) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		}
		g2d.drawImage(bufferedImage, insets.left + TITLE_SPACE_X, insets.top, this);
		drawTitle(g2d);
		drawStateIcon(g2d);

		ProcessDescriptor descriptor = controller.getProcessDescriptor();
		if (descriptor.hasCounters()) {
			drawCounters(g2d);
		}
	}

	private void drawCounters(Graphics2D g2d) {
		Insets insets = this.getInsets();
		int y = 19;
		int x = getWidth() - insets.right - COUNTER_DIMENSIONS - 2;
		CounterExpressions counters = controller.getCounterExpressions();
		List<CounterState> states = counters.getFirst(MAX_COUNTERS);
		for (int i = 0; i < states.size(); i++) {
			CounterState counter = states.get(i);
			g2d.setColor(counter.getColor());
			g2d.fillOval(x, y, COUNTER_DIMENSIONS, COUNTER_DIMENSIONS);
			g2d.setColor(Color.WHITE);
			g2d.drawOval(x, y, COUNTER_DIMENSIONS, COUNTER_DIMENSIONS);
			int count = counter.getCount();
			if (count >= MAX_COUNTER_VALUE) {
				count = MAX_COUNTER_VALUE;
			}
			String countString = String.valueOf(count);
			Rectangle2D stringBounds = g2d.getFontMetrics()
			    .getStringBounds(countString, g2d);
			int xStr = x + 1 + (int) ((COUNTER_DIMENSIONS - stringBounds.getWidth()) / 2.0);
			int yStr = (int) (y + stringBounds.getHeight());
			g2d.drawString(countString, xStr, yStr);
			y += COUNTER_DIMENSIONS + 2;
		}
	}

	private void drawTitle(Graphics2D g2d) {
		Insets insets = this.getInsets();
		AffineTransform orig = g2d.getTransform();
		g2d.rotate(-Math.PI / 2);
		g2d.drawImage(titleImage, -HEIGHT + insets.top, insets.left, null);
		g2d.setTransform(orig);
	}

	private void drawStateIcon(Graphics2D g2d) {
		Insets insets = this.getInsets();

		BufferedImage stateIcon = null;
		if (processState == null) {
			stateIcon = Resources.getQuestion();
		} else {
			switch (processState) {
				case RUNNING:
					stateIcon = Resources.getRunningState();
					break;
				case STOPPED_OK:
					stateIcon = Resources.getCheckState();
					break;
				case STOPPED_ALERT:
					stateIcon = Resources.getAlertState();
					break;
				case NOT_STARTED:
				case ABANDONED:
				case STOPPING:
					stateIcon = Resources.getQuestion();
					break;
			}
		}
		int x = getWidth() - insets.right - stateIcon.getWidth() - 2;
		int y = insets.top;
		g2d.drawImage(stateIcon, x, y, this);
	}

	private void _appendANSI(String s) { // convert ANSI color codes first
		int aPos = 0; // current char position in addString
		int aIndex = 0; // index of next Escape sequence
		int mIndex = 0; // index of "m" terminating Escape sequence
		String tmpString = "";
		boolean stillSearching = true; // true until no more Escape sequences
		String addString = remaining + s;
		remaining = "";

		if (addString.length() > 0) {
			aIndex = addString.indexOf("\u001B"); // find first escape
			if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
				_append(colorCurrent, addString);
				return;
			}
			// otherwise There is an escape character in the string, so we must process it

			if (aIndex > 0) { // Escape is not first char, so send text up to first escape
				tmpString = addString.substring(0, aIndex);
				_append(colorCurrent, tmpString);
				aPos = aIndex;
			}
			// aPos is now at the beginning of the first escape sequence

			stillSearching = true;
			while (stillSearching) {
				mIndex = addString.indexOf("m", aPos); // find the end of the escape sequence
				if (mIndex < 0) { // the buffer ends halfway through the ansi string!
					remaining = addString.substring(aPos, addString.length());
					stillSearching = false;
					continue;
				} else {
					tmpString = addString.substring(aPos, mIndex + 1);
					colorCurrent = getANSIColor(tmpString, cReset);
				}
				aPos = mIndex + 1;
				// now we have the color, send text that is in that color (up to next escape)

				aIndex = addString.indexOf("\u001B", aPos);

				if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
					tmpString = addString.substring(aPos, addString.length());
					_append(colorCurrent, tmpString);
					stillSearching = false;
					continue; // jump out of loop early, as the whole string has been sent now
				}

				// there is another escape sequence, so send part of the string and prepare for
				// the next
				tmpString = addString.substring(aPos, aIndex);
				aPos = aIndex;
				_append(colorCurrent, tmpString);
			} // while there's text in the input buffer
		}
	}

	private void _append(Color color, String s) {
		for (int i = 0; i < s.length(); i++) {
			int localCurY = HEIGHT - Y_INC;
			char c = s.charAt(i);
			if (c == '\n') {
				// Trigger line break
				bufferedImage = cropImage(bufferedImage);
				curX = INITIAL_X;
			} else {
				if (c != ' ') {
					// Paint character
					Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
					g.setColor(color);
					g.setStroke(new BasicStroke(Y_INC - 1));
					g.drawLine(curX, localCurY, curX + X_INC, localCurY);
				}
				curX += X_INC;
			}
		}
	}

	private BufferedImage cropImage(BufferedImage oldImage) {
		BufferedImage img = oldImage.getSubimage(0, INITIAL_Y + 1, WIDTH, HEIGHT - INITIAL_Y - 1);
		BufferedImage newImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = newImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.drawImage(img, 0, 0, null);
		return newImage;
	}

	public void setIsSelected(boolean isSelected) {
		this.selected = isSelected;
	}

	@Override
	public void processUpdate(ProcessController controller) {
		this.processState = controller.getState();
	}

	@Override
	public void output(int lines, String line) {
		_appendANSI(line);
	}

	@Override
	public void append(String string) {
		_appendANSI(string);
	}

	@Override
	public void jumpToLastLine(int lines) {
		String lastLines = controller.getLastLines((int) Math.round(HEIGHT / (double) Y_INC));
		_appendANSI(lastLines);
	}

	@Override
	public void processOutput(ProcessController processController) {
	}
}
