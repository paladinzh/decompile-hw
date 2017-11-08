package com.android.systemui.operatorname;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.operatorname.HwOperatorNameManager.Callback;
import com.android.systemui.utils.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.Locale;

public class HwOperatorNameParentView extends LinearLayout implements Callback {
    private static final String TAG = HwOperatorNameParentView.class.getSimpleName();
    private boolean mLastSettingsVisible;
    private String mLocalLanguage;
    HwOperatorNameManager mManager;
    private int mSimTextSizeNormal;
    private TextView mSmallOperatorView1;
    private TextView mSmallOperatorView2;
    private int mType;

    public HwOperatorNameParentView(Context ctx, AttributeSet attrSet) {
        this(ctx, attrSet, 0);
    }

    public HwOperatorNameParentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSimTextSizeNormal = 8;
        this.mLocalLanguage = BuildConfig.FLAVOR;
        this.mManager = new HwOperatorNameManager();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.OperatorNameParentView, defStyleAttr, 0);
        this.mType = a.getInteger(0, 0);
        a.recycle();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        HwLog.i(TAG, "onFinishInflate()");
        initViews();
    }

    protected void onAttachedToWindow() {
        HwLog.i(TAG, "onAttachedToWindow()");
        this.mManager.register(getContext(), this);
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        HwLog.i(TAG, "onDetachedFromWindow()");
        this.mManager.unregister(getContext(), this);
        super.onDetachedFromWindow();
    }

    public void onVisibilityChanged(boolean visible) {
        this.mLastSettingsVisible = visible;
        HwLog.i(TAG, "onVisibilityChanged visible:" + visible + " id:" + getId());
        if (this.mType == 1) {
            HwLog.i(TAG, "do not hide keyguard operator name");
            return;
        }
        int i;
        if (visible) {
            i = 0;
        } else {
            i = 8;
        }
        setVisibility(i);
    }

    public void setSingleCardName(String cardName) {
        HwLog.i(TAG, "setSingleCardName::cardName=" + cardName);
        this.mSmallOperatorView1.setVisibility(0);
        this.mSmallOperatorView1.setText(cardName);
        this.mSmallOperatorView1.setTextSize(1, 12.0f);
        this.mSmallOperatorView2.setVisibility(8);
        this.mSmallOperatorView1.requestLayout();
    }

    public void setMultiCardsName(String card1Name, String card2Name) {
        HwLog.i(TAG, "setMultiCardsName::card1Name=" + card1Name + ", card2Name=" + card2Name);
        this.mSmallOperatorView1.setVisibility(0);
        this.mSmallOperatorView1.setText(card1Name);
        this.mSmallOperatorView2.setVisibility(0);
        this.mSmallOperatorView2.setText(card2Name);
        updateMsimTextSize();
        this.mSmallOperatorView1.requestLayout();
        this.mSmallOperatorView2.requestLayout();
    }

    public void updateLocale(String locale) {
        this.mLocalLanguage = locale;
    }

    private void initViews() {
        this.mSmallOperatorView1 = (TextView) View.inflate(getContext(), R.layout.hw_operator_view, null);
        this.mSmallOperatorView1.setId(R.id.msim_status_bar_operators_card_one_id);
        this.mSmallOperatorView2 = (TextView) View.inflate(getContext(), R.layout.hw_operator_view, null);
        this.mSmallOperatorView2.setId(R.id.msim_status_bar_operators_card_two_id);
        addView(this.mSmallOperatorView1);
        addView(this.mSmallOperatorView2);
        this.mSimTextSizeNormal = getContext().getResources().getInteger(R.integer.sim_text_size_normal);
        this.mLocalLanguage = Locale.getDefault().getLanguage();
    }

    private void updateMsimTextSize() {
        if (TextUtils.isEmpty(this.mLocalLanguage)) {
            this.mLocalLanguage = Locale.getDefault().getLanguage();
        }
        if ("my".equalsIgnoreCase(this.mLocalLanguage) || "th".equalsIgnoreCase(this.mLocalLanguage)) {
            this.mSmallOperatorView1.setTextSize(1, 6.0f);
            this.mSmallOperatorView2.setTextSize(1, 6.0f);
            return;
        }
        this.mSmallOperatorView1.setTextSize(1, (float) this.mSimTextSizeNormal);
        this.mSmallOperatorView2.setTextSize(1, (float) this.mSimTextSizeNormal);
    }

    public void setVisibility(int visibility) {
        if (this.mType == 2) {
            if (!this.mLastSettingsVisible && visibility == 0) {
                HwLog.i(TAG, "last is not visible and now is visible , ignore");
                return;
            } else if (!this.mLastSettingsVisible && visibility == 4) {
                HwLog.i(TAG, "last is not visible and now is invisible , change to gone");
                super.setVisibility(8);
                return;
            }
        }
        super.setVisibility(visibility);
    }
}
