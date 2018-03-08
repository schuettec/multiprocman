package de.schuette.procman;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

public class Resources {

	private static BufferedImage check;
	private static BufferedImage running;
	private static BufferedImage alert;
	private static BufferedImage question;
	private static BufferedImage terminal;
	private static BufferedImage scrollFree;
	private static BufferedImage scrollLock;

	static {
		try {
			check = ImageIO.read(getResource("/check-circle.png"));
			running = ImageIO.read(getResource("/eye.png"));
			alert = ImageIO.read(getResource("/x-circle.png"));
			question = ImageIO.read(getResource("/help-circle.png"));
			terminal = ImageIO.read(getResource("/terminal.png"));
			scrollFree = ImageIO.read(getResource("/scrollFree.png"));
			scrollLock = ImageIO.read(getResource("/scrollToBottom.png"));
		} catch (IOException e) {
			ExceptionDialog.showException(e, "Some application resources could not be found!");
		}

	}

	public static BufferedImage getScrollFree() {
		return scrollFree;
	}

	public static BufferedImage getScrollLock() {
		return scrollLock;
	}

	public static BufferedImage getTerminal() {
		return terminal;
	}

	public static BufferedImage getQuestion() {
		return question;
	}

	public static BufferedImage getCheckState() {
		return check;
	}

	public static BufferedImage getRunningState() {
		return running;
	}

	public static BufferedImage getAlertState() {
		return alert;
	}

	private static URL getResource(String name) {
		return Resource.class.getResource(name);
	}
}
