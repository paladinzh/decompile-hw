package com.huawei.gallery.actionbar;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.storage.GalleryStorageManager;

public class MergeCardActionMode extends ActionMode implements OnClickListener, OnItemSelectedListener {
    private Listener mListener;
    private Spinner mStorageLocation;
    private int mStorageLocationIndex = -1;

    public interface Listener {
        void onCardActionShowed(ActionBarStateBase actionBarStateBase);

        void onCardLocationFiltered(int i);
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public int getMode() {
        return 4;
    }

    protected void showHeadView() {
        super.showHeadView();
        this.mActionBar.setDisplayOptions(20);
    }

    protected void initViewItem() {
        this.mMainView = (ViewGroup) this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_camera_action, null);
        this.mTitleView = (TextView) this.mMainView.findViewById(R.id.head_actionmode_title);
        this.mTitleView.setClickable(true);
        this.mStorageLocation = (Spinner) this.mMainView.findViewById(R.id.card_location_spinner);
        if (GalleryStorageManager.getInstance().hasAnyMountedOuterGalleryStorage()) {
            this.mStorageLocation.setVisibility(0);
            this.mTitleView.setVisibility(8);
        } else {
            this.mStorageLocation.setVisibility(8);
            this.mTitleView.setVisibility(0);
        }
        this.mStorageLocation.setOnItemSelectedListener(this);
        setTitleInternal();
    }

    public void show() {
        super.show();
        if (this.mListener != null) {
            this.mListener.onCardActionShowed(this);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (this.mStorageLocation == parent && this.mStorageLocationIndex != position) {
            if (this.mStorageLocationIndex != -1) {
                reportForLocationSelected(position);
            }
            this.mStorageLocationIndex = position;
            if (this.mListener != null) {
                this.mListener.onCardLocationFiltered(position);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    protected void setTitleInternal() {
        super.setTitleInternal();
        if (this.mTitleView != null && this.mStorageLocation != null) {
            this.mStorageLocation.setPrompt(this.mTitleView.getText());
        }
    }

    public void updateLocationSelection(int locationType) {
        if (this.mStorageLocation != null) {
            this.mStorageLocation.setSelection(locationType);
        }
    }

    public void updateCardLocationVisibility(int visibility) {
        int i = 0;
        if (this.mStorageLocation != null) {
            this.mStorageLocation.setVisibility(visibility);
        }
        if (this.mTitleView != null) {
            TextView textView = this.mTitleView;
            if (visibility == 0) {
                i = 8;
            }
            textView.setVisibility(i);
        }
    }

    private void reportForLocationSelected(int index) {
        String location = "PhoneAndSdcard";
        switch (index) {
            case 0:
                location = "PhoneAndSdcard";
                break;
            case 1:
                location = "Phone";
                break;
            case 2:
                location = "Sdcard";
                break;
        }
        ReportToBigData.report(54, String.format("{Location:%s}", new Object[]{location}));
    }
}
