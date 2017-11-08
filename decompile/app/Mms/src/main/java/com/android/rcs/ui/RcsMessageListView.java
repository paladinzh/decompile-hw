package com.android.rcs.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.MessageListView;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwCustFavoritesUtils;
import com.huawei.rcs.ui.RcsFileTransDataHander;
import com.huawei.rcs.ui.RcsImageCache;
import com.huawei.rcs.util.RcsFavoritesUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RcsMessageListView {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private IHwCustMessageListViewCallback mCallback;
    private Context mContext = null;

    public interface IHwCustMessageListViewCallback {
        long getMsgIdFromTypeId(String str);

        String getMsgTypeFromTypeId(String str);
    }

    public RcsMessageListView(Context context) {
        this.mContext = context;
    }

    public void setAllSelectedPosition(boolean selected, MessageListView msgListView) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (selected) {
                HashSet<Integer> newSelected = new HashSet();
                for (int i = 0; i < msgListView.getCount(); i++) {
                    newSelected.add(Integer.valueOf(i));
                }
                msgListView.getRecorder().getRcsSelectRecorder().replacePosition(newSelected);
            } else {
                msgListView.getRecorder().getRcsSelectRecorder().clearPosition();
            }
            setAllViewsChecked(selected, msgListView);
        }
    }

    private void setAllViewsChecked(boolean selected, MessageListView msgListView) {
        for (int index = 0; index < msgListView.getChildCount(); index++) {
            boolean isMms;
            MessageListItem listItem = (MessageListItem) msgListView.getChildAt(index);
            if (3 == msgListView.getViewMode()) {
                isMms = listItem.getMessageItem().isMms();
            } else {
                isMms = false;
            }
            if (!isMms) {
                listItem.setChecked(true, true);
            }
        }
    }

    public void setHwCustCallback(IHwCustMessageListViewCallback callback) {
        this.mCallback = callback;
    }

    public List<Long> getChatList(HashSet<String> selectedMsgItems) {
        List<Long> idListChat = new ArrayList();
        if (!this.isRcsOn) {
            return idListChat;
        }
        for (String sid : selectedMsgItems) {
            if ("chat".equals(this.mCallback.getMsgTypeFromTypeId(sid))) {
                idListChat.add(Long.valueOf(this.mCallback.getMsgIdFromTypeId(sid)));
            }
        }
        return idListChat;
    }

    public void deleteRcsMsgPop(AsyncQueryHandlerEx handler, int token, List<Long> idListChat) {
        if (this.isRcsOn && idListChat.size() > 0) {
            cancelRcsFtMsgBeforeDelete(idListChat);
            Uri uri = Uri.parse("content://rcsim/chat_multy/").buildUpon().appendEncodedPath(String.valueOf(idListChat.get(0))).build();
            StringBuilder selectionFt = new StringBuilder(" msg_id IN ( ").append(idListChat.get(0));
            StringBuilder selection = new StringBuilder(" _id IN ( ").append(idListChat.get(0));
            for (int i = 1; i < idListChat.size(); i++) {
                selection.append(", ").append(idListChat.get(i));
                selectionFt.append(", ").append(idListChat.get(i));
            }
            selectionFt.append(" ) AND chat_type = 1");
            selection.append(" ) ");
            handler.startDelete(token, null, uri, selection.toString(), null);
            SqliteWrapper.delete(this.mContext, Uri.parse("content://rcsim/file_trans").buildUpon().build(), selectionFt.toString(), null);
            removewMsgListItemImageCache(idListChat);
        }
    }

    public void deleteRcsMsg(AsyncQueryHandlerEx handler, int token, HashSet<String> selectedMsgItems) {
        if (this.isRcsOn) {
            List<Long> idListChat = getChatList(selectedMsgItems);
            if (idListChat.size() > 0) {
                cancelRcsFtMsgBeforeDelete(idListChat);
                Uri uri = Uri.parse("content://rcsim/chat_multy/").buildUpon().appendEncodedPath(String.valueOf(idListChat.get(0))).build();
                StringBuilder selection = new StringBuilder(" _id IN ( ").append(idListChat.get(0));
                StringBuilder selectionFt = new StringBuilder(" msg_id IN ( ").append(idListChat.get(0));
                for (int i = 1; i < idListChat.size(); i++) {
                    selection.append(", ").append(idListChat.get(i));
                    selectionFt.append(", ").append(idListChat.get(i));
                }
                selection.append(" ) ");
                selectionFt.append(" ) AND chat_type = 1");
                handler.startDelete(token, null, uri, selection.toString(), null);
                SqliteWrapper.delete(this.mContext, Uri.parse("content://rcsim/file_trans").buildUpon().build(), selectionFt.toString(), null);
                removewMsgListItemImageCache(idListChat);
            }
        }
    }

    private void removewMsgListItemImageCache(List<Long> idListChat) {
        if (this.mContext != null && (this.mContext instanceof Activity)) {
            RcsImageCache imageCache = RcsImageCache.getInstance(((Activity) this.mContext).getFragmentManager(), this.mContext);
            for (Long msgId : idListChat) {
                imageCache.removeBitmapCache(RcsUtility.getBitmapFromMemCacheKey(msgId.longValue(), 1));
            }
        }
    }

    private void cancelRcsFtMsgBeforeDelete(List<Long> idListChat) {
        StringBuilder ids = new StringBuilder(" msg_id IN ( ").append(idListChat.get(0));
        for (int i = 1; i < idListChat.size(); i++) {
            ids.append(", ").append(idListChat.get(i));
        }
        ids.append(" ) ");
        String selection = "_id IN( select msg_id from file_trans where" + ids + " AND transfer_status IN  (" + 1000 + "," + 1007 + "))";
        MLog.d("RcsMessageListView", "cancelRcsFtMsgBeforeDelete selection=" + selection);
        Cursor cursorCancel = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/chat"), new String[]{"sdk_sms_id"}, selection, null, null);
        if (cursorCancel != null && cursorCancel.getCount() != 0) {
            cancelRejectFtMsg(cursorCancel, true);
        } else if (cursorCancel != null) {
            cursorCancel.close();
        }
        String selectionReject = "_id IN( select msg_id from file_trans where" + ids + " AND transfer_status IN  (" + Place.TYPE_POSTAL_TOWN + "))";
        MLog.d("RcsMessageListView", "cancelRcsFtMsgBeforeDelete selectionReject=" + selectionReject);
        Cursor cursorReject = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/chat"), new String[]{"sdk_sms_id"}, selectionReject, null, null);
        if (cursorReject != null && cursorReject.getCount() != 0) {
            cancelRejectFtMsg(cursorReject, false);
        } else if (cursorReject != null) {
            cursorReject.close();
        }
    }

    private static void cancelRejectFtMsg(final Cursor c, final boolean optCode) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        long sdkId = -c.getLong(c.getColumnIndexOrThrow("sdk_sms_id"));
                        if (optCode) {
                            RcsTransaction.cancelFT(sdkId, true, 1);
                        } else {
                            RcsTransaction.rejectFile(sdkId, 1);
                        }
                        c.moveToNext();
                    }
                } catch (RuntimeException e) {
                    MLog.e("RcsMessageListView", "cursor unknowable error");
                } finally {
                    c.close();
                }
            }
        }).start();
    }

    public long getSmsThreadid(long oldThreadId, List<Long> idListSms) {
        if (!this.isRcsOn) {
            return oldThreadId;
        }
        Long thread_id = Long.valueOf(0);
        String str = null;
        Cursor c = SqliteWrapper.query(this.mContext, ContentUris.withAppendedId(Uri.parse("content://sms/"), ((Long) idListSms.get(0)).longValue()), null, null, null, null);
        if (c != null) {
            try {
                if (c.getCount() > 0 && c.moveToFirst()) {
                    str = c.getString(2);
                    thread_id = Long.valueOf(c.getLong(1));
                    MLog.i("RcsMessageListView", "multdelete address = *****thread_id = " + thread_id);
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (c != null) {
            c.close();
        }
        if (thread_id.longValue() == 0 && RcsConversationUtils.getHwCustUtils() != null) {
            thread_id = Long.valueOf(RcsConversationUtils.getHwCustUtils().querySmsThreadIdWithAddress(str, this.mContext));
        }
        return thread_id.longValue();
    }

    public void insertExtFav(List<Long> idListChat, List<Long> idListMms, AsyncQueryHandlerEx handler, int token1, int token2, int recipientSize) {
        if (this.isRcsOn && idListChat.size() > 0) {
            MLog.d("RcsMessageListView", "insertFav recipientSize = " + recipientSize);
            if (recipientSize == 1) {
                RcsFileTransDataHander.addNewFavTransTransRecord(this.mContext, idListChat, 1);
                if (idListMms.size() <= 0) {
                    token1 = token2;
                }
                handler.startInsert(token1, null, RcsFavoritesUtils.URI_FAV_IM, FavoritesUtils.getAddFavoritesContent(HwCustFavoritesUtils.OPER_TYPE_IM_MULTY, idListChat));
            } else if (recipientSize > 1) {
                RcsFileTransDataHander.addNewFavTransTransRecord(this.mContext, idListChat, 3);
                if (idListMms.size() <= 0) {
                    token1 = token2;
                }
                handler.startInsert(token1, null, RcsFavoritesUtils.URI_FAV_IM, FavoritesUtils.getAddFavoritesContent(HwCustFavoritesUtils.OPER_TYPE_MASS_MULTY, idListChat));
            }
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }

    public Long[] getSelectedItems(MessageListView mMsgListView, Long[] selectedItems) {
        if (!this.isRcsOn) {
            return selectedItems;
        }
        Integer[] selectedItemsExt = mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
        Long[] mSelectedItems = new Long[selectedItemsExt.length];
        for (int i = 0; i < selectedItemsExt.length; i++) {
            mSelectedItems[i] = Long.valueOf(selectedItemsExt[i].longValue());
        }
        return mSelectedItems;
    }

    public MessageItem getMessageItemWithIdAssigned(MessageListAdapter adapter, int selectItem, Cursor c, MessageItem messageItem) {
        if (!this.isRcsOn || adapter.getRcsMessageListAdapter() == null) {
            return messageItem;
        }
        return adapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(selectItem, c);
    }
}
