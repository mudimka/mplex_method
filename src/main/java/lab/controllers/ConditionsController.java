package lab.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import lab.helpers.Fraction;
import lab.helpers.Helper;
import lab.helpers.Observer;
import lab.models.Conditions;
import lab.views.EditingCell;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static lab.helpers.Helper.stringToFraction;

public class ConditionsController {
    final int MIN_FIELD_VALUE = 1;
    final int MAX_FIELD_VALUE = 16;
    final int INITIAL_FIELD_VALUE = 2;

    @FXML
    public Button applyBtn;
    @FXML
    private Spinner<Integer> varCount;
    @FXML
    private Spinner<Integer> restrictCount;
    @FXML
    private ComboBox<String> targetCombo;
    @FXML
    private ComboBox<String> fractionCombo;
    @FXML
    private TableView targetTable;
    @FXML
    private TableView restrictTable;

    private ObservableList<Map<String, Object>> targetItems;
    private ObservableList<Map<String, Object>> restrictItems;
    private final List<Observer> observerList = new ArrayList<>();

    @FXML
    private void initialize() {
        inputOnlyInteger(varCount, MAX_FIELD_VALUE);
        inputOnlyInteger(restrictCount, MAX_FIELD_VALUE);
        rebuildConditionTables(INITIAL_FIELD_VALUE, INITIAL_FIELD_VALUE);

        Tooltip t = new Tooltip("Целевая функция вида: a1*X1 + a2*X2 + ... + Const\n" +
                "Можно вводить: целые, дробные числа. \n" +
                "Пример: 1, 1/2, 0.65");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(targetTable, t);

        t = new Tooltip("Уравнения ограничений имеют вид: a1*X1 + a2*X2 + ... = b\n" +
                "Можно вводить: целые, дробные числа. \n" +
                "Пример: 1, 1/2, 0.65");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(restrictTable, t);

        t = new Tooltip("Можно вводить от 1 до 16 и число переменных >= число ограничений");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(restrictCount, t);
        Tooltip.install(varCount, t);

        t = new Tooltip("Вывод будет отображаться таким типом дробей. \nПример десятичной дроби 0.1 или 0,15.\n" +
                "Пример обыкновенной дроби -1/5");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(fractionCombo, t);

        t = new Tooltip("Целевая функция -> min, Целевая функция -> max");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(targetCombo, t);

        t = new Tooltip("Применить введённые значения условий");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(applyBtn, t);



        varCount.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue < restrictCount.getValue()) {
                restrictCount.getValueFactory().setValue(newValue);
//                Helper.message(Alert.AlertType.INFORMATION,
//                        "Ошибка ввода числа переменных",
//                        "",
//                        "Переменных не должно быть меньше, чем ограничений");
                System.out.println("var count error");

            } else {
                if (newValue <= MAX_FIELD_VALUE && newValue >= MIN_FIELD_VALUE) {
                    rebuildConditionTables(newValue, restrictCount.getValue());
                }
            }
        });

        // Динамическая связка кол-ва ограничений и таблицы для ввода значений
        restrictCount.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue > varCount.getValue()) {
                varCount.getValueFactory().setValue(newValue);
//                Helper.message(Alert.AlertType.INFORMATION,
//                        "Ошибка ввода числа переменных",
//                        "",
//                        "Переменных не должно быть меньше, чем ограничений");
                System.out.println("restrict count error");
            } else {
                if (newValue >= MIN_FIELD_VALUE && newValue <= MAX_FIELD_VALUE) {
                    rebuildConditionTables(varCount.getValue(), newValue);
                }
            }
        });

        readConditions(null);

        targetItems.addListener((ListChangeListener<Map<String, Object>>) c -> {
            System.out.println("Changed on " + c);
            targetTable.setItems(targetItems);
            if (c.next()) {
                System.out.println(c.getFrom());
            }

        });
    }

    // Считывание данных и заполнение условий
    @FXML
    public void readConditions(ActionEvent actionEvent) {
        Fraction[] target = readTarget();
        Conditions.setTarget(target);

        Fraction[][] restrict = readRestrict();
        Conditions.setRestrict(restrict);

        int vCount = varCount.getValue();
        Conditions.setVarCount(vCount);

        int rCount = restrictCount.getValue();
        Conditions.setRestrictCount(rCount);

        boolean isRational = fractionCombo.getValue().equals("Обыкновенный");
        Conditions.setRational(isRational);

        boolean isMin = targetCombo.getValue().equals("Минимум");
        Conditions.setMin(isMin);

        notifyObservers("conditionsApply");
        System.out.println(Conditions.verbose());
    }

    // Построение спинеров
    private void inputOnlyInteger(Spinner spinner, int max) {
        // get a localized format for parsing
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
//            rebuildConditionTables(varCount.getValue(), restrictCount.getValue());
//            readConditions(null);
            return c;
        };
        TextFormatter<Integer> priceFormatter = new TextFormatter<>(
                new IntegerStringConverter(), 2, filter);

        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, max, 2));
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(priceFormatter);
    }

    // Перестройка таблиц в зависимости от кол-ва ограничей и переменных
    private void rebuildConditionTables(int vCount, int rCount) {
        // Target table
        targetTable.setEditable(true);
        targetTable.getSelectionModel().setCellSelectionEnabled(true);
        targetTable.getColumns().clear();
        targetTable.getItems().clear();

        // Ввод с помощью выделения и ввода клавиш
        targetTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                  event.consume(); // don't consume the event or else the values won't be updated;
                return;
            }
            // switch to edit mode on keypress, but only if we aren't already in edit mode
            if (targetTable.getEditingCell() == null) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    TablePosition focusedCellPosition = targetTable.getFocusModel().getFocusedCell();
                    targetTable.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());
                }
            }
        });
        targetTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {

                // move focus & selection
                // we need to clear the current selection first or else the selection would be added to the current selection since we are in multi selection mode
                TablePosition pos = targetTable.getFocusModel().getFocusedCell();

                if (pos.getRow() == -1) {
                    targetTable.getSelectionModel().select(0);
                }
//                    // add new row when we are at the last row
//                    else if (pos.getRow() == targetTable.getItems().size() -1) {
//                        addRow();
//                    }
                // select next row, but same column as the current selection
                else if (pos.getRow() < targetTable.getItems().size() - 1) {
                    targetTable.getSelectionModel().clearAndSelect(pos.getRow() + 1, pos.getTableColumn());
                }

            }
        });

        // Своя версия таблицы
        Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactory = p -> new EditingCell();

        // Заголовок
        for (int i = 1; i <= vCount; i++) {
            TableColumn<Map, String> column = new TableColumn<>("X" + i);
            column.setSortable(false);
            column.setCellValueFactory(new MapValueFactory<>("X" + i));
            targetTable.getColumns().add(column);
            column.setCellFactory(cellFactory);
            int finalI = i;
            column.setOnEditCommit(e -> {
                e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + finalI, e.getNewValue());
//                readConditions(null);
            });
        }
        TableColumn<Map, String> column = new TableColumn<>("Const");
        column.setSortable(false);
        column.setCellValueFactory(new MapValueFactory<>("C0"));
        targetTable.getColumns().add(column);
        column.setCellFactory(cellFactory);
        column.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C0", e.getNewValue());
//            readConditions(null);
        });

        // Заполнение
        targetItems = FXCollections.observableArrayList();
        Map<String, Object> item1 = new HashMap<>();
        for (int i = 0; i <= vCount; i++) {
            String colName = i == 0 ? "C" : "X";
            item1.put(colName + i, "0");
        }
        targetItems.add(item1);

        targetTable.getItems().addAll(targetItems);


        // Таблица ограничений
        restrictTable.setEditable(true);
        restrictTable.getColumns().clear();
        restrictTable.getSelectionModel().setCellSelectionEnabled(true);
        restrictTable.getItems().clear();

        // Ввод с помощью выделения и ввода клавиш
        restrictTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                  event.consume(); // don't consume the event or else the values won't be updated;
                return;
            }
            // switch to edit mode on keypress, but only if we aren't already in edit mode
            if (restrictTable.getEditingCell() == null) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    TablePosition focusedCellPosition = restrictTable.getFocusModel().getFocusedCell();
                    restrictTable.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());

                }
            }
        });
        restrictTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // move focus & selection
                // we need to clear the current selection first or else the selection would be added to the current selection since we are in multi selection mode
                TablePosition pos = restrictTable.getFocusModel().getFocusedCell();

                if (pos.getRow() == -1) {
                    restrictTable.getSelectionModel().select(0);
                }
//                    // add new row when we are at the last row
//                    else if (pos.getRow() == targetTable.getItems().size() -1) {
//                        addRow();
//                    }
                // select next row, but same column as the current selection
                else if (pos.getRow() < restrictTable.getItems().size() - 1) {
                    restrictTable.getSelectionModel().clearAndSelect(pos.getRow() + 1, pos.getTableColumn());
                }
            }

        });

        // Заголовок
        for (int i = 1; i <= vCount; i++) {
            TableColumn<Map, String> restrictColumn = new TableColumn<>("X" + i);
            restrictColumn.setSortable(false);
            restrictColumn.setCellValueFactory(new MapValueFactory<>("X" + i));
            restrictTable.getColumns().add(restrictColumn);
            restrictColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            restrictColumn.setCellFactory(cellFactory);
            int finalI = i;
            restrictColumn.setOnEditCommit(e -> {
                e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + finalI, e.getNewValue());
//                readConditions(null);
            });
        }
        TableColumn<Map, String> restrictColumn = new TableColumn<>("b");
        restrictColumn.setSortable(false);
        restrictColumn.setCellValueFactory(new MapValueFactory<>("C0"));
        restrictTable.getColumns().add(restrictColumn);
        restrictColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        restrictColumn.setCellFactory(cellFactory);
        restrictColumn.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C0", e.getNewValue());
//            readConditions(null);
        });

        // Заполнение
        restrictItems = FXCollections.<Map<String, Object>>observableArrayList();
        for (int row = 0; row < rCount; row++) {
            Map<String, Object> restrictItem = new HashMap<>();
            for (int i = 0; i <= vCount; i++) {
                String colName = i == 0 ? "C" : "X";
                restrictItem.put(colName + i, "0");
            }
            restrictItems.add(restrictItem);
        }
        restrictTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        restrictTable.getItems().addAll(restrictItems);
    }

    // Из условий записать данные на экран
    public void setConditions() {
        varCount.getValueFactory().setValue(Conditions.getVarCount());
        restrictCount.getValueFactory().setValue(Conditions.getRestrictCount());

        Fraction[] target = Conditions.getSourceTarget();
        ObservableList<Map<String, Object>> targetItems = FXCollections.observableArrayList();
        Map<String, Object> item1 = new HashMap<>();
        for (int i = 0; i <= Conditions.getVarCount(); i++) {
            String colName = i == 0 ? "C" : "X";
            item1.put(colName + i, target[i].toString());
        }
        targetItems.add(item1);
        this.targetItems.setAll(targetItems);
        targetTable.setItems(targetItems);

        targetCombo.setValue(Conditions.isMin() ? "Минимум" : "Максимум");
        fractionCombo.setValue(Conditions.isRational() ? "Обыкновенный" : "Десятичный");

        Fraction[][] restrict = Conditions.getRestrict();
        restrictItems.clear();
        for (int row = 0; row < Conditions.getRestrictCount(); row++) {
            Map<String, Object> restrictItem = new HashMap<>();
            for (int i = 0; i <= Conditions.getVarCount(); i++) {
                String colName = i == 0 ? "C" : "X";
                restrictItem.put(colName + i, restrict[row][i].toString());
            }
            restrictItems.add(restrictItem);
        }
        restrictTable.setItems(restrictItems);
    }

    // Считать данные из целевой функции таблицы
    private Fraction[] readTarget() {
        return itemsToFraction(targetItems)[0];
    }

    // Считать данные из таблицы ограничей
    private Fraction[][] readRestrict() {
        return itemsToFraction(restrictItems);
    }

    // Считать табличные данные в массив
    private Fraction[][] itemsToFraction(ObservableList<Map<String, Object>> items) {
        Fraction[][] fractions = new Fraction[items.size()][];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = new Fraction[items.get(i).size()];
        }

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            for (int j = 0; j < item.size(); j++) {
                String nameCol = j == 0 ? "C" : "X";
                String value = (String) items.get(i).get(nameCol + j);
                Fraction fraction = Fraction.ZERO;
                try {
                    fraction = stringToFraction(value);
                } catch (IllegalArgumentException e) {
                    Helper.message(Alert.AlertType.ERROR,
                            "Несоответствие формата ввода",
                            "",
                            "Могут быть лишь введены числа, десятичные и обыкновенные дроби (пример 1.5 или 3/2)"
                    );
                    Map<String, Object> remove = items.remove(i);
                    remove.put(nameCol + j, "0");
                    items.add(remove);
                }
                fractions[i][j] = fraction;
            }
        }

        return fractions;
    }

    private void notifyObservers(String type) {
        for (Observer observer : observerList) {
            observer.onConditionChanged(type);
        }
    }

    public void addObserver(MainController observer) {
        observerList.add(observer);
    }
}
