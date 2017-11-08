package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ViewPropertyCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.util.HwUnlockUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HwInflaterCommon {
    private static boolean mVersionFlag = false;

    public static HwTextView parseTextViewNode(Node textNode, Context mContext) {
        NamedNodeMap attrs = textNode.getAttributes();
        HwTextView result = new HwTextView(mContext);
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
            } else if ("background".equalsIgnoreCase(name)) {
                result.setBackground(value);
            } else if ("align".equalsIgnoreCase(name)) {
                result.setGravity(HwUnlockUtils.getGravity(value));
            } else if ("color".equalsIgnoreCase(name)) {
                result.setTextColor(Color.parseColor(value));
            } else if ("size".equalsIgnoreCase(name)) {
                result.setTextSize(1, (float) Integer.parseInt(value));
            } else if ("visible".equalsIgnoreCase(name)) {
                result.setVisiblityProp(value);
            } else if ("ellipsize".equalsIgnoreCase(name)) {
                result.setEllipsizeType(value);
            } else if ("textstyle".equalsIgnoreCase(name)) {
                result.setTypeface(Typeface.create("default", HwUnlockUtils.getTypeface(value)));
            } else if ("name".equalsIgnoreCase(name)) {
                result.setTextViewName(value);
            } else if ("ChargingType".equalsIgnoreCase(name)) {
                HwUnlockUtils.setChargingType(value);
            } else if ("MusicTextType".equalsIgnoreCase(name)) {
                HwUnlockUtils.setMusicTextType(value);
            }
        }
        result.setLayout(x, y, w, h);
        parseAnimationSet(mContext, textNode, result);
        return result;
    }

    public static void parseAnimationSet(Context mContext, Node viewNode, HwUnlockInterface$ViewPropertyCallback callback) {
        NodeList animationSetNodes = viewNode.getChildNodes();
        for (int i = 0; i < animationSetNodes.getLength(); i++) {
            Node node = animationSetNodes.item(i);
            String nodeName = node.getNodeName();
            if ("animationset".equals(nodeName)) {
                HwAnimationSet animationSet = new HwAnimationSet(mContext, node.getAttributes(), callback);
                parseAnimation(node, animationSet);
                ((HwImageView) callback).addHwAnimation(animationSet);
            } else if ("frameAnimation".equals(nodeName)) {
                AnimationDrawable frameAnimation = new AnimationDrawable();
                NamedNodeMap attrs = node.getAttributes();
                Node oneShotNode = attrs.getNamedItem("oneShot");
                if (oneShotNode != null) {
                    frameAnimation.setOneShot(Boolean.parseBoolean(oneShotNode.getNodeValue()));
                }
                Node conditionNode = attrs.getNamedItem("condition");
                if (conditionNode != null) {
                    HwViewProperty hwViewProperty = new HwViewProperty(mContext, conditionNode.getNodeValue(), HwUnlockConstants$ViewPropertyType.TYPE_CONDITION, callback);
                }
                if (mVersionFlag) {
                    parseFrameAnimation2(node, frameAnimation);
                } else {
                    parseFrameAnimation(node, frameAnimation);
                }
                ((HwImageView) callback).setFrameAnimation(frameAnimation);
            }
        }
    }

    private static void parseAnimation(Node animationSetNode, HwAnimationSet animationSet) {
        NodeList animationNodes = animationSetNode.getChildNodes();
        for (int i = 0; i < animationNodes.getLength(); i++) {
            Node animationNode = animationNodes.item(i);
            if ("animation".equals(animationNode.getNodeName())) {
                animationSet.addAnimation(animationNode.getAttributes());
            }
        }
    }

    public static boolean parseVersionFlag(Element rootElement) {
        NamedNodeMap rootAttrs = rootElement.getAttributes();
        if (rootAttrs == null) {
            mVersionFlag = false;
            return false;
        }
        int attrsLength = rootAttrs.getLength();
        String version = null;
        for (int j = 0; j < attrsLength; j++) {
            String name = rootAttrs.item(j).getNodeName();
            String value = rootAttrs.item(j).getNodeValue();
            if ("version".equalsIgnoreCase(name)) {
                version = value;
                break;
            }
        }
        if (version == null) {
            mVersionFlag = false;
            return false;
        }
        mVersionFlag = true;
        return true;
    }

    private static void parseFrameAnimation(Node frameAnimationNode, AnimationDrawable frameAnimation) {
        NodeList frameNodes = frameAnimationNode.getChildNodes();
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Node frameNode = frameNodes.item(i);
            if ("frame".equals(frameNode.getNodeName())) {
                NamedNodeMap attr = frameNode.getAttributes();
                Node srcNode = attr.getNamedItem("src");
                Node durationNode = attr.getNamedItem("duration");
                if (!(srcNode == null || durationNode == null)) {
                    Drawable frame = Drawable.createFromPath("/data/skin/unlock/drawable-hdpi/" + srcNode.getNodeValue());
                    int duration = Integer.parseInt(durationNode.getNodeValue());
                    if (frame != null) {
                        frameAnimation.addFrame(frame, duration);
                    }
                }
            }
        }
    }

    private static void parseFrameAnimation2(Node frameAnimationNode, AnimationDrawable frameAnimation) {
        NodeList frameNodes = frameAnimationNode.getChildNodes();
        HwAsyncDrawable preHwAsyncDrawable = null;
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Node frameNode = frameNodes.item(i);
            if ("frame".equals(frameNode.getNodeName())) {
                NamedNodeMap attr = frameNode.getAttributes();
                Node srcNode = attr.getNamedItem("src");
                Node durationNode = attr.getNamedItem("duration");
                Options opt = new Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                if (!(srcNode == null || durationNode == null)) {
                    Drawable frame;
                    String src = srcNode.getNodeValue();
                    if (i == 0) {
                        preHwAsyncDrawable = new HwAsyncDrawable(BitmapFactory.decodeFile("/data/skin/unlock/drawable-hdpi/" + src, opt));
                        frame = preHwAsyncDrawable;
                    } else {
                        Drawable asyncDrawable = new HwAsyncDrawable("/data/skin/unlock/drawable-hdpi/" + src);
                        asyncDrawable.setPreAsyncDrawbale(preHwAsyncDrawable);
                        Drawable drawable = asyncDrawable;
                        frame = asyncDrawable;
                    }
                    frameAnimation.addFrame(frame, Integer.parseInt(durationNode.getNodeValue()));
                }
            }
        }
    }
}
