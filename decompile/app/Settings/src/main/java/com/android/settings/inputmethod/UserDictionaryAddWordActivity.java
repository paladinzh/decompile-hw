package com.android.settings.inputmethod;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class UserDictionaryAddWordActivity extends UserDictionaryAddWordActivityHwBase {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        int mode;
        String action = intent.getAction();
        if ("com.android.settings.USER_DICTIONARY_EDIT".equals(action)) {
            mode = 0;
        } else if ("com.android.settings.USER_DICTIONARY_INSERT".equals(action)) {
            mode = 1;
        } else {
            finish();
            return;
        }
        Bundle args = intent.getExtras();
        if (args == null) {
            finish();
            return;
        }
        args.putInt("mode", mode);
        buildArgs(args);
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }
        this.mView = getLayoutInflater().inflate(2130969243, null);
        this.mContents = new UserDictionaryAddWordContents(this.mView, args);
        getWindow().setBackgroundDrawableResource(17170445);
        showDialog(0);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mContents != null) {
            this.mContents.saveStateIntoBundle(outState);
        }
    }

    protected void reportBackToCaller(int resultCode, Bundle result) {
        Messenger listener = getIntent().getExtras().get("listener");
        if (listener instanceof Messenger) {
            Messenger messenger = listener;
            Message m = Message.obtain();
            m.obj = result;
            m.what = resultCode;
            try {
                messenger.send(m);
            } catch (RemoteException e) {
            }
        }
    }
}
