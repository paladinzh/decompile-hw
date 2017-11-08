package com.android.settings;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class FontsizeSettingsActivity extends Activity {
    private static final String[] font_size = new String[]{"font_size_small", "font_size_normal", "font_size_large", "font_size_huge", "font_size_extra_huge"};
    private String[] entries_font_size;
    private String[] entryvalues_font_size;
    private boolean mChangeToExtraHuge = false;
    private Context mContext;
    private final Configuration mCurConfig = new Configuration();
    private int mCurrentSelection = 0;
    private AlertDialog mFontsizeDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.entries_font_size = getResources().getStringArray(2131361838);
        this.entryvalues_font_size = getResources().getStringArray(2131361839);
        getWindow().setBackgroundDrawableResource(17170445);
        showDialog(100);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 100:
                this.mCurrentSelection = readFontSizePreference();
                this.mFontsizeDialog = new Builder(this).setTitle(2131625182).setSingleChoiceItems(new FontsizeAdapter(this, 2130969135, this.entries_font_size), this.mCurrentSelection, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                        }
                        int extraHugeIndex = FontsizeSettingsActivity.this.entryvalues_font_size.length - 1;
                        if (FontsizeSettingsActivity.this.mCurrentSelection == extraHugeIndex || which != extraHugeIndex) {
                            FontsizeSettingsActivity.this.mChangeToExtraHuge = false;
                        } else {
                            FontsizeSettingsActivity.this.mChangeToExtraHuge = true;
                        }
                        if (which != FontsizeSettingsActivity.this.mCurrentSelection) {
                            ItemUseStat.getInstance().handleClick(FontsizeSettingsActivity.this.mContext, 2, FontsizeSettingsActivity.font_size[which]);
                        }
                        FontsizeSettingsActivity.this.mCurrentSelection = which;
                        FontsizeSettingsActivity.this.writeFontSizePreference(FontsizeSettingsActivity.this.entryvalues_font_size[which]);
                        dialog.dismiss();
                    }
                }).setNegativeButton(2131627333, null).create();
                this.mFontsizeDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (FontsizeSettingsActivity.this.mChangeToExtraHuge) {
                            Toast.makeText(FontsizeSettingsActivity.this, FontsizeSettingsActivity.this.getResources().getString(2131627435), 1).show();
                        }
                        if (!FontsizeSettingsActivity.this.isFinishing()) {
                            FontsizeSettingsActivity.this.finish();
                        }
                    }
                });
                this.mFontsizeDialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        ItemUseStat.getInstance().handleClick(FontsizeSettingsActivity.this.mContext, 1, "font_size_cancel");
                    }
                });
                this.mFontsizeDialog.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    public static int floatToIndex(float val, Context context) {
        String[] indices = context.getResources().getStringArray(2131361839);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < ((thisVal - lastVal) * 0.5f) + lastVal) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        int indexExtraHuge = indices.length - 1;
        if (Math.abs(lastVal - Float.parseFloat(indices[indexExtraHuge])) < 1.0E-7f) {
            return indexExtraHuge;
        }
        return indices.length - 2;
    }

    public int readFontSizePreference() {
        try {
            this.mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w("FontsizeSettings", "Unable to retrieve font size");
        }
        return floatToIndex(this.mCurConfig.fontScale, this);
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            this.mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(this.mCurConfig);
        } catch (RemoteException e) {
            Log.w("FontsizeSettings", "Unable to save font size");
        }
    }

    protected void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(this.mContext);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mFontsizeDialog != null && this.mFontsizeDialog.isShowing()) {
            this.mFontsizeDialog.dismiss();
        }
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
