package com.android.mms.dom.smil.parser;

import com.android.mms.dom.smil.SmilDocumentImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.smil.SMILDocument;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SmilContentHandler extends DefaultHandler {
    private Node mCurrentNode;
    private SMILDocument mSmilDocument;

    public void reset() {
        this.mSmilDocument = new SmilDocumentImpl();
        this.mCurrentNode = this.mSmilDocument;
    }

    public SMILDocument getSmilDocument() {
        return this.mSmilDocument;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element element = this.mSmilDocument.createElement(localName);
        if (attributes != null) {
            int i = 0;
            while (i < attributes.getLength()) {
                String name = attributes.getLocalName(i);
                if (name != null) {
                    element.setAttribute(name, attributes.getValue(i));
                    i++;
                } else {
                    return;
                }
            }
        }
        this.mCurrentNode.appendChild(element);
        this.mCurrentNode = element;
    }

    public void endElement(String uri, String localName, String qName) {
        this.mCurrentNode = this.mCurrentNode.getParentNode();
    }

    public void characters(char[] ch, int start, int length) {
    }
}
