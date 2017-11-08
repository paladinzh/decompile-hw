package com.huawei.mms.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Editable.Factory;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.cache.MmsMatchContact;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.ChipsTextSpan.IChipsConfiguration;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.HwRecipientUtils;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwRecipientsEditor extends MultiAutoCompleteTextView {
    private final String TAG = getClass().getSimpleName();
    private boolean isAddedToText = true;
    private boolean isCrossScreenCopy = false;
    private boolean isCrossScreenCut = false;
    private AppendWatcher mAppendCallback;
    private int mBeginBatchEditEnd = 0;
    private int mBeginBatchEditStart = 0;
    private Editable mBeginBatchEditText;
    private Drawable mChipBackground = null;
    private Drawable mChipClearDrawable = null;
    private TextView mCollapseNumbersTextView;
    private RelativeLayout mCollapseRecipientView;
    private TextView mCollapseTotalTextView;
    private ComposeActivityCallBack mComposeCallBack;
    private ContactList mContactsInfoList;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    removeMessages(1);
                    SpannableStringBuilder builder = msg.obj;
                    HwRecipientsEditor.this.removeTextChangedListener(HwRecipientsEditor.this.mTextWatcher);
                    HwRecipientsEditor.this.setText(builder);
                    HwRecipientsEditor.this.setSelection(builder.length());
                    HwRecipientsEditor.this.addTextChangedListener(HwRecipientsEditor.this.mTextWatcher);
                    HwRecipientsEditor.this.setUpdatedText(true);
                    return;
                case 3:
                    HwRecipientsEditor.this.toastForInvalidNumber(msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private HwCustHwRecipientsEditor mHwCustHwRecipientsEditor = ((HwCustHwRecipientsEditor) HwCustUtils.createObj(HwCustHwRecipientsEditor.class, new Object[0]));
    private IHwRecipientEditorCallBack mHwRecipientEditorCallBack;
    private boolean mNeedBlockRecentContactsLoading;
    private Runnable mOnSelectChipRunnable;
    private int mSelectedEnd;
    private int mSelectedStart;
    private RecipientTextWatcher mTextWatcher;
    private final RecipientsEditorTokenizer mTokenizer = new RecipientsEditorTokenizer();
    ChipsConfiguration mUIAdapter;

    public interface AppendWatcher {
        void onAppendCompleted();
    }

    public interface ComposeActivityCallBack {
        MmsMatchContact getMatchContact();
    }

    public interface IHwRecipientEditorCallBack {
        boolean isSendMms();
    }

    public class ChipsConfiguration implements IChipsConfiguration {
        protected boolean isRtlLayout = false;
        protected float mChipFontSize;
        protected int mChipHeight;
        protected int mChipPadding;
        protected int mClearAndTextPadidng;
        protected int mIconWidth;
        protected int mMargin = 0;
        protected int mYAdjust = 0;
        protected int mYOffset = 0;

        ChipsConfiguration(Context context) {
            Resources r = HwRecipientsEditor.this.getContext().getResources();
            float height = r.getDimension(R.dimen.mms_chip_height);
            this.mChipHeight = (int) height;
            this.mIconWidth = (int) r.getDimension(R.dimen.mms_chips_clear_width);
            this.mChipFontSize = r.getDimension(R.dimen.mms_chip_text_size);
            this.mChipPadding = (int) r.getDimension(R.dimen.mms_chip_padding);
            this.mClearAndTextPadidng = (int) r.getDimension(R.dimen.mms_chips_clear_and_text_padding);
            this.mYAdjust = (int) r.getDimension(R.dimen.mms_chip_adjust_y);
            this.mYOffset = (this.mYAdjust + ((int) (height / 1.5f))) + ((int) ((context.getResources().getConfiguration().fontScale - ContentUtil.FONT_SIZE_HUGE) * 5.0f));
            this.isRtlLayout = MessageUtils.isNeedLayoutRtl(HwRecipientsEditor.this);
        }

        public int getChipsHeight() {
            return this.mChipHeight;
        }

        public int getIconWidth() {
            return this.mIconWidth;
        }

        public float getAvailableWidth() {
            return calculateAvailableWidth();
        }

        public float getFontSize() {
            return this.mChipFontSize;
        }

        public TextPaint getTextPaint() {
            return HwRecipientsEditor.this.getPaint();
        }

        public int getYAdjust() {
            return this.mYAdjust;
        }

        public int getMargin() {
            if (this.mMargin == 0) {
                float[] widths = new float[1];
                HwRecipientsEditor.this.getPaint().getTextWidths("A", widths);
                this.mMargin = (int) widths[0];
            }
            return this.mMargin;
        }

        public int getChipsPadding() {
            return this.mChipPadding;
        }

        public int getClearAndTextPadidng() {
            return this.mClearAndTextPadidng;
        }

        public int getXOffset(int style) {
            if (!isCurrentSelectedState(style)) {
                return this.mChipPadding;
            }
            return this.isRtlLayout ? (this.mChipPadding + this.mIconWidth) + this.mClearAndTextPadidng : this.mChipPadding;
        }

        public int getYOffset() {
            return this.mYOffset;
        }

        private float calculateAvailableWidth() {
            if (HwRecipientsEditor.this.getWidth() == 0) {
                return HwRecipientsEditor.this.getResources().getDimension(R.dimen.mms_chip_default_max_width);
            }
            return (float) (((HwRecipientsEditor.this.getWidth() - HwRecipientsEditor.this.getPaddingLeft()) - HwRecipientsEditor.this.getPaddingRight()) - (this.mChipPadding * 2));
        }

        public int getFontColor() {
            return HwRecipientsEditor.this.getContext().getResources().getColor(R.color.recipients_text_color);
        }

        public Drawable getBackgroundDrawable() {
            return HwRecipientsEditor.this.mChipBackground;
        }

        public boolean isCurrentSelectedState(int type) {
            return type == 1;
        }

        public Drawable getClearDrawable() {
            return HwRecipientsEditor.this.mChipClearDrawable;
        }

        public boolean isRtlLayout() {
            return this.isRtlLayout;
        }
    }

    private class RecipientTextWatcher implements TextWatcher {
        private RecipientTextWatcher() {
        }

        public void afterTextChanged(Editable s) {
            if (HwRecipientsEditor.this.lastCharacterIsCommitCharacter(s)) {
                HwRecipientsEditor.this.commitNumberChip();
            }
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (count == 1) {
                int selStart = HwRecipientsEditor.this.getSelectionStart() - 1;
                if (((ChipsTextSpan[]) HwRecipientsEditor.this.getText().getSpans(selStart, selStart, ChipsTextSpan.class)).length > 0) {
                    Editable editable = HwRecipientsEditor.this.getText();
                    int tokenStart = HwRecipientsEditor.this.mTokenizer.findTokenStart(editable, selStart);
                    int tokenEnd = HwRecipientsEditor.this.mTokenizer.findTokenEnd(editable, tokenStart) + 1;
                    if (tokenEnd > editable.length()) {
                        tokenEnd = editable.length();
                    }
                    HwRecipientsEditor.this.deleteChip(tokenStart, tokenEnd);
                }
            }
        }
    }

    private static class RecipientsEditorTokenizer implements Tokenizer {
        private RecipientsEditorTokenizer() {
        }

        public int findTokenStart(CharSequence text, int cursor) {
            int best = 0;
            int i = 0;
            while (i < cursor) {
                i = findTokenEnd(text, i);
                if (i < cursor) {
                    i++;
                    while (i < cursor && text.charAt(i) == ' ') {
                        i++;
                    }
                    if (i < cursor) {
                        best = i;
                    }
                }
            }
            return best;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int len = text.length();
            for (int i = cursor; i < len; i++) {
                char c = text.charAt(i);
                if (c == ',' || c == ';') {
                    return i;
                }
            }
            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            if (i > 0) {
                char c = text.charAt(i - 1);
                if (c == ',' || c == ';') {
                    return text;
                }
            }
            String separator = ", ";
            if (!(text instanceof Spanned)) {
                return text + separator;
            }
            SpannableString sp = new SpannableString(text + separator);
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
            return sp;
        }
    }

    public HwRecipientsEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTokenizer(this.mTokenizer);
        this.mTextWatcher = new RecipientTextWatcher();
        addTextChangedListener(this.mTextWatcher);
        this.mContactsInfoList = new ContactList();
        this.mUIAdapter = new ChipsConfiguration(context);
        Resources r = getContext().getResources();
        this.mChipBackground = r.getDrawable(R.drawable.mms_new_bubble_default);
        this.mChipClearDrawable = r.getDrawable(R.drawable.chips_clear_icon);
        setImeOptions(5);
    }

    public List<String> getNumbers() {
        ArrayList<String> numberList = new ArrayList();
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                numberList.add(PhoneNumberUtils.replaceUnicodeDigits(contact.getNumber()));
            }
        }
        return numberList;
    }

    public void setCollapseRecipientView(RelativeLayout collapseRecipient) {
        this.mCollapseRecipientView = collapseRecipient;
        if (this.mCollapseRecipientView != null) {
            this.mCollapseNumbersTextView = (TextView) this.mCollapseRecipientView.findViewById(R.id.mms_recipient_number_text);
            this.mCollapseTotalTextView = (TextView) this.mCollapseRecipientView.findViewById(R.id.mms_recipient_total_sign_text);
        }
    }

    public boolean containsEmail() {
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                if (contact.isEmail()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setChipHeight(int height) {
        this.mUIAdapter.mChipHeight = height;
    }

    public int getRecipientCount() {
        int size;
        synchronized (this.mContactsInfoList) {
            size = this.mContactsInfoList.size();
        }
        return size;
    }

    public ContactList getRecipients() {
        ContactList contactList;
        synchronized (this.mContactsInfoList) {
            contactList = this.mContactsInfoList;
        }
        return contactList;
    }

    public boolean hasInvalidRecipient(boolean isMms) {
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                if (!HwRecipientUtils.isValidAddress(MccMncConfig.getFilterNumberByMCCMNC(contact.getNumber()), isMms)) {
                    if (MmsConfig.getEmailGateway() == null) {
                        return true;
                    } else if (!MessageUtils.isAlias(contact.getNumber())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean hasComplexInvalidRecipient() {
        if (MmsConfig.isUseGgSmsAddressCheck()) {
            return false;
        }
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                String sNum = MccMncConfig.getFilterNumberByMCCMNC(contact.getNumber());
                if (!Contact.isEmailAddress(sNum) && HwRecipientUtils.isComplexInvalidRecipient(sNum)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean hasInvalidRecipient(String number, boolean isMms) {
        if (HwRecipientUtils.isValidAddress(MccMncConfig.getFilterNumberByMCCMNC(number), isMms) || (MmsConfig.getEmailGateway() != null && MessageUtils.isAlias(number))) {
            return false;
        }
        return true;
    }

    public boolean hasComplexInvalidRecipient(String number) {
        if (MmsConfig.isUseGgSmsAddressCheck()) {
            return false;
        }
        String sNum = MccMncConfig.getFilterNumberByMCCMNC(number);
        if (!Contact.isEmailAddress(sNum) && HwRecipientUtils.isComplexInvalidRecipient(sNum)) {
            return true;
        }
        return false;
    }

    public String formatInvalidNumbers(boolean isMms) {
        StringBuilder sb = new StringBuilder();
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                String number = contact.getNumber();
                if (!HwRecipientUtils.isValidAddress(number, isMms) || (!Contact.isEmailAddress(number) && HwRecipientUtils.isComplexInvalidRecipient(number))) {
                    if (sb.length() != 0) {
                        sb.append(", ");
                    }
                    sb.append(number);
                }
            }
        }
        return sb.toString();
    }

    public boolean hasValidRecipient(boolean isMms) {
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                if (HwRecipientUtils.isValidAddress(contact.getNumber(), isMms)) {
                    return true;
                }
            }
            return false;
        }
    }

    public ContactList constructContactsFromInput(boolean blocking) {
        ContactList list = new ContactList();
        synchronized (this.mContactsInfoList) {
            for (Contact contactInfo : this.mContactsInfoList) {
                String number = contactInfo.getNumber();
                Contact contact = Contact.get(number, blocking);
                contact.setNumber(number);
                contact.setOriginNumber(number);
                list.add(contact);
            }
        }
        return list;
    }

    public SpannableStringBuilder filterInvalidRecipients(boolean isMms) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        synchronized (this.mContactsInfoList) {
            for (Contact contact : this.mContactsInfoList) {
                String number = contact.getNumber();
                if (HwRecipientUtils.isValidAddress(number, isMms) || (MmsConfig.getEmailGateway() != null && MessageUtils.isAlias(number))) {
                    sb.append(number + ", ");
                }
            }
        }
        return sb;
    }

    public void addRecipients(ContactList newList, ContactList oldContactList) {
        boolean isContain = false;
        boolean hasDuplicate = false;
        synchronized (this.mContactsInfoList) {
            for (Contact c : newList) {
                String number = c.getOriginNumber();
                if (TextUtils.isEmpty(number)) {
                    number = c.getNumber();
                }
                for (Contact contact : this.mContactsInfoList) {
                    if (contact.getKey() == c.getKey()) {
                        if (!contact.isEmail() || !c.isEmail()) {
                            if (!(contact.isEmail() || c.isEmail() || !HwNumberMatchUtils.isNumbersMatched(number, contact.getNumber()))) {
                                isContain = true;
                                break;
                            }
                        } else if (c.getNumber().equals(contact.getNumber())) {
                            isContain = true;
                            break;
                        }
                    }
                }
                if (isContain) {
                    hasDuplicate = true;
                    isContain = false;
                } else {
                    this.mContactsInfoList.add(c);
                }
            }
        }
        if (hasDuplicate) {
            toastForDucplicateAddress();
        }
        appendChipsCompletedCallBack();
        setUpdatedText(false);
        asyncAddRecipientsText();
    }

    public boolean isUpdatedText() {
        return this.isAddedToText;
    }

    public void setUpdatedText(boolean isUpdated) {
        this.isAddedToText = isUpdated;
    }

    private void asyncAddRecipientsText() {
        ThreadEx.execute(new Runnable() {
            public void run() {
                Message msg;
                SpannableStringBuilder builder = new SpannableStringBuilder();
                List<String> invalidAddresses = new ArrayList();
                synchronized (HwRecipientsEditor.this.mContactsInfoList) {
                    List<Contact> toDel = new ArrayList();
                    for (Contact contact : HwRecipientsEditor.this.mContactsInfoList) {
                        String number = contact.getNumber();
                        if (HwRecipientUtils.isValidAddress(number, Contact.isEmailAddress(number))) {
                            builder.append(HwRecipientsEditor.this.createChip(contact, false));
                        } else {
                            if (number.length() > 0) {
                                invalidAddresses.add(number);
                            }
                            toDel.add(contact);
                            Log.d(HwRecipientsEditor.this.TAG, "getChipsSequence: has invalid number ***");
                        }
                    }
                    if (toDel.size() > 0) {
                        Log.d(HwRecipientsEditor.this.TAG, "Has invalide contacts. len " + toDel.size());
                        HwRecipientsEditor.this.mContactsInfoList.removeAll(toDel);
                    }
                }
                if (invalidAddresses.size() > 0) {
                    msg = HwRecipientsEditor.this.mHandler.obtainMessage(3);
                    msg.obj = invalidAddresses;
                    msg.sendToTarget();
                }
                msg = new Message();
                msg.obj = builder;
                msg.what = 1;
                HwRecipientsEditor.this.mHandler.sendMessage(msg);
            }
        });
    }

    private void appendChipsCompletedCallBack() {
        if (this.mAppendCallback != null) {
            this.mAppendCallback.onAppendCompleted();
        }
    }

    public void setAppendWatcher(AppendWatcher watcher) {
        this.mAppendCallback = watcher;
    }

    public void removeAppendWatcher() {
        this.mAppendCallback = null;
    }

    public void setComposeCallBack(ComposeActivityCallBack callBack) {
        this.mComposeCallBack = callBack;
    }

    public void removeComposeCallBack() {
        this.mComposeCallBack = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeEmail() {
        int numberFrom = 0;
        synchronized (this.mContactsInfoList) {
            Iterator<Contact> iterator = this.mContactsInfoList.iterator();
            while (iterator.hasNext()) {
                if (((Contact) iterator.next()).isEmail()) {
                    int[] location = getNumbersLocation(numberFrom);
                    int start = location[0];
                    int end = location[1];
                    if (end > length() || end == -1) {
                    } else {
                        iterator.remove();
                        SpannableStringBuilder tmpBuilder = new SpannableStringBuilder(getText());
                        tmpBuilder.delete(start, end);
                        setText(tmpBuilder);
                        setSelection(start);
                    }
                } else {
                    numberFrom++;
                }
            }
        }
    }

    public boolean isNeedBlockRecentContactsLoading() {
        return this.mNeedBlockRecentContactsLoading;
    }

    public void setNeedBlockRecentContactsLoading(boolean needBlock) {
        this.mNeedBlockRecentContactsLoading = needBlock;
    }

    private CharSequence createChip(Contact contact, boolean pressed) {
        String displayText = contact.getName();
        if (TextUtils.isEmpty(displayText)) {
            return "";
        }
        ChipsTextSpan chip = constructChipSpan(displayText, pressed);
        String text = createAddressText(contact.getName(), contact.getNumber(), contact.isEmail()).trim() + ", ";
        int textLength = text.length() - 1;
        SpannableString chipText = new SpannableString(text);
        chipText.setSpan(chip, 0, textLength, 33);
        return chipText;
    }

    private ChipsTextSpan constructChipSpan(String display, boolean pressed) {
        ChipsTextSpan ret = new ChipsTextSpan(getContext(), display, pressed ? 1 : 0, this.mUIAdapter);
        ret.measuereWidth();
        return ret;
    }

    public void addPopupRecipient(MmsMatchContact matchCon) {
        clearComposingText();
        int end = getSelectionEnd();
        int start = this.mTokenizer.findTokenStart(getText(), end);
        Editable editable = getText();
        boolean needConstruct = true;
        List<String> numberList = getNumbers();
        String formatNumber = NumberUtils.formatAndParseNumber(matchCon.mNumber, null);
        if (numberList.size() >= 1) {
            for (String number : numberList) {
                if (number.indexOf("@") == -1) {
                    if (formatNumber != null && formatNumber.indexOf("@") == -1 && HwNumberMatchUtils.isNumbersMatched(number, formatNumber)) {
                        needConstruct = false;
                        break;
                    }
                } else if (number.equalsIgnoreCase(matchCon.mNumber)) {
                    needConstruct = false;
                    break;
                }
            }
        }
        if (needConstruct) {
            this.mNeedBlockRecentContactsLoading = true;
        } else {
            ResEx.self().showMmsToast(getContext(), R.string.duplicate_chips_Toast, 0);
        }
        if (start < 0 || start >= end || isCompletedInputToken(editable)) {
            StatisticalHelper.incrementReportCount(getContext(), 2014);
        } else {
            StatisticalHelper.incrementReportCount(getContext(), 2015);
            editable.delete(start, end);
        }
        if (needConstruct) {
            SpannableStringBuilder tempBuilder = new SpannableStringBuilder(getText());
            Contact contact = Contact.get(formatNumber, false);
            if (!TextUtils.isEmpty(matchCon.mName)) {
                contact.setName(matchCon.mName);
            }
            if (matchCon.mContactId.longValue() > 0) {
                contact.setContactId(matchCon.mContactId.longValue());
            }
            synchronized (this.mContactsInfoList) {
                this.mContactsInfoList.add(contact);
            }
            tempBuilder.append(createChip(contact, false));
            setText(tempBuilder);
            setSelection(length());
            checkSelectRunnable();
            appendChipsCompletedCallBack();
            resetSelectedChip();
        }
    }

    public void setOnSelectChipRunnable(Runnable onSelectChipRunnable) {
        this.mOnSelectChipRunnable = onSelectChipRunnable;
    }

    private void checkSelectRunnable() {
        if (this.mOnSelectChipRunnable != null) {
            this.mOnSelectChipRunnable.run();
        }
    }

    private int[] getNumbersLocation(int numberFrom) {
        Spanned sp = getText();
        int[] location = new int[2];
        String[] numbers = sp.toString().replaceAll(";", ",").split(",");
        if (numbers.length <= numberFrom) {
            location[1] = -1;
            return location;
        }
        for (int i = 0; i <= numberFrom; i++) {
            if (i == 0) {
                location[0] = 0;
            } else {
                location[0] = location[0] + (numbers[i - 1].length() + ",".length());
            }
        }
        location[1] = (location[0] + numbers[numberFrom].length()) + ",".length();
        if (numbers[numberFrom].startsWith(" ")) {
            location[0] = location[0] + " ".length();
        }
        if (numbers.length > numberFrom + 1 && numbers[numberFrom + 1].startsWith(" ")) {
            location[1] = location[1] + " ".length();
        }
        if (location[1] > sp.length()) {
            location[1] = sp.length();
        }
        return location;
    }

    private boolean isCompletedInputToken(CharSequence text) {
        boolean z = true;
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        int end = text.length();
        String token = text.toString().substring(this.mTokenizer.findTokenStart(text, end), end).trim();
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        char atEnd = token.charAt(token.length() - 1);
        if (!(atEnd == ',' || atEnd == ';')) {
            z = false;
        }
        return z;
    }

    private boolean lastCharacterIsCommitCharacter(CharSequence s) {
        boolean z = true;
        int end = getSelectionEnd() == 0 ? 0 : getSelectionEnd() - 1;
        int len = length() - 1;
        if (len < 0 || end < 0) {
            return false;
        }
        char last;
        if (end < len) {
            last = s.charAt(end);
        } else {
            last = s.charAt(len);
        }
        if (!(last == ',' || last == ';' || last == '\n')) {
            z = false;
        }
        return z;
    }

    private int getUncommitPostion() {
        int additionStart = 0;
        Editable text = getText();
        if (TextUtils.isEmpty(text)) {
            return -1;
        }
        int length = length();
        int totalRecipient = getRecipientCount();
        int i = 0;
        while (i < length && totalRecipient > 0) {
            char textChar = text.charAt(i);
            if (textChar == ',' || textChar == ';') {
                totalRecipient--;
            }
            i++;
        }
        if (i != 0) {
            additionStart = i + 1;
        }
        if (additionStart > length - 1) {
            return -1;
        }
        return additionStart;
    }

    private CharSequence getChipsSequence(String dotSplitString) {
        int contactsInfoListSize;
        synchronized (this.mContactsInfoList) {
            contactsInfoListSize = this.mContactsInfoList.size();
        }
        return getChipsSequence(dotSplitString, MmsConfig.getRecipientLimit() - contactsInfoListSize);
    }

    private CharSequence getChipsSequence(String dotSplitString, int maxLimit) {
        String[] numbers = dotSplitString.split(",");
        int counter = 0;
        int process = 0;
        Contact dupContact = null;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        List<String> invalidAddresses = new ArrayList();
        for (String number : numbers) {
            if (counter == maxLimit) {
                counter++;
                break;
            }
            process++;
            String number2 = number2.trim();
            if (number2.indexOf("@") != -1) {
                number2 = getEmailAddressFromToken(number2);
            }
            if (HwRecipientUtils.isValidAddress(number2, Contact.isEmailAddress(number2))) {
                boolean isMms = false;
                if (this.mHwRecipientEditorCallBack != null) {
                    isMms = this.mHwRecipientEditorCallBack.isSendMms();
                }
                if (!hasInvalidRecipient(number2, isMms) && !hasComplexInvalidRecipient(number2)) {
                    Contact contact = getContainedContact(number2);
                    if (contact != null) {
                        dupContact = contact;
                        Log.d(this.TAG, "getChipsSequence: has duplicate number ***");
                    } else {
                        contact = getContact(number2);
                        CharSequence newChip = createChip(contact, false);
                        if (newChip != null) {
                            builder.append(newChip);
                            synchronized (this.mContactsInfoList) {
                                this.mContactsInfoList.add(contact);
                            }
                            counter++;
                        } else {
                            continue;
                        }
                    }
                } else if (number2.length() > 0) {
                    invalidAddresses.add(number2);
                }
            } else {
                if (number2.length() > 0) {
                    invalidAddresses.add(number2);
                }
                Log.d(this.TAG, "getChipsSequence: has invalid number ***");
            }
        }
        if (invalidAddresses.size() > 0) {
            toastForInvalidNumber(invalidAddresses);
            if (this.mHwCustHwRecipientsEditor != null) {
                this.mHwCustHwRecipientsEditor.handleInvalidRecipent(this);
            }
        }
        if (counter > maxLimit) {
            toastForTooManyRecipients(numbers.length - process);
        }
        if (dupContact != null) {
            toastForDucplicateAddress();
        }
        return builder;
    }

    private void toastForDucplicateAddress() {
        ResEx.self().showMmsToast(getContext(), R.string.duplicate_chips_Toast, 0);
    }

    private void toastForTooManyRecipients(int exceed) {
        int max = MmsConfig.getRecipientLimit();
        int recepientCount = max + exceed;
        Toast.makeText(getContext(), getContext().getResources().getQuantityString(R.plurals.too_many_recipients, max, new Object[]{Integer.valueOf(recepientCount), Integer.valueOf(max)}), 0).show();
    }

    private void toastForInvalidNumber(List<String> numbers) {
        StringBuilder sb = new StringBuilder();
        for (String number : numbers) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(number);
        }
        Toast.makeText(getContext(), getResources().getQuantityString(R.plurals.has_invalid_recipient, numbers.size(), new Object[]{sb.toString()}), 0).show();
    }

    private void doPaste(int min, int max) {
        SpannableStringBuilder builder = new SpannableStringBuilder(getText());
        ClipData clip = ((ClipboardManager) getContext().getSystemService("clipboard")).getPrimaryClip();
        if (TextUtils.isEmpty(getText())) {
            synchronized (this.mContactsInfoList) {
                this.mContactsInfoList.clear();
            }
        }
        if (clip != null && clip.getItemCount() > 0) {
            builder.insert(min, clip.getItemAt(0).coerceToStyledText(getContext()).toString().trim().replaceAll(";|\n", ","));
            setText(builder);
            setSelection(length());
        }
    }

    public void commitNumberChip() {
        Editable e = getText();
        int length = e.length();
        int additionStart = getUncommitPostion();
        if (additionStart != -1) {
            StatisticalHelper.incrementReportCount(getContext(), 2016);
            getText().replace(additionStart, length, getChipsSequence(e.subSequence(additionStart, length).toString().replace(";", ",")));
            setSelection(length());
            resetSelectedChip();
        }
    }

    private Contact getContainedContact(String number) {
        boolean isEmail = Contact.isEmailAddress(number);
        synchronized (this.mContactsInfoList) {
            if (isEmail) {
                Contact matchedEmailContact = getMatchedEmailContact(number, this.mContactsInfoList);
                return matchedEmailContact;
            }
            Contact[] numbers = (Contact[]) this.mContactsInfoList.toArray(new Contact[this.mContactsInfoList.size()]);
            return HwNumberMatchUtils.getMatchedContact(numbers, number);
        }
    }

    private Contact getMatchedEmailContact(String number, ContactList list) {
        for (Contact contact : list) {
            if (contact.isEmail() && contact.getNumber().equalsIgnoreCase(number)) {
                return contact;
            }
        }
        return null;
    }

    private Contact getContact(String number) {
        Contact contact = Contact.get(number, false);
        contact.setNumber(number);
        MmsMatchContact matchCon = null;
        if (this.mComposeCallBack != null) {
            matchCon = this.mComposeCallBack.getMatchContact();
        }
        if (!(matchCon == null || TextUtils.isEmpty(matchCon.mName) || matchCon.mContactId.longValue() <= 0)) {
            if (number.indexOf("@") != -1) {
                if (number.equalsIgnoreCase(matchCon.mNumber)) {
                    contact.setName(matchCon.mName);
                    contact.setContactId(matchCon.mContactId.longValue());
                }
            } else if (HwNumberMatchUtils.isNumbersMatched(number, matchCon.mNumber)) {
                contact.setName(matchCon.mName);
                contact.setContactId(matchCon.mContactId.longValue());
            }
        }
        return contact;
    }

    public boolean onTextContextMenuItem(int id) {
        int min = 0;
        int max = length();
        if (isFocused()) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }
        switch (id) {
            case 16908320:
                ((ClipboardManager) getContext().getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(null, getText().subSequence(min, max)));
                deleteSelectedChips(min, max);
                if (getContext().getResources().getConfiguration().orientation == 2) {
                    this.isCrossScreenCut = true;
                }
                return true;
            case 16908321:
                if (getContext().getResources().getConfiguration().orientation == 2) {
                    this.isCrossScreenCopy = true;
                    break;
                }
                break;
            case 16908322:
                deleteSelectedChips(min, max);
                doPaste(min, max);
                return true;
        }
        return super.onTextContextMenuItem(id);
    }

    public void updateSpanStyle(ChipsTextSpan span, int style) {
        if (span.getStyle() != style) {
            span.updateStyle(style);
            span.measuereWidth();
            Editable editor = getText();
            editor.setSpan(span, editor.getSpanStart(span), editor.getSpanEnd(span), 33);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        int action = event.getAction();
        int selectStart = 0;
        int selectEnd = 0;
        int x = (int) event.getX();
        int offset = getOffsetForPosition((float) x, (float) ((int) event.getY()));
        ChipsTextSpan clickChip = getSpan(offset);
        if (clickChip == null) {
            boolean z2;
            if (offset != 0 || getText().length() == 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setCursorVisible(z2);
        } else {
            selectStart = getText().getSpanStart(clickChip);
            selectEnd = getText().getSpanEnd(clickChip);
            if (MessageUtils.isNeedLayoutRtl(this)) {
                Float chipStartPositionX = Float.valueOf(getLayout().getPrimaryHorizontal(selectStart));
                Float chipEndPositionX = Float.valueOf(getLayout().getPrimaryHorizontal(selectEnd));
                if (x < chipStartPositionX.intValue() || x > chipEndPositionX.intValue()) {
                    super.onTouchEvent(event);
                    setSelection(length());
                    return true;
                }
            }
        }
        if (action == 0) {
            if (this.mSelectedStart + this.mSelectedEnd == 0 || (selectStart == this.mSelectedStart && selectEnd == this.mSelectedEnd)) {
                if (clickChip == null) {
                    z = super.onTouchEvent(event);
                }
                return z;
            }
            ChipsTextSpan oldSpan = getSpan(this.mSelectedStart, this.mSelectedEnd);
            if (oldSpan == null || oldSpan == clickChip) {
                if (clickChip == null) {
                    z = super.onTouchEvent(event);
                }
                return z;
            }
            updateSpanStyle(oldSpan, 0);
            this.mSelectedStart = 0;
            this.mSelectedEnd = 0;
            if (clickChip == null) {
                z = super.onTouchEvent(event);
            }
            return z;
        } else if (action != 1) {
            return super.onTouchEvent(event);
        } else {
            if (clickChip != null) {
                if (clickChip.getStyle() != 0) {
                    deleteChip(selectStart, selectEnd);
                    setSelection(length());
                    requestFocus();
                } else if (this.mSelectedStart + this.mSelectedEnd == 0) {
                    updateSpanStyle(clickChip, 1);
                    this.mSelectedStart = selectStart;
                    this.mSelectedEnd = selectEnd;
                }
            }
            if (clickChip == null) {
                z = super.onTouchEvent(event);
            }
            return z;
        }
    }

    public void updateCollapseTextView(int numMaxWidth) {
        if (this.mCollapseRecipientView != null && this.mCollapseNumbersTextView != null && this.mCollapseTotalTextView != null) {
            int idx;
            this.mCollapseTotalTextView.setVisibility(8);
            int recipientCount = getRecipientCount();
            TextPaint numberPaint = new TextPaint(this.mCollapseNumbersTextView.getPaint());
            numberPaint.setTextSize(this.mCollapseNumbersTextView.getTextSize());
            StringBuilder numberString = new StringBuilder();
            for (idx = 0; idx < recipientCount; idx++) {
                Contact contact;
                synchronized (this.mContactsInfoList) {
                    contact = (Contact) this.mContactsInfoList.get(idx);
                }
                numberString.append(HwMessageUtils.formatNumberString(contact.getName()));
                if (((int) numberPaint.measureText(numberString.toString())) >= numMaxWidth) {
                    if (recipientCount != 1) {
                        NumberFormat.getIntegerInstance().setGroupingUsed(false);
                        this.mCollapseTotalTextView.setText(getContext().getResources().getQuantityString(R.plurals.mms_recipient_number_total_message, recipientCount, new Object[]{nf.format((long) recipientCount)}));
                        this.mCollapseTotalTextView.setVisibility(0);
                    }
                    if (idx == recipientCount) {
                        this.mCollapseTotalTextView.setVisibility(8);
                    }
                    this.mCollapseNumbersTextView.setText(numberString.toString());
                }
                if (idx != recipientCount - 1) {
                    numberString.append(", ");
                }
            }
            if (idx == recipientCount) {
                this.mCollapseTotalTextView.setVisibility(8);
            }
            this.mCollapseNumbersTextView.setText(numberString.toString());
        }
    }

    public boolean isNeedCollapse() {
        return getRecipientCount() > 0;
    }

    private void deleteChip(int start, int end) {
        if (start == this.mSelectedStart && end == this.mSelectedEnd) {
            this.mSelectedStart = 0;
            this.mSelectedEnd = 0;
        } else {
            resetSelectedChip();
        }
        if (end <= length() - 1) {
            SpannableStringBuilder tmpBuilder = new SpannableStringBuilder(getText());
            ChipsTextSpan[] spans = (ChipsTextSpan[]) tmpBuilder.getSpans(start, end, ChipsTextSpan.class);
            if (spans == null || spans.length < 1) {
                tmpBuilder.delete(start, end);
                return;
            }
            String number = tmpBuilder.subSequence(start, end - 1).toString();
            synchronized (this.mContactsInfoList) {
                for (Contact contact : this.mContactsInfoList) {
                    if (contact.isEmail()) {
                        number = getEmailAddressFromToken(number);
                    }
                    if (contact.getNumber().equals(number)) {
                        this.mContactsInfoList.remove(contact);
                        break;
                    }
                }
            }
            tmpBuilder.delete(start, end + 1);
            setText(tmpBuilder);
            if (start > length()) {
                start = length();
            }
            setSelection(start);
        }
    }

    private String getEmailAddressFromToken(String token) {
        int start = token.indexOf(60);
        int end = token.lastIndexOf(62);
        if (start < 0 || end <= 0 || end <= start + 1) {
            return token;
        }
        return token.substring(start + 1, end);
    }

    private void deleteAllChips(boolean needChangeText) {
        if (needChangeText) {
            setText("");
        }
        synchronized (this.mContactsInfoList) {
            this.mContactsInfoList.clear();
        }
    }

    private void deleteSelectedChips(int selectStart, int selectEnd) {
        deleteSelectedChips(getText(), selectStart, selectEnd, true);
    }

    private void deleteSelectedChips(Editable text, int selectStart, int selectEnd, boolean needChangeText) {
        int start = selectStart;
        int end = selectEnd;
        if (selectStart > selectEnd) {
            start = selectEnd;
            end = selectStart;
        }
        if (start != end && end > 0) {
            int length = text.length();
            if (end - start == length) {
                deleteAllChips(needChangeText);
                return;
            }
            String tempText = text.toString().replace(";", ",");
            int index = -1;
            synchronized (this.mContactsInfoList) {
                int i = 0;
                while (i < length) {
                    if (tempText.charAt(i) == ',') {
                        index++;
                        if (i >= start && i <= end && this.mContactsInfoList.size() > index && index >= 0) {
                            this.mContactsInfoList.remove(index);
                            index--;
                        }
                    }
                    i++;
                }
            }
            if (needChangeText) {
                SpannableStringBuilder builder = new SpannableStringBuilder(text);
                builder.delete(start, end);
                setText(builder);
            }
            setSelection(start);
            setCursorVisible(false);
        }
    }

    public void onBeginBatchEdit() {
        this.mBeginBatchEditStart = getSelectionStart();
        this.mBeginBatchEditEnd = getSelectionEnd();
        this.mBeginBatchEditText = Factory.getInstance().newEditable(getText());
        super.onBeginBatchEdit();
    }

    public void onEndBatchEdit() {
        if (!(this.mBeginBatchEditEnd == this.mBeginBatchEditStart || this.mBeginBatchEditEnd <= 0 || this.isCrossScreenCopy || this.isCrossScreenCut)) {
            removeTextChangedListener(this.mTextWatcher);
            if (!TextUtils.isEmpty(this.mBeginBatchEditText)) {
                deleteSelectedChips(this.mBeginBatchEditText, this.mBeginBatchEditStart, this.mBeginBatchEditEnd, false);
            }
            addTextChangedListener(this.mTextWatcher);
        }
        this.isCrossScreenCopy = false;
        this.isCrossScreenCut = false;
        this.mBeginBatchEditText = null;
        super.onEndBatchEdit();
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selStart != selEnd) {
            boolean isRemoveStart = isRemoveSelection(selStart);
            boolean isRemoveEnd = isRemoveSelection(selEnd);
            if (isRemoveEnd || isRemoveStart) {
                setSelection(isRemoveStart ? selStart + 1 : selStart, isRemoveEnd ? selEnd + 1 : selEnd);
            }
        } else if (isRemoveSelection(selEnd)) {
            setSelection(selEnd + 1);
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    private boolean isRemoveSelection(int index) {
        if (index > 0 && index < length()) {
            char endIndexPreChar = getText().charAt(index - 1);
            char endIndexChar = getText().charAt(index);
            if (endIndexPreChar == ',' && endIndexChar == ' ') {
                return true;
            }
        }
        return false;
    }

    private String createAddressText(String name, String address, boolean isEmail) {
        String str = name;
        if (address == null) {
            return "";
        }
        String trimmedText;
        if (isEmail) {
            if (TextUtils.isEmpty(name) || TextUtils.equals(name, address)) {
                str = null;
            }
            Rfc822Token[] tokenized = Rfc822Tokenizer.tokenize(address);
            if (tokenized != null && tokenized.length > 0) {
                address = tokenized[0].getAddress();
            }
            trimmedText = new Rfc822Token(str, address, null).toString().trim();
        } else {
            trimmedText = address.trim();
        }
        int index = trimmedText.indexOf(",");
        if (index > 1) {
            trimmedText = trimmedText.substring(0, index);
        }
        index = trimmedText.indexOf(";");
        if (index > 1) {
            trimmedText = trimmedText.substring(0, index);
        }
        return trimmedText;
    }

    private ChipsTextSpan getSpan(int offset) {
        int i = 0;
        Editable edit = getText();
        ChipsTextSpan[] chips = (ChipsTextSpan[]) edit.getSpans(0, edit.length(), ChipsTextSpan.class);
        int length = chips.length;
        while (i < length) {
            ChipsTextSpan chip = chips[i];
            if (chip == null) {
                break;
            }
            int start = edit.getSpanStart(chip);
            int end = edit.getSpanEnd(chip);
            if (offset >= start && offset <= end) {
                return chip;
            }
            i++;
        }
        return null;
    }

    public ChipsTextSpan getSpan(int selectStart, int selectEnd) {
        if (selectStart == 0 && selectEnd == 0) {
            return null;
        }
        ChipsTextSpan[] oldChips = (ChipsTextSpan[]) getText().getSpans(selectStart, selectEnd, ChipsTextSpan.class);
        if (oldChips == null || oldChips.length == 0) {
            return null;
        }
        return oldChips[0];
    }

    public void resetSelectedChip() {
        ChipsTextSpan oldChip = getSpan(this.mSelectedStart, this.mSelectedEnd);
        if (oldChip != null) {
            updateSpanStyle(oldChip, 0);
            this.mSelectedStart = 0;
            this.mSelectedEnd = 0;
        }
    }

    public void setHwRecipientEditorCallBack(IHwRecipientEditorCallBack callBack) {
        this.mHwRecipientEditorCallBack = callBack;
    }
}
