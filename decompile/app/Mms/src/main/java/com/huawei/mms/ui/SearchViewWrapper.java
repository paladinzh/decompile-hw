package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan.Standard;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwMessageUtils;
import java.util.Locale;

public class SearchViewWrapper implements OnTouchListener, OnClickListener {
    private Activity mContext;
    private Runnable mImeFresher = new Runnable() {
        public void run() {
            if (SearchViewWrapper.this.mSearchStyle == 3) {
                SearchViewWrapper.this.showSoftInputAndGetFocus();
            }
        }
    };
    private InputMethodManager mIms = null;
    private LinearLayout mInnerViewLayout;
    private View mLayout;
    private CustEditText mQueryText;
    private int mSearchCharLimit = 100;
    private OnClickListener mSearchClickListener = null;
    private int mSearchStyle = 0;
    private SearchViewListener mSearchViewListener;
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int after) {
            boolean hasText = !TextUtils.isEmpty(s);
            SearchViewWrapper.this.mViewClear.setVisibility(hasText ? 0 : 8);
            if (!hasText || s.length() < SearchViewWrapper.this.mSearchCharLimit) {
                SearchViewWrapper.this.mQueryText.setError(null);
            } else {
                SearchViewWrapper.this.mQueryText.setError(SearchViewWrapper.this.mContext.getString(R.string.entered_too_many_characters));
            }
            SearchViewWrapper.this.mSearchViewListener.onSearchTextChange(s, start, before, after);
        }

        public void afterTextChanged(Editable s) {
        }
    };
    private View mViewClear;

    public interface SearchViewListener {
        void onSearchTextChange(CharSequence charSequence, int i, int i2, int i3);
    }

    public SearchViewWrapper(Activity context) {
        this.mContext = context;
        this.mIms = (InputMethodManager) context.getSystemService("input_method");
        this.mSearchCharLimit = this.mContext.getResources().getInteger(R.integer.search_editor_char_limit);
    }

    public void setQueryText(CharSequence queryText) {
        this.mQueryText.setText(queryText);
    }

    public void setSearchClickListener(OnClickListener l) {
        this.mSearchClickListener = l;
    }

    public void setSearchViewListener(SearchViewListener l) {
        this.mSearchViewListener = l;
    }

    public SearchViewWrapper init(View parentView, int searchStyle) {
        if (parentView == null) {
            return this;
        }
        this.mLayout = parentView;
        this.mSearchStyle = searchStyle;
        this.mQueryText = (CustEditText) parentView.findViewById(R.id.search_text);
        this.mQueryText.setHint(getSearchViewSpannableHint(this.mContext, R.string.search_hint, R.color.search_hint_text_color));
        this.mQueryText.setCursorVisible(false);
        this.mInnerViewLayout = (LinearLayout) this.mLayout.findViewById(R.id.inner_searchlayout);
        this.mQueryText.setOnTouchListener(this);
        this.mQueryText.setCustOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 4 || event.getAction() != 1) {
                    return false;
                }
                SearchViewWrapper.this.mQueryText.postDelayed(new Runnable() {
                    public void run() {
                        SearchViewWrapper.this.hideSoftInputAndClearFocus();
                    }
                }, 100);
                return true;
            }
        });
        if (searchStyle == 3) {
            this.mQueryText.addTextChangedListener(this.mTextWatcher);
            if (HwMessageUtils.isSplitOn()) {
                ImageView buttonBack = (ImageView) parentView.findViewById(R.id.bt_back);
                buttonBack.setVisibility(0);
                buttonBack.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Activity activity = SearchViewWrapper.this.mContext;
                        Intent intent = new Intent(activity, ConversationList.class);
                        intent.setAction("android.intent.action.MAIN");
                        SearchViewWrapper.this.mContext.startActivity(intent);
                        ((ConversationList) activity).overridePendingTransition(0, 0);
                        activity.finish();
                    }
                });
            }
            this.mViewClear = parentView.findViewById(R.id.clearSearchResult);
            this.mViewClear.setOnClickListener(this);
            this.mQueryText.postDelayed(this.mImeFresher, 200);
            this.mQueryText.postDelayed(new Runnable() {
                public void run() {
                    SearchViewWrapper.this.emulateTouchEvent(0);
                    SearchViewWrapper.this.emulateTouchEvent(1);
                }
            }, 150);
            MessageUtils.setMargin(this.mContext, this.mLayout.findViewById(R.id.inner_searchlayout), 8, -1, 8, -1);
        } else {
            this.mQueryText.setFocusableInTouchMode(false);
            this.mContext.getWindow().setSoftInputMode(35);
            if (searchStyle == 2) {
                this.mInnerViewLayout.setBackgroundResource(R.drawable.message_search_view_edit_bg_disable);
                this.mQueryText.setHint(getSearchViewSpannableHint(this.mContext, R.string.search_hint, R.color.search_hint_text_color_disable));
            }
            hideSoftInputAndClearFocus();
        }
        return this;
    }

    public void setSearchStyle(int style) {
        if (style == 2) {
            this.mInnerViewLayout.setBackgroundResource(R.drawable.message_search_view_edit_bg_disable);
            this.mQueryText.setHint(getSearchViewSpannableHint(this.mContext, R.string.search_hint, R.color.search_hint_text_color_disable));
            this.mQueryText.setEnabled(false);
            this.mLayout.setEnabled(false);
        } else if (style == 1) {
            this.mInnerViewLayout.setBackgroundResource(R.drawable.message_search_view_edit_bg);
            this.mQueryText.setHint(getSearchViewSpannableHint(this.mContext, R.string.search_hint, R.color.search_hint_text_color));
            this.mQueryText.setEnabled(true);
            this.mLayout.setEnabled(true);
        } else {
            MLog.e("MMS_SERCHVIEW", "can't set to style " + style);
        }
    }

    public void onRotationChanged(int oldOritation, int newOritation) {
        MLog.d("MMS_SERCHVIEW", "onRotationChanged " + newOritation + " isFullscreenMode: " + this.mIms.isFullscreenMode() + "  mSearchStyle: " + this.mSearchStyle);
        if (TextUtils.isEmpty(this.mQueryText.getText().toString())) {
            this.mQueryText.postDelayed(this.mImeFresher, 200);
        }
    }

    public void hideSoftInputAndClearFocus() {
        this.mContext.getWindow().setSoftInputMode(32);
        this.mIms.hideSoftInputFromWindow(this.mQueryText.getWindowToken(), 0);
        this.mQueryText.setFocusableInTouchMode(false);
        this.mLayout.clearFocus();
        this.mQueryText.setCursorVisible(false);
    }

    public void showSoftInputAndGetFocus() {
        if (!this.mQueryText.hasFocus() || !this.mQueryText.isCursorVisible()) {
            showSoftInput();
        }
    }

    private void showSoftInput() {
        this.mQueryText.setFocusableInTouchMode(true);
        this.mQueryText.setFocusable(true);
        this.mQueryText.requestFocus();
        this.mQueryText.setCursorVisible(true);
        this.mContext.getWindow().setSoftInputMode(37);
        this.mIms.showSoftInput(this.mQueryText, 0);
        this.mIms.showSoftInputFromInputMethod(this.mQueryText.getWindowToken(), 0);
    }

    private void emulateTouchEvent(int event) {
        this.mQueryText.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, event, 0.0f, 0.0f, 0));
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() != 1) {
            return false;
        }
        switch (v.getId()) {
            case R.id.search_text:
                if (this.mSearchStyle != 3) {
                    if (this.mSearchClickListener != null) {
                        this.mSearchClickListener.onClick(v);
                        break;
                    }
                }
                showSoftInputAndGetFocus();
                if (HwMessageUtils.isSplitOn()) {
                    ((ConversationList) this.mContext).showRightCover();
                    break;
                }
                break;
        }
        return false;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearSearchResult:
                clearQueryText();
                if (HwMessageUtils.isSplitOn()) {
                    ((ConversationList) this.mContext).backToListWhenSplit();
                    ((ConversationList) this.mContext).showRightCover();
                }
                showSoftInput();
                return;
            default:
                return;
        }
    }

    public void clearQueryText() {
        this.mQueryText.setText("");
    }

    public static SpannableStringBuilder getSearchViewSpannableHint(Context aContext, int hintRes, int hintColorRes) {
        String hintText = aContext.getResources().getString(hintRes);
        boolean aIsMirror = isLayoutRTL();
        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        ssb.append(hintText);
        if (aIsMirror) {
            ssb.setSpan(new Standard(Alignment.ALIGN_RIGHT), 0, ssb.toString().length(), 33);
        } else {
            ssb.setSpan(new Standard(Alignment.ALIGN_LEFT), 0, ssb.toString().length(), 33);
        }
        ssb.setSpan(new ForegroundColorSpan(aContext.getResources().getColor(hintColorRes)), 0, ssb.toString().length(), 33);
        return ssb;
    }

    public static boolean isLayoutRTL() {
        switch (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    public boolean onBackPressed() {
        if (!this.mQueryText.hasFocus() || !this.mQueryText.isCursorVisible()) {
            return false;
        }
        hideSoftInputAndClearFocus();
        return true;
    }

    public boolean isClearViewVisible() {
        boolean z = false;
        if (this.mViewClear == null) {
            return false;
        }
        if (this.mViewClear.getVisibility() == 0) {
            z = true;
        }
        return z;
    }
}
