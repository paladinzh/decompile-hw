package com.android.rcs.ui;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RcsGroupChatConversationDetailFragment extends HwBaseFragment {
    protected AbstractEmuiActionBar mActionBarWhenSplit;
    private FragmentManager mFragmentManager;
    private RcsGroupChatDetailSettingFragment mGroupChatDetailSettingFragment;
    private View mRootView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.rcs_groupchat_conversation_detail_activity, container, false);
        this.mActionBarWhenSplit = createEmuiActionBar(this.mRootView);
        return this.mRootView;
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.rcs_groupchat_detail_top), null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MLog.v("RcsGroupChatConversationDetailFragment", "onActivityCreated");
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.groupchat_detail));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(null);
        }
        this.mActionBarWhenSplit.setTitle(getString(R.string.groupchat_detail));
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View v) {
                RcsGroupChatConversationDetailFragment.this.getActivity().onBackPressed();
            }
        });
        createFragment(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    private void createFragment(Bundle savedInstanceState) {
        this.mFragmentManager = getFragmentManager();
        FragmentTransaction ft = this.mFragmentManager.beginTransaction();
        if (this.mRootView != null && this.mRootView.findViewById(R.id.groupchat_detail_fragment) != null) {
            if (RcsCommonConfig.isRCSSwitchOn() && this.mGroupChatDetailSettingFragment == null) {
                this.mGroupChatDetailSettingFragment = new RcsGroupChatDetailSettingFragment();
            }
            if (this.mGroupChatDetailSettingFragment != null) {
                this.mGroupChatDetailSettingFragment.setIntent(getIntent());
                ft.replace(R.id.groupchat_detail_fragment, this.mGroupChatDetailSettingFragment);
                ft.commit();
                ft.show(this.mGroupChatDetailSettingFragment);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finishSelf(false);
                break;
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mGroupChatDetailSettingFragment != null) {
            this.mGroupChatDetailSettingFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static List<PeerInformation> processPickIMcontactResult(Context context, Intent data) {
        if (context == null) {
            return null;
        }
        ArrayList<Uri> uriList = data.getParcelableArrayListExtra("SelItemData_KeyValue");
        if (uriList == null) {
            uriList = new ArrayList();
            uriList.add(data.getData());
        }
        if (uriList.size() <= 0) {
            MLog.w("RcsGroupChatConversationDetailFragment", "processPickIMcontactResult uriList error");
            return null;
        }
        List<PeerInformation> members = new ArrayList();
        Iterator<Uri> iter = uriList.iterator();
        while (iter.hasNext()) {
            PeerInformation memberInfo = getMemberInfoByContactUri(context, (Uri) iter.next());
            if (memberInfo != null) {
                boolean flag = false;
                for (PeerInformation peer : members) {
                    if (AddrMatcher.isNumberMatch(peer.getNumber(), memberInfo.getNumber()) > 0) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    members.add(memberInfo);
                }
            }
        }
        return members;
    }

    private static PeerInformation getMemberInfoByContactUri(Context context, Uri uri) {
        String number = "";
        String nickName = "";
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, null, null, null, null);
            if (cursor == null) {
                MLog.w("RcsGroupChatConversationDetailFragment", "Get phone number from Contact Uri error, phone == null");
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (cursor.moveToFirst()) {
                if (uri.toString().startsWith("content://call_log/calls")) {
                    number = cursor.getString(cursor.getColumnIndex(HarassNumberUtil.NUMBER));
                    nickName = cursor.getString(cursor.getColumnIndex("name"));
                    if (TextUtils.isEmpty(nickName)) {
                        nickName = number;
                    }
                } else {
                    number = cursor.getString(cursor.getColumnIndex("data1"));
                    nickName = cursor.getString(cursor.getColumnIndex("display_name"));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return new PeerInformation(nickName, NumberUtils.normalizeNumber(number));
        } catch (RuntimeException e) {
            MLog.e("RcsGroupChatConversationDetailFragment", "Get phone number from Contact error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBarWhenSplit.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
    }
}
