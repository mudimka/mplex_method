package lab.models;

import javafx.util.Pair;
import lab.helpers.*;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    private boolean solved = false;
    private boolean noSolution = false;
    private Fraction[] target;
    private Fraction[][] restrict;
    private int varCount;
    private List<Integer> basisList = new ArrayList<>();
    private List<Integer> freeList = new ArrayList<>();
    private int restrictCount;
    private Map<Integer, Fraction[]> equations = new HashMap<>();

    public Graph() {
    }

    public void init(
            Fraction[] target,
            Fraction[][] restrict,
            int varCount,
            int restrictCount,
            List<Integer> basisList
    ) {
        setTarget(target);
        setBasisList(basisList);
        setVarCount(varCount);
        setRestrictCount(restrictCount);
        setRestrict(restrict);
        GaussObject gaussObject = Helper.gauss(
                basisList,
                restrict
        );
        int countBasisVar = countBasisVar(gaussObject.getMatr());
        if (countBasisVar != basisList.size()) {
            gaussObject = recalcBasis(gaussObject.getMatr());
        }
        Fraction[][] oneMatr = gaussObject.getMatr();
        List<Integer> swapedCols = gaussObject.getSwapedCols();
        setRestrict(mainRestrict(oneMatr, swapedCols));
        setRestrictCount(gaussObject.getMatr().length);
        setBasisList(swapedCols.stream().limit(oneMatr.length).collect(Collectors.toList()));
        setFreeList(swapedCols.stream().filter(num -> !getBasisList().contains(num))
                .collect(Collectors.toList()));

        Fraction[] targetFunc = func(oneMatr, swapedCols);
        setTarget(targetFunc);

        setRestrict(
                convertRestrict(getRestrict())
        );
        setRestrictCount(getRestrict().length);
        if (isNoSolutionRestrict()) {
            noSolution = true;
        }
        if (freeList.size() > 2) {
            noSolution = true;
        }
        setSolved(true);
    }

    private GaussObject recalcBasis(Fraction[][] matr) {
        List<Integer> newBasisList = new ArrayList<>();

        for (int i = 0; i < matr.length; i++) {
            Fraction[] row = matr[i];
            for (int j = 1; j < row.length; j++) {
                Fraction val = row[j];
                if (i + 1 == j && !val.equals(Fraction.ZERO)) {
                    newBasisList.add(i + 1);
                }
            }
        }

        return Helper.gauss(
                newBasisList,
                getRestrict()
        );
    }

    private int countBasisVar(Fraction[][] matr) {
        int count = 0;
        for (int i = 0; i < matr.length; i++) {
            Fraction[] row = matr[i];
            for (int j = 1; j < row.length; j++) {
                Fraction val = row[j];
                if (i + 1 == j && !val.equals(Fraction.ZERO)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isNoSolutionRestrict() {
        Fraction[][] restrict = getRestrict();
        return Arrays.stream(restrict).filter(
                row -> {
                    for (int i = 1; i < row.length; i++) {
                        if (!row[i].equals(Fraction.ZERO)) {
                            return false;
                        }
                    }
                    return true;
                }
        ).anyMatch(
                row -> row[0].compareTo(Fraction.ZERO) < 0
        );
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

        // Приводим в нашему стандартному виду x1 + x2 + x3 ...
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

    private Fraction[][] convertRestrict(Fraction[][] matr) {
        int countEq = countEquation(restrict);
        Fraction[][] restrict = new Fraction[matr.length + getFreeList().size()][];

        for (int k = 0; k < matr.length; k++) {
            Fraction[] row = new Fraction[matr[0].length];
            Arrays.fill(row, Fraction.ZERO);

            row[0] = matr[k][0];
            for (int i = 1; i < matr[k].length; i++) {
                if (!getBasisList().contains(i)) {
                    row[i] = matr[k][i];
                }
            }
            restrict[k] = row;
        }

        // Добавление неравенств x >= 0 y >= 0
        int freeIdx = 0;
        countEq = 0;
        for (int k = matr.length; k < matr.length + getFreeList().size(); k++) {
            Fraction[] row = new Fraction[matr.length + getFreeList().size() + 1];
            row[0] = Fraction.ZERO;
            Arrays.fill(row, Fraction.ZERO);
            int col = getFreeList().get(freeIdx++);
            row[col] = Fraction.MINUS_ONE;

            restrict[k] = row;
        }

        // Убираем неравенства вида 0 <= 2
        for (Fraction[] fractions : restrict) {
            boolean isEq = true;
            for (int i = 1; i < fractions.length; i++) {
                if (!fractions[i].equals(Fraction.ZERO)) {
                    isEq = false;
                    break;
                }
            }
            if (isEq && fractions[0].compareTo(Fraction.ZERO) > 0) {
                countEq++;
            }
        }

        Fraction[][] cleanedRestrict = new Fraction[restrict.length - countEq][];
        int cleanedIdx = 0;
        for (Fraction[] fractions : restrict) {
            boolean isEq = true;
            for (int j = 1; j < fractions.length; j++) {
                if (!fractions[j].equals(Fraction.ZERO)) {
                    isEq = false;
                    break;
                }
            }
            if (isEq && fractions[0].compareTo(Fraction.ZERO) >= 0) {
                continue;
            }
            cleanedRestrict[cleanedIdx] = new Fraction[restrict[0].length];
            System.arraycopy(fractions, 0, cleanedRestrict[cleanedIdx], 0, fractions.length);
            cleanedIdx++;
        }


        return cleanedRestrict;
    }

    private int countEquation(Fraction[][] oneMatr) {
        int countEq = 0;
        for (int i = 0; i < oneMatr.length; i++) {
            Fraction[] row = oneMatr[0];
            boolean isEq = true;
            for (int j = 1; j < row.length; j++) {
                if (!row[j].equals(Fraction.ZERO) && j != i + 1) {
                    isEq = false;
                    break;
                }
            }
            if (isEq && !row[0].equals(Fraction.ZERO) && !row[i + 1].equals(Fraction.ZERO)) {
                countEq++;
            }
        }
        return countEq;
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

            Fraction[] eq = new Fraction[row.length];
            System.arraycopy(row, 0, eq, 0, row.length);
            addEquation(swapedCols.get(k), eq);

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

    public Pair<Fraction, Fraction> getAntinormal() {
        Fraction[] target = getTarget();
        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }

        Fraction key = target[freeList.get(0)].multiply(-1);
        Fraction value = target[freeList.get(1)].multiply(-1);
        Fraction abs = new Fraction(
                Math.abs(Math.sqrt(key.multiply(key).add(
                        value.multiply(value)).doubleValue()))
        );
//        key = key.divide(abs);
//        value = value.divide(abs);
        return new Pair<>(key, value);
    }

    public List<VertexObject> findVertexes() {
        List<VertexObject> vertexes = new ArrayList<>();
        Fraction[][] restrict = getRestrict();
        for (int i = 0; i < restrict.length; i++) {
            Fraction[] restrict1 = getRestrictRow(i);
            for (int j = i + 1; j < restrict.length; j++) {
                Fraction[] restrict2 = getRestrictRow(j);
                if (restrict1 == restrict2) {
                    continue;
                }
                Fraction[][] intersection = {
                        restrict1,
                        restrict2
                };
                GaussObject gauss = Helper.gauss(getFreeList(), intersection);
                if (gauss.getType() != GaussObject.Solution.ONE) {
                    continue;
                }
                Fraction[][] answMatr = gauss.getMatr();
                Pair<Fraction, Fraction> vertex = new Pair<>(answMatr[0][0], answMatr[1][0]);
                if (isAllVertex(vertex)) {
                    VertexObject vertexObject = new VertexObject(
                            answMatr[0][0],
                            answMatr[1][0],
                            new Fraction[][]{restrict1, restrict2}
                    );
                    if (vertexes.stream().filter(
                            obj -> obj.getKey().equals(vertexObject.getKey()) && obj.getValue().equals(vertexObject.getValue()))
                            .count() <= 0
                    ) {
                        vertexes.add(vertexObject);
                    } else {
                        vertexes.stream().filter(
                                obj -> obj.getKey().equals(vertexObject.getKey()) && obj.getValue().equals(vertexObject.getValue()))
                                .findFirst().get().addLines(vertexObject.getLines());
                    }

                    List<Integer> freeList = getFreeList();
                    if (getBasisList().size() > 0) {
                        freeList.add(getBasisList().get(0));
                    }

                    Fraction key = new Fraction(100_000);
                    Fraction value = valueFromKeyRestrict(key, restrict1);

                    if (restrict1[freeList.get(1)].equals(Fraction.ZERO) && restrict1[0].equals(Fraction.ZERO)
                    ) {
                        key = Fraction.ZERO;
                        value = new Fraction(100_000);
                    }

                    if (restrict1[freeList.get(0)].equals(Fraction.ZERO) && restrict1[0].equals(Fraction.ZERO)
                    ) {
                        key = new Fraction(100_000);
                        value = Fraction.ZERO;
                    }
                    VertexObject infVertex = new VertexObject(key, value, new Fraction[][]{restrict1});

                    Fraction finalKey1 = key;
                    Fraction finalValue1 = value;
                    if (isAllVertex(new Pair<>(key, value))) {
                        if (vertexes.stream().filter(
                                obj -> obj.getKey().equals(finalKey1) && obj.getValue().equals(finalValue1))
                                .count() <= 0) {
                            vertexes.add(infVertex);
                        } else {
                            vertexes.stream().filter(
                                    obj -> obj.getKey().equals(finalKey1) && obj.getValue().equals(finalValue1))
                                    .findFirst().get().addLines(infVertex.getLines());
                        }
                    }

                    key = new Fraction(100_000);
                    value = valueFromKeyRestrict(key, restrict2);

                    if (restrict2[freeList.get(1)].equals(Fraction.ZERO) && restrict2[0].equals(Fraction.ZERO)
                    ) {
                        key = Fraction.ZERO;
                        value = new Fraction(100_000);
                    }

                    if (restrict2[freeList.get(0)].equals(Fraction.ZERO) && restrict2[0].equals(Fraction.ZERO)
                    ) {
                        key = new Fraction(100_000);
                        value = Fraction.ZERO;
                    }

                    infVertex = new VertexObject(key, value, new Fraction[][]{restrict2});

                    Fraction finalKey = key;
                    Fraction finalValue = value;
                    if (isAllVertex(new Pair<>(key, value))) {
                        if (
                                vertexes.stream().filter(
                                        obj -> obj.getKey().equals(finalKey) && obj.getValue().equals(finalValue))
                                        .count() <= 0) {
                            vertexes.add(infVertex);
                        } else {
                            vertexes.stream().filter(
                                    obj -> obj.getKey().equals(finalKey) && obj.getValue().equals(finalValue))
                                    .findFirst().get().addLines(infVertex.getLines());
                        }

                    }
                }
            }

            Fraction[] restrict2;
            if (getFreeList().size() == 1) {
                restrict1 = new Fraction[]{
                        restrict1[0], restrict1[getFreeList().get(0)], Fraction.ZERO
                };
                restrict2 = new Fraction[]{
                        Fraction.ZERO, Fraction.ZERO, Fraction.MINUS_ONE
                };
                Fraction[][] intersection = new Fraction[][]{
                        restrict1,
                        restrict2
                };
                GaussObject gauss = Helper.gauss(List.of(1, 2), intersection);
                if (gauss.getType() != GaussObject.Solution.ONE) {
                    continue;
                }
                Fraction[][] answMatr = gauss.getMatr();
                Pair<Fraction, Fraction> vertex = new Pair<>(answMatr[0][0], answMatr[1][0]);
                if (isAllVertex(vertex)) {
                    VertexObject vertexObject = new VertexObject(
                            answMatr[0][0],
                            answMatr[1][0],
                            new Fraction[][]{restrict1, restrict2}
                    );
                    if (vertexes.stream().filter(
                            obj -> obj.getKey().equals(vertexObject.getKey()) && obj.getValue().equals(vertexObject.getValue()))
                            .count() <= 0
                    ) {
                        vertexes.add(vertexObject);
                    } else {
                        vertexes.stream().filter(
                                obj -> obj.getKey().equals(vertexObject.getKey()) && obj.getValue().equals(vertexObject.getValue()))
                                .findFirst().get().addLines(vertexObject.getLines());

                    }
                }
            }
        }

        return vertexes;
    }

    private boolean isAllVertex(Pair<Fraction, Fraction> vertex) {
        Fraction[][] restrict = getRestrict();
        for (int i = 0; i < restrict.length; i++) {
            Fraction[] restrictRow = restrict[i];
            if (!isOkVertex(vertex, restrictRow)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOkVertex(Pair<Fraction, Fraction> vertex, Fraction[] restrictRow) {
        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }
        int prevIdx = freeList.get(0);
        int lastIdx = freeList.get(1);
        return restrictRow[prevIdx].multiply(vertex.getKey()).add(
                restrictRow[lastIdx].multiply(vertex.getValue())
        ).compareTo(restrictRow[0]) <= 0;
    }

    private Fraction[] getRestrictRow(int row) {
        Fraction[][] restrict = getRestrict();
        Fraction[] restrictRow = new Fraction[restrict[row].length];
        System.arraycopy(restrict[row], 0, restrictRow, 0, restrict[row].length);
        return restrictRow;
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

    public List<Integer> getBasisList() {
        return new ArrayList<>(basisList);
    }

    public void setBasisList(List<Integer> basisList) {
        this.basisList = basisList;
        Collections.sort(basisList);
    }

    public List<Integer> getFreeList() {
        return new ArrayList<>(freeList);
    }

    public void setFreeList(List<Integer> freeList) {
        this.freeList = freeList;
        Collections.sort(freeList);
    }

    public List<VertexObject> getAnswerVertexes() {
        List<VertexObject> minVertexes = new ArrayList<>();
        List<VertexObject> vertexes = findVertexes();
        if (vertexes.size() == 0) {
            noSolution = true;
            return new ArrayList<>();
        }
        VertexObject min = vertexes.get(0);
        Fraction minValue = getTargetValue(min.getKey(), min.getValue());
        for (VertexObject vertexObject : vertexes) {
            Fraction targetValue = getTargetValue(vertexObject.getKey(), vertexObject.getValue());
            if (targetValue.compareTo(minValue) <= 0 && vertexObject.getKey().doubleValue() < 80_000.0 &&
                    vertexObject.getValue().doubleValue() < 80_00.0) {
                minValue = targetValue;
            }
        }

        for (VertexObject vertexObject : vertexes) {
            Fraction targetValue = getTargetValue(vertexObject.getKey(), vertexObject.getValue());
            if (targetValue.equals(minValue) &&
                    minVertexes.stream().filter(
                            obj -> obj.getKey().equals(vertexObject.getKey()) && obj.getValue().equals(vertexObject.getValue()))
                            .count() <= 0) {
                minVertexes.add(vertexObject);
            }
        }

        Pair<Fraction, Fraction> antinormal = getAntinormal();

        Fraction[] target = getTarget();
        Fraction[] moved = moveVector(target, antinormal.getKey().multiply(50_000), antinormal.getValue().multiply(50_000));
        moved[0] = moved[0].multiply(-1);
        Fraction[][] restrict = getRestrict();
        for (int i = 0; i < restrict.length; i++) {
            Fraction[] restrict1 = getRestrictRow(i);
            Fraction[] restrict2 = moved;

            Fraction[][] intersection = {
                    restrict1,
                    restrict2
            };
            GaussObject gauss = Helper.gauss(getFreeList(), intersection);
            if (gauss.getType() != GaussObject.Solution.ONE) {
                continue;
            }
            Fraction[][] answMatr = gauss.getMatr();
            Pair<Fraction, Fraction> vertex = new Pair<>(answMatr[0][0], answMatr[1][0]);
            if (isAllVertex(vertex)) {
                VertexObject vertexObject = new VertexObject(
                        answMatr[0][0],
                        answMatr[1][0],
                        new Fraction[][]{restrict1, restrict2}
                );
                Fraction val = getTargetValue(vertexObject.getKey(), vertexObject.getValue());
                if (val.compareTo(minValue) < 0) {
                    noSolution = true;
                    return new ArrayList<>();
                }
            }
        }

        if (getFreeList().size() == 1) {

            Fraction[] restrict1 = new Fraction[moved.length];
            for (int i = 0; i < restrict1.length; i++) {
                restrict1[i] = Fraction.ZERO;
                if (i == getBasisList().get(0)) {
                    restrict1[i] = Fraction.MINUS_ONE;
                }
            }

            Fraction[][] intersection = {
                    restrict1,
                    moved
            };
            GaussObject gauss = Helper.gauss(List.of(getBasisList().get(0), getFreeList().get(0)), intersection);
            if (gauss.getType() != GaussObject.Solution.ONE) {
                return minVertexes;
            }
            Fraction[][] answMatr = gauss.getMatr();
            Pair<Fraction, Fraction> vertex = new Pair<>(answMatr[1][0], answMatr[0][0]);
            if (isAllVertex(vertex)) {
                VertexObject vertexObject = new VertexObject(
                        answMatr[1][0],
                        answMatr[0][0],
                        new Fraction[][]{restrict1, moved}
                );
                Fraction val = getTargetValue(vertexObject.getKey(), vertexObject.getValue());
                if (val.compareTo(minValue) < 0) {
                    noSolution = true;
                    return new ArrayList<>();
                }
            }
        }
        return minVertexes;
    }

    private Fraction[] moveVector(Fraction[] vector, Fraction key, Fraction value) {
        Fraction[] moved = new Fraction[vector.length];
        System.arraycopy(vector, 0, moved, 0, vector.length);

        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }
        Integer first = freeList.get(0);
        Integer second = freeList.get(1);

        Fraction constVal = moved[0].subtract(
                moved[first].multiply(key)
        ).subtract(
                moved[second].multiply(value)
        );
        moved[0] = constVal;
        return moved;
    }

    private Fraction getTargetValue(Fraction key, Fraction value) {
        Fraction[] target = getTarget();
        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }
        Integer firstIdx = freeList.get(0);
        Integer secondIdx = freeList.get(1);
        return target[firstIdx].multiply(key).add(
                target[secondIdx].multiply(value)
        ).add(target[0]);
    }

    public Fraction[][] getAnswer() {
        if (freeList.size() > 2) {
            noSolution = true;
            return null;
        }
        if (isNoSolution()) {
            return null;
        }
        List<VertexObject> answerVertexes = getAnswerVertexes();
        Fraction[][] answer = new Fraction[answerVertexes.size()][];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = new Fraction[getVarCount() + 1];
        }
        int row = 0;
        for (VertexObject vertex : answerVertexes) {
            Fraction funcVal = getTargetValue(vertex.getKey(), vertex.getValue());
            List<Integer> freeList = getFreeList();


            for (int i = 1; i <= getVarCount(); i++) {
                if (i == freeList.get(0)) {
                    answer[row][i] = vertex.getKey();
                } else if (freeList.size() >= 2 && i == freeList.get(1)) {
                    answer[row][i] = vertex.getValue();
                } else {
                    if (getEquations().containsKey(i)) {
                        answer[row][i] = baseValue(i, vertex.getKey(), vertex.getValue());
                    }
                }
            }
            answer[row][0] = funcVal;
            row++;
        }
        return answer;
    }

    private Fraction baseValue(int i, Fraction key, Fraction value) {
        Fraction[] eq = getEquations().get(i);
        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }
        Integer firstIdx = freeList.get(0);
        Integer secondIdx = freeList.get(1);

        return eq[0].add(
                eq[firstIdx].multiply(key)
        ).add(
                eq[secondIdx].multiply(value)
        );
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

    public Map<Integer, Fraction[]> getEquations() {
        return equations;
    }

    public void setEquations(Map<Integer, Fraction[]> equations) {
        this.equations = equations;
    }

    public void addEquation(int baseNum, Fraction[] eq) {
        if (!equations.containsKey(baseNum)) {
            equations.put(baseNum, eq);
        }
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isNoSolution() {
        return noSolution;
    }

    public void setNoSolution(boolean noSolution) {
        this.noSolution = noSolution;
    }

    public List<LineObject> getInfVertexesLines() {
        List<LineObject> list = new ArrayList<>();
        Fraction[][] restrict = getRestrict();

        List<Integer> freeList = getFreeList();
        freeList.add(0);
        int first = freeList.get(0);
        int second = freeList.get(1);


        for (int i = 0; i < restrict.length - 2; i++) {
            Fraction[] restrictRow = restrict[i];
            Fraction key = Fraction.ZERO;
            Fraction value = valueFromKeyRestrict(key, restrictRow);
            VertexObject vertex1 = new VertexObject(key, value, new Fraction[][]{restrictRow});

            key = new Fraction(1_500_000);
            value = valueFromKeyRestrict(key, restrictRow);
            VertexObject vertex2 = new VertexObject(key, value, new Fraction[][]{restrictRow});

            LineObject line = new LineObject(vertex1, vertex2, restrictRow);
            list.add(line);
        }

        return list;
    }

    public Fraction valueFromKeyRestrict(Fraction key, Fraction[] row) {
        List<Integer> freeList = getFreeList();
        if (getBasisList().size() > 0) {
            freeList.add(getBasisList().get(0));
        }
        int first = freeList.get(0);
        int second = freeList.get(1);
        if (row[second].equals(Fraction.ZERO)) {
            return Fraction.ZERO;
        }
        Fraction keyVal = key.multiply(row[first]);

        Fraction value = row[0].subtract(keyVal);
        if (!row[second].equals(Fraction.ZERO)) {
            value = value.divide(row[second]);
        }
        return value;
    }
}
