package com.android.rcs.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RcsGroupMemberAdapter extends CursorAdapter implements OnClickListener {
    private static final String[] MEMBER_PROJECTION = new String[]{"_id", "rcs_id", "status", "nickname"};
    private Activity mActivity;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private ContactLoader mContactLoader;
    private Handler mContextHandler = new Handler() {
        public void handleMessage(Message msg) {
            TaskInfo taskInfo = msg.obj;
            RcsGroupMemberAdapter.this.mMemberInfoCache.put(taskInfo.address, taskInfo.memberInfo);
            RcsGroupMemberAdapter.this.bindContact(taskInfo.view, taskInfo.memberInfo);
            super.handleMessage(msg);
        }
    };
    private Fragment mFragment;
    private HandlerThread mHandlerThread = new HandlerThread("member task", 1);
    private LayoutInflater mInflater;
    private boolean mIsShowExtBtn = true;
    private Map<String, MemberInfo> mMemberInfoCache = new HashMap();
    private ArrayList<String> mMemberList = new ArrayList();
    private HashMap<String, Integer> mMemberLoginStatusMap = new HashMap();
    private HashMap<String, String> mMemberNickNameMap = new HashMap();
    private long mThreadID;
    private LayoutParams param;
    private Drawable sDefaultContactImage;

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case 9527:
                    if (cursor == null || !cursor.moveToFirst()) {
                        RcsGroupMemberAdapter.this.changeCursor(cursor);
                        return;
                    }
                    RcsGroupMemberAdapter.this.mMemberList.clear();
                    do {
                        try {
                            String addr = NumberUtils.normalizeNumber(cursor.getString(cursor.getColumnIndexOrThrow("rcs_id")));
                            String nickName = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
                            boolean isInMap = false;
                            for (String addrInMap : RcsGroupMemberAdapter.this.mMemberNickNameMap.keySet()) {
                                if (AddrMatcher.isNumberMatch(addrInMap, addr) > 0) {
                                    isInMap = true;
                                    if (!isInMap) {
                                        RcsGroupMemberAdapter.this.mMemberNickNameMap.put(addr, nickName);
                                    }
                                    RcsGroupMemberAdapter.this.mMemberList.add(addr);
                                }
                            }
                            if (isInMap) {
                                RcsGroupMemberAdapter.this.mMemberNickNameMap.put(addr, nickName);
                            }
                            RcsGroupMemberAdapter.this.mMemberList.add(addr);
                        } catch (RuntimeException e) {
                            MLog.e("RcsGroupMemberAdapter", "onQueryComplete" + e.toString());
                        }
                    } while (cursor.moveToNext());
                    RcsGroupMemberAdapter.this.changeCursor(cursor);
                    return;
                case 9528:
                    if (cursor == null || !cursor.moveToFirst()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        RcsGroupMemberAdapter.this.mBackgroundQueryHandler.startQuery(9527, null, RcsGroupChatComposeMessageFragment.sMemberUri, RcsGroupMemberAdapter.MEMBER_PROJECTION, "thread_id = ?", new String[]{String.valueOf(RcsGroupMemberAdapter.this.mThreadID)}, null);
                        return;
                    }
                    RcsGroupMemberAdapter.this.mMemberNickNameMap.clear();
                    do {
                        try {
                            RcsGroupMemberAdapter.this.mMemberNickNameMap.put(NumberUtils.normalizeNumber(cursor.getString(cursor.getColumnIndexOrThrow("rcs_id"))), cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
                        } catch (RuntimeException e2) {
                            MLog.e("RcsGroupMemberAdapter", "onQueryComplete get Nick name error");
                        }
                    } while (cursor.moveToNext());
                    if (cursor != null) {
                        cursor.close();
                    }
                    RcsGroupMemberAdapter.this.mBackgroundQueryHandler.startQuery(9527, null, RcsGroupChatComposeMessageFragment.sMemberUri, RcsGroupMemberAdapter.MEMBER_PROJECTION, "thread_id = ?", new String[]{String.valueOf(RcsGroupMemberAdapter.this.mThreadID)}, null);
                    return;
                default:
                    return;
            }
        }
    }

    private class ContactLoader extends Handler {
        public ContactLoader(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof TaskInfo)) {
                TaskInfo taskInfo = msg.obj;
                RcsGroupMemberAdapter.this.doLoadTask(taskInfo);
                Message newMsg = RcsGroupMemberAdapter.this.mContextHandler.obtainMessage(0);
                newMsg.obj = taskInfo;
                RcsGroupMemberAdapter.this.mContextHandler.sendMessage(newMsg);
            }
        }
    }

    private static class MemberInfo {
        private String address;
        private Bitmap bitmap;
        private Contact contact;
        private String name;

        private MemberInfo() {
            this.name = null;
            this.address = null;
            this.bitmap = null;
            this.contact = null;
        }
    }

    private static class TaskInfo {
        private String address = null;
        private MemberInfo memberInfo = null;
        private View view = null;

        public TaskInfo(String address, View view) {
            this.address = address;
            this.view = view;
        }
    }

    public RcsGroupMemberAdapter(Activity activity, Cursor c, boolean autoRequery, long threadID) {
        super(activity, c, autoRequery);
        this.mHandlerThread.start();
        this.mContactLoader = new ContactLoader(this.mHandlerThread.getLooper());
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(this.mActivity);
        this.mBackgroundQueryHandler = new BackgroundQueryHandler(this.mActivity.getContentResolver());
        this.sDefaultContactImage = new BitmapDrawable(this.mActivity.getResources(), BitmapFactory.decodeStream(this.mActivity.getResources().openRawResource(R.drawable.rcs_group_member_head_default)));
        this.param = new LayoutParams(this.sDefaultContactImage.getIntrinsicWidth(), this.sDefaultContactImage.getIntrinsicHeight());
        this.mThreadID = threadID;
        startMemberQuery();
    }

    public ArrayList<String> getMemberList() {
        return this.mMemberList;
    }

    public int getCount() {
        if (this.mIsShowExtBtn) {
            return super.getCount() + 1;
        }
        return super.getCount();
    }

    public int getItemViewType(int position) {
        if (this.mIsShowExtBtn && position == getCount() - 1) {
            return 1;
        }
        return 0;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor != null) {
            String addr = cursor.getString(cursor.getColumnIndexOrThrow("rcs_id"));
            if (cursor.getPosition() == 0) {
                TaskInfo info = new TaskInfo(addr, view);
                doLoadTask(info);
                bindContact(view, info.memberInfo);
                return;
            }
            MemberInfo info2 = (MemberInfo) this.mMemberInfoCache.get(addr);
            if (info2 != null) {
                bindContact(view, info2);
            } else {
                Message msg = this.mContactLoader.obtainMessage(0);
                msg.obj = new TaskInfo(addr, view);
                this.mContactLoader.sendMessage(msg);
            }
            if (RcsTransaction.isShowGroupDetailsStatusIcon()) {
                ImageView loginStatusView = (ImageView) view.findViewById(R.id.rcs_longin_status);
                if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 1) {
                    loginStatusView.setImageResource(R.drawable.rcs_core_notif_on_icon);
                } else {
                    loginStatusView.setImageResource(R.drawable.rcs_core_notif_outline_icon);
                }
            }
        } else {
            QuickContactBadge qcb = (QuickContactBadge) view.findViewById(R.id.head_icon);
            qcb.setImageResource(R.drawable.rcs_ic_group_member_add);
            qcb.setBackground(context.getResources().getDrawable(R.drawable.rcs_group_chat_add_button_background));
            qcb.setContentDescription(this.mActivity.getResources().getString(R.string.rcs_add_new_members));
            qcb.setScaleType(ScaleType.CENTER);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.rcs_group_chat_head_size) - context.getResources().getDimensionPixelSize(R.dimen.rcs_group_chat_head_real_size);
            qcb.setLayoutParams(new LayoutParams(this.sDefaultContactImage.getIntrinsicWidth() - (padding * 2), this.sDefaultContactImage.getIntrinsicHeight() - (padding * 2)));
            ((TextView) view.findViewById(R.id.name_text)).setText(" ");
            view.findViewById(R.id.rcs_longin_status).setVisibility(8);
            if (this.mIsShowExtBtn) {
                qcb.setVisibility(0);
                qcb.setOnClickListener(this);
            } else {
                qcb.setVisibility(8);
            }
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return this.mInflater.inflate(R.layout.rcs_groupchat_member_item, null);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) != 1) {
            return super.getView(position, convertView, parent);
        }
        View v;
        if (convertView == null) {
            v = newView(this.mContext, null, parent);
        } else {
            v = convertView;
        }
        bindView(v, this.mContext, null);
        return v;
    }

    protected void onContentChanged() {
        cancelLoadTask();
        startMemberQuery();
    }

    public void notifyDataSetChanged() {
        cancelLoadTask();
        super.notifyDataSetChanged();
    }

    private void cancelLoadTask() {
        this.mContactLoader.removeMessages(0);
        this.mContextHandler.removeMessages(0);
        this.mMemberInfoCache.clear();
    }

    private void startMemberQuery() {
        this.mBackgroundQueryHandler.cancelOperation(9527);
        this.mBackgroundQueryHandler.cancelOperation(9528);
        this.mBackgroundQueryHandler.startQuery(9528, null, RcsGroupChatComposeMessageFragment.sMemberUri, MEMBER_PROJECTION, "thread_id = 0", null, null);
    }

    public static Bitmap createRoundPhoto(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, size, size);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-16777216);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Config config;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_icon:
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.item/rcs_contacts_for_message");
                contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
                contactIntent.putExtra("from_activity_key", 3);
                contactIntent.putExtra("member_size_of_exsiting_group", this.mMemberList.size());
                contactIntent.putStringArrayListExtra("list_phonenumber_from_forward", this.mMemberList);
                this.mFragment.startActivityForResult(contactIntent, 1);
                return;
            default:
                return;
        }
    }

    public void setShowExtBtn(boolean state) {
        this.mIsShowExtBtn = state;
        notifyDataSetChanged();
    }

    private void bindContact(View view, final MemberInfo info) {
        QuickContactBadge qcb = (QuickContactBadge) view.findViewById(R.id.head_icon);
        TextView tv = (TextView) view.findViewById(R.id.name_text);
        if (info.contact.existsInDatabase()) {
            qcb.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    MLog.i("RcsGroupMemberAdapter", "click group chat member exists in database contactId");
                    Intent intent = new Intent("android.intent.action.VIEW", info.contact.getUri());
                    intent.putExtra("phoneNumber", info.address);
                    RcsGroupMemberAdapter.this.mActivity.startActivity(intent);
                }
            });
        } else {
            qcb.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    MLog.i("RcsGroupMemberAdapter", "click group chat member not exists in database");
                    RcsProfile.startContactDetailActivityFromGroupChat(info.name, info.address, RcsGroupMemberAdapter.this.mActivity);
                }
            });
        }
        qcb.setImageBitmap(info.bitmap);
        qcb.setContentDescription(this.mActivity.getResources().getString(R.string.rcs_contact_photo));
        Resources resource = qcb.getContext().getResources();
        int padding = resource.getDimensionPixelSize(R.dimen.rcs_group_chat_head_size) - resource.getDimensionPixelSize(R.dimen.rcs_group_chat_head_real_size);
        qcb.setPadding(padding, padding, padding, padding);
        qcb.setBackground(null);
        qcb.setLayoutParams(this.param);
        tv.setText(info.name);
    }

    public void removeLoaderTask() {
        this.mHandlerThread.quit();
        cancelLoadTask();
    }

    private void doLoadTask(TaskInfo taskInfo) {
        MemberInfo memberInfo = new MemberInfo();
        String nickName = (String) this.mMemberNickNameMap.get(NumberUtils.normalizeNumber(taskInfo.address));
        memberInfo.contact = Contact.get(NumberUtils.normalizeNumber(taskInfo.address), true);
        if (memberInfo.contact != null) {
            Drawable avatarDrawable = memberInfo.contact.getAvatar(this.mActivity, this.sDefaultContactImage);
            if (avatarDrawable == this.sDefaultContactImage) {
                avatarDrawable = ResEx.self().getAvtarDefault(memberInfo.contact);
            }
            memberInfo.bitmap = createRoundPhoto(drawableToBitmap(avatarDrawable));
            if (memberInfo.bitmap == null) {
                memberInfo.bitmap = drawableToBitmap(avatarDrawable);
            }
            memberInfo.name = memberInfo.contact.getOnlyName();
            if (TextUtils.isEmpty(memberInfo.name)) {
                memberInfo.name = taskInfo.address;
            }
            if (memberInfo.contact.existsInDatabase()) {
                memberInfo.address = memberInfo.contact.getNumber();
            } else {
                memberInfo.address = taskInfo.address;
                if (TextUtils.isEmpty(nickName) || !RcsProfile.isGroupChatNicknameEnabled()) {
                    memberInfo.name = taskInfo.address;
                } else {
                    memberInfo.name = nickName;
                }
            }
        }
        taskInfo.memberInfo = memberInfo;
    }

    public void setFragment(Fragment f) {
        this.mFragment = f;
    }
}
