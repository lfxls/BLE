package cn.hexing.fdm.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.util.Log;
//import android_serialport_api.SerialPort;

public class PowerSecurityController {

	private static PowerSecurityController pSamCon;

//	private ReceiveListener l;
//	protected SerialPort mSerialPort;
	public OutputStream mOutputStream;
	private InputStream mInputStream;
	protected boolean bWriteCardData = false;
	private File power = new File("/sys/devices/platform/c110sysfs/gpio");
	private File power2 = new File("/sys/devices/platform/tesam/dc_power");
	private static String serialPort_Path = "/dev/ttySAC1"; // 操作的串口地址
	private final String RootPath = "/sys/devices/platform/uhf/";
	private final String dc_power = "dc_power";
	private final String com = "com";
	private final String en = "en";
	
	/**
	 * 串口数据回调接口
	 * 
	 * @ 2015-4-18
	 */
//	public interface ReceiveListener {
//		void onReceive(byte[] data);
//	}
	
	public static PowerSecurityController getInstance() {
		if (pSamCon == null) {
			synchronized (PowerSecurityController.class) {
				if (pSamCon == null) {
					pSamCon = new PowerSecurityController();
				}
			}
		}
		return pSamCon;
	}

	public void writeCommand(byte[] command) {
		try {
			if (mOutputStream != null) {
				System.out.println();
				mOutputStream.write(command);
				mOutputStream.flush();
//				System.out.println(Tools.bytesToHexString(command));
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private synchronized void writeFile(File file, String value) {

		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(value.getBytes());
			outputStream.flush();
			outputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启设备，给设备上电
	 */
	public void openDevice() {
		// TODO Auto-generated method stub
//		closeDevice();
		
		//上电操作
//		uhf_power_up();
		power_up();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	private boolean writeFile(String name, int status) {
		try {
			File file = new File(RootPath + name);
			if (file.exists()) {
				OutputStream out = new FileOutputStream(file);
				out.write((status + "").getBytes());
				out.flush();
				out.close();
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	/**
	 * 超高频上电
	 * 
	 * @return
	 */
	private boolean uhf_power_up() {
		return writeFile(dc_power, 1) && writeFile(com, 1) && writeFile(en, 1);
	}
	
	/**
	 * 超高频下电
	 * 
	 * @return
	 */
	private boolean uhf_power_down() {
		return writeFile(en, 0) && writeFile(com, 0) && writeFile(dc_power, 0);
	}
	
	/**
	 * Psam模块上电
	 */
	private void power_up() {
		writeFile(power, "1");
		writeFile(power2, "1");
	}

	/**
	 * Psam模块下电
	 */
	private void power_down() {
		writeFile(power, "0");
		writeFile(power2, "0");
	}


	/**
	 * 关闭设备，给设备下电
	 */
	public void closeDevice() throws IOException {
		// TODO Auto-generated method stub
		power_down();
		uhf_power_down();
	}
}
