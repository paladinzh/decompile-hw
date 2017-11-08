package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FastgrabConfigReader {
    private static String CONFIG_FILEPATH = "/system/emui/china/xml/hw_bastet_partner.xml";
    private static String CONFIG_UPDATE_FILEPATH = "/data/bastet/hw_bastet_partner.xml";
    public static final int INVALID_VALUE = -1;
    private static final String TAG = "FastgrabConfigReader";
    private static final int VALID_VERSION_FORMAT_LENGTH = 2;
    private static final String XML_ATTR_SWITCH = "switch";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final HashMap<String, String> mAppNameToNodeName = new HashMap<String, String>() {
        {
            put("com.tencent.mm", "wechat");
            put("com.eg.android.AlipayGphone", "alipay");
        }
    };
    private static FastgrabConfigReader mFastgrabConfigReader = null;
    private HashMap<String, String> mAppConfig = new HashMap();
    private int mVersionCode;

    private FastgrabConfigReader() {
    }

    private FastgrabConfigReader(Context context) {
        parseFile(getProcessName(), context);
    }

    public static synchronized FastgrabConfigReader getInstance(Context context) {
        FastgrabConfigReader fastgrabConfigReader;
        synchronized (FastgrabConfigReader.class) {
            if (mFastgrabConfigReader == null && context != null) {
                mFastgrabConfigReader = new FastgrabConfigReader(context);
            }
            fastgrabConfigReader = mFastgrabConfigReader;
        }
        return fastgrabConfigReader;
    }

    private String getProcessName() {
        String processName = Process.getCmdlineForPid(Process.myPid());
        if (mAppNameToNodeName.containsKey(processName)) {
            return processName;
        }
        return null;
    }

    private void parseFile(String processName, Context context) {
        Throwable th;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && processName != null) {
            this.mAppConfig.clear();
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(processName, 0);
                if (info != null) {
                    this.mVersionCode = info.versionCode;
                    StringBuilder log = new StringBuilder();
                    log.append("process:").append(processName).append("\nversion:").append(this.mVersionCode);
                    File file = new File(CONFIG_UPDATE_FILEPATH);
                    if (!file.exists()) {
                        file = new File(CONFIG_FILEPATH);
                        if (!file.exists()) {
                            AwareLog.e(TAG, "config file is not exist!");
                            return;
                        }
                    }
                    InputStream inputStream = null;
                    try {
                        InputStream is = new FileInputStream(file);
                        try {
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(is, StandardCharsets.UTF_8.name());
                            int outerDepth = parser.getDepth();
                            while (true) {
                                int type = parser.next();
                                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                                    closeStream(is, parser);
                                    inputStream = is;
                                } else if (type == 2 && XML_TAG_IAWARE.equals(parser.getName())) {
                                    parseIaware(parser, processName, log);
                                    closeStream(is, parser);
                                    return;
                                }
                            }
                            closeStream(is, parser);
                            inputStream = is;
                        } catch (XmlPullParserException e) {
                            inputStream = is;
                            AwareLog.e(TAG, "failed parsing switch file parser error");
                            closeStream(inputStream, null);
                        } catch (IOException e2) {
                            inputStream = is;
                            AwareLog.e(TAG, "failed parsing switch file IO error ");
                            closeStream(inputStream, null);
                        } catch (NumberFormatException e3) {
                            inputStream = is;
                            try {
                                AwareLog.e(TAG, "switch number format error");
                                closeStream(inputStream, null);
                            } catch (Throwable th2) {
                                th = th2;
                                closeStream(inputStream, null);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = is;
                            closeStream(inputStream, null);
                            throw th;
                        }
                    } catch (XmlPullParserException e4) {
                        AwareLog.e(TAG, "failed parsing switch file parser error");
                        closeStream(inputStream, null);
                    } catch (IOException e5) {
                        AwareLog.e(TAG, "failed parsing switch file IO error ");
                        closeStream(inputStream, null);
                    } catch (NumberFormatException e6) {
                        AwareLog.e(TAG, "switch number format error");
                        closeStream(inputStream, null);
                    }
                }
            } catch (NameNotFoundException e7) {
                AwareLog.e(TAG, "parse version failed ! appName = " + processName);
            }
        }
    }

    private void closeStream(InputStream is, XmlPullParser parser) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "close file input stream fail!");
            }
        }
        if (parser != null) {
            try {
                ((KXmlParser) parser).close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "parser close error");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseIaware(XmlPullParser parser, String processName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                String tagName = parser.getName();
                if (type == 2 && tagName != null && tagName.equals(mAppNameToNodeName.get(processName))) {
                    break;
                }
            }
        }
    }

    private void parseVersion(XmlPullParser parser, String tagName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (XML_TAG_CONFIG.equals(parser.getName())) {
                    String supportVersion = parser.getAttributeValue(null, XML_ATTR_VERSION);
                    if (supportVersion != null) {
                        String[] versionStartAndEnd = supportVersion.split("-");
                        if (versionStartAndEnd.length == 2 && this.mVersionCode >= Integer.parseInt(versionStartAndEnd[0]) && this.mVersionCode <= Integer.parseInt(versionStartAndEnd[1])) {
                            parseApp(parser, tagName, log);
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private void parseApp(XmlPullParser parser, String appName, StringBuilder log) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                String value = parser.nextText();
                if (!(tagName == null || value == null)) {
                    this.mAppConfig.put(tagName, strFormat(value));
                }
            }
        }
        for (Entry<String, String> entry : this.mAppConfig.entrySet()) {
            log.append("\n").append((String) entry.getKey()).append(":").append((String) entry.getValue());
        }
        AwareLog.i(TAG, "---------- parse complete ----------\n" + log.toString());
    }

    public int getInt(String tagName) {
        String str = (String) this.mAppConfig.get(tagName);
        if (str != null) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "failed convert " + str + " to integer, format error!");
            }
        }
        return -1;
    }

    public String getString(String tagName) {
        return (String) this.mAppConfig.get(tagName);
    }

    private String strFormat(String rawStr) {
        if (rawStr == null) {
            return null;
        }
        char[] charArray = rawStr.toCharArray();
        int size = charArray.length;
        StringBuilder sb = new StringBuilder(rawStr.length());
        int m = 0;
        while (m < size) {
            if ('\\' == charArray[m] && m <= size - 6 && 'u' == charArray[m + 1]) {
                char cc = '\u0000';
                for (int n = 0; n < 4; n++) {
                    char ch = charArray[(m + n) + 2];
                    if ((ch < '0' || ch > '9') && ((ch < 'A' || ch > 'F') && (ch < 'a' || ch > 'f'))) {
                        cc = '\u0000';
                        break;
                    }
                    cc = (char) ((Character.digit(ch, 16) << ((3 - n) * 4)) | cc);
                }
                if (cc > '\u0000') {
                    sb.append(cc);
                    m += 5;
                    m++;
                }
            }
            sb.append(charArray[m]);
            m++;
        }
        return sb.toString();
    }
}
