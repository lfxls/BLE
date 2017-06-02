package cn.hexing.fdm.protocol.model;

import android.R.bool;

import java.util.List;

public class HXFramePara {

	public enum APDUtype {
		glo_initiateRequest, glo_initiateResponse, glo_get_request, glo_set_request, glo_events_notification_request, glo_action_request, glo_get_response, glo_set_response, glo_action_response,
	}

	public enum AuthMethod {
		AES_GCM_128, MD5,
	}

	public enum AuthMode {
		NONE, LLS, HLS,
	}

	public enum ProtocolType {
		DLMS, IEC21, GW376_1, GW645_97
	}

	public enum HHUTYPE {
		Idata, HT368
	}

	public enum ErrorMessage {
		None, APIModeError, IrdaError, OpenPortError, HandclaspFailed, PasswordError, WriteFailed, OverTime, CSError, DataError, PortException
	}

	// 表号
	public String MeterNo;

	// 加密方法
	public AuthMethod encryptionMethod;

	// 加密级别
	public byte enLevel;

	// AK
	public byte[] auKey;

	// EK
	public byte[] enKey;

	// 高级身份认证HLSkey
	public byte[] aesKey;

	// 身份认证方式
	public AuthMode Mode;

	// 是否为第一帧
	public boolean FirstFrame;
	
	//设备类型
	public String CommDeviceType;

	// 参数值(用于读时，带参数)
	public String WriteData;

	// 发送之后停顿多少时间
	public int SleepT;

	// 超时时间
	public int WaitT;

	// 字节超时
	public int ByteWaitT;

	// 错误
	public String ErrTxt;

	//Write Token的返回值
	public String WriteTokenResult;

	// 帧内容
	public String FrameShowTxt;

	// 帧数量
	public int frameCnt;

	public String StrsysTitleC;
	// 厂家信息
	public byte[] sysTitleC;

	// 厂家信息返回
	public byte[] sysTitleS;

	// 分类+OBIS+属性+"00",21协议是则为IDCODE
	public String OBISattri;

	// 分包返回时的包数
	public int BlockNum;

	// 最大发送字节
	public int MaxSendInfo_Value;

	// 最大接收字节
	public int MaxRecInfo_Value;

	// / 最大发送窗体
	public int MaxSendWindow_Value;

	// 最大接收窗体
	public int MaxRecWindow_Value;

	// 最大接收字节数
	public byte[] RecData;

	// 目标地址
	public byte[] DestAddr;
	
	//表地址
	public String strMeterNo;

	// 源地址
	public byte SourceAddr;

	// 发送计数 SSS
	public int Nsend;

	// 接收计数 RRR
	public int Nrec;

	// 低级身份认证LLSkey 21也使用
	public String Pwd;

	// 21专用
	// z字
	public String ZWord;

	// 是否是读出模式
	public Boolean IsReadOut;

	// 发送帧
	public String SendFrame;

	// 376.1参数

	// 终端地址
	public String TermiAddr;

	// 终端功能码
	public String TermiAFN;

	// 终端SEQ
	public String TermiSEQ;

	// 终端控制码
	public String TermiCMD;

	// 终端EC
	public String TermiEC;

	// 终端TP 日、月、曲线类
	public String TermiTP;

	// 终端2类数据密度
	public String TermiDensity;

	// 终端2类数据时标 年后2位+月日时分
	public String TermiTimeMark;

	// 终端2类数据点数
	public String TermiPoint;

	// 终端FN
	public String TermiFN;

	// 终端PN
	public String TermiPN;

	// 终端PWD
	public String TermiPWD;

	// 终端data
	public String TermiDATA;

	// 645参数
	// 表计地址
	public String MeterAddr;

	// 表计协议97
	public String MeterPrtlType;

	// 表计控制码
	public String MeterCMD;

	// 表计ID
	public String MeterIDCode;

	// 表计编程密码
	public String MeterPWD;

	// 表计写入数据
	public String MeterDATA;

	// 特殊参数传递
	public Object Tag;

	// 21协议 是否发送 06
	public bool isContinue;

	public String strDecDataType;
	
	public String strUnitString;
	
	public cn.hexing.fdm.protocol.bll.dlmsService.DataType decDataType;

	public String stringDataType;

	public List<TranXADRAssist> listTranXADRAssist=null;//

	
	public cn.hexing.fdm.protocol.bll.dlmsService.DataType getDecDataType() {
		return decDataType;
	}

	public void setDecDataType(
			cn.hexing.fdm.protocol.bll.dlmsService.DataType decDataType) {
		this.decDataType = decDataType;
	}

	public String getMeterNo() {
		return MeterNo;
	}

	public void setMeterNo(String meterNo) {
		MeterNo = meterNo;
	}

	public AuthMethod getEncryptionMethod() {
		return encryptionMethod;
	}

	public void setEncryptionMethod(AuthMethod encryptionMethod) {
		this.encryptionMethod = encryptionMethod;
	}

	public byte getEnLevel() {
		return enLevel;
	}

	public void setEnLevel(byte enLevel) {
		this.enLevel = enLevel;
	}

	public byte[] getAuKey() {
		return auKey;
	}

	public void setAuKey(byte[] auKey) {
		this.auKey = auKey;
	}

	public byte[] getEnKey() {
		return enKey;
	}

	public void setEnKey(byte[] enKey) {
		this.enKey = enKey;
	}

	public byte[] getAesKey() {
		return aesKey;
	}

	public void setAesKey(byte[] aesKey) {
		this.aesKey = aesKey;
	}

	public AuthMode getMode() {
		return Mode;
	}

	public void setMode(AuthMode mode) {
		Mode = mode;
	}

	public boolean getFirstFrame() {
		return FirstFrame;
	}

	public void setFirstFrame(boolean firstFrame) {
		FirstFrame = firstFrame;
	}

	public String getWriteData() {
		return WriteData;
	}

	public void setWriteData(String writeData) {
		WriteData = writeData;
	}

	public int getSleepT() {
		return SleepT;
	}

	public void setSleepT(int sleepT) {
		SleepT = sleepT;
	}

	public int getWaitT() {
		return WaitT;
	}

	public void setWaitT(int waitT) {
		WaitT = waitT;
	}

	public int getByteWaitT() {
		return ByteWaitT;
	}

	public void setByteWaitT(int byteWaitT) {
		ByteWaitT = byteWaitT;
	}

	public String getErrTxt() {
		return ErrTxt;
	}

	public void setErrTxt(String errTxt) {
		ErrTxt = errTxt;
	}

	public String getFrameShowTxt() {
		return FrameShowTxt;
	}

	public void setFrameShowTxt(String frameShowTxt) {
		FrameShowTxt = frameShowTxt;
	}

	public int getFrameCnt() {
		return frameCnt;
	}

	public void setFrameCnt(int frameCnt) {
		this.frameCnt = frameCnt;
	}

	public byte[] getSysTitleC() {
		return sysTitleC;
	}

	public void setSysTitleC(byte[] sysTitleC) {
		this.sysTitleC = sysTitleC;
	}

	public byte[] getSysTitleS() {
		return sysTitleS;
	}

	public void setSysTitleS(byte[] sysTitleS) {
		this.sysTitleS = sysTitleS;
	}

	public String getOBISattri() {
		return OBISattri;
	}

	public void setOBISattri(String oBISattri) {
		OBISattri = oBISattri;
	}

	public int getBlockNum() {
		return BlockNum;
	}

	public void setBlockNum(int blockNum) {
		BlockNum = blockNum;
	}

	public int getMaxSendInfo_Value() {
		return MaxSendInfo_Value;
	}

	public void setMaxSendInfo_Value(int maxSendInfo_Value) {
		MaxSendInfo_Value = maxSendInfo_Value;
	}

	public int getMaxRecInfo_Value() {
		return MaxRecInfo_Value;
	}

	public void setMaxRecInfo_Value(int maxRecInfo_Value) {
		MaxRecInfo_Value = maxRecInfo_Value;
	}

	public int getMaxSendWindow_Value() {
		return MaxSendWindow_Value;
	}

	public void setMaxSendWindow_Value(int maxSendWindow_Value) {
		MaxSendWindow_Value = maxSendWindow_Value;
	}

	public int getMaxRecWindow_Value() {
		return MaxRecWindow_Value;
	}

	public void setMaxRecWindow_Value(int maxRecWindow_Value) {
		MaxRecWindow_Value = maxRecWindow_Value;
	}

	public byte[] getRecData() {
		return RecData;
	}

	public void setRecData(byte[] recData) {
		RecData = recData;
	}

	public byte[] getDestAddr() {
		return DestAddr;
	}

	public void setDestAddr(byte[] destAddr) {
		DestAddr = destAddr;
	}

	public byte getSourceAddr() {
		return SourceAddr;
	}

	public void setSourceAddr(byte sourceAddr) {
		SourceAddr = sourceAddr;
	}

	public int getNsend() {
		return Nsend;
	}

	public void setNsend(int nsend) {
		Nsend = nsend;
	}

	public int getNrec() {
		return Nrec;
	}

	public void setNrec(int nrec) {
		Nrec = nrec;
	}

	public String getPwd() {
		return Pwd;
	}

	public void setPwd(String pwd) {
		Pwd = pwd;
	}

	public String getZWord() {
		return ZWord;
	}

	public void setZWord(String zWord) {
		ZWord = zWord;
	}

	public Boolean getIsReadOut() {
		return IsReadOut;
	}

	public void setIsReadOut(Boolean isReadOut) {
		IsReadOut = isReadOut;
	}

	public String getSendFrame() {
		return SendFrame;
	}

	public void setSendFrame(String sendFrame) {
		SendFrame = sendFrame;
	}

	public String getTermiAddr() {
		return TermiAddr;
	}

	public void setTermiAddr(String termiAddr) {
		TermiAddr = termiAddr;
	}

	public String getTermiAFN() {
		return TermiAFN;
	}

	public void setTermiAFN(String termiAFN) {
		TermiAFN = termiAFN;
	}

	public String getTermiSEQ() {
		return TermiSEQ;
	}

	public void setTermiSEQ(String termiSEQ) {
		TermiSEQ = termiSEQ;
	}

	public String getTermiCMD() {
		return TermiCMD;
	}

	public void setTermiCMD(String termiCMD) {
		TermiCMD = termiCMD;
	}

	public String getTermiEC() {
		return TermiEC;
	}

	public void setTermiEC(String termiEC) {
		TermiEC = termiEC;
	}

	public String getTermiTP() {
		return TermiTP;
	}

	public void setTermiTP(String termiTP) {
		TermiTP = termiTP;
	}

	public String getTermiDensity() {
		return TermiDensity;
	}

	public void setTermiDensity(String termiDensity) {
		TermiDensity = termiDensity;
	}

	public String getTermiTimeMark() {
		return TermiTimeMark;
	}

	public void setTermiTimeMark(String termiTimeMark) {
		TermiTimeMark = termiTimeMark;
	}

	public String getTermiPoint() {
		return TermiPoint;
	}

	public void setTermiPoint(String termiPoint) {
		TermiPoint = termiPoint;
	}

	public String getTermiFN() {
		return TermiFN;
	}

	public void setTermiFN(String termiFN) {
		TermiFN = termiFN;
	}

	public String getTermiPN() {
		return TermiPN;
	}

	public void setTermiPN(String termiPN) {
		TermiPN = termiPN;
	}

	public String getTermiPWD() {
		return TermiPWD;
	}

	public void setTermiPWD(String termiPWD) {
		TermiPWD = termiPWD;
	}

	public String getTermiDATA() {
		return TermiDATA;
	}

	public void setTermiDATA(String termiDATA) {
		TermiDATA = termiDATA;
	}

	public String getMeterAddr() {
		return MeterAddr;
	}

	public void setMeterAddr(String meterAddr) {
		MeterAddr = meterAddr;
	}

	public String getMeterPrtlType() {
		return MeterPrtlType;
	}

	public void setMeterPrtlType(String meterPrtlType) {
		MeterPrtlType = meterPrtlType;
	}

	public String getMeterCMD() {
		return MeterCMD;
	}

	public void setMeterCMD(String meterCMD) {
		MeterCMD = meterCMD;
	}

	public String getMeterIDCode() {
		return MeterIDCode;
	}

	public void setMeterIDCode(String meterIDCode) {
		MeterIDCode = meterIDCode;
	}

	public String getMeterPWD() {
		return MeterPWD;
	}

	public void setMeterPWD(String meterPWD) {
		MeterPWD = meterPWD;
	}

	public String getMeterDATA() {
		return MeterDATA;
	}

	public void setMeterDATA(String meterDATA) {
		MeterDATA = meterDATA;
	}

	public Object getTag() {
		return Tag;
	}

	public void setTag(Object tag) {
		Tag = tag;
	}

	public bool getIsContinue() {
		return isContinue;
	}

	public void setIsContinue(bool isContinue) {
		this.isContinue = isContinue;
	}
}
