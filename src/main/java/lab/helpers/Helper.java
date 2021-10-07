package lab.helpers;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Helper {
    private static List<Integer> swapedCols;
    private static GaussObject.Solution type = GaussObject.Solution.ONE;
    private static List<Integer> basisList;

    public static GaussObject gauss(List<Integer> basisList, Fraction[][] matr) {
        type = GaussObject.Solution.ONE;
        GaussObject gaussResult = new GaussObject();
        basisList = basisList.stream().sorted().collect(Collectors.toList());
        Helper.basisList = new ArrayList<>(basisList);
        Fraction[][] gaussMatr = new Fraction[matr.length][];
        swapedCols = IntStream.rangeClosed(1, matr[0].length - 1).boxed().collect(Collectors.toList());
        for (int i = 0; i < matr.length; i++) {
            gaussMatr[i] = new Fraction[matr[i].length];
        }
        for (int i = 0; i < matr.length; i++) {
            System.arraycopy(matr[i], 0, gaussMatr[i], 0, matr[i].length);
        }
        swapByOneMatr(basisList, gaussMatr);
        gauss(gaussMatr);

        gaussMatr = removeZeroRows(gaussMatr);
        gaussResult.setMatr(gaussMatr);
        gaussResult.setSwapedCols(swapedCols);
        gaussResult.setType(type);

        return gaussResult;
    }

    private static Fraction[][] removeZeroRows(Fraction[][] matr) {
        List<Integer> zeroRows = getZeroRows(matr);
        Fraction[][] cleanedMatr = new Fraction[matr.length - zeroRows.size()][];
        int rowIdx = 0;
        for (int i = 0; i < matr.length; i++) {
            if (!zeroRows.contains(i)) {
                cleanedMatr[rowIdx] = new Fraction[matr[i].length];
                System.arraycopy(matr[i], 0, cleanedMatr[rowIdx], 0, matr[i].length);
                rowIdx++;
            }
        }
        return cleanedMatr;
    }

    private static List<Integer> getZeroRows(Fraction[][] matr) {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < matr.length; i++) {
            if (isZeroRow(matr[i])) {
                rows.add(i);
            }
        }
        return rows;
    }

    private static boolean isZeroRow(Fraction[] fractions) {
        return Arrays.stream(fractions).allMatch(val -> val.equals(Fraction.ZERO));
    }

    private static void gauss(Fraction[][] matr) {
        // Прямой ход
        for (int i = 0; i < matr.length; i++) {
            replaceNotZero(matr, i);
            if (matr[i][i + 1].equals(Fraction.ZERO)) {
                continue;
            }
            for (int k = i + 1; k < matr.length; k++) {
                addRow(matr, k, i, matr[k][i + 1].divide(matr[i][i + 1]).multiply(-1));
                if (isZeroType(matr)) {
                    type = GaussObject.Solution.ZERO;
                    return;
                }
            }

            rowMultiply(matr, i, Fraction.ONE.divide(matr[i][i + 1]));
        }

        // Обратный ход
        for (int i = matr.length - 1; i >= 0; i--) {
            if (matr[i][i + 1].equals(Fraction.ZERO)) {
                continue;
            }
            for (int k = i - 1; k >= 0; k--) {
                addRow(matr, k, i, matr[k][i + 1].divide(matr[i][i + 1]).multiply(-1));
                if (isZeroType(matr)) {
                    type = GaussObject.Solution.ZERO;
                    return;
                }
            }
        }
        if (isInfTypeCol(matr)) {
            type = GaussObject.Solution.INF;
        }
    }

    private static boolean isZeroType(Fraction[][] matr) {
        for (int i = 0; i < matr.length; i++) {
            if (isZeroTypeRow(matr, i)) {
                return true;
            }
        }
        return false;
    }


    private static boolean isInfTypeCol(Fraction[][] matr) {
        for (Fraction[] fractions : matr) {
            for (int k = basisList.size() + 1; k < matr[0].length; k++) {
                if (!fractions[k].equals(Fraction.ZERO)) {
                    return true;
                }
            }
        }
        for (int i = 0; i < matr.length; i++) {
            if (matr[i][i + 1].equals(Fraction.ZERO)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isZeroTypeRow(Fraction[][] matr, int row) {
        Fraction val = matr[row][0];
        if (val.equals(Fraction.ZERO)) {
            return false;
        }
        for (int k = 1; k < matr[row].length; k++) {
            if (!matr[row][k].equals(Fraction.ZERO)) {
                return false;
            }
        }
        return true;
    }

    private static void addRow(Fraction[][] matr, int from, int to, Fraction multiply) {
        for (int j = 0; j < matr[from].length; j++) {
            matr[from][j] = matr[from][j].add(matr[to][j].multiply(multiply));
        }
    }

    private static void rowMultiply(Fraction[][] matr, int row, Fraction coef) {
        for (int i = 0; i < matr[row].length; i++) {
            matr[row][i] = matr[row][i].multiply(coef);
        }
    }


    private static void replaceNotZero(Fraction[][] matr, int j) {
        int row = j;
        for (int i = j; i < matr.length; i++) {
            if (!matr[i][j + 1].equals(Fraction.ZERO)) {
                row = i;
                break;
            }
        }
        if (row != j) {
            swapRow(matr, row, j);
        }

    }

    private static void swapByOneMatr(List<Integer> cols, Fraction[][] matr) {
        int curCol = 1;
        for (int colVal : cols) {
            if (curCol == colVal) {
                curCol++;
                continue;
            }

            swapCol(matr, curCol, colVal);
            curCol++;
        }
    }

    private static void swapRow(Fraction[][] matr, int a, int b) {
        for (int i = 0; i < matr[0].length; i++) {
            swap(matr, a, i, b, i);
        }
    }

    private static void swapCol(Fraction[][] matr, int a, int b) {
        for (int i = 0; i < matr.length; i++) {
            swap(matr, i, a, i, b);
        }
        Collections.swap(swapedCols, a - 1, b - 1);
    }


    public static void swapMatrCol(Fraction[][] matr, int a, int b) {
        for (int i = 0; i < matr.length; i++) {
            swap(matr, i, a, i, b);
        }
    }

    public static void swap(Fraction[][] matr, int i, int a, int i1, int b) {
        Fraction temp = matr[i][a];
        matr[i][a] = matr[i1][b];
        matr[i1][b] = temp;
    }


    public static Fraction stringToFraction(String value) throws IllegalArgumentException {
        if (value.matches("-?[0-9]+")) {
            int num = Integer.parseInt(value);
            return new Fraction(num, 1);
        }
        if (value.matches("-?[0-9]+/?[0-9]+")) {
            String[] data = value.split("/");
            int num = Integer.parseInt(data[0]);
            int denum = Integer.parseInt(data[1]);
            return new Fraction(num, denum);
        }
        if (value.matches("^-?[0-9]+([.,][0-9]+)?$")) {
            value = value.replaceAll(",", ".");
            double doubleVal = Double.parseDouble(value);
            return new Fraction(doubleVal);
        }
        throw new IllegalArgumentException("Строка не соответстует Fraction");
    }


    public static void printMatr(Fraction[][] matr) {
        for (Fraction[] fractions : matr) {
            for (Fraction fraction : fractions) {
                System.out.print("\t" + fraction);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void message(
            Alert.AlertType type,
            String title,
            String headerText,
            String contentText
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(Helper.class.getResourceAsStream("/lab/main-icon.png")))
        );
        alert.showAndWait();
    }
}
