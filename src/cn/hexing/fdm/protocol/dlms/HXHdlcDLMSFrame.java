package cn.hexing.fdm.protocol.dlms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import cn.hexing.fdm.protocol.model.HXFramePara;
import cn.hexing.fdm.protocol.model.HXFramePara.APDUtype;
import cn.hexing.fdm.protocol.model.HXFramePara.AuthMethod;

/**
 * @Title: DLMS协议帧
 * @Description: DLMS协议的帧函数
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 * @author 王昌豹
 * @version 1.0
 */
public class HXHdlcDLMSFrame {

	private static int[] CRC16Tab = { 0x0000, 0x1189, 0x2312, 0x329b, 0x4624,
			0x57ad, 0x6536, 0x74bf, 0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c,
			0xdbe5, 0xe97e, 0xf8f7, 0x1081, 0x0108, 0x3393, 0x221a, 0x56a5,
			0x472c, 0x75b7, 0x643e, 0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed,
			0xcb64, 0xf9ff, 0xe876, 0x2102, 0x308b, 0x0210, 0x1399, 0x6726,
			0x76af, 0x4434, 0x55bd, 0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e,
			0xfae7, 0xc87c, 0xd9f5, 0x3183, 0x200a, 0x1291, 0x0318, 0x77a7,
			0x662e, 0x54b5, 0x453c, 0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef,
			0xea66, 0xd8fd, 0xc974, 0x4204, 0x538d, 0x6116, 0x709f, 0x0420,
			0x15a9, 0x2732, 0x36bb, 0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868,
			0x99e1, 0xab7a, 0xbaf3, 0x5285, 0x430c, 0x7197, 0x601e, 0x14a1,
			0x0528, 0x37b3, 0x263a, 0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9,
			0x8960, 0xbbfb, 0xaa72, 0x6306, 0x728f, 0x4014, 0x519d, 0x2522,
			0x34ab, 0x0630, 0x17b9, 0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a,
			0xb8e3, 0x8a78, 0x9bf1, 0x7387, 0x620e, 0x5095, 0x411c, 0x35a3,
			0x242a, 0x16b1, 0x0738, 0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb,
			0xa862, 0x9af9, 0x8b70, 0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c,
			0xd3a5, 0xe13e, 0xf0b7, 0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64,
			0x5fed, 0x6d76, 0x7cff, 0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad,
			0xc324, 0xf1bf, 0xe036, 0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5,
			0x4f6c, 0x7df7, 0x6c7e, 0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e,
			0xf2a7, 0xc03c, 0xd1b5, 0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66,
			0x7eef, 0x4c74, 0x5dfd, 0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af,
			0xe226, 0xd0bd, 0xc134, 0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7,
			0x6e6e, 0x5cf5, 0x4d7c, 0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028,
			0x91a1, 0xa33a, 0xb2b3, 0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60,
			0x1de9, 0x2f72, 0x3efb, 0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9,
			0x8120, 0xb3bb, 0xa232, 0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1,
			0x0d68, 0x3ff3, 0x2e7a, 0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a,
			0xb0a3, 0x8238, 0x93b1, 0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62,
			0x3ceb, 0x0e70, 0x1ff9, 0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab,
			0xa022, 0x92b9, 0x8330, 0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3,
			0x2c6a, 0x1ef1, 0x0f78 };

	/**
	 * 前导符号
	 * 
	 * @return
	 */
	public byte[] getUFrame() {
		byte[] TmpArr = new byte[1];
		TmpArr[0] = 0x55;
		return TmpArr;
	}

	/**
	 * 握手帧
	 * 
	 * @return
	 */
	public byte[] getHandclaspFrame() {
		byte[] TmpArr = new byte[5];
		TmpArr[0] = (byte) 0xaf;
		TmpArr[1] = 0x3f;
		TmpArr[2] = 0x21;
		TmpArr[3] = (byte) 0x8d;
		TmpArr[4] = 0x0a;
		return TmpArr;
	}

	/**
	 * 转波特率帧
	 * 
	 * @param Recdata
	 *            ：握手帧回复的Z字
	 * @return
	 */
	public byte[] getZFrame(byte Recdata) {
		byte[] TmpArr = new byte[6];
		TmpArr[0] = 0x06;// ACK
		TmpArr[1] = (byte) 0xB2;// 0
		TmpArr[2] = Recdata;// Z字
		TmpArr[3] = (byte) 0xB2;// 1编程
		TmpArr[4] = (byte) 0x8d;
		TmpArr[5] = 0x0a;
		return TmpArr;
	}

	/**
	 * 物理链接SNRM帧
	 * 
	 * @param fpara
	 *            ：协议相关的参数（通讯地址）
	 * @return
	 */
	public byte[] getNoAuthSNRMFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = (byte) 0x93;
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0x81;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = 0x14;
		TmpArr[Index++] = 0x05;//
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0xd0;
		TmpArr[Index++] = 0x06;//
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0xd0;
		TmpArr[Index++] = 0x07;//
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = 0x08;//
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x01;
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 无身份认证的AARQ帧
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getNoAuthAARQFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];

		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = 0x10;
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = 0x20;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = (byte) 0xA1;
		TmpArr[Index++] = 0x09;
		TmpArr[Index++] = 0x06;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = (byte) 0x85;
		TmpArr[Index++] = 0x74;
		TmpArr[Index++] = 0x05;
		TmpArr[Index++] = 0x08;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = (byte) 0xBE;
		TmpArr[Index++] = 0x0F;
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x0D;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x06;
		TmpArr[Index++] = 0x5F;
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x19;
		TmpArr[Index++] = (byte) 0xff;
		TmpArr[Index++] = (byte) 0xff;
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;

	}

	/**
	 * 低级身份认证（LLS）的AARQ帧
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getLLSAuthAARQFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = 0x10;
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = 0x39;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = (byte) 0xA1;
		TmpArr[Index++] = 0x09;
		TmpArr[Index++] = 0x06;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = (byte) 0x85;
		TmpArr[Index++] = 0x74;
		TmpArr[Index++] = 0x05;
		TmpArr[Index++] = 0x08;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = (byte) 0x8a;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = (byte) 0x8b;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = (byte) 0x85;
		TmpArr[Index++] = 0x74;
		TmpArr[Index++] = 0x05;
		TmpArr[Index++] = 0x08;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = (byte) 0xac;
		TmpArr[Index++] = 0x0a;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = 0x08;
		byte bPwd[] = fpara.Pwd.getBytes();

		for (int i = 0; i < bPwd.length; i++) {
			TmpArr[Index++] = bPwd[i];

		}
		TmpArr[Index++] = (byte) 0xBE;
		TmpArr[Index++] = 0x0F;
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x0D;
		TmpArr[Index++] = 0x01;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x06;
		TmpArr[Index++] = 0x5F;
		TmpArr[Index++] = 0x04;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x19;
		TmpArr[Index++] = (byte) 0xff;
		TmpArr[Index++] = (byte) 0xff;
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 高级身份认证（HLS）的AARQ帧
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getHLSAuthAARQFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		int aarqLenIndex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		byte[] plaintByte;
		byte[] cByte;
		int indexPlain = 0;
		fpara.frameCnt = 1;
		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = 0x10;
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		TmpArr[Index++] = 0x60;
		aarqLenIndex = Index;
		Index++;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = (byte) 0xA1;
		TmpArr[Index++] = 0x09;
		TmpArr[Index++] = 0x06;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = (byte) 0x85;
		TmpArr[Index++] = 0x74;
		TmpArr[Index++] = 0x05;
		TmpArr[Index++] = 0x08;
		TmpArr[Index++] = 0x01;
		if (fpara.enLevel != 0x00) {
			TmpArr[Index++] = 0x03;// logical name and ciphering
		} else {
			TmpArr[Index++] = 0x01;// logical name no ciphering
		}
		if (fpara.enLevel != 0x00)// 如果需要Authention or
									// Encryption,则要带上systemTitle
		{
			TmpArr[Index++] = (byte) 0xA6;
			TmpArr[Index++] = 0x0a;
			TmpArr[Index++] = 0x04;
			TmpArr[Index++] = 0x08;
			for (int i = 0; i < 8; i++) {
				TmpArr[Index++] = fpara.sysTitleC[i];
			}
		}
		TmpArr[Index++] = (byte) 0x8a;
		TmpArr[Index++] = 0x02;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = (byte) 0x80;
		TmpArr[Index++] = (byte) 0x8b;
		TmpArr[Index++] = 0x07;
		TmpArr[Index++] = 0x60;
		TmpArr[Index++] = (byte) 0x85;
		TmpArr[Index++] = 0x74;
		TmpArr[Index++] = 0x05;
		TmpArr[Index++] = 0x08;
		TmpArr[Index++] = 0x02;
		if (fpara.enLevel != 0x00) {
			if (fpara.encryptionMethod == AuthMethod.AES_GCM_128) {
				TmpArr[Index++] = 0x05;// AES-GCM-128,带8个字节随机数
			} else if (fpara.encryptionMethod == AuthMethod.MD5) {
				TmpArr[Index++] = 0x03;
			}
			TmpArr[Index++] = (byte) 0xac;
			TmpArr[Index++] = 0x0a;
			TmpArr[Index++] = (byte) 0x80;
			TmpArr[Index++] = 0x08;

			byte[] BytMyradom = new byte[8];
			Random random = new Random();
			random.nextBytes(BytMyradom);

			for (int i = 0; i < 8; i++) {
				TmpArr[Index++] = BytMyradom[i];
			}
		} else {// AES-EBC-128，带16个字节随机数
			TmpArr[Index++] = 0x02;
			TmpArr[Index++] = (byte) 0xac;
			TmpArr[Index++] = 0x12;
			TmpArr[Index++] = (byte) 0x80;
			TmpArr[Index++] = 0x10;

			byte[] BytMyradom = new byte[8];
			Random random = new Random();
			random.nextBytes(BytMyradom);

			for (int i = 0; i < 8; i++) {
				TmpArr[Index++] = BytMyradom[i];
			}
			byte[] BytMyradom1 = new byte[8];
			random = new Random();
			random.nextBytes(BytMyradom1);

			for (int i = 0; i < 8; i++) {
				TmpArr[Index++] = BytMyradom1[i];
			}
		}

		// 组织明文
		plaintByte = new byte[13];
		indexPlain = 0;
		plaintByte[indexPlain++] = 0x01;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x06;
		plaintByte[indexPlain++] = 0x5F;
		plaintByte[indexPlain++] = 0x04;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x00;
		plaintByte[indexPlain++] = 0x19;
		plaintByte[indexPlain++] = (byte) 0xff;
		plaintByte[indexPlain++] = (byte) 0xff;
		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_initiateRequest, fpara);
			TmpArr[Index++] = (byte) 0xBE;// initiate.req
			TmpArr[Index++] = (byte) (cByte.length + 2);
			TmpArr[Index++] = 0x04;
			TmpArr[Index++] = (byte) (cByte.length);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			TmpArr[Index++] = (byte) 0xBE;// initiate.req
			TmpArr[Index++] = (byte) (indexPlain + 2);
			TmpArr[Index++] = 0x04;
			TmpArr[Index++] = (byte) (indexPlain);
			for (int i = 0; i < indexPlain; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		TmpArr[2] = (byte) (Index + 1);
		TmpArr[aarqLenIndex] = (byte) (Index - aarqLenIndex - 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 应用层数据单元的（APDU）加密或验证帧
	 * 
	 * @param plaintByte
	 * @param operType
	 * @param fpara
	 * @return
	 */
	private byte[] BuildAPDU(byte[] plaintByte, APDUtype operType,
			HXFramePara fpara) {
		byte[] rtnBytes;
		byte[] TmpArr = new byte[400];
		int Index = 0;
		byte apduTAG = 0x00;
		byte[] iv = new byte[12];
		byte[] addinfo;
		byte[] tag = new byte[12];
		byte[] msg;
		switch (operType) {
		case glo_initiateRequest:
			apduTAG = 0x21;
			break;
		case glo_initiateResponse:
			apduTAG = 0x28;
			break;
		case glo_get_request:
			apduTAG = (byte) 0xC8;
			break;
		case glo_set_request:
			apduTAG = (byte) 0xC9;
			break;
		case glo_events_notification_request:
			apduTAG = (byte) 0xCA;
			break;
		case glo_action_request:
			apduTAG = (byte) 0xCB;
			break;
		case glo_get_response:
			apduTAG = (byte) 0xCC;
			break;
		case glo_set_response:
			apduTAG = (byte) 0xCD;
			break;
		case glo_action_response:
			apduTAG = (byte) 0xCE;
			break;
		}
		if (fpara.enLevel == 0x00) {
			return plaintByte;
		} else {

			if (fpara.enLevel == 0x10)// TAG||LEN||SH||APDU||T
			{
				TmpArr[Index++] = apduTAG;
				if (5 + plaintByte.length + 12 > 0x7f) {
					TmpArr[Index++] = (byte) 0x81;
					TmpArr[Index++] = (byte) (5 + plaintByte.length + 12);
				} else {
					TmpArr[Index++] = (byte) (5 + plaintByte.length + 12);
				}
				TmpArr[Index++] = fpara.enLevel;
				TmpArr[Index++] = (byte) (fpara.frameCnt / 0x1000000);
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x100));
				for (int i = 0; i < 8; i++)// 给IV前8个字节赋值
				{
					iv[i] = fpara.sysTitleC[i];
				}
				iv[8] = (byte) (fpara.frameCnt / 0x1000000);
				iv[9] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				iv[10] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				iv[11] = (byte) (fpara.frameCnt % (0x100));
				msg = new byte[0];
				addinfo = new byte[17 + plaintByte.length];
				addinfo[0] = fpara.enLevel;
				for (int i = 0; i < 16; i++) {
					addinfo[i + 1] = fpara.auKey[i];
				}
				for (int i = 0; i < plaintByte.length; i++) {
					addinfo[17 + i] = plaintByte[i];
				}
				byte[] tmpEncryBytes = gcm_encrypt_message(fpara.enKey,
						(byte) fpara.enKey.length, iv, (byte) iv.length,
						addinfo, (byte) addinfo.length, msg, tag);
				msg = new byte[tmpEncryBytes.length - 12];
				System.arraycopy(tmpEncryBytes, 0, msg, 0, msg.length);
				tag = new byte[12];
				System.arraycopy(tmpEncryBytes, msg.length, tag, 0, tag.length);

				for (int i = 0; i < plaintByte.length; i++) {
					TmpArr[Index++] = plaintByte[i];
				}
				for (int i = 0; i < tag.length; i++) {
					TmpArr[Index++] = tag[i];
				}
			}
			if (fpara.enLevel == 0x20)// TAG||LEN||SH||C
			{
				TmpArr[Index++] = apduTAG;
				if (5 + plaintByte.length > 0x7f) {
					TmpArr[Index++] = (byte) 0x81;
					TmpArr[Index++] = (byte) (5 + plaintByte.length);
				} else {
					TmpArr[Index++] = (byte) (5 + plaintByte.length);
				}
				TmpArr[Index++] = fpara.enLevel;
				TmpArr[Index++] = (byte) (fpara.frameCnt / 0x1000000);
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x100));
				for (int i = 0; i < 8; i++)// 给IV前8个字节赋值
				{
					iv[i] = fpara.sysTitleC[i];
				}
				iv[8] = (byte) (fpara.frameCnt / 0x1000000);
				iv[9] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				iv[10] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				iv[11] = (byte) (fpara.frameCnt % (0x100));
				msg = new byte[plaintByte.length];
				for (int i = 0; i < plaintByte.length; i++) {
					msg[i] = plaintByte[i];
				}
				addinfo = new byte[0];
				byte[] tmpEncryBytes = gcm_encrypt_message(fpara.enKey,
						(byte) fpara.enKey.length, iv, (byte) iv.length,
						addinfo, (byte) addinfo.length, msg, tag);
				msg = new byte[tmpEncryBytes.length - 12];
				System.arraycopy(tmpEncryBytes, 0, msg, 0, msg.length);
				tag = new byte[12];
				System.arraycopy(tmpEncryBytes, msg.length, tag, 0, tag.length);

				for (int i = 0; i < msg.length; i++) {
					TmpArr[Index++] = msg[i];
				}
			}
			if (fpara.enLevel == 0x30)// TAG||LEN||SH||C||T
			{
				TmpArr[Index++] = apduTAG;
				if (5 + plaintByte.length + 12 > 0x7f) {
					TmpArr[Index++] = (byte) 0x81;
					TmpArr[Index++] = (byte) (5 + plaintByte.length + 12);
				} else {
					TmpArr[Index++] = (byte) (5 + plaintByte.length + 12);
				}
				TmpArr[Index++] = fpara.enLevel;
				TmpArr[Index++] = (byte) (fpara.frameCnt / 0x1000000);
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				TmpArr[Index++] = (byte) (fpara.frameCnt % (0x100));
				for (int i = 0; i < 8; i++)// 给IV前8个字节赋值
				{
					iv[i] = fpara.sysTitleC[i];
				}
				iv[8] = (byte) (fpara.frameCnt / 0x1000000);
				iv[9] = (byte) (fpara.frameCnt % (0x1000000) / (0x10000));
				iv[10] = (byte) (fpara.frameCnt % (0x10000) / (0x100));
				iv[11] = (byte) (fpara.frameCnt % (0x100));
				msg = new byte[plaintByte.length];
				for (int i = 0; i < plaintByte.length; i++) {
					msg[i] = plaintByte[i];
				}
				addinfo = new byte[17];
				addinfo[0] = fpara.enLevel;
				for (int i = 0; i < 16; i++) {
					addinfo[i + 1] = fpara.auKey[i];
				}
				byte[] tmpEncryBytes = gcm_encrypt_message(fpara.enKey,
						(byte) fpara.enKey.length, iv, (byte) iv.length,
						addinfo, (byte) addinfo.length, msg, tag);
				msg = new byte[tmpEncryBytes.length - 12];
				System.arraycopy(tmpEncryBytes, 0, msg, 0, msg.length);
				tag = new byte[12];
				System.arraycopy(tmpEncryBytes, msg.length, tag, 0, tag.length);
				for (int i = 0; i < plaintByte.length; i++) {
					TmpArr[Index++] = msg[i];
				}
				for (int i = 0; i < tag.length; i++) {
					TmpArr[Index++] = tag[i];
				}
			}
			rtnBytes = new byte[Index];
			for (int i = 0; i < rtnBytes.length; i++) {
				rtnBytes[i] = TmpArr[i];
			}
			return rtnBytes;
		}
	}

	/**
	 * 随机数或者密码加密StoC帧
	 * 
	 * @param fpara
	 * @param RecArr
	 * @return
	 */
	public String getStoc(HXFramePara fpara, byte[] RecArr) {
		byte[] iv = new byte[12];
		byte[] addinfo;
		byte[] tag = new byte[12];
		byte[] msg;
		byte[] RandfromMeter = FindRandNum(RecArr, fpara);
		// 专成int
		int[] RandfromMeterInt = new int[RandfromMeter.length];
		for (int i = 0; i < RandfromMeterInt.length; i++) {
			RandfromMeterInt[i] = RandfromMeter[i] & 0xff;
		}

		if (fpara.enLevel == 0x00) {
			int[] EncodeNum = new int[16];
			Aes.KeySize Mykeysize = Aes.KeySize.Bits128;

			int[] aesKeyInt = new int[fpara.aesKey.length];
			for (int i = 0; i < aesKeyInt.length; i++) {
				aesKeyInt[i] = fpara.aesKey[i] & 0xff;
			}

			Aes MyAes = new Aes(Mykeysize, aesKeyInt);

			EncodeNum = MyAes.Cipher(RandfromMeterInt, EncodeNum);
			String EncodeStr = "0910";
			for (int i = 0; i < 16; i++) {
				if (EncodeNum[i] < 0x10) {
					EncodeStr += "0";
				}
				EncodeStr += Integer.toString(EncodeNum[i], 16);
			}
			return EncodeStr;
		} else {
			if (fpara.encryptionMethod == AuthMethod.AES_GCM_128) {
				// AES-GCM-128 身份认证
				// SC||AK||随机数作为ADD经AES-GCM认证得到结果T,将SC||FC||T作为验证结果发给server
				for (int i = 0; i < 8; i++)// 给IV前8个字节赋值
				{
					iv[i] = fpara.sysTitleC[i];
				}
				iv[8] = (byte) ((fpara.frameCnt + 1) / 0x1000000);
				iv[9] = (byte) ((fpara.frameCnt + 1) % (0x1000000) / (0x10000));
				iv[10] = (byte) ((fpara.frameCnt + 1) % (0x10000) / (0x100));
				iv[11] = (byte) ((fpara.frameCnt + 1) % (0x100));
				msg = new byte[0];
				addinfo = new byte[25];
				addinfo[0] = 0x10;
				for (int i = 0; i < 16; i++) {
					addinfo[i + 1] = fpara.auKey[i];
				}
				for (int i = 0; i < 8; i++) {
					addinfo[i + 17] = RandfromMeter[i];
				}
				byte[] tmpEncryBytes = gcm_encrypt_message(fpara.enKey,
						(byte) fpara.enKey.length, iv, (byte) iv.length,
						addinfo, (byte) addinfo.length, msg, tag);
				msg = new byte[tmpEncryBytes.length - 12];
				System.arraycopy(tmpEncryBytes, 0, msg, 0, msg.length);
				tag = new byte[12];
				System.arraycopy(tmpEncryBytes, msg.length, tag, 0, tag.length);
				String EncodeStr = "0911";
				EncodeStr += "10";// SC
				for (int i = 8; i < 12; i++)// FC
				{
					if ((iv[i] & 0xff) < 0x10) {
						EncodeStr += "0";
					}
					EncodeStr += (Integer.toString((iv[i] & 0xff), 16));
				}
				for (int i = 0; i < 12; i++)// T
				{
					if ((tag[i] & 0xff) < 0x10) {
						EncodeStr += "0";
					}
					EncodeStr += (Integer.toString((tag[i] & 0xff), 16));
				}
				return EncodeStr;

			}
			if (fpara.encryptionMethod == AuthMethod.MD5) {
				// MD5身份验证
				MessageDigest md;
				String EncodeStr = "0910";
				try {
					md = MessageDigest.getInstance("MD5");

					byte[] toHash = new byte[fpara.aesKey.length
							+ RandfromMeter.length];

					for (int i = 0; i < RandfromMeter.length; i++) {
						toHash[i] = RandfromMeter[i];
					}
					for (int i = 0; i < fpara.aesKey.length; i++) {
						toHash[i + RandfromMeter.length] = fpara.aesKey[i];
					}
					byte[] Myresult = md.digest(toHash);
					for (int i = 0; i < 16; i++) {
						if (Myresult[i] < 0x10) {
							EncodeStr += "0";
						}
						EncodeStr += Integer.toString(Myresult[i], 16);
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return EncodeStr;

			}
			return "";
		}
	}

	/**
	 * 正常读
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getReadRequestNormalFrame(HXFramePara fpara) {
		byte[] TmpArr;
		int Index = 0;
		int indexPlain = 0;
 		byte[] tmpPlain = new byte[300];
		byte[] plaintByte;
		byte[] cByte;
		int HCSindex = 0;
		byte[] CheckByte = new byte[2];
		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = IControlWordBuild(fpara);
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		// 组织数据
		indexPlain = 0;
		tmpPlain[indexPlain++] = (byte) 0xC0;
		tmpPlain[indexPlain++] = 0x01;
		tmpPlain[indexPlain++] = (byte) 0xc1;

		if (fpara.WriteData != null && fpara.WriteData != "") {
			if (fpara.WriteData != "")// 读带参数
			{
				fpara.OBISattri = fpara.OBISattri.substring(0,
						fpara.OBISattri.length() - 2);
				fpara.OBISattri += fpara.WriteData;
			}
		}
		for (int i = 0; i < fpara.OBISattri.length() / 2; i++) {
			tmpPlain[indexPlain++] = (byte) Integer.parseInt(
					fpara.OBISattri.substring(i * 2, i * 2 + 2), 16);
		}

		plaintByte = new byte[indexPlain];
		for (int i = 0; i < plaintByte.length; i++) {
			plaintByte[i] = tmpPlain[i];
		}

		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_get_request, fpara);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			for (int i = 0; i < plaintByte.length; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 块读
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getReadRequestBlockFrame(HXFramePara fpara) {
		byte[] TmpArr;
		int Index = 0;
		int indexPlain = 0;
		byte[] tmpPlain = new byte[300];
		byte[] plaintByte;
		byte[] cByte;
		int HCSindex = 0;
		byte[] CheckByte = new byte[2];
		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = IControlWordBuild(fpara);
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		// 组织明文
		indexPlain = 0;
		tmpPlain[indexPlain++] = (byte) 0xC0;
		tmpPlain[indexPlain++] = 0x02;
		tmpPlain[indexPlain++] = (byte) 0xc1;
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum / (256 * 256 * 256));// block_number
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256 * 256 * 256) / (256 * 256));
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256 * 256) / (256));
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256));
		plaintByte = new byte[indexPlain];
		for (int i = 0; i < plaintByte.length; i++) {
			plaintByte[i] = tmpPlain[i];
		}
		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_get_request, fpara);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			for (int i = 0; i < plaintByte.length; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 正常写
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getWriteRequestNormalFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		byte[] tmpPlain = new byte[300];
		byte[] plaintByte;
		int indexPlain = 0;
		byte[] cByte;
		TmpArr = new byte[600];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = IControlWordBuild(fpara);
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		// 组织明文
		indexPlain = 0;
		tmpPlain[indexPlain++] = (byte) 0xC1;
		tmpPlain[indexPlain++] = 0x01;
		tmpPlain[indexPlain++] = (byte) 0xc1;
		for (int i = 0; i < fpara.OBISattri.length() / 2; i++) {
			tmpPlain[indexPlain++] = (byte) Integer.parseInt(
					fpara.OBISattri.substring(i * 2, i * 2 + 2), 16);
		}
		if (fpara.WriteData != null && fpara.WriteData != "") {
			for (int i = 0; i < fpara.WriteData.length() / 2; i++) {
				tmpPlain[indexPlain++] = (byte) Integer.parseInt(
						fpara.WriteData.substring(i * 2, i * 2 + 2), 16);
			}
		}
		plaintByte = new byte[indexPlain];
		for (int i = 0; i < plaintByte.length; i++) {
			plaintByte[i] = tmpPlain[i];
		}
		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_set_request, fpara);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			for (int i = 0; i < plaintByte.length; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/***
	 * 分块写
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getWriteReuqestBlockFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		byte[] tmpPlain = new byte[300];
		byte[] plaintByte;
		int indexPlain = 0;
		byte[] cByte;
		Boolean IsLastBlock = ((fpara.WriteData.trim().length() / 2 + 20) <= fpara.MaxSendInfo_Value) ? true
				: false;
		TmpArr = new byte[300];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = IControlWordBuild(fpara);
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		// 组织明文
		indexPlain = 0;
		tmpPlain[indexPlain++] = (byte) 0xC1;
		if (fpara.BlockNum == 1) {
			tmpPlain[indexPlain++] = 0x02;
		} else {
			tmpPlain[indexPlain++] = 0x03;
		}
		tmpPlain[indexPlain++] = (byte) 0xc1;
		if (fpara.BlockNum == 1) {
			for (int i = 0; i < fpara.OBISattri.length() / 2; i++) {
				tmpPlain[indexPlain++] = (byte) Integer.parseInt(
						fpara.OBISattri.substring(i * 2, i * 2 + 2), 16);
			}
		}
		tmpPlain[indexPlain++] = (byte) ((IsLastBlock == false) ? 0x00 : 0xff);// last
																				// block
																				// or
																				// not
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum / (256 * 256 * 256));// block_number
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256 * 256 * 256) / (256 * 256));
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256 * 256) / (256));
		tmpPlain[indexPlain++] = (byte) (fpara.BlockNum % (256));
		int ForEnd = ((fpara.WriteData.trim().length() / 2 + 20) <= fpara.MaxSendInfo_Value) ? fpara.WriteData
				.trim().length() / 2 : (int) (fpara.MaxSendInfo_Value - 25);
		if (ForEnd >= 0x80)// 数据长度大于128时，数据长度需编码
		{
			tmpPlain[indexPlain++] = (byte) 0x81;
		}
		tmpPlain[indexPlain++] = (byte) ForEnd;// length of raw_data ??
		for (int i = 0; i < ForEnd; i++) {
			tmpPlain[indexPlain++] = (byte) Integer.parseInt(fpara.WriteData
					.trim().substring(i * 2, i * 2 + 2), 16);
		}
		plaintByte = new byte[indexPlain];
		for (int i = 0; i < plaintByte.length; i++) {
			plaintByte[i] = tmpPlain[i];
		}
		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_set_request, fpara);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			for (int i = 0; i < plaintByte.length; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		fpara.WriteData = fpara.WriteData.substring(ForEnd * 2);
		if (Index > 254) {
			TmpArr[1] = (byte) (0xA0 + ((Index + 1) / 256));
			TmpArr[2] = (byte) ((Index + 1) % 256);
		} else {
			TmpArr[2] = (byte) (Index + 1);
		}
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	public byte[] getActionRequestNormalFrame(HXFramePara fpara) {
		int Index = 0;
		int HCSindex = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];
		byte[] tmpPlain = new byte[300];
		byte[] plaintByte;
		int indexPlain = 0;
		byte[] cByte;
		TmpArr = new byte[600];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = IControlWordBuild(fpara);
		HCSindex = Index;
		Index++;
		Index++;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = (byte) 0xE6;
		TmpArr[Index++] = 0x00;
		// #region 组织明文
		indexPlain = 0;
		tmpPlain[indexPlain++] = (byte) 0xC3;
		tmpPlain[indexPlain++] = 0x01;
		tmpPlain[indexPlain++] = (byte) 0xc1;
		for (int i = 0; i < fpara.OBISattri.length() / 2; i++) {
			tmpPlain[indexPlain++] = (byte) Integer.parseInt(
					fpara.OBISattri.substring(i * 2, i * 2 + 2), 16);
		}
		if (fpara.WriteData == null || fpara.WriteData == "") {
			tmpPlain[indexPlain++] = 0x00;// 不带数据
		} else {
			tmpPlain[indexPlain++] = 0x01;// 带数据
			for (int i = 0; i < fpara.WriteData.length() / 2; i++) {
				tmpPlain[indexPlain++] = (byte) Integer.parseInt(
						fpara.WriteData.substring(i * 2, i * 2 + 2), 16);
			}
		}
		plaintByte = new byte[indexPlain];
		for (int i = 0; i < plaintByte.length; i++) {
			plaintByte[i] = tmpPlain[i];
		}
		// #endregion
		if (fpara.enLevel != 0x00) {
			cByte = BuildAPDU(plaintByte, APDUtype.glo_action_request, fpara);
			for (int i = 0; i < cByte.length; i++) {
				TmpArr[Index++] = cByte[i];
			}
		} else {
			for (int i = 0; i < plaintByte.length; i++) {
				TmpArr[Index++] = plaintByte[i];
			}
		}
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, 4 + fpara.DestAddr.length);
		TmpArr[HCSindex] = CheckByte[0];
		TmpArr[HCSindex + 1] = CheckByte[1];
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}

	/**
	 * 计算CRC16数值
	 * 
	 * @param data
	 * @param StartIndex
	 * @param EndIndex
	 * @return
	 */
	public byte[] CRC16(byte[] data, int StartIndex, int EndIndex) {
		int CRCval = 0xFFFF;
		for (int i = StartIndex; i <= EndIndex; i++) {
			int Temp = (int) (CRCval & 0x00ff);
			CRCval = (int) (CRC16Tab[Temp ^ (data[i] & 0xff)] ^ (CRCval >> 8));
		}
		CRCval = (int) (CRCval ^ 0xFFFF);
		int Temp1 = (int) (CRCval & 0x00FF);
		int Temp2 = (int) (CRCval & 0xFF00);
		Temp2 = (int) (Temp2 >> 8);
		byte[] RtnByte = new byte[2];
		RtnByte[0] = (byte) Temp1;
		RtnByte[1] = (byte) Temp2;
		return RtnByte;
	}

	/**
	 * 获取表计回复的随机数
	 * 
	 * @param AARE
	 * @param fpara
	 * @return
	 */
	private byte[] FindRandNum(byte[] AARE, HXFramePara fpara) {
		byte[] RtnByte;
		if (fpara.enLevel == 0x00) {
			RtnByte = new byte[16];
			for (int i = 0; i < AARE.length; i++) {
				if ((AARE[i] & 0xFF) == 0xAA && (AARE[i + 1] & 0xFF) == 0x12
						&& (AARE[i + 2] & 0xFF) == 0x80
						&& (AARE[i + 3] & 0xFF) == 0x10) {
					for (int j = 0; j < 16; j++) {
						RtnByte[j] = AARE[i + 4 + j];
					}
				}
			}
		} else {
			RtnByte = new byte[8];
			for (int i = 0; i < AARE.length; i++) {
				if ((AARE[i] & 0xFF) == 0xAA && (AARE[i + 1] & 0xFF) == 0x0a
						&& (AARE[i + 2] & 0xFF) == 0x80
						&& (AARE[i + 3] & 0xFF) == 0x08) {
					for (int j = 0; j < 8; j++) {
						RtnByte[j] = AARE[i + 4 + j];
					}
				}
			}
			for (int i = 0; i < AARE.length; i++) {
				if ((AARE[i] & 0xFF) == 0xA4 && (AARE[i + 1] & 0xFF) == 0x0a
						&& (AARE[i + 2] & 0xFF) == 0x04
						&& (AARE[i + 3] & 0xFF) == 0x08) {
					for (int j = 0; j < 8; j++) {
						fpara.sysTitleS[j] = AARE[i + 4 + j];
					}
				}
			}
		}
		return RtnByte;
	}

	/**
	 * AES_GCM_128 加密方法
	 * 
	 * @param key
	 *            ：密钥EK
	 * @param key_len
	 * @param iv
	 *            ：初始化向量
	 * @param iv_len
	 * @param hdr
	 *            ：验证附加信息ADD（10+AK）
	 * @param hdr_len
	 * 
	 * @param msg
	 *            ：输出-密文C
	 * @param tag
	 *            ：输出-消息验证码Mac
	 */
	private byte[] gcm_encrypt_message(byte[] key, int key_len, byte[] iv,
			int iv_len, byte[] hdr, int hdr_len, byte[] msg, byte[] tag) {
		byte[] tmpEncryBytes = null;
		try {
			AEADParameters tmpAeadParameters = new AEADParameters(
					new KeyParameter(key), tag.length * 8, iv, hdr);
			GCMBlockCipher tmpGcmBlockCipher = new GCMBlockCipher(
					new AESFastEngine(), null);
			tmpGcmBlockCipher.init(true, tmpAeadParameters);
			tmpEncryBytes = new byte[tmpGcmBlockCipher
					.getOutputSize(msg.length)];
			int tmpLength = tmpGcmBlockCipher.processBytes(msg, 0, msg.length,
					tmpEncryBytes, 0);
			tmpLength += tmpGcmBlockCipher.doFinal(tmpEncryBytes, tmpLength);
			// msg = new byte[tmpEncryBytes.length - 12];
			// System.arraycopy(tmpEncryBytes,0, msg,0, msg.length);
			// tag = new byte[12];
			// System.arraycopy(tmpEncryBytes, msg.length, tag, 0, tag.length);

		} catch (Exception ex) {

		}
		return tmpEncryBytes;
	}

	/**
	 * AES_GCM_128 解密方法
	 * 
	 * @param key
	 * @param key_len
	 * @param iv
	 * @param iv_len
	 * @param hdr
	 * @param hdr_len
	 * @param msg
	 * @param msg_len
	 * @param tag
	 * @param tag_len
	 */
	private void gcm_decrypt_message(byte[] key, int key_len, byte[] iv,
			int iv_len, byte[] hdr, int hdr_len, byte[] msg, int msg_len,
			byte[] tag, int tag_len) {
		try {
			AEADParameters tmpAeadParameters = new AEADParameters(
					new KeyParameter(key), tag.length * 8, iv, hdr);
			GCMBlockCipher tmpGcmBlockCipher = new GCMBlockCipher(
					new AESFastEngine(), null);
			tmpGcmBlockCipher.init(false, tmpAeadParameters);
			byte[] tmpBytes = new byte[msg.length + tag.length];

			System.arraycopy(msg, 0, tmpBytes, 0, msg.length);
			System.arraycopy(tag, 0, tmpBytes, msg.length, tag.length);
			byte[] tmpDecryBytes = new byte[tmpGcmBlockCipher
					.getOutputSize(tmpBytes.length)];

			int tmpLength = tmpGcmBlockCipher.processBytes(tmpBytes, 0,
					tmpBytes.length, tmpDecryBytes, 0);

			tmpGcmBlockCipher.doFinal(tmpDecryBytes, tmpLength);
			byte[] tmpPlaintextBytes = new byte[msg.length];
			System.arraycopy(tmpDecryBytes, 0, tmpPlaintextBytes, 0,
					tmpPlaintextBytes.length);
			System.arraycopy(tmpPlaintextBytes, 0, msg, 0,
					tmpPlaintextBytes.length);
		} catch (Exception ex) {

		}
	}

	/**
	 * 控制字产生方法
	 * 
	 * @param fpara
	 * @return
	 */
	private byte IControlWordBuild(HXFramePara fpara) {
		if (fpara.Nrec == 8) {
			fpara.Nrec = 0;
		}
		if (fpara.Nsend == 8) {
			fpara.Nsend = 0;
		}
		String TmpStrH = "00" + Integer.toBinaryString(fpara.Nrec);
		String TmpStrL = "00" + Integer.toBinaryString(fpara.Nsend);
		TmpStrH = TmpStrH.substring(TmpStrH.length() - 3, TmpStrH.length())
				+ "1"
				+ TmpStrL.substring(TmpStrL.length() - 3, TmpStrH.length())
				+ "0";
		return (byte) Integer.parseInt(TmpStrH, 2);
	}

	/**
	 * 还原数据函数 如果有加密或认证，则将收到数据还原
	 * 
	 * @param cText
	 * @param fpara
	 * @return
	 */
	public byte[] GetOriginalData(byte[] cText, HXFramePara fpara) {
		int indexAPDUtag = 0;
		byte[] recFrameCnt = new byte[4];
		byte recAuEnByte = 0x00;
		byte[] iv = new byte[12];
		byte[] addInfo;
		byte[] tag;
		byte[] msg;
		byte[] rtnBytes = new byte[0];
		int indeRtnByte = 0;
		int extraLenByte = 0;
		for (int i = 0; i < cText.length; i++) {
			if ((cText[i] & 0xff) == 0xe6 && (cText[i + 1] & 0xff) == 0xe7
					&& (cText[i + 2] & 0xff) == 0x00) {
				indexAPDUtag = i + 3;
				if ((cText[i + 4] & 0xff) == 0x81)// LEN超过0x7F的话indexAPDUtag就取0x81的这个索引
				{
					extraLenByte = 1;
				}
				break;
			}
		}
		recAuEnByte = cText[indexAPDUtag + 2 + extraLenByte];
		recFrameCnt[0] = cText[indexAPDUtag + 3 + extraLenByte];
		recFrameCnt[1] = cText[indexAPDUtag + 4 + extraLenByte];
		recFrameCnt[2] = cText[indexAPDUtag + 5 + extraLenByte];
		recFrameCnt[3] = cText[indexAPDUtag + 6 + extraLenByte];
		for (int i = 0; i < 8; i++)// 给IV前8个字节赋值
		{
			iv[i] = fpara.sysTitleS[i];
		}
		iv[8] = recFrameCnt[0];
		iv[9] = recFrameCnt[1];
		iv[10] = recFrameCnt[2];
		iv[11] = recFrameCnt[3];
		if (recAuEnByte == 0x10) {
			rtnBytes = new byte[cText.length - 19 - extraLenByte];
			for (int i = 0; i < indexAPDUtag; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
			for (int i = 0; i < cText.length - indexAPDUtag - 7 - 12 - 3
					- extraLenByte; i++) {
				rtnBytes[indeRtnByte++] = cText[indexAPDUtag + 7 + i
						- extraLenByte];
			}
			for (int i = cText.length - 4; i < cText.length - 1; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
		}
		if (recAuEnByte == 0x20) {
			rtnBytes = new byte[cText.length - 7 - extraLenByte];
			addInfo = new byte[0];
			tag = new byte[0];
			msg = new byte[cText.length - indexAPDUtag - 7 - 3 - extraLenByte];
			for (int i = 0; i < msg.length; i++) {
				msg[i] = cText[indexAPDUtag + 7 + i + extraLenByte];
			}
			gcm_decrypt_message(fpara.enKey, (byte) fpara.enKey.length, iv,
					(byte) iv.length, addInfo, (byte) addInfo.length, msg,
					msg.length, tag, tag.length);
			for (int i = 0; i < indexAPDUtag; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
			for (int i = 0; i < cText.length - indexAPDUtag - 7 - 3
					- extraLenByte; i++) {
				rtnBytes[indeRtnByte++] = msg[i];
			}
			for (int i = cText.length - 4; i < cText.length - 1; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
		}
		if (recAuEnByte == 0x30) {
			rtnBytes = new byte[cText.length - 19 - extraLenByte];
			addInfo = new byte[17];
			addInfo[0] = recAuEnByte;
			for (int i = 0; i < 16; i++) {
				addInfo[i + 1] = fpara.auKey[i];
			}
			msg = new byte[cText.length - indexAPDUtag - 7 - 12 - 3
					- extraLenByte];
			for (int i = 0; i < msg.length; i++) {
				msg[i] = cText[indexAPDUtag + 7 + i + extraLenByte];
			}
			tag = new byte[12];
			for (int i = 0; i < 12; i++) {
				tag[i] = cText[cText.length - 3 - 12 + i];
			}
			gcm_decrypt_message(fpara.enKey, fpara.enKey.length, iv, iv.length,
					addInfo, addInfo.length, msg, msg.length, tag, tag.length);
			for (int i = 0; i < indexAPDUtag; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
			for (int i = 0; i < msg.length; i++) {
				rtnBytes[indeRtnByte++] = msg[i];
			}
			for (int i = cText.length - 4; i < cText.length - 1; i++) {
				rtnBytes[indeRtnByte++] = cText[i];
			}
		}
		return rtnBytes;
	}

	/**
	 * 断开链路帧
	 * 
	 * @param fpara
	 * @return
	 */
	public byte[] getDISCFrame(HXFramePara fpara) {
		int Index = 0;
		byte[] TmpArr;
		byte[] CheckByte = new byte[2];

		TmpArr = new byte[200];
		Index = 0;
		TmpArr[Index++] = 0x7E;
		TmpArr[Index++] = (byte) 0xA0;
		Index++;
		for (int j = 0; j < fpara.DestAddr.length; j++) {
			TmpArr[Index++] = fpara.DestAddr[j];
		}
		TmpArr[Index++] = fpara.SourceAddr;
		TmpArr[Index++] = 0x53;
		TmpArr[2] = (byte) (Index + 1);
		CheckByte = CRC16(TmpArr, 1, Index - 1);
		TmpArr[Index++] = CheckByte[0];
		TmpArr[Index++] = CheckByte[1];
		TmpArr[Index++] = 0x7E;
		byte[] rtnArr = new byte[Index];
		System.arraycopy(TmpArr, 0, rtnArr, 0, Index);
		return rtnArr;
	}
}
