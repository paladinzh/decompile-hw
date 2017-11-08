package com.huawei.systemmanager.netassistant.ui.mainpage;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;
import com.huawei.systemmanager.util.HSMConst;

public class MainEmptyFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.netassistant_traffic_no_simcard_empty_view, container, false);
        TextView textView = (TextView) v.findViewById(R.id.empty_text);
        ((ImageView) v.findViewById(R.id.empty_image)).setImageResource(R.drawable.ic_simcard_emptypage);
        textView.setText(R.string.net_assistant_no_sim_card);
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (HSMConst.isSupportSubfiled(null)) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = activity.getIntent();
                if (view instanceof SlidingUpPanelLayout) {
                    HSMConst.setCfgForSlidingUp(intent, (SlidingUpPanelLayout) view);
                }
            }
        }
    }
}
