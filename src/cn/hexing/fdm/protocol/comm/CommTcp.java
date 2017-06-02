package cn.hexing.fdm.protocol.comm;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Printer;
import android.util.Xml;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;

/**
 * @author 王昌豹
 * @version 1.0
 * @Title: Tcp通讯类
 * @Description: Tcp Socket
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 */
public class CommTcp implements ICommucation {
    private Socket socket = null;
    private InputStream in = null;
    private byte[] arrList;
    byte[] strResult = null;

    @Override
    public boolean OpenDevice(CommPara cpara) {
        return false;
    }

    @Override
    public boolean Close() {
        return false;
    }



    static Timer timer2 = new Timer();
    static boolean delay_occurZJ = false;

    public static void timer2(int time) {
        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            public void run() {
                delay_occurZJ = true;
            }
        }, time, time);
    }

    @Override
    public byte[] ReceiveByt(int SleepT, int WaitT) {

        try {
            //Thread.sleep(100);
            delay_occurZJ = false;
            timer2.cancel();
            timer2(WaitT);
            //ToDo:接受改成按照协议去接受

            int length = 0;
            if (!socket.isClosed()) {
                if (socket.isConnected()) {
                    if (!socket.isInputShutdown()) {
                        byte[] buffer = new byte[1024];
                        byte[] rbuffer = new byte[600];
                        byte returnbyte = (byte) 0xff;
                        int Index = 0;
                        int size = 0;

                        while ((delay_occurZJ == false)) {

                            if (in.available() > 0) {
                                Thread.sleep(10);
                                size = in.read(buffer);// 刚开始为-1
                                //Log.d("DLMS", "第一针接收到了");
                                if (size > 0) {
                                    break;
                                }
                            }
                        }
                        delay_occurZJ = false;
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                rbuffer[i] = buffer[i];
                            }
                            returnbyte = (byte) rbuffer[size - 1];
                            Index = size;
                            while ((Index < ((rbuffer[2] & 0xff) + 1) || (returnbyte & 0xff) != 0x7e) && delay_occurZJ == false) {
                                if (returnbyte == 0x0a) {
                                    //Log.d("DLMS", "第二个循环");
                                    if ((rbuffer[Index - 2] & 0xff) == 0x8d) {
                                        Index--;
                                        break;
                                    }
                                }
                                size = in.read(buffer);// 刚开始为-1
                                if (size > -1) {
                                    //Index++;
                                    for (int i = 0; i < size; i++) {
                                        rbuffer[Index++] = buffer[i];
                                    }
                                    returnbyte = rbuffer[Index - 1];
                                    if ((returnbyte & 0xff) == 0x0a) {
                                        if ((rbuffer[Index - 2] & 0xff) == 0x8d) {
                                            //Index--;
                                            break;
                                        }
                                    }
                                }

                            }
                            //}
                            byte[] RtnData = new byte[Index];
                            for (int i = 0; i < Index; i++) {
                                RtnData[i] = rbuffer[i];
                            }
                            // rtnByt = RtnData;
                            strResult = RtnData;//转换过来的byte数组 不过介于你初学 建议你用第2中方式
                            String strSend = bytesToHexString(strResult);
                            Log.d("DLMS", "DLMS-Rec:" + strSend);


                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }


        return strResult;
    }

    /**
     * Byte 转String
     *
     * @param src
     * @return
     */

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean SendByt(byte[] sndByte) {
        try {
            if (socket == null) {
                socket = new Socket("10.10.100.254", 8899);
                // socket.setSoTimeout(3000);
            }
            OutputStream out = socket.getOutputStream();
            out.write(sndByte);
            in = socket.getInputStream();
            String strSend = bytesToHexString(sndByte);
            Log.d("DLMS", "DLMS-Send:" + strSend);

        } catch (IOException ex) {
            String str = ex.getMessage();
        }

        return true;
    }

    @Override
    public void SetBaudRate(int Baudrate) {

        byte[] sndByte = null;
        if (Baudrate == 300) {
            sndByte = new byte[]{0x55, (byte) 0xAA, 0x55, 0x00, 0x01, 0x2C, 0x03, 0x30};
        } else if (Baudrate == 9600) {
            sndByte = new byte[]{0x55, (byte) 0xAA, 0x55, 0x00, 0x25, (byte) 0x80, 0x03, (byte) 0xA8};
        }
        try {
            if (socket == null) {
                socket = new Socket("10.10.100.254", 8899);
                socket.setSoTimeout(5000);
            }
            OutputStream out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write(sndByte);

            String strSend = bytesToHexString(sndByte);

            Log.d("DLMS", "DLMS-Send:" + strSend);

        } catch (IOException ex) {
            String str = ex.getMessage();
        }
    }

	@Override
	public void setORIData(String ORIData) {
		// TODO Auto-generated method stub
		
	}
}
