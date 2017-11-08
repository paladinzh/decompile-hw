package com.huawei.watermark.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.util.WMLocationService;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.report.HwWatermarkReporter;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WMLocationEditor {
    private List<String> mAddresses;
    private View mBackView;
    private View mClearButton;
    private Context mContext;
    private Dialog mDialog;
    private EditText mEditText;
    private Runnable mExitEditRunnable = new Runnable() {
        public void run() {
            if (WMLocationEditor.this.mDialog != null) {
                WMLocationEditor.this.mDialog.dismiss();
            }
            ((InputMethodManager) WMLocationEditor.this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(WMLocationEditor.this.mWMEditLayout.getWindowToken(), 2);
        }
    };
    private ListView mListView;
    private OnTextChangedListener mListener;
    private LocationUpdateCallback mLocationUpdateCallback = new LocationUpdateCallback() {
        public void onAddressReport(List<Address> addresses) {
            WMLocationEditor.this.mAddresses = WMLocationService.toStringArray(addresses, WMLocationEditor.this.mContext);
            WMLocationEditor.this.mListView.post(new Runnable() {
                public void run() {
                    WMLocationEditor.this.initListView(WMLocationEditor.this.mAddresses, WMLocationEditor.this.mListView, null);
                }
            });
        }
    };
    LogicDelegate mLogicDelegate;
    private Runnable mPrepareEditRunnable = new Runnable() {
        public void run() {
            WMLocationEditor.this.mEditText.selectAll();
            WMLocationEditor.this.mEditText.requestFocus();
            ((InputMethodManager) WMLocationEditor.this.mContext.getSystemService("input_method")).toggleSoftInput(0, 2);
        }
    };
    private View mSubmitButton;
    private View mWMEditLayout;

    public interface OnTextChangedListener {
        void onTextChanged(String str);
    }

    private static class LocationListViewAdapter extends SimpleAdapter {
        private LayoutInflater mInflater;

        public LocationListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ViewHolder holder = new ViewHolder();
                convertView = this.mInflater.inflate(WMResourceUtil.getLayoutId(parent.getContext(), "wm_jar_location_edit_item"), parent, false);
                holder.location_value = (TextView) convertView.findViewById(WMResourceUtil.getId(parent.getContext(), "location_value"));
                convertView.setTag(holder);
            }
            ((ViewHolder) convertView.getTag()).location_value.setText(((Map) getItem(position)).get("location_value").toString());
            return convertView;
        }
    }

    private static class ViewHolder {
        private TextView location_value;

        private ViewHolder() {
        }
    }

    public WMLocationEditor(Context context, String input, boolean showWhenLocked, OnTextChangedListener mListener, LogicDelegate logicDelegate) {
        this.mListener = mListener;
        this.mLogicDelegate = logicDelegate;
        initEditView(context, input, showWhenLocked);
        this.mLogicDelegate.addLocationUpdateCallback(this.mLocationUpdateCallback);
    }

    private void initEditView(Context context, String input, boolean showWhenLocked) {
        this.mContext = context;
        this.mDialog = new Dialog(context, WMResourceUtil.getStyleId(context, "wm_jar_fullscreen_inputDialogTheme"));
        this.mDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode != 4) {
                    return false;
                }
                WMLocationEditor.this.hide();
                return true;
            }
        });
        this.mDialog.setContentView(WMResourceUtil.getLayoutId(context, "wm_jar_location_edit"));
        Window dialogWindow = this.mDialog.getWindow();
        LayoutParams lp = dialogWindow.getAttributes();
        lp.width = -1;
        lp.height = -1;
        if (showWhenLocked) {
            lp.flags |= 524288;
        }
        dialogWindow.setAttributes(lp);
        this.mDialog.show();
        initializeViews();
        if (!WMStringUtil.isEmptyString(input)) {
            this.mEditText.setText(input);
        }
        this.mEditText.post(this.mPrepareEditRunnable);
        this.mWMEditLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WMLocationEditor.this.hide();
            }
        });
        this.mLogicDelegate.setFullScreenViewShowStatus(true);
    }

    private void hide() {
        hide(true);
    }

    private void hide(boolean shouldHide) {
        if (shouldHide) {
            this.mExitEditRunnable.run();
        }
        this.mLogicDelegate.setFullScreenViewShowStatus(false);
    }

    private void initializeViews() {
        this.mListView = (ListView) this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "list_view_location"));
        this.mClearButton = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_clear"));
        this.mSubmitButton = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_submit"));
        this.mSubmitButton.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_ok));
        this.mBackView = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_back"));
        this.mBackView.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_review_cancel));
        this.mEditText = (EditText) this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "edit_text"));
        WMStringUtil.setEditTextStringStyle(this.mEditText);
        this.mWMEditLayout = this.mDialog.findViewById(WMResourceUtil.getId(this.mContext, "water_mark_location_edit"));
        this.mListView.setVerticalFadingEdgeEnabled(true);
        this.mListView.setFadingEdgeLength(30);
        this.mClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WMLocationEditor.this.mEditText.setText("");
            }
        });
        this.mSubmitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwWatermarkReporter.reportHwWatermarkEdit(WMLocationEditor.this.mContext);
                if (WMLocationEditor.this.mListener != null) {
                    WMLocationEditor.this.mListener.onTextChanged(WMLocationEditor.this.mEditText.getText().toString());
                }
                WMLocationEditor.this.hide();
            }
        });
        this.mBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WMLocationEditor.this.hide();
            }
        });
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WMLocationEditor.this.mClearButton.setVisibility(WMStringUtil.isEmptyString(s.toString()) ? 8 : 0);
                if (!WMCollectionUtil.isEmptyCollection(WMLocationEditor.this.mAddresses)) {
                    WMLocationEditor.this.initListView(WMLocationEditor.this.mAddresses, WMLocationEditor.this.mListView, s.toString());
                }
                if (WMLocationEditor.this.mContext != null) {
                    ((Activity) WMLocationEditor.this.mContext).onUserInteraction();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initListView(List<String> addrs, ListView mListView, String filter) {
        final List<HashMap<String, String>> listItem = genListViewData(addrs, filter);
        mListView.setAdapter(new LocationListViewAdapter(this.mContext, listItem, WMResourceUtil.getLayoutId(this.mContext, "wm_jar_location_edit_item"), new String[]{"location_value"}, new int[]{WMResourceUtil.getId(this.mContext, "location_value")}));
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (WMLocationEditor.this.mListener != null) {
                    WMLocationEditor.this.mListener.onTextChanged((String) ((HashMap) listItem.get(position)).get("location_value"));
                }
                WMLocationEditor.this.hide();
                HwWatermarkReporter.reportHwWatermarkEdit(WMLocationEditor.this.mContext);
            }
        });
    }

    private List<HashMap<String, String>> genListViewData(List<String> address, String filter) {
        if (WMCollectionUtil.isEmptyCollection((Collection) address)) {
            return null;
        }
        List<HashMap<String, String>> listItem = new ArrayList();
        for (String addr : address) {
            HashMap<String, String> map = new HashMap();
            if (WMStringUtil.isEmptyString(filter) || addr.contains(filter)) {
                map.put("location_value", addr);
                listItem.add(map);
            }
        }
        return listItem;
    }

    public void pause() {
        if (this.mExitEditRunnable != null) {
            hide(this.mLogicDelegate.getShouldHideSoftKeyboard());
        }
    }
}
