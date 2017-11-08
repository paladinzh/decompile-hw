package com.android.contacts.hap.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.CustomStateListDrawable;

public class SplitActionBarView extends LinearLayout {
    private static int mMenuMaxWidth;
    private static int mMixMenuWidth;
    private SparseArray<SetButtonDetails> mCollMenu = new SparseArray();
    private SparseArray<MenuItem> mCollapsedItems = new SparseArray();
    private int mContainerWidth = 0;
    protected Context mContext;
    protected boolean mIsAccessabilityEnabled;
    private boolean mIsLastMenuPopup = true;
    private TextView mLeft;
    protected SetButtonDetails mLeftDetail;
    protected int mMaxIconSize;
    protected OnCustomMenuListener mMenuClickListener;
    private Rect mMenuMoreIconBounds;
    private TextView mMiddle;
    protected SetButtonDetails mMiddleDetail;
    protected SetButtonDetails mMoreMenuDetail;
    protected PopupMenu mPopupMenu;
    private TextView mRight;
    protected SetButtonDetails mRightDetail;
    private TextView mRightOrMenu;

    public interface OnCustomMenuListener {
        boolean onCustomMenuItemClick(MenuItem menuItem);

        boolean onCustomSplitMenuItemClick(int i);

        void onPrepareOptionsMenu(Menu menu);
    }

    public static class SetButtonDetails {
        public int mDrawableResourceID;
        public int mStringResourceID;

        public SetButtonDetails(int drawableResourceID, int stringResourceID) {
            this.mDrawableResourceID = drawableResourceID;
            this.mStringResourceID = stringResourceID;
        }
    }

    public void setOnCustomMenuListener(OnCustomMenuListener aMenuClickListener) {
        this.mMenuClickListener = aMenuClickListener;
    }

    public SplitActionBarView(Context context) {
        super(context);
        init(context);
    }

    public SplitActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SplitActionBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mIsAccessabilityEnabled = CommonUtilMethods.isTalkBackEnabled(this.mContext);
        this.mMaxIconSize = (int) ((32.0f * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
        View view = inflate(this.mContext, R.layout.split_action_bar, this);
        this.mLeft = (TextView) view.findViewById(R.id.btn_left);
        this.mMiddle = (TextView) view.findViewById(R.id.btn_middle);
        this.mRight = (TextView) view.findViewById(R.id.btn_right);
        this.mRightOrMenu = (TextView) view.findViewById(R.id.btn_menu);
        if (this.mRightOrMenu.getCompoundDrawables().length == 4) {
            this.mMenuMoreIconBounds = this.mRightOrMenu.getCompoundDrawables()[1].getBounds();
        }
        int width = ContactDpiAdapter.getScreenSize(this.mContext, true);
        int height = ContactDpiAdapter.getScreenSize(this.mContext, false);
        if (width >= height) {
            width = height;
        }
        mMenuMaxWidth = (width - this.mContext.getResources().getDimensionPixelSize(R.dimen.split_action_bar_menu_left_and_right)) / 4;
        this.mLeft.setMaxWidth(mMenuMaxWidth);
        this.mMiddle.setMaxWidth(mMenuMaxWidth);
        this.mRight.setMaxWidth(mMenuMaxWidth);
        this.mRightOrMenu.setMaxWidth(mMenuMaxWidth);
        mMixMenuWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.split_action_bar_menu_min_width);
    }

    @SuppressLint({"DrawAllocation"})
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(measureMenuContainer(widthMeasureSpec), heightMeasureSpec);
    }

    private int measureMenuContainer(int widthMeasureSpec) {
        if (this.mContainerWidth == MeasureSpec.getSize(widthMeasureSpec)) {
            return widthMeasureSpec;
        }
        int countMenuToShow = 0;
        if (this.mLeft.getVisibility() != 8) {
            countMenuToShow = 1;
            this.mCollMenu.remove(1);
        }
        if (this.mMiddle.getVisibility() != 8) {
            countMenuToShow++;
            this.mCollMenu.remove(2);
        }
        if (this.mRight.getVisibility() != 8) {
            countMenuToShow++;
            this.mCollMenu.remove(3);
        }
        if (this.mRightOrMenu.getVisibility() != 8) {
            countMenuToShow++;
            this.mCollMenu.remove(4);
        }
        if (countMenuToShow == 0) {
            return widthMeasureSpec;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int wd = this.mContext.getResources().getDimensionPixelSize(R.dimen.split_action_bar_menu_left_and_right);
        int maxMenuCount = (width - wd) / mMixMenuWidth;
        if (mMenuMaxWidth * countMenuToShow < width - wd) {
            width = (mMenuMaxWidth * countMenuToShow) + wd;
            this.mContainerWidth = width;
            if (this.mCollMenu.size() > 0 && countMenuToShow < maxMenuCount) {
                expanse(maxMenuCount - countMenuToShow);
            }
        } else {
            this.mContainerWidth = 0;
            if (isVisible(4) && this.mIsLastMenuPopup) {
                if (countMenuToShow > maxMenuCount) {
                    collapse(countMenuToShow - maxMenuCount);
                } else if (this.mCollMenu.size() > 0 && countMenuToShow < maxMenuCount) {
                    expanse(maxMenuCount - countMenuToShow);
                }
            }
        }
        return MeasureSpec.makeMeasureSpec(width, 1073741824);
    }

    private void collapse(int menuToCollapse) {
        if (this.mRight.getVisibility() != 8) {
            this.mCollMenu.put(3, this.mRightDetail);
            this.mRight.setVisibility(8);
            menuToCollapse--;
        }
        if (menuToCollapse > 0 && this.mMiddle.getVisibility() != 8) {
            this.mCollMenu.put(2, this.mMiddleDetail);
            this.mMiddle.setVisibility(8);
            menuToCollapse--;
        }
        if (menuToCollapse > 0 && this.mLeft.getVisibility() != 8) {
            this.mCollMenu.put(1, this.mLeftDetail);
            this.mLeft.setVisibility(8);
        }
    }

    private void expanse(int menuToExpanse) {
        int countToExpanse = Math.min(this.mCollMenu.size(), menuToExpanse);
        if (countToExpanse != 0) {
            if (this.mCollMenu.get(1) != null) {
                this.mLeft.setVisibility(0);
                this.mCollMenu.remove(1);
                countToExpanse--;
            }
            if (countToExpanse > 0 && this.mCollMenu.get(2) != null) {
                this.mMiddle.setVisibility(0);
                this.mCollMenu.remove(2);
                countToExpanse--;
            }
            if (countToExpanse > 0 && this.mCollMenu.get(3) != null) {
                this.mRight.setVisibility(0);
                this.mCollMenu.remove(3);
            }
        }
    }

    public void fillDetails(SetButtonDetails left, SetButtonDetails middle, SetButtonDetails right, SetButtonDetails moreMenu, boolean isMoreMenuPopup) {
        this.mCollMenu.clear();
        this.mIsLastMenuPopup = isMoreMenuPopup;
        setMenuItem(left, middle, right, moreMenu);
        buildMenu(this.mLeft, left, 1);
        buildMenu(this.mMiddle, middle, 2);
        buildMenu(this.mRight, right, 3);
        buildMenuMore(this.mRightOrMenu, moreMenu, 4);
    }

    public void refreshDetail(int aButton, SetButtonDetails newDetail) {
        switch (aButton) {
            case 1:
                buildMenu(this.mLeft, newDetail, aButton);
                this.mLeftDetail = newDetail;
                return;
            case 2:
                buildMenu(this.mMiddle, newDetail, aButton);
                this.mMiddleDetail = newDetail;
                return;
            case 3:
                buildMenu(this.mRight, newDetail, aButton);
                this.mRightDetail = newDetail;
                return;
            case 4:
                buildMenuMore(this.mRightOrMenu, newDetail, aButton);
                this.mMoreMenuDetail = newDetail;
                return;
            default:
                return;
        }
    }

    private void buildMenuMore(TextView menu, SetButtonDetails detail, int index) {
        if (menu != null) {
            if (this.mIsLastMenuPopup) {
                menu.setText(this.mContext.getResources().getString(R.string.contacts_title_menu));
                Drawable drawableSrc = this.mContext.getResources().getDrawable(R.drawable.ic_menu);
                if (drawableSrc instanceof BitmapDrawable) {
                    Drawable drawable = CustomStateListDrawable.createStateDrawable(this.mContext, (BitmapDrawable) drawableSrc, false);
                    if (this.mMenuMoreIconBounds != null) {
                        drawable.setBounds(this.mMenuMoreIconBounds);
                    }
                    menu.setCompoundDrawables(null, drawable, null, null);
                } else {
                    if (this.mMenuMoreIconBounds != null) {
                        drawableSrc.setBounds(this.mMenuMoreIconBounds);
                    }
                    menu.setCompoundDrawables(null, drawableSrc, null, null);
                }
                if (detail != null) {
                    menu.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            Toast.makeText(SplitActionBarView.this.mContext, SplitActionBarView.this.mMoreMenuDetail.mStringResourceID, 0).show();
                            return true;
                        }
                    });
                }
                if (detail == null || detail.mDrawableResourceID <= 0) {
                    menu.setVisibility(8);
                } else {
                    menu.setOnClickListener(new OnClickListener() {
                        public void onClick(View aView) {
                            SplitActionBarView.this.showPopup(aView);
                            StatisticalHelper.report(1150);
                        }
                    });
                }
            } else {
                buildMenu(menu, detail, index);
            }
        }
    }

    private void buildMenu(TextView menu, final SetButtonDetails detail, int index) {
        if (menu != null) {
            if (detail != null) {
                menu.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        Toast.makeText(SplitActionBarView.this.mContext, detail.mStringResourceID, 0).show();
                        return true;
                    }
                });
            }
            if (detail == null || detail.mDrawableResourceID <= 0) {
                menu.setVisibility(8);
                this.mCollMenu.remove(index);
            } else {
                setIcon(menu, this.mContext.getResources().getDrawable(detail.mDrawableResourceID));
                menu.setText(detail.mStringResourceID);
                if (this.mIsAccessabilityEnabled) {
                    menu.setContentDescription(this.mContext.getString(detail.mStringResourceID));
                }
                menu.setOnClickListener(new OnClickListener() {
                    public void onClick(View aView) {
                        if (SplitActionBarView.this.mMenuClickListener != null) {
                            SplitActionBarView.this.mMenuClickListener.onCustomSplitMenuItemClick(detail.mStringResourceID);
                        }
                    }
                });
            }
        }
    }

    private void setIcon(TextView menu, Drawable icon) {
        Drawable finalIcon = icon;
        if (icon instanceof BitmapDrawable) {
            finalIcon = CustomStateListDrawable.createStateDrawable(this.mContext, (BitmapDrawable) icon, false);
        }
        if (finalIcon != null) {
            float scale;
            int width = finalIcon.getIntrinsicWidth();
            int height = finalIcon.getIntrinsicHeight();
            if (width > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) width);
                width = this.mMaxIconSize;
                height = (int) (((float) height) * scale);
            }
            if (height > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) height);
                height = this.mMaxIconSize;
                width = (int) (((float) width) * scale);
            }
            finalIcon.setBounds(0, 0, width, height);
        }
        menu.setCompoundDrawables(null, finalIcon, null, null);
    }

    public void refreshIcon(int aButton, Drawable icon) {
        if (icon != null) {
            switch (aButton) {
                case 1:
                    setIcon(this.mLeft, icon);
                    break;
                case 2:
                    setIcon(this.mMiddle, icon);
                    break;
                case 3:
                    setIcon(this.mRight, icon);
                    break;
                case 4:
                    setIcon(this.mRightOrMenu, icon);
                    break;
            }
        }
    }

    public void setVisibility(int aButton, boolean aVisibility) {
        int i = 0;
        TextView textView;
        switch (aButton) {
            case 1:
                textView = this.mLeft;
                if (!aVisibility) {
                    i = 8;
                }
                textView.setVisibility(i);
                break;
            case 2:
                textView = this.mMiddle;
                if (!aVisibility) {
                    i = 8;
                }
                textView.setVisibility(i);
                break;
            case 3:
                textView = this.mRight;
                if (!aVisibility) {
                    i = 8;
                }
                textView.setVisibility(i);
                break;
            case 4:
                if (this.mMoreMenuDetail != null && this.mMoreMenuDetail.mDrawableResourceID > 0) {
                    textView = this.mRightOrMenu;
                    if (!aVisibility) {
                        i = 8;
                    }
                    textView.setVisibility(i);
                    break;
                }
        }
        if (!aVisibility) {
            this.mCollMenu.remove(aButton);
        }
    }

    public boolean isVisible(int aButton) {
        boolean z = true;
        if (this.mCollMenu.get(aButton) != null) {
            return true;
        }
        switch (aButton) {
            case 1:
                if (this.mLeft.getVisibility() != 0) {
                    z = false;
                }
                return z;
            case 2:
                if (this.mMiddle.getVisibility() != 0) {
                    z = false;
                }
                return z;
            case 3:
                if (this.mRight.getVisibility() != 0) {
                    z = false;
                }
                return z;
            case 4:
                if (this.mRightOrMenu.getVisibility() != 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void setEnable(int aButton, boolean aEnabled) {
        switch (aButton) {
            case 1:
                this.mLeft.setEnabled(aEnabled);
                return;
            case 2:
                this.mMiddle.setEnabled(aEnabled);
                return;
            case 3:
                this.mRight.setEnabled(aEnabled);
                return;
            case 4:
                this.mRightOrMenu.setEnabled(aEnabled);
                return;
            default:
                return;
        }
    }

    public void performaClick(int aButton) {
        switch (aButton) {
            case 1:
                this.mLeft.performClick();
                return;
            case 2:
                this.mMiddle.performClick();
                return;
            case 3:
                this.mRight.performClick();
                return;
            case 4:
                if (this.mMoreMenuDetail != null && this.mMoreMenuDetail.mDrawableResourceID > 0) {
                    this.mRightOrMenu.performClick();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void refreshDescriptionInternal() {
        if (this.mLeftDetail != null && this.mLeftDetail.mStringResourceID > 0) {
            this.mLeft.setContentDescription(this.mContext.getString(this.mLeftDetail.mStringResourceID));
        }
        if (this.mMiddleDetail != null && this.mMiddleDetail.mStringResourceID > 0) {
            this.mMiddle.setContentDescription(this.mContext.getString(this.mMiddleDetail.mStringResourceID));
        }
        if (this.mRightDetail != null && this.mRightDetail.mStringResourceID > 0) {
            this.mRight.setContentDescription(this.mContext.getString(this.mRightDetail.mStringResourceID));
        }
        if (this.mMoreMenuDetail != null && this.mMoreMenuDetail.mStringResourceID > 0) {
            this.mRightOrMenu.setContentDescription(this.mContext.getString(this.mMoreMenuDetail.mStringResourceID));
        }
    }

    public void reset() {
        this.mLeft.setVisibility(0);
        this.mLeft.setEnabled(true);
        this.mMiddle.setVisibility(0);
        this.mMiddle.setEnabled(true);
        this.mRight.setVisibility(0);
        this.mRight.setEnabled(true);
        this.mRightOrMenu.setVisibility(0);
        this.mRightOrMenu.setEnabled(true);
        this.mCollMenu.clear();
    }

    private void showPopup(View aView) {
        if (this.mPopupMenu == null) {
            this.mCollapsedItems.clear();
            PopupMenu popup = new PopupMenu(this.mContext, aView);
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem aMenuItem) {
                    SplitActionBarView.this.mPopupMenu = null;
                    if (SplitActionBarView.this.mMenuClickListener == null) {
                        return false;
                    }
                    if (SplitActionBarView.this.mMenuClickListener.onCustomMenuItemClick(aMenuItem)) {
                        return true;
                    }
                    return SplitActionBarView.this.mMenuClickListener.onCustomSplitMenuItemClick(SplitActionBarView.this.mCollapsedItems.keyAt(SplitActionBarView.this.mCollapsedItems.indexOfValue(aMenuItem)));
                }
            });
            popup.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(PopupMenu menu) {
                    SplitActionBarView.this.mPopupMenu = null;
                    SplitActionBarView.this.mCollapsedItems.clear();
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            Menu popupMenu = popup.getMenu();
            if (this.mMoreMenuDetail != null) {
                inflater.inflate(this.mMoreMenuDetail.mDrawableResourceID, popupMenu);
            }
            if (this.mMenuClickListener != null) {
                this.mMenuClickListener.onPrepareOptionsMenu(popupMenu);
            }
            for (int i = this.mCollMenu.size() - 1; i >= 0; i--) {
                int textResId = ((SetButtonDetails) this.mCollMenu.valueAt(i)).mStringResourceID;
                this.mCollapsedItems.put(textResId, popupMenu.add(textResId));
            }
            this.mPopupMenu = popup;
            popup.show();
        }
    }

    public void close() {
        if (this.mPopupMenu != null) {
            this.mPopupMenu.dismiss();
        }
    }

    private void setMenuItem(SetButtonDetails aLeft, SetButtonDetails aMiddle, SetButtonDetails aRight, SetButtonDetails aMoreMenu) {
        this.mLeftDetail = aLeft;
        this.mMiddleDetail = aMiddle;
        this.mRightDetail = aRight;
        this.mMoreMenuDetail = aMoreMenu;
    }

    public void refreshDescription() {
        boolean isAccessabilityEnabled = CommonUtilMethods.isTalkBackEnabled(this.mContext);
        if (this.mIsAccessabilityEnabled != isAccessabilityEnabled) {
            this.mIsAccessabilityEnabled = isAccessabilityEnabled;
            if (this.mIsAccessabilityEnabled) {
                refreshDescriptionInternal();
            }
        }
    }
}
