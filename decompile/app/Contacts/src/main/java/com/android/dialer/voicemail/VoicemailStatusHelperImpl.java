package com.android.dialer.voicemail;

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.net.Uri;
import com.android.contacts.util.UriUtils;
import com.android.dialer.voicemail.VoicemailStatusHelper.StatusMessage;
import com.google.android.gms.R;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VoicemailStatusHelperImpl implements VoicemailStatusHelper {
    static final String[] PROJECTION = new String[7];

    public enum Action {
        NONE(-1),
        CALL_VOICEMAIL(R.string.voicemail_status_action_call_server),
        CONFIGURE_VOICEMAIL(R.string.voicemail_status_action_configure);
        
        private final int mMessageId;

        private Action(int messageId) {
            this.mMessageId = messageId;
        }

        public int getMessageId() {
            return this.mMessageId;
        }
    }

    private static class MessageComparator implements Comparator<MessageStatusWithPriority>, Serializable {
        private static final long serialVersionUID = 1;

        private MessageComparator() {
        }

        public int compare(MessageStatusWithPriority msg1, MessageStatusWithPriority msg2) {
            return msg1.mPriority - msg2.mPriority;
        }
    }

    private static class MessageStatusWithPriority {
        private final StatusMessage mMessage;
        private final int mPriority;

        public MessageStatusWithPriority(StatusMessage message, int priority) {
            this.mMessage = message;
            this.mPriority = priority;
        }
    }

    private enum OverallState {
        NO_CONNECTION(0, Action.CALL_VOICEMAIL, R.string.voicemail_status_voicemail_not_available, R.string.voicemail_status_audio_not_available),
        NO_DATA(1, Action.CALL_VOICEMAIL, R.string.voicemail_status_voicemail_not_available, R.string.voicemail_status_audio_not_available),
        MESSAGE_WAITING(2, Action.CALL_VOICEMAIL, R.string.voicemail_status_messages_waiting, R.string.voicemail_status_audio_not_available),
        NO_NOTIFICATIONS(3, Action.CALL_VOICEMAIL, R.string.voicemail_status_voicemail_not_available),
        INVITE_FOR_CONFIGURATION(4, Action.CONFIGURE_VOICEMAIL, R.string.voicemail_status_configure_voicemail),
        NO_DETAILED_NOTIFICATION(5, Action.NONE, -1),
        NOT_CONFIGURED(6, Action.NONE, -1),
        OK(7, Action.NONE, -1),
        INVALID(8, Action.NONE, -1);
        
        private final Action mAction;
        private final int mCallDetailsMessageId;
        private final int mCallLogMessageId;
        private final int mPriority;

        private OverallState(int priority, Action action, int callLogMessageId) {
            this(r8, r9, priority, action, callLogMessageId, -1);
        }

        private OverallState(int priority, Action action, int callLogMessageId, int callDetailsMessageId) {
            this.mPriority = priority;
            this.mAction = action;
            this.mCallLogMessageId = callLogMessageId;
            this.mCallDetailsMessageId = callDetailsMessageId;
        }

        public Action getAction() {
            return this.mAction;
        }

        public int getPriority() {
            return this.mPriority;
        }

        public int getCallLogMessageId() {
            return this.mCallLogMessageId;
        }

        public int getCallDetailsMessageId() {
            return this.mCallDetailsMessageId;
        }
    }

    static {
        PROJECTION[0] = "source_package";
        PROJECTION[1] = "configuration_state";
        PROJECTION[2] = "data_channel_state";
        PROJECTION[3] = "notification_channel_state";
        PROJECTION[4] = "settings_uri";
        PROJECTION[5] = "voicemail_access_uri";
        PROJECTION[6] = "phone_account_id";
    }

    public List<StatusMessage> getStatusMessages(Cursor cursor) {
        List<MessageStatusWithPriority> messages = new ArrayList();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            MessageStatusWithPriority message = getMessageForStatusEntry(cursor);
            if (message != null) {
                messages.add(message);
            }
        }
        return reorderMessages(messages);
    }

    public int getNumberActivityVoicemailSources(Cursor cursor) {
        int count = 0;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            if (isVoicemailSourceActive(cursor)) {
                count++;
            }
        }
        return count;
    }

    private boolean isVoicemailSourceActive(Cursor cursor) {
        if (cursor.getString(0) != null) {
            return cursor.getInt(1) == 0;
        } else {
            return false;
        }
    }

    private List<StatusMessage> reorderMessages(List<MessageStatusWithPriority> messageWrappers) {
        Collections.sort(messageWrappers, new MessageComparator());
        List<StatusMessage> reorderMessages = new ArrayList();
        for (MessageStatusWithPriority messageWrapper : messageWrappers) {
            reorderMessages.add(messageWrapper.mMessage);
        }
        return reorderMessages;
    }

    private MessageStatusWithPriority getMessageForStatusEntry(Cursor cursor) {
        String sourcePackage = cursor.getString(0);
        if (sourcePackage == null) {
            return null;
        }
        OverallState overallState = getOverallState(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3));
        Action action = overallState.getAction();
        if (action == Action.NONE) {
            return null;
        }
        Uri actionUri = null;
        if (action == Action.CALL_VOICEMAIL) {
            actionUri = UriUtils.parseUriOrNull(cursor.getString(5));
        } else if (action == Action.CONFIGURE_VOICEMAIL) {
            actionUri = UriUtils.parseUriOrNull(cursor.getString(4));
            if (actionUri == null) {
                return null;
            }
        }
        return new MessageStatusWithPriority(new StatusMessage(sourcePackage, overallState.getCallLogMessageId(), overallState.getCallDetailsMessageId(), action.getMessageId(), actionUri), overallState.getPriority());
    }

    private OverallState getOverallState(int configurationState, int dataChannelState, int notificationChannelState) {
        if (configurationState == 0) {
            if (dataChannelState == 0) {
                if (notificationChannelState == 0) {
                    return OverallState.OK;
                }
                if (notificationChannelState == 2) {
                    return OverallState.NO_DETAILED_NOTIFICATION;
                }
                if (notificationChannelState == 1) {
                    return OverallState.NO_NOTIFICATIONS;
                }
            } else if (dataChannelState == 1) {
                if (notificationChannelState == 0) {
                    return OverallState.NO_DATA;
                }
                if (notificationChannelState == 2) {
                    return OverallState.MESSAGE_WAITING;
                }
                if (notificationChannelState == 1) {
                    return OverallState.NO_CONNECTION;
                }
            }
        } else if (configurationState == 2) {
            return OverallState.INVITE_FOR_CONFIGURATION;
        } else {
            if (configurationState == 1) {
                return OverallState.NOT_CONFIGURED;
            }
        }
        return OverallState.INVALID;
    }

    public static void startQuery(AsyncQueryHandler handler, int token, Uri uri, String selection, String[] selectionArgs, String orderBy) {
        handler.startQuery(token, null, uri, PROJECTION, selection, selectionArgs, orderBy);
    }

    public List<String> getActivityVoicemailSoucesAccounts(Cursor cursor) {
        List<String> accounts = new ArrayList();
        if (cursor == null || !cursor.moveToFirst()) {
            return accounts;
        }
        do {
            if (isVoicemailSourceActive(cursor)) {
                accounts.add(cursor.getString(6));
            }
        } while (cursor.moveToNext());
        return accounts;
    }
}
