package com.android.mms.ui;

import android.content.Context;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.util.HwBackgroundLoader;
import java.text.NumberFormat;

public class HwSlidePage extends LinearLayout {
    private Runnable mDeleteEmptyRunnable;
    private EditTextWithSmiley mEditText;
    private boolean mHasText;
    private TextView mIndexText;
    private HwSlideChangeListener mListener;
    private InputFilter mMaxTextLimitInputFilter;
    private int mMinPxSize;
    private OnFocusChangeListener mOnFocusChangeListener;
    private boolean mOnTextChangedListenerEnabled;
    private OnTouchListener mOnTouchListener;
    private TextView mPageNumber;
    private SmileyParser mParser;
    private int mPosition;
    private int mRestrictedTextLen;
    private View mSpliteView;
    private TextWatcher mTextWatcher;
    private Toast mToast;

    public HwSlidePage(Context context) {
        this(context, null);
    }

    public HwSlidePage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwSlidePage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPosition = -1;
        this.mRestrictedTextLen = -1;
        this.mMinPxSize = 0;
        this.mOnTextChangedListenerEnabled = true;
        this.mParser = null;
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (HwSlidePage.this.mOnTextChangedListenerEnabled && HwSlidePage.this.mListener != null) {
                    HwSlidePage.this.mListener.onSlideTextChange(HwSlidePage.this, s.toString());
                }
            }

            public void afterTextChanged(Editable s) {
                try {
                    if (-1 != HwSlidePage.this.mRestrictedTextLen) {
                        s.delete(HwSlidePage.this.mRestrictedTextLen, s.length());
                    }
                } catch (IndexOutOfBoundsException ex) {
                    MLog.e("HwSlidePage", "delete caused IndexOutOfBoundsException: ", (Throwable) ex);
                } catch (Exception e) {
                    MLog.e("HwSlidePage", "delete Exception: ", (Throwable) e);
                }
            }
        };
        this.mOnTouchListener = new OnTouchListener() {
            private boolean mIsLongClick = false;
            private boolean mIsMoved = false;
            private Runnable mLongClickRunnable = new Runnable() {
                public void run() {
                    AnonymousClass2.this.mIsLongClick = true;
                }
            };
            private float mStartX;
            private float mStartY;

            public boolean onTouch(View v, MotionEvent event) {
                boolean z = true;
                boolean isConsumed = false;
                switch (event.getAction()) {
                    case 0:
                        this.mStartX = event.getRawX();
                        this.mStartY = event.getRawY();
                        HwBackgroundLoader.getUIHandler().postDelayed(this.mLongClickRunnable, 800);
                        break;
                    case 1:
                        if (this.mIsLongClick || !this.mIsMoved) {
                            HwSlidePage.this.mListener.onInputManagerShow();
                        }
                        if (this.mIsMoved) {
                            isConsumed = true;
                        }
                        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                        this.mIsLongClick = false;
                        this.mIsMoved = false;
                        break;
                    case 2:
                        float moveY = event.getRawY();
                        float moveX = event.getRawX();
                        if (!this.mIsMoved) {
                            if (Math.abs(moveX - this.mStartX) < ((float) HwSlidePage.this.mMinPxSize) && Math.abs(moveY - this.mStartY) < ((float) HwSlidePage.this.mMinPxSize)) {
                                z = false;
                            }
                            this.mIsMoved = z;
                        }
                        if (this.mIsMoved) {
                            HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                            break;
                        }
                        break;
                    case 3:
                        if (this.mIsLongClick) {
                            HwSlidePage.this.mListener.onInputManagerShow();
                        }
                        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                        this.mIsLongClick = false;
                        this.mIsMoved = false;
                        break;
                }
                return isConsumed;
            }
        };
        this.mDeleteEmptyRunnable = new Runnable() {
            public void run() {
                HwSlidePage.this.mListener.onSlideRemoved(HwSlidePage.this);
            }
        };
        this.mOnFocusChangeListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    HwSlidePage.this.mListener.onSlideAcitived(HwSlidePage.this);
                }
            }
        };
        this.mMaxTextLimitInputFilter = new LengthFilter(MmsConfig.getMaxTextLimit()) {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dest == null) {
                    return null;
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                int length = dest.length();
                if (source != null) {
                    length += source.length();
                }
                if (length >= MmsConfig.getMaxTextLimit()) {
                    if (HwSlidePage.this.mToast == null) {
                        HwSlidePage.this.mToast = Toast.makeText(HwSlidePage.this.getContext(), R.string.entered_too_many_characters, 0);
                    }
                    HwSlidePage.this.mToast.show();
                }
                return result;
            }
        };
        this.mMinPxSize = (int) context.getResources().getDimension(R.dimen.editor_touch_moved_min_size);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEditText = (EditTextWithSmiley) findViewById(R.id.text_message);
        this.mPageNumber = (TextView) findViewById(R.id.hw_page_number_text);
        this.mEditText.setFilters(new InputFilter[]{this.mMaxTextLimitInputFilter});
        this.mParser = SmileyParser.getInstance();
        this.mIndexText = (TextView) findViewById(R.id.page_number_text);
        this.mSpliteView = findViewById(R.id.slides_splite_line);
        this.mEditText.setOnTouchListener(this.mOnTouchListener);
        this.mEditText.setEmptyDeleter(this.mDeleteEmptyRunnable);
        this.mEditText.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mEditText.setText(this.mEditText.getText().toString());
        this.mEditText.setVisibility(0);
        this.mEditText.addTextChangedListener(this.mTextWatcher);
    }

    public void setSlideChangeListener(HwSlideChangeListener l) {
        this.mListener = l;
    }

    public void setTextFocus() {
        this.mEditText.requestFocus();
        this.mEditText.setSelection(this.mEditText.length());
    }

    public void addAttachment(int type) {
        if (type == 1) {
            this.mHasText = true;
        }
    }

    public void removeAttachment(int type) {
        if (type == 1) {
            this.mHasText = false;
        }
    }

    public void setText(String name, String text) {
        this.mOnTextChangedListenerEnabled = false;
        if (!(text == null || text.equals(this.mEditText.getText().toString()))) {
            this.mEditText.setText(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mEditText.setSelection(this.mEditText.length());
        }
        this.mOnTextChangedListenerEnabled = true;
    }

    public String getText() {
        return this.mEditText.getText().toString();
    }

    public EditText getMsgEditor() {
        return this.mEditText;
    }

    public void resetPosition(int location) {
        if (this.mPosition != location) {
            this.mPosition = location;
        }
    }

    public void setPosition(int location) {
        this.mPosition = location;
    }

    public void showSlideIndex(int current, int all) {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        if (MessageUtils.isNeedLayoutRtl()) {
            this.mPageNumber.setText(nf.format((long) all) + "/" + nf.format((long) (current + 1)));
        } else {
            this.mPageNumber.setText(nf.format((long) (current + 1)) + "/" + nf.format((long) all));
        }
        resetPosition(current);
        if (current + 1 >= all) {
            this.mSpliteView.setVisibility(8);
            return;
        }
        this.mIndexText.setText(this.mContext.getString(R.string.slides_index, new Object[]{nf.format((long) (current + 1)), nf.format((long) all)}));
        this.mSpliteView.setVisibility(0);
    }

    public int getPosition() {
        return this.mPosition;
    }

    public boolean isItemsFocused() {
        return this.mEditText.isFocused();
    }

    public void setRestrictedTextLen(int len) {
        this.mRestrictedTextLen = len;
    }
}
