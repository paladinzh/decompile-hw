package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.google.android.gms.R;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.rcs.utils.RcseMmsExt;
import java.util.ArrayList;

public class OthersMediaChooser extends MediaChooser {
    private Context mContext;
    private GridView mGridView;
    private MediaPicker mMediaPicker;
    private ArrayList<Integer> mOtherTypes;

    private class OtherGridAdapter extends BaseAdapter {
        private OtherGridAdapter() {
        }

        public int getCount() {
            return OthersMediaChooser.this.mOtherTypes.size();
        }

        public Object getItem(int position) {
            return OthersMediaChooser.this.mOtherTypes.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup viewParent) {
            if (convertView == null) {
                convertView = OthersMediaChooser.this.getLayoutInflater().inflate(R.layout.mediapicker_others_btn, viewParent, false);
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.others_tab_button);
            TextView textView = (TextView) convertView.findViewById(R.id.others_tab_text);
            int imageRes = -1;
            int textRes = -1;
            switch (((Integer) OthersMediaChooser.this.mOtherTypes.get(position)).intValue()) {
                case 1003:
                    textRes = R.string.attach_contacts;
                    imageRes = R.drawable.ic_public_contacts;
                    break;
                case 1004:
                    textRes = R.string.vcalendar_calendar;
                    imageRes = R.drawable.ic_sms_schedule;
                    break;
                case 1005:
                    textRes = R.string.attach_phrases;
                    imageRes = R.drawable.ic_sms_add_phrase;
                    break;
                case 1006:
                    textRes = R.string.attach_sound;
                    imageRes = R.drawable.ic_public_music;
                    break;
                case 1007:
                    textRes = R.string.subject_hint;
                    imageRes = R.drawable.ic_sms_add_subject;
                    break;
                case 1008:
                    textRes = R.string.attach_slideshow;
                    imageRes = R.drawable.ic_sms_ppt_normal;
                    break;
                case 1009:
                    textRes = R.string.attach_anyfile;
                    imageRes = R.drawable.ic_message_rcs_toolbox_folder;
                    break;
            }
            imageView.setImageResource(imageRes);
            textView.setText(textRes);
            return convertView;
        }
    }

    public OthersMediaChooser(MediaPicker mediaPicker, Context context) {
        super(mediaPicker);
        this.mMediaPicker = mediaPicker;
        this.mContext = context;
        refreshOthersTypes();
    }

    private void refreshOthersTypes() {
        if (this.mOtherTypes != null) {
            this.mOtherTypes.clear();
        } else {
            this.mOtherTypes = new ArrayList();
        }
        if (RcseMmsExt.isRcsMode() || this.mContext.getClass().toString().contains("RcsGroupChatComposeMessageActivity")) {
            this.mOtherTypes.add(Integer.valueOf(1003));
            this.mOtherTypes.add(Integer.valueOf(1005));
            if (MmsConfig.getSupportedVCalendarEnabled()) {
                this.mOtherTypes.add(Integer.valueOf(1004));
            }
            this.mOtherTypes.add(Integer.valueOf(1006));
            this.mOtherTypes.add(Integer.valueOf(1009));
        } else if (MmsConfig.isInSimpleUI() && MmsConfig.isSupportMmsSubject()) {
            this.mOtherTypes.add(Integer.valueOf(1007));
        } else {
            this.mOtherTypes.add(Integer.valueOf(1003));
            this.mOtherTypes.add(Integer.valueOf(1005));
            if (MmsConfig.getSupportedVCalendarEnabled()) {
                this.mOtherTypes.add(Integer.valueOf(1004));
            }
            this.mOtherTypes.add(Integer.valueOf(1006));
            this.mOtherTypes.add(Integer.valueOf(1007));
            this.mOtherTypes.add(Integer.valueOf(1008));
        }
    }

    public int getSupportedMediaTypes() {
        return 192;
    }

    protected int getIconResource() {
        return this.mSelected ? R.drawable.ic_public_more : R.drawable.ic_public_more_normal;
    }

    protected int getIconTextResource() {
        return R.string.menu_add_rcs_more;
    }

    protected void onRestoreChooserState() {
        setSelected(false);
    }

    protected int getActionBarTitleResId() {
        return 1;
    }

    protected void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    protected View createView(ViewGroup container) {
        View rootView = getLayoutInflater().inflate(R.layout.mediapicker_others_chooser, container, false);
        this.mGridView = (GridView) rootView.findViewById(R.id.mediapicker_others_gridview);
        this.mGridView.setAdapter(new OtherGridAdapter());
        this.mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                int type = ((Integer) OthersMediaChooser.this.mOtherTypes.get(position)).intValue();
                if (type != 1008 || OthersMediaChooser.this.mMediaPicker.getActivity() == null || FragmentTag.getFragmentByTag(OthersMediaChooser.this.mMediaPicker.getActivity(), "Mms_UI_CMF") == null) {
                    OthersMediaChooser.this.mMediaPicker.dispatchPickerOperate(type, null);
                } else {
                    ((ComposeMessageFragment) FragmentTag.getFragmentByTag(OthersMediaChooser.this.mMediaPicker.getActivity(), "Mms_UI_CMF")).addAttachment(14, false);
                }
            }
        });
        return rootView;
    }

    public View destroyView() {
        return super.destroyView();
    }

    protected void updateActionBar(AbstractEmuiActionBar actionBar) {
        if (actionBar != null && this.mMediaPicker.isFullScreen()) {
            actionBar.setTitle(getContext().getResources().getString(R.string.menu_add_rcs_more));
        }
    }
}
