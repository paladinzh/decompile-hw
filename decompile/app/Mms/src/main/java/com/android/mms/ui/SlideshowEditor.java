package com.android.mms.ui;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsConfig;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.utils.RcseMmsExt;
import java.nio.charset.Charset;

public class SlideshowEditor {
    private final Context mContext;
    private SlideshowModel mModel;

    public SlideshowEditor(Context context, SlideshowModel model) {
        this.mContext = context;
        this.mModel = model;
    }

    public boolean addNewSlide(int position) {
        if (this.mModel.size() < MmsConfig.getMaxSlides()) {
            SlideModel slide = new SlideModel(this.mModel);
            try {
                slide.add(new TextModel(this.mContext, "text/plain", "text_" + SystemClock.elapsedRealtime() + ".txt", this.mModel.getLayout().getTextRegion()));
                this.mModel.add(position, slide);
            } catch (ExceedMessageSizeException e) {
                MLog.v("Mms:slideshow", "ExceedMessageSizeException");
            }
            return true;
        }
        int maxSlides = MmsConfig.getMaxSlides();
        int importSlides = maxSlides;
        Toast.makeText(this.mContext, this.mContext.getResources().getQuantityString(R.plurals.too_many_attachments_Toast, maxSlides, new Object[]{Integer.valueOf(maxSlides), Integer.valueOf(maxSlides)}), 1).show();
        MLog.w("Mms:slideshow", "The limitation of the number of slides is reached.");
        return false;
    }

    public boolean removeSlide(SlideModel slideModel) {
        if (slideModel == null) {
            return false;
        }
        return this.mModel.remove((Object) slideModel);
    }

    public void removeSlide(int position) {
        this.mModel.remove(position);
    }

    public void removeAllSlides() {
        while (this.mModel.size() > 0) {
            removeSlide(0);
        }
    }

    public void changeText(int position, String newText) {
        if (newText != null) {
            try {
                SlideModel slide = this.mModel.get(position);
                TextModel text = slide.getText();
                if (text == null) {
                    try {
                        slide.add(new TextModel(this.mContext, "text/plain", "text_" + SystemClock.elapsedRealtime() + ".txt", 106, newText.getBytes(Charset.defaultCharset()), this.mModel.getLayout().getTextRegion()));
                    } catch (ExceedMessageSizeException e) {
                        ResEx.self();
                        ResEx.makeToast((int) R.string.change_text_fail, 1);
                        MLog.v("Mms:slideshow", "ExceedMessageSizeException");
                    }
                } else if (!newText.equals(text.getText())) {
                    int oldTextSize = text.encodeText(text.getText()).length;
                    int newTextSize = text.encodeText(newText).length;
                    if (RcseMmsExt.isRcsMode() || (this.mModel.getCurrentMessageSize() - oldTextSize) + newTextSize <= MmsConfig.getMaxMessageSize() - 4096) {
                        if (newTextSize > oldTextSize) {
                            this.mModel.increaseMessageSize(newTextSize - oldTextSize);
                        } else {
                            this.mModel.decreaseMessageSize(oldTextSize - newTextSize);
                        }
                        text.setText(newText);
                    } else {
                        MLog.i("Mms:slideshow", "slide add text failed because size excess MaxMessageSize!");
                        ResEx.self();
                        ResEx.makeToast((int) R.string.change_text_fail, 1);
                    }
                }
            } catch (ExceedMessageSizeException e2) {
            }
        }
    }

    public void changeDuration(int position, int dur) {
        if (dur >= 0) {
            this.mModel.get(position).setDuration(dur);
        }
    }

    public void changeLayout(int layout) {
        this.mModel.getLayout().changeTo(layout);
    }
}
