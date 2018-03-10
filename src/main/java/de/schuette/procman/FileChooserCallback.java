package de.schuette.procman;

import java.io.File;

import javafx.stage.FileChooser.ExtensionFilter;

public interface FileChooserCallback {

	public void fileSelected(File file, ExtensionFilter selectedFilter);

	public default void noFile() {
	}

}
