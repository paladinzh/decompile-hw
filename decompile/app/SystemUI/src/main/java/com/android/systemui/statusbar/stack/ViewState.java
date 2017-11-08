package com.android.systemui.statusbar.stack;

import android.view.View;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ViewState {
    public float alpha;
    public boolean gone;
    public boolean hidden;
    public float yTranslation;
    public float zTranslation;

    public void copyFrom(ViewState viewState) {
        this.alpha = viewState.alpha;
        this.yTranslation = viewState.yTranslation;
        this.zTranslation = viewState.zTranslation;
        this.gone = viewState.gone;
        this.hidden = viewState.hidden;
    }

    public void initFrom(View view) {
        boolean z;
        this.alpha = view.getAlpha();
        this.yTranslation = view.getTranslationY();
        this.zTranslation = view.getTranslationZ();
        if (view.getVisibility() == 8) {
            z = true;
        } else {
            z = false;
        }
        this.gone = z;
        this.hidden = false;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ViewState:alpha=" + this.alpha + ", yTranslation=" + this.yTranslation + ", zTranslation=" + this.zTranslation + ", gone=" + this.gone + ", hidden=" + this.hidden);
    }
}
