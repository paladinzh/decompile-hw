package com.android.contacts.editor;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.BaseAccountType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.DialogManager.DialogShowingView;
import com.android.contacts.util.DialogManager.DialogShowingViewActivity;
import com.android.contacts.util.TextUtil;
import com.android.contacts.widget.AbstractExpandableViewAdapter.ExpandCollapseAnimation;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public abstract class LabeledEditorView extends RelativeLayout implements Editor, DialogShowingView {
    public static final EditType CUSTOM_SELECTION = new EditType(0, 0);
    private View line;
    private ImageView mDelete;
    private View mDeleteContainer;
    private DialogManager mDialogManager = null;
    protected int mEditTextItemHeight;
    private int mEditTextWidth;
    private EditTypeAdapter mEditTypeAdapter;
    protected int mEditorItemTotalWidth;
    private int mEditorItemWidth;
    private ValuesDelta mEntry;
    private ArrayList<ValuesDelta> mEntryList = new ArrayList();
    private boolean mIsAttachedToWindow;
    private boolean mIsDeletable = true;
    private boolean mIsInMultiWindowMode;
    protected DataKind mKind;
    private Spinner mLabel;
    private String mLableSelectText;
    private TextView mLableSelectTextView;
    private int mLableTextMinWidth;
    private int mLableTextWidth;
    private EditorListener mListener;
    private boolean mReadOnly;
    private int mSelection = -1;
    private RawContactDelta mState;
    private EditType mType;
    private ViewIdGenerator mViewIdGenerator;
    private boolean mWasEmpty = true;
    private LinearLayout mainLayout;
    private int mlableTextLineFeedMaxWidth;

    private class EditTypeAdapter extends ArrayAdapter<EditType> {
        private boolean mHasCustomSelection;
        private final LayoutInflater mInflater;
        int selectedColor = 0;
        int unSelectedColor = 0;

        public EditTypeAdapter(Context context) {
            super(context, 0);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.selectedColor = LabeledEditorView.this.getResources().getColor(R.color.contact_editor_spinner_unselected_color);
            this.unSelectedColor = LabeledEditorView.this.getResources().getColor(R.color.contact_editor_spinner_unselected_color);
            if (!(LabeledEditorView.this.mType == null || LabeledEditorView.this.mType.customColumn == null || LabeledEditorView.this.mEntry.getAsString(LabeledEditorView.this.mType.customColumn) == null)) {
                add(LabeledEditorView.CUSTOM_SELECTION);
                this.mHasCustomSelection = true;
            }
            addAll(RawContactModifier.getValidTypes(LabeledEditorView.this.mState, LabeledEditorView.this.mKind, LabeledEditorView.this.mType, true, null, getContext(), true));
        }

        public boolean hasCustomSelection() {
            return this.mHasCustomSelection;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent, 17367048);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createViewFromLocalResource(position, convertView, parent, R.layout.contact_editor_spinner_list_item);
        }

        private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
            TextView textView;
            String text;
            if (convertView == null) {
                textView = (TextView) this.mInflater.inflate(resource, parent, false);
                textView.setHorizontalFadingEdgeEnabled(false);
                textView.setEllipsize(TruncateAt.END);
            } else {
                textView = (TextView) convertView;
            }
            EditType type = (EditType) getItem(position);
            if (type == LabeledEditorView.CUSTOM_SELECTION) {
                text = LabeledEditorView.this.mEntry.getAsString(LabeledEditorView.this.mType.customColumn);
            } else {
                text = getContext().getString(type.labelRes);
            }
            textView.setText(text);
            textView.setPaddingRelative(0, 0, 0, 0);
            if (LabeledEditorView.this.mSelection != position) {
                textView.setWidth(LabeledEditorView.this.mLableTextMinWidth);
                return textView;
            } else if (text != null && text.equals(LabeledEditorView.this.mLableSelectText)) {
                return textView;
            } else {
                LabeledEditorView.this.mLableSelectText = text;
                if (TextUtil.getTextWidth(text, textView.getTextSize()) > LabeledEditorView.this.mLableTextMinWidth) {
                    textView.setMinimumWidth(LabeledEditorView.this.mLableTextMinWidth);
                } else {
                    textView.setWidth(LabeledEditorView.this.mLableTextMinWidth);
                }
                LabeledEditorView.this.mLableSelectTextView = textView;
                LabeledEditorView.this.setLableWidth(text);
                return LabeledEditorView.this.mLableSelectTextView;
            }
        }

        private View createViewFromLocalResource(int position, View convertView, ViewGroup parent, int resource) {
            View resultView;
            String text;
            int color;
            if (convertView == null) {
                resultView = this.mInflater.inflate(resource, parent, false);
            } else {
                resultView = convertView;
            }
            TextView textView = (TextView) resultView.findViewById(R.id.text);
            EditType type = (EditType) getItem(position);
            if (type == LabeledEditorView.CUSTOM_SELECTION) {
                text = LabeledEditorView.this.mEntry.getAsString(LabeledEditorView.this.mType.customColumn);
                color = this.selectedColor;
            } else {
                text = getContext().getString(type.labelRes);
                if (this.mHasCustomSelection || !LabeledEditorView.this.mType.equals(getItem(position))) {
                    color = this.unSelectedColor;
                } else {
                    color = this.selectedColor;
                }
            }
            textView.setText(text);
            textView.setBackgroundColor(color);
            return resultView;
        }
    }

    private class EditTypeSpinner extends Spinner {
        OnItemSelectedListener listener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
                LabeledEditorView.this.mSelection = position;
                LabeledEditorView.this.onTypeSelectionChange(position);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        public EditTypeSpinner(Context context) {
            super(context);
            setOnItemSelectedListener(this.listener);
        }
    }

    private static class UpdateAnimationListener implements AnimationListener {
        private View mTarget;

        public UpdateAnimationListener(View target) {
            this.mTarget = target;
        }

        public void onAnimationStart(Animation animation) {
            this.mTarget.setAlpha(0.0f);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            ViewGroup victimParent = (ViewGroup) this.mTarget.getParent();
            if (victimParent != null) {
                victimParent.removeView(this.mTarget);
            }
        }
    }

    protected abstract void requestFocusForFirstEditField();

    public RawContactDelta getState() {
        return this.mState;
    }

    public LabeledEditorView(Context context) {
        super(context);
    }

    public LabeledEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabeledEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mLabel != null && this.mType != null) {
            initLableWidth();
            setLableWidth(this.mLableSelectText);
        }
    }

    private void initLableWidth() {
        if (this.mIsInMultiWindowMode) {
            this.mlableTextLineFeedMaxWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_type_label_LineFeed_max_multiwindow_width);
            this.mEditorItemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_item_multiwindow_width);
            this.mEditorItemTotalWidth = getContext().getResources().getDimensionPixelSize(R.dimen.edit_item_total_multiwindow_width);
            return;
        }
        this.mlableTextLineFeedMaxWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_type_label_LineFeed_max_width);
        this.mEditorItemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_item_width);
        this.mEditorItemTotalWidth = getContext().getResources().getDimensionPixelSize(R.dimen.edit_item_total_width);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View lSpinner = findViewById(R.id.spinner);
        this.mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        this.line = findViewById(R.id.line);
        if (!(this.mainLayout == null || lSpinner == null)) {
            this.mLableTextMinWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_type_label_width);
            this.mlableTextLineFeedMaxWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_type_label_LineFeed_max_width);
            this.mEditorItemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.editor_item_width);
            this.mEditorItemTotalWidth = getContext().getResources().getDimensionPixelSize(R.dimen.edit_item_total_width);
            this.mLableTextWidth = this.mLableTextMinWidth;
            this.mLabel = new EditTypeSpinner(getContext());
            this.mLabel.setId(-1);
            this.mainLayout.addView(this.mLabel, this.mainLayout.indexOfChild(lSpinner), lSpinner.getLayoutParams());
            this.mainLayout.removeView(lSpinner);
            if (this.mainLayout.getContext() instanceof ContactEditorActivity) {
                this.mIsInMultiWindowMode = ((ContactEditorActivity) this.mainLayout.getContext()).isInMultiWindowMode();
                if (this.mIsInMultiWindowMode) {
                    initLableWidth();
                }
            }
        }
        this.mDelete = (ImageView) findViewById(R.id.delete_button);
        this.mDeleteContainer = findViewById(R.id.delete_button_container);
        if (this.mDeleteContainer != null) {
            this.mDeleteContainer.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    new Handler().post(new Runnable() {
                        public void run() {
                            if (LabeledEditorView.this.mIsAttachedToWindow && LabeledEditorView.this.mListener != null) {
                                LabeledEditorView.this.mListener.onDeleteRequested(LabeledEditorView.this);
                            }
                        }
                    });
                }
            });
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIsAttachedToWindow = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsAttachedToWindow = false;
    }

    public void deleteEditor() {
        ArrayList<ValuesDelta> entryList = getValuesList();
        if (entryList.size() > 0) {
            for (ValuesDelta entry : entryList) {
                entry.markDeleted();
            }
        } else {
            this.mEntry.markDeleted();
        }
        if (this.mKind.mimeType.equals(this.mState.getExtraMimetype())) {
            this.mState.setHasExtra(this.mKind.mimeType, false);
        }
        animateCollapseView(this);
    }

    private void animateCollapseView(View target) {
        Animation anim = new ExpandCollapseAnimation(target, 1);
        anim.setDuration(280);
        target.startAnimation(anim);
        anim.setAnimationListener(new UpdateAnimationListener(target));
    }

    public boolean isReadOnly() {
        return this.mReadOnly;
    }

    private void setupLabelButton(boolean shouldExist) {
        if (this.mLabel != null) {
            if (shouldExist) {
                boolean z;
                Spinner spinner = this.mLabel;
                if (this.mReadOnly) {
                    z = false;
                } else {
                    z = isEnabled();
                }
                spinner.setEnabled(z);
                this.mLabel.setVisibility(0);
            } else {
                this.mLabel.setVisibility(8);
            }
        }
    }

    private void setupDeleteButton() {
        boolean z = false;
        if (this.mDeleteContainer == null) {
            return;
        }
        if (this.mIsDeletable) {
            if (isEmpty()) {
                this.mDeleteContainer.setVisibility(4);
            } else {
                this.mDeleteContainer.setVisibility(0);
            }
            ImageView imageView = this.mDelete;
            if (!this.mReadOnly) {
                z = isEnabled();
            }
            imageView.setEnabled(z);
            return;
        }
        this.mDeleteContainer.setVisibility(4);
    }

    public void setDeleteButtonVisible(boolean visible) {
        if (this.mIsDeletable && this.mDeleteContainer != null) {
            int i;
            View view = this.mDeleteContainer;
            if (visible) {
                i = 0;
            } else {
                i = 4;
            }
            view.setVisibility(i);
        }
    }

    protected void onOptionalFieldVisibilityChange() {
        if (this.mListener != null) {
            this.mListener.onRequest(5);
        }
    }

    public void setEditorListener(EditorListener listener) {
        this.mListener = listener;
    }

    public void setDeletable(boolean deletable) {
        this.mIsDeletable = deletable;
        setupDeleteButton();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mLabel != null) {
            boolean z;
            Spinner spinner = this.mLabel;
            if (this.mReadOnly) {
                z = false;
            } else {
                z = enabled;
            }
            spinner.setEnabled(z);
        }
        if (this.mDelete != null) {
            ImageView imageView = this.mDelete;
            if (this.mReadOnly) {
                enabled = false;
            }
            imageView.setEnabled(enabled);
        }
    }

    protected DataKind getKind() {
        return this.mKind;
    }

    protected ValuesDelta getEntry() {
        return this.mEntry;
    }

    protected EditType getType() {
        return this.mType;
    }

    private void rebuildLabel() {
        this.mEditTypeAdapter = new EditTypeAdapter(getContext());
        if (this.mLabel != null) {
            this.mLabel.setAdapter(this.mEditTypeAdapter);
            if (this.mEditTypeAdapter.hasCustomSelection()) {
                this.mSelection = this.mEditTypeAdapter.getPosition(CUSTOM_SELECTION);
                this.mLabel.setSelection(this.mSelection);
                return;
            }
            this.mSelection = this.mEditTypeAdapter.getPosition(this.mType);
            this.mLabel.setSelection(this.mSelection);
        }
    }

    private void setLableWidth(String text) {
        if (this.mLableSelectTextView != null) {
            this.mLableTextWidth = Math.max(TextUtil.getTextWidth(text, this.mLableSelectTextView.getTextSize()), this.mLableTextMinWidth);
            if (this.mEditTextWidth + this.mLableTextWidth > this.mEditorItemWidth || this.mEditTextWidth > this.mlableTextLineFeedMaxWidth) {
                this.mainLayout.setOrientation(1);
                this.line.setVisibility(0);
            } else {
                this.mainLayout.setOrientation(0);
                this.line.setVisibility(8);
            }
        }
    }

    private void rebuildLabelWithBestType() {
        this.mEditTypeAdapter = new EditTypeAdapter(getContext());
        if (this.mLabel != null) {
            this.mLabel.setAdapter(this.mEditTypeAdapter);
        }
        if (!this.mEditTypeAdapter.hasCustomSelection()) {
            EditType bestType = this.mType;
            if (!(this.mType == null || this.mType.customColumn == null)) {
                bestType = RawContactModifier.getBestValidType(this.mState, this.mKind, false, Integer.MIN_VALUE);
                if (bestType == null) {
                    bestType = RawContactModifier.getBestValidType(this.mState, this.mKind, false, Integer.MIN_VALUE);
                }
            }
            if (bestType != null && this.mLabel != null) {
                this.mLabel.setSelection(this.mEditTypeAdapter.getPosition(bestType));
            }
        } else if (this.mLabel != null) {
            int position = this.mEditTypeAdapter.getPosition(CUSTOM_SELECTION);
            this.mSelection = position;
            this.mLabel.setSelection(position);
        }
    }

    public void onFieldChanged(String column, String value, View view) {
        if (value != null) {
            onFileChangedLableWidth((TextView) view);
            if (isFieldChanged(column, value)) {
                saveValue(column, value);
                notifyEditorListener();
            }
        }
    }

    private void onFileChangedLableWidth(TextView value) {
        if (value != null && this.mainLayout != null) {
            this.mEditTextWidth = TextUtil.getTextWidth(value.getText().toString(), value.getTextSize());
            if (this.mLableTextWidth + this.mEditTextWidth > this.mEditorItemWidth || this.mLableTextWidth > this.mlableTextLineFeedMaxWidth) {
                this.mainLayout.setOrientation(1);
                this.line.setVisibility(0);
            } else {
                this.mainLayout.setOrientation(0);
                this.line.setVisibility(8);
            }
        }
    }

    protected void saveValue(String column, String value) {
        ArrayList<ValuesDelta> entryList = getValuesList();
        if (entryList.size() > 0) {
            for (ValuesDelta entry : entryList) {
                entry.put(column, value);
            }
            return;
        }
        this.mEntry.put(column, value);
    }

    protected void notifyEditorListener() {
        if (this.mListener != null) {
            this.mListener.onRequest(2);
        }
        boolean isEmpty = isEmpty();
        if (this.mWasEmpty != isEmpty) {
            if (isEmpty) {
                if (this.mListener != null) {
                    this.mListener.onRequest(3);
                }
                if (this.mIsDeletable && this.mDeleteContainer != null) {
                    this.mDeleteContainer.setVisibility(4);
                }
            } else {
                if (this.mListener != null) {
                    this.mListener.onRequest(4);
                }
                if (this.mIsDeletable && this.mDeleteContainer != null) {
                    this.mDeleteContainer.setVisibility(0);
                }
            }
            this.mWasEmpty = isEmpty;
        }
    }

    protected boolean isFieldChanged(String column, String value) {
        String dbValue;
        String valueNoNull;
        ArrayList<ValuesDelta> entryList = getValuesList();
        if (entryList.size() > 0) {
            dbValue = ((ValuesDelta) entryList.get(0)).getAsString(column);
        } else {
            dbValue = this.mEntry.getAsString(column);
        }
        String dbValueNoNull = dbValue == null ? "" : dbValue;
        if (value == null) {
            valueNoNull = "";
        } else {
            valueNoNull = value;
        }
        if (TextUtils.equals(dbValueNoNull, valueNoNull)) {
            return false;
        }
        return true;
    }

    protected void rebuildValues() {
        setValues(this.mKind, this.mEntry, this.mState, this.mReadOnly, this.mViewIdGenerator);
    }

    public void setValues(DataKind kind, ValuesDelta entry, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        boolean z = false;
        this.mKind = kind;
        this.mEntry = entry;
        this.mState = state;
        this.mReadOnly = readOnly;
        this.mViewIdGenerator = vig;
        setId(vig.getId(state, kind, entry, -1));
        if (entry.isVisible()) {
            setVisibility(0);
            boolean hasTypes = RawContactModifier.hasEditTypes(kind);
            setupLabelButton(hasTypes);
            if (this.mLabel != null) {
                Spinner spinner = this.mLabel;
                if (!readOnly) {
                    z = isEnabled();
                }
                spinner.setEnabled(z);
            }
            if (hasTypes) {
                this.mType = RawContactModifier.getCurrentType(entry, kind);
                if (this.mType == null && "vnd.android.cursor.item/phone_v2".equals(kind.mimeType)) {
                    this.mType = BaseAccountType.getOtherEditType();
                }
                if (this.mType == null && "vnd.android.cursor.item/email_v2".equals(kind.mimeType)) {
                    this.mType = BaseAccountType.getEmailOtherEditType();
                }
                if (this.mType == null && "vnd.android.cursor.item/im".equals(kind.mimeType)) {
                    this.mType = BaseAccountType.getImDefaultEditType();
                }
                if (this.mType == null && "vnd.android.cursor.item/postal-address_v2".equals(kind.mimeType)) {
                    this.mType = BaseAccountType.getStructuredPostalOtherEditType();
                }
                rebuildLabel();
            }
            return;
        }
        setVisibility(8);
    }

    public ValuesDelta getValues() {
        return this.mEntry;
    }

    private Dialog createCustomDialog(Bundle bundle) {
        View view = LayoutInflater.from(new ContextThemeWrapper(getContext(), getContext().getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null))).inflate(R.layout.labeled_editor_view, null);
        final EditText customType = (EditText) view.findViewById(R.id.custom_dialog_content);
        customType.setInputType(8193);
        customType.setSaveEnabled(true);
        customType.requestFocus();
        Builder builder = new Builder(getContext());
        if (bundle.getString("dialog_title") != null) {
            builder.setTitle(bundle.getString("dialog_title"));
        } else {
            builder.setTitle(R.string.customLabelPickerTitle);
        }
        builder.setView(view);
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String customText = customType.getText().toString().trim();
                if (ContactsUtils.isGraphic(customText)) {
                    List<EditType> allTypes = RawContactModifier.getValidTypes(LabeledEditorView.this.mState, LabeledEditorView.this.mKind, LabeledEditorView.this.mType);
                    LabeledEditorView.this.mType = null;
                    for (EditType editType : allTypes) {
                        if (editType.customColumn != null) {
                            LabeledEditorView.this.mType = editType;
                            break;
                        }
                    }
                    if (LabeledEditorView.this.mType != null) {
                        ArrayList<ValuesDelta> entryList = LabeledEditorView.this.getValuesList();
                        if (entryList.size() > 0) {
                            for (ValuesDelta entry : entryList) {
                                entry.put(LabeledEditorView.this.mKind.typeColumn, LabeledEditorView.this.mType.rawValue);
                                entry.put(LabeledEditorView.this.mType.customColumn, customText);
                            }
                        } else {
                            LabeledEditorView.this.mEntry.put(LabeledEditorView.this.mKind.typeColumn, LabeledEditorView.this.mType.rawValue);
                            LabeledEditorView.this.mEntry.put(LabeledEditorView.this.mType.customColumn, customText);
                        }
                        LabeledEditorView.this.invokeRebuilder();
                    }
                } else if (TextUtils.isEmpty(customText)) {
                    LabeledEditorView.this.invokeRebuilder();
                }
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                LabeledEditorView.this.onCancelAction();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                LabeledEditorView.this.onCancelAction();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                LabeledEditorView.this.updateCustomDialogOkButtonState(dialog, customType);
                LabeledEditorView.this.updateCustomDialogCancelButtonState(dialog);
            }
        });
        customType.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                LabeledEditorView.this.updateCustomDialogOkButtonState(dialog, customType);
            }
        });
        dialog.getWindow().setSoftInputMode(5);
        return dialog;
    }

    private void onCancelAction() {
        if (!this.mEditTypeAdapter.hasCustomSelection() && "0".equals(this.mEntry.getAsString("data2")) && "vnd.android.cursor.item/postal-address_v2".equals(this.mEntry.getMimetype())) {
            new Handler().post(new Runnable() {
                public void run() {
                    LabeledEditorView.this.deleteEditor();
                }
            });
        } else {
            invokeRebuilder();
        }
    }

    void updateCustomDialogOkButtonState(AlertDialog dialog, EditText editText) {
        Button okButton = dialog.getButton(-1);
        if (okButton != null) {
            boolean z;
            if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                z = false;
            } else {
                z = true;
            }
            okButton.setEnabled(z);
        }
    }

    void updateCustomDialogCancelButtonState(AlertDialog dialog) {
        boolean z = true;
        Button cancelButton = dialog.getButton(-2);
        if (cancelButton != null) {
            if (this.mType != null) {
                if (this.mType.specificMax == 1 && this.mType.customColumn != null) {
                    z = false;
                }
                cancelButton.setEnabled(z);
            } else {
                cancelButton.setEnabled(false);
            }
        }
    }

    private void invokeRebuilder() {
        rebuildLabelWithBestType();
        requestFocusForFirstEditField();
        onLabelRebuilt();
    }

    protected void onLabelRebuilt() {
    }

    protected void onTypeSelectionChange(int position) {
        EditType selected = (EditType) this.mEditTypeAdapter.getItem(position);
        if (!this.mEditTypeAdapter.hasCustomSelection() || selected != CUSTOM_SELECTION) {
            ArrayList<ValuesDelta> entryList;
            if (this.mType == null) {
                this.mType = selected;
                entryList = getValuesList();
                if (entryList.size() > 0) {
                    for (ValuesDelta entry : entryList) {
                        entry.put(this.mKind.typeColumn, this.mType.rawValue);
                    }
                } else {
                    this.mEntry.put(this.mKind.typeColumn, this.mType.rawValue);
                }
                rebuildLabel();
                onLabelRebuilt();
            } else if (this.mType != selected || this.mType.customColumn != null) {
                if (selected.customColumn != null) {
                    showDialog(1, selected.customTitle);
                } else {
                    this.mType = selected;
                    entryList = getValuesList();
                    if (entryList.size() > 0) {
                        for (ValuesDelta entry2 : entryList) {
                            entry2.put(this.mKind.typeColumn, this.mType.rawValue);
                        }
                    } else {
                        this.mEntry.put(this.mKind.typeColumn, this.mType.rawValue);
                    }
                    rebuildLabel();
                    requestFocusForFirstEditField();
                    onLabelRebuilt();
                }
            }
        }
    }

    void showDialog(int bundleDialogId, String customDialogTitle) {
        Bundle bundle = new Bundle();
        bundle.putInt("dialog_id", bundleDialogId);
        bundle.putString("dialog_title", customDialogTitle);
        getDialogManager().showDialogInView(this, bundle);
    }

    private DialogManager getDialogManager() {
        if (this.mDialogManager == null) {
            Context context = getContext();
            if (context instanceof DialogShowingViewActivity) {
                this.mDialogManager = ((DialogShowingViewActivity) context).getDialogManager();
            } else {
                throw new IllegalStateException("View must be hosted in an Activity that implements DialogManager.DialogShowingViewActivity");
            }
        }
        return this.mDialogManager;
    }

    public Dialog createDialog(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("bundle must not be null");
        }
        int dialogId = bundle.getInt("dialog_id");
        switch (dialogId) {
            case 1:
                return createCustomDialog(bundle);
            default:
                throw new IllegalArgumentException("Invalid dialogId: " + dialogId);
        }
    }

    public void setIfDataPickerShouldBeDisplayed(boolean val) {
        getKind().shouldShowDatePicker = val;
    }

    public void addEntry(ValuesDelta object) {
        this.mEntryList.add(object);
    }

    public ArrayList<ValuesDelta> getValuesList() {
        return this.mEntryList;
    }
}
