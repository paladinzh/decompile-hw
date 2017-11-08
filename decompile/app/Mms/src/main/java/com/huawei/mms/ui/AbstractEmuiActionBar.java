package com.huawei.mms.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;

public class AbstractEmuiActionBar implements ActionBarInterface {
    protected ActionBar mActionBarInner;
    private int mActionMode = 0;
    protected Context mContext;
    protected BaseCustomEmuiEditView mCustomView;
    protected boolean mDestroyWhenExitActionMode = false;
    protected BaseCustomEmuiSearchView mSearchBar;

    public abstract class BaseCustomEmuiEditView {
        protected OnClickListener mCancelListener;
        protected ImageView mCancelView;
        protected View mCustomedView;
        protected OnClickListener mOkListener;
        protected ImageView mOkView;
        protected SplitActionBarView mSplitActionBar;
        protected TextView mSubTitle;
        protected TextView mTitle;
        protected LinearLayout mTitleContainer;
        protected LinearLayout mTitleHolder;
        protected TextView mTitleNumber;

        public abstract Menu getActionMenu();

        protected abstract boolean initLayoutForEditMode();

        protected abstract boolean unInitLayoutForEditMode();

        public boolean onEnterEditMode(OnClickListener cancelListener) {
            this.mTitle = null;
            this.mSubTitle = null;
            this.mTitleNumber = null;
            this.mCancelView = null;
            this.mOkListener = null;
            this.mCancelListener = cancelListener;
            return initLayoutForEditMode();
        }

        public boolean onEnterEditMode(OnClickListener cancelListener, OnClickListener okListener) {
            this.mTitle = null;
            this.mSubTitle = null;
            this.mTitleNumber = null;
            this.mCancelView = null;
            this.mCancelListener = cancelListener;
            this.mOkListener = okListener;
            return initLayoutForEditMode();
        }

        public boolean onExitEditMode() {
            this.mTitle = null;
            this.mSubTitle = null;
            this.mTitleNumber = null;
            this.mCancelView = null;
            this.mCancelListener = null;
            this.mOkListener = null;
            return unInitLayoutForEditMode();
        }

        public boolean onExitSelectMode() {
            this.mTitle = null;
            this.mSubTitle = null;
            this.mTitleNumber = null;
            this.mCancelView = null;
            this.mCancelListener = null;
            this.mOkListener = null;
            return unInitLayoutForEditMode();
        }

        public boolean onExitTitleMode() {
            this.mTitle = null;
            this.mSubTitle = null;
            this.mTitleNumber = null;
            this.mCancelView = null;
            this.mCancelListener = null;
            this.mOkListener = null;
            return unInitLayoutForEditMode();
        }

        protected void initViews() {
            this.mTitle = (TextView) this.mCustomedView.findViewById(R.id.title);
            this.mSubTitle = (TextView) this.mCustomedView.findViewById(R.id.sub_title);
            this.mTitleNumber = (TextView) this.mCustomedView.findViewById(R.id.title_number);
            this.mCancelView = (ImageView) this.mCustomedView.findViewById(R.id.bt_cancel);
            this.mOkView = (ImageView) this.mCustomedView.findViewById(R.id.bt_ok);
            this.mSplitActionBar = (SplitActionBarView) this.mCustomedView.findViewById(R.id.actionbar_top_menu);
            this.mTitleHolder = (LinearLayout) this.mCustomedView.findViewById(R.id.title_holder);
            this.mTitleContainer = (LinearLayout) this.mCustomedView.findViewById(R.id.title_container);
            this.mSplitActionBar.setOnTop(true);
            if (this.mCancelListener != null) {
                this.mCancelView.setOnClickListener(this.mCancelListener);
                this.mCancelView.setVisibility(0);
            } else {
                this.mCancelView.setVisibility(8);
            }
            if (this.mOkListener != null) {
                this.mOkView.setOnClickListener(this.mOkListener);
                this.mOkView.setVisibility(0);
            } else {
                this.mOkView.setVisibility(8);
            }
            immersionStyleForActionBar(AbstractEmuiActionBar.this.mContext);
        }

        private void immersionStyleForActionBar(Context context) {
            if (ResEx.self().isUseThemeBackground(context)) {
                MLog.d("EmuiActionBar", "immersionStyleForActionBar() using the theme");
            } else if (this.mTitle != null && this.mTitleNumber != null && this.mCancelView != null && this.mOkView != null) {
                Resources rs = context.getResources();
                int textDarkColor = rs.getColor(R.color.title_color_primary_dark);
                int textSubTitleColor = rs.getColor(R.color.subtitle_color_primary_dark);
                this.mTitle.setTextColor(textDarkColor);
                this.mCancelView.setImageResource(R.drawable.mms_ic_cancel_dark);
                this.mSubTitle.setTextColor(textSubTitleColor);
                this.mSubTitle.setTypeface(Typeface.create("chnfzxh", 0));
                this.mTitleNumber.setTextColor(textDarkColor);
            }
        }

        public void setActionBarHeight(int heigtPx) {
            if (this.mCustomedView.getLayoutParams() instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) this.mCustomedView.getLayoutParams();
                lp.height = heigtPx;
                this.mCustomedView.setLayoutParams(lp);
            }
        }

        public void setStartIcon(boolean visible, int resId) {
            this.mCancelView.setImageResource(resId);
            this.mCancelView.setVisibility(visible ? 0 : 8);
        }

        public void setStartIcon(boolean visible, int resId, OnClickListener l) {
            if (this.mCancelView != null) {
                if (visible) {
                    if (l != null) {
                        this.mCancelView.setOnClickListener(l);
                    }
                    this.mCancelView.setVisibility(0);
                    this.mCancelView.setImageResource(resId);
                } else {
                    this.mCancelView.setVisibility(8);
                }
            }
        }

        public void setEndIcon(boolean enabled) {
            if (this.mOkView == null) {
                return;
            }
            if (enabled) {
                this.mOkView.setEnabled(true);
                this.mOkView.setImageResource(R.drawable.ic_public_ok);
                return;
            }
            this.mOkView.setEnabled(false);
            this.mOkView.setImageResource(R.drawable.ic_public_ok_unenable);
        }

        public void setEndIconDisable(boolean disabled, int resId) {
            if (this.mOkView == null) {
                return;
            }
            if (disabled) {
                this.mOkView.setEnabled(false);
                this.mOkView.setImageResource(resId);
                return;
            }
            this.mOkView.setEnabled(true);
            this.mOkView.setImageResource(resId);
        }

        public void setEndIcon(boolean visible, int resId, OnClickListener l) {
            if (visible) {
                if (l != null) {
                    this.mOkView.setOnClickListener(l);
                }
                this.mOkView.setVisibility(0);
                this.mOkView.setImageResource(resId);
                return;
            }
            this.mOkView.setVisibility(8);
        }

        public void setEndIcon(boolean visible, Drawable icon, OnClickListener l) {
            if (visible) {
                if (l != null) {
                    this.mOkView.setOnClickListener(l);
                }
                this.mOkView.setVisibility(0);
                this.mOkView.setImageDrawable(icon);
                return;
            }
            this.mOkView.setVisibility(8);
        }

        public void setEndIconDescription(String description) {
            if (this.mOkView != null) {
                this.mOkView.setContentDescription(description);
            }
        }

        public void setStartIconDescription(String description) {
            if (this.mCancelView != null) {
                this.mCancelView.setContentDescription(description);
            }
        }

        public boolean setTitle(CharSequence title, int size) {
            if (size == 0) {
                return setTitle(title);
            }
            if (this.mTitleNumber == null || this.mTitle == null) {
                return false;
            }
            if (this.mTitleNumber.getVisibility() != 0) {
                this.mTitleNumber.setVisibility(0);
            }
            this.mTitle.setText(title);
            String strSize = NumberFormat.getIntegerInstance().format((long) size);
            if (AbstractEmuiActionBar.this.mActionMode == 2 || AbstractEmuiActionBar.this.mActionMode == 4) {
                this.mTitleNumber.setText(strSize);
                this.mTitleNumber.setBackgroundResource(R.drawable.select_count_bg);
                this.mTitleNumber.setTextSize(0, (float) AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.number_text_size_enter_editmode));
                this.mTitleNumber.setTextColor(AbstractEmuiActionBar.this.mContext.getResources().getColor(R.color.message_top_text_number_color));
            } else {
                this.mTitleNumber.setText("(" + strSize + ")");
                this.mTitleNumber.setBackground(null);
                this.mTitleNumber.setTextSize(0, (float) AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.number_text_size_exit_editmode));
                this.mTitleNumber.setTextColor(AbstractEmuiActionBar.this.mContext.getResources().getColor(R.color.title_color_primary_dark));
            }
            return true;
        }

        public boolean setListTitle(CharSequence title, int size) {
            if (this.mTitleNumber == null || this.mTitle == null) {
                return false;
            }
            if (size == 0) {
                return setTitle(title);
            }
            this.mTitle.setText(title);
            setListNumberStyle(size);
            return true;
        }

        private void setListNumberStyle(int size) {
            if (this.mTitleNumber.getVisibility() != 0) {
                this.mTitleNumber.setVisibility(0);
            }
            this.mTitleNumber.setText(NumberFormat.getIntegerInstance().format((long) size));
            this.mTitleNumber.setBackgroundResource(R.drawable.list_select_count_bg);
            this.mTitleNumber.setTextSize(0, (float) AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.number_text_size_enter_editmode));
            this.mTitleNumber.setTextColor(AbstractEmuiActionBar.this.mContext.getResources().getColor(R.color.message_top_text_number_color));
        }

        public void changeListTitleNumber(int size) {
            if (this.mTitleNumber != null) {
                if (size == 0) {
                    this.mTitleNumber.setVisibility(8);
                } else {
                    setListNumberStyle(size);
                }
            }
        }

        public boolean setSubtitle(CharSequence subTitle) {
            boolean isLandscape = false;
            if (this.mSubTitle == null) {
                return false;
            }
            if (subTitle == null || subTitle.equals("")) {
                this.mSubTitle.setVisibility(8);
                this.mTitle.setTextSize(0, (float) AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_title_size_in_singleline));
            } else {
                this.mSubTitle.setVisibility(0);
                if (AbstractEmuiActionBar.this.mContext.getResources().getConfiguration().orientation == 2) {
                    isLandscape = true;
                }
                setTitleTextSizeWhenTwoLine(isLandscape);
                setSubTitleTextSize();
            }
            this.mSubTitle.setText(subTitle);
            return true;
        }

        public void setSubTitleTextSize() {
            if (this.mSubTitle != null) {
                int dimensionPixelSize;
                if (AbstractEmuiActionBar.this.mContext.getResources().getConfiguration().fontScale >= ContentUtil.FONT_SIZE_NORMAL) {
                    dimensionPixelSize = AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_subtitle_size);
                } else {
                    dimensionPixelSize = AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_subtitle_size_in_scale);
                }
                this.mSubTitle.setTextSize(0, (float) dimensionPixelSize);
            }
        }

        public void setTitleTextSizeWhenTwoLine(boolean isInLandscape) {
            if (AbstractEmuiActionBar.this.mContext.getResources().getConfiguration().fontScale >= ContentUtil.FONT_SIZE_NORMAL) {
                int dimensionPixelSize;
                if (isInLandscape) {
                    dimensionPixelSize = AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_title_size_in_landscape);
                } else {
                    dimensionPixelSize = AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_title_size_in_portrait);
                }
                this.mTitle.setTextSize(0, (float) dimensionPixelSize);
                return;
            }
            this.mTitle.setTextSize(0, (float) AbstractEmuiActionBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.actionbar_title_size_in_singleline));
        }

        public void doOnConfigurationChange(Configuration newConfig) {
            if (this.mSubTitle != null && this.mTitle != null) {
                CharSequence subText = this.mSubTitle.getText();
                if (!(subText == null || subText.equals(""))) {
                    setTitleTextSizeWhenTwoLine(newConfig.orientation == 2);
                    setSubTitleTextSize();
                }
            }
        }

        public void hideMenu() {
            this.mSplitActionBar.setVisibility(8);
        }

        public void showMenu() {
            this.mSplitActionBar.setVisibility(0);
        }

        public void show(boolean show) {
            this.mCustomedView.setVisibility(show ? 0 : 8);
        }

        public void showEndIcon(boolean show) {
            this.mOkView.setVisibility(show ? 0 : 8);
        }

        public void showStartIcon(boolean show) {
            this.mCancelView.setVisibility(show ? 0 : 8);
        }

        public boolean setTitle(CharSequence title) {
            if (this.mTitleNumber == null || this.mTitle == null) {
                return false;
            }
            this.mTitleNumber.setVisibility(8);
            this.mTitle.setText(title);
            return true;
        }

        public Menu getMenu() {
            return this.mSplitActionBar.getMenu();
        }

        public void refreshMenu() {
            this.mSplitActionBar.refreshMenu();
        }

        public void setTitleGravityCenter(boolean isCenter) {
            int dipToPx;
            int i = 0;
            LayoutParams clp = (LayoutParams) this.mTitleContainer.getLayoutParams();
            if (isCenter) {
                dipToPx = MessageUtils.dipToPx(AbstractEmuiActionBar.this.mContext, 40.0f);
            } else {
                dipToPx = 0;
            }
            clp.setMargins(0, 0, dipToPx, 0);
            this.mTitleContainer.setLayoutParams(clp);
            LayoutParams lp = (LayoutParams) this.mTitleHolder.getLayoutParams();
            lp.gravity = isCenter ? 17 : 8388611;
            this.mTitleHolder.setLayoutParams(lp);
            AbstractEmuiActionBar abstractEmuiActionBar = AbstractEmuiActionBar.this;
            if (isCenter) {
                i = 2;
            }
            abstractEmuiActionBar.mActionMode = i;
        }

        public void setTitleGravityCenter() {
            if (AbstractEmuiActionBar.this.mActionMode == 5 || AbstractEmuiActionBar.this.mActionMode == 4) {
                if (AbstractEmuiActionBar.this.mActionMode == 5) {
                    LayoutParams clp = (LayoutParams) this.mTitleContainer.getLayoutParams();
                    if (MessageUtils.isNeedLayoutRtl()) {
                        clp.setMargins(MessageUtils.dipToPx(AbstractEmuiActionBar.this.mContext, 25.0f), 0, 0, 0);
                    } else {
                        clp.setMargins(0, 0, MessageUtils.dipToPx(AbstractEmuiActionBar.this.mContext, 40.0f), 0);
                    }
                    this.mTitleContainer.setLayoutParams(clp);
                }
                LayoutParams lp = (LayoutParams) this.mTitleHolder.getLayoutParams();
                lp.gravity = 17;
                this.mTitleHolder.setLayoutParams(lp);
            }
        }

        public void setSubTitleGravityCenter() {
            LayoutParams lp = (LayoutParams) this.mSubTitle.getLayoutParams();
            lp.gravity = 17;
            this.mSubTitle.setLayoutParams(lp);
        }
    }

    protected abstract class BaseCustomEmuiSearchView {
        protected View mCustomedView;

        public abstract Menu getActionMenu();

        public abstract boolean onEnterSearchMode();

        protected BaseCustomEmuiSearchView() {
        }
    }

    public AbstractEmuiActionBar(Activity activity) {
        this.mContext = activity;
        this.mActionBarInner = activity.getActionBar();
    }

    public int getActionMode() {
        return this.mActionMode;
    }

    public boolean isInTitleModel() {
        return this.mActionMode == 5;
    }

    public boolean isInSelectModel() {
        return this.mActionMode == 4;
    }

    public void setTitle(CharSequence title) {
        if (!this.mCustomView.setTitle(title)) {
            this.mActionBarInner.setTitle(title);
        }
    }

    public void setDestronyWhenExitActionMode(boolean destry) {
        this.mDestroyWhenExitActionMode = destry;
    }

    public void setUseSelecteSize(int size) {
        setTitle(this.mContext.getResources().getString(size == 0 ? R.string.no_selected : R.string.has_selected), size);
    }

    public void setTitle(CharSequence title, int size) {
        if (!this.mCustomView.setTitle(title, size)) {
            if (size > 0) {
                this.mActionBarInner.setTitle(" (" + size + ")");
            } else {
                this.mActionBarInner.setTitle(title);
            }
        }
    }

    public void setListTitle(CharSequence title, int size) {
        if (!this.mCustomView.setListTitle(title, size)) {
            if (size > 0) {
                this.mActionBarInner.setTitle(" (" + size + ")");
            } else {
                this.mActionBarInner.setTitle(title);
            }
        }
    }

    public void changeListTitleNumber(int size) {
        this.mCustomView.changeListTitleNumber(size);
    }

    public void setActionBarHeight(int heigtPx) {
        this.mCustomView.setActionBarHeight(heigtPx);
    }

    public void setSubtitle(CharSequence title) {
        if (!this.mCustomView.setSubtitle(title)) {
            this.mActionBarInner.setSubtitle(title);
        }
    }

    public void doOnConfigurationChange(Configuration newConfig) {
        this.mCustomView.doOnConfigurationChange(newConfig);
    }

    public Menu getMenu() {
        return this.mCustomView.getMenu();
    }

    public void refreshMenu() {
        this.mCustomView.refreshMenu();
    }

    public void showMenu(boolean show) {
        if (show) {
            this.mCustomView.showMenu();
        } else {
            this.mCustomView.hideMenu();
        }
    }

    public void showStartIcon(boolean show) {
        this.mCustomView.showStartIcon(show);
    }

    public void showEndIcon(boolean show) {
        this.mCustomView.showEndIcon(show);
    }

    public SplitActionBarView getSplitActionBarView() {
        return this.mCustomView.mSplitActionBar;
    }

    public void setTitleGravityCenter(boolean isCenter) {
        this.mCustomView.setTitleGravityCenter(isCenter);
    }

    public void setTitleGravityCenter() {
        this.mCustomView.setTitleGravityCenter();
    }

    public void setSubTitleGravityCenter() {
        this.mCustomView.setSubTitleGravityCenter();
    }

    public void enterEditMode(OnClickListener negtive) {
        if (!this.mCustomView.onEnterEditMode(negtive)) {
            setStartIcon(true, null, negtive);
            setUseSelecteSize(0);
        }
        this.mActionMode = 2;
    }

    public void enterEditMode(OnClickListener negtive, OnClickListener positive) {
        if (!this.mCustomView.onEnterEditMode(negtive, positive)) {
            setStartIcon(true, null, negtive);
            setEndIcon(true, null, positive);
        }
        this.mActionMode = 2;
    }

    public void exitEditMode() {
        this.mCustomView.onExitEditMode();
        this.mActionMode = 0;
    }

    public void enterSelectModeState() {
        this.mActionMode = 4;
    }

    public void exitSelectMode() {
        this.mCustomView.setEndIcon(true, null, null);
        if (!this.mCustomView.onExitSelectMode()) {
            setStartIcon(false, null, null);
            setEndIcon(false, null, null);
        }
        this.mActionMode = 0;
    }

    public void enterTitleMode() {
        this.mActionMode = 5;
    }

    public void exitTitleMode() {
        if (!this.mCustomView.onExitTitleMode()) {
            setStartIcon(false, null, null);
            setEndIcon(false, null, null);
        }
        this.mActionMode = 0;
    }

    public void setStartIcon(boolean visible, int resId) {
        this.mCustomView.setStartIcon(visible, resId);
    }

    public void setEndIcon(boolean enabled) {
        this.mCustomView.setEndIcon(enabled);
    }

    public void setStartIcon(boolean visible, Drawable icon, OnClickListener l) {
    }

    public void setStartIcon(boolean visible, int resId, OnClickListener l) {
        this.mCustomView.setStartIcon(visible, resId, l);
    }

    public void setEndIcon(boolean visible, int resId, OnClickListener l) {
        this.mCustomView.setEndIcon(visible, resId, l);
    }

    public void setEndIcon(boolean visible, Drawable icon, OnClickListener l) {
        this.mCustomView.setEndIcon(visible, icon, l);
    }

    public void setEndIconDisable(boolean disable, int resId) {
        if (this.mCustomView != null) {
            this.mCustomView.setEndIconDisable(disable, resId);
        }
    }

    public void setEndIconDescription(String description) {
        this.mCustomView.setEndIconDescription(description);
    }

    public void setStartIconDescription(String description) {
        this.mCustomView.setStartIconDescription(description);
    }

    public void show(boolean show) {
        this.mCustomView.show(show);
    }

    public Menu getActionMenu() {
        switch (this.mActionMode) {
            case 2:
                return this.mCustomView.getActionMenu();
            case 3:
                return this.mSearchBar.getActionMenu();
            default:
                return null;
        }
    }

    public void enterSearchMode() {
        StatisticalHelper.incrementReportCount(this.mContext.getApplicationContext(), 2149);
        this.mSearchBar.onEnterSearchMode();
        this.mActionMode = 3;
        if (HwMessageUtils.isSplitOn()) {
            show(false);
        }
    }

    public View getSearchView() {
        if (this.mSearchBar != null) {
            return this.mSearchBar.mCustomedView;
        }
        return null;
    }
}
