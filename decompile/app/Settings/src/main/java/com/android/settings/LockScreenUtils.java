package com.android.settings;

import android.content.Context;
import android.provider.Settings.Global;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LockScreenUtils {
    public static boolean isMagazineLock() {
        return "magazine".equals(getLockStyle());
    }

    public static boolean isMagazineUnlockForbidden(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), "magazine_unlock_enabled", 0) == 1) {
            z = false;
        }
        return z;
    }

    private static String getLockStyle() {
        String style = "potter";
        Document document = getLockLayoutXML();
        if (document == null) {
            return style;
        }
        Element rootElement = document.getDocumentElement();
        if (rootElement == null) {
            return style;
        }
        NodeList itemNodes = rootElement.getChildNodes();
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Node itemNode = itemNodes.item(i);
            if ("item".equals(itemNode.getNodeName())) {
                NamedNodeMap attrs = itemNode.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    String name = attrs.item(j).getNodeName();
                    String value = attrs.item(j).getNodeValue();
                    if ("style".equalsIgnoreCase(name)) {
                        style = value;
                        return value;
                    }
                }
                continue;
            }
        }
        return style;
    }

    private static Document getLockLayoutXML() {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/data/skin/unlock", "theme.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }
}
