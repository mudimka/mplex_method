package lab.models;

import javafx.util.Pair;
import lab.helpers.Fraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExtraSimplexTable {
    private Fraction[] target;
    private Fraction[][] restrict;
    private int varCount;
    private int restrictCount;
    private SimplexTable table;
    private boolean extraSolved;
    private boolean solved;
    private Fraction[] extraAnswer;
    private boolean noSolution;
    private SolutionMode mode = SolutionMode.EXTRA;

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
        setBasisList(basisList);
        if (!extraSolved) {
            initConvertedConditions();
            if (checkExtraSolved()) {
                setExtraSolved(true);
            }
        } else {
            table.init(
                    target, restrict, varCount, restrictCount, basisList
            );
            if (checkSolved()) {
                setSolved(true);
            }
        }
    }

    private void initConvertedConditions() {
        Fraction[] eTarget = convertTarget();
        int eVarCount = getVarCount() + getRestrictCount();
        int eRestrictCount = getRestrictCount();
        List<Integer> eBasisList = convertBasisList();
        Fraction[][] eRestrict = convertRestrict();

        table.init(
                eTarget,
                eRestrict,
                eVarCount,
                eRestrictCount,
                eBasisList
        );
    }

    private Fraction[] convertTarget() {
        Fraction[] eTarget = new Fraction[getVarCount() + getRestrictCount() + 1];
        for (int i = 0; i < eTarget.length; i++) {
            if (i >= getVarCount() + 1) {
                eTarget[i] = Fraction.ONE;
            } else {
                eTarget[i] = Fraction.ZERO;
            }
        }
        return eTarget;
    }

    private List<Integer> convertBasisList() {
        List<Integer> list = new ArrayList<>();
        int newVar = getVarCount() + 1;
        for (int i = 0; i < getRestrictCount(); i++) {
            list.add(newVar++);
        }
        return list;
    }

    private Fraction[][] convertRestrict() {
        int newVar = getVarCount() + 1;
        Fraction[][] eRestrict = new Fraction[getRestrictCount()][];
        for (int i = 0; i < eRestrict.length; i++) {
            eRestrict[i] = new Fraction[getVarCount() + getRestrictCount() + 1];
            for (int j = 0; j < eRestrict[i].length; j++) {
                if (j == newVar) {
                    eRestrict[i][j] = Fraction.ONE;
                } else if (j > getVarCount()) {
                    eRestrict[i][j] = Fraction.ZERO;
                } else {
                    eRestrict[i][j] = restrict[i][j];
                }
                if (restrict[i][0].compareTo(Fraction.ZERO) < 0 && j <= getVarCount()) {
                    eRestrict[i][j] = restrict[i][j].multiply(-1);
                }
            }
            newVar++;
        }
        return eRestrict;
    }

    public ExtraSimplexTable getPrev() {
        if (getIteration() == 0 && mode == SolutionMode.SIMPLEX) {
            setMode(SolutionMode.EXTRA);
        }
        if (getIteration() >= 0) {
            extraSolved = isExtraSolved();
        } else {
            extraSolved = false;
        }
        table = table.getPrev();
        boolean newSolved = table.isSolved();
        if (!newSolved && extraSolved && mode == SolutionMode.EXTRA) {
            setExtraSolved(false);
        }
        solved = false;
        return this;
    }

    public void setPrev(SimplexTable prev) {
        table.setPrev(prev);
    }

    public int getIteration() {
        return table.getIteration();
    }

    public void setIteration(int iteration) {
        table.setIteration(iteration);
    }

    public List<Integer> getBasisList() {
       return table.getBasisList();
    }

    public List<Integer> getFreeList() {
        return table.getFreeList();
    }

    public Fraction get(int basis, int free) {
        return table.get(basis, free);
    }

    public void setTable(Map<Integer, Map<Integer, Fraction>> table) {
        this.table.setTable(table);
    }

    public List<Pair<Integer, Integer>> findPivots() {
        return table.findPivots();
    }

    private List<Pair<Integer, Integer>> findForEmptyPivots(int extraRow) {
        List<Pair<Integer, Integer>> list = new ArrayList<>();

        for (Integer col : table.getFreeList()) {
            for (int row : table.getBasisList()) {
                Fraction val = table.get(row, col);
                if (val.compareTo(Fraction.ZERO) != 0 && col > getVarCount()) {
                    continue;
                }
                Fraction rel = table.get(row, 0).divide(val);
                list.add(new Pair<>(row, col));
            }
        }

        list.removeIf(pair -> pair.getValue() == 0 || pair.getKey() == 0);
        return list;
    }

    private boolean isOptimalSolution() {
        Fraction[] deltas = calcDeltas();
        return Arrays.stream(deltas).noneMatch(
                delta -> delta.compareTo(Fraction.ZERO) > 0
        );
    }

    private Fraction[] calcDeltas() {
        Fraction[] deltas = new Fraction[getFreeList().size() + 1];
        for (int i = 0; i < deltas.length; i++) {
            deltas[i] = Fraction.ZERO;
        }
        Fraction[] target = getTarget();
        List<Integer> freeList = getFreeList();
        int idxCol = 0;
        for (int col : getFreeList()) {
            Fraction delta = Fraction.ZERO;
            for (int row : getBasisList()) {
                Fraction c = Fraction.ZERO;
                if (row > getVarCount()) {
                    c = new Fraction(1000_000);
                } else {
                    c = row < target.length ? target[row] : Fraction.ZERO;
                }
                Fraction val = get(row, col);
                delta = delta.add(
                        c.multiply(val)
                );
            }
            Fraction c;
            if (col > getVarCount()) {
                c = new Fraction(1000_000);
            } else {
                c = col < target.length ? target[col] : Fraction.ZERO;
            }
            delta = delta.subtract(
                    c
            );
            deltas[idxCol++] = delta;
        }
        return deltas;
    }

    private boolean isBasisHasExtra() {
        return getBasisList().stream().anyMatch(
                basisNum -> basisNum > getVarCount()
        );
    }


    public ExtraSimplexTable generate(Pair<Integer, Integer> selectedPivot) {
        table = table.generate(selectedPivot);
        return this;
    }

    public void iterate() {
        table.iterate();

        if (checkExtraSolved() && mode == SolutionMode.EXTRA) {
            setExtraAnswer();
            setExtraSolved(true);
            return;
        }
        if (mode == SolutionMode.EXTRA) {
            solved = false;
        }
        if (mode == SolutionMode.SIMPLEX) {
            if (checkSolved()) {
                setSolved(true);
            }
        }
    }

    private void setExtraAnswer() {
        if (noSolution) {
            extraAnswer = null;
            return;
        }
        extraAnswer = new Fraction[getVarCount() + getRestrictCount() + 1];
        extraAnswer[0] = table.get(0, 0).multiply(-1);
        for (int i = 1; i < extraAnswer.length; i++) {
            extraAnswer[i] = getTable().getTable().getOrDefault(i, Map.of(0, Fraction.ZERO)).get(0);
        }
    }

    public Fraction[] getAnswer() {
        if (noSolution) {
            return null;
        }
        if (extraSolved && !isSolved()) {
            return extraAnswer;
        } else {
            return table.getAnswer();
        }
    }


    public ExtraSimplexTable() {
        table = new SimplexTable();
    }

    public SimplexTable getTable() {
        return table;
    }

    public void setTable(SimplexTable table) {
        this.table = table;
    }

    public Fraction[] getTarget() {
        return target;
    }

    public void setTarget(Fraction[] target) {
        this.target = target;
    }

    public Fraction[][] getRestrict() {
        return restrict;
    }

    public void setRestrict(Fraction[][] restrict) {
        this.restrict = restrict;
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
    }

    public boolean checkExtraSolved() {
        int countExtraVar = (int) getBasisList().stream().filter(
                basisNum -> basisNum > getVarCount()
        ).count();
        boolean hasSolution = table.isSolved();
        if (hasSolution) {
            extraAnswer = getAnswer();
            if (extraAnswer == null || extraAnswer[0].compareTo(Fraction.ZERO) > 0) {
                noSolution = true;
            }
        }
        if (isExtraHasNoSolution()) {
            noSolution = true;
        }
        return hasSolution || noSolution;
    }

    private boolean isExtraHasNoSolution() {
        if (isOptimalSolution()) {
            boolean isAllExtraZero = table.getBasisList().stream().filter(
                    basisNum -> basisNum > getVarCount()
            ).allMatch(
                    extraVar -> get(extraVar, 0).equals(Fraction.ZERO)
            );
            if (isAllExtraZero) {
                return false;
            }

            boolean isAnyExtraNotZero = table.getBasisList().stream().filter(
                    basisNum -> basisNum > getVarCount()
            ).anyMatch(
                    extraVar -> !get(extraVar, 0).equals(Fraction.ZERO)
            );

            if (isAnyExtraNotZero) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }


    public boolean checkSolved() {
        setSolved(table.isSolved());
        return isSolved();
    }

    public boolean isExtraSolved() {
        return extraSolved;
    }

    public void setExtraSolved(boolean extraSolved) {
        this.extraSolved = extraSolved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isSolved() {
        return solved;
    }

    public void initSimplex() {
        setMode(SolutionMode.SIMPLEX);
        Fraction[] extraAnswer = getAnswer();
        List<Integer> basisList = new ArrayList<>();
        for (int i = 1; i < extraAnswer.length; i++) {
            if (!extraAnswer[i].equals(Fraction.ZERO)) {
                basisList.add(i);
            }
        }
        table = table.generate();
    }

    public SolutionMode getMode() {
        return mode;
    }

    public void setMode(SolutionMode mode) {
        this.mode = mode;
    }

    public enum SolutionMode {
        EXTRA, SIMPLEX
    }
}
