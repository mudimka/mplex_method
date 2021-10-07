package lab.views;

import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lab.helpers.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditingCell extends TableCell<Map, String> {

    protected TextField textField;

    public EditingCell() {
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            Platform.runLater(() -> {
                textField.requestFocus();
                textField.selectAll();
            });
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem());
        setGraphic(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()
                * 2);


        textField.focusedProperty().addListener(
                (arg0, arg1, arg2) -> {
                    if (!arg2) {
                        commitEdit(textField.getText());
                    }
                });

        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (t.getCode() == KeyCode.TAB) {
                commitEdit(textField.getText());
                TableColumn<Map, ?> nextColumn = getNextColumn(!t.isShiftDown());
                if (nextColumn != null) {
                    getTableView().edit(getTableRow().getIndex(),
                            nextColumn);
                }

            }
        });
    }

    @Override
    public void commitEdit(String s) {
        try {
            Helper.stringToFraction(s);
        } catch (IllegalArgumentException e) {
            s = "0";
        }
        textField.setText(s);
        super.commitEdit(s);
    }

    protected String getString() {
        return getItem() == null ? "" : getItem();
    }

    private TableColumn<Map, ?> getNextColumn(boolean forward) {
        List<TableColumn<Map, ?>> columns = new ArrayList<>();
        for (TableColumn<Map, ?> column : getTableView().getColumns()) {
            columns.addAll(getLeaves(column));
        }
        // There is no other column that supports editing.
        if (columns.size() < 2) {
            return null;
        }
        int nextIndex = columns.indexOf(getTableColumn());
        if (forward) {
            nextIndex++;
            if (nextIndex > columns.size() - 1) {
                nextIndex = 0;
            }
        } else {
            nextIndex--;
            if (nextIndex < 0) {
                nextIndex = columns.size() - 1;
            }
        }
        return columns.get(nextIndex);
    }

    private List<TableColumn<Map, ?>> getLeaves(
            TableColumn<Map, ?> root) {
        List<TableColumn<Map, ?>> columns = new ArrayList<>();
        if (root.getColumns().isEmpty()) {
            // We only want the leaves that are editable.
            if (root.isEditable()) {
                columns.add(root);
            }
            return columns;
        } else {
            for (TableColumn<Map, ?> column : root.getColumns()) {
                columns.addAll(getLeaves(column));
            }
            return columns;
        }
    }
}