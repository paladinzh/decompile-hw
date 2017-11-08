package com.android.mms.ui;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwListFragment;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import huawei.android.widget.CounterTextLayout;
import java.util.ArrayList;

public class CommonPhraseFragment extends HwListFragment {
    private Editor editor = null;
    private AlertDialog mAddDialog = null;
    private EditTextWithSmiley mAddText = null;
    private OnClickListener mCancelListener = new MyDialogCancelOnClickListener();
    private CounterTextLayout mCounterCommonModifyError;
    private AlertDialog mDeleteDialog = null;
    private ItemOnClickListener mDeleteListener = new ItemOnClickListener(this) {
        public void onClick(DialogInterface dialog, int which) {
            this.mPhraseAdatper.remove((CharSequence) this.mPhraseAdatper.getItem(this.position));
            this.editor.clear();
            this.editor.putInt("LINE_NUMBERS", this.mPhraseAdatper.getCount());
            this.editor.putString("FIRST_USE", "NO");
            for (int j = 0; j < this.mPhraseAdatper.getCount(); j++) {
                this.editor.putString("LINE" + j, ((CharSequence) this.mPhraseAdatper.getItem(j)).toString());
            }
            this.editor.commit();
            dialog.dismiss();
        }
    };
    private View mFooterView = null;
    private boolean mIsComeFromSetting = true;
    MenuEx mMenuEx;
    private AlertDialog mModifyDialog = null;
    private EditTextWithSmiley mModifyEditText = null;
    private ItemOnClickListener mModifyListener = new ItemOnClickListener(this) {
        public void onClick(DialogInterface dialog, int which) {
            String value = this.mModifyEditText.getText().toString().trim();
            if (value.length() > 0) {
                this.mPhraseAdatper.remove((CharSequence) this.mPhraseAdatper.getItem(this.position));
                this.mPhraseAdatper.insert(SmileyParser.getInstance().addSmileySpans(value, SMILEY_TYPE.LIST_TEXTVIEW), this.position);
                this.editor.putString("LINE" + this.position, value);
            } else {
                this.deletePhrase(this.position);
            }
            this.editor.commit();
            dialog.dismiss();
        }
    };
    RelativeLayout mNoMessageLayout = null;
    LinearLayout mNoMessageSuper = null;
    private ArrayAdapter<CharSequence> mPhraseAdatper = null;

    private abstract class ItemOnClickListener implements OnClickListener {
        protected int position;

        private ItemOnClickListener() {
        }

        public void setPositon(int position) {
            this.position = position;
        }
    }

    private static abstract class MyTextWatcher implements TextWatcher {
        private Button mButton;

        protected abstract AlertDialog getDialog();

        protected abstract EditText getEditText();

        private MyTextWatcher() {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            boolean isEnabled = !s.toString().matches("\\s*");
            if (this.mButton == null) {
                this.mButton = getDialog().getButton(-1);
            }
            if (!(this.mButton == null || this.mButton.isEnabled() == isEnabled)) {
                this.mButton.setFocusable(isEnabled);
            }
            if (this.mButton != null) {
                this.mButton.setEnabled(isEnabled);
            }
            if (getEditText() != null) {
            }
        }
    }

    private class MenuEx extends EmuiMenu {
        public MenuEx() {
            super(null);
        }

        public void setMenuEnabled(boolean enabled) {
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onCreateOptionsMenu() {
            addMenu(278925338, R.string.menu_add, getDrawableId(278927460, CommonPhraseFragment.this.isInLandscape()));
            return true;
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 16908332:
                    CommonPhraseFragment.this.getController().setResult(CommonPhraseFragment.this, 0, new Intent());
                    CommonPhraseFragment.this.finishSelf(false);
                    break;
                case 278925338:
                    StatisticalHelper.incrementReportCount(CommonPhraseFragment.this.getContext(), 2178);
                    CommonPhraseFragment.this.mAddDialog.show();
                    CommonPhraseFragment.this.mAddText.setText("");
                    break;
            }
            return true;
        }

        void resetOptionsMenu() {
            EmuiMenu.resetMenu(this.mOptionMenu, 278925338, R.string.menu_add, ResEx.self().getStateListDrawable(CommonPhraseFragment.this.getContext(), getDrawableId(278927460, CommonPhraseFragment.this.isInLandscape())));
        }
    }

    private static class MyDialogCancelOnClickListener implements OnClickListener {
        private MyDialogCancelOnClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_phrase_activity, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mMenuEx = new MenuEx();
        this.mMenuEx.setContext(getContext());
        this.mMenuEx.setMenuEnabled(true);
        this.mPhraseAdatper = new ArrayAdapter(getContext(), R.layout.common_phrase_list_item, R.id.text);
        setListAdapter(this.mPhraseAdatper);
        getListView().setOnCreateContextMenuListener(this);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        if (this.mDeleteDialog == null) {
            View contents = View.inflate(getActivity(), R.layout.delete_thread_dialog_view, null);
            ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(R.string.phrase_delete_confirm);
            Builder builder = new Builder(getContext());
            builder.setView(contents);
            builder.setIcon(17301543);
            builder.setPositiveButton(R.string.delete, this.mDeleteListener);
            builder.setNegativeButton(R.string.no, this.mCancelListener);
            this.mDeleteDialog = builder.create();
        }
        if (this.mModifyDialog == null) {
            LinearLayout modifyTextDialogView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.common_phrase_modify_item, null);
            this.mModifyEditText = (EditTextWithSmiley) modifyTextDialogView.findViewById(R.id.common_modify);
            this.mCounterCommonModifyError = (CounterTextLayout) modifyTextDialogView.findViewById(R.id.common_modify_counter_error);
            this.mCounterCommonModifyError.setMaxLength(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE);
            this.mModifyEditText.addTextChangedListener(new MyTextWatcher() {
                protected AlertDialog getDialog() {
                    return CommonPhraseFragment.this.mModifyDialog;
                }

                protected EditText getEditText() {
                    return CommonPhraseFragment.this.mModifyEditText;
                }
            });
            builder = new Builder(getContext());
            builder.setTitle(R.string.modify_phrase);
            builder.setIcon(17301659);
            builder.setView(modifyTextDialogView);
            builder.setPositiveButton(R.string.yes, this.mModifyListener);
            builder.setNegativeButton(17039360, this.mCancelListener);
            this.mModifyDialog = builder.create();
            this.mModifyDialog.getWindow().setSoftInputMode(37);
        }
        if (this.mAddDialog == null) {
            LinearLayout addTextDialogView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.common_phrase_modify_item, null);
            this.mAddText = (EditTextWithSmiley) addTextDialogView.findViewById(R.id.common_modify);
            this.mCounterCommonModifyError = (CounterTextLayout) addTextDialogView.findViewById(R.id.common_modify_counter_error);
            this.mCounterCommonModifyError.setMaxLength(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE);
            this.mAddText.addTextChangedListener(new MyTextWatcher() {
                protected AlertDialog getDialog() {
                    return CommonPhraseFragment.this.mAddDialog;
                }

                protected EditText getEditText() {
                    return CommonPhraseFragment.this.mAddText;
                }
            });
            builder = new Builder(getContext());
            builder.setIcon(17301659);
            builder.setTitle(R.string.Create_phrase);
            builder.setView(addTextDialogView);
            builder.setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    StatisticalHelper.incrementReportCount(CommonPhraseFragment.this.getContext(), 2179);
                    String value = CommonPhraseFragment.this.mAddText.getText().toString().trim();
                    if (!TextUtils.isEmpty(value)) {
                        CommonPhraseFragment.this.mPhraseAdatper.insert(SmileyParser.getInstance().addSmileySpans(value, SMILEY_TYPE.LIST_TEXTVIEW), 0);
                        CommonPhraseFragment.this.editor.clear();
                        CommonPhraseFragment.this.editor.putInt("LINE_NUMBERS", CommonPhraseFragment.this.mPhraseAdatper.getCount());
                        CommonPhraseFragment.this.editor.putString("FIRST_USE", "NO");
                        for (int j = 0; j < CommonPhraseFragment.this.mPhraseAdatper.getCount(); j++) {
                            CommonPhraseFragment.this.editor.putString("LINE" + j, ((CharSequence) CommonPhraseFragment.this.mPhraseAdatper.getItem(j)).toString());
                        }
                        CommonPhraseFragment.this.editor.commit();
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton(17039360, this.mCancelListener);
            this.mAddDialog = builder.create();
            this.mAddDialog.getWindow().setSoftInputMode(37);
        }
        this.mNoMessageLayout = (RelativeLayout) getView().findViewById(R.id.phrase_nomessage_image_layout);
        this.mNoMessageSuper = (LinearLayout) getView().findViewById(16908292);
        if (this.mNoMessageSuper.getVisibility() == 0) {
            resetImageLayout(getResources().getConfiguration().orientation);
        }
        ListView list = (ListView) getView().findViewById(16908298);
        this.mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.divider_footer_view, list, false);
        list.setFooterDividersEnabled(false);
        list.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null) {
            boolean isLandscape = newConfig == null ? 2 == getResources().getConfiguration().orientation : 2 == newConfig.orientation;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!isLandscape || isInMultiWindowMode()) ? (int) getResources().getDimension(R.dimen.toolbar_footer_height) : 0;
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    public void onResume() {
        super.onResume();
        SharedPreferences sp = getPreferences(getContext());
        if (sp != null) {
            ArrayList<CharSequence> list = prepare(getResources(), sp);
            this.editor = sp.edit();
            this.mPhraseAdatper.clear();
            for (CharSequence s : list) {
                this.mPhraseAdatper.add(s);
            }
            Intent intent = getIntent();
            if (intent.hasExtra("FROM_COMPOCE")) {
                Object res = intent.getExtra("FROM_COMPOCE");
                this.mIsComeFromSetting = res == null ? false : ((Boolean) res).booleanValue();
            }
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        if (this.mIsComeFromSetting) {
            this.mModifyListener.setPositon(position);
            this.mModifyDialog.show();
            this.mModifyEditText.setText("");
            this.mModifyEditText.append(SmileyParser.getInstance().addSmileySpans((CharSequence) this.mPhraseAdatper.getItem(position), SMILEY_TYPE.MESSAGE_EDITTEXT));
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("COMMON_PHRASE", (CharSequence) this.mPhraseAdatper.getItem(position));
        getController().setResult(this, -1, intent);
        finishSelf(false);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, 1, 0, R.string.menu_edit);
        menu.add(0, 2, 0, R.string.delete);
        menu.setHeaderTitle(R.string.dialogTitle_PhraseOptions_new);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenuEx.setOptionMenu(menu).onCreateOptionsMenu();
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        int position = info.position;
        switch (item.getItemId()) {
            case 1:
                this.mModifyListener.setPositon(position);
                this.mModifyDialog.show();
                this.mModifyEditText.setText("");
                this.mModifyEditText.append(SmileyParser.getInstance().addSmileySpans((CharSequence) this.mPhraseAdatper.getItem(position), SMILEY_TYPE.MESSAGE_EDITTEXT));
                return true;
            case 2:
                deletePhrase(position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deletePhrase(int position) {
        this.mDeleteListener.setPositon(position);
        this.mDeleteDialog.show();
        MessageUtils.setButtonTextColor(this.mDeleteDialog, -1, getResources().getColor(R.drawable.text_color_red));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public static String getCommonPhrasePreferencesName(Context context) {
        String commonPhrasePreferencesName = null;
        try {
            commonPhrasePreferencesName = ActivityManagerNative.getDefault().getConfiguration().locale.toString();
        } catch (RemoteException e) {
            MLog.e("CommonPhraseFragment", "getCommonPhrasePreferencesName RemoteException");
        }
        return commonPhrasePreferencesName;
    }

    public static SharedPreferences getPreferences(Context context) {
        String commonPhrasePreferencesName = getCommonPhrasePreferencesName(context);
        if (TextUtils.isEmpty(commonPhrasePreferencesName)) {
            return null;
        }
        return context.getSharedPreferences(commonPhrasePreferencesName, 0);
    }

    public static ArrayList<CharSequence> prepare(Resources resources, SharedPreferences sp) {
        int i = 0;
        ArrayList<CharSequence> re = new ArrayList();
        if (sp != null) {
            String first = sp.getString("FIRST_USE", null);
            Editor editor = sp.edit();
            SmileyParser parser = SmileyParser.getInstance();
            if (first == null) {
                editor.putString("FIRST_USE", "NO");
                String[] defalutCommonPhrase = resources.getStringArray(R.array.default_common_phrase_hw);
                int length = defalutCommonPhrase.length;
                while (i < length) {
                    String s = defalutCommonPhrase[i];
                    editor.putString("LINE" + re.size(), s);
                    re.add(parser.addSmileySpans(s, SMILEY_TYPE.LIST_TEXTVIEW));
                    i++;
                }
                editor.putInt("LINE_NUMBERS", re.size());
                editor.commit();
            } else {
                int count = sp.getInt("LINE_NUMBERS", 0);
                for (int i2 = 0; i2 < count; i2++) {
                    re.add(parser.addSmileySpans(sp.getString("LINE" + i2, null), SMILEY_TYPE.LIST_TEXTVIEW));
                }
            }
        }
        return re;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mMenuEx != null) {
            this.mMenuEx.resetOptionsMenu();
        }
        updateFooterViewHeight(newConfig);
        resetImageLayout(newConfig.orientation);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDeleteDialog != null && this.mDeleteDialog.isShowing()) {
            this.mDeleteDialog.dismiss();
        }
        if (this.mModifyDialog != null && this.mModifyDialog.isShowing()) {
            this.mModifyDialog.dismiss();
        }
        if (this.mAddDialog != null && this.mAddDialog.isShowing()) {
            this.mAddDialog.dismiss();
        }
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
    }

    private void resetImageLayout(int orientation) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mNoMessageLayout.getLayoutParams();
        RelativeLayout.LayoutParams imageSuperParams = new RelativeLayout.LayoutParams(-2, -2);
        Resources resources = getResources();
        if (isInMultiWindowMode()) {
            layoutParams.topMargin = resources.getDimensionPixelOffset(R.dimen.mms_nomessageview_height_top_margin_multiwindow);
            imageSuperParams.addRule(13);
        } else {
            layoutParams.topMargin = resources.getDimensionPixelOffset(R.dimen.mms_nomessageview_height);
            imageSuperParams.addRule(14);
        }
        this.mNoMessageSuper.setLayoutParams(imageSuperParams);
        this.mNoMessageLayout.setLayoutParams(layoutParams);
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
