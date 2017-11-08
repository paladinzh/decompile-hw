package com.android.mms.model;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.cspcommon.MLog;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.w3c.dom.events.Event;

public class TextModel extends RegionMediaModel {
    private static final boolean IS_CHINA_OPTB = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private final int mCharset;
    private CharSequence mText;

    public TextModel(Context context, String contentType, String src, RegionModel region) {
        this(context, contentType, src, 106, new byte[0], region);
    }

    public TextModel(Context context, String contentType, String src, int charset, byte[] data, RegionModel region) {
        super(context, "text", contentType, src, data != null ? data : new byte[0], region);
        if (charset == 0) {
            charset = 4;
            if (IS_CHINA_OPTB) {
                charset = 106;
            }
        }
        this.mCharset = charset;
        this.mText = extractTextFromData(data);
    }

    private CharSequence extractTextFromData(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            if (this.mCharset == 0) {
                return new String(data, Charset.defaultCharset());
            }
            return new String(data, CharacterSets.getMimeName(this.mCharset));
        } catch (UnsupportedEncodingException e) {
            MLog.e("Mms/text", "Unsupported encoding: " + this.mCharset, (Throwable) e);
            return new String(data, Charset.defaultCharset());
        }
    }

    public byte[] encodeText(CharSequence text) {
        if (text == null) {
            return new byte[0];
        }
        try {
            return text.toString().getBytes(CharacterSets.getMimeName(this.mCharset));
        } catch (UnsupportedEncodingException e) {
            MLog.e("Mms/text", "Unsupported encoding: " + this.mCharset, (Throwable) e);
            return text.toString().getBytes(Charset.defaultCharset());
        }
    }

    public String getText() {
        if (this.mText == null) {
            this.mText = extractTextFromData(getData());
        }
        if (!(this.mText instanceof String)) {
            this.mText = this.mText.toString();
        }
        return this.mText.toString();
    }

    public void setText(CharSequence text) {
        this.mText = text;
        try {
            this.mSize = text.toString().getBytes(CharacterSets.getMimeName(this.mCharset)).length;
        } catch (UnsupportedEncodingException e) {
            MLog.e("Mms/text", "Unsupported encoding: " + this.mCharset, (Throwable) e);
            this.mSize = text.toString().getBytes(Charset.defaultCharset()).length;
        }
    }

    public void cloneText() {
        if (TextUtils.isEmpty(this.mText)) {
            this.mText = "";
        }
    }

    public int getCharset() {
        return this.mCharset;
    }

    public void handleEvent(Event evt) {
        if (evt.getType().equals("SmilMediaStart")) {
            this.mVisible = true;
        } else if (this.mFill != (short) 1) {
            this.mVisible = false;
        }
        notifyModelChanged(false);
    }
}
