package com.huawei.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.util.HwCustEcidLookup;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.util.WallPaperImageHelper;
import com.huawei.cspcommon.util.WallPaperImageHelper.BlurWallpaperChangedWatcher;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.BlacklistCommonUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsPeopleActionBar;
import com.huawei.rcs.ui.RcsPeopleActionBar.IHwCustPeopleActionBarCallback;
import java.util.Arrays;

public class PeopleActionBar extends RelativeLayout implements ActionBarInterface, OnClickListener, AnimationListener, BlurWallpaperChangedWatcher {
    private static float FULL_ALPHA = ContentUtil.FONT_SIZE_NORMAL;
    private static float HALF_ALPHA = 0.5f;
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private Animation mActionBarExpandAnimation = null;
    private int mActionBarHeight = 0;
    private int mActionBarLandscapeMaxHeight = 0;
    private int mActionBarMaxHeight = 0;
    private int mActionBarMinHeight = 0;
    private int mActionBarTitleRegion = 0;
    private ImageView mBlacklistMenuDivider;
    private boolean mCanExpand = true;
    private RcsPeopleActionBar mCust;
    private AddWhatsAppPeopleActionBarAdapter mDataAdatper;
    private Drawable mDrawable;
    private ImageView mExpandButton;
    private ExpandListener mExpandListener;
    private int mFirstMotionY = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (this) {
                        PeopleActionBar.this.setBlurWallpaperBackground(PeopleActionBar.this, false);
                        PeopleActionBar.this.invalidate();
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mInCollpaseAnimation = false;
    private boolean mIsExpanded = false;
    private Paint mLinePaint;
    private EmuiMenuText mMenuFirst;
    private EmuiMenuText mMenuFourth;
    private LinearLayout mMenuHolder;
    private EmuiMenuText mMenuSecond;
    private EmuiMenuText mMenuThird;
    private AlertDialog mMoreDialog;
    private int[] mMoreMenuItemIds = null;
    private String[] mMoreMenuOperationStrings = null;
    private LinearLayout mMsgListViewLayout;
    private boolean mNeedHideKeyBoard = true;
    private int mOrientation;
    private PopupWindow mPopupWindow;
    private EmuiAvatarImage mProfileView;
    private ImageView mRelatedContactMenuDivider;
    private boolean mShowSubTitle = true;
    private boolean mShowSubTitle2 = true;
    private TextView mSubTitle;
    private TextView mSubTitle2;
    private TextView mTitle;
    private ViewGroup mTitleHolder;
    private int mTitleMoveDownOffset = 0;
    private int mTitlePaddingTopNormal = 0;
    private ImageView mWeichatIcon;
    private ImageView mWhatsappIcon;
    public int maxLine = 0;
    private Runnable onAddToBlackList = new Runnable() {
        public void run() {
            HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                public void run() {
                    if (PeopleActionBar.this.mMenuFourth.getVisibility() == 0 && PeopleActionBar.this.mIsExpanded) {
                        PeopleActionBar.this.updateMenu();
                    }
                }
            }, 2000);
        }
    };

    public interface PeopleActionBarAdapter {
        void addToBlacklist(boolean z, Runnable runnable);

        void addToContact();

        void callRecipients();

        ContactList getContactList();

        String getName();

        String getNumber();

        boolean hasEmail();

        boolean hasWeichat();

        boolean isExistsInContact();

        boolean isGroup();

        boolean isHwMsgSender();

        void viewPeopleInfo();

        void writeEmail();

        void writeWeichat();
    }

    public interface AddWhatsAppPeopleActionBarAdapter extends PeopleActionBarAdapter {
        void editBeforeCall();

        boolean hasWhatsapp();

        void writeWhatsapp();
    }

    public interface ExpandListener {
        void onCollapsStop();

        void onExpandStart();

        void onExpandStop();
    }

    private class HwPeopleActionBarCustHolder implements IHwCustPeopleActionBarCallback {
        private HwPeopleActionBarCustHolder() {
        }

        public void updateRcsMenu(boolean isNeedToShowCreateGroupchat) {
            PeopleActionBar.this.updateRcsMenu(isNeedToShowCreateGroupchat);
        }
    }

    public RcsPeopleActionBar getHwCust() {
        return this.mCust;
    }

    public PeopleActionBar(Context context) {
        super(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mCust = new RcsPeopleActionBar();
        }
    }

    public PeopleActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mCust = new RcsPeopleActionBar();
        }
    }

    public PeopleActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mCust = new RcsPeopleActionBar();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mTitle.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (PeopleActionBar.this.mTitle.getHeight() != 0) {
                    PeopleActionBar.this.mTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    PeopleActionBar.this.calculaterActionBarMinHeight(PeopleActionBar.this.mActionBarMinHeight);
                }
            }
        });
        if (this.mCust != null) {
            this.mCust.updateTitle(getContext(), this);
        }
        this.mSubTitle = (TextView) findViewById(R.id.sub_title);
        this.mSubTitle.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (PeopleActionBar.this.mSubTitle.getHeight() != 0) {
                    PeopleActionBar.this.mSubTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (PeopleActionBar.this.calculaterLandActionBarMaxHeight(PeopleActionBar.this.mActionBarLandscapeMaxHeight) && PeopleActionBar.this.mOrientation == 2) {
                        PeopleActionBar.this.mActionBarMaxHeight = PeopleActionBar.this.mActionBarLandscapeMaxHeight;
                        if (PeopleActionBar.this.mIsExpanded && PeopleActionBar.this.mTitleHolder != null && PeopleActionBar.this.mMsgListViewLayout != null) {
                            LayoutParams param = (LayoutParams) PeopleActionBar.this.mTitleHolder.getLayoutParams();
                            param.height = PeopleActionBar.this.mActionBarMaxHeight;
                            PeopleActionBar.this.mTitleHolder.setLayoutParams(param);
                            PeopleActionBar.this.mMsgListViewLayout.setTranslationY((float) PeopleActionBar.this.mActionBarMaxHeight);
                        }
                    }
                }
            }
        });
        this.mSubTitle2 = (TextView) findViewById(R.id.sub_title_2);
        this.mWeichatIcon = (ImageView) findViewById(R.id.icon_weichat);
        this.mWhatsappIcon = (ImageView) findViewById(R.id.icon_whatsapp);
        this.mMenuHolder = (LinearLayout) findViewById(R.id.custom_menus);
        this.mTitleHolder = (ViewGroup) findViewById(R.id.people_actionbar);
        this.mExpandButton = (ImageView) findViewById(R.id.button_expand);
        this.mExpandButton.setContentDescription(getContext().getString(R.string.hint_expand));
        this.mMenuHolder.setAnimationCacheEnabled(false);
        this.mExpandButton.setOnClickListener(this);
        this.mLinePaint = new Paint();
        this.mLinePaint.setColor(getResources().getColor(R.color.text_color_white_splite_line));
        this.mLinePaint.setStyle(Style.STROKE);
        this.mTitleMoveDownOffset = (int) getResources().getDimension(R.dimen.action_bar_title_drop_offset);
        this.mOrientation = getResources().getConfiguration().orientation;
        if (this.mOrientation == 2) {
            this.mActionBarLandscapeMaxHeight = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height_landscape);
            this.mActionBarMaxHeight = this.mActionBarLandscapeMaxHeight;
            updateMenuHolderLayout(true);
        } else {
            this.mActionBarMaxHeight = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height);
        }
        this.mActionBarTitleRegion = (int) getResources().getDimension(R.dimen.mms_people_action_bar_title_alpha_region);
        this.mTitlePaddingTopNormal = this.mTitle.getPaddingTop();
        this.mIsExpanded = false;
        immersionStyleForActionBar(getContext());
        updateActionBarMinHeight();
        resetParaLayout();
        setContacts();
        immersionStyleForMenu(getContext());
        WallPaperImageHelper.getInstance(getContext()).setBlurWallpaperChangedListener(this);
    }

    private void updateActionBarMinHeight() {
        TypedArray actionbarSizeTypedArray = getContext().obtainStyledAttributes(new int[]{16843499});
        this.mActionBarHeight = (int) actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        calculaterActionBarMinHeight(this.mActionBarHeight);
    }

    private void calculaterActionBarMinHeight(int actionBarHeight) {
        if (this.mTitle != null && this.mTitle.isShown()) {
            int height = this.mTitle.getHeight() + getContext().getResources().getDimensionPixelSize(R.dimen.mms_people_action_bar_title_top);
            if (height > actionBarHeight) {
                actionBarHeight = height;
            }
        }
        if (this.mActionBarMinHeight != actionBarHeight) {
            this.mActionBarMinHeight = actionBarHeight;
            resetParaLayout();
        }
    }

    private boolean calculaterLandActionBarMaxHeight(int actionBarLandscapeMaxHeight) {
        int height = getContext().getResources().getDimensionPixelSize(R.dimen.mms_people_action_bar_title_top);
        if (this.mTitle != null && this.mTitle.isShown()) {
            height += this.mTitle.getHeight();
        }
        if (this.mSubTitle != null && this.mSubTitle.isShown()) {
            height += this.mSubTitle.getHeight() * 2;
        }
        if (height > actionBarLandscapeMaxHeight) {
            actionBarLandscapeMaxHeight = height;
        }
        if (this.mActionBarLandscapeMaxHeight == actionBarLandscapeMaxHeight) {
            return false;
        }
        this.mActionBarLandscapeMaxHeight = actionBarLandscapeMaxHeight;
        return true;
    }

    private void immersionStyleForActionBar(Context context) {
        if (!ResEx.self().isUseThemeBackground(context)) {
            if (HwUiStyleUtils.isSuggestDarkStyle(context)) {
                Resources rs = context.getResources();
                int textTitleDarkColor = rs.getColor(R.color.title_color_primary_dark);
                int textSubTitleDarkColor = rs.getColor(R.color.subtitle_color_primary_dark);
                this.mTitle.setTextColor(textTitleDarkColor);
                this.mSubTitle.setTextColor(textSubTitleDarkColor);
                this.mSubTitle2.setTextColor(textSubTitleDarkColor);
                this.mWeichatIcon.setImageDrawable(rs.getDrawable(R.drawable.mms_ic_wechat_little_dark));
                this.mWhatsappIcon.setImageDrawable(rs.getDrawable(R.drawable.mms_ic_whatsapp_little_dark));
                this.mExpandButton.setImageDrawable(rs.getDrawable(R.drawable.csp_menu_expand_dark));
                this.mLinePaint.setColor(rs.getColor(R.color.text_color_dark_splite_line));
            }
            setBackground(new ColorDrawable(HwUiStyleUtils.getPrimaryColor(context)));
        }
    }

    private void immersionStyleForMenu(Context context) {
        if (!ResEx.self().isUseThemeBackground(context) && HwUiStyleUtils.isSuggestDarkStyle(context)) {
            Resources rs = context.getResources();
            int textTitleDarkColor = rs.getColor(R.color.title_color_primary_dark);
            int count = this.mMenuHolder.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = this.mMenuHolder.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(textTitleDarkColor);
                } else if (child instanceof ImageView) {
                    ((ImageView) child).setImageDrawable(rs.getDrawable(R.drawable.splite_line_drawable_dark));
                }
            }
        }
    }

    public void onBlurWallpaperChanged() {
        this.mHandler.sendEmptyMessage(1);
    }

    public boolean onTouchEvent(MotionEvent event) {
        handleTouchAction(event);
        super.onTouchEvent(event);
        return true;
    }

    private void handleTouchAction(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case 0:
                this.mFirstMotionY = y;
                resetHideKeyBoardSign();
                return;
            case 1:
            case 3:
                expandOrCollapseActionBar(this.mFirstMotionY - y);
                this.mFirstMotionY = 0;
                resetHideKeyBoardSign();
                return;
            case 2:
                int offset = this.mFirstMotionY - y;
                if (this.mIsExpanded) {
                    if (y < this.mFirstMotionY) {
                        setTranslateY(offset);
                        return;
                    }
                    return;
                } else if (y > this.mFirstMotionY) {
                    showOrHideMenu(true);
                    hideTheKeyboard();
                    setTranslateY(offset);
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public void showOrHideMenu(boolean show) {
        if (this.mMenuHolder == null || this.mTitleHolder == null) {
            MLog.e("AB:People", "showOrHideMenu::menu hoder or title holder is null!!");
            return;
        }
        if (this.mCust != null) {
            show = this.mCust.isShowMenuHolder(show);
        }
        LayoutParams param = (LayoutParams) this.mTitleHolder.getLayoutParams();
        if (show) {
            this.mMenuHolder.setVisibility(0);
            param.height = this.mActionBarMaxHeight;
        } else {
            this.mMenuHolder.setVisibility(8);
            param.height = this.mActionBarMinHeight;
        }
        this.mTitleHolder.setLayoutParams(param);
    }

    public void hideTheKeyboard() {
        if (this.mExpandListener != null && this.mNeedHideKeyBoard) {
            this.mExpandListener.onExpandStart();
            this.mNeedHideKeyBoard = false;
        }
    }

    public void resetHideKeyBoardSign() {
        this.mNeedHideKeyBoard = true;
    }

    public void setTranslateY(int offset) {
        if (this.mCanExpand) {
            int translateY;
            float alpha;
            if (this.mIsExpanded && offset > 0) {
                if (this.mActionBarMaxHeight - offset > this.mActionBarMinHeight) {
                    translateY = this.mActionBarMaxHeight - offset;
                } else {
                    translateY = this.mActionBarMinHeight;
                }
                if (offset >= this.mActionBarTitleRegion) {
                    this.mSubTitle.setAlpha(0.0f);
                    this.mSubTitle2.setAlpha(0.0f);
                    this.mMenuHolder.setAlpha(0.0f);
                    if (this.mCust != null) {
                        this.mCust.setGroupMenuAlpha(0.0f);
                    }
                } else {
                    alpha = ((float) offset) / ((float) this.mActionBarTitleRegion);
                    this.mSubTitle.setAlpha(ContentUtil.FONT_SIZE_NORMAL - alpha);
                    this.mSubTitle2.setAlpha(ContentUtil.FONT_SIZE_NORMAL - alpha);
                    this.mMenuHolder.setAlpha(ContentUtil.FONT_SIZE_NORMAL - alpha);
                    if (this.mCust != null) {
                        this.mCust.setGroupMenuAlpha(ContentUtil.FONT_SIZE_NORMAL - alpha);
                    }
                }
            } else if (!this.mIsExpanded && offset < 0) {
                if (this.mActionBarMinHeight - offset < this.mActionBarMaxHeight) {
                    translateY = this.mActionBarMinHeight - offset;
                } else {
                    translateY = this.mActionBarMaxHeight;
                }
                if (this.mTitlePaddingTopNormal - offset > this.mTitleMoveDownOffset) {
                    int movedHight = (-offset) - (this.mTitleMoveDownOffset - this.mTitlePaddingTopNormal);
                    if (movedHight > 0) {
                        alpha = ((float) movedHight) / ((float) this.mActionBarTitleRegion);
                        this.mSubTitle.setAlpha(alpha < ContentUtil.FONT_SIZE_NORMAL ? alpha : ContentUtil.FONT_SIZE_NORMAL);
                        this.mSubTitle2.setAlpha(alpha < ContentUtil.FONT_SIZE_NORMAL ? alpha : ContentUtil.FONT_SIZE_NORMAL);
                        this.mMenuHolder.setAlpha(alpha < ContentUtil.FONT_SIZE_NORMAL ? alpha : ContentUtil.FONT_SIZE_NORMAL);
                        if (this.mCust != null) {
                            RcsPeopleActionBar rcsPeopleActionBar = this.mCust;
                            if (alpha >= ContentUtil.FONT_SIZE_NORMAL) {
                                alpha = ContentUtil.FONT_SIZE_NORMAL;
                            }
                            rcsPeopleActionBar.setGroupMenuAlpha(alpha);
                        }
                    } else {
                        this.mSubTitle.setAlpha(0.0f);
                        this.mSubTitle2.setAlpha(0.0f);
                        this.mMenuHolder.setAlpha(0.0f);
                        if (this.mCust != null) {
                            this.mCust.setGroupMenuAlpha(0.0f);
                        }
                    }
                }
            } else if (this.mIsExpanded) {
                translateY = this.mActionBarMaxHeight;
            } else {
                translateY = this.mActionBarMinHeight;
            }
            if (this.mMsgListViewLayout != null) {
                this.mMsgListViewLayout.setTranslationY((float) translateY);
            }
        }
    }

    public void dismissPopupWindow() {
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
        }
    }

    public boolean isActionBarExpand() {
        return this.mIsExpanded;
    }

    public void expandOrCollapseActionBar(int offset) {
        if (this.mCanExpand) {
            if (!this.mIsExpanded || offset <= 0) {
                if (this.mIsExpanded || offset >= 0) {
                    resetParaLayout();
                } else if ((-offset) > this.mActionBarMaxHeight / 3) {
                    StatisticalHelper.incrementReportCount(getContext(), 2107);
                    if (this.mActionBarMinHeight - offset >= this.mActionBarMaxHeight) {
                        this.mIsExpanded = true;
                        resetParaLayout();
                    } else {
                        setExpandAnamtion(this.mActionBarMinHeight - offset, this.mActionBarMaxHeight);
                    }
                } else {
                    setExpandAnamtion(this.mActionBarMinHeight - offset, this.mActionBarMinHeight);
                }
            } else if (offset <= this.mActionBarMaxHeight / 3) {
                setExpandAnamtion(this.mActionBarMaxHeight - offset, this.mActionBarMaxHeight);
            } else if (this.mActionBarMaxHeight - offset <= this.mActionBarMinHeight) {
                this.mIsExpanded = false;
                resetParaLayout();
            } else {
                setExpandAnamtion(this.mActionBarMaxHeight - offset, this.mActionBarMinHeight);
            }
            if (this.mExpandListener != null) {
                if (this.mIsExpanded) {
                    this.mExpandListener.onCollapsStop();
                } else {
                    this.mExpandListener.onExpandStop();
                }
            }
        }
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setBlurWallpaperBackground(this, this.mInCollpaseAnimation);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0);
        int xpos1 = this.mProfileView.getLeft() + (this.mProfileView.getWidth() >> 1);
        canvas.drawLine((float) xpos1, (float) (this.mProfileView.getBottom() - 14), (float) xpos1, (float) getHeight(), this.mLinePaint);
    }

    private void setBlurWallpaperBackground(View headWallpaperView, boolean isInCollpase) {
        if (ResEx.self().isUseThemeBackground(getContext())) {
            headWallpaperView.setBackgroundResource(R.drawable.mms_header_background0);
        } else if (HwUiStyleUtils.isNewImmersionStyle(getContext())) {
            MLog.i("AB:People", "is new immersionStyle not set BlurWallpaper");
        } else if (!isInCollpase || this.mDrawable == null) {
            this.mDrawable = ResEx.self().setBlurWallpaperBackground(getContext(), this);
        } else {
            headWallpaperView.setBackground(this.mDrawable);
        }
    }

    private void setContacts() {
        this.mMenuFirst = (EmuiMenuText) this.mMenuHolder.findViewById(R.id.menu_add_to_contact);
        this.mMenuFourth = (EmuiMenuText) this.mMenuHolder.findViewById(R.id.menu_black_list);
        this.mBlacklistMenuDivider = (ImageView) this.mMenuHolder.findViewById(R.id.mms_menu_blacklist_divider);
        this.mMenuSecond = (EmuiMenuText) this.mMenuHolder.findViewById(R.id.menu_call_or_mail);
        this.mRelatedContactMenuDivider = (ImageView) this.mMenuHolder.findViewById(R.id.mms_menu_weichat_divider);
        this.mMenuThird = (EmuiMenuText) this.mMenuHolder.findViewById(R.id.menu_weichat);
        this.mMenuThird.setOnClickListener(this);
        if (getHwCust() != null) {
            getHwCust().setMenuMultiLine(this.mMenuFirst, this.mMenuSecond, this.mMenuThird, this.mMenuFourth);
        }
        this.mMenuFirst.setOnClickListener(this);
        this.mMenuFourth.setOnClickListener(this);
        this.mMenuSecond.setOnClickListener(this);
        if (this.mCust != null) {
            this.mCust.initRcsView(this, getContext());
            this.mCust.setHwCustCallback(new HwPeopleActionBarCustHolder());
        }
    }

    private void disabeMenuItem(View menu) {
        menu.setAlpha(HALF_ALPHA);
        menu.setEnabled(false);
        menu.setClickable(false);
    }

    private void updateMenu() {
        boolean z = true;
        AddWhatsAppPeopleActionBarAdapter adapter = this.mDataAdatper;
        if (this.mCust != null && this.mCust.changeToGroupChatMenu()) {
            MLog.i("AB:People", "update menu rcs group chat");
        } else if (this.mCust != null && !adapter.isGroup()) {
            MLog.i("AB:People", "update menu rcs single chat");
            this.mCust.updateRcsMenu();
        } else if (adapter.isGroup()) {
            updateGroupMenu();
        } else {
            updateMenuWithRelatedContact(adapter, updateNormalMenu());
        }
        int[] list = new int[]{this.mMenuFirst.getLineCount(), this.mMenuSecond.getLineCount(), this.mMenuThird.getLineCount(), this.mMenuFourth.getLineCount()};
        Arrays.sort(list);
        this.maxLine = list[3];
        if (!MmsConfig.isVoiceCapable() && this.mMenuSecond.getMenuId() == 2) {
            disabeMenuItem(this.mMenuSecond);
        }
        if (this.mOrientation != 2) {
            z = false;
        }
        updateMenuHolderLayout(z);
    }

    private boolean updateGroupMenu() {
        boolean shouldMenuBlacklistHidden = false;
        if (this.mMenuFirst.getMenuId() != 1) {
            this.mMenuFirst.updateMenu(1, R.string.menu_check_group_participants, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_view_contact));
        }
        if (isBlacklistFeatureEnable()) {
            if (this.mMenuFourth.getMenuId() != 5) {
                this.mMenuFourth.updateMenu(5, R.string.menu_blacklist, getImmersionStyleIcon(getContext(), R.drawable.mms_icon_blacklist));
            }
            disabeMenuItem(this.mMenuFourth);
            this.mMenuFourth.setOnClickListener(this);
        } else {
            setMenuFouthVisibility(8);
            shouldMenuBlacklistHidden = true;
        }
        if (this.mMenuSecond.getMenuId() != 2) {
            this.mMenuSecond.updateMenu(2, R.string.menu_call, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_call_white));
        }
        disabeMenuItem(this.mMenuSecond);
        this.mMenuSecond.setOnClickListener(this);
        return shouldMenuBlacklistHidden;
    }

    private int updateNormalMenu() {
        PeopleActionBarAdapter adapter = this.mDataAdatper;
        int blacklistAndCallEditFlag = 0;
        if (adapter.isExistsInContact()) {
            this.mMenuFirst.updateMenu(1, R.string.menu_view_contact, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_view_contact));
        } else {
            this.mMenuFirst.updateMenu(4, R.string.menu_add_to_contacts, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_add_contact));
        }
        String number = adapter.getNumber();
        if (Contact.isEmailAddress(number)) {
            if (this.mMenuFourth.getMenuId() != 5) {
                this.mMenuFourth.updateMenu(5, R.string.menu_blacklist, getImmersionStyleIcon(getContext(), R.drawable.mms_icon_blacklist));
            }
            disabeMenuItem(this.mMenuFourth);
            this.mMenuFourth.setOnClickListener(this);
        } else if (!isBlacklistFeatureEnable()) {
            blacklistAndCallEditFlag = 1;
        } else if (BlacklistCommonUtils.isNumberBlocked(number)) {
            this.mMenuFourth.updateMenu(6, R.string.menu_remove_from_blacklist, getImmersionStyleIcon(getContext(), R.drawable.mms_icon_blacklist));
        } else {
            this.mMenuFourth.updateMenu(5, R.string.menu_blacklist, getImmersionStyleIcon(getContext(), R.drawable.mms_icon_blacklist));
        }
        if (adapter.hasEmail()) {
            this.mMenuSecond.updateMenu(3, R.string.menu_mail, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_mail_white));
            return blacklistAndCallEditFlag | 2;
        }
        this.mMenuSecond.updateMenu(2, R.string.menu_call, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_call_white));
        this.mMenuThird.updateMenu(13, R.string.clickspan_edit_call, getImmersionStyleIcon(getContext(), R.drawable.bg_message_received_project));
        setMenuThirdVisibility(0);
        return blacklistAndCallEditFlag;
    }

    private void updateRcsMenu(boolean isNeedToShowCreateGroupchat) {
        updateRcsMenuWithRelatedContact(this.mDataAdatper, updateNormalMenu(), isNeedToShowCreateGroupchat);
    }

    private void updateRcsMenuWithRelatedContact(AddWhatsAppPeopleActionBarAdapter adapter, int blacklistAndCallEditFlag, boolean isNeedToShowCreateGroupchat) {
        int i = 10;
        String number = adapter.getNumber();
        if (isNeedToShowCreateGroupchat) {
            this.mMenuSecond.updateMenu(8, R.string.rcs_add_group_chat, R.drawable.rcs_creat_group_chat_selector);
            if (adapter.hasEmail()) {
                this.mMenuThird.updateMenu(3, R.string.menu_mail, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_mail_white));
            } else {
                this.mMenuThird.updateMenu(2, R.string.menu_call, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_call_white));
            }
            setMenuThirdVisibility(0);
            if (adapter.hasWeichat() && adapter.hasWhatsapp()) {
                if (getLastUsedMenu() != 0) {
                    i = 12;
                }
                setRelatedContactMenuVisibility(i, 0);
                shouldShowMoreMenu(number, blacklistAndCallEditFlag, true, true);
                return;
            } else if (adapter.hasWeichat()) {
                setRelatedContactMenuVisibility(10, 0);
                if ((blacklistAndCallEditFlag & 1) == 0 || (blacklistAndCallEditFlag & 2) == 0) {
                    shouldShowMoreMenu(number, blacklistAndCallEditFlag, true, false);
                    return;
                } else {
                    this.mMenuFourth.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
                    return;
                }
            } else if (adapter.hasWhatsapp()) {
                setRelatedContactMenuVisibility(12, 0);
                if ((blacklistAndCallEditFlag & 1) == 0 || (blacklistAndCallEditFlag & 2) == 0) {
                    shouldShowMoreMenu(number, blacklistAndCallEditFlag, false, true);
                    return;
                } else {
                    this.mMenuFourth.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                    return;
                }
            } else {
                if ((blacklistAndCallEditFlag & 1) != 0 && (blacklistAndCallEditFlag & 2) != 0) {
                    setMenuFouthVisibility(8);
                } else if ((blacklistAndCallEditFlag & 1) != 0) {
                    this.mMenuFourth.updateMenu(13, R.string.clickspan_edit_call, getImmersionStyleIcon(getContext(), R.drawable.bg_message_received_project));
                } else if ((blacklistAndCallEditFlag & 2) == 0) {
                    shouldShowMoreMenu(number, blacklistAndCallEditFlag, false, false);
                }
                setRelatedContactMenuVisibility(8, 0);
                return;
            }
        }
        updateMenuWithRelatedContact(adapter, blacklistAndCallEditFlag);
    }

    private int getLastUsedMenu() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("lastUsedMenu", -1);
    }

    private void updateMenuWithRelatedContact(AddWhatsAppPeopleActionBarAdapter adapter, int blacklistAndCallEditFlag) {
        int i = 12;
        boolean isChinaVersion = Contact.IS_CHINA_REGION;
        MLog.d("AB:People", "isChinaVersion : " + isChinaVersion);
        String number = adapter.getNumber();
        if (adapter.hasWeichat() && adapter.hasWhatsapp()) {
            int lastUsedMenu = getLastUsedMenu();
            if ((blacklistAndCallEditFlag & 1) == 0 || (blacklistAndCallEditFlag & 2) == 0) {
                boolean weichatInMoreMenu;
                boolean z;
                if (lastUsedMenu == 0) {
                    this.mMenuThird.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
                    weichatInMoreMenu = false;
                } else if (lastUsedMenu == 1) {
                    this.mMenuThird.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                    weichatInMoreMenu = true;
                } else if (isChinaVersion) {
                    this.mMenuThird.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
                    weichatInMoreMenu = false;
                } else {
                    this.mMenuThird.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                    weichatInMoreMenu = true;
                }
                if (!weichatInMoreMenu) {
                    i = 10;
                }
                setRelatedContactMenuVisibility(i, 0);
                setMenuThirdVisibility(0);
                if (weichatInMoreMenu) {
                    z = false;
                } else {
                    z = true;
                }
                shouldShowMoreMenu(number, blacklistAndCallEditFlag, weichatInMoreMenu, z);
            } else if (lastUsedMenu == 0) {
                setRelatedContactMenuVisibility(10, 0);
                setMenuThirdVisibility(0);
                setMenuFouthVisibility(0);
                this.mMenuThird.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
                this.mMenuFourth.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
            } else if (lastUsedMenu == 1) {
                setRelatedContactMenuVisibility(12, 0);
                setMenuThirdVisibility(0);
                setMenuFouthVisibility(0);
                this.mMenuThird.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                this.mMenuFourth.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
            } else if (lastUsedMenu == -1) {
                int i2;
                if (isChinaVersion) {
                    i2 = 10;
                } else {
                    i2 = 12;
                }
                setRelatedContactMenuVisibility(i2, 0);
                setMenuThirdVisibility(0);
                setMenuFouthVisibility(0);
                if (isChinaVersion) {
                    this.mMenuThird.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
                    this.mMenuFourth.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                    return;
                }
                this.mMenuThird.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
                this.mMenuFourth.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
            }
        } else if (adapter.hasWeichat()) {
            setRelatedContactMenuVisibility(10, 0);
            setMenuThirdVisibility(0);
            this.mMenuThird.updateMenu(10, R.string.menu_weichat, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_wechat));
            if ((blacklistAndCallEditFlag & 1) != 0 && (blacklistAndCallEditFlag & 2) != 0) {
                setMenuFouthVisibility(8);
            } else if ((blacklistAndCallEditFlag & 1) != 0) {
                this.mMenuFourth.updateMenu(13, R.string.clickspan_edit_call, getImmersionStyleIcon(getContext(), R.drawable.bg_message_received_project));
            } else if ((blacklistAndCallEditFlag & 2) == 0) {
                shouldShowMoreMenu(number, blacklistAndCallEditFlag, false, false);
            }
        } else if (adapter.hasWhatsapp()) {
            setRelatedContactMenuVisibility(12, 0);
            setMenuThirdVisibility(0);
            this.mMenuThird.updateMenu(12, R.string.menu_whatsapp, getImmersionStyleIcon(getContext(), R.drawable.mms_ic_whatsapp));
            if ((blacklistAndCallEditFlag & 1) != 0 && (blacklistAndCallEditFlag & 2) != 0) {
                setMenuFouthVisibility(8);
            } else if ((blacklistAndCallEditFlag & 1) != 0) {
                this.mMenuFourth.updateMenu(13, R.string.clickspan_edit_call, getImmersionStyleIcon(getContext(), R.drawable.bg_message_received_project));
            } else if ((blacklistAndCallEditFlag & 2) == 0) {
                shouldShowMoreMenu(number, blacklistAndCallEditFlag, false, false);
            }
        } else {
            if ((blacklistAndCallEditFlag & 1) != 0) {
                setMenuFouthVisibility(8);
            }
            if ((blacklistAndCallEditFlag & 2) != 0) {
                setMenuThirdVisibility(8);
            }
            setRelatedContactMenuVisibility(-1, 8);
        }
    }

    private void shouldShowMoreMenu(String number, int blacklistAndCallEditFlag, boolean weiChatInMoreMenu, boolean whatsappInMoreMenu) {
        boolean isBlackNumber = BlacklistCommonUtils.isNumberBlocked(number);
        boolean blacklistInMoreMenu = true;
        boolean callEditInMoreMenu = true;
        if ((blacklistAndCallEditFlag & 1) != 0) {
            blacklistInMoreMenu = false;
        }
        if ((blacklistAndCallEditFlag & 2) != 0) {
            callEditInMoreMenu = false;
        }
        shouldShowMoreMenu(isBlackNumber, blacklistInMoreMenu, callEditInMoreMenu, weiChatInMoreMenu, whatsappInMoreMenu);
    }

    private void shouldShowMoreMenu(boolean isBlackNumber, boolean blacklistInMoreMenu, boolean callEditInMoreMenu, boolean weiChatInMoreMenu, boolean whatsappInMoreMenu) {
        this.mMoreMenuItemIds = null;
        this.mMoreMenuOperationStrings = null;
        this.mMenuFourth.updateMenu(14, R.string.menu_add_rcs_more, R.drawable.rcs_more_selector);
        MLog.i("AB:People", "should show more menu isBlackNumber: " + isBlackNumber + ", blacklistInMoreMenu: " + blacklistInMoreMenu + ", callEditInMoreMenu: " + callEditInMoreMenu + ", weiChatInMoreMenu: " + weiChatInMoreMenu + ", whatsappInMoreMenu : " + whatsappInMoreMenu);
        int itemSize = 0;
        if (blacklistInMoreMenu) {
            itemSize = 1;
        }
        if (callEditInMoreMenu) {
            itemSize++;
        }
        if (whatsappInMoreMenu) {
            itemSize++;
        }
        if (weiChatInMoreMenu) {
            itemSize++;
        }
        if (itemSize < 2) {
            MLog.e("AB:People", "show more menu but has item < 2 : " + itemSize);
            return;
        }
        int index;
        this.mMoreMenuItemIds = new int[itemSize];
        this.mMoreMenuOperationStrings = new String[itemSize];
        if (whatsappInMoreMenu && weiChatInMoreMenu) {
            int i;
            boolean lastUsedWeichat = getLastUsedMenu() == 0;
            this.mMoreMenuItemIds[0] = lastUsedWeichat ? 10 : 12;
            this.mMoreMenuOperationStrings[0] = getContext().getResources().getString(lastUsedWeichat ? R.string.menu_weichat : R.string.menu_whatsapp);
            this.mMoreMenuItemIds[1] = !lastUsedWeichat ? 10 : 12;
            String[] strArr = this.mMoreMenuOperationStrings;
            index = 1 + 1;
            Resources resources = getContext().getResources();
            if (lastUsedWeichat) {
                i = R.string.menu_whatsapp;
            } else {
                i = R.string.menu_weichat;
            }
            strArr[1] = resources.getString(i);
        } else if (whatsappInMoreMenu || weiChatInMoreMenu) {
            this.mMoreMenuItemIds[0] = whatsappInMoreMenu ? 12 : 10;
            this.mMoreMenuOperationStrings[0] = getContext().getResources().getString(whatsappInMoreMenu ? R.string.menu_whatsapp : R.string.menu_weichat);
            index = 1;
        } else {
            MLog.e("AB:People", "show more menu but do not has weichat or whatsapp");
            index = 0;
        }
        if (callEditInMoreMenu) {
            this.mMoreMenuItemIds[index] = 13;
            int index2 = index + 1;
            this.mMoreMenuOperationStrings[index] = getContext().getResources().getString(R.string.clickspan_edit_call);
            index = index2;
        }
        if (blacklistInMoreMenu) {
            this.mMoreMenuItemIds[index] = isBlackNumber ? 6 : 5;
            index2 = index + 1;
            this.mMoreMenuOperationStrings[index] = getContext().getResources().getString(isBlackNumber ? R.string.menu_remove_from_blacklist : R.string.menu_blacklist);
        }
        if (this.mMoreDialog != null && this.mMoreDialog.isShowing() && blacklistInMoreMenu && this.mMoreDialog.getListView() != null) {
            TextView blackView = (TextView) this.mMoreDialog.getListView().getChildAt(this.mMoreDialog.getListView().getLastVisiblePosition());
            if (blackView != null) {
                blackView.setText(this.mMoreMenuOperationStrings[itemSize - 1]);
            }
        }
    }

    private int getImmersionStyleIcon(Context context, int resId) {
        if (ResEx.self().isUseThemeBackground(context)) {
            return resId;
        }
        int resoultId = resId;
        if (HwUiStyleUtils.isSuggestDarkStyle(context)) {
            switch (resId) {
                case R.drawable.csp_menu_collapse:
                    resoultId = R.drawable.csp_menu_collapse_dark;
                    break;
                case R.drawable.csp_menu_expand:
                    resoultId = R.drawable.csp_menu_expand_dark;
                    break;
                case R.drawable.mms_ic_add_contact:
                    resoultId = R.drawable.mms_ic_add_contact_dark;
                    break;
                case R.drawable.mms_ic_call_white:
                    resoultId = R.drawable.mms_ic_call_dark;
                    break;
                case R.drawable.mms_ic_mail_white:
                    resoultId = R.drawable.mms_ic_mail_dark;
                    break;
                case R.drawable.mms_ic_view_contact:
                    resoultId = R.drawable.mms_ic_view_contact_dark;
                    break;
                case R.drawable.mms_ic_wechat:
                    resoultId = R.drawable.mms_ic_wechat_dark;
                    break;
                case R.drawable.mms_ic_whatsapp:
                    resoultId = R.drawable.mms_ic_whatsapp_dark;
                    break;
                case R.drawable.mms_icon_blacklist:
                    resoultId = R.drawable.mms_icon_blacklist_dark;
                    break;
            }
        }
        return resoultId;
    }

    private void setRelatedContactMenuVisibility(int iconId, int visible) {
        switch (iconId) {
            case -1:
                this.mWeichatIcon.setVisibility(visible);
                this.mWhatsappIcon.setVisibility(visible);
                return;
            case 8:
                this.mWhatsappIcon.setVisibility(8);
                this.mWeichatIcon.setVisibility(8);
                return;
            case 10:
                this.mWeichatIcon.setVisibility(visible);
                if (visible == 0) {
                    this.mWhatsappIcon.setVisibility(8);
                    return;
                }
                return;
            case 12:
                this.mWhatsappIcon.setVisibility(visible);
                if (visible == 0) {
                    this.mWeichatIcon.setVisibility(8);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void setMenuFouthVisibility(int visible) {
        this.mMenuFourth.setVisibility(visible);
        this.mBlacklistMenuDivider.setVisibility(visible);
    }

    private void setMenuThirdVisibility(int visible) {
        this.mMenuThird.setVisibility(visible);
        this.mRelatedContactMenuDivider.setVisibility(visible);
    }

    private boolean isBlacklistFeatureEnable() {
        return !this.mDataAdatper.isHwMsgSender() ? BlacklistCommonUtils.isBlacklistFeatureEnable() : false;
    }

    private void setExpandAnamtion(int currentHeight, int finalHeight) {
        int totalMovedHeight = currentHeight - finalHeight;
        boolean needPlus = false;
        if (currentHeight < finalHeight) {
            totalMovedHeight = finalHeight - currentHeight;
            needPlus = true;
        }
        final int totalMovedHeightF = totalMovedHeight;
        final boolean needPlusF = needPlus;
        final int i = currentHeight;
        final int i2 = finalHeight;
        this.mActionBarExpandAnimation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation trans) {
                int translateY;
                int movedHeight = (int) (((float) totalMovedHeightF) * interpolatedTime);
                if (needPlusF) {
                    if (i + movedHeight > i2) {
                        translateY = i2;
                    } else {
                        translateY = i + movedHeight;
                    }
                } else if (i - movedHeight < i2) {
                    translateY = i2;
                } else {
                    translateY = i - movedHeight;
                }
                if (PeopleActionBar.this.mMsgListViewLayout != null) {
                    PeopleActionBar.this.mMsgListViewLayout.setTranslationY((float) translateY);
                }
            }

            public boolean willChangeBounds() {
                return false;
            }
        };
        this.mActionBarExpandAnimation.setAnimationListener(this);
        this.mActionBarExpandAnimation.setDuration(200);
        startAnimation(this.mActionBarExpandAnimation);
        if (finalHeight == this.mActionBarMaxHeight) {
            this.mIsExpanded = true;
            this.mExpandButton.setImageResource(getImmersionStyleIcon(getContext(), R.drawable.csp_menu_collapse));
            return;
        }
        this.mIsExpanded = false;
        this.mExpandButton.setImageResource(getImmersionStyleIcon(getContext(), R.drawable.csp_menu_expand));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onClick(View v) {
        if (v == this.mExpandButton) {
            if (this.mIsExpanded) {
                setExpandAnamtion(this.mActionBarMaxHeight, this.mActionBarMinHeight);
            } else {
                showOrHideMenu(true);
                if (this.mExpandListener != null) {
                    this.mExpandListener.onExpandStart();
                }
                setExpandAnamtion(this.mActionBarMinHeight, this.mActionBarMaxHeight);
                StatisticalHelper.incrementReportCount(getContext(), 2107);
            }
        } else if (this.mDataAdatper != null && this.mIsExpanded) {
            int menuId = -1;
            if (v instanceof EmuiMenuText) {
                menuId = ((EmuiMenuText) v).getMenuId();
            }
            doOperationByMenuId(menuId);
        }
    }

    private void doOperationByMenuId(int menuId) {
        switch (menuId) {
            case 1:
                StatisticalHelper.incrementReportCount(getContext(), AMapException.CODE_AMAP_CLIENT_USERID_ILLEGAL);
                this.mDataAdatper.viewPeopleInfo();
                return;
            case 2:
                StatisticalHelper.incrementReportCount(getContext(), 2110);
                this.mDataAdatper.callRecipients();
                return;
            case 3:
                this.mDataAdatper.writeEmail();
                return;
            case 4:
                StatisticalHelper.incrementReportCount(getContext(), 2108);
                this.mDataAdatper.addToContact();
                return;
            case 5:
                StatisticalHelper.incrementReportCount(getContext(), 2109);
                this.mDataAdatper.addToBlacklist(true, this.onAddToBlackList);
                return;
            case 6:
                this.mDataAdatper.addToBlacklist(false, this.onAddToBlackList);
                return;
            case 8:
                if (this.mCust != null) {
                    this.mCust.createGroupChat();
                    return;
                }
                return;
            case 10:
                StatisticalHelper.incrementReportCount(getContext(), 2142);
                this.mDataAdatper.writeWeichat();
                return;
            case 12:
                StatisticalHelper.incrementReportCount(getContext(), AMapException.CODE_AMAP_CLIENT_UPLOAD_TOO_FREQUENT);
                this.mDataAdatper.writeWhatsapp();
                return;
            case 13:
                this.mDataAdatper.editBeforeCall();
                return;
            case 14:
                StatisticalHelper.incrementReportCount(getContext(), AMapException.CODE_AMAP_CLIENT_UPLOAD_LOCATION_ERROR);
                showMoreMenu();
                return;
            default:
                MLog.e("AB:People", "unknow menuId:" + menuId);
                return;
        }
    }

    private void showMoreMenu() {
        if (this.mMoreMenuOperationStrings == null) {
            MLog.e("AB:People", "mMoreMenuOperationStrings not init");
            return;
        }
        Builder builder = new Builder(getContext());
        builder.setItems(this.mMoreMenuOperationStrings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                if (PeopleActionBar.this.mMoreMenuItemIds != null && index < PeopleActionBar.this.mMoreMenuItemIds.length) {
                    PeopleActionBar.this.doOperationByMenuId(PeopleActionBar.this.mMoreMenuItemIds[index]);
                }
            }
        });
        this.mMoreDialog = builder.create();
        this.mMoreDialog.show();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsExpanded = false;
        this.mHandler.removeMessages(1);
        WallPaperImageHelper.getInstance(getContext()).clearBlurWallpaperChangedListener(this);
        dismissPopupWindow();
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        if (animation == this.mActionBarExpandAnimation) {
            MLog.d("AB:People", "PeopleActionBar mActionBarExpandAnimation " + this.mTitleMoveDownOffset);
            clearAnimation();
            resetParaLayout();
            if (this.mExpandListener != null) {
                if (this.mIsExpanded) {
                    this.mExpandListener.onCollapsStop();
                } else {
                    this.mExpandListener.onExpandStop();
                }
            }
        }
    }

    private void resetParaLayout() {
        int translateY;
        if (this.mIsExpanded) {
            this.mSubTitle.setAlpha(FULL_ALPHA);
            this.mSubTitle2.setAlpha(FULL_ALPHA);
            this.mMenuHolder.setAlpha(FULL_ALPHA);
            translateY = this.mActionBarMaxHeight;
            updateMenu();
            this.mExpandButton.setImageResource(getImmersionStyleIcon(getContext(), R.drawable.csp_menu_collapse));
        } else {
            this.mSubTitle.setAlpha(0.0f);
            this.mSubTitle2.setAlpha(0.0f);
            this.mMenuHolder.setAlpha(0.0f);
            translateY = this.mActionBarMinHeight;
            this.mExpandButton.setImageResource(getImmersionStyleIcon(getContext(), R.drawable.csp_menu_expand));
            showOrHideMenu(false);
        }
        if (this.mCust != null) {
            this.mCust.resetParaLayoutGroup(this.mIsExpanded);
        }
        if (this.mMsgListViewLayout != null) {
            this.mMsgListViewLayout.setTranslationY((float) translateY);
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mOrientation != newConfig.orientation) {
            this.mOrientation = newConfig.orientation;
            updateActionBarMinHeight();
            if (this.mOrientation == 2) {
                this.mActionBarLandscapeMaxHeight = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height_landscape);
                this.mActionBarMaxHeight = this.mActionBarLandscapeMaxHeight;
            } else {
                this.mActionBarMaxHeight = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height);
            }
            updateMenuHolderLayout(this.mOrientation == 2);
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                PeopleActionBar.this.updateMenu();
            }
        }, 100);
    }

    public void updateMenuHolderLayout(boolean isLandscape) {
        LayoutParams params = (LayoutParams) this.mMenuHolder.getLayoutParams();
        if (isLandscape) {
            if (this.maxLine == 1) {
                params.topMargin = (int) getResources().getDimension(R.dimen.mms_people_action_bar_margintop_landscape_1);
            } else if (this.maxLine == 2) {
                params.topMargin = (int) getResources().getDimension(R.dimen.mms_people_action_bar_margintop_landscape_2);
            } else if (this.maxLine == 3) {
                params.topMargin = (int) getResources().getDimension(R.dimen.mms_people_action_bar_margintop_landscape_3);
            }
            params.width = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_width_landscape);
            params.setMarginEnd((int) getResources().getDimension(R.dimen.mms_people_action_bar_marginend_landscape));
            params.addRule(21);
            params.addRule(10);
            this.mActionBarMaxHeight = this.mActionBarLandscapeMaxHeight;
            if (!this.mIsExpanded) {
                this.mMenuHolder.setAlpha(0.0f);
            } else if (this.mMsgListViewLayout != null) {
                params.topMargin += Math.abs(this.mActionBarMaxHeight - ((int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height_landscape)));
                this.mMsgListViewLayout.setTranslationY((float) this.mActionBarMaxHeight);
                this.mMenuHolder.setAlpha(FULL_ALPHA);
                LayoutParams param = (LayoutParams) this.mTitleHolder.getLayoutParams();
                param.height = this.mActionBarMaxHeight;
                this.mTitleHolder.setLayoutParams(param);
            }
        } else {
            if (this.maxLine == 1) {
                params.topMargin = (int) getResources().getDimension(R.dimen.action_bar_menu_padding_top_1);
            } else if (this.maxLine == 2) {
                params.topMargin = (int) getResources().getDimension(R.dimen.action_bar_menu_padding_top_2);
            } else if (this.maxLine == 3) {
                params.topMargin = (int) getResources().getDimension(R.dimen.action_bar_menu_padding_top_3);
            }
            params.width = -1;
            params.setMarginStart((int) getResources().getDimension(R.dimen.mms_people_action_bar_marginstart));
            params.setMarginEnd((int) getResources().getDimension(R.dimen.mms_people_action_bar_marginend));
            this.mMenuHolder.setGravity(80);
            this.mActionBarMaxHeight = (int) getResources().getDimension(R.dimen.mms_people_action_bar_max_height);
            if (this.mIsExpanded && this.mMsgListViewLayout != null) {
                this.mMsgListViewLayout.setTranslationY((float) this.mActionBarMaxHeight);
            }
        }
        this.mMenuHolder.setLayoutParams(params);
        if (this.mCust != null) {
            this.mCust.updateGroupMenuHolderLayout(isLandscape, this.mIsExpanded);
        }
        requestLayout();
    }
}
