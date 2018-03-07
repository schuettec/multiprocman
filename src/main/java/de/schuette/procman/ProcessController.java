package de.schuette.procman;

import de.schuette.procman.console.AnsiColorTextPane;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.consolepreview.ConsolePreview;
import de.schuette.procman.themes.ThemeUtil;
import de.schuette.procman.themes.console.AnsiColorTextPaneTheme;

public class ProcessController {

	private ScrollableAnsiColorTextPaneContainer consoleScroller;
	private AnsiColorTextPane textPane;
	private ConsolePreview consolePreview;

	public ProcessController() {
		this.textPane = new AnsiColorTextPane();
		ThemeUtil.theme(textPane, AnsiColorTextPaneTheme.class);

		this.consoleScroller = new ScrollableAnsiColorTextPaneContainer(textPane);
		this.consolePreview = new ConsolePreview();
		this.textPane.addAppendListener(consolePreview);
	}

	public ScrollableAnsiColorTextPaneContainer getConsoleScroller() {
		return consoleScroller;
	}

	public AnsiColorTextPane getTextPane() {
		return textPane;
	}

	public ConsolePreview getConsolePreview() {
		return consolePreview;
	}

}
