package org.apache.commons.jexl2.parser;

import java.io.IOException;
import java.io.Reader;

public class SimpleCharStream {
    int available;
    protected int[] bufcolumn;
    protected char[] buffer;
    protected int[] bufline;
    public int bufpos;
    int bufsize;
    protected int column;
    protected int inBuf;
    protected Reader inputStream;
    protected int line;
    protected int maxNextCharInd;
    protected boolean prevCharIsCR;
    protected boolean prevCharIsLF;
    protected int tabSize;
    int tokenBegin;

    protected void ExpandBuff(boolean wrapAround) {
        char[] newbuffer = new char[(this.bufsize + 2048)];
        int[] newbufline = new int[(this.bufsize + 2048)];
        int[] newbufcolumn = new int[(this.bufsize + 2048)];
        int i;
        if (wrapAround) {
            System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
            System.arraycopy(this.buffer, 0, newbuffer, this.bufsize - this.tokenBegin, this.bufpos);
            this.buffer = newbuffer;
            System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
            System.arraycopy(this.bufline, 0, newbufline, this.bufsize - this.tokenBegin, this.bufpos);
            this.bufline = newbufline;
            System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
            System.arraycopy(this.bufcolumn, 0, newbufcolumn, this.bufsize - this.tokenBegin, this.bufpos);
            this.bufcolumn = newbufcolumn;
            i = this.bufpos + (this.bufsize - this.tokenBegin);
            this.bufpos = i;
            this.maxNextCharInd = i;
        } else {
            try {
                System.arraycopy(this.buffer, this.tokenBegin, newbuffer, 0, this.bufsize - this.tokenBegin);
                this.buffer = newbuffer;
                System.arraycopy(this.bufline, this.tokenBegin, newbufline, 0, this.bufsize - this.tokenBegin);
                this.bufline = newbufline;
                System.arraycopy(this.bufcolumn, this.tokenBegin, newbufcolumn, 0, this.bufsize - this.tokenBegin);
                this.bufcolumn = newbufcolumn;
                i = this.bufpos - this.tokenBegin;
                this.bufpos = i;
                this.maxNextCharInd = i;
            } catch (Throwable t) {
                Error error = new Error(t.getMessage());
            }
        }
        this.bufsize += 2048;
        this.available = this.bufsize;
        this.tokenBegin = 0;
    }

    protected void FillBuff() throws IOException {
        if (this.maxNextCharInd == this.available) {
            if (this.available != this.bufsize) {
                if (this.available > this.tokenBegin) {
                    this.available = this.bufsize;
                } else if (this.tokenBegin - this.available >= 2048) {
                    this.available = this.tokenBegin;
                } else {
                    ExpandBuff(true);
                }
            } else if (this.tokenBegin > 2048) {
                this.maxNextCharInd = 0;
                this.bufpos = 0;
                this.available = this.tokenBegin;
            } else if (this.tokenBegin >= 0) {
                ExpandBuff(false);
            } else {
                this.maxNextCharInd = 0;
                this.bufpos = 0;
            }
        }
        try {
            int i = this.inputStream.read(this.buffer, this.maxNextCharInd, this.available - this.maxNextCharInd);
            if (i != -1) {
                this.maxNextCharInd += i;
            } else {
                this.inputStream.close();
                throw new IOException();
            }
        } catch (IOException e) {
            this.bufpos--;
            backup(0);
            if (this.tokenBegin == -1) {
                this.tokenBegin = this.bufpos;
            }
            throw e;
        }
    }

    public char BeginToken() throws IOException {
        this.tokenBegin = -1;
        char c = readChar();
        this.tokenBegin = this.bufpos;
        return c;
    }

    protected void UpdateLineColumn(char c) {
        this.column++;
        int i;
        if (this.prevCharIsLF) {
            this.prevCharIsLF = false;
            i = this.line;
            this.column = 1;
            this.line = i + 1;
        } else if (this.prevCharIsCR) {
            this.prevCharIsCR = false;
            if (c != '\n') {
                i = this.line;
                this.column = 1;
                this.line = i + 1;
            } else {
                this.prevCharIsLF = true;
            }
        }
        switch (c) {
            case '\t':
                this.column--;
                this.column += this.tabSize - (this.column % this.tabSize);
                break;
            case '\n':
                this.prevCharIsLF = true;
                break;
            case '\r':
                this.prevCharIsCR = true;
                break;
        }
        this.bufline[this.bufpos] = this.line;
        this.bufcolumn[this.bufpos] = this.column;
    }

    public char readChar() throws IOException {
        int i;
        if (this.inBuf <= 0) {
            i = this.bufpos + 1;
            this.bufpos = i;
            if (i >= this.maxNextCharInd) {
                FillBuff();
            }
            char c = this.buffer[this.bufpos];
            UpdateLineColumn(c);
            return c;
        }
        this.inBuf--;
        i = this.bufpos + 1;
        this.bufpos = i;
        if (i == this.bufsize) {
            this.bufpos = 0;
        }
        return this.buffer[this.bufpos];
    }

    public int getEndColumn() {
        return this.bufcolumn[this.bufpos];
    }

    public int getEndLine() {
        return this.bufline[this.bufpos];
    }

    public int getBeginColumn() {
        return this.bufcolumn[this.tokenBegin];
    }

    public int getBeginLine() {
        return this.bufline[this.tokenBegin];
    }

    public void backup(int amount) {
        this.inBuf += amount;
        int i = this.bufpos - amount;
        this.bufpos = i;
        if (i < 0) {
            this.bufpos += this.bufsize;
        }
    }

    public SimpleCharStream(Reader dstream, int startline, int startcolumn, int buffersize) {
        this.bufpos = -1;
        this.column = 0;
        this.line = 1;
        this.prevCharIsCR = false;
        this.prevCharIsLF = false;
        this.maxNextCharInd = 0;
        this.inBuf = 0;
        this.tabSize = 8;
        this.inputStream = dstream;
        this.line = startline;
        this.column = startcolumn - 1;
        this.bufsize = buffersize;
        this.available = buffersize;
        this.buffer = new char[buffersize];
        this.bufline = new int[buffersize];
        this.bufcolumn = new int[buffersize];
    }

    public SimpleCharStream(Reader dstream, int startline, int startcolumn) {
        this(dstream, startline, startcolumn, 4096);
    }

    public void ReInit(Reader dstream, int startline, int startcolumn, int buffersize) {
        this.inputStream = dstream;
        this.line = startline;
        this.column = startcolumn - 1;
        if (this.buffer == null || buffersize != this.buffer.length) {
            this.bufsize = buffersize;
            this.available = buffersize;
            this.buffer = new char[buffersize];
            this.bufline = new int[buffersize];
            this.bufcolumn = new int[buffersize];
        }
        this.prevCharIsCR = false;
        this.prevCharIsLF = false;
        this.maxNextCharInd = 0;
        this.inBuf = 0;
        this.tokenBegin = 0;
        this.bufpos = -1;
    }

    public void ReInit(Reader dstream, int startline, int startcolumn) {
        ReInit(dstream, startline, startcolumn, 4096);
    }

    public String GetImage() {
        if (this.bufpos < this.tokenBegin) {
            return new String(this.buffer, this.tokenBegin, this.bufsize - this.tokenBegin) + new String(this.buffer, 0, this.bufpos + 1);
        }
        return new String(this.buffer, this.tokenBegin, (this.bufpos - this.tokenBegin) + 1);
    }
}
