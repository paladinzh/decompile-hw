package com.huawei.gallery.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;

public class ListSpinnerActionBar extends ListSpinnerBase {
    private AlertDialog mPopup;

    public ListSpinnerActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListSpinnerActionBar(Context context) {
        this(context, null);
    }

    private void init() {
        ImmersionUtils.setTextViewDefaultColorImmersionStyle((TextView) findViewById(R.id.title), 0);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle((TextView) findViewById(R.id.parenthese_left), 0);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle((TextView) findViewById(16908308), 0);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle((TextView) findViewById(R.id.parenthese_right), 0);
        ImmersionUtils.setImageViewSrcImmersionStyle((ImageView) findViewById(R.id.menu_select), R.drawable.ic_menu_select_light, R.drawable.ic_menu_select, 0);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        TextView title = (TextView) findViewById(R.id.title);
        if (!(title == null || title.getText() == null || title.getText().equals(getPrompt()))) {
            title.setText(getPrompt());
        }
        init();
    }

    protected int getCustomLayoutId() {
        return R.layout.spinner_camera_actionbar_layout;
    }

    public boolean performClick() {
        this.mPopup = new Builder(getContext()).setSingleChoiceItems(this.mListAdapter, getSelectedItemPosition(), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListSpinnerActionBar.this.setSelection(which);
                ListSpinnerActionBar.this.performItemClick(null, which, ListSpinnerActionBar.this.mListAdapter.getItemId(which));
                if (ListSpinnerActionBar.this.mPopup != null && ListSpinnerActionBar.this.mPopup.isShowing()) {
                    ListSpinnerActionBar.this.mPopup.dismiss();
                }
            }
        }).create();
        if (!(this.mPopup == null || this.mPopup.isShowing())) {
            this.mPopup.show();
        }
        return true;
    }
}
