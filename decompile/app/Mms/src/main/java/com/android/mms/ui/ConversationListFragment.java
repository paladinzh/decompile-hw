package com.android.mms.ui;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.BaseConversationListFragment.BaseFragmentMenu;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.EmuiActionBar.OnDoubleClicklistener;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.util.StatisticalHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ConversationListFragment extends BaseConversationListFragment {
    private EmuiActionBar mEmuiActionBar;
    private int mFirstVisiblePosition;
    private View mFooterView = null;
    private int mHeaderViewsCount;
    private int mLastVisiblePosition;
    private ArrayList<Integer> mUnReadMsg = null;

    class FragmentMenu extends BaseFragmentMenu {
        FragmentMenu() {
            super();
        }

        protected Menu getMenuFromSubClass() {
            return this.mOptionMenu;
        }

        public void onPrepareOptionsMenu(Menu menu) {
            MLog.d("Mms_UI_CLFrag", "FragmentMenu onPrepareOptionsMenu...");
            boolean inEditMode = ConversationListFragment.this.mActionBar.getActionMode() == 2;
            boolean inSearchMode = ConversationListFragment.this.mActionBar.getActionMode() == 3;
            Menu rightOne = menu;
            if (inSearchMode || inEditMode) {
                rightOne = ConversationListFragment.this.mActionBar.getActionMenu();
            }
            if (inSearchMode) {
                if (rightOne != null) {
                    rightOne.clear();
                }
                if (menu != null) {
                    menu.clear();
                }
                return;
            }
            menu = rightOne;
            if (ConversationListFragment.this.checkIsAdded()) {
                View rootView = ConversationListFragment.this.getView();
                if (rootView != null) {
                    ConversationListFragment.this.mCustomActionBar = (SplitActionBarView) rootView.findViewById(R.id.cl_menu_layout2);
                }
                if (ConversationListFragment.this.mCustomActionBar != null) {
                    ConversationListFragment.this.mCustomActionBar.setVisibility(8);
                }
                if (menu != null) {
                    setupMenu(menu);
                }
                ConversationListFragment.this.mListView.onMenuPrepared();
            }
        }
    }

    private static class TempReadMsgComparator implements Comparator<Integer>, Serializable {
        private static final long serialVersionUID = 1;

        private TempReadMsgComparator() {
        }

        public int compare(Integer position1, Integer position2) {
            if (position1.intValue() > position2.intValue()) {
                return 1;
            }
            if (position1.intValue() < position2.intValue()) {
                return -1;
            }
            return 0;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mRootLayoutId = R.layout.conversation_list_fragment;
        setFragmentMenu(new FragmentMenu());
        this.mUnReadMsg = new ArrayList();
        super.onCreate(savedInstanceState);
    }

    void enterSearchMode() {
        this.mEmuiActionBar.setDisplayShowTitleEnabled(Boolean.valueOf(true));
        this.mEmuiActionBar.setDisplayShowCustomEnabled(Boolean.valueOf(false));
        super.enterSearchMode();
    }

    protected void invalidateOptionsMenu() {
        Activity act = getActivity();
        if (act != null) {
            act.invalidateOptionsMenu();
            if (!isInLandscape() && isAdded()) {
                onPrepareOptionsMenu(this.mMenuEx.getMenu());
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        this.mFooterView = LayoutInflater.from(getContext()).inflate(R.layout.blank_footer_view, this.mListView, false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
        if (rootView != null) {
            rootView.setFocusable(false);
            rootView.setFocusableInTouchMode(false);
            ((ViewGroup) rootView).setDescendantFocusability(393216);
        }
        return rootView;
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        this.mEmuiActionBar = new EmuiActionBar(getActivity());
        this.mActionBar = this.mEmuiActionBar;
        initDCActionBar();
        return this.mActionBar;
    }

    protected void clickToSearchMode() {
        if (!this.mListView.isInEditMode()) {
            Bundle bundle = new Bundle();
            bundle.putInt("running_mode", 3);
            HwBaseActivity.startMmsActivity(getActivity(), ConversationEditor.class, bundle, false);
        }
    }

    public boolean onBackPressed() {
        MLog.d("Mms_UI_CLFrag", "onBackPressed");
        Activity act = getActivity();
        if (act == null) {
            return false;
        }
        if (this.mRunningMode == 2 || this.mRunningMode == 6) {
            finishSelf(true);
            return false;
        } else if (this.mRunningMode == 3) {
            if (this.mSearchWrapper == null || !this.mSearchWrapper.onBackPressed()) {
                finishSelf(true);
                return false;
            }
            MLog.d("Mms_UI_CLFrag", "Ignore backpress as SearchWrapper.isActivie");
            return true;
        } else if (this.mListView.isInEditMode()) {
            this.mListView.exitEditMode();
            if (this.mRunningMode == 4) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_common_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                    public void onClick(View v) {
                        ConversationListFragment.this.backToConversationList(ConversationListFragment.this.getActivity());
                    }
                });
            } else if (this.mRunningMode == 5) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_hw_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                    public void onClick(View v) {
                        ConversationListFragment.this.backToConversationList(ConversationListFragment.this.getActivity());
                    }
                });
            } else if (!MmsConfig.isCspVersion()) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.app_label), this.mUnreadCount);
                if (this.mSearchWrapper != null) {
                    this.mSearchWrapper.setSearchStyle(1);
                }
            }
            return true;
        } else if (this.mRunningMode != 0 && this.mRunningMode != -1) {
            return false;
        } else {
            if (!act.isTaskRoot()) {
                return super.onBackPressed();
            }
            if (act.moveTaskToBack(false)) {
                return true;
            }
            return super.onBackPressed();
        }
    }

    private void initUnReadMessages() {
        this.mUnReadMsg.clear();
        Cursor cursor = this.mListAdapter.getCursor();
        if (cursor == null) {
            MLog.d("Mms_UI_CLFrag", "double click actionbar initUnreadMessages cursor == null");
            return;
        }
        int pos = cursor.getPosition();
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(11) > 0) {
                    this.mUnReadMsg.add(Integer.valueOf(cursor.getPosition()));
                }
            } while (cursor.moveToNext());
        }
        cursor.moveToPosition(pos);
        MLog.d("Mms_UI_CLFrag", "double click actionbar mUnReadMsg.size() = " + this.mUnReadMsg.size());
    }

    private void initDCActionBar() {
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.mms_actionbar_title, new LinearLayout(getContext()), false);
        TextView titleView = (TextView) customView.findViewById(R.id.mms_actionbar_title);
        ImageView backView = (ImageView) customView.findViewById(R.id.bt_back);
        int screenHeight = MessageUtils.getScreenHeight(getActivity());
        int screenWidth = MessageUtils.getScreenWidth(getActivity());
        LayoutParams params = new LayoutParams(screenHeight > screenWidth ? screenHeight : screenWidth, -1, 8388611);
        final Activity activity = getActivity();
        if (activity instanceof NotificationList) {
            backView.setVisibility(0);
            backView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        } else {
            backView.setVisibility(8);
        }
        this.mEmuiActionBar.setDisplayShowHomeEnabled(Boolean.valueOf(false));
        this.mEmuiActionBar.setDisplayShowCustomEnabled(Boolean.valueOf(true));
        this.mEmuiActionBar.setCustomView(customView, titleView, params);
        this.mEmuiActionBar.setCustomTitle("initTitle");
        this.mEmuiActionBar.setDisplayShowTitleEnabled(Boolean.valueOf(false));
        this.mEmuiActionBar.setOnDoubleClickListener(new OnDoubleClicklistener() {
            public void onDoubleClick() {
                StatisticalHelper.incrementReportCount(MmsApp.getApplication().getApplicationContext(), 2221);
                ConversationListFragment.this.mHeaderViewsCount = ConversationListFragment.this.mListView.getHeaderViewsCount();
                ConversationListFragment.this.mFirstVisiblePosition = ConversationListFragment.this.mListView.getFirstVisiblePosition();
                ConversationListFragment.this.mLastVisiblePosition = ConversationListFragment.this.mListView.getLastVisiblePosition();
                MLog.d("Mms_UI_CLFrag", "double click actionbar mHeaderViewsCount = " + ConversationListFragment.this.mHeaderViewsCount);
                MLog.d("Mms_UI_CLFrag", "double click actionbar mFirstVisiblePosition = " + ConversationListFragment.this.mFirstVisiblePosition);
                MLog.d("Mms_UI_CLFrag", "double click actionbar mLastVisiblePosition = " + ConversationListFragment.this.mLastVisiblePosition);
                if (ConversationListFragment.this.isItemFullList()) {
                    ConversationListFragment.this.initUnReadMessages();
                    MLog.d("Mms_UI_CLFrag", "double click actionbar mUnReadMsg.size() = " + ConversationListFragment.this.mUnReadMsg.size());
                    int targetPosition = ConversationListFragment.this.mHeaderViewsCount;
                    int mFocusPosition = 0;
                    if (ConversationListFragment.this.mUnReadMsg.size() > 0) {
                        if (ConversationListFragment.this.isScrollToEnd()) {
                            mFocusPosition = ((Integer) ConversationListFragment.this.mUnReadMsg.get(0)).intValue();
                        } else {
                            mFocusPosition = ConversationListFragment.this.getPosition((ConversationListFragment.this.mFirstVisiblePosition + 1) - ConversationListFragment.this.mHeaderViewsCount);
                        }
                    }
                    ConversationListFragment.this.scrollToUnReadMessage(ConversationListFragment.this.adjustTargetPosition(targetPosition + mFocusPosition));
                    return;
                }
                MLog.d("Mms_UI_CLFrag", "double click actionbar isItemFullList false return");
            }
        });
    }

    private boolean isScrollToEnd() {
        if (this.mLastVisiblePosition != this.mListView.getCount() - 1) {
            MLog.d("Mms_UI_CLFrag", "double click actionbar isScrollToEnd false");
            return false;
        }
        int[] loc = new int[2];
        this.mListView.getLocationOnScreen(loc);
        int listViewEndPosition = this.mListView.getHeight() + loc[1];
        View listItem = this.mListView.getChildAt(this.mLastVisiblePosition - this.mFirstVisiblePosition);
        listItem.measure(0, 0);
        Rect listRect = new Rect();
        listItem.getBoundsOnScreen(listRect);
        if (listRect.bottom == listViewEndPosition) {
            MLog.d("Mms_UI_CLFrag", "double click actionbar isScrollToEnd true");
            return true;
        }
        MLog.d("Mms_UI_CLFrag", "double click actionbar isScrollToEnd false");
        return false;
    }

    private boolean isItemFullList() {
        int listCount = this.mListAdapter.getCount();
        if (this.mListAdapter.getCount() == 0) {
            return false;
        }
        View listItem = this.mListAdapter.getView(0, null, this.mListView);
        listItem.measure(0, 0);
        if (((double) (((int) getResources().getDimension(R.dimen.search_header_height)) * this.mHeaderViewsCount)) + (((double) listCount) * ((double) listItem.getMeasuredHeight())) >= ((double) this.mListView.getHeight())) {
            return true;
        }
        return false;
    }

    private int getPosition(int target) {
        int position = this.mUnReadMsg.indexOf(Integer.valueOf(target));
        if (position < 0) {
            ArrayList<Integer> tempReadMsg = new ArrayList();
            tempReadMsg.addAll(this.mUnReadMsg);
            tempReadMsg.add(Integer.valueOf(target));
            Collections.sort(tempReadMsg, new TempReadMsgComparator());
            int tempPosition = tempReadMsg.indexOf(Integer.valueOf(target));
            position = tempPosition <= this.mUnReadMsg.size() + -1 ? tempPosition : 0;
        }
        return ((Integer) this.mUnReadMsg.get(position)).intValue();
    }

    private int adjustTargetPosition(int targetPosition) {
        MLog.d("Mms_UI_CLFrag", "double click actionbar adjustTargetPosition  targetPosition= " + targetPosition);
        double listViewHeight = (double) this.mListView.getHeight();
        View listItem = this.mListAdapter.getView(0, null, this.mListView);
        listItem.measure(0, 0);
        if (((double) targetPosition) <= ((double) this.mListView.getCount()) - Math.ceil(listViewHeight / ((double) listItem.getMeasuredHeight()))) {
            return targetPosition;
        }
        MLog.d("Mms_UI_CLFrag", "double click actionbar adjustTargetPosition  done ");
        return this.mListView.getCount() - 1;
    }

    private void scrollToUnReadMessage(int targetPosition) {
        MLog.d("Mms_UI_CLFrag", "double click actionbar scrollToUnReadMessage  targetPosition= " + targetPosition);
        if (targetPosition >= this.mListView.getCount() - 1) {
            this.mListView.smoothScrollToPosition(this.mListView.getCount() - 1);
        } else {
            this.mListView.smoothScrollToPositionFromTop(targetPosition, 0, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE);
        }
        if (this.mSearchHeaderView != null && this.mSearchHeaderView.getTranslationY() >= 0.0f) {
            translateViewInVariTime(this.mSearchHeaderView, (float) (-this.mSearchHeaderView.getHeight()), false);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFooterViewHeight(newConfig);
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null) {
            boolean isLandscape = newConfig == null ? 2 == getResources().getConfiguration().orientation : newConfig.orientation == 2;
            ViewGroup.LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!isLandscape || isInMultiWindowMode()) ? (int) getResources().getDimension(R.dimen.toolbar_footer_height) : 0;
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        updateFooterViewHeight(getResources().getConfiguration());
    }
}
