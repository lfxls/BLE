package cn.hexing.fdm.protocol.comm;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.android.SerialPort.SerialPort;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;

/**
 * @Title: 光电通讯类
 * @Description: 光电头与表通讯，300波特率握手再转高波特率
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 * @author 王昌豹
 * @version 1.0
 */
public class CommOpticalSerialPort implements ICommucation {

	FileDescriptor mfd;
	OutputStream mOutputStream;
	InputStream mInputStream;
	SerialPort mSerialPort = new SerialPort();
	boolean m_stop = false;
	int stopBit;
	char parity;
	int dataBit;
	int baudRate;
	String uartpath;
	// 第一字节接收超时时间
	static Timer timer2 = new Timer();
	// 字节与字节之间超时时间
	static Timer timer1 = new Timer();
	static boolean delay_occurZJ = false;
	static boolean delay_occur = false;


	@Override
	public boolean OpenDevice(CommPara cpara) {
		// 停止位
		stopBit = cpara.Sbit;
		// 校验位
		parity = cpara.Pty;
		// 数据位
		dataBit = cpara.DBit;
		// 波特率
		baudRate = cpara.BRate;
		// 串口名
		uartpath = cpara.ComName;

		try {
			mSerialPort = new SerialPort();
			mfd = mSerialPort
					.open(uartpath, baudRate, dataBit, parity, stopBit);
			mOutputStream = mSerialPort.getOutputStream(mfd);
			mInputStream = mSerialPort.getInputStream(mfd);

		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Override
	public boolean Close() {
		try {
			if (mSerialPort != null) {
				mSerialPort.Close();
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] ReceiveByt(int ByteWaitT, int WaitT) {
		byte[] rtnByt = new byte[0];
		byte[] rbuffer = new byte[600];
		byte returnbyte = (byte) 0xff;
		delay_occurZJ = false;
		int size = 0;
		int Index = 0;
		timer2(WaitT);
		byte[] buffer = new byte[2048];
		try {

			while ((delay_occurZJ == false)) {
				if (mInputStream.available() > 0) {
					size = mInputStream.read(buffer);// 刚开始为-1
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
				while ((Index < ((rbuffer[2] & 0xff) + 1) || (returnbyte & 0xff) != 0x7e)
						&& delay_occurZJ == false) {
					if (returnbyte == 0x0a) {
						if ((rbuffer[Index - 1] & 0xff) == 0x8d) {
							Index--;
							break;
						}
					}
					size = mInputStream.read(buffer);// 刚开始为-1
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
			rtnByt = RtnData;
			String res = bytesToHexString(rtnByt);
			System.out.println("Rec:" + res);
			}

		} catch (Exception ex) {
			return null;
		} finally {
			timer2.cancel();
		}

		return rtnByt;
	}

	@Override
	public boolean SendByt(byte[] sndByte) {
		// TODO Auto-generated method stub
		boolean result = false;
		try {
			mOutputStream.write(sndByte, 0, sndByte.length);// write(sndByte);
			result = true;
			String res = bytesToHexString(sndByte);
			System.out.println("Send:" + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void SetBaudRate(int Baudrate) {
		mSerialPort.close();
		baudRate = Baudrate;
		mfd = mSerialPort.open(uartpath, baudRate, dataBit, parity, stopBit);
		mOutputStream = mSerialPort.getOutputStream(mfd);
		mInputStream = mSerialPort.getInputStream(mfd);
	}

	public static void timer2(int time) {
		timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			public void run() {
				delay_occurZJ = true;
			}
		}, time, time);
	}

	public static void timer1(int time) {
		timer1 = new Timer();
		timer1.schedule(new TimerTask() {
			public void run() {
				delay_occur = true;
			}
		}, time, time);
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
	public void setORIData(String ORIData) {
		// TODO Auto-generated method stub
		
	}

	// private char Receive(int delaytime) {
	// boolean receiveflag = false; // 接收成功标志
	// char returnbyte = '\0';
	// byte[] buffer = new byte[2048];
	// timer1(delaytime);
	//
	// delay_occur = false;
	// try {
	// while (delay_occur == false && receiveflag == false) {
	// int reResult = mInputStream.read();
	// int size = mInputStream.read(buffer);
	// if (reResult != -1) {
	// returnbyte = (char) reResult;
	// receiveflag = true;
	// }
	// // }
	// }
	// } catch (Exception ex) {
	// } finally {
	// timer1.cancel();
	// }
	// return returnbyte;
	// }

}
