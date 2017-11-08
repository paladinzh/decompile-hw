package com.android.contacts.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract.DisplayPhoto;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsUtils;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.ArrayList;

public class PhotoEditorView extends FrameLayout implements Editor {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final String TAG = PhotoEditorView.class.getSimpleName();
    private long mContactId = 0;
    private ContactPhotoManager mContactPhotoManager;
    private Context mContext;
    private ValuesDelta mEntry;
    private ArrayList<ValuesDelta> mEntryList = new ArrayList();
    private View mFrameViewPressed;
    private boolean mHasSetPhoto = false;
    private EditorListener mListener;
    private ImageView mPhotoImageView;
    private boolean mReadOnly;

    public PhotoEditorView(Context context) {
        super(context);
        this.mContext = context;
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void editNewlyAddedField() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContactPhotoManager = ContactPhotoManager.getInstance(getContext());
        this.mPhotoImageView = (ImageView) findViewById(R.id.photo);
        this.mFrameViewPressed = findViewById(R.id.frame_pressed);
        this.mFrameViewPressed.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PhotoEditorView.this.mListener != null) {
                    PhotoEditorView.this.mListener.onRequest(1);
                }
            }
        });
    }

    public void setValues(DataKind kind, ValuesDelta values, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        boolean z;
        if (DEBUG) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("setValues (values != null):");
            if (values != null) {
                z = true;
            } else {
                z = false;
            }
            HwLog.d(str, append.append(z).toString());
        }
        this.mEntry = values;
        this.mReadOnly = readOnly;
        setId(vig.getId(state, kind, values, 0));
        if (values == null) {
            resetDefault();
        } else if (kind.getmBitmapFromDetailCard() != null) {
            this.mPhotoImageView.setScaleType(ScaleType.FIT_XY);
            this.mPhotoImageView.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(getResources(), kind.getmBitmapFromDetailCard())));
            this.mFrameViewPressed.setEnabled(!this.mReadOnly);
            this.mHasSetPhoto = true;
            this.mEntry.setFromTemplate(false);
        } else {
            byte[] photoBytes = values.getAsByteArray("data15");
            if (photoBytes != null) {
                Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                this.mPhotoImageView.setScaleType(ScaleType.FIT_XY);
                this.mPhotoImageView.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(getResources(), photo)));
                View view = this.mFrameViewPressed;
                if (this.mReadOnly) {
                    z = false;
                } else {
                    z = true;
                }
                view.setEnabled(z);
                this.mHasSetPhoto = true;
                this.mEntry.setFromTemplate(false);
                if (values.getAfter() == null || values.getAfter().get("data15") == null) {
                    Integer photoFileId = values.getAsInteger("data14");
                    if (photoFileId != null) {
                        setFullSizedPhoto(DisplayPhoto.CONTENT_URI.buildUpon().appendPath(photoFileId.toString()).build());
                        return;
                    }
                    return;
                }
                return;
            }
            resetDefault();
        }
    }

    public boolean hasSetPhoto() {
        return this.mHasSetPhoto;
    }

    public void setPhotoBitmap(Bitmap photo) {
        boolean z;
        if (DEBUG) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("setPhotoBitmap photo == null:");
            if (photo == null) {
                z = true;
            } else {
                z = false;
            }
            HwLog.d(str, append.append(z).toString());
        }
        if (photo == null) {
            if (!this.mEntryList.isEmpty()) {
                for (ValuesDelta entry : this.mEntryList) {
                    entry.put("data15", (byte[]) null);
                }
            } else if (this.mEntry != null) {
                this.mEntry.put("data15", (byte[]) null);
            }
            resetDefault();
            return;
        }
        int size = ContactsUtils.getThumbnailSize(getContext());
        Bitmap scaled = Bitmap.createScaledBitmap(photo, size, size, false);
        if (DEBUG) {
            HwLog.d(TAG, "setPhotoBitmap createScaledBitmap scale:" + size);
        }
        byte[] compressed = ContactPhotoUtils.compressBitmap(scaled);
        if (scaled != photo) {
            scaled.recycle();
        }
        this.mPhotoImageView.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(getResources(), photo)));
        View view = this.mFrameViewPressed;
        if (this.mReadOnly) {
            z = false;
        } else {
            z = true;
        }
        view.setEnabled(z);
        this.mHasSetPhoto = true;
        if (!this.mEntryList.isEmpty()) {
            for (ValuesDelta entry2 : this.mEntryList) {
                entry2.setFromTemplate(false);
                entry2.setSuperPrimary(true);
                if (compressed != null) {
                    entry2.setPhoto(compressed);
                }
            }
        } else if (this.mEntry != null) {
            this.mEntry.setFromTemplate(false);
            this.mEntry.setSuperPrimary(true);
            if (compressed != null) {
                this.mEntry.setPhoto(compressed);
            }
        }
    }

    public void setSuperPrimary() {
        this.mEntry.put("is_super_primary", 1);
    }

    protected void resetDefault() {
        boolean z;
        View view = this.mFrameViewPressed;
        if (this.mReadOnly) {
            z = false;
        } else {
            z = true;
        }
        view.setEnabled(z);
        this.mHasSetPhoto = false;
        if (!this.mEntryList.isEmpty()) {
            for (ValuesDelta entry : this.mEntryList) {
                entry.setFromTemplate(true);
            }
        } else if (this.mEntry != null) {
            this.mEntry.setFromTemplate(true);
        }
    }

    public void setEditorListener(EditorListener listener) {
        this.mListener = listener;
    }

    public void setDeletable(boolean deletable) {
    }

    public boolean isEmpty() {
        return !this.mHasSetPhoto;
    }

    public void deleteEditor() {
    }

    public void clearAllFields() {
        resetDefault();
    }

    public void addEntry(ValuesDelta object) {
        this.mEntryList.add(object);
    }

    public void setContactId(long contactId) {
        this.mContactId = contactId;
    }

    public void setFullSizedPhoto(Uri photoUri) {
        if (photoUri != null) {
            this.mContactPhotoManager.loadPhoto(this.mPhotoImageView, photoUri, this.mPhotoImageView.getWidth(), false, null);
        }
    }
}
