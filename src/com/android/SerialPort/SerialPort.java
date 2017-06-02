package com.android.SerialPort;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
	public FileDescriptor mFd;
	public FileInputStream mFileInputStream;
	public FileOutputStream mFileOutputStream;
	private int dev_num=0;
	private String deviceType;
	
	public SerialPort()
	{
//		String ss = SystemProperties.get("persist.idata.device.code", "");	
//		if (ss.equals("ZH188V100")) {
//			dev_num = 1;
//			deviceType="android-95";
//			
//		} else if (ss.equals("BW189V100")) {
//			dev_num = 2;
//			deviceType="android-95E";
//			
//		} else if (ss.equals("SF186V100")) {
//			dev_num = 3;
//			deviceType="android-70";
//			
//		}
//		else if (ss.equals("KB188V100")||ss.equals("KB172V100")) {
//			dev_num = 4;
//			deviceType="android-95w";
//
//		}else if (ss.equals("SF182V100")) {
//			dev_num = 5;
//			deviceType="android-A2Q";
//			
//		} else {
//			dev_num = 1;
//			deviceType="android-95";
//		}
	}
	
	public FileDescriptor open(String path, int baudrate, int nBits, char nEvent,int nStop) 
	{
		mFd = open(path, baudrate, nBits, nEvent, nStop,0);
		if (mFd == null) {
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
		return mFd;
	}
	
	public String GetDeviceType()
	{
		return deviceType;
	}
	
	public void Close()
	{
		close();
		SetPowerState(0x18);
	}
	
	public InputStream getInputStream(FileDescriptor mfd) 
	{
		return mFileInputStream;
	}
	
	public void SetPowerState(int Powerstate)
	{
		SetIoState(dev_num,Powerstate);
		
	}
	
	public OutputStream getOutputStream(FileDescriptor mfd) 
	{
		return mFileOutputStream;
	}
	
	private native FileDescriptor open(String path, int baudrate,int nBits,	char nVerify,int nStop,int flags);

	public native void close();
	
	private native int SetIoState(int dev_num, int controlcode);
	
	static 
	{
		System.loadLibrary("serialport");
	}

}
