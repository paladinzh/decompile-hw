package com.android.mms.ui;

import android.content.Context;
import android.widget.Toast;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.google.android.gms.R;

public class HwCustSlideshowFragmentImpl extends HwCustSlideshowFragment {
    Context mContext;

    public HwCustSlideshowFragmentImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void showToastInSlideshowWithVcardOrVcal(SlideshowModel model) {
        if (HwCustMmsConfigImpl.getEnableToastInSlideshowWithVcardOrVcal()) {
            for (int i = 0; i < model.size(); i++) {
                SlideModel slide = model.get(i);
                if (slide.getVcard() != null || slide.getVCalendar() != null) {
                    Toast.makeText(this.mContext, R.string.unsupported_mediatype_slideshow, 0).show();
                }
            }
        }
    }
}
