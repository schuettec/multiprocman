package de.schuette.procman.themes;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

import javafx.embed.swing.JFXPanel;

public class ThemeUtil {

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
}
