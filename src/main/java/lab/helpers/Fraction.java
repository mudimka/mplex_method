package lab.helpers;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DecimalFormat;

import lab.models.Conditions;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fraction.FractionConversionException;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;

public class Fraction extends Number implements FieldElement<Fraction>, Comparable<Fraction>, Serializable {
    public static final Fraction TWO = new Fraction(2, 1);
    public static final Fraction ONE = new Fraction(1, 1);
    public static final Fraction ZERO = new Fraction(0, 1);
    public static final Fraction FOUR_FIFTHS = new Fraction(4, 5);
    public static final Fraction ONE_FIFTH = new Fraction(1, 5);
    public static final Fraction ONE_HALF = new Fraction(1, 2);
    public static final Fraction ONE_QUARTER = new Fraction(1, 4);
    public static final Fraction ONE_THIRD = new Fraction(1, 3);
    public static final Fraction THREE_FIFTHS = new Fraction(3, 5);
    public static final Fraction THREE_QUARTERS = new Fraction(3, 4);
    public static final Fraction TWO_FIFTHS = new Fraction(2, 5);
    public static final Fraction TWO_QUARTERS = new Fraction(2, 4);
    public static final Fraction TWO_THIRDS = new Fraction(2, 3);
    public static final Fraction MINUS_ONE = new Fraction(-1, 1);
    private static final long serialVersionUID = 3698073679419233275L;
    private static final double DEFAULT_EPSILON = 1.0E-5D;
    private final int denominator;
    private final int numerator;

    public Fraction(double value) throws FractionConversionException {
        this(value, 1.0E-5D, 100);
    }

    public Fraction(double value, double epsilon, int maxIterations) throws FractionConversionException {
        this(value, epsilon, 2147483647, maxIterations);
    }

    public Fraction(double value, int maxDenominator) throws FractionConversionException {
        this(value, 0.0D, maxDenominator, 100);
    }

    private Fraction(double value, double epsilon, int maxDenominator, int maxIterations) throws FractionConversionException {
        long overflow = 2147483647L;
        double r0 = value;
        long a0 = (long)FastMath.floor(value);
        if (FastMath.abs(a0) > overflow) {
            throw new FractionConversionException(value, a0, 1L);
        } else if (FastMath.abs((double)a0 - value) < epsilon) {
            this.numerator = (int)a0;
            this.denominator = 1;
        } else {
            long p0 = 1L;
            long q0 = 0L;
            long p1 = a0;
            long q1 = 1L;
            long p2 = 0L;
            long q2 = 1L;
            int n = 0;
            boolean stop = false;

            while(true) {
                ++n;
                double r1 = 1.0D / (r0 - (double)a0);
                long a1 = (long)FastMath.floor(r1);
                p2 = a1 * p1 + p0;
                q2 = a1 * q1 + q0;
                if (FastMath.abs(p2) <= overflow && FastMath.abs(q2) <= overflow) {
                    double convergent = (double)p2 / (double)q2;
                    if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < (long)maxDenominator) {
                        p0 = p1;
                        p1 = p2;
                        q0 = q1;
                        q1 = q2;
                        a0 = a1;
                        r0 = r1;
                    } else {
                        stop = true;
                    }

                    if (!stop) {
                        continue;
                    }
                    break;
                }

                if (epsilon != 0.0D || FastMath.abs(q1) >= (long)maxDenominator) {
                    throw new FractionConversionException(value, p2, q2);
                }
                break;
            }

            if (n >= maxIterations) {
                throw new FractionConversionException(value, maxIterations);
            } else {
                if (q2 < (long)maxDenominator) {
                    this.numerator = (int)p2;
                    this.denominator = (int)q2;
                } else {
                    this.numerator = (int)p1;
                    this.denominator = (int)q1;
                }

            }
        }
    }

    public Fraction(int num) {
        this(num, 1);
    }

    public Fraction(int num, int den) {
        if (den == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, new Object[]{num, den});
        } else {
            if (den < 0) {
                if (num == -2147483648 || den == -2147483648) {
                    throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, new Object[]{num, den});
                }

                num = -num;
                den = -den;
            }

            int d = ArithmeticUtils.gcd(num, den);
            if (d > 1) {
                num /= d;
                den /= d;
            }

            if (den < 0) {
                num = -num;
                den = -den;
            }

            this.numerator = num;
            this.denominator = den;
        }
    }

    public Fraction abs() {
        Fraction ret;
        if (this.numerator >= 0) {
            ret = this;
        } else {
            ret = this.negate();
        }

        return ret;
    }

    public int compareTo(Fraction object) {
        long nOd = (long)this.numerator * (long)object.denominator;
        long dOn = (long)this.denominator * (long)object.numerator;
        return nOd < dOn ? -1 : (nOd > dOn ? 1 : 0);
    }

    public double doubleValue() {
        return (double)this.numerator / (double)this.denominator;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Fraction)) {
            return false;
        } else {
            Fraction rhs = (Fraction)other;
            return this.numerator == rhs.numerator && this.denominator == rhs.denominator;
        }
    }

    public float floatValue() {
        return (float)this.doubleValue();
    }

    public int getDenominator() {
        return this.denominator;
    }

    public int getNumerator() {
        return this.numerator;
    }

    public int hashCode() {
        return 37 * (629 + this.numerator) + this.denominator;
    }

    public int intValue() {
        return (int)this.doubleValue();
    }

    public long longValue() {
        return (long)this.doubleValue();
    }

    public Fraction negate() {
        if (this.numerator == -2147483648) {
            throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, new Object[]{this.numerator, this.denominator});
        } else {
            return new Fraction(-this.numerator, this.denominator);
        }
    }

    public Fraction reciprocal() {
        return new Fraction(this.denominator, this.numerator);
    }

    public Fraction add(Fraction fraction) {
        return this.addSub(fraction, true);
    }

    public Fraction add(int i) {
        return new Fraction(this.numerator + i * this.denominator, this.denominator);
    }

    public Fraction subtract(Fraction fraction) {
        return this.addSub(fraction, false);
    }

    public Fraction subtract(int i) {
        return new Fraction(this.numerator - i * this.denominator, this.denominator);
    }

    private Fraction addSub(Fraction fraction, boolean isAdd) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
        } else if (this.numerator == 0) {
            return isAdd ? fraction : fraction.negate();
        } else if (fraction.numerator == 0) {
            return this;
        } else {
            int d1 = ArithmeticUtils.gcd(this.denominator, fraction.denominator);
            if (d1 == 1) {
                int uvp = ArithmeticUtils.mulAndCheck(this.numerator, fraction.denominator);
                int upv = ArithmeticUtils.mulAndCheck(fraction.numerator, this.denominator);
                return new Fraction(isAdd ? ArithmeticUtils.addAndCheck(uvp, upv) : ArithmeticUtils.subAndCheck(uvp, upv), ArithmeticUtils.mulAndCheck(this.denominator, fraction.denominator));
            } else {
                BigInteger uvp = BigInteger.valueOf((long)this.numerator).multiply(BigInteger.valueOf((long)(fraction.denominator / d1)));
                BigInteger upv = BigInteger.valueOf((long)fraction.numerator).multiply(BigInteger.valueOf((long)(this.denominator / d1)));
                BigInteger t = isAdd ? uvp.add(upv) : uvp.subtract(upv);
                int tmodd1 = t.mod(BigInteger.valueOf((long)d1)).intValue();
                int d2 = tmodd1 == 0 ? d1 : ArithmeticUtils.gcd(tmodd1, d1);
                BigInteger w = t.divide(BigInteger.valueOf((long)d2));
                if (w.bitLength() > 31) {
                    throw new MathArithmeticException(LocalizedFormats.NUMERATOR_OVERFLOW_AFTER_MULTIPLY, new Object[]{w});
                } else {
                    return new Fraction(w.intValue(), ArithmeticUtils.mulAndCheck(this.denominator / d1, fraction.denominator / d2));
                }
            }
        }
    }

    public Fraction multiply(Fraction fraction) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
        } else if (this.numerator != 0 && fraction.numerator != 0) {
            int d1 = ArithmeticUtils.gcd(this.numerator, fraction.denominator);
            int d2 = ArithmeticUtils.gcd(fraction.numerator, this.denominator);
            return getReducedFraction(ArithmeticUtils.mulAndCheck(this.numerator / d1, fraction.numerator / d2), ArithmeticUtils.mulAndCheck(this.denominator / d2, fraction.denominator / d1));
        } else {
            return ZERO;
        }
    }

    public Fraction multiply(int i) {
        return this.multiply(new Fraction(i));
    }

    public Fraction divide(Fraction fraction) {
        if (fraction == null) {
            throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
        } else if (fraction.numerator == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_FRACTION_TO_DIVIDE_BY, new Object[]{fraction.numerator, fraction.denominator});
        } else {
            return this.multiply(fraction.reciprocal());
        }
    }

    public Fraction divide(int i) {
        return this.divide(new Fraction(i));
    }

    public double percentageValue() {
        return 100.0D * this.doubleValue();
    }

    public static Fraction getReducedFraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, new Object[]{numerator, denominator});
        } else if (numerator == 0) {
            return ZERO;
        } else {
            if (denominator == -2147483648 && (numerator & 1) == 0) {
                numerator /= 2;
                denominator /= 2;
            }

            if (denominator < 0) {
                if (numerator == -2147483648 || denominator == -2147483648) {
                    throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, new Object[]{numerator, denominator});
                }

                numerator = -numerator;
                denominator = -denominator;
            }

            int gcd = ArithmeticUtils.gcd(numerator, denominator);
            numerator /= gcd;
            denominator /= gcd;
            return new Fraction(numerator, denominator);
        }
    }

    public String toString() {
        String str = null;
        if (Conditions.isRational()) {
            if (this.denominator == 1) {
                str = Integer.toString(this.numerator);
            } else if (this.numerator == 0) {
                str = "0";
            } else {
                str = this.numerator + "/" + this.denominator;
            }
        } else {
            DecimalFormat format = new DecimalFormat("#.##");
            str = format.format(doubleValue());
        }

        return str;
    }

    public FractionField getField() {
        return FractionField.getInstance();
    }
}
