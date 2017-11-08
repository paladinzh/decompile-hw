package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiListView_V3;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwAlertDialog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.NoMessageView;
import com.huawei.mms.util.ProviderCallUtils;
import com.huawei.mms.util.SelectionChangedListener;

public class RecyclerSmsListFragment extends HwBaseFragment implements OnItemClickListener {
    private static final String[] PROJECTION_TRASH_SMS = new String[]{"_id", "address", "body", "date_delete"};
    CryptoReclerSmsListItem cryptoReclerSmsListItem;
    private EmuiActionBar mActionBar;
    private EditHandler mDeleteOrRestoreHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedIds, boolean isAllSelected) {
            int length = selectedIds.length;
            if (!isAllSelected) {
                StringBuilder idsBuilder = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    idsBuilder.append(selectedIds[i]);
                    if (i != length - 1) {
                        idsBuilder.append(",");
                    }
                }
                if (RecyclerSmsListFragment.this.mOperationType == 0) {
                    ProviderCallUtils.deleteSelectMessages(RecyclerSmsListFragment.this.getContext(), idsBuilder.toString());
                } else {
                    ProviderCallUtils.restoreSelectMessages(RecyclerSmsListFragment.this.getContext(), idsBuilder.toString(), true);
                }
            } else if (RecyclerSmsListFragment.this.mOperationType == 0) {
                ProviderCallUtils.cleanTrashBox(RecyclerSmsListFragment.this.getContext(), -1);
            } else if (1 == RecyclerSmsListFragment.this.mOperationType) {
                ProviderCallUtils.restoreTrashBoxMessages(RecyclerSmsListFragment.this.getContext(), length);
            }
            return length;
        }
    };
    private boolean mHasNoMessage;
    private boolean mIsAllSelected = false;
    private RecyclerSmsListAdapter mListAdapter;
    private EmuiListView_V3 mListView;
    private MenuEx mMenuEx;
    private NoMessageView mNoSmsTrashView;
    private int mOperationType = -1;
    private TrashSmsQueryHandler mQueryHandler;
    private AlertDialog mSmsDetailsDialog;
    private final ContentObserver mTrashSmsChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            if (RecyclerSmsListFragment.this.mQueryHandler != null) {
                RecyclerSmsListFragment.this.mQueryHandler.startMsgListQuery(20150528);
            }
        }
    };

    private class DetailsDlgListener implements OnClickListener, OnCancelListener {
        private long mMsgId;

        private DetailsDlgListener() {
            this.mMsgId = -1;
        }

        public void setMessageId(long id) {
            this.mMsgId = id;
        }

        public void onClick(View view) {
            RecyclerSmsListFragment.this.dismissDetailsDialog();
            switch (view.getId()) {
                case R.id.btn_restore_trash_sms:
                    ProviderCallUtils.restoreSelectMessages(RecyclerSmsListFragment.this.getContext(), String.valueOf(this.mMsgId), false);
                    return;
                case R.id.btn_delete_trash_message:
                    RecyclerSmsListFragment.this.confirmDeleteDialog(new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ProviderCallUtils.deleteSelectMessages(RecyclerSmsListFragment.this.getContext(), String.valueOf(DetailsDlgListener.this.mMsgId));
                        }
                    }, RecyclerSmsListFragment.this.getResources().getString(R.string.delete_the_trash_message));
                    return;
                default:
                    return;
            }
        }

        public void onCancel(DialogInterface dialog) {
            RecyclerSmsListFragment.this.dismissDetailsDialog();
        }
    }

    private class RecyclerSmsListListener implements EmuiListViewListener {
        private RecyclerSmsListListener() {
        }

        public void onEnterEditMode() {
        }

        public void onExitEditMode() {
        }

        public EditHandler getHandler(int mode) {
            return null;
        }

        public String getHintText(int mode, int count) {
            return null;
        }

        public int getHintColor(int mode, int count) {
            return 0;
        }
    }

    private class EmuiListViewListenerV3 extends RecyclerSmsListListener implements SelectionChangedListener, OnItemLongClickListener, OnClickListener {
        private EmuiListViewListenerV3() {
            super();
        }

        public void onClick(View view) {
            RecyclerSmsListFragment.this.mListView.exitEditMode();
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (MmsConfig.isSmsEnabled(RecyclerSmsListFragment.this.getContext())) {
                RecyclerSmsListFragment.this.mListView.enterEditMode(1);
                RecyclerSmsListFragment.this.mListView.setSeleceted(id, true);
                RecyclerSmsListFragment.this.mListAdapter.notifyDataSetChanged();
            }
            return false;
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z;
            boolean z2 = true;
            RecyclerSmsListFragment recyclerSmsListFragment = RecyclerSmsListFragment.this;
            if (selectedSize != totalSize || selectedSize <= 0) {
                z = false;
            } else {
                z = true;
            }
            recyclerSmsListFragment.mIsAllSelected = z;
            RecyclerSmsListFragment.this.mMenuEx.setAllChecked(RecyclerSmsListFragment.this.mIsAllSelected, RecyclerSmsListFragment.this.isInLandscape());
            MenuEx -get7 = RecyclerSmsListFragment.this.mMenuEx;
            if (selectedSize > 0) {
                z = true;
            } else {
                z = false;
            }
            -get7.setItemEnabled(278925315, z);
            MenuEx -get72 = RecyclerSmsListFragment.this.mMenuEx;
            if (selectedSize <= 0) {
                z2 = false;
            }
            -get72.setItemEnabled(278925348, z2);
            RecyclerSmsListFragment.this.mActionBar.setUseSelecteSize(selectedSize);
        }

        public void onEnterEditMode() {
            RecyclerSmsListFragment.this.mActionBar.enterEditMode(this);
            RecyclerSmsListFragment.this.getActivity().invalidateOptionsMenu();
        }

        public void onExitEditMode() {
            RecyclerSmsListFragment.this.mActionBar.exitEditMode();
            RecyclerSmsListFragment.this.mActionBar.setTitle(RecyclerSmsListFragment.this.getResources().getString(R.string.sms_recovery_title));
            RecyclerSmsListFragment.this.getActivity().invalidateOptionsMenu();
        }
    }

    private class MenuEx extends EmuiMenu implements OnCreateContextMenuListener, OnMenuItemClickListener {
        public MenuEx(Menu optionMenu) {
            super(optionMenu);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onMenuItemClick(MenuItem menuItem) {
            return false;
        }

        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        }

        public boolean onCreateOptionsMenu() {
            boolean z = false;
            if (2 != RecyclerSmsListFragment.this.mActionBar.getActionMode()) {
                boolean isLandscape;
                if (RecyclerSmsListFragment.this.getResources().getConfiguration().orientation == 2) {
                    isLandscape = true;
                } else {
                    isLandscape = false;
                }
                addMenu(278925347, R.string.clean_trash_messages_box, getDrawableId(278925315, isLandscape));
                if (!RecyclerSmsListFragment.this.mHasNoMessage) {
                    z = true;
                }
                setItemEnabled(278925347, z);
            }
            return true;
        }

        public void onPrepareOptionsMenu(Menu menu) {
            setupMenu();
            RecyclerSmsListFragment.this.mListView.onMenuPrepared();
        }

        private void setupMenu() {
            Menu actionMenu = RecyclerSmsListFragment.this.mActionBar.getActionMenu();
            if (actionMenu != null) {
                setOptionMenu(actionMenu);
                clear();
                boolean inEditMode = 2 == RecyclerSmsListFragment.this.mActionBar.getActionMode();
                boolean isLandscape = RecyclerSmsListFragment.this.isInLandscape();
                if (inEditMode) {
                    addMenu(278925348, R.string.restore_select_sms_menu, getDrawableId(278925339, isLandscape));
                    addMenu(278925315, R.string.delete, getDrawableId(278925315, isLandscape));
                    addMenu(278925313, R.string.menu_select_all, getDrawableId(278925313, isLandscape));
                }
            }
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            boolean z = false;
            switch (item.getItemId()) {
                case 16908332:
                    RecyclerSmsListFragment.this.onBackPressed();
                    break;
                case 278925313:
                    EmuiListView_V3 -get6 = RecyclerSmsListFragment.this.mListView;
                    if (!RecyclerSmsListFragment.this.mIsAllSelected) {
                        z = true;
                    }
                    -get6.setAllSelected(z);
                    break;
                case 278925315:
                    int selectSize = RecyclerSmsListFragment.this.mListView.getSelectedCount();
                    RecyclerSmsListFragment.this.confirmDeleteDialog(new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RecyclerSmsListFragment.this.setType(0);
                            RecyclerSmsListFragment.this.mListView.doOperation(RecyclerSmsListFragment.this.mDeleteOrRestoreHandler);
                            RecyclerSmsListFragment.this.mListView.exitEditMode();
                        }
                    }, RecyclerSmsListFragment.this.getResources().getQuantityString(R.plurals.delete_select_trash_messages, selectSize, new Object[]{Integer.valueOf(selectSize)}));
                    break;
                case 278925347:
                    RecyclerSmsListFragment.this.confirmCleanTrashBox();
                    break;
                case 278925348:
                    RecyclerSmsListFragment.this.setType(1);
                    RecyclerSmsListFragment.this.mListView.doOperation(RecyclerSmsListFragment.this.mDeleteOrRestoreHandler);
                    RecyclerSmsListFragment.this.mListView.exitEditMode();
                    break;
            }
            return true;
        }
    }

    private final class TrashSmsQueryHandler extends AsyncQueryHandler {
        public TrashSmsQueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            boolean z = false;
            if (cursor != null) {
                RecyclerSmsListFragment recyclerSmsListFragment = RecyclerSmsListFragment.this;
                if (cursor.getCount() == 0) {
                    z = true;
                }
                recyclerSmsListFragment.mHasNoMessage = z;
                switch (token) {
                    case 20150528:
                        RecyclerSmsListFragment.this.mListAdapter.changeCursor(cursor);
                        RecyclerSmsListFragment.this.getActivity().invalidateOptionsMenu();
                        break;
                }
                RecyclerSmsListFragment.this.updateNoMessageViewState(RecyclerSmsListFragment.this.mHasNoMessage);
            }
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
        }

        public void startMsgListQuery(int token) {
            cancelOperation(token);
            try {
                startQuery(token, null, ProviderCallUtils.URI_TRASH_SMS, RecyclerSmsListFragment.PROJECTION_TRASH_SMS, null, null, null);
            } catch (Exception e) {
                Log.e("mms/recyclersmslist", "startMsgListQuery:: occur sqlite exception: " + e);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_sms_list, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mQueryHandler = new TrashSmsQueryHandler(getActivity().getContentResolver());
        initViews();
        this.cryptoReclerSmsListItem = new CryptoReclerSmsListItem();
    }

    private void initViews() {
        this.mListView = (EmuiListView_V3) getView().findViewById(R.id.emui_list_view);
        this.mListAdapter = new RecyclerSmsListAdapter(getContext(), null, this.mListView, R.layout.recycler_sms_list_item);
        this.mListView.setAdapter(this.mListAdapter);
        this.mListView.setVisibility(0);
        EmuiListViewListenerV3 listener = new EmuiListViewListenerV3();
        this.mListView.setListViewListener(listener);
        this.mListView.setSelectionChangeLisenter(listener);
        this.mListView.setOnItemLongClickListener(listener);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setClickable(true);
        this.mMenuEx = new MenuEx(null);
        this.mMenuEx.setContext(getContext());
        this.mActionBar = new EmuiActionBar(getActivity());
        this.mActionBar.setTitle(getResources().getString(R.string.sms_recovery_title));
        this.mListAdapter.setActivityViewTime(System.currentTimeMillis());
    }

    private void updateNoMessageViewState(boolean noMessage) {
        if (noMessage) {
            if (this.mNoSmsTrashView == null) {
                ViewStub stub = (ViewStub) getView().findViewById(R.id.no_trash_sms);
                if (stub == null) {
                    Log.e("mms/recyclersmslist", "updateNoMessageViewState:: view stub and view are null!");
                    return;
                } else {
                    this.mNoSmsTrashView = (NoMessageView) stub.inflate();
                    this.mNoSmsTrashView.setViewType(3);
                }
            }
            this.mNoSmsTrashView.setVisibility(0, getActivity().isInMultiWindowMode());
        } else if (this.mNoSmsTrashView != null) {
            this.mNoSmsTrashView.setVisibility(8, getActivity().isInMultiWindowMode());
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mMenuEx.onPrepareOptionsMenu(menu);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenuEx.setOptionMenu(menu).onCreateOptionsMenu();
    }

    public void onStart() {
        super.onStart();
        this.mListAdapter.setActivityViewTime(System.currentTimeMillis());
        if (this.mQueryHandler != null) {
            this.mQueryHandler.startMsgListQuery(20150528);
        }
        registerContentChangeListener();
    }

    public void onStop() {
        super.onStop();
        if (this.mListAdapter != null) {
            this.mListAdapter.changeCursor(null);
        }
        unregisterContentChangeListener();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        dismissDetailsDialog();
    }

    public boolean onBackPressed() {
        if (!this.mListView.isInEditMode()) {
            return false;
        }
        this.mListView.exitEditMode();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        View itemView = view.findViewById(R.id.mms_animation_list_item_view);
        if (itemView instanceof RecyclerSmsListItem) {
            showTrashSmsInfo((RecyclerSmsListItem) itemView);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isDetached()) {
            Log.w("mms/recyclersmslist", "onConfigurationChanged::activity is finishing, return");
            return;
        }
        this.mMenuEx.onPrepareOptionsMenu(null);
        this.mMenuEx.onCreateOptionsMenu();
    }

    private void showTrashSmsInfo(RecyclerSmsListItem item) {
        if (this.mSmsDetailsDialog == null || !this.mSmsDetailsDialog.isShowing()) {
            View layout = getActivity().getLayoutInflater().inflate(R.layout.recycler_sms_details_dialog, null);
            this.mSmsDetailsDialog = new Builder(getContext()).create();
            this.mSmsDetailsDialog.setView(layout);
            initDialogInfo(item, layout);
            this.mSmsDetailsDialog.show();
            return;
        }
        Log.w("mms/recyclersmslist", "showTrashSmsInfo:: the previous dialog is not dismiss!");
    }

    private void dismissDetailsDialog() {
        if (this.mSmsDetailsDialog != null) {
            this.mSmsDetailsDialog.dismiss();
            this.mSmsDetailsDialog = null;
        }
    }

    private void initDialogInfo(RecyclerSmsListItem item, View layout) {
        if (this.cryptoReclerSmsListItem != null) {
            TextView view = (TextView) layout.findViewById(R.id.trash_sms_body);
            String resultValue = this.cryptoReclerSmsListItem.checkForCryptoMessage(item, item.getSmsBodyText(), layout);
            view.setMovementMethod(ScrollingMovementMethod.getInstance());
            view.setText(resultValue);
            this.mSmsDetailsDialog.setTitle(item.getSmsAddress().getNameAndNumber());
            Button btnRestoreSms = (Button) layout.findViewById(R.id.btn_restore_trash_sms);
            Button btnDeleteSms = (Button) layout.findViewById(R.id.btn_delete_trash_message);
            Button btnCancel = (Button) layout.findViewById(R.id.btn_cancel_trash_message);
            DetailsDlgListener listener = new DetailsDlgListener();
            listener.setMessageId(item.getSmsMsgId());
            btnRestoreSms.setOnClickListener(listener);
            btnDeleteSms.setOnClickListener(listener);
            btnCancel.setOnClickListener(listener);
        }
    }

    private boolean confirmCleanTrashBox() {
        Builder builder = new Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(R.string.clean_trash_messages_box);
        builder.setMessage(R.string.delete_all_trash_messages);
        builder.setPositiveButton(R.string.clean_trash_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ProviderCallUtils.cleanTrashBox(RecyclerSmsListFragment.this.getContext(), -1);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        AlertDialog alertDialog = builder.show();
        MessageUtils.setButtonTextColor(alertDialog, -1, getResources().getColor(R.color.mms_unread_text_color));
        alertDialog.setCanceledOnTouchOutside(true);
        return false;
    }

    private void confirmDeleteDialog(DialogInterface.OnClickListener l, String message) {
        new HwAlertDialog(getContext()).setCancelable(true).setTitle(R.string.delete).setMessage(message).setPositiveButton(R.string.delete, l).setNegativeButton(R.string.no, null).show();
    }

    public void setType(int type) {
        this.mOperationType = type;
    }

    private void registerContentChangeListener() {
        getActivity().getContentResolver().registerContentObserver(ProviderCallUtils.URI_TRASH_SMS, true, this.mTrashSmsChangeObserver);
    }

    private void unregisterContentChangeListener() {
        getActivity().getContentResolver().unregisterContentObserver(this.mTrashSmsChangeObserver);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mNoSmsTrashView != null) {
            this.mNoSmsTrashView.setInMultiWindowMode(isInMultiWindowMode);
        }
    }
}
