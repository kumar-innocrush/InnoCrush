package com.innocrush.laser.utils;

public class Checksum {
    public static int calculateCheckSum(Byte[] bytes ){
       /* int CheckSum = 0, i;
        for( i = 0; i < bytes.length; i++){
            CheckSum += bytes[i] & 0xFF;
        }
        System.out.println(CheckSum);
        return CheckSum;*/

        int sample =  0;
        for (int i = 0; i < bytes.length; i++) {
            sample ^= bytes[i];
        }
        // convert back to byte
        byte b = (byte)(0xff & sample);
        return b;
    }
}
