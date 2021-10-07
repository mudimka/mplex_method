package lab.helpers;

import java.util.List;

public class GaussObject {
    public enum Solution {
        ZERO, ONE, INF
    }
    private List<Integer> swapedCols;
    private Fraction[][] matr;
    private Solution type;

    public GaussObject() {
    }

    public GaussObject(Solution type) {
        this.type = type;
    }

    public GaussObject(Fraction[][] matr) {
        this.matr = matr;
    }

    public GaussObject(List<Integer> swapedCols) {
        this.swapedCols = swapedCols;
    }

    public GaussObject(List<Integer> swapedCols, Fraction[][] matr, Solution type) {
        this.swapedCols = swapedCols;
        this.matr = matr;
        this.type = type;
    }

    public List<Integer> getSwapedCols() {
        return swapedCols;
    }

    public void setSwapedCols(List<Integer> swapedCols) {
        this.swapedCols = swapedCols;
    }

    public Fraction[][] getMatr() {
        return matr;
    }

    public void setMatr(Fraction[][] matr) {
        this.matr = matr;
    }

    public Solution getType() {
        return type;
    }

    public void setType(Solution type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder matrLine = new StringBuilder();
        for (Fraction[] fractions : matr) {
            for (Fraction fraction : fractions) {
                matrLine.append("\t\t").append(fraction).append(" ");
            }
            matrLine.append("\n");
        }
        return "GaussObject{" +
                "\n\tswapedCols=" + swapedCols +
                ",\n\tmatr=[\n" + matrLine +
                "\t],\n\ttype=" + type +
                "\n}";
    }
}
