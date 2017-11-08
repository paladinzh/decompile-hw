package com.android.contacts.editor;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.google.android.gms.R;
import java.util.ArrayList;

public class PhotoActionPopup {

    public interface Listener {
        void onPickFromGalleryChosen();

        void onRemovePictureChosen();

        void onTakePhotoChosen();

        void onUseAsPrimaryChosen();
    }

    private static final class ChoiceListItem {
        private final String mCaption;
        private final int mId;

        public ChoiceListItem(int id, String caption) {
            this.mId = id;
            this.mCaption = caption;
        }

        public String toString() {
            return this.mCaption;
        }

        public int getId() {
            return this.mId;
        }
    }

    public static AlertDialog createAlertDialog(Context context, View anchorView, Listener listener, int mode) {
        final ArrayList<ChoiceListItem> choices = new ArrayList(4);
        if ((mode & 1) != 0) {
            choices.add(new ChoiceListItem(0, context.getString(R.string.use_photo_as_primary)));
        }
        if ((mode & 2) != 0) {
            choices.add(new ChoiceListItem(4, context.getString(R.string.removePhoto)));
        }
        if ((mode & 4) != 0) {
            int pickPhotoResId;
            boolean replace = (mode & 8) != 0;
            String takePhotoString = context.getString(replace ? R.string.take_new_photo : R.string.take_photo);
            if (replace) {
                pickPhotoResId = R.string.pick_new_photo;
            } else {
                pickPhotoResId = R.string.pickPhoto;
            }
            String pickPhotoString = context.getString(pickPhotoResId);
            choices.add(new ChoiceListItem(1, takePhotoString));
            choices.add(new ChoiceListItem(3, pickPhotoString));
        }
        final int srcColor = context.getResources().getColor(R.color.shortcut_item_type_textcolor);
        ListAdapter adapter = new ArrayAdapter<ChoiceListItem>(context, R.layout.select_dialog_item, choices) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int id = ((ChoiceListItem) getItem(position)).getId();
                if (!EmuiFeatureManager.isSuperSaverMode()) {
                    return view;
                }
                TextView mTextView = (TextView) view.findViewById(16908308);
                switch (id) {
                    case 1:
                    case 3:
                        int unenableColor = ImmersionUtils.getColorWithAlpha(srcColor, 76);
                        view.setEnabled(false);
                        mTextView.setTextColor(unenableColor);
                        break;
                    default:
                        view.setEnabled(true);
                        mTextView.setTextColor(srcColor);
                        break;
                }
                return view;
            }
        };
        Builder builder = new Builder(context);
        final Listener listener2 = listener;
        builder.setAdapter(adapter, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (((ChoiceListItem) choices.get(which)).getId()) {
                    case 0:
                        listener2.onUseAsPrimaryChosen();
                        break;
                    case 1:
                        if (!EmuiFeatureManager.isSuperSaverMode()) {
                            listener2.onTakePhotoChosen();
                            break;
                        }
                        break;
                    case 3:
                        if (!EmuiFeatureManager.isSuperSaverMode()) {
                            listener2.onPickFromGalleryChosen();
                            break;
                        }
                        break;
                    case 4:
                        listener2.onRemovePictureChosen();
                        break;
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
