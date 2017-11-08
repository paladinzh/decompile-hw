package com.android.contacts.editor;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import com.android.contacts.hap.editor.RingtoneEditorView;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRawContactEditorView extends RelativeLayout {
    private static final String TAG = BaseRawContactEditorView.class.getSimpleName();
    private List<AccountWithDataSet> mAccountsList;
    private Bitmap mBitmapFromDetail;
    private View mBody;
    private Activity mCurrentActivity;
    private View mDivider;
    private ViewGroup mEditHead;
    private boolean mExpanded;
    private boolean mHasPhotoEditor;
    private int[] mLocation;
    private PhotoEditorView mPhoto;
    protected RingtoneEditorView mRingtone;
    private ScrollView mScrollView;
    protected LinearLayout mVibration;
    private WallpaperManager mWallpaperManager;

    public abstract long getRawContactId();

    public abstract void setState(RawContactDeltaList rawContactDeltaList, RawContactDelta rawContactDelta, AccountType accountType, ViewIdGenerator viewIdGenerator, boolean z);

    public void setBitmapFromDetail(Bitmap bitmapFromDetail) {
        this.mBitmapFromDetail = bitmapFromDetail;
    }

    public Bitmap getBitmapFromDetail() {
        return this.mBitmapFromDetail;
    }

    public BaseRawContactEditorView(Context context) {
        super(context);
        this.mHasPhotoEditor = false;
        this.mExpanded = true;
        this.mLocation = new int[2];
        this.mAccountsList = new ArrayList();
    }

    public BaseRawContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHasPhotoEditor = false;
        this.mExpanded = true;
        this.mLocation = new int[2];
        this.mAccountsList = new ArrayList();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBody = findViewById(R.id.body);
        this.mDivider = findViewById(R.id.divider);
        this.mPhoto = (PhotoEditorView) findViewById(R.id.edit_photo);
        this.mPhoto.setEnabled(isEnabled());
        this.mEditHead = (ViewGroup) findViewById(R.id.edit_photo);
        this.mScrollView = (ScrollView) findViewById(R.id.contact_editor_scroll_view);
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
    }

    public void setGroupMetaData(Cursor groupMetaData) {
    }

    public View getScrollView() {
        return this.mScrollView;
    }

    public void setPhotoBitmap(Bitmap bitmap) {
        this.mPhoto.setPhotoBitmap(bitmap);
    }

    protected void setHasPhotoEditor(boolean hasPhotoEditor) {
        this.mHasPhotoEditor = hasPhotoEditor;
        this.mPhoto.setVisibility(hasPhotoEditor ? 0 : 8);
    }

    public boolean hasSetPhoto() {
        return this.mPhoto.hasSetPhoto();
    }

    public PhotoEditorView getPhotoEditor() {
        return this.mPhoto;
    }

    public void setRingtone(String ringtone) {
        if (this.mRingtone != null) {
            this.mRingtone.setRingtone(ringtone);
        }
    }

    public RingtoneEditorView getRingtoneEditor() {
        return this.mRingtone;
    }

    public ViewGroup getEditHead() {
        return this.mEditHead;
    }

    public void setCurrentActivity(Activity activity) {
        this.mCurrentActivity = activity;
    }

    public List<AccountWithDataSet> getAccountsList() {
        return this.mAccountsList;
    }

    public void setAccountsList(RawContactDeltaList stateList) {
        if (stateList != null && !stateList.isEmpty()) {
            for (RawContactDelta rawContactDelta : stateList) {
                String accountType = rawContactDelta.getAccountType();
                String accountName = rawContactDelta.getAccountName();
                String dataSet = rawContactDelta.getDataSet();
                if (!(accountType == null || accountName == null)) {
                    AccountWithDataSet account = new AccountWithDataSet(accountName, accountType, dataSet);
                    if (!this.mAccountsList.contains(account)) {
                        this.mAccountsList.add(account);
                    }
                }
            }
        }
    }
}
