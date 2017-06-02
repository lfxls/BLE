package cn.hexing.fdm.protocol.iprotocol;

import java.io.IOException;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.model.HXFramePara;

public interface IProtocol {

	/***
	 * 读取
	 * @param paraModel
	 * @param commDevice
	 * @return
	 */
	 byte[] Read(HXFramePara paraModel, ICommucation commDevice) throws IOException;
	
	 /***
	  * 设置
	  * @param paraModel
	  * @param commDevice
	  * @return
	  */
	 boolean Write(HXFramePara paraModel, ICommucation commDevice) throws IOException;
	
	 /***
	 * 执行
	 * @param paraModel
	 * @param commDevice
	 * @return
	 */
	 boolean Action(HXFramePara paraModel, ICommucation commDevice) throws IOException;
   
	 /***
     * 断开链路
     * @param commDevice
     * @return
     */
	 boolean DiscFrame(ICommucation commDevice) throws IOException;
}
