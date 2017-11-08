package com.android.contacts.list;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.Serializable;
import java.util.ArrayList;

public class ContactNumberSelectionDialogFragment extends DialogFragment {
    private NumberSelectionListener mListener;
    private ArrayList<NumberInfo> mNumberInfoList = new ArrayList();
    private CheckBox mSetDefaultCheckBox;

    public interface NumberSelectionListener {
        void onNumberSelected(NumberInfo numberInfo, boolean z);
    }

    private static class CheckedChangeListener implements OnCheckedChangeListener {
        private CheckedChangeListener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            if (isChecked) {
                i = 4028;
            } else {
                i = 4029;
            }
            StatisticalHelper.report(i);
        }
    }

    public static class NumberInfo implements Serializable {
        private static final long serialVersionUID = 1;
        public String customPhoneLabel;
        public long dataId;
        public String normalizedNumber;
        public String number;
        public int phoneType;
    }

    private class NumberSelectionAdapter extends BaseAdapter {

        private class ViewHolder {
            NumberInfo dataInfo;
            TextView label;
            TextView number;

            private ViewHolder() {
            }
        }

        private NumberSelectionAdapter() {
        }

        public int getCount() {
            return ContactNumberSelectionDialogFragment.this.mNumberInfoList.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= ContactNumberSelectionDialogFragment.this.mNumberInfoList.size()) {
                HwLog.e("ContactNumberSelectionDialogFragment", "position=" + position + " mNumberInfoList.size()==" + ContactNumberSelectionDialogFragment.this.mNumberInfoList.size());
                return null;
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ContactNumberSelectionDialogFragment.this.getActivity()).inflate(R.layout.contact_phone_number_selection_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) convertView.findViewById(R.id.contacts_phone_number);
                holder.label = (TextView) convertView.findViewById(R.id.contacts_phone_label);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            NumberInfo numberInfo = (NumberInfo) ContactNumberSelectionDialogFragment.this.mNumberInfoList.get(position);
            holder.number.setText(numberInfo.number);
            if (ContactNumberSelectionDialogFragment.this.getResources() != null) {
                holder.label.setText((String) Phone.getTypeLabel(ContactNumberSelectionDialogFragment.this.getResources(), numberInfo.phoneType, numberInfo.customPhoneLabel));
            }
            holder.dataInfo = numberInfo;
            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    StatisticalHelper.report(4030);
                    ContactNumberSelectionDialogFragment.this.mListener.onNumberSelected(holder.dataInfo, ContactNumberSelectionDialogFragment.this.mSetDefaultCheckBox.isChecked());
                    ContactNumberSelectionDialogFragment.this.dismissDialog();
                }
            });
            return convertView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            if (position >= ContactNumberSelectionDialogFragment.this.mNumberInfoList.size()) {
                return null;
            }
            return ContactNumberSelectionDialogFragment.this.mNumberInfoList.get(position);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        View custView = LayoutInflater.from(context).inflate(R.layout.favorites_select_number_dialog_view, null, false);
        this.mSetDefaultCheckBox = (CheckBox) custView.findViewById(R.id.set_to_default_check);
        this.mSetDefaultCheckBox.setOnCheckedChangeListener(new CheckedChangeListener());
        ListView list = (ListView) custView.findViewById(R.id.contact_number_select_list);
        list.setAdapter(new NumberSelectionAdapter());
        list.setFastScrollEnabled(true);
        return new Builder(context).setTitle(getResources().getString(R.string.call_other)).setView(custView, 0, 0, 0, 0).create();
    }

    public static void show(FragmentManager fragmentManager, ArrayList<NumberInfo> numberList, NumberSelectionListener listener) {
        ContactNumberSelectionDialogFragment fragment = new ContactNumberSelectionDialogFragment();
        fragment.setNumberInfoList(numberList);
        fragment.setListener(listener);
        try {
            fragment.show(fragmentManager, "number_selection_dialog");
        } catch (IllegalStateException e) {
            HwLog.e("ContactNumberSelectionDialogFragment", e.toString(), e);
        }
    }

    public void setNumberInfoList(ArrayList<NumberInfo> list) {
        this.mNumberInfoList = list;
    }

    public void setListener(NumberSelectionListener listener) {
        this.mListener = listener;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mNumberInfoList = (ArrayList) savedInstanceState.getSerializable("save_number_list");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("save_number_list", this.mNumberInfoList);
    }

    private void dismissDialog() {
        dismiss();
    }
}
