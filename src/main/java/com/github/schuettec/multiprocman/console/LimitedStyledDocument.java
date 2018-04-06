package com.github.schuettec.multiprocman.console;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;

public class LimitedStyledDocument extends DefaultStyledDocument {

	private int maximumLines;

	public LimitedStyledDocument(int maximumLines) {
		super();
		this.maximumLines = maximumLines;
	}

	public LimitedStyledDocument(int maximumLines, Content c, StyleContext styles) {
		super(c, styles);
		this.maximumLines = maximumLines;
	}

	public LimitedStyledDocument(int maximumLines, StyleContext styles) {
		super(styles);
		this.maximumLines = maximumLines;
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offs, str, a);
		removeFromStart();
	}

	/*
	 * Remove lines from the start of the Document
	 */
	private void removeFromStart() {
		Element root = getDefaultRootElement();
		int count = root.getElementCount();
		if (count > maximumLines) {
			int diff = count - maximumLines;

			Element element = root.getElement(diff);

			int end = element.getEndOffset();
			try {
				remove(0, end);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Remove lines from the end of the Document
	 */
	private void removeFromEnd(Document document, Element root) {
		// We use start minus 1 to make sure we remove the newline
		// character of the previous line

		Element line = root.getElement(root.getElementCount() - 1);
		int start = line.getStartOffset();
		int end = line.getEndOffset();

		try {
			document.remove(start - 1, end - start);
		} catch (BadLocationException ble) {
		}
	}

}
