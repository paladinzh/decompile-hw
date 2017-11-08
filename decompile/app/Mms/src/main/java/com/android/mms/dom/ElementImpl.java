package com.android.mms.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class ElementImpl extends NodeImpl implements Element {
    private NamedNodeMap mAttributes = new NamedNodeMapImpl();
    private String mTagName;

    protected ElementImpl(DocumentImpl owner, String tagName) {
        super(owner);
        this.mTagName = tagName;
    }

    public String getAttribute(String name) {
        Attr attrNode = getAttributeNode(name);
        String attrValue = "";
        if (attrNode != null) {
            return attrNode.getValue();
        }
        return attrValue;
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        return null;
    }

    public Attr getAttributeNode(String name) {
        return (Attr) this.mAttributes.getNamedItem(name);
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return null;
    }

    public NodeList getElementsByTagName(String name) {
        return new NodeListImpl(this, name, true);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return null;
    }

    public String getTagName() {
        return this.mTagName;
    }

    public boolean hasAttribute(String name) {
        return getAttributeNode(name) != null;
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return false;
    }

    public void removeAttribute(String name) throws DOMException {
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return null;
    }

    public void setAttribute(String name, String value) throws DOMException {
        Attr attribute = getAttributeNode(name);
        if (attribute == null) {
            attribute = this.mOwnerDocument.createAttribute(name);
        }
        attribute.setNodeValue(value);
        this.mAttributes.setNamedItem(attribute);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return null;
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return null;
    }

    public short getNodeType() {
        return (short) 1;
    }

    public String getNodeName() {
        return this.mTagName;
    }

    public NamedNodeMap getAttributes() {
        return this.mAttributes;
    }

    public boolean hasAttributes() {
        return this.mAttributes.getLength() > 0;
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        throw new DOMException((short) 9, null);
    }
}
