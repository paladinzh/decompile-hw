package com.android.settings.wifi.ap;

import android.content.Context;
import android.util.Xml;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

public class AllowedDevicesParser {
    public static List<WifiApClientInfo> loadAllowedDevices(Context context) {
        InputStream inputStream = null;
        List<WifiApClientInfo> infoList = new ArrayList();
        try {
            inputStream = context.openFileInput("allowed_devices.xml");
            readXML(inputStream, infoList);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe22) {
                    ioe22.printStackTrace();
                }
            }
        }
        return infoList;
    }

    private static void readXML(InputStream inStream, List<WifiApClientInfo> infoList) {
        try {
            NodeList devicesNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inStream).getDocumentElement().getElementsByTagName("allowed_device");
            for (int i = 0; i < devicesNodes.getLength(); i++) {
                infoList.add(parseDeviceInfo((Element) devicesNodes.item(i)));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            infoList.clear();
        } catch (SAXException e2) {
            e2.printStackTrace();
            infoList.clear();
        } catch (IOException e3) {
            e3.printStackTrace();
            infoList.clear();
        } catch (RuntimeException e4) {
            e4.printStackTrace();
            infoList.clear();
        }
    }

    private static WifiApClientInfo parseDeviceInfo(Element devicesNode) {
        WifiApClientInfo info = new WifiApClientInfo();
        info.setId(Integer.parseInt(devicesNode.getAttribute("id")));
        NodeList allowedDevicesChildList = devicesNode.getChildNodes();
        for (int i = 0; i < allowedDevicesChildList.getLength(); i++) {
            Node node = allowedDevicesChildList.item(i);
            if (node.getNodeType() == (short) 1) {
                Element element = (Element) node;
                if ("device_name".equals(element.getNodeName())) {
                    info.setDeviceName(getElementValue(element));
                } else if ("device_mac".equals(element.getNodeName())) {
                    info.setMAC(getElementValue(element));
                }
            }
        }
        return info;
    }

    private static String getElementValue(Element element) {
        Node firstChildNode = element.getFirstChild();
        if (firstChildNode != null) {
            return firstChildNode.getNodeValue();
        }
        return null;
    }

    public static synchronized void saveAllowedDevices(Context context, List<WifiApClientInfo> infoList) {
        synchronized (AllowedDevicesParser.class) {
            OutputStream outputStream = null;
            try {
                outputStream = context.openFileOutput("allowed_devices.xml", 0);
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(outputStream, "UTF-8");
                serializer.startDocument("UTF-8", Boolean.valueOf(true));
                serializer.startTag(null, "allowed_devices");
                if (infoList != null) {
                    for (int i = 0; i < infoList.size(); i++) {
                        WifiApClientInfo info = (WifiApClientInfo) infoList.get(i);
                        serializer.startTag(null, "allowed_device");
                        serializer.attribute(null, "id", String.valueOf(info.getId()));
                        writeTag(serializer, "device_name", info.getDeviceName());
                        writeTag(serializer, "device_mac", info.getMAC());
                        serializer.endTag(null, "allowed_device");
                    }
                }
                serializer.endTag(null, "allowed_devices");
                serializer.endDocument();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (IOException e32) {
                e32.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
            }
        }
    }

    private static void writeTag(XmlSerializer serializer, String key, CharSequence value) throws IOException {
        serializer.startTag(null, key);
        if (value != null) {
            serializer.text(value.toString());
        }
        serializer.endTag(null, key);
    }
}
