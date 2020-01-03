package com.example.intrek.SensorTile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NumberConversion {

    /**
     * Returns the short value for the specified bytes array in little endian format,
     * from a specified index start in the array and 2 bytes length.
     *
     * @param arr input bytes array that contains the value to convert
     * @param start start index in the array of the value to convert
     * @return the short value converted
     */
    public static short bytesToInt16(byte[] arr, int start)
    {
        return ByteBuffer.wrap(arr, start, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    /**
     * Returns the int value for the specified bytes array in little endian format,
     * from a specified index start in the array and 4 bytes length.
     *
     * @param arr input bytes array that contains the value to convert
     * @param start start index in the array of the value to convert
     * @return the int value contained in the array
     */
    public static int bytesToInt32(byte[] arr, int start)
    {
        return ByteBuffer.wrap(arr, start, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

}
