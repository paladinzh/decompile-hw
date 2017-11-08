package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;

public class SplitActionBarView extends LinearLayout implements OnLongClickListener, OnClickListener {
    private Handler mHandler = new Handler();
    private boolean mIsOnTop;
    private boolean mIsPopShow = false;
    private MmsMenuImpl mMenu;
    protected OnCustomMenuListener mMenuClickListener;
    protected PopupMenu mPopupMenu;
    private int mSplitWidth;
    private ViewGroup mToolbarSahpeHolder;

    public interface OnCustomMenuListener {
        boolean onCustomMenuItemClick(MenuItem menuItem);
    }

    public SplitActionBarView(Context context) {
        super(context);
    }

    public SplitActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SplitActionBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMenu = new MmsMenuImpl(getContext());
    }

    public void setOnCustomMenuListener(OnCustomMenuListener aMenuClickListener) {
        this.mMenuClickListener = aMenuClickListener;
    }

    public void showPopup(View aView) {
        if (this.mPopupMenu == null) {
            PopupMenu popup = new PopupMenu(getContext(), aView);
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem aMenuItem) {
                    SplitActionBarView.this.mPopupMenu = null;
                    if (SplitActionBarView.this.mMenuClickListener != null) {
                        return SplitActionBarView.this.mMenuClickListener.onCustomMenuItemClick(aMenuItem);
                    }
                    return false;
                }
            });
            popup.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(PopupMenu menu) {
                    SplitActionBarView.this.mPopupMenu = null;
                    SplitActionBarView.this.mIsPopShow = false;
                }
            });
            Menu popupMenu = popup.getMenu();
            for (int i = 0; i < this.mMenu.size(); i++) {
                MmsMenuItemImpl item = (MmsMenuItemImpl) this.mMenu.getItem(i);
                if (item.isVisible() && !item.requiresActionButton()) {
                    popupMenu.add(0, item.getItemId(), 0, item.getTitle()).setEnabled(item.isEnabled());
                }
            }
            StatisticalHelper.incrementReportCount(MmsApp.getApplication(), 2116);
            this.mPopupMenu = popup;
            popup.show();
            this.mIsPopShow = true;
        }
    }

    public void dismissPopup() {
        if (this.mPopupMenu != null && this.mIsPopShow) {
            this.mPopupMenu.dismiss();
            this.mIsPopShow = false;
        }
    }

    public void showPopup() {
        View v = findViewWithTag("mms_more_menu");
        if (v != null) {
            showPopup(v);
            this.mIsPopShow = true;
        }
    }

    public boolean isPopShow() {
        return this.mIsPopShow;
    }

    public void refreshMenu() {
        initToolbarSahpeHolder();
        if (this.mIsOnTop) {
            removeAllViews();
        } else {
            this.mToolbarSahpeHolder.removeAllViews();
        }
        boolean hasMoreMenu = false;
        for (int i = 0; i < this.mMenu.size(); i++) {
            MmsMenuItemImpl item = (MmsMenuItemImpl) this.mMenu.getItem(i);
            if (item.isVisible()) {
                if (item.requiresActionButton()) {
                    View itemView = item.getItemView(this.mIsOnTop);
                    itemView.setId(item.getItemId());
                    if (this.mIsOnTop) {
                        addView(itemView);
                    } else {
                        this.mToolbarSahpeHolder.addView(itemView);
                    }
                    if (item.getItemId() == 11114) {
                        itemView.setContentDescription(getContext().getString(R.string.menu_call));
                    }
                    itemView.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
                } else {
                    hasMoreMenu = true;
                }
            }
        }
        if (hasMoreMenu) {
            View moreMenuItemView = getOverFlowItem();
            moreMenuItemView.setTag("mms_more_menu");
            moreMenuItemView.setContentDescription(getContext().getString(R.string.hint_more_options));
            if (this.mIsOnTop) {
                addView(moreMenuItemView);
            } else {
                this.mToolbarSahpeHolder.addView(moreMenuItemView);
            }
            moreMenuItemView.setOnClickListener(this);
            moreMenuItemView.setOnLongClickListener(this);
        }
        refreshLayout();
    }

    private View getOverFlowItem() {
        if (this.mIsOnTop) {
            EmuiMenuImage image = (EmuiMenuImage) View.inflate(getContext(), R.layout.mms_top_overflow_menu_item, null);
            image.setImageDrawable(ResEx.self().getCachedDrawable(R.drawable.ic_public_more));
            image.setPadding(MessageUtils.dipToPx(getContext(), 6.0f), 0, MessageUtils.dipToPx(getContext(), 6.0f), 0);
            return image;
        }
        EmuiMenuText text = (EmuiMenuText) View.inflate(getContext(), R.layout.mms_menu_item, null);
        text.setText(R.string.menu_add_rcs_more);
        text.setIcon((int) R.drawable.ic_public_more);
        return text;
    }

    protected void refreshLayout() {
        int buttomMenuCount = this.mIsOnTop ? getChildCount() : this.mToolbarSahpeHolder.getChildCount();
        if (buttomMenuCount > 0) {
            int i;
            if (this.mIsOnTop) {
                i = 0;
                while (i < buttomMenuCount) {
                    MessageUtils.setMargin(getContext(), this.mIsOnTop ? getChildAt(i) : this.mToolbarSahpeHolder.getChildAt(i), 16, -1, -1, -1);
                    i++;
                }
                return;
            }
            LayoutParams layoutParams;
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels;
            if (screenWidth != dm.widthPixels && (getContext() instanceof Activity) && ((Activity) getContext()).isInMultiWindowMode()) {
                screenWidth = dm.widthPixels;
            }
            if (!HwMessageUtils.isSplitOn()) {
                this.mSplitWidth = screenWidth;
            }
            int contentWidth = ((screenWidth - MessageUtils.dipToPx(getContext(), 40.0f)) / 4) - MessageUtils.dipToPx(getContext(), 8.0f);
            int margin = (int) getContext().getResources().getDimension(R.dimen.mms_multi_btn_padding);
            if (((MessageUtils.dipToPx(getContext(), 8.0f) + contentWidth) * buttomMenuCount) + MessageUtils.dipToPx(getContext(), 40.0f) > this.mSplitWidth) {
                layoutParams = new LayoutParams(((this.mSplitWidth - MessageUtils.dipToPx(getContext(), 40.0f)) / buttomMenuCount) - MessageUtils.dipToPx(getContext(), 8.0f), -2);
            } else {
                layoutParams = new LayoutParams(contentWidth, -2);
            }
            layoutParams.setMarginStart(margin);
            layoutParams.setMarginEnd(margin);
            for (i = 0; i < buttomMenuCount; i++) {
                View view;
                if (this.mIsOnTop) {
                    view = getChildAt(i);
                } else {
                    view = this.mToolbarSahpeHolder.getChildAt(i);
                }
                ((EmuiMenuText) view).setLayoutParams(layoutParams);
                ((EmuiMenuText) view).setGravity(17);
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mSplitWidth != MeasureSpec.getSize(widthMeasureSpec)) {
            this.mSplitWidth = MeasureSpec.getSize(widthMeasureSpec);
            refreshLayout();
        }
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public void onClick(View v) {
        if ("mms_more_menu".equals(v.getTag())) {
            showPopup(v);
        } else if (this.mMenuClickListener != null && this.mMenu.findItem(v.getId()) != null) {
            this.mMenuClickListener.onCustomMenuItemClick(this.mMenu.findItem(v.getId()));
        }
    }

    public boolean onLongClick(View v) {
        if (v instanceof EmuiMenuText) {
            CharSequence title = ((EmuiMenuText) v).getText();
            if (!TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), title, 0).show();
                return true;
            }
        }
        return false;
    }

    public void setOnTop(boolean isOnTop) {
        this.mIsOnTop = isOnTop;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.mIsOnTop) {
            refreshMenu();
        }
        if (this.mPopupMenu != null && this.mIsPopShow) {
            dismissPopup();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SplitActionBarView.this.showPopup();
                }
            }, 200);
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != 0 && this.mPopupMenu != null && this.mIsPopShow) {
            dismissPopup();
        }
    }

    private void initToolbarSahpeHolder() {
        if (this.mToolbarSahpeHolder == null || this.mIsOnTop) {
            this.mToolbarSahpeHolder = (ViewGroup) inflate(getContext(), R.layout.toolbar_shape_holder, this).findViewById(R.id.shape_holder);
        }
    }
}
