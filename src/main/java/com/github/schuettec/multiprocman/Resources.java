package com.github.schuettec.multiprocman;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

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
	private static BufferedImage play;
	private static BufferedImage x;
	private static BufferedImage plus;
	private static BufferedImage appIcon;
	private static BufferedImage minus;
	private static BufferedImage up;
	private static BufferedImage down;
	private static BufferedImage upload;
	private static BufferedImage download;
	private static BufferedImage warning;
	private static BufferedImage remote;
	private static BufferedImage local;
	private static BufferedImage ok;
	private static BufferedImage branch;

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
			play = ImageIO.read(getResource("/play.png"));
			x = ImageIO.read(getResource("/x.png"));
			appIcon = ImageIO.read(getResource("/app.png"));
			plus = ImageIO.read(getResource("/plus.png"));
			minus = ImageIO.read(getResource("/minus.png"));
			up = ImageIO.read(getResource("/up.png"));
			down = ImageIO.read(getResource("/down.png"));
			upload = ImageIO.read(getResource("/upload.png"));
			download = ImageIO.read(getResource("/download.png"));
			warning = ImageIO.read(getResource("/warning.png"));
			remote = ImageIO.read(getResource("/remote.png"));
			local = ImageIO.read(getResource("/local.png"));
			ok = ImageIO.read(getResource("/ok.png"));
			branch = ImageIO.read(getResource("/branch.png"));
		} catch (IOException e) {
			ExceptionDialog.showException(null, e, "Some application resources could not be found!");
		}

	}

	public static BufferedImage getBranch() {
		return branch;
	}

	public static BufferedImage getOk() {
		return ok;
	}

	public static BufferedImage getLocal() {
		return local;
	}

	public static BufferedImage getRemote() {
		return remote;
	}

	public static BufferedImage getWarning() {
		return warning;
	}

	public static BufferedImage getExport() {
		return upload;
	}

	public static BufferedImage getImport() {
		return download;
	}

	public static BufferedImage getUp() {
		return up;
	}

	public static BufferedImage getDown() {
		return down;
	}

	public static BufferedImage getCheck() {
		return check;
	}

	public static BufferedImage getRunning() {
		return running;
	}

	public static BufferedImage getAlert() {
		return alert;
	}

	public static BufferedImage getPlus() {
		return plus;
	}

	public static BufferedImage getAppIcon() {
		return appIcon;
	}

	public static BufferedImage getMinus() {
		return minus;
	}

	public static BufferedImage getX() {
		return x;
	}

	public static BufferedImage getPlay() {
		return play;
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
		return Resources.class.getResource(name);
	}

	public static BufferedImage getApplicationIcon() {
		return appIcon;
	}
}
