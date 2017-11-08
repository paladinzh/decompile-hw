package com.huawei.systemmanager.customize;

import android.text.TextUtils;
import android.util.Xml;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DefValueXmlHelper {
    public static final int ALLOW = 1;
    private static final String ATTR_ALLOW = "allow";
    private static final String ATTR_NAME = "name";
    private static final String ELE_TAG = "package";
    public static final int FORBID = 2;
    public static final int NOCONFIG = 0;
    private static final String TAG = DefValueXmlHelper.class.getSimpleName();
    private String mAssertFile = null;
    private Map<String, Boolean> mCachedConfigs = null;
    private String mCustFile = null;

    public DefValueXmlHelper(String diskFile) {
        this.mCustFile = diskFile;
    }

    public DefValueXmlHelper(String diskFile, String assertFile) {
        this.mCustFile = diskFile;
        this.mAssertFile = assertFile;
    }

    public synchronized int getDefaultConfig(String pkg) {
        if (pkg == null) {
            return 0;
        }
        if (this.mCachedConfigs == null) {
            HwLog.i(TAG, "mCachedConfigs = null, init.");
            this.mCachedConfigs = new HashMap();
            if (!TextUtils.isEmpty(this.mAssertFile)) {
                HwLog.i(TAG, "Assert exist");
                this.mCachedConfigs.putAll(getAssertConfigs(this.mAssertFile));
            }
            this.mCachedConfigs.putAll(getFileConfigs(this.mCustFile));
        }
        if (!this.mCachedConfigs.containsKey(pkg)) {
            return 0;
        }
        if (((Boolean) this.mCachedConfigs.get(pkg)).booleanValue()) {
            return 1;
        }
        return 2;
    }

    private Map<String, Boolean> getFileConfigs(String filename) {
        Exception e;
        Throwable th;
        Map<String, Boolean> results = new HashMap();
        FileInputStream fileInputStream = null;
        try {
            File custFile = new File(filename);
            if (custFile.exists()) {
                FileInputStream fileInputStream2 = new FileInputStream(custFile);
                try {
                    getConfigs(results, fileInputStream2);
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    fileInputStream = fileInputStream2;
                } catch (Exception e3) {
                    e = e3;
                    fileInputStream = fileInputStream2;
                    try {
                        e.printStackTrace();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        return results;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
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
                return results;
            }
            HwLog.i(TAG, "Invalid configs file :" + this.mCustFile);
            return results;
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return results;
        }
    }

    private Map<String, Boolean> getAssertConfigs(String assertFileName) {
        Map<String, Boolean> results = new HashMap();
        InputStream inputStream = null;
        try {
            inputStream = GlobalContext.getContext().getAssets().open(assertFileName);
            getConfigs(results, inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        }
        return results;
    }

    private void getConfigs(Map<String, Boolean> results, InputStream inputStream) {
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, null);
            parseXmlInner(results, xmlPullParser);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (NumberFormatException e4) {
            e4.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (XmlPullParserException e5) {
            e5.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (IOException e3222) {
            e3222.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32222) {
                    e32222.printStackTrace();
                }
            }
        } catch (IndexOutOfBoundsException e6) {
            e6.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322222) {
                    e322222.printStackTrace();
                }
            }
        } catch (Exception e7) {
            e7.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3222222) {
                    e3222222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32222222) {
                    e32222222.printStackTrace();
                }
            }
        }
    }

    private static void parseXmlInner(Map<String, Boolean> results, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int type = 0;
        do {
            if (xmlPullParser != null) {
                type = xmlPullParser.next();
                if (type == 2) {
                    if ("package".equals(xmlPullParser.getName())) {
                        results.put(xmlPullParser.getAttributeValue(null, "name"), Boolean.valueOf("true".equals(xmlPullParser.getAttributeValue(null, "allow"))));
                    }
                }
            }
        } while (type != 1);
    }
}
