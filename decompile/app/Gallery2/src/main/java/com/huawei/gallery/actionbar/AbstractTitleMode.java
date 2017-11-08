package com.huawei.gallery.actionbar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.actionbar.view.ActionItem;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.util.ImmersionUtils;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class AbstractTitleMode extends ActionBarStateBase implements OnClickListener {
    protected int mGravity = 17;
    protected boolean mIsSupportDoubleFace = false;
    protected Action mLeftAction = Action.NONE;
    protected SimpleActionItem mLeftActionItem = null;
    private final HashMap<Integer, View> mLightView = new HashMap();
    protected ViewGroup mMainView = null;
    protected Action mRightAction = Action.NONE;
    protected SimpleActionItem mRightActionItem = null;
    protected View mShadowRootView = null;
    private final HashMap<Integer, View> mShadowView = new HashMap();
    protected int mTitleRes = -1;
    protected String mTitleStr = null;
    protected TextView mTitleView = null;

    protected abstract void initViewItem();

    protected void clearView(int flag) {
        if (flag == 0) {
            this.mShadowView.clear();
        } else {
            this.mLightView.clear();
        }
    }

    protected void putView(int flag, int id, View view) {
        if (flag == 0) {
            this.mShadowView.put(Integer.valueOf(id), view);
        } else {
            this.mLightView.put(Integer.valueOf(id), view);
        }
    }

    protected View getView(int flag, int id) {
        if (flag == 0) {
            return (View) this.mShadowView.get(Integer.valueOf(id));
        }
        return (View) this.mLightView.get(Integer.valueOf(id));
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public void setSupportDoubleFace(boolean isSupport) {
        if (this.mIsSupportDoubleFace != isSupport) {
            this.mIsSupportDoubleFace = isSupport;
            if (this.mIsSupportDoubleFace) {
                this.mContainerMgr.setDefaultBackground(null);
                this.mContainerMgr.setHeadBackgroundVisible(false);
            } else {
                this.mContainerMgr.setDefaultBackground(this.mContainerMgr.getHeadBarBackgroundDrawable());
                this.mContainerMgr.setHeadBackgroundVisible(true);
            }
            if (!(this.mMainView == null || this.mShadowRootView == null)) {
                if (this.mIsSupportDoubleFace) {
                    this.mShadowRootView.setVisibility(0);
                } else {
                    this.mShadowRootView.setVisibility(8);
                }
            }
        }
    }

    public void setDoubleFaceAlpha(float lightAlpha, float shadowAlpha) {
        for (Entry<Integer, View> entry : this.mLightView.entrySet()) {
            View view = (View) this.mLightView.get(Integer.valueOf(((Integer) entry.getKey()).intValue()));
            if (view != null) {
                view.setAlpha(lightAlpha);
            }
        }
        for (Entry<Integer, View> entry2 : this.mShadowView.entrySet()) {
            view = (View) this.mShadowView.get(Integer.valueOf(((Integer) entry2.getKey()).intValue()));
            if (view != null) {
                view.setAlpha(shadowAlpha);
            }
        }
    }

    protected int getReverseStyle() {
        return this.mContainerMgr.getStyle() == 1 ? 0 : 1;
    }

    protected Bundle saveState() {
        Bundle data = super.saveState();
        data.putInt("ACTION_MODE_LEFT_ITEM_KEY", this.mLeftAction.id);
        data.putInt("ACTION_MODE_RIGHT_ITEM_KEY", this.mRightAction.id);
        data.putInt("ACTION_MODE_TITLE_RES_KEY", this.mTitleRes);
        if (this.mTitleStr != null) {
            data.putString("ACTION_MODE_TITLE_STR_KEY", this.mTitleStr);
        }
        data.putBoolean("ACTION_MODE_SUPPORT_DOUBLE_FACE", this.mIsSupportDoubleFace);
        data.putInt("ACTION_MODE_GRAVITY", this.mGravity);
        return data;
    }

    protected void resume(Bundle resumeData) {
        super.resume(resumeData);
        this.mLeftAction = Action.getAction(resumeData.getInt("ACTION_MODE_LEFT_ITEM_KEY", Action.ACTION_ID_NONE));
        this.mRightAction = Action.getAction(resumeData.getInt("ACTION_MODE_RIGHT_ITEM_KEY", Action.ACTION_ID_NONE));
        this.mTitleRes = resumeData.getInt("ACTION_MODE_TITLE_RES_KEY", -1);
        this.mTitleStr = resumeData.getString("ACTION_MODE_TITLE_STR_KEY");
        this.mIsSupportDoubleFace = resumeData.getBoolean("ACTION_MODE_SUPPORT_DOUBLE_FACE");
        this.mGravity = resumeData.getInt("ACTION_MODE_GRAVITY");
    }

    public void setBothAction(Action left, Action right) {
        setLeftAction(left);
        setRightAction(right);
    }

    private void setShadowAction(int key, Action action) {
        View leftActionItemShadow = (View) this.mShadowView.get(Integer.valueOf(key));
        if (leftActionItemShadow instanceof SimpleActionItem) {
            ((SimpleActionItem) leftActionItemShadow).applyStyle(getReverseStyle());
            ((SimpleActionItem) leftActionItemShadow).setAction(action);
        }
    }

    public void setLeftAction(Action action) {
        this.mLeftAction = action;
        if (this.mLeftActionItem != null) {
            this.mLeftActionItem.applyStyle(this.mContainerMgr.getStyle());
            this.mLeftActionItem.setAction(action);
            setShadowAction(this.mLeftActionItem.hashCode(), action);
        }
    }

    public void setRightAction(Action action) {
        this.mRightAction = action;
        if (this.mRightActionItem != null) {
            this.mRightActionItem.applyStyle(this.mContainerMgr.getStyle());
            this.mRightActionItem.setAction(action);
            setShadowAction(this.mRightActionItem.hashCode(), action);
        }
    }

    public void hideRightAction(boolean needShow) {
        if (this.mRightActionItem != null) {
            this.mRightActionItem.setVisibility(needShow ? 0 : 8);
        }
    }

    private void setShadowTitleInternal(int key) {
        View view = (View) this.mShadowView.get(Integer.valueOf(key));
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (this.mTitleRes > 0) {
                textView.setText(this.mTitleRes);
            } else if (this.mTitleStr != null) {
                textView.setText(this.mTitleStr);
            } else {
                textView.setText("");
            }
            ImmersionUtils.setTextViewDefaultColorImmersionStyle(textView, getReverseStyle());
        }
    }

    protected void setTitleInternal() {
        if (this.mTitleView != null) {
            if (this.mTitleRes > 0) {
                this.mTitleView.setText(this.mTitleRes);
            } else if (this.mTitleStr != null) {
                this.mTitleView.setText(this.mTitleStr);
            } else {
                this.mTitleView.setText("");
            }
            ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mTitleView, this.mContainerMgr.getStyle());
            setShadowTitleInternal(this.mTitleView.hashCode());
        }
    }

    public void updateTitleStyle() {
        setTitleInternal();
    }

    public void setTitle(int titleResID) {
        this.mTitleRes = titleResID;
        this.mTitleStr = null;
        setTitleInternal();
    }

    public void setTitle(String title) {
        this.mTitleRes = -1;
        this.mTitleStr = title;
        setTitleInternal();
    }

    public ActionItem getRightActionItem() {
        return this.mRightActionItem;
    }

    protected void showHeadView() {
        if (this.mMainView == null) {
            initViewItem();
            if (this.mActionBar.getNavigationMode() != 0) {
                this.mActionBar.setNavigationMode(0);
            }
            this.mActionBar.setDisplayOptions(16);
            this.mActionBar.setDisplayShowCustomEnabled(true);
            this.mActionBar.setCustomView(this.mMainView);
            this.mMainView.setLayoutParams(new LayoutParams(-1, -1));
        }
    }

    public void onClick(View view) {
        if (view instanceof ActionItem) {
            this.mActivity.onActionItemClicked(((ActionItem) view).getAction());
        } else {
            GalleryLog.d("AbstractTitleMode", "Click on a strange view, return : " + view);
        }
    }
}
