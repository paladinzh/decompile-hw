package com.android.contacts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.AutoEllipseTextView;
import com.android.contacts.util.IceMyRecords;
import com.android.contacts.widget.CubicBezierInterpolator;
import com.google.android.gms.R;

public class IceMyInfoActivity extends IceMyInfoBaseActivity {
    private boolean enterAnimFinished = true;
    private Animation mAniFinish = null;
    private View mContentView = null;
    private IceMyRecords mIceMyRecords;
    private Uri mProfileLookupUri;
    OnClickListener menu_click_listener = new OnClickListener() {
        public void onClick(View arg0) {
            IceMyInfoActivity.this.startEditMode();
        }
    };
    private MyInfoAdapter myInfoAdapter;
    private UpdateProfileTask updateProfileTask;

    private class DetailAnimation implements AnimationListener {
        boolean mIsEnter = false;

        public DetailAnimation(boolean isEnterActivity) {
            this.mIsEnter = isEnterActivity;
        }

        public void onAnimationEnd(Animation animation) {
            if (this.mIsEnter) {
                IceMyInfoActivity.this.enterAnimFinished = true;
            } else {
                IceMyInfoActivity.this.finishFinal();
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            if (this.mIsEnter) {
                IceMyInfoActivity.this.enterAnimFinished = false;
            }
        }
    }

    private class MyInfoAdapter extends BaseAdapter {
        Context mContext;
        String[] my_info_title = IceMyInfoActivity.this.mIceMyRecords.getMyRecordTitles();
        String[] my_info_values = IceMyInfoActivity.this.mIceMyRecords.getMyRecordValues();

        public MyInfoAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {
            return IceMyInfoActivity.this.mIceMyRecords.getMyRecordTitles().length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public void notifyDataSetChanged() {
            this.my_info_values = IceMyInfoActivity.this.mIceMyRecords.getMyRecordValues();
            super.notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.ice_myinfo_popup_listitem, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_record_title);
                holder.record = (TextView) convertView.findViewById(R.id.tv_record);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(this.my_info_title[position]);
            if (this.my_info_values[position].equals("")) {
                holder.record.setText(this.mContext.getString(R.string.ice_no_record_found));
            } else {
                holder.record.setText(this.my_info_values[position]);
            }
            return convertView;
        }
    }

    class UpdateProfileTask extends AsyncTask<Void, Void, Bitmap> {
        UpdateProfileTask() {
        }

        protected Bitmap doInBackground(Void... arg0) {
            IceMyInfoActivity.this.mProfileLookupUri = IceMyInfoActivity.this.getProfileLookupUri();
            if (isCancelled() || IceMyInfoActivity.this.mProfileLookupUri == null) {
                return null;
            }
            return IceMyInfoActivity.this.loadProfileDataInfo(IceMyInfoActivity.this.mProfileLookupUri);
        }

        protected void onPostExecute(Bitmap profilePhoto) {
            IceMyInfoActivity.this.loadProfilePhotoAndName(profilePhoto);
        }
    }

    static class ViewHolder {
        TextView record;
        TextView title;

        ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setType(2009);
        getWindow().addFlags(4718592);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ice_myinfo_popup);
        this.mIceMyRecords = new IceMyRecords(this);
        updateUI();
        this.updateProfileTask = new UpdateProfileTask();
        this.updateProfileTask.execute(new Void[0]);
    }

    protected void onDestroy() {
        if (this.updateProfileTask != null) {
            this.updateProfileTask.cancel(true);
        }
        super.onDestroy();
    }

    private void startEditMode() {
        Intent intent = new Intent(this, IceMyInfoEditViewActivity.class);
        if (this.mProfileLookupUri != null) {
            intent.putExtra(HwCustCommonConstants.ICE_EXTRA_URI_NAME, this.mProfileLookupUri.toString());
        }
        finish();
        startActivity(intent);
    }

    private void updateUI() {
        this.mContentView = findViewById(R.id.my_dtails_info_content);
        ((AutoEllipseTextView) findViewById(R.id.title)).setText(getString(R.string.my_emergency_info));
        ListView my_info_listView = (ListView) findViewById(R.id.my_info_listView);
        this.myInfoAdapter = new MyInfoAdapter(this);
        my_info_listView.setAdapter(this.myInfoAdapter);
        LinearLayout edit_menu = (LinearLayout) findViewById(R.id.menu_layout);
        String INTENT_ACTION = getIntent().getAction();
        if (INTENT_ACTION != null && INTENT_ACTION.equals(HwCustCommonConstants.ICE_DIALER_MODE_ACTION)) {
            edit_menu.setVisibility(8);
        }
        edit_menu.setOnClickListener(this.menu_click_listener);
    }

    private void finishFinal() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void finish() {
        if (this.mAniFinish == null) {
            this.mAniFinish = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_exit);
            this.mAniFinish.setInterpolator(CubicBezierInterpolator.TYPE_C);
            this.mAniFinish.setAnimationListener(new DetailAnimation(false));
            if (this.mContentView != null) {
                this.mContentView.startAnimation(this.mAniFinish);
            }
        } else if (!(this.mAniFinish.hasStarted() && this.mAniFinish.hasEnded())) {
        }
    }

    private void loadProfilePhotoAndName(Bitmap profilePhoto) {
        if (profilePhoto != null) {
            ((ImageView) findViewById(R.id.photo)).setImageBitmap(profilePhoto);
        }
        if (TextUtils.isEmpty(this.mIceMyRecords.getMyRecordValues()[0]) && this.mDisplayName != null) {
            this.mIceMyRecords.setMyRecordValue(this.mDisplayName, 0);
        }
        this.myInfoAdapter.notifyDataSetChanged();
    }
}
