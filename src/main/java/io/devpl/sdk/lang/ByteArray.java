package io.devpl.sdk.lang;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * <NOTE>this class if copied from the source code of Apache Calcite.</NOTE>
 * Collection of bytes.
 * <p>ByteArray is to bytes what {@link String} is to chars: It is immutable,
 * implements equality ({@link #hashCode} and {@link #equals}),
 * comparison ({@link #compareTo}) and {@link Serializable serialization} correctly.</p>
 */
public class ByteArray implements Comparable<ByteArray>, Serializable {

    /**
     * An empty byte string.
     */
    public static final ByteArray EMPTY = new ByteArray(new byte[0], false);
    @Serial
    private static final long serialVersionUID = -7661788929944453848L;
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final byte[] bytes;

    /**
     * Creates a ByteString.
     *
     * @param bytes Bytes
     */
    public ByteArray(byte[] bytes) {
        this(bytes.clone(), false);
    }

    private ByteArray(byte[] bytes, boolean dummy) {
        this.bytes = bytes;
    }

    /**
     * Returns the given byte array in hexadecimal format.
     *
     * <p>For example, <code>toString(new byte[] {0xDE, 0xAD})</code>
     * returns {@code "DEAD"}.</p>
     *
     * @param bytes Array of bytes
     * @param base  Base (2 or 16)
     * @return String
     */
    public static String toString(byte[] bytes, int base) {
        char[] chars;
        int j = 0;
        switch (base) {
            case 2 -> {
                chars = new char[bytes.length * 8];
                for (byte b : bytes) {
                    chars[j++] = DIGITS[(b & 0x80) >> 7];
                    chars[j++] = DIGITS[(b & 0x40) >> 6];
                    chars[j++] = DIGITS[(b & 0x20) >> 5];
                    chars[j++] = DIGITS[(b & 0x10) >> 4];
                    chars[j++] = DIGITS[(b & 0x08) >> 3];
                    chars[j++] = DIGITS[(b & 0x04) >> 2];
                    chars[j++] = DIGITS[(b & 0x02) >> 1];
                    chars[j++] = DIGITS[b & 0x01];
                }
            }
            case 16 -> {
                chars = new char[bytes.length * 2];
                for (byte b : bytes) {
                    chars[j++] = DIGITS[(b & 0xF0) >> 4];
                    chars[j++] = DIGITS[b & 0x0F];
                }
            }
            default -> throw new IllegalArgumentException("bad base " + base);
        }
        return new String(chars, 0, j);
    }

    /**
     * Creates a byte string from a hexadecimal or binary string.
     *
     * <p>For example, <code>of("DEAD", 16)</code>
     * returns the same as {@code ByteString(new byte[] {0xDE, 0xAD})}.
     *
     * @param string Array of bytes
     * @param base   Base (2 or 16)
     * @return String
     */
    public static ByteArray of(String string, int base) {
        final byte[] bytes = parse(string, base);
        return new ByteArray(bytes, false);
    }

    /**
     * Parses a hexadecimal or binary string to a byte array.
     *
     * @param string Hexadecimal or binary string
     * @param base   Base (2 or 16)
     * @return Byte array
     */
    public static byte[] parse(String string, int base) {
        char[] chars = string.toCharArray();
        byte[] bytes;
        int j = 0;
        byte b = 0;
        switch (base) {
            case 2 -> {
                bytes = new byte[chars.length / 8];
                for (char c : chars) {
                    b <<= 1;
                    if (c == '1') {
                        b |= 0x1;
                    }
                    if (j % 8 == 7) {
                        bytes[j / 8] = b;
                        b = 0;
                    }
                    ++j;
                }
            }
            case 16 -> {
                if (chars.length % 2 != 0) {
                    throw new IllegalArgumentException("hex string has odd length");
                }
                bytes = new byte[chars.length / 2];
                for (char c : chars) {
                    b <<= 4;
                    byte i = decodeHex(c);
                    b |= (byte) (i & 0x0F);
                    if (j % 2 == 1) {
                        bytes[j / 2] = b;
                        b = 0;
                    }
                    ++j;
                }
            }
            default -> throw new IllegalArgumentException("bad base " + base);
        }
        return bytes;
    }

    private static byte decodeHex(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        }
        if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        }
        if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        }
        throw new IllegalArgumentException("invalid hex character: " + c);
    }

    /**
     * Creates a byte string from a Base64 string.
     *
     * @param string Base64 string
     * @return Byte string
     */
    public static ByteArray ofBase64(String string) {
        final byte[] bytes = parseBase64(string);
        return new ByteArray(bytes, false);
    }

    /**
     * Parses a Base64 to a byte array.
     *
     * @param string Base64 string
     * @return Byte array
     */
    public static byte[] parseBase64(String string) {
        return Base64.getDecoder().decode(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ByteArray && Arrays.equals(bytes, ((ByteArray) obj).bytes);
    }

    @Override
    public int compareTo(ByteArray that) {
        final byte[] v1 = bytes;
        final byte[] v2 = that.bytes;
        final int n = Math.min(v1.length, v2.length);
        for (int i = 0; i < n; i++) {
            int c1 = v1[i] & 0xff;
            int c2 = v2[i] & 0xff;
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return v1.length - v2.length;
    }

    /**
     * Returns this byte string in hexadecimal format.
     *
     * @return Hexadecimal string
     */
    @Override
    public String toString() {
        return toString(16);
    }

    /**
     * Returns this byte string in a given base.
     *
     * @return String in given base
     */
    public String toString(int base) {
        return toString(bytes, base);
    }

    @Override
    public Object clone() {
        try {
            Object clone = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Returns the number of bytes in this byte string.
     *
     * @return Length of this byte string
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Returns the byte at a given position in the byte string.
     *
     * @param i Index
     * @return Byte at given position
     * @throws IndexOutOfBoundsException if the <code>index</code> argument is
     *                                   negative or not less than <code>length()</code>
     */
    public byte byteAt(int i) {
        return bytes[i];
    }

    /**
     * Returns a ByteString that consists of a given range.
     *
     * @param start Start of range
     * @param end   Position after end of range
     * @return Substring
     */
    public ByteArray substring(int start, int end) {
        byte[] bytes = Arrays.copyOfRange(this.bytes, start, end);
        return new ByteArray(bytes, false);
    }

    /**
     * Returns a ByteString that starts at a given position.
     *
     * @param start Start of range
     * @return Substring
     */
    public ByteArray substring(int start) {
        return substring(start, length());
    }

    /**
     * Returns a ByteString consisting of the concatenation of this and another
     * string.
     *
     * @param other Byte string to concatenate
     * @return Combined byte string
     */
    public ByteArray concat(ByteArray other) {
        int otherLen = other.length();
        if (otherLen == 0) {
            return this;
        }
        int len = bytes.length;
        byte[] buf = Arrays.copyOf(bytes, len + otherLen);
        System.arraycopy(other.bytes, 0, buf, len, other.bytes.length);
        return new ByteArray(buf, false);
    }

    /**
     * Returns the position at which {@code seek} first occurs in this byte
     * string, or -1 if it does not occur.
     */
    public int indexOf(ByteArray seek) {
        return indexOf(seek, 0);
    }

    /**
     * Returns the position at which {@code seek} first occurs in this byte
     * string, starting at the specified index, or -1 if it does not occur.
     */
    public int indexOf(ByteArray seek, int start) {
        iLoop:
        for (int i = start; i < bytes.length - seek.bytes.length + 1; i++) {
            for (int j = 0; ; j++) {
                if (j == seek.bytes.length) {
                    return i;
                }
                if (bytes[i + j] != seek.bytes[j]) {
                    continue iLoop;
                }
            }
        }
        return -1;
    }
}
