package de.schuette.procman.consolepreview;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.plaf.BorderUIResource;

import de.schuette.procman.AppendListener;

public class ConsolePreview extends JPanel implements AppendListener {

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

	public static final int WIDTH = 110;
	public static final int HEIGHT = 140;

	private BufferedImage bufferedImage;

	private int curX = INITIAL_X;

	private boolean selected = false;

	public ConsolePreview() {
		super();
		initialize();
	}

	private void initialize() {
		this.setBorder(BorderUIResource.getRaisedBevelBorderUIResource());
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT + 20));
		this.bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Insets insets = this.getInsets();
		if (!selected) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		}
		g2d.drawImage(bufferedImage, insets.left, insets.top, this);
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

}
