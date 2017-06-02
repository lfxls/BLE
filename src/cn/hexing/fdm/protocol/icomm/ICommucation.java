package cn.hexing.fdm.protocol.icomm;

import java.io.IOException;

import cn.hexing.fdm.protocol.model.CommPara;


public interface ICommucation {

    // 特殊参数
	public static final Object  Tag=null;

    // 转化波特�?	public static final int CBaudRate=300;
	
	/***
	 * 打开通讯模块
	 * @param cpara
	 * @return
	 */
	boolean OpenDevice(CommPara cpara);

	/***
	 * 关闭串口
	 * @return
	 */
	boolean Close();

	/***
	 * 接受
	 * @param SleepT
	 * @param WaitT
	 * @return
	 */
	byte[] ReceiveByt(int SleepT, int WaitT);

	/***
	 * 发�?
	 * @param sndByte
	 * @return
	 */
	boolean SendByt(byte[] sndByte);

	/***
	 * 设置波特�?	 * @param Baudrate
	 */
	void SetBaudRate(int Baudrate);
	 
	    public void setORIData(String ORIData);
}
