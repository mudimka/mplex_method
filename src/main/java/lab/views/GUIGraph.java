package lab.views;

import javafx.scene.chart.XYChart;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class GUIGraph {
    private final XYChart<Double, Double> graph;
    private  XYChart.Series<Double, Double> series;
    private final List<Pair<Double, Double>> listPoints = new ArrayList<>();

    public GUIGraph(final XYChart<Double, Double> graph) {
        this.graph = graph;
        graph.setLegendVisible(true);
    }

    private void plotPoint(final double x, final double y,
                           final XYChart.Series<Double, Double> series) {
        series.getData().add(new XYChart.Data<>(x, y));
    }

    public void clear() {
        graph.getData().clear();
    }

    public void plotLineFromZero(double a, double b, String name) {
        final XYChart.Series<Double, Double> series = new XYChart.Series<>();
        plotPoint(0, 0, series);
        plotPoint(a, b, series);
        series.setName(name);
        graph.getData().add(series);
    }

    public void startPlotLines(String name) {
        series = new XYChart.Series<>();
        series.setName(name);
    }

    public void drawLine(Pair<Double, Double> vert1, Pair<Double, Double> vert2, String name) {
        final XYChart.Series<Double, Double> series = new XYChart.Series<>();
        plotPoint(vert1.getKey(), vert1.getValue(), series);
        plotPoint(vert2.getKey(), vert2.getValue(), series);
        series.setName(name);
        graph.getData().add(series);
    }

    public void addPoint(double a, double b) {
        listPoints.add(new Pair<>(a, b));
        plotPoint(a, b, series);
    }

    public boolean contains(double a, double b) {
        return listPoints.contains(new Pair<>(a, b));
    }
    public void commitPlotLines() {
        graph.getData().add(series);
        listPoints.clear();
    }
}