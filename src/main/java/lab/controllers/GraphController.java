package lab.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import lab.helpers.Fraction;
import lab.helpers.Helper;
import lab.helpers.LineObject;
import lab.helpers.VertexObject;
import lab.models.Conditions;
import lab.models.Graph;
import lab.views.GUIGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GraphController {
    @FXML
    public ListView<String> basisListView;
    @FXML
    public Button quickAnswerBtn;
    @FXML
    public Button resetBtn;
    @FXML
    public NumberAxis lineXAxis;
    @FXML
    public NumberAxis lineYAxis;
    @FXML
    public TextArea answerTextArea;
    @FXML
    private LineChart<Double, Double> lineGraph;
    @FXML
    private LineChart<Double, Double> answerLineGraph;

    // Отображение графиков
    private GUIGraph mathsGraph;
    // Решение графическим методом
    private Graph graph;
    private ObservableList<String> basisData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setBasisListView();
        setUpGraph();
        graph = new Graph();
        mathsGraph = new GUIGraph(lineGraph);

        // Ввод базиса с помощью выделеним и ввода клавиш
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

        Tooltip t = new Tooltip("Решить задачу");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(quickAnswerBtn, t);

        t = new Tooltip("Стереть решение задачи");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(resetBtn, t);

        t = new Tooltip("Можно вводить целые числа от 1 до (кол-ва огранчений)");
        t.setShowDelay(Duration.millis(300));
        Tooltip.install(basisListView, t);
    }

    @FXML
    public void quickAnswer(ActionEvent event) {
        reset(false);
        graph.init(
                Conditions.getTarget(),
                Conditions.getRestrict(),
                Conditions.getVarCount(),
                Conditions.getRestrictCount(),
                Conditions.getBaseList()
        );
        List<Integer> freeList = graph.getFreeList();
        Pair<Fraction, Fraction> antinormal = graph.getAntinormal();
        if (freeList.size() == 1) {
            nameAxis(freeList.get(0), null);
        } else {
            nameAxis(freeList.get(0), freeList.get(1));
        }
        if (graph.isNoSolution()) {
            if (graph.getFreeList().size() > 2) {
                Helper.message(Alert.AlertType.INFORMATION,
                        "Невозможно решить графически",
                        "", "Данную задачу невозможно решить графически, т.к. " +
                                "по итогу кол-во переменных получилось больше 2-х");
            } else {
                drawInfLines();
            }
            displayAnswer();
            return;
        }
        drawAntinormal(antinormal.getKey(), antinormal.getValue());
        List<VertexObject> vertexes = graph.findVertexes();
        List<VertexObject> answerVertexes = graph.getAnswerVertexes();
        if (graph.isNoSolution()) {
            drawInfLines();
            displayAnswer();
            return;
        }
        Pair<Double, Double> scaleValues = getScaleValues(vertexes);
        scaleGraph(scaleValues.getKey(), scaleValues.getValue());
        drawLines(vertexes, "Границы");
        String answerLegend = "";
        if (answerVertexes.size() > 1) {
            answerLegend = String.format("(%s, %s) - (%s, %s)",
                    answerVertexes.get(0).getKey().toString(),
                    answerVertexes.get(0).getValue().toString(),
                    answerVertexes.get(1).getKey().toString(),
                    answerVertexes.get(1).getValue().toString()
            );
        } else if (answerVertexes.size() == 1) {
            answerLegend = String.format("(%s, %s))",
                    answerVertexes.get(0).getKey().toString(),
                    answerVertexes.get(0).getValue().toString()
            );
        }
        drawLines(answerVertexes, "Ответ " + answerLegend);
        displayAnswer();
    }

    @FXML
    public void resetGraph(ActionEvent event) {
        reset(true);
    }

    // Рисуем все прямые
    private void drawInfLines() {
        List<LineObject> infVertexesLines = graph.getInfVertexesLines();
        for (LineObject line : infVertexesLines) {
            VertexObject vertex1 = line.getVertex1();
            VertexObject vertex2 = line.getVertex2();
            String name = line.getEquationString();
            mathsGraph.drawLine(
                    new Pair<>(vertex1.getKey().doubleValue(), vertex1.getValue().doubleValue()),
                    new Pair<>(vertex2.getKey().doubleValue(), vertex2.getValue().doubleValue()),
                    name
            );
        }

    }

    private void displayAnswer() {
        answerTextArea.setVisible(true);
        Fraction[][] answer = graph.getAnswer();
        StringBuilder line = new StringBuilder();
        if (answer == null) {
            line.append("НЕТ РЕШЕНИЯ");
            line.append("\n");
            Fraction[][] restrict = graph.getRestrict();
            for (Fraction[] fractions : restrict) {
                StringBuilder restrictLine = new StringBuilder();
                boolean isNotZero = false;
                for (int j = 1; j < fractions.length; j++) {
                    Fraction val = fractions[j];
                    if (!val.equals(Fraction.ZERO)) {
                        restrictLine.append("(").append(val).append(")x").append(j).append(" + ");
                        isNotZero = true;
                    }
                }
                if (isNotZero) {
                    restrictLine.delete(restrictLine.length() - 3, restrictLine.length() - 1);
                    restrictLine.append(" <= ").append(fractions[0]);
                }

                restrictLine.append("\n");

                line.append(restrictLine);
            }
            answerTextArea.setStyle("-fx-border-color: #ff0000; -fx-font-size: 16;");
        } else {
            line = new StringBuilder();
            for (Fraction[] fractions : answer) {
                line.append("X(");
                for (int i = 1; i < fractions.length; i++) {
                    line.append(fractions[i].toString()).append(", ");
                }
                line.deleteCharAt(line.length() - 2);
                line.append(")\n");
            }
            line.append("F = ");
            if (Conditions.isMin()) {
                line.append(answer[0][0]);
            } else {
                line.append(answer[0][0].multiply(-1));
            }
            line.append("\n");
            Fraction[][] restrict = graph.getRestrict();
            for (Fraction[] fractions : restrict) {
                StringBuilder restrictLine = new StringBuilder();
                boolean isNotZero = false;
                for (int j = 1; j < fractions.length; j++) {
                    Fraction val = fractions[j];
                    if (!val.equals(Fraction.ZERO)) {
                        restrictLine.append("(").append(val.toString()).append(")x").append(j).append(" + ");
                        isNotZero = true;
                    }
                }
                if (isNotZero) {
                    restrictLine.delete(restrictLine.length() - 3, restrictLine.length() - 1);
                    restrictLine.append(" <= ").append(fractions[0]);
                }

                restrictLine.append("\n");
                line.append(restrictLine);
            }
            answerTextArea.setStyle("-fx-border-color: #00ff00; -fx-font-size: 16;");

        }
        answerTextArea.setText(line.toString());
    }

    // Настройка графиков
    private void setUpGraph() {
        lineYAxis.setAutoRanging(false);
        lineXAxis.setAutoRanging(false);
        lineYAxis.setUpperBound(5);
        lineYAxis.setLowerBound(-5);
        lineXAxis.setUpperBound(5);
        lineXAxis.setLowerBound(-5);

        lineGraph.setCreateSymbols(true);
        lineGraph.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

        // Маштабирование
        lineGraph.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 0.95;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }
            lineXAxis.setUpperBound(lineXAxis.getUpperBound() * zoomFactor);
            lineYAxis.setUpperBound(lineYAxis.getUpperBound() * zoomFactor);
            lineXAxis.setLowerBound(lineXAxis.getLowerBound() * zoomFactor);
            lineYAxis.setLowerBound(lineYAxis.getLowerBound() * zoomFactor);
        });
    }

    private void scaleGraph(double xValue, double yValue) {
        lineXAxis.setUpperBound(xValue);
        lineYAxis.setUpperBound(yValue);
    }

    private Pair<Double, Double> getScaleValues(List<VertexObject> answerVertexes) {
        double x = answerVertexes.stream().filter(num -> num.getKey().doubleValue() < 50_000.0)
                .max(
                        Comparator.comparingDouble((a) -> Math.abs(a.getKey().doubleValue()))
                ).get().getKey().doubleValue() + 3;
        double y = answerVertexes.stream().filter(num -> num.getValue().doubleValue() < 50_000.0)
                .max(
                        Comparator.comparingDouble((a) -> Math.abs(a.getValue().doubleValue()))
                ).get().getValue().doubleValue() + 3;
        return new Pair<>(x, y);
    }

    private void drawLines(List<VertexObject> vertexes, String name) {
        VertexObject firstVertex;
        mathsGraph.startPlotLines(name);
        VertexObject vert1 = vertexes.stream().filter(
                vert -> vert.getKey().doubleValue() >= 100_00 || vert.getValue().doubleValue() >= 100_000
        ).findFirst().orElse(vertexes.get(0));
        firstVertex = vert1;
        mathsGraph.addPoint(vert1.getKey().doubleValue(), vert1.getValue().doubleValue());
        for (int i = 0; i < vertexes.size(); i++) {
            for (VertexObject vert2 : vertexes) {
                if (
                        hasSameLine(vert1, vert2) &&
                                !mathsGraph.contains(vert2.getKey().doubleValue(), vert2.getValue().doubleValue()) &&
                                vert1 != vert2
                ) {
                    mathsGraph.addPoint(vert2.getKey().doubleValue(),
                            vert2.getValue().doubleValue());
                    vert1 = vert2;
                }
            }
        }
        if (!(firstVertex.getKey().doubleValue() >= 80_000) && !(firstVertex.getValue().doubleValue() >= 80_000) &&
                !(vert1.getKey().doubleValue() >= 80_000) && !(vert1.getValue().doubleValue() >= 80_000)
        ) {
            mathsGraph.addPoint(firstVertex.getKey().doubleValue(), firstVertex.getValue().doubleValue());
        }
        mathsGraph.commitPlotLines();
    }

    // Проверка - лежат ли вершины на 1-ой прямой
    private boolean hasSameLine(VertexObject vert1, VertexObject vert2) {
        Fraction[][] lines1 = vert1.getLines();
        Fraction[][] lines2 = vert2.getLines();
        for (Fraction[] fractions : lines1) {
            for (Fraction[] value : lines2) {
                if (equalsLine(fractions, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean equalsLine(Fraction[] line1, Fraction[] line2) {
        if (line1.length != line2.length) {
            return false;
        }
        for (int i = 0; i < line1.length; i++) {
            if (!line1[i].equals(line2[i])) {
                return false;
            }
        }
        return true;
    }

    private void drawAntinormal(Fraction a, Fraction b) {
        mathsGraph.plotLineFromZero(a.doubleValue(), b.doubleValue(), "Антинормаль (" + a + ", " + b + ")");
    }

    private void nameAxis(Integer a, Integer b) {
        String strA = a == null ? "" : "X" + a;
        String strB = b == null ? "" : "X" + b;
        lineGraph.getXAxis().setLabel(strA);
        lineGraph.getYAxis().setLabel(strB);
    }

    public void setBasisListView() {
        basisListView.setEditable(true);
        basisListView.setOnEditCommit(t -> {
            String val = t.getNewValue();
            int defaultNum = IntStream.rangeClosed(1, Conditions.getVarCount()).boxed()
                    .filter(rangeNum -> {
                        List<String> list = new ArrayList<>(basisData);
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

    public void reset(boolean isResetBasis) {
        answerTextArea.setVisible(false);
        mathsGraph.clear();
//        basisData = FXCollections.observableArrayList();

        answerTextArea.setText("");
        graph = new Graph();
        mathsGraph = new GUIGraph(lineGraph);
        answerTextArea.setStyle("-fx-border-color: #000; -fx-font-size: 16;");
        setUpGraph();
        if (isResetBasis) {
            setBasisListView();
        }
    }
}
