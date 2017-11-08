package com.android.contacts.hap.list;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.activities.ViewJoinContactsActivity;
import com.android.contacts.hap.list.RawContactsPhotoFetcher.RawContactsPhotoFetchListener;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.MapCore;
import com.google.android.collect.Lists;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewJoinedContactsFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener, RawContactsPhotoFetchListener {
    protected int REQUEST_CODE_JOIN = MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER;
    private UnjoinContactTask mAsyncUnjoinOperation;
    private long mContactId;
    private ContactPhotoManager mContactPhotoManager;
    private Activity mContext;
    private Handler mHandler = new Handler();
    private JoinAdapter mJoinAdapter;
    private HashMap<Long, Long> mPhotoIdForRawContactIdMap;
    private ArrayList<Long> mRawContactIds = new ArrayList();

    private interface ContactQuery {
        public static final String[] COLUMNS = new String[]{"_id", "display_name", "account_type"};
    }

    private class JoinAdapter extends CursorAdapter {
        public JoinAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        public void bindView(View aView, Context aContext, Cursor aContactsCursor) {
            int i = false;
            ViewHolder lHolder = (ViewHolder) aView.getTag();
            lHolder.mRawContactId = Long.valueOf(aContactsCursor.getLong(0));
            if (ViewJoinedContactsFragment.this.mPhotoIdForRawContactIdMap == null) {
                ViewJoinedContactsFragment.this.mContactPhotoManager.loadThumbnail(lHolder.mContactImage, 0, false, null, -1);
            } else if (ViewJoinedContactsFragment.this.mPhotoIdForRawContactIdMap.containsKey(lHolder.mRawContactId)) {
                ViewJoinedContactsFragment.this.mContactPhotoManager.loadThumbnail(lHolder.mContactImage, ((Long) ViewJoinedContactsFragment.this.mPhotoIdForRawContactIdMap.get(lHolder.mRawContactId)).longValue(), false, null, 0);
            } else {
                ViewJoinedContactsFragment.this.mContactPhotoManager.loadThumbnail(lHolder.mContactImage, 0, false, null, -1);
            }
            String dispName = aContactsCursor.getString(1);
            if (dispName == null || "".equals(dispName)) {
                dispName = aContext.getString(R.string.missing_name);
            }
            lHolder.mName.setText(dispName);
            String accountType = aContactsCursor.getString(2);
            if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
                lHolder.mAccountImage.setImageBitmap(AccountsDataManager.getInstance(this.mContext).getAccountIcon(accountType));
            }
            ImageView -get2 = lHolder.mDeleteImage;
            if (aContactsCursor.getCount() <= 1) {
                i = 8;
            }
            -get2.setVisibility(i);
        }

        public View newView(Context aContext, Cursor aCursor, ViewGroup aParent) {
            View view = ViewJoinedContactsFragment.this.getActivity().getLayoutInflater().inflate(R.layout.joinedcontacts_content, null);
            ViewHolder lHolder = new ViewHolder();
            lHolder.mContactImage = (ImageView) view.findViewById(R.id.contactImage);
            lHolder.mAccountImage = (ImageView) view.findViewById(R.id.accountImage);
            lHolder.mDeleteImage = (ImageView) view.findViewById(R.id.unjoin);
            lHolder.mName = (TextView) view.findViewById(R.id.NameForCopy);
            view.setTag(lHolder);
            return view;
        }
    }

    private class UnjoinContactTask extends AsyncTask<Object, Object, Object> {
        private Long mRawContactID;

        public UnjoinContactTask(Long aRawContactID) {
            this.mRawContactID = aRawContactID;
        }

        protected Object doInBackground(Object... params) {
            ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
            ViewJoinedContactsFragment.this.buildSplitContactDiff(diff, this.mRawContactID);
            long rawContactId = ((Long) ViewJoinedContactsFragment.this.mRawContactIds.get(0)).longValue();
            if (rawContactId == this.mRawContactID.longValue()) {
                rawContactId = ((Long) ViewJoinedContactsFragment.this.mRawContactIds.get(1)).longValue();
            }
            if (diff.isEmpty()) {
                return null;
            }
            try {
                ViewJoinedContactsFragment.this.mContext.getContentResolver().applyBatch("com.android.contacts", diff);
            } catch (RemoteException e) {
                HwLog.e("ViewJoinedContactsFragment", "Problem persisting user edits", e);
            } catch (OperationApplicationException e2) {
                HwLog.w("ViewJoinedContactsFragment", "Version consistency failed, re-parenting: " + e2.toString());
            }
            Uri uri = RawContacts.getContactLookupUri(ViewJoinedContactsFragment.this.mContext.getContentResolver(), ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
            Intent intent = new Intent();
            intent.setData(uri);
            return intent;
        }

        protected void onPostExecute(Object result) {
            if (result != null && !ViewJoinedContactsFragment.this.isDetached() && !ViewJoinedContactsFragment.this.isRemoving()) {
                ViewJoinedContactsFragment.this.mJoinAdapter.swapCursor(null);
                ViewJoinedContactsFragment.this.onServiceCompleted((Intent) result);
            }
        }
    }

    private static class ViewHolder {
        private ImageView mAccountImage;
        private ImageView mContactImage;
        private ImageView mDeleteImage;
        private TextView mName;
        private Long mRawContactId;

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle;
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (savedInstanceState != null) {
            this.mContactId = savedInstanceState.getLong("contact_id");
            bundle = savedInstanceState;
        } else {
            this.mContactId = intent.getLongExtra("ContactId", 0);
            bundle = new Bundle();
        }
        this.mContactPhotoManager = ContactPhotoManager.getInstance(this.mContext);
        getLoaderManager().restartLoader(0, bundle, this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("contact_id", this.mContactId);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.joinedview, container, false);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.add_contact_dlg_title);
        this.mJoinAdapter = new JoinAdapter(this.mContext, null, false);
        return view;
    }

    public void onViewCreated(View aView, Bundle aSavedInstanceState) {
        super.onViewCreated(aView, aSavedInstanceState);
        aView.findViewById(R.id.addContactHeader).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("com.android.contacts.action.JOIN_CONTACT");
                intent.putExtra("com.android.contacts.action.CONTACT_ID", ViewJoinedContactsFragment.this.mContactId);
                ViewJoinedContactsFragment.this.startActivityForResult(intent, ViewJoinedContactsFragment.this.REQUEST_CODE_JOIN);
            }
        });
        getListView().setAdapter(this.mJoinAdapter);
        getListView().setOnItemClickListener(this);
    }

    public Loader<Cursor> onCreateLoader(int aLoaderId, Bundle aBundle) {
        StringBuilder wherClause = new StringBuilder();
        wherClause.append("contact_id").append(" = ").append(this.mContactId);
        return new CursorLoader(this.mContext, RawContacts.CONTENT_URI, ContactQuery.COLUMNS, wherClause.toString(), null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor aCursor) {
        if (aCursor != null) {
            updateRawContacts(this.mContactId, this.mContext.getContentResolver(), aCursor);
            this.mJoinAdapter.swapCursor(aCursor);
            RawContactsPhotoFetcher photoFetcher = new RawContactsPhotoFetcher(null, this.mContext.getContentResolver(), (Long[]) this.mRawContactIds.toArray(new Long[this.mRawContactIds.size()]));
            photoFetcher.setRawContactsPhotoFetchListener(this);
            photoFetcher.start();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onActivityResult(int requestCode, int aResultCode, Intent aIntent) {
        super.onActivityResult(requestCode, aResultCode, aIntent);
        if (requestCode == this.REQUEST_CODE_JOIN && aResultCode == -1 && aIntent != null) {
            joinAggregate(ContentUris.parseId(aIntent.getData()));
        }
    }

    private void joinAggregate(long contactId) {
        this.mContext.startService(ContactSaveService.createJoinContactsIntent(this.mContext, this.mContactId, contactId, true, ViewJoinContactsActivity.class, "joinCompleted"));
    }

    public void onServiceCompleted(Intent callbackIntent) {
        if (callbackIntent != null) {
            this.mContactId = ContentUris.parseId(callbackIntent.getData());
            if (getActivity() != null) {
                getLoaderManager().restartLoader(0, new Bundle(), this);
            }
        }
    }

    private synchronized void buildSplitContactDiff(ArrayList<ContentProviderOperation> aDiff, Long aRawContactIdSeperate) {
        int count = this.mRawContactIds.size();
        for (int i = 0; i < count - 1; i++) {
            Long id1 = (Long) this.mRawContactIds.get(i);
            for (int j = i + 1; j < count; j++) {
                Long id2 = (Long) this.mRawContactIds.get(j);
                if (i != j && (id1.equals(aRawContactIdSeperate) || id2.equals(aRawContactIdSeperate))) {
                    Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
                    builder.withValue("type", Integer.valueOf(2));
                    builder.withValue("raw_contact_id1", id1);
                    builder.withValue("raw_contact_id2", id2);
                    aDiff.add(builder.build());
                }
            }
        }
    }

    public void updateRawContacts(long aContactId, ContentResolver aContentResolver, Cursor aCursor) {
        if (aCursor != null && aCursor.moveToFirst()) {
            int lSize = aCursor.getCount();
            synchronized (this.mRawContactIds) {
                this.mRawContactIds.clear();
                for (int i = 0; i < lSize; i++) {
                    this.mRawContactIds.add(Long.valueOf(aCursor.getLong(aCursor.getColumnIndex("_id"))));
                    aCursor.moveToNext();
                }
            }
            aCursor.moveToFirst();
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View aView, int aPosition, long id) {
        if (getListView().getCount() > 1) {
            Long lRawContactID = ((ViewHolder) aView.getTag()).mRawContactId;
            getLoaderManager().destroyLoader(0);
            this.mAsyncUnjoinOperation = new UnjoinContactTask(lRawContactID);
            this.mAsyncUnjoinOperation.execute(new Object[0]);
        }
    }

    public void onPhotoFetchComplete(HashMap<Long, Long> aPhotoIdForRawContactIdMap) {
        this.mPhotoIdForRawContactIdMap = aPhotoIdForRawContactIdMap;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (ViewJoinedContactsFragment.this.mJoinAdapter != null) {
                    ViewJoinedContactsFragment.this.mJoinAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
