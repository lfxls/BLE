/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bluetooth.le;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.utils.CRCUtil;
import com.utils.CustomProgressDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.hexing.fdm.protocol.comm.CommBLEBlueTooth;
import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;
import cn.hexing.fdm.protocol.model.HXFramePara;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMethod;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMode;
import cn.hexing.fdm.protocol.model.TranXADRAssist;
import cn.hexing.fdm.services.CommServer;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private Button button,button1,button2,button3,button4,button5,button6,button7,button8,button9,button10,button11,button12;
    private EditText token;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    ICommucation icomm =new CommBLEBlueTooth();//new CommTcp(); //new CommOpticalSerialPort();
    
    private BluetoothGatt mBluetoothGatt;
    private String DecDataType="";
    private int selectId=0;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private String AvaData = "";
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
//                updateConnectionState(R.string.connected);
                updateConnectionState(R.string.ready);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().replace(" ", "");
            	AvaData = AvaData+data;
            	Pattern p = Pattern.compile("7E+\\w+7E");
            	Matcher m = p.matcher(AvaData);
            	if(m.matches()){
            		AvaData.substring(AvaData.indexOf("7E"), AvaData.lastIndexOf("7E")+2);
            		displayData(AvaData);
            		Log.i("返回数据", AvaData);
            		AvaData = "";
            	}
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            mBluetoothLeService.readCharacteristic(characteristic);//读操作
                            
                            
                            if(characteristic!=null){//通知
                            	if(characteristic.getUuid().toString().contains("ffe4")){
                            		mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//                            		Toast.makeText(getApplicationContext(), "通知！", Toast.LENGTH_SHORT).show();
                            	}else{//写数据
//                                	characteristic.setValue(new byte[] { 0x7E,(byte)0xA0,(byte)0x1C });//随便举个数据
                                	
                                	String commond1 = "7E A0 21 00 02 FE FF 03 32 DC 33 E6 E6 00 C1 01 C1".replaceAll(" ", "");
                                	String commond2 = "00 01 01 00 8C 82 00 FF 02 00 0A 03 38 36 35 90 6F 7E".replaceAll(" ", "");
                                	
                                	characteristic.setValue(hexStringToByte(commond1));
                                	mBluetoothLeService.writeCharacteristic(characteristic);//写命令到设备，
                                	
                                	SystemClock.sleep(150);
                                	
                                	characteristic.setValue(hexStringToByte(commond2));
                                	mBluetoothLeService.writeCharacteristic(characteristic);//写命令到设备，
                                	
                                	Toast.makeText(getApplicationContext(), "写入！", Toast.LENGTH_SHORT).show();
                            	}
                            }
                            
                            
                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            mBluetoothLeService.setCharacteristicNotification(
//                                    characteristic, true);
//                        }
                        return true;
                    }
                    return false;
                }
    };
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

    private void clearUI() {
//        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    CommServer commDlmsServer = null;
    CommPara Cpara = new CommPara();
    HXFramePara FramePara = new HXFramePara();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
//        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
//        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        
        button = (Button)findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);
        button10 = (Button) findViewById(R.id.button10);
        button11 = (Button) findViewById(R.id.button11);
        button12 = (Button) findViewById(R.id.button12);
        token = (EditText) findViewById(R.id.token);
        
        button.setOnClickListener(btnlisten);
        button1.setOnClickListener(btnlisten);
        button2.setOnClickListener(btnlisten);
        button3.setOnClickListener(btnlisten);
        button4.setOnClickListener(btnlisten);
        button5.setOnClickListener(btnlisten);
        button6.setOnClickListener(btnlisten);
        button7.setOnClickListener(btnlisten);
        button8.setOnClickListener(btnlisten);
        button9.setOnClickListener(btnlisten);
        button10.setOnClickListener(btnlisten);
        button11.setOnClickListener(btnlisten);
        button12.setOnClickListener(btnlisten);
        
        
            Cpara.setComName(mDeviceAddress);
            // DLMS 通讯参数
            FramePara.CommDeviceType = "RF";// RF  Optical
            FramePara.FirstFrame = false;
            FramePara.Mode = AuthMode.HLS;
            FramePara.enLevel = 0x00;
            FramePara.SourceAddr = 0x03;
            FramePara.strMeterNo = mDeviceName;
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
            commDlmsServer = new CommServer();
            icomm = commDlmsServer.OpenDevice(Cpara, icomm);
            
            DeviceControlActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        getActionBar().setIcon(R.drawable.chilun);
        getActionBar().setTitle(R.string.title_devices);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            mBluetoothGatt = mBluetoothLeService.getBluetoothGatt();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
    	 icomm.setORIData("");
    	 if(progressDialog!=null){
    		 progressDialog.dismiss();;
    	 }
    	 if (data != null) {
//             mDataField.setText(data);
    		 Log.i("返回原始数据", data);
         }
    	
            icomm.setORIData(data);
            String strResult = "";
            boolean bolResult = false;
            
            switch(selectId){
             case 1: case 2:case 3:
            	 bolResult = commDlmsServer.Write(FramePara, icomm);
            	 if(bolResult){
            		 mDataField.setText("Success");
            	 }else{
            		 mDataField.setText("Failed");
            	 }
            	break;
             case 12://token
//            	 0：成功		
//            	 1：TOKEN码解析错误		
//            	 2：TOKEN码已用过		
//            	 3：TOKEN码过期		
//            	 4：密钥过期		
//            	 5：充值数值超囤积限额		
//            	 6：密钥类型不允许充值		
//            	 7：非指定厂家产生的测试码		
//            	 8：输入第一串修改密钥TOKEN后，接着输入的不是第二串修改密钥TOKEN		
//            	 9：密钥类型不能改为3		
//            	 10：如果父密钥类型不是初始化密钥，则子密钥类型不能改为初始化密钥		
//            	 bolResult = commDlmsServer.Write(FramePara, icomm);// 提示
//            	 if(bolResult){
//            		 mDataField.setText("Success");
//            	 }else{
//            		 mDataField.setText("Failed");
//            	 }
            	 
            	 if(data.length()>44){
            		 String tem = data.substring(42, 44);
        			 strResult = tem;
            		 
            	 }
        		 if (strResult != "") {
        			 if(strResult.equals("00")){
//        				 strResult = "Token下装成功";
        				 strResult = "Token Download Success";
        			 }else if(strResult.equals("01")){
        				 strResult = "TOKEN Parse Error";
        			 }else if(strResult.equals("02")){
        				 strResult = "TOKEN Used";
        			 }else if(strResult.equals("03")){
        				 strResult = "TOKEN Out of Date";
        			 }else if(strResult.equals("04")){
        				 strResult = "Key Out of Date";
        			 }else if(strResult.equals("05")){
        				 strResult = "Recharge value over accumulation limit";
        			 }else if(strResult.equals("06")){
        				 strResult = "Key type is not allowed to recharge";
        			 }else if(strResult.equals("07")){
        				 strResult = "Test code produced by a non designated manufacturer";
        			 }else if(strResult.equals("08")){
        				 strResult = "Token Type Error";
        			 }else if(strResult.equals("09")){
        				 strResult = "Key Type Error";
        			 }else if(strResult.equals("10")){
        				 strResult = "Token Type Error";
        			 }else{
        				 strResult = "Token Download Failed";
        			 }
        		  mDataField.setText(strResult);
	            } else {
	            	mDataField.setText("NO Data");
	            }
            	 
            	 break;
             case 5:
            	 strResult = commDlmsServer.Read(FramePara, icomm);
            	
        		 if (strResult != "") {
        			 int numResult = Integer.parseInt(strResult);
                	 if(numResult>0){
                		 numResult = numResult/100;
                	 }
                	 strResult = numResult+"";
                	 mDataField.setText(strResult+" KWh");
//                    Toast.makeText(getApplicationContext(), strResult,
//                            Toast.LENGTH_SHORT).show();
                } else {
                	mDataField.setText("NO Data");
//                    Toast.makeText(getApplicationContext(), "Error",
//                            Toast.LENGTH_SHORT).show();
                }
        		 break;
             case 4://继电器操作原因 int8类型无解析
//            	 strResult = commDlmsServer.Read(FramePara, icomm);
            	 if(data.length()>36){
            		 String tem = data.substring(34, 36);
            		 if(tem.equals("00")){
            			 strResult = data.substring(38, 40);
            		 }else{
            			 strResult = "";
            		 }
            		 
            	 }
//            	 01		余额不足没有使用透支功能
//            	 02		余额不足，已使用透支功能
//            	 03		过载断开
//            	 04		STS测试断开
//            	 05		开表盖断开
//            	 06		开端盖断开
//            	 07		远程拉闸
//            	 08		过流断开
//            	 09		过压断开
//            	 0B		出厂默认断开继电器
//            	 0C		欠压断开
//            	 0D		反向断开
//            	 0E		缺零线断开
//            	 00		正常用电
//            	 10		STS测试闭合
//            	 20		远程合闸
//            	 11		开表箱断开
        		 if (strResult != "") {
        			 if(strResult.equals("01")){
        				 strResult = "Out of credit but not use overdraft";
        			 }else if(strResult.equals("02")){
        				 strResult = " Out of credit after overdraft";
        			 }else if(strResult.equals("03")){
        				 strResult = "Over load";
        			 }else if(strResult.equals("04")){
        				 strResult = "Disconnection test";
        			 }else if(strResult.equals("05")){
        				 strResult = "Main cover removed ";
        			 }else if(strResult.equals("06")){
        				 strResult = "Terminal cover removed";
        			 }else if(strResult.equals("07")){
        				 strResult = "Remote disconnection";
        			 }else if(strResult.equals("08")){
        				 strResult = "Over current";
        			 }else if(strResult.equals("09")){
        				 strResult = "Over voltage";
        			 }else if(strResult.equals("0A")){
        				 strResult = "No charge for long period";
        			 }else if(strResult.equals("0B")){
        				 strResult = "Default operation after factory";
        			 }else if(strResult.equals("0C")){
        				 strResult = "Under voltage";
        			 }else if(strResult.equals("0D")){
        				 strResult = "Current reversal";
        			 }else if(strResult.equals("0E")){
        				 strResult = "Missing neutral ";
        			 }else if(strResult.equals("00")){
        				 strResult = "Normal";
        			 }else if(strResult.equals("10")){
        				 strResult = "Reconnection test";
        			 }else if(strResult.equals("20")){
        				 strResult = "Remote reconnection";
        			 }else if(strResult.equals("11")){
        				 strResult = "Box cover removed";
        			 }else{
        				 strResult = "";
        			 }
                	 mDataField.setText(strResult);
                } else {
                	mDataField.setText("NO Data");
                }
        		 break;
             case 6:
            	 strResult = commDlmsServer.Read(FramePara, icomm);
        		 if (strResult != "") {
                	 mDataField.setText(strResult + "KWh");
//                    Toast.makeText(getApplicationContext(), strResult,
//                            Toast.LENGTH_SHORT).show();
                } else {
                	mDataField.setText("NO Data");
//                    Toast.makeText(getApplicationContext(), "Error",
//                            Toast.LENGTH_SHORT).show();
                }
        		 break;
            	default:
            		strResult = commDlmsServer.Read(FramePara, icomm);
            		 if (strResult != "") {
                    	 mDataField.setText(strResult);
//                        Toast.makeText(getApplicationContext(), strResult,
//                                Toast.LENGTH_SHORT).show();
                    } else {
                    	mDataField.setText("NO Data");
//                        Toast.makeText(getApplicationContext(), "Error",
//                                Toast.LENGTH_SHORT).show();
                    }
            		 break;
            }
           
           

       
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        
        boolean write_uuid = false;
        boolean notify_uuid= false;
        String services = "";
        for(BluetoothGattService gattService : gattServices){
        	String temp = gattService.getUuid().toString();
        	if(temp.equalsIgnoreCase(SampleGattAttributes.METER_WRITE_SERVICE)){
        		write_uuid = true;
        	}
        	if(temp.equalsIgnoreCase(SampleGattAttributes.METER_NOTIFY_SERVICE)){
        		notify_uuid = true;
        	}
        	services = services + "\n" +temp;
        }
//        alert(services);
        
        if(write_uuid && notify_uuid){
        	Toast.makeText(getApplicationContext(), "Connect Success", Toast.LENGTH_SHORT).show();
        	updateConnectionState(R.string.connected);
        	//设置按钮可用
        	//...
        }else{
        	Toast.makeText(getApplicationContext(), "invalid device", Toast.LENGTH_SHORT).show();
        	//设置按钮非可用
        	//...
        }
        
       /* 
       String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );*/
//        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    android.view.View.OnClickListener btnlisten = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String commond = "";
			selectId = 0;
			clearUI();
			switch(v.getId()){
			case R.id.button:
				//通信 发送通知服务
				BluetoothGattService nservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_NOTIFY_SERVICE));
				BluetoothGattCharacteristic ncharacteristic= mBluetoothLeService.getCharacteristic(nservice,UUID.fromString(SampleGattAttributes.METER_NOTIFY_CHARACTERISTIC));
				
				if( nservice==null || ncharacteristic==null){
					 Toast.makeText(getApplicationContext(), "Get BLE services failed", Toast.LENGTH_SHORT).show();
				}
				mBluetoothLeService.setCharacteristicNotification(ncharacteristic, true);
				return;
			case R.id.button1: //进入正常运行模式 Octs_ascii
				commond = "7E A0 21 00 02 FE FF 03 32 DC 33 E6 E6 00 C1 01 C1 00 01 01 00 8C 82 00 FF 02 00 0A 03 38 36 35 90 6F 7E";
				FramePara.OBISattri = "3#1.0.140.129.0.255#2";//
				FramePara.WriteData = "865";
				FramePara.strDecDataType = "Octs_ascii";
				selectId = 1;
				break;
			case R.id.button2: //取消声音报警
				commond = "7E A0 21 00 02 FE FF 03 32 DC 33 E6 E6 00 C1 01 C1 00 01 01 00 8C 82 00 FF 02 00 0A 03 38 31 32 27 56 7E";
				FramePara.OBISattri = "3#1.0.140.129.0.255#2";//
	              FramePara.strDecDataType = "Octs_ascii";
	              FramePara.WriteData = "812";
	              selectId = 2;
				break;
			case R.id.button3: //紧急透支
				commond = "7E A0 21 00 02 FE FF 03 32 DC 33 E6 E6 00 C1 01 C1 00 01 01 00 8C 82 00 FF 02 00 0A 03 38 31 31 BC 64 7E";
				FramePara.OBISattri = "3#1.0.140.129.0.255#2";//
	              FramePara.strDecDataType = "Octs_ascii";
	              FramePara.WriteData = "811";
	              selectId = 3;
				break;
			case R.id.button4: //继电器操作原因（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 46 00 00 60 03 0A FF 05 00 26 28 7E";
				 FramePara.OBISattri = "3#1.0.140.129.0.255#2";//
	             FramePara.strDecDataType = "INT8";
	             selectId = 4;
				break;
			case R.id.button5://查询电表余额（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 03 01 00 8C 81 00 FF 02 00 7E 83 7E";
				//电表余额 3  1-0:140.129.0.255  2
	              FramePara.OBISattri = "3#1.0.140.129.0.255#2";
	              FramePara.strDecDataType = "Int32";
	              selectId = 5;
				break;
			case R.id.button6://正向有功总电能（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 03 01 00 01 08 00 FF 02 00 32 68 7E";
				 FramePara.OBISattri = "3#1.0.140.129.0.255#2";
	              FramePara.strDecDataType = "Int32";
	              selectId = 6;
				break;
			case R.id.button7://月冻结电能信息（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 07 00 00 62 01 00 FF 02 00 C0 0C 7E";
				//日冻结
	            FramePara.OBISattri = "7#0.0.98.2.0.255#2";
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
	            selectId = 7;
				break;
			case R.id.button8://日冻结电能信息（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 07 00 00 62 02 00 FF 02 00 0C 11 7E";
				//日冻结
	            FramePara.OBISattri = "7#0.0.98.2.0.255#2";
	            FramePara.strDecDataType = "Struct_Billing";
	            List<TranXADRAssist> temp1 = new ArrayList<TranXADRAssist>();
	            TranXADRAssist item11 = new TranXADRAssist();
	            item11.strName = "DateTime";
	            temp1.add(item11);

	            TranXADRAssist item12 = new TranXADRAssist();
	            item12.strName = "Active energy (+)";
	            item12.unit="kWh";
	            item12.nScaler=0;
	            temp1.add(item12);

	            TranXADRAssist item13 = new TranXADRAssist();
	            item13.strName = "Active energy (-)";
	            item13.unit="kWh";
	            item13.nScaler=0;
	            temp1.add(item13);

	            FramePara.listTranXADRAssist = temp1;
	            selectId = 8;
				break;
			case R.id.button9://月冻结预付费信息（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 07 00 00 63 01 00 FF 02 00 EB 08 7E";
				//日冻结
	            FramePara.OBISattri = "7#0.0.98.2.0.255#2";
	            FramePara.strDecDataType = "Struct_Billing";
	            List<TranXADRAssist> temp2 = new ArrayList<TranXADRAssist>();
	            TranXADRAssist item21 = new TranXADRAssist();
	            item21.strName = "DateTime";
	            temp2.add(item21);

	            TranXADRAssist item22 = new TranXADRAssist();
	            item22.strName = "Active energy (+)";
	            item22.unit="kWh";
	            item22.nScaler=0;
	            temp2.add(item22);

	            TranXADRAssist item23 = new TranXADRAssist();
	            item23.strName = "Active energy (-)";
	            item23.unit="kWh";
	            item23.nScaler=0;
	            temp2.add(item23);

	            FramePara.listTranXADRAssist = temp2;
	            selectId = 9;
				break;
			case R.id.button10://日冻结预付费信息（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 07 00 00 63 02 00 FF 02 00 27 15 7E";
				//日冻结
	            FramePara.OBISattri = "7#0.0.98.2.0.255#2";
	            FramePara.strDecDataType = "Struct_Billing";
	            List<TranXADRAssist> temp3 = new ArrayList<TranXADRAssist>();
	            TranXADRAssist item31 = new TranXADRAssist();
	            item31.strName = "DateTime";
	            temp3.add(item31);

	            TranXADRAssist item32 = new TranXADRAssist();
	            item32.strName = "Active energy (+)";
	            item32.unit="kWh";
	            item32.nScaler=0;
	            temp3.add(item32);

	            TranXADRAssist item33 = new TranXADRAssist();
	            item33.strName = "Active energy (-)";
	            item33.unit="kWh";
	            item33.nScaler=0;
	            temp3.add(item33);

	            FramePara.listTranXADRAssist = temp3;
	            selectId = 10;
				break;
			case R.id.button11://月冻结特殊信息包（读）
				commond = "7E A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C0 01 C1 00 07 00 00 62 09 00 FF 02 00 E0 56 7E";
				break;
			case R.id.button12://token OCTS
				commond = "";
				String tokenStr = token.getText().toString().replaceAll(" ", "");
				Pattern p = Pattern.compile("\\d{20}");
				Matcher m = p.matcher(tokenStr);
				if(tokenStr.equals("") || !m.matches()){
					Toast.makeText(DeviceControlActivity.this, "Invalid Token", Toast.LENGTH_SHORT).show();
					return;
				}
				commond = //"A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C1 01 C1 00 01 01 00 81 81 02 FF 02 00 09 0A" + tokenStr;
						  "A0 28 00 02 FE FF 03 54 D5 74 E6 E6 00 C1 01 C1 00 01 01 00 81 81 02 FF 02 00 09 0A" + tokenStr;
				commond = commond.replaceAll(" ", "").toUpperCase();
				String CRC = CRCUtil.getCRC16(commond);
				if(CRC.equals("") || CRC==null){
					Toast.makeText(DeviceControlActivity.this, "Invalid Token", Toast.LENGTH_SHORT).show();
					return;
				}
				commond = ("7E" + commond + CRC + "7E").replaceAll(" ", "").toUpperCase();
				
				 FramePara.OBISattri = "3#1.0.140.129.0.255#2";
	              FramePara.strDecDataType = "Octs_string";
	              FramePara.WriteData = tokenStr;
	              selectId = 12;
				break;
			}
			Log.i("命令：", commond);
			new WriteTask().execute(commond);
			
			/*BluetoothGattService nservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_NOTIFY_SERVICE));
			BluetoothGattCharacteristic ncharacteristic= mBluetoothLeService.getCharacteristic(nservice,UUID.fromString(SampleGattAttributes.METER_NOTIFY_CHARACTERISTIC));
			
			if( nservice==null || ncharacteristic==null){
				 Toast.makeText(getApplicationContext(), "获取服务失败", Toast.LENGTH_SHORT).show();
				 return;
			}
			mBluetoothLeService.setCharacteristicNotification(ncharacteristic, true);
			SystemClock.sleep(200);
			
			//通信 接受命令服务
			BluetoothGattService wservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_WRITE_SERVICE));
			BluetoothGattCharacteristic wcharacteristic= mBluetoothLeService.getCharacteristic(wservice,UUID.fromString(SampleGattAttributes.METER_WRITE_CHARACTERISTIC));
			
			//通信 发送通知服务
//			BluetoothGattService nservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_NOTIFY_SERVICE));
//			BluetoothGattCharacteristic ncharacteristic= mBluetoothLeService.getCharacteristic(nservice,UUID.fromString(SampleGattAttributes.METER_NOTIFY_CHARACTERISTIC));
			
			if(wservice==null || wservice==null ){
				 Toast.makeText(getApplicationContext(), "获取服务失败", Toast.LENGTH_SHORT).show();
			}
//			mBluetoothLeService.setCharacteristicNotification(ncharacteristic, true);
//			SystemClock.sleep(150);
			
//			Toast.makeText(getApplicationContext(), "命令:"+commond, Toast.LENGTH_SHORT).show();
			commond = commond.replaceAll(" ", "");
			
			
			if(commond.equals("")){
				Toast.makeText(getApplicationContext(), "命令发送失败", Toast.LENGTH_SHORT).show();
			}else{
				int start = 0;
				int len = 40;
				String temp = "";
				while(commond.length() > len){
					temp = commond.substring(start, start+len);
					wcharacteristic.setValue(hexStringToByte(temp));
		        	mBluetoothLeService.writeCharacteristic(wcharacteristic);//写命令到设备，
		        	commond = commond.substring(start+len);
		        	SystemClock.sleep(150);
				}
				wcharacteristic.setValue(hexStringToByte(commond));
	        	mBluetoothLeService.writeCharacteristic(wcharacteristic);//写命令到设备，
				
			}*/
			
			
		}
	};
	CustomProgressDialog progressDialog;
	Context mActivity = DeviceControlActivity.this;
	private void progress_show() {

        progressDialog = CustomProgressDialog.createProgressDialog(
                DeviceControlActivity.this, 20000,
                new CustomProgressDialog.OnTimeOutListener() {

                    @Override
                    public void onTimeOut(CustomProgressDialog dialog) {
                        Toast.makeText(mActivity,"TimeOut",
                                Toast.LENGTH_LONG).show();
                        if (dialog != null
                                && (!((Activity) mActivity).isFinishing())) {
                            dialog.dismiss();
                            dialog = null;
                        }

                    }
                }
        );

        progressDialog.setTitle("");
        progressDialog.setMessage("Loading...");
        // 设置进度条是否不明确
//        progressDialog.setIndeterminate(false);
        // 是否可以按下退回键取消
        progressDialog.setCancelable(false);
        progressDialog.show();

    }
	
	private class WriteTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {

//            if(progressDialog!=null&&progressDialog.isShowing()){
//                progressDialog.dismiss();
//            }
//            createDialog();
//            progressDialog
//                    .setTitle(getString(R.string.str_dayin));
//            progressDialog.setMessage(getString(R.string.progress_conducting));
////            progressDialog.setIndeterminate(false);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        	progress_show();
        }
        @Override
        protected String doInBackground(String... params) {
        	String commond = params[0];
//			String tokenStr = token.getText().toString().replaceAll(" ", "");
//			Pattern p = Pattern.compile("\\d+");
//			Matcher m = p.matcher(tokenStr);
//			if(tokenStr.equals("") || !m.matches()){
////				Toast.makeText(DeviceControlActivity.this, "请输入合法Token", Toast.LENGTH_SHORT).show();
//				return "01";
//			}
//			commond = "A0 1C 00 02 FE FF 03 32 D9 CB E6 E6 00 C1 01 C1 00 01 01 00 81 81 02 FF 02 00 09 0A" + tokenStr;
//			commond = commond.replaceAll(" ", "").toUpperCase();
//			String CRC = CRCUtil.getCRC16(commond);
//			if(CRC.equals("") || CRC==null){
////				Toast.makeText(DeviceControlActivity.this, "请输入合法Token", Toast.LENGTH_SHORT).show();
//				return "01";
//			}
//			commond = ("7E" + commond + CRC + "7E").replaceAll(" ", "").toUpperCase();
//			
//			 FramePara.OBISattri = "3#1.0.140.129.0.255#2";
//              FramePara.strDecDataType = "OCTS";
//              FramePara.WriteData = tokenStr;
//              selectId = 12;
        	
        	BluetoothGattService nservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_NOTIFY_SERVICE));
			BluetoothGattCharacteristic ncharacteristic= mBluetoothLeService.getCharacteristic(nservice,UUID.fromString(SampleGattAttributes.METER_NOTIFY_CHARACTERISTIC));
			
			if( nservice==null || ncharacteristic==null){
//				 Toast.makeText(getApplicationContext(), "获取服务失败", Toast.LENGTH_SHORT).show();
				return "02";
			}
			mBluetoothLeService.setCharacteristicNotification(ncharacteristic, true);
			SystemClock.sleep(200);
			
			//通信 接受命令服务
			BluetoothGattService wservice = mBluetoothLeService.getService(UUID.fromString(SampleGattAttributes.METER_WRITE_SERVICE));
			BluetoothGattCharacteristic wcharacteristic= mBluetoothLeService.getCharacteristic(wservice,UUID.fromString(SampleGattAttributes.METER_WRITE_CHARACTERISTIC));
			
			if(wservice==null || wservice==null ){
//				 Toast.makeText(getApplicationContext(), "获取服务失败", Toast.LENGTH_SHORT).show();
				return "02";
			}
//			mBluetoothLeService.setCharacteristicNotification(ncharacteristic, true);
//			SystemClock.sleep(150);
			
//			Toast.makeText(getApplicationContext(), "命令:"+commond, Toast.LENGTH_SHORT).show();
			commond = commond.replaceAll(" ", "");
			
			
			if(commond.equals("")){
//				Toast.makeText(getApplicationContext(), "命令发送失败", Toast.LENGTH_SHORT).show();
				return "03";
			}else{
				int start = 0;
				int len = 40;
				String temp = "";
				while(commond.length() > len){
					temp = commond.substring(start, start+len);
					wcharacteristic.setValue(hexStringToByte(temp));
		        	mBluetoothLeService.writeCharacteristic(wcharacteristic);//写命令到设备，
		        	commond = commond.substring(start+len);
		        	SystemClock.sleep(150);
				}
				wcharacteristic.setValue(hexStringToByte(commond));
	        	mBluetoothLeService.writeCharacteristic(wcharacteristic);//写命令到设备，
				
			}
            return "00";
        }
        @Override
        protected void onPostExecute(String result) {
//        	Toast.makeText(DeviceControlActivity.this, "结果"+result, Toast.LENGTH_SHORT).show();
        	if(result.equals("00")){
        		
        	}else if(result.equals("01")){
        		Toast.makeText(DeviceControlActivity.this, "Invalid Token", Toast.LENGTH_SHORT).show();
        	}else if(result.equals("02")){
        		Toast.makeText(getApplicationContext(), "Get BLE services failed", Toast.LENGTH_SHORT).show();
        	}else if(result.equals("03")){
        		Toast.makeText(getApplicationContext(), "send data failed", Toast.LENGTH_SHORT).show();
        	}
        	
        	
        }
    }
	
	private boolean ISDEBUG = true;
	public void alert(String msg){
		if(ISDEBUG){
	    	//AlertDialog.Builder builder;
	    	AlertDialog.Builder  builder = new AlertDialog.Builder (DeviceControlActivity.this);
	   	  builder.setMessage(msg);
	   	  builder.setTitle("Tip");
	   	 builder.setPositiveButton("OK",
	             new DialogInterface.OnClickListener() {
	   	   @Override
	   	   public void onClick(DialogInterface dialog, int which) {
	   		   	dialog.dismiss();
	   	   }
	   	  });
	   	  AlertDialog x = builder.create();
	   	  x.show();
    	}
	}
	
	
}
