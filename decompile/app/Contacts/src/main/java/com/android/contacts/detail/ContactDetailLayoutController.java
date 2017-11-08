package com.android.contacts.detail;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import com.android.contacts.NfcHandler;
import com.android.contacts.detail.ContactDetailFragment.Listener;
import com.android.contacts.model.Contact;
import com.android.contacts.util.UriUtils;

public class ContactDetailLayoutController {
    private final Activity mActivity;
    private Contact mContactData;
    private final Listener mContactDetailFragmentListener;
    private boolean mContactHasUpdates;
    private Uri mContactUri;
    private ContactDetailFragment mDetailFragment = null;
    private Bundle mSavedState;

    public ContactDetailLayoutController(Activity activity, ContactDetailFragment detailFragment, Bundle savedState, Listener contactDetailFragmentListener) {
        this.mActivity = activity;
        this.mSavedState = savedState;
        this.mDetailFragment = detailFragment;
        this.mContactDetailFragmentListener = contactDetailFragmentListener;
        initialize(this.mDetailFragment);
    }

    private void initialize(ContactDetailFragment detailFragment) {
        if (this.mDetailFragment != null) {
            this.mDetailFragment.setListener(this.mContactDetailFragmentListener);
            NfcHandler.register(this.mActivity, this.mDetailFragment);
            if (this.mSavedState != null) {
                this.mContactUri = (Uri) this.mSavedState.getParcelable("contactUri");
                this.mContactHasUpdates = this.mSavedState.getBoolean("contactHasUpdates");
                if (this.mContactHasUpdates) {
                    showContactWithUpdates(false);
                } else {
                    showContactWithoutUpdates();
                }
            }
        }
    }

    public void setContactData(Contact data) {
        boolean contactWasLoaded;
        boolean z;
        boolean z2 = false;
        boolean contactHadUpdates;
        if (this.mContactData == null) {
            contactHadUpdates = false;
            contactWasLoaded = false;
        } else {
            contactHadUpdates = this.mContactHasUpdates;
            contactWasLoaded = true;
            if (UriUtils.areEqual(this.mContactData.getLookupUri(), data.getLookupUri())) {
            }
        }
        this.mContactData = data;
        if (data.getStreamItems().isEmpty()) {
            z = false;
        } else {
            z = true;
        }
        this.mContactHasUpdates = z;
        if (this.mContactHasUpdates) {
            if (contactWasLoaded && !r0) {
                z2 = true;
            }
            showContactWithUpdates(z2);
            return;
        }
        showContactWithoutUpdates();
    }

    public void showEmptyState() {
        if (this.mDetailFragment != null) {
            this.mDetailFragment.showEmptyState();
        }
    }

    private void showContactWithUpdates(boolean animateStateChange) {
        if (this.mContactData != null) {
            Uri previousContactUri = this.mContactUri;
            this.mContactUri = this.mContactData.getLookupUri();
            if (!UriUtils.areEqual(previousContactUri, this.mContactUri)) {
                resetFragments();
            }
            if (this.mDetailFragment != null) {
                this.mDetailFragment.setData(this.mContactUri, this.mContactData);
            }
        }
    }

    private void showContactWithoutUpdates() {
        if (this.mContactData != null) {
            Uri previousContactUri = this.mContactUri;
            this.mContactUri = this.mContactData.getLookupUri();
            if (!UriUtils.areEqual(previousContactUri, this.mContactUri)) {
                resetFragments();
            }
            if (this.mDetailFragment != null) {
                this.mDetailFragment.setData(this.mContactUri, this.mContactData);
            }
        }
    }

    private void resetFragments() {
        if (this.mDetailFragment != null) {
            this.mDetailFragment.resetAdapter();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("contactUri", this.mContactUri);
        outState.putBoolean("contactHasUpdates", this.mContactHasUpdates);
    }
}
