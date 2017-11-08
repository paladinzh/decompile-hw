package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;

public class UserDictionaryAddWordActivityHwBase extends Activity {
    protected UserDictionaryAddWordContents mContents;
    protected View mView;

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                new Builder(this).setTitle(2131625789).setView(this.mView).setNegativeButton(2131627333, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDictionaryAddWordActivityHwBase.this.reportBackToCaller(1, null);
                        UserDictionaryAddWordActivityHwBase.this.finish();
                    }
                }).setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (UserDictionaryAddWordActivityHwBase.this.mContents == null) {
                            UserDictionaryAddWordActivityHwBase.this.finish();
                            return;
                        }
                        Bundle parameters = new Bundle();
                        UserDictionaryAddWordActivityHwBase.this.reportBackToCaller(UserDictionaryAddWordActivityHwBase.this.mContents.apply(UserDictionaryAddWordActivityHwBase.this, parameters), parameters);
                        UserDictionaryAddWordActivityHwBase.this.finish();
                    }
                }).create().show();
                break;
        }
        return super.onCreateDialog(id);
    }

    protected void reportBackToCaller(int resultCode, Bundle result) {
    }

    protected void buildArgs(Bundle args) {
        String word = args.getString("word");
        int maxlen = getResources().getInteger(2131820549);
        if (word.length() > maxlen) {
            word = word.substring(0, maxlen - 1);
            args.remove("word");
            args.putString("word", word);
        }
    }
}
