package com.android.contacts.calllog;

import android.content.res.Resources;
import com.google.android.gms.R;

public class CallTypeHelper {
    private final CharSequence mIncomingName;
    private final CharSequence mMissedName;
    private final int mNewMissedColor;
    private final int mNewVoicemailColor;
    private final CharSequence mOutgoingName;
    private final CharSequence mVoicemailName;

    public CallTypeHelper(Resources resources) {
        this.mIncomingName = resources.getString(R.string.contact_calllog_dial_in);
        this.mOutgoingName = resources.getString(R.string.contact_calllog_dial_out);
        this.mMissedName = resources.getString(R.string.type_missed);
        this.mVoicemailName = resources.getString(R.string.type_voicemail);
        this.mNewMissedColor = resources.getColor(R.color.call_log_missed_call_highlight_color);
        this.mNewVoicemailColor = resources.getColor(R.color.call_log_missed_call_highlight_color);
    }

    public CharSequence getCallTypeText(int callType) {
        switch (callType) {
            case 1:
                return this.mIncomingName;
            case 2:
                return this.mOutgoingName;
            case 3:
            case 5:
                return this.mMissedName;
            case 4:
                return this.mVoicemailName;
            default:
                return this.mIncomingName;
        }
    }

    public Integer getHighlightedColor(int callType) {
        switch (callType) {
            case 1:
                return null;
            case 2:
                return null;
            case 3:
            case 5:
                return Integer.valueOf(this.mNewMissedColor);
            case 4:
                return Integer.valueOf(this.mNewVoicemailColor);
            default:
                return null;
        }
    }
}
