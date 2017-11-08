package com.android.contacts.hap.rcs.list;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.widget.ListView;
import android.widget.Toast;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.list.DataListAdapter;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.rcs.capability.CapabilityService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RcsContactDataMultiSelectFragment {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();
    private Set<String> keySet = new HashSet();
    private Context mContext;

    public RcsContactDataMultiSelectFragment(Context context) {
        this.mContext = context;
    }

    public boolean ifGroupMemberSizeValid(ContactMultiSelectionActivity activity) {
        if (!EmuiFeatureManager.isRcsFeatureEnable() || activity.getRcsCust() == null) {
            return true;
        }
        int fromActivity = activity.getRcsCust().getFromActivity();
        if (RcsContactsUtils.isValidFromActivity(fromActivity)) {
            int maxSize = 0;
            try {
                CapabilityService rcsService = CapabilityService.getInstance("contacts");
                if (rcsService != null) {
                    maxSize = rcsService.getMaxGroupMemberSize();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<String> selectNumberList = getActivityExistNumberList(activity, activity.mSelectedDataUris);
            ArrayList<String> existNumberList = activity.getRcsCust().getMemberListFromForward();
            int selectSize = selectNumberList.size();
            int existSize = 0;
            if (existNumberList != null) {
                existSize = existNumberList.size();
            }
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDataMultiSelectFragment", "group maxSize: " + maxSize + ", selectContactSize:" + selectSize + ", existContactSize:" + existSize);
            }
            switch (fromActivity) {
                case 1:
                    int addNumber = activity.getIntent().getBooleanExtra("is_self_chat", false) ? 1 : 2;
                    if (selectSize + addNumber > maxSize) {
                        showToast(Boolean.valueOf(true), maxSize);
                        return false;
                    } else if (selectSize + addNumber < 3) {
                        showToast(Boolean.valueOf(false), maxSize);
                        return false;
                    }
                    break;
                case 2:
                    if (selectSize + existSize > maxSize) {
                        showToast(Boolean.valueOf(true), maxSize);
                        return false;
                    } else if (selectSize + existSize < 3) {
                        showToast(Boolean.valueOf(false), maxSize);
                        return false;
                    }
                    break;
                case 3:
                    if (selectSize + existSize > maxSize) {
                        showToast(Boolean.valueOf(true), maxSize);
                        return false;
                    }
                    break;
            }
            return true;
        }
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDataMultiSelectFragment", "ifGroupMemberSizeValid fromActivity=" + fromActivity);
        }
        return true;
    }

    private void showToast(Boolean moreThanMax, int maxSize) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            if (moreThanMax.booleanValue()) {
                Toast.makeText(this.mContext, this.mContext.getResources().getQuantityString(R.plurals.rcsGroupMaxSize, maxSize, new Object[]{Integer.valueOf(maxSize)}), 0).show();
            } else {
                Toast.makeText(this.mContext, this.mContext.getResources().getQuantityString(R.plurals.rcsGroupMinSize, 3, new Object[]{Integer.valueOf(3)}), 0).show();
            }
        }
    }

    private HashMap<String, String> getMap(Activity activity) {
        if (((ContactMultiSelectionActivity) activity).getRcsCust() != null) {
            return ((ContactMultiSelectionActivity) activity).getRcsCust().getSelectedPersonMap();
        }
        return null;
    }

    public void addSelectedNameKey(String key) {
        if (this.isRcsOn && this.keySet != null) {
            this.keySet.add(key);
        }
    }

    public void addSelectedName(String selectedData, String selectedName, Activity activity) {
        if (this.isRcsOn && this.keySet != null && this.keySet.contains(selectedData)) {
            this.keySet.remove(selectedData);
            HashMap<String, String> map = getMap(activity);
            if (!(map == null || map.containsKey(selectedData))) {
                map.put(selectedData, selectedName);
            }
        }
    }

    public void rmSelectedName(String selectedData, Activity activity) {
        if (this.isRcsOn) {
            HashMap<String, String> map = getMap(activity);
            if (map != null) {
                map.remove(selectedData);
            }
        }
    }

    public void handleCustomizationsOnSaveInstanceState(Bundle outState, Activity activity) {
        if (this.isRcsOn) {
            HashMap<String, String> map = getMap(activity);
            if (map != null) {
                outState.putSerializable("person_map_key", map);
            }
        }
    }

    public void handleCustomizationsOnCreate(Bundle savedState, Activity activity) {
        if (!(!this.isRcsOn || ((ContactMultiSelectionActivity) activity).getRcsCust() == null || savedState.getSerializable("person_map_key") == null)) {
            ((ContactMultiSelectionActivity) activity).getRcsCust().setSelectedPersonMap((HashMap) savedState.getSerializable("person_map_key"));
        }
    }

    public int getFilterForCustomizationsRequest(int actionCode, int filterType) {
        if (!this.isRcsOn) {
            return filterType;
        }
        switch (actionCode) {
            case Place.TYPE_MEAL_DELIVERY /*60*/:
                return -3;
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                return -50;
            default:
                return filterType;
        }
    }

    public void doOperationForCustomizations(int actionCode, Activity activity, Fragment fragment) {
        if (this.isRcsOn) {
            switch (actionCode) {
                case Place.TYPE_MEAL_DELIVERY /*60*/:
                    try {
                        if (HwLog.HWDBG) {
                            HwLog.d("RcsContactDataMultiSelectFragment FileTrans ", "localActivityRef.mSelectedDataUris size : " + ((ContactMultiSelectionActivity) activity).mSelectedDataUris.size());
                        }
                        if (((ContactMultiSelectionActivity) activity).mSelectedDataUris.size() > 0) {
                            ArrayList<Uri> lData = new ArrayList(((ContactMultiSelectionActivity) activity).mSelectedDataUris);
                            ArrayList<String> addressList = new ArrayList(((ContactMultiSelectionActivity) activity).mSelectedData);
                            Bundle bundle = ((Activity) this.mContext).getIntent().getExtras();
                            if (bundle != null) {
                                bundle.putParcelableArrayList("MEMBER_LIST", lData);
                                bundle.putStringArrayList("ADDRESS_LIST", addressList);
                                bundle.putSerializable("PERSON_MAP", getMap(activity));
                                String mimeType = ((Activity) this.mContext).getIntent().getType();
                                if (mimeType != null) {
                                    bundle.putString("mimeType", mimeType);
                                }
                                Intent intent = new Intent();
                                intent.setAction("com.kris.contasts.file.trans.action");
                                intent.putExtras(bundle);
                                fragment.startActivity(intent);
                                ((ContactMultiSelectionActivity) activity).finish();
                                break;
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        HwLog.e("RcsContactDataMultiSelectFragment FileTrans ", "RCS send file using Contacts Failed! ");
                        break;
                    }
                    break;
            }
        }
    }

    private ArrayList<String> getActivityExistNumberList(ContactMultiSelectionActivity activity, Set<Uri> dataUri) {
        ArrayList<String> lsNumber = new ArrayList();
        if (activity == null || dataUri == null) {
            return lsNumber;
        }
        ContentResolver resolver = activity.getContentResolver();
        Cursor dataCursor = null;
        for (Uri uri : dataUri) {
            try {
                dataCursor = resolver.query(uri, null, null, null, null);
                while (dataCursor != null && dataCursor.moveToNext()) {
                    String number;
                    if (uri.toString().startsWith("content://call_log/calls")) {
                        number = PhoneNumberUtils.normalizeNumber(dataCursor.getString(dataCursor.getColumnIndex("number")));
                    } else {
                        number = PhoneNumberUtils.normalizeNumber(dataCursor.getString(dataCursor.getColumnIndex("data1")));
                    }
                    if (!lsNumber.contains(number)) {
                        boolean isContainSameNumber = false;
                        for (int i = 0; i < lsNumber.size(); i++) {
                            if (PhoneNumberUtils.compare((String) lsNumber.get(i), number)) {
                                isContainSameNumber = true;
                                break;
                            }
                        }
                        if (!isContainSameNumber) {
                            lsNumber.add(number);
                        }
                    }
                }
                if (dataCursor != null) {
                    dataCursor.close();
                }
            } catch (IllegalArgumentException ex) {
                HwLog.e("RcsContactDataMultiSelectFragment", "countActivityExistUri occur exception " + ex);
                if (dataCursor != null) {
                    dataCursor.close();
                }
            } catch (IllegalStateException e) {
                HwLog.e("RcsContactDataMultiSelectFragment", "getColumnIndex occur exception " + e);
                if (dataCursor != null) {
                    dataCursor.close();
                }
            } catch (Throwable th) {
                if (dataCursor != null) {
                    dataCursor.close();
                }
            }
        }
        return lsNumber;
    }

    public int resetSelectedDataInSearch(ListView mListView, DataListAdapter mAdapter, ContactMultiSelectionActivity localActivityRef, int mSelectedCountInSearch) {
        if (!this.isRcsOn) {
            return mSelectedCountInSearch;
        }
        for (int i = 1; i < mListView.getCount() - mListView.getFooterViewsCount(); i++) {
            Uri selectedUri = mAdapter.getSelectedDataUri(i - 1);
            mListView.setItemChecked(i, false);
            String selectedData = mAdapter.getSelectedData(i - 1);
            long selectedContactId = mAdapter.getContactId(i - 1);
            localActivityRef.mSelectedDataUris.remove(selectedUri);
            localActivityRef.mSelectedData.remove(selectedData);
            localActivityRef.mSelectedContactId.remove(Long.valueOf(selectedContactId));
            rmSelectedName(selectedData, localActivityRef);
            mSelectedCountInSearch--;
        }
        return mSelectedCountInSearch;
    }

    public void resetSelectedDataInNotSearch(ListView mListView, DataListAdapter mAdapter, ContactMultiSelectionActivity localActivityRef) {
        if (this.isRcsOn) {
            for (int i = 0; i < mListView.getCount() - mListView.getFooterViewsCount(); i++) {
                Uri selectedUri = mAdapter.getSelectedDataUri(i + 0);
                mListView.setItemChecked(i, false);
                String selectedData = mAdapter.getSelectedData(i + 0);
                long selectedContactId = mAdapter.getContactId(i + 0);
                localActivityRef.mSelectedDataUris.remove(selectedUri);
                localActivityRef.mSelectedData.remove(selectedData);
                localActivityRef.mSelectedContactId.remove(Long.valueOf(selectedContactId));
            }
        }
    }

    public boolean isFromActivityForGroupChat(ContactMultiSelectionActivity activity) {
        if (!this.isRcsOn || activity == null) {
            return false;
        }
        int fromActivity = activity.getRcsCust().getFromActivity();
        if (!RcsContactsUtils.isValidFromActivity(fromActivity)) {
            return false;
        }
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDataMultiSelectFragment", "ifGroupMemberSizeValid fromActivity=" + fromActivity);
        }
        return true;
    }
}
