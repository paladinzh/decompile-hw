package com.android.contacts.speeddial;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.SpeedDialContract;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.ShowErrorDiallogUtils;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class SpeedDialerFragment extends Fragment {
    private static final boolean LOG_CONSTANT = HwLog.HWDBG;
    private static final String[] SPEED_DIAL_DATA_PROJECTION = new String[]{"_id", "raw_contact_id", "contact_id", "photo_id", "data1", "display_name", "account_type"};
    private static final String[] SPEED_DIAL_DATA_PROJECTION_PRIVATE;
    private int LAUNCH_CONTACT_LIST_ACTIVITY = 100;
    private ArrayList<String> mContactsAdded = new ArrayList();
    private Context mContext;
    private HwCustSpeedDialerFragment mCust = null;
    private HashMap<Integer, DialPadState> mDialpadState;
    public OnClickListener mGridClickListener = new OnClickListener() {
        public void onClick(View aView) {
            SpeedDialerFragment.this.onGridItemSelected(aView);
        }
    };
    public OnItemClickListener mGridItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View aView, int arg2, long arg3) {
            SpeedDialerFragment.this.onGridItemSelected(aView);
        }
    };
    private GridView mGridView;
    private boolean mIsLoaderStarted;
    private int mSelectedItemPosition = -1;
    private ArrayList<Integer> mSelectedItemsToRemove;
    private ShowErrorDiallogUtils mShowErrorDiallogUtils;
    private int mSpeedDialCount;
    private HashMap<Integer, String> mSpeedDialPredefinedNumberMap = new HashMap();
    private final Uri mSpeedDialURI = SpeedDialContract.CONTENT_URI;
    private final LoaderCallbacks<Cursor> mSpeeddialLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (SpeedDialerFragment.LOG_CONSTANT) {
                HwLog.d("SpeedDial", "onCreateLoader");
            }
            String lErrorMsg = "UnExpected request for loader with ID: " + id;
            switch (id) {
                case 1:
                    return new CursorLoader(SpeedDialerFragment.this.mContext, SpeedDialerFragment.this.mSpeedDialURI, null, null, null, null);
                case 2:
                    if (args == null || !args.containsKey("selection")) {
                        lErrorMsg = "selection required with loader 2";
                        HwLog.e("SpeedDial", "onCreateLoader : error = " + lErrorMsg);
                        throw new IllegalArgumentException(lErrorMsg);
                    }
                    String[] -get2;
                    Context -get3 = SpeedDialerFragment.this.mContext;
                    Uri uri = Data.CONTENT_URI;
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                        -get2 = SpeedDialerFragment.SPEED_DIAL_DATA_PROJECTION_PRIVATE;
                    } else {
                        -get2 = SpeedDialerFragment.SPEED_DIAL_DATA_PROJECTION;
                    }
                    return new CursorLoader(-get3, uri, -get2, args.getString("selection"), null, null);
                default:
                    throw new IllegalArgumentException(lErrorMsg);
            }
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor aCursor) {
            if (aCursor != null) {
                if (SpeedDialerFragment.LOG_CONSTANT) {
                    HwLog.d("SpeedDial", "onLoadFinished");
                }
                if (loader != null) {
                    switch (loader.getId()) {
                        case 1:
                            SpeedDialerFragment.this.updateSpeedDialInfo(aCursor);
                            break;
                        case 2:
                            SpeedDialerFragment.this.updateSpeedDialDataInfo(aCursor);
                            SpeedDialerFragment.this.getActivity().invalidateOptionsMenu();
                            SpeedDialerFragment.this.mGridView.setAdapter(SpeedDialerFragment.this.mViewAdapter);
                            break;
                        default:
                            throw new IllegalArgumentException("UnExpected call back from loader with ID: " + loader.getId());
                    }
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            if (SpeedDialerFragment.LOG_CONSTANT) {
                HwLog.d("SpeedDial", "onLoaderReset");
            }
        }
    };
    private GridAdapter mViewAdapter;

    public static class DialPadState {
        public String mAccType;
        public long mContactId;
        public String mContactNumber = null;
        public long mDataID = -1;
        public int mDialpadNumber = -1;
        public String mDisplayName = null;
        public boolean mHasContactForSpeedDial = false;
        public boolean mIsPrivate;
        public long mPhotoId;
    }

    public class GridAdapter extends BaseAdapter {
        ContactPhotoManager mContactPhotoManager;

        public GridAdapter() {
            this.mContactPhotoManager = ContactPhotoManager.getInstance(SpeedDialerFragment.this.mContext);
        }

        public int getCount() {
            return SpeedDialerFragment.this.mDialpadState.size();
        }

        public Object getItem(int aPosition) {
            return SpeedDialerFragment.this.mDialpadState.get(Integer.valueOf(aPosition));
        }

        public long getItemId(int aPosition) {
            return (long) aPosition;
        }

        public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
            GridItem lView;
            int lRealPosition = aPosition + 1;
            if (aConvertView == null) {
                lView = (GridItem) View.inflate(SpeedDialerFragment.this.mContext, R.layout.speeddial_gridchild_item, null);
            } else {
                lView = (GridItem) aConvertView;
            }
            lView.setTag(Integer.valueOf(lRealPosition));
            lView.setOnClickListener(SpeedDialerFragment.this.mGridClickListener);
            setDataWithView((DialPadState) SpeedDialerFragment.this.mDialpadState.get(Integer.valueOf(lRealPosition)), lView);
            return lView;
        }

        public void setDataWithView(DialPadState aDialPadState, GridItem aView) {
            ImageView lContactImage = (ImageView) aView.findViewById(R.id.speeddial_contactimage);
            TextView lDialpadNumber = (TextView) aView.findViewById(R.id.speed_dial_number);
            TextView lSpeedDialName = (TextView) ((LinearLayout) aView.findViewById(R.id.speeddial_contactnamebar)).findViewById(R.id.speeddial_contactname);
            lDialpadNumber.setText("" + aDialPadState.mDialpadNumber);
            if (aDialPadState.mHasContactForSpeedDial) {
                aView.setOnLongClickListener(null);
                lSpeedDialName.setText(aDialPadState.mDisplayName);
                aView.setPhotoManager(this.mContactPhotoManager);
                aView.setPhotoId(aDialPadState.mPhotoId);
                aView.setContactId(aDialPadState.mContactId);
                aView.setIsPrivate(aDialPadState.mIsPrivate);
                lContactImage.setVisibility(0);
                lContactImage.setScaleType(ScaleType.FIT_CENTER);
            } else if (1 == aDialPadState.mDialpadNumber) {
                lSpeedDialName.setText(SpeedDialerFragment.this.getString(R.string.voice_mail_text));
                lSpeedDialName.setTextColor(SpeedDialerFragment.this.getResources().getColor(R.color.speed_dial_name_default_color));
                lDialpadNumber.setTextColor(SpeedDialerFragment.this.getResources().getColor(R.color.speed_dial_number_default_color));
                lContactImage.setVisibility(0);
                lContactImage.setImageResource(R.drawable.speed_dial_voicemail_background);
                aView.setPhotoManager(null);
            } else if (SpeedDialerFragment.this.mSpeedDialPredefinedNumberMap.containsKey(Integer.valueOf(aDialPadState.mDialpadNumber))) {
                lSpeedDialName.setText("" + ((String) SpeedDialerFragment.this.mSpeedDialPredefinedNumberMap.get(Integer.valueOf(aDialPadState.mDialpadNumber))));
                lContactImage.setImageResource(R.drawable.contact_avatar_180_holo);
            } else {
                aView.setPhotoManager(null);
                SpeedDialerFragment.this.setDefaultView(aView);
                lSpeedDialName.setText(SpeedDialerFragment.this.getString(R.string.add_label));
                lSpeedDialName.setTextColor(SpeedDialerFragment.this.getResources().getColor(R.color.speed_dial_name_default_color));
                lDialpadNumber.setTextColor(SpeedDialerFragment.this.getResources().getColor(R.color.speed_dial_number_default_color));
            }
        }
    }

    static {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            SPEED_DIAL_DATA_PROJECTION_PRIVATE = new String[(SPEED_DIAL_DATA_PROJECTION.length + 1)];
            System.arraycopy(SPEED_DIAL_DATA_PROJECTION, 0, SPEED_DIAL_DATA_PROJECTION_PRIVATE, 0, SPEED_DIAL_DATA_PROJECTION.length);
            SPEED_DIAL_DATA_PROJECTION_PRIVATE[SPEED_DIAL_DATA_PROJECTION.length] = "is_private";
            return;
        }
        SPEED_DIAL_DATA_PROJECTION_PRIVATE = SPEED_DIAL_DATA_PROJECTION;
    }

    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        if (aSavedInstanceState != null) {
            this.mSpeedDialCount = aSavedInstanceState.getInt("speed_dial_count", this.mSpeedDialCount);
            this.mSelectedItemPosition = aSavedInstanceState.getInt("speed_dial_index", this.mSelectedItemPosition);
        }
    }

    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onCreateView");
        }
        return View.inflate(getActivity(), R.layout.speeddial_gridview, null);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onViewCreated");
        }
        super.onViewCreated(view, savedInstanceState);
        this.mContext = getActivity();
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustSpeedDialerFragment) HwCustUtils.createObj(HwCustSpeedDialerFragment.class, new Object[]{this.mContext});
        }
        this.mGridView = (GridView) view.findViewById(R.id.speeddial_grid_view);
        this.mGridView.setOnItemClickListener(this.mGridItemClickListener);
        this.mViewAdapter = new GridAdapter();
        this.mSelectedItemsToRemove = new ArrayList();
        fillDefaultData();
        updateSpeedDialPredefinedCache();
    }

    private void fillDefaultData() {
        if (this.mDialpadState == null) {
            this.mDialpadState = new HashMap();
        }
        this.mDialpadState.clear();
        for (int i = 1; i <= 9; i++) {
            DialPadState lDialPadState = new DialPadState();
            this.mDialpadState.put(Integer.valueOf(i), lDialPadState);
            lDialPadState.mDialpadNumber = i;
        }
    }

    public void setDefaultView(View aView) {
        ((ImageView) aView.findViewById(R.id.speeddial_contactimage)).setImageResource(R.drawable.speed_dial_add_background);
    }

    private void launchContactList() {
        startActivityForResult(CommonUtilMethods.getContactSelectionIntentForSpeedDial(this.mContext, this.mContactsAdded, null), this.LAUNCH_CONTACT_LIST_ACTIVITY);
    }

    private void onGridItemSelected(View aView) {
        this.mSelectedItemPosition = ((Integer) aView.getTag()).intValue();
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onClick() " + this.mSelectedItemPosition);
        }
        DialPadState lCurrentPositionState = (DialPadState) this.mDialpadState.get(Integer.valueOf(this.mSelectedItemPosition));
        if (lCurrentPositionState.mDialpadNumber == 1) {
            if (SimFactoryManager.isDualSim()) {
                checkVoiceMailAvailability();
            } else if (!SimFactoryManager.hasIccCard(-1)) {
                this.mShowErrorDiallogUtils.showSIMNotAvailableForVoicemailDialog();
            } else if (CommonUtilMethods.isAirplaneModeOn(this.mContext)) {
                this.mShowErrorDiallogUtils.showAirplaneModeOnForVoicemailDialog();
            } else {
                startActivity(getVoiceMailSettingsIntentForSingleSim(SimFactoryManager.getSubscriptionIdBasedOnSlot(-1)));
            }
        } else if (lCurrentPositionState.mHasContactForSpeedDial) {
            showOptionsForSelectedItem(lCurrentPositionState);
        } else if (!this.mSpeedDialPredefinedNumberMap.containsKey(Integer.valueOf(lCurrentPositionState.mDialpadNumber))) {
            launchContactList();
            StatisticalHelper.report(4052);
        } else if (this.mCust == null || this.mCust.isPredefinedSpeedNumberEditable()) {
            ArrayAdapter<String> lAdapter = new ArrayAdapter(this.mContext, R.layout.select_dialog_item);
            lAdapter.add(getString(R.string.contact_change_assigned_number));
            new Builder(this.mContext).setTitle(R.string.speed_dial_modification).setSingleChoiceItems(lAdapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface aDialog, int aWhich) {
                    SpeedDialerFragment.this.launchContactList();
                    aDialog.dismiss();
                }
            }).create().show();
        }
    }

    private void checkVoiceMailAvailability() {
        boolean isSim1Present = SimFactoryManager.hasIccCard(0);
        boolean isSim2Present = SimFactoryManager.hasIccCard(1);
        if (!isSim1Present && !isSim2Present) {
            this.mShowErrorDiallogUtils.showSIMNotAvailableForVoicemailDialog();
        } else if (CommonUtilMethods.isAirplaneModeOn(this.mContext)) {
            this.mShowErrorDiallogUtils.showAirplaneModeOnForVoicemailDialog();
        } else {
            Intent intent;
            int subId1 = SimFactoryManager.getSubscriptionIdBasedOnSlot(0);
            int subId2 = SimFactoryManager.getSubscriptionIdBasedOnSlot(1);
            if (isSim1Present && isSim2Present) {
                boolean isSim1VMAvilable = CommonUtilMethods.isVoicemailAvailable(subId1);
                boolean isSim2VMAvilable = CommonUtilMethods.isVoicemailAvailable(subId2);
                if (!(isSim1VMAvilable && isSim2VMAvilable) && (isSim1VMAvilable || isSim2VMAvilable)) {
                    if (!isSim1VMAvilable) {
                        subId1 = subId2;
                    }
                    intent = getVoiceMailSettingsIntentForSingleSim(subId1);
                } else {
                    intent = getVoiceMailSettingsIntentForDualSim();
                }
            } else {
                int subId;
                if (isSim1Present) {
                    subId = subId1;
                } else {
                    subId = subId2;
                }
                intent = getVoiceMailSettingsIntentForSingleSim(subId);
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                HwLog.e("SpeedDial", "voice mail settings activity not found");
            }
        }
    }

    private Intent getVoiceMailSettingsIntentForDualSim() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.phone", "com.android.phone.MSimCallFeaturesSetting");
        intent.setFlags(67108864);
        intent.putExtra("voicemail", true);
        return intent;
    }

    private Intent getVoiceMailSettingsIntentForSingleSim(int subId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.phone", "com.android.phone.settings.VoicemailSettingsActivity");
        intent.putExtra("com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId", subId);
        return intent;
    }

    private void showOptionsForSelectedItem(DialPadState aCurrentPositionState) {
        final ArrayAdapter<String> lAdapter = new ArrayAdapter(this.mContext, R.layout.select_dialog_item);
        final String lReAssign = getString(R.string.contact_change_assigned_number);
        final String lRemove = getString(R.string.contact_clear_assigned_number);
        lAdapter.add(lRemove);
        lAdapter.add(lReAssign);
        new Builder(this.mContext).setTitle(R.string.speed_dial_modification).setSingleChoiceItems(lAdapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface aDialog, int aWhich) {
                String lSeletedOption = (String) lAdapter.getItem(aWhich);
                if (lReAssign.equals(lSeletedOption)) {
                    SpeedDialerFragment.this.launchContactList();
                } else if (lRemove.equals(lSeletedOption)) {
                    SpeedDialerFragment.this.mSelectedItemsToRemove.add(Integer.valueOf(SpeedDialerFragment.this.mSelectedItemPosition));
                    SpeedDialerFragment.this.removeSpeedDialFromTheKey();
                    SpeedDialerFragment.this.checkAndStartLoader();
                }
                aDialog.dismiss();
            }
        }).create().show();
    }

    private void removeSpeedDialFromTheKey() {
        StringBuilder lWhereClause = new StringBuilder();
        if (this.mSelectedItemsToRemove.size() != 0) {
            int lSize = this.mSelectedItemsToRemove.size();
            for (int i = 0; i < lSize; i++) {
                if (i != 0) {
                    lWhereClause = lWhereClause.append(" OR ");
                }
                int lPositionToRemove = ((Integer) this.mSelectedItemsToRemove.get(i)).intValue();
                lWhereClause.append("key_number = ").append(lPositionToRemove);
                DialPadState lstate = (DialPadState) this.mDialpadState.get(Integer.valueOf(lPositionToRemove));
                lstate.mDialpadNumber = lPositionToRemove;
                lstate.mHasContactForSpeedDial = false;
                lstate.mDisplayName = null;
                lstate.mContactNumber = null;
                lstate.mDataID = -1;
                lstate.mPhotoId = 0;
                lstate.mContactId = 0;
                this.mSpeedDialCount--;
            }
            this.mContext.getContentResolver().delete(this.mSpeedDialURI, lWhereClause.toString(), null);
        }
        this.mSelectedItemsToRemove.clear();
    }

    public void onResume() {
        super.onResume();
        this.mShowErrorDiallogUtils = new ShowErrorDiallogUtils(this);
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onResume(Entered)");
        }
        if (getActivity() != null && getActivity().isInMultiWindowMode()) {
            int gridViewpadding = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_speed_dial_vertical_multiwindon_padding);
            this.mGridView.setPadding(gridViewpadding, gridViewpadding, gridViewpadding, gridViewpadding);
        }
        checkAndStartLoader();
    }

    private void checkAndStartLoader() {
        Bundle bundle = new Bundle();
        if (this.mIsLoaderStarted) {
            getLoaderManager().restartLoader(1, bundle, this.mSpeeddialLoaderListener);
            return;
        }
        getLoaderManager().initLoader(1, bundle, this.mSpeeddialLoaderListener);
        this.mIsLoaderStarted = true;
    }

    public void onPause() {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onPause(Entered)");
        }
        super.onPause();
    }

    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aRequestCode == this.LAUNCH_CONTACT_LIST_ACTIVITY && aResultCode == -1) {
            Bundle lBundle = aData.getExtras();
            if (lBundle == null) {
                lBundle = new Bundle();
            }
            lBundle.putInt("key_speed_dial", this.mSelectedItemPosition);
            aData.putExtras(lBundle);
            SpeedDialerActivity.handleResultFromContactSelection(aData, getActivity(), false);
            this.mSpeedDialCount++;
            this.mContactsAdded.clear();
        }
    }

    public boolean onContextItemSelected(MenuItem aItem) {
        switch (aItem.getItemId()) {
            case 1:
                this.mSelectedItemsToRemove.add(Integer.valueOf(this.mSelectedItemPosition));
                removeSpeedDialFromTheKey();
                checkAndStartLoader();
                break;
            case 5:
                launchContactList();
                break;
        }
        return super.onContextItemSelected(aItem);
    }

    public void onDestroy() {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onDestroy");
        }
        super.onDestroy();
        if (this.mSelectedItemsToRemove != null) {
            this.mSelectedItemsToRemove.clear();
            this.mSelectedItemsToRemove = null;
        }
        if (this.mDialpadState != null) {
            this.mDialpadState.clear();
            this.mDialpadState = null;
        }
        this.mViewAdapter = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onSaveInstanceState ");
        }
        outState.putInt("speed_dial_count", this.mSpeedDialCount);
        outState.putInt("speed_dial_index", this.mSelectedItemPosition);
    }

    private void updateSpeedDialDataInfo(Cursor aCursor) {
        if (aCursor != null) {
            this.mSpeedDialCount = aCursor.getCount();
            this.mContactsAdded.clear();
            if (aCursor.moveToFirst()) {
                do {
                    DialPadState lSpeedDialDataObject = getMatchedObjWithDataId(aCursor.getLong(0));
                    if (lSpeedDialDataObject != null) {
                        lSpeedDialDataObject.mDataID = aCursor.getLong(0);
                        if (!(lSpeedDialDataObject.mDataID == -1 || aCursor.isNull(1))) {
                            lSpeedDialDataObject.mAccType = aCursor.getString(6);
                            if ("com.android.huawei.sim".equals(lSpeedDialDataObject.mAccType)) {
                                lSpeedDialDataObject.mPhotoId = -2;
                            } else if ("com.android.huawei.secondsim".equals(lSpeedDialDataObject.mAccType)) {
                                lSpeedDialDataObject.mPhotoId = -3;
                            } else {
                                lSpeedDialDataObject.mPhotoId = aCursor.getLong(3);
                            }
                            lSpeedDialDataObject.mContactId = aCursor.getLong(2);
                            lSpeedDialDataObject.mContactNumber = aCursor.getString(4);
                            lSpeedDialDataObject.mDisplayName = aCursor.getString(5);
                            lSpeedDialDataObject.mHasContactForSpeedDial = true;
                            this.mContactsAdded.add(String.valueOf(lSpeedDialDataObject.mDataID));
                            boolean lIsPrivateContact = false;
                            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                                lIsPrivateContact = CommonUtilMethods.isPrivateContact(aCursor);
                            }
                            lSpeedDialDataObject.mIsPrivate = lIsPrivateContact;
                        }
                    }
                } while (aCursor.moveToNext());
            }
            checkForStaleDataAndResetState();
        }
    }

    private void checkForStaleDataAndResetState() {
        for (Entry<Integer, DialPadState> lEntry : this.mDialpadState.entrySet()) {
            DialPadState lState = (DialPadState) lEntry.getValue();
            if (!(lState.mDataID == -1 || this.mContactsAdded.contains(String.valueOf(lState.mDataID)))) {
                lState.mDataID = -1;
                lState.mDisplayName = null;
                lState.mHasContactForSpeedDial = false;
                lState.mContactNumber = null;
                lState.mPhotoId = 0;
                lState.mContactId = 0;
                lState.mAccType = null;
                lState.mIsPrivate = false;
            }
        }
    }

    private DialPadState getMatchedObjWithDataId(long aDataId) {
        for (DialPadState entry : this.mDialpadState.values()) {
            if (entry.mDataID == aDataId) {
                return entry;
            }
        }
        return null;
    }

    private void updateSpeedDialInfo(Cursor aCursor) {
        fillDefaultData();
        StringBuilder dataIds = new StringBuilder();
        this.mContactsAdded.clear();
        if (aCursor == null || !aCursor.moveToFirst()) {
            getActivity().invalidateOptionsMenu();
            this.mGridView.setAdapter(this.mViewAdapter);
            return;
        }
        do {
            DialPadState lSpeedDialDataObject = (DialPadState) this.mDialpadState.get(Integer.valueOf(aCursor.getInt(aCursor.getColumnIndex("key_number"))));
            if (lSpeedDialDataObject != null) {
                lSpeedDialDataObject.mDataID = aCursor.getLong(aCursor.getColumnIndex("phone_data_id"));
                lSpeedDialDataObject.mContactNumber = aCursor.getString(aCursor.getColumnIndex("number"));
                lSpeedDialDataObject.mDisplayName = lSpeedDialDataObject.mContactNumber;
                lSpeedDialDataObject.mHasContactForSpeedDial = true;
                if (lSpeedDialDataObject.mDataID != -1) {
                    dataIds.append(lSpeedDialDataObject.mDataID).append(",");
                }
            }
        } while (aCursor.moveToNext());
        if (TextUtils.isEmpty(dataIds.toString())) {
            getActivity().invalidateOptionsMenu();
            this.mGridView.setAdapter(this.mViewAdapter);
            return;
        }
        dataIds.setLength(dataIds.length() - 1);
        String lSelection = "_id IN (" + dataIds.toString() + ")";
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "lSelection --> " + lSelection);
        }
        Bundle bundle = new Bundle();
        bundle.putString("selection", lSelection);
        if (this.mIsLoaderStarted) {
            getLoaderManager().restartLoader(2, bundle, this.mSpeeddialLoaderListener);
        } else {
            getLoaderManager().initLoader(2, bundle, this.mSpeeddialLoaderListener);
        }
    }

    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onInflate");
        }
        super.onInflate(activity, attrs, savedInstanceState);
    }

    public void onAttach(Activity activity) {
        if (LOG_CONSTANT) {
            HwLog.d("SpeedDial", "onAttach");
        }
        super.onAttach(activity);
    }

    private void updateSpeedDialPredefinedCache() {
        String predefinedSpeedDialNumbers = SharePreferenceUtil.getDefaultSp_de(this.mContext).getString("speeddial_predefined_numbers", null);
        if (predefinedSpeedDialNumbers != null) {
            String[] pairs = predefinedSpeedDialNumbers.split(";");
            if (pairs != null) {
                for (String pair : pairs) {
                    String[] singlePair = pair.split(",");
                    if (singlePair != null && singlePair.length == 2) {
                        try {
                            boolean booleanValue;
                            int key = Integer.parseInt(singlePair[0]);
                            if (this.mCust != null) {
                                booleanValue = this.mCust.isDisableCustomService().booleanValue();
                            } else {
                                booleanValue = false;
                            }
                            if (!booleanValue) {
                                String temp = singlePair[1];
                                if (this.mCust != null) {
                                    temp = this.mCust.getPredefinedSpeedDialNumbersByMccmnc(temp);
                                }
                                if (!TextUtils.isEmpty(temp)) {
                                    this.mSpeedDialPredefinedNumberMap.put(Integer.valueOf(key), temp);
                                }
                            }
                        } catch (NumberFormatException e) {
                            HwLog.e("SpeedDial", "Problem with the speed dial predefined numbers");
                        }
                    }
                }
            }
        }
    }
}
