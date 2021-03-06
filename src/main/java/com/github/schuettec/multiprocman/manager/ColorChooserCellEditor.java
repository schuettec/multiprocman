package com.github.schuettec.multiprocman.manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class ColorChooserCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JButton delegate = new JButton();

    Color savedColor;

    public ColorChooserCellEditor() {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Color color = JColorChooser.showDialog(delegate, "Color Chooser", savedColor);
                ColorChooserCellEditor.this.changeColor(color);
            }
        };
        delegate.addActionListener(actionListener);
    }

    @Override
    public Object getCellEditorValue() {
        return savedColor;
    }

    private void changeColor(Color color) {
        if (color != null) {
            savedColor = color;
            delegate.setBackground(color);
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        changeColor((Color) value);
        return delegate;
    }
}
