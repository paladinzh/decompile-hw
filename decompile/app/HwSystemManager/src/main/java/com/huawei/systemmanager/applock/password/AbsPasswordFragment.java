package com.huawei.systemmanager.applock.password;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.password.callback.PasswordInputClickListener;
import com.huawei.systemmanager.applock.password.callback.PasswordPadInputCallback;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public abstract class AbsPasswordFragment extends Fragment implements PasswordPadInputCallback {
    private static final int DELAY_TIME = 20;
    private static final String KEY = "key";
    private static final int MAX_PASSWORD_LENGTH = 4;
    private static final int MSG_INPUT_PASSWORD_FINISHED = 0;
    private static final String TAG = "AbsPasswordFragment";
    private static final float WIDTH_LAND_WEIGHT = 0.6666667f;
    private static final float WIDTH_PORT_WEIGHT = 0.75f;
    protected Context mAppContext = null;
    private ImageButton mDelBtn = null;
    private Button mEmptyBtn = null;
    private Handler mHandler = null;
    private View mIncludeDisplay;
    private View mIncludeInputNum;
    private View mInputFrame;
    private List<Button> mNumPadBtns = Lists.newArrayList();
    private StringBuffer mPasswordBuf = new StringBuffer();
    private List<ImageView> mPasswordViews = Lists.newArrayList();
    private TextView mPwdHintTv = null;
    protected Animation mShakeAnimation = null;

    protected abstract int getPasswordHint();

    protected abstract int getPwdFragmentLayoutId();

    protected abstract int getPwdFragmentTitle();

    protected abstract void inputFinished(String str);

    protected abstract boolean shouldShowForgetPwd();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppContext = getActivity().getApplicationContext();
        getActivity().getActionBar().setTitle(getPwdFragmentTitle());
        getActivity().setTitle(getPwdFragmentTitle());
        initExtendDataOnCreate();
        registerHandler();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getPwdFragmentLayoutId(), container, false);
        initPasswordView(view);
        initExtendViewOnCreateView(view);
        this.mIncludeDisplay = view.findViewById(R.id.applock_password_display_layout);
        this.mIncludeInputNum = view.findViewById(R.id.app_lock_password_input_numPad);
        this.mInputFrame = view.findViewById(R.id.app_lock_password_view);
        this.mShakeAnimation = AnimationUtils.loadAnimation(this.mAppContext, R.anim.pwd_input_wrong_shake);
        if (getActivity().getResources().getConfiguration().orientation == 2) {
            initScreenOrientation(getActivity().getResources().getConfiguration());
        }
        return view;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreenOrientation(newConfig);
    }

    public void onResume() {
        super.onResume();
        updatePasswordHintMsg(getPasswordHint());
        clearPasswordBuf();
    }

    public void onPasswordInput(int num) {
        boolean z;
        if (num < 0 || 9 < num) {
            z = false;
        } else {
            z = true;
        }
        Preconditions.checkState(z, "Invalid input number");
        if (4 != this.mPasswordBuf.length()) {
            this.mPasswordBuf.append(num);
            updatePasswordShowViews();
            if (4 == this.mPasswordBuf.length()) {
                sendMessage(0, this.mPasswordBuf.toString());
            }
        }
    }

    public void onPasswordBackspace() {
        if (this.mPasswordBuf.length() != 0) {
            this.mPasswordBuf.deleteCharAt(this.mPasswordBuf.length() - 1);
            updatePasswordShowViews();
        }
    }

    protected void clearPasswordBuf() {
        this.mPasswordBuf = new StringBuffer();
        updatePasswordShowViews();
    }

    protected void updatePasswordHintMsg(int strId) {
        this.mPwdHintTv.setText(strId);
    }

    protected void updatePasswordHintMsg(String text) {
        this.mPwdHintTv.setText(text);
    }

    protected void setNumPadEnabled(boolean enabled) {
        for (int i = 0; i < this.mNumPadBtns.size(); i++) {
            ((Button) this.mNumPadBtns.get(i)).setEnabled(enabled);
        }
        this.mEmptyBtn.setEnabled(enabled);
        this.mDelBtn.setEnabled(enabled);
    }

    protected void shakePhone() {
        ((Vibrator) this.mAppContext.getSystemService("vibrator")).vibrate(500);
    }

    protected void shakeView() {
        setInputFrameBackGround(true);
        this.mShakeAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation a) {
            }

            public void onAnimationRepeat(Animation a) {
            }

            public void onAnimationEnd(Animation a) {
                AbsPasswordFragment.this.setInputFrameBackGround(false);
                AbsPasswordFragment.this.clearPasswordBuf();
            }
        });
        this.mInputFrame.startAnimation(this.mShakeAnimation);
    }

    private void initPasswordView(View view) {
        this.mPwdHintTv = (TextView) view.findViewById(R.id.app_lock_pin_auth_hint_text_view);
        initNumpadViewI(view, R.id.app_lock_password_num0);
        initNumpadViewI(view, R.id.app_lock_password_num1);
        initNumpadViewI(view, R.id.app_lock_password_num2);
        initNumpadViewI(view, R.id.app_lock_password_num3);
        initNumpadViewI(view, R.id.app_lock_password_num4);
        initNumpadViewI(view, R.id.app_lock_password_num5);
        initNumpadViewI(view, R.id.app_lock_password_num6);
        initNumpadViewI(view, R.id.app_lock_password_num7);
        initNumpadViewI(view, R.id.app_lock_password_num8);
        initNumpadViewI(view, R.id.app_lock_password_num9);
        initPasswordImageViewI(view, R.id.app_lock_password_img_0);
        initPasswordImageViewI(view, R.id.app_lock_password_img_1);
        initPasswordImageViewI(view, R.id.app_lock_password_img_2);
        initPasswordImageViewI(view, R.id.app_lock_password_img_3);
        this.mEmptyBtn = (Button) view.findViewById(R.id.app_lock_password_empty);
        this.mDelBtn = (ImageButton) view.findViewById(R.id.app_lock_password_backspace);
        TextView button = (TextView) view.findViewById(R.id.password_forget);
        if (!shouldShowForgetPwd()) {
            button.setVisibility(8);
        }
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HsmStat.statE(Events.E_APPLOCK_FORGET_PWD_CLICKED);
                AbsPasswordFragment.this.startActivity(new Intent(AbsPasswordFragment.this.mAppContext, PasswordProtectVerifyActivity.class));
            }
        });
        for (int i = 0; i < this.mNumPadBtns.size(); i++) {
            ((Button) this.mNumPadBtns.get(i)).setText(String.valueOf(i));
            ((Button) this.mNumPadBtns.get(i)).setOnClickListener(new PasswordInputClickListener(this, i));
        }
        this.mDelBtn.setOnClickListener(new PasswordInputClickListener(this, -1));
    }

    private void initNumpadViewI(View view, int id) {
        this.mNumPadBtns.add((Button) view.findViewById(id));
    }

    private void initPasswordImageViewI(View view, int id) {
        this.mPasswordViews.add((ImageView) view.findViewById(id));
    }

    private void updatePasswordShowViews() {
        for (int i = 0; i < this.mPasswordBuf.length(); i++) {
            ((ImageView) this.mPasswordViews.get(i)).setImageResource(R.drawable.img_dot_password_app_lock);
        }
        for (int j = this.mPasswordBuf.length(); j < 4; j++) {
            ((ImageView) this.mPasswordViews.get(j)).setImageDrawable(null);
        }
    }

    private void registerHandler() {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        String pwd = msg.getData().getString("key");
                        HwLog.d(AbsPasswordFragment.TAG, "handle MSG_INPUT_PASSWORD_FINISHED message");
                        AbsPasswordFragment.this.inputFinished(pwd);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void sendMessage(int type, String data) {
        Message msg = this.mHandler.obtainMessage(type);
        Bundle bundle = new Bundle();
        bundle.putString("key", data);
        msg.setData(bundle);
        this.mHandler.sendMessageDelayed(msg, 20);
    }

    private void initScreenOrientation(Configuration newConfig) {
        Activity ac = getActivity();
        boolean island = newConfig.orientation == 2;
        LayoutParams displayParams = (LayoutParams) this.mIncludeDisplay.getLayoutParams();
        LinearLayout.LayoutParams inputNumParams = (LinearLayout.LayoutParams) this.mIncludeInputNum.getLayoutParams();
        LinearLayout.LayoutParams inputFrameParams = (LinearLayout.LayoutParams) this.mInputFrame.getLayoutParams();
        float deviceSize = HSMConst.getDeviceSize();
        if (island) {
            if (deviceSize >= HSMConst.DEVICE_SIZE_80) {
                inputNumParams.height = ac.getResources().getDimensionPixelOffset(R.dimen.applock_numpad_land_pad_height);
            } else {
                inputNumParams.height = ac.getResources().getDimensionPixelOffset(R.dimen.applock_numpad_land_phone_height);
            }
            inputFrameParams.weight = WIDTH_LAND_WEIGHT;
            inputNumParams.weight = WIDTH_LAND_WEIGHT;
            inputNumParams.bottomMargin = ac.getResources().getDimensionPixelOffset(R.dimen.applock_numpad_bottom);
            displayParams.topMargin = ac.getResources().getDimensionPixelOffset(R.dimen.applock_password_hint_margin_top_land);
            displayParams.addRule(13);
        } else {
            inputFrameParams.weight = 0.75f;
            inputNumParams.weight = 0.75f;
            inputNumParams.height = ac.getResources().getDimensionPixelOffset(R.dimen.applock_numpad_port_height);
            inputNumParams.bottomMargin = ac.getResources().getDimensionPixelOffset(R.dimen.applock_numpad_bottom);
            displayParams.topMargin = ac.getResources().getDimensionPixelOffset(R.dimen.applock_password_hint_margin_top);
            displayParams.removeRule(13);
        }
        this.mIncludeDisplay.setLayoutParams(displayParams);
        this.mIncludeInputNum.setLayoutParams(inputNumParams);
        this.mInputFrame.setLayoutParams(inputFrameParams);
    }

    protected void initExtendDataOnCreate() {
    }

    protected void initExtendViewOnCreateView(View view) {
    }

    protected void setInputFrameBackGround(boolean showError) {
        int error_status = showError ? 0 : 8;
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_error_0)).setVisibility(error_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_error_1)).setVisibility(error_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_error_2)).setVisibility(error_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_error_3)).setVisibility(error_status);
        int normal_status = showError ? 8 : 0;
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_0)).setVisibility(normal_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_1)).setVisibility(normal_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_2)).setVisibility(normal_status);
        ((ImageView) this.mInputFrame.findViewById(R.id.pwd_input_3)).setVisibility(normal_status);
    }
}
