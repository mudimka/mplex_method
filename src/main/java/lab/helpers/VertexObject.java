package lab.helpers;

import java.util.Arrays;

public class VertexObject {
    private Fraction key;
    private Fraction value;
    private Fraction[][] lines;

    public VertexObject(Fraction key, Fraction value, Fraction[][] lines) {
        this.key = key;
        this.value = value;
        this.lines = lines;
    }

    public VertexObject(Fraction key, Fraction value) {
        this.key = key;
        this.value = value;
        this.lines = new Fraction[0][];
    }

    public Fraction getKey() {
        return key;
    }

    public void setKey(Fraction key) {
        this.key = key;
    }

    public Fraction getValue() {
        return value;
    }

    public void setValue(Fraction value) {
        this.value = value;
    }

    public Fraction[][] getLines() {
        return lines;
    }

    public void setLines(Fraction[][] lines) {
        this.lines = lines;
    }

    public void addLines(Fraction[][] newLines) {
        Fraction[][] ln = new Fraction[lines.length + newLines.length][];
        System.arraycopy(lines, 0, ln, 0, lines.length);
        int idx = lines.length;
        for (Fraction[] newLine : newLines) {
            ln[idx++] = newLine;
        }
        this.lines = ln;
    }

    @Override
    public String toString() {
        return "VertexObject{" +
                "key=" + key +
                ", value=" + value +
                ", lines=" + Arrays.toString(lines) +
                '}';
    }
}
