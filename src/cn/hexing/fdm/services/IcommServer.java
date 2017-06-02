package cn.hexing.fdm.services;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.CommPara;
import cn.hexing.fdm.protocol.model.HXFramePara;

public interface IcommServer {
	
	/***
	 * 打开通讯模块
	 * @param cpara
	 * @return
	 */
	ICommucation OpenDevice(CommPara cpara, ICommucation commDevice);
	/***
	 * 关闭通讯模块
	 * @return
	 */
	boolean Close(ICommucation commDevice);
	/**
	 * 读取电表
	 * @return
	 */
	public String Read(HXFramePara paraModel, ICommucation commDevice);
	/**
	 * 设置电表
	 * @param strData
	 * @return
	 */
	public boolean Write(HXFramePara paraModel, ICommucation commDevice);
	/**
	 * 执行电表
	 * @param strData
	 * @return
	 */
	public boolean Action(HXFramePara paraModel, ICommucation commDevice);
}
