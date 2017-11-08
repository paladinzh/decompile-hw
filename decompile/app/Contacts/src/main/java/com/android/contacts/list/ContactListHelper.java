package com.android.contacts.list;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.optimize.BackgroundCacheHdlr;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import java.util.EmptyStackException;
import java.util.Stack;

public class ContactListHelper extends Handler {
    private int CONTACT_HEADER_SIZE = 3;
    private View mAllListView = null;
    private Object mAllListViewLock = new Object();
    private View mCachedCallLogFragmentView = null;
    private Stack<View> mCachedCallLogItems = null;
    private DialpadFragment mCachedDialpadFragment = null;
    private CallLogAdapter mCallLogAdapter;
    private Handler mCalllogFragmentHandler;
    private Stack<ContactListItemView> mContactListItemViews = null;
    private Context mContext = null;
    private View mDirectoryHeader;
    private HandlerThread mHandlerThread = null;
    private Stack<View> mHeaderViews = null;
    private LayoutInflater mInflater = null;
    private ListView mListView = null;
    private PeopleActivity mPeopleActivity = null;
    private String mSim1VoiceMailNumber = null;
    private String mSim2VoiceMailNumber = null;
    private boolean mVMNCached = false;

    public static ContactListHelper createContactListHelper(Context context) {
        HandlerThread handlerThread = new HandlerThread("ContactListHelper");
        handlerThread.start();
        return new ContactListHelper(handlerThread, context);
    }

    public ContactListHelper(HandlerThread handlerThread, Context context) {
        super(handlerThread.getLooper());
        this.mContext = new ContextThemeWrapper(context.getApplicationContext(), context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        this.mContext.getTheme().applyStyle(R.style.PeopleTheme, true);
        this.mHandlerThread = handlerThread;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                initPeopleActivity();
                return;
            case 1:
                createContactItem();
                return;
            case 2:
                inflateHeader();
                return;
            case 4:
                String sim1VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
                String sim2VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
                if (isRefreshCallLog(sim1VoiceMailNumber, sim2VoiceMailNumber)) {
                    this.mSim1VoiceMailNumber = sim1VoiceMailNumber;
                    this.mSim2VoiceMailNumber = sim2VoiceMailNumber;
                    if (this.mCallLogAdapter != null && this.mCalllogFragmentHandler != null) {
                        this.mCallLogAdapter.setVoicemailNumber(this.mSim1VoiceMailNumber, this.mSim2VoiceMailNumber);
                        this.mCalllogFragmentHandler.sendEmptyMessage(1);
                        return;
                    }
                    return;
                }
                return;
            case 5:
                initContactListContent();
                return;
            case 10:
                init4DialpadFragment();
                return;
            case 11:
                this.mCachedDialpadFragment.prepareInBackground();
                return;
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                if (this.mCachedDialpadFragment != null) {
                    this.mCachedDialpadFragment.doUnRegisterReceiver();
                }
                if (this.mPeopleActivity != null) {
                    this.mPeopleActivity.doUnRegisterReceiver();
                }
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
                return;
            default:
                return;
        }
    }

    private void createContactItem() {
        this.mContactListItemViews = new Stack();
        for (int i = 0; i < 7; i++) {
            this.mContactListItemViews.push(new ContactListItemView(this.mPeopleActivity, null));
        }
    }

    public void startInitHeader(ListView listView) {
        this.mListView = listView;
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            sendEmptyMessage(2);
        }
    }

    private void inflateHeader() {
        if (this.mHeaderViews == null) {
            this.mHeaderViews = new Stack();
        } else {
            this.mHeaderViews.clear();
        }
        initLayoutInflater();
        for (int i = 0; i < this.CONTACT_HEADER_SIZE; i++) {
            this.mHeaderViews.push(this.mInflater.inflate(R.layout.contact_list_group_item_view, this.mListView, false));
        }
        this.mListView = null;
    }

    public void inflateDirectoryHeader(ViewGroup parent) {
        initLayoutInflater();
        this.mDirectoryHeader = this.mInflater.inflate(R.layout.directory_header, parent, false);
    }

    public View getDirectoryHeader() {
        View header = this.mDirectoryHeader;
        this.mDirectoryHeader = null;
        return header;
    }

    private synchronized void initLayoutInflater() {
        if (this.mInflater == null) {
            this.mInflater = LayoutInflater.from(this.mContext);
        }
    }

    public ContactListItemView getContactListItemView() {
        if (this.mContactListItemViews == null || this.mContactListItemViews.isEmpty()) {
            return new ContactListItemView(this.mPeopleActivity, null);
        }
        return (ContactListItemView) this.mContactListItemViews.pop();
    }

    public View getHeader() {
        if (this.mHeaderViews == null || this.mHeaderViews.isEmpty()) {
            initLayoutInflater();
            return this.mInflater.inflate(R.layout.contact_list_group_item_view, null, false);
        }
        try {
            return (View) this.mHeaderViews.pop();
        } catch (EmptyStackException e) {
            initLayoutInflater();
            return this.mInflater.inflate(R.layout.contact_list_group_item_view, null, false);
        }
    }

    private void initContactListContent() {
        synchronized (this.mAllListViewLock) {
            this.mAllListView = BackgroundCacheHdlr.getAndUpdateAllListLayout(this.mContext);
            if (this.mAllListView == null) {
                initLayoutInflater();
                this.mAllListView = this.mInflater.inflate(R.layout.contact_list_content, null);
            }
        }
    }

    public View getAllListLayout() {
        synchronized (this.mAllListViewLock) {
            if (this.mAllListView != null) {
                View lAllListView = this.mAllListView;
                this.mAllListView = null;
                return lAllListView;
            }
            initLayoutInflater();
            View inflate = this.mInflater.inflate(R.layout.contact_list_content, null);
            return inflate;
        }
    }

    public void startInitPeopleActivity(PeopleActivity peopleActivity) {
        this.mPeopleActivity = peopleActivity;
        sendEmptyMessage(0);
    }

    private void initPeopleActivity() {
        ImmersionUtils.initImmersionState();
        this.mPeopleActivity.doRegisterReceivers();
        CountryMonitor.getInstance(this.mPeopleActivity.getApplicationContext());
    }

    public void checkVoicemailNumberChange(Handler handler, CallLogAdapter callogAdapter) {
        if (handler != null && callogAdapter != null) {
            this.mCalllogFragmentHandler = handler;
            this.mCallLogAdapter = callogAdapter;
            sendEmptyMessage(4);
        }
    }

    public void startInitDialpadFragmentInBackground(DialpadFragment dialpadFragment) {
        if (dialpadFragment != null) {
            this.mCachedDialpadFragment = dialpadFragment;
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                sendEmptyMessage(11);
            } else {
                sendEmptyMessage(10);
            }
        }
    }

    private void init4DialpadFragment() {
        initCallLogItems();
        this.mCachedDialpadFragment.prepareInBackground();
        initVoiceMailNumber();
    }

    private void initCallLogItems() {
        initLayoutInflater();
        this.mCachedCallLogFragmentView = this.mInflater.inflate(R.layout.call_log_fragment, null);
        if (this.mCachedCallLogItems != null) {
            this.mCachedCallLogItems.clear();
        } else {
            this.mCachedCallLogItems = new Stack();
        }
        for (int i = 0; i < 12; i++) {
            this.mCachedCallLogItems.push(this.mInflater.inflate(R.layout.call_log_list_item_time_axis, null));
        }
    }

    public View getCallLogItem() {
        if (this.mCachedCallLogItems != null && this.mCachedCallLogItems.size() > 0) {
            return (View) this.mCachedCallLogItems.pop();
        }
        initLayoutInflater();
        return this.mInflater.inflate(R.layout.call_log_list_item_time_axis, null);
    }

    public View getCallLogView() {
        if (this.mCachedCallLogFragmentView == null) {
            initLayoutInflater();
            return this.mInflater.inflate(R.layout.call_log_fragment, null);
        }
        View retView = this.mCachedCallLogFragmentView;
        this.mCachedCallLogFragmentView = null;
        return retView;
    }

    private void initVoiceMailNumber() {
        try {
            this.mSim1VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
            this.mSim2VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
            this.mVMNCached = true;
        } catch (SecurityException e) {
        }
    }

    public String getSim1VoiceMailNumber() {
        if (!this.mVMNCached) {
            initVoiceMailNumber();
        }
        return this.mSim1VoiceMailNumber;
    }

    public String getSim2VoiceMailNumber() {
        if (!this.mVMNCached) {
            initVoiceMailNumber();
        }
        return this.mSim2VoiceMailNumber;
    }

    private boolean isRefreshCallLog(String sim1VoiceMailNumber, String sim2VoiceMailNumber) {
        if (sim1VoiceMailNumber != null && !sim1VoiceMailNumber.equals(this.mSim1VoiceMailNumber)) {
            return true;
        }
        if (sim1VoiceMailNumber == null && this.mSim1VoiceMailNumber != null) {
            return true;
        }
        if (sim2VoiceMailNumber != null && !sim2VoiceMailNumber.equals(this.mSim2VoiceMailNumber)) {
            return true;
        }
        if (sim2VoiceMailNumber != null || this.mSim2VoiceMailNumber == null) {
            return false;
        }
        return true;
    }
}
