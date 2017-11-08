package com.android.dialer.voicemail;

import android.database.Cursor;
import android.net.Uri;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;

public interface VoicemailStatusHelper {

    @VisibleForTesting
    public static class StatusMessage {
        public final int actionMessageId;
        public final Uri actionUri;
        public final int callDetailsMessageId;
        public final int callLogMessageId;

        public StatusMessage(String sourcePackage, int callLogMessageId, int callDetailsMessageId, int actionMessageId, Uri actionUri) {
            this.callLogMessageId = callLogMessageId;
            this.callDetailsMessageId = callDetailsMessageId;
            this.actionMessageId = actionMessageId;
            this.actionUri = actionUri;
        }

        public boolean showInCallLog() {
            return this.callLogMessageId != -1;
        }
    }

    List<String> getActivityVoicemailSoucesAccounts(Cursor cursor);

    int getNumberActivityVoicemailSources(Cursor cursor);

    @VisibleForTesting
    List<StatusMessage> getStatusMessages(Cursor cursor);
}
