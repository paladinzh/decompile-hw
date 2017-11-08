package com.android.settings.datausage;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.CustomDialogPreference;
import com.android.settings.Utils;
import com.android.settings.datausage.TemplatePreference.NetworkServices;
import java.util.List;

public class CellDataPreference extends CustomDialogPreference implements TemplatePreference {
    public boolean mChecked;
    private final DataStateListener mListener = new DataStateListener() {
        public void onChange(boolean selfChange) {
            CellDataPreference.this.updateChecked();
        }
    };
    public boolean mMultiSimDialog;
    public int mSubId = -1;
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;

    public static abstract class DataStateListener extends ContentObserver {
        public DataStateListener() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void setListener(boolean listening, int subId, Context context) {
            if (listening) {
                Uri uri = Global.getUriFor("mobile_data");
                if (TelephonyManager.getDefault().getSimCount() != 1) {
                    uri = Global.getUriFor("mobile_data" + subId);
                }
                context.getContentResolver().registerContentObserver(uri, false, this);
                return;
            }
            context.getContentResolver().unregisterContentObserver(this);
        }
    }

    public static class CellDataState extends BaseSavedState {
        public static final Creator<CellDataState> CREATOR = new Creator<CellDataState>() {
            public CellDataState createFromParcel(Parcel source) {
                return new CellDataState(source);
            }

            public CellDataState[] newArray(int size) {
                return new CellDataState[size];
            }
        };
        public boolean mChecked;
        public boolean mMultiSimDialog;
        public int mSubId;

        public CellDataState(Parcelable base) {
            super(base);
        }

        public CellDataState(Parcel source) {
            boolean z;
            boolean z2 = true;
            super(source);
            if (source.readByte() != (byte) 0) {
                z = true;
            } else {
                z = false;
            }
            this.mChecked = z;
            if (source.readByte() == (byte) 0) {
                z2 = false;
            }
            this.mMultiSimDialog = z2;
            this.mSubId = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            if (this.mChecked) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (!this.mMultiSimDialog) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
            dest.writeInt(this.mSubId);
        }
    }

    public CellDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 16843629);
    }

    protected void onRestoreInstanceState(Parcelable s) {
        CellDataState state = (CellDataState) s;
        super.onRestoreInstanceState(state.getSuperState());
        this.mTelephonyManager = TelephonyManager.from(getContext());
        this.mChecked = state.mChecked;
        this.mMultiSimDialog = state.mMultiSimDialog;
        if (this.mSubId == -1) {
            this.mSubId = state.mSubId;
            setKey(getKey() + this.mSubId);
        }
        notifyChanged();
    }

    protected Parcelable onSaveInstanceState() {
        CellDataState state = new CellDataState(super.onSaveInstanceState());
        state.mChecked = this.mChecked;
        state.mMultiSimDialog = this.mMultiSimDialog;
        state.mSubId = this.mSubId;
        return state;
    }

    public void onAttached() {
        super.onAttached();
        this.mListener.setListener(true, this.mSubId, getContext());
    }

    public void onDetached() {
        this.mListener.setListener(false, this.mSubId, getContext());
        super.onDetached();
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        if (subId == -1) {
            throw new IllegalArgumentException("CellDataPreference needs a SubscriptionInfo");
        }
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        this.mTelephonyManager = TelephonyManager.from(getContext());
        if (this.mSubId == -1) {
            this.mSubId = subId;
            setKey(getKey() + subId);
        }
        updateChecked();
    }

    private void updateChecked() {
        setChecked(this.mTelephonyManager.getDataEnabled(this.mSubId));
    }

    protected void performClick(View view) {
        boolean z;
        Context context = getContext();
        if (this.mChecked) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, 178, z);
        if (this.mChecked) {
            SubscriptionInfo currentSir = this.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
            SubscriptionInfo nextSir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
            if (Utils.showSimCardTile(getContext()) && (nextSir == null || currentSir == null || currentSir.getSubscriptionId() != nextSir.getSubscriptionId())) {
                this.mMultiSimDialog = false;
                super.performClick(view);
            } else {
                setMobileDataEnabled(false);
                if (!(nextSir == null || currentSir == null || currentSir.getSubscriptionId() != nextSir.getSubscriptionId())) {
                    disableDataForOtherSubscriptions(this.mSubId);
                }
            }
        } else if (Utils.showSimCardTile(getContext())) {
            this.mMultiSimDialog = true;
            super.performClick(view);
        } else {
            setMobileDataEnabled(true);
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        this.mTelephonyManager.setDataEnabled(this.mSubId, enabled);
        setChecked(enabled);
    }

    private void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            notifyChanged();
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(16908352);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(this.mChecked);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        if (this.mMultiSimDialog) {
            showMultiSimDialog(builder, listener);
        } else {
            showDisableDialog(builder, listener);
        }
    }

    private void showDisableDialog(Builder builder, OnClickListener listener) {
        builder.setTitle(null).setMessage(2131626307).setPositiveButton(17039370, listener).setNegativeButton(17039360, null);
    }

    private void showMultiSimDialog(Builder builder, OnClickListener listener) {
        String previousName;
        Object displayName;
        SubscriptionInfo currentSir = this.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
        SubscriptionInfo nextSir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (nextSir == null) {
            previousName = getContext().getResources().getString(2131626635);
        } else {
            previousName = nextSir.getDisplayName().toString();
        }
        builder.setTitle(2131625206);
        Context context = getContext();
        Object[] objArr = new Object[2];
        if (currentSir != null) {
            displayName = currentSir.getDisplayName();
        } else {
            displayName = null;
        }
        objArr[0] = String.valueOf(displayName);
        objArr[1] = previousName;
        builder.setMessage(context.getString(2131625207, objArr));
        builder.setPositiveButton(2131624573, listener);
        builder.setNegativeButton(2131624572, null);
    }

    private void disableDataForOtherSubscriptions(int subId) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (subInfo.getSubscriptionId() != subId) {
                    this.mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(), false);
                }
            }
        }
    }

    protected void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            if (this.mMultiSimDialog) {
                this.mSubscriptionManager.setDefaultDataSubId(this.mSubId);
                setMobileDataEnabled(true);
                disableDataForOtherSubscriptions(this.mSubId);
            } else {
                setMobileDataEnabled(false);
            }
        }
    }
}
