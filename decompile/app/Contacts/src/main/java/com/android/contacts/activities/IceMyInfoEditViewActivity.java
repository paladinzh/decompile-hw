package com.android.contacts.activities;

import android.app.ActionBar;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.IceMyRecords;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.WallPaperImageHelper;

public class IceMyInfoEditViewActivity extends IceMyInfoBaseActivity {
    private EditText et_ice_my_info_allergies;
    private EditText et_ice_my_info_current_medication;
    private EditText et_ice_my_info_health_record;
    private EditText et_ice_my_info_my_name;
    private EditText et_ice_my_info_other;
    private ImageView iv_profile_photo;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295) {
                IceMyInfoEditViewActivity.this.finish();
            } else if (viewId == 16908296) {
                IceMyInfoEditViewActivity.this.saveData();
                IceMyInfoEditViewActivity.this.finish();
            }
        }
    };
    private IceMyRecords mIceMyRecords;
    private UpdateProfileTask updateProfileTask;

    class UpdateProfileTask extends AsyncTask<Void, Void, Bitmap> {
        private Uri mProfileLookupUri;

        UpdateProfileTask() {
        }

        protected Bitmap doInBackground(Void... arg0) {
            String uriString = IceMyInfoEditViewActivity.this.getIntent().getStringExtra(HwCustCommonConstants.ICE_EXTRA_URI_NAME);
            if (uriString != null) {
                this.mProfileLookupUri = Uri.parse(uriString);
                if (!isCancelled() && this.mProfileLookupUri == null) {
                    this.mProfileLookupUri = IceMyInfoEditViewActivity.this.getProfileLookupUri();
                }
                if (!(isCancelled() || this.mProfileLookupUri == null)) {
                    return IceMyInfoEditViewActivity.this.loadProfileDataInfo(this.mProfileLookupUri);
                }
            }
            return null;
        }

        protected void onPostExecute(Bitmap profilePhoto) {
            IceMyInfoEditViewActivity.this.loadProfilePhotoAndName(profilePhoto);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_editContact);
        }
        setContentView(R.layout.ice_my_info_edit_view);
        this.mIceMyRecords = new IceMyRecords(this);
        updateEditModeUI();
        this.updateProfileTask = new UpdateProfileTask();
        this.updateProfileTask.execute(new Void[0]);
    }

    protected void onDestroy() {
        if (this.updateProfileTask != null) {
            this.updateProfileTask.cancel(true);
        }
        super.onDestroy();
    }

    private void updateEditModeUI() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(28, 28);
            if (EmuiVersion.isSupportEmui()) {
                ActionBarEx.setStartIcon(actionBar, true, null, this.mActionBarListener);
                ActionBarEx.setEndIcon(actionBar, true, null, this.mActionBarListener);
            }
            this.iv_profile_photo = (ImageView) findViewById(R.id.photo);
            setBlurWallpaperBackground(findViewById(R.id.head_image_background));
        }
        String[] my_info_values = this.mIceMyRecords.getMyRecordValues();
        this.et_ice_my_info_my_name = (EditText) findViewById(R.id.et_ice_my_info_my_name);
        this.et_ice_my_info_health_record = (EditText) findViewById(R.id.et_ice_my_info_health_record);
        this.et_ice_my_info_allergies = (EditText) findViewById(R.id.et_ice_my_info_allergies);
        this.et_ice_my_info_current_medication = (EditText) findViewById(R.id.et_ice_my_info_current_medication);
        this.et_ice_my_info_other = (EditText) findViewById(R.id.et_ice_my_info_other);
        this.et_ice_my_info_my_name.setText(my_info_values[0]);
        this.et_ice_my_info_health_record.setText(my_info_values[1]);
        this.et_ice_my_info_allergies.setText(my_info_values[2]);
        this.et_ice_my_info_current_medication.setText(my_info_values[3]);
        this.et_ice_my_info_other.setText(my_info_values[4]);
    }

    private void saveData() {
        if (this.mIceMyRecords != null) {
            this.mIceMyRecords.saveDataToSharedPref(new String[]{this.et_ice_my_info_my_name.getText().toString().trim(), this.et_ice_my_info_health_record.getText().toString().trim(), this.et_ice_my_info_allergies.getText().toString().trim(), this.et_ice_my_info_current_medication.getText().toString().trim(), this.et_ice_my_info_other.getText().toString().trim()});
            Toast.makeText(this, getString(R.string.ice_my_info_data_save), 0).show();
        }
    }

    private void loadProfilePhotoAndName(Bitmap profilePhoto) {
        if (profilePhoto != null) {
            this.iv_profile_photo.setClickable(false);
            this.iv_profile_photo.setImageBitmap(profilePhoto);
        }
        if (TextUtils.isEmpty(this.mIceMyRecords.getMyRecordValues()[0]) && this.mDisplayName != null) {
            this.mIceMyRecords.setMyRecordValue(this.mDisplayName, 0);
            this.et_ice_my_info_my_name.setText(this.mDisplayName);
        }
    }

    private void setBlurWallpaperBackground(View headWallpaper) {
        int statusBarHeight = getResources().getDimensionPixelSize(17104919);
        TypedArray actionbarSizeTypedArray = obtainStyledAttributes(new int[]{16843499});
        float actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        int headHeight = getResources().getDimensionPixelSize(R.dimen.contact_editor_head_height);
        int top = statusBarHeight + ((int) actionBarHeight);
        int i = top;
        Rect tempRect = new Rect(0, i, getResources().getDisplayMetrics().widthPixels + 0, top + headHeight);
        if (CommonConstants.LOG_DEBUG) {
            Log.d("IceMyInfoEditViewActivity", "tempRect=" + tempRect.toShortString());
            Log.d("IceMyInfoEditViewActivity", "tempRect.width()" + tempRect.width());
            Log.d("IceMyInfoEditViewActivity", "tempRect.height()" + tempRect.height());
        }
        if (tempRect.width() > 0 && tempRect.height() > 0) {
            Bitmap src = WallPaperImageHelper.getInstance(this).getBitmap(tempRect, 0.0f, 0.0f, 0.0f, 0.0f);
            if (src != null) {
                headWallpaper.setBackground(new BitmapDrawable(getResources(), src));
            }
        }
    }
}
