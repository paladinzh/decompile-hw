package com.android.rcs.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import com.android.mms.util.ShareUtils;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsShowDialogForFT;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RcsFtShareMsgForwarder extends RcsBaseForwarder<Bundle> implements RcsShowDialogForFT {
    private boolean isFtShareForwardProcess = false;

    public void onShareFtResult(Intent data) {
        if (this.isRcsOn) {
            super.rcsActivityResult(data);
        }
    }

    public void dispatchGroupChatIntent(String groupId) {
        if (this.isRcsOn) {
            ArrayList arrayList;
            try {
                arrayList = (ArrayList) getUriFromIntent((Bundle) this.mMessageData);
            } catch (RuntimeException e) {
                MLog.e("RcsFtShareMsgForwarder", "dispatchGroupChatIntent getUriFromIntent error:" + e.getLocalizedMessage());
                arrayList = null;
            }
            Intent intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
            intent.putExtra("bundle_group_id", groupId);
            if (arrayList != null) {
                intent.putParcelableArrayListExtra("bundle_pic_uri", arrayList);
            }
            intent.putExtras((Bundle) this.mMessageData);
            if (this.mFragment != null) {
                this.mFragment.startActivity(intent);
                this.mFragment.finishSelf(false);
            }
        }
    }

    public void setMessageData(Bundle bundle) {
        if (this.isRcsOn) {
            this.mMessageData = bundle;
        }
    }

    public void processMassOrSingle(List<String> rcsList, int rcsUsers) {
        if (this.isRcsOn) {
            Intent intent;
            if (rcsList.size() <= 1) {
                intent = getChatIntent(rcsList, (Bundle) this.mMessageData);
            } else {
                intent = getGroupSmsIntent(rcsList, (Bundle) this.mMessageData);
            }
            intent.setClassName(this.mContext, "com.android.mms.ui.ForwardMessageActivity");
            if (this.mFragment != null) {
                this.mFragment.startActivity(intent);
                this.mFragment.finishSelf(false);
            }
            this.isFtShareForwardProcess = true;
        }
    }

    private List<Uri> getUriFromIntent(Bundle extras) {
        List<Uri> uriList = new ArrayList();
        if (extras.get("android.intent.extra.STREAM") == null) {
            uriList.add(((Intent) extras.get("BODY")).getData());
        } else {
            String uriString = extras.get("android.intent.extra.STREAM").toString();
            if (uriString.startsWith("[")) {
                uriString = uriString.substring(1, uriString.length() - 1);
            }
            String[] uriStrs = uriString.split(",");
            for (int i = 0; i < uriStrs.length; i++) {
                if (ShareUtils.isFileProviderImageType(uriStrs[i].trim())) {
                    uriList.add(ShareUtils.copyFile(this.mContext, Uri.parse(uriStrs[i].trim()), "shared_image_file.png"));
                } else {
                    uriList.add(Uri.parse(uriStrs[i].trim()));
                }
            }
        }
        return uriList;
    }

    public void showUserFtVardDialog(HashMap<String, String> personMap, RcsShowDialogForFT show, int type, List<String> rcsList) {
        final SharedPreferences pref = this.mContext.getSharedPreferences(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, 0);
        if (pref.getBoolean("no_need_dialog_for_vcf", false)) {
            callbackToContinue(personMap, type, show, rcsList);
            return;
        }
        View view = View.inflate(this.mContext, R.layout.rcs_ft_send_notice_dialog, null);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.vcard_not_ask_me);
        final HashMap<String, String> hashMap = personMap;
        final int i = type;
        final RcsShowDialogForFT rcsShowDialogForFT = show;
        final List<String> list = rcsList;
        new Builder(this.mContext).setMessage(R.string.rcs_im_send_contacts_note).setTitle(R.string.mms_remind_title).setView(view).setPositiveButton(R.string.nickname_dialog_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkbox.isChecked()) {
                    Editor editor = pref.edit();
                    editor.putBoolean("no_need_dialog_for_vcf", true);
                    editor.commit();
                }
                RcsFtShareMsgForwarder.this.callbackToContinue(hashMap, i, rcsShowDialogForFT, list);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.nickname_dialog_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (RcsFtShareMsgForwarder.this.mFragment != null) {
                    RcsFtShareMsgForwarder.this.mFragment.finishSelf(false);
                }
                dialog.dismiss();
            }
        }).create().show();
    }

    public void clickOk(Context context, HashMap<String, String> personMap) {
        if (this.isRcsOn) {
            super.showCreateGroupChatDialog(context, personMap);
        }
    }

    protected void showCreateGroupChatDialog(Context context, HashMap<String, String> personMap) {
        if (this.isRcsOn) {
            if (checkShareContact()) {
                showUserFtVardDialog(personMap, this, 2, null);
            } else {
                super.showCreateGroupChatDialog(context, personMap);
            }
        }
    }

    private boolean checkShareContact() {
        try {
            ArrayList<Uri> picUriList = (ArrayList) getUriFromIntent((Bundle) this.mMessageData);
            if (picUriList.size() != 1) {
                return false;
            }
            Uri uriFilename = (Uri) picUriList.get(0);
            String filename = uriFilename.toString();
            Intent data = new Intent();
            data.putExtras((Bundle) this.mMessageData);
            return !filename.substring(filename.lastIndexOf(".") + 1).equals("vcf") && RcsTransaction.isVCardFile(uriFilename, data);
        } catch (RuntimeException e) {
            MLog.e("RcsFtShareMsgForwarder", "checkShareContact getUriFromIntent error:" + e.getLocalizedMessage());
            return false;
        }
    }

    private void callbackToContinue(HashMap<String, String> personMap, int type, RcsShowDialogForFT show, List<String> list) {
        switch (type) {
            case 2:
                show.clickOk(this.mContext, personMap);
                return;
            default:
                return;
        }
    }

    public boolean isFtShareProcess() {
        return this.isFtShareForwardProcess;
    }
}
