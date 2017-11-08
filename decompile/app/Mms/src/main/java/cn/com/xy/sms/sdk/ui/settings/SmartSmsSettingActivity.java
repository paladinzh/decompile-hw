package cn.com.xy.sms.sdk.ui.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.dialog.UpdateTypeDialog;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwPreferenceActivity;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@SuppressLint({"NewApi"})
public class SmartSmsSettingActivity extends HwPreferenceActivity implements OnPreferenceClickListener {
    private static final int BUBBLE_RICH = 2;
    private static final int BUBBLE_SIMPLE = 1;
    public static final int DEFAULT_TYPE_INDEX = 1;
    public static final int PAGE_COUNT = 3;
    public static final int UPDATE_TYPE_ALL = 2;
    public static final int UPDATE_TYPE_CLOSE = 0;
    public static final int UPDATE_TYPE_WALAN = 1;
    private final int[] HELP_IMAGES = new int[]{R.drawable.duoqu_help_page01, R.drawable.duoqu_help_page02, R.drawable.duoqu_help_page03};
    private Preference mBubblePreference;
    private RadioButton mBubbleRadio;
    private String[] mBubbleStyles;
    private Switch mEnhanceSwitch;
    private LinearLayout mEnhanceSwitchLayout;
    private ArrayList<ImageView> mHelpImages = new ArrayList();
    private ArrayList<View> mHelpViews = new ArrayList();
    private NavigationSpotsView mNavigationSpotsView;
    private RadioButton mNormalRadio;
    private int mSelectedIndex;
    private Dialog mStyleDlg;
    private String[] mUndateTypes;
    private XyPreference mUpdatePreference;

    private static class ListViewTouchListener implements OnTouchListener {
        private ListViewTouchListener() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 2:
                    return true;
                default:
                    return false;
            }
        }
    }

    private class MyPageViewAdaptor extends PagerAdapter {
        public MyPageViewAdaptor(Context aContext) {
        }

        public int getCount() {
            return 3;
        }

        public boolean isViewFromObject(View aPageView, Object aObject) {
            return aPageView == ((RelativeLayout) aObject);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            SmartSmsSettingActivity.this.mSelectedIndex = position;
            if (((ImageView) SmartSmsSettingActivity.this.mHelpImages.get(position)).getDrawable() == null) {
                ((ImageView) SmartSmsSettingActivity.this.mHelpImages.get(position)).setImageResource(SmartSmsSettingActivity.this.HELP_IMAGES[position]);
            }
            container.addView((View) SmartSmsSettingActivity.this.mHelpViews.get(position));
            return SmartSmsSettingActivity.this.mHelpViews.get(position);
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duoqu_smart_setting_main);
        addPreferencesFromResource(R.xml.duoqu_smart_msm_settings_pref);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView smartsmsEnhanceTitle = (TextView) findViewById(R.id.duoqu_title);
        TextView smartsmsEnhanceSummary = (TextView) findViewById(R.id.duoqu_summary);
        this.mEnhanceSwitch = (Switch) findViewById(R.id.duoqu_switch);
        this.mEnhanceSwitchLayout = (LinearLayout) findViewById(R.id.duoqu_switch_preference_layout);
        smartsmsEnhanceTitle.setText(R.string.duoqu_smartsms_enhance_title_2);
        smartsmsEnhanceSummary.setText(R.string.duoqu_smartsms_enhance_summary_4);
        this.mBubblePreference = findPreference(SmartSmsSdkUtil.SMARTSMS_BUBBLE);
        this.mBubblePreference.setTitle(getResources().getString(R.string.duoqu_pre_bubble_style));
        this.mBubbleStyles = getResources().getStringArray(R.array.duoqu_bubble_style_arr);
        this.mUpdatePreference = (XyPreference) findPreference(SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE);
        this.mUpdatePreference.setTitle(getResources().getString(R.string.duoqu_pre_version_update_2));
        setAlgorithmVersion();
        this.mUndateTypes = getResources().getStringArray(R.array.duoqu_update_type_arr);
        initHelperView();
        setListener();
        getListView().setOnTouchListener(new ListViewTouchListener());
    }

    private void setAlgorithmVersion() {
        try {
            String algorithmVersion = ParseManager.getAlgorithmVerion();
            if (!StringUtils.isNull(algorithmVersion)) {
                algorithmVersion = HwMessageUtils.convertDateToVersion(new SimpleDateFormat("yyyyMMddHH").parse(algorithmVersion).getTime());
                this.mUpdatePreference.setSummary(getResources().getString(R.string.duoqu_current_version, new Object[]{algorithmVersion}));
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSettingActivity.setAlgorithmVersion error: " + e.getMessage(), e);
        }
    }

    protected void onResume() {
        this.mEnhanceSwitch.setChecked(SmartSmsSdkUtil.getEnhance(this));
        this.mBubblePreference.setSummary(this.mBubbleStyles[SmartSmsSdkUtil.getBubbleStyle(this)]);
        super.onResume();
    }

    private void initHelperView() {
        ViewPager mHelpPager = (ViewPager) findViewById(R.id.duoqu_help_page);
        this.mNavigationSpotsView = (NavigationSpotsView) findViewById(R.id.navigation_dots);
        mHelpPager.setOnPageChangeListener(new HelpPageChangeListener(this.mNavigationSpotsView));
        mHelpPager.setAdapter(new MyPageViewAdaptor(this));
        for (int i = 0; i < 3; i++) {
            View view = getLayoutInflater().inflate(R.layout.duoqu_help_open_pager, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.duoqu_help_graphic);
            if (this.mSelectedIndex == i) {
                imageView.setImageDrawable(getResources().getDrawable(this.HELP_IMAGES[i]));
            } else {
                imageView.setImageDrawable(null);
            }
            this.mHelpViews.add(view);
            this.mHelpImages.add(imageView);
        }
    }

    private void setListener() {
        this.mUpdatePreference.setOnPreferenceClickListener(this);
        this.mBubblePreference.setOnPreferenceClickListener(this);
        this.mEnhanceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                SmartSmsSettingActivity.this.changeSmartSmsEnhance(checked);
                SmartSmsSdkUtil.setNoShowAgain(SmartSmsSettingActivity.this, true);
            }
        });
        this.mEnhanceSwitchLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 1) {
                    SmartSmsSettingActivity.this.mEnhanceSwitch.performClick();
                    SmartSmsSdkUtil.setNoShowAgain(SmartSmsSettingActivity.this, true);
                }
                return false;
            }
        });
    }

    private void changeSmartSmsEnhance(boolean enhance) {
        String str;
        if (enhance) {
            str = "on";
        } else {
            str = "off";
        }
        StatisticalHelper.reportEvent(this, 2122, str);
        SmartSmsSdkUtil.setEnhance(this, enhance);
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (!"smartsms_enhance".equals(key)) {
            if (SmartSmsSdkUtil.SMARTSMS_BUBBLE.equals(key)) {
                StatisticalHelper.incrementReportCount(this, 2123);
                doBubbleStylePreferenceClick();
            } else if (SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE.equals(key)) {
                StatisticalHelper.incrementReportCount(this, 2126);
                doUpdateTypePreferenceClick();
            }
        }
        return false;
    }

    private void doBubbleStylePreferenceClick() {
        int selectId;
        if (this.mStyleDlg != null) {
            selectId = SmartSmsSdkUtil.getBubbleStyle(this);
            if (1 == selectId) {
                this.mNormalRadio.setChecked(true);
            } else if (2 == selectId) {
                this.mBubbleRadio.setChecked(true);
            }
            this.mStyleDlg.show();
            return;
        }
        View dialogView = LayoutInflater.from(this).inflate(R.layout.duoqu_bubble_style_dialog, null);
        this.mStyleDlg = new Builder(this).setView(dialogView).setTitle(R.string.duoqu_pre_bubble_style).setNegativeButton(R.string.duoqu_setting_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SmartSmsSettingActivity.this.mStyleDlg.dismiss();
            }
        }).create();
        this.mStyleDlg.setCanceledOnTouchOutside(false);
        RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.duoqu_style);
        this.mNormalRadio = (RadioButton) dialogView.findViewById(R.id.duoqu_normal_style);
        this.mBubbleRadio = (RadioButton) dialogView.findViewById(R.id.duoqu_bubble_style);
        ImageView normalImage = (ImageView) dialogView.findViewById(R.id.duoqu_normal_style_img);
        ImageView bubbleImage = (ImageView) dialogView.findViewById(R.id.duoqu_bubble_style_img);
        selectId = SmartSmsSdkUtil.getBubbleStyle(this);
        if (1 == selectId) {
            this.mNormalRadio.setChecked(true);
        } else if (2 == selectId) {
            this.mBubbleRadio.setChecked(true);
        }
        View.OnClickListener styleClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.duoqu_normal_style) {
                    StatisticalHelper.incrementReportCount(SmartSmsSettingActivity.this, 2124);
                    SmartSmsSdkUtil.setBubbleStyle(SmartSmsSettingActivity.this, 1);
                    SmartSmsSettingActivity.this.mBubblePreference.setSummary(SmartSmsSettingActivity.this.mBubbleStyles[1]);
                } else if (id == R.id.duoqu_bubble_style) {
                    StatisticalHelper.incrementReportCount(SmartSmsSettingActivity.this, 2125);
                    SmartSmsSdkUtil.setBubbleStyle(SmartSmsSettingActivity.this, 2);
                    SmartSmsSettingActivity.this.mBubblePreference.setSummary(SmartSmsSettingActivity.this.mBubbleStyles[2]);
                } else if (id == R.id.duoqu_normal_style_img) {
                    StatisticalHelper.incrementReportCount(SmartSmsSettingActivity.this, 2124);
                    SmartSmsSdkUtil.setBubbleStyle(SmartSmsSettingActivity.this, 1);
                    SmartSmsSettingActivity.this.mBubblePreference.setSummary(SmartSmsSettingActivity.this.mBubbleStyles[1]);
                } else if (id == R.id.duoqu_bubble_style_img) {
                    StatisticalHelper.incrementReportCount(SmartSmsSettingActivity.this, 2125);
                    SmartSmsSdkUtil.setBubbleStyle(SmartSmsSettingActivity.this, 2);
                    SmartSmsSettingActivity.this.mBubblePreference.setSummary(SmartSmsSettingActivity.this.mBubbleStyles[2]);
                }
                SmartSmsSettingActivity.this.mStyleDlg.dismiss();
            }
        };
        radioGroup.setOnClickListener(styleClickListener);
        this.mNormalRadio.setOnClickListener(styleClickListener);
        this.mBubbleRadio.setOnClickListener(styleClickListener);
        normalImage.setOnClickListener(styleClickListener);
        bubbleImage.setOnClickListener(styleClickListener);
        this.mStyleDlg.show();
    }

    private void doUpdateTypePreferenceClick() {
        UpdateTypeDialog.showDialog(this, new XyCallBack() {
            public void execute(Object... obj) {
                if (obj != null && obj.length >= 1 && obj[0] != null && (obj[0] instanceof Integer)) {
                    SmartSmsSettingActivity.this.mUpdatePreference.setState(SmartSmsSettingActivity.getSummary(SmartSmsSettingActivity.this.getSumaryByType(((Integer) obj[0]).intValue())));
                }
            }
        });
    }

    private String getSumaryByType(int whichButton) {
        if (whichButton < 0 || whichButton >= this.mUndateTypes.length) {
            whichButton = 0;
        }
        return this.mUndateTypes[whichButton];
    }

    protected void onDestroy() {
        UpdateTypeDialog.dismissDialog();
        if (this.mStyleDlg != null) {
            this.mStyleDlg.dismiss();
            this.mStyleDlg = null;
        }
        super.onDestroy();
    }

    public static String getSummary(String str) {
        if (str == null) {
            return "";
        }
        if (str.indexOf("\n") != -1) {
            return str.substring(0, str.indexOf("\n"));
        }
        return str;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
