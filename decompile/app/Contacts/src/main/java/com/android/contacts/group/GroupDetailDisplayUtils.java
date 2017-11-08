package com.android.contacts.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.google.android.gms.R;

public class GroupDetailDisplayUtils {
    private GroupDetailDisplayUtils() {
    }

    public static View getNewGroupSourceView(Context context) {
        return ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.group_source_button, null);
    }

    public static void bindGroupSourceView(Context context, View view, String accountTypeString, String dataSet) {
        AccountType accountType = AccountTypeManager.getInstance(context).getAccountType(accountTypeString, dataSet);
        TextView label = (TextView) view.findViewById(16908310);
        if (label == null) {
            throw new IllegalStateException("Group source view must contain a TextView with idandroid.R.id.label");
        }
        label.setText(accountType.getViewGroupLabel(context));
        ImageView accountIcon = (ImageView) view.findViewById(16908294);
        if (accountIcon == null) {
            throw new IllegalStateException("Group source view must contain an ImageView with idandroid.R.id.icon");
        }
        accountIcon.setImageDrawable(accountType.getDisplayIcon(context));
    }
}
