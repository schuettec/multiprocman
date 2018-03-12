package com.github.schuettec.multiprocman.console;

import static com.github.schuettec.multiprocman.AnsiColors.getANSIColor;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.lang3.event.EventListenerSupport;

import com.github.schuettec.multiprocman.AppendListener;
import com.github.schuettec.multiprocman.Appendable;
import com.github.schuettec.multiprocman.ExceptionDialog;

public class AnsiColorTextPane extends JTextPane implements Appendable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public enum ExportType {
    TEXT,
    HTML,
    RTF;
  }

  static final Color cReset = Color.getHSBColor(0.000f, 0.000f, 1.000f);

  static Color colorCurrent = cReset;

  private EventListenerSupport<AppendListener> appendListener = new EventListenerSupport<>(AppendListener.class);

  private String remaining = "";

  private StyleContext lastStyleContext;

  public AnsiColorTextPane() {
    super();
    initialize();
  }

  public AnsiColorTextPane(StyledDocument doc) {
    super(doc);
    initialize();
  }

  private void initialize() {
    this.setEditable(false);
  }

  public void addAppendListener(AppendListener listener) {
    appendListener.addListener(listener);
  }

  public void removeAppendListener(AppendListener listener) {
    appendListener.removeListener(listener);
  }

  private StyleContext lastStyleContext() {
    if (isNull(lastStyleContext)) {
      this.lastStyleContext = StyleContext.getDefaultStyleContext();
    }
    return lastStyleContext;
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    SimpleAttributeSet newFontStyle = new SimpleAttributeSet();
    StyleConstants.setFontFamily(newFontStyle, font.getFamily());
    StyleConstants.setFontSize(newFontStyle, font.getSize());
    StyleConstants.setBold(newFontStyle, font.isBold());
    StyleConstants.setItalic(newFontStyle, font.isItalic());
    StyledDocument styledDocument = getStyledDocument();
    if (nonNull(styledDocument)) {
      styledDocument.setCharacterAttributes(0, styledDocument.getLength(), newFontStyle.copyAttributes(), false);
    }
    lastStyleContext().addAttributes(SimpleAttributeSet.EMPTY, newFontStyle);
  }

  @Override
  public void append(Color c, String s) {
    _append(c, s);
    appendListener.fire()
        .append(c, s);
  }

  private void _append(Color c, String s) {
    StyleContext lastStyleContext = lastStyleContext();
    AttributeSet aset = lastStyleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
    int len = getDocument().getLength();
    appendString(s, aset, len);
  }

  private void appendString(String s, AttributeSet aset, int len) {
    try {
      getDocument().insertString(len, s, aset);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void appendANSI(String s) { // convert ANSI color codes first
    int aPos = 0; // current char position in addString
    int aIndex = 0; // index of next Escape sequence
    int mIndex = 0; // index of "m" terminating Escape sequence
    String tmpString = "";
    boolean stillSearching = true; // true until no more Escape sequences
    String addString = remaining + s;
    remaining = "";

    if (addString.length() > 0) {
      aIndex = addString.indexOf("\u001B"); // find first escape
      if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
        _append(colorCurrent, addString);
        appendListener.fire()
            .append(colorCurrent, addString);
        return;
      }
      // otherwise There is an escape character in the string, so we must process it

      if (aIndex > 0) { // Escape is not first char, so send text up to first escape
        tmpString = addString.substring(0, aIndex);
        _append(colorCurrent, tmpString);
        appendListener.fire()
            .append(colorCurrent, tmpString);
        aPos = aIndex;
      }
      // aPos is now at the beginning of the first escape sequence

      stillSearching = true;
      while (stillSearching) {
        mIndex = addString.indexOf("m", aPos); // find the end of the escape sequence
        if (mIndex < 0) { // the buffer ends halfway through the ansi string!
          remaining = addString.substring(aPos, addString.length());
          stillSearching = false;
          continue;
        } else {
          tmpString = addString.substring(aPos, mIndex + 1);
          colorCurrent = getANSIColor(tmpString, cReset);
        }
        aPos = mIndex + 1;
        // now we have the color, send text that is in that color (up to next escape)

        aIndex = addString.indexOf("\u001B", aPos);

        if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
          tmpString = addString.substring(aPos, addString.length());
          _append(colorCurrent, tmpString);
          appendListener.fire()
              .append(colorCurrent, tmpString);
          stillSearching = false;
          continue; // jump out of loop early, as the whole string has been sent now
        }

        // there is another escape sequence, so send part of the string and prepare for
        // the next
        tmpString = addString.substring(aPos, aIndex);
        aPos = aIndex;
        _append(colorCurrent, tmpString);
        appendListener.fire()
            .append(colorCurrent, tmpString);
      } // while there's text in the input buffer
    }
  }

  public static String getHTMLColor(Color color) {
    if (color == null) {
      return "#000000";
    }
    return "#" + Integer.toHexString(color.getRGB())
        .substring(2)
        .toUpperCase();
  }

  public void saveAs(File file, ExportType exportType) {
    EditorKit editorKit = null;
    switch (exportType) {
      case HTML:
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        editorKit = htmlEditorKit;
        break;
      case RTF:
        editorKit = new RTFEditorKit();
        break;
      case TEXT:
        editorKit = new DefaultEditorKit();
        break;
    }
    try (FileOutputStream fout = new FileOutputStream(file)) {
      StyledDocument styledDocument = getStyledDocument();
      editorKit.write(fout, styledDocument, 0, styledDocument.getLength());
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(this, "The file was not found or could not be created.", "File not found",
          JOptionPane.ERROR_MESSAGE);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "The document was not saved due to an I/O error.", "I/O error",
          JOptionPane.ERROR_MESSAGE);
    } catch (BadLocationException e) {
      ExceptionDialog.showException(e, "Bad location while saving the document.");
    }
  }

}