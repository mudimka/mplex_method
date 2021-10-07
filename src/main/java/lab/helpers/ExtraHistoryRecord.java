package lab.helpers;

import javafx.collections.ObservableList;
import javafx.util.Pair;
import lab.models.ExtraSimplexTable;

import java.util.List;
import java.util.Map;

public class ExtraHistoryRecord {
    private ExtraSimplexTable table;
    private boolean rollBackBtn;
    private boolean extraPane;
    private boolean simplexPane;
    private boolean nextExtraBtn;
    private boolean quickExtraAnswerBtn;
    private boolean nextSimplexBtn;
    private boolean quickSimplexAnswerBtn;
    private List<Pair<Integer, Integer>> pivotList;
    private List<Integer> eBasisList;
    private ObservableList<Map<String, Object>> simplexItems;
    private boolean showSimplex = false;
    private boolean answerPane;
    private String answerTextAreaText;
    private String answerTextAreaStyle;

    public ExtraHistoryRecord() {
    }

    public ExtraHistoryRecord(ExtraHistoryRecord other) {
        this.table = other.table;
        this.rollBackBtn = other.rollBackBtn;
        this.extraPane = other.extraPane;
        this.simplexPane = other.simplexPane;
        this.nextExtraBtn = other.nextExtraBtn;
        this.quickExtraAnswerBtn = other.quickExtraAnswerBtn;
        this.nextSimplexBtn = other.nextSimplexBtn;
        this.quickSimplexAnswerBtn = other.quickSimplexAnswerBtn;
        this.pivotList = other.pivotList;
        this.eBasisList = other.eBasisList;
        this.simplexItems = other.simplexItems;
        this.showSimplex = other.showSimplex;
        this.answerPane = other.answerPane;
        this.answerTextAreaText = other.answerTextAreaText;
        this.answerTextAreaStyle = other.answerTextAreaStyle;
    }

    public ExtraSimplexTable getTable() {
        return table;
    }

    public void setTable(ExtraSimplexTable table) {
        this.table = table;
    }

    public boolean isRollBackBtn() {
        return rollBackBtn;
    }

    public boolean isExtraPane() {
        return extraPane;
    }

    public void setExtraPane(boolean extraPane) {
        this.extraPane = extraPane;
    }

    public void setRollBackBtn(boolean rollBackBtn) {
        this.rollBackBtn = rollBackBtn;
    }

    public boolean isNextExtraBtn() {
        return nextExtraBtn;
    }

    public void setNextExtraBtn(boolean nextExtraBtn) {
        this.nextExtraBtn = nextExtraBtn;
    }

    public boolean isQuickExtraAnswerBtn() {
        return quickExtraAnswerBtn;
    }

    public void setQuickExtraAnswerBtn(boolean quickExtraAnswerBtn) {
        this.quickExtraAnswerBtn = quickExtraAnswerBtn;
    }

    public boolean isNextSimplexBtn() {
        return nextSimplexBtn;
    }

    public void setNextSimplexBtn(boolean nextSimplexBtn) {
        this.nextSimplexBtn = nextSimplexBtn;
    }

    public boolean isQuickSimplexAnswerBtn() {
        return quickSimplexAnswerBtn;
    }

    public void setQuickSimplexAnswerBtn(boolean quickSimplexAnswerBtn) {
        this.quickSimplexAnswerBtn = quickSimplexAnswerBtn;
    }

    public boolean isSimplexPane() {
        return simplexPane;
    }

    public void setSimplexPane(boolean simplexPane) {
        this.simplexPane = simplexPane;
    }

    public List<Pair<Integer, Integer>> getPivotList() {
        return pivotList;
    }

    public void setPivotList(List<Pair<Integer, Integer>> pivotList) {
        this.pivotList = pivotList;
    }

    public List<Integer> geteBasisList() {
        return eBasisList;
    }

    public void seteBasisList(List<Integer> eBasisList) {
        this.eBasisList = eBasisList;
    }

    public ObservableList<Map<String, Object>> getSimplexItems() {
        return simplexItems;
    }

    public void setSimplexItems(ObservableList<Map<String, Object>> simplexItems) {
        this.simplexItems = simplexItems;
    }

    public boolean isShowSimplex() {
        return showSimplex;
    }

    public void setShowSimplex(boolean showSimplex) {
        this.showSimplex = showSimplex;
    }


    public boolean isAnswerPane() {
        return answerPane;
    }

    public void setAnswerPane(boolean answerPane) {
        this.answerPane = answerPane;
    }

    public String getAnswerTextAreaText() {
        return answerTextAreaText;
    }

    public void setAnswerTextAreaText(String answerTextAreaText) {
        this.answerTextAreaText = answerTextAreaText;
    }

    public String getAnswerTextAreaStyle() {
        return answerTextAreaStyle;
    }

    public void setAnswerTextAreaStyle(String answerTextAreaStyle) {
        this.answerTextAreaStyle = answerTextAreaStyle;
    }
}
