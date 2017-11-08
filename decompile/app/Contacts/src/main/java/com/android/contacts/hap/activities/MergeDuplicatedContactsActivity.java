package com.android.contacts.hap.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.GeoUtil;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.util.MergeContacts;
import com.android.contacts.hap.util.MergeContacts.MergeFinishNotification;
import com.android.contacts.hap.util.MergeModel;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import huawei.android.app.HwProgressDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MergeDuplicatedContactsActivity extends Activity implements OnTouchListener {
    private boolean IdleState = true;
    private final OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (MergeDuplicatedContactsActivity.this.mExactMergeProgress == null || !MergeDuplicatedContactsActivity.this.mExactMergeProgress.isShowing()) {
                int checkPostion = ((Integer) buttonView.getTag(R.id.CURSOR_POSITION)).intValue();
                ArrayList<Long> checkData = (ArrayList) buttonView.getTag(R.id.CURSOR_DATA);
                if (checkData != null) {
                    ((MergeModel) MergeDuplicatedContactsActivity.this.mMergeModelList.get(checkPostion)).setSelected(buttonView.isChecked());
                    MergeDuplicatedContactsActivity mergeDuplicatedContactsActivity;
                    if (buttonView.isChecked()) {
                        if (MergeDuplicatedContactsActivity.this.finalIds.add(checkData)) {
                            mergeDuplicatedContactsActivity = MergeDuplicatedContactsActivity.this;
                            mergeDuplicatedContactsActivity.mTotalMergedContacts = mergeDuplicatedContactsActivity.mTotalMergedContacts + 1;
                        }
                    } else if (MergeDuplicatedContactsActivity.this.finalIds.remove(checkData)) {
                        mergeDuplicatedContactsActivity = MergeDuplicatedContactsActivity.this;
                        mergeDuplicatedContactsActivity.mTotalMergedContacts = mergeDuplicatedContactsActivity.mTotalMergedContacts - 1;
                    }
                } else if (MergeDuplicatedContactsActivity.this.mAllSelected) {
                    MergeDuplicatedContactsActivity.this.finalIds.addAll(MergeDuplicatedContactsActivity.this.mAllMembersId);
                } else {
                    MergeDuplicatedContactsActivity.this.finalIds.clear();
                }
                if (MergeDuplicatedContactsActivity.this.mTotalContacts == MergeDuplicatedContactsActivity.this.mTotalMergedContacts) {
                    MergeDuplicatedContactsActivity.this.mAllSelected = true;
                } else {
                    MergeDuplicatedContactsActivity.this.mAllSelected = false;
                }
                MergeDuplicatedContactsActivity.this.setActionBarTitle();
                MergeDuplicatedContactsActivity.this.invalidateOptionsMenu();
            }
        }
    };
    private HashSet<ArrayList<Long>> finalIds = new HashSet();
    private boolean isValidMatchPresent = false;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int idCencel;
            if (EmuiVersion.isSupportEmui()) {
                idCencel = 16908295;
            } else {
                idCencel = R.id.icon1;
            }
            if (v.getId() == idCencel) {
                MergeDuplicatedContactsActivity.this.cancelLoading();
                MergeDuplicatedContactsActivity.this.finish();
            }
        }
    };
    private MergeListAdapter mAdapter;
    private HashSet<ArrayList<Long>> mAllMembersId = new HashSet();
    private boolean mAllSelected = false;
    private ContactPhotoManager mContactPhotoManager;
    private Cursor mCursor = null;
    private ActionBarCustom mCustActionBar;
    public int[] mDataPositionCountArray = null;
    private AlertDialog mDiscardConfirmDialog = null;
    private View mEmptyContentView;
    private TextView mEmptyTextView;
    public String mExactMatchDataID = null;
    private final LoaderCallbacks<Cursor> mExactMatchLoader = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (id != 0) {
                return null;
            }
            if (HwLog.HWDBG) {
                HwLog.d("MergeDuplicatedContacts", "extractMatch Loader create .....");
            }
            Uri uri = MergeDuplicatedContactsActivity.this.getExactMatchUri();
            String selection = MergeDuplicatedContactsActivity.this.getSelection();
            return new CursorLoader(MergeDuplicatedContactsActivity.this, uri, new String[]{"_id", "display_name", "raw_contact_id", "data1", "photo_id", "is_private", "data4"}, selection, null, null);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor aResultCursor) {
            if (aResultCursor != null) {
                MergeDuplicatedContactsActivity.this.mCursor = aResultCursor;
                Bundle bundle = MergeDuplicatedContactsActivity.this.mCursor.getExtras();
                if (bundle != null) {
                    if (HwLog.HWDBG) {
                        HwLog.d("MergeDuplicatedContacts", "exactMatch Loader finished....");
                    }
                    MergeDuplicatedContactsActivity.this.mDataPositionCountArray = bundle.getIntArray("count");
                    MergeDuplicatedContactsActivity.this.mconvertedCountArray = MergeDuplicatedContactsActivity.this.getConvertedCountData();
                    MergeDuplicatedContactsActivity.this.mExactMatchDataID = bundle.getString("ids");
                    if (MergeDuplicatedContactsActivity.this.mDataPositionCountArray == null || !MergeDuplicatedContactsActivity.this.isValidData(MergeDuplicatedContactsActivity.this.mDataPositionCountArray)) {
                        MergeDuplicatedContactsActivity.this.isValidMatchPresent = false;
                        MergeDuplicatedContactsActivity.this.startFetchingPartialContacts();
                        return;
                    }
                    MergeDuplicatedContactsActivity.this.isValidMatchPresent = true;
                    MergeDuplicatedContactsActivity.this.mManipulatedDataArray = MergeDuplicatedContactsActivity.this.processCountArray();
                    if (MergeDuplicatedContactsActivity.this.mManipulatedDataArray != null) {
                        if (MergeDuplicatedContactsActivity.this.mMergeModelList == null) {
                            MergeDuplicatedContactsActivity.this.mMergeModelList = new ArrayList();
                        }
                        if (MergeDuplicatedContactsActivity.this.mMergeModelList.size() != MergeDuplicatedContactsActivity.this.mManipulatedDataArray.length) {
                            for (int mergeModel : MergeDuplicatedContactsActivity.this.mManipulatedDataArray) {
                                MergeDuplicatedContactsActivity.this.mMergeModelList.add(new MergeModel(mergeModel));
                            }
                        }
                        MergeDuplicatedContactsActivity.this.prepareList();
                        MergeDuplicatedContactsActivity.this.showOrHideLoadingView(false);
                        MergeDuplicatedContactsActivity.this.showOrHideList(false);
                        MergeDuplicatedContactsActivity.this.mergeExactContact();
                    }
                } else {
                    return;
                }
            }
            MergeDuplicatedContactsActivity.this.isValidMatchPresent = false;
            MergeDuplicatedContactsActivity.this.showEmptyView();
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private HwProgressDialog mExactMergeProgress;
    private ExactMergeFinishNotificationListener mExactMergeProgressListener = new ExactMergeFinishNotificationListener();
    ContactListFilter mFilter;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1 && MergeDuplicatedContactsActivity.this.mList != null) {
                MergeDuplicatedContactsActivity.this.mList.invalidateViews();
            }
        }
    };
    private ConcurrentHashMap<String, String> mHashMap = new ConcurrentHashMap();
    private LayoutInflater mInflater;
    private boolean mIsPartialMerge;
    private boolean mIsVisible = false;
    private ListView mList;
    LinearLayout mListParent;
    LinearLayout mLoadingView;
    public int[] mManipulatedDataArray = null;
    private MenuItem mMergeMenuItem;
    ArrayList<MergeModel> mMergeModelList;
    public HashMap<Integer, String> mNeedtoSkipPositionList;
    private NumberFormatThread mNumberFormatThread = null;
    private final LoaderCallbacks<Cursor> mPartialMatchLoader = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (id != 1) {
                return null;
            }
            if (HwLog.HWDBG) {
                HwLog.d("MergeDuplicatedContacts", "partial match loader create .....");
            }
            int displayOrder = new ContactsPreferences(MergeDuplicatedContactsActivity.this.getApplicationContext()).getDisplayOrder();
            Uri uri = MergeDuplicatedContactsActivity.this.getPartialMatchUri(displayOrder);
            String selection = MergeDuplicatedContactsActivity.this.getSelection();
            if (!(MergeDuplicatedContactsActivity.this.mExactMatchDataID == null || MergeDuplicatedContactsActivity.this.mExactMatchDataID.length() <= 0 || MergeDuplicatedContactsActivity.this.mconvertedCountArray == null)) {
                uri = uri.buildUpon().appendQueryParameter("ids", MergeDuplicatedContactsActivity.this.mExactMatchDataID).appendQueryParameter("count", MergeDuplicatedContactsActivity.this.mconvertedCountArray).appendQueryParameter("displayOrder", String.valueOf(displayOrder)).build();
            }
            return new CursorLoader(MergeDuplicatedContactsActivity.this, uri, new String[]{"_id", "display_name", "raw_contact_id", "data1", "photo_id", "is_private", "data4"}, selection, null, null);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor aResultCursor) {
            if (HwLog.HWDBG) {
                HwLog.d("MergeDuplicatedContacts", "partial match loader finish .....");
            }
            if (aResultCursor != null) {
                MergeDuplicatedContactsActivity.this.mCursor = aResultCursor;
                MergeDuplicatedContactsActivity.this.mDataPositionCountArray = MergeDuplicatedContactsActivity.this.mCursor.getExtras().getIntArray("count");
                if (MergeDuplicatedContactsActivity.this.mDataPositionCountArray == null || !MergeDuplicatedContactsActivity.this.isValidData(MergeDuplicatedContactsActivity.this.mDataPositionCountArray)) {
                    MergeDuplicatedContactsActivity.this.isValidMatchPresent = false;
                    MergeDuplicatedContactsActivity.this.showEmptyView();
                    return;
                }
                MergeDuplicatedContactsActivity.this.mManipulatedDataArray = MergeDuplicatedContactsActivity.this.processCountArray();
                MergeDuplicatedContactsActivity.this.isValidMatchPresent = true;
                MergeDuplicatedContactsActivity.this.showOrHideLoadingView(false);
                MergeDuplicatedContactsActivity.this.showOrHideList(true);
                MergeDuplicatedContactsActivity.this.mMergeModelList = new ArrayList();
                for (int mergeModel : MergeDuplicatedContactsActivity.this.mManipulatedDataArray) {
                    MergeDuplicatedContactsActivity.this.mMergeModelList.add(new MergeModel(mergeModel));
                }
                MergeDuplicatedContactsActivity.this.mAllSelected = false;
                MergeDuplicatedContactsActivity.this.mTotalMergedContacts = 0;
                MergeDuplicatedContactsActivity.this.finalIds.clear();
                MergeDuplicatedContactsActivity.this.setActionBarTitle();
                MergeDuplicatedContactsActivity.this.prepareList();
                MergeDuplicatedContactsActivity.this.mAdapter = new MergeListAdapter(MergeDuplicatedContactsActivity.this);
                MergeDuplicatedContactsActivity.this.mList.setAdapter(MergeDuplicatedContactsActivity.this.mAdapter);
                MergeDuplicatedContactsActivity.this.mList.setOnScrollListener(MergeDuplicatedContactsActivity.this.mAdapter);
                if (!MergeDuplicatedContactsActivity.this.mhasFirstTimeLoaded) {
                    MergeDuplicatedContactsActivity.this.performSelectAll();
                    MergeDuplicatedContactsActivity.this.mhasFirstTimeLoaded = true;
                }
                MergeDuplicatedContactsActivity.this.invalidateOptionsMenu();
            } else if (MergeDuplicatedContactsActivity.this.isValidMatchPresent) {
                MergeDuplicatedContactsActivity.this.finish();
            } else {
                MergeDuplicatedContactsActivity.this.showEmptyView();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private PartialMergeFinishNotificationListener mPartialMergeProgressListener = new PartialMergeFinishNotificationListener();
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private Drawable mSelectAllDrawable;
    private MenuItem mSelectAllMenuItem;
    private Drawable mSelectNoneDrawable;
    private ActionBarCustomTitle mTitle;
    private int mTotalContacts = 0;
    private int mTotalMergedContacts = 0;
    private String mconvertedCountArray = null;
    private boolean mhasFirstTimeLoaded = false;

    public class ExactMergeFinishNotificationListener implements MergeFinishNotification {
        int mCurrentCount = 0;
        int mState = -1;
        int mTotalContactsCount = 0;

        public void onFinished() {
            this.mState = 1;
            MergeDuplicatedContactsActivity.this.showProgressDialog(false, 0, 0);
            MergeDuplicatedContactsActivity.this.showOrHideLoadingView(true);
            MergeContacts.unRegisterMergeCallback(MergeDuplicatedContactsActivity.this.mExactMergeProgressListener);
            MergeDuplicatedContactsActivity.this.startFetchingPartialContacts();
        }

        public void onProgress(int aCurrentCount, int aTotalContacts) {
            this.mState = 0;
            this.mTotalContactsCount = aTotalContacts;
            this.mCurrentCount = aCurrentCount;
            if (MergeDuplicatedContactsActivity.this.mEmptyContentView.getVisibility() == 0) {
                MergeDuplicatedContactsActivity.this.mEmptyTextView.setText(MergeDuplicatedContactsActivity.this.getResources().getText(R.string.merging_duplicate_contacts).toString() + " (" + this.mCurrentCount + "/" + aTotalContacts + ")");
                return;
            }
            MergeDuplicatedContactsActivity.this.showProgressDialog(true, aCurrentCount, aTotalContacts);
        }
    }

    static class Info {
        private String contactNumber;
        private String mDefaultCountryIso;
        private String phoneNumberE164;

        public Info(String contactNumber, String phoneNumberE164, String mDefaultCountryIso) {
            this.contactNumber = contactNumber;
            this.phoneNumberE164 = phoneNumberE164;
            this.mDefaultCountryIso = mDefaultCountryIso;
        }

        public String getContactNumber() {
            return this.contactNumber;
        }

        public String getPhoneNumberE164() {
            return this.phoneNumberE164;
        }

        public String getDefaultCountryIso() {
            return this.mDefaultCountryIso;
        }
    }

    public final class MergeListAdapter extends BaseAdapter implements OnScrollListener {
        ArrayList<Long> ids = null;
        final String mDefaultCountryIso;

        public MergeListAdapter(Context context) {
            this.mDefaultCountryIso = GeoUtil.getCurrentCountryIso(context);
            if (MergeDuplicatedContactsActivity.this.mNumberFormatThread == null) {
                MergeDuplicatedContactsActivity.this.mNumberFormatThread = new NumberFormatThread();
                new Thread(MergeDuplicatedContactsActivity.this.mNumberFormatThread).start();
            }
        }

        public int getCount() {
            return MergeDuplicatedContactsActivity.this.mManipulatedDataArray != null ? MergeDuplicatedContactsActivity.this.mManipulatedDataArray.length : 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public void onScrollStateChanged(AbsListView listView, int scrollState) {
            boolean z = false;
            MergeDuplicatedContactsActivity mergeDuplicatedContactsActivity = MergeDuplicatedContactsActivity.this;
            if (scrollState == 0) {
                z = true;
            }
            mergeDuplicatedContactsActivity.IdleState = z;
            if (MergeDuplicatedContactsActivity.this.IdleState && MergeDuplicatedContactsActivity.this.mNumberFormatThread != null) {
                MergeDuplicatedContactsActivity.this.mNumberFormatThread.refreshListView();
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = MergeDuplicatedContactsActivity.this.mInflater.inflate(R.layout.merg_duplicate_contacts_container, null);
                viewHolder = new ViewHolder();
                viewHolder.idHolderLayout = (LinearLayout) convertView.findViewById(R.id.contacts_container);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.select);
                int size = viewHolder.fieldView.size();
                for (int i = 0; i < 5 - size; i++) {
                    viewHolder.fieldView.add(MergeDuplicatedContactsActivity.this.mInflater.inflate(R.layout.merg_duplicate_contacts_contact, null));
                }
                if (MergeDuplicatedContactsActivity.this.mIsPartialMerge) {
                    viewHolder.checkBox.setVisibility(0);
                }
                viewHolder.checkBox.setOnCheckedChangeListener(MergeDuplicatedContactsActivity.this.checkListener);
                viewHolder.checkBox.setOnTouchListener(MergeDuplicatedContactsActivity.this);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (MergeDuplicatedContactsActivity.this.mCursor.moveToFirst()) {
                int individualGroupSize = MergeDuplicatedContactsActivity.this.mManipulatedDataArray[position];
                MergeDuplicatedContactsActivity.this.mCursor.moveToPosition(getCursorPostionFromArray(position));
                this.ids = new ArrayList();
                viewHolder.checkBox.setTag(R.id.CURSOR_DATA, this.ids);
                viewHolder.idHolderLayout.removeAllViews();
                int iteration = 0;
                do {
                    View fieldView;
                    iteration++;
                    this.ids.add(Long.valueOf(MergeDuplicatedContactsActivity.this.mCursor.getLong(0)));
                    String contactNumber = MergeDuplicatedContactsActivity.this.mCursor.getString(3);
                    if (EmuiFeatureManager.isChinaArea() && !TextUtils.isEmpty(contactNumber) && contactNumber.contains("-")) {
                        contactNumber = contactNumber.replaceAll("-", "");
                    }
                    if (iteration <= viewHolder.fieldView.size()) {
                        fieldView = (View) viewHolder.fieldView.get(iteration - 1);
                    } else {
                        fieldView = MergeDuplicatedContactsActivity.this.mInflater.inflate(R.layout.merg_duplicate_contacts_contact, null);
                    }
                    ((TextView) fieldView.findViewById(R.id.name)).setText(MergeDuplicatedContactsActivity.this.mCursor.getString(1));
                    int lFlags = 0;
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled() && MergeDuplicatedContactsActivity.this.mCursor.getInt(5) == 1) {
                        lFlags = 4;
                    }
                    if (contactNumber == null || contactNumber.equals("null")) {
                        ((TextView) fieldView.findViewById(R.id.number)).setText("");
                    } else {
                        String phoneNumberE164;
                        String formatNumber;
                        if (-1 != MergeDuplicatedContactsActivity.this.mCursor.getColumnIndex("data4")) {
                            phoneNumberE164 = MergeDuplicatedContactsActivity.this.mCursor.getString(MergeDuplicatedContactsActivity.this.mCursor.getColumnIndex("data4"));
                        } else {
                            phoneNumberE164 = MergeDuplicatedContactsActivity.this.getPhoneNumberE164(MergeDuplicatedContactsActivity.this.mCursor.getLong(2));
                        }
                        if (MergeDuplicatedContactsActivity.this.mNumberFormatThread != null) {
                            formatNumber = MergeDuplicatedContactsActivity.this.mNumberFormatThread.getFormatNumber(contactNumber, phoneNumberE164, this.mDefaultCountryIso);
                        } else {
                            formatNumber = PhoneNumberUtils.formatNumber(contactNumber, phoneNumberE164, this.mDefaultCountryIso);
                        }
                        ((TextView) fieldView.findViewById(R.id.number)).setText(formatNumber);
                    }
                    ImageView mContactsAvatar = (QuickContactBadge) fieldView.findViewById(R.id.quick_contact_photo);
                    if (ContactDisplayUtils.isSimpleDisplayMode()) {
                        mContactsAvatar.setVisibility(8);
                        ((LinearLayout) fieldView.findViewById(R.id.name_and_number_container)).setPaddingRelative(0, 0, 0, 0);
                    } else {
                        mContactsAvatar.assignContactUri(null);
                        long photoId = MergeDuplicatedContactsActivity.this.mCursor.getLong(4);
                        DefaultImageRequest request = null;
                        if (photoId <= 0) {
                            MergeDuplicatedContactsActivity.this.mRequest.displayName = MergeDuplicatedContactsActivity.this.mCursor.getString(1);
                            MergeDuplicatedContactsActivity.this.mRequest.identifier = MergeDuplicatedContactsActivity.this.mCursor.getString(0);
                            MergeDuplicatedContactsActivity.this.mRequest.isCircular = true;
                            request = MergeDuplicatedContactsActivity.this.mRequest;
                        }
                        MergeDuplicatedContactsActivity.this.mContactPhotoManager.loadThumbnail(mContactsAvatar, photoId, false, request, lFlags);
                    }
                    viewHolder.idHolderLayout.addView(fieldView);
                    if (!MergeDuplicatedContactsActivity.this.mCursor.moveToNext()) {
                        break;
                    }
                } while (iteration < individualGroupSize);
            }
            viewHolder.checkBox.setTag(R.id.CURSOR_POSITION, Integer.valueOf(position));
            viewHolder.checkBox.setChecked(((MergeModel) MergeDuplicatedContactsActivity.this.mMergeModelList.get(position)).isSelected());
            return convertView;
        }

        public int getCursorPostionFromArray(int listItemPosition) {
            return Integer.parseInt((String) MergeDuplicatedContactsActivity.this.mNeedtoSkipPositionList.get(Integer.valueOf(listItemPosition)));
        }
    }

    private static class NegativeButtonListener implements DialogInterface.OnClickListener {
        private NegativeButtonListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    public class NumberFormatThread implements Runnable {
        private LinkedBlockingQueue<Info> mLinkedBlockingQueue = new LinkedBlockingQueue();

        public String getFormatNumber(String contactNumber, String phoneNumberE164, String mDefaultCountryIso) {
            if (MergeDuplicatedContactsActivity.this.mHashMap.containsKey(contactNumber)) {
                return (String) MergeDuplicatedContactsActivity.this.mHashMap.get(contactNumber);
            }
            instoreInfo(new Info(contactNumber, phoneNumberE164, mDefaultCountryIso));
            return contactNumber;
        }

        public void instoreInfo(Info _info) {
            if (_info != null) {
                try {
                    this.mLinkedBlockingQueue.put(_info);
                } catch (InterruptedException e) {
                }
            }
        }

        private void refreshListView() {
            MergeDuplicatedContactsActivity.this.mHandler.removeMessages(1);
            if (MergeDuplicatedContactsActivity.this.IdleState) {
                MergeDuplicatedContactsActivity.this.mHandler.sendMessageDelayed(MergeDuplicatedContactsActivity.this.mHandler.obtainMessage(1), 500);
            }
        }

        public void run() {
            int origPri = Process.getThreadPriority(Process.myTid());
            Process.setThreadPriority(10);
            while (true) {
                Info info = null;
                while (info == null) {
                    try {
                        info = (Info) this.mLinkedBlockingQueue.take();
                    } catch (InterruptedException e) {
                    }
                }
                if (info.getContactNumber() == null && info.getPhoneNumberE164() == null && info.getDefaultCountryIso() == null) {
                    Process.setThreadPriority(origPri);
                    return;
                }
                MergeDuplicatedContactsActivity.this.mHashMap.put(info.getContactNumber(), PhoneNumberUtils.formatNumber(info.getContactNumber(), info.getPhoneNumberE164(), info.getDefaultCountryIso()));
                synchronized (info) {
                    refreshListView();
                }
            }
        }
    }

    public class PartialMergeFinishNotificationListener implements MergeFinishNotification {
        int mCurrentCount = 0;
        int mState = -1;
        int mTotalContactsCount = 0;

        public void onFinished() {
            this.mState = 1;
            MergeDuplicatedContactsActivity.this.showProgressDialog(false, 0, 0);
            MergeDuplicatedContactsActivity.this.showOrHideLoadingView(true);
            MergeContacts.unRegisterMergeCallback(MergeDuplicatedContactsActivity.this.mPartialMergeProgressListener);
            if (MergeDuplicatedContactsActivity.this.isValidMatchPresent) {
                if (MergeDuplicatedContactsActivity.this.mAdapter != null) {
                    MergeDuplicatedContactsActivity.this.mAdapter.notifyDataSetInvalidated();
                }
                MergeDuplicatedContactsActivity.this.restartFetchingPartialContacts();
                return;
            }
            MergeDuplicatedContactsActivity.this.showEmptyView();
        }

        public void onProgress(int aCurrentCount, int aTotalContacts) {
            this.mState = 0;
            this.mTotalContactsCount = aTotalContacts;
            this.mCurrentCount = aCurrentCount;
            if (MergeDuplicatedContactsActivity.this.mEmptyContentView.getVisibility() == 0) {
                MergeDuplicatedContactsActivity.this.mEmptyTextView.setText(MergeDuplicatedContactsActivity.this.getResources().getText(R.string.merging_duplicate_contacts).toString() + " (" + this.mCurrentCount + "/" + aTotalContacts + ")");
                return;
            }
            MergeDuplicatedContactsActivity.this.showProgressDialog(true, aCurrentCount, aTotalContacts);
        }
    }

    static class ViewHolder {
        protected CheckBox checkBox;
        protected ArrayList<View> fieldView = new ArrayList();
        protected LinearLayout idHolderLayout;

        ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (HwLog.HWDBG) {
            HwLog.d("MergeDuplicatedContacts", "onCreate begin");
        }
        setTheme(R.style.ContactPickerTheme);
        setContentView(R.layout.merg_duplicate_contacts_layout);
        ActionBar actionBar = getActionBar();
        this.mTitle = new ActionBarCustomTitle(this);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setCustomTitle(actionBar, this.mTitle.getTitleLayout());
        } else {
            this.mCustActionBar = new ActionBarCustom(this, actionBar);
            this.mCustActionBar.setCustomTitle(this.mTitle.getTitleLayout());
        }
        if (savedInstanceState != null) {
            this.mMergeModelList = savedInstanceState.getParcelableArrayList("MergeModelList");
            this.mTotalMergedContacts = savedInstanceState.getInt("TotalMergedContacts");
            if (this.mMergeModelList != null) {
                for (int i = 0; i < this.mMergeModelList.size(); i++) {
                    if (((MergeModel) this.mMergeModelList.get(i)).isSelected()) {
                        this.finalIds.add(((MergeModel) this.mMergeModelList.get(i)).getContactIds());
                    }
                }
            }
            setActionBarTitle();
        }
        this.mFilter = (ContactListFilter) getIntent().getParcelableExtra("contactListFilter");
        if (1 == MergeContacts.getMergeType()) {
            MergeContacts.registerMergeCallback(this.mExactMergeProgressListener);
            this.mExactMergeProgressListener.mState = 0;
        } else if (2 == MergeContacts.getMergeType()) {
            MergeContacts.registerMergeCallback(this.mPartialMergeProgressListener);
            this.mPartialMergeProgressListener.mState = 0;
        }
        setActionBarTitle();
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(actionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(actionBar, false, null, null);
        } else {
            this.mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
            this.mCustActionBar.setEndIcon(false, null, null);
        }
        this.mList = (ListView) findViewById(R.id.merge_list);
        this.mList.setFriction(0.0075f);
        this.mList.setVelocityScale(0.65f);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mList, this);
        this.mEmptyContentView = findViewById(R.id.emptyContentView);
        this.mEmptyTextView = (TextView) this.mEmptyContentView.findViewById(R.id.emptyTextView);
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mContactPhotoManager = ContactPhotoManager.getInstance(this);
        this.mLoadingView = (LinearLayout) findViewById(R.id.loadingcontacts);
        this.mListParent = (LinearLayout) findViewById(R.id.listParent);
        if (!(this.mExactMergeProgressListener.mState == 0 || this.mPartialMergeProgressListener.mState == 0)) {
            getLoaderManager().initLoader(0, null, this.mExactMatchLoader);
        }
        if (this.mMergeModelList == null) {
            this.mMergeModelList = new ArrayList();
        }
        this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(this);
        this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(this);
        if (HwLog.HWDBG) {
            HwLog.d("MergeDuplicatedContacts", "onCreate end");
        }
    }

    protected void onResume() {
        if (HwLog.HWDBG) {
            HwLog.d("MergeDuplicatedContacts", "onResume begin");
        }
        super.onResume();
        this.mIsVisible = true;
        if (this.mExactMergeProgressListener.mState == 0) {
            if (this.mExactMergeProgressListener.mCurrentCount > 0 && this.mExactMergeProgressListener.mTotalContactsCount > 0) {
                showProgressDialog(true, this.mExactMergeProgressListener.mCurrentCount, this.mExactMergeProgressListener.mTotalContactsCount);
            }
        } else if (this.mPartialMergeProgressListener.mState == 0 && this.mPartialMergeProgressListener.mCurrentCount > 0 && this.mPartialMergeProgressListener.mTotalContactsCount > 0) {
            showProgressDialog(true, this.mPartialMergeProgressListener.mCurrentCount, this.mPartialMergeProgressListener.mTotalContactsCount);
        }
        if (HwLog.HWDBG) {
            HwLog.d("MergeDuplicatedContacts", "onResume end");
        }
    }

    protected void onPause() {
        super.onPause();
        this.mIsVisible = false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mEmptyTextView != null) {
            LayoutParams params = (LayoutParams) this.mEmptyTextView.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.empty_contacts_icon_top_margin);
            this.mEmptyTextView.setLayoutParams(params);
        }
        this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(this);
        this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(this);
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.contact_merge_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        this.mMergeMenuItem = menu.findItem(R.id.menu_action_merge);
        this.mSelectAllMenuItem = menu.findItem(R.id.menu_action_selectall);
        ImmersionUtils.setImmersionMommonMenu(this, this.mMergeMenuItem);
        ViewUtil.setMenuItemStateListIcon(getApplicationContext(), menu.findItem(R.id.menu_action_merge));
        this.mMergeMenuItem.setTitle(getString(R.string.merge_label));
        if (this.mAllSelected) {
            this.mSelectAllMenuItem.setTitle(getString(R.string.menu_select_none));
            this.mSelectAllMenuItem.setIcon(this.mSelectNoneDrawable);
        } else {
            this.mSelectAllMenuItem.setTitle(getString(R.string.contact_menu_select_all));
            this.mSelectAllMenuItem.setIcon(this.mSelectAllDrawable);
        }
        this.mSelectAllMenuItem.setChecked(this.mAllSelected);
        if (!this.isValidMatchPresent) {
            this.mMergeMenuItem.setVisible(false);
            this.mSelectAllMenuItem.setVisible(false);
        } else if (this.mExactMergeProgressListener.mState == 0 || this.mPartialMergeProgressListener.mState == 0) {
            this.mMergeMenuItem.setVisible(false);
            this.mSelectAllMenuItem.setVisible(false);
        } else {
            this.mMergeMenuItem.setVisible(true);
            this.mSelectAllMenuItem.setVisible(true);
            MenuItem menuItem = this.mMergeMenuItem;
            if (this.finalIds.size() <= 0) {
                z = false;
            }
            menuItem.setEnabled(z);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_selectall:
                performSelectAll();
                break;
            case R.id.menu_action_merge:
                if (this.mTotalMergedContacts < 1) {
                    return true;
                }
                showProgressDialog(true, 0, this.mTotalMergedContacts);
                MergeContacts.registerMergeCallback(this.mPartialMergeProgressListener);
                this.mPartialMergeProgressListener.mState = 0;
                MergeContacts.mergeRawContacts(this, this.finalIds, this.mTotalMergedContacts, 2);
                StatisticalHelper.report(1109);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        handleBackPressed();
        cancelLoading();
    }

    private void handleBackPressed() {
        if (!this.isValidMatchPresent || this.mExactMergeProgressListener.mState == 0 || this.mPartialMergeProgressListener.mState == 0) {
            finish();
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        if (this.mDiscardConfirmDialog == null) {
            Builder builder = new Builder(this);
            builder.setMessage(getResources().getString(R.string.merge_duplicate_message_label));
            builder.setPositiveButton(R.string.contact_menu_discard, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MergeDuplicatedContactsActivity.this.finish();
                }
            });
            builder.setNegativeButton(R.string.merge_dialog_cancel_button, new NegativeButtonListener());
            this.mDiscardConfirmDialog = builder.create();
            this.mDiscardConfirmDialog.setMessageNotScrolling();
        }
        this.mDiscardConfirmDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                MergeDuplicatedContactsActivity.this.mDiscardConfirmDialog = null;
            }
        });
        if (!this.mDiscardConfirmDialog.isShowing()) {
            this.mDiscardConfirmDialog.show();
        }
    }

    public void performSelectAll() {
        this.mAllSelected = !this.mAllSelected;
        if (this.mManipulatedDataArray != null) {
            for (int i = 0; i < this.mManipulatedDataArray.length; i++) {
                ((MergeModel) this.mMergeModelList.get(i)).setSelected(this.mAllSelected);
            }
        }
        this.finalIds.clear();
        this.mTotalMergedContacts = 0;
        if (this.mAllSelected) {
            this.finalIds.addAll(this.mAllMembersId);
            this.mTotalMergedContacts = this.mTotalContacts;
        }
        setActionBarTitle();
        this.mList.invalidateViews();
        invalidateOptionsMenu();
    }

    public void showProgressDialog(boolean aShow, int aCurrent, int aTotal) {
        if (this.mIsVisible && aShow) {
            showOrHideLoadingView(false);
            this.mEmptyContentView.setVisibility(8);
            if (this.mExactMergeProgress == null) {
                this.mExactMergeProgress = new HwProgressDialog(this);
                this.mExactMergeProgress.setCancelable(false);
                this.mExactMergeProgress.setProgressStyle(1);
                this.mExactMergeProgress.setMessage(getString(R.string.merging_contacts));
                this.mExactMergeProgress.setMax(aTotal);
                this.mExactMergeProgress.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() != 1 || keyCode != 4) {
                            return false;
                        }
                        MergeDuplicatedContactsActivity.this.onBackPressed();
                        return true;
                    }
                });
                this.mExactMergeProgress.show();
                this.mExactMergeProgress.disableCancelButton();
            }
            this.mExactMergeProgress.setProgress(aCurrent);
        } else if (this.mExactMergeProgress != null && this.mExactMergeProgress.isShowing()) {
            this.mExactMergeProgress.dismiss();
            this.mExactMergeProgress = null;
        }
    }

    private void showEmptyView() {
        showOrHideLoadingView(false);
        if (this.mMergeMenuItem != null) {
            this.mMergeMenuItem.setEnabled(false);
            this.mMergeMenuItem.setVisible(false);
        }
        if (this.mSelectAllMenuItem != null) {
            this.mSelectAllMenuItem.setEnabled(false);
            this.mSelectAllMenuItem.setVisible(false);
        }
        this.mEmptyContentView.setVisibility(0);
        this.mEmptyTextView.setText(R.string.contact_no_duplicate_contacts);
        this.mListParent.setVisibility(8);
        setActionBarTitle();
    }

    private void showOrHideList(boolean aShow) {
        int i;
        int i2 = 0;
        View view = this.mEmptyContentView;
        if (aShow) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        LinearLayout linearLayout = this.mLoadingView;
        if (aShow) {
            i = 8;
        } else {
            i = 0;
        }
        linearLayout.setVisibility(i);
        LinearLayout linearLayout2 = this.mListParent;
        if (!aShow) {
            i2 = 8;
        }
        linearLayout2.setVisibility(i2);
        if (this.mMergeMenuItem != null) {
            this.mMergeMenuItem.setVisible(aShow);
        }
        if (this.mSelectAllMenuItem != null) {
            this.mSelectAllMenuItem.setVisible(aShow);
        }
    }

    private void startFetchingPartialContacts() {
        clear();
        this.mIsPartialMerge = true;
        this.finalIds.clear();
        setActionBarTitle();
        invalidateOptionsMenu();
        if (HwLog.HWDBG) {
            HwLog.d("MergeDuplicatedContacts", "start partial contacts merge....");
        }
        getLoaderManager().destroyLoader(0);
        getLoaderManager().initLoader(1, null, this.mPartialMatchLoader);
    }

    private void restartFetchingPartialContacts() {
        clear();
        this.mIsPartialMerge = true;
        this.finalIds.clear();
        setActionBarTitle();
        invalidateOptionsMenu();
        getLoaderManager().restartLoader(1, null, this.mPartialMatchLoader);
    }

    private void clear() {
        if (this.mCursor != null) {
            this.mCursor = null;
        }
        this.mManipulatedDataArray = null;
        this.mMergeModelList = null;
        this.mAdapter = null;
        this.mDataPositionCountArray = null;
    }

    private String getPhoneNumberE164(long rawContactsId) {
        Cursor cursor = getContentResolver().query(Data.CONTENT_URI, new String[]{"_id", "data4"}, "raw_contact_id = ? AND mimetype=?", new String[]{String.valueOf(rawContactsId), "vnd.android.cursor.item/phone_v2"}, null);
        String str = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    str = cursor.getString(1);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return str;
    }

    private Uri getExactMatchUri() {
        Uri uri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "merge_suggestions");
        if (this.mFilter == null || this.mFilter.filterType == -3 || this.mFilter.filterType == -6) {
            return uri;
        }
        Uri.Builder builder = uri.buildUpon();
        if (this.mFilter.filterType == 0) {
            this.mFilter.addAccountQueryParameterToUrl(builder);
        }
        return builder.build();
    }

    private String getSelection() {
        if (this.mFilter == null || -3 != this.mFilter.filterType) {
            return null;
        }
        return "in_visible_group=1";
    }

    private void mergeExactContact() {
        this.finalIds.clear();
        this.finalIds.addAll(this.mAllMembersId);
        setActionBarTitle();
        this.mTotalMergedContacts = this.mTotalContacts;
        showProgressDialog(true, 0, this.mTotalContacts);
        MergeContacts.registerMergeCallback(this.mExactMergeProgressListener);
        this.mExactMergeProgressListener.mState = 0;
        MergeContacts.mergeRawContacts(this, this.finalIds, this.mTotalContacts, 1);
    }

    private Uri getPartialMatchUri(int displayOrder) {
        Uri uri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "partial_merge_suggestions");
        if (this.mFilter == null || this.mFilter.filterType == -3 || this.mFilter.filterType == -6) {
            return uri;
        }
        Uri.Builder builder = uri.buildUpon();
        if (this.mFilter.filterType == 0) {
            this.mFilter.addAccountQueryParameterToUrl(builder);
        }
        builder.appendQueryParameter("displayOrder", String.valueOf(displayOrder));
        return builder.build();
    }

    private String getConvertedCountData() {
        StringBuilder st = new StringBuilder();
        if (this.mDataPositionCountArray == null || this.mDataPositionCountArray.length <= 0) {
            return null;
        }
        for (int append : this.mDataPositionCountArray) {
            st.append(append).append(",");
        }
        st.setLength(st.length() - 1);
        return st.toString();
    }

    private void showOrHideLoadingView(boolean showLoadingView) {
        showOrHideList(false);
        if (showLoadingView) {
            this.mList.setAdapter(null);
            this.mLoadingView.setVisibility(0);
            return;
        }
        this.mLoadingView.setVisibility(8);
    }

    private void prepareList() {
        this.mAllMembersId.clear();
        if (this.mCursor.moveToFirst()) {
            int count = 0;
            int lIndex = 0;
            for (int i = 0; i < this.mManipulatedDataArray.length; i++) {
                ArrayList<Long> contactsIds = new ArrayList();
                count += this.mManipulatedDataArray[i];
                if (this.mManipulatedDataArray[i] > 0) {
                    int lIndex2;
                    while (true) {
                        contactsIds.add(Long.valueOf(this.mCursor.getLong(0)));
                        if (!this.mCursor.moveToNext() || this.mCursor.isAfterLast() || this.mCursor.getPosition() >= count) {
                            this.mAllMembersId.add(contactsIds);
                            lIndex2 = lIndex + 1;
                            ((MergeModel) this.mMergeModelList.get(lIndex)).setContactIds(contactsIds);
                            lIndex = lIndex2;
                        }
                    }
                    this.mAllMembersId.add(contactsIds);
                    lIndex2 = lIndex + 1;
                    ((MergeModel) this.mMergeModelList.get(lIndex)).setContactIds(contactsIds);
                    lIndex = lIndex2;
                } else if (!this.mCursor.moveToNext()) {
                    return;
                }
            }
        }
    }

    public boolean isValidData(int[] countArray) {
        this.mTotalContacts = 0;
        for (int i : countArray) {
            if (i > 0) {
                this.mTotalContacts++;
            }
        }
        if (this.mTotalContacts > 0) {
            return true;
        }
        return false;
    }

    public int[] processCountArray() {
        int l = 0;
        this.mNeedtoSkipPositionList = new HashMap();
        int skipCursorCount = 0;
        int currentPos = 0;
        for (int currentVal : this.mDataPositionCountArray) {
            if (currentVal > 0) {
                this.mNeedtoSkipPositionList.put(Integer.valueOf(currentPos), "" + skipCursorCount);
                currentPos++;
            }
            skipCursorCount += currentVal;
        }
        int[] validInt = new int[this.mNeedtoSkipPositionList.size()];
        for (int k = 0; k < this.mDataPositionCountArray.length; k++) {
            if (this.mDataPositionCountArray[k] > 0) {
                int l2 = l + 1;
                validInt[l] = this.mDataPositionCountArray[k];
                l = l2;
            }
        }
        return validInt;
    }

    public void cancelLoading() {
        if (this.mNumberFormatThread != null) {
            this.mNumberFormatThread.instoreInfo(new Info(null, null, null));
            this.mNumberFormatThread = null;
        }
        if (this.mHashMap != null) {
            this.mHashMap.clear();
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == 0) {
            v.playSoundEffect(0);
        }
        return false;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("MergeModelList", this.mMergeModelList);
        outState.putInt("TotalMergedContacts", this.mTotalMergedContacts);
    }

    protected void onDestroy() {
        super.onDestroy();
        cancelLoading();
        MergeContacts.unRegisterMergeCallback(this.mExactMergeProgressListener);
        MergeContacts.unRegisterMergeCallback(this.mPartialMergeProgressListener);
        if (this.mExactMergeProgress != null && this.mExactMergeProgress.isShowing()) {
            this.mExactMergeProgress.dismiss();
        }
        if (this.mDiscardConfirmDialog != null) {
            this.mDiscardConfirmDialog.setOnDismissListener(null);
            if (this.mDiscardConfirmDialog.isShowing()) {
                this.mDiscardConfirmDialog.dismiss();
            }
            this.mDiscardConfirmDialog = null;
        }
    }

    private void setActionBarTitle() {
        if (this.finalIds != null) {
            this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(getApplicationContext(), this.finalIds.size()), this.finalIds.size());
        } else {
            this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(getApplicationContext(), 0), 0);
        }
    }
}
