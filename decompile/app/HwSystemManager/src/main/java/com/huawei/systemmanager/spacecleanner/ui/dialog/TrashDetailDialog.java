package com.huawei.systemmanager.spacecleanner.ui.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public class TrashDetailDialog extends DialogFragment {
    ITrashItem mItem;

    public TrashDetailDialog(ITrashItem item) {
        this.mItem = item;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = "";
        if (this.mItem != null) {
            title = this.mItem.getName();
        }
        String msg = getDialogMessge();
        ListAdapter adapter = getAdapter(msg);
        Builder builder = new Builder(getActivity());
        builder.setTitle(title);
        if (adapter == null) {
            builder.setMessage(msg);
        } else {
            ListView listView = (ListView) LayoutInflater.from(getActivity()).inflate(R.layout.trash_detail_dialog_listview, null, false);
            listView.setAdapter(adapter);
            listView.setOverScrollMode(1);
            builder.setView(listView);
        }
        builder.setPositiveButton(R.string.space_clean_list_detail_button, null);
        return builder.show();
    }

    private ListAdapter getAdapter(String msg) {
        if (this.mItem == null || TextUtils.isEmpty(msg)) {
            return null;
        }
        ListAdapter adapter = null;
        switch (this.mItem.getTrashType()) {
            case 4:
            case 8192:
            case 16384:
            case 65536:
                adapter = new ArrayAdapter(getActivity(), R.layout.trash_detail_list_item, msg.split("\n"));
                break;
        }
        return adapter;
    }

    private String getDialogMessge() {
        if (this.mItem == null) {
            return "";
        }
        String result;
        Context ctx = GlobalContext.getContext();
        switch (this.mItem.getTrashType()) {
            case 4:
            case 8192:
            case 16384:
            case 65536:
                new StringBuilder().append("\n").append(this.mItem.getTrashPath());
                result = ctx.getString(R.string.space_clean_list_path_detail, new Object[]{value.toString()});
                break;
            default:
                result = ctx.getString(R.string.space_clean_list_size_detail, new Object[]{FileUtil.getFileSize(this.mItem.getTrashSize())});
                break;
        }
        return result;
    }
}
