package com.android.mms.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AttachmentSmileyPagerAdatper;
import com.android.mms.ui.AttachmentTypeSelectorAdapter;
import com.android.mms.ui.AttachmentTypeSelectorAdapter.AttachmentListItem;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.SignView;
import com.android.mms.ui.SmileyFaceSelectorAdapter;
import com.android.mms.util.SmileyParser;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;

public class ComposeChoosePanel implements OnTabChangeListener {
    private ViewStub mAttachmentStub;
    private AttachmentSmileyPagerAdatper mAttachmentTypeSelectorAdapter;
    private Context mContext;
    private ImageButton mDeleteKey;
    private int mEmojiTabIconColor;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg != null && 1 == msg.what) {
                ComposeChoosePanel.this.setAttachmentPagerAdapter(((Boolean) msg.obj).booleanValue());
            }
        }
    };
    private IChoosePanelHoler mHolder;
    private boolean mIsAttachmentShow = false;
    private boolean mIsPagerChange = false;
    private boolean mIsSmileyFaceShow = false;
    private int mItemColor;
    private int mLastCategoryID;
    private int mLineColor;
    private View mNoEmojiHistoryView;
    private ViewPager mPager;
    private final SharedPreferences mPrefs;
    private int mSignColor;
    private ViewGroup mSignView;
    private float mSmileyAnimationMove = 0.0f;
    private float mSmileyAnimationMoved = ContentUtil.FONT_SIZE_NORMAL;
    private AttachmentSmileyPagerAdatper mSmileyFaceSelectorAdapter;
    private ViewStub mSmileyFaceStub;
    private RelativeLayout mSmileyLayout;
    private TabHost mTabHost;
    private final int[] sCategoryIconIds = new int[]{R.drawable.ic_sms_clock, R.drawable.ic_sms_expression, R.drawable.ic_sms_crown, R.drawable.ic_sms_flower, R.drawable.ic_sms_car, R.drawable.ic_sms_figure};
    private final int[] sCategoryIconIdsChecked = new int[]{R.drawable.ic_sms_clock_checked, R.drawable.ic_sms_expression_checked, R.drawable.ic_sms_crown_checked, R.drawable.ic_sms_flower_checked, R.drawable.ic_sms_car_checked, R.drawable.ic_sms_figure_checked};
    private final int[] sDescriptionResourceIdsForCategories = new int[]{R.string.spoken_descrption_emoji_category_recents, R.string.spoken_descrption_emoji_category_people, R.string.spoken_descrption_emoji_category_objects, R.string.spoken_descrption_emoji_category_nature, R.string.spoken_descrption_emoji_category_places, R.string.spoken_descrption_emoji_category_symbols};

    public interface IChoosePanelHoler {
        void addAttachment(int i, boolean z);

        View findViewById(int i);

        OnTouchListener getDeleteKeyClickListener();

        HwBaseFragment getFragment();

        Resources getResources();

        OnItemClickListener getSmileyItemClickListener();

        void hideKeyboard();

        boolean isShowSlideOptions();

        void showEnableFullScreenIcon();
    }

    public ComposeChoosePanel(IChoosePanelHoler holder) {
        this.mHolder = holder;
        this.mItemColor = this.mHolder.getResources().getColor(R.color.signview_divider_line_color);
        this.mSignColor = this.mHolder.getResources().getColor(R.color.emoji_category_panel_choose);
        this.mLineColor = this.mHolder.getResources().getColor(R.color.text_color_dark_splite_line);
        this.mEmojiTabIconColor = this.mHolder.getResources().getColor(R.color.emoji_category_panel_dark);
        this.mContext = holder.getFragment().getContext();
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mLastCategoryID = SmileyParser.getCategoryIdByPagerPosition(this.mPrefs.getInt("emoji_pager", 1), false, this.mContext);
    }

    public boolean isShowAttachment() {
        return this.mIsAttachmentShow;
    }

    public boolean isShowSmileyFace() {
        return this.mIsSmileyFaceShow;
    }

    public boolean isVisible() {
        return !this.mIsAttachmentShow ? this.mIsSmileyFaceShow : true;
    }

    public void onActivityConfigurationChanged(Configuration newConfig) {
        if (!SmileyParser.isInMultiWindowMode(this.mContext)) {
            this.mPrefs.edit().putInt("emoji_pager", SmileyParser.getEmojiPagePosition(SmileyParser.getCategoryIdByPagerPosition(this.mPrefs.getInt("emoji_pager", 1), true, this.mContext), this.mContext)).apply();
        }
        if (this.mIsSmileyFaceShow) {
            setSmileyPagerAdapter(true);
        } else if (this.mIsAttachmentShow) {
            setAttachmentPagerAdapter(true);
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mPrefs.edit().putInt("emoji_pager", SmileyParser.getEmojiPagePosition(SmileyParser.getCategoryIdByPagerPositionWhenMultiWindowChanged(this.mPrefs.getInt("emoji_pager", 1), isInMultiWindowMode, this.mContext), this.mContext)).apply();
    }

    public void onMulitWindowChanged(boolean aMWStatus) {
        if (this.mIsSmileyFaceShow) {
            setSmileyPagerAdapter(true);
        } else if (this.mIsAttachmentShow) {
            setAttachmentPagerAdapter(true);
        }
    }

    public boolean hidePanel() {
        boolean changed = false;
        if (this.mIsAttachmentShow) {
            this.mAttachmentStub.setVisibility(8);
            this.mHolder.showEnableFullScreenIcon();
            this.mIsAttachmentShow = false;
            changed = true;
        }
        if (!this.mIsSmileyFaceShow) {
            return changed;
        }
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            TranslateAnimation animation = new TranslateAnimation(1, this.mSmileyAnimationMove, 1, this.mSmileyAnimationMove, 1, this.mSmileyAnimationMove, 1, this.mSmileyAnimationMoved);
            animation.setDuration(200);
            this.mSmileyLayout.startAnimation(animation);
        }
        this.mSmileyFaceStub.setVisibility(8);
        this.mIsSmileyFaceShow = false;
        return true;
    }

    public void showSmileyDialog(boolean replace, Context context) {
        if (this.mIsAttachmentShow) {
            this.mAttachmentStub.setVisibility(8);
            this.mIsAttachmentShow = false;
        }
        this.mHolder.hideKeyboard();
        setSmileyPagerAdapter(replace);
    }

    private void setAttachmentPagerAdapter(final boolean replace) {
        ViewPager pager;
        ViewGroup signView;
        if (this.mAttachmentStub == null) {
            this.mAttachmentStub = (ViewStub) this.mHolder.findViewById(R.id.attachmentview);
            View view = this.mAttachmentStub.inflate();
            pager = (ViewPager) view.findViewById(R.id.grid_pager);
            signView = (ViewGroup) view.findViewById(R.id.current_sign_view);
            view.setBackgroundColor(this.mLineColor);
            pager.setBackgroundColor(this.mItemColor);
            signView.setBackgroundColor(this.mSignColor);
        } else {
            signView = (ViewGroup) this.mHolder.findViewById(R.id.current_sign_view);
            pager = (ViewPager) this.mHolder.findViewById(R.id.grid_pager);
            signView.removeAllViews();
            pager.removeAllViews();
        }
        LayoutParams layoutParam = this.mAttachmentStub.getLayoutParams();
        if (MmsConfig.isInSimpleUI()) {
            layoutParam.height = (int) this.mHolder.getResources().getDimension(R.dimen.attach_panel_height_sui);
        } else if (SmileyParser.isInMultiWindowMode(this.mContext)) {
            layoutParam.height = (int) this.mHolder.getResources().getDimension(R.dimen.attach_panel_height_multiwindow);
        } else {
            layoutParam.height = (int) this.mHolder.getResources().getDimension(R.dimen.attach_panel_height);
        }
        this.mAttachmentTypeSelectorAdapter = null;
        this.mAttachmentTypeSelectorAdapter = new AttachmentSmileyPagerAdatper(this.mContext, false, this.mHolder.isShowSlideOptions());
        this.mAttachmentTypeSelectorAdapter.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ComposeChoosePanel.this.mHolder.addAttachment(((AttachmentListItem) ((AttachmentTypeSelectorAdapter) parent.getAdapter()).getItem(position)).getCommand(), replace);
            }
        });
        this.mAttachmentTypeSelectorAdapter.getAdapter().notifyDataSetChanged();
        pager.setAdapter(this.mAttachmentTypeSelectorAdapter.getAdapter());
        if (!this.mIsAttachmentShow) {
            this.mAttachmentStub.setVisibility(0);
            this.mIsAttachmentShow = true;
        }
        setPagerView(signView, pager);
    }

    private void setSmileyPagerAdapter(boolean replace) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            ViewPager pager;
            ViewGroup signView;
            if (this.mSmileyFaceStub == null || (HwMessageUtils.isSplitOn() && (this.mContext instanceof ConversationList))) {
                View view;
                if (HwMessageUtils.isSplitOn() && (this.mContext instanceof ConversationList)) {
                    if (((ConversationList) this.mContext).getSmileyFaceStub() == null) {
                        view = ((ConversationList) this.mContext).findSmileyFaceStub();
                    } else {
                        view = ((ConversationList) this.mContext).getSmileyFaceView();
                    }
                    this.mSmileyFaceStub = ((ConversationList) this.mContext).getSmileyFaceStub();
                } else {
                    this.mSmileyFaceStub = (ViewStub) this.mHolder.findViewById(R.id.smileyfaceview);
                    view = this.mSmileyFaceStub.inflate();
                }
                pager = (ViewPager) view.findViewById(R.id.smiley_grid_pager);
                signView = (ViewGroup) view.findViewById(R.id.smiley_current_sign_view);
                view.setBackgroundColor(this.mLineColor);
                pager.setBackgroundColor(this.mSignColor);
                signView.setBackgroundColor(this.mSignColor);
            } else {
                pager = (ViewPager) this.mHolder.findViewById(R.id.smiley_grid_pager);
                signView = (ViewGroup) this.mHolder.findViewById(R.id.smiley_current_sign_view);
                pager.removeAllViewsInLayout();
                signView.removeAllViews();
            }
            this.mSmileyFaceSelectorAdapter = null;
            this.mSmileyFaceSelectorAdapter = new AttachmentSmileyPagerAdatper(this.mContext, true, false);
            LayoutParams layoutParam = this.mSmileyFaceStub.getLayoutParams();
            boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
            boolean isInMultiWindow = SmileyParser.isInMultiWindowMode(this.mContext);
            Resources resources = this.mHolder.getResources();
            int i = (isLand || isInMultiWindow) ? R.dimen.smiley_panel_height_multiwindow : R.dimen.smiley_panel_height;
            layoutParam.height = (int) resources.getDimension(i);
            pager.getLayoutParams().height = (layoutParam.height - ((int) this.mHolder.getResources().getDimension(R.dimen.sign_view_height))) - (((int) this.mHolder.getResources().getDimension(R.dimen.attach_panel_spacing)) * 2);
            this.mSmileyFaceSelectorAdapter.setOnItemClickListener(this.mHolder.getSmileyItemClickListener());
            this.mSmileyFaceSelectorAdapter.getAdapter().notifyDataSetChanged();
            pager.setAdapter(this.mSmileyFaceSelectorAdapter.getAdapter());
            if (!this.mIsSmileyFaceShow) {
                this.mSmileyFaceStub.setVisibility(0);
                this.mIsSmileyFaceShow = true;
            }
            setPagerView(signView, pager);
            return;
        }
        setEmojiPagerAdapter();
    }

    private void setPagerView(ViewGroup signView, ViewPager pager) {
        if (signView != null && pager != null) {
            int count;
            signView.setVisibility(0);
            final float signViewItemHeight = this.mHolder.getResources().getDimension(R.dimen.attachment_signview_height);
            final float signViewItemWidth = this.mHolder.getResources().getDimension(R.dimen.attachment_signview_width);
            if (this.mIsSmileyFaceShow) {
                count = this.mSmileyFaceSelectorAdapter.getAdapter().getCount();
            } else {
                count = this.mAttachmentTypeSelectorAdapter.getAdapter().getCount();
            }
            int i = 0;
            while (i < count) {
                if (count < 2) {
                    signView.setVisibility(8);
                    break;
                } else {
                    signView.addView(new SignView(this.mContext, pager.getCurrentItem() == i), (int) signViewItemWidth, (int) signViewItemHeight);
                    i++;
                }
            }
            final ViewGroup viewGroup = signView;
            pager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    if (viewGroup != null) {
                        viewGroup.removeAllViews();
                        int i = 0;
                        while (i < count && count >= 2) {
                            viewGroup.addView(new SignView(ComposeChoosePanel.this.mContext, position == i), (int) signViewItemWidth, (int) signViewItemHeight);
                            i++;
                        }
                    }
                }
            });
        }
    }

    private void setEmojiPagerAdapter() {
        if (this.mSmileyFaceStub == null || (HwMessageUtils.isSplitOn() && (this.mContext instanceof ConversationList))) {
            View view;
            boolean z = true;
            if (HwMessageUtils.isSplitOn() && (this.mContext instanceof ConversationList)) {
                z = ((ConversationList) this.mContext).getSmileyFaceStub() == null;
                if (z) {
                    view = ((ConversationList) this.mContext).findSmileyFaceStub();
                } else {
                    view = ((ConversationList) this.mContext).getSmileyFaceView();
                }
                this.mSmileyFaceStub = ((ConversationList) this.mContext).getSmileyFaceStub();
            } else {
                this.mSmileyFaceStub = (ViewStub) this.mHolder.findViewById(R.id.smiley_face_view_emoji);
                view = this.mSmileyFaceStub.inflate();
            }
            this.mSmileyLayout = (RelativeLayout) view.findViewById(R.id.smiley_grid_view);
            this.mPager = (ViewPager) view.findViewById(R.id.smiley_grid_pager_emoji);
            this.mSignView = (ViewGroup) view.findViewById(R.id.smiley_current_sign_view);
            this.mDeleteKey = (ImageButton) view.findViewById(R.id.emoji_keyboard_delete);
            this.mDeleteKey.setBackgroundResource(R.drawable.ic_sms_delete);
            this.mDeleteKey.setOnTouchListener(this.mHolder.getDeleteKeyClickListener());
            this.mTabHost = (TabHost) view.findViewById(R.id.emoji_category_tabhost);
            if (z) {
                this.mTabHost.setup();
                int categoryNum = SmileyParser.getCategoryNum();
                for (int categoryId = 0; categoryId < categoryNum; categoryId++) {
                    addTab(this.mTabHost, categoryId);
                }
            }
            this.mTabHost.setOnTabChangedListener(this);
            view.setBackgroundColor(this.mLineColor);
            this.mPager.setBackgroundColor(this.mSignColor);
            this.mSignView.setBackgroundColor(this.mSignColor);
            this.mNoEmojiHistoryView = view.findViewById(R.id.no_emoji_history);
        } else {
            this.mPager.removeAllViewsInLayout();
            this.mSignView.removeAllViews();
        }
        boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
        boolean isInMultiWindow = SmileyParser.isInMultiWindowMode(this.mContext);
        LayoutParams layoutParam = this.mSmileyFaceStub.getLayoutParams();
        Resources resources = this.mHolder.getResources();
        int i = isInMultiWindow ? R.dimen.multi_window_smiley_content_height_emoji : isLand ? R.dimen.smiley_content_height_emoji : R.dimen.smiley_panel_height_emoji;
        layoutParam.height = (int) resources.getDimension(i);
        View view2 = this.mNoEmojiHistoryView;
        Resources resources2 = this.mHolder.getResources();
        i = (isLand || isInMultiWindow) ? R.dimen.emoji_no_history_land : R.dimen.emoji_no_history;
        view2.setPadding(0, (int) resources2.getDimension(i), 0, 0);
        this.mSmileyFaceSelectorAdapter = null;
        this.mSmileyFaceSelectorAdapter = new AttachmentSmileyPagerAdatper(this.mContext, true, false);
        this.mSmileyFaceSelectorAdapter.setOnItemClickListener(this.mHolder.getSmileyItemClickListener());
        this.mSmileyFaceSelectorAdapter.getAdapter().notifyDataSetChanged();
        this.mPager.setAdapter(this.mSmileyFaceSelectorAdapter.getAdapter());
        if (!this.mIsSmileyFaceShow) {
            this.mSmileyFaceStub.setVisibility(0);
            TranslateAnimation animation = new TranslateAnimation(1, this.mSmileyAnimationMove, 1, this.mSmileyAnimationMove, 1, this.mSmileyAnimationMoved, 1, this.mSmileyAnimationMove);
            animation.setDuration(200);
            this.mSmileyLayout.startAnimation(animation);
            this.mIsSmileyFaceShow = true;
        }
        int emojiPagerPosition = this.mPrefs.getInt("emoji_pager", 1);
        setPagerView(emojiPagerPosition);
        this.mIsPagerChange = true;
        setPagerIndicatorView(emojiPagerPosition);
        this.mIsPagerChange = false;
    }

    private void setPagerView(int position) {
        if (this.mSignView != null && this.mPager != null) {
            this.mSignView.setVisibility(0);
            this.mPager.setCurrentItem(position, false);
            this.mPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    ComposeChoosePanel.this.mNoEmojiHistoryView.setVisibility(8);
                    ComposeChoosePanel.this.mIsPagerChange = true;
                    ComposeChoosePanel.this.setPagerIndicatorView(position);
                    ComposeChoosePanel.this.mPrefs.edit().putInt("emoji_pager", position).apply();
                    ComposeChoosePanel.this.mIsPagerChange = false;
                }
            });
        }
    }

    private void setPagerIndicatorView(int position) {
        float signViewItemHeight = this.mHolder.getResources().getDimension(R.dimen.attachment_signview_height);
        float signViewItemWidth = this.mHolder.getResources().getDimension(R.dimen.attachment_signview_width);
        int categoryId = SmileyParser.getCategoryIdByPagerPosition(position, false, this.mContext);
        if (categoryId == 0) {
            refreshRecentEmojiAdapter();
        }
        int categoryPageCount = SmileyParser.getCategoryPageCount(categoryId, this.mContext);
        int emojiIndicatorPosition = SmileyParser.getPageIndicatorPosition(categoryId, position, this.mContext);
        this.mTabHost.setCurrentTab(categoryId);
        updateTabIconView(categoryId);
        if (this.mSignView != null) {
            this.mSignView.removeAllViews();
            for (int i = 0; i < categoryPageCount && categoryPageCount >= 2; i++) {
                boolean z;
                ViewGroup viewGroup = this.mSignView;
                Context context = this.mContext;
                if (emojiIndicatorPosition == i) {
                    z = true;
                } else {
                    z = false;
                }
                viewGroup.addView(new SignView(context, z), (int) signViewItemWidth, (int) signViewItemHeight);
            }
        }
    }

    private void addTab(TabHost host, int categoryId) {
        TabSpec tspec = host.newTabSpec(SmileyParser.getCategoryName(categoryId, 0));
        tspec.setContent(R.id.emoji_keyboard_dummy);
        ImageView iconView = (ImageView) LayoutInflater.from(this.mContext).inflate(R.layout.emoji_keyboard_tab_icon, null);
        iconView.setImageResource(this.sCategoryIconIds[categoryId]);
        iconView.setContentDescription(this.mContext.getResources().getString(this.sDescriptionResourceIdsForCategories[categoryId]));
        tspec.setIndicator(iconView);
        host.addTab(tspec);
    }

    private void updateTabIconView(int currentCategoryId) {
        if (this.mTabHost != null) {
            TabWidget tabWidget = this.mTabHost.getTabWidget();
            if (this.mLastCategoryID >= tabWidget.getChildCount()) {
                this.mLastCategoryID = tabWidget.getChildCount() - 1;
            }
            tabWidget.getChildTabViewAt(this.mLastCategoryID).setBackgroundColor(this.mEmojiTabIconColor);
            ((ImageView) tabWidget.getChildTabViewAt(this.mLastCategoryID)).setImageResource(this.sCategoryIconIds[this.mLastCategoryID]);
            tabWidget.getChildTabViewAt(currentCategoryId).setBackgroundColor(this.mSignColor);
            ((ImageView) tabWidget.getChildTabViewAt(currentCategoryId)).setImageResource(this.sCategoryIconIdsChecked[currentCategoryId]);
            this.mLastCategoryID = currentCategoryId;
        }
    }

    public void onTabChanged(String tabId) {
        if (!this.mIsPagerChange) {
            this.mNoEmojiHistoryView.setVisibility(8);
            int categoryId = SmileyParser.getCategoryIdByTabId(tabId);
            int position = SmileyParser.getEmojiPagePosition(categoryId, this.mContext);
            this.mPrefs.edit().putInt("emoji_pager", position).apply();
            setPagerView(position);
            if (categoryId == 0) {
                refreshRecentEmojiAdapter();
            }
            updateTabIconView(categoryId);
        }
    }

    private void refreshRecentEmojiAdapter() {
        SmileyFaceSelectorAdapter recentAdapter = this.mSmileyFaceSelectorAdapter.getRecentAdapter();
        recentAdapter.clear();
        if (SmileyFaceSelectorAdapter.getRecentEmojiData(this.mContext) == null) {
            this.mNoEmojiHistoryView.setVisibility(0);
            return;
        }
        this.mNoEmojiHistoryView.setVisibility(8);
        recentAdapter.addAll(SmileyFaceSelectorAdapter.getRecentEmojiData(this.mContext));
        recentAdapter.notifyDataSetChanged();
    }

    public void setIsAttachmentShow(boolean isAttachmentShow) {
        this.mIsAttachmentShow = isAttachmentShow;
    }
}
