package com.github.schuettec.multiprocman.consolepreview;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.plaf.BorderUIResource;

import com.github.schuettec.multiprocman.AppendListener;
import com.github.schuettec.multiprocman.CounterExpressions;
import com.github.schuettec.multiprocman.CounterState;
import com.github.schuettec.multiprocman.ProcessController;
import com.github.schuettec.multiprocman.ProcessController.State;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.ProcessListener;
import com.github.schuettec.multiprocman.Resources;
import com.github.schuettec.multiprocman.console.AnsiColorTextPane;

public class ConsolePreview extends JPanel implements AppendListener, ProcessListener {

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

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		AnsiColorTextPane ansiText = new AnsiColorTextPane();
		ProcessDescriptor processDescriptor = new ProcessDescriptor();
		processDescriptor.setTitle("title");
		ConsolePreview preview = new ConsolePreview(new ProcessController(processDescriptor));
		ansiText.addAppendListener(preview);
		frame.add(preview, BorderLayout.NORTH);
		frame.add(ansiText, BorderLayout.CENTER);
		ansiText.appendANSI("Listening for transport dt_socket at address: 5012\n"
		    + "2018-06-25 12:56:24.920  INFO [user-service] [,] 17528 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@38aa816f: startup date [Mon Jun 25 12:56:24 CEST 2018]; root of context hierarchy\n"
		    + "2018-06-25 12:56:25.273  INFO [user-service] [,] 17528 --- [           main] f.a.AutowiredAnnotationBeanPostProcessor : JSR-330 'javax.inject.Inject' annotation found and supported for autowiring\n"
		    + "2018-06-25 12:56:25.364  INFO [user-service] [,] 17528 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'configurationPropertiesRebinderAutoConfiguration' of type [org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration$$EnhancerBySpringCGLIB$$66b84db6] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)\n"
		    + "                    ________________________________________________\n"
		    + "                   |  _____  ______      _   _ ________          __ |\n"
		    + "            /|     | |  __ \\|  ____|    | \\ | |  ____\\ \\        / / |\n"
		    + "            ||     | | |__) | |__ ______|  \\| | |__   \\ \\  /\\  / /  |\n"
		    + "       .----|-----,| |  _  /|  __|______| . ` |  __|   \\ \\/  \\/ /   |\n"
		    + "       ||  ||   ==|| | | \\ \\| |____     | |\\  | |____   \\  /\\  /    |\n"
		    + "  .-----'--'|   ==|| |_|  \\_\\______|    |_| \\_|______|   \\/  \\/     |\n"
		    + "  |)-      ~|     ||________________________________________________|\n"
		    + "  | ___     |     |____.________  ________________________________|\n"
		    + " [_/.-.\\\"--\"-------- //.-.  .-.\\\\/                 \\\\ .-.  .-. //\n"
		    + "   ( o )`===\"\"\"\"\"\"\"\"\"`( o )( o )                    `( o )( o )`\n"
		    + "    '-'                '-'  '-'                       '-'  '-'\n" + "\n" + " user-service v0.0.1\n" + "\n"
		    + "\n"
		    + "2018-06-25 12:56:26.309  INFO [renew-user-service] [,] 17528 --- [           main] c.r.r.u.UserServiceApplication           : The following profiles are active: dev\n"
		    + "2018-06-25 12:56:26.364  INFO [renew-user-service] [,] 17528 --- [           main] ationConfigEmbeddedWebApplicationContext : Refreshing",
		    false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

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

	@Override
	public void append(Color color, String s) {
		for (int i = 0; i < s.length(); i++) {
			int localCurY = HEIGHT - Y_INC;
			char c = s.charAt(i);
			if (c == '\n') {
				// Trigger line break
				bufferedImage = cropImage(bufferedImage);
				curX = INITIAL_X;
			} else {
				// Paint charatcer
				Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
				g.setColor(color);
				g.setStroke(new BasicStroke(Y_INC - 1));
				g.drawLine(curX, localCurY, curX + X_INC, localCurY);
				curX += X_INC;
			}
		}

		this.repaint();
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

}
