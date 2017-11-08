package com.android.settings.sim;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.appcompat.R$id;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimDialogActivity extends Activity {
    public static String DIALOG_TYPE_KEY = "dialog_type";
    public static String PREFERRED_SIM = "preferred_sim";

    private class SelectAccountListAdapter extends ArrayAdapter<String> {
        private final float OPACITY = 0.54f;
        private Context mContext;
        private int mDialogId;
        private int mResId;
        private List<SubscriptionInfo> mSubInfoList;

        private class ViewHolder {
            ImageView icon;
            TextView summary;
            TextView title;

            private ViewHolder() {
            }
        }

        public SelectAccountListAdapter(List<SubscriptionInfo> subInfoList, Context context, int resource, String[] arr, int dialogId) {
            super(context, resource, arr);
            this.mContext = context;
            this.mResId = resource;
            this.mDialogId = dialogId;
            this.mSubInfoList = subInfoList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            if (convertView == null) {
                rowView = inflater.inflate(this.mResId, null);
                holder = new ViewHolder();
                holder.title = (TextView) rowView.findViewById(R$id.title);
                holder.summary = (TextView) rowView.findViewById(2131886387);
                holder.icon = (ImageView) rowView.findViewById(2131886147);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) convertView.getTag();
            }
            SubscriptionInfo sir = (SubscriptionInfo) this.mSubInfoList.get(position);
            if (sir == null) {
                holder.title.setText((CharSequence) getItem(position));
                holder.summary.setText("");
                holder.icon.setImageDrawable(SimDialogActivity.this.getResources().getDrawable(2130838268));
                holder.icon.setAlpha(0.54f);
            } else {
                holder.title.setText(sir.getDisplayName());
                holder.summary.setText(sir.getNumber());
                holder.icon.setImageBitmap(sir.createIconBitmap(this.mContext));
            }
            return rowView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        int dialogType = extras.getInt(DIALOG_TYPE_KEY, -1);
        switch (dialogType) {
            case 0:
            case 1:
            case 2:
                createDialog(this, dialogType).show();
                break;
            case 3:
                displayPreferredDialog(extras.getInt(PREFERRED_SIM));
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type " + dialogType + " sent.");
        }
    }

    private void displayPreferredDialog(int slotId) {
        Resources res = getResources();
        final Context context = getApplicationContext();
        final SubscriptionInfo sir = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(slotId);
        if (sir != null) {
            Builder alertDialogBuilder = new Builder(this);
            alertDialogBuilder.setTitle(2131625208);
            alertDialogBuilder.setMessage(res.getString(2131625209, new Object[]{sir.getDisplayName()}));
            alertDialogBuilder.setPositiveButton(2131624348, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    int subId = sir.getSubscriptionId();
                    PhoneAccountHandle phoneAccountHandle = SimDialogActivity.this.subscriptionIdToPhoneAccountHandle(subId);
                    SimDialogActivity.setDefaultDataSubId(context, subId);
                    SimDialogActivity.setDefaultSmsSubId(context, subId);
                    SimDialogActivity.this.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    SimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.setNegativeButton(2131624349, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SimDialogActivity.this.finish();
                }
            });
            alertDialogBuilder.create().show();
            return;
        }
        finish();
    }

    private static void setDefaultDataSubId(Context context, int subId) {
        SubscriptionManager.from(context).setDefaultDataSubId(subId);
        Toast.makeText(context, 2131626607, 1).show();
    }

    private static void setDefaultSmsSubId(Context context, int subId) {
        SubscriptionManager.from(context).setDefaultSmsSubId(subId);
    }

    private void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccount) {
        TelecomManager.from(this).setUserSelectedOutgoingPhoneAccount(phoneAccount);
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(int subId) {
        TelecomManager telecomManager = TelecomManager.from(this);
        TelephonyManager telephonyManager = TelephonyManager.from(this);
        Iterator<PhoneAccountHandle> phoneAccounts = telecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            PhoneAccountHandle phoneAccountHandle = (PhoneAccountHandle) phoneAccounts.next();
            if (subId == telephonyManager.getSubIdForPhoneAccount(telecomManager.getPhoneAccount(phoneAccountHandle))) {
                return phoneAccountHandle;
            }
        }
        return null;
    }

    public Dialog createDialog(Context context, int id) {
        List list;
        ArrayList<String> list2 = new ArrayList();
        List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        int selectableSubInfoLength = subInfoList == null ? 0 : subInfoList.size();
        final int i = id;
        final List<SubscriptionInfo> list3 = subInfoList;
        final Context context2 = context;
        OnClickListener anonymousClass3 = new OnClickListener() {
            public void onClick(DialogInterface dialog, int value) {
                switch (i) {
                    case 0:
                        SimDialogActivity.setDefaultDataSubId(context2, ((SubscriptionInfo) list3.get(value)).getSubscriptionId());
                        break;
                    case 1:
                        SimDialogActivity.this.setUserSelectedOutgoingPhoneAccount(value < 1 ? null : (PhoneAccountHandle) TelecomManager.from(context2).getCallCapablePhoneAccounts().get(value - 1));
                        break;
                    case 2:
                        SimDialogActivity.setDefaultSmsSubId(context2, ((SubscriptionInfo) list3.get(value)).getSubscriptionId());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid dialog type " + i + " in SIM dialog.");
                }
                SimDialogActivity.this.finish();
            }
        };
        OnKeyListener anonymousClass4 = new OnKeyListener() {
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    SimDialogActivity.this.finish();
                }
                return true;
            }
        };
        ArrayList<SubscriptionInfo> callsSubInfoList = new ArrayList();
        if (id == 1) {
            TelecomManager telecomManager = TelecomManager.from(context);
            TelephonyManager telephonyManager = TelephonyManager.from(context);
            Iterator<PhoneAccountHandle> phoneAccounts = telecomManager.getCallCapablePhoneAccounts().listIterator();
            list2.add(getResources().getString(2131626634));
            callsSubInfoList.add(null);
            while (phoneAccounts.hasNext()) {
                PhoneAccount phoneAccount = telecomManager.getPhoneAccount((PhoneAccountHandle) phoneAccounts.next());
                list2.add((String) phoneAccount.getLabel());
                int subId = telephonyManager.getSubIdForPhoneAccount(phoneAccount);
                if (subId != -1) {
                    callsSubInfoList.add(SubscriptionManager.from(context).getActiveSubscriptionInfo(subId));
                } else {
                    callsSubInfoList.add(null);
                }
            }
        } else {
            for (int i2 = 0; i2 < selectableSubInfoLength; i2++) {
                CharSequence displayName = ((SubscriptionInfo) subInfoList.get(i2)).getDisplayName();
                if (displayName == null) {
                    displayName = "";
                }
                list2.add(displayName.toString());
            }
        }
        String[] arr = (String[]) list2.toArray(new String[0]);
        Builder builder = new Builder(context);
        if (id == 1) {
            list = callsSubInfoList;
        } else {
            List<SubscriptionInfo> list4 = subInfoList;
        }
        ListAdapter adapter = new SelectAccountListAdapter(list, builder.getContext(), 2130969093, arr, id);
        switch (id) {
            case 0:
                builder.setTitle(2131626606);
                break;
            case 1:
                builder.setTitle(2131626608);
                break;
            case 2:
                builder.setTitle(2131626618);
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type " + id + " in SIM dialog.");
        }
        Dialog dialog = builder.setAdapter(adapter, anonymousClass3).create();
        dialog.setOnKeyListener(anonymousClass4);
        dialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                SimDialogActivity.this.finish();
            }
        });
        return dialog;
    }
}
