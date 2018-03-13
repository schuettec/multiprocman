package com.github.schuettec.multiprocman.themes;

import static java.util.Objects.requireNonNull;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

import javafx.embed.swing.JFXPanel;

public class ThemeUtil {

	public static boolean disableSettings = false;
	private static final WindowStateListener windowStateListener = new WindowStateListener() {
		@Override
		public void windowStateChanged(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

	};
	private static final ComponentListener componentListener = new ComponentListener() {

		@Override
		public void componentShown(ComponentEvent e) {

		}

		@Override
		public void componentResized(ComponentEvent e) {
			saveWindowState((Window) e.getComponent());
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			saveWindowState((Window) e.getComponent());
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
	};

	private static final WindowListener windowListener = new WindowListener() {

		@Override
		public void windowOpened(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

		@Override
		public void windowIconified(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

		@Override
		public void windowClosing(WindowEvent e) {
			saveWindowState(e.getWindow());
		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowActivated(WindowEvent e) {
			saveWindowState(e.getWindow());
		}
	};

	public static void saveWindowState(Window window) {
		if (window instanceof JFrame) {
			ThemeUtil.saveWindowLocation((JFrame) window);
		} else if (window instanceof JDialog) {
			JDialog jDialog = (JDialog) window;
			ThemeUtil.saveWindowLocation(jDialog);
		} else {
			ThemeUtil.saveWindowLocation(window);
		}
	}

	public static void loadWindowState(Window window) {
		if (window instanceof JFrame) {
			ThemeUtil.loadWindowLocation((JFrame) window);
		} else if (window instanceof JDialog) {
			JDialog jDialog = (JDialog) window;
			ThemeUtil.loadWindowLocation(jDialog);
		} else {
			ThemeUtil.loadWindowLocation(window);
		}
	}

	private static void saveWindowLocation(JFrame frame) {
		Preferences prefs = Preferences.userNodeForPackage(frame.getClass());
		saveWindowLocation(prefs, frame);
		int extendedState = frame.getExtendedState();
		prefs.putInt("extendedWindowState", extendedState);
	}

	private static void loadWindowLocation(JFrame frame) {
		if (disableSettings) {
			return;
		}
		Preferences prefs = Preferences.userNodeForPackage(frame.getClass());
		loadWindowLocation(prefs, frame);
		int extendedState = prefs.getInt("extendedWindowState", frame.getExtendedState());
		frame.setExtendedState(extendedState);
	}

	private static void saveWindowLocation(Window window) {
		saveWindowLocation(Preferences.userNodeForPackage(window.getClass()), window);
	}

	private static void saveWindowLocation(Preferences prefs, Window window) {
		Point location = window.getLocation();
		prefs.putInt("x", location.x);
		prefs.putInt("y", location.y);
		Dimension size = window.getSize();
		prefs.putInt("width", size.width);
		prefs.putInt("height", size.height);
	}

	private static void loadWindowLocation(Window window) {
		loadWindowLocation(Preferences.userNodeForPackage(window.getClass()), window);
	}

	private static void loadWindowLocation(Preferences prefs, Window window) {
		if (disableSettings) {
			return;
		}
		int x = prefs.getInt("x", -1);
		int y = prefs.getInt("y", -1);

		if (x == -1 || y == -1) {
			window.setLocationRelativeTo(null);
		} else {
			window.setLocation(new Point(x, y));
		}

		int width = prefs.getInt("width", -1);
		int height = prefs.getInt("height", -1);
		if (x > -1 && y > -1) {
			window.setSize(new Dimension(width, height));
		}
	}

	public static <C extends JComponent> void theme(C component, Class<? extends Theme<C>> service) {
		requireNonNull(component, "The component to be themed may not be null.");
		requireNonNull(service, "The theme type may not be null.");
		ServiceLoader<? extends Theme<C>> themeLoader = ServiceLoader.load(service);
		Iterator<? extends Theme<C>> it = themeLoader.iterator();
		if (it.hasNext()) {
			Theme<C> theme = it.next();
			theme.apply(component);
		}
	}

	public static void setLookAndFeel() {
		if (SystemUtils.IS_OS_WINDOWS) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void startJavaFX() {
		new JFXPanel();
	}

	public static void stopJavaFX() {
		try {
			javafx.application.Platform.exit();
		} catch (Exception e) {
		}
	}

	public static void installListeners(Window window) {
		window.addWindowStateListener(windowStateListener);
		window.addComponentListener(componentListener);
		window.addWindowListener(windowListener);
	}

	public static void deinstallListeners(Window window) {
		window.removeWindowStateListener(windowStateListener);
		window.removeComponentListener(componentListener);
		window.removeWindowListener(windowListener);
	}
}
