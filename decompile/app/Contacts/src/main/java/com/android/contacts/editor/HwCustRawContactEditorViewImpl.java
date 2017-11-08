package com.android.contacts.editor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.SystemProperties;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.Locale;

public class HwCustRawContactEditorViewImpl extends HwCustRawContactEditorView {
    private static final String TAG = "HwCustRawContactEditorViewImpl";

    public boolean handleAnniversaryCust(KindSectionView sectionView, ArrayList<KindSectionView> fields) {
        if (!HwCustCommonConstants.IS_AAB_ATT) {
            return false;
        }
        if (isAnniversaryPresent(sectionView)) {
            RawContactEditorView.setAnniverseryPos(-1);
        } else {
            fields.add(sectionView);
            RawContactEditorView.setAnniverseryPos(fields.size() - 1);
        }
        return true;
    }

    private boolean isAnniversaryPresent(KindSectionView sectionView) {
        ViewGroup lEditors = sectionView.getEditor();
        if (lEditors != null) {
            int i = 0;
            while (i < lEditors.getChildCount()) {
                if ((lEditors.getChildAt(i) instanceof EventFieldEditorView) && ((EventFieldEditorView) lEditors.getChildAt(i)).getType().rawValue == 1) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    public void hideTextFieldsEditorView(TextFieldsEditorView mOrganisationName) {
        if (SystemProperties.getBoolean("ro.config.hide_fields_view", false) && mOrganisationName != null) {
            mOrganisationName.setVisibility(8);
        }
    }

    public void removeViews(ViewGroup mFields, LinearLayout mVibration) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            mFields.removeView(mVibration);
        }
    }

    public void addViews(ViewGroup mFields, View view) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && view != null && view.getParent() == null) {
            mFields.addView(view);
        }
    }

    public LinearLayout inflateNewViews(Context context, LayoutInflater mInflater, ContactEditorFragment mFragment, ViewGroup mFields, long rawContactId) {
        if (!HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            return null;
        }
        LinearLayout mVibration = (LinearLayout) mInflater.inflate(R.layout.edit_contact_entry_ringtone, mFields, false);
        if (mVibration == null) {
            return null;
        }
        ((TextView) mVibration.findViewById(R.id.type)).setText(context.getResources().getString(R.string.phone_vibration));
        Object obj = null;
        Cursor cursor = null;
        if (rawContactId > 0) {
            try {
                cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"vibration_type"}, "_id=" + rawContactId, null, null);
                if (cursor.moveToFirst()) {
                    obj = cursor.getString(0);
                }
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Execption during query");
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            }
        }
        if (TextUtils.isEmpty(obj)) {
            ((TextView) mVibration.findViewById(R.id.data)).setText(context.getResources().getString(R.string.default_option));
        } else {
            ((TextView) mVibration.findViewById(R.id.data)).setText(obj.replace("_", HwCustPreloadContacts.EMPTY_STRING).toUpperCase(Locale.US));
        }
        final ContactEditorFragment contactEditorFragment = mFragment;
        mVibration.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwCustRawContactEditorViewImpl.this.pickVibration(contactEditorFragment);
            }
        });
        return mVibration;
    }

    private void pickVibration(ContactEditorFragment mFragment) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            Intent vibrationIntent = new Intent();
            vibrationIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.sound.VibratePatternPickerActivity"));
            vibrationIntent.setAction("android.intent.action.VIBRATION_PICKER");
            if (mFragment != null) {
                mFragment.startActivityForResult(vibrationIntent, 1003);
            }
        }
    }
}
