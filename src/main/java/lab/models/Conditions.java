package lab.models;

import lab.helpers.Fraction;

import java.util.Arrays;
import java.util.List;

public class Conditions{
    private static Fraction[] target;
    private static Fraction[][] restrict;
    private static List<Integer> baseList;
    private static boolean rational = true;
    private static boolean min = true;
    private static int varCount;
    private static int restrictCount;

    public static Fraction[] getTarget() {
        Fraction[] target = new Fraction[Conditions.target.length];

            for (int i = 0; i < target.length; i++) {
                if (isMin()) {
                    target[i] = Conditions.target[i];
                } else {
                    target[i] = Conditions.target[i].multiply(-1);
                }
            }

        return target;
    }

    public static void setTarget(Fraction[] target) {
        Conditions.target = target;
        Conditions.varCount = target.length - 1;
    }

    public static Fraction[] getSourceTarget() {
        return target;
    }

    public static Fraction[][] getRestrict() {
        return restrict;
    }

    public static void setRestrict(Fraction[][] restrict) {
        Conditions.restrict = restrict;
    }

    public static List<Integer> getBaseList() {
        return baseList;
    }

    public static void setBaseList(List<Integer> baseList) {
        Conditions.baseList = baseList;
    }

    public static boolean isRational() {
        return rational;
    }

    public static void setRational(boolean rational) {
        Conditions.rational = rational;

    }

    public static boolean isMin() {
        return min;
    }

    public static void setMin(boolean min) {
        Conditions.min = min;

    }

    public static int getVarCount() {
        return varCount;
    }

    public static void setVarCount(int varCount) {
        Conditions.varCount = varCount;
    }

    public static int getRestrictCount() {
        return restrictCount;
    }

    public static void setRestrictCount(int restrictCount) {
        Conditions.restrictCount = restrictCount;

    }

    public static String verbose() {
        StringBuilder restrictLine = new StringBuilder();
        for (Fraction[] fractions : restrict) {
            for (Fraction fraction : fractions) {
                restrictLine.append("\t\t").append(fraction).append(" ");
            }
            restrictLine.append("\n");
        }
        return "Conditions{\n" +
                "\ttarget= " + Arrays.toString(target) +
                ", \n\trestrict=[\n" + restrictLine +
                "\t], \n\tbaseList= " + baseList +
                ", \n\trational= " + rational +
                ", \n\tmin= " + min +
                "\n}";

    }
}
