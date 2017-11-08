package com.android.settings.colortemper;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorXmlReader {
    private static boolean DEBUG;
    private float alphab;
    private float alphac;
    private float alphag;
    private float alpham;
    private float alphar;
    private float alphay;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable("ColorXmlReader", 4) : false : true;
        DEBUG = isLoggable;
    }

    public boolean getConfig() throws IOException {
        Exception e;
        String xmlPath = String.format("/xml/%s", new Object[]{"colortemperature.xml"});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile == null) {
            Slog.e("ColorXmlReader", "get xmlFile :" + xmlPath + " failed!");
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream2) && checkConfigLoadedFromXML()) {
                    printConfigFromXML();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                return true;
            } catch (Exception e2) {
                e = e2;
                inputStream = inputStream2;
                try {
                    e.printStackTrace();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return true;
                }
            } catch (Throwable th2) {
                inputStream = inputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                return true;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        }
    }

    private boolean isInValid(float value) {
        if (value >= 0.0f && value <= 1.0f) {
            return false;
        }
        Slog.e("ColorXmlReader", "invalid value found: " + value);
        return true;
    }

    private boolean checkConfigLoadedFromXML() {
        if (isInValid(this.alphar) || isInValid(this.alphag) || isInValid(this.alphab) || isInValid(this.alphac) || isInValid(this.alpham) || isInValid(this.alphay)) {
            loadDefaultConfig();
            Slog.e("ColorXmlReader", "LoadXML as wrong value, LoadDefaultConfig!");
            return false;
        }
        if (DEBUG) {
            Slog.i("ColorXmlReader", "checkConfigLoadedFromXML success!");
        }
        return true;
    }

    public void printConfigFromXML() {
        Slog.i("ColorXmlReader", "alphar = " + this.alphar);
        Slog.i("ColorXmlReader", "alphag = " + this.alphag);
        Slog.i("ColorXmlReader", "alphab = " + this.alphab);
        Slog.i("ColorXmlReader", "alphac = " + this.alphac);
        Slog.i("ColorXmlReader", "alpham = " + this.alpham);
        Slog.i("ColorXmlReader", "alphay = " + this.alphay);
    }

    public void loadDefaultConfig() {
        this.alphar = 0.96f;
        this.alphag = 0.95f;
        this.alphab = 0.95f;
        this.alphac = 0.95f;
        this.alpham = 0.96f;
        this.alphay = 0.9f;
        if (DEBUG) {
            printConfigFromXML();
        }
    }

    public boolean getConfigFromXML(InputStream inStream) {
        boolean mRLoaded = false;
        boolean mGLoaded = false;
        boolean mBLoaded = false;
        boolean mCLoaded = false;
        boolean mMLoaded = false;
        boolean mYLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        String name = parser.getName();
                        if (!"AlphaR".equals(name)) {
                            if (!"AlphaG".equals(name)) {
                                if (!"AlphaB".equals(name)) {
                                    if (!"AlphaC".equals(name)) {
                                        if (!"AlphaM".equals(name)) {
                                            if (!"AlphaY".equals(name)) {
                                                break;
                                            }
                                            this.alphay = Float.parseFloat(parser.nextText());
                                            mYLoaded = true;
                                            break;
                                        }
                                        this.alpham = Float.parseFloat(parser.nextText());
                                        mMLoaded = true;
                                        break;
                                    }
                                    this.alphac = Float.parseFloat(parser.nextText());
                                    mCLoaded = true;
                                    break;
                                }
                                this.alphab = Float.parseFloat(parser.nextText());
                                mBLoaded = true;
                                break;
                            }
                            this.alphag = Float.parseFloat(parser.nextText());
                            mGLoaded = true;
                            break;
                        }
                        this.alphar = Float.parseFloat(parser.nextText());
                        mRLoaded = true;
                        break;
                    case 3:
                        if (!"Colors".equals(parser.getName())) {
                            break;
                        }
                        Slog.i("ColorXmlReader", "getConfigFromeXML end!");
                        break;
                    default:
                        break;
                }
            }
            if (mRLoaded && mGLoaded && mBLoaded && mCLoaded && mMLoaded && mYLoaded) {
                Slog.i("ColorXmlReader", "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        Slog.e("ColorXmlReader", "getConfigFromeXML failed!");
        return false;
    }

    public float getAlphaR() {
        return this.alphar;
    }

    public float getAlphaG() {
        return this.alphag;
    }

    public float getAlphaB() {
        return this.alphab;
    }

    public float getAlphaC() {
        return this.alphac;
    }

    public float getAlphaM() {
        return this.alpham;
    }

    public float getAlphaY() {
        return this.alphay;
    }
}
