package lab.models;

import javafx.util.Pair;
import lab.helpers.GaussObject;
import lab.helpers.Helper;
import lab.helpers.Fraction;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SimplexTable {
    private Map<Integer, Map<Integer, Fraction>> table = new HashMap<>();
    private Fraction[] answer = null;
    private boolean solved = false;
    private int iteration = -1;
    private Pair<Integer, Integer> pivot;
    private SimplexTable prev;
    private boolean noSolution = false;
    private Fraction[] target;
    private int varCount;
    private List<Integer> basisList = new ArrayList<>();
    private List<Integer> freeList = new ArrayList<>();
    private int restrictCount;

    public SimplexTable() {
    }

    public SimplexTable(int iteration,
                        Pair<Integer, Integer> pivot,
                        SimplexTable prev) {
        setIteration(iteration);
        setPivot(pivot);
        setPrev(prev);
    }

    public List<Integer> getBasisList() {
        return new ArrayList<>(basisList);
    }

    public List<Integer> getFreeList() {
        return new ArrayList<>(freeList);
    }

    public Fraction get(int basis, int free) {
        if (!getTable().containsKey(basis) ||
                !getTable().get(basis).containsKey(free)) {
            return null;
        }
        return getTable().get(basis).get(free);
    }

    public Map<Integer, Map<Integer, Fraction>> getTable() {
        return table;
    }

    public void setTable(Map<Integer, Map<Integer, Fraction>> table) {
        this.table = table;
    }

    public void init(
            Fraction[] target,
            Fraction[][] restrict,
            int varCount,
            int restrictCount,
            List<Integer> basisList
    ) {
        setTarget(target);
        setRestrict(restrict);
        setVarCount(varCount);
        setRestrictCount(restrictCount);

        GaussObject gaussObject = Helper.gauss(
                basisList,
                restrict
        );
        Fraction[][] oneMatr = gaussObject.getMatr();
        List<Integer> swapedCols = gaussObject.getSwapedCols();
        setRestrict(mainRestrict(oneMatr, swapedCols));
        setRestrictCount(gaussObject.getMatr().length);
        setBasisList(swapedCols.stream().limit(oneMatr.length).collect(Collectors.toList()));
        setFreeList(swapedCols.stream().filter(num -> !getBasisList().contains(num))
                .collect(Collectors.toList()));


        Fraction[] targetFunc = func(oneMatr, swapedCols);
        setTarget(targetFunc);

        for (int i = 0; i < oneMatr.length; i++) {
            Iterator<Integer> freeIterator = getFreeList().iterator();
            for (int j = 0; j < oneMatr[i].length; j++) {
                int rowNum = getBasisList().get(i);
                if (j == 0) {
                    add(rowNum, 0, oneMatr[i][0]);
                } else {
                    if (freeIterator.hasNext()) {
                        int colNum = freeIterator.next();
                        int swapIdx = 0;
                        for (int swapCol : swapedCols) {
                            if (swapCol == colNum) {
                                break;
                            }
                            swapIdx++;
                        }
                        add(rowNum, colNum, oneMatr[i][swapIdx + 1]);
                    }
                }
            }
        }

        for (int i = 0; i < targetFunc.length; i++) {
            if (i == 0) {
                add(0, 0, targetFunc[0].multiply(-1));
            } else {
//                if (!targetFunc[i].equals(Fraction.ZERO)) {
//                    add(0, i, targetFunc[i]);
//                }
                add(0, i, targetFunc[i]);

            }
        }

        if (checkSolved()) {
            setAnswer();
            setSolved();
        }
        incIteration();
    }

    private Fraction[][] mainRestrict(Fraction[][] matr, List<Integer> swapedCols) {
        Fraction[][] restrict = new Fraction[matr.length][];
        List<Integer> swapedList = new ArrayList<>(swapedCols);
        for (int i = 0; i < restrict.length; i++) {
            restrict[i] = new Fraction[matr[i].length];
        }
        for (int i = 0; i < matr.length; i++) {
            System.arraycopy(matr[i], 0, restrict[i], 0, matr[i].length);
        }
        for (int colIdx = 0; colIdx < swapedList.size(); colIdx++) {
            int col = swapedList.get(colIdx);
            int swapedCol = colIdx + 1;
            int swapedIdx = 0;
            for (int i = 0; i < swapedList.size(); i++) {
                if (swapedList.get(i) == swapedCol) {
                    swapedIdx = i + 1;
                    break;
                }
            }

            if (colIdx + 1 != col) {
                Helper.swapMatrCol(restrict, swapedCol, swapedIdx);

                swapedList.set(colIdx, swapedCol);
                swapedList.set(swapedIdx - 1, col);
            }
        }
        return restrict;
    }

    private boolean checkSolved() {
        Map<Integer, Fraction> colMap = getTable().get(0);
        boolean hasSolution = colMap.entrySet().stream().filter(entry -> entry.getKey() != 0)
                .map(Map.Entry::getValue)
                .allMatch(fr -> fr.compareTo(Fraction.ZERO) >= 0);

        boolean hasNegativeB = hasNegativeB();
        if (hasNoSolutionNegativeB()) {
            noSolution = true;
            return true;
        }
        if (hasNegativeB) {
            noSolution = false;
            return false;
        }


        AtomicBoolean noSolution = new AtomicBoolean(false);
        colMap.entrySet().stream().filter(entry -> entry.getKey() != 0)
                .forEach(entry -> {
                    Fraction func = entry.getValue();
                    int col = entry.getKey();
                    if (func.compareTo(Fraction.ZERO) < 0) {
                        boolean no = getTable().entrySet()
                                .stream()
                                .allMatch(rowEntry -> rowEntry.getValue().get(col).compareTo(Fraction.ZERO) <= 0);
                        if (no) {
                            noSolution.set(true);
                        }
                    }
                });

        this.noSolution = noSolution.get();

        return (hasSolution || noSolution.get());
    }

    private boolean hasNoSolutionNegativeB() {
        if (!hasNegativeB()) {
            return false;
        }
        int minRow = getBasisList().get(0);
        Fraction minValue = get(minRow, 0);
        for (int i : getBasisList()) {
            Fraction val = get(i, 0);
            if (val.compareTo(Fraction.ZERO) < 0 &&
                    val.compareTo(minValue) <= 0
            ) {
                minRow = i;
                minValue = val;
            }
        }

        boolean hasNegativeCol = false;
        for (int i : getFreeList()) {
            Fraction val = get(minRow, i);
            if (val.compareTo(Fraction.ZERO) < 0) {
                hasNegativeCol = true;
            }
        }
        return !hasNegativeCol;
    }

    private boolean hasNegativeB() {
        return getBasisList().stream().anyMatch(basisNum -> get(basisNum, 0).compareTo(Fraction.ZERO) < 0);
    }

    private void add(int row, int col, Fraction val) {
        if (!table.containsKey(row)) {
            table.put(row, new HashMap<>());
        }
        Map<Integer, Fraction> hashRow = table.get(row);
        hashRow.put(col, val);
    }

    private Fraction[] func(Fraction[][] oneMatr, List<Integer> swapedCols) {
        Fraction[] targetFunc = getTarget();
        Fraction[] resultFunc = new Fraction[targetFunc.length];
        Fraction[] row = new Fraction[targetFunc.length];

        for (int i = 0; i < resultFunc.length; i++) {
            resultFunc[i] = Fraction.ZERO;
            row[i] = Fraction.ZERO;
        }

        for (int k = 0; k < oneMatr.length; k++) {
            int swapIdx = 0;
            row[0] = oneMatr[k][0];
            for (int i = 1; i < oneMatr[k].length; i++) {
                if (i > getBasisList().size()) {
                    row[swapedCols.get(swapIdx)] = oneMatr[k][i].multiply(-1);
                } else {
                    row[i] = Fraction.ZERO;
                }
                swapIdx++;
            }
            swapIdx = k;


            Fraction val = targetFunc[swapedCols.get(swapIdx)];
            for (int j = 0; j < row.length; j++) {
                row[j] = row[j].multiply(val);
            }

            for (int j = 0; j < resultFunc.length; j++) {
                resultFunc[j] = resultFunc[j].add(row[j]);
            }

        }

        for (int i = 1; i < resultFunc.length; i++) {
            resultFunc[i] = resultFunc[i].add(
                    getBasisList().contains(i) ? Fraction.ZERO : targetFunc[i]
            );
        }
        resultFunc[0] = resultFunc[0].add(targetFunc[0]);

        return resultFunc;
    }

    public List<Pair<Integer, Integer>> findPivots() {

        List<Pair<Integer, Integer>> list = new ArrayList<>();
        List<Integer> cols = new ArrayList<>();
        Map<Integer, Fraction> targetFunc = table.get(0);

        targetFunc.forEach((key, val) -> {
            if (val.compareTo(Fraction.ZERO) < 0 && key != 0) {
                cols.add(key);
            }
        });


        for (Integer col : cols) {
            Fraction minRel = null;
            for (Integer row : getBasisList()) {
                Fraction val = table.get(row).get(col);
                if (val.compareTo(Fraction.ZERO) <= 0) {
                    continue;
                }
                Fraction rel = table.get(row).get(0).divide(val);
                if (minRel == null || rel.compareTo(minRel) <= 0) {
                    minRel = rel;
                }

            }

            for (Integer row : getBasisList()) {
                if (!table.get(row).get(col).equals(Fraction.ZERO)) {
                    Fraction rel = table.get(row).get(0).divide(table.get(row).get(col));
                    if (rel.equals(minRel)) {
                        list.add(new Pair<>(row, col));
                    }
                }
            }

        }

        list.removeIf(pair -> pair.getValue() == 0 || pair.getKey() == 0);

        if (hasNegativeB()) {
            int minRow = getBasisList().get(0);
            Fraction minValue = get(getBasisList().get(0), 0);
            for (int i : getBasisList()) {
                Fraction val = get(i, 0);
                if (val.compareTo(Fraction.ZERO) < 0 &&
                        val.compareTo(minValue) <= 0
                ) {
                    minRow = i;
                    minValue = val;
                }
            }

            boolean hasNegativeCol = false;
            int negativeCol = 0;
            Fraction minCol = get(minRow, getFreeList().get(0));
            for (int i : getFreeList()) {
                Fraction val = get(minRow, i);
                if (val.compareTo(Fraction.ZERO) < 0 && val.compareTo(minCol) <= 0) {
                    hasNegativeCol = true;
                    negativeCol = i;
                }
            }
            if (hasNegativeCol) {
                list.clear();
                list.add(
                        new Pair<>(minRow, negativeCol)
                );
            } else {
                list.clear();
                noSolution = true;
                setAnswer();
            }
        }
        return list;
    }

    public SimplexTable generate(Pair<Integer, Integer> selectedPivot) {
        SimplexTable generated = new SimplexTable(iteration + 1, selectedPivot, this);
        generated.setBasisList(getBasisList());
        generated.setFreeList(getFreeList());
        generated.setTarget(getTarget());
        return generated;
    }

    public SimplexTable generate() {
        SimplexTable generated = new SimplexTable();
        generated.setPrev(this);
        generated.setIteration(-1);
        return generated;
    }

    public void iterate() {
        if (isSolved()) {
            setAnswer();
            return;
        }

        swapRowColPivot();
        setPivotCell();
        setPivotRow();
        setPivotCol();
        setRemain();

        if (checkSolved()) {
            setAnswer();
            setSolved();
        }
    }

    private void swapRowColPivot() {
        int row = pivot.getKey();
        int col = pivot.getValue();

        List<Integer> basisList = getBasisList();
        List<Integer> freeList = getFreeList();

        basisList.removeIf(val -> val == row || val == 0);
        freeList.removeIf(val -> val == col || val == 0);

        basisList.add(col);
        freeList.add(row);
        setBasisList(basisList);
        setFreeList(freeList);
    }

    private void setRemain() {
        for (Integer row : getTable().keySet()) {
            for (Integer col : getTable().get(pivot.getValue()).keySet()) {
                if (!row.equals(pivot.getValue()) && !col.equals(pivot.getKey())) {
                    Fraction val = getPrev().get(row, col).subtract(
                            get(pivot.getValue(), col).multiply(
                                    getPrev().get(row, pivot.getValue())
                            )
                    );
                    add(row, col, val);
                }
            }
        }
    }

    private void setPivotCol() {
        Fraction newPivotValue = get(pivot.getValue(), pivot.getKey());

        for (Integer row : getPrev().getTable().keySet()) {
            if (row.equals(pivot.getKey())) {
                continue;
            }
            Fraction val = newPivotValue.multiply(
                    prev.get(row, pivot.getValue())
            ).multiply(-1);
            add(row, pivot.getKey(), val);
        }
    }

    private void setPivotRow() {
        Fraction newPivotValue = get(pivot.getValue(), pivot.getKey());
        for (Integer col : getPrev().getTable().get(pivot.getKey()).keySet()) {
            if (col.equals(pivot.getValue())) {
                continue;
            }
            Fraction val = newPivotValue.multiply(
                    prev.get(pivot.getKey(), col)
            );
            add(pivot.getValue(), col, val);
        }
    }

    private void setPivotCell() {
        Fraction pivotValue = getPrev().get(pivot.getKey(), pivot.getValue());
        add(pivot.getValue(), pivot.getKey(), Fraction.ONE.divide(pivotValue));
    }

    private void setAnswer() {
        if (noSolution) {
            answer = null;
            return;
        }
        answer = new Fraction[getBasisList().size() + getFreeList().size() + 1];
        for (int i = 1; i < answer.length; i++) {
            answer[i] = getTable().getOrDefault(i, Map.of(0, Fraction.ZERO)).get(0);
        }
        answer[0] = getTargetValue(answer);
    }

    private Fraction getTargetValue(Fraction[] answer) {
        Fraction[] target = getTarget();
        Fraction value = Fraction.ZERO;
        for (int i = 1; i < target.length; i++) {
            value = value.add(
                    target[i].multiply(answer[i])
            );
        }
        value = value.add(target[0]);
        return value;
    }

    public Fraction[] getAnswer() {
        return answer;
    }

    private void setSolved() {
        this.solved = true;
    }

    public boolean isSolved() {
        return solved;
    }

    public Fraction[] getTarget() {
        return target;
    }

    public void setTarget(Fraction[] target) {
        this.target = target;
    }

    public void setRestrict(Fraction[][] restrict) {
    }

    public int getVarCount() {
        return varCount;
    }

    public void setVarCount(int varCount) {
        this.varCount = varCount;
    }

    public int getRestrictCount() {
        return restrictCount;
    }

    public void setRestrictCount(int restrictCount) {
        this.restrictCount = restrictCount;
    }

    public void setBasisList(List<Integer> basisList) {
        basisList.removeIf(val -> val == 0);
        Collections.sort(basisList);
        this.basisList = basisList;
    }

    public void setFreeList(List<Integer> freeList) {
        freeList.removeIf(val -> val == 0);
        Collections.sort(freeList);
        this.freeList = freeList;
    }

    private void setPivot(Pair<Integer, Integer> pivot) {
        this.pivot = pivot;
    }

    private Pair<Integer, Integer> getPivot() {
        return pivot;
    }

    public SimplexTable getPrev() {
        return prev;
    }

    public void setPrev(SimplexTable prev) {
        this.prev = prev;
    }

    private void incIteration() {
        iteration++;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }
}
