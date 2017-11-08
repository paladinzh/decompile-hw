package com.android.mms.util;

import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class VNodeBuilder implements VCardInterpreter {
    private static String LOG_TAG = "VNodeBuilder";
    private VNode mCurrentVNode;
    private List<VNode> mVNodeList;

    public VNodeBuilder() {
        this("UTF-8", false);
    }

    public VNodeBuilder(String targetCharset, boolean strictLineBreakParsing) {
        this.mVNodeList = new ArrayList();
    }

    public void onVCardStarted() {
    }

    public void onVCardEnded() {
    }

    public void onEntryStarted() {
        this.mCurrentVNode = new VNode();
        this.mVNodeList.add(this.mCurrentVNode);
    }

    public void onEntryEnded() {
        int lastIndex = this.mVNodeList.size() - 1;
        this.mCurrentVNode = lastIndex >= 0 ? (VNode) this.mVNodeList.get(lastIndex) : null;
    }

    public void onPropertyCreated(VCardProperty property) {
        PropertyNode propNode = new PropertyNode();
        propNode.propName = property.getName();
        List<String> groupList = property.getGroupList();
        if (groupList != null) {
            propNode.propGroupSet.addAll(groupList);
        }
        for (Entry<String, Collection<String>> s : property.getParameterMap().entrySet()) {
            String paramType = (String) s.getKey();
            Collection<String> paramValueList = (Collection) s.getValue();
            if (paramType.equalsIgnoreCase("TYPE")) {
                propNode.paramMap_TYPE.addAll(paramValueList);
            } else {
                for (String paramValue : paramValueList) {
                    propNode.paramMap.put(paramType, paramValue);
                }
            }
        }
        if (property.getRawValue() == null) {
            propNode.propValue_bytes = null;
            propNode.propValue_vector.clear();
            propNode.propValue_vector.add("");
            propNode.propValue = "";
            return;
        }
        List<String> values = property.getValueList();
        if (values == null || values.size() == 0) {
            propNode.propValue_vector.clear();
            propNode.propValue_vector.add("");
            propNode.propValue = "";
        } else {
            propNode.propValue_vector.addAll(values);
            propNode.propValue = listToString(propNode.propValue_vector);
        }
        propNode.propValue_bytes = property.getByteValue();
        this.mCurrentVNode.propList.add(propNode);
    }

    private String listToString(List<String> list) {
        int size = list.size();
        if (size > 1) {
            StringBuilder typeListB = new StringBuilder();
            for (String type : list) {
                typeListB.append(type).append(";");
            }
            int len = typeListB.length();
            if (len <= 0 || typeListB.charAt(len - 1) != ';') {
                return typeListB.toString();
            }
            return typeListB.substring(0, len - 1);
        } else if (size == 1) {
            return (String) list.get(0);
        } else {
            return "";
        }
    }

    public List<VNode> getVNodeList() {
        return this.mVNodeList;
    }
}
