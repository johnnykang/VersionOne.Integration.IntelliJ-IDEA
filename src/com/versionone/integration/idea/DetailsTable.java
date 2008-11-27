/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.util.ui.Table;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class DetailsTable extends Table {

    private final EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();

    public DetailsTable(DetailsModel v1TableModel) {
        super(v1TableModel);
    }

    @Override
    public DetailsModel getModel() {
        return (DetailsModel) super.getModel();
    }

    public TableCellEditor getCellEditor(final int row, final int col) {
        TableCellEditor editor = getModel().getCellEditor(row, col);
        if (editor == null) {
            editor = super.getCellEditor(row, col);
        }
        return editor;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if (rowIndex != getSelectedRow()) {
            if (getModel().isRowChanged(rowIndex)) {
                c.setBackground(colorsScheme.getColor(ColorKey.find("V1_CHANGED_ROW")));
                c.setForeground(Color.black);
            } else {
                c.setBackground(getBackground());
                c.setForeground(getForeground());
            }
        } else {
            c.setBackground(getSelectionBackground());
            c.setForeground(getSelectionForeground());
        }

        return c;
    }
}
