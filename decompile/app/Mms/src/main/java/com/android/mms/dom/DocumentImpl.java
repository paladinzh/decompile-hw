package com.android.mms.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public abstract class DocumentImpl extends NodeImpl implements Document {
    public abstract Element createElement(String str) throws DOMException;

    public abstract Element getDocumentElement();

    public DocumentImpl() {
        super(null);
    }

    public Attr createAttribute(String name) throws DOMException {
        return new AttrImpl(this, name);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return null;
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        return null;
    }

    public Comment createComment(String data) {
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        return null;
    }

    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return null;
    }

    public EntityReference createEntityReference(String name) throws DOMException {
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return null;
    }

    public Text createTextNode(String data) {
        return null;
    }

    public DocumentType getDoctype() {
        return null;
    }

    public Element getElementById(String elementId) {
        return null;
    }

    public NodeList getElementsByTagName(String tagname) {
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return null;
    }

    public DOMImplementation getImplementation() {
        return null;
    }

    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return null;
    }

    public short getNodeType() {
        return (short) 9;
    }

    public String getNodeName() {
        return "#document";
    }

    public String getInputEncoding() {
        return null;
    }

    public String getXmlEncoding() {
        return null;
    }

    public boolean getXmlStandalone() {
        return false;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
    }

    public String getXmlVersion() {
        return null;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
    }

    public boolean getStrictErrorChecking() {
        return true;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
    }

    public String getDocumentURI() {
        return null;
    }

    public void setDocumentURI(String documentURI) {
    }

    public Node adoptNode(Node source) throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public DOMConfiguration getDomConfig() {
        throw new DOMException((short) 9, null);
    }

    public void normalizeDocument() {
        throw new DOMException((short) 9, null);
    }

    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        throw new DOMException((short) 9, null);
    }
}
