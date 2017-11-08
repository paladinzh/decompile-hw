package ucd.ui.util;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;

public class GenerateColor {

    /* renamed from: ucd.ui.util.GenerateColor$1 */
    class AnonymousClass1 implements PaletteAsyncListener {
        private final /* synthetic */ OnGenerateColorListener val$onGenerateColorListener;

        AnonymousClass1(OnGenerateColorListener onGenerateColorListener) {
            this.val$onGenerateColorListener = onGenerateColorListener;
        }

        public void onGenerated(Palette palette) {
            int[] pColors = new int[]{-1, -1, -1, -16777216, -16777216, -16777216};
            if (palette != null) {
                pColors = new int[]{palette.getVibrantColor(-1), palette.getLightVibrantColor(-1), palette.getDarkVibrantColor(-1), palette.getDarkMutedColor(-16777216), palette.getMutedColor(-16777216), palette.getLightMutedColor(-16777216)};
            }
            if (this.val$onGenerateColorListener != null) {
                this.val$onGenerateColorListener.onGenerateColor(pColors);
            }
        }
    }

    public interface OnGenerateColorListener {
        void onGenerateColor(int[] iArr);
    }

    public static void generate(Bitmap bit, OnGenerateColorListener onGenerateColorListener) {
        Palette.from(bit).generate(new AnonymousClass1(onGenerateColorListener));
    }
}
