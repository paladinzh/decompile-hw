package android.support.v17.leanback.widget.picker;

public class PickerColumn {
    private int mCurrentValue;
    private String mLabelFormat;
    private int mMaxValue;
    private int mMinValue;
    private CharSequence[] mStaticLabels;

    public void setLabelFormat(String labelFormat) {
        this.mLabelFormat = labelFormat;
    }

    public void setStaticLabels(CharSequence[] labels) {
        this.mStaticLabels = labels;
    }

    public CharSequence getLabelFor(int value) {
        if (this.mStaticLabels != null) {
            return this.mStaticLabels[value];
        }
        return String.format(this.mLabelFormat, new Object[]{Integer.valueOf(value)});
    }

    public int getCurrentValue() {
        return this.mCurrentValue;
    }

    public void setCurrentValue(int value) {
        this.mCurrentValue = value;
    }

    public int getCount() {
        return (this.mMaxValue - this.mMinValue) + 1;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMinValue(int minValue) {
        this.mMinValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
    }
}
