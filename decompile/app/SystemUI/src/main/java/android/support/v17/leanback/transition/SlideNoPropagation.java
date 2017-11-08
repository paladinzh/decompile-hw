package android.support.v17.leanback.transition;

import android.content.Context;
import android.transition.Slide;
import android.util.AttributeSet;

public class SlideNoPropagation extends Slide {
    public SlideNoPropagation(int slideEdge) {
        super(slideEdge);
    }

    public SlideNoPropagation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSlideEdge(int slideEdge) {
        super.setSlideEdge(slideEdge);
        setPropagation(null);
    }
}
