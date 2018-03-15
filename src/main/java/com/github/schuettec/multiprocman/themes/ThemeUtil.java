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

	private static final WindowStateListener windowStateListener = new WindowStateListener() {
		@Override
		public void windowStateChanged(WindowEvent e) {
			saveWindow(e.getWindow());
		}

	};
	private static final ComponentListener componentListener = new ComponentListener() {

		@Override
		public void componentShown(ComponentEvent e) {

		}

		@Override
		public void componentResized(ComponentEvent e) {
			saveWindow((Window) e.getComponent());
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			saveWindow((Window) e.getComponent());
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
	};

	private static final WindowListener windowListener = new WindowListener() {

		@Override
		public void windowOpened(WindowEvent e) {
			saveWindow(e.getWindow());
		}

		@Override
		public void windowIconified(WindowEvent e) {
			saveWindow(e.getWindow());
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			saveWindow(e.getWindow());
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			saveWindow(e.getWindow());
		}

		@Override
		public void windowClosing(WindowEvent e) {
			saveWindow(e.getWindow());
		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowActivated(WindowEvent e) {
			saveWindow(e.getWindow());
		}
	};

	public static void saveWindow(Window window) {
		Preferences prefs = Preferences.userNodeForPackage(window.getClass())
		    .node(window.getClass()
		        .getSimpleName());
		if (window instanceof JFrame) {
			JFrame jFrame = (JFrame) window;
			savePosition(prefs, jFrame);
			saveDimension(prefs, jFrame);
			saveWindowState(prefs, jFrame);
		} else if (window instanceof JDialog) {
			JDialog jDialog = (JDialog) window;
			savePosition(prefs, jDialog);
			saveDimension(prefs, jDialog);
		} else {
			savePosition(prefs, window);
			saveDimension(prefs, window);
		}
	}

	public static void loadWindow(Window window) {
		Preferences prefs = Preferences.userNodeForPackage(window.getClass())
		    .node(window.getClass()
		        .getSimpleName());
		if (window instanceof JFrame) {
			JFrame jFrame = (JFrame) window;
			loadPosition(prefs, jFrame);
			loadDimension(prefs, jFrame);
			loadWindow(prefs, jFrame);
		} else if (window instanceof JDialog) {
			JDialog jDialog = (JDialog) window;
			loadPosition(prefs, jDialog);
			loadDimension(prefs, jDialog);
		} else {
			loadPosition(prefs, window);
			loadDimension(prefs, window);
		}
	}

	private static void resetDimension(Preferences prefs) {
		prefs.putInt("width", -1);
		prefs.putInt("height", -1);
	}

	private static void saveWindowState(Preferences prefs, JFrame frame) {
		int extendedState = frame.getExtendedState();
		if (extendedState == JFrame.MAXIMIZED_BOTH) {
			resetDimension(prefs);
		}
		prefs.putInt("extendedWindowState", extendedState);
	}

	private static void saveDimension(Preferences prefs, Window window) {
		Dimension size = window.getSize();
		prefs.putInt("width", size.width);
		prefs.putInt("height", size.height);
	}

	private static void savePosition(Preferences prefs, Window window) {
		Point location = window.getLocation();
		prefs.putInt("x", location.x);
		prefs.putInt("y", location.y);
	}

	private static void loadWindow(Preferences prefs, JFrame frame) {
		int extendedState = prefs.getInt("extendedWindowState", frame.getExtendedState());
		frame.setExtendedState(extendedState);
	}

	private static void loadDimension(Preferences prefs, Window window) {
		int width = prefs.getInt("width", -1);
		int height = prefs.getInt("height", -1);
		if (width == -1 || height == -1) {
			window.setSize(new Dimension(window.getPreferredSize()));
		} else {
			window.setSize(new Dimension(width, height));
		}
	}

	private static void loadPosition(Preferences prefs, Window window) {
		int x = prefs.getInt("x", -1);
		int y = prefs.getInt("y", -1);
		if (x == -1 || y == -1) {
			window.setLocationRelativeTo(null);
		} else {
			window.setLocation(new Point(x, y));
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
