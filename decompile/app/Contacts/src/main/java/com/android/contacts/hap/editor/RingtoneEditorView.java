package com.android.contacts.hap.editor;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.editor.ContactEditorInfo;
import com.android.contacts.editor.Editor;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.editor.ViewIdGenerator;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;

public class RingtoneEditorView extends LinearLayout implements Editor, OnClickListener, ContactEditorInfo {
    private View mActionsViewContainer;
    private DataKind mCachedDataKind = null;
    TextView mDataView;
    private ValuesDelta mEntry;
    private ArrayList<ValuesDelta> mEntryList = new ArrayList();
    private boolean mHasSetRingtone = false;
    private EditorListener mListener;
    private boolean mReadOnly;
    private TextView mTitleView;

    public RingtoneEditorView(Context context) {
        super(context);
    }

    public RingtoneEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        LinearLayout itemHeader = (LinearLayout) findViewById(R.id.ringtone);
        ViewUtil.setStateListIcon(getContext(), itemHeader.findViewById(R.id.toneIcon), false);
        itemHeader.setOnClickListener(this);
        this.mDataView = (TextView) findViewById(R.id.data);
        this.mTitleView = (TextView) findViewById(R.id.label);
        this.mActionsViewContainer = findViewById(R.id.actions_view_container);
    }

    public void onClick(View v) {
        if (this.mListener != null) {
            this.mListener.onRequest(6);
        }
    }

    public void setValues(DataKind kind, ValuesDelta values, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        setValues(kind, values, null, state, readOnly, vig);
    }

    public void setValues(DataKind kind, ValuesDelta values, RawContactDeltaList stateList, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        this.mEntry = values;
        if (stateList != null && stateList.size() > 1) {
            for (int i = 0; i < stateList.size(); i++) {
                this.mEntryList.add(((RawContactDelta) stateList.get(i)).getValues());
            }
        }
        this.mReadOnly = readOnly;
        if (this.mTitleView != null) {
            this.mTitleView.setText(kind.titleRes);
        }
        setId(vig.getId(state, kind, values, 0));
        if (CommonUtilMethods.isSimAccount(state.getAccountType())) {
            LayoutParams lp = (LayoutParams) this.mActionsViewContainer.getLayoutParams();
            lp.setMarginStart(0);
            this.mActionsViewContainer.setLayoutParams(lp);
        }
        if (values != null) {
            String lRingtoneString = getRingtone();
            String ROOT_EXTERNAL = Media.EXTERNAL_CONTENT_URI.toString();
            if (lRingtoneString != null) {
                if (TextUtils.isEmpty(lRingtoneString) || !lRingtoneString.equals("-1")) {
                    Ringtone lRingtone = null;
                    if (!TextUtils.isEmpty(lRingtoneString)) {
                        lRingtone = RingtoneManager.getRingtone(getContext(), Uri.parse(lRingtoneString));
                    }
                    if (lRingtone == null || TextUtils.isEmpty(lRingtone.getTitle(getContext()))) {
                        resetDefault();
                    } else if (!lRingtoneString.contains(ROOT_EXTERNAL)) {
                        this.mDataView.setText(lRingtone.getTitle(getContext()));
                    } else if (CommonUtilMethods.getPathFromUri(getContext(), Uri.parse(lRingtoneString)) == null) {
                        resetDefault();
                    } else {
                        this.mDataView.setText(lRingtone.getTitle(getContext()));
                    }
                } else {
                    this.mDataView.setText(getContext().getString(R.string.contact_silent_ringtone));
                }
                setEnabled(true);
                this.mHasSetRingtone = true;
                if (this.mEntryList.isEmpty()) {
                    this.mEntry.setFromTemplate(false);
                    return;
                }
                for (ValuesDelta entry : this.mEntryList) {
                    entry.setFromTemplate(false);
                }
                return;
            }
            resetDefault();
            return;
        }
        resetDefault();
    }

    public String getRingtone() {
        if (this.mEntry == null) {
            return null;
        }
        String ringTone = this.mEntry.getAsString("custom_ringtone");
        if (ringTone == null && this.mEntryList.size() > 1) {
            for (ValuesDelta entry : this.mEntryList) {
                String ring = entry.getAsString("custom_ringtone");
                if (ring != null) {
                    ringTone = ring;
                    break;
                }
            }
        }
        return ringTone;
    }

    public void setRingtone(String ringtone) {
        if (ringtone == null) {
            HwLog.d("TAG", "Ringtone is null");
            if (this.mEntryList.isEmpty()) {
                this.mEntry.putNull("custom_ringtone");
            } else {
                for (ValuesDelta entry : this.mEntryList) {
                    entry.putNull("custom_ringtone");
                }
            }
            resetDefault();
            return;
        }
        if (TextUtils.isEmpty(ringtone) || !ringtone.equals("-1")) {
            Ringtone rRingtone = RingtoneManager.getRingtone(getContext(), Uri.parse(ringtone));
            if (rRingtone != null) {
                this.mDataView.setText(rRingtone.getTitle(getContext()));
            } else {
                return;
            }
        }
        this.mDataView.setText(getContext().getString(R.string.contact_silent_ringtone));
        if (this.mEntryList.isEmpty()) {
            this.mEntry.put("custom_ringtone", ringtone);
        } else {
            for (ValuesDelta entry2 : this.mEntryList) {
                entry2.put("custom_ringtone", ringtone);
            }
        }
        setEnabled(true);
        this.mHasSetRingtone = true;
    }

    public void setSuperPrimary(boolean superPrimary) {
    }

    protected void resetDefault() {
        this.mDataView.setText(getContext().getString(R.string.default_ringtone));
        if (this.mReadOnly) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
        this.mHasSetRingtone = false;
    }

    public void setEditorListener(EditorListener listener) {
        this.mListener = listener;
    }

    public boolean isEmpty() {
        return false;
    }

    public void setDeletable(boolean deletable) {
    }

    public void deleteEditor() {
    }

    public void clearAllFields() {
    }

    public void editNewlyAddedField() {
    }

    public String getTitle() {
        return getContext().getString(R.string.phone_ringtone_string);
    }

    public DataKind getKind() {
        return this.mCachedDataKind;
    }

    public void setDataKind(DataKind kind) {
        this.mCachedDataKind = kind;
    }
}
