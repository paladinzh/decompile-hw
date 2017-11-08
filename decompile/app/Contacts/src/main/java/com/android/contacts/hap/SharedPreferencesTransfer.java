package com.android.contacts.hap;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.util.Map;
import java.util.Map.Entry;

public class SharedPreferencesTransfer {
    private Editor mEditorFrom;
    private Editor mEditorTo;
    private SharedPreferences mFrom;
    private SharedPreferences mTo;

    public SharedPreferencesTransfer(SharedPreferences from, SharedPreferences to) {
        Editor edit;
        Editor editor = null;
        this.mFrom = from;
        this.mTo = to;
        if (this.mFrom != null) {
            edit = this.mFrom.edit();
        } else {
            edit = null;
        }
        this.mEditorFrom = edit;
        if (this.mTo != null) {
            editor = this.mTo.edit();
        }
        this.mEditorTo = editor;
    }

    public void moveSharedPreferences() {
        if (this.mFrom != null && this.mEditorFrom != null && this.mEditorTo != null) {
            Map<String, ?> map = this.mFrom.getAll();
            if (map != null) {
                for (Entry<String, ?> entry : map.entrySet()) {
                    putObject((String) entry.getKey(), entry.getValue());
                }
                this.mEditorTo.apply();
                this.mEditorFrom.clear();
                this.mEditorFrom.apply();
            }
        }
    }

    private void putObject(String key, Object value) {
        if (value instanceof String) {
            this.mEditorTo.putString(key, (String) value);
        } else if (value instanceof Integer) {
            this.mEditorTo.putInt(key, ((Integer) value).intValue());
        } else if (value instanceof Boolean) {
            this.mEditorTo.putBoolean(key, ((Boolean) value).booleanValue());
        } else if (value instanceof Long) {
            this.mEditorTo.putLong(key, ((Long) value).longValue());
        } else if (value instanceof Float) {
            this.mEditorTo.putFloat(key, ((Float) value).floatValue());
        }
    }
}
