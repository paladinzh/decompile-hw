package com.android.systemui.statusbar.stack;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class StackViewState extends ViewState {
    public boolean belowSpeedBump;
    public int clipTopAmount;
    public boolean dark;
    public boolean dimmed;
    public int height;
    public boolean hideSensitive;
    public boolean isBottomClipped;
    public int location;
    public int notGoneIndex;
    public boolean overlap;
    public float shadowAlpha;

    public void copyFrom(ViewState viewState) {
        super.copyFrom(viewState);
        if (viewState instanceof StackViewState) {
            StackViewState svs = (StackViewState) viewState;
            this.height = svs.height;
            this.dimmed = svs.dimmed;
            this.shadowAlpha = svs.shadowAlpha;
            this.dark = svs.dark;
            this.hideSensitive = svs.hideSensitive;
            this.belowSpeedBump = svs.belowSpeedBump;
            this.clipTopAmount = svs.clipTopAmount;
            this.notGoneIndex = svs.notGoneIndex;
            this.location = svs.location;
            this.isBottomClipped = svs.isBottomClipped;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("StackViewState:");
        pw.print(" alpha=" + this.alpha);
        pw.print(" yTranslation=" + this.yTranslation);
        pw.print(" zTranslation=" + this.zTranslation);
        pw.print(" gone=" + this.gone);
        pw.print(" hidden=" + this.hidden);
        pw.print(" height=" + this.height);
        pw.print(" dimmed=" + this.dimmed);
        pw.print(" shadowAlpha=" + this.shadowAlpha);
        pw.print(" dark=" + this.dark);
        pw.print(" hideSensitive=" + this.hideSensitive);
        pw.print(" belowSpeedBump=" + this.belowSpeedBump);
        pw.print(" clipTopAmount=" + this.clipTopAmount);
        pw.print(" notGoneIndex=" + this.notGoneIndex);
        pw.print(" location=" + this.location);
        pw.println();
    }
}
