package com.topeet.serialtest;

import com.topeet.serialtest.util.NumberUtil;

/**
 * @author Administrator
 * @date 2017/4/20
 */
public class DipperCom {


    /**
     * 报文串口相关变量
     */
    static int[] sSendData = new int[500];
    static int sSendNum;
    private static int[] sReceiveData = new int[500];
    private static int sReceiveNum;

    /**
     * 语音报文发送相关变量
     */
    public static int FSPDWD_TIME = 20;

    /**
     * 语音报文接收相关变量
     */
    public static int receive_id;
    public static char[] receive_message = new char[256];
    public static int receive_message_len;

    /**
     * 北斗信息相关变量
     */
    public static int t_shi, t_fen, t_miao, l_du, l_fen, l_miao, b_du, b_fen, b_miao, gaodu;

    /**
     * 语音收发记录
     */
    public static int S_SAVE_NUM = 0;
    public static String[] save_data = new String[15];

    /**
     * 系统信息相关变量
     */
    public static int local_id;
    public static int[] glzk = new int[6];
    public static int IC_state, hardware_state, inbound_state;
    public static String versions = "QB3510_1.5.130410_BONARF2.01";


    private static int message_len;

    private static int XOR_test = 0;


    public static byte XORCheck(byte[] dptr, int num) {
        byte a = 0;
        a = (byte) (dptr[0] ^ dptr[1]);
        for (int i = 2; i < (num); i++) {
            a = (byte) (a ^ dptr[i]);
        }
        return a;
    }

    public static int XORCheck(int[] dptr, int num) {
        int a = 0;
        a = (dptr[0] ^ dptr[1]);
        for (int i = 2; i < (num); i++) {
            a = (a ^ dptr[i]);
        }
        return a;
    }

    public static void comSend(byte[] dptr, int num) {
        //调用发送函数
        int[] text = new int[num];
        int i;

        sSendNum = num;

        for (i = 0; i < num; i++) {
            sSendData[i] = ((dptr[i]) & 0x000000ff);
        }
    }

    public static int comReceive(byte[] sptr, int num) {
        int res = 0;
        byte[] id = new byte[3];
        byte[] message = new byte[256];
        int rx_len = 0;
        int i = 0;

        for (i = 0; i < num; i++) {
            sReceiveData[sReceiveNum] = (int) sptr[i];
            sReceiveNum++;
        }
        if (sReceiveNum > 7) {
            message_len = (sReceiveData[5] << 8) | sReceiveData[6];
            if (message_len == sReceiveNum) {
                XOR_test = XORCheck(sReceiveData, message_len - 1);
                if (sReceiveData[message_len - 1] == XORCheck(sReceiveData, message_len - 1)) {
                    sReceiveNum = 0;
                    switch (sReceiveData[1]) {
                        case 'F':
                            if (sReceiveData[11] == 'D') {
                                res = sReceiveData[10] + 10 + 300;

                            } else {
                                FSPDWD_TIME = sReceiveData[11];
                                res = sReceiveData[10] + 0 + 100;
                            }
                            break;
                        case 'T':
                            id[0] = NumberUtil.intToByte(sReceiveData[11]);
                            id[1] = NumberUtil.intToByte(sReceiveData[12]);
                            id[2] = NumberUtil.intToByte(sReceiveData[13]);
                            rx_len = (sReceiveData[16] * 256 + sReceiveData[17]) / 8;
                            for (i = 0; i < rx_len; i++) {
                                message[i] = NumberUtil.intToByte(sReceiveData[19 + i]);
                            }
                            receive_id = NumberUtil.numByteToInt(id);
                            receive_message = NumberUtil.messageByteToChars(message, rx_len);
                            receive_message_len = rx_len / 2;
                            res = 1 + 200;
                            break;
                        case 'D':
                            t_shi = sReceiveData[14];
                            t_fen = sReceiveData[15];
                            t_miao = sReceiveData[16];
                            l_du = sReceiveData[18];
                            l_fen = sReceiveData[19];
                            l_miao = sReceiveData[20];
                            b_du = sReceiveData[22];
                            b_fen = sReceiveData[23];
                            b_miao = sReceiveData[24];
                            gaodu = sReceiveData[26] * 256 + sReceiveData[27];
                            res = 1 + 300;
                            break;
                        case 'Z':
                            id[0] = NumberUtil.intToByte(sReceiveData[7]);
                            id[1] = NumberUtil.intToByte(sReceiveData[8]);
                            id[2] = NumberUtil.intToByte(sReceiveData[9]);
                            local_id = NumberUtil.numByteToInt(id);
                            for (i = 0; i < 6; i++) {
                                glzk[i] = sReceiveData[i + 14];
                            }
                            IC_state = sReceiveData[10];
                            hardware_state = sReceiveData[11];
                            inbound_state = sReceiveData[12];
                            res = 1 + 400;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return res;
    }


}
