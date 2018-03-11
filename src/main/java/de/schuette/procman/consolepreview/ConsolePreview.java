package de.schuette.procman.consolepreview;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.plaf.BorderUIResource;

import de.schuette.procman.AppendListener;
import de.schuette.procman.ProcessController;
import de.schuette.procman.ProcessController.State;
import de.schuette.procman.ProcessDescriptor;
import de.schuette.procman.ProcessListener;
import de.schuette.procman.Resources;

public class ConsolePreview extends JPanel implements AppendListener, ProcessListener {

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

	private BufferedImage bufferedImage;

	private int curX = INITIAL_X;

	private boolean selected = false;

	private State processState;
	private ProcessDescriptor descriptor;
	private BufferedImage titleImage;

	public ConsolePreview(ProcessDescriptor descriptor) {
		super();
		this.descriptor = descriptor;
		initialize();
	}

	private void initialize() {
		this.setBorder(BorderUIResource.getRaisedBevelBorderUIResource());
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Insets insets = this.getInsets();
		this.titleImage = new BufferedImage(HEIGHT - insets.top - insets.bottom, TITLE_SPACE_X, BufferedImage.TYPE_INT_RGB);

		clearImage(bufferedImage, Color.BLACK);
		drawTitleImage();

	}

	private void clearImage(BufferedImage bufferedImage, Color clearColor) {
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.setColor(clearColor);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
	}

	public void clear() {
		clearImage(bufferedImage, Color.BLACK);
	}

	private void drawTitleImage() {
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
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Insets insets = this.getInsets();
		if (!selected) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		}
		g2d.drawImage(bufferedImage, insets.left + TITLE_SPACE_X, insets.top, this);
		drawTitle(g2d);
		drawStateIcon(g2d);
		// drawQueries(g2d);
	}

	private void drawQueries(Graphics2D g2d) {
		Insets insets = this.getInsets();
		int y = 19;
		int x = getWidth() - insets.right - COUNTER_DIMENSIONS - 2;
		for (int i = 0; i < 6; i++) {
			g2d.setColor(Color.RED);
			g2d.fillOval(x, y, COUNTER_DIMENSIONS, COUNTER_DIMENSIONS);
			g2d.setColor(Color.WHITE);
			g2d.drawOval(x, y, COUNTER_DIMENSIONS, COUNTER_DIMENSIONS);
			String counter = "99";
			Rectangle2D stringBounds = g2d.getFontMetrics()
			    .getStringBounds(counter, g2d);
			int xStr = x + 1 + (int) ((COUNTER_DIMENSIONS - stringBounds.getWidth()) / 2.0);
			int yStr = (int) (y + stringBounds.getHeight());
			g2d.drawString(counter, xStr, yStr);
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

	@Override
	public void append(Color c, String s) {

		List<String> lines = Arrays.asList(s.split("\\\n|\\\r\\\n"));
		Iterator<String> it = lines.iterator();
		while (it.hasNext()) {
			int localCurY = HEIGHT - Y_INC;
			String line = it.next();
			int length = line.length();
			int endX = length * X_INC;
			Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
			g.setColor(c);
			g.setStroke(new BasicStroke(Y_INC - 1));
			g.drawLine(curX, localCurY, curX + endX, localCurY);
			curX += endX;
			if (it.hasNext()) {
				bufferedImage = cropImage(bufferedImage);
				curX = INITIAL_X;
			} else {
				if (s.endsWith("\n") || s.endsWith("\r\n")) {
					bufferedImage = cropImage(bufferedImage);
					curX = INITIAL_X;
				}
			}
		}

		this.repaint();
	}

	private BufferedImage cropImage(BufferedImage oldImage) {
		BufferedImage img = oldImage.getSubimage(0, INITIAL_Y + Y_INC, WIDTH, HEIGHT - INITIAL_Y - Y_INC);
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

}
