package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactsActivity;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.group.GroupEditorFragment.Listener;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.DialogManager.DialogShowingViewActivity;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class GroupEditorActivity extends ContactsActivity implements DialogShowingViewActivity {
    private DialogManager mDialogManager = new DialogManager(this);
    private GroupEditorFragment mFragment;
    private final Listener mFragmentListener = new Listener() {
        public void onGroupNotFound() {
            GroupEditorActivity.this.finish();
        }

        public void onReverted() {
            GroupEditorActivity.this.finish();
        }

        public void onAccountsNotFound() {
            GroupEditorActivity.this.finish();
        }

        public void onSaveFinished(int resultCode, Intent resultIntent) {
            if (!(resultIntent == null || resultIntent.getData() == null)) {
                Intent intent = new Intent(GroupEditorActivity.this, GroupDetailActivity.class);
                intent.setData(resultIntent.getData());
                intent.setFlags(67108864);
                GroupEditorActivity.this.startActivity(intent);
            }
            GroupEditorActivity.this.finish();
        }
    };

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        String action = getIntent().getAction();
        if ("saveCompleted".equals(action)) {
            finish();
            return;
        }
        long lContactId = 0;
        if (savedState == null) {
            lContactId = getIntent().getLongExtra("ContactId", 0);
        }
        setContentView(R.layout.group_editor_activity);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            View customActionBarView = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.editor_custom_action_bar, new LinearLayout(getBaseContext()), false);
            customActionBarView.findViewById(R.id.save_menu_item).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    GroupEditorActivity.this.mFragment.onDoneClicked();
                }
            });
            TextView lSaveTextView = (TextView) customActionBarView.findViewById(R.id.save_menu_textview);
            if (lSaveTextView != null) {
                lSaveTextView.setText(R.string.description_save_button);
            }
            customActionBarView.findViewById(R.id.cancle_menu_item).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    GroupEditorActivity.this.mFragment.revert();
                }
            });
            actionBar.setDisplayOptions(16, 26);
            actionBar.setCustomView(customActionBarView);
        }
        this.mFragment = (GroupEditorFragment) getFragmentManager().findFragmentById(R.id.group_editor_fragment);
        this.mFragment.setListener(this.mFragmentListener);
        this.mFragment.setContentResolver(getContentResolver());
        if (savedState == null) {
            this.mFragment.load(action, "android.intent.action.EDIT".equals(action) ? getIntent().getData() : null, getIntent().getExtras());
        }
        if (lContactId != 0) {
            this.mFragment.setContactId(lContactId);
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        if (DialogManager.isManagedId(id)) {
            return this.mDialogManager.onCreateDialog(id, args);
        }
        HwLog.w("GroupEditorActivity", "Unknown dialog requested, id: " + id + ", args: " + args);
        return null;
    }

    public void onBackPressed() {
        this.mFragment.revert();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mFragment != null) {
            if ("saveCompleted".equals(intent.getAction())) {
                this.mFragment.onSaveCompleted(true, intent.getData());
            }
        }
    }

    public DialogManager getDialogManager() {
        return this.mDialogManager;
    }
}
