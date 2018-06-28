package com.github.schuettec.multiprocman.console;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Stack;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class WordSearch {

	private static final int DEFAULT_MIN_SEARCH_LENGTH = 2;

	protected JTextComponent comp;

	protected IndexAwareHighlighter painter;

	private Integer lastOccurence = null;

	private Stack<Integer> occurences = new Stack<>();

	private String lastWord;

	public WordSearch(JTextComponent comp) {
		this.comp = comp;
		this.painter = new IndexAwareHighlighter(Color.CYAN.darker()
		    .darker(), Color.CYAN);
	}

	public void clear() {
		removeAll(comp);
	}

	public void gotoNextOccurrence() {
		if (nonNull(lastOccurence)) {
			lastOccurence++;
			if (lastOccurence > occurences.size() - 1) {
				lastOccurence = 0;
			}
			scrollToOccurrence();
		}
	}

	public void gotoPrevOccurrence() {
		if (nonNull(lastOccurence)) {
			lastOccurence--;
			if (lastOccurence < 0) {
				lastOccurence = occurences.size() - 1;
			}
			scrollToOccurrence();
		}
	}

	private void scrollToOccurrence() {
		try {
			if (occurences.contains(lastOccurence)) {
				Integer index = occurences.get(lastOccurence);
				painter.setCurrentIndex(index);
				Rectangle rect = comp.modelToView(index);
				comp.scrollRectToVisible(rect);
				comp.moveCaretPosition(index);
				comp.repaint();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Searches a word in the underlying text component and highlight the
	 * occurrences.
	 *
	 * @param word
	 *        The word to search for.
	 * @return Returns the index of the last occurrence.
	 */
	public int search(String word) {
		if (word.length() > DEFAULT_MIN_SEARCH_LENGTH) {
			int firstOffset = -1;
			Highlighter highlighter = comp.getHighlighter();

			// Remove any existing highlights for last word
			removeAll(comp);

			if (word == null || word.equals("")) {
				return -1;
			}

			// Look for the word we are given - insensitive search
			String content = null;
			try {
				Document d = comp.getDocument();
				content = d.getText(0, d.getLength())
				    .toLowerCase();
			} catch (BadLocationException e) {
				// Cannot happen
				return -1;
			}

			word = word.toLowerCase();
			int lastIndex = 0;
			int wordSize = word.length();

			while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
				int endIndex = lastIndex + wordSize;
				try {
					this.lastWord = word;
					occurences.add(lastIndex);
					highlighter.addHighlight(lastIndex, endIndex, painter);
				} catch (BadLocationException e) {
					// Nothing to do
				}
				if (firstOffset == -1) {
					firstOffset = lastIndex;
				}
				lastIndex = endIndex;
			}
			if (!occurences.isEmpty()) {
				if (isNull(lastOccurence) || lastOccurence >= occurences.size()) {
					this.lastOccurence = occurences.size() - 1;
				}
				scrollToOccurrence();
			}

			return lastIndex;
		} else {
			return -1;
		}
	}

	private void removeAll(JTextComponent component) {
		this.occurences.clear();
		Highlighter highlighter = comp.getHighlighter();
		Highlighter.Highlight[] highlights = highlighter.getHighlights();
		for (int i = 0; i < highlights.length; i++) {
			Highlighter.Highlight h = highlights[i];
			highlighter.removeHighlight(h);
		}
	}

}