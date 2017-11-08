package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwCustHwMessageUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;

public class AssignRingTonePreference extends RingtonePreference {
    private static HwCustHwMessageUtils mHwCustMessageUtils = ((HwCustHwMessageUtils) HwCustUtils.createObj(HwCustHwMessageUtils.class, new Object[0]));
    private boolean accessHwThemeManagerRingtone = true;
    OnClickListener dialogInterface = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            int requestCode;
            switch (which) {
                case 0:
                    Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
                    AssignRingTonePreference.this.onPrepareRingtonePickerIntent(intent);
                    requestCode = 12;
                    if (AssignRingTonePreference.this.mCurrentSubId == 0) {
                        requestCode = 14;
                    } else if (AssignRingTonePreference.this.mCurrentSubId == 1) {
                        requestCode = 15;
                    }
                    ((Activity) AssignRingTonePreference.this.getContext()).startActivityForResult(intent, requestCode);
                    return;
                case 1:
                    Intent musicIntent = new Intent("android.intent.action.PICK");
                    musicIntent.setData(Uri.parse("content://media/external/audio/media"));
                    musicIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", AssignRingTonePreference.this.onRestoreRingtone());
                    requestCode = 13;
                    if (AssignRingTonePreference.this.mCurrentSubId == 0) {
                        requestCode = 16;
                    } else if (AssignRingTonePreference.this.mCurrentSubId == 1) {
                        requestCode = 17;
                    }
                    ((Activity) AssignRingTonePreference.this.getContext()).startActivityForResult(musicIntent, requestCode);
                    return;
                default:
                    return;
            }
        }
    };
    private AlertDialog mAlertDialog;
    private int mCurrentSubId = -1;
    private OnSummaryChangeListener mOnSummaryChangeListener;

    public interface OnSummaryChangeListener {
        void onSummaryChange(Preference preference, CharSequence charSequence);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            boolean z = true;
            super(source);
            if (source.readInt() != 1) {
                z = false;
            }
            this.isDialogShowing = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AssignRingTonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        String[] rinttonetype = context.getResources().getStringArray(R.array.ringtone_filters);
        Builder builder = new Builder(context);
        builder.setTitle(getTitle());
        builder.setIcon(17301659);
        builder.setCancelable(true);
        builder.setItems(rinttonetype, this.dialogInterface);
        this.mAlertDialog = builder.create();
    }

    public static Uri getCustomRingtoneUri(Context context) {
        RingtoneManager ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setIncludeDrm(true);
        ringtoneManager.setType(2);
        Cursor cursor = ringtoneManager.getCursor();
        String defaultTitle = MmsConfig.getDefaultNotificationRingtone();
        Uri uri = null;
        do {
            try {
                if (!cursor.moveToNext()) {
                    break;
                }
            } catch (Exception e) {
                MLog.e("AssignRingTonePreference", "can not find the default ringtone uri!");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } while (!defaultTitle.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("title"))));
        uri = ringtoneManager.getRingtoneUri(cursor.getPosition());
        if (cursor != null) {
            cursor.close();
        }
        return uri;
    }

    protected void onClick() {
        int requestCode;
        if (MmsConfig.getEnableOptionalRingtone()) {
            Intent musicIntent = new Intent("android.intent.action.RINGTONE_PICKER");
            musicIntent.addCategory("android.intent.category.HWRING");
            musicIntent.putExtra("android.intent.extra.ringtone.TYPE", 2);
            String followNotificationKeyStr = "pref_mms_is_follow_notification";
            if (MessageUtils.isMultiSimEnabled()) {
                if (this.mCurrentSubId == 0) {
                    followNotificationKeyStr = "pref_mms_is_follow_notification_sub0";
                } else {
                    followNotificationKeyStr = "pref_mms_is_follow_notification_sub1";
                }
            }
            boolean isFollowNotification = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(followNotificationKeyStr, true);
            String currentUriStr = null;
            if (!isFollowNotification) {
                currentUriStr = MmsConfig.getRingToneUriFromDatabase(getContext(), this.mCurrentSubId);
                if (!("null".equalsIgnoreCase(currentUriStr) || TextUtils.isEmpty(currentUriStr) || MessagingNotification.isUriAvalible(getContext(), currentUriStr))) {
                    if (HwMessageUtils.getDefaultFollowNotificationState(getContext()) == 1) {
                        isFollowNotification = true;
                    } else {
                        currentUriStr = MessageUtils.getDefaultRintoneStr(getContext());
                        if (!(TextUtils.isEmpty(currentUriStr) || MessagingNotification.isUriAvalible(getContext(), currentUriStr))) {
                            isFollowNotification = true;
                        }
                    }
                }
            }
            Parcelable parse = (currentUriStr == null || "null".equals(currentUriStr)) ? null : Uri.parse(currentUriStr);
            musicIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", parse);
            musicIntent.putExtra("is_follow_notification", isFollowNotification);
            requestCode = 20;
            try {
                if (this.mCurrentSubId == 0) {
                    requestCode = 21;
                } else if (this.mCurrentSubId == 1) {
                    requestCode = 22;
                }
                ((Activity) getContext()).startActivityForResult(musicIntent, requestCode);
            } catch (Exception e) {
                MLog.e("AssignRingTonePreference", "can not find the hw ringtone", (Throwable) e);
                this.accessHwThemeManagerRingtone = false;
                this.mAlertDialog.show();
            }
        } else {
            requestCode = 12;
            if (this.mCurrentSubId == 0) {
                requestCode = 14;
            } else if (this.mCurrentSubId == 1) {
                requestCode = 15;
            }
            Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
            onPrepareRingtonePickerIntent(intent);
            ((Activity) getContext()).startActivityForResult(intent, requestCode);
        }
        StatisticalHelper.incrementReportCount(getContext(), 2067);
    }

    public void changeRingtone(Uri ringtoneUri) {
        onSaveRingtone(ringtoneUri);
        setRingtoneSummary(onRestoreRingtone());
        if (this.mOnSummaryChangeListener != null) {
            this.mOnSummaryChangeListener.onSummaryChange(this, getSummary());
        }
    }

    private void setRingtoneSummary(Uri uri) {
        if (uri == null) {
            setSummary(R.string.pref_summary_silent_ringstone);
            return;
        }
        String[] uriStrs = uri.toString().split(";");
        String uriStr = uri.toString();
        if (this.mCurrentSubId >= 0 && uriStrs.length > this.mCurrentSubId) {
            uriStr = uriStrs[this.mCurrentSubId];
        }
        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(Uri.parse(uriStr), new String[]{"_id", "_data", "title"}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                onSaveRingtone(RingtoneManager.getDefaultUri(2));
                setSummary(R.string.pref_summary_ringstone);
            } else {
                setSummary(cursor.getString(2));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            setSummary(R.string.pref_summary_ringstone);
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        String str = null;
        super.onActivityResult(requestCode, resultCode, data);
        String followNotificationKey = "pref_mms_is_follow_notification";
        if (MessageUtils.isMultiSimEnabled()) {
            if (this.mCurrentSubId == 0) {
                followNotificationKey = "pref_mms_is_follow_notification_sub0";
            } else {
                followNotificationKey = "pref_mms_is_follow_notification_sub1";
            }
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if (-1 == resultCode) {
            if (data != null) {
                if (!(requestCode == 20 || requestCode == 21)) {
                    if (requestCode == 22) {
                    }
                    if ((requestCode != 20 || this.mCurrentSubId == -1) && ((requestCode != 21 || this.mCurrentSubId == 0) && ((requestCode != 22 || this.mCurrentSubId == 1) && ((requestCode != 12 || this.mCurrentSubId == -1) && ((requestCode != 14 || this.mCurrentSubId == 0) && ((requestCode != 15 || this.mCurrentSubId == 1) && ((requestCode != 13 || this.mCurrentSubId == -1) && ((requestCode != 16 || this.mCurrentSubId == 0) && (requestCode != 17 || this.mCurrentSubId == 1))))))))) {
                        Uri uri;
                        switch (requestCode) {
                            case 12:
                            case 14:
                            case 15:
                                if (mHwCustMessageUtils != null && mHwCustMessageUtils.getEnableCotaFeature()) {
                                    editor.putBoolean("key_user_has_selected_ring", true).commit();
                                }
                                uri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                                changeRingtone(uri);
                                MmsConfig.setRingToneUriToDatabase(getContext(), uri == null ? null : uri.toString(), this.mCurrentSubId);
                                break;
                            case 13:
                            case 16:
                            case 17:
                                uri = data.getData();
                                if (uri != null) {
                                    changeRingtone(uri);
                                    MmsConfig.setRingToneUriToDatabase(getContext(), uri.toString(), this.mCurrentSubId);
                                    break;
                                }
                                break;
                            case 20:
                            case 21:
                            case 22:
                                if (mHwCustMessageUtils != null && mHwCustMessageUtils.getEnableCotaFeature()) {
                                    editor.putBoolean("key_user_has_selected_ring", true).commit();
                                }
                                boolean isFollowNotification = data.getBooleanExtra("is_follow_notification", false);
                                uri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                                if (uri == null) {
                                    uri = data.getData();
                                }
                                if (isFollowNotification) {
                                    setSummary(R.string.mms_follow_notification_tone);
                                    editor.putBoolean(followNotificationKey, true);
                                    StatisticalHelper.incrementReportCount(getContext(), 2069);
                                } else {
                                    changeRingtone(uri);
                                    Context context = getContext();
                                    if (uri != null) {
                                        str = uri.toString();
                                    }
                                    MmsConfig.setRingToneUriToDatabase(context, str, this.mCurrentSubId);
                                    editor.putBoolean(followNotificationKey, false);
                                    if (uri == null) {
                                        StatisticalHelper.incrementReportCount(getContext(), 2068);
                                    } else {
                                        StatisticalHelper.incrementReportCount(getContext(), 2070);
                                    }
                                }
                                editor.commit();
                                break;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mAlertDialog == null || !this.mAlertDialog.isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            this.mAlertDialog.show();
        }
    }

    public void setCurrentSubId(int subId) {
        this.mCurrentSubId = subId;
    }
}
