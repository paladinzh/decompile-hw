package com.huawei.mms.ui;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.android.app.ActionBarEx;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar.BaseCustomEmuiEditView;
import java.text.NumberFormat;

public class EmuiActionBar extends AbstractEmuiActionBar {
    protected Activity mActivity;
    private OnDoubleClicklistener mOnDoubleClicklistener;
    private MotionEvent mPreviousDownEvent;
    private MotionEvent mPreviousUpEvent;
    private TextView mTitleNumber = null;
    private TextView mTitleView = null;

    public interface OnDoubleClicklistener {
        void onDoubleClick();
    }

    private class CustomEmuiEditView extends BaseCustomEmuiEditView implements Callback {
        private ActionMode mActionMode;

        private CustomEmuiEditView() {
            super();
        }

        protected boolean initLayoutForEditMode() {
            this.mActionMode = EmuiActionBar.this.mActivity.startActionMode(this);
            return this.mActionMode != null;
        }

        protected boolean unInitLayoutForEditMode() {
            if (this.mActionMode != null) {
                ActionMode tmpMode = this.mActionMode;
                this.mActionMode = null;
                tmpMode.finish();
            }
            return true;
        }

        public Menu getActionMenu() {
            return this.mActionMode == null ? null : this.mActionMode.getMenu();
        }

        private boolean setCustomView(ActionMode actionMode) {
            if (EmuiActionBar.this.mActivity == null || actionMode == null) {
                return false;
            }
            this.mCustomedView = EmuiActionBar.this.mActivity.getLayoutInflater().inflate(R.layout.mms_actionbar_edit_view, null);
            actionMode.setCustomView(this.mCustomedView);
            initViews();
            setTitleGravityCenter(true);
            return true;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            setCustomView(mode);
            this.mActionMode = mode;
            EmuiActionBar.this.mActivity.onCreateOptionsMenu(menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            this.mActionMode = mode;
            return EmuiActionBar.this.mActivity.onPrepareOptionsMenu(menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return EmuiActionBar.this.mActivity.onOptionsItemSelected(item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            Menu menu = mode.getMenu();
            if (menu != null) {
                menu.clear();
            }
            if (this.mActionMode != null) {
                if (EmuiActionBar.this.mDestroyWhenExitActionMode) {
                    MLog.e("EmuiActionBar", "ActionBar onDestroyActionMode finish activity");
                    EmuiActionBar.this.mActivity.finish();
                } else {
                    MLog.e("EmuiActionBar", "ActionBar onDestroyActionMode finish do back pressed");
                    EmuiActionBar.this.mActivity.onBackPressed();
                }
            }
        }
    }

    private class CustomEmuiSearchView extends BaseCustomEmuiSearchView {
        private CustomEmuiSearchView() {
            super();
        }

        public boolean onEnterSearchMode() {
            this.mCustomedView = EmuiActionBar.this.mActivity.getLayoutInflater().inflate(R.layout.mms_search_view, null);
            setCustomView(this.mCustomedView);
            return true;
        }

        private void setCustomView(View v) {
            ActionBarEx.setCustomTitle(EmuiActionBar.this.mActionBarInner, v);
        }

        public Menu getActionMenu() {
            return null;
        }
    }

    public void setOnDoubleClickListener(OnDoubleClicklistener mOnDoubleClicklistener) {
        this.mOnDoubleClicklistener = mOnDoubleClicklistener;
    }

    public EmuiActionBar(Activity activity) {
        super(activity);
        this.mActivity = activity;
        this.mCustomView = new CustomEmuiEditView();
        this.mSearchBar = new CustomEmuiSearchView();
    }

    public void setStartIcon(boolean visible, OnClickListener l) {
        setStartIcon(visible, null, l);
    }

    public void setEndIcon(boolean visible, OnClickListener l) {
        setEndIcon(visible, null, l);
    }

    public void setStartIcon(boolean visible, Drawable icon, OnClickListener l) {
        ActionBarEx.setStartIcon(this.mActionBarInner, visible, icon, l);
    }

    public void setEndIcon(boolean visible, Drawable icon, OnClickListener l) {
        ActionBarEx.setEndIcon(this.mActionBarInner, visible, icon, l);
    }

    public void setDisplayShowTitleEnabled(Boolean show) {
        this.mActionBarInner.setDisplayShowTitleEnabled(show.booleanValue());
    }

    public void setDisplayShowHomeEnabled(Boolean show) {
        this.mActionBarInner.setDisplayShowHomeEnabled(show.booleanValue());
    }

    public void setDisplayShowCustomEnabled(Boolean show) {
        this.mActionBarInner.setDisplayShowCustomEnabled(show.booleanValue());
    }

    public void setCustomView(View view, TextView titleView, LayoutParams layoutParams) {
        if (view != null) {
            this.mActionBarInner.setCustomView(view, layoutParams);
            view.getParent().requestDisallowInterceptTouchEvent(false);
            ((ViewGroup) view.getParent()).setClickable(true);
            ((ViewGroup) view.getParent()).setFocusableInTouchMode(true);
            ViewGroup mCusView = (ViewGroup) view;
            mCusView.setBackgroundColor(0);
            this.mTitleView = titleView;
            this.mTitleNumber = (TextView) view.findViewById(R.id.title_number);
            mCusView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case 0:
                            MotionEvent currentEventDown = MotionEvent.obtain(event);
                            if (EmuiActionBar.this.isConsideredDoubleTap(EmuiActionBar.this.mPreviousDownEvent, EmuiActionBar.this.mPreviousUpEvent, currentEventDown) && EmuiActionBar.this.mOnDoubleClicklistener != null) {
                                EmuiActionBar.this.mOnDoubleClicklistener.onDoubleClick();
                            }
                            EmuiActionBar.this.mPreviousDownEvent = currentEventDown;
                            break;
                        case 1:
                            EmuiActionBar.this.mPreviousUpEvent = MotionEvent.obtain(event);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        boolean z = false;
        if (firstDown == null || firstUp == null || secondDown == null) {
            return false;
        }
        long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > 300 || deltaTime < 40) {
            return false;
        }
        int deltaX = ((int) firstDown.getX()) - ((int) secondDown.getX());
        int deltaY = ((int) firstDown.getY()) - ((int) secondDown.getY());
        if ((deltaX * deltaX) + (deltaY * deltaY) < 10000) {
            z = true;
        }
        return z;
    }

    public void setCustomTitle(CharSequence title) {
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setTitle(CharSequence title) {
        if ((this.mTitleView == null && !this.mCustomView.setTitle(title)) || (!this.mCustomView.setTitle(title) && this.mTitleView != null && TextUtils.isEmpty(this.mTitleView.getText()))) {
            this.mActionBarInner.setTitle(title);
        } else if (this.mTitleView != null && !TextUtils.isEmpty(this.mTitleView.getText())) {
            this.mTitleView.setText(title);
        }
    }

    public void setListTitle(CharSequence title, int size) {
        if (this.mTitleView != null && this.mTitleNumber != null) {
            this.mTitleView.setText(title);
            if (size == 0) {
                this.mTitleNumber.setVisibility(8);
            } else {
                setListNumberStyle(size);
            }
        }
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

    private void setListNumberStyle(int size) {
        if (this.mTitleNumber.getVisibility() != 0) {
            this.mTitleNumber.setVisibility(0);
        }
        this.mTitleNumber.setText(NumberFormat.getIntegerInstance().format((long) size));
    }
}
