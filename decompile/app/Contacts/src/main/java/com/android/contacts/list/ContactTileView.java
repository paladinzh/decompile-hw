package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.MoreContactUtils;
import com.google.android.gms.R;

public abstract class ContactTileView extends FrameLayout {
    private static final String TAG = ContactTileView.class.getSimpleName();
    private long mId;
    private View mInfoContainer;
    protected Listener mListener;
    private String mLookupKey;
    private Uri mLookupUri;
    private TextView mName;
    private ImageView mPhoto;
    private ContactPhotoManager mPhotoManager = null;
    private ImageView mPhotoOverlay;
    private View mPhotoPressView;

    public interface Listener {
        void addContact();

        void onCallNumberDirectly(String str);

        void onContactSelected(Uri uri, Rect rect);

        void onViewDetailAction(Uri uri);
    }

    public ContactTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mName = (TextView) findViewById(R.id.contact_tile_name);
        this.mPhoto = (ImageView) findViewById(R.id.contact_tile_image);
        this.mPhotoOverlay = (ImageView) findViewById(R.id.contact_tile_image_overlay);
        this.mPhotoPressView = findViewById(R.id.contact_tile_pressed);
        this.mInfoContainer = findViewById(R.id.contact_tile_info_container);
        this.mInfoContainer.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Uri lookupUri = ContactTileView.this.getLookupUri();
                if (ContactTileView.this.mListener != null && lookupUri != null) {
                    ContactTileView.this.mListener.onViewDetailAction(lookupUri);
                }
            }
        });
        this.mPhotoPressView.setOnClickListener(createClickListener());
    }

    protected OnClickListener createClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                if (ContactTileView.this.mListener != null) {
                    Uri lContactUri = ContactTileView.this.getLookupUri();
                    if (lContactUri == null) {
                        ContactTileView.this.mListener.addContact();
                    } else {
                        ContactTileView.this.mListener.onContactSelected(lContactUri, MoreContactUtils.getTargetRectFromView(ContactTileView.this.getContext(), ContactTileView.this));
                    }
                }
            }
        };
    }

    public Uri getLookupUri() {
        return getAndUpdateLookupKey();
    }

    public Uri getAndUpdateLookupKey() {
        if (this.mLookupUri == null && this.mLookupKey != null && this.mId > 0) {
            this.mLookupUri = ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, this.mLookupKey), this.mId);
        }
        return this.mLookupUri;
    }
}
