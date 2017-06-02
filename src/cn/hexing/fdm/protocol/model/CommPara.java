package cn.hexing.fdm.protocol.model;

public class CommPara {

	// COM������
	public String ComName;

	public String getComName() {
		return ComName;
	}

	public void setComName(String comName) {
		ComName = comName;
	}

	// ������
	public int BRate;

	public int getBRate() {
		return BRate;
	}

	public void setBRate(int bRate) {
		BRate = bRate;
	}

	// ����λ��
	public int DBit;

	public int getDBit() {
		return DBit;
	}

	public void setDBit(int dBit) {
		DBit = dBit;
	}

	// У��λ
	public char Pty;

	public char getPty() {
		return Pty;
	}

	public void setPty(char pty) {
		Pty = pty;
	}

	// ֹͣλ
	public int Sbit;

	public int getSbit() {
		return Sbit;
	}

	public void setSbit(int sbit) {
		Sbit = sbit;
	}

	// �Ƿ���Ƶ�Դ
	public Boolean IsControlPower;

	public Boolean getIsControlPower() {
		return IsControlPower;
	}

	public void setIsControlPower(Boolean isControlPower) {
		IsControlPower = isControlPower;
	}

	public enum StopBits {
		None,
		// ժҪ:
		// ʹ��һ��ֹͣλ��
		One1,
		//
		// ժҪ:
		// ʹ������ֹͣλ��
		Two,
		//
		// ժҪ:
		// ʹ�� 1.5 ��ֹͣλ��
		OnePointFive
	}

	public enum Parity {
		// ժҪ:
		// ��������żУ���顣
		None,
		//
		// ժҪ:
		// ������żУ��λ��ʹλ������������
		Odd,
		//
		// ժҪ:
		// ������żУ��λ��ʹλ������ż����
		Even,
		//
		// ժҪ:
		// ����żУ��λ����Ϊ 1��
		Mark,
		//
		// ժҪ:
		// ����żУ��λ����Ϊ 0��
		Space
	}

}
