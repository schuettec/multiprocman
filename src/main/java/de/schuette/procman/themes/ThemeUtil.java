package de.schuette.procman.themes;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JComponent;

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

}
