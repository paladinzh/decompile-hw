package com.android.mms.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILRegionElement;

public class SmilRegionElementImpl extends SmilElementImpl implements SMILRegionElement {
    SmilRegionElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public String getFit() {
        String fit = getAttribute("fit");
        if ("fill".equalsIgnoreCase(fit)) {
            return "fill";
        }
        if ("meet".equalsIgnoreCase(fit)) {
            return "meet";
        }
        if ("scroll".equalsIgnoreCase(fit)) {
            return "scroll";
        }
        if ("slice".equalsIgnoreCase(fit)) {
            return "slice";
        }
        return "hidden";
    }

    public int getLeft() {
        try {
            return parseRegionLength(getAttribute("left"), true);
        } catch (NumberFormatException e) {
            try {
                return (((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getWidth() - parseRegionLength(getAttribute("right"), true)) - parseRegionLength(getAttribute("width"), true);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    public int getTop() {
        try {
            return parseRegionLength(getAttribute("top"), false);
        } catch (NumberFormatException e) {
            try {
                return (((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getHeight() - parseRegionLength(getAttribute("bottom"), false)) - parseRegionLength(getAttribute("height"), false);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    public void setFit(String fit) throws DOMException {
        if (fit.equalsIgnoreCase("fill") || fit.equalsIgnoreCase("meet") || fit.equalsIgnoreCase("scroll") || fit.equalsIgnoreCase("slice")) {
            setAttribute("fit", fit.toLowerCase());
        } else {
            setAttribute("fit", "hidden");
        }
    }

    public void setLeft(int left) throws DOMException {
        setAttribute("left", String.valueOf(left));
    }

    public void setTop(int top) throws DOMException {
        setAttribute("top", String.valueOf(top));
    }

    public String getBackgroundColor() {
        return getAttribute("backgroundColor");
    }

    public int getHeight() {
        try {
            int height = parseRegionLength(getAttribute("height"), false);
            if (height == 0) {
                height = ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getHeight();
            }
            return height;
        } catch (NumberFormatException e) {
            int bbh = ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getHeight();
            try {
                bbh -= parseRegionLength(getAttribute("top"), false);
            } catch (NumberFormatException e2) {
            }
            try {
                bbh -= parseRegionLength(getAttribute("bottom"), false);
            } catch (NumberFormatException e3) {
            }
            return bbh;
        }
    }

    public int getWidth() {
        try {
            int width = parseRegionLength(getAttribute("width"), true);
            if (width == 0) {
                width = ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getWidth();
            }
            return width;
        } catch (NumberFormatException e) {
            int bbw = ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getWidth();
            try {
                bbw -= parseRegionLength(getAttribute("left"), true);
            } catch (NumberFormatException e2) {
            }
            try {
                bbw -= parseRegionLength(getAttribute("right"), true);
            } catch (NumberFormatException e3) {
            }
            return bbw;
        }
    }

    public void setBackgroundColor(String backgroundColor) throws DOMException {
        setAttribute("backgroundColor", backgroundColor);
    }

    public void setHeight(int height) throws DOMException {
        setAttribute("height", String.valueOf(height) + "px");
    }

    public void setWidth(int width) throws DOMException {
        setAttribute("width", String.valueOf(width) + "px");
    }

    public String getId() {
        return getAttribute("id");
    }

    public void setId(String id) throws DOMException {
        setAttribute("id", id);
    }

    private int parseRegionLength(String length, boolean horizontal) {
        if (length.endsWith("px")) {
            return Integer.parseInt(length.substring(0, length.indexOf("px")));
        }
        if (!length.endsWith("%")) {
            return Integer.parseInt(length);
        }
        double value = 0.01d * ((double) Integer.parseInt(length.substring(0, length.length() - 1)));
        if (horizontal) {
            value *= (double) ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getWidth();
        } else {
            value *= (double) ((SMILDocument) getOwnerDocument()).getLayout().getRootLayout().getHeight();
        }
        return (int) Math.round(value);
    }

    public String toString() {
        return super.toString() + ": id=" + getId() + ", width=" + getWidth() + ", height=" + getHeight() + ", left=" + getLeft() + ", top=" + getTop();
    }
}
