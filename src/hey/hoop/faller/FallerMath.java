package hey.hoop.faller;

public final class FallerMath {
    /**
     * If <code>v</code> is outside range [<code>start</code>, <code>end</code>], it is moved to the nearest boundary
     * . <code>tmp</code> should be <code>null</code> or be of length > 0. If <code>tmp</code> is not
     * <code>null</code> then <code>tmp[0]</code> says if <code>v</code> has been changed.
     *
     * @param v     The value to be changed.
     * @param start The first number of the range.
     * @param end   The last number of the range.
     * @param tmp   Indicator if <code>v</code> has been changed.
     * @return New value of <code>v</code>.
     */
    public static float putIntoRange(float v, float start, float end, boolean[] tmp) {
        if (tmp != null) {
            assert tmp.length > 0;
            tmp[0] = false;
        }
        if (v < start || v > end) {
            if (tmp != null)
                tmp[0] = true;
            if (v < start) v = start;
            else v = end;
        }
        return v;
    }

    public static float sqr(float v) {
        return v * v;
    }

    public static float decreaseAbsValue(float v) {
        if (v > 1 || v < -1)
            return (float) Math.copySign(Math.sqrt(Math.abs(v)), v);
        else
            return Math.copySign(v * v, v);
    }

    /**
     * Transposes row-wise matrix.
     *
     * @param in Input row-wise matrix.
     * @param d1 Number of rows in <code>in</code>.
     * @param d2 Number of columns in <code>in</code>.
     * @return Transposition of <code>in</code>.
     */
    public static float[] transposeMatrix(float[] in, int d1, int d2) {
        assert in.length == d1 * d2;
        float[] out = new float[in.length];
        for (int i = 0; i < d1; ++i)
            for (int j = 0; j < d2; ++j)
                out[j * d2 + i] = in[i * d1 + j];
        return out;
    }

    /**
     * Multiplies row-wise <code>matrix</code> and <code>vector</code>.
     *
     * @param matrix Row-wise matrix.
     * @param vector Column-wise vector.
     * @return Multiplication of <code>matrix</code> and <code>vector</code>.
     */
    public static float[] applyMatrix(float[] matrix, float[] vector) {
        int d2 = vector.length;
        int d1 = matrix.length / d2;
        assert d1 * d2 == matrix.length;
        float[] out = new float[d1];
        for (int i = 0; i < d1; ++i) {
            out[i] = 0;
            for (int j = 0; j < d2; ++j)
                out[i] += matrix[i * d1 + j] * vector[j];
        }
        return out;
    }
}
