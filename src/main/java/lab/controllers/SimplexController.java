package lab.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;
import lab.helpers.Fraction;
import lab.helpers.Helper;
import lab.models.Conditions;
import lab.models.SimplexTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimplexController {
    @FXML
    public Button resetBtn;
    @FXML
    private ListView<String> basisListView;
    @FXML
    private TableView<Map> simplexTable;
    @FXML
    private TextArea answerTextArea;
    @FXML
    private VBox answerPane;
    @FXML
    private Pane basisPane;
    @FXML
    private Button rollBackBtn;
    @FXML
    private Button nextBtn;
    @FXML
    private Button quickAnswerBtn;

    private ObservableList<String> basisData = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> simplexItems;
    // Главная таблица
    private SimplexTable table;
    // Список ведущих элементов
    private List<Pair<Integer, Integer>> pivotList = new ArrayList<>();

    @FXML
    private void initialize() {
        table = new SimplexTable();
        Tooltip t = new Tooltip("Получить следующую итерацию симплекс таблицы");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(nextBtn, t);

        t = new Tooltip("Мгновенно получить ответ, без промежуточных симплекс таблиц");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(quickAnswerBtn, t);

        t = new Tooltip("Стереть промежуточные симплекс таблицы");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(resetBtn, t);

        t = new Tooltip("Можно вводить целые числа от 1 до (кол-ва огранчений)");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(basisListView, t);

        setBasisListView();
        setBasisList();
    }

    @FXML
    public void nextTable(ActionEvent actionEvent) {
        onRollBack();
        if (table.getIteration() < 0) {
            offBasisPane();
            table.init(
                    Conditions.getTarget(),
                    Conditions.getRestrict(),
                    Conditions.getVarCount(),
                    Conditions.getRestrictCount(),
                    Conditions.getBaseList()
            );
        } else {
            offBasisPane();
            if (isPivotSelected()) {
                table = table.generate(getSelectedPivot());
                table.iterate();
            } else {
                Helper.message(Alert.AlertType.ERROR,
                        "Ошибка опорного элемента",
                        "",
                        "Вы не выделили опорный элемент, обязательно выберите его (в зелёной рамке или в жёлтой)," +
                                " чтобы продолжить решение");
            }
        }
        if (table.isSolved()) {
            displayAnswer();
        } else {
            pivotList = table.findPivots();
        }
        showSimplexTable(table.getIteration());
    }

    @FXML
    private void prevTable(ActionEvent event) {
        int iteration = table.getIteration();
        if (iteration == 0) {
            reset();
            offRollBack();
            onBasisPane();
        } else {
            rollBack();
            offBasisPane();
        }
    }

    @FXML
    private void quickAnswer(ActionEvent event) {
        onRollBack();
        while (!table.isSolved()) {
            if (table.getIteration() < 0) {
                table.init(
                        Conditions.getTarget(),
                        Conditions.getRestrict(),
                        Conditions.getVarCount(),
                        Conditions.getRestrictCount(),
                        Conditions.getBaseList()
                );
            } else {
                table = table.generate(pivotList.get(0));
                table.iterate();
            }
            pivotList = table.findPivots();
        }
        if (table.isSolved()) {
            displayAnswer();
        }
        showSimplexTable(table.getIteration());
    }

    @FXML
    private void resetTable(ActionEvent event) {
        reset();
    }

    public void reset() {
        setBasisListView();
        offAnswer();
        offRollBack();
        simplexItems = null;
        table = null;
        pivotList = new ArrayList<>();

        simplexTable.getColumns().clear();
        simplexTable.getSelectionModel().setCellSelectionEnabled(true);
        simplexTable.getItems().clear();

        basisListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            if (event.getCode() == KeyCode.ENTER) {
//                  event.consume(); // don't consume the event or else the values won't be updated;
                return;
            }

            // switch to edit mode on keypress, but only if we aren't already in edit mode
            if (basisListView.getEditingIndex() < 0) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    int focusedCellPosition = basisListView.getFocusModel().getFocusedIndex();
                    basisListView.edit(focusedCellPosition);
                }
            }

        });

        basisListView.addEventFilter(KeyEvent.KEY_RELEASED, event -> {

            if (event.getCode() == KeyCode.ENTER) {


                // move focus & selection
                // we need to clear the current selection first or else the selection would be added to the current selection since we are in multi selection mode
                int pos = basisListView.getFocusModel().getFocusedIndex();

                if (pos == -1) {
                    basisListView.getSelectionModel().select(0);
                }
//                    // add new row when we are at the last row
//                    else if (pos.getRow() == targetTable.getItems().size() -1) {
//                        addRow();
//                    }
                // select next row, but same column as the current selection
                else if (pos < basisListView.getItems().size() - 1) {
                    basisListView.getSelectionModel().clearAndSelect(pos + 1);
                }

            }

        });

        initialize();
    }

    public void setBasisListView() {
        basisListView.setEditable(true);
        basisListView.setOnEditCommit(t -> {
            String val = t.getNewValue();
            int defaultNum = IntStream.rangeClosed(1, Conditions.getVarCount()).boxed()
                    .filter(rangeNum -> {
                        List<String> list = basisData.stream().collect(Collectors.toList());
                        list.remove(t.getIndex());
                        return !list.contains(String.valueOf(rangeNum));
                    }).findAny().get();
            if (val.matches("1?[0-9]")) {
                int num = Integer.parseInt(val);
                if (num >= 1 && num <= Conditions.getVarCount() && !basisData.contains(val)) {
                    basisListView.getItems().set(t.getIndex(), t.getNewValue());
                } else {
                    basisListView.getItems().set(t.getIndex(), String.valueOf(defaultNum));
                }
            } else {
                basisListView.getItems().set(t.getIndex(), String.valueOf(defaultNum));
            }
            setBasisList();
        });

        basisListView.setCellFactory(TextFieldListCell.forListView());

        // switch to edit mode on keypress
        // this must be KeyEvent.KEY_PRESSED so that the key gets forwarded to the editing cell; it wouldn't be forwarded on KEY_RELEASED

        basisData.clear();
        basisData = FXCollections.observableArrayList();
        for (int i = 0; i < Conditions.getRestrictCount(); i++) {
            basisData.add(String.valueOf(i + 1));
        }
        basisListView.setItems(basisData);
    }

    private void setBasisList() {
        List<Integer> basisListInt = basisListView.getItems().stream().map(Integer::parseInt).collect(Collectors.toList());
        Conditions.setBaseList(basisListInt);
    }

    private void showSimplexTable(int iteration) {
        {
            simplexTable.getColumns().clear();
            simplexTable.getSelectionModel().setCellSelectionEnabled(true);
            simplexTable.getItems().clear();
            // Header
            TableColumn<Map, String> simplexColumn = new TableColumn<>("X^" + iteration);
            simplexColumn.setSortable(false);
            simplexColumn.setCellValueFactory(new MapValueFactory<>("F"));
            simplexTable.getColumns().add(simplexColumn);

            simplexColumn.getStyleClass().add("first-column");
            simplexColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("F", e.getNewValue()));


        }

        List<Integer> freeList = table.getFreeList();
        freeList.add(0);
        for (int i : freeList) {
            if (i != 0) {
                TableColumn<Map, Pair<String, Fraction>> simplexColumn = new TableColumn<>("X" + i);
                simplexColumn.setSortable(false);
                simplexColumn.setCellValueFactory(new MapValueFactory<>("X" + i));
                simplexTable.getColumns().add(simplexColumn);
//                simplexColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                simplexColumn.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Pair<String, Fraction> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Fraction pivotRel = getPivotValue(i);
                            setText(item.getKey());
                            if (pivotRel.equals(item.getValue()) && !table.isSolved()
                                    && this.getIndex() != table.getBasisList().size() &&
                                    table.get(0, i).compareTo(Fraction.ZERO) < 0
                            ) {
                                setTextFill(Color.GREEN); // or use setStyle(String)
                                setStyle("-fx-border-color: #00ff00;");
                            } else {
                                setTextFill(Color.BLACK); // or use setStyle(String)
                            }
                            if (pivotRel.equals(item.getValue()) && !table.isSolved()
                                    && Helper.stringToFraction(item.getKey()).compareTo(Fraction.ZERO) < 0
                            ) {
                                setTextFill(Color.ORANGE); // or use setStyle(String)
                                setStyle("-fx-border-color: #FFA500;");
                            }
                        }
                    }
                });
                simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + i, e.getNewValue()));
            }
        }

        TableColumn<Map, String> simplexColumn = new TableColumn<>("C");
        simplexColumn.setSortable(false);
        simplexColumn.setCellValueFactory(new MapValueFactory<>("C"));
        simplexTable.getColumns().add(simplexColumn);
        simplexColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C", e.getNewValue()));
        simplexItems = FXCollections.observableArrayList();
        List<Integer> basisList = table.getBasisList();
        basisList.add(0);


        for (int i : basisList) {
            Map<String, Object> simplexItem = new HashMap<>();
            String rowName = i == 0 ? " " : "X" + i;
            simplexItem.put("F", rowName);

            for (int j : freeList) {
                String colName = j == 0 ? "C" : "X" + j;
                if (colName.equals("C")) {
                    simplexItem.put(colName, table.get(i, j).toString());
                } else {
                    Fraction rel = null;
                    if (table.get(i, j).compareTo(Fraction.ZERO) != 0) {
                        rel = table.get(i, 0).divide(table.get(i, j));
                    }

                    simplexItem.put(colName, new Pair(table.get(i, j).toString(), rel));
                }
            }
            simplexItems.add(simplexItem);
        }

        simplexTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        simplexTable.getItems().setAll(simplexItems);
    }

    public Fraction getPivotValue(int col) {
        Fraction pivotRel = Fraction.ZERO;
        pivotList = table.findPivots();
        for (Pair<Integer, Integer> pair : pivotList) {
            if (pair.getValue() == col) {
                Fraction val = table.get(pair.getKey(), pair.getValue());
                pivotRel = table.get(pair.getKey(), 0).divide(val);
            }
        }
        return pivotRel;
    }

    private void displayAnswer() {
        onAnswer();
        Fraction[] answer = table.getAnswer();
        StringBuilder line = new StringBuilder();
        if (answer == null) {
            line.append("НЕТ РЕШЕНИЯ");
            answerTextArea.setStyle("-fx-border-color: #ff0000; -fx-font-size: 16;");
        } else {
            line = new StringBuilder("X(");
            for (int i = 1; i < answer.length; i++) {
                line.append(answer[i].toString()).append(", ");
            }
            line.deleteCharAt(line.length() - 2);
            line.append(")\n");
            line.append("F = ");
            if (Conditions.isMin()) {
                line.append(answer[0]);
            } else {
                line.append(answer[0].multiply(-1));
            }
            answerTextArea.setStyle("-fx-border-color: #00ff00; -fx-font-size: 16;");
        }
        answerTextArea.setText(line.toString());
    }

    private boolean isPivotSelected() {
        ObservableList<TablePosition> selectedCells = simplexTable.getSelectionModel().getSelectedCells();
        if (selectedCells.size() > 0) {
            int selectedCol = selectedCells.get(0).getColumn() - 1;
            int selectedRow = selectedCells.get(0).getRow();
            if (selectedRow >= table.getBasisList().size() ||
                    selectedCol >= table.getFreeList().size()) {
                return false;
            }
            int col = table.getFreeList().get(
                    selectedCol
            );
            int row = table.getBasisList().get(
                    selectedRow
            );
            return pivotList.contains(new Pair(row, col));
        }
        return false;
    }

    private Pair<Integer, Integer> getSelectedPivot() {
        ObservableList<TablePosition> selectedCells = simplexTable.getSelectionModel().getSelectedCells();
        if (selectedCells.size() > 0) {
            int col = table.getFreeList().get(
                    selectedCells.get(0).getColumn() - 1
            );
            int row = table.getBasisList().get(
                    selectedCells.get(0).getRow()
            );
            return new Pair(row, col);
        }
        return null;
    }

    private void rollBack() {
        offAnswer();
        onRollBack();

        table = table.getPrev();
        pivotList = table.findPivots();
        showSimplexTable(table.getIteration());
    }

    private void onRollBack() {
        rollBackBtn.setVisible(true);
    }

    private void offRollBack() {
        rollBackBtn.setVisible(false);
    }

    private void offAnswer() {
        basisPane.setVisible(true);
        answerPane.setVisible(false);
        nextBtn.setVisible(true);
        quickAnswerBtn.setVisible(true);
    }

    private void onAnswer() {
        basisPane.setVisible(false);
        answerPane.setVisible(true);
        nextBtn.setVisible(false);
        quickAnswerBtn.setVisible(false);
    }

    private void offBasisPane() {
        basisPane.setVisible(false);
    }

    private void onBasisPane() {
        basisPane.setVisible(true);
    }
}
