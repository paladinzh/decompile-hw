package com.android.mms.ui;

import com.android.mms.model.Model;
import com.android.mms.model.SlideModel;

public class HwThumbnailPresenter extends MmsThumbnailPresenter {
    public void onModelChanged(Model model, boolean dataChanged) {
        if (dataChanged) {
            present(null);
        }
    }

    protected void presentFirstSlide(final SlideViewInterface view, final SlideModel slide) {
        this.mHandler.post(new Runnable() {
            public void run() {
                view.reset();
                if (slide.hasText()) {
                    HwThumbnailPresenter.this.presentTextThumbnail(view, slide.getText());
                }
                if (slide.hasAudio()) {
                    HwThumbnailPresenter.this.presentAudioThumbnail(view, slide.getAudio());
                }
                if (slide.hasImage()) {
                    HwThumbnailPresenter.this.presentImageThumbnail(view, slide.getImage());
                }
                if (slide.hasVideo()) {
                    HwThumbnailPresenter.this.presentVideoThumbnail(view, slide.getVideo());
                }
                if (slide.hasVcard()) {
                    HwThumbnailPresenter.this.presentVcardThumbnail(view, slide.getVcard());
                }
                if (slide.hasVCalendar()) {
                    HwThumbnailPresenter.this.presentVCalendarThumbnail(view, slide.getVCalendar());
                }
            }
        });
    }
}
