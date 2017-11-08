package com.android.contacts.calllog;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.calllog.CallRecord;
import com.android.contacts.hap.calllog.CallRecord.CallRecordItem;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.PhoneCapabilityTester;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class CallDetailHistoryAdapter extends BaseAdapter {
    private final CallTypeHelper mCallTypeHelper;
    private final Context mContext;
    private final View mControls;
    HwCustCallDetailHistoryAdapter mCustCallDetailHistoryAdapter;
    private OnFocusChangeListener mHeaderFocusChangeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                CallDetailHistoryAdapter.this.mControls.requestFocus();
            }
        }
    };
    private boolean mIsRingTimesEnabled;
    private final LayoutInflater mLayoutInflater;
    private PhoneCallDetails[] mPhoneCallDetails;
    private final boolean mShowCallAndSms;
    private boolean mShowCallPlus;
    private final boolean mShowVoicemail;

    private static class ViewHolder {
        ImageView callRecord;
        CallTypeIconsView callTypeIconView;
        TextView callTypeTextView;
        TextView dateView;
        TextView durationView;
        TextView ringTimesView;

        private ViewHolder() {
        }
    }

    private CallDetailHistoryAdapter(Context context, LayoutInflater layoutInflater, CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails, boolean showVoicemail, boolean showCallAndSms, View controls) {
        this.mContext = context;
        this.mLayoutInflater = layoutInflater;
        this.mCallTypeHelper = callTypeHelper;
        this.mPhoneCallDetails = phoneCallDetails;
        this.mShowVoicemail = showVoicemail;
        this.mShowCallAndSms = showCallAndSms;
        this.mControls = controls;
        this.mIsRingTimesEnabled = EmuiFeatureManager.isRingTimesDisplayEnabled(this.mContext);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCustCallDetailHistoryAdapter = (HwCustCallDetailHistoryAdapter) HwCustUtils.createObj(HwCustCallDetailHistoryAdapter.class, new Object[0]);
        }
    }

    public static CallDetailHistoryAdapter newInstance(Context context, LayoutInflater layoutInflater, CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails, boolean showVoicemail, boolean showCallAndSms, View controls) {
        return new CallDetailHistoryAdapter(context, layoutInflater, callTypeHelper, phoneCallDetails, showVoicemail, showCallAndSms, controls);
    }

    public int getCount() {
        return this.mPhoneCallDetails.length + 1;
    }

    public Object getItem(int position) {
        if (position == 0) {
            return null;
        }
        return this.mPhoneCallDetails[position - 1];
    }

    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        }
        return (long) (position - 1);
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            View header;
            if (convertView == null) {
                header = this.mLayoutInflater.inflate(R.layout.call_detail_history_header, parent, false);
            } else {
                header = convertView;
            }
            View callUnknownContactButtonContainer = header.findViewById(R.id.call_unknown_contact_button_container);
            View callUnknownContactTitleContainer = header.findViewById(R.id.call_unknown_contact_title_container);
            View voicemailContainer = header.findViewById(R.id.header_voicemail_container);
            View callIPContainer = header.findViewById(R.id.call_ip_title_container);
            View callPlusContainer = header.findViewById(R.id.call_plus_title_container);
            View callAndSmsContainer = header.findViewById(R.id.header_call_and_sms_container);
            View callLogsTitleContainer = header.findViewById(R.id.call_logs_title_container);
            if (this.mShowCallAndSms) {
                voicemailContainer.setVisibility(this.mShowVoicemail ? 0 : 8);
                callIPContainer.setVisibility(DialpadFragment.getIsIpCallEnabled() ? 0 : 8);
                callPlusContainer.setVisibility(this.mShowCallPlus ? 0 : 8);
                PhoneCallDetails firstDetails = this.mPhoneCallDetails[0];
                boolean isNameEmpty = TextUtils.isEmpty(firstDetails.name);
                boolean isContact;
                if (firstDetails.contactUri == null) {
                    isContact = false;
                } else {
                    isContact = true;
                }
                callAndSmsContainer.setVisibility(0);
                if (2 == this.mContext.getResources().getConfiguration().orientation) {
                    callUnknownContactButtonContainer.setVisibility(8);
                    callUnknownContactTitleContainer.setVisibility(8);
                    header.findViewById(R.id.call_detail_photo_header).setVisibility(8);
                } else {
                    callUnknownContactButtonContainer.setVisibility(0);
                    if (isNameEmpty || !isContact) {
                        callUnknownContactTitleContainer.setVisibility(0);
                    } else {
                        callUnknownContactTitleContainer.setVisibility(8);
                    }
                }
            } else {
                callUnknownContactButtonContainer.setVisibility(8);
                callUnknownContactTitleContainer.setVisibility(8);
                voicemailContainer.setVisibility(8);
                callIPContainer.setVisibility(8);
                callPlusContainer.setVisibility(8);
                callLogsTitleContainer.setVisibility(0);
                callAndSmsContainer.setVisibility(8);
                if (2 == this.mContext.getResources().getConfiguration().orientation) {
                    header.findViewById(R.id.call_detail_photo_header).setVisibility(8);
                }
            }
            header.setFocusable(true);
            header.setOnFocusChangeListener(this.mHeaderFocusChangeListener);
            return header;
        }
        View result;
        ViewHolder viewHolder;
        if (convertView == null) {
            result = this.mLayoutInflater.inflate(R.layout.call_detail_history_item, parent, false);
            ViewHolder viewHolder2 = new ViewHolder();
            viewHolder2.callTypeIconView = (CallTypeIconsView) result.findViewById(R.id.call_type_icon);
            viewHolder2.callTypeTextView = (TextView) result.findViewById(R.id.call_type_text);
            viewHolder2.dateView = (TextView) result.findViewById(R.id.contact_date);
            viewHolder2.durationView = (TextView) result.findViewById(R.id.duration);
            viewHolder2.ringTimesView = (TextView) result.findViewById(R.id.ring_times);
            viewHolder2.callRecord = (ImageView) result.findViewById(R.id.call_record);
            result.setTag(viewHolder2);
        } else {
            result = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ImageView callRecord = viewHolder.callRecord;
        PhoneCallDetails details = this.mPhoneCallDetails[position - 1];
        setupCallRecord(callRecord, details);
        CallTypeIconsView callTypeIconView = viewHolder.callTypeIconView;
        TextView callTypeTextView = viewHolder.callTypeTextView;
        TextView dateView = viewHolder.dateView;
        TextView durationView = viewHolder.durationView;
        TextView ringTimesView = viewHolder.ringTimesView;
        if (QueryUtil.isSupportDualSim() && SimFactoryManager.isDualSim()) {
            ImageView image = (ImageView) result.findViewById(R.id.call_type_image);
            image.setVisibility(0);
            if (SimFactoryManager.getSimCombination() == 2) {
                if (details.subscriptionID == 0) {
                    image.setImageResource(R.drawable.stat_sys_sim1);
                    image.setContentDescription(this.mContext.getString(R.string.str_filter_sim1));
                } else if (details.subscriptionID == 1) {
                    image.setImageResource(R.drawable.stat_sys_sim2);
                    image.setContentDescription(this.mContext.getString(R.string.str_filter_sim2));
                } else if (details.subscriptionID == 2) {
                    image.setImageResource(R.drawable.fastscroll_familyname_normal);
                    image.setContentDescription(this.mContext.getString(R.string.content_description_card_type_g_roaming));
                }
            } else if (details.subscriptionID == 0) {
                image.setImageResource(R.drawable.stat_sys_sim1);
                image.setContentDescription(this.mContext.getString(R.string.str_filter_sim1));
            } else {
                image.setImageResource(R.drawable.stat_sys_sim2);
                image.setContentDescription(this.mContext.getString(R.string.str_filter_sim2));
            }
        }
        int callType = details.callTypes[0];
        callTypeIconView.clear();
        callTypeIconView.add(callType, details.getCallsTypeFeatures(), details.mReadState);
        if (ringTimesView != null) {
            if (this.mIsRingTimesEnabled && CommonUtilMethods.isMissedType(callType)) {
                ringTimesView.setText(String.format(this.mContext.getResources().getQuantityText(R.plurals.contacts_ring_times, details.mRingTimes).toString(), new Object[]{Integer.valueOf(details.mRingTimes)}));
                ringTimesView.setVisibility(0);
            } else {
                ringTimesView.setVisibility(8);
            }
        }
        callTypeTextView.setText(this.mCallTypeHelper.getCallTypeText(callType));
        dateView.setText(DateUtils.formatDateTime(this.mContext, details.date, com.android.contacts.util.DateUtils.getDefaultDateTimeFormat()));
        if (CommonUtilMethods.isMissedType(callType) || callType == 4 || PhoneCapabilityTester.isCallDurationHid()) {
            durationView.setVisibility(8);
        } else {
            durationView.setVisibility(0);
            durationView.setText(" : " + formatDuration(details.duration));
        }
        if (this.mCustCallDetailHistoryAdapter != null) {
            this.mCustCallDetailHistoryAdapter.hideCallTypeAndDurationView(callTypeTextView, durationView);
        }
        return result;
    }

    private void setupCallRecord(ImageView callRecord, PhoneCallDetails details) {
        if (callRecord != null && details != null) {
            CallRecordItem[] items = details.mCallRecordItems;
            if (items == null || items.length <= 0) {
                callRecord.setVisibility(8);
            } else {
                callRecord.setVisibility(0);
            }
        }
    }

    private String formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long hours = 0;
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            if (minutes >= 60) {
                hours = minutes / 60;
                minutes -= 60 * hours;
                elapsedSeconds -= (60 * hours) * 60;
            }
            elapsedSeconds -= 60 * minutes;
        }
        long seconds = elapsedSeconds;
        String min = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Minutes, (int) minutes, new Object[]{Long.valueOf(minutes)});
        String sec = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Seconds, (int) seconds, new Object[]{Long.valueOf(seconds)});
        if (hours >= 1) {
            String h = this.mContext.getResources().getQuantityString(R.plurals.callDetailsDurationFormatHours_Hours, (int) hours, new Object[]{Long.valueOf(hours)});
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{h, min, sec});
        } else if (minutes < 1) {
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", "", sec});
        } else {
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", min, sec});
        }
    }

    public void removeSingleCallDetail(int deletedIndex) {
        if (-1 != deletedIndex) {
            ArrayList<PhoneCallDetails> lTempDetails = new ArrayList();
            for (int i = 0; i < this.mPhoneCallDetails.length; i++) {
                if (i != deletedIndex) {
                    lTempDetails.add(this.mPhoneCallDetails[i]);
                }
            }
            this.mPhoneCallDetails = new PhoneCallDetails[lTempDetails.size()];
            this.mPhoneCallDetails = (PhoneCallDetails[]) lTempDetails.toArray(this.mPhoneCallDetails);
            lTempDetails.clear();
            notifyDataSetChanged();
        }
    }

    public void setupCallRecords(final String number) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                if (!(number == null || number.equals(""))) {
                    CallRecord cr = new CallRecord(CallDetailHistoryAdapter.this.mContext, number);
                    for (PhoneCallDetails pcd : CallDetailHistoryAdapter.this.mPhoneCallDetails) {
                        if (pcd != null && PhoneNumberUtils.compare(number, pcd.number.toString())) {
                            long begin = pcd.date;
                            pcd.mCallRecordItems = cr.getCallRecordItems(begin, begin + (pcd.duration * 1000), number);
                        }
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                CallDetailHistoryAdapter.this.notifyDataSetChanged();
            }
        }.execute(new Void[0]);
    }
}
