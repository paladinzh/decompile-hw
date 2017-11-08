package org.kxml2.wap;

import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WbxmlParser implements XmlPullParser {
    private int ATTR_START_TABLE = 1;
    private int ATTR_VALUE_TABLE = 2;
    private int TAG_TABLE = 0;
    private String[] attrStartTable;
    private String[] attrValueTable;
    private int attributeCount;
    private String[] attributes = new String[16];
    private Hashtable cacheStringTable = null;
    private boolean degenerated;
    private int depth;
    private String[] elementStack = new String[16];
    private String encoding;
    private InputStream in;
    private boolean isWhitespace;
    private String name;
    private String namespace;
    private int nextId = -2;
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    private String prefix;
    private boolean processNsp;
    private int publicIdentifierId;
    private byte[] stringTable;
    private Vector tables = new Vector();
    private String[] tagTable;
    private String text;
    private int type;
    private int wapCode;
    private Object wapExtensionData;

    public boolean getFeature(String feature) {
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(feature)) {
            return this.processNsp;
        }
        return false;
    }

    public String getInputEncoding() {
        return this.encoding;
    }

    public void defineEntityReplacementText(String entity, String value) throws XmlPullParserException {
    }

    public Object getProperty(String property) {
        return null;
    }

    public int getNamespaceCount(int depth) {
        if (depth <= this.depth) {
            return this.nspCounts[depth];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getNamespacePrefix(int pos) {
        return this.nspStack[pos << 1];
    }

    public String getNamespaceUri(int pos) {
        return this.nspStack[(pos << 1) + 1];
    }

    public String getNamespace(String prefix) {
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if ("xmlns".equals(prefix)) {
            return "http://www.w3.org/2000/xmlns/";
        }
        for (int i = (getNamespaceCount(this.depth) << 1) - 2; i >= 0; i -= 2) {
            if (prefix == null) {
                if (this.nspStack[i] == null) {
                    return this.nspStack[i + 1];
                }
            } else if (prefix.equals(this.nspStack[i])) {
                return this.nspStack[i + 1];
            }
        }
        return null;
    }

    public int getDepth() {
        return this.depth;
    }

    public String getPositionDescription() {
        StringBuffer buf = new StringBuffer(this.type < TYPES.length ? TYPES[this.type] : "unknown");
        buf.append(' ');
        if (this.type == 2 || this.type == 3) {
            if (this.degenerated) {
                buf.append("(empty) ");
            }
            buf.append('<');
            if (this.type == 3) {
                buf.append('/');
            }
            if (this.prefix != null) {
                buf.append("{" + this.namespace + "}" + this.prefix + ":");
            }
            buf.append(this.name);
            int cnt = this.attributeCount << 2;
            for (int i = 0; i < cnt; i += 4) {
                buf.append(' ');
                if (this.attributes[i + 1] != null) {
                    buf.append("{" + this.attributes[i] + "}" + this.attributes[i + 1] + ":");
                }
                buf.append(this.attributes[i + 2] + "='" + this.attributes[i + 3] + "'");
            }
            buf.append('>');
        } else if (this.type != 7) {
            if (this.type != 4) {
                buf.append(getText());
            } else if (this.isWhitespace) {
                buf.append("(whitespace)");
            } else {
                String text = getText();
                if (text.length() > 16) {
                    text = text.substring(0, 16) + "...";
                }
                buf.append(text);
            }
        }
        return buf.toString();
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (!(this.type == 4 || this.type == 7 || this.type == 5)) {
            exception("Wrong event type");
        }
        return this.isWhitespace;
    }

    public String getText() {
        return this.text;
    }

    public char[] getTextCharacters(int[] poslen) {
        if (this.type >= 4) {
            poslen[0] = 0;
            poslen[1] = this.text.length();
            char[] buf = new char[this.text.length()];
            this.text.getChars(0, this.text.length(), buf, 0);
            return buf;
        }
        poslen[0] = -1;
        poslen[1] = -1;
        return null;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.type != 2) {
            exception("Wrong event type");
        }
        return this.degenerated;
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int index) {
        return false;
    }

    public String getAttributeNamespace(int index) {
        if (index < this.attributeCount) {
            return this.attributes[index << 2];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeName(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 2];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributePrefix(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index << 2) + 3];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(String namespace, String name) {
        int i = (this.attributeCount << 2) - 4;
        while (i >= 0) {
            if (this.attributes[i + 2].equals(name) && (namespace == null || this.attributes[i].equals(namespace))) {
                return this.attributes[i + 3];
            }
            i -= 4;
        }
        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return this.type;
    }

    public int next() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        int minType = 9999;
        while (true) {
            String save = this.text;
            nextImpl();
            if (this.type < minType) {
                minType = this.type;
            }
            if (minType <= 5) {
                if (minType >= 4) {
                    if (save != null) {
                        if (this.text != null) {
                            save = save + this.text;
                        }
                        this.text = save;
                    }
                    switch (peekId()) {
                        case 2:
                        case 3:
                        case 4:
                        case Place.TYPE_PAINTER /*68*/:
                        case 131:
                        case 132:
                        case 196:
                            break;
                        default:
                            break;
                    }
                }
                this.type = minType;
                if (this.type > 4) {
                    this.type = 4;
                }
                return this.type;
            }
        }
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        nextImpl();
        return this.type;
    }

    public int nextTag() throws XmlPullParserException, IOException {
        next();
        if (this.type == 4 && this.isWhitespace) {
            next();
        }
        if (!(this.type == 3 || this.type == 2)) {
            exception("unexpected type");
        }
        return this.type;
    }

    public String nextText() throws XmlPullParserException, IOException {
        String result;
        if (this.type != 2) {
            exception("precondition: START_TAG");
        }
        next();
        if (this.type == 4) {
            result = getText();
            next();
        } else {
            result = "";
        }
        if (this.type != 3) {
            exception("END_TAG expected");
        }
        return result;
    }

    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.type || ((namespace != null && !namespace.equals(getNamespace())) || (name != null && !name.equals(getName())))) {
            exception("expected: " + (type == 64 ? "WAP Ext." : TYPES[type] + " {" + namespace + "}" + name));
        }
    }

    public void setInput(Reader reader) throws XmlPullParserException {
        exception("InputStream required");
    }

    public void setInput(InputStream in, String enc) throws XmlPullParserException {
        this.in = in;
        try {
            readByte();
            this.publicIdentifierId = readInt();
            if (this.publicIdentifierId == 0) {
                readInt();
            }
            int charset = readInt();
            if (enc == null) {
                switch (charset) {
                    case 4:
                        this.encoding = "ISO-8859-1";
                        break;
                    case 106:
                        this.encoding = "UTF-8";
                        break;
                    default:
                        throw new UnsupportedEncodingException("" + charset);
                }
            }
            this.encoding = enc;
            int strTabSize = readInt();
            this.stringTable = new byte[strTabSize];
            int ok = 0;
            while (ok < strTabSize) {
                int cnt = in.read(this.stringTable, ok, strTabSize - ok);
                if (cnt <= 0) {
                    selectPage(0, true);
                    selectPage(0, false);
                }
                ok += cnt;
            }
            selectPage(0, true);
            selectPage(0, false);
        } catch (IOException e) {
            exception("Illegal input format");
        }
    }

    public void setFeature(String feature, boolean value) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(feature)) {
            this.processNsp = value;
        } else {
            exception("unsupported feature: " + feature);
        }
    }

    public void setProperty(String property, Object value) throws XmlPullParserException {
        throw new XmlPullParserException("unsupported property: " + property);
    }

    private final boolean adjustNsp() throws XmlPullParserException {
        boolean any = false;
        int i = 0;
        while (i < (this.attributeCount << 2)) {
            String prefix;
            String attrName = this.attributes[i + 2];
            int cut = attrName.indexOf(58);
            if (cut != -1) {
                prefix = attrName.substring(0, cut);
                attrName = attrName.substring(cut + 1);
            } else if (attrName.equals("xmlns")) {
                prefix = attrName;
                attrName = null;
            } else {
                i += 4;
            }
            if (prefix.equals("xmlns")) {
                int[] iArr = this.nspCounts;
                int i2 = this.depth;
                int i3 = iArr[i2];
                iArr[i2] = i3 + 1;
                int j = i3 << 1;
                this.nspStack = ensureCapacity(this.nspStack, j + 2);
                this.nspStack[j] = attrName;
                this.nspStack[j + 1] = this.attributes[i + 3];
                if (attrName != null && this.attributes[i + 3].equals("")) {
                    exception("illegal empty namespace");
                }
                Object obj = this.attributes;
                i2 = i + 4;
                Object obj2 = this.attributes;
                int i4 = this.attributeCount - 1;
                this.attributeCount = i4;
                System.arraycopy(obj, i2, obj2, i, (i4 << 2) - i);
                i -= 4;
            } else {
                any = true;
            }
            i += 4;
        }
        if (any) {
            for (i = (this.attributeCount << 2) - 4; i >= 0; i -= 4) {
                attrName = this.attributes[i + 2];
                cut = attrName.indexOf(58);
                if (cut == 0) {
                    throw new RuntimeException("illegal attribute name: " + attrName + " at " + this);
                }
                if (cut != -1) {
                    String attrPrefix = attrName.substring(0, cut);
                    attrName = attrName.substring(cut + 1);
                    String attrNs = getNamespace(attrPrefix);
                    if (attrNs == null) {
                        throw new RuntimeException("Undefined Prefix: " + attrPrefix + " in " + this);
                    }
                    this.attributes[i] = attrNs;
                    this.attributes[i + 1] = attrPrefix;
                    this.attributes[i + 2] = attrName;
                    j = (this.attributeCount << 2) - 4;
                    while (j > i) {
                        if (attrName.equals(this.attributes[j + 2]) && attrNs.equals(this.attributes[j])) {
                            exception("Duplicate Attribute: {" + attrNs + "}" + attrName);
                        }
                        j -= 4;
                    }
                }
            }
        }
        if (this.name != null) {
            cut = this.name.indexOf(58);
            if (cut == 0) {
                exception("illegal tag name: " + this.name);
            } else if (cut != -1) {
                this.prefix = this.name.substring(0, cut);
                this.name = this.name.substring(cut + 1);
            }
            this.namespace = getNamespace(this.prefix);
            if (this.namespace == null) {
                if (this.prefix != null) {
                    exception("undefined prefix: " + this.prefix);
                }
                this.namespace = "";
            }
        }
        return any;
    }

    private final void setTable(int page, int type, String[] table) {
        if (this.stringTable != null) {
            throw new RuntimeException("setXxxTable must be called before setInput!");
        }
        while (this.tables.size() < (page * 3) + 3) {
            this.tables.addElement(null);
        }
        this.tables.setElementAt(table, (page * 3) + type);
    }

    private final void exception(String desc) throws XmlPullParserException {
        throw new XmlPullParserException(desc, this, null);
    }

    private void selectPage(int nr, boolean tags) throws XmlPullParserException {
        if (this.tables.size() != 0 || nr != 0) {
            if (nr * 3 > this.tables.size()) {
                exception("Code Page " + nr + " undefined!");
            }
            if (tags) {
                this.tagTable = (String[]) this.tables.elementAt((nr * 3) + this.TAG_TABLE);
            } else {
                this.attrStartTable = (String[]) this.tables.elementAt((nr * 3) + this.ATTR_START_TABLE);
                this.attrValueTable = (String[]) this.tables.elementAt((nr * 3) + this.ATTR_VALUE_TABLE);
            }
        }
    }

    private final void nextImpl() throws IOException, XmlPullParserException {
        if (this.type == 3) {
            this.depth--;
        }
        if (this.degenerated) {
            this.type = 3;
            this.degenerated = false;
            return;
        }
        this.text = null;
        this.prefix = null;
        this.name = null;
        int id = peekId();
        while (id == 0) {
            this.nextId = -2;
            selectPage(readByte(), true);
            id = peekId();
        }
        this.nextId = -2;
        switch (id) {
            case -1:
                this.type = 1;
                break;
            case 1:
                int sp = (this.depth - 1) << 2;
                this.type = 3;
                this.namespace = this.elementStack[sp];
                this.prefix = this.elementStack[sp + 1];
                this.name = this.elementStack[sp + 2];
                break;
            case 2:
                this.type = 6;
                char c = (char) readInt();
                this.text = "" + c;
                this.name = "#" + c;
                break;
            case 3:
                this.type = 4;
                this.text = readStrI();
                break;
            case Place.TYPE_MOVIE_THEATER /*64*/:
            case Place.TYPE_MOVING_COMPANY /*65*/:
            case Place.TYPE_MUSEUM /*66*/:
            case 128:
            case 129:
            case 130:
            case 192:
            case 193:
            case 194:
            case 195:
                this.type = 64;
                this.wapCode = id;
                this.wapExtensionData = parseWapExtension(id);
                break;
            case Place.TYPE_NIGHT_CLUB /*67*/:
                throw new RuntimeException("PI curr. not supp.");
            case 131:
                this.type = 4;
                this.text = readStrT();
                break;
            default:
                parseElement(id);
                break;
        }
    }

    public Object parseWapExtension(int id) throws IOException, XmlPullParserException {
        switch (id) {
            case Place.TYPE_MOVIE_THEATER /*64*/:
            case Place.TYPE_MOVING_COMPANY /*65*/:
            case Place.TYPE_MUSEUM /*66*/:
                return readStrI();
            case 128:
            case 129:
            case 130:
                return Integer.valueOf(readInt());
            case 192:
            case 193:
            case 194:
                return null;
            case 195:
                int count = readInt();
                byte[] buf = new byte[count];
                while (count > 0) {
                    count -= this.in.read(buf, buf.length - count, count);
                }
                return buf;
            default:
                exception("illegal id: " + id);
                return null;
        }
    }

    public void readAttr() throws IOException, XmlPullParserException {
        int id = readByte();
        int i = 0;
        while (id != 1) {
            while (id == 0) {
                selectPage(readByte(), false);
                id = readByte();
            }
            String name = resolveId(this.attrStartTable, id);
            StringBuffer stringBuffer = null;
            int cut = 0;
            if (name != null) {
                cut = name.indexOf(61);
            }
            if (cut == -1) {
                stringBuffer = new StringBuffer();
            } else if (name != null) {
                stringBuffer = new StringBuffer(name.substring(cut + 1));
                name = name.substring(0, cut);
            }
            id = readByte();
            while (true) {
                if (id >= 128 || id == 0 || id == 2 || id == 3 || (id >= 64 && id <= 66)) {
                    switch (id) {
                        case 0:
                            selectPage(readByte(), false);
                            break;
                        case 2:
                            if (stringBuffer == null) {
                                break;
                            }
                            stringBuffer.append((char) readInt());
                            break;
                        case 3:
                            if (stringBuffer == null) {
                                break;
                            }
                            stringBuffer.append(readStrI());
                            break;
                        case Place.TYPE_MOVIE_THEATER /*64*/:
                        case Place.TYPE_MOVING_COMPANY /*65*/:
                        case Place.TYPE_MUSEUM /*66*/:
                        case 128:
                        case 129:
                        case 130:
                        case 192:
                        case 193:
                        case 194:
                        case 195:
                            if (stringBuffer == null) {
                                break;
                            }
                            stringBuffer.append(resolveWapExtension(id, parseWapExtension(id)));
                            break;
                        case 131:
                            if (stringBuffer == null) {
                                break;
                            }
                            stringBuffer.append(readStrT());
                            break;
                        default:
                            if (stringBuffer == null) {
                                break;
                            }
                            stringBuffer.append(resolveId(this.attrValueTable, id));
                            break;
                    }
                    id = readByte();
                } else {
                    this.attributes = ensureCapacity(this.attributes, i + 4);
                    int i2 = i + 1;
                    this.attributes[i] = "";
                    i = i2 + 1;
                    this.attributes[i2] = null;
                    if (name != null) {
                        i2 = i + 1;
                        this.attributes[i] = name;
                        i = i2;
                    }
                    if (stringBuffer != null) {
                        i2 = i + 1;
                        this.attributes[i] = stringBuffer.toString();
                    } else {
                        i2 = i;
                    }
                    this.attributeCount++;
                    i = i2;
                }
            }
        }
    }

    private int peekId() throws IOException {
        if (this.nextId == -2) {
            this.nextId = this.in.read();
        }
        return this.nextId;
    }

    protected String resolveWapExtension(int id, Object data) {
        if (!(data instanceof byte[])) {
            return "$(" + data + ")";
        }
        StringBuffer sb = new StringBuffer();
        byte[] b = (byte[]) data;
        for (int i = 0; i < b.length; i++) {
            sb.append("0123456789abcdef".charAt((b[i] >> 4) & 15));
            sb.append("0123456789abcdef".charAt(b[i] & 15));
        }
        return sb.toString();
    }

    String resolveId(String[] tab, int id) throws IOException {
        int idx = (id & 127) - 5;
        if (idx == -1) {
            this.wapCode = -1;
            return readStrT();
        } else if (idx < 0 || tab == null || idx >= tab.length || tab[idx] == null) {
            throw new IOException("id " + id + " undef.");
        } else {
            this.wapCode = idx + 5;
            return tab[idx];
        }
    }

    void parseElement(int id) throws IOException, XmlPullParserException {
        boolean z;
        this.type = 2;
        this.name = resolveId(this.tagTable, id & 63);
        this.attributeCount = 0;
        if ((id & 128) != 0) {
            readAttr();
        }
        if ((id & 64) == 0) {
            z = true;
        } else {
            z = false;
        }
        this.degenerated = z;
        int i = this.depth;
        this.depth = i + 1;
        int sp = i << 2;
        this.elementStack = ensureCapacity(this.elementStack, sp + 4);
        if (this.name != null) {
            this.elementStack[sp + 3] = this.name;
        }
        if (this.depth >= this.nspCounts.length) {
            int[] bigger = new int[(this.depth + 4)];
            System.arraycopy(this.nspCounts, 0, bigger, 0, this.nspCounts.length);
            this.nspCounts = bigger;
        }
        this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];
        for (int i2 = this.attributeCount - 1; i2 > 0; i2--) {
            for (int j = 0; j < i2; j++) {
                if (getAttributeName(i2).equals(getAttributeName(j))) {
                    exception("Duplicate Attribute: " + getAttributeName(i2));
                }
            }
        }
        if (this.processNsp) {
            adjustNsp();
        } else {
            this.namespace = "";
        }
        this.elementStack[sp] = this.namespace;
        this.elementStack[sp + 1] = this.prefix;
        if (this.name != null) {
            this.elementStack[sp + 2] = this.name;
        }
    }

    private final String[] ensureCapacity(String[] arr, int required) {
        if (arr.length >= required) {
            return arr;
        }
        String[] bigger = new String[(required + 16)];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }

    int readByte() throws IOException {
        int i = this.in.read();
        if (i != -1) {
            return i;
        }
        throw new IOException("Unexpected EOF");
    }

    int readInt() throws IOException {
        int result = 0;
        int i;
        do {
            i = readByte();
            result = (result << 7) | (i & 127);
        } while ((i & 128) != 0);
        return result;
    }

    String readStrI() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        boolean wsp = true;
        while (true) {
            int i = this.in.read();
            if (i == 0) {
                this.isWhitespace = wsp;
                String result = new String(buf.toByteArray(), this.encoding);
                buf.close();
                return result;
            } else if (i == -1) {
                throw new IOException("Unexpected EOF");
            } else {
                if (i > 32) {
                    wsp = false;
                }
                buf.write(i);
            }
        }
    }

    String readStrT() throws IOException {
        int pos = readInt();
        if (this.cacheStringTable == null) {
            this.cacheStringTable = new Hashtable();
        }
        String forReturn = (String) this.cacheStringTable.get(Integer.valueOf(pos));
        if (forReturn != null || this.stringTable == null) {
            return forReturn;
        }
        int end = pos;
        while (end < this.stringTable.length && this.stringTable[end] != (byte) 0) {
            end++;
        }
        forReturn = new String(this.stringTable, pos, end - pos, this.encoding);
        this.cacheStringTable.put(Integer.valueOf(pos), forReturn);
        return forReturn;
    }

    public void setTagTable(int page, String[] table) {
        setTable(page, this.TAG_TABLE, table);
    }

    public void setAttrStartTable(int page, String[] table) {
        setTable(page, this.ATTR_START_TABLE, table);
    }

    public void setAttrValueTable(int page, String[] table) {
        setTable(page, this.ATTR_VALUE_TABLE, table);
    }
}
