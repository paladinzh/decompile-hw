package com.android.contacts.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.detail.PhotoSelectionHandler;
import com.android.contacts.detail.PhotoSelectionHandler.PhotoActionListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SchedulingUtils;
import com.google.android.gms.R;

public class PhotoSelectionActivity extends Activity {
    private AnimatorListenerAdapter mAnimationListener;
    private boolean mAnimationPending;
    private boolean mAnimotionEnable = true;
    private View mBackdrop;
    private boolean mCloseActivityWhenCameBackFromSubActivity;
    private Uri mCurrentPhotoUri;
    private boolean mExpandPhoto;
    private int mExpandedPhotoSize;
    private int mHeightOffset;
    private boolean mIsDirectoryContact;
    private boolean mIsProfile;
    Rect mOriginalPos = new Rect();
    private PendingPhotoResult mPendingPhotoResult;
    private ObjectAnimator mPhotoAnimator;
    private LayoutParams mPhotoEndParams;
    private PhotoHandler mPhotoHandler;
    private long mPhotoId;
    private LayoutParams mPhotoStartParams;
    private Uri mPhotoUri;
    private ImageView mPhotoView;
    private Rect mSourceBounds;
    private RawContactDeltaList mState;
    private boolean mSubActivityInProgress;

    private static class PendingPhotoResult {
        private final Intent mData;
        private final int mRequestCode;
        private final int mResultCode;

        private PendingPhotoResult(int requestCode, int resultCode, Intent data) {
            this.mRequestCode = requestCode;
            this.mResultCode = resultCode;
            this.mData = data;
        }
    }

    private final class PhotoHandler extends PhotoSelectionHandler {
        private final PhotoActionListener mListener;

        private final class PhotoListener extends PhotoActionListener {
            private PhotoListener() {
                super();
            }

            public void onPhotoSelected(Uri uri) {
                RawContactDeltaList delta = PhotoHandler.this.getDeltaForAttachingPhotoToContact();
                if (delta == null) {
                    HwLog.w("PhotoSelectionActivity", "return null when call getDeltaForAttachingPhotoToContact");
                    PhotoSelectionActivity.this.finish();
                    return;
                }
                Intent intent = ContactSaveService.createSaveContactIntent(PhotoHandler.this.mContext, delta, "", 0, PhotoSelectionActivity.this.mIsProfile, null, null, delta.getAllWritableRawContact(PhotoSelectionActivity.this), uri);
                Intent lIntent = new Intent();
                lIntent.setData(uri);
                PhotoSelectionActivity.this.setResult(-1, lIntent);
                PhotoSelectionActivity.this.startService(intent);
                PhotoSelectionActivity.this.finish();
            }

            public Uri getCurrentPhotoUri() {
                return PhotoSelectionActivity.this.mCurrentPhotoUri;
            }

            public void onPhotoSelectionDismissed() {
                if (!PhotoSelectionActivity.this.mSubActivityInProgress) {
                    PhotoSelectionActivity.this.finishImmediatelyWhenPopupDismissed();
                }
            }
        }

        private PhotoHandler(Context context, View photoView, int photoMode, RawContactDeltaList state) {
            super(context, photoView, photoMode, PhotoSelectionActivity.this.mIsDirectoryContact, state);
            this.mListener = new PhotoListener();
        }

        public PhotoActionListener getListener() {
            return this.mListener;
        }

        public void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            PhotoSelectionActivity.this.mSubActivityInProgress = true;
            PhotoSelectionActivity.this.mCloseActivityWhenCameBackFromSubActivity = true;
            PhotoSelectionActivity.this.mCurrentPhotoUri = photoUri;
            PhotoSelectionActivity.this.startActivityForResult(intent, requestCode);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        getWindow().addFlags(67108864);
        setContentView(R.layout.photoselection_activity);
        if (savedInstanceState != null) {
            this.mCurrentPhotoUri = (Uri) savedInstanceState.getParcelable("currentphotouri");
            this.mSubActivityInProgress = savedInstanceState.getBoolean("subinprogress");
        }
        Intent intent = getIntent();
        this.mPhotoUri = (Uri) intent.getParcelableExtra("photo_uri");
        this.mPhotoId = intent.getLongExtra("photo_id", -1);
        this.mState = (RawContactDeltaList) intent.getParcelableExtra("entity_delta_list");
        this.mIsProfile = intent.getBooleanExtra("is_profile", false);
        this.mIsDirectoryContact = intent.getBooleanExtra("is_directory_contact", false);
        this.mExpandPhoto = intent.getBooleanExtra("expand_photo", false);
        this.mExpandedPhotoSize = getResources().getDimensionPixelSize(R.dimen.detail_contact_photo_expanded_size);
        this.mHeightOffset = getResources().getDimensionPixelOffset(R.dimen.expanded_photo_height_offset);
        this.mBackdrop = findViewById(R.id.backdrop);
        this.mPhotoView = (ImageView) findViewById(R.id.photo);
        this.mSourceBounds = intent.getSourceBounds();
        this.mAnimotionEnable = intent.getBooleanExtra("animotion_enable", true);
        if (this.mSourceBounds == null) {
            this.mSubActivityInProgress = true;
            finish();
            return;
        }
        animateInBackground();
        this.mBackdrop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PhotoSelectionActivity.this.finish();
            }
        });
        SchedulingUtils.doAfterLayout(this.mBackdrop, new Runnable() {
            public void run() {
                PhotoSelectionActivity.this.displayPhoto();
            }
        });
    }

    private int getAdjustedExpandedPhotoSize(View enclosingView, int heightOffset) {
        Rect bounds = new Rect();
        enclosingView.getDrawingRect(bounds);
        float alpha = Math.min(((float) (bounds.height() - heightOffset)) / ((float) this.mExpandedPhotoSize), ((float) bounds.width()) / ((float) this.mExpandedPhotoSize));
        if (alpha < 1.0f) {
            return Float.valueOf(((float) this.mExpandedPhotoSize) * alpha).intValue();
        }
        return this.mExpandedPhotoSize;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSubActivityInProgress) {
            this.mCloseActivityWhenCameBackFromSubActivity = true;
        } else {
            finishImmediatelyWithNoAnimation();
        }
    }

    public void onStop() {
        super.onStop();
        if (!this.mSubActivityInProgress) {
            finishImmediatelyWithNoAnimation();
        }
    }

    public void finish() {
        if (this.mSubActivityInProgress) {
            finishImmediatelyWithNoAnimation();
        } else {
            closePhotoAndFinish();
        }
    }

    public static Intent buildIntent(Context context, Uri photoUri, Bitmap photoBitmap, boolean hasPhoto, Rect photoBounds, RawContactDeltaList delta, boolean isProfile, boolean isDirectoryContact, boolean expandPhotoOnClick, long aPhotoId) {
        Intent intent = new Intent(context, PhotoSelectionActivity.class);
        if (!(photoUri == null || photoBitmap == null || !hasPhoto)) {
            intent.putExtra("photo_uri", photoUri);
        }
        intent.putExtra("photo_id", aPhotoId);
        intent.setSourceBounds(photoBounds);
        intent.putExtra("entity_delta_list", delta);
        intent.putExtra("is_profile", isProfile);
        intent.putExtra("is_directory_contact", isDirectoryContact);
        intent.putExtra("expand_photo", expandPhotoOnClick);
        intent.putExtra("animotion_enable", false);
        intent.setExtrasClassLoader(PhotoSelectionActivity.class.getClassLoader());
        return intent;
    }

    private void finishImmediatelyWithNoAnimation() {
        finishImmediatelyWhenPopupDismissed();
    }

    private void finishImmediatelyWhenPopupDismissed() {
        if (this.mAnimotionEnable) {
            closePhoto();
            return;
        }
        super.finish();
        overridePendingTransition(0, 0);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mPhotoAnimator != null) {
            this.mPhotoAnimator.cancel();
            this.mPhotoAnimator = null;
        }
        if (this.mPhotoHandler != null) {
            this.mPhotoHandler.destroy();
            this.mPhotoHandler = null;
        }
    }

    private void displayPhoto() {
        if (this.mAnimotionEnable) {
            int[] pos = new int[2];
            this.mBackdrop.getLocationOnScreen(pos);
            LayoutParams layoutParams = new LayoutParams(this.mSourceBounds.width(), this.mSourceBounds.height());
            this.mOriginalPos.left = this.mSourceBounds.left - pos[0];
            this.mOriginalPos.top = this.mSourceBounds.top - pos[1];
            this.mOriginalPos.right = this.mOriginalPos.left + this.mSourceBounds.width();
            this.mOriginalPos.bottom = this.mOriginalPos.top + this.mSourceBounds.height();
            layoutParams.setMargins(this.mOriginalPos.left, this.mOriginalPos.top, this.mOriginalPos.right, this.mOriginalPos.bottom);
            this.mPhotoStartParams = layoutParams;
            this.mPhotoView.setLayoutParams(layoutParams);
            this.mPhotoView.requestLayout();
            int photoWidth = getPhotoEndParams().width;
            if (this.mPhotoUri != null) {
                ContactPhotoManager.getInstance(this).loadPhoto(this.mPhotoView, this.mPhotoUri, photoWidth, false, null);
            } else {
                this.mPhotoView.setImageDrawable(getResources().getDrawable(ContactPhotoManager.getDefaultAvatarResId(true, false)));
            }
            this.mPhotoView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (PhotoSelectionActivity.this.mAnimationPending) {
                        PhotoSelectionActivity.this.mAnimationPending = false;
                        PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", new int[]{PhotoSelectionActivity.this.mOriginalPos.left, left});
                        PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", new int[]{PhotoSelectionActivity.this.mOriginalPos.top, top});
                        PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", new int[]{PhotoSelectionActivity.this.mOriginalPos.right, right});
                        PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom", new int[]{PhotoSelectionActivity.this.mOriginalPos.bottom, bottom});
                        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(PhotoSelectionActivity.this.mPhotoView, new PropertyValuesHolder[]{pvhLeft, pvhTop, pvhRight, pvhBottom}).setDuration(400);
                        if (PhotoSelectionActivity.this.mAnimationListener != null) {
                            anim.addListener(PhotoSelectionActivity.this.mAnimationListener);
                        }
                        anim.start();
                    }
                }
            });
        }
        attachPhotoHandler();
    }

    private LayoutParams getPhotoEndParams() {
        if (this.mPhotoEndParams == null) {
            this.mPhotoEndParams = new LayoutParams(this.mPhotoStartParams);
            if (this.mExpandPhoto) {
                int adjustedPhotoSize = getAdjustedExpandedPhotoSize(this.mBackdrop, this.mHeightOffset);
                int widthDelta = adjustedPhotoSize - this.mPhotoStartParams.width;
                int heightDelta = adjustedPhotoSize - this.mPhotoStartParams.height;
                if (widthDelta >= 1 || heightDelta >= 1) {
                    this.mPhotoEndParams.width = adjustedPhotoSize;
                    this.mPhotoEndParams.height = adjustedPhotoSize;
                    this.mPhotoEndParams.topMargin = Math.max(this.mPhotoStartParams.topMargin - heightDelta, 0);
                    this.mPhotoEndParams.leftMargin = Math.max(this.mPhotoStartParams.leftMargin - widthDelta, 0);
                    this.mPhotoEndParams.bottomMargin = 0;
                    this.mPhotoEndParams.rightMargin = 0;
                }
            }
        }
        return this.mPhotoEndParams;
    }

    private void animatePhotoOpen() {
        if (this.mAnimotionEnable) {
            this.mAnimationListener = new AnimatorListenerAdapter() {
                private void capturePhotoPos() {
                    PhotoSelectionActivity.this.mPhotoView.requestLayout();
                    PhotoSelectionActivity.this.mOriginalPos.left = PhotoSelectionActivity.this.mPhotoView.getLeft();
                    PhotoSelectionActivity.this.mOriginalPos.top = PhotoSelectionActivity.this.mPhotoView.getTop();
                    PhotoSelectionActivity.this.mOriginalPos.right = PhotoSelectionActivity.this.mPhotoView.getRight();
                    PhotoSelectionActivity.this.mOriginalPos.bottom = PhotoSelectionActivity.this.mPhotoView.getBottom();
                }

                public void onAnimationEnd(Animator animation) {
                    capturePhotoPos();
                    if (PhotoSelectionActivity.this.mPhotoHandler != null) {
                        PhotoSelectionActivity.this.mPhotoHandler.onClick(PhotoSelectionActivity.this.mPhotoView);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    capturePhotoPos();
                }
            };
            animatePhoto(getPhotoEndParams());
        } else if (this.mPhotoHandler != null) {
            this.mPhotoHandler.onClick(this.mPhotoView);
        }
    }

    private void closePhotoAndFinish() {
        if (this.mAnimotionEnable) {
            closePhoto();
        } else {
            finishImmediatelyWithNoAnimation();
        }
    }

    private void closePhoto() {
        this.mAnimationListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(PhotoSelectionActivity.this.mPhotoView, "alpha", new float[]{0.0f}).setDuration(200);
                anim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        PhotoSelectionActivity.this.finishImmediatelyWithNoAnimation();
                    }
                });
                anim.start();
            }
        };
        animatePhoto(this.mPhotoStartParams);
        animateAwayBackground();
    }

    private void animatePhoto(MarginLayoutParams to) {
        if (this.mPhotoAnimator != null) {
            this.mPhotoAnimator.cancel();
        }
        if (this.mPhotoView != null) {
            this.mPhotoView.setLayoutParams(to);
            this.mAnimationPending = true;
            this.mPhotoView.requestLayout();
        }
    }

    private void animateInBackground() {
        ObjectAnimator.ofFloat(this.mBackdrop, "alpha", new float[]{0.0f, 0.1f}).setDuration(400).start();
    }

    private void animateAwayBackground() {
        ObjectAnimator.ofFloat(this.mBackdrop, "alpha", new float[]{0.0f}).setDuration(400).start();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentphotouri", this.mCurrentPhotoUri);
        outState.putBoolean("subinprogress", this.mSubActivityInProgress);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mPhotoHandler != null) {
            this.mSubActivityInProgress = false;
            if (this.mPhotoHandler.handlePhotoActivityResult(requestCode, resultCode, data)) {
                this.mPendingPhotoResult = null;
                return;
            } else if (this.mCloseActivityWhenCameBackFromSubActivity) {
                finishImmediatelyWithNoAnimation();
                return;
            } else {
                this.mPhotoHandler.onClick(this.mPhotoView);
                return;
            }
        }
        this.mPendingPhotoResult = new PendingPhotoResult(requestCode, resultCode, data);
    }

    private void attachPhotoHandler() {
        int mode;
        if (this.mPhotoUri == null) {
            mode = 4;
        } else {
            mode = 14;
        }
        this.mPhotoHandler = new PhotoHandler(this, this.mPhotoView, mode, this.mState);
        this.mPhotoHandler.initPopup();
        if (this.mPendingPhotoResult != null) {
            this.mPhotoHandler.handlePhotoActivityResult(this.mPendingPhotoResult.mRequestCode, this.mPendingPhotoResult.mResultCode, this.mPendingPhotoResult.mData);
            this.mPendingPhotoResult = null;
            return;
        }
        SchedulingUtils.doAfterLayout(this.mBackdrop, new Runnable() {
            public void run() {
                PhotoSelectionActivity.this.animatePhotoOpen();
            }
        });
    }
}
