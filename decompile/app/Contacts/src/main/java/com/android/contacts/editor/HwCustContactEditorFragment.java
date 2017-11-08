package com.android.contacts.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.FrameLayout;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;

public class HwCustContactEditorFragment {
    protected static final String TAG = "ContactEditorFragment";
    Context mContext;

    public HwCustContactEditorFragment(Context context) {
        this.mContext = context;
    }

    public boolean isSupportValidateDuplicate() {
        return false;
    }

    public boolean doValidateDuplicate(int saveMode, RawContactDeltaList mState, int mStatus, int mEditing, Activity activity, ContactEditorFragment mContactEditorFragment) {
        return true;
    }

    public void setValue() {
    }

    public void setNewContactFragment(ContactEditorFragment mContactEditorFragment) {
    }

    public void customizeOnActivityResult(int requestCode, int resultCode, Intent data, Context context, FrameLayout mContent, long mRawContactIdRequestingRingtone) {
    }

    public void addSaveIntentExtras(Intent intent) {
    }

    public boolean hasCustomFeatureChange(long rawContactId) {
        return false;
    }

    public RawContactDelta getBestState(RawContactDeltaList stateList, RawContactDelta bestState, long nameRawContactId) {
        return bestState;
    }
}
