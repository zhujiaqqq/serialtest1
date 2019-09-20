package com.topeet.serialtest;

/**
 * Created by Administrator on 2017/4/20.
 */
public class DipperCom {


    //报文串口相关变量
    public static int[] send_data = new int[500];
    public static int send_num;
    public static int[] receive_data = new int[500];
    public static int receive_num;

    //语音报文发送相关变量
    public static int fspdwd_time = 20;

    //语音报文接收相关变量
    public static int receive_id;
    public static char[] receive_message = new char[256];
    public static int receive_message_len;

    //北斗信息相关变量
    public static int t_shi, t_fen, t_miao, l_du, l_fen, l_miao, b_du, b_fen, b_miao, gaodu;

    //语音收发记录
    public static int save_num = 0;
    public static String[] save_data = new String[15];

    //系统信息相关变量
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

        send_num = num;

        for (i = 0; i < num; i++) {
            send_data[i] = ((dptr[i]) & 0x000000ff);
        }
    }

    public static int comReceive(byte[] sptr, int num) {
        int res = 0;
        byte[] id = new byte[3];
        byte[] message = new byte[256];
        int rx_len = 0;
        int i = 0;

        for (i = 0; i < num; i++) {
            receive_data[receive_num] = (int) sptr[i];
            receive_num++;
        }
        if (receive_num > 7) {
            message_len = (receive_data[5] << 8) | receive_data[6];
            if (message_len == receive_num) {
                XOR_test = XORCheck(receive_data, message_len - 1);
                if (receive_data[message_len - 1] == XORCheck(receive_data, message_len - 1)) {
                    receive_num = 0;
                    switch (receive_data[1]) {
                        case 'F':
                            if (receive_data[11] == 'D') {
                                res = receive_data[10] + 10 + 300;

                            } else {
                                fspdwd_time = receive_data[11];
                                res = receive_data[10] + 0 + 100;
                            }
                            break;
                        case 'T':
                            id[0] = DataChange.intToByte(receive_data[11]);
                            id[1] = DataChange.intToByte(receive_data[12]);
                            id[2] = DataChange.intToByte(receive_data[13]);
                            rx_len = (receive_data[16] * 256 + receive_data[17]) / 8;
                            for (i = 0; i < rx_len; i++) {
                                message[i] = DataChange.intToByte(receive_data[19 + i]);
                            }
                            receive_id = DataChange.numByteToInt(id);
                            receive_message = DataChange.messageByteToChars(message, rx_len);
                            receive_message_len = rx_len / 2;
                            res = 1 + 200;
                            break;
                        case 'D':
                            t_shi = receive_data[14];
                            t_fen = receive_data[15];
                            t_miao = receive_data[16];
                            l_du = receive_data[18];
                            l_fen = receive_data[19];
                            l_miao = receive_data[20];
                            b_du = receive_data[22];
                            b_fen = receive_data[23];
                            b_miao = receive_data[24];
                            gaodu = receive_data[26] * 256 + receive_data[27];
                            res = 1 + 300;
                            break;
                        case 'Z':
                            id[0] = DataChange.intToByte(receive_data[7]);
                            id[1] = DataChange.intToByte(receive_data[8]);
                            id[2] = DataChange.intToByte(receive_data[9]);
                            local_id = DataChange.numByteToInt(id);
                            for (i = 0; i < 6; i++) {
                                glzk[i] = receive_data[i + 14];
                            }
                            IC_state = receive_data[10];
                            hardware_state = receive_data[11];
                            inbound_state = receive_data[12];
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
