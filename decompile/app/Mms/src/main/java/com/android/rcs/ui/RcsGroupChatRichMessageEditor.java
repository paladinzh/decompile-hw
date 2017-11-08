package com.android.rcs.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Calendars;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.VCalSmsMessage;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager.RcsGroupChatAttachmentThreadCallBack;
import com.android.mms.model.MediaModel;
import com.android.mms.ui.AttachmentTypeSelectorAdapter.AttachmentListItem;
import com.android.mms.ui.EditableSlides$RichMessageListener;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.SmileyFaceSelectorAdapter;
import com.android.mms.util.SignatureUtil;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class RcsGroupChatRichMessageEditor extends LinearLayout implements OnEditorActionListener, OnItemClickListener {
    private static final int[] CheckedStateSet = new int[]{16842908, 16842919};
    private List<Uri> mAttachmentMap;
    private ArrayList<JSONObject> mDraftListDatas;
    private RcsGroupChatComposeMessageFragment mFragment;
    private Handler mHandler;
    private boolean mHintState;
    private boolean mIsOriginalSize;
    private List<MediaModel> mListData;
    private EditableSlides$RichMessageListener mListener;
    private SmileyParser mParser;
    private int mPosition;
    private ArrayList<RcsGroupRichAttachmentListener> mRcsGroupRichAttachmentListeners;
    private ArrayList<Uri> mSendData;
    private EditTextWithSmiley mSmsEditorText;
    private ArrayList<String> mSourceBuilds;
    private final TextWatcher mTextEditorWatcher;
    private Toast mToast;

    public interface RcsGroupRichAttachmentListener {
        void onRcsGroupChatRichAttachmentChanged(int i);
    }

    private class RcsGroupChatRichAttachmentThreadCallBack implements RcsGroupChatAttachmentThreadCallBack {
        private RcsGroupChatRichAttachmentThreadCallBack() {
        }

        public void onModelFinish(int resultCode, Uri originalUri, MediaModel mediaModel, int type) {
            if (mediaModel != null) {
                RcsGroupChatRichMessageEditor.this.saveModelData(originalUri, mediaModel);
                RcsGroupChatRichMessageEditor.this.refreshHandlerSendMsg(originalUri, type);
            }
        }
    }

    private void addNewAttachment(Activity activity, int type, Uri uri) {
        AttachmentThreadManager.addAttachment(activity, type, uri, new RcsGroupChatRichAttachmentThreadCallBack());
    }

    public void refreshHandlerSendMsg(Uri originalUri, int type) {
        refreshMediaAttachment(type);
        Message msg = this.mHandler.obtainMessage();
        msg.obj = originalUri;
        msg.what = 101;
        this.mHandler.removeMessages(101);
        this.mHandler.sendMessageDelayed(msg, 100);
    }

    public void saveModelData(Uri originalUri, MediaModel mediaModel) {
        if (MmsConfig.compareWithMaxSlides(this.mListData.size())) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 20;
            this.mHandler.removeMessages(msg.what);
            this.mHandler.sendEmptyMessage(msg.what);
            return;
        }
        this.mAttachmentMap.add(originalUri);
        this.mListData.add(mediaModel);
    }

    private void removeModelData(int position) {
        this.mAttachmentMap.remove(position);
        this.mListData.remove(position);
    }

    public void viewAttach(MediaModel mm, Uri dataUri, String contentType, String fileName) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(1);
        intent.putExtra("android.intent.extra.TITLE", fileName);
        intent.setDataAndType(dataUri, contentType);
        if (!(TextUtils.isEmpty(contentType) || TextUtils.isEmpty(fileName) || !MessageUtils.isEndWithImageExtension(fileName))) {
            intent.setDataAndType(dataUri, "image/*");
        }
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            MLog.e("RcsGroupChatRichMessageEditor", "Unsupported Format,startActivity(intent) error,intent");
            MessageUtils.showErrorDialog(getContext(), getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
        }
    }

    public void removeData(Uri uri, int type) {
        for (int i = 0; i < this.mListData.size(); i++) {
            if (((MediaModel) this.mListData.get(i)).getUri().toString().equals(uri.toString())) {
                removeModelData(i);
                break;
            }
        }
        refreshHandlerSendMsg(uri, type);
    }

    public List<MediaModel> getMediaModelData() {
        return this.mListData;
    }

    public ArrayList<Uri> getUriData() {
        if (this.mSendData == null) {
            this.mSendData = new ArrayList();
        }
        this.mSendData.clear();
        if (this.mListData == null) {
            return null;
        }
        for (MediaModel m : this.mListData) {
            this.mSendData.add(m.getUri());
        }
        return this.mSendData;
    }

    public ArrayList<String> getSourceBuildsData() {
        if (this.mSourceBuilds == null) {
            this.mSourceBuilds = new ArrayList();
        }
        this.mSourceBuilds.clear();
        for (MediaModel m : this.mListData) {
            this.mSourceBuilds.add(m.getSourceBuild());
        }
        return this.mSourceBuilds;
    }

    public ArrayList<JSONObject> getDraftListDatas() {
        if (this.mDraftListDatas == null) {
            this.mDraftListDatas = new ArrayList();
        }
        this.mDraftListDatas.clear();
        if (this.mListData == null) {
            return null;
        }
        try {
            for (MediaModel m : this.mListData) {
                HashMap<String, String> locationMap = m.getLocationSource();
                JSONObject obj = new JSONObject();
                JSONObject objData = new JSONObject();
                if (locationMap != null) {
                    String locationTitle = (String) locationMap.get("title");
                    obj.put("title", locationTitle);
                    String locationSub = (String) locationMap.get("subtitle");
                    obj.put("subtitle", locationSub);
                    String latitude = (String) locationMap.get("latitude");
                    obj.put("latitude", latitude);
                    String longitude = (String) locationMap.get("longitude");
                    obj.put("longitude", longitude);
                    obj.put("locationinfo", locationTitle + "\n" + locationSub + "\n" + MessageUtils.getLocationWebLink(this.mFragment.getActivity()) + latitude + "," + longitude);
                }
                objData.put(m.getUri().toString(), obj.toString());
                this.mDraftListDatas.add(objData);
            }
            return this.mDraftListDatas;
        } catch (JSONException e) {
            MLog.e("RcsGroupChatRichMessageEditor", "getDraftListDatas occurs JSONException");
            return null;
        } catch (Exception e2) {
            MLog.e("RcsGroupChatRichMessageEditor", "getDraftListDatas occurs Exception");
            return null;
        }
    }

    public void setNewAttachment(Uri uri, int type) {
        addNewAttachment(this.mFragment.getActivity(), type, uri);
    }

    public void setNewAttachment(ArrayList<Uri> uriLists, int type) {
        if (uriLists == null || uriLists.size() == 0) {
            MLog.d("RcsGroupChatRichMessageEditor", "setNewAttachmnet uriLists is invisible");
            return;
        }
        for (int i = 0; i < uriLists.size(); i++) {
            final Uri uri = (Uri) uriLists.get(i);
            final String mimType = inferMimeTypeForUri(this.mFragment.getActivity(), uri);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (TextUtils.isEmpty(mimType)) {
                        RcsGroupChatRichMessageEditor.this.setNewAttachment(uri, 2);
                        return;
                    }
                    if (mimType.startsWith("video")) {
                        RcsGroupChatRichMessageEditor.this.setNewAttachment(uri, 5);
                    } else {
                        RcsGroupChatRichMessageEditor.this.setNewAttachment(uri, 2);
                    }
                }
            }, ((long) i) * 150);
        }
    }

    private String inferMimeTypeForUri(Context context, Uri uri) {
        String scheme = uri.getScheme();
        String type = null;
        if ("content".equals(scheme)) {
            type = context.getContentResolver().getType(uri);
        } else if ("file".equals(scheme)) {
            MLog.d("RcsGroupChatRichMessageEditor", "inferMimeTypeForUri->uri is file.");
        }
        if (type == null) {
            MLog.w("RcsGroupChatRichMessageEditor", "inferMimeTypeForUri->Unable to determine MIME type for uri=" + uri);
        }
        return type;
    }

    public long computeAddRecordSizeLimit() {
        return (long) RcsTransaction.getWarFileSizePermitedValue();
    }

    public RcsGroupChatRichMessageEditor(Context context) {
        this(context, null);
    }

    public RcsGroupChatRichMessageEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RcsGroupChatRichMessageEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPosition = -1;
        this.mParser = null;
        this.mHintState = false;
        this.mIsOriginalSize = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 20:
                        if (RcsGroupChatRichMessageEditor.this.mFragment != null) {
                            int maxSlides = MmsConfig.getMaxSlides();
                            Toast.makeText(RcsGroupChatRichMessageEditor.this.mFragment.getContext(), RcsGroupChatRichMessageEditor.this.mFragment.getContext().getResources().getQuantityString(R.plurals.too_many_attachments_Toast, maxSlides, new Object[]{Integer.valueOf(maxSlides), Integer.valueOf(maxSlides)}), 1).show();
                            return;
                        }
                        return;
                    case 100:
                        onTextChange((CharSequence) msg.obj, msg.arg1, msg.arg2);
                        return;
                    case 101:
                        if (msg.obj != null) {
                            RcsGroupChatRichMessageEditor.this.mListener.onContentChange();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }

            private void onTextChange(CharSequence s, int start, int before) {
                RcsGroupChatRichMessageEditor.this.mListener.onContentChange();
            }
        };
        this.mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Message msg = RcsGroupChatRichMessageEditor.this.mHandler.obtainMessage(100, start, before, s);
                RcsGroupChatRichMessageEditor.this.mHandler.removeMessages(100);
                RcsGroupChatRichMessageEditor.this.mHandler.sendMessageDelayed(msg, 100);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        this.mRcsGroupRichAttachmentListeners = new ArrayList();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        this.mAttachmentMap = Collections.synchronizedList(new ArrayList());
        this.mListData = Collections.synchronizedList(new ArrayList());
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mParser = SmileyParser.getInstance();
        this.mSmsEditorText = (EditTextWithSmiley) findViewById(R.id.embedded_text_editor);
        this.mSmsEditorText.setOnEditorActionListener(this);
        this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
        InputFilter inputFilter = new LengthFilter(MmsConfig.getMaxTextLimit()) {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dest == null) {
                    return null;
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                int length = dest.length();
                if (source != null) {
                    length += source.length();
                }
                if (length >= MmsConfig.getMaxTextLimit()) {
                    if (RcsGroupChatRichMessageEditor.this.mToast == null) {
                        RcsGroupChatRichMessageEditor.this.mToast = Toast.makeText(RcsGroupChatRichMessageEditor.this.getContext(), R.string.entered_too_many_characters, 0);
                    }
                    RcsGroupChatRichMessageEditor.this.mToast.show();
                }
                return result;
            }
        };
        this.mSmsEditorText.setFilters(new InputFilter[]{inputFilter});
        this.mSmsEditorText.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 && R.id.embedded_text_editor == v.getId() && RcsGroupChatRichMessageEditor.this.mSmsEditorText != null) {
                    RcsGroupChatRichMessageEditor.this.mSmsEditorText.setCursorVisible(true);
                }
                if (event.getAction() == 1) {
                    RcsGroupChatRichMessageEditor.this.mListener.onInputManagerShow();
                }
                return false;
            }
        });
        if (MmsConfig.isExtraHugeEnabled(getResources().getConfiguration().fontScale)) {
            this.mSmsEditorText.setTextSize(1, 26.1f);
        }
    }

    public CharSequence getText() {
        if (this.mSmsEditorText.getVisibility() == 0) {
            return this.mSmsEditorText.getText().toString();
        }
        return "";
    }

    public void setListener(EditableSlides$RichMessageListener l) {
        this.mListener = l;
    }

    public void onKeyboardStateChanged(boolean isSmsEnabled, boolean isKeyboardOpen, boolean isHintShowSendIm) {
        this.mHintState = isHintShowSendIm;
        onKeyboardStateChanged(isSmsEnabled, isKeyboardOpen);
        this.mHintState = false;
    }

    public void onKeyboardStateChanged(boolean isSmsEnabled, boolean isKeyboardOpen) {
        this.mSmsEditorText.setEnabled(isSmsEnabled);
        if (!isSmsEnabled) {
            this.mSmsEditorText.setFocusableInTouchMode(false);
            this.mSmsEditorText.setHint(R.string.sending_disabled_not_default_app);
        } else if (isKeyboardOpen) {
            this.mSmsEditorText.setFocusableInTouchMode(true);
            if (this.mHintState) {
                this.mSmsEditorText.setHint(R.string.type_to_compose_im_text_enter_to_send_new_rcs);
            } else {
                this.mSmsEditorText.setHint(R.string.type_to_compose_text_enter_to_send_new_sms);
            }
        } else {
            this.mSmsEditorText.setFocusable(false);
            this.mSmsEditorText.setHint(R.string.open_keyboard_to_compose_message);
        }
    }

    public void setText(CharSequence text) {
        if (text != null) {
            this.mSmsEditorText.setTextKeepState(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mSmsEditorText.setSelection(this.mSmsEditorText.length());
            return;
        }
        this.mSmsEditorText.setText("");
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(this.mSmsEditorText.getText().toString());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (MmsConfig.isExtraHugeEnabled(newConfig.fontScale)) {
            this.mSmsEditorText.setTextSize(1, 26.1f);
        }
    }

    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    private void deleteSmiley(EditTextWithSmiley textEditor) {
        if (textEditor.getSelectionStart() == 0 && textEditor.getText().length() == 0) {
            textEditor.doEmptyDelete();
        } else {
            textEditor.onKeyDown(67, new KeyEvent(0, 67));
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isDelete = true;
        AttachmentListItem item = (AttachmentListItem) ((SmileyFaceSelectorAdapter) parent.getAdapter()).getItem(position);
        String smiley = item.getTitle();
        int index = item.getCommand();
        EditTextWithSmiley textEditor = this.mSmsEditorText;
        if (!((index + 1) % 18 == 0 || index == SmileyParser.getSmileyResIds().length - 1)) {
            isDelete = false;
        }
        if (isDelete) {
            deleteSmiley(textEditor);
            return;
        }
        textEditor.getText().insert(textEditor.getSelectionEnd(), this.mParser.addSmileySpans(smiley, SMILEY_TYPE.MESSAGE_EDITTEXT));
        textEditor.getText().insert(textEditor.getSelectionEnd(), " ");
        textEditor.requestFocus();
    }

    public void resetAttachmentMap() {
        if (this.mAttachmentMap != null) {
            this.mAttachmentMap.clear();
        } else {
            this.mAttachmentMap = new ArrayList();
        }
        if (this.mListData != null) {
            this.mListData.clear();
        } else {
            this.mListData = new ArrayList();
        }
    }

    public void insertPhrase(CharSequence phrase) {
        EditText editor = null;
        if (this.mPosition == -1) {
            editor = this.mSmsEditorText;
        }
        if (editor != null) {
            editor.getText().insert(editor.getSelectionEnd(), phrase);
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (hasFocus() || isPressed()) {
            mergeDrawableStates(drawableState, CheckedStateSet);
        }
        return drawableState;
    }

    public void appendSignature(boolean isCheckAppendSignature) {
        String signature = SignatureUtil.getSignature(getContext(), "");
        if (isCheckAppendSignature && isContainSignature(this.mSmsEditorText.getText().toString(), signature)) {
            String temp = this.mSmsEditorText.getText().toString();
            if (temp.indexOf(signature) != -1) {
                this.mSmsEditorText.setText(this.mParser.addSmileySpans(temp.substring(0, temp.indexOf(signature)) + signature, SMILEY_TYPE.MESSAGE_EDITTEXT));
                this.mFragment.controlNeedSendComposing(false);
                this.mSmsEditorText.requestFocus();
            }
            return;
        }
        if (!TextUtils.isEmpty(signature)) {
            this.mSmsEditorText.setText(this.mParser.addSmileySpans(this.mSmsEditorText.getText() + System.getProperty("line.separator") + signature, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mFragment.controlNeedSendComposing(false);
            this.mSmsEditorText.requestFocus();
            this.mSmsEditorText.setSelection(0);
        }
    }

    private boolean isContainSignature(String content, String signature) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(signature) || !content.endsWith(signature)) {
            return false;
        }
        return true;
    }

    public boolean isOnlyContainsSignature() {
        String editString = getText().toString();
        Context context = getContext();
        if (TextUtils.isEmpty(editString) || context == null) {
            return false;
        }
        return SignatureUtil.deleteNewlineSymbol(editString).equals(SignatureUtil.getSignature(context, MmsConfig.getDefaultSignatureText()));
    }

    public void setEditTextFocus() {
        this.mSmsEditorText.requestFocus();
    }

    public void setFragment(RcsGroupChatComposeMessageFragment fragment) {
        this.mFragment = fragment;
    }

    public void insertVcalendarText(final ArrayList<Uri> uriList) {
        final ProgressDialog waitDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.wait));
        ThreadEx.execute(new Runnable() {
            public void run() {
                if (!RcsGroupChatRichMessageEditor.this.mFragment.isDetached()) {
                    String BIRTHDAY_ACCOUNT_TYPE = "com.android.huawei.birthday";
                    String SELECTION = "account_type = 'com.android.huawei.birthday'";
                    long birthdayCalendarId = -1;
                    Cursor cursor = null;
                    try {
                        cursor = SqliteWrapper.query(RcsGroupChatRichMessageEditor.this.mContext, Calendars.CONTENT_URI, new String[]{"_id"}, "account_type = 'com.android.huawei.birthday'", null, null);
                        if (cursor == null || !cursor.moveToFirst()) {
                            birthdayCalendarId = -1;
                        } else {
                            birthdayCalendarId = cursor.getLong(0);
                        }
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Exception e) {
                                MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText cursor close error ");
                            }
                        }
                    } catch (RuntimeException e2) {
                        MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText query. RuntimeException");
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Exception e3) {
                                MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText cursor close error ");
                            }
                        }
                    } catch (Exception e4) {
                        MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText query Exception ", (Throwable) e4);
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Exception e5) {
                                MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText cursor close error ");
                            }
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Exception e6) {
                                MLog.e("RcsGroupChatRichMessageEditor", "insertVcalendarText cursor close error ");
                            }
                        }
                    }
                    final String vCalText = VCalSmsMessage.getVCalText(uriList, RcsGroupChatRichMessageEditor.this.mContext, birthdayCalendarId);
                    Handler -get2 = RcsGroupChatRichMessageEditor.this.mHandler;
                    final ProgressDialog progressDialog = waitDialog;
                    -get2.post(new Runnable() {
                        public void run() {
                            if (!RcsGroupChatRichMessageEditor.this.mFragment.isDetached()) {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                if (vCalText != null && RcsGroupChatRichMessageEditor.this.mPosition == -1) {
                                    EditText editor = RcsGroupChatRichMessageEditor.this.mSmsEditorText;
                                    editor.getText().insert(editor.getSelectionEnd(), vCalText);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public int getLineNumber() {
        if (this.mSmsEditorText == null || this.mSmsEditorText.getVisibility() != 0) {
            return 0;
        }
        return this.mSmsEditorText.getLineCount();
    }

    public void addRcsGroupRichAttachmentListener(RcsGroupRichAttachmentListener rcsRichattachmentlistener) {
        if (!this.mRcsGroupRichAttachmentListeners.contains(rcsRichattachmentlistener)) {
            this.mRcsGroupRichAttachmentListeners.add(rcsRichattachmentlistener);
        }
    }

    public void removeRcsGroupRichAttachmentListener(RcsGroupRichAttachmentListener rcsRichattachmentlistener) {
        if (this.mRcsGroupRichAttachmentListeners.contains(rcsRichattachmentlistener)) {
            this.mRcsGroupRichAttachmentListeners.remove(rcsRichattachmentlistener);
        }
    }

    private void refreshMediaAttachment(int changedType) {
        for (RcsGroupRichAttachmentListener richAttachmentListener : this.mRcsGroupRichAttachmentListeners) {
            richAttachmentListener.onRcsGroupChatRichAttachmentChanged(changedType);
        }
    }

    public void setFullSizeFlag(boolean flag) {
        this.mIsOriginalSize = flag;
    }

    public boolean getFullSizeFlag() {
        return this.mIsOriginalSize;
    }
}
