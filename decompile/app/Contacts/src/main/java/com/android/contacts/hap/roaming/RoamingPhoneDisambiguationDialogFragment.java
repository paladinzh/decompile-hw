package com.android.contacts.hap.roaming;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;
import com.android.contacts.CallUtil;
import com.android.contacts.ChooseSubActivity;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class RoamingPhoneDisambiguationDialogFragment extends DialogFragment implements OnClickListener, OnDismissListener {
    private int mInteractionType;
    private boolean mIsAutoLearnRoamingNumber = false;
    private String mOriginalNumber;
    private List<RoamingPhoneItem> mPhoneList;
    private ListAdapter mPhonesAdapter;
    private RoamingDialPadDirectlyDataListener mRoamingDialpadDirectlyDataListener;

    public static void show(FragmentManager fragmentManager, ArrayList<RoamingPhoneItem> phoneList, int interactionType, RoamingLearnCarrier romaingLearn) {
        RoamingPhoneDisambiguationDialogFragment fragment = new RoamingPhoneDisambiguationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("phoneList", phoneList);
        bundle.putInt("interactionType", interactionType);
        if (romaingLearn != null) {
            bundle.putString("OriginalNumber", romaingLearn.getOriginalNumber());
            bundle.putBoolean("mIsAutoLearnRoamingNumber", romaingLearn.getOriginalNormalizedNumberIsNull());
        }
        fragment.setArguments(bundle);
        fragment.show(fragmentManager, "RoamingPhoneDisambiguationDialogFragment");
    }

    public static void show(FragmentManager fragmentManager, ArrayList<RoamingPhoneItem> phoneList, int interactionType, RoamingLearnCarrier romaingLearn, RoamingDialPadDirectlyDataListener roamingDialpadDirectlyDataListener) {
        RoamingPhoneDisambiguationDialogFragment fragment = new RoamingPhoneDisambiguationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("phoneList", phoneList);
        bundle.putInt("interactionType", interactionType);
        if (romaingLearn != null) {
            bundle.putString("OriginalNumber", romaingLearn.getOriginalNumber());
            bundle.putBoolean("mIsAutoLearnRoamingNumber", romaingLearn.getOriginalNormalizedNumberIsNull());
        }
        fragment.setArguments(bundle);
        fragment.setRoamingDialpadDirectlyDataListener(roamingDialpadDirectlyDataListener);
        fragment.show(fragmentManager, "RoamingPhoneDisambiguationDialogFragment");
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        this.mPhoneList = getArguments().getParcelableArrayList("phoneList");
        this.mInteractionType = getArguments().getInt("interactionType");
        this.mOriginalNumber = getArguments().getString("OriginalNumber");
        this.mIsAutoLearnRoamingNumber = getArguments().getBoolean("mIsAutoLearnRoamingNumber");
        this.mPhonesAdapter = new RoamingPhoneItemAdapter(activity, this.mPhoneList, this.mInteractionType);
        AlertDialog lDialog = new Builder(activity).setAdapter(this.mPhonesAdapter, this).setTitle(activity.getString(R.string.roaming_dial_title)).create();
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalRoamingDialogReference = lDialog;
        } else if (getActivity() instanceof ContactDetailActivity) {
            ((ContactDetailActivity) getActivity()).mGlobalRoamingDialogReference = lDialog;
        }
        return lDialog;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() == null) {
            return;
        }
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalRoamingDialogReference = null;
        } else if (getActivity() instanceof ContactDetailActivity) {
            ((ContactDetailActivity) getActivity()).mGlobalRoamingDialogReference = null;
        } else if (getActivity() instanceof ChooseSubActivity) {
            ((ChooseSubActivity) getActivity()).finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        Context activity = getActivity();
        if (activity != null) {
            if (this.mPhoneList != null && this.mPhoneList.size() > which && which >= 0) {
                RoamingPhoneItem phoneItem = (RoamingPhoneItem) this.mPhoneList.get(which);
                Uri numberUri = Uri.fromParts("tel", phoneItem.phoneNumber, null);
                if (this.mIsAutoLearnRoamingNumber) {
                    RoamingLearnManage.saveRoamingLearnCarrier(activity.getApplicationContext(), this.mOriginalNumber, phoneItem.phoneNumber);
                }
                switch (this.mInteractionType) {
                    case 3:
                        activity.startActivity(CallUtil.getCallIntent(phoneItem.phoneNumber, phoneItem.subScriptionId));
                        break;
                    case 258:
                        if (this.mRoamingDialpadDirectlyDataListener != null) {
                            this.mRoamingDialpadDirectlyDataListener.selectedDirectlyData(phoneItem.getPhoneNumber());
                            break;
                        }
                        break;
                    default:
                        CommonUtilMethods.dialNumber(activity, numberUri, phoneItem.subScriptionId, phoneItem.isFromDetail, phoneItem.sendReport);
                        break;
                }
            }
            dialog.dismiss();
        }
    }

    public void onPause() {
        dismiss();
        super.onPause();
    }

    public void setRoamingDialpadDirectlyDataListener(RoamingDialPadDirectlyDataListener roamingDialpadDirectlyDataListener) {
        this.mRoamingDialpadDirectlyDataListener = roamingDialpadDirectlyDataListener;
    }
}
