package lab.helpers;

import java.util.Arrays;

public class LineObject {
    VertexObject vertex1;
    VertexObject vertex2;
    Fraction[] equation;

    public LineObject(VertexObject vertex1, VertexObject vertex2, Fraction[] equation) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.equation = equation;
    }

    public VertexObject getVertex1() {
        return vertex1;
    }

    public void setVertex1(VertexObject vertex1) {
        this.vertex1 = vertex1;
    }

    public VertexObject getVertex2() {
        return vertex2;
    }

    public void setVertex2(VertexObject vertex2) {
        this.vertex2 = vertex2;
    }

    public Fraction[] getEquation() {
        return equation;
    }

    public void setEquation(Fraction[] equation) {
        this.equation = equation;
    }

    public String getEquationString() {
        StringBuilder eq = new StringBuilder();
        for (int i = 0; i < equation.length; i++) {
            Fraction val = equation[i];
            if (!val.equals(Fraction.ZERO)) {
                String term = String.format("(%s)x%d", val, i);
                if (i == 0) {
                    term = String.format("(%s)", val, i);
                }
                eq.append(term).append(" + ");
            }
        }
        if (eq.length() > 0) {
            eq.delete(eq.length() - 3, eq.length());
            eq.append(" = 0");
        }
        return eq.toString();
    }


    @Override
    public String toString() {
        return "LIneObject{" +
                "vertex1=" + vertex1 +
                ", vertex2=" + vertex2 +
                ", equation=" + Arrays.toString(equation) +
                '}';
    }
}
