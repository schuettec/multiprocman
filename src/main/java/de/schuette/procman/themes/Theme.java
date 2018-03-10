package de.schuette.procman.themes;

import javax.swing.JComponent;

public interface Theme<C extends JComponent> {

	public void apply(C component);

	public void reset(C component);

}
