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

	private static BufferedImage scrollFree;
	private static BufferedImage scrollLock;
	private static BufferedImage save;
	private static BufferedImage clear;
	private static BufferedImage stop;
	private static BufferedImage stopForcibly;
	private static BufferedImage restart;
	private static BufferedImage terminal;
	private static BufferedImage folder;
	private static BufferedImage folderPlus;
	private static BufferedImage folderMinus;
	private static BufferedImage edit;

	static {
		try {
			check = ImageIO.read(getResource("/check-circle.png"));
			running = ImageIO.read(getResource("/eye.png"));
			alert = ImageIO.read(getResource("/x-circle.png"));
			question = ImageIO.read(getResource("/help-circle.png"));
			terminal = ImageIO.read(getResource("/terminal.png"));
			scrollFree = ImageIO.read(getResource("/scrollFree.png"));
			scrollLock = ImageIO.read(getResource("/scrollToBottom.png"));
			folder = ImageIO.read(getResource("/folder.png"));
			save = ImageIO.read(getResource("/save.png"));
			clear = ImageIO.read(getResource("/trash-2.png"));
			stop = ImageIO.read(getResource("/stop-circle.png"));
			stopForcibly = ImageIO.read(getResource("/stop-forcibly.png"));
			restart = ImageIO.read(getResource("/repeat.png"));
			folderPlus = ImageIO.read(getResource("/folder-plus.png"));
			folderMinus = ImageIO.read(getResource("/folder-minus.png"));
			edit = ImageIO.read(getResource("/edit.png"));
		} catch (IOException e) {
			ExceptionDialog.showException(e, "Some application resources could not be found!");
		}
	}

	public static BufferedImage getEdit() {
		return edit;
	}

	public static BufferedImage getFolderMinus() {
		return folderMinus;
	}

	public static BufferedImage getFolderPlus() {
		return folderPlus;
	}

	public static BufferedImage getFolder() {
		return folder;
	}

	public static BufferedImage getTerminal() {
		return terminal;
	}

	public static BufferedImage getSave() {
		return save;
	}

	public static BufferedImage getClear() {
		return clear;
	}

	public static BufferedImage getStop() {
		return stop;
	}

	public static BufferedImage getStopForcibly() {
		return stopForcibly;
	}

	public static BufferedImage getRestart() {
		return restart;
	}

	public static BufferedImage getScrollFree() {
		return scrollFree;
	}

	public static BufferedImage getScrollLock() {
		return scrollLock;
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
