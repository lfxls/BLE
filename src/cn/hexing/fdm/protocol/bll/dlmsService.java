package cn.hexing.fdm.protocol.bll;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.hexing.fdm.protocol.model.TranXADRAssist;

public class dlmsService {


    public static enum DataType {
        bool,
        octet_string_origal,
        Octs_string,
        Octs_hex,
        Octs_ascii,
        Ascs,
        yyyy_mm_dd,
        HH_mm_ss,
        Octs_datetime,
        Int32,
        U32_hex,
        unsigned32_decimal,
        unsigned32_decimal2,
        U32,
        U8_hex,
        U8,
        unsigned8_workMode,
        unsigned8_limiterActive,
        unsigned16_hex,
        Int64,
        float32,
        U16,
        BCD,
        enumm,
        time,
        mm_dd,
        yy_mm_dd,
        Array_dd,
        Struct_Billing;
    }


    public static enum DateType {
        YYMMDDhhmmssYYMMDDhhmmss, YYMMDDhhmmss, YYMMDDhhmm, YYYYMMDD, YYYYMMDDhhmmss, YYYYMMDDhhmm, MMDDhhmm, HHmmss, YYMMDDhhmmssNNNN, NNNNNNNN, NNNN, DDHHmmss,

    }

    public static enum DtFormat {
        dd_MM_yyyy, MM_dd_yyyy, dd_yyyy_MM, yyyy_dd_MM, MM_yyyy_dd, yyyy_MM_dd,

    }

    private static DtFormat dtFormat = DtFormat.yyyy_MM_dd;

    public static String Byte2String(byte[] bBytes, int iStartIndex, int iLen) {
        StringBuilder sb = new StringBuilder();
        int i = iStartIndex;
        char cChar = (char) 0;
        byte bCnt = (byte) 0;
        for (i = iStartIndex; i < iStartIndex + iLen; i++) {
            if ((bBytes[i] >= (byte) 0x20) && (bBytes[i] <= (byte) 0x7F)) {// 可显示ASCII
                cChar = (char) bBytes[i];
                sb.append(cChar);
            } else {// 不可显示的ASCII
                if ((bBytes[i] & 0xff) >= 0x80) {
                    bCnt++;
                    if (bCnt > 2) {
                        bCnt = 0;
                        String str = null;
                        try {
                            str = new String(bBytes, i - 2, 3, "UTF8");
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        sb.append(str);
                    }
                } else {
                    sb.append(String.format("%02x", bBytes[i]));
                }
            }
        }
        //
        return sb.toString();
    }

    public static String TranXADRCode(byte[] XADRcodeStr) {
        String RtnStr = "";
        double RtnValue = 0.0;

        switch (XADRcodeStr[0] & 0xff) {
            case 2:
                int TmUInt32 = (XADRcodeStr[3] & 0xff) * 0x1000000
                        + (XADRcodeStr[4] & 0xff) * 0x10000
                        + (XADRcodeStr[5] & 0xff) * 0x100 + (XADRcodeStr[6] & 0xff);
                RtnValue = TmUInt32;
                RtnStr = Double.toString(RtnValue);
                break;
            case 3:// boolean
                RtnStr = Integer.toString((XADRcodeStr[1] & 0xff));
                break;
            case 5:// integer32
                int TmpInt32 = (XADRcodeStr[1] & 0xff) * 0x1000000
                        + (XADRcodeStr[2] & 0xff) * 0x10000
                        + (XADRcodeStr[3] & 0xff) * 0x100 + (XADRcodeStr[4] & 0xff);
                RtnValue = TmpInt32;
                RtnStr = Double.toString(RtnValue);
                break;
            case 6:// unsigned32
                int TmpUInt32 = (XADRcodeStr[1] & 0xff) * 0x1000000
                        + (XADRcodeStr[2] & 0xff) * 0x10000
                        + (XADRcodeStr[3] & 0xff) * 0x100 + (XADRcodeStr[4] & 0xff);
                RtnValue = TmpUInt32;
                RtnStr = Double.toString(RtnValue);
                break;
            case 9:
                if ((XADRcodeStr[1] & 0xff) >= 0x80) {
                    int lenofLen = (XADRcodeStr[1] & 0xff) - 0x80;
                    int len = 0;
                    for (int i = 0; i < lenofLen; i++) {
                        len += (((XADRcodeStr[2 + i] & 0xff) * Math.pow(0x100,
                                lenofLen - 1 - i)));
                    }
                    for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                        RtnStr += String.format("%02x",
                                (XADRcodeStr[i + 2] & 0xff));
                    }
                    break;

                } else {
                    for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                        RtnStr += String.format("%02x",
                                (XADRcodeStr[i + 2] & 0xff));
                    }
                }
                break;
            case 10:
                RtnStr += Byte2String(XADRcodeStr, 2, XADRcodeStr.length - 2)
                        .replace(" ", "");
                break;
            case 16:
            case 18:// unsigned16
                int u16 = 0;
                u16 = (XADRcodeStr[1] & 0xff) * 0x100 + (XADRcodeStr[2] & 0xff);
                RtnStr = Integer.toString(u16);
                break;
            case 17:
                int TmpUInt8 = (XADRcodeStr[1] & 0xff);
                RtnValue = (TmpUInt8);
                RtnStr = Double.toString(RtnValue);
                break;
            case 22:
                int Tmp = (XADRcodeStr[1] & 0xff);
                RtnStr = String.format("%02d", Tmp);
                break;
            case 13:// BCD 0D 02 12 34 ->1234
                for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                    int TmpInt = (XADRcodeStr[2 + i] & 0xff);
                    if (TmpInt != 0) {
                    }
                    RtnValue += (TmpInt)
                            * Math.pow(100, (XADRcodeStr[1] & 0xff) - i - 1);
                }
                RtnStr = Double.toString(RtnValue);
                break;
            case 15:// integer8
                byte TmpInt8 = XADRcodeStr[1];
                RtnValue = TmpInt8;
                RtnStr = Double.toString(RtnValue);
                break;
            case 21:// unsigned64
                long TmpUInt64 = ((XADRcodeStr[1] & 0xff) * 0x100000000000000L
                        + (XADRcodeStr[2] & 0xff) * 0x1000000000000L
                        + (XADRcodeStr[3] & 0xff) * 0x10000000000L
                        + (XADRcodeStr[4] & 0xff) * 0x100000000L
                        + (XADRcodeStr[5] & 0xff) * 0x1000000
                        + (XADRcodeStr[6] & 0xff) * 0x10000
                        + (XADRcodeStr[7] & 0xff) * 0x100 + (XADRcodeStr[8] & 0xff));
                RtnValue = TmpUInt64;
                RtnStr = Double.toString(RtnValue);
                break;
            case 23:
                byte[] tmpbytes = new byte[4];
                tmpbytes[0] = XADRcodeStr[4];
                tmpbytes[1] = XADRcodeStr[3];
                tmpbytes[2] = XADRcodeStr[2];
                tmpbytes[3] = XADRcodeStr[1];
                RtnStr = Float.toString(getFloat(tmpbytes));
                if (RtnStr.indexOf('.') > 0) {
                    RtnStr = RtnStr + "00";
                    RtnStr = RtnStr.substring(0, RtnStr.indexOf('.') + 3);
                }
                break;
        }
        return RtnStr;
    }


    public static List<String> TranBillingCode(byte[] XADRcodeStr, List<TranXADRAssist> listTranXADRAssist) {
        List<String> data = new ArrayList<String>();

        int TmpIndex = 2;
        int ItemCnt = listTranXADRAssist.size();//冻结配置项个数
        String TmpStr = "";

        try {
            int indexpre = XADRcodeStr[1];//冻结记录条数
            for (int i = 0; i < indexpre; i++) {

                TmpIndex=TmpIndex+2;
                if (ItemCnt > 0x80) {
                    TmpIndex++;
                }

                String strTemp="";

                for (int j = 0; j < ItemCnt; j++)//每条记录的项数
                {

                    int nScaler = listTranXADRAssist.get(j).nScaler;
                    String strName = listTranXADRAssist.get(j).strName;
                    String strUnit = listTranXADRAssist.get(j).unit;
                    TmpStr ="";

                        strTemp+=strName+":";


                    switch (XADRcodeStr[TmpIndex++]& 0xff) {
                        case 9:
                            if (XADRcodeStr[TmpIndex++] == 0x0c)//date_time
                            {
                                TmpStr = String.format("%04d", (XADRcodeStr[TmpIndex++] & 0xff) * 256
                                        + (XADRcodeStr[TmpIndex++] & 0xff))
                                        + String.format("%02d", (XADRcodeStr[TmpIndex++] & 0xff))
                                        + String.format("%02d", (XADRcodeStr[TmpIndex++] & 0xff))
                                        + String.format("%02d", (XADRcodeStr[TmpIndex + 2] & 0xff))
                                        + String.format("%02d", (XADRcodeStr[TmpIndex++] & 0xff))
                                        + String.format("%02d", (XADRcodeStr[TmpIndex++] & 0xff));
                                TmpStr = FormatValue(TmpStr, 1, 1, 1, 1,
                                        DateType.YYYYMMDDhhmmss);
                                TmpIndex = TmpIndex + 6;
                            }
                            break;
                        case 6://unsigned32
                            Integer inResult = (XADRcodeStr[TmpIndex++] & 0xff) * 0x1000000
                                    + (XADRcodeStr[TmpIndex++] & 0xff) * 0x10000
                                    + (XADRcodeStr[TmpIndex++] & 0xff) * 0x100
                                    + (XADRcodeStr[TmpIndex++] & 0xff);

                            Double dlResult = inResult * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult)+strUnit;
                            break;
                        case 5:
                            for (int m = 0; m < 4; m++) {
                                TmpStr += String
                                        .format("%02x", (XADRcodeStr[TmpIndex++] & 0xff));
                            }
                            inResult = Integer.parseInt(TmpStr, 16);

                             dlResult = inResult * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult) +strUnit ;
                            break;
                        case 13://bcd
                            int ForCount = XADRcodeStr[TmpIndex++] & 0xff;
                            long lResult=0;
                            for (int k = 0; k < ForCount; k++) {
                                Integer TmpInt = (XADRcodeStr[TmpIndex++] & 0xff);

                                 lResult= (long)(TmpInt * Math.pow(100, ForCount - k - 1));

                            }

                            dlResult = lResult * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult)+strUnit;
                            break;
                        case 17://unsigned8
                            TmpStr += String.format("%02x", (XADRcodeStr[TmpIndex++] & 0xff));
                            break;
                        case 18://unsigned16
                            Integer u16 = 0;
                            u16 = (XADRcodeStr[TmpIndex++] & 0xff) * 0x100 + (XADRcodeStr[TmpIndex++] & 0xff);
                            dlResult = u16 * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult)+strUnit;
                            break;
                        case 20://int64
                            String RtnStr="";
                            for (int k = 0; k < 8; k++) {
                                RtnStr += String.format("%02x",
                                        (XADRcodeStr[TmpIndex++] & 0xff), 16);
                            }
                            Integer TmpInt = Integer.parseInt(RtnStr,16);
                            dlResult = TmpInt * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult)+strUnit;
                            break;
                        case 23://float32
                            byte[] tmpbytes = new byte[4];
                            tmpbytes[3] = XADRcodeStr[TmpIndex++];
                            tmpbytes[2] = XADRcodeStr[TmpIndex++];
                            tmpbytes[1] = XADRcodeStr[TmpIndex++];
                            tmpbytes[0] = XADRcodeStr[TmpIndex++];

                            DataInputStream dis=new DataInputStream(new ByteArrayInputStream(tmpbytes));
                            float valCPT=dis.readFloat();
                            dis.close();
                            dlResult = valCPT * Math.pow(10,nScaler);
                            TmpStr = Double.toString(dlResult)+strUnit;
                            break;
                    }
                    strTemp += TmpStr+",";

                    }
                data.add(strTemp.substring(0,strTemp.length()-1));
                }
            } catch(Exception ex){

            }
            return data;
        }


        /***
         * 解析函数
         *
         * @param XADRcodeStr
         * @param TypeStr
         * @return
         */

    public static String TranXADRCode(byte[] XADRcodeStr, DataType TypeStr) {
        String RtnStr = "";
        switch (XADRcodeStr[0] & 0xff) {
            case 1:
                switch (TypeStr) {
                    case Array_dd:
                        RtnStr = String
                                .format("%02x", (XADRcodeStr[15] & 0xff));
                        break;
                }
                break;
            case 3:
                switch (TypeStr) {
                    case bool:
                        if (XADRcodeStr[1] == 0x00) {
                            RtnStr = "00";
                        } else {
                            RtnStr = "01";
                        }
                        break;
                }
            case 9:// octet_string
                switch (TypeStr) {
                    case octet_string_origal:
                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                            RtnStr += ((XADRcodeStr[i + 2] & 0xff));
                        }
                        break;
                    case Octs_string:
                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                            RtnStr += String
                                    .format("%02x", (XADRcodeStr[i + 2] & 0xff));
                        }
                        break;
                    case Octs_ascii:
                    case Octs_hex:
                        if ((XADRcodeStr[1] & 0xff) >= 0x80) {
                            int lenofLen = (XADRcodeStr[1] & 0xff) - 0x80;
                            int len = 0;
                            for (int i = 0; i < lenofLen; i++) {
                                len += (((XADRcodeStr[2 + i] & 0xff) * Math.pow(0x100,
                                        lenofLen - 1 - i)));
                            }
                            String temp = null;
                            try {
                                temp = new String(XADRcodeStr, 2 + lenofLen, len,
                                        "ASCII");
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            RtnStr += temp;
                        } else {
                            String temp = null;
                            try {
                                temp = new String(XADRcodeStr, 2,
                                        (XADRcodeStr[1] & 0xff), "ASCII");
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            RtnStr += temp;
                        }
                        break;
                    case Ascs:
                        RtnStr += Byte2String(XADRcodeStr, 2, XADRcodeStr.length - 2);
                        break;
                    case yyyy_mm_dd:
                        RtnStr = String.format("%04d", (XADRcodeStr[2] & 0xff) * 256
                                + (XADRcodeStr[3] & 0xff))
                                + String.format("%02d", (XADRcodeStr[4] & 0xff))
                                + String.format("%02d", (XADRcodeStr[5] & 0xff));
                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1, DateType.YYYYMMDD);
                        break;
                    case HH_mm_ss:
                        RtnStr = String.format("%02d", (XADRcodeStr[2] & 0xff))
                                + String.format("%02d", (XADRcodeStr[3] & 0xff))
                                + String.format("%02d", (XADRcodeStr[4] & 0xff));
                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1, DateType.HHmmss);
                        break;
                    case Octs_datetime:
                        RtnStr = String.format("%04d", (XADRcodeStr[2] & 0xff) * 256
                                + (XADRcodeStr[3] & 0xff))
                                + String.format("%02d", (XADRcodeStr[4] & 0xff))
                                + String.format("%02d", (XADRcodeStr[5] & 0xff))
                                + String.format("%02d", (XADRcodeStr[7] & 0xff))
                                + String.format("%02d", (XADRcodeStr[8] & 0xff))
                                + String.format("%02d", (XADRcodeStr[9] & 0xff));
                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1,
                                DateType.YYYYMMDDhhmmss);
                        break;
                }
                break;
            case 10:
                switch (TypeStr) {
                    case Ascs:
                        RtnStr += Byte2String(XADRcodeStr, 2, XADRcodeStr.length - 2)
                                .replace(" ", "");
                        break;
                }
            case 13:// BCD
                switch (TypeStr) {
                    case BCD:
                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                            RtnStr += String
                                    .format("%02x", (XADRcodeStr[i + 2] & 0xff));
                        }
                        break;
                }
            case 5:// int32
                switch (TypeStr) {
                    case Int32:
                        for (int i = 0; i < 4; i++) {
                            RtnStr += String
                                    .format("%02x", (XADRcodeStr[i + 1] & 0xff));
                        }
                        RtnStr = Integer.toString(Integer.parseInt(RtnStr, 16));
                        break;
                }
                break;
            case 6:// unsigned32
                switch (TypeStr) {
	                case Int32:
	                    for (int i = 0; i < 4; i++) {
	                        RtnStr += String
	                                .format("%02x", (XADRcodeStr[i + 1] & 0xff));
	                    }
	                    RtnStr = Integer.toString(Integer.parseInt(RtnStr, 16));
	                    break;
                    case U32_hex:
                        for (int i = 0; i < 4; i++) {
                            RtnStr += String
                                    .format("%02x", (XADRcodeStr[i + 1] & 0xff));
                        }
                        break;
                    case unsigned32_decimal:
                        RtnStr = Integer.toString((XADRcodeStr[1] & 0xff) * 0x1000000
                                + (XADRcodeStr[2] & 0xff) * 0x10000
                                + (XADRcodeStr[3] & 0xff) * 0x100
                                + (XADRcodeStr[4] & 0xff));
                        break;
                    case unsigned32_decimal2:
                        RtnStr = Integer
                                .toString(((XADRcodeStr[1] & 0xff) * 0x1000000
                                        + (XADRcodeStr[2] & 0xff) * 0x10000
                                        + (XADRcodeStr[3] & 0xff) * 0x100 + (XADRcodeStr[4] & 0xff)) / 100);
                        break;
                    case U16:
                    case U32:// 增加U32的解析lyh
                        RtnStr = Integer.toString((XADRcodeStr[1] & 0xff) * 0x1000000
                                + (XADRcodeStr[2] & 0xff) * 0x10000
                                + (XADRcodeStr[3] & 0xff) * 0x100
                                + (XADRcodeStr[4] & 0xff));
                        break;
                }
                break;
            case 17:// unsigned8
                switch (TypeStr) {
                    case U8_hex:
                        RtnStr += String.format("%02x", (XADRcodeStr[1] & 0xff));
                        break;
                    case U8:
                        RtnStr += Integer.toString((XADRcodeStr[1] & 0xff));
                        break;
                    case unsigned8_workMode:
                        switch ((XADRcodeStr[1] & 0xff)) {
                            case 0x00:
                                RtnStr = "Normal mode";
                                break;
                            case 0x01:
                                RtnStr = "Pre-payment";
                                break;
                        }
                        break;
                    case unsigned8_limiterActive:
                        switch ((XADRcodeStr[1] & 0xff)) {
                            case 0x00:
                                RtnStr = "Deactive";
                                break;
                            case 0x01:
                                RtnStr = "Active";
                                break;
                            case (byte) 0xff:
                                RtnStr = "Not valid";
                                break;
                        }
                        break;
                    default:
                        RtnStr += String.format("%02x", (XADRcodeStr[1] & 0xff), 16);
                        break;
                }
                break;
            case 18:
                switch (TypeStr) {
                    case U16:
                        int u16 = 0;
                        u16 = (XADRcodeStr[1] & 0xff) * 0x100 + (XADRcodeStr[2] & 0xff);
                        RtnStr = Integer.toString(u16);
                        break;
                    default:
                        break;
                }
                break;
            case 20:// int64
                switch (TypeStr) {
                    case Int64:
                        for (int i = 0; i < 8; i++) {
                            RtnStr += String.format("%02x",
                                    (XADRcodeStr[i + 1] & 0xff), 16);
                        }
                        RtnStr = Integer.toString(Integer.parseInt(RtnStr, 16));
                        break;
                    default:
                        for (int i = 0; i < 8; i++) {
                            RtnStr += String
                                    .format("%02x", (XADRcodeStr[i + 1] & 0xff));
                        }
                        break;
                }
                break;
            case 22:
                switch (TypeStr) {
                    case enumm:
                        int Tmp = ((XADRcodeStr[1] & 0xff));
                        RtnStr = String.format("%02d", Tmp);
                        break;
                }
                break;
            case 23:// float32
                switch (TypeStr) {
                    case enumm:
                        byte[] tmpbytes = new byte[4];
                        tmpbytes[0] = XADRcodeStr[4];
                        tmpbytes[1] = XADRcodeStr[3];
                        tmpbytes[2] = XADRcodeStr[2];
                        tmpbytes[3] = XADRcodeStr[1];
                        RtnStr = Float.toString(getFloat(tmpbytes));
                        if (RtnStr.indexOf('.') > 0) {
                            RtnStr = RtnStr + "00";
                            RtnStr = RtnStr.substring(0, RtnStr.indexOf('.') + 3);
                        }
                        break;
                }
                break;
            case 15:
                RtnStr = Integer.toString((XADRcodeStr[1] & 0xff));
                break;
        }
        return RtnStr;
    }

    public static int numLen(int len) {
        int rtnNum = 0;

        while (len % 0xff > 0) {
            rtnNum++;
            len = len / 0xff;
        }

        return rtnNum;
    }

    public static byte[] String2Bytes(String strData) {
        char[] charData = strData.toCharArray();
        byte[] bData = new byte[charData.length];
        for (int i = 0; i < charData.length; i++) {
            bData[i] = (byte) (charData[i]);
        }
        return bData;
    }

    /***
     * 组织数据
     *
     * @param EncodeStr
     * @param TypeStr
     * @return
     */
    public static String GetXADRCode(String EncodeStr, DataType TypeStr) {
        String RtnStr = "";

        switch (TypeStr) {
            // case ppp:
            // case PPP:
            // if (EncodeStr != "")
            // RtnStr = "01010203110311040312" + EncodeStr;
            // break;
            case Array_dd://010102020904310000000905FFFFFF00FF
                RtnStr = "010102020904310000000905FFFFFF" + EncodeStr + "FF";
                break;
            case bool:
                RtnStr = "03" + EncodeStr;
                break;
            case Int32:// 1234->06 80 00 04 d2

                RtnStr = "05"
                        + String.format("%08x", (Integer.parseInt(EncodeStr)));
                break;
            //case unsigned32:// "00120F1108"->"0600120F1108"
            case U32:
                // RtnStr = "06" + EncodeStr.PadLeft(10, '0');
                RtnStr = "06"
                        + String.format("%08x", (Double.parseDouble(EncodeStr)));
                break;
            case unsigned32_decimal:// 1234->06 00 00 04 d2
                RtnStr = "06"
                        + String.format("%08x", (Double.parseDouble(EncodeStr)));
                break;
            case BCD:// "0012021108"->"0d040012021108"
                RtnStr = "0d" + String.format("%02x", EncodeStr.length() / 2)
                        + EncodeStr;
                break;
            case Octs_string:
                if (EncodeStr.length() / 2 < 128) {
                    RtnStr = "09" + String.format("%02x", EncodeStr.length() / 2);
                } else {
                    RtnStr = "09"
                            + Integer
                            .toString(0x80 + numLen(EncodeStr.length() / 2));
                    RtnStr = "0981" + String.format("%02x", EncodeStr.length() / 2);
                }
                for (int i = 0; i < EncodeStr.length() / 2; i++) {
                    RtnStr += EncodeStr.substring(i * 2, i * 2 + 2);
                }
                break;
//		case Octs_string:
//			if (EncodeStr.length() / 2 < 128) {
//				RtnStr = "09" + String.format("%02x", EncodeStr.length());
//			} else {
//				RtnStr = "09"
//						+ Integer.toString(0x80 + numLen(EncodeStr.length()));
//			}
//			byte[] bReceiveData = String2Bytes(EncodeStr);
//			for (int i = 0; i < bReceiveData.length; i++) {
//				RtnStr += Integer.toString(bReceiveData[i], 16);
//			}
//			break;
//		case Octs_string:
//			if (EncodeStr.length() < 128) {
//				RtnStr = "09" + String.format("%02x", EncodeStr.length());
//			} else {
//				RtnStr = "0981" + String.format("%02x", EncodeStr.length());
//			}
//			for (int i = 0; i < EncodeStr.length(); i++) {
//				RtnStr += "0" + EncodeStr.substring(i, i + 1);
//			}
//			break;
            case Ascs:
            case Octs_ascii:
                if (EncodeStr.length() < 128) {
                    RtnStr = "0A" + String.format("%02x", EncodeStr.length());// "09"
                } else {
                    int len = numLen(EncodeStr.length() / 2);
                    RtnStr = "0A" + Integer.toString((0x80 + len), 16);// "09"
                    RtnStr = RtnStr
                            + String.format("%02x", Integer.parseInt(EncodeStr));

                }
                byte[] TmpBytes = EncodeStr.getBytes();
                for (int i = 0; i < TmpBytes.length; i++) {
                    RtnStr += String.format("%02x", TmpBytes[i]);
                }
                break;
            // case visible_string:
            // if (EncodeStr.length() < 128)
            // {
            // RtnStr = "0A" + String.format("%02x%n",Integer.parseInt(EncodeStr));
            // }
            // else
            // {
            // RtnStr = "0A81" +
            // String.format("%02x%n",Integer.parseInt(EncodeStr));
            // }
            // byte[] TmpByteArr = EncodeStr.getBytes();
            // for (int i = 0; i < TmpByteArr.length; i++)
            // {
            // RtnStr +=String.format("%02x%n",TmpByteArr[i]);
            // }
            // break;
            // case integer:
            // RtnStr = "0F" + EncodeStr;
            // break;
            // case long:
            // RtnStr = "10" + EncodeStr;
            // break;
            case U8:// LYH
                RtnStr = "11" + String.format("%02x", Integer.parseInt(EncodeStr));
                break;
            // case "unsigned":
            // RtnStr = "11" + EncodeStr;
            // break;
            // case "unsigned8_decimal":
            // RtnStr = "11" + String.format("%02x%n",Integer.parseInt(EncodeStr));
            // break;
            // case "unsigned8_workMode":
            // RtnStr = "11";
            // switch (EncodeStr)
            // {
            // case "Normal mode":
            // case "Normal0mode":
            // RtnStr += "00";
            // break;
            // case "Pre-payment mode":
            // case "Pre-payment0mode":
            // RtnStr += "01";
            // break;
            // }
            // break;
            // case "unsigned8_limiterActive":
            // RtnStr = "11";
            // switch (EncodeStr)
            // {
            // case "Deactive":
            // RtnStr += "00";
            // break;
            // case "Active":
            // RtnStr += "01";
            // break;
            // }
            // break;
            // case long_unsigned:
            // RtnStr = "12" + EncodeStr;
            // break;
            // case unsigned16_decimal:
            // case unsigned16:
            case U16:
                RtnStr = "12" + String.format("%04x", Integer.parseInt(EncodeStr));
                break;
            // case "unsigned16_hex":
            // RtnStr = "12" + String.format("%04x%n",Integer.parseInt(EncodeStr));
            // break;
            // case long64:
            // RtnStr = "14" + EncodeStr;
            // break;
            case Int64:
                RtnStr = "14" + String.format("%16x", Long.parseLong(EncodeStr));
                break;
            // case enum:
            // RtnStr = "16" + EncodeStr;
            // break;
            // case "MulEnum":
            // RtnStr = "16";
            // switch (EncodeStr)
            // {
            // case "WhUnit":
            // RtnStr += "1E";
            // break;
            // case "VarhUnit":
            // RtnStr += "20";
            // break;
            // case "WUnit":
            // RtnStr += "1B";
            // break;
            // case "varUnit":
            // RtnStr += "1D";
            // break;
            // case "AUnit":
            // RtnStr += "21";
            // break;
            // case "VUnit":
            // RtnStr += "23";
            // break;
            // case "None":
            // RtnStr += "FF";
            // break;
            // }
            // break;
            case float32:
                RtnStr = "17";
                byte[] byteArray = new byte[4];
                byteArray = getBytes(Float.parseFloat(EncodeStr));
                for (int i = 3; i >= 0; i--) {
                    RtnStr += String.format("%02x", byteArray[i]);
                }
                break;
            case time:
                RtnStr = "0904";
                RtnStr += String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(0, 2)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(2, 4)));
                RtnStr += "FFFF";
                break;
            case HH_mm_ss:
                RtnStr = "0904";
                RtnStr += String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(0, 2)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(2, 4)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(4, 6)));
                RtnStr += "FF";
                break;
            case mm_dd:
                RtnStr = "090CFFFF";
                RtnStr += String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(0, 2)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(2, 4)));
                RtnStr += "FFFFFFFF80000000";
                break;
            case yy_mm_dd:
                int Year = 0xFFFF;
                String YearHigh = "FF";
                String YearLow = "FF";
                if (EncodeStr.length() > 4) {
                    Year = 2000 + Integer.parseInt(EncodeStr.substring(0, 2));
                    YearHigh = String.format("%02x", Year / 256);
                    YearLow = String.format("%02x", Year % 256);
                    EncodeStr = EncodeStr.substring(2);
                }
                RtnStr = "0905" + YearHigh + YearLow;
                RtnStr += String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(0, 2)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(2, 4)));
                RtnStr += "FF";
                break;
            case yyyy_mm_dd:
                int year = 0xFFFF;
                String yearHigh = "FF";
                String yearLow = "FF";
                if (EncodeStr.length() > 4) {
                    year = Integer.parseInt(EncodeStr.substring(0, 4));
                    yearHigh = String.format("%02x", year / 256);
                    yearLow = String.format("%02x", year % 256);
                    EncodeStr = EncodeStr.substring(4);
                }
                RtnStr = "0905" + yearHigh + yearLow;
                RtnStr += String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(0, 2)))
                        + String.format("%02x",
                        Integer.parseInt(EncodeStr.substring(2, 4)));
                RtnStr += "FF";
                break;
            case Octs_datetime:
                Date dt = new Date();

                SimpleDateFormat dateFormat2 = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                try {
                    dt = dateFormat2.parse(EncodeStr);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String WeekStr = getWeekOfDate(dt);
                RtnStr = "090C";

                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
                int year1 = cal.get(Calendar.YEAR);// 获取年份
                int month = cal.get(Calendar.MONTH) + 1;// 获取月份
                int day = cal.get(Calendar.DATE);// 获取日
                int hour = cal.get(Calendar.HOUR_OF_DAY);// 24制小时
                int minute = cal.get(Calendar.MINUTE);// 分
                int second = cal.get(Calendar.SECOND);// 秒

                RtnStr += String.format("%04x", year1);
                RtnStr += String.format("%02x", month);
                RtnStr += String.format("%02x", day);
                RtnStr += WeekStr;
                RtnStr += String.format("%02x", hour);
                RtnStr += String.format("%02x", minute);
                RtnStr += String.format("%02x", second);
                RtnStr += "FF";
                RtnStr += "01E0";
                RtnStr += "00";
                break;
            // case "clock_manual":
            // break;
        }
        return RtnStr;
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static String fnChangeOBIS(String strOBIS) {
        String strData = "";
        try {
            String strTmp = "";
            String[] str = strOBIS.split("#");//, StringSplitOptions.RemoveEmptyEntries);
            if (str.length == 3) {
                StringBuilder strB = new StringBuilder();

                strB.append(String.format("%04x", Integer.parseInt(str[0], 10)));
                String[] strMain = str[1].toString().split("\\.");
                for (int i = 0; i < strMain.length; i++) {
                    strB.append(String.format("%02x", Integer.parseInt(strMain[i], 10)));
                }
                strB.append(String.format("%02x", Integer.parseInt(str[2], 10)));
                strB.append("00");
                strData = strB.toString();
            } else {
                strData = strOBIS;
            }

        } catch (Exception ex) {
            strData = strOBIS;
        }
        ;
        return strData;
    }

    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"00", "01", "02", "03", "04", "05", "06"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * 字节数组转为单精度（符点数） 小端
     *
     * @param bytes
     * @return
     */
    public static float getFloat(byte[] bytes) {

        return Float.intBitsToFloat(getInt(bytes));
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8))
                | (0xff0000 & (bytes[2] << 16))
                | (0xff000000 & (bytes[3] << 24));
    }

    public static String FormatValue(String StrValue, int ValInt,
                                     int ValDeciml, double CTval, double PTval, DateType dataType) {
        switch (dataType) {
            case YYMMDDhhmmssYYMMDDhhmmss:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(12, 14) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(12, 14) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(14, 16) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(14, 16) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(16, 18) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(16, 18) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                }
                return StrValue;
            case YYMMDDhhmmss:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case yyyy_MM_dd:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                }
                return StrValue;
            case YYMMDDhhmm:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case yyyy_MM_dd:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                }
                return StrValue;
            case YYYYMMDD:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8);
                return StrValue;
            case YYYYMMDDhhmmss:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8)
                        + " " + StrValue.substring(8, 10) + ":"
                        + StrValue.substring(10, 12) + ":"
                        + StrValue.substring(12, 14);
                return StrValue;
            case YYYYMMDDhhmm:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8)
                        + " " + StrValue.substring(8, 10) + ":"
                        + StrValue.substring(10, 12);
                return StrValue;
            case MMDDhhmm:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(0, 2) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(0, 2) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(2, 4) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(2, 4) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(4, 6) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                }
                StrValue = StrValue.substring(0, 2) + "-"
                        + StrValue.substring(2, 4) + " " + StrValue.substring(4, 6)
                        + ":" + StrValue.substring(6, 8);
                return StrValue;
            case HHmmss:
                StrValue = StrValue.substring(0, 2) + ":"
                        + StrValue.substring(2, 4) + ":" + StrValue.substring(4, 6);
                return StrValue;
            case YYMMDDhhmmssNNNN:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                }
                return StrValue;
            case NNNNNNNN:
            case NNNN:
                return StrValue;
            default:
                return StrValue;
            // if (ValDeciml != 0) {
            // StrValue = StrValue.substring(0,ValInt) + "." +
            // StrValue.substring(ValInt,StrValue.length());
            // }
            // try {
            // double RtnValue = Double.parseDouble(StrValue);
            // RtnValue = RtnValue * CTval * PTval;
            // RtnValue = new
            // java.text.DecimalFormat("#.00").format(RtnValue);//
            // 最多保留ValDeciml位小数
            // if (CTval * PTval >= 1000)// 此处有待修改 变单位
            // {
            // RtnValue = RtnValue / 1000;
            // return RtnValue.ToString() + "E+3";
            // }
            // return RtnValue.ToString();
            // } catch (Exception ex) {
            // return StrValue;
            // }
        }
    }

}
