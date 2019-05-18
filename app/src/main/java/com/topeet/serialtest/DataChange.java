package com.topeet.serialtest;

/**
 * Created by Administrator on 2017/4/15.
 */
public class DataChange {
// char转byte

    public static byte[] messageCharToByte(char[] chars, int chars_len) {
        byte i;
        byte[] bytes = new byte[256];
        for (i = 0;i<chars_len; i++){
            bytes[i*2+0] = (byte)((chars[i]>>8)&0x00ff);
            bytes[i*2+1] = (byte)((chars[i]>>0)&0x00ff);
        }
        return bytes;
    }

// byte转char

    public static char[] messageByteToChars(byte[] bytes, int bytes_len) {
        byte i;
        char[] chars = new char[128];
        bytes_len = (byte)(bytes_len/2);

        for(i = 0; i<bytes_len; i++){
            chars[i] = (char)((((char)bytes[i*2+1]) & 0x00ff) | (((char)(bytes[i*2+0]<<8)) & 0xff00));
        }
        return chars;
    }

    //number int to byte[] change
    public static byte[]  numIntToByte(int number) {
        byte[] i= new byte[3];
        i[0] = (byte) (number / 65536);
        i[1] = (byte) (number % 65536 / 256);
        i[2] = (byte) (number % 256);
        return i;
    }

    //number int to byte[] change
    public static int  numByteToInt(byte[] number) {
        int i;
        i = (((int)number[0]&0x000000ff)*65536)+(((int)number[1]&0x000000ff)*256)+(((int)number[2]&0x000000ff));
        return i;
    }


    //byte 与 int 的相互转换
    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

}
