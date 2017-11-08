package com.android.mms.attachment.ui.mediapicker.camerafocus;

public interface FocusIndicator {
    void clear();

    void showFail(boolean z);

    void showStart();

    void showSuccess(boolean z);
}
