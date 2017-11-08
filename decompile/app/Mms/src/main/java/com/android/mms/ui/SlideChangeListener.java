package com.android.mms.ui;

/* compiled from: SlidePage */
interface SlideChangeListener {
    void onAttachmentRemoved(SlidePage slidePage);

    void onAudioRemoved(SlidePage slidePage);

    void onInputManagerShow();

    void onSlideAcitived(SlidePage slidePage);

    void onSlideRemoved(SlidePage slidePage);

    void onSlideTextChange(SlidePage slidePage, String str);

    void onSlideView(SlidePage slidePage, int i);
}
