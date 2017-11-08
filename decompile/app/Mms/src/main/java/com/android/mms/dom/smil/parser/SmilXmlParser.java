package com.android.mms.dom.smil.parser;

import com.google.android.mms.MmsException;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.smil.SMILDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SmilXmlParser {
    private SmilContentHandler mContentHandler;
    private XMLReader mXmlReader;

    public SmilXmlParser() throws MmsException {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        try {
            this.mXmlReader = XMLReaderFactory.createXMLReader();
            this.mContentHandler = new SmilContentHandler();
            this.mXmlReader.setContentHandler(this.mContentHandler);
        } catch (SAXException e) {
            throw new MmsException(e);
        }
    }

    public SMILDocument parse(InputStream in) throws IOException, SAXException {
        this.mContentHandler.reset();
        this.mXmlReader.parse(new InputSource(in));
        SMILDocument doc = this.mContentHandler.getSmilDocument();
        validateDocument(doc);
        return doc;
    }

    private void validateDocument(SMILDocument doc) {
        doc.getBody();
        doc.getLayout();
    }
}
