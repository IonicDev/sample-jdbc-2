package com.ionic.sdk.addon.jdbc.usecase2.jdbc;

import java.nio.ByteBuffer;

/**
 * The Ionic chunk cipher implementation used by this sample project provides for the reversible encryption of byte
 * arrays and strings.  For non-string types (such as integers), conversion of the data to byte arrays is necessary as
 * a prerequisite step.
 */
public class IonicTypes {

    /**
     * Convert the input integer to a byte array, suitable for Ionic-protection using
     * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2}.
     *
     * @param value the int value to be converted
     * @return the byte[] representation of "value"
     */
    public static byte[] toBytes(final int value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE_INT32);
        return byteBuffer.putInt(value).array();
    }

    /**
     * Convert the input byte array to the equivalent integer.
     *
     * @param value the byte[] value to be converted
     * @return the int representation of "value"
     */
    public static int toInt(final byte[] value) {
        int offset = value.length - SIZE_INT32;
        final ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE_INT32);
        byteBuffer.put(value, offset, SIZE_INT32);
        return byteBuffer.getInt(0);
    }

    private static final int SIZE_INT32 = Integer.SIZE / Byte.SIZE;
}
