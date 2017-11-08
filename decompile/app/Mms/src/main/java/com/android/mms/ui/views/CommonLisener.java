package com.android.mms.ui.views;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class CommonLisener {
    private static OnCancelListener sDismissCancelListener = new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
        }
    };
    private static OnClickListener sDismissClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    public static abstract class HideKeyboardTouchListener implements OnTouchListener {
        private boolean needhideInputMode = true;
        private int posStart = 0;

        protected abstract void hideKeyboard();

        public boolean onTouch(View arg0, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    this.posStart = (int) event.getRawY();
                    break;
                case 1:
                case 3:
                    this.needhideInputMode = true;
                    break;
                case 2:
                    if (this.needhideInputMode && Math.abs(((int) event.getRawY()) - this.posStart) > 20) {
                        hideKeyboard();
                        this.needhideInputMode = false;
                        break;
                    }
            }
            return false;
        }
    }

    public static OnClickListener getDismissClickListener() {
        return sDismissClickListener;
    }

    public static OnCancelListener getDismissCancelListener() {
        return sDismissCancelListener;
    }
}
