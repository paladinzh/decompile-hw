package com.android.mms.attachment.datamodel.control;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import com.android.mms.attachment.datamodel.data.AttachmentSelectLocation;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import java.util.HashMap;

public class AttachmentSelectLocationControl {
    public static HashMap<String, String> parseLocationMap(Context context, String fragmentTag) {
        if (context == null || TextUtils.isEmpty(fragmentTag)) {
            return null;
        }
        AttachmentSelectLocation attachmentLoaction = null;
        Fragment fragment = FragmentTag.getFragmentByTag(context, fragmentTag);
        if (fragment == null) {
            return null;
        }
        if ("Mms_UI_CMF".equals(fragmentTag)) {
            attachmentLoaction = ((ComposeMessageFragment) fragment).getAttachmentLocation();
        } else if ("Mms_UI_GCCMF".equals(fragmentTag)) {
            attachmentLoaction = ((RcsGroupChatComposeMessageFragment) fragment).getAttachmentLocation();
        }
        if (attachmentLoaction == null) {
            return null;
        }
        HashMap<String, String> locationMap = new HashMap();
        String locationTitle = attachmentLoaction.getLocationTitle();
        String locationSub = attachmentLoaction.getLocationSub();
        String latitude = attachmentLoaction.getLatitude();
        String longitude = attachmentLoaction.getLongitude();
        locationMap.put("title", locationTitle);
        locationMap.put("subtitle", locationSub);
        locationMap.put("latitude", latitude);
        locationMap.put("longitude", longitude);
        locationMap.put("locationinfo", locationTitle + "\n" + locationSub + "\n" + MessageUtils.getLocationWebLink(context) + latitude + "," + longitude);
        return locationMap;
    }

    public static void sealLoactionValue(Context context, String fragmentTag, ContentValues values) {
        if (context != null && values != null && !TextUtils.isEmpty(fragmentTag)) {
            AttachmentSelectLocation attachmentLoaction = null;
            Fragment fragment = FragmentTag.getFragmentByTag(context, fragmentTag);
            if (fragment != null) {
                if ("Mms_UI_CMF".equals(fragmentTag)) {
                    attachmentLoaction = ((ComposeMessageFragment) fragment).getAttachmentLocation();
                }
                if (attachmentLoaction != null) {
                    values.put("islocation", Integer.valueOf(1));
                    values.put("locationtitle", attachmentLoaction.getLocationTitle());
                    values.put("locationsub", attachmentLoaction.getLocationSub());
                    values.put("latitude", attachmentLoaction.getLatitude());
                    values.put("longitude", attachmentLoaction.getLongitude());
                }
            }
        }
    }
}
