package shellmark.watermark.util;

/**
 * Subclasses of this class are used to encode integral values in
 * methods.
 *
 * @see shellmark.analysis.controlflowgraph.MethodCFG
 */

public abstract class MethodMarker {

    /**
     * Attempts to encode the given value in the given method.  The
     * method is modified in place.  If the number of bits in the given
     * value exceeds that of the value returned by
     * {@link #getCapacity(shellmark.program.Method)},
     * an {@link java.lang.IllegalArgumentException}
     * will be thrown.
     *
     * @param method method to encode data in
     * @param value data to encode
     * @throws java.lang.IllegalArgumentException if value has too many bits
     * @see #getCapacity(shellmark.program.Method)
     */
    public abstract void embed(shellmark.program.Method method,
                               java.math.BigInteger value);

    /**
     * Attempts to encode the given value in the given method.  The
     * method is modified in place.  If the number of bits in the given
     * value exceeds that of the value returned by
     * {@link #getCapacity(shellmark.program.Method)},
     * an {@link IllegalArgumentException}
     * will be thrown.<br><br>
     * This method is implemented with a call to
     * {@link #embed(shellmark.program.Method, java.math.BigInteger)}.
     *
     * @param method method to encode data in
     * @param value data to encode
     * @throws java.lang.IllegalArgumentException if value has too many bits
     * @see #getCapacity(shellmark.program.Method)
     */
    public final void embed(shellmark.program.Method method,
                            long value) {
        embed(method, java.math.BigInteger.valueOf(value));
    }

    /**
     * Returns an {@link java.util.Iterator} over all
     * values found to be embedded in the
     * given method.  Only values embedded using the marking scheme
     * used by this marker will be reported.
     *
     * @param method method to search for marks in
     */
    public abstract java.util.Iterator recognize(shellmark.program.Method method);

    /**
     * Returns the number of bits that can be encoded into the given method.
     *
     * @param method method to report the bit capacity of
     * @see #embed(shellmark.program.Method, java.math.BigInteger)
     * @see #embed(shellmark.program.Method, long)
     */
    public abstract int getCapacity(shellmark.program.Method method);
}

