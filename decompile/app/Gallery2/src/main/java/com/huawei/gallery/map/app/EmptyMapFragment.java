package com.huawei.gallery.map.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.AbstractGalleryFragment;

public class EmptyMapFragment extends AbstractGalleryFragment {
    private OnClickListener mAllowClickListener = new AllowClickListener();
    private OnClickListener mCancelClickListener = new CancelClickListener();

    private class AllowClickListener implements OnClickListener {
        private AllowClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(EmptyMapFragment.this.getActivity()).edit();
            editor.putBoolean(GallerySettings.KEY_USE_NETWORK, true);
            editor.apply();
            FragmentTransaction ft = EmptyMapFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
            AbstractGalleryFragment content = MapFragmentFactory.create(EmptyMapFragment.this.getActivity());
            content.setArguments(EmptyMapFragment.this.getArguments());
            ft.replace(R.id.fragment_container, content, MapAlbumActivity.class.getSimpleName());
            ft.commit();
            ReportToBigData.report(9);
        }
    }

    private static class CancelClickListener implements OnClickListener {
        private CancelClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.map_frag_empty, container, false);
        TextView networkTips = (TextView) fragView.findViewById(R.id.network_tips);
        if (networkTips != null) {
            networkTips.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    EmptyMapFragment.this.showUseNetworkDialog(EmptyMapFragment.this.getActivity());
                }
            });
        }
        return fragView;
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(34);
        GalleryActionBar actionBar = getGalleryActionBar();
        if (actionBar != null) {
            ActionMode am = actionBar.enterStandardTitleActionMode(false);
            am.setBothAction(Action.NONE, Action.NONE);
            am.setTitle((int) R.string.map_album);
            am.show();
        }
    }

    @SuppressLint({"InflateParams"})
    private void showUseNetworkDialog(Context context) {
        View tipsView = LayoutInflater.from(context).inflate(R.layout.use_network_dialog, null);
        AlertDialog tipsDialog = new Builder(context).setTitle(R.string.use_network_dialog_title).setNegativeButton(R.string.cancel, this.mCancelClickListener).setPositiveButton(R.string.allow, this.mAllowClickListener).create();
        int padding = context.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
        tipsDialog.setView(tipsView, padding, padding, padding, 0);
        tipsDialog.show();
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }
}
