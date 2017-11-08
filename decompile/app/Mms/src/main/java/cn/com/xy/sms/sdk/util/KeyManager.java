package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.net.l;

/* compiled from: Unknown */
public class KeyManager {
    public static String channel = null;

    public static String getAppKey() {
        return l.b;
    }

    public static void initAppKey() {
        if (channel == null) {
            channel = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CHANNEL);
        }
        if (StringUtils.isNull(l.a)) {
            String str;
            if ("PKWBZlRIbwLENOVO".equals(channel)) {
                str = "LENOVO";
                l.b = str;
            } else {
                if ("hdtKldgsdkgo".equals(channel)) {
                    str = "GOSMS";
                } else if ("J8KeTyOROASamsungReminder".equals(channel)) {
                    str = "SReminder";
                } else if ("TGsTZewAYUN".equals(channel)) {
                    str = "OSYUN";
                } else if ("KQIDAQABLEV".equals(channel)) {
                    str = "LENOVO2";
                } else if ("BwIDAQABFROG".equals(channel)) {
                    str = "LEFROG";
                } else if ("XwIDAQABYUN".equals(channel)) {
                    str = "BJYUNOS";
                } else if ("NQIDAQABCOOL".equals(channel)) {
                    str = "COOLPAD";
                } else if ("SAMOPERATORYQIDAQAB".equals(channel)) {
                    str = "SAMOPERATOR";
                } else if ("SAMBANKVwIDAQAB".equals(channel)) {
                    str = "SAMBANK";
                } else if ("SAMCLASSFIYVwIDAQAB".equals(channel)) {
                    str = "SAMCLASSFIY";
                } else if ("6QIDAQABSTARRYSKY".equals(channel)) {
                    str = "STARRYSKY";
                } else if ("vwIDAQABLIANLUOOS".equals(channel)) {
                    str = "LIANLUOOS";
                } else if ("5xKI47wSAMALL".equals(channel)) {
                    str = "SAMALL";
                } else if ("FEhNrwHTXL".equals(channel)) {
                    str = "HTXL";
                } else if ("SAMALLxKI47w".equals(channel)) {
                    str = "SAMALL";
                } else if ("VMhlWdEwVNEW_LENOVO".equals(channel)) {
                    str = "VNEW_LENOVO";
                } else if ("jE5vSv5QPIAO".equals(channel)) {
                    str = "XYPIAO";
                } else if ("GwIDAQABZTE".equals(channel)) {
                    str = "ZTE";
                } else if ("1i1BDH2wONE+".equals(channel)) {
                    str = "ONE+";
                } else if ("1w36SBLwVNEW_ZTE".equals(channel)) {
                    str = "VNEW_ZTE";
                } else if ("Oq3iD6UlMAGIC".equals(channel)) {
                    str = "MAGIC";
                } else if ("7kRgxjdwVNEW_STARRYSKY".equals(channel)) {
                    str = "VNEW_STARRYSKY";
                } else if ("D6mKXM8MEIZU".equals(channel)) {
                    str = "MEIZU";
                } else if ("rq7Fyxl5DUOQU".equals(channel)) {
                    str = "DUOQU";
                } else if ("3GdfMSKwHUAWEI".equals(channel)) {
                    str = "HUAWEI";
                } else if ("j3FIT5mwLETV".equals(channel)) {
                    str = "LETV";
                } else if ("1i1BDH2wONE+CARD".equals(channel)) {
                    str = "ONE+CARD";
                } else if ("0GCSqGSITOS".equals(channel)) {
                    str = "TOS";
                } else if ("UM0srSjQ365".equals(channel)) {
                    str = "365";
                } else if ("YHMesqOQCOOL".equals(channel)) {
                    str = "COOL";
                } else if ("5Mj22a4wHUAWEICARD".equals(channel)) {
                    str = "HUAWEICARD";
                } else if ("wupzCqnwGUAIWU".equals(channel)) {
                    str = "GUAIWU";
                } else if ("XRyvMvZwSMARTISAN".equals(channel)) {
                    str = "SMARTISAN";
                } else if ("MEIZUPAYGJw".equals(channel)) {
                    str = "MEIZUPAY";
                } else if ("dToXA5JQDAKELE".equals(channel)) {
                    str = "DAKELE";
                } else if ("p5O4wKmwGIONEE".equals(channel)) {
                    str = "GIONEE";
                } else if ("z5N7W51wKINGSUN".equals(channel)) {
                    str = "KINGSUN";
                } else if ("Cko59T6wSUGAR".equals(channel)) {
                    str = "SUGAR";
                } else if ("oWIH+3ZQLEIDIANOS".equals(channel)) {
                    str = "LEIDIANOS";
                } else if ("XYTEST".equals(channel)) {
                    str = "XYTEST";
                } else if ("al30zFgQTEST_T".equals(channel)) {
                    str = "TEST_T";
                } else if ("gsjHPHwIKOOBEE".equals(channel)) {
                    str = "KOOBEE";
                } else if ("QlTNSIgQWENTAI2".equals(channel)) {
                    str = "WENTAI2";
                } else if ("JqyMtaHQNUBIA".equals(channel)) {
                    str = "NUBIA";
                } else if ("15Du354QGIONEECARD".equals(channel)) {
                    str = "GIONEECARD";
                } else if ("rahtBH7wTCL".equals(channel)) {
                    str = "TCL";
                } else if ("xU6UT6pwTOS2".equals(channel)) {
                    str = "TOS2";
                } else if ("5Gx84kmwYULONG_COOLPAD".equals(channel)) {
                    str = "YULONG_COOLPAD";
                } else if ("Uj2pznXQHCT".equals(channel)) {
                    str = "HCT";
                } else if ("tnjdWFeQKTOUCH".equals(channel)) {
                    str = "KTOUCH";
                } else if ("XkXZJmwIPPTV".equals(channel)) {
                    str = "PPTV";
                } else if ("dGxSiEbwTOSCARD".equals(channel)) {
                    str = "TOSCARD";
                } else if ("PzqP0ONQTOSWATCH".equals(channel)) {
                    str = "TOSWATCH";
                } else if ("VCTyBOSwSmartisan".equals(channel)) {
                    str = "Smartisan";
                } else if ("5rLWVKgQMEITU_PHONE".equals(channel)) {
                    str = "MEITU_PHONE";
                } else if ("HUAWEIAND".equals(channel)) {
                    str = "HUAWEIAND";
                } else if ("HUAWEITMW".equals(channel)) {
                    str = "HUAWEITMW";
                } else if ("zcK2P6yQINNOS".equals(channel)) {
                    str = "INNOS";
                } else if ("RbWRsTYQdroi".equals(channel)) {
                    str = "droi";
                } else if ("J2kSrxdQGigaset".equals(channel)) {
                    str = "Gigaset";
                } else if ("5zZZdrFQIUNI".equals(channel)) {
                    str = "IUNI";
                } else if ("nZpg6u3wDOOV".equals(channel)) {
                    str = "DOOV";
                } else if ("RQIDAQABONEPLUSCARDNEW".equals(channel)) {
                    str = "ONEPLUSCARDNEW";
                } else if ("i3GPvZLwASUS".equals(channel)) {
                    str = "ASUS";
                } else if ("NsJCCyFwPHILIPS".equals(channel)) {
                    str = "PHILIPS";
                } else if ("cNNrw5WQEBEN".equals(channel)) {
                    str = "EBEN";
                } else if ("UdcqV6aQLANMO".equals(channel)) {
                    str = "LANMO";
                } else if ("PunKwZfwHISENSE".equals(channel)) {
                    str = "HISENSE";
                } else if ("DAS9exiQQIKUBOX".equals(channel)) {
                    str = "QIKUBOX";
                } else if ("gO0o2CXwVIVO".equals(channel)) {
                    str = "VIVO";
                } else if ("kpGIJXywSAMSUNGFLOW".equals(channel)) {
                    str = "SAMSUNGFLOW";
                } else if ("DEaerxdwASUSCARD".equals(channel)) {
                    str = "ASUSCARD";
                } else if ("d7tjnrkwCNSAMSUNG".equals(channel)) {
                    str = "CNSAMSUNG";
                } else if ("NVbQx3QQMEIZUCENTER".equals(channel)) {
                    str = "MEIZUCENTER";
                } else if ("K8wgPuIwFREEMEOS".equals(channel)) {
                    str = "FREEMEOS";
                } else if ("uDM3hYtwGIGASET".equals(channel)) {
                    str = "GIGASET";
                } else if ("OmwdltCwONEPLUS2".equals(channel)) {
                    str = "ONEPLUS2";
                } else if ("eOXJhLyQLINGHIT".equals(channel)) {
                    str = "LINGHIT";
                } else if ("ZkhM4GyQ360OS".equals(channel)) {
                    str = "360OS";
                } else if ("mmNPM4cQVNEW_ZTE2".equals(channel)) {
                    str = "VNEW_ZTE2";
                } else if (SmartSmsSdkUtil.DUOQU_SDK_CHANNEL.equals(channel)) {
                    str = "HUAWEI2";
                } else if ("oxvw9DvQTCLFLOW".equals(channel)) {
                    str = "TCLFLOW";
                } else if ("Hg9iPQ4wLIFENUM_A".equals(channel)) {
                    str = "LIFENUM_A";
                } else if ("vRICR8qQYULONG_COOLPAD2".equals(channel)) {
                    str = "YULONG_COOLPAD2";
                } else if ("v22YJ3QwKINGSOFTMAIL".equals(channel)) {
                    str = "KINGSOFTMAIL";
                } else if ("W5MmRZCwIMOO".equals(channel)) {
                    str = "IMOO";
                } else if ("XHpWJNFQTCLOS".equals(channel)) {
                    str = "TCLOS";
                } else if ("R1pU1XXwUNISCOPE".equals(channel)) {
                    str = "UNISCOPE";
                } else if ("gOLrCBhQMEIZU2".equals(channel)) {
                    str = "MEIZU2";
                } else if ("MkekV0RQRAGENTEK".equals(channel)) {
                    str = "RAGENTEK";
                } else if ("rNllyzbwLAKALA".equals(channel)) {
                    str = "LAKALA";
                } else if ("YVmD5UkQ360OSBOX".equals(channel)) {
                    str = "360OSBOX";
                } else if ("MXUnXjvw360FLOW".equals(channel)) {
                    str = "360FLOW";
                } else if ("sX7t39KQMEIZUDATA".equals(channel)) {
                    str = "MEIZUDATA";
                } else if ("2qqJKJbwZTE_TRIP".equals(channel)) {
                    str = "ZTE_TRIP";
                } else if ("0LLy0INQWEHOME".equals(channel)) {
                    str = "WEHOME";
                } else if ("n2zkSOdwZTE3".equals(channel)) {
                    str = "ZTE3";
                } else if ("VrWc0QnQNUBIACARD".equals(channel)) {
                    str = "NUBIACARD";
                } else if ("AINYCzUwMEIZUCENTER2".equals(channel)) {
                    str = "MEIZUCENTER2";
                } else if ("LLJ53XOw360CONTACTS".equals(channel)) {
                    str = "360CONTACTS";
                }
                l.b = str;
            }
            l.a = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.SECRETKEY);
            NewXyHttpRunnable.RSA_PRV_KEY = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RSAPRVKEY);
        }
        if (StringUtils.isNull(l.b)) {
            throw new Exception("无效的渠道");
        }
    }
}
