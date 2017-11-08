package com.android.mms.ui.twopane;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.ui.BaseConversationListFragment;
import com.android.mms.ui.BaseConversationListFragment.BaseFragmentMenu;
import com.android.mms.ui.ConversationEditor;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.NotificationList;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import java.util.List;

public class LeftPaneConversationListFragment extends BaseConversationListFragment {
    private boolean mDoOnResume = false;
    private View mFooterView = null;
    private boolean mIsAfterDelete = false;
    private boolean mIsConfigChange;
    private boolean mIsCreateMode = true;
    private long mSelectedThreadId = -1;
    private long mThreadIdToShowAfterDelete = -1;

    class LeftPaneFragmentMenu extends BaseFragmentMenu {
        LeftPaneFragmentMenu() {
            super();
        }

        protected Menu getMenuFromSubClass() {
            if (LeftPaneConversationListFragment.this.mCustomActionBar != null) {
                return LeftPaneConversationListFragment.this.mCustomActionBar.getMenu();
            }
            return null;
        }

        public void updateSplitActionbarVisivility(boolean show) {
            if (LeftPaneConversationListFragment.this.mCustomActionBar != null) {
                LeftPaneConversationListFragment.this.mCustomActionBar.setVisibility(show ? 0 : 8);
            }
        }

        public void dimissPop() {
            if (LeftPaneConversationListFragment.this.mCustomActionBar != null) {
                LeftPaneConversationListFragment.this.mCustomActionBar.dismissPopup();
            }
        }

        public void onPrepareOptionsMenu(Menu menu) {
            MLog.d("Mms_UI_CLFrag", "LeftPaneFragmentMenu onPrepareOptionsMenu...");
            boolean inSearchMode = LeftPaneConversationListFragment.this.mActionBar.getActionMode() == 3;
            View rootView = LeftPaneConversationListFragment.this.getView();
            if (rootView != null) {
                LeftPaneConversationListFragment.this.mCustomActionBar = (SplitActionBarView) rootView.findViewById(R.id.cl_menu_layout2);
            }
            if (inSearchMode) {
                if (LeftPaneConversationListFragment.this.mCustomActionBar != null) {
                    LeftPaneConversationListFragment.this.mCustomActionBar.setVisibility(8);
                }
                return;
            }
            if (LeftPaneConversationListFragment.this.mCustomActionBar != null) {
                if ((LeftPaneConversationListFragment.this.getActivity() instanceof ConversationList) && HwMessageUtils.isSplitOn() && ((ConversationList) LeftPaneConversationListFragment.this.getActivity()).isSplitState()) {
                    ((ConversationList) LeftPaneConversationListFragment.this.getActivity()).updateLeftSplitActionbarVisibility();
                } else {
                    LeftPaneConversationListFragment.this.mCustomActionBar.setVisibility(0);
                }
                LeftPaneConversationListFragment.this.mCustomActionBar.setOnCustomMenuListener(this);
                setupMenu(LeftPaneConversationListFragment.this.mCustomActionBar.getMenu());
                LeftPaneConversationListFragment.this.mCustomActionBar.refreshMenu();
            }
            LeftPaneConversationListFragment.this.mListView.onMenuPrepared();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mRootLayoutId = R.layout.left_pane_conversation_list_fragment;
        setFragmentMenu(new LeftPaneFragmentMenu());
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        this.mFooterView = LayoutInflater.from(getContext()).inflate(R.layout.blank_footer_view, this.mListView, false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.addFooterView(this.mFooterView, null, false);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mListView.setFocusable(false);
        if (this.mIsOnTop && this.mIsBeingDragged) {
            addListViewSearchHeader();
        }
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.edit_layout), (ViewStub) fragmentRootView.findViewById(R.id.search_view_with_divider));
    }

    protected void clickToSearchMode() {
        if (!this.mListView.isInEditMode()) {
            Bundle bundle = new Bundle();
            bundle.putInt("running_mode", 3);
            bundle.putLong("search_save_thread_id", this.mSelectedThreadId);
            HwBaseActivity.startMmsActivity(getActivity(), ConversationEditor.class, bundle, false);
            getActivity().finish();
        }
    }

    public boolean onBackPressed() {
        Activity act = getActivity();
        if (act == null) {
            return false;
        }
        if (this.mRunningMode == 3 && this.mSearchWrapper != null && this.mSearchWrapper.onBackPressed()) {
            MLog.d("Mms_UI_CLFrag", "Ignore backpress as SearchWrapper.isActivie");
            return true;
        } else if (!this.mListView.isInEditMode()) {
            return false;
        } else {
            this.mListView.exitEditMode();
            if (this.mRunningMode == 4) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_common_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                    public void onClick(View v) {
                        LeftPaneConversationListFragment.this.backToConversationList(LeftPaneConversationListFragment.this.getActivity());
                    }
                });
            } else if (this.mRunningMode == 5) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_hw_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                    public void onClick(View v) {
                        LeftPaneConversationListFragment.this.backToConversationList(LeftPaneConversationListFragment.this.getActivity());
                    }
                });
            } else if (!MmsConfig.isCspVersion()) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.app_label), this.mUnreadCount);
                if (this.mSearchWrapper != null) {
                    this.mSearchWrapper.setSearchStyle(1);
                }
            }
            return true;
        }
    }

    protected void invalidateOptionsMenu() {
        Activity act = getActivity();
        if (act != null) {
            act.invalidateOptionsMenu();
            if (isAdded()) {
                onPrepareOptionsMenu(this.mMenuEx.getMenu());
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mIsConfigChange = true;
        super.onConfigurationChanged(newConfig);
        viewItemAtPostion(this.mListAdapter.getCursor(), this.mSelectedThreadId);
        this.mIsConfigChange = false;
        TypedArray actionbarSizeTypedArray = getContext().obtainStyledAttributes(new int[]{16843499});
        this.mActionBar.setActionBarHeight((int) actionbarSizeTypedArray.getDimension(0, 0.0f));
        if (this.mListView.isInEditMode()) {
            ((ConversationList) getActivity()).showRightCover();
        }
        actionbarSizeTypedArray.recycle();
        if (this.mMenuEx.isPopupShowing() && newConfig.orientation == 1 && !((ConversationList) getActivity()).isSplitState()) {
            this.mMenuEx.dismissPopup();
        }
    }

    protected void backFromSearch() {
        getActivity().onBackPressed();
    }

    protected void startActivityOverrideAnimation(Intent intent) {
        if (getActivity() instanceof ConversationList) {
            HwBaseFragment fragment = new RightPaneComposeMessageFragment();
            fragment.setIntent(intent);
            ((ConversationList) getActivity()).openRightClearStack(fragment);
        }
    }

    public void viewItemAtPostion(Cursor cursor, long threadId) {
        if (cursor == null || cursor.getCount() == 0) {
            MLog.d("Mms_UI_CLFrag", "list: cursor is null");
            if (this.mIsAfterDelete) {
                ((ConversationList) getActivity()).clearRightInBackStack();
                this.mIsAfterDelete = false;
            }
        } else if (!((ConversationList) getActivity()).isSplitState() || -1 < getFirstConvFromCursor(cursor).getThreadId()) {
            boolean isSplitNow = ((ConversationList) getActivity()).isSplitState();
            boolean isNeedToCreateRightMessge = ((ConversationList) getActivity()).isNeedToCreateRightMessge();
            if (this.mIsCreateMode) {
                this.mIsCreateMode = false;
                if (!isSplitNow) {
                    return;
                }
            } else if (!this.mIsConfigChange) {
                if (this.mIsAfterDelete) {
                    this.mIsAfterDelete = false;
                }
                return;
            } else if (!(isSplitNow && isNeedToCreateRightMessge)) {
                MLog.i("Mms_UI_CLFrag", "viewItemAtPostion->no need to view item this case!");
                return;
            }
            Conversation conv = getFirstConvFromCursor(cursor);
            int numberType = conv.getNumberType();
            if ((getActivity() instanceof NotificationList) || (numberType != 1 && numberType != 2)) {
                long threadId2;
                if (threadId == -1) {
                    threadId2 = conv.getThreadId();
                } else {
                    threadId2 = threadId;
                }
                this.mSelectedThreadId = threadId2;
                if (threadId != -1) {
                    Conversation temp = Conversation.getConvsationFromId(getContext(), cursor, this.mSelectedThreadId);
                    if (temp != null) {
                        conv = temp;
                    }
                }
                ContactList contacts = conv.getRecipients();
                if (contacts.size() == 1) {
                    Contact c = (Contact) contacts.get(0);
                    if (c.isMe() || c.existsInDatabase()) {
                        AvatarCache.preLoadData(getActivity().getApplicationContext(), c.isMe(), c.getPersonId(), c);
                    } else if (c.isYpContact() && !TextUtils.isEmpty(c.getYpPhotoUri())) {
                        AvatarCache.preLoadData(getActivity().getApplicationContext(), c.getYpContactId(), c.getYpPhotoUri(), c);
                    }
                }
                if (this.mRunningMode != 3) {
                    this.mListAdapter.setSelectedPosition(this.mSelectedThreadId);
                    try {
                        gotoComposeMessageDelay(conv, false, false);
                    } catch (Exception e) {
                        this.mDoOnResume = true;
                    }
                }
                if (this.mHwCust == null || !this.mHwCust.openRcsThreadId(conv, false)) {
                    if (this.mRunningMode == 3) {
                        ((ConversationList) getActivity()).showOrHideRightCover();
                    }
                    return;
                }
                if (this.mRunningMode == 3) {
                    ((ConversationList) getActivity()).showOrHideRightCover();
                }
            }
        } else {
            if (this.mIsAfterDelete) {
                ((ConversationList) getActivity()).clearRightInBackStack();
                this.mIsAfterDelete = false;
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mDoOnResume) {
            gotoComposeMessageDelay(Conversation.getConvsationFromId(getContext(), this.mListAdapter.getCursor(), this.mSelectedThreadId), false, false);
            this.mDoOnResume = false;
        }
    }

    public void onPause() {
        super.onPause();
        ((LeftPaneFragmentMenu) this.mMenuEx).dimissPop();
    }

    private void gotoComposeMessageDelay(final Conversation conv, final boolean isHwNotification, final boolean isPreview) {
        HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
            public void run() {
                LeftPaneConversationListFragment.this.gotoComposeMessage(conv, isHwNotification, isPreview);
            }
        }, 200);
    }

    private Conversation getFirstConvFromCursor(Cursor cursor) {
        Conversation conv;
        cursor.moveToFirst();
        while (true) {
            conv = Conversation.from(getActivity(), cursor);
            int numberType = conv.getNumberType();
            if (!(getActivity() instanceof NotificationList) && ((numberType == 1 || numberType == 2) && cursor.moveToNext())) {
            }
        }
        return conv;
    }

    protected void setThreadIdToShowAfterDelete(List<Long> deleteList, boolean isAllSelected) {
        this.mThreadIdToShowAfterDelete = calcItemToShowAfterDelete(deleteList, isAllSelected);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long calcItemToShowAfterDelete(List<Long> deleteList, boolean isAllSelected) {
        if (!isAllSelected && this.mSelectedThreadId != -1 && ((ConversationList) getActivity()).isSplitState() && deleteList.contains(Long.valueOf(this.mSelectedThreadId))) {
            int i;
            List<Long> ids = Conversation.getConversationIdFromCursor(this.mListAdapter.getCursor());
            int destIndex = ids.indexOf(Long.valueOf(this.mSelectedThreadId));
            for (i = destIndex + 1; i < ids.size(); i++) {
                if (!deleteList.contains(ids.get(i))) {
                    return ((Long) ids.get(i)).longValue();
                }
            }
            for (i = destIndex - 1; i >= 0; i--) {
                if (!deleteList.contains(ids.get(i))) {
                    return ((Long) ids.get(i)).longValue();
                }
            }
        }
        return -1;
    }

    protected void openConversationWhenSplit(Cursor cursor) {
        long searchThreadId = -1;
        if (this.mThreadIdToShowAfterDelete == -1) {
            if (getIntent() != null) {
                searchThreadId = getIntent().getLongExtra("search_save_thread_id", -1);
            }
            viewItemAtPostion(cursor, searchThreadId);
            this.mIsAfterDelete = false;
            return;
        }
        Conversation conv = Conversation.getConvsationFromId(getActivity(), cursor, this.mThreadIdToShowAfterDelete);
        setSelected(conv);
        gotoComposeMessage(conv, false, false);
        this.mThreadIdToShowAfterDelete = -1;
        this.mIsAfterDelete = false;
    }

    protected boolean isNeedToInteruptOpenConv(Conversation conv) {
        if (!(getActivity() instanceof ConversationList) || !((ConversationList) getActivity()).isSplitState()) {
            return false;
        }
        if (this.mSelectedThreadId == conv.getThreadId()) {
            return true;
        }
        return false;
    }

    public void setSelected(Conversation conv) {
        this.mSelectedThreadId = conv == null ? -1 : conv.getThreadId();
        this.mListAdapter.setSelectedPosition(this.mSelectedThreadId);
    }

    public void setSelectedThread(long threadId) {
        this.mListAdapter.setSelectedPosition(threadId);
    }

    protected void setActivityAnimation(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }

    protected void startNewMessageActivity(Intent intent) {
        if (getActivity() instanceof ConversationList) {
            HwBaseFragment fragment = new RightPaneComposeMessageFragment();
            fragment.setIntent(intent);
            ((ConversationList) getActivity()).openRightClearStack(fragment);
            ((ConversationList) getActivity()).showLeftCover();
        }
    }

    protected void resetThreadIdAfterDelete() {
        this.mThreadIdToShowAfterDelete = -1;
    }

    public void showNextConversation() {
        long id = this.mListAdapter.getThreadIdToShow();
        if (-1 == id) {
            ((ConversationList) getActivity()).clearRightInBackStack();
            return;
        }
        Conversation conv = Conversation.getConvsationFromId(getActivity(), this.mListAdapter.getCursor(), id);
        setSelected(conv);
        gotoComposeMessage(conv, false, false);
    }

    public void resetSelectedItem() {
        this.mSelectedThreadId = -1;
    }

    public void backToListWhenSplit() {
        Cursor cursor = this.mListAdapter.getCursor();
        if (cursor == null || cursor.getCount() == 0) {
            ((ConversationList) getActivity()).clearRightInBackStack();
            return;
        }
        Conversation conv;
        if (this.mSelectedThreadId != -1) {
            conv = Conversation.getConvsationFromId(getContext(), cursor, this.mSelectedThreadId);
        } else {
            conv = getFirstConvFromCursor(cursor);
        }
        setSelected(conv);
        gotoComposeMessage(conv, false, false);
    }

    public void updateSplitActionbar(boolean show) {
        ((LeftPaneFragmentMenu) this.mMenuEx).updateSplitActionbarVisivility(show);
    }

    public void setIsAfterDelete(boolean isAfterDelete) {
        this.mIsAfterDelete = isAfterDelete;
    }
}
