package de.schuette.procman;

import de.schuette.procman.console.AnsiColorTextPane;
import de.schuette.procman.console.ScrollableAnsiColorTextPaneContainer;
import de.schuette.procman.consolepreview.ConsolePreview;

public class ProcessController {

	private ScrollableAnsiColorTextPaneContainer consoleScroller;
	private AnsiColorTextPane textPane;
	private ConsolePreview consolePreview;

	public ProcessController(AnsiColorTextPane textPane) {
		this.textPane = textPane;
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
