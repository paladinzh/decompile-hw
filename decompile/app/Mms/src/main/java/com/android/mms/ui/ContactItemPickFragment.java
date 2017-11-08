package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.util.VcardMessageHelper;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.AvatarCache;
import java.util.List;

public class ContactItemPickFragment extends HwBaseFragment implements OnClickListener {
    EmuiActionBar mActionBar;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295) {
                ContactItemPickFragment.this.cancelSave();
            } else if (viewId == 16908296) {
                ContactItemPickFragment.this.saveSelectItem();
            }
        }
    };
    private View mConvertView;
    private LayoutInflater mInflater;
    private LinearLayout mItemsLayout;
    private TextView mNameText;
    private ImageView mPhotoImage;
    private VcardMessageHelper mVCardMessageHelper;
    private VcardModel mVCardModel;
    private List<VCardDetailNode> mVcardDetailList = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_item_pick, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        initViews();
        getContactData(getIntent());
        if (this.mVCardModel == null) {
            cancelSave();
        }
        initItemView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        cancelSave();
        return true;
    }

    private void initViews() {
        String title = getString(R.string.select_items);
        this.mActionBar = new EmuiActionBar(getActivity());
        this.mActionBar.setTitle(title);
        this.mActionBar.setStartIcon(true, this.mActionBarListener);
        this.mActionBar.setEndIcon(true, this.mActionBarListener);
        this.mPhotoImage = (ImageView) getView().findViewById(R.id.photo_image);
        this.mNameText = (TextView) getView().findViewById(R.id.name_text);
        this.mNameText.setVisibility(0);
        this.mItemsLayout = (LinearLayout) getView().findViewById(R.id.items_layout);
    }

    private void getContactData(Intent data) {
        this.mVCardModel = null;
        if (data != null && data.getData() != null) {
            try {
                this.mVCardModel = new VcardModel(getContext(), "text/x-vCard", data.getData());
                this.mVCardMessageHelper = new VcardMessageHelper(getContext());
                this.mVcardDetailList = this.mVCardModel.getVcardSelectedDetailList();
                this.mNameText.setText(this.mVCardModel.getName());
                if (this.mVCardModel.isUseDefaultImg() || this.mVCardModel.getBitmap() == null) {
                    this.mPhotoImage.setImageResource(R.drawable.csp_default_avatar);
                } else {
                    this.mPhotoImage.setImageBitmap(AvatarCache.createRoundPhoto(this.mVCardModel.getBitmap()));
                }
            } catch (Exception e) {
                MLog.e("ContactItemPickFragment", MLog.getStackTraceString(e));
                this.mVCardModel = null;
            }
        }
    }

    private void initItemView() {
        if (this.mVcardDetailList == null || this.mInflater == null) {
            MLog.v("ContactItemPickFragment", "initItemView:: the mVcardDetailList is null");
            return;
        }
        String mPropName = null;
        String previousName = null;
        String previousValue = null;
        int paddingLeft = (int) getResources().getDimension(R.dimen.mms_contact_pick_item_padding_left);
        int paddingRight = (int) getResources().getDimension(R.dimen.mms_contact_pick_item_padding_right);
        for (final VCardDetailNode node : this.mVcardDetailList) {
            String tempPropName = node.getPropName();
            String currentName = node.getName();
            String currentValue = node.getValue();
            if (TextUtils.equals(currentName, previousName) && TextUtils.equals(currentValue, r19)) {
                previousName = currentName;
                previousValue = currentValue;
                node.setSelect(false);
            } else if (!"FN".equalsIgnoreCase(tempPropName)) {
                View addTitleView = this.mInflater.inflate(R.layout.contact_item_title, null);
                TextView title_item_title = (TextView) addTitleView.findViewById(R.id.bcard_title_txt);
                View divider_view = addTitleView.findViewById(R.id.divider);
                if (tempPropName.equalsIgnoreCase(mPropName)) {
                    divider_view.setVisibility(0);
                } else {
                    mPropName = tempPropName;
                    title_item_title.setVisibility(0);
                    title_item_title.setText(convertPropName(getContext(), tempPropName));
                }
                this.mItemsLayout.addView(addTitleView);
                this.mConvertView = this.mInflater.inflate(R.layout.vcard_list_item_extend, null);
                this.mConvertView.setSelected(node.isSelect());
                this.mConvertView.setTag(node);
                Rect rect = new Rect(this.mConvertView.getPaddingLeft(), this.mConvertView.getPaddingTop(), this.mConvertView.getPaddingRight(), this.mConvertView.getPaddingBottom());
                this.mConvertView.setPadding(paddingLeft, rect.top, paddingRight, rect.bottom);
                ((TextView) this.mConvertView.findViewById(R.id.name)).setText(currentName);
                TextView valueView = (TextView) this.mConvertView.findViewById(R.id.value);
                if (currentValue == null) {
                    valueView.setVisibility(8);
                } else {
                    valueView.setVisibility(0);
                    valueView.setText(currentValue);
                }
                final CheckBox checkBox = (CheckBox) this.mConvertView.findViewById(R.id.select_box);
                checkBox.setVisibility(0);
                checkBox.setChecked(node.isSelect());
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        CheckBox checkBox = (CheckBox) v;
                        node.setSelect(checkBox.isChecked());
                        ContactItemPickFragment.this.mConvertView.setSelected(checkBox.isChecked());
                    }
                });
                this.mConvertView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                        node.setSelect(checkBox.isChecked());
                        ContactItemPickFragment.this.mConvertView.setSelected(checkBox.isChecked());
                    }
                });
                this.mItemsLayout.addView(this.mConvertView);
                previousName = currentName;
                previousValue = currentValue;
            }
        }
        View addDivideLine = this.mInflater.inflate(R.layout.contact_item_title, null);
        addDivideLine.findViewById(R.id.divider).setVisibility(0);
        this.mItemsLayout.addView(addDivideLine);
    }

    private String convertPropName(Context context, String propName) {
        if ("FN".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_name);
        }
        if ("X-NICKNAME".equalsIgnoreCase(propName) || "NICKNAME".equalsIgnoreCase(propName) || "X-ANDROID-CUSTOM".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_nickname);
        }
        if ("ORG".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_organisation);
        }
        if ("BDAY".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_birthday);
        }
        if ("NOTE".equalsIgnoreCase(propName)) {
            return context.getString(R.string.label_notes);
        }
        if ("TITLE".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_title);
        }
        if ("TEL".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_mobile);
        }
        if ("EMAIL".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_mobileemail);
        }
        if ("ADR".equalsIgnoreCase(propName)) {
            return context.getString(R.string.postalLabelsGroup);
        }
        if ("URL".equalsIgnoreCase(propName)) {
            return context.getString(R.string.websiteLabelsGroup);
        }
        if ("X-IMPP".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_im);
        }
        if ("CATEGORIES".equalsIgnoreCase(propName) || "X-CATEGORIES".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_group);
        }
        if ("X-PHONETIC-LAST-NAME".equalsIgnoreCase(propName)) {
            return context.getString(R.string.vcard_phoneticname);
        }
        return "";
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.affirm_cancel:
                cancelSave();
                return;
            case R.id.affirm_ok:
                saveSelectItem();
                return;
            default:
                return;
        }
    }

    private void saveSelectItem() {
        this.mVCardMessageHelper.editVcardDetailEx(this.mVCardModel);
        getController().setResult(this, -1, new Intent());
        if (!isDetached()) {
            finishSelf(false);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 4) {
            cancelSave();
        }
        return false;
    }

    private void cancelSave() {
        getController().setResult(this, 0, new Intent());
        if (!isDetached()) {
            finishSelf(false);
        }
    }
}
