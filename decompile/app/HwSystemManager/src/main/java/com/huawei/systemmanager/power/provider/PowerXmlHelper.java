package com.huawei.systemmanager.power.provider;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.TimeSceneXmlBean;
import com.huawei.systemmanager.power.model.UnifiedPowerBean;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PowerXmlHelper {
    public static final String ATTR_CHECK_FOR_UNIFIED_APP_DEFAULT_VALUE = "false";
    public static final String ATTR_CHECK_FOR_UNIFIED_APP_TABLE = "isProtected";
    public static final String ATTR_CURRENT_FOR_TIME_SCENE_DEFAULT_VALUE = "current_value";
    public static final String ATTR_NAME_FOR_UNIFIED_APP_TABLE = "name";
    public static final String ATTR_NUMBER_FOR_TIME_SCENE_DEFAULT_VALUE = "number";
    public static final String ATTR_PACKAGE_NAME_FOR_UNIFIED_APP_TABLE = "package";
    public static final String ATTR_SCENE_FOR_TIME_SCENE_DEFAULT_VALUE = "scene";
    public static final String ATTR_SHOW_FOR_UNIFIED_APP_DEFAULT_VALUE = "true";
    public static final String ATTR_SHOW_FOR_UNIFIED_APP_TABLE = "isShow";
    private static final String SUB_ITEM_ATTR = "name";
    private static final String SUB_ITEM_TAG = "item";
    private static final String TAG = "PowerXmlHelper";

    public static List<UnifiedPowerBean> parseUnifiedPowerAppTableDefaultValue(Context ctx) {
        List<UnifiedPowerBean> list = Lists.newArrayList();
        try {
            list = parseRootElement(ctx, XmlParsers.assetXmlRootElement(ctx, "cloud/config/unifiedPowerApps.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static List<UnifiedPowerBean> parseRootElement(Context context, Element element) {
        if (element == null) {
            HwLog.e(TAG, "The root element is null");
            return Lists.newArrayList();
        }
        List<UnifiedPowerBean> list = Lists.newArrayList();
        NodeList nodeList = element.getElementsByTagName("package");
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            UnifiedPowerBean mUnifiedPowerBean = new UnifiedPowerBean();
            Element packageElement = (Element) nodeList.item(i);
            mUnifiedPowerBean.setPkg_name(packageElement.getAttribute("name"));
            NodeList permissionNodeList = packageElement.getElementsByTagName(SUB_ITEM_TAG);
            int permissionSize = permissionNodeList.getLength();
            for (int j = 0; j < permissionSize; j++) {
                Element permissionElement = (Element) permissionNodeList.item(j);
                String attrName = permissionElement.getAttribute("name");
                String value = permissionElement.getTextContent();
                if (attrName.equalsIgnoreCase("isProtected")) {
                    boolean z;
                    if (value.equals("1")) {
                        z = false;
                    } else {
                        z = true;
                    }
                    mUnifiedPowerBean.setIs_protected(z);
                } else if (attrName.equalsIgnoreCase("isShow")) {
                    mUnifiedPowerBean.setIs_show(!value.equals("1"));
                }
            }
            list.add(mUnifiedPowerBean);
        }
        return list;
    }

    private static List<TimeSceneXmlBean> parseRootElementForTimeScene(Context context, Element element) {
        if (element == null) {
            HwLog.e(TAG, "parseRootElementForTimeScene, The root element is null");
            return Lists.newArrayList();
        }
        List<TimeSceneXmlBean> list = Lists.newArrayList();
        NodeList nodeList = element.getElementsByTagName(ATTR_SCENE_FOR_TIME_SCENE_DEFAULT_VALUE);
        int size = nodeList.getLength();
        for (int i = 0; i < size; i++) {
            TimeSceneXmlBean mTimeSceneXmlBean = new TimeSceneXmlBean();
            Element sceneElement = (Element) nodeList.item(i);
            mTimeSceneXmlBean.setNumber(Integer.parseInt(sceneElement.getAttribute("number")));
            NodeList mNodeList = sceneElement.getElementsByTagName(SUB_ITEM_TAG);
            int permissionSize = mNodeList.getLength();
            for (int j = 0; j < permissionSize; j++) {
                Element permissionElement = (Element) mNodeList.item(j);
                String attrName = permissionElement.getAttribute("name");
                String value = permissionElement.getTextContent();
                if (attrName.equalsIgnoreCase(ATTR_CURRENT_FOR_TIME_SCENE_DEFAULT_VALUE)) {
                    mTimeSceneXmlBean.setCurrentValue(Double.parseDouble(value));
                }
            }
            list.add(mTimeSceneXmlBean);
        }
        return list;
    }

    public static List<TimeSceneXmlBean> parseTimeSceneDefaultValue(Context ctx) {
        List<TimeSceneXmlBean> list = Lists.newArrayList();
        try {
            list = parseRootElementForTimeScene(ctx, XmlParsers.assetXmlRootElement(ctx, ApplicationConstant.HSM_TIME_SCENE_DEFAULT_FILE_INNER));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
