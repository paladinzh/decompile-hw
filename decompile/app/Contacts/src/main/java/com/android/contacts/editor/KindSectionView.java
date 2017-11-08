package com.android.contacts.editor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.editor.RingtoneEditorView;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LunarUtils;
import com.android.contacts.widget.AbstractExpandableViewAdapter.ExpandCollapseAnimation;
import com.google.android.gms.R;
import com.google.common.collect.Maps;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KindSectionView extends LinearLayout implements EditorListener, ContactEditorInfo {
    private int animationDuration;
    private ImageView filedTpype;
    private String mAccountType;
    private ImageButton mAddItemButton;
    private TextView mAddItemName;
    private ViewGroup mAddItems;
    private ViewGroup mEditors;
    HwCustContactEditorCustomization mHwCustContactEditorCustomizationObj;
    private LayoutInflater mInflater;
    private boolean mIsSimAccount;
    private TextView mItemName;
    private DataKind mKind;
    private boolean mReadOnly;
    private RingtoneEditorView mRingToneView;
    private final ArrayList<Runnable> mRunWhenWindowFocused;
    private RawContactDelta mState;
    private RawContactDeltaList mStateList;
    private String mTitleString;
    private ViewIdGenerator mViewIdGenerator;

    public KindSectionView(Context context) {
        this(context, null);
    }

    public KindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRunWhenWindowFocused = new ArrayList(1);
        this.mHwCustContactEditorCustomizationObj = null;
        this.animationDuration = 280;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCustContactEditorCustomizationObj = (HwCustContactEditorCustomization) HwCustUtils.createObj(HwCustContactEditorCustomization.class, new Object[0]);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mEditors != null) {
            int childCount = this.mEditors.getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mEditors.getChildAt(i).setEnabled(enabled);
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);
        this.mInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mEditors = (ViewGroup) findViewById(R.id.kind_editors);
        this.mAddItems = (ViewGroup) findViewById(R.id.kind_add_items);
        this.mAddItemName = (TextView) findViewById(R.id.add_item_name);
        this.mAddItemButton = (ImageButton) this.mAddItems.findViewById(R.id.add_item_button);
        int color = ImmersionUtils.getControlColor(getContext().getResources());
        if (color != 0) {
            Drawable drawable = this.mAddItemButton.getDrawable();
            drawable.setTint(color);
            this.mAddItemButton.setBackground(drawable);
        }
        this.mAddItemButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                    KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                }
                KindSectionView.this.addItem(true, true);
            }
        });
        this.filedTpype = (ImageView) findViewById(R.id.fields_image_type);
    }

    public void onDeleteRequested(Editor editor) {
        HwLog.i("KindSectionView", "onDeleteRequested");
        if (getEditorCount() == 1) {
            editor.clearAllFields();
            editor.setDeletable(true);
            return;
        }
        setAddItemNameAndVisibility(false, true);
        editor.deleteEditor();
    }

    public void onRequest(int request) {
        if (request == 3 || request == 4) {
            updateAddFooterVisible(true);
        }
        if (request != 2) {
            return;
        }
        if (this.mAddItems != null && this.mAddItems.getVisibility() == 0) {
            return;
        }
        if (this.mKind.typeOverallMax <= 0 || this.mKind.typeOverallMax > getEditorCount()) {
            setAddItemNameAndVisibility(true, false);
        }
    }

    public void setState(DataKind kind, RawContactDeltaList stateList, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        String str;
        this.mKind = kind;
        this.mStateList = stateList;
        this.mState = state;
        this.mReadOnly = readOnly;
        this.mViewIdGenerator = vig;
        setId(this.mViewIdGenerator.getId(state, kind, null, -1));
        if (kind.titleRes == -1 || kind.titleRes == 0) {
            str = "";
        } else {
            str = getResources().getString(kind.titleRes);
        }
        this.mTitleString = str;
        if (stateList == null || stateList.size() <= 1) {
            rebuildFromState();
        } else {
            rebuildFromStateList();
        }
        updateAddFooterVisible(false);
        updateSectionVisible();
    }

    public String getTitle() {
        return this.mTitleString;
    }

    private void setAddItemNameAndVisibility(boolean needAnimation, boolean isDelete) {
        if (this.mKind != null) {
            if ("vnd.android.cursor.item/phone_v2".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_phone);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else if ("vnd.android.cursor.item/email_v2".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_email);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else if ("vnd.android.cursor.item/postal-address_v2".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_postaddress);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else if ("vnd.android.cursor.item/website".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_website);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else if ("vnd.android.cursor.item/im".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_im);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else if ("vnd.android.cursor.item/relation".equals(this.mKind.mimeType)) {
                this.mAddItemName.setText(R.string.contact_add_relationship);
                if (needAnimation) {
                    animateView(this.mAddItems, 0);
                }
                this.mAddItems.setVisibility(0);
                this.mAddItems.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (KindSectionView.this.mKind.mimeType.equals(KindSectionView.this.mState.getExtraMimetype())) {
                            KindSectionView.this.mState.setHasExtra(KindSectionView.this.mKind.mimeType, false);
                        }
                        KindSectionView.this.addItem(true, true);
                    }
                });
            } else {
                this.mAddItems.setVisibility(8);
            }
            int currentEidtorCount = getEditorCount();
            if (isDelete) {
                currentEidtorCount--;
            }
            if (this.mKind.typeOverallMax > 0 && this.mKind.typeOverallMax <= currentEidtorCount) {
                this.mAddItems.setVisibility(8);
            }
        }
    }

    private void animateView(View target, int type) {
        Animation anim = new ExpandCollapseAnimation(target, type);
        anim.setDuration((long) this.animationDuration);
        target.startAnimation(anim);
    }

    public void rebuildFromState() {
        this.mEditors.removeAllViews();
        String aAccountType = this.mState.getAccountType();
        if (this.mState.hasMimeEntries(this.mKind.mimeType)) {
            ArrayList<ValuesDelta> mimeEntries = this.mState.getMimeEntries(this.mKind.mimeType);
            if (mimeEntries != null) {
                for (ValuesDelta entry : mimeEntries) {
                    if (!(!entry.isVisible() || isEmptyNoop(entry) || refuseLunar(this.mKind, entry, aAccountType))) {
                        if (entry.getAsString(((EditField) this.mKind.fieldList.get(0)).column) != null) {
                            setAddItemNameAndVisibility(false, false);
                        }
                        createEditorView(entry);
                    }
                }
            }
        }
    }

    private boolean refuseLunar(DataKind kind, ValuesDelta entry, String accountType) {
        if ("vnd.android.cursor.item/contact_event".equals(this.mKind.mimeType)) {
            boolean isLunarBirthday = false;
            if (RawContactModifier.hasEditTypes(kind) && entry.containsKey(kind.typeColumn)) {
                Long rawValue = entry.getAsLong(kind.typeColumn);
                if (rawValue != null && rawValue.longValue() == 4) {
                    isLunarBirthday = true;
                }
            }
            if (LunarUtils.checkTimeValidity(false, entry.getAsString(((EditField) kind.fieldList.get(0)).column))) {
                return !LunarUtils.supportLunarAccount(accountType, getContext()) && isLunarBirthday;
            } else {
                return true;
            }
        }
    }

    private View createEditorView(ValuesDelta entry) {
        int layoutResId = EditorUiUtils.getLayoutResourceId(this.mKind.mimeType);
        try {
            View view = this.mInflater.inflate(layoutResId, this.mEditors, false);
            view.setEnabled(isEnabled());
            if (this.mKind.mimeType.equals("vnd.android.huawei.cursor.item/ringtone")) {
                RingtoneEditorView editor = (RingtoneEditorView) view;
                editor.setValues(this.mKind, entry, this.mStateList, this.mState, this.mReadOnly, this.mViewIdGenerator);
                setRingtoneView(editor);
            } else if (view instanceof Editor) {
                Editor editor2 = (Editor) view;
                boolean deletable = true;
                if ((view instanceof TextFieldsEditorView) && this.mIsSimAccount && ("vnd.android.cursor.item/phone_v2".equals(this.mKind.mimeType) || "vnd.android.cursor.item/email_v2".equals(this.mKind.mimeType))) {
                    TextFieldsEditorView textView = (TextFieldsEditorView) view;
                    textView.setAccountType(this.mAccountType);
                    textView.setIsSimAccount(true);
                }
                editor2.setValues(this.mKind, entry, this.mState, this.mReadOnly, this.mViewIdGenerator);
                if ("vnd.android.cursor.item/note".equals(this.mKind.mimeType) || "vnd.android.cursor.item/nickname".equals(this.mKind.mimeType)) {
                    deletable = false;
                }
                editor2.setDeletable(deletable);
                editor2.setEditorListener(this);
                if (view instanceof TextFieldsEditorView) {
                    LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.mainLayout);
                    if (mainLayout != null) {
                        this.mItemName = (TextView) mainLayout.findViewById(R.id.item_title);
                        if (!(this.mItemName == null || this.mKind == null || (!"vnd.android.cursor.item/website".equals(this.mKind.mimeType) && !"vnd.android.cursor.item/note".equals(this.mKind.mimeType) && !"vnd.android.cursor.item/sip_address".equals(this.mKind.mimeType) && !"vnd.android.cursor.item/nickname".equals(this.mKind.mimeType)))) {
                            this.mItemName.setVisibility(0);
                            this.mItemName.setText(this.mTitleString);
                        }
                    }
                }
            }
            if (this.mHwCustContactEditorCustomizationObj != null) {
                this.mHwCustContactEditorCustomizationObj.handleEditorCustomization(this.mKind, entry, this.mState, this.mReadOnly, this.mViewIdGenerator, view);
            }
            this.mEditors.addView(view);
            if ((this.mKind != null && this.mKind.typeOverallMax > 0 && this.mKind.typeOverallMax == getEditorCount()) || !RawContactModifier.canInsert(this.mState, this.mKind)) {
                this.mAddItems.setVisibility(8);
            }
            if (this.filedTpype != null) {
                this.filedTpype.setImageDrawable(getLableTypeDrawable());
                if (this.mIsSimAccount) {
                    LayoutParams layoutParams = (LayoutParams) this.filedTpype.getLayoutParams();
                    layoutParams.setMarginStart(0);
                    this.filedTpype.setLayoutParams(layoutParams);
                }
            }
            return view;
        } catch (Exception e) {
            throw new RuntimeException("Cannot allocate editor with layout resource ID " + layoutResId + " for MIME type " + this.mKind.mimeType + " with error " + e.toString());
        }
    }

    public void setRingtoneView(RingtoneEditorView aView) {
        this.mRingToneView = aView;
    }

    public RingtoneEditorView getRingtoneView() {
        return this.mRingToneView;
    }

    private boolean isEmptyNoop(ValuesDelta item) {
        if (!item.isNoop()) {
            return false;
        }
        int fieldCount = this.mKind.fieldList.size();
        for (int i = 0; i < fieldCount; i++) {
            if (!TextUtils.isEmpty(item.getAsString(((EditField) this.mKind.fieldList.get(i)).column))) {
                return false;
            }
        }
        return true;
    }

    private void updateSectionVisible() {
        int i = 0;
        if (getEditorCount() == 0) {
            i = 8;
        }
        setVisibility(i);
    }

    protected void updateAddFooterVisible(boolean animate) {
        if (!(this.mReadOnly || this.mKind.typeOverallMax == 1)) {
            updateEmptyEditors();
            if (!hasEmptyEditor() && RawContactModifier.canInsert(this.mState, this.mKind) && animate && this.mKind.mimeType.equals(this.mState.getExtraMimetype())) {
                this.mState.setHasExtra(this.mKind.mimeType, false);
            }
        }
    }

    private void updateEmptyEditors() {
        List<View> emptyEditors = getEmptyEditors();
        if (emptyEditors.size() > 1) {
            for (View emptyEditorView : emptyEditors) {
                if (emptyEditorView.findFocus() == null && (emptyEditorView instanceof Editor)) {
                    ((Editor) emptyEditorView).deleteEditor();
                    setAddItemNameAndVisibility(false, true);
                }
            }
        }
        removeEmptyEventEditors();
    }

    private List<View> getEmptyEditors() {
        List<View> emptyEditorViews = new ArrayList();
        for (int i = 0; i < this.mEditors.getChildCount(); i++) {
            View view = this.mEditors.getChildAt(i);
            if (((Editor) view).isEmpty()) {
                emptyEditorViews.add(view);
            }
        }
        return emptyEditorViews;
    }

    private boolean hasEmptyEditor() {
        return getEmptyEditors().size() > 0;
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.mEditors.getChildCount(); i++) {
            if (!((Editor) this.mEditors.getChildAt(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            for (Runnable r : this.mRunWhenWindowFocused) {
                r.run();
            }
            this.mRunWhenWindowFocused.clear();
        }
    }

    private void runWhenWindowFocused(Runnable r) {
        if (hasWindowFocus()) {
            r.run();
        } else {
            this.mRunWhenWindowFocused.add(r);
        }
    }

    private void postWhenWindowFocused(final Runnable r) {
        post(new Runnable() {
            public void run() {
                KindSectionView.this.runWhenWindowFocused(r);
            }
        });
    }

    public void addItem(final boolean aRequestFocus, boolean animat) {
        ValuesDelta valuesDelta = null;
        if (this.mKind.typeOverallMax == 1) {
            if (getEditorCount() != 1) {
                ArrayList<ValuesDelta> entries = this.mState.getMimeEntries(this.mKind.mimeType);
                if (entries != null && entries.size() > 0) {
                    valuesDelta = (ValuesDelta) entries.get(0);
                }
            } else {
                return;
            }
        }
        if (valuesDelta == null) {
            if (this.mStateList.size() > 1) {
                valuesDelta = RawContactModifier.insertChild(this.mStateList, this.mKind, AccountTypeManager.getInstance(getContext()));
            } else {
                valuesDelta = RawContactModifier.insertChild(this.mState, this.mKind);
            }
        }
        final View newField = createEditorView(valuesDelta);
        if (animat) {
            animateView(newField, 0);
        }
        if (newField instanceof Editor) {
            postWhenWindowFocused(new Runnable() {
                public void run() {
                    DataKind lKind = KindSectionView.this.getKind();
                    if (aRequestFocus) {
                        newField.requestFocus();
                    }
                    if (!(newField instanceof EventFieldEditorView) && KindSectionView.this.mEditors.getChildCount() < 2) {
                        ((Editor) newField).editNewlyAddedField();
                    } else if ((newField instanceof EventFieldEditorView) && lKind != null && lKind.typeToSelect != -1 && lKind.shouldShowDatePicker) {
                        ((Editor) newField).editNewlyAddedField();
                    }
                }
            });
        }
        updateSectionVisible();
    }

    public int getEditorCount() {
        return this.mEditors.getChildCount();
    }

    public DataKind getKind() {
        return this.mKind;
    }

    public boolean isBirthdayPresent(int rawValue) {
        if (this.mEditors != null) {
            int i = 0;
            while (i < this.mEditors.getChildCount()) {
                if ((this.mEditors.getChildAt(i) instanceof EventFieldEditorView) && ((EventFieldEditorView) this.mEditors.getChildAt(i)).getType().rawValue == rawValue) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    public void setIsSimAccount(boolean isSimAccount) {
        this.mIsSimAccount = isSimAccount;
    }

    public void setAccountType(String accountType) {
        this.mAccountType = accountType;
    }

    public ViewGroup getEditor() {
        return this.mEditors;
    }

    public void rebuildFromStateList() {
        this.mEditors.removeAllViews();
        ArrayList<ValuesDelta> commonEntries = new ArrayList();
        HashMap<ValuesDelta, View> entryViewMap = Maps.newHashMap();
        for (int index = 0; index < this.mStateList.size(); index++) {
            RawContactDelta state = (RawContactDelta) this.mStateList.get(index);
            String aAccountType = state.getAccountType();
            if (state.hasMimeEntries(this.mKind.mimeType)) {
                ArrayList<ValuesDelta> mimeEntries = state.getMimeEntries(this.mKind.mimeType);
                if (mimeEntries != null) {
                    for (ValuesDelta entry : mimeEntries) {
                        if (!(!entry.isVisible() || isEmptyNoop(entry) || refuseLunar(this.mKind, entry, aAccountType))) {
                            if (entry.getAsString(((EditField) this.mKind.fieldList.get(0)).column) != null) {
                                setAddItemNameAndVisibility(false, false);
                            }
                            View resultView;
                            if (isEntryContained(entry, commonEntries)) {
                                ValuesDelta sameEntry = getFromCommonEntry(entry, commonEntries);
                                if (sameEntry != null) {
                                    resultView = (View) entryViewMap.get(sameEntry);
                                    if (resultView instanceof LabeledEditorView) {
                                        ((LabeledEditorView) resultView).addEntry(entry);
                                    }
                                }
                            } else {
                                commonEntries.add(entry);
                                resultView = createEditorView(entry);
                                entryViewMap.put(entry, resultView);
                                if (resultView instanceof LabeledEditorView) {
                                    ((LabeledEditorView) resultView).addEntry(entry);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isEntryContained(ValuesDelta destEntry, ArrayList<ValuesDelta> commonEntries) {
        if (commonEntries == null || commonEntries.size() == 0) {
            return false;
        }
        for (ValuesDelta entry : commonEntries) {
            if (entry.isValueEqual(destEntry)) {
                return true;
            }
        }
        return false;
    }

    private ValuesDelta getFromCommonEntry(ValuesDelta destEntry, ArrayList<ValuesDelta> commonEntries) {
        if (commonEntries == null || commonEntries.size() == 0) {
            return null;
        }
        for (ValuesDelta entry : commonEntries) {
            if (entry.isValueEqual(destEntry)) {
                return entry;
            }
        }
        return null;
    }

    private void removeEmptyEventEditors() {
        if (this.mState != null && this.mKind != null && "vnd.android.cursor.item/contact_event".equals(this.mKind.mimeType)) {
            ValuesDelta entry;
            List<ValuesDelta> emptyEntries = new ArrayList();
            if (this.mEditors != null && this.mEditors.getChildCount() > 0) {
                for (int i = 0; i < this.mEditors.getChildCount(); i++) {
                    if (this.mEditors.getChildAt(i) instanceof EventFieldEditorView) {
                        entry = ((EventFieldEditorView) this.mEditors.getChildAt(i)).getEntry();
                        if (TextUtils.isEmpty(entry.getAsString("data1"))) {
                            emptyEntries.add(entry);
                        }
                    }
                }
            }
            List<ValuesDelta> eventEntries = this.mState.getMimeEntries("vnd.android.cursor.item/contact_event");
            if (eventEntries != null && eventEntries.size() > 0) {
                List<ValuesDelta> deleteEntries = new ArrayList();
                for (ValuesDelta entry2 : eventEntries) {
                    if (TextUtils.isEmpty(entry2.getAsString("data1")) && !emptyEntries.contains(entry2)) {
                        deleteEntries.add(entry2);
                    }
                }
                if (deleteEntries.size() > 0) {
                    for (ValuesDelta entry22 : deleteEntries) {
                        eventEntries.remove(entry22);
                    }
                }
            }
        }
    }

    private Drawable getLableTypeDrawable() {
        if (this.mKind == null) {
            return null;
        }
        Drawable drawable;
        String str = this.mKind.mimeType;
        if (str.equals("vnd.android.cursor.item/phone_v2")) {
            drawable = getResources().getDrawable(R.drawable.contact_ic_phone);
        } else if (str.equals("vnd.android.cursor.item/email_v2")) {
            drawable = getResources().getDrawable(R.drawable.contact_icon_mail);
        } else if (str.equals("vnd.android.cursor.item/note")) {
            drawable = getResources().getDrawable(R.drawable.contact_icon_note);
        } else if (str.equals("vnd.android.cursor.item/im")) {
            drawable = getResources().getDrawable(R.drawable.contacts_icon_aim);
        } else if (str.equals("vnd.android.cursor.item/postal-address_v2")) {
            drawable = getResources().getDrawable(R.drawable.contacts_icon_address);
        } else if (str.equals("vnd.android.cursor.item/website")) {
            drawable = getResources().getDrawable(R.drawable.contacts_edit_icon_website);
        } else if (str.equals("vnd.android.cursor.item/relation")) {
            drawable = getResources().getDrawable(R.drawable.contacts_edit_icon_relation);
        } else if (str.equals("vnd.android.cursor.item/contact_event")) {
            drawable = getResources().getDrawable(R.drawable.contacts_edit_icon_date);
        } else if (str.equals("vnd.android.cursor.item/group_membership")) {
            drawable = getResources().getDrawable(R.drawable.contacts_edit_icon_group);
        } else {
            drawable = getResources().getDrawable(R.drawable.contacts_edit_icon_nickname);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }
}
