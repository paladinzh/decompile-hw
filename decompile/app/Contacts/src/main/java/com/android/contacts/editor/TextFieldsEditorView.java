package com.android.contacts.editor;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.editor.CompanyListAdater;
import com.android.contacts.hap.editor.PopupCompanyListTask;
import com.android.contacts.hap.sim.IIccPhoneBookAdapter;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import java.util.ArrayList;
import java.util.Objects;

public class TextFieldsEditorView extends LabeledEditorView {
    private int DEFAULT_SIM_NUMBER_LENGTH = 20;
    private String mAccountType;
    private CompanyListAdater mCompanyListAdater;
    private TextFieldsEditorViewUtils mCust = new TextFieldsEditorViewUtils(this.mContext);
    private RawContactEditorView mEditor;
    private EditText[] mFieldEditTexts = null;
    private ViewGroup mFields = null;
    protected boolean mHasShortAndLongForms;
    private boolean mHideOptional = true;
    protected IIccPhoneBookAdapter mIccPhoneBookAdapter;
    private boolean mIsSimAccount;
    private int mItemTextStartPadding;
    private ArrayList<EditText> mMultiLineEditTexts = new ArrayList();
    private EditText mNameFieldView = null;
    private String mOldData = null;
    private boolean mPhoneticNameExpandable = true;
    private PopupCompanyListTask mPopupCompanyListTask;
    private int[] mResult;
    protected int mSimMaxLimit;

    private class LoadADNRecordSize extends AsyncTask<Void, Void, Void> {
        private LoadADNRecordSize() {
        }

        protected Void doInBackground(Void... params) {
            IIccPhoneBookAdapter lIccPhoneBookAdapter;
            if (SimFactoryManager.isDualSim()) {
                int slotId = 0;
                if (TextFieldsEditorView.this.mAccountType != null) {
                    slotId = SimFactoryManager.getSlotIdBasedOnAccountType(TextFieldsEditorView.this.mAccountType);
                }
                HwLog.i("TextFieldsEditorView", "limit recored length : soltId == " + slotId);
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(slotId);
            } else {
                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
            }
            try {
                TextFieldsEditorView.this.mResult = lIccPhoneBookAdapter.getAdnRecordsSize();
                if (TextFieldsEditorView.this.mResult == null || TextFieldsEditorView.this.mResult.length <= 0) {
                    TextFieldsEditorView.this.mSimMaxLimit = 14;
                    return null;
                }
                TextFieldsEditorView.this.mSimMaxLimit = TextFieldsEditorView.this.mResult[0] - 14;
                if (TextFieldsEditorView.this.mSimMaxLimit <= 0) {
                    TextFieldsEditorView.this.mSimMaxLimit = 14;
                }
                return null;
            } catch (UnsupportedException e) {
                HwLog.e("TextFieldsEditorView", "getRecordsSize() is unsupported in initSimRecordsSize()");
            }
        }

        protected void onPostExecute(Void unused) {
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mHideOptional;
        public int[] mVisibilities;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.mVisibilities = new int[in.readInt()];
            in.readIntArray(this.mVisibilities);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mVisibilities.length);
            out.writeIntArray(this.mVisibilities);
        }
    }

    public TextFieldsEditorView(Context context) {
        super(context);
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFieldsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);
        this.mEditTextItemHeight = getContext().getResources().getDimensionPixelSize(R.dimen.contact_editor_item_height);
        this.mItemTextStartPadding = getContext().getResources().getDimensionPixelSize(R.dimen.contact_editor_item_start_padding);
        this.mFields = (ViewGroup) findViewById(R.id.editors);
    }

    private void expansion() {
        boolean z;
        View focusedChild = getFocusedChild();
        int focusedViewId = focusedChild == null ? -1 : focusedChild.getId();
        if (this.mHideOptional) {
            z = false;
        } else {
            z = true;
        }
        this.mHideOptional = z;
        if (EmuiFeatureManager.isChinaArea() && getKind().mimeType.contentEquals("#displayName")) {
            this.mHideOptional = true;
        }
        onOptionalFieldVisibilityChange();
        rebuildValues();
        View newFocusView = findViewById(focusedViewId);
        if (newFocusView == null || newFocusView.getVisibility() == 8) {
            newFocusView = this;
        }
        newFocusView.requestFocus();
    }

    public void editNewlyAddedField() {
        View editor = this.mFields.getChildAt(0);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null && (editor instanceof EditText) && !imm.showSoftInput(editor, 1)) {
            HwLog.w("TextFieldsEditorView", "Failed to show soft input method.");
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mFieldEditTexts != null) {
            for (EditText enabled2 : this.mFieldEditTexts) {
                enabled2.setEnabled(!isReadOnly() ? enabled : false);
            }
        }
    }

    public void hideExpansuinView() {
        this.mPhoneticNameExpandable = false;
    }

    public RawContactEditorView getRawContactEditorView() {
        return this.mEditor;
    }

    protected void requestFocusForFirstEditField() {
        if (this.mFieldEditTexts != null && this.mFieldEditTexts.length != 0) {
            EditText firstField = null;
            boolean anyFieldHasFocus = false;
            for (EditText editText : this.mFieldEditTexts) {
                if (firstField == null && editText.getVisibility() == 0) {
                    firstField = editText;
                }
                if (editText.hasFocus()) {
                    anyFieldHasFocus = true;
                    break;
                }
            }
            if (!anyFieldHasFocus && firstField != null) {
                firstField.requestFocus();
            }
        }
    }

    public EditText getFirstEditField() {
        if (!(this.mFieldEditTexts == null || this.mFieldEditTexts.length == 0)) {
            for (EditText lEditText : this.mFieldEditTexts) {
                if (lEditText.getVisibility() == 0) {
                    return lEditText;
                }
            }
        }
        return null;
    }

    public void setValues(DataKind kind, ValuesDelta entry, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        super.setValues(kind, entry, state, readOnly, vig);
        this.mFields.removeAllViews();
        boolean hidePossible = false;
        if (EmuiFeatureManager.isProductCustFeatureEnable() && this.mCust != null) {
            this.DEFAULT_SIM_NUMBER_LENGTH = this.mCust.getSimNumLen();
        }
        int fieldCount = kind.fieldList.size();
        this.mFieldEditTexts = new EditText[fieldCount];
        boolean lShowDeleteIcon = false;
        for (int index = 0; index < fieldCount; index++) {
            String value;
            int color;
            final EditField field = (EditField) kind.fieldList.get(index);
            final EditText fieldView = new EditText(getContext());
            if ("vnd.android.cursor.item/organization".equals(kind.mimeType) || (("#phoneticName".equals(kind.mimeType) && this.mPhoneticNameExpandable) || (!EmuiFeatureManager.isChinaArea() && "#displayName".equals(kind.mimeType)))) {
                final DataKind dataKind = kind;
                fieldView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if ("vnd.android.cursor.item/organization".equals(dataKind.mimeType) && event.getAction() == 0) {
                            TextFieldsEditorView.this.uploadBigData(dataKind.mimeType);
                        }
                        if (TextFieldsEditorView.this.mHideOptional) {
                            TextFieldsEditorView.this.expansion();
                        }
                        return false;
                    }
                });
            }
            if ("vnd.android.cursor.item/note".equals(kind.mimeType) || "vnd.android.cursor.item/phone_v2".equals(kind.mimeType) || (EmuiFeatureManager.isChinaArea() && "#displayName".equals(kind.mimeType))) {
                dataKind = kind;
                fieldView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == 0) {
                            TextFieldsEditorView.this.uploadBigData(dataKind.mimeType);
                        }
                        return false;
                    }
                });
            }
            LayoutParams layoutParams;
            if ("vnd.android.cursor.item/organization".equals(kind.mimeType) || "#displayName".equals(kind.mimeType) || "#phoneticName".equals(kind.mimeType)) {
                layoutParams = new LinearLayout.LayoutParams(-1, this.mEditTextItemHeight + 1);
                layoutParams.topMargin = this.mItemTextStartPadding;
            } else {
                layoutParams = new LinearLayout.LayoutParams(-1, this.mEditTextItemHeight);
            }
            fieldView.setLayoutParams(lp);
            if (field.isMultiLine()) {
                final LayoutParams layoutParams2 = lp;
                fieldView.addTextChangedListener(new TextWatcher() {
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void afterTextChanged(Editable s) {
                        int lineCount = fieldView.getLineCount();
                        if (lineCount != 0) {
                            if (lineCount > 1) {
                                layoutParams2.height = -2;
                            } else {
                                layoutParams2.height = TextFieldsEditorView.this.mEditTextItemHeight;
                            }
                            fieldView.setLayoutParams(layoutParams2);
                        }
                    }
                });
            }
            fieldView.setEllipsize(TruncateAt.END);
            if (field.minLines != 0) {
                fieldView.setMinLines(field.minLines);
            } else {
                fieldView.setMinHeight(this.mEditTextItemHeight);
            }
            fieldView.setTextAppearance(getContext(), 16973892);
            this.mFieldEditTexts[index] = fieldView;
            if (field.isMultiLine()) {
                this.mMultiLineEditTexts.add(fieldView);
            }
            fieldView.setId(vig.getId(state, kind, entry, index));
            if (field.titleRes > 0) {
                if ("vnd.android.cursor.item/phone_v2".equals(kind.mimeType)) {
                    fieldView.setHint(R.string.phoneLabelsGroupHint);
                } else {
                    fieldView.setHint(field.titleRes);
                }
            }
            fieldView.setTextSize(2, (float) getContext().getResources().getInteger(R.integer.contact_eidtor_item_common_text_size));
            fieldView.setTextColor(getContext().getResources().getColor(R.color.contact_eidtor_item_content_color));
            int inputType = field.inputType;
            fieldView.setInputType(inputType);
            if (inputType == 3) {
                PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getContext(), fieldView);
            }
            final String column = field.column;
            ArrayList<ValuesDelta> entryList = getValuesList();
            if (entryList.size() > 0) {
                value = ((ValuesDelta) entryList.get(0)).getAsString(column);
            } else {
                value = entry.getAsString(column);
            }
            final String newMimetype = kind.mimeType;
            final String extraMimetype = state.getExtraMimetype();
            boolean extraValue = state.getExtraBoolean();
            final String extraString = state.getExtraValue();
            if (newMimetype.equals(extraMimetype) && ((entry.isInsert() || entry.isUpdate()) && extraString != null && extraString.equals(value) && state.isFromCalllog())) {
                color = ImmersionUtils.getControlColor(getResources());
                if (color != 0) {
                    fieldView.setTextColor(color);
                } else {
                    fieldView.setTextColor(getResources().getColor(R.color.contact_highlight_color));
                }
            }
            if (EmuiFeatureManager.isChinaArea() && "vnd.android.cursor.item/phone_v2".equals(newMimetype) && !TextUtils.isEmpty(value)) {
                this.mOldData = value;
                value = PhoneNumberFormatter.parsePhoneNumber(value);
                if (!Objects.equals(value, this.mOldData)) {
                    onFieldChanged(column, value, fieldView);
                }
                value = ContactsUtils.getChinaFormatNumber(value);
            }
            fieldView.setText(value);
            onFieldChanged(column, value, fieldView);
            lShowDeleteIcon = lShowDeleteIcon || !TextUtils.isEmpty(value);
            if (this.mIsSimAccount) {
                if ("#displayName".equals(kind.mimeType)) {
                    if (SimFactoryManager.isDualSim()) {
                        int soltId = "com.android.huawei.sim".equals(state.getAccountType()) ? 0 : 1;
                        HwLog.i("TextFieldsEditorView", "limit recored length : soltId == " + soltId);
                        this.mIccPhoneBookAdapter = new IIccPhoneBookAdapter(soltId);
                    } else {
                        this.mIccPhoneBookAdapter = new IIccPhoneBookAdapter();
                    }
                    if (this.mResult == null || this.mResult.length <= 0) {
                        this.mSimMaxLimit = 14;
                    } else {
                        this.mSimMaxLimit = this.mResult[0] - 14;
                        if (this.mSimMaxLimit <= 0) {
                            this.mSimMaxLimit = 14;
                        }
                    }
                    this.mNameFieldView = fieldView;
                    this.mOldData = value;
                    int lEncodedLength = this.mIccPhoneBookAdapter.getAlphaEncodedLength(this.mOldData);
                    if (!(this.mOldData == null || lEncodedLength == -1 || lEncodedLength < this.mSimMaxLimit)) {
                        this.mHasShortAndLongForms = true;
                        setAlphaEncodeNameforSIM(this.mIccPhoneBookAdapter, this.mOldData);
                        onFieldChanged(column, this.mOldData, fieldView);
                    }
                } else if ("vnd.android.cursor.item/phone_v2".equals(kind.mimeType)) {
                    if (value != null && value.length() > this.DEFAULT_SIM_NUMBER_LENGTH) {
                        String newData = value.substring(0, this.DEFAULT_SIM_NUMBER_LENGTH);
                        fieldView.setText(newData);
                        onFieldChanged(column, newData, fieldView);
                    }
                    InputFilter[] mLengthFilter = new InputFilter[]{new LengthFilter(this.DEFAULT_SIM_NUMBER_LENGTH)};
                    if (this.mCust != null && this.mCust.showNumToast()) {
                        mLengthFilter[0] = this.mCust.getNewNumFilter(this.DEFAULT_SIM_NUMBER_LENGTH);
                    }
                    fieldView.setFilters(mLengthFilter);
                } else if ("vnd.android.cursor.item/email_v2".equals(newMimetype) && "vnd.android.cursor.item/email_v2".equals(extraMimetype) && !TextUtils.isEmpty(extraString)) {
                    fieldView.setText(extraString);
                    color = ImmersionUtils.getControlColor(getResources());
                    if (color != 0) {
                        fieldView.setTextColor(color);
                    } else {
                        fieldView.setTextColor(getResources().getColor(R.color.contact_highlight_color));
                    }
                    onFieldChanged(column, extraString, fieldView);
                }
            }
            if ("vnd.android.cursor.item/organization".equals(kind.mimeType) && "data1".equals(field.column)) {
                fieldView.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (TextFieldsEditorView.this.mEditor != null) {
                            TextFieldsEditorView.this.mEditor.findViewById(R.id.company_popup).setVisibility(8);
                            TextFieldsEditorView.this.cancelPopupCompanyListTask();
                        }
                    }
                });
            }
            final DataKind dataKind2 = kind;
            final RawContactDelta rawContactDelta = state;
            fieldView.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if ("vnd.android.cursor.item/organization".equals(dataKind2.mimeType) && "data1".equals(field.column)) {
                        if (fieldView.isFocused()) {
                            if (TextFieldsEditorView.this.mPopupCompanyListTask != null) {
                                TextFieldsEditorView.this.mPopupCompanyListTask.cancel(true);
                            }
                            if (TextFieldsEditorView.this.mEditor != null) {
                                if (TextFieldsEditorView.this.mCompanyListAdater == null) {
                                    TextFieldsEditorView.this.mCompanyListAdater = new CompanyListAdater(TextFieldsEditorView.this.getContext(), s.toString());
                                } else {
                                    TextFieldsEditorView.this.mCompanyListAdater.setInputString(s.toString());
                                }
                                if (TextFieldsEditorView.this.mEditor.getQueryCompanyInfoState()) {
                                    TextFieldsEditorView.this.mPopupCompanyListTask = new PopupCompanyListTask(TextFieldsEditorView.this.getContext(), s.toString(), TextFieldsEditorView.this.mEditor, fieldView, TextFieldsEditorView.this.mCompanyListAdater);
                                    TextFieldsEditorView.this.mPopupCompanyListTask.executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, (Void[]) null);
                                } else {
                                    TextFieldsEditorView.this.mEditor.setQueryCompanyInfoState(true);
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    if (!(!newMimetype.equals(extraMimetype) || extraString == null || extraString.equals(s.toString()))) {
                        fieldView.setTextColor(TextFieldsEditorView.this.getContext().getResources().getColor(R.color.contact_eidtor_item_content_color));
                        rawContactDelta.setHasExtra(dataKind2.mimeType, false);
                    }
                    String currentData = s.toString();
                    if (TextFieldsEditorView.this.mIsSimAccount && "#displayName".equals(dataKind2.mimeType) && !TextUtils.isEmpty(TextFieldsEditorView.this.mOldData) && currentData.length() > TextFieldsEditorView.this.mOldData.length()) {
                        currentData = TextFieldsEditorView.this.mOldData;
                        fieldView.setText(TextFieldsEditorView.this.mOldData);
                        fieldView.setSelection(TextFieldsEditorView.this.mOldData.length());
                    }
                    TextFieldsEditorView.this.onFieldChanged(column, currentData, fieldView);
                    if (TextFieldsEditorView.this.mIsSimAccount && "vnd.android.cursor.item/phone_v2".equals(dataKind2.mimeType)) {
                        TextFieldsEditorView.this.fillNormalizedNumber(currentData, TextFieldsEditorView.this.getEntry());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextFieldsEditorView.this.mIsSimAccount && "#displayName".equals(dataKind2.mimeType)) {
                        if (!TextUtils.isEmpty(s)) {
                            IIccPhoneBookAdapter lIccPhoneBookAdapter;
                            String currentData = s.toString();
                            if (!TextUtils.isEmpty(TextFieldsEditorView.this.mOldData) && currentData.length() < TextFieldsEditorView.this.mOldData.length()) {
                                TextFieldsEditorView.this.mNameFieldView.setFilters(new InputFilter[]{new LengthFilter(TextFieldsEditorView.this.mSimMaxLimit)});
                            }
                            TextFieldsEditorView.this.mOldData = s.toString();
                            if (SimFactoryManager.isDualSim()) {
                                int soltId = "com.android.huawei.sim".equals(rawContactDelta.getAccountType()) ? 0 : 1;
                                HwLog.i("TextFieldsEditorView", "limit recored length : soltId == " + soltId);
                                lIccPhoneBookAdapter = new IIccPhoneBookAdapter(soltId);
                            } else {
                                lIccPhoneBookAdapter = new IIccPhoneBookAdapter();
                            }
                            int lEncodedLength = lIccPhoneBookAdapter.getAlphaEncodedLength(TextFieldsEditorView.this.mOldData);
                            if (lEncodedLength != -1) {
                                if (lEncodedLength >= TextFieldsEditorView.this.mSimMaxLimit) {
                                    TextFieldsEditorView.this.mNameFieldView.setFilters(new InputFilter[]{new LengthFilter(TextFieldsEditorView.this.mOldData.length())});
                                    if (TextFieldsEditorView.this.mCust != null) {
                                        TextFieldsEditorView.this.mCust.remindNameToast();
                                    }
                                }
                                if (lEncodedLength > TextFieldsEditorView.this.mSimMaxLimit) {
                                    TextFieldsEditorView.this.setAlphaEncodeNameforSIM(lIccPhoneBookAdapter, TextFieldsEditorView.this.mOldData);
                                }
                            }
                        }
                    } else if ("com.android.huawei.phone".equals(rawContactDelta.getAccountType())) {
                        InputFilter[] lengthFilter = new InputFilter[1];
                        if ("vnd.android.cursor.item/postal-address_v2".equals(dataKind2.mimeType) || "vnd.android.cursor.item/note".equals(dataKind2.mimeType)) {
                            lengthFilter[0] = new LengthFilter(Place.TYPE_SUBLOCALITY_LEVEL_1);
                            fieldView.setFilters(lengthFilter);
                        } else {
                            lengthFilter[0] = new LengthFilter(255);
                            fieldView.setFilters(lengthFilter);
                        }
                    }
                }
            });
            boolean z = isEnabled() && !readOnly;
            fieldView.setEnabled(z);
            int i;
            if (field.shortForm) {
                hidePossible = true;
                this.mHasShortAndLongForms = true;
                if (this.mHideOptional) {
                    i = 0;
                } else {
                    i = 8;
                }
                fieldView.setVisibility(i);
            } else if (field.longForm) {
                hidePossible = true;
                this.mHasShortAndLongForms = true;
                if (field.column.equals("data4")) {
                    if (entry.getAsString(field.column) != null) {
                        if (!entry.getAsString(field.column).isEmpty() && "vnd.android.cursor.item/organization".equals(kind.mimeType)) {
                            fieldView.setVisibility(0);
                            this.mHideOptional = false;
                        }
                    }
                }
                if (this.mHideOptional) {
                    i = 8;
                } else {
                    i = 0;
                }
                fieldView.setVisibility(i);
            } else {
                boolean z2 = !ContactsUtils.isGraphic(value) ? field.optional : false;
                fieldView.setVisibility(this.mHideOptional ? z2 : false ? 8 : 0);
                hidePossible = !hidePossible ? z2 : true;
            }
            if (isDataKindEditName(kind)) {
                fieldView.setBackground(getContext().getResources().getDrawable(R.drawable.contact_edit_text_background));
            } else {
                fieldView.setBackground(null);
            }
            if (field.isMultiLine() && fieldView.getVisibility() == 0) {
                int editTextWidth = TextUtil.getTextWidth(value, fieldView.getTextSize());
                if ("vnd.android.cursor.item/postal-address_v2".equals(kind.mimeType)) {
                    if (editTextWidth > this.mEditorItemTotalWidth || fieldView.getText().toString().contains("\n")) {
                        lp.height = -2;
                    } else {
                        lp.height = this.mEditTextItemHeight;
                    }
                } else if (editTextWidth >= getResources().getDimensionPixelSize(R.dimen.editor_item_width_without_spiner) || fieldView.getText().toString().contains("\n")) {
                    lp.height = -2;
                } else {
                    lp.height = this.mEditTextItemHeight;
                }
                fieldView.setLayoutParams(lp);
            }
            fieldView.setPaddingRelative(0, 0, 0, 0);
            this.mFields.addView(fieldView);
        }
        setDeleteButtonVisible(lShowDeleteIcon);
    }

    private void uploadBigData(String mimeType) {
        if ("vnd.android.cursor.item/organization".equals(mimeType)) {
            StatisticalHelper.report(2031);
        } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
            StatisticalHelper.report(2032);
        } else if ("#displayName".equals(mimeType)) {
            StatisticalHelper.report(2030);
        } else if ("vnd.android.cursor.item/note".equals(mimeType)) {
            StatisticalHelper.report(2033);
        }
    }

    private void setAlphaEncodeNameforSIM(IIccPhoneBookAdapter lIccPhoneBookAdapter, String oldData) {
        if (!TextUtils.isEmpty(oldData)) {
            int maxNameLength = this.mSimMaxLimit;
            if (oldData.length() < maxNameLength) {
                maxNameLength = oldData.length();
            }
            while (maxNameLength > 0) {
                String newString = oldData.substring(0, maxNameLength);
                if (lIccPhoneBookAdapter.getAlphaEncodedLength(newString) <= this.mSimMaxLimit) {
                    this.mOldData = newString;
                    this.mNameFieldView.setText(this.mOldData);
                    this.mNameFieldView.setSelection(this.mOldData.length());
                    this.mNameFieldView.setFilters(new InputFilter[]{new LengthFilter(this.mOldData.length())});
                    break;
                }
                maxNameLength--;
            }
        }
    }

    public boolean isEmpty() {
        int i = 0;
        while (i < this.mFields.getChildCount()) {
            if ((this.mFields.getChildAt(i) instanceof EditText) && !TextUtils.isEmpty(((EditText) this.mFields.getChildAt(i)).getText())) {
                return false;
            }
            i++;
        }
        return true;
    }

    public boolean areOptionalFieldsVisible() {
        return !this.mHideOptional;
    }

    public boolean hasShortAndLongForms() {
        return this.mHasShortAndLongForms;
    }

    protected Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mHideOptional = this.mHideOptional;
        int numChildren = this.mFieldEditTexts == null ? 0 : this.mFieldEditTexts.length;
        ss.mVisibilities = new int[numChildren];
        for (int i = 0; i < numChildren; i++) {
            ss.mVisibilities[i] = this.mFieldEditTexts[i].getVisibility();
        }
        return ss;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mHideOptional = ss.mHideOptional;
        if (this.mFieldEditTexts != null) {
            int numChildren = Math.min(this.mFieldEditTexts.length, ss.mVisibilities.length);
            for (int i = 0; i < numChildren; i++) {
                this.mFieldEditTexts[i].setVisibility(ss.mVisibilities[i]);
            }
        }
    }

    public void clearAllFields() {
        if (this.mFieldEditTexts != null) {
            for (EditText fieldEditText : this.mFieldEditTexts) {
                fieldEditText.setText("");
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (int i = 0; i < this.mMultiLineEditTexts.size(); i++) {
            EditText editText = (EditText) this.mMultiLineEditTexts.get(i);
            LayoutParams lp = editText.getLayoutParams();
            int editTextWidth = TextUtil.getTextWidth(editText.getText().toString(), editText.getTextSize());
            if ("vnd.android.cursor.item/postal-address_v2".equals(this.mKind.mimeType)) {
                if (editTextWidth >= this.mEditorItemTotalWidth || editText.getText().toString().contains("\n")) {
                    lp.height = -2;
                } else {
                    lp.height = this.mEditTextItemHeight;
                }
            } else if (editTextWidth < getResources().getDimensionPixelSize(R.dimen.editor_item_width_without_spiner)) {
                lp.height = this.mEditTextItemHeight;
            } else {
                lp.height = -2;
            }
            editText.setLayoutParams(lp);
        }
    }

    private boolean isDataKindEditName(DataKind kind) {
        if ("#displayName".equals(kind.mimeType) || "#phoneticName".equals(kind.mimeType) || "vnd.android.cursor.item/organization".equals(kind.mimeType)) {
            return true;
        }
        return false;
    }

    public void setRawContactEditorView(RawContactEditorView editor) {
        this.mEditor = editor;
    }

    public boolean isAllVisibleEditTextEmpty() {
        for (int i = 0; i < this.mFields.getChildCount(); i++) {
            if (this.mFields.getChildAt(i) instanceof EditText) {
                EditText editText = (EditText) this.mFields.getChildAt(i);
                if (editText.getVisibility() == 0 && !TextUtils.isEmpty(editText.getText())) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setAccountType(String accountType) {
        this.mAccountType = accountType;
    }

    public void setIsSimAccount(boolean isSimAccount) {
        this.mIsSimAccount = isSimAccount;
        if (isSimAccount) {
            int slotId = 0;
            if (this.mAccountType != null) {
                slotId = SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType);
            }
            int[] result = IIccPhoneBookAdapter.getCachedAdnRecordsSize(slotId);
            if (result == null || result.length <= 0) {
                new LoadADNRecordSize().executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
            } else {
                this.mResult = result;
            }
        }
    }

    protected String getSIMCountryISO() {
        int slotId = -1;
        if (this.mAccountType != null) {
            slotId = SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType);
        }
        HwLog.i("TextFieldsEditorView", "limit recored length : soltId == " + slotId);
        if (slotId == -1) {
            return null;
        }
        return CommonUtilMethods.getSIMCountryIso(slotId);
    }

    private void fillNormalizedNumber(String currentData, ValuesDelta values) {
        if (!TextUtils.isEmpty(currentData) && values != null) {
            String countryISO = getSIMCountryISO();
            String newSIMNumberE164 = null;
            if (!TextUtils.isEmpty(countryISO)) {
                newSIMNumberE164 = PhoneNumberUtils.formatNumberToE164(currentData, countryISO);
            }
            if (TextUtils.isEmpty(newSIMNumberE164)) {
                values.put("data4", "SIM_DATA");
            } else {
                values.put("data4", newSIMNumberE164);
            }
        }
    }

    public void cancelPopupCompanyListTask() {
        if (this.mPopupCompanyListTask != null) {
            this.mPopupCompanyListTask.cancel(true);
            this.mPopupCompanyListTask.closeCursor();
        }
        if (this.mEditor != null) {
            this.mEditor.setPopupCompanyShowState(false);
        }
    }
}
