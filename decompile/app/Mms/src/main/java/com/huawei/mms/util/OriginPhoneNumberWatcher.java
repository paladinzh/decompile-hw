package com.huawei.mms.util;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import java.util.Vector;

public class OriginPhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
    private static EnterLock locker = new EnterLock();
    private Vector<TextChangeListener> listeners = new Vector();
    private OriginPhoneNumberRecoder recorder = new OriginPhoneNumberRecoder();
    private StringBuffer sbBefore = new StringBuffer();
    private int state = 0;

    private static class EnterLock {
        int counter;

        private EnterLock() {
            this.counter = 0;
        }

        public void Enter() {
            this.counter++;
        }

        public void Leave() {
            this.counter--;
        }

        public boolean isLocked() {
            return this.counter != 0;
        }
    }

    interface TextChangeListener {
        void OnSpecialCharAdd(String str, CharSequence charSequence, Editable editable);

        void OnSpecialCharRemove(String str, CharSequence charSequence, Editable editable);
    }

    static class MailAddressUnformater implements TextChangeListener {
        MailAddressUnformater() {
        }

        private boolean isSplit(char a) {
            return ";,".lastIndexOf(a) >= 0;
        }

        private CharSequence getLastNumber(CharSequence origin) {
            StringBuffer last = new StringBuffer();
            for (int idx = 0; idx < origin.length(); idx++) {
                if (isSplit(origin.charAt(idx))) {
                    last.delete(0, last.length());
                } else {
                    last.append(origin.charAt(idx));
                }
            }
            return last;
        }

        public void unformatMulty(CharSequence origin, Editable s) {
            CharSequence origin_last = getLastNumber(origin);
            CharSequence fomate_last = getLastNumber(s);
            if (!TextUtils.equals(origin_last, fomate_last)) {
                s.replace(s.length() - fomate_last.length(), s.length(), origin_last);
            }
        }

        public void OnSpecialCharAdd(String append, CharSequence origin, Editable s) {
            if (append.indexOf("@") >= 0) {
                unformatMulty(origin, s);
            }
        }

        public void OnSpecialCharRemove(String append, CharSequence origin, Editable s) {
        }
    }

    static class OriginPhoneNumberRecoder {
        private boolean isValid = true;
        private StringBuffer sbChange = new StringBuffer();
        private StringBuffer sbOrigin = null;

        private boolean useAddChange(CharSequence snew, int start, int end) {
            int iorg = 0;
            for (int ifmd = 0; ifmd < start; ifmd++) {
                if (iorg >= this.sbOrigin.length()) {
                    return false;
                }
                if (this.sbOrigin.charAt(iorg) == snew.charAt(ifmd)) {
                    iorg++;
                }
            }
            this.sbOrigin.insert(iorg, snew, start, end);
            return true;
        }

        private boolean useDelChange(CharSequence sold, int start, int end) {
            int ifmd;
            int iorg = 0;
            for (ifmd = 0; ifmd < start; ifmd++) {
                if (this.sbOrigin.charAt(iorg) == sold.charAt(ifmd)) {
                    iorg++;
                }
                if (iorg >= this.sbOrigin.length()) {
                    return false;
                }
            }
            if ((iorg + end) - start > this.sbOrigin.length()) {
                return false;
            }
            int idx = iorg;
            ifmd = start;
            while (ifmd < end) {
                if (this.sbOrigin.charAt(idx) != sold.charAt(ifmd)) {
                    return false;
                }
                ifmd++;
                idx++;
            }
            this.sbOrigin.delete(iorg, (iorg + end) - start);
            return true;
        }

        private int addDiffToOrigin(CharSequence snew, CharSequence sold) {
            int i = 1;
            int i2 = 0;
            int idff = snew.length() - sold.length();
            int inew = 0;
            int iold = 0;
            int changeStart = -1;
            int changeEnd = -1;
            while (this.isValid && inew < snew.length() && iold < sold.length()) {
                if (snew.charAt(inew) == sold.charAt(iold)) {
                    if (changeStart >= 0 && changeEnd == -1) {
                        changeEnd = idff > 0 ? inew : iold;
                    }
                    inew++;
                    iold++;
                } else if (idff == 0 || changeEnd >= 0) {
                    this.isValid = false;
                } else {
                    int npos;
                    if (idff > 0) {
                        npos = inew;
                        inew++;
                    } else {
                        npos = iold;
                        iold++;
                    }
                    if (changeStart < 0) {
                        changeStart = npos;
                    }
                }
            }
            if (inew == iold) {
                changeStart = inew;
                if (idff > 0) {
                    inew = snew.length();
                    changeEnd = inew;
                } else {
                    iold = sold.length();
                    changeEnd = iold;
                }
            }
            boolean z = (!this.isValid || changeStart == -1 || changeStart >= changeEnd) ? false : inew == snew.length() && iold == sold.length();
            this.isValid = z;
            if (!this.isValid) {
                return 0;
            }
            if (idff > 0) {
                this.isValid = useAddChange(snew, changeStart, changeEnd);
                this.sbChange.append(snew, changeStart, changeEnd);
                if (!this.isValid) {
                    i = 0;
                }
                return i;
            } else if (idff >= 0) {
                return 0;
            } else {
                this.isValid = useDelChange(sold, changeStart, changeEnd);
                this.sbChange.append(sold, changeStart, changeEnd);
                if (this.isValid) {
                    i2 = -1;
                }
                return i2;
            }
        }

        public int updateOriginNumber(CharSequence snew, CharSequence sold) {
            this.sbChange.delete(0, this.sbChange.length());
            if (this.sbOrigin == null || this.sbOrigin.length() == 0 || sold == null || sold.length() == 0) {
                this.sbOrigin = new StringBuffer(snew);
                this.sbChange.delete(0, this.sbChange.length()).append(this.sbOrigin);
                this.isValid = true;
                return 1;
            } else if (!snew.toString().contains(";") && !snew.toString().contains(",")) {
                return addDiffToOrigin(snew, sold);
            } else {
                this.isValid = false;
                return 0;
            }
        }

        public CharSequence getOringNumber() {
            return (!this.isValid || this.sbOrigin == null) ? "" : this.sbOrigin.toString();
        }

        public String getChanges() {
            return this.sbChange.toString();
        }

        public boolean beStateValide() {
            return this.isValid;
        }

        public void init() {
            this.isValid = true;
            this.sbOrigin = null;
        }

        public String toString() {
            return "isValid: " + this.isValid + " ,OriginNumber: " + getOringNumber();
        }
    }

    public void addListener(TextChangeListener l) {
        this.listeners.add(l);
    }

    public OriginPhoneNumberWatcher(String countryCOde) {
        super(countryCOde);
        addListener(new MailAddressUnformater());
    }

    private void onCharChange(String snew, CharSequence sold, Editable s) {
        if (this.recorder.beStateValide()) {
            for (TextChangeListener l : this.listeners) {
                if (1 == this.state) {
                    l.OnSpecialCharAdd(this.recorder.getChanges(), this.recorder.getOringNumber(), s);
                } else if (-1 == this.state) {
                    l.OnSpecialCharRemove(this.recorder.getChanges(), this.recorder.getOringNumber(), s);
                }
            }
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
            this.recorder.init();
        }
        this.sbBefore.delete(0, this.sbBefore.length());
        this.sbBefore.append(s);
        super.beforeTextChanged(s, start, before, count);
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.state = locker.isLocked() ? 0 : this.recorder.updateOriginNumber(s, this.sbBefore);
        super.onTextChanged(s, start, before, count);
    }

    public void afterTextChanged(Editable s) {
        locker.Enter();
        if (this.state != 0 && locker.counter == 1) {
            onCharChange(this.recorder.getChanges(), this.recorder.getOringNumber(), s);
        }
        super.afterTextChanged(s);
        locker.Leave();
    }
}
