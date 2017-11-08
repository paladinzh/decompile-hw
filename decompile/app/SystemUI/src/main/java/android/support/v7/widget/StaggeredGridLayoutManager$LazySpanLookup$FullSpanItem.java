package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

class StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem implements Parcelable {
    public static final Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> CREATOR = new Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem>() {
        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem createFromParcel(Parcel in) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(in);
        }

        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[] newArray(int size) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[size];
        }
    };
    int mGapDir;
    int[] mGapPerSpan;
    boolean mHasUnwantedGapAfter;
    int mPosition;

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(Parcel in) {
        boolean z = true;
        this.mPosition = in.readInt();
        this.mGapDir = in.readInt();
        if (in.readInt() != 1) {
            z = false;
        }
        this.mHasUnwantedGapAfter = z;
        int spanCount = in.readInt();
        if (spanCount > 0) {
            this.mGapPerSpan = new int[spanCount];
            in.readIntArray(this.mGapPerSpan);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(this.mPosition);
        dest.writeInt(this.mGapDir);
        if (this.mHasUnwantedGapAfter) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mGapPerSpan == null || this.mGapPerSpan.length <= 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mGapPerSpan.length);
        dest.writeIntArray(this.mGapPerSpan);
    }

    public String toString() {
        return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
    }
}
