package de.schuette.procman.console;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

public class IndexAwareHighlighter extends DefaultHighlightPainter {

	private Color currentIndexColor;
	private Integer currentIndex;

	public IndexAwareHighlighter(Color occurencesColor, Color currentIndexColor) {
		super(occurencesColor);
		this.currentIndexColor = currentIndexColor;
	}

	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
	}

	@Override
	public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
		Rectangle alloc = bounds.getBounds();
		try {
			// --- determine locations ---
			TextUI mapper = c.getUI();
			Rectangle p0 = mapper.modelToView(c, offs0);
			Rectangle p1 = mapper.modelToView(c, offs1);

			// --- render ---
			Color color = null;
			if (offs0 >= currentIndex) {
				color = currentIndexColor;
			} else {
				color = getColor();
			}

			if (color == null) {
				g.setColor(c.getSelectionColor());
			} else {
				g.setColor(color);
			}
			if (p0.y == p1.y) {
				// same line, render a rectangle
				Rectangle r = p0.union(p1);
				g.fillRect(r.x, r.y, r.width, r.height);
			} else {
				// different lines
				int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
				g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
				if ((p0.y + p0.height) != p1.y) {
					g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y - (p0.y + p0.height));
				}
				g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
			}
		} catch (BadLocationException e) {
			// can't render
		}
	}

	@Override
	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {

		Color color = null;
		if (offs0 == currentIndex) {
			color = currentIndexColor;
		} else {
			color = getColor();
		}

		if (color == null) {
			g.setColor(c.getSelectionColor());
		} else {
			g.setColor(color);
		}

		Rectangle r;

		if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
			// Contained in view, can just use bounds.
			if (bounds instanceof Rectangle) {
				r = (Rectangle) bounds;
			} else {
				r = bounds.getBounds();
			}
		} else {
			// Should only render part of View.
			try {
				// --- determine locations ---
				Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
				r = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
			} catch (BadLocationException e) {
				// can't render
				r = null;
			}
		}

		if (r != null) {
			// If we are asked to highlight, we should draw something even
			// if the model-to-view projection is of zero width (6340106).
			r.width = Math.max(r.width, 1);

			g.fillRect(r.x, r.y, r.width, r.height);
		}

		return r;

	}

}
