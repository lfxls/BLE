package cn.hexing.fdm.protocol.dlms;

import java.io.IOException;
import java.util.ArrayList;

import cn.hexing.fdm.protocol.icomm.ICommucation;
import cn.hexing.fdm.protocol.iprotocol.IProtocol;
import cn.hexing.fdm.protocol.model.HXFramePara;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMode;


/**
 * @Title: DLMS协议类
 * @Description: DLMS协议，读、写、执行电表
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 * @author 王昌豹
 * @version 1.0
 */
public class HXHdlcDLMS implements IProtocol {

	// 发送计数 SSS
	private int Nsend = 0;
	// 接收计数 RRR
	private int Nrec = 0;
	int frameCnt = 0;
	HXHdlcDLMSFrame hdlcframe = new HXHdlcDLMSFrame();
	// 转换的波特率
	private int ToBaudTate = 300;
	private HXFramePara commParaModel;

	/**
	 * DLMS-握手
	 * 
	 * @param paraModel
	 * @param commDevice
	 * @return
	 */
	private boolean Handclasp(HXFramePara paraModel, ICommucation commDevice) {
		try {
			commDevice.SetBaudRate(300);
			byte[] sndByt = null;
			byte[] receiveByt = null;
			boolean isSend = false;
			Nrec = 0;
			Nsend = 0;
			paraModel.Nrec = Nrec;
			paraModel.Nsend = Nsend;
			sndByt = hdlcframe.getHandclaspFrame();
			isSend = commDevice.SendByt(sndByt);
			// 检查是否发送成功，发送成功
			if (!isSend) {
				paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
				return false;
			}
			receiveByt = commDevice.ReceiveByt(paraModel.ByteWaitT,
					paraModel.WaitT);
			if (receiveByt != null && receiveByt.length > 1) {
				if (receiveByt.length > 5) {
					// 发送Z字
					sndByt = hdlcframe.getZFrame(receiveByt[4]);
					isSend = commDevice.SendByt(sndByt);
					Thread.sleep(200);
					if (!isSend) {
						// 返回错误代码，串口打开失败
						paraModel.ErrTxt = "Serial port access denied!";
						return false;
					}
					// 读Z字
					if (receiveByt[0] == 0x55) {
						ToBaudTate = StrToBaud(String
								.valueOf((char) receiveByt[5]));

					} else {
						ToBaudTate = StrToBaud(String
								.valueOf((char) receiveByt[4]));
					}
					commDevice.SetBaudRate(ToBaudTate);
					receiveByt = commDevice.ReceiveByt(paraModel.ByteWaitT,
							paraModel.WaitT);
					if (receiveByt == null || receiveByt.length == 0) {
						paraModel.ErrTxt = "Over time!";
						return false;
					}
				}
			} else {
				paraModel.ErrTxt = "Over time!";
				return false;
			}
		} catch (Exception ex) {
			paraModel.ErrTxt = "Er2:" + ex.getMessage();
			return false;
		}

		return true;
	}

	/**
	 * DLMS-None身份验证
	 * 
	 * @param fpara
	 * @param commDevice
	 * @return
	 */
	private boolean LinkNoAuth(HXFramePara fpara, ICommucation commDevice) throws IOException {
		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		Nrec = 0;
		Nsend = 0;
		fpara.Nrec = Nrec;
		fpara.Nsend = Nsend;
		sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			// 协议提示通讯错误，通讯日志记录各类错误
			fpara.ErrTxt = "DLMS_SNRM_FAILED";
			return false;
		} else {
		}
		receiveByt = commDevice.ReceiveByt(fpara.ByteWaitT, fpara.WaitT);
		if (receiveByt != null) {
			if (CheckFrame(receiveByt, false, fpara) == false) {
				return false;
			}
			GetLinkPara(receiveByt, fpara);
		} else {
			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		sndByt = hdlcframe.getNoAuthAARQFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		;
		if (!isSend) {
			fpara.ErrTxt = "DLMS_AARQ_FAILED";
			return false;
		} else {
		}
		Nsend = 1;
		fpara.Nsend = Nsend;
		receiveByt = commDevice.ReceiveByt(fpara.SleepT, fpara.WaitT);
		if (receiveByt != null) {
			if (CheckFrame(receiveByt, true, fpara) == false) {
				fpara.ErrTxt = "DLMS_FORMAT_ERROR";
				return false;
			}
			Nrec = 1;
			fpara.Nrec = 1;
		} else {
			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		return true;
	}

	/**
	 * DLMS-LLS身份认证
	 * 
	 * @param fpara
	 * @param commDevice
	 * @return
	 */
	private boolean LinkLLSAuth(HXFramePara fpara, ICommucation commDevice) throws IOException {
		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		Nrec = 0;
		Nsend = 0;
		sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			fpara.ErrTxt = "DLMS_SNRM_FAILED";
			return false;
		}
		receiveByt = commDevice.ReceiveByt(fpara.ByteWaitT, fpara.WaitT);
		if (receiveByt != null) {
			if (CheckFrame(receiveByt, false, fpara) == false) {
				return false;
			}
			GetLinkPara(receiveByt, fpara);

		} else {
			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		sndByt = hdlcframe.getLLSAuthAARQFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			fpara.ErrTxt = "DLMS_AARQ_FAILED";
			return false;
		}
		Nsend = 1;
		fpara.Nsend = Nsend;
		receiveByt = commDevice.ReceiveByt(fpara.ByteWaitT, fpara.WaitT);
		if (receiveByt != null) {
			if (CheckFrame(receiveByt, true, fpara) == false) {
				return false;
			}
			Nrec = 1;
			fpara.Nrec = Nrec;

		} else {

			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		return true;
	}

	/**
	 * DLMS-HLS身份认证
	 * 
	 * @param fpara
	 * @param commDevice
	 * @return
	 */
	private boolean LinkHLSAuth(HXFramePara fpara, ICommucation commDevice) throws IOException {
		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		Nrec = 0;
		Nsend = 0;
		sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			fpara.ErrTxt = "DLMS_SNRM_FAILED";
			return false;
		}
		receiveByt = commDevice.ReceiveByt(fpara.ByteWaitT, fpara.WaitT);
		if (receiveByt != null && receiveByt.length > 0) {
			if (CheckFrame(receiveByt, false, fpara) == false) {
				return false;
			}
			GetLinkPara(receiveByt, fpara);
		} else {
			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		frameCnt = 1;
		fpara.frameCnt = frameCnt;
		sndByt = hdlcframe.getHLSAuthAARQFrame(fpara);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			fpara.ErrTxt = "DLMS_AARQ_FAILED";
			return false;
		}
		Nsend = 1;
		fpara.Nsend = 1;
		receiveByt = commDevice.ReceiveByt(fpara.ByteWaitT, fpara.WaitT);
		if (receiveByt != null && receiveByt.length > 0) {
			if (CheckFrame(receiveByt, true, fpara) == false) {
				return false;
			}
			Nrec = 1;
			fpara.Nrec = Nrec;
			// 提取随机数，对其加密
			fpara.frameCnt = frameCnt;
			HXFramePara paraModelCopy = new HXFramePara();
			copyPara(paraModelCopy, fpara);
			paraModelCopy.OBISattri = "000F0000280000FF01";
			paraModelCopy.FirstFrame = false;
			String EncodeStr = hdlcframe.getStoc(paraModelCopy, receiveByt);
			paraModelCopy.WriteData = EncodeStr;
			if (Action(paraModelCopy, commDevice) != true) {
				return false;
			}
		} else {

			fpara.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		return true;
	}

	@Override
	public byte[] Read(HXFramePara paraModel, ICommucation commDevice) throws IOException {
//		//光电只握手一次，帧序号，不可清零
//		if (paraModel.CommDeviceType=="Optical")
//		{
//
//		}
//		else {
//			frameCnt = 0;
//			Nsend = 0;
//			Nrec = 0;
//		}
		commParaModel = paraModel;
		ArrayList<Byte> rtnReceiveByt = new ArrayList<Byte>();
		paraModel.Nsend = Nsend;
		paraModel.Nrec = Nrec;
		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		if (paraModel.FirstFrame) {
			if (paraModel.CommDeviceType == "Optical") {
				if (!Handclasp(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return null;
				}
			}
			if (paraModel.Mode == AuthMode.NONE) {
				if (!LinkNoAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return null;
				}
			} else if (paraModel.Mode == AuthMode.LLS) {
				if (!LinkLLSAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return null;
				}
			} else if (paraModel.Mode == AuthMode.HLS) {
				if (!LinkHLSAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return null;
				}

			}
		}
			frameCnt++;
			paraModel.frameCnt = frameCnt;
			paraModel.Nrec = Nrec;
			paraModel.Nsend = Nsend;
			sndByt = hdlcframe.getReadRequestNormalFrame(paraModel);
			isSend = commDevice.SendByt(sndByt);
			if (!isSend) {
				paraModel.ErrTxt = "DLMS_NORMAL_FAILED";
				return null;
			}
			Nsend++;
			paraModel.Nsend = Nsend;
			receiveByt = commDevice.ReceiveByt(paraModel.SleepT,
					paraModel.WaitT);
			if (receiveByt == null || receiveByt.length == 0
					|| !CheckFrame(receiveByt)) {

				if (receiveByt != null && receiveByt.length > 5) {
				}
			}
			if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
			{
				receiveByt = hdlcframe.GetOriginalData(receiveByt, paraModel);
			}
			if ((receiveByt[10 + paraModel.DestAddr.length] & 0xff) == 0xd8) {
				paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
				return null;
			}
			if ((receiveByt[13 + paraModel.DestAddr.length] & 0xff) != 0x00) {
				paraModel.ErrTxt = AccessResult(receiveByt[14 + paraModel.DestAddr.length]);
				return null;
			}
			Nrec++;
			paraModel.Nrec = Nrec;
			if ((receiveByt[11 + paraModel.DestAddr.length] & 0xff) == 0x01) {//一个数据块
				for (int i = 14 + paraModel.DestAddr.length; i < receiveByt.length - 3; i++) {
					rtnReceiveByt.add(receiveByt[i]);
				}
			}// 01010203090c07d00102ff0000000080000106000000000600000000
			if ((receiveByt[11 + paraModel.DestAddr.length] & 0xff) >= 0x02) {//两个
				boolean IsLastFrame = false;
				int BlockNum = 1;
				paraModel.BlockNum = BlockNum;
				if ((receiveByt[19 + paraModel.DestAddr.length] & 0xff) >= 0x80) {
					for (int i = 21 + paraModel.DestAddr.length; i < receiveByt[20 + paraModel.DestAddr.length]
							+ 21 + paraModel.DestAddr.length; i++) {
						rtnReceiveByt.add(receiveByt[i]);
					}
				} else {
					for (int i = 20 + paraModel.DestAddr.length; i < receiveByt.length - 3; i++) {
						rtnReceiveByt.add(receiveByt[i]);
					}
				}
				IsLastFrame = true;
				while (IsLastFrame == false) {
					frameCnt++;
					IsLastFrame = ((receiveByt[13 + paraModel.DestAddr.length] & 0xff) == 0x00) ? false
							: true;
					if ((receiveByt[13 + paraModel.DestAddr.length] & 0xff) == 0x00) {
						;
					}
					paraModel.frameCnt = frameCnt;
					paraModel.Nsend = Nsend;
					paraModel.Nrec = Nrec;
					sndByt = hdlcframe.getReadRequestBlockFrame(paraModel);
					isSend = commDevice.SendByt(sndByt);
					if (!isSend) {
						paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
						return null;
					} else {
					}
					Nsend++;
					paraModel.Nsend = Nsend;
					receiveByt = commDevice.ReceiveByt(paraModel.SleepT,
							paraModel.WaitT);
					if (receiveByt != null) {
						// 接受校验CRC
						if (!CheckFrame(receiveByt)) {
							paraModel.ErrTxt = "DLMS_FRAME_ERROR";
							return null;
						}
						Nrec++;
						paraModel.Nsend = Nrec;

						if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
						{
							receiveByt = hdlcframe.GetOriginalData(receiveByt,
									paraModel);

						}
						if (receiveByt.length > 19 + paraModel.DestAddr.length
								&& receiveByt[19 + paraModel.DestAddr.length] >= 0x80) {
							for (int i = 21 + paraModel.DestAddr.length; i < receiveByt[20 + paraModel.DestAddr.length]
									+ 21 + paraModel.DestAddr.length; i++) {
								rtnReceiveByt.add(receiveByt[i]);
							}
						} else {
							for (int i = 20 + paraModel.DestAddr.length; i < receiveByt.length - 3; i++) {
								rtnReceiveByt.add(receiveByt[i]);
							}
						}
						if (receiveByt[13 + paraModel.DestAddr.length] == 0x00) {
							;
						}
						IsLastFrame = (receiveByt[13 + paraModel.DestAddr.length] == 0x00) ? false
								: true;
						BlockNum = (int) (receiveByt[14 + paraModel.DestAddr.length]
								* 0x100
								* 0x100
								* 0x100
								+ receiveByt[15 + paraModel.DestAddr.length]
								* 0x100
								* 0x100
								+ receiveByt[16 + paraModel.DestAddr.length]
								* 0x100 + receiveByt[17 + paraModel.DestAddr.length]);
						paraModel.BlockNum = BlockNum;
					} else {
						paraModel.ErrTxt = "DLMS_OVER_TIME";
						return null;
					}

				}
			}
		byte[] bytResult =new byte[rtnReceiveByt.size()];
		for(int i=0;i<bytResult.length;i++)
		{
			bytResult[i]= rtnReceiveByt.get(i);
		}
		return bytResult;
	}

	@Override
	public boolean Write(HXFramePara paraModel, ICommucation commDevice) throws IOException {

		commParaModel = paraModel;//
		paraModel.Nsend = Nsend;
		paraModel.Nrec = Nrec;
		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		if (paraModel.FirstFrame) {
			// 光电必须发“/?!”握手
			if (paraModel.CommDeviceType == "Optical") {
				if (!Handclasp(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			}
			if (paraModel.Mode == AuthMode.NONE) {
				if (!LinkNoAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			}
			if (paraModel.Mode == AuthMode.LLS) {
				if (!LinkLLSAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			} else if (paraModel.Mode == AuthMode.HLS) {
				if (!LinkHLSAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			}
		}
		if ((paraModel.WriteData.length() / 2 + 18) < paraModel.MaxSendInfo_Value) {
			frameCnt++;
			paraModel.frameCnt = frameCnt;
			paraModel.Nsend = Nsend;
			paraModel.Nrec = Nrec;
			sndByt = hdlcframe.getWriteRequestNormalFrame(paraModel);
			isSend = commDevice.SendByt(sndByt);
			if (!isSend) {
				paraModel.ErrTxt = "DLMS_WRITE_FAILED";
				return false;
			}
			Nsend++;
			paraModel.Nsend = Nsend;
			receiveByt = commDevice.ReceiveByt(paraModel.ByteWaitT,
					paraModel.WaitT);
			if (receiveByt == null || receiveByt.length == 0
					|| !CheckFrame(receiveByt)) {
			}
			// 如果有加密或认证，则将收到数据还原
			if (paraModel.enLevel != 0x00) {
				receiveByt = hdlcframe.GetOriginalData(receiveByt, paraModel);
			}
			if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
				paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
				return false;
			}
			if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
				paraModel.ErrTxt = AccessResult(receiveByt[14 + paraModel.DestAddr.length]);
				return false;
			}
			else
			{
				//Token 设置有返回值，暂时这样处理，后面需要修改Write 函数
				if(receiveByt.length>18)
				{
					paraModel.WriteTokenResult =String
							.format("%02x",receiveByt[18]);
				}
			}
			Nrec++;
			paraModel.Nrec = Nrec;
			return true;
		} else {
			// #region set.request_block
			int BlockNum = 1;
			while (paraModel.WriteData.length() > 0) {
				frameCnt++;
				paraModel.frameCnt = frameCnt;
				paraModel.BlockNum = BlockNum;
				paraModel.Nrec = Nrec;
				paraModel.Nsend = Nsend;
				sndByt = hdlcframe.getWriteReuqestBlockFrame(paraModel);
				isSend = commDevice.SendByt(sndByt);
				;
				if (!isSend) {
					paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
					return false;
				}
				Nsend++;
				paraModel.Nsend = Nsend;
				if (BlockNum == 0xff) {
				}
				BlockNum++;
				receiveByt = commDevice.ReceiveByt(paraModel.ByteWaitT,
						paraModel.WaitT);

				if (receiveByt != null) {
					Nrec++;
					paraModel.Nrec = Nrec;
				} else {
					paraModel.ErrTxt = "DLMS_OVER_TIME";
					return false;
				}
				// 如果有加密或认证，则将收到数据还原
				if (paraModel.enLevel != 0x00) {
					receiveByt = hdlcframe.GetOriginalData(receiveByt,
							paraModel);

				}
				if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
					paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
					return false;
				}
				if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
					paraModel.ErrTxt = AccessResult(receiveByt[14 + paraModel.DestAddr.length]);
					return false;
				}
			}
			return true;
		}
	}


	@Override
	public boolean Action(HXFramePara paraModel, ICommucation commDevice) throws IOException {
		commParaModel = paraModel;//
		paraModel.Nsend = Nsend;
		paraModel.Nrec = Nrec;

		byte[] sndByt = null;
		byte[] receiveByt = null;
		boolean isSend = false;
		if (paraModel.FirstFrame) {
			if (paraModel.CommDeviceType == "Optical") {
				if (!Handclasp(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			}
			if (paraModel.Mode == AuthMode.NONE) {
				if (!LinkNoAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			} else if (paraModel.Mode == AuthMode.HLS) {
				if (!LinkHLSAuth(paraModel, commDevice)) {
					paraModel.ErrTxt = "DLMS_AUTH_FAILED";
					return false;
				}
			}
		}
		// action.request_normal
		frameCnt++;
		paraModel.frameCnt = frameCnt;
		paraModel.Nsend = Nsend;
		paraModel.Nrec = Nrec;
		sndByt = hdlcframe.getActionRequestNormalFrame(paraModel);
		isSend = commDevice.SendByt(sndByt);
		if (!isSend) {
			paraModel.ErrTxt = "DLMS_ACTION_FAILED";
			return false;
		}
		Nsend++;
		paraModel.Nsend = Nsend;
		receiveByt = commDevice
				.ReceiveByt(paraModel.ByteWaitT, paraModel.WaitT);
		if (receiveByt != null) {
			Nrec++;
			paraModel.Nrec = Nrec;
		} else {
			paraModel.ErrTxt = "DLMS_OVER_TIME";
			return false;
		}
		if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
		{
			receiveByt = hdlcframe.GetOriginalData(receiveByt, paraModel);
		}
		if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
			paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
			return false;
		}
		if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
			paraModel.ErrTxt = AccessResult(receiveByt[13 + paraModel.DestAddr.length]);
			return false;
		}
		return true;
	}

	@Override
	public boolean DiscFrame(ICommucation commDevice) throws IOException {
		byte[] Recdata;
		boolean SendSucs = false;
		byte[] TmpArr = hdlcframe.getDISCFrame(commParaModel);
		SendSucs = commDevice.SendByt(TmpArr);
		if (SendSucs) {
			Recdata = commDevice.ReceiveByt(300, 1500);
			if (Recdata == null) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * CRC校验和检验
	 * 
	 * @param receiveByt
	 * @return
	 */
	private boolean CheckFrame(byte[] receiveByt) {
		boolean isOK = false;
		if (receiveByt == null) {
			return isOK;
		}
		if (receiveByt.length > 3) {
			if ((receiveByt[2] & 0xff) == receiveByt.length - 2) {
				isOK = true;
				byte[] FrameEnd = hdlcframe.CRC16(receiveByt, 1,
						receiveByt.length - 4);
				if (FrameEnd[0] != receiveByt[receiveByt.length - 3]
						&& FrameEnd[1] != receiveByt[receiveByt.length - 2]) {
					isOK = false;
				}
			}
		}
		return isOK;
	}

	/**
	 * 帧长度+CRC校验和检验
	 * 
	 * @param CheckArr
	 * @param CheckNrs
	 * @param fpara
	 * @return
	 */
	private boolean CheckFrame(byte[] CheckArr, boolean CheckNrs,
			HXFramePara fpara) {
		// 只检查 长度和HCS
		if (CheckArr.length != (CheckArr[1] & 0xFF & 0x0F) + CheckArr[2] + 2) {
			fpara.ErrTxt = "DLMS_FRAME_NOTVALID";// Message.getCaptionEn("DLMS_FRAME_NOTVALID");
			return false;
		}
		byte[] HCSarr = hdlcframe.CRC16(CheckArr, 1, CheckArr.length - 4);
		if (HCSarr[0] != CheckArr[CheckArr.length - 3]
				|| HCSarr[1] != CheckArr[CheckArr.length - 2]) {
			// Error = "Check out error!";
			// return false;
		}
		if (CheckNrs == true) {
			if (fpara.Nrec >= 8) {
				fpara.Nrec = fpara.Nrec - 8;
			}
			if (fpara.Nsend >= 8) {
				fpara.Nsend = fpara.Nsend - 8;
			}
			byte NsendR = (byte) ((CheckArr[4 + fpara.DestAddr.length] & 0x0E) >> 1);
			byte NrecR = (byte) ((CheckArr[4 + fpara.DestAddr.length] & (byte) 0xE0) >> 5);

			if ((NrecR != fpara.Nsend) || (NsendR != fpara.Nrec)) {
				fpara.ErrTxt = "DLMS_NRNS_ERROR";// Message.getCaptionEn("DLMS_NRNS_ERROR");
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取链路协商后的表计链路层参数
	 * 
	 * @param RecPara
	 * @param fpara
	 */
	private void GetLinkPara(byte[] RecPara, HXFramePara fpara) {
		int MaxSendInfo_ValueCom = 0x80;
		int MaxRecInfo_ValueCom = 0x80;
		int MaxSendWindow_ValueCom = 0x01;
		int MaxRecWindow_ValueCom = 0x01;
		int TmpVal = 0;
		for (int i = 0; i < RecPara.length; i++) {
			if ((RecPara[i] & 0xff) == 0x81 && (RecPara[i + 1] & 0xff) == 0x80
					&& i < RecPara.length - 2)// 找到81 80
			{
				if (RecPara[i + 2] > 0)// 长度是否大于0
				{
					int Length = (byte) (RecPara[i + 2]);
					i = i + 3;
					if ((RecPara[i] & 0xff) == 0x05 && Length > 0) {
						byte[] TmpArr = new byte[RecPara[i + 1]];

						for (int j = 0; j < TmpArr.length; j++) {
							TmpArr[j] = RecPara[j + i + 2];
							TmpVal = TmpVal
									+ (int) ((TmpArr[j] & 0xff) * Math.pow(256,
											TmpArr.length - j - 1));
						}
						MaxSendInfo_ValueCom = TmpVal;
						Length = Length - 1 - TmpArr.length;
						i = i + 2 + TmpArr.length;
					}
					if ((RecPara[i] & 0xff) == 0x06 && Length > 0) {
						byte[] TmpArr = new byte[RecPara[i + 1]];
						TmpVal = 0;
						for (int j = 0; j < TmpArr.length; j++) {
							TmpArr[j] = RecPara[j + i + 2];
							TmpVal = TmpVal
									+ (int) ((TmpArr[j] & 0xff) * Math.pow(256,
											TmpArr.length - j - 1));
						}
						MaxRecInfo_ValueCom = TmpVal;
						Length = Length - 1 - TmpArr.length;
						i = i + 2 + TmpArr.length;
					}
					if ((RecPara[i] & 0xff) == 0x07 && Length > 0) {
						byte[] TmpArr = new byte[RecPara[i + 1]];
						TmpVal = 0;
						for (int j = 0; j < TmpArr.length; j++) {
							TmpArr[j] = RecPara[j + i + 2];
							TmpVal = TmpVal
									+ (int) ((TmpArr[j] & 0xff) * Math.pow(256,
											TmpArr.length - j - 1));
						}
						MaxSendWindow_ValueCom = TmpVal;
						Length = Length - 1 - TmpArr.length;
						i = i + 2 + TmpArr.length;
					}
					// window size_receive
					if ((RecPara[i] & 0xff) == 0x08 && Length > 0) {
						byte[] TmpArr = new byte[RecPara[i + 1]];
						TmpVal = 0;
						for (int j = 0; j < TmpArr.length; j++) {
							TmpArr[j] = RecPara[j + i + 2];
							TmpVal = TmpVal
									+ (int) ((TmpArr[j] & 0xff) * Math.pow(256,
											TmpArr.length - j - 1));
						}
						MaxRecWindow_ValueCom = TmpVal;
						Length = Length - 1 - TmpArr.length;
						i = i + 2 + TmpArr.length;
					}
				}
				break;
			}
		}
		fpara.MaxSendInfo_Value = Math.min(fpara.MaxSendInfo_Value,
				MaxRecInfo_ValueCom);
		fpara.MaxRecInfo_Value = Math.min(fpara.MaxRecInfo_Value,
				MaxSendInfo_ValueCom);
		fpara.MaxSendWindow_Value = Math.min(fpara.MaxSendWindow_Value,
				MaxRecWindow_ValueCom);
		fpara.MaxRecWindow_Value = Math.min(fpara.MaxRecWindow_Value,
				MaxSendWindow_ValueCom);
	}

	/**
	 * 解析波特率
	 * 
	 * @param Baudrates
	 *            波特率特征字
	 * @return
	 */
	private int StrToBaud(String Baudrates) {
		int Bdrate = 300;
		char[] caBaudrates = Baudrates.toCharArray();
		char baud = caBaudrates[0];
		int tmpInt = (int) ((char) (int) baud & 0x7f);
		int iBaudrates = tmpInt - 48;
		switch (iBaudrates) {
		case 0:
			Bdrate = 300;
			break;
		case 1:
			Bdrate = 600;
			break;
		case 2:
			Bdrate = 1200;
			break;
		case 3:
			Bdrate = 2400;
			break;
		case 4:
			Bdrate = 4800;
			break;
		case 5:
			Bdrate = 9600;
			break;
		case 6:
			Bdrate = 19200;
			break;
		default:
			break;
		}
		return Bdrate;
	}

	/**
	 * 协议参数拷贝
	 * 
	 * @param pdts
	 * @param ps
	 */
	private void copyPara(HXFramePara pdts, HXFramePara ps) {
		pdts.aesKey = ps.aesKey;
		pdts.auKey = ps.auKey;
		pdts.BlockNum = ps.BlockNum;
		pdts.DestAddr = ps.DestAddr;
		pdts.encryptionMethod = ps.encryptionMethod;
		pdts.enKey = ps.enKey;
		pdts.enLevel = ps.enLevel;
		pdts.ErrTxt = ps.ErrTxt;
		pdts.FirstFrame = ps.FirstFrame;
		pdts.frameCnt = ps.frameCnt;
		pdts.FrameShowTxt = ps.FrameShowTxt;
		pdts.MaxRecInfo_Value = ps.MaxRecInfo_Value;
		pdts.MaxRecWindow_Value = ps.MaxRecWindow_Value;
		pdts.MaxSendInfo_Value = ps.MaxSendInfo_Value;
		pdts.MaxSendWindow_Value = ps.MaxSendWindow_Value;
		pdts.MeterNo = ps.MeterNo;
		pdts.Mode = ps.Mode;
		pdts.Nrec = ps.Nrec;
		pdts.Nsend = ps.Nsend;
		pdts.OBISattri = ps.OBISattri;
		pdts.Pwd = ps.Pwd;
		pdts.RecData = ps.RecData;
		pdts.ByteWaitT = ps.ByteWaitT;
		pdts.SourceAddr = ps.SourceAddr;
		pdts.sysTitleC = ps.sysTitleC;
		pdts.sysTitleS = ps.sysTitleS;
		pdts.WaitT = ps.WaitT;
		pdts.WriteData = ps.WriteData;
	}

	/**
	 * 协议回复异常解析提示
	 * 
	 * @param FaultCode
	 * @return
	 */
	private String AccessResult(byte FaultCode) {
		int FaultStr = (int) (FaultCode);
		String RtnStr = "";
		switch (FaultStr) {
		case 1:
			RtnStr = "DLMS_HARDWARE_FAULT";
			break;
		case 2:
			RtnStr = "DLMS_TEMP_FAILURE";
			break;
		case 3:
			RtnStr = "DLMS_RW_DENIED";
			break;
		case 4:
			RtnStr = "DLMS_OBJECT_UNDEF";
			break;
		case 9:
			RtnStr = "DLMS_OBJECT_INCON";
			break;
		case 11:
			RtnStr = "DLMS_OBJECT_UNAVAILABLE";
			break;
		case 12:
			RtnStr = "DLMS_TYPE_UNMATCHED";
			break;
		case 13:
			RtnStr = "DLMS_ACCESS_VIOLATED";
			break;
		case 14:
			RtnStr = "DLMS_DATA_UNAVAILABLE";
			break;
		case 15:
			RtnStr = "DLMS_LONGGET_ABORTED";
			break;
		case 16:
			RtnStr = "DLMS_NOLONG_GET";
			break;
		case 17:
			RtnStr = "DLMS_LONGSET_ABORTED";
			break;
		case 18:
			RtnStr = "DLMS_NOLONG_SET";
			break;
		default:
			RtnStr = "DLMS_DATA_DENIED";
			break;
		}
		return RtnStr;
	}

	/**
	 * 协议参数Get方法
	 * 
	 * @return
	 */
	public HXFramePara getCommParaModel() {
		return commParaModel;
	}

	/**
	 * 协议参数设置方法
	 * 
	 * @param commParaModel
	 */
	public void setCommParaModel(HXFramePara commParaModel) {
		this.commParaModel = commParaModel;
	}
}
