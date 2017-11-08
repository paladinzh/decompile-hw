package org.apache.commons.jexl2.parser;

public class TokenMgrError extends Error {
    private static final long serialVersionUID = 1;
    private String after;
    private int column;
    private char current;
    private boolean eof;
    private int errorCode;
    private int line;
    private int state;

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getMessage() {
        return "Lexical error at line " + this.line + ", column " + this.column + ".  Encountered: " + (!this.eof ? StringParser.escapeString(String.valueOf(this.current), '\"') + " (" + this.current + "), " : "<EOF> ") + "after : " + StringParser.escapeString(this.after, '\"');
    }

    public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
        this.eof = EOFSeen;
        this.state = lexState;
        this.line = errorLine;
        this.column = errorColumn;
        this.after = errorAfter;
        this.current = (char) curChar;
        this.errorCode = reason;
    }
}
