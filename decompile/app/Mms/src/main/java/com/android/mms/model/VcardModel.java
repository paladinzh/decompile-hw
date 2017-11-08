package com.android.mms.model;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.mms.VCardSmsMessage;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.events.Event;

public class VcardModel extends MediaModel {
    private static int sVcardSelectIndex = 0;
    private static int sVcardSelectNum = 0;
    private Bitmap mBitmap;
    private boolean mIsUseDefaultImg;
    private String mName;
    private Set<String> mUnSelectNodes = new HashSet();
    private List<VCardDetailNode> mVcardDetailList;
    private int mVcardSize;

    public static class VCardDetailNode {
        private String mName;
        private int mPriority;
        private String mPropName;
        private String mPropType;
        private boolean mSelect = true;
        private String mValue;

        public VCardDetailNode(String name, String value, String propName, String propType, int priority) {
            this.mName = name;
            this.mValue = value;
            this.mPropName = propName;
            this.mPropType = propType;
            this.mPriority = priority;
        }

        public String getName() {
            return this.mName;
        }

        public String getValue() {
            return this.mValue;
        }

        public String getPropName() {
            return this.mPropName;
        }

        public String getPropType() {
            return this.mPropType;
        }

        public boolean isSelect() {
            return this.mSelect;
        }

        public void setSelect(boolean selectstatus) {
            this.mSelect = selectstatus;
        }

        public int before(VCardDetailNode node) {
            return this.mPriority - node.mPriority;
        }
    }

    public static class VcardAdapter extends BaseAdapter {
        private boolean mCanEdit;
        private Button mConfirmButton;
        private LayoutInflater mInflater;
        private List<VCardDetailNode> mVCardDetailList;

        public VcardAdapter(Context context, List<VCardDetailNode> vCardDetailList, boolean canEdit) {
            this.mVCardDetailList = vCardDetailList;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mCanEdit = canEdit;
        }

        public int getCount() {
            return this.mVCardDetailList.size();
        }

        public Object getItem(int position) {
            return this.mVCardDetailList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.vcard_list_item, parent, false);
            }
            bindView(position, convertView);
            return convertView;
        }

        private void bindView(int position, View view) {
            if (this.mCanEdit) {
                Rect r = new Rect(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                view.setPadding(r.left, r.top, r.right, r.bottom);
            }
            final VCardDetailNode vCardDetailNode = (VCardDetailNode) this.mVCardDetailList.get(position);
            ((TextView) view.findViewById(R.id.name)).setText(vCardDetailNode.mName);
            TextView valueView = (TextView) view.findViewById(R.id.value);
            if (vCardDetailNode.mValue == null) {
                valueView.setVisibility(8);
            } else {
                valueView.setVisibility(0);
                valueView.setText(vCardDetailNode.mValue);
            }
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.select_box);
            if (this.mCanEdit) {
                checkBox.setVisibility(0);
                checkBox.setChecked(vCardDetailNode.mSelect);
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        vCardDetailNode.mSelect = ((CheckBox) v).isChecked();
                        VcardAdapter.this.updateButtonState();
                    }
                });
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        checkBox.setChecked(!checkBox.isChecked());
                        vCardDetailNode.mSelect = checkBox.isChecked();
                        VcardAdapter.this.updateButtonState();
                    }
                });
                return;
            }
            checkBox.setVisibility(8);
        }

        public void setButton(Button confirmButton) {
            this.mConfirmButton = confirmButton;
            updateButtonState();
        }

        private void updateButtonState() {
            if (this.mConfirmButton != null) {
                for (VCardDetailNode vCardDetailNode : this.mVCardDetailList) {
                    if (vCardDetailNode.mSelect) {
                        this.mConfirmButton.setEnabled(true);
                        return;
                    }
                }
                this.mConfirmButton.setEnabled(false);
            }
        }
    }

    public static byte[] getVcardDataFromUri(Context cntxt, Uri uri) throws MmsException {
        byte[] bArr = null;
        InputStream inputStream = null;
        try {
            inputStream = cntxt.getContentResolver().openInputStream(uri);
            if (inputStream instanceof FileInputStream) {
                bArr = new byte[inputStream.available()];
                MLog.d("Mms/media", "vcard read data number=" + inputStream.read(bArr));
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e);
                }
            }
        } catch (IOException e2) {
            MLog.e("Mms/media", "IOException caught while opening or reading stream", (Throwable) e2);
            if (e2 instanceof FileNotFoundException) {
                throw new MmsException(e2.getMessage());
            } else if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e22);
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e222);
                }
            }
        }
        return bArr;
    }

    public VcardModel(Context context, String contentType, Uri uri) throws MmsException {
        super(context, "vcard", contentType, "vCard.vcf", uri);
        synchronized (VcardModel.class) {
            init(context, uri);
        }
    }

    private void init(Context context, Uri uri) throws MmsException {
        VCardSmsMessage.createVNodeBuilder(uri, context);
        this.mName = VCardSmsMessage.stripVcardName(context);
        this.mVcardSize = VCardSmsMessage.getVcardSize();
        this.mBitmap = VCardSmsMessage.stripVcardPhoto();
        if (this.mBitmap == null) {
            this.mBitmap = VCardSmsMessage.stripDefaultVcardPhoto(this.mContext);
            this.mIsUseDefaultImg = true;
        }
        if (uri.getAuthority().startsWith("mms")) {
            setUri(uri);
        }
        this.mVcardDetailList = VCardSmsMessage.getVcardDetail(this.mContext);
        setData(getVcardDataFromUri(context, uri));
    }

    public void handleEvent(Event evt) {
        notifyModelChanged(false);
    }

    public Bitmap getBitmap() {
        return internalGetBitmap();
    }

    private Bitmap internalGetBitmap() {
        if (this.mBitmap == null) {
            try {
                this.mBitmap = createThumbnailBitmap();
            } catch (OutOfMemoryError e) {
            }
        }
        return this.mBitmap;
    }

    private Bitmap createThumbnailBitmap() {
        return BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.cs_spinner);
    }

    public String getName() {
        return this.mName;
    }

    public boolean isUseDefaultImg() {
        return this.mIsUseDefaultImg;
    }

    public List<VCardDetailNode> getVcardDetailList() {
        return this.mVcardDetailList;
    }

    public List<VCardDetailNode> getVcardSelectedDetailList() {
        List<VCardDetailNode> selectedDetailList = new ArrayList();
        for (VCardDetailNode vCardDetailNode : this.mVcardDetailList) {
            if (!this.mUnSelectNodes.contains(getSaveStateKey(vCardDetailNode))) {
                selectedDetailList.add(vCardDetailNode);
            }
        }
        return selectedDetailList;
    }

    public int getVcardSize() {
        return this.mVcardSize;
    }

    public void cacheSelectState() {
        for (VCardDetailNode vCardDetailNode : this.mVcardDetailList) {
            if (vCardDetailNode.mSelect) {
                this.mUnSelectNodes.remove(getSaveStateKey(vCardDetailNode));
            } else {
                this.mUnSelectNodes.add(getSaveStateKey(vCardDetailNode));
            }
        }
    }

    public static int getSelectState() {
        return sVcardSelectIndex;
    }

    public static int getSelectNum() {
        return sVcardSelectNum;
    }

    public void saveSelectState(boolean saveDetail) {
        if (getUri() != null) {
            ContentValues values = new ContentValues();
            if (saveDetail) {
                StringBuffer selectStateBuffer = new StringBuffer();
                for (VCardDetailNode vCardDetailNode : this.mVcardDetailList) {
                    if (!vCardDetailNode.mSelect) {
                        selectStateBuffer.append(getSaveStateKey(vCardDetailNode)).append(";");
                    }
                }
                if (selectStateBuffer.length() > 1) {
                    selectStateBuffer.deleteCharAt(selectStateBuffer.length() - 1);
                }
                values.put("text", selectStateBuffer.toString());
            } else {
                values.put("text", "");
            }
            SqliteWrapper.update(this.mContext, getUri(), values, null, null);
        }
    }

    private String getSaveStateKey(VCardDetailNode vCardDetailNode) {
        StringBuffer keyBuffer = new StringBuffer(vCardDetailNode.mPropName);
        if (this.mVcardSize == 1 && vCardDetailNode.mValue != null) {
            keyBuffer.append(vCardDetailNode.mValue);
        }
        MLog.v("Mms/media", "getSaveStateKey::the name and value are: " + vCardDetailNode.mPropName + " and ****.");
        return keyBuffer.toString();
    }
}
