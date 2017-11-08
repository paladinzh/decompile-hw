package com.android.server.pfw.autostartup.xmlparser;

class PreciseServiceParser extends AbsPreciseParser {
    PreciseServiceParser() {
    }

    protected int getPreciseType() {
        return 2;
    }

    protected String getXmlSubElementKey() {
        return "caller";
    }
}
