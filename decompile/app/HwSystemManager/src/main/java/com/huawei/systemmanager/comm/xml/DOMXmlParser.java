package com.huawei.systemmanager.comm.xml;

import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class DOMXmlParser {
    private static final String TAG = DOMXmlParser.class.getSimpleName();
    private InnerParser parser = new InnerParser();

    private static class InnerParser {
        private Document document;

        private InnerParser() {
            this.document = null;
        }

        public void do_parse(InputStream is) {
            try {
                this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            } catch (ParserConfigurationException ex) {
                HwLog.e(DOMXmlParser.TAG, "do_parse ParserConfigurationException " + ex.getMessage());
                this.document = null;
            } catch (SAXException ex2) {
                HwLog.e(DOMXmlParser.TAG, "do_parse SAXException " + ex2.getMessage());
                this.document = null;
            } catch (IOException ex3) {
                HwLog.e(DOMXmlParser.TAG, "do_parse IOException " + ex3.getMessage());
                this.document = null;
            }
        }

        public Document getDocument() {
            return this.document;
        }
    }

    public DOMXmlParser(InputStream is) {
        this.parser.do_parse(is);
    }

    public Element rootElement() {
        if (this.parser.getDocument() != null) {
            return this.parser.getDocument().getDocumentElement();
        }
        return null;
    }
}
