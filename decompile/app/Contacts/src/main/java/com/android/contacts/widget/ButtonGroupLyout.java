package com.android.contacts.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ButtonGroupLyout extends LinearLayout implements OnClickListener {
    private int COLOR_BUTTON_TEXT_NORMAL = getContext().getResources().getColor(R.color.call_log_primary_text_color);
    private int COLOR_BUTTON_TEXT_SELECT = getContext().getResources().getColor(R.color.contact_highlight_color);
    private Typeface TYPEFACE_ROBOT_MEDIUM = Typeface.create("HwChinese-medium", 0);
    private Typeface TYPEFACE_ROBOT_REGULAR = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
    private int backgroundcolor;
    private int mButtonType = -1;
    private TextView mEndButton;
    private ImageView mFirstDivider;
    private boolean mIsEndButtonShow = false;
    private TextView mLeftButton;
    private TextView mMiddleButton;
    private RadioButtonListener mRadioButtonListener;
    private TextView mRightButton;
    private ImageView mSecondDivider;
    private int mSelectedButtonColor;
    private ImageView mThreeDivider;

    public interface RadioButtonListener {
        void onRadioButtonClick(int i);
    }

    public void setRadioButtonListener(RadioButtonListener listener) {
        this.mRadioButtonListener = listener;
    }

    public ButtonGroupLyout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonGroupLyout(Context context) {
        super(context);
    }

    public void setButtonText(int left, int middle, int right, int end) {
        this.mLeftButton.setText(left);
        HwLog.i("ButtonGroupLyout", "leftButton text length is " + this.mLeftButton.getText().length());
        this.mMiddleButton.setText(middle);
        HwLog.i("ButtonGroupLyout", "middleButton text length is " + this.mMiddleButton.getText().length());
        this.mRightButton.setText(right);
        HwLog.i("ButtonGroupLyout", "rightButton text length is " + this.mRightButton.getText().length());
        this.mEndButton.setText(end);
        HwLog.i("ButtonGroupLyout", "endButton text length is " + this.mEndButton.getText().length());
    }

    public void setLeftButtonSelected() {
        this.mButtonType = 1;
        if (this.mLeftButton != null && this.mMiddleButton != null && this.mRightButton != null) {
            this.mLeftButton.setSelected(true);
            this.mMiddleButton.setSelected(false);
            this.mRightButton.setSelected(false);
            this.mEndButton.setSelected(false);
            initButtonText(1);
            setSelectedButtonBackGround(this.mLeftButton);
        }
    }

    public void setMiddleButtonSelected() {
        this.mButtonType = 2;
        if (this.mLeftButton != null && this.mMiddleButton != null && this.mRightButton != null) {
            this.mLeftButton.setSelected(false);
            this.mMiddleButton.setSelected(true);
            this.mRightButton.setSelected(false);
            this.mEndButton.setSelected(false);
            initButtonText(2);
            setSelectedButtonBackGround(this.mMiddleButton);
        }
    }

    public void setRightButtonSelected() {
        this.mButtonType = 3;
        if (this.mLeftButton != null && this.mMiddleButton != null && this.mRightButton != null) {
            this.mLeftButton.setSelected(false);
            this.mMiddleButton.setSelected(false);
            this.mRightButton.setSelected(true);
            this.mEndButton.setSelected(false);
            initButtonText(3);
            setSelectedButtonBackGround(this.mRightButton);
        }
    }

    public void setEndButtonSelected() {
        this.mButtonType = 4;
        if (this.mLeftButton != null && this.mEndButton != null && this.mMiddleButton != null && this.mRightButton != null) {
            this.mLeftButton.setSelected(false);
            this.mMiddleButton.setSelected(false);
            this.mRightButton.setSelected(false);
            this.mEndButton.setSelected(true);
            initButtonText(4);
            setSelectedButtonBackGround(this.mEndButton);
        }
    }

    public void setButtonSelectedType(int buttonSelectedType) {
        switch (buttonSelectedType) {
            case 1:
                setLeftButtonSelected();
                return;
            case 2:
                setMiddleButtonSelected();
                return;
            case 3:
                setRightButtonSelected();
                return;
            case 4:
                setEndButtonSelected();
                return;
            default:
                setLeftButtonSelected();
                return;
        }
    }

    public int getButtonSelectedType() {
        return this.mButtonType;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.backgroundcolor = ImmersionUtils.getControlColor(getResources());
        this.mSelectedButtonColor = getResources().getColor(R.color.call_log_button_group_layout_selected_color);
        this.mLeftButton = (TextView) findViewById(R.id.btn_status_left);
        this.mLeftButton.setOnClickListener(this);
        this.mMiddleButton = (TextView) findViewById(R.id.btn_status_middle);
        this.mMiddleButton.setOnClickListener(this);
        this.mRightButton = (TextView) findViewById(R.id.btn_status_right);
        this.mRightButton.setOnClickListener(this);
        this.mEndButton = (TextView) findViewById(R.id.btn_status_end);
        this.mEndButton.setOnClickListener(this);
        this.mThreeDivider = (ImageView) findViewById(R.id.three_subtab_divider);
        this.mFirstDivider = (ImageView) findViewById(R.id.first_subtab_divider);
        this.mSecondDivider = (ImageView) findViewById(R.id.second_subtab_divider);
        if (CommonUtilMethods.isLayoutRTL()) {
            if (getResources().getConfiguration().orientation == 2) {
                setLayoutDirection(1);
            }
            this.mLeftButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_right_seletor));
            this.mRightButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_left_seletor));
            this.mEndButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_left_seletor));
        }
        initButtonGroupLayoutColor();
        if (this.backgroundcolor != 0) {
            setBackgroundColorForMultiColorTheme(this.mLeftButton, this.backgroundcolor);
            setBackgroundColorForMultiColorTheme(this.mMiddleButton, this.backgroundcolor);
            setBackgroundColorForMultiColorTheme(this.mRightButton, this.backgroundcolor);
            setBackgroundColorForMultiColorTheme(this.mEndButton, this.backgroundcolor);
            setBackgroundColorForMultiColorTheme(this.mFirstDivider, this.backgroundcolor);
            setBackgroundColorForMultiColorTheme(this.mSecondDivider, this.backgroundcolor);
        }
        if (ImmersionUtils.getImmersionStyle(getContext()) == 0) {
            int color = ImmersionUtils.getUserDefinedColor(getContext(), 256);
            if (color != -1) {
                this.mLeftButton.setTextColor(color);
                this.mMiddleButton.setTextColor(color);
                this.mRightButton.setTextColor(color);
            }
        }
        setLeftButtonSelected();
    }

    public void refreshFilterButton(boolean showVM) {
        HwLog.i("ButtonGroupLyout", "refreshFilterButton showVM: " + showVM);
        if (showVM != this.mIsEndButtonShow) {
            this.mIsEndButtonShow = showVM;
            if (showVM) {
                this.mRightButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_middle_seletor));
                this.mEndButton.setVisibility(0);
                this.mThreeDivider.setVisibility(0);
                this.mLeftButton.setEllipsize(TruncateAt.MARQUEE);
                this.mMiddleButton.setEllipsize(TruncateAt.MARQUEE);
                this.mRightButton.setEllipsize(TruncateAt.MARQUEE);
                this.mEndButton.setEllipsize(TruncateAt.MARQUEE);
            } else {
                if (CommonUtilMethods.isLayoutRTL()) {
                    this.mRightButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_left_seletor));
                } else {
                    this.mRightButton.setBackground(getResources().getDrawable(R.drawable.contacts_radio_button_right_seletor));
                }
                this.mEndButton.setVisibility(8);
                this.mThreeDivider.setVisibility(8);
                this.mLeftButton.setEllipsize(TruncateAt.END);
                this.mMiddleButton.setEllipsize(TruncateAt.END);
                this.mRightButton.setEllipsize(TruncateAt.END);
                if (this.mButtonType == 4) {
                    setLeftButtonSelected();
                    if (this.mRadioButtonListener != null) {
                        this.mRadioButtonListener.onRadioButtonClick(1);
                    }
                }
            }
            initButtonGroupLayoutColor();
        }
    }

    public void onClick(View v) {
        if (this.mRadioButtonListener == null) {
            if (HwLog.HWDBG) {
                HwLog.d("ButtonGroupLyout", "ButtonGroupLyout == null");
            }
            return;
        }
        switch (v.getId()) {
            case R.id.btn_status_left:
                this.mRadioButtonListener.onRadioButtonClick(1);
                setLeftButtonSelected();
                break;
            case R.id.btn_status_middle:
                this.mRadioButtonListener.onRadioButtonClick(2);
                setMiddleButtonSelected();
                break;
            case R.id.btn_status_right:
                this.mRadioButtonListener.onRadioButtonClick(3);
                setRightButtonSelected();
                break;
            case R.id.btn_status_end:
                StatisticalHelper.report(5035);
                this.mRadioButtonListener.onRadioButtonClick(4);
                setEndButtonSelected();
                break;
        }
    }

    private void setSelectedButtonBackGround(TextView button) {
        Drawable drawable = button.getBackground();
        drawable.setTint(this.mSelectedButtonColor);
        button.setBackground(drawable);
    }

    private void initButtonGroupLayoutColor() {
        Drawable leftDrawable = this.mLeftButton.getBackground();
        leftDrawable.setTint(this.mSelectedButtonColor);
        this.mLeftButton.setBackground(leftDrawable);
        Drawable middleDrawable = this.mMiddleButton.getBackground();
        middleDrawable.setTint(this.mSelectedButtonColor);
        this.mMiddleButton.setBackground(middleDrawable);
        Drawable rightDrawable = this.mRightButton.getBackground();
        rightDrawable.setTint(this.mSelectedButtonColor);
        this.mRightButton.setBackground(rightDrawable);
        Drawable endDrawable = this.mEndButton.getBackground();
        endDrawable.setTint(this.mSelectedButtonColor);
        this.mEndButton.setBackground(endDrawable);
        Drawable firstDrawable = this.mFirstDivider.getBackground();
        firstDrawable.setTint(this.mSelectedButtonColor);
        this.mFirstDivider.setBackground(firstDrawable);
        Drawable secondDrawable = this.mSecondDivider.getBackground();
        secondDrawable.setTint(this.mSelectedButtonColor);
        this.mSecondDivider.setBackground(secondDrawable);
        Drawable threeDrawable = this.mThreeDivider.getBackground();
        threeDrawable.setTint(this.mSelectedButtonColor);
        this.mThreeDivider.setBackground(threeDrawable);
    }

    private void setBackgroundColorForMultiColorTheme(TextView button, int color) {
        Drawable drawable = button.getBackground();
        drawable.setTint(color);
        button.setBackground(drawable);
    }

    private void setBackgroundColorForMultiColorTheme(ImageView imageView, int color) {
        Drawable drawable = imageView.getBackground();
        drawable.setTint(color);
        imageView.setBackground(drawable);
    }

    private void initButtonText(int selectButton) {
        if (this.mLeftButton != null && this.mMiddleButton != null && this.mRightButton != null) {
            switch (selectButton) {
                case 1:
                    setTextColorAndTypeface(this.mLeftButton, this.COLOR_BUTTON_TEXT_SELECT, this.TYPEFACE_ROBOT_MEDIUM);
                    setTextColorAndTypeface(new TextView[]{this.mMiddleButton, this.mRightButton, this.mEndButton}, this.COLOR_BUTTON_TEXT_NORMAL, this.TYPEFACE_ROBOT_REGULAR);
                    break;
                case 2:
                    setTextColorAndTypeface(this.mMiddleButton, this.COLOR_BUTTON_TEXT_SELECT, this.TYPEFACE_ROBOT_MEDIUM);
                    setTextColorAndTypeface(new TextView[]{this.mLeftButton, this.mRightButton, this.mEndButton}, this.COLOR_BUTTON_TEXT_NORMAL, this.TYPEFACE_ROBOT_REGULAR);
                    break;
                case 3:
                    setTextColorAndTypeface(this.mRightButton, this.COLOR_BUTTON_TEXT_SELECT, this.TYPEFACE_ROBOT_MEDIUM);
                    setTextColorAndTypeface(new TextView[]{this.mLeftButton, this.mMiddleButton, this.mEndButton}, this.COLOR_BUTTON_TEXT_NORMAL, this.TYPEFACE_ROBOT_REGULAR);
                    break;
                case 4:
                    setTextColorAndTypeface(this.mEndButton, this.COLOR_BUTTON_TEXT_SELECT, this.TYPEFACE_ROBOT_MEDIUM);
                    setTextColorAndTypeface(new TextView[]{this.mLeftButton, this.mMiddleButton, this.mRightButton}, this.COLOR_BUTTON_TEXT_NORMAL, this.TYPEFACE_ROBOT_REGULAR);
                    break;
            }
        }
    }

    private void setTextColorAndTypeface(TextView button, int color, Typeface typeface) {
        if (button != null) {
            button.setTextColor(color);
            button.setTypeface(typeface);
        }
    }

    private void setTextColorAndTypeface(TextView[] buttons, int color, Typeface typeface) {
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] != null) {
                    buttons[i].setTextColor(color);
                    buttons[i].setTypeface(typeface);
                }
            }
        }
    }
}
