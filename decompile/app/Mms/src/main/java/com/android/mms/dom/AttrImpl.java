package com.android.mms.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public class AttrImpl extends NodeImpl implements Attr {
    private String mName;
    private String mValue;

    protected AttrImpl(DocumentImpl owner, String name) {
        super(owner);
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public Element getOwnerElement() {
        return null;
    }

    public boolean getSpecified() {
        return this.mValue != null;
    }

    public String getValue() {
        return this.mValue;
    }

    public void setValue(String value) throws DOMException {
        this.mValue = value;
    }

    public String getNodeName() {
        return this.mName;
    }

    public short getNodeType() {
        return (short) 2;
    }

    public Node getParentNode() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        setValue(nodeValue);
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public boolean isId() {
        return false;
    }
}
