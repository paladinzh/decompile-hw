package com.android.contacts.list;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.widget.AutoScrollListView;
import java.util.List;

public abstract class ContactBrowseListFragment extends ContactEntryListFragment<ContactListAdapter> {
    boolean hasClicked = false;
    private ContactLookupTask mContactLookupTask;
    private boolean mDelaySelection;
    private ContactListFilter mFilter;
    private Handler mHandler;
    private int mLastSelectedPosition = -1;
    protected OnContactBrowserActionListener mListener;
    private String mPersistentSelectionPrefix = "defaultContactBrowserSelection";
    private SharedPreferences mPrefs;
    private boolean mRefreshingContactUri;
    private long mSelectedContactDirectoryId;
    private long mSelectedContactId;
    private String mSelectedContactLookupKey;
    private Uri mSelectedContactUri;
    private boolean mSelectionPersistenceRequested;
    private boolean mSelectionRequired;
    private boolean mSelectionToScreenRequested;
    private boolean mSelectionVerified;
    private boolean mSmoothScrollRequested;
    private boolean mStartedLoading;

    private final class ContactLookupTask extends AsyncTask<Void, Void, Uri> {
        private boolean mIsCancelled;
        private final Uri mUri;

        protected android.net.Uri doInBackground(java.lang.Void... r13) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0096 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r12 = this;
            r11 = 0;
            r8 = 0;
            r2 = com.android.contacts.list.ContactBrowseListFragment.this;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = r2.getContext();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r0 = r2.getContentResolver();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = r12.mUri;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r1 = com.android.contacts.util.ContactLoaderUtils.ensureIsContactUri(r0, r2);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = 2;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = new java.lang.String[r2];	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = "_id";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2[r4] = r3;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = "lookup";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = 1;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2[r4] = r3;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r5 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r8 == 0) goto L_0x0046;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
        L_0x002a:
            r2 = r8.moveToFirst();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r2 == 0) goto L_0x0046;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
        L_0x0030:
            r2 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r6 = r8.getLong(r2);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = 1;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r10 = r8.getString(r2);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = 0;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r2 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1));	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r2 == 0) goto L_0x0046;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
        L_0x0040:
            r2 = android.text.TextUtils.isEmpty(r10);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r2 == 0) goto L_0x0068;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
        L_0x0046:
            r2 = "ContactList";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3.<init>();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = "Error: No contact ID or lookup key for contact ";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = r12.mUri;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.toString();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            com.android.contacts.util.HwLog.e(r2, r3);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r8 == 0) goto L_0x0067;
        L_0x0064:
            r8.close();
        L_0x0067:
            return r11;
        L_0x0068:
            r2 = android.provider.ContactsContract.Contacts.getLookupUri(r6, r10);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r8 == 0) goto L_0x0071;
        L_0x006e:
            r8.close();
        L_0x0071:
            return r2;
        L_0x0072:
            r9 = move-exception;
            r2 = "ContactList";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = new java.lang.StringBuilder;	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3.<init>();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = "uri exception: ";	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r4 = r9.toString();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.append(r4);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            r3 = r3.toString();	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            com.android.contacts.util.HwLog.e(r2, r3);	 Catch:{ IllegalArgumentException -> 0x0072, all -> 0x0097 }
            if (r8 == 0) goto L_0x0096;
        L_0x0093:
            r8.close();
        L_0x0096:
            return r11;
        L_0x0097:
            r2 = move-exception;
            if (r8 == 0) goto L_0x009d;
        L_0x009a:
            r8.close();
        L_0x009d:
            throw r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.list.ContactBrowseListFragment.ContactLookupTask.doInBackground(java.lang.Void[]):android.net.Uri");
        }

        public ContactLookupTask(Uri uri) {
            this.mUri = uri;
        }

        public void cancel() {
            super.cancel(true);
            this.mIsCancelled = true;
        }

        protected void onPostExecute(Uri uri) {
            if (!this.mIsCancelled && ContactBrowseListFragment.this.isAdded()) {
                ContactBrowseListFragment.this.onContactUriQueryFinished(uri);
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ContactBrowseListFragment.this.selectDefaultContact();
                    return;
                case 2:
                    ContactBrowseListFragment.this.hasClicked = false;
                    return;
                default:
                    return;
            }
        }
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new MyHandler();
        }
        return this.mHandler;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!isReplacable()) {
            this.mPrefs = SharePreferenceUtil.getDefaultSp_de(activity);
            restoreFilter();
            if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
                restoreSelectedUri(false);
            }
        }
    }

    public void setSearchMode(boolean flag) {
        if (isSearchMode() != flag) {
            if (!(flag || CommonUtilMethods.calcIfNeedSplitScreen())) {
                restoreSelectedUri(true);
            }
            super.setSearchMode(flag);
        }
    }

    public void setFilter(ContactListFilter filter) {
        setFilter(filter, true, false);
    }

    public void setFilter(ContactListFilter filter, boolean restoreSelectedUri, boolean isPreferenceChanged) {
        if (this.mFilter != null || filter != null) {
            if (isPreferenceChanged || this.mFilter == null || !this.mFilter.equals(filter)) {
                this.mFilter = filter;
                this.mLastSelectedPosition = -1;
                saveFilter();
                if (restoreSelectedUri) {
                    this.mSelectedContactUri = null;
                    restoreSelectedUri(true);
                }
                reloadData();
            }
        }
    }

    public ContactListFilter getFilter() {
        return this.mFilter;
    }

    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);
        if (savedState != null) {
            this.mFilter = (ContactListFilter) savedState.getParcelable("filter");
            this.mSelectedContactUri = (Uri) savedState.getParcelable("selectedUri");
            this.mSelectionVerified = savedState.getBoolean("selectionVerified");
            this.mLastSelectedPosition = savedState.getInt("lastSelected");
            parseSelectedContactUri();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("filter", this.mFilter);
        outState.putParcelable("selectedUri", this.mSelectedContactUri);
        outState.putBoolean("selectionVerified", this.mSelectionVerified);
        outState.putInt("lastSelected", this.mLastSelectedPosition);
    }

    protected void refreshSelectedContactUri() {
        if (this.mContactLookupTask != null) {
            this.mContactLookupTask.cancel();
        }
        if (isSelectionVisible()) {
            this.mRefreshingContactUri = true;
            if (this.mSelectedContactUri == null) {
                onContactUriQueryFinished(null);
                return;
            }
            if (this.mSelectedContactDirectoryId == 0 || this.mSelectedContactDirectoryId == 1) {
                this.mContactLookupTask = new ContactLookupTask(this.mSelectedContactUri);
                this.mContactLookupTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            } else {
                onContactUriQueryFinished(this.mSelectedContactUri);
            }
        }
    }

    protected void onContactUriQueryFinished(Uri uri) {
        this.mRefreshingContactUri = false;
        this.mSelectedContactUri = uri;
        parseSelectedContactUri();
        checkSelection();
    }

    public Uri getSelectedContactUri() {
        return this.mSelectedContactUri;
    }

    protected Uri getDefaultContactUri() {
        Uri contactUri = null;
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (this.mLastSelectedPosition != -1) {
            int count = adapter.getCount();
            int pos = this.mLastSelectedPosition;
            if (pos >= count && count > 0) {
                pos = count - 1;
            }
            contactUri = adapter.getContactUri(pos);
        }
        if (contactUri == null) {
            return adapter.getFirstContactUri();
        }
        return contactUri;
    }

    protected int getSelectedContactPosition() {
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null) {
            return adapter.getSelectedContactPosition();
        }
        return -1;
    }

    protected Uri getFristContactUri() {
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null) {
            return adapter.getFirstContactUri();
        }
        return null;
    }

    protected void refreshSelection() {
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null && TextUtils.isEmpty(adapter.getSelectedContactLookupKey())) {
            adapter.setSelectedContact(this.mSelectedContactDirectoryId, this.mSelectedContactLookupKey, this.mSelectedContactId);
            getListView().invalidateViews();
        }
    }

    protected int getContactPosition(long dirId, String LookupKey, long contactId) {
        if (LookupKey == null) {
            return -1;
        }
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter == null) {
            return -1;
        }
        return adapter.getSelectedContactPosition(dirId, LookupKey, contactId);
    }

    public void setSelectedContactUri(Uri uri) {
        setSelectedContactUri(uri, true, false, true, false);
    }

    public void setQueryString(String queryString, boolean delaySelection) {
        this.mDelaySelection = delaySelection;
        super.setQueryString(queryString, delaySelection);
    }

    public void setSelectionRequired(boolean required) {
        this.mSelectionRequired = required;
    }

    protected void setSelectedContactUri(Uri uri, boolean required, boolean smoothScroll, boolean persistent, boolean willReloadData) {
        this.mSmoothScrollRequested = smoothScroll;
        this.mSelectionToScreenRequested = true;
        if (this.mSelectedContactUri != null || uri == null) {
            if (this.mSelectedContactUri == null) {
                return;
            }
            if (this.mSelectedContactUri.equals(uri)) {
                return;
            }
        }
        this.mSelectionVerified = false;
        this.mSelectionRequired = required;
        this.mSelectionPersistenceRequested = persistent;
        this.mSelectedContactUri = uri;
        parseSelectedContactUri();
        if (!willReloadData) {
            ContactListAdapter adapter = (ContactListAdapter) getAdapter();
            if (adapter != null) {
                adapter.setSelectedContact(this.mSelectedContactDirectoryId, this.mSelectedContactLookupKey, this.mSelectedContactId);
                getListView().invalidateViews();
            }
        }
        refreshSelectedContactUri();
    }

    private void parseSelectedContactUri() {
        if (this.mSelectedContactUri != null) {
            long j;
            String directoryParam = this.mSelectedContactUri.getQueryParameter("directory");
            if (TextUtils.isEmpty(directoryParam)) {
                j = 0;
            } else {
                j = Long.parseLong(directoryParam);
            }
            this.mSelectedContactDirectoryId = j;
            if (this.mSelectedContactUri.toString().startsWith(Contacts.CONTENT_LOOKUP_URI.toString())) {
                List<String> pathSegments = this.mSelectedContactUri.getPathSegments();
                this.mSelectedContactLookupKey = Uri.encode((String) pathSegments.get(2));
                if (pathSegments.size() == 4) {
                    this.mSelectedContactId = ContentUris.parseId(this.mSelectedContactUri);
                    return;
                }
                return;
            } else if (!this.mSelectedContactUri.toString().startsWith(Contacts.CONTENT_URI.toString()) || this.mSelectedContactUri.getPathSegments().size() < 2) {
                this.mSelectedContactLookupKey = null;
                this.mSelectedContactId = 0;
                return;
            } else {
                this.mSelectedContactLookupKey = null;
                this.mSelectedContactId = ContentUris.parseId(this.mSelectedContactUri);
                return;
            }
        }
        this.mSelectedContactDirectoryId = 0;
        this.mSelectedContactLookupKey = null;
        this.mSelectedContactId = 0;
    }

    protected void configureAdapter() {
        super.configureAdapter();
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null) {
            if (this.mFilter != null) {
                adapter.setFilter(this.mFilter);
                if (this.mSelectionRequired || this.mFilter.isFilterTypeSame(-6)) {
                    adapter.setSelectedContact(this.mSelectedContactDirectoryId, this.mSelectedContactLookupKey, this.mSelectedContactId);
                }
            }
            adapter.setIncludeProfile(true);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            super.onLoadFinished((Loader) loader, data);
            this.mSelectionVerified = false;
            refreshSelectedContactUri();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void checkSelection() {
        if (!this.mSelectionVerified && !this.mRefreshingContactUri && !isLoadingDirectoryList()) {
            ContactListAdapter adapter = (ContactListAdapter) getAdapter();
            if (adapter != null) {
                boolean directoryLoading = true;
                int count = adapter.getPartitionCount();
                for (int i = 0; i < count; i++) {
                    Partition partition = adapter.getPartition(i);
                    if (partition instanceof DirectoryPartition) {
                        DirectoryPartition directory = (DirectoryPartition) partition;
                        if (directory.getDirectoryId() == this.mSelectedContactDirectoryId) {
                            directoryLoading = directory.isLoading();
                            break;
                        }
                    }
                }
                if (!directoryLoading) {
                    adapter.setSelectedContact(this.mSelectedContactDirectoryId, this.mSelectedContactLookupKey, this.mSelectedContactId);
                    int selectedPosition = adapter.getSelectedContactPosition();
                    if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                        this.mLastSelectedPosition = selectedPosition;
                        this.mSelectionVerified = true;
                        getListView().invalidateViews();
                        return;
                    }
                    if (selectedPosition != -1) {
                        this.mLastSelectedPosition = selectedPosition;
                    } else {
                        if (isSearchMode()) {
                            if (this.mDelaySelection) {
                                selectFirstFoundContactAfterDelay();
                                if (this.mListener != null) {
                                    this.mListener.onSelectionChange();
                                }
                                return;
                            }
                        } else if (this.mSelectionRequired) {
                            this.mSelectionRequired = false;
                            if (this.mFilter == null || !this.mFilter.isFilterTypeSame(-6)) {
                                notifyInvalidSelection();
                            } else {
                                reloadData();
                            }
                            return;
                        } else if (this.mFilter != null && this.mFilter.isFilterTypeSame(-6)) {
                            notifyInvalidSelection();
                            return;
                        }
                        saveSelectedUri(null);
                        selectDefaultContact();
                    }
                    this.mSelectionRequired = false;
                    this.mSelectionVerified = true;
                    if (this.mSelectionPersistenceRequested) {
                        saveSelectedUri(this.mSelectedContactUri);
                        this.mSelectionPersistenceRequested = false;
                    }
                    if (this.mSelectionToScreenRequested) {
                        requestSelectionToScreen(selectedPosition);
                    }
                    getListView().invalidateViews();
                    if (this.mListener != null) {
                        this.mListener.onSelectionChange();
                    }
                }
            }
        }
    }

    public void selectFirstFoundContactAfterDelay() {
        Handler handler = getHandler();
        handler.removeMessages(1);
        String queryString = getQueryString();
        if (queryString == null || queryString.length() < 2) {
            setSelectedContactUri(null, false, false, false, false);
        } else {
            handler.sendEmptyMessageDelayed(1, 500);
        }
    }

    protected void selectDefaultContact() {
        Uri contactUri = null;
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (this.mLastSelectedPosition != -1) {
            int count = adapter.getCount();
            int pos = this.mLastSelectedPosition;
            if (pos >= count && count > 0) {
                pos = count - 1;
            }
            contactUri = adapter.getContactUri(pos);
        }
        if (contactUri == null) {
            contactUri = adapter.getFirstContactUri();
        }
        setSelectedContactUri(contactUri, false, this.mSmoothScrollRequested, false, false);
    }

    protected void requestSelectionToScreen(int selectedPosition) {
        if (selectedPosition != -1) {
            AutoScrollListView listView = (AutoScrollListView) getListView();
            listView.requestPositionToScreen(listView.getHeaderViewsCount() + selectedPosition, this.mSmoothScrollRequested);
            this.mSelectionToScreenRequested = false;
        }
    }

    public boolean isLoading() {
        return !this.mRefreshingContactUri ? super.isLoading() : true;
    }

    protected void startLoading() {
        this.mStartedLoading = true;
        this.mSelectionVerified = false;
        super.startLoading();
    }

    public void reloadDataAndSetSelectedUri(Uri uri) {
        setSelectedContactUri(uri, true, true, true, true);
        reloadData();
    }

    public void reloadData() {
        if (this.mStartedLoading) {
            this.mSelectionVerified = false;
            this.mLastSelectedPosition = -1;
            super.reloadData();
        }
    }

    public void setOnContactListActionListener(OnContactBrowserActionListener listener) {
        this.mListener = listener;
    }

    public void viewContact(Uri contactUri, boolean isEnterpriseContact) {
        if (!this.hasClicked) {
            this.hasClicked = true;
            setSelectedContactUri(contactUri, false, false, true, false);
            if (this.mListener != null) {
                this.mListener.onViewContactAction(contactUri, isEnterpriseContact);
            }
            getHandler().sendEmptyMessageDelayed(2, 500);
        }
    }

    public void editContact(Uri contactUri) {
        if (this.mListener != null) {
            this.mListener.onEditContactAction(contactUri);
        }
    }

    public void deleteContact(Uri contactUri) {
        if (this.mListener != null) {
            this.mListener.onDeleteContactAction(contactUri);
        }
    }

    private void notifyInvalidSelection() {
        if (this.mListener != null) {
            this.mListener.onInvalidSelection();
        }
    }

    private void saveSelectedUri(Uri contactUri) {
        if (!isSearchMode() && this.mPrefs != null) {
            ContactListFilter.storeToPreferences(this.mPrefs, this.mFilter);
            Editor editor = this.mPrefs.edit();
            if (contactUri == null) {
                editor.remove(getPersistentSelectionKey());
            } else {
                editor.putString(getPersistentSelectionKey(), contactUri.toString());
            }
            editor.apply();
        }
    }

    private void restoreSelectedUri(boolean willReloadData) {
        if (!this.mSelectionRequired) {
            String selectedUri = this.mPrefs == null ? null : this.mPrefs.getString(getPersistentSelectionKey(), null);
            if (selectedUri == null) {
                setSelectedContactUri(null, false, false, false, willReloadData);
            } else {
                setSelectedContactUri(Uri.parse(selectedUri), false, false, false, willReloadData);
            }
        }
    }

    private void saveFilter() {
        if (this.mPrefs != null) {
            ContactListFilter.storeToPreferences(this.mPrefs, this.mFilter);
        }
    }

    private void restoreFilter() {
        if (this.mPrefs != null) {
            this.mFilter = ContactListFilter.restoreDefaultPreferences(this.mPrefs);
        }
    }

    private String getPersistentSelectionKey() {
        if (this.mFilter == null) {
            return this.mPersistentSelectionPrefix;
        }
        return this.mPersistentSelectionPrefix + "-" + this.mFilter.getId();
    }
}
