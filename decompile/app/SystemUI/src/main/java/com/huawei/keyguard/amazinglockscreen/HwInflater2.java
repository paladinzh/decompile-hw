package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import com.huawei.keyguard.amazinglockscreen.HwUnlocker.EndPoint;
import com.huawei.keyguard.amazinglockscreen.data.ExpressParser;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HwInflater2 {
    private Context mContext;
    private ArrayList<HwImageView> mHwImageArray = new ArrayList();
    private ArrayList<HwTextView> mHwTextViewArray = new ArrayList();
    private AmazingLockScreen mLockScreen;
    private HwMusicController mMusicController;

    public HwInflater2(Context context) {
        this.mContext = context;
        cleanCache();
    }

    public View parseLayoutFromXml(Element rootElements, AmazingLockScreen lockScreen) {
        if (rootElements == null || lockScreen == null) {
            return null;
        }
        NodeList childNodes = rootElements.getChildNodes();
        this.mLockScreen = lockScreen;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String nodeName = childNode.getNodeName();
            HwLog.w("HwInflater2", "nodeName is " + nodeName);
            View view;
            if ("image".equals(nodeName)) {
                view = parseImageView(childNode);
                if (view != null) {
                    this.mLockScreen.addView(view);
                }
            } else if ("text".equals(nodeName)) {
                view = parseTextView(childNode);
                if (view != null) {
                    this.mLockScreen.addView(view);
                }
            } else if ("button".equalsIgnoreCase(nodeName)) {
                parseVirtualButton(childNode, false);
            } else if ("MusicControl".equalsIgnoreCase(nodeName)) {
                parseMusicControl(childNode);
            } else if ("unlocker".equalsIgnoreCase(nodeName)) {
                parseUnlocker(childNode);
            }
        }
        return this.mLockScreen;
    }

    private HwTextView parseTextView(Node textNode) {
        HwTextView result = HwInflaterCommon.parseTextViewNode(textNode, this.mContext);
        if (result.getTextViewName() != null) {
            this.mHwTextViewArray.add(result);
        }
        return result;
    }

    private HwImageView parseImageView(Node imageNode) {
        if (imageNode == null) {
            return null;
        }
        NamedNodeMap attrs = imageNode.getAttributes();
        HwImageView result = new HwImageView(this.mContext);
        String x = "0";
        String y = "0";
        String w = "0";
        String h = "0";
        boolean addFlag = false;
        String sourceName = "0";
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            HwLog.w("HwInflater2", "name is " + name + " and value is " + value);
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
            } else if ("name".equalsIgnoreCase(name)) {
                result.setImageName(value);
                if (!value.isEmpty()) {
                    addFlag = true;
                }
            } else if ("masktype".equalsIgnoreCase(name)) {
                result.setMaskType(value);
            }
        }
        result.setLayout(x, y, w, h);
        HwInflaterCommon.parseAnimationSet(this.mContext, imageNode, result);
        if (addFlag) {
            this.mHwImageArray.add(result);
        }
        return result;
    }

    private HwVirtualButton parseVirtualButton(Node virtualButton, boolean musicFlag) {
        if (virtualButton == null) {
            return null;
        }
        NamedNodeMap attrs = virtualButton.getAttributes();
        HwVirtualButton result = new HwVirtualButton(this.mContext);
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
            } else if (!("src".equalsIgnoreCase(name) || "id".equalsIgnoreCase(name))) {
                if ("visible".equalsIgnoreCase(name)) {
                    result.setVisiblityProp(value);
                } else if ("name".equalsIgnoreCase(name)) {
                    result.setButtonName(value);
                }
            }
        }
        result.setTouchRect(x, y, w, h);
        return addView(virtualButton, result);
    }

    private HwVirtualButton addView(Node virtualButton, HwVirtualButton hwVirtualButton) {
        NodeList nodeList = virtualButton.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node nodeItem = nodeList.item(i);
            String nodeName = nodeItem.getNodeName();
            if ("triggers".equalsIgnoreCase(nodeName)) {
                parseXmlTriggerNode(nodeItem, hwVirtualButton);
            } else if ("NormalState".equalsIgnoreCase(nodeName) || "PressedState".equalsIgnoreCase(nodeName)) {
                NodeList nodes = nodeItem.getChildNodes();
                int nodeLength = nodes.getLength();
                for (int j = 0; j < nodeLength; j++) {
                    Node viewNode = nodes.item(j);
                    String childNodeName = viewNode.getNodeName();
                    if ("text".equalsIgnoreCase(childNodeName)) {
                        this.mLockScreen.addView(parseTextView(viewNode));
                    } else if ("image".equalsIgnoreCase(childNodeName)) {
                        this.mLockScreen.addView(parseImageView(viewNode));
                    }
                }
            }
        }
        this.mLockScreen.addTriggerButton(hwVirtualButton);
        return hwVirtualButton;
    }

    private void parseXmlTriggerNode(Node triggerNode, HwVirtualButton hwAmazingButton) {
        if (triggerNode != null && hwAmazingButton != null) {
            NodeList nodeList = triggerNode.getChildNodes();
            int length = nodeList.getLength();
            for (int j = 0; j < length; j++) {
                Node node = nodeList.item(j);
                if ("trigger".equalsIgnoreCase(node.getNodeName())) {
                    parseXmlCommand(node, hwAmazingButton);
                }
            }
        }
    }

    private void parseXmlCommand(Node cmdNode, HwVirtualButton hwAmazingButton) {
        if (cmdNode != null && hwAmazingButton != null) {
            NamedNodeMap triggerAttrs = cmdNode.getAttributes();
            int attrLength = triggerAttrs.getLength();
            for (int i = 0; i < attrLength; i++) {
                String triggerName = triggerAttrs.item(i).getNodeName();
                String triggerValue = triggerAttrs.item(i).getNodeValue();
                if ("action".equalsIgnoreCase(triggerName)) {
                    hwAmazingButton.setTriggerAction(triggerValue);
                }
                if ("name".equalsIgnoreCase(triggerName)) {
                    hwAmazingButton.setTriggerName(triggerValue);
                }
            }
            NodeList nodeList = cmdNode.getChildNodes();
            int length = nodeList.getLength();
            for (int j = 0; j < length; j++) {
                Node node = nodeList.item(j);
                if ("command".equalsIgnoreCase(node.getNodeName())) {
                    parseXmlCmdAttrs(node, hwAmazingButton);
                }
            }
        }
    }

    private void parseXmlCmdAttrs(Node cmdNode, HwVirtualButton hwAmazingButton) {
        if (cmdNode != null && hwAmazingButton != null) {
            NamedNodeMap cmdAttrs = cmdNode.getAttributes();
            int attrLength = cmdAttrs.getLength();
            View hwImageView = null;
            String cmdAttrValue = BuildConfig.FLAVOR;
            View hwTextView = null;
            for (int i = 0; i < attrLength; i++) {
                String cmdAttrName = cmdAttrs.item(i).getNodeName();
                cmdAttrValue = cmdAttrs.item(i).getNodeValue();
                if ("target".equalsIgnoreCase(cmdAttrName)) {
                    hwImageView = getHwImageView(cmdAttrValue);
                    hwTextView = getHwTextView(cmdAttrValue);
                } else if ("value".equalsIgnoreCase(cmdAttrName)) {
                    hwAmazingButton.setTargetValue(cmdAttrValue);
                } else if ("visible".endsWith(cmdAttrName)) {
                    hwAmazingButton.setVisiblityProp(cmdAttrValue);
                }
            }
            if (hwImageView != null) {
                hwAmazingButton.addView(hwImageView, hwAmazingButton.getVisibityProp());
            } else if (hwTextView != null) {
                hwAmazingButton.addView(hwTextView, hwAmazingButton.getVisibityProp());
            } else if (this.mMusicController != null) {
                hwAmazingButton.addView(this.mMusicController, hwAmazingButton.getVisibityProp());
            }
        }
    }

    private HwImageView getHwImageView(String targetName) {
        int length = this.mHwImageArray.size();
        for (int i = 0; i < length; i++) {
            HwImageView hwImageView = (HwImageView) this.mHwImageArray.get(i);
            if (targetName.equalsIgnoreCase(hwImageView.getImageName())) {
                return hwImageView;
            }
        }
        return null;
    }

    private HwTextView getHwTextView(String name) {
        int length = this.mHwTextViewArray.size();
        for (int i = 0; i < length; i++) {
            HwTextView hwTextView = (HwTextView) this.mHwTextViewArray.get(i);
            if (name.equalsIgnoreCase(hwTextView.getTextViewName())) {
                return hwTextView;
            }
        }
        return null;
    }

    private void parseUnlocker(Node unlockNode) {
        if (unlockNode != null) {
            int i;
            NamedNodeMap attrs = unlockNode.getAttributes();
            HwUnlocker result = new HwUnlocker(this.mContext);
            for (i = 0; i < attrs.getLength(); i++) {
                String name = attrs.item(i).getNodeName();
                String value = attrs.item(i).getNodeValue();
                if ("name".equalsIgnoreCase(name)) {
                    result.setName(value);
                    ExpressParser mExpressParser = ExpressParser.getInstance();
                    String state = result.getName() + "_state";
                    String moveX = result.getName() + "_moveX";
                    String moveY = result.getName() + "_moveY";
                    mExpressParser.setSystemValue(state, Integer.valueOf(0));
                    mExpressParser.parse(state);
                    mExpressParser.setSystemValue(moveX, Integer.valueOf(0));
                    mExpressParser.parse(moveX);
                    mExpressParser.setSystemValue(moveY, Integer.valueOf(0));
                    mExpressParser.parse(moveY);
                } else if ("visible".equalsIgnoreCase(name)) {
                    result.setVisiblityProp(value);
                }
            }
            NodeList childNodes = unlockNode.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                String nodeName = childNode.getNodeName();
                if ("StartPoint".equals(nodeName)) {
                    parseUnlockStartPoint(childNode, result);
                } else if ("EndPoint".equals(nodeName)) {
                    parseUnlockEndPoint(childNode, result);
                }
            }
            this.mLockScreen.addUnlocker(result);
        }
    }

    private void parseUnlockStartPoint(Node unlockNode, HwUnlocker hwunlocker) {
        hwunlocker.setStartPointRect(parseRect(unlockNode.getAttributes()));
        NodeList childNodes = unlockNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if ("NormalState".equals(childNode.getNodeName())) {
                parseUnlockNode(childNode, hwunlocker.getName(), "_state eq 0");
            } else if ("PressedState".equals(childNode.getNodeName())) {
                parseUnlockNode(childNode, hwunlocker.getName(), "_state ne 1");
            }
        }
    }

    private void parseUnlockEndPoint(Node unlockNode, HwUnlocker hwunlocker) {
        EndPoint endPoint = new EndPoint(parseRect(unlockNode.getAttributes()));
        NodeList childNodes = unlockNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if ("NormalState".equals(childNode.getNodeName())) {
                parseUnlockNode(childNode, hwunlocker.getName(), "_state eq 0");
            } else if ("PressedState".equals(childNode.getNodeName())) {
                parseUnlockNode(childNode, hwunlocker.getName(), "_state ne 1");
            } else if ("Path".equals(childNode.getNodeName())) {
                pathAttr = childNode.getAttributes();
                String x = "0";
                String y = "0";
                for (j = 0; j < pathAttr.getLength(); j++) {
                    name = pathAttr.item(j).getNodeName();
                    value = pathAttr.item(j).getNodeValue();
                    if ("x".equalsIgnoreCase(name)) {
                        x = value;
                    } else if ("y".equalsIgnoreCase(name)) {
                        y = value;
                    }
                }
                endPoint.setPath(Integer.parseInt(x), Integer.parseInt(y));
            } else if ("Intent".equals(childNode.getNodeName())) {
                pathAttr = childNode.getAttributes();
                String IntentType = null;
                for (j = 0; j < pathAttr.getLength(); j++) {
                    name = pathAttr.item(j).getNodeName();
                    value = pathAttr.item(j).getNodeValue();
                    if ("type".equalsIgnoreCase(name)) {
                        IntentType = value;
                    }
                }
                endPoint.setIntentType(IntentType);
            }
        }
        hwunlocker.addEndPoint(endPoint);
    }

    private void parseUnlockNode(Node unlockNode, String unlockName, String viewState) {
        if (unlockNode != null) {
            NodeList normalNodes = unlockNode.getChildNodes();
            for (int j = 0; j < normalNodes.getLength(); j++) {
                Node nomalViewNode = normalNodes.item(j);
                if ("text".equalsIgnoreCase(nomalViewNode.getNodeName())) {
                    HwTextView view = parseTextView(nomalViewNode);
                    view.setVisiblityProp(unlockName + viewState);
                    this.mLockScreen.addView(view);
                } else if ("image".equalsIgnoreCase(nomalViewNode.getNodeName())) {
                    HwImageView view2 = parseImageView(nomalViewNode);
                    view2.setVisiblityProp(unlockName + viewState);
                    this.mLockScreen.addView(view2);
                }
            }
        }
    }

    private Rect parseRect(NamedNodeMap attrs) {
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
        float scalePara = AmazingUtils.getScalePara();
        if (scalePara != 1.0f) {
            x = ((int) (((float) Integer.parseInt(x)) * scalePara)) + BuildConfig.FLAVOR;
            y = ((int) (((float) Integer.parseInt(y)) * scalePara)) + BuildConfig.FLAVOR;
            w = ((int) (((float) Integer.parseInt(w)) * scalePara)) + BuildConfig.FLAVOR;
            h = ((int) (((float) Integer.parseInt(h)) * scalePara)) + BuildConfig.FLAVOR;
        }
        int l = Integer.parseInt(x);
        int t = Integer.parseInt(y);
        return new Rect(l, t, l + Integer.parseInt(w), t + Integer.parseInt(h));
    }

    private void parseMusicControl(Node musicNode) {
        if (musicNode != null) {
            this.mMusicController = new HwMusicController(this.mContext);
            NamedNodeMap attrs = musicNode.getAttributes();
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
                } else if ("visible".equalsIgnoreCase(name)) {
                    this.mMusicController.setVisiblityProp(value);
                } else if ("name".equalsIgnoreCase(name)) {
                    this.mMusicController.setMusicControllerName(value);
                }
            }
            NodeList nodeList = musicNode.getChildNodes();
            int nodeLength = nodeList.getLength();
            for (int j = 0; j < nodeLength; j++) {
                Node node = nodeList.item(j);
                String nodeName = node.getNodeName();
                if ("image".equalsIgnoreCase(nodeName)) {
                    this.mLockScreen.addView(parseImageView(node));
                } else if ("text".equalsIgnoreCase(nodeName)) {
                    this.mLockScreen.addView(parseTextView(node));
                } else if ("button".equalsIgnoreCase(nodeName)) {
                    parseVirtualButton(node, true);
                }
            }
            this.mLockScreen.setMusicControl(this.mMusicController);
        }
    }

    private void cleanCache() {
        if (!this.mHwImageArray.isEmpty()) {
            this.mHwImageArray.clear();
        }
        if (!this.mHwTextViewArray.isEmpty()) {
            this.mHwTextViewArray.clear();
        }
    }
}
