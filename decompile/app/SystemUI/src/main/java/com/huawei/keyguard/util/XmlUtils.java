package com.huawei.keyguard.util;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtils {

    public interface INoteReader {
        void parseNode(Node node);
    }

    public static class NodeAttributeReader implements INoteReader {
        private INoteReader mAttrReader;

        public NodeAttributeReader(INoteReader attrReader) {
            this.mAttrReader = attrReader;
        }

        public void parseNode(Node node) {
            NamedNodeMap attrs = node.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                this.mAttrReader.parseNode(attrs.item(j));
            }
        }
    }

    public static Document getXMLDocument(String path) {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
        } catch (ParserConfigurationException e) {
            HwLog.e("XmlUtils", "getLayoutXML ParserConfigurationException " + path, e);
        } catch (SAXException e2) {
            HwLog.e("XmlUtils", "getLayoutXML SAXException " + path, e2);
        } catch (IOException e3) {
            HwLog.e("XmlUtils", "getLayoutXML IOException " + path, e3);
        } catch (Exception e4) {
            HwLog.e("XmlUtils", "getLayoutXML Exception has no theme.xml", e4);
        }
        return document;
    }

    public static void parseXmlNode(String filePath, String nodeName, INoteReader reader) {
        Document document = getXMLDocument(filePath);
        if (document != null) {
            parseElement(document.getDocumentElement(), nodeName, reader);
        }
    }

    public static void parseElement(Element rootElement, String nodeName, INoteReader reader) {
        NodeList itemNodes = rootElement.getChildNodes();
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Node itemNode = itemNodes.item(i);
            if (nodeName.equals(itemNode.getNodeName())) {
                reader.parseNode(itemNode);
            }
        }
    }
}
