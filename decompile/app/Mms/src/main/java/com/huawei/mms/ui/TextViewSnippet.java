package com.huawei.mms.ui;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextViewSnippet extends TextView {
    private static String sEllipsis = "…";
    private final boolean IS_MIRROR_LANGUAGE = MessageUtils.isNeedLayoutRtl();
    private String mFullText;
    private Pattern mPattern;
    private String mPurpose;
    private String mTargetString;

    public TextViewSnippet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewSnippet(Context context) {
        super(context);
    }

    public TextViewSnippet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mFullText == null || this.mTargetString == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        Matcher m;
        int start;
        String fullTextLower = this.mFullText.toLowerCase(Locale.getDefault());
        String targetStringLower = this.mTargetString.toLowerCase(Locale.getDefault());
        int startPos = 0;
        int bodyLength = fullTextLower.length();
        int endPos = 0;
        if (this.mPattern != null) {
            m = this.mPattern.matcher(this.mFullText);
            int cnt = 0;
            while (m.find(endPos)) {
                if (cnt == 0) {
                    startPos = m.start();
                }
                int end = m.end();
                if (end == endPos) {
                    break;
                }
                endPos = end;
                cnt++;
            }
            if (cnt > 1) {
                targetStringLower = this.mFullText.substring(startPos, endPos);
            }
        }
        int searchStringLength = targetStringLower.length();
        if (endPos == 0) {
            endPos = startPos + searchStringLength;
        }
        TextPaint tp = getPaint();
        float textFieldWidth = ((float) getWidth()) - (2.0f * tp.measureText(sEllipsis));
        String str = null;
        if (tp.measureText(targetStringLower) <= textFieldWidth) {
            int offset = -1;
            start = -1;
            end = -1;
            while (true) {
                offset++;
                int newstart = Math.max(0, startPos - offset);
                int newend = Math.min(bodyLength, (startPos + searchStringLength) + offset);
                if (newstart == start && newend == end) {
                    break;
                }
                start = newstart;
                end = newend;
                try {
                    String candidate = this.mFullText.substring(newstart, newend);
                    if (tp.measureText(candidate) > textFieldWidth) {
                        break;
                    }
                    String str2 = "%s%s%s";
                    Object[] objArr = new Object[3];
                    objArr[0] = newstart == 0 ? "" : sEllipsis;
                    objArr[1] = candidate;
                    objArr[2] = newend == bodyLength ? "" : sEllipsis;
                    str = String.format(str2, objArr);
                } catch (StringIndexOutOfBoundsException e) {
                    MLog.e("Mms_UI_TextViewSnippet", "sub string index error");
                }
            }
        } else {
            if (endPos > bodyLength) {
                endPos = bodyLength;
            }
            str = this.mFullText.substring(startPos, endPos);
        }
        if (str != null) {
            SpannableStringBuilder spannable;
            int filterCount = 0;
            if (this.mPurpose == null || this.mPurpose.isEmpty()) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
            } else {
                if (this.IS_MIRROR_LANGUAGE) {
                    filterCount = this.mPurpose.length() + 3;
                }
                spannable = getPurposeSpannableStringBuilder(str, this.mPurpose, this.IS_MIRROR_LANGUAGE);
            }
            if (this.mPattern != null) {
                m = this.mPattern.matcher(str);
                for (start = 0; m.find(start); start = m.end()) {
                    spannable.setSpan(new ForegroundColorSpan(ResEx.self().getSearchTextHightColor()), m.start() + filterCount, m.end() + filterCount, 17);
                    if (start == m.end()) {
                        break;
                    }
                }
            }
            super.setText(SmileyParser.getInstance().addSmileySpans(spannable, SMILEY_TYPE.SEARCH_MESSAGE_TEXTVIEW));
            super.onLayout(changed, left, top, right, bottom);
        }
    }

    public void setText(String fullText, String target, Pattern pattern) {
        setText(fullText, null, target, pattern);
    }

    public void setText(String fullText, String purpose, String target, Pattern pattern) {
        this.mPattern = null;
        this.mFullText = null;
        this.mTargetString = null;
        this.mPurpose = purpose;
        if (fullText == null) {
            MLog.e("Mms_UI_TextViewSnippet", "snippet setText empty text");
        } else if (target != null) {
            this.mPattern = pattern;
            this.mFullText = fullText;
            this.mTargetString = target;
            requestLayout();
        } else if (purpose == null || purpose.isEmpty()) {
            super.setText(fullText);
        } else {
            super.setText(getPurposeSpannableStringBuilder(fullText, purpose, this.IS_MIRROR_LANGUAGE));
        }
    }

    public static SpannableStringBuilder getPurposeSpannableStringBuilder(String text, String purpose, boolean isMirrorLanguage) {
        int sizeSpanEnd;
        SpannableStringBuilder spannable;
        String space = " | ";
        int sizeSpanStart = 0;
        if (isMirrorLanguage) {
            purpose = purpose + space;
            sizeSpanEnd = purpose.length() - space.length();
            spannable = new SpannableStringBuilder(purpose + text);
        } else {
            purpose = space + purpose;
            sizeSpanStart = text.length() + space.length();
            sizeSpanEnd = text.length() + purpose.length();
            spannable = new SpannableStringBuilder(text + purpose);
        }
        try {
            spannable.setSpan(new AbsoluteSizeSpan((int) MmsApp.getApplication().getResources().getDimension(R.dimen.mms_size_span_text_size)), sizeSpanStart, sizeSpanEnd, 33);
        } catch (Exception e) {
            MLog.e("Mms_UI_TextViewSnippet", "TextViewSnippet getPurposeSpannableStringBuilder exception", (Throwable) e);
        }
        return spannable;
    }
}
