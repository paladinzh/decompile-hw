package org.apache.commons.jexl2.parser;

import fyusion.vislib.BuildConfig;

public class ParseException extends Exception {
    private static final long serialVersionUID = 1;
    private String after = BuildConfig.FLAVOR;
    private int column = -1;
    private int line = -1;

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public ParseException(Token currentToken, int[][] expectedTokenSequences, String[] tokenImage) {
        super("parse error");
        Token tok = currentToken.next;
        this.after = tok.image;
        this.line = tok.beginLine;
        this.column = tok.beginColumn;
    }

    public ParseException(String message) {
        super(message);
    }
}
