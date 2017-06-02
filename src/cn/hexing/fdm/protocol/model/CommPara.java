package cn.hexing.fdm.protocol.model;

public class CommPara {

	// COM口名称
	public String ComName;

	public String getComName() {
		return ComName;
	}

	public void setComName(String comName) {
		ComName = comName;
	}

	// 波特率
	public int BRate;

	public int getBRate() {
		return BRate;
	}

	public void setBRate(int bRate) {
		BRate = bRate;
	}

	// 数据位数
	public int DBit;

	public int getDBit() {
		return DBit;
	}

	public void setDBit(int dBit) {
		DBit = dBit;
	}

	// 校验位
	public char Pty;

	public char getPty() {
		return Pty;
	}

	public void setPty(char pty) {
		Pty = pty;
	}

	// 停止位
	public int Sbit;

	public int getSbit() {
		return Sbit;
	}

	public void setSbit(int sbit) {
		Sbit = sbit;
	}

	// 是否控制电源
	public Boolean IsControlPower;

	public Boolean getIsControlPower() {
		return IsControlPower;
	}

	public void setIsControlPower(Boolean isControlPower) {
		IsControlPower = isControlPower;
	}

	public enum StopBits {
		None,
		// 摘要:
		// 使用一个停止位。
		One1,
		//
		// 摘要:
		// 使用两个停止位。
		Two,
		//
		// 摘要:
		// 使用 1.5 个停止位。
		OnePointFive
	}

	public enum Parity {
		// 摘要:
		// 不发生奇偶校验检查。
		None,
		//
		// 摘要:
		// 设置奇偶校验位，使位数等于奇数。
		Odd,
		//
		// 摘要:
		// 设置奇偶校验位，使位数等于偶数。
		Even,
		//
		// 摘要:
		// 将奇偶校验位保留为 1。
		Mark,
		//
		// 摘要:
		// 将奇偶校验位保留为 0。
		Space
	}

}
