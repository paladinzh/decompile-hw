package com.huawei.watermark.manager.parse;

import org.xmlpull.v1.XmlPullParser;

public abstract class WMTextStyleElement extends WMElement {
    protected static final String DROIDCHINESE = "droidchinese";
    protected static final String DROIDCHINESELIM = "droidchineselim";
    protected static final String ROBOTOLIGHT = "robotolight";
    protected static final String ROBOTOREGULAR = "robotoregular";
    protected String bold;
    protected String fontcolor;
    protected String fontname;
    protected int max_height;
    protected int max_width;
    protected String shadowcolor;
    protected float shadowr;
    protected float shadowx;
    protected float shadowy;
    protected int size;

    public WMTextStyleElement(XmlPullParser parser) {
        super(parser);
        this.max_width = getIntByAttributeName(parser, "max_width");
        this.max_height = getIntByAttributeName(parser, "max_height");
        this.size = getIntByAttributeName(parser, "size");
        this.fontcolor = getStringByAttributeName(parser, "fontcolor", "#ffffffff");
        this.fontname = getStringByAttributeName(parser, "fontname");
        this.shadowcolor = getStringByAttributeName(parser, "shadowcolor", "#ffffffff");
        this.shadowx = getFloatByAttributeName(parser, "shadowx");
        this.shadowy = getFloatByAttributeName(parser, "shadowy");
        this.shadowr = getFloatByAttributeName(parser, "shadowr");
        this.bold = getStringByAttributeName(parser, "bold", "");
    }
}
