package com.huawei.powergenie.core;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.core.security.DecodeXmlFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class XmlHelper {
    private XmlHelper() {
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean loadResAppList(Context context, int resFileId, String type, ArrayList<String> outAppList) {
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getXml(resFileId);
            if (xmlResourceParser == null) {
                Log.e("XmlHelper", "There is no res app list file!");
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return false;
            }
            boolean parseAppList = parseAppList(xmlResourceParser, type, outAppList);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return parseAppList;
        } catch (NotFoundException e) {
            e.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    public static boolean loadCustAppList(String fileName, String type, ArrayList<String> outAppList) {
        FileNotFoundException e;
        XmlPullParserException e2;
        Throwable th;
        boolean ret = false;
        InputStream inputStream = null;
        String custPath = "/product/etc/hwpg/";
        try {
            if (!new File(custPath, fileName).exists()) {
                custPath = "/system/etc/";
                if (!new File(custPath, fileName).exists()) {
                    return false;
                }
            }
            InputStream in = new FileInputStream(custPath + fileName);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);
                ret = parseAppList(parser, type, outAppList);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        Log.w("XmlHelper", "Close Input stream error!");
                    }
                }
                inputStream = in;
            } catch (FileNotFoundException e4) {
                e = e4;
                inputStream = in;
                e.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.w("XmlHelper", "Close Input stream error!");
                    }
                }
                return ret;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                inputStream = in;
                try {
                    e2.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e7) {
                            Log.w("XmlHelper", "Close Input stream error!");
                        }
                    }
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                            Log.w("XmlHelper", "Close Input stream error!");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return ret;
        } catch (XmlPullParserException e10) {
            e2 = e10;
            e2.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return ret;
        }
        return ret;
    }

    private static boolean parseAppList(XmlPullParser parser, String type, ArrayList<String> outAppList) {
        boolean targetType = false;
        try {
            beginDocument(parser, "list");
            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    return true;
                }
                if ("item".equals(tag)) {
                    if (targetType) {
                        outAppList.add(parser.nextText());
                    }
                } else if ("array".equals(tag)) {
                    if (type == null) {
                        targetType = true;
                    } else if (type.equals(parser.getAttributeValue(0))) {
                        targetType = true;
                    } else {
                        targetType = false;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            return false;
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
            return false;
        } catch (IOException e4) {
            e4.printStackTrace();
            return false;
        }
    }

    public static InputStream setParserAssetsInputStream(Context context, String modeStr, XmlPullParser parser) {
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        boolean isOk = false;
        try {
            inputStream = context.getAssets().open(modeStr);
            inputStream2 = DecodeXmlFile.getDecodeInputStream(inputStream);
            if (inputStream2 != null) {
                parser.setInput(inputStream2, "UTF-8");
            }
            isOk = true;
            if (1 == null && inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                inputStream2 = null;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (XmlPullParserException oe) {
            Log.e("XmlHelper", "Fail to get XmlPullParser!", oe);
            if (null == null && inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
                inputStream2 = null;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
        } catch (IOException ie) {
            Log.e("XmlHelper", modeStr + " not exist in assets!", ie);
            if (null == null && inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
                inputStream2 = null;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
        } catch (Exception e222222) {
            Log.e("XmlHelper", "error to decode, in assets: " + modeStr, e222222);
            if (null == null && inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e2222222) {
                    e2222222.printStackTrace();
                }
                inputStream2 = null;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22222222) {
                    e22222222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (null == null && inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Exception e222222222) {
                    e222222222.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2222222222) {
                    e2222222222.printStackTrace();
                }
            }
        }
        if (isOk) {
            return inputStream2;
        }
        throw new RuntimeException("fail read: " + modeStr);
    }
}
