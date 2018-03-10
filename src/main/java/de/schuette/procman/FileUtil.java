package de.schuette.procman;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import de.schuette.procman.themes.ThemeUtil;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileUtil {

	public enum Type {
		OPEN,
		SAVE;
	}

	public static void showFileChooser(Type type, FileChooserCallback fileConsumer) {
		showFileChooser(type, null, fileConsumer);
	}

	public static void showFileChooser(Type type, Consumer<List<ExtensionFilter>> extensionFilterProvider,
	    FileChooserCallback fileConsumer) {
		requireNonNull(fileConsumer, "File consumer must not be null!");
		ThemeUtil.startJavaFX();
		Semaphore s = new Semaphore(1);
		s.acquireUninterruptibly();
		javafx.application.Platform.runLater(new Runnable() {

			@Override
			public void run() {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Resource File");
				ObservableList<ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
				List<ExtensionFilter> list = new LinkedList<>();
				if (nonNull(extensionFilterProvider)) {
					extensionFilterProvider.accept(list);
				}
				extensionFilters.addAll(list);
				File selectedFile = null;
				if (type == Type.OPEN) {
					selectedFile = fileChooser.showOpenDialog(null);
				} else {
					selectedFile = fileChooser.showSaveDialog(null);
				}
				if (nonNull(selectedFile)) {
					fileConsumer.fileSelected(selectedFile, fileChooser.getSelectedExtensionFilter());
				} else {
					fileConsumer.noFile();
				}
				s.release();
			}
		});
		s.acquireUninterruptibly();
		ThemeUtil.stopJavaFX();
	}

}
