package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import java.util.List;

public class DreamSettingsHw extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final int[] PLAY_TIMEOUT = new int[]{5, 15, 30, 1};
    private static final String TAG = DreamSettingsHw.class.getSimpleName();
    private PreferenceScreen mPicturePath;
    private ListPreference mPlayTimeOut;
    private SwitchPreference mScreensaverSwitch;
    private PreferenceCategory mSettingsCategory;

    protected int getMetricsCategory() {
        return 100000;
    }

    private CharSequence[] buildTimeoutEntries(Context context) {
        CharSequence[] timeoutEntries = new CharSequence[5];
        timeoutEntries[0] = String.format(getResources().getString(2131628295, new Object[]{Integer.valueOf(PLAY_TIMEOUT[0])}), new Object[0]);
        timeoutEntries[1] = String.format(getResources().getString(2131628296, new Object[]{Integer.valueOf(PLAY_TIMEOUT[1])}), new Object[0]);
        timeoutEntries[2] = String.format(getResources().getString(2131628297, new Object[]{Integer.valueOf(PLAY_TIMEOUT[2])}), new Object[0]);
        timeoutEntries[3] = String.format(getResources().getString(2131628298, new Object[]{Integer.valueOf(PLAY_TIMEOUT[3])}), new Object[0]);
        timeoutEntries[4] = getResources().getString(2131628299);
        return timeoutEntries;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230853);
        PreferenceScreen root = getPreferenceScreen();
        this.mScreensaverSwitch = (SwitchPreference) root.findPreference("screensaver_switch");
        this.mScreensaverSwitch.setOnPreferenceChangeListener(this);
        this.mSettingsCategory = (PreferenceCategory) root.findPreference("settings_category");
        this.mPicturePath = (PreferenceScreen) root.findPreference("origin_of_pictures");
        this.mPlayTimeOut = (ListPreference) root.findPreference("timeout_list");
        this.mPlayTimeOut.setEntries(buildTimeoutEntries(getActivity()));
        this.mPlayTimeOut.setOnPreferenceChangeListener(this);
    }

    public void onResume() {
        super.onResume();
        initPreferencesValue();
    }

    private void initPreferencesValue() {
        this.mScreensaverSwitch.setChecked(readSecureBoolean("screensaver_enabled", false));
        if (!this.mScreensaverSwitch.isChecked()) {
            getPreferenceScreen().removePreference(this.mSettingsCategory);
        }
        String albumName = readSecureString("screensaver_path_name");
        if (TextUtils.isEmpty(albumName)) {
            setPicturePathSummary(readSecureString("screensaver_picture_path"));
        } else {
            this.mPicturePath.setSummary((CharSequence) albumName);
        }
        String timeout = readSecureString("screensaver_timeout");
        if (timeout == null) {
            timeout = "300";
        }
        this.mPlayTimeOut.setValue(timeout);
        updateTimeoutPreferenceDescription(timeout);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mPicturePath) {
            startAlbumActivity();
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if ("screensaver_switch".equals(key)) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            boolean isChecked = ((Boolean) newValue).booleanValue();
            writeSecureBoolean("screensaver_enabled", isChecked);
            if (isChecked) {
                getPreferenceScreen().addPreference(this.mSettingsCategory);
                writeSecureString("screensaver_components", "com.android.dreams.phototable/com.android.dreams.phototable.FlipperDream");
                writeSecureBoolean("screensaver_activate_on_sleep", true);
            } else {
                getPreferenceScreen().removePreference(this.mSettingsCategory);
            }
        } else if ("timeout_list".equals(key)) {
            int value = Integer.parseInt((String) newValue);
            try {
                writeSecureInt("screensaver_timeout", value);
                updateTimeoutPreferenceDescription((long) value);
            } catch (Throwable e) {
                MLog.e(TAG, "could not persist screensaver timeout setting", e);
            }
        }
        return true;
    }

    private void updateTimeoutPreferenceDescription(String timeoutString) {
        try {
            updateTimeoutPreferenceDescription(Long.parseLong(timeoutString));
        } catch (NumberFormatException e) {
            this.mPlayTimeOut.setSummary("");
            MLog.e(TAG, "timeout tranfer to integer eror!");
        }
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = this.mPlayTimeOut;
        String summary = "";
        CharSequence[] entries = preference.getEntries();
        if (currentTimeout == 0) {
            summary = entries[entries.length - 1].toString();
        } else if (currentTimeout > 0) {
            CharSequence[] values = preference.getEntryValues();
            if (entries != null && entries.length > 0) {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout && timeout != 0) {
                        best = i;
                    }
                }
                summary = entries[best].toString();
            }
        }
        preference.setSummary(summary);
    }

    private void startAlbumActivity() {
        Intent albumIntent = new Intent("android.intent.action.PICK").setClassName("com.android.gallery3d", "com.android.gallery3d.app.AlbumPicker");
        albumIntent.putExtra("get-album-include-virtual", true);
        String selectedPath = getSelectedPathFromDB();
        if (selectedPath != null) {
            albumIntent.putExtra("choosed_album_path", selectedPath);
        }
        try {
            startActivityForResult(albumIntent, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 1 && !handleMultiPaths(data)) {
            handleSinglePath(data);
        }
    }

    private boolean handleMultiPaths(Intent data) {
        String[] paths = data.getStringArrayExtra("albums-path");
        if (paths == null || paths.length <= 0) {
            return false;
        }
        StringBuffer pathsBuf = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            pathsBuf.append(paths[i]);
            if (i != paths.length - 1) {
                pathsBuf.append(",");
            }
        }
        if (!writeSecureString("screensaver_picture_path", pathsBuf.toString())) {
            return false;
        }
        String ablumName = data.getStringExtra("album-name");
        writeSecureString("screensaver_path_name", ablumName);
        this.mPicturePath.setSummary((CharSequence) ablumName);
        return true;
    }

    private void handleSinglePath(Intent data) {
        String albumPath = data.getStringExtra("album-path");
        if (albumPath != null && writeSecureString("screensaver_picture_path", albumPath)) {
            setPicturePathSummary(albumPath);
        }
    }

    private void setPicturePathSummary(String albumPath) {
        if (TextUtils.isEmpty(albumPath)) {
            this.mPicturePath.setSummary(2131627568);
            return;
        }
        String[] albums = albumPath.split(",");
        if (albums.length < 1) {
            this.mPicturePath.setSummary(2131627568);
            return;
        }
        List<String> path = Uri.parse(albums[0]).getPathSegments();
        if (path == null) {
            this.mPicturePath.setSummary(2131627568);
            return;
        }
        int bucketIdx = path.size() - 1;
        if (bucketIdx <= 0) {
            bucketIdx = 0;
        }
        new AsyncTask<String, Void, String>() {
            protected String doInBackground(String... params) {
                return DreamSettingsHw.this.getAblumNameFromDB(params[0]);
            }

            protected void onPostExecute(String result) {
                if (result == null) {
                    DreamSettingsHw.this.mPicturePath.setSummary(2131627568);
                } else {
                    DreamSettingsHw.this.mPicturePath.setSummary((CharSequence) result);
                }
            }
        }.execute(new String[]{(String) path.get(bucketIdx)});
    }

    private String getAblumNameFromDB(String bucket_id) {
        String str = null;
        String[] projection = new String[]{"bucket_display_name"};
        String[] selectionArgs = new String[]{bucket_id};
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, "bucket_id=?", selectionArgs, "bucket_display_name ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                str = cursor.getString(cursor.getColumnIndex("bucket_display_name"));
            }
            cursor.close();
        }
        return str;
    }

    private String getSelectedPathFromDB() {
        String albumPath = readSecureString("screensaver_picture_path");
        if (albumPath == null) {
            return null;
        }
        return albumPath.split(",")[0];
    }

    private boolean readSecureBoolean(String key, boolean def) {
        return Secure.getInt(getContentResolver(), key, def ? 1 : 0) == 1;
    }

    private boolean writeSecureBoolean(String key, boolean value) {
        return Secure.putInt(getContentResolver(), key, value ? 1 : 0);
    }

    private String readSecureString(String key) {
        return Secure.getString(getContentResolver(), key);
    }

    private boolean writeSecureString(String key, String value) {
        return Secure.putString(getContentResolver(), key, value);
    }

    private boolean writeSecureInt(String key, int value) {
        return Secure.putInt(getContentResolver(), key, value);
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
