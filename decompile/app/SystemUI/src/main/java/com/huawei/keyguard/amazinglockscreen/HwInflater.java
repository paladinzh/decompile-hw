package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.view.View;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenView;
import com.huawei.keyguard.HwUnlockConstants$StateViewSubType;
import com.huawei.keyguard.amazinglockscreen.data.HwResManager;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HwInflater {
    private Context mContext;

    public HwInflater(Context context) {
        this.mContext = context;
        HwResManager.getInstance().clearCache();
    }

    private void parseState(Node stateNode, AmazingLockScreen lockscreen) {
        HwStateView stateViews = new HwStateView(this.mContext, stateNode.getAttributes());
        stateViews.registerOnTriggerCallback(lockscreen);
        lockscreen.addView(stateViews);
        NodeList childNodes = stateNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String nodeName = childNode.getNodeName();
            NamedNodeMap attr = childNode.getAttributes();
            HwStateViewSub stateViewSub;
            if ("normal".equals(nodeName)) {
                stateViewSub = new HwStateViewSub(this.mContext, attr, HwUnlockConstants$StateViewSubType.NORMAL, lockscreen);
                stateViews.addView(stateViewSub);
                parseLayout(childNode, stateViewSub);
            } else if ("press".equals(nodeName)) {
                stateViewSub = new HwStateViewSub(this.mContext, attr, HwUnlockConstants$StateViewSubType.PRESS, lockscreen);
                stateViews.addView(stateViewSub);
                parseLayout(childNode, stateViewSub);
            }
        }
    }

    public View parseLayoutFromXml(Document document, AmazingLockScreen lockScreen) {
        Element rootElement = document.getDocumentElement();
        if (rootElement == null) {
            HwLog.w("HwLockScreenInflater", "parseLayoutFromXml root element null");
            return null;
        }
        boolean versionFlag = HwInflaterCommon.parseVersionFlag(rootElement);
        HwLog.d("HwLockScreenInflater", "HwInfalter parseLayoutFromXml versionFlag=" + versionFlag);
        AmazingUtils.calScreenScale(this.mContext);
        if (versionFlag) {
            lockScreen.setFrameRate(parseFrameRate(rootElement));
            AmazingUtils.calScaleParaEMUI30(this.mContext, rootElement);
            new HwInflater2(this.mContext).parseLayoutFromXml(rootElement, lockScreen);
        } else {
            NodeList childNodes = rootElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                HwLog.d("HwLockScreenInflater", "childNode.getNodeName() is " + childNode.getNodeName());
                if ("static".equals(childNode.getNodeName())) {
                    parseLayout(childNode, lockScreen);
                } else if ("state".equals(childNode.getNodeName())) {
                    parseState(childNode, lockScreen);
                }
            }
        }
        return lockScreen;
    }

    private void parseTouchRect(NamedNodeMap attrs, HwStateViewSub parent) {
        String x = "0";
        String y = "0";
        String w = "0";
        String h = "0";
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if ("x".equalsIgnoreCase(name)) {
                x = value;
            } else if ("y".equalsIgnoreCase(name)) {
                y = value;
            } else if ("w".equalsIgnoreCase(name)) {
                w = value;
            } else if ("h".equalsIgnoreCase(name)) {
                h = value;
            }
        }
        float scale = AmazingUtils.getScalePara();
        if (scale != 1.0f) {
            x = ((int) (((float) Integer.parseInt(x)) * scale)) + BuildConfig.FLAVOR;
            y = ((int) (((float) Integer.parseInt(y)) * scale)) + BuildConfig.FLAVOR;
            w = ((int) (((float) Integer.parseInt(w)) * scale)) + BuildConfig.FLAVOR;
            h = ((int) (((float) Integer.parseInt(h)) * scale)) + BuildConfig.FLAVOR;
        }
        parent.setTouchRect(x, y, w, h);
    }

    private void parseLayout(Node node, HwStateViewSub parent) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeItem = nodeList.item(i);
            String name = nodeItem.getNodeName();
            NamedNodeMap attrs = nodeItem.getAttributes();
            HwUnlockInterface$HwLockScreenView view = null;
            if ("text".equalsIgnoreCase(name)) {
                view = HwInflaterCommon.parseTextViewNode(nodeItem, this.mContext);
            } else if ("image".equalsIgnoreCase(name)) {
                view = parseImageView(nodeItem);
            } else if ("unlockintent".equalsIgnoreCase(name)) {
                HwUnlockIntent intent = parseUnlockIntent(attrs, parent);
                if (parent != null) {
                    parent.addIntent(intent);
                }
            } else if ("touchrect".equalsIgnoreCase(name) && parent != null) {
                parseTouchRect(attrs, parent);
            }
            if (!(view == null || parent == null)) {
                parent.addView((View) view);
            }
        }
    }

    private void parseLayout(Node node, AmazingLockScreen parent) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeItem = nodeList.item(i);
            String name = nodeItem.getNodeName();
            View view = null;
            if ("text".equalsIgnoreCase(name)) {
                view = HwInflaterCommon.parseTextViewNode(nodeItem, this.mContext);
            } else if ("image".equalsIgnoreCase(name)) {
                view = parseImageView(nodeItem);
            }
            if (!(view == null || parent == null)) {
                parent.addView(view);
            }
        }
    }

    private HwImageView parseImageView(Node imageNode) {
        NamedNodeMap attrs = imageNode.getAttributes();
        HwImageView result = new HwImageView(this.mContext);
        String x = "0";
        String y = "0";
        String w = "0";
        String h = "0";
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if ("x".equalsIgnoreCase(name)) {
                x = value;
            } else if ("y".equalsIgnoreCase(name)) {
                y = value;
            } else if ("w".equalsIgnoreCase(name)) {
                w = value;
            } else if ("h".equalsIgnoreCase(name)) {
                h = value;
            } else if ("src".equalsIgnoreCase(name)) {
                result.setContent(value);
            } else if ("id".equalsIgnoreCase(name)) {
                result.setId(value);
            } else if ("visible".equalsIgnoreCase(name)) {
                result.setVisiblityProp(value);
            } else if ("mask".equalsIgnoreCase(name)) {
                result.setImageBitmapSrc(value, true);
            } else if ("maskX".equalsIgnoreCase(name)) {
                result.setMaskPositionX(value);
            } else if ("maskY".equalsIgnoreCase(name)) {
                result.setMaskPositionY(value);
            }
        }
        result.setLayout(x, y, w, h);
        HwInflaterCommon.parseAnimationSet(this.mContext, imageNode, result);
        return result;
    }

    private HwUnlockIntent parseUnlockIntent(NamedNodeMap attrs, HwUnlockInterface$ConditionCallback conditionCallback) {
        String type = "default";
        String condition = BuildConfig.FLAVOR;
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if ("type".equalsIgnoreCase(name)) {
                type = value;
                if (value.equalsIgnoreCase("hwmessage")) {
                    type = "message";
                }
            } else if ("condition".equalsIgnoreCase(name)) {
                condition = value;
            }
        }
        return new HwUnlockIntent(this.mContext, condition, type, conditionCallback);
    }

    private int parseFrameRate(Element rootElement) {
        NamedNodeMap rootAttrs = rootElement.getAttributes();
        int frameRate = 33;
        if (rootAttrs == null) {
            return 33;
        }
        int attrsLength = rootAttrs.getLength();
        for (int j = 0; j < attrsLength; j++) {
            String name = rootAttrs.item(j).getNodeName();
            String value = rootAttrs.item(j).getNodeValue();
            if ("frameRate".equalsIgnoreCase(name)) {
                frameRate = Integer.parseInt(value);
                break;
            }
        }
        if (frameRate < 0) {
            frameRate = 33;
        }
        return frameRate;
    }
}
