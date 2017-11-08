package com.android.mms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.PropertyNode;
import com.android.mms.util.VNode;
import com.android.mms.util.VNodeBuilder;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardSourceDetector;
import com.android.vcard.VCardUtils;
import com.android.vcard.exception.VCardVersionException;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VCardSmsMessage {
    private static final String noFormateCountry = SystemProperties.get("ro.config.noFormateCountry", "");
    private static VNodeBuilder sVNodeBuilder;

    private static List<VCardDetailNode> parseOneVcardDetail(Context context, int index) {
        List<VCardDetailNode> list = new ArrayList();
        if (sVNodeBuilder == null) {
            MLog.e("VCardSmsMessage", "parseOneVcardDetail::the sVNodeBuilder is null, return empty list!");
            return list;
        }
        boolean isdisplaynameavailable = false;
        String phoneticNameKey = null;
        for (PropertyNode prop : ((VNode) sVNodeBuilder.getVNodeList().get(index)).propList) {
            if (prop.propName.equalsIgnoreCase("FN") && prop.propValue != null) {
                isdisplaynameavailable = true;
            } else if ((prop.propName.equalsIgnoreCase("X-PHONETIC-LAST-NAME") || prop.propName.equalsIgnoreCase("X-PHONETIC-FIRST-NAME") || prop.propName.equalsIgnoreCase("X-PHONETIC-MIDDLE-NAME")) && prop.propValue != null) {
                phoneticNameKey = prop.propName;
            }
        }
        int type = 0;
        String familyName = null;
        String middleName = null;
        String givenName = null;
        String phoneticName = null;
        for (PropertyNode prop2 : ((VNode) sVNodeBuilder.getVNodeList().get(index)).propList) {
            String phoneticName2;
            if (prop2.propName.equalsIgnoreCase("FN") && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.vcard_name), prop2.propValue, prop2.propName, null, 1));
                phoneticName2 = phoneticName;
            } else if (!isdisplaynameavailable && prop2.propName.equalsIgnoreCase("N") && prop2.propValue != null) {
                Object obj = null;
                String[] name = prop2.propValue.split(";", 5);
                if (name.length == 5) {
                    obj = VCardUtils.constructNameFromElements(0, name[0], name[2], name[1], name[3], name[4]);
                } else if (name.length == 2) {
                    obj = VCardUtils.constructNameFromElements(0, name[0], null, name[1], null, null);
                }
                if (!TextUtils.isEmpty(obj)) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_name), obj, prop2.propName, null, 1));
                }
                phoneticName2 = phoneticName;
            } else if ((prop2.propName.equalsIgnoreCase("NICKNAME") || prop2.propName.equalsIgnoreCase("X-NICKNAME")) && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.vcard_nickname), prop2.propValue, prop2.propName, null, 8));
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("X-ANDROID-CUSTOM") && prop2.propValue != null) {
                String[] values = prop2.propValue.split(";");
                if (values.length > 1 && "vnd.android.cursor.item/nickname".equals(values[0])) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_nickname), values[1], prop2.propName, getType(prop2.paramMap_TYPE), 8));
                }
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("ORG") && prop2.propValue != null) {
                type = type + 1;
                list.add(new VCardDetailNode(context.getString(R.string.vcard_organisation), prop2.propValue, prop2.propName, String.valueOf(type), 2));
                phoneticName2 = phoneticName;
                type = type;
            } else if (prop2.propName.equalsIgnoreCase("BDAY") && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.vcard_birthday), prop2.propValue, prop2.propName, null, 11));
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("NOTE") && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.label_notes), prop2.propValue, prop2.propName, null, 12));
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("TITLE") && prop2.propValue != null) {
                type = type + 1;
                list.add(new VCardDetailNode(context.getString(R.string.vcard_title), prop2.propValue, prop2.propName, String.valueOf(type), 3));
                phoneticName2 = phoneticName;
                type = type;
            } else if (prop2.propName.equalsIgnoreCase("TEL") && prop2.propValue != null) {
                String telValue;
                if (haveNoFormateCountry()) {
                    telValue = removeAllSeparate(prop2.propValue);
                } else {
                    telValue = prop2.propValue;
                }
                if (prop2.paramMap_TYPE.contains("CELL")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_mobile), telValue, prop2.propName, getType(prop2.paramMap_TYPE), 5));
                } else if (prop2.paramMap_TYPE.contains("HOME")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_hometel), telValue, prop2.propName, getType(prop2.paramMap_TYPE), 5));
                } else if (prop2.paramMap_TYPE.contains("WORK")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_worktel), telValue, prop2.propName, getType(prop2.paramMap_TYPE), 5));
                } else {
                    type = type + 1;
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_othertel), telValue, prop2.propName, String.valueOf(type), 5));
                    type = type;
                }
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("EMAIL") && prop2.propValue != null) {
                if (prop2.paramMap_TYPE.contains("HOME")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_personalemail), prop2.propValue, prop2.propName, getType(prop2.paramMap_TYPE), 6));
                } else if (prop2.paramMap_TYPE.contains("WORK")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_workemail), prop2.propValue, prop2.propName, getType(prop2.paramMap_TYPE), 6));
                } else if (prop2.paramMap_TYPE.contains("CELL")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_mobileemail), prop2.propValue, prop2.propName, getType(prop2.paramMap_TYPE), 6));
                } else {
                    type = type + 1;
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_othermail), prop2.propValue, prop2.propName, String.valueOf(type), 6));
                    type = type;
                }
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("ADR") && prop2.propValue != null) {
                String address = prop2.propValue.replace(';', ' ').trim();
                if (prop2.paramMap_TYPE.contains("HOME")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_homeaddress), address, prop2.propName, getType(prop2.paramMap_TYPE), 10));
                } else if (prop2.paramMap_TYPE.contains("WORK")) {
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_workaddress), address, prop2.propName, getType(prop2.paramMap_TYPE), 10));
                } else {
                    type = type + 1;
                    list.add(new VCardDetailNode(context.getString(R.string.vcard_otheraddress), address, prop2.propName, String.valueOf(type), 10));
                    type = type;
                }
                phoneticName2 = phoneticName;
            } else if (prop2.propName.equalsIgnoreCase("URL") && prop2.propValue != null) {
                type = type + 1;
                list.add(new VCardDetailNode(context.getString(R.string.websiteLabelsGroup), prop2.propValue, prop2.propName, String.valueOf(type), 9));
                phoneticName2 = phoneticName;
                type = type;
            } else if (prop2.propName.equalsIgnoreCase("X-IMPP") && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.vcard_im), prop2.propValue, prop2.propName, null, 7));
                phoneticName2 = phoneticName;
            } else if ((prop2.propName.equalsIgnoreCase("CATEGORIES") || prop2.propName.equalsIgnoreCase("X-CATEGORIES")) && prop2.propValue != null) {
                list.add(new VCardDetailNode(context.getString(R.string.vcard_group), prop2.propValue, prop2.propName, null, 13));
                phoneticName2 = phoneticName;
            } else {
                if (prop2.propName.equalsIgnoreCase("X-PHONETIC-LAST-NAME") && prop2.propValue != null) {
                    familyName = prop2.propValue;
                    if ("X-PHONETIC-LAST-NAME".equalsIgnoreCase(phoneticNameKey)) {
                        phoneticName2 = getPhoneticName(familyName, middleName, givenName);
                        list.add(new VCardDetailNode(context.getString(R.string.vcard_phoneticname), phoneticName2, prop2.propName, null, 4));
                    }
                } else if (prop2.propName.equalsIgnoreCase("X-PHONETIC-FIRST-NAME") && prop2.propValue != null) {
                    givenName = prop2.propValue;
                    if ("X-PHONETIC-FIRST-NAME".equalsIgnoreCase(phoneticNameKey)) {
                        phoneticName2 = getPhoneticName(familyName, middleName, givenName);
                        list.add(new VCardDetailNode(context.getString(R.string.vcard_phoneticname), phoneticName2, prop2.propName, null, 4));
                    }
                } else if (!prop2.propName.equalsIgnoreCase("X-PHONETIC-MIDDLE-NAME") || prop2.propValue == null) {
                    phoneticName2 = phoneticName;
                } else {
                    middleName = prop2.propValue;
                    if ("X-PHONETIC-MIDDLE-NAME".equalsIgnoreCase(phoneticNameKey)) {
                        phoneticName2 = getPhoneticName(familyName, middleName, givenName);
                        list.add(new VCardDetailNode(context.getString(R.string.vcard_phoneticname), phoneticName2, prop2.propName, null, 4));
                    }
                }
                phoneticName2 = phoneticName;
            }
            phoneticName = phoneticName2;
        }
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<VCardDetailNode>() {
                public int compare(VCardDetailNode o1, VCardDetailNode o2) {
                    return o1.before(o2);
                }
            });
        }
        return list;
    }

    private static String getPhoneticName(String familyName, String middleName, String givenName) {
        if (Locale.getDefault().getCountry().equalsIgnoreCase("ru")) {
            return buildPhoneticName(familyName, givenName, middleName);
        }
        return buildPhoneticName(familyName, middleName, givenName);
    }

    public static String buildPhoneticName(String family, String middle, String given) {
        if (TextUtils.isEmpty(family) && TextUtils.isEmpty(middle) && TextUtils.isEmpty(given)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(family)) {
            sb.append(family.trim()).append(' ');
        }
        if (!TextUtils.isEmpty(middle)) {
            sb.append(middle.trim()).append(' ');
        }
        if (!TextUtils.isEmpty(given)) {
            sb.append(given.trim()).append(' ');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private static boolean haveNoFormateCountry() {
        return noFormateCountry.contains(Locale.getDefault().getCountry());
    }

    private static String removeAllSeparate(String input) {
        String inputStr = input;
        if (TextUtils.isEmpty(input)) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (!('*' == input.charAt(i) || '#' == input.charAt(i) || (input.charAt(i) >= '0' && input.charAt(i) <= '9'))) {
                if ('+' != input.charAt(i)) {
                    i++;
                }
            }
            sb.append(input.charAt(i));
            i++;
        }
        return sb.toString();
    }

    private static String getType(Set<String> typeSet) {
        StringBuilder sb = new StringBuilder();
        for (String append : typeSet) {
            sb.append(append);
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    public static Bitmap stripVcardPhoto() {
        Bitmap bitmap = null;
        if (getVcardSize() == 1) {
            for (PropertyNode prop : ((VNode) sVNodeBuilder.getVNodeList().get(0)).propList) {
                if (prop.propName.equalsIgnoreCase("PHOTO") && prop.propValue != null) {
                    try {
                        bitmap = BitmapFactory.decodeByteArray(prop.propValue_bytes, 0, prop.propValue_bytes.length);
                    } catch (Exception ex) {
                        MLog.e("VCardSmsMessage", "decodeByteArray has exception : " + ex);
                    } catch (Error e) {
                        MLog.e("VCardSmsMessage", "decodeByteArray has error : " + e);
                    }
                }
            }
        }
        return bitmap;
    }

    public static Bitmap stripDefaultVcardPhoto(Context context) {
        if (getVcardSize() > 1) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.cs_textfield_default_emui);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.cs_textfield_default_press_emui);
    }

    private static String getPropValueFromName(String propName, ArrayList<PropertyNode> propList) {
        for (PropertyNode prop : propList) {
            if (prop.propName.equalsIgnoreCase(propName) && prop.propValue != null) {
                return prop.propValue.trim();
            }
        }
        return null;
    }

    public static List<VCardDetailNode> getVcardDetail(Context context) {
        List<VCardDetailNode> vCardDetailList = new ArrayList();
        int vNodeSize = getVcardSize();
        if (vNodeSize > 1) {
            int propName = 0;
            for (VNode vnode : sVNodeBuilder.getVNodeList()) {
                int propName2;
                VCardDetailNode vcardDetailNode = null;
                String name = getPropValueFromName("FN", vnode.propList);
                if (name == null) {
                    name = getPropValueFromName("TEL", vnode.propList);
                }
                if (name == null) {
                    name = getPropValueFromName("EMAIL", vnode.propList);
                }
                if (name != null) {
                    propName2 = propName + 1;
                    vcardDetailNode = new VCardDetailNode(name, null, String.valueOf(propName), null, 0);
                    vCardDetailList.add(vcardDetailNode);
                } else {
                    propName2 = propName;
                }
                if (vcardDetailNode == null) {
                    propName = propName2 + 1;
                    vCardDetailList.add(new VCardDetailNode(context.getString(R.string.vcard_contact), null, String.valueOf(propName2), null, 0));
                } else {
                    propName = propName2;
                }
            }
        } else if (vNodeSize == 1) {
            return parseOneVcardDetail(context, 0);
        }
        return vCardDetailList;
    }

    public static String stripVcardName(Context context) {
        if (sVNodeBuilder == null) {
            MLog.e("VCardSmsMessage", "stripVcardName::the sVNodeBuilder is null, return default vcard name!");
            return context.getString(R.string.vcard_contact);
        }
        int vNodeSize = getVcardSize();
        int vCardSelectIndex = VcardModel.getSelectState();
        int vCardSelectNum = VcardModel.getSelectNum();
        if (vNodeSize == 1 || vCardSelectIndex == 1) {
            VNode vnode;
            if (vNodeSize == 1) {
                vnode = (VNode) sVNodeBuilder.getVNodeList().get(0);
            } else {
                vnode = (VNode) sVNodeBuilder.getVNodeList().get(vCardSelectNum);
            }
            String name = getPropValueFromName("FN", vnode.propList);
            if (name == null) {
                name = getPropValueFromName("TEL", vnode.propList);
            }
            if (name == null) {
                name = getPropValueFromName("EMAIL", vnode.propList);
            }
            if (name != null) {
                return name;
            }
        } else if (vNodeSize > 1) {
            return context.getResources().getQuantityString(R.plurals.multi_vcard, vNodeSize, new Object[]{Integer.valueOf(vNodeSize)});
        }
        return context.getString(R.string.vcard_contact);
    }

    public static void createVNodeBuilder(Uri uri, Context context) {
        if (createVNodeBuilder(uri, context, 0) <= 0 || createVNodeBuilder(uri, context, 1) <= 0 || createVNodeBuilder(uri, context, 2) > 0) {
        }
    }

    private static int createVNodeBuilder(Uri uri, Context context, int vCardType) {
        VCardVersionException e1;
        VNodeBuilder vNodeBuilder;
        Exception e;
        Throwable th;
        InputStream inputStream = null;
        try {
            VCardSourceDetector detector = new VCardSourceDetector();
            VCardParser parser = VCardUtils.getAppropriateParser(vCardType);
            VNodeBuilder builder = new VNodeBuilder();
            try {
                parser.addInterpreter(builder);
                parser.addInterpreter(detector);
                inputStream = context.getContentResolver().openInputStream(uri);
                parser.parse(inputStream);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        MLog.e("VCardSmsMessage", "stripVcardMsg IOException inputstream close failed " + e2);
                    }
                }
                sVNodeBuilder = builder;
                return 0;
            } catch (VCardVersionException e3) {
                e1 = e3;
                vNodeBuilder = builder;
                MLog.e("VCardSmsMessage", "stripVcardMsg " + e1);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        MLog.e("VCardSmsMessage", "stripVcardMsg IOException inputstream close failed " + e22);
                    }
                }
                return 1;
            } catch (Exception e4) {
                e = e4;
                vNodeBuilder = builder;
                try {
                    MLog.e("VCardSmsMessage", "stripVcardMsg " + e);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e222) {
                            MLog.e("VCardSmsMessage", "stripVcardMsg IOException inputstream close failed " + e222);
                        }
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2222) {
                            MLog.e("VCardSmsMessage", "stripVcardMsg IOException inputstream close failed " + e2222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (VCardVersionException e5) {
            e1 = e5;
            MLog.e("VCardSmsMessage", "stripVcardMsg " + e1);
            if (inputStream != null) {
                inputStream.close();
            }
            return 1;
        } catch (Exception e6) {
            e = e6;
            MLog.e("VCardSmsMessage", "stripVcardMsg " + e);
            if (inputStream != null) {
                inputStream.close();
            }
            return -1;
        }
    }

    public static int getVcardSize() {
        return sVNodeBuilder != null ? sVNodeBuilder.getVNodeList().size() : 0;
    }

    public static String getDisplayingVcardText(Context context) {
        StringBuilder sb = new StringBuilder();
        int vNodeSize = getVcardSize();
        int index = 0;
        while (index < vNodeSize) {
            List<VCardDetailNode> nodes = parseOneVcardDetail(context, index);
            for (int i = 0; i < nodes.size(); i++) {
                VCardDetailNode vdn = (VCardDetailNode) nodes.get(i);
                if (!(TextUtils.isEmpty(vdn.getName()) || TextUtils.isEmpty(vdn.getValue()))) {
                    String value = vdn.getValue();
                    if (MessageUtils.isNeedLayoutRtl()) {
                        value = new StringBuffer().append('‪').append(value).append('‬').toString();
                    }
                    sb.append(context.getResources().getString(R.string.vnode_format, new Object[]{vdn.getName(), value}));
                    if (i < nodes.size() - 1 || index < vNodeSize - 1) {
                        sb.append("\n");
                    }
                }
            }
            if (index < vNodeSize - 1) {
                sb.append("---");
                sb.append("\n");
            }
            index++;
        }
        return sb.toString();
    }
}
