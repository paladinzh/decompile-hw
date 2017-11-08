package com.huawei.systemmanager.customize;

import android.util.Xml;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlConfigParser {
    private static final String ATTR_ENABLE = "enable";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TYPE = "type";
    private static String CONFIG_FILE_NAME = composeCustFileName("xml/hsm_config.xml");
    private static final String TAG_CONFIG = "config";
    private static final String TAG_FEATURE = "feature";

    public static Map<String, String> parseConfig() {
        HashMap<String, String> map = new HashMap();
        getValueFromXml(map);
        return map;
    }

    private static void getValueFromXml(HashMap<String, String> configMap) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Exception e6;
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            File custFile = new File(CONFIG_FILE_NAME);
            if (custFile.exists()) {
                FileInputStream fileInputStream2 = new FileInputStream(custFile);
                try {
                    XmlPullParser xmlPullParser = Xml.newPullParser();
                    xmlPullParser.setInput(fileInputStream2, null);
                    parseProtectListInner(configMap, xmlPullParser);
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e7) {
                            e7.printStackTrace();
                        }
                    }
                    fileInputStream = fileInputStream2;
                } catch (NullPointerException e8) {
                    e2 = e8;
                    fileInputStream = fileInputStream2;
                    e2.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e72) {
                            e72.printStackTrace();
                        }
                    }
                } catch (NumberFormatException e9) {
                    e3 = e9;
                    fileInputStream = fileInputStream2;
                    e3.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e722) {
                            e722.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e10) {
                    e4 = e10;
                    fileInputStream = fileInputStream2;
                    e4.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7222) {
                            e7222.printStackTrace();
                        }
                    }
                } catch (IOException e11) {
                    e7222 = e11;
                    fileInputStream = fileInputStream2;
                    e7222.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e72222) {
                            e72222.printStackTrace();
                        }
                    }
                } catch (IndexOutOfBoundsException e12) {
                    e5 = e12;
                    fileInputStream = fileInputStream2;
                    e5.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e722222) {
                            e722222.printStackTrace();
                        }
                    }
                } catch (Exception e13) {
                    e6 = e13;
                    fileInputStream = fileInputStream2;
                    try {
                        e6.printStackTrace();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e7222222) {
                                e7222222.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e72222222) {
                                e72222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
        } catch (NullPointerException e14) {
            e2 = e14;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (NumberFormatException e15) {
            e3 = e15;
            e3.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (XmlPullParserException e16) {
            e4 = e16;
            e4.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e17) {
            e72222222 = e17;
            e72222222.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IndexOutOfBoundsException e18) {
            e5 = e18;
            e5.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Exception e19) {
            e6 = e19;
            e6.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private static void parseProtectListInner(HashMap<String, String> configMap, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int type = 0;
        do {
            if (xmlPullParser != null) {
                type = xmlPullParser.next();
                if (type == 2) {
                    String tag = xmlPullParser.getName();
                    if (TAG_FEATURE.equals(tag)) {
                        configMap.put(xmlPullParser.getAttributeValue(null, "name"), xmlPullParser.getAttributeValue(null, "enable"));
                    } else if (TAG_CONFIG.equals(tag)) {
                        configMap.put(xmlPullParser.getAttributeValue(null, "name"), xmlPullParser.getAttributeValue(null, "type"));
                    }
                }
            }
        } while (type != 1);
    }

    private static String composeCustFileName(String baseName) {
        File file = null;
        try {
            file = HwCfgFilePolicy.getCfgFile(baseName, 0);
        } catch (NoExtAPIException e) {
            HwLog.e("Customize", "HwCfgFilePolicy.getCfgFile not supported.");
        } catch (NoClassDefFoundError e2) {
            HwLog.e("Customize", "HwCfgFilePolicy.getCfgFile not supported.");
        } catch (Exception e3) {
            HwLog.e("Customize", "Exception");
        }
        if (file == null) {
            return "/data/cust/" + baseName;
        }
        return file.getAbsolutePath();
    }
}
