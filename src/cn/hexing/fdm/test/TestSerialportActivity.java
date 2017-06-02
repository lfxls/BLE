package cn.hexing.fdm.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.hexing.fdm.protocol.bll.dlmsService.DataType;
import cn.hexing.fdm.protocol.comm.CommBLEBlueTooth;
import cn.hexing.fdm.protocol.comm.CommOpticalSerialPort;
import cn.hexing.fdm.protocol.comm.CommTcp;
import cn.hexing.fdm.protocol.dlms.HXHdlcDLMS;
import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.iprotocol.IProtocol;
import cn.hexing.fdm.protocol.model.CommPara;
import cn.hexing.fdm.protocol.model.HXFramePara;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMethod;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMode;
import cn.hexing.fdm.protocol.model.TranXADRAssist;
import cn.hexing.fdm.services.CommServer;

import com.android.SerialPort.SerialPort;
import com.example.bluetooth.le.R;

//import android.provider.Settings.System;

public class TestSerialportActivity extends Activity implements OnClickListener {
    FileDescriptor mfd;
    protected SerialPort mSerialPort = new SerialPort();
    protected OutputStream mOutputStream;
    private InputStream mInputStream;

    private Button buttonsend;
    private EditText EditTDisplay;

    boolean m_stop = false;
    final static int IOCTRL_PMU_RFID_ON = 0x03;
    final static int IOCTRL_PMU_RFID_OFF = 0x04;
    final static int IOCTRL_PMU_BARCODE_ON = 0x05;
    final static int IOCTRL_PMU_BARCODE_OFF = 0x06;
    final static int IOCTRL_PMU_BARCODE_TRIG_HIGH = 0x11;
    final static int IOCTRL_PMU_BARCODE_TRIG_LOW = 0x12;
    final static int IOCTRL_PMU_RS232_ON = 0x17;
    final static int IOCTRL_PMU_RS232_OFF = 0x18;
    final static int IOCTRL_PMU_RFID_GPIOEXT_HIGH = 0x13;
    final static int IOCTRL_PMU_RFID_GPIOEXT_LOW = 0x14;
    private String uartpath;
    private SharedPreferences localSharedPreferences;
    ICommucation icomm =new CommBLEBlueTooth();//new CommTcp(); //new CommOpticalSerialPort();
    IProtocol DLMSProtocol = new HXHdlcDLMS();

    private boolean Openport = false;
    private ArrayAdapter<String> adapter;
    String baudrate;
    final String setting_file = "/mnt/sdcard/sendtext.txt";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EditTDisplay = (EditText) findViewById(R.id.EditTDisplay);
        buttonsend = (Button) findViewById(R.id.btn_send);

        buttonsend.setOnClickListener(this);
        localSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String strTransmit = "";
                CommServer commDlmsServer = null;
                try {
                    CommPara Cpara = new CommPara();
                    Cpara.setComName("0F:03:16:71:52:48");

                    // DLMS 通讯参数
                    HXFramePara FramePara = new HXFramePara();
                    FramePara.CommDeviceType = "RF";// RF  Optical
                    FramePara.FirstFrame = false;
                    FramePara.Mode = AuthMode.HLS;
                    FramePara.enLevel = 0x00;
                    FramePara.SourceAddr = 0x03;
                    FramePara.strMeterNo = "014000000019";
                    FramePara.WaitT = 3000;
                    FramePara.ByteWaitT = 1500;
                    FramePara.Pwd = "00000000";
                    FramePara.aesKey = new byte[16];
                    FramePara.auKey = new byte[16];
                    FramePara.enKey = new byte[16];
                    String sysTstr = "4845430005000001";
                    FramePara.StrsysTitleC = "4845430005000001";
                    FramePara.encryptionMethod = AuthMethod.AES_GCM_128;
                    FramePara.sysTitleS = new byte[8];
                    FramePara.MaxSendInfo_Value = 255;
                    EditTDisplay.setText("");
                    commDlmsServer = new CommServer();
                    icomm = commDlmsServer.OpenDevice(Cpara, icomm);

//                    //电表余额 3  1-0:140.129.0.255  2
//                    FramePara.OBISattri = "3#1.0.140.129.0.255#2";
//                    FramePara.strDecDataType = "Int32";
//                    String strResult = commDlmsServer.Read(FramePara, icomm);
//                    if (strResult != "") {
//                        Toast.makeText(getApplicationContext(), strResult,
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Error",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    if (FramePara.CommDeviceType == "Optical") {
//                        commDlmsServer.DiscFrame(icomm);
//                    }

                    //日冻�?                    FramePara.OBISattri = "7#0.0.98.2.0.255#2";
                    FramePara.strDecDataType = "Struct_Billing";
                    List<TranXADRAssist> temp = new ArrayList<TranXADRAssist>();
                    TranXADRAssist item1 = new TranXADRAssist();
                    item1.strName = "DateTime";
                    temp.add(item1);

                    TranXADRAssist item2 = new TranXADRAssist();
                    item2.strName = "Active energy (+)";
                    item2.unit="kWh";
                    item2.nScaler=0;
                    temp.add(item2);

                    TranXADRAssist item3 = new TranXADRAssist();
                    item3.strName = "Active energy (-)";
                    item3.unit="kWh";
                    item3.nScaler=0;
                    temp.add(item3);

                    FramePara.listTranXADRAssist = temp;
                    String strResult = commDlmsServer.Read(FramePara, icomm);
                    if (strResult != "") {
                        Toast.makeText(getApplicationContext(), strResult,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (FramePara.CommDeviceType == "Optical") {
                        commDlmsServer.DiscFrame(icomm);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (icomm != null) {

                        commDlmsServer.Close(icomm);

                    }
                }
        }
    }
    public void onClicktest(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String strTransmit = "";
                CommServer commDlmsServer = null;

                try {
                    //执行上电操作
//                    RS232Controller mPower232 = new RS232Controller();
//                    mPower232.Rs232_PowerOn();

//                    //执行RF上电操作
//                    PowerSecurityController mPowerController = new PowerSecurityController();
//                    mPowerController.openDevice();

//                    // 光电头�?讯参数设�?//                    uartpath = "/dev/ttySAC1";// localSharedPreferences.getString("path",
//                    // GetDeviceDefaultpath());
//                    int baudrate = Integer.valueOf((localSharedPreferences
//                            .getString("baudrate", "4800")));
//                    int nBits = Integer.valueOf(localSharedPreferences.getString(
//                            "nBits", "8"));
//                    String sVerify = localSharedPreferences.getString("nVerify",
//                            "N");
//                    char cVerify = sVerify.charAt(0);
//                    int nStop = Integer.valueOf(localSharedPreferences.getString(
//                            "nStop", "1"));
                    CommPara Cpara = new CommPara();
                    Cpara.setComName("0F:03:16:71:52:48");
//                    Cpara.setBRate(baudrate);
//                    Cpara.setDBit(nBits);
//                    Cpara.setPty(cVerify);
//                    Cpara.setSbit(nStop);

                    // DLMS 通讯参数
                    HXFramePara FramePara = new HXFramePara();
                    FramePara.CommDeviceType = "Optical";// RF  Optical
                    FramePara.FirstFrame = true;
                    FramePara.Mode = AuthMode.HLS;
                    FramePara.enLevel = 0x00;
                    FramePara.SourceAddr = 0x03;
                    FramePara.strMeterNo = "014254455455";
                    FramePara.WaitT = 3000;
                    FramePara.ByteWaitT = 1500;
                    FramePara.Pwd = "00000000";
                    FramePara.aesKey = new byte[16];
                    FramePara.auKey = new byte[16];
                    FramePara.enKey = new byte[16];
                    String sysTstr = "4845430005000001";
                    FramePara.StrsysTitleC = "4845430005000001";
                    FramePara.encryptionMethod = AuthMethod.AES_GCM_128;
                    FramePara.sysTitleS = new byte[8];
                    FramePara.MaxSendInfo_Value = 255;
                    EditTDisplay.setText("");
                    commDlmsServer = new CommServer();
                    icomm = commDlmsServer.OpenDevice(Cpara, icomm);

                    //清事�?//                    FramePara.OBISattri = "1#1.0.144.128.0.255#2";//清事�?//                    FramePara.strDecDataType = "U8";
//                    FramePara.WriteData = "01";
//                    boolean strResult = commDlmsServer.Write(FramePara, icomm);
//                    if (strResult) {
//                        Toast.makeText(getApplicationContext(), "Clear Event Succeed",
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Clear Event Failure",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    if (FramePara.CommDeviceType == "Optical") {
//                        commDlmsServer.DiscFrame(icomm);
//                    }

//                    //日冻�?//                    FramePara.OBISattri = "7#0.0.98.2.0.255#2";
//                    FramePara.strDecDataType = "Struct_Billing";
//                    List<TranXADRAssist> temp = new ArrayList<TranXADRAssist>();
//                    TranXADRAssist item1 = new TranXADRAssist();
//                    item1.strName = "DateTime";
//                    temp.add(item1);
//
//                    TranXADRAssist item2 = new TranXADRAssist();
//                    item2.strName = "Active energy (+)";
//                    item2.unit="kWh";
//                    item2.nScaler=0;
//                    temp.add(item2);
//
//                    TranXADRAssist item3 = new TranXADRAssist();
//                    item3.strName = "Active energy (-)";
//                    item3.unit="kWh";
//                    item3.nScaler=0;
//                    temp.add(item3);
//
//                    FramePara.listTranXADRAssist = temp;
//                    String strResult = commDlmsServer.Read(FramePara, icomm);
//                    if (strResult != "") {
//                        Toast.makeText(getApplicationContext(), strResult,
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Error",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    if (FramePara.CommDeviceType == "Optical") {
//                        commDlmsServer.DiscFrame(icomm);
//                    }

//                    //电表余额 3  1-0:140.129.0.255  2
                    FramePara.OBISattri = "3#1.0.140.129.0.255#2";
                    FramePara.strDecDataType = "Int32";
                    String strResult = commDlmsServer.Read(FramePara, icomm);
                    if (strResult != "") {
                        Toast.makeText(getApplicationContext(), strResult,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (FramePara.CommDeviceType == "Optical") {
                        commDlmsServer.DiscFrame(icomm);
                    }

                    //下装Token
//                    FramePara.OBISattri = "1#1.0.129.129.2.255#2";
//                    FramePara.strDecDataType = "Octs_string";
//                    FramePara.WriteData = "29191410388149031911";
//                    boolean strResult = commDlmsServer.Write(FramePara, icomm);
//                    if (strResult) {
//                        Toast.makeText(getApplicationContext(), "Set Token Succeed",
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Set Token Failure",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    if (FramePara.CommDeviceType == "Optical") {
//                        commDlmsServer.DiscFrame(icomm);
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (icomm != null) {

                        commDlmsServer.Close(icomm);

                    }
                }


                break;

        }
    }

    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toUpperCase();
        final byte[] byteArray = new byte[(hexString.length() + 1) / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    char HexChar(char c) {
        if ((c >= '0') && (c <= '9'))
            return (char) (c - 0x30);
        else if ((c >= 'A') && (c <= 'F'))
            return (char) (c - 'A' + 10);
        else if ((c >= 'a') && (c <= 'f'))
            return (char) (c - 'a' + 10);
        else
            return 0x10;
    }

    // 将一个字符串作为十六进制串转化为�?��字节数组，字节间可用空格分隔�?    // 返回转换后的字节数组长度，同时字节数组长度自动设置�?
    byte[] Str2Hex(String str) {
        int t, t1;
        int rlen = 0, len = str.length();
        final byte[] byteArray = new byte[str.length()];
        // data.SetSize(len/2);
        for (int i = 0; i < len; ) {
            char l, h = str.charAt(i);
            if (h == ' ') {
                i++;
                continue;
            }
            i++;
            if (i >= len)
                break;
            l = str.charAt(i);
            t = HexChar(h);
            t1 = HexChar(l);
            if ((t == 16) || (t1 == 16))
                break;
            else
                t = t * 16 + t1;
            i++;
            byteArray[rlen] = (byte) t;
            rlen++;
        }

        final byte[] byteArray1 = new byte[rlen];
        System.arraycopy(byteArray, 0, byteArray1, 0, rlen);

        return byteArray1;

    }

    int dev_num = 0;
    private String deviceType = " ";

    public static String toHexString(byte[] byteArray, int size) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException(
                    "this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder(2 * size);
        for (int i = 0; i < size; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
            if (i != (byteArray.length - 1))
                hexString.append(" ");
        }
        return hexString.toString().toUpperCase();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("0122", keyCode + "");

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mSerialPort != null) {

                mSerialPort.SetPowerState(IOCTRL_PMU_BARCODE_OFF);

                mSerialPort.SetPowerState(IOCTRL_PMU_RFID_OFF);
                mSerialPort.SetPowerState(IOCTRL_PMU_RFID_GPIOEXT_LOW);
                mSerialPort.SetPowerState(IOCTRL_PMU_RS232_OFF);
                mSerialPort.SetPowerState(IOCTRL_PMU_BARCODE_TRIG_LOW);
                if (mOutputStream != null)
                    mSerialPort.close();
                mSerialPort = null;
            }
            m_stop = true;
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }



    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}