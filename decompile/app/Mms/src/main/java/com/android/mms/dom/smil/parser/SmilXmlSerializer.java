package com.android.mms.dom.smil.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;

public class SmilXmlSerializer {
    public static void serialize(SMILDocument smilDoc, OutputStream out) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 2048);
            writeElement(writer, smilDoc.getDocumentElement());
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private static void writeElement(Writer writer, Element element) throws IOException {
        writer.write(60);
        writer.write(element.getTagName());
        if (element.hasAttributes()) {
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                if (attribute != null) {
                    writer.write(" " + attribute.getName());
                    writer.write("=\"" + attribute.getValue() + "\"");
                }
            }
        }
        SMILElement childElement = (SMILElement) element.getFirstChild();
        if (childElement != null) {
            writer.write(62);
            do {
                writeElement(writer, childElement);
                childElement = (SMILElement) childElement.getNextSibling();
            } while (childElement != null);
            writer.write("</");
            writer.write(element.getTagName());
            writer.write(62);
            return;
        }
        writer.write("/>");
    }
}
