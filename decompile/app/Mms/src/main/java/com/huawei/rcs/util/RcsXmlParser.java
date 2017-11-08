package com.huawei.rcs.util;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.util.Xml;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.android.internal.util.XmlUtils;
import com.huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RcsXmlParser {
    private static String RCS_DEFAULTS_FILEPATH = "/data/cust/xml/rcs_defaults.xml";
    private static Context mContext;
    private static boolean mForTest = SystemProperties.getBoolean("ro.config.hw_rcs_test", false);
    private static boolean mInitFinshed = false;
    private static ArrayList<Attribute> rcsProperty = new ArrayList();
    private static CarrierConfigManager sCfgMgr = null;
    private static final PersistableBundle sConfigDefaults = new PersistableBundle();

    private static class Attribute {
        String name;
        String value;

        private Attribute() {
        }
    }

    static {
        sConfigDefaults.putString("huawei_rcs_enabler", "false");
        sConfigDefaults.putString("huawei_rcs_switcher", "1");
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_ENA_DM_HTTP_X_UP_CALLING_LINE_ID", "0");
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_ENA_SIP_REG_ID", "0");
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_IM_MSG_TECH", ThemeUtil.SET_NULL_STR);
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_ENA_SIP_TAG_CMCC_GPMANAGE", "0");
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_FORCE_IM_MODE", ThemeUtil.SET_NULL_STR);
        sConfigDefaults.putString("RCS_CARRIER_CONFIG_REQ_TO_SEND_DELI", "0");
        sConfigDefaults.putString("hw_ims_voip_toast_on", "true");
        sConfigDefaults.putString("Platform", "RCSAndrd");
        sConfigDefaults.putString("VersionMajor", "2");
        sConfigDefaults.putString("VersionMinor", "0");
        sConfigDefaults.putString("beta_support", "false");
        sConfigDefaults.putString("dm_ip", "");
        sConfigDefaults.putString("dm_port", "80");
        sConfigDefaults.putString("dm_http_port", "80");
        sConfigDefaults.putString("dm_https_port", "443");
        sConfigDefaults.putString("dm_mode", "2");
        sConfigDefaults.putString("ims_ip", "");
        sConfigDefaults.putString("ims_port", "");
        sConfigDefaults.putString("ims_domain", "");
        sConfigDefaults.putString("CONFIG_MAJOR_MULTI_DEV", "1");
        sConfigDefaults.putString("CONFIG_MAJOR_GROUP_DEPARTED", "1");
        sConfigDefaults.putString("CONFIG_MAJOR_IM_USE_BASE64", "0");
        sConfigDefaults.putString("CONFIG_MAJOR_CPIM_ANONYMOUS", "1");
        sConfigDefaults.putString("CONFIG_MAJOR_GROUP_REJOIN_WITH_RECIPIENT_LIST", "0");
        sConfigDefaults.putString("GSMA_KEEP_RESUME_SWITCH", "true");
        sConfigDefaults.putString("CONFIG_MAJOR_NAT_KEEP_ALIVE", "0");
        sConfigDefaults.putString("CONFIG_MAJOR_GROUP_IDLE_EXIT_SEND_BYE", "0");
        sConfigDefaults.putString("CONFIG_MAJOR_GSMA_HDR_CHECK", "1");
        sConfigDefaults.putString("CONFIG_MAJOR_TYPE_KEEP_ALIVE_RSP_TIMER_LEN", "0");
        sConfigDefaults.putString("CONFIG_MAJOR_SUPT_VIDEO_CODEC", "no");
        sConfigDefaults.putString("CONFIG_THUMBNAIL_ICON", "false");
        sConfigDefaults.putString("show_rcs_disconnect_notify_in_MMS", "false");
        sConfigDefaults.putString("group_invit_auto_accept_switcher", "1");
        sConfigDefaults.putString("CONFIG_CUST_PARA_ENABLE_ALL_LOG", "yes");
        sConfigDefaults.putString("huawei_rcs_vdf_user_treaty", "true");
        sConfigDefaults.putString("CONFIG_RCS_USER_GUIDE_ENABLE", "1");
        sConfigDefaults.putString("CONFIG_GROUPCHAT_NICKNAME_ENABLE", "1");
        sConfigDefaults.putString("CONFIG_MAJOR_AUTO_RESEND_POLICY", "1");
        sConfigDefaults.putString("CONFIG_RCS_GRACE_UNDELIVERED_FLAG", "true");
        sConfigDefaults.putString("is_show_undelivered_icon", "true");
        sConfigDefaults.putString("is_enable_group_silentmode", "true");
        sConfigDefaults.putString("is_location_enable_cust", "false");
        sConfigDefaults.putString("CONFIG_GSMA_IMAGE_SHARE_COMPRESS", "true");
        sConfigDefaults.putString("INCOMING_FT_CHANGE_MODE", "true");
        sConfigDefaults.putString("once_again_login_mode", NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR);
        sConfigDefaults.putString("CONFIG_RCS_POPUP_WHEN_UNDELIVERED", "false");
        sConfigDefaults.putString("CONFIG_GROUPCHAT_MEMBER_TOPIC_ENABLE", "1");
        sConfigDefaults.putString("hw_rcs_contact_icon_on", "false");
        sConfigDefaults.putString("support_capability_validity", "true");
        sConfigDefaults.putString("VDF_FEATURE_FOR_SWITCH_JOYNCLIENT", "true");
        sConfigDefaults.putString("ui_order_support", "false");
        sConfigDefaults.putString("CONFIG_MAJOR_DELAY_BROADCAST_FAILURE", "1");
        sConfigDefaults.putString("mms_disconnected_notify_enable", "true");
        sConfigDefaults.putString("RCS_VIDEO_SHARE_TIMEOUT_LIMIT", "15");
        sConfigDefaults.putString("is_support_ft_outdate", "false");
        sConfigDefaults.putString("dm_domain", "");
        sConfigDefaults.putString("sms_port", "");
        sConfigDefaults.putString("timeOutRejectFileSwitch", "");
        sConfigDefaults.putString("KEEP_ALIVE_TIMER_CIRCLE", "");
        sConfigDefaults.putString("Config_setKeepAliveEnable", "");
        sConfigDefaults.putString("CONFIG_MAJOR_IS_SUPPORT_UNDELIVERED", "");
        sConfigDefaults.putString("hw_rcs_version", "0");
        sConfigDefaults.putString("is_support_LocationShare", "false");
    }

    public static void initParser(Context context) {
        if (mForTest) {
            parserAllCfgXML();
        } else {
            mContext = context;
        }
    }

    public static boolean getBoolean(String name, boolean defValue) {
        String value = getValueByNameFromXml(name);
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        return defValue;
    }

    public static int getInt(String name, int defValue) {
        try {
            return Integer.parseInt(getValueByNameFromXml(name));
        } catch (NumberFormatException e) {
            MLog.e("RcsXmlParser", "getInt NumberFormatException");
            return defValue;
        }
    }

    public static String getValueByNameFromXml(String name) {
        if (!mForTest) {
            String value = "";
            if (name == null) {
                MLog.d("RcsXmlParser", "name is null ,return empty string!");
                return "";
            }
            value = getValueFromCarrierConfig(name);
            if (value == null) {
                value = getValueFromLocalStore(name);
            }
            if (value == null) {
                value = sConfigDefaults.getString(name);
            }
            if (value == null) {
                value = "";
            }
            return value;
        } else if ("huawei_rcs_enabler".equals(name)) {
            return "true";
        } else {
            return getValueByNameFromXmlForTest(name);
        }
    }

    private static String getValueFromCarrierConfig(String name) {
        if (mContext == null) {
            MLog.e("RcsXmlParser", "mContext is null ,return empty string!");
            return null;
        }
        if (sCfgMgr == null) {
            sCfgMgr = (CarrierConfigManager) mContext.getSystemService("carrier_config");
            if (sCfgMgr == null) {
                MLog.d("RcsXmlParser", "sCfgMgr is null ,return empty string!");
                return null;
            }
        }
        PersistableBundle carrierConfig = null;
        try {
            carrierConfig = sCfgMgr.getConfig();
        } catch (SecurityException e) {
            MLog.e("RcsXmlParser", "getConfig error, SecurityException.");
        }
        if (carrierConfig != null) {
            return carrierConfig.getString(name);
        }
        MLog.e("RcsXmlParser", "carrierConfig= null");
        return null;
    }

    private static String getValueFromLocalStore(String name) {
        if (mContext != null) {
            return mContext.createDeviceProtectedStorageContext().getSharedPreferences("rcs_defaults", 0).getString(name, null);
        }
        MLog.e("RcsXmlParser", "mContext is null ,return empty string!");
        return null;
    }

    private static String getValueByNameFromXmlForTest(String name) {
        String value = "";
        if (name == null || rcsProperty == null) {
            MLog.d("RcsXmlParser", "name or rcsProperty is null ,return empty string!");
            return "";
        }
        if (!mInitFinshed) {
            parserAllCfgXML();
        }
        if (rcsProperty.isEmpty()) {
            MLog.d("RcsXmlParser", "the rcs attribute is empty in memory!");
            return "";
        }
        int length = rcsProperty.size();
        boolean findName = false;
        for (int index = 0; index < length; index++) {
            if (name.equals(((Attribute) rcsProperty.get(index)).name)) {
                value = ((Attribute) rcsProperty.get(index)).value;
                findName = true;
                break;
            }
        }
        if (!findName) {
            MLog.d("RcsXmlParser", name + " can not be found in the rcs xml !");
            return "";
        } else if (value != null) {
            return value;
        } else {
            MLog.d("RcsXmlParser", name + " but the value is null !");
            return "";
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int xmlParser(String path) {
        try {
            BufferedReader rcsExReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "UTF-8"));
            XmlPullParser xmlPullParser = null;
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(rcsExReader);
                XmlUtils.beginDocument(xmlPullParser, "resources");
                while (true) {
                    XmlUtils.nextElement(xmlPullParser);
                    if (xmlPullParser.getName() == null) {
                        break;
                    }
                    Object value = xmlPullParser.getAttributeValue(0);
                    if (value != null) {
                        MLog.d("RcsXmlParser", xmlPullParser.getAttributeName(0) + "  read from xml the value= " + value.toString());
                        addProperty(xmlPullParser.getAttributeName(0), value.toString());
                    }
                }
                MLog.d("RcsXmlParser", " no other information for this xml file !");
                try {
                    rcsExReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return 0;
            } catch (XmlPullParserException e3) {
                MLog.w("RcsXmlParser", "XmlPullParserException." + e3.toString());
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
                return -1;
            } catch (IOException e4) {
                MLog.w("RcsXmlParser", "IOException." + e4.toString());
                try {
                    rcsExReader.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
                return -1;
            } catch (Exception e22222) {
                MLog.w("RcsXmlParser", "Exception." + e22222.toString());
                try {
                    rcsExReader.close();
                } catch (Exception e222222) {
                    e222222.printStackTrace();
                }
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e2222222) {
                    e2222222.printStackTrace();
                }
                return -1;
            } catch (Throwable th) {
                try {
                    rcsExReader.close();
                } catch (Exception e22222222) {
                    e22222222.printStackTrace();
                }
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e222222222) {
                    e222222222.printStackTrace();
                }
            }
        } catch (FileNotFoundException e5) {
            MLog.d("RcsXmlParser", "do not find the rcs_defaults.xml!");
            return -1;
        } catch (UnsupportedEncodingException e6) {
            MLog.d("RcsXmlParser", " xml encoding is not supported!");
            e6.printStackTrace();
            return -1;
        }
    }

    private static boolean parserAllCfgXML() {
        boolean z = true;
        ArrayList<File> cfgFileList = null;
        try {
            cfgFileList = HwCfgFilePolicy.getCfgFileList("xml/rcs_defaults.xml", 0);
        } catch (NoClassDefFoundError e) {
            MLog.e("RcsXmlParser", "class HwCfgFilePolicy not found error");
        } catch (Exception e2) {
            MLog.e("RcsXmlParser", "class HwCfgFilePolicy exception");
        }
        if (cfgFileList == null || cfgFileList.size() == 0) {
            if (xmlParser(RCS_DEFAULTS_FILEPATH) != 0) {
                z = false;
            }
            mInitFinshed = z;
        } else {
            for (File cfg : cfgFileList) {
                boolean z2;
                MLog.d("RcsXmlParser", "cfgpath = " + cfg.getPath());
                if (xmlParser(cfg.getPath()) != 0) {
                    z2 = mInitFinshed;
                } else {
                    z2 = true;
                }
                mInitFinshed = z2;
            }
        }
        return mInitFinshed;
    }

    private static void addProperty(String name, String value) {
        if (name != null && value != null) {
            int size = rcsProperty.size();
            boolean hasProp = false;
            for (int index = 0; index < size; index++) {
                Attribute att = (Attribute) rcsProperty.get(index);
                if (name.equals(att.name) && !value.equals(att.value)) {
                    att.value = value;
                    hasProp = true;
                    break;
                }
            }
            if (!hasProp) {
                Attribute attributeInformation = new Attribute();
                attributeInformation.name = name;
                attributeInformation.value = value;
                rcsProperty.add(attributeInformation);
            }
        }
    }

    public static PersistableBundle getDefaultConfig() {
        return sConfigDefaults;
    }
}
