package cn.hexing.fdm.test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RS232Controller {
	
	//HT380A尾部光电通讯上电操作
	private File power = new File("/sys/devices/platform/uhf/rs232");

	public final static String TAG = "RS232Controller";
	//private static String serialPort_Path = "/dev/ttySAC3"; 
	public static File versionFile = new File("/sys/devices/platform/exynos4412-adc/ver");
	
	private static RS232Controller rs232Con;

	public static RS232Controller getInstance() {
		if (rs232Con == null) {
			rs232Con = new RS232Controller();
		}
		return rs232Con;
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


	private void com(String status) {
		// TODO Auto-generated method stub
		File infCom = new File("/sys/devices/platform/em3095/com");
		writeFile(infCom, status);
	}

	/**
	 * 232上电操作
	 */
	private void power_up() {
		writeFile(power, "1");
	}

	/**
	 * 232下电操作
	 */
	private void power_down() {
		writeFile(power, "0");
	}

	/**
      *  尾部光电上电操作
      *
	 */
	public void Rs232_PowerOn() {
		// TODO Auto-generated method stub
		try {
			com("1");
			power_up();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 尾部光电下电操作
	 */
	public void Rs232_PowerOff() {
		// TODO Auto-generated method stub
		try {
			com("0");
			power_down();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}
