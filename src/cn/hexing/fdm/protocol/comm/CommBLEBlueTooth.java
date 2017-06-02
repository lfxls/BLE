package cn.hexing.fdm.protocol.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;

/**
 * Created by Ace on 17/3/10.
 */
public class CommBLEBlueTooth implements ICommucation {

    // ��ȡ������������
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;

    // UUID����������������Ҫ��
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");//db764ac8-4b08-7f25-aafe-59d03c27bae3//00001101-0000-1000-8000-00805F9B34FB
    // Ϊ�����Ӵ���һ������
    private final String NAME = "Bluetooth_Socket";
    // ѡ�з������ݵ������豸��ȫ�ֱ��������������ڷ���ִ����ͽ�����
    private BluetoothDevice selectDevice;
    // ��ȡ��ѡ���豸�Ŀͻ��˴��ڣ�ȫ�ֱ��������������ڷ���ִ����ͽ�����
    private BluetoothSocket clientSocket;
    // ��ȡ�����豸д���������ȫ�ֱ��������������ڷ���ִ����ͽ�����
    private OutputStream os;
    // ����������̲߳��Ͻ��ܿͻ�����Ϣ

    byte[] strResult = null;
    static Timer timer2 = new Timer();
    static boolean delay_occurZJ = false;

    public void timer2(int time) {
        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            public void run() {
                delay_occurZJ = true;
            }
        }, time, time);
    }


    @Override
    public boolean OpenDevice(CommPara cpara) {

        if (true){
            return true;
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String address = cpara.ComName;
        // �жϵ�ǰ�Ƿ������������ܱ��豸�����������ͣ����
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // ���ѡ���豸Ϊ�������û��ѡ���豸
        if (selectDevice == null) {
            //ͨ����ַ��ȡ�����豸
            selectDevice = mBluetoothAdapter.getRemoteDevice(address);
        }

        try {
            // �жϿͻ��˽ӿ��Ƿ�Ϊ��
            if (clientSocket == null) {
                // ��ȡ���ͻ��˽ӿ�
                clientSocket = selectDevice.createRfcommSocketToServiceRecord(MY_UUID);
                // �����˷�������
                clientSocket.connect();
                // ��ȡ�������������д����
                os = clientSocket.getOutputStream();

                // ������������
//                ReadThread mReadThread = new ReadThread();
//                mReadThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
            // ��������쳣������û�����ʧ��
        }


        return true;
    }

    byte[] buf = null;
    // ����handler����Ϊ���ǽ����ǲ����߳������յģ����߳����޷�����UI��������Ҫhandler
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            buf = (byte[]) msg.obj;
            String strRec = bytesToHexString(buf);
            Log.d("DLMS", "HandleRec:" + strRec);
            // ͨ��msg���ݹ�������Ϣ����˾һ���յ�����Ϣ
            //Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    };

    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = clientSocket.getInputStream();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true) {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        //String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = buf_data;
                        msg.what = 1;
                        handler.sendMessage(msg);
                        Log.d("DLMS", "Begin to rec");
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    @Override
    public boolean Close() {

        if (clientSocket != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (clientSocket.isConnected()) {
                        clientSocket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientSocket = null;
        }
        return true;
    }
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    private String ORIData = "";
    public void setORIData(String ORIData){
    	this.ORIData = ORIData;
    }
    @Override
    public byte[] ReceiveByt(int SleepT, int WaitT) {
//        byte[] b = new byte[]{0x7E,(byte)0xA0,0x1C,0x00,0x02,(byte)0xFE,(byte)0xFF,0x03,0x32 ,(byte)0xD9 ,(byte)0xCB ,(byte)0xE6 ,(byte)0xE6 ,0x00 ,0xC0 ,0x01 ,0xC1 ,0x00 ,0x03 ,0x01 ,0x00
//                ,0x8C ,0x81 ,0x00 ,0xFF ,0x02 ,0x00 ,0x7E ,0x83 ,0x7E};
        String resp = ORIData; //"7EA02F030002FEFFDA25ABE6E700C401410001010203090C07DA0A01FF0000000080000106000000000600000000ADBE7E";
                //"7EA018030002FEFF961F32E6E700C40141000500004E20D87E7E";
        byte[] b = hexStringToByte(resp);

        if(true)return b;

        try {
            //Thread.sleep(100);
            delay_occurZJ = false;
            timer2.cancel();
            timer2(WaitT);
            //ToDo:���ܸĳɰ���Э��ȥ����

            int length = 0;
            if (clientSocket != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (clientSocket.isConnected()) {

                        InputStream in = clientSocket.getInputStream();
                        byte[] buffer = new byte[1024];
                        byte[] rbuffer = new byte[600];
                        byte returnbyte = (byte) 0xff;
                        int Index = 0;
                        int size = 0;

                        while ((delay_occurZJ == false)) {
                            if (in.available() > 0) {
                                size = in.read(buffer);// �տ�ʼΪ-1
                                if (size > 0) {
                                    break;
                                }
                            } else {
                                Thread.interrupted();
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
                                    //Log.d("DLMS", "�ڶ���ѭ��");
                                    if (((rbuffer[Index - 2] & 0xff) == 0x0d) | ((rbuffer[Index - 2] & 0xff) == 0x8d)) {
                                        Index--;
                                        break;
                                    }
                                }
                                size = in.read(buffer);// �տ�ʼΪ-1
                                if (size > -1) {
                                    //Index++;
                                    for (int i = 0; i < size; i++) {
                                        rbuffer[Index++] = buffer[i];
                                    }
                                    returnbyte = rbuffer[Index - 1];
                                    if ((returnbyte & 0xff) == 0x0a) {
                                        if (((rbuffer[Index - 2] & 0xff) == 0x0d) | ((rbuffer[Index - 2] & 0xff) == 0x8d)) {
                                            //Index--;
                                            break;
                                        }
                                    }
                                }

                            }
                            byte[] RtnData = new byte[Index];
                            for (int i = 0; i < Index; i++) {
                                RtnData[i] = rbuffer[i];
                            }
                            strResult = RtnData;//ת��������byte���� �����������ѧ �������õ�2�з�ʽ
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

    @Override
    public boolean SendByt(byte[] sndByte) {

        if(true){
            return true;
        }
        try {
            if (os != null) {
                os.write(sndByte);
            }

            String strRec = bytesToHexString(sndByte);

            Log.d("DLMS", "DLMS-Send:" + strRec);
        } catch (IOException ex) {
            String str = ex.getMessage();
        }

        return true;
    }

    @Override
    public void SetBaudRate(int Baudrate) {

        byte[] sndByte = null;

        if (Baudrate == 300) {
            sndByte = new byte[]{0x42, 0x61, 0x75, 0x64, 0x54, 0x72, 0x61, 0x6E, 0x2C, 0x33
                    , 0x30, 0x30, 0x2C, 0x4E, 0x2C, 0x38, 0x2C, 0x30, 0x2C, 0x0D, 0x0A};
        } else if (Baudrate == 9600) {
            sndByte = new byte[]{0x42, 0x61, 0x75, 0x64, 0x54, 0x72, 0x61, 0x6E, 0x2C, 0x39,
                    0x36, 0x30, 0x30, 0x2C, 0x4E, 0x2C, 0x38, 0x2C, 0x30, 0x2C, 0x0D, 0x0A};
        }
        try {

            if (os != null) {
                os.write(sndByte);
            }

            String strSend = bytesToHexString(sndByte);

            Log.d("DLMS", "DLMS-Send1:" + strSend);

            InputStream is = clientSocket.getInputStream();
            int bytes = 0;
            byte[] buffer = new byte[1024];
            byte[] buf_data = null;
            if ((bytes = is.read(buffer)) > 0) {
                buf_data = new byte[bytes];
                for (int i = 0; i < bytes; i++) {
                    buf_data[i] = buffer[i];
                }
            }

            String strRec = bytesToHexString(buf_data);

            Log.d("DLMS", "DLMS-Rec1:" + strRec);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (IOException ex) {
            String str = ex.getMessage();
        }

    }

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
}
