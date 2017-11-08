package com.android.settings.applications;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.settings.accessibility.ListDialogPreference;

public class AppDomainsPreference extends ListDialogPreference {
    private int mNumEntries;

    public AppDomainsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(2130968625);
        setListItemLayoutResource(17367049);
        setWidgetLayoutResource(2130968998);
        setNegativeButtonText(2131627945);
    }

    public void setTitles(CharSequence[] titles) {
        int length;
        boolean z = false;
        if (titles != null) {
            length = titles.length;
        } else {
            length = 0;
        }
        this.mNumEntries = length;
        super.setTitles(titles);
        if (titles != null) {
            z = true;
        }
        setEnabled(z);
    }

    public CharSequence getSummary() {
        Context context = getContext();
        if (this.mNumEntries == 0) {
            return context.getString(2131626934);
        }
        int whichVersion;
        CharSequence summary = super.getSummary();
        if (this.mNumEntries == 1) {
            whichVersion = 2131626935;
        } else {
            whichVersion = 2131626936;
        }
        return context.getString(whichVersion, new Object[]{summary});
    }

    protected void onBindListItem(View view, int index) {
        CharSequence title = getTitleAt(index);
        if (title != null) {
            TextView domainName = (TextView) view.findViewById(16908308);
            if (domainName != null) {
                domainName.setText(title);
            }
        }
    }
}
