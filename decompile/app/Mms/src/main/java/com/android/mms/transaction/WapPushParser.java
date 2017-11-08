package com.android.mms.transaction;

import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.cspcommon.MLog;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.kxml2.wap.WbxmlParser;
import org.xmlpull.v1.XmlPullParserException;

public class WapPushParser {
    private static final String[] ATTR_START_TABLE_SI = new String[]{"action=signal-none", "action=signal-low", "action=signal-medium", "action=signal-high", "action=signal-delete", "created", "href", "href=http://", "href=http://www.", "href=https://", "href=https://www.", "si-expires", "si-id", "class"};
    private static final String[] ATTR_START_TABLE_SL = new String[]{"action=execute-low", "action=execute-high", "action=cache", "href", "href=http://", "href=http://www.", "href=https://", "href=https://www."};
    private static final String[] ATTR_VALUE_TABLE_SI_SL = new String[]{".com/", ".edu/", ".net/", ".org/"};
    private static Hashtable<String, Integer> PUSH_ATTR_NAME_MAP = new Hashtable(5);
    private static Hashtable<String, Integer> PUSH_ATTR_VALUE_MAP = new Hashtable(7);
    private static final String[] TAG_TABLE_SI = new String[]{"si", "indication", "info", "item"};
    private static final String[] TAG_TABLE_SL = new String[]{"sl"};
    private WbxmlParser mParser = null;
    private WapPushMsg mPushMsg = null;
    private ByteArrayInputStream mWapPushDataStream = null;

    public WapPushParser(byte[] pushDataStream) {
        this.mWapPushDataStream = new ByteArrayInputStream(pushDataStream);
    }

    public WapPushMsg parse(int type) {
        if (this.mWapPushDataStream == null) {
            MLog.e("WapPushParser", "mWapPushDataStream is not set!");
            return null;
        }
        this.mPushMsg = new WapPushMsg(type);
        if (type == 0) {
            this.mParser = createSIParser();
        } else if (1 == type) {
            this.mParser = createSLParser();
        } else {
            MLog.e("WapPushParser", "wap push unknown type=" + type);
            return null;
        }
        try {
            this.mParser.setInput(this.mWapPushDataStream, null);
            MLog.i("WapPushParser", "Document charset : " + this.mParser.getInputEncoding());
            int eventType = this.mParser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 0:
                        MLog.i("WapPushParser", "Start document");
                        break;
                    case 2:
                        MLog.i("WapPushParser", "Start tag = " + this.mParser.getName());
                        elementParser(this.mParser.getName());
                        break;
                    case 3:
                        MLog.i("WapPushParser", "End tag = " + this.mParser.getName());
                        break;
                    case 4:
                        if (type == 0) {
                            this.mPushMsg.setAttributeValue(4, this.mParser.getText());
                            break;
                        }
                        break;
                    default:
                        MLog.i("WapPushParser", "unknown event type =  " + eventType);
                        break;
                }
                eventType = this.mParser.next();
            }
            return this.mPushMsg;
        } catch (IOException e) {
            MLog.e("WapPushParser", e.toString());
            return null;
        } catch (XmlPullParserException e2) {
            MLog.e("WapPushParser", e2.toString());
            return null;
        }
    }

    private static WbxmlParser createSIParser() {
        WbxmlParser p = new WbxmlParser();
        p.setTagTable(0, TAG_TABLE_SI);
        p.setAttrStartTable(0, ATTR_START_TABLE_SI);
        p.setAttrValueTable(0, ATTR_VALUE_TABLE_SI_SL);
        return p;
    }

    private static WbxmlParser createSLParser() {
        WbxmlParser p = new WbxmlParser();
        p.setTagTable(0, TAG_TABLE_SL);
        p.setAttrStartTable(0, ATTR_START_TABLE_SL);
        p.setAttrValueTable(0, ATTR_VALUE_TABLE_SI_SL);
        return p;
    }

    private void elementParser(String tagName) {
        int attrCount = this.mParser.getAttributeCount();
        if (tagName.equalsIgnoreCase(TAG_TABLE_SL[0]) || tagName.equalsIgnoreCase(TAG_TABLE_SI[0]) || tagName.equalsIgnoreCase(TAG_TABLE_SI[1])) {
            for (int i = 0; i < attrCount; i++) {
                String attrValue = this.mParser.getAttributeValue(i);
                String attrName = this.mParser.getAttributeName(i);
                if (PUSH_ATTR_NAME_MAP.containsKey(attrName)) {
                    this.mPushMsg.setAttributeValue(((Integer) PUSH_ATTR_NAME_MAP.get(attrName)).intValue(), attrValue);
                }
            }
            return;
        }
        MLog.e("WapPushParser", "Unknown tag = " + tagName);
    }

    static {
        PUSH_ATTR_NAME_MAP.put("action", Integer.valueOf(2));
        PUSH_ATTR_NAME_MAP.put("href", Integer.valueOf(1));
        PUSH_ATTR_NAME_MAP.put("si-expires", Integer.valueOf(6));
        PUSH_ATTR_NAME_MAP.put("created", Integer.valueOf(5));
        PUSH_ATTR_NAME_MAP.put("si-id", Integer.valueOf(3));
        PUSH_ATTR_VALUE_MAP.put("signal-none", Integer.valueOf(5));
        PUSH_ATTR_VALUE_MAP.put("signal-low", Integer.valueOf(0));
        PUSH_ATTR_VALUE_MAP.put("signal-medium", Integer.valueOf(1));
        PUSH_ATTR_VALUE_MAP.put("signal-high", Integer.valueOf(2));
        PUSH_ATTR_VALUE_MAP.put("signal-delete", Integer.valueOf(3));
        PUSH_ATTR_VALUE_MAP.put("execute-low", Integer.valueOf(0));
        PUSH_ATTR_VALUE_MAP.put("execute-high", Integer.valueOf(2));
        PUSH_ATTR_VALUE_MAP.put(MapTilsCacheAndResManager.MAP_CACHE_PATH_NAME, Integer.valueOf(4));
    }
}
