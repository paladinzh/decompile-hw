package com.android.gallery3d.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;

@TargetApi(11)
public class EditSetting extends PreferenceFragment {
    private static final String KEY_EDIT_RESOLUTION = "key-edit-resolution";
    private static final String TAG = "EditSetting";

    public enum EditSaveResolution {
        HIGH,
        MIDDLE,
        LOW;
        
        public static final int DEFAULT_SAVE_RESOLUTION = 1;
        public static final int SAVE_RESOLUTION_HIGH = 0;
        public static final int SAVE_RESOLUTION_LOW = 2;
        public static final int SAVE_RESOLUTION_MIDDLE = 1;

        static EditSaveResolution parseInt(int value) {
            switch (value) {
                case 0:
                    return HIGH;
                case 2:
                    return LOW;
                default:
                    return MIDDLE;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_edit);
    }

    public static EditSaveResolution getEditSaveResulution(Context context) {
        int value = 1;
        try {
            value = Integer.parseInt(GallerySettings.getString(context, "key-edit-resolution", Integer.toString(1)));
        } catch (NumberFormatException e) {
            GalleryLog.d(TAG, "Error when get the setting for edit save resolution, using default value");
        }
        return EditSaveResolution.parseInt(value);
    }
}
