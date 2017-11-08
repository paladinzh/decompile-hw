package com.android.contacts.hap.editor;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import com.android.contacts.editor.RawContactEditorView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.editor.CompanyListAdater.CompanyListItemCache;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class PopupCompanyListTask extends AsyncTask<Void, Void, Cursor> {
    private CompanyListAdater mCompanyListAdater;
    private Context mContext;
    private EditText mEditorText;
    private String mInputString;
    private RawContactEditorView mRawContactEditor;
    Runnable mRunnable = new Runnable() {
        public void run() {
            if (PopupCompanyListTask.this.mCompanyListAdater != null) {
                PopupCompanyListTask.this.mCompanyListAdater.notifyDataSetChanged();
            }
        }
    };

    public PopupCompanyListTask(Context context, String inputString, RawContactEditorView rawContactEditor, EditText editorText, CompanyListAdater companyListAdater) {
        this.mContext = context;
        this.mInputString = inputString;
        this.mRawContactEditor = rawContactEditor;
        this.mEditorText = editorText;
        this.mCompanyListAdater = companyListAdater;
    }

    protected Cursor doInBackground(Void... params) {
        Cursor cursor = null;
        String formatString = this.mInputString.replace(HwCustPreloadContacts.EMPTY_STRING, "");
        String formatSpecialString = this.mInputString.replace("/", "//").replace("%", "/%").replace("_", "/_");
        try {
            if (!"".equals(formatString)) {
                StringBuilder sb = new StringBuilder();
                CommonUtilMethods.appendEscapedSQLString(sb, this.mInputString);
                String orderBy = "instr(company," + sb.toString() + "), " + "company";
                cursor = this.mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"company"}, "_id IN (SELECT DISTINCT _id FROM raw_contacts WHERE deleted=0 AND company LIKE '%'||?||'%' ESCAPE '/' GROUP BY company)", new String[]{formatSpecialString}, orderBy);
            }
        } catch (Exception e) {
            HwLog.e("PopupCompanyListTask", e.getMessage());
        }
        return cursor;
    }

    protected void onPostExecute(Cursor result) {
        super.onPostExecute(result);
        View view = this.mRawContactEditor.findViewById(R.id.company_popup);
        ListView listView = (ListView) this.mRawContactEditor.findViewById(R.id.company_list);
        if (listView != null) {
            listView.setFastScrollEnabled(true);
        }
        if (result == null || result.getCount() <= 0) {
            view.setVisibility(8);
            cancel(true);
            if (result != null) {
                result.close();
                return;
            }
            return;
        }
        int count = result.getCount();
        int[] position = new int[2];
        this.mEditorText.getLocationInWindow(position);
        int[] rawContactEditorPosition = new int[2];
        this.mRawContactEditor.getLocationInWindow(rawContactEditorPosition);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.topMargin = ((position[1] - rawContactEditorPosition[1]) + this.mEditorText.getHeight()) - this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_margin_hide_company_line);
        if (count <= 3) {
            lp.height = -2;
        } else {
            lp.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_company_pop_height);
        }
        view.setLayoutParams(lp);
        StatisticalHelper.report(1172);
        view.setVisibility(0);
        listView.setAdapter(this.mCompanyListAdater);
        this.mRawContactEditor.setPopupCompanyShowState(true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CompanyListItemCache companyListItem = (CompanyListItemCache) view.getTag();
                if (companyListItem != null) {
                    String companyName = companyListItem.getCompanyName();
                    if (!companyName.isEmpty()) {
                        PopupCompanyListTask.this.mRawContactEditor.setQueryCompanyInfoState(false);
                        PopupCompanyListTask.this.mRawContactEditor.findViewById(R.id.company_popup).setVisibility(8);
                        PopupCompanyListTask.this.mEditorText.setText(companyName);
                        PopupCompanyListTask.this.mEditorText.setSelection(companyName.length());
                        PopupCompanyListTask.this.mRawContactEditor.setPopupCompanyShowState(false);
                    }
                }
                StatisticalHelper.report(1173);
            }
        });
        this.mCompanyListAdater.setCursor(result);
        new Handler().postDelayed(this.mRunnable, 50);
    }

    protected void onCancelled(Cursor result) {
        super.onCancelled(result);
        if (result != null) {
            result.close();
        }
    }

    public void closeCursor() {
        if (this.mCompanyListAdater != null) {
            this.mCompanyListAdater.setCursor(null);
        }
    }
}
