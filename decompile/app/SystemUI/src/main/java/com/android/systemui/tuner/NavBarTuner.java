package com.android.systemui.tuner;

import android.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
import com.android.systemui.tuner.KeycodeSelectionHelper.OnSelectionComplete;
import com.android.systemui.tuner.TunerService.Tunable;
import java.util.ArrayList;
import java.util.List;

public class NavBarTuner extends Fragment implements Tunable {
    private NavBarAdapter mNavBarAdapter;
    private PreviewNavInflater mPreview;

    private static class Dividers extends ItemDecoration {
        private final Drawable mDivider;

        public Dividers(Context context) {
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(16843284, value, true);
            this.mDivider = context.getDrawable(value.resourceId);
        }

        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                int top = child.getBottom() + ((LayoutParams) child.getLayoutParams()).bottomMargin;
                this.mDivider.setBounds(left, top, right, top + this.mDivider.getIntrinsicHeight());
                this.mDivider.draw(c);
            }
        }
    }

    private static class Holder extends ViewHolder {
        private TextView title;

        public Holder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(16908310);
        }
    }

    private class NavBarAdapter extends Adapter<Holder> implements OnClickListener {
        private int mButtonLayout;
        private List<String> mButtons = new ArrayList();
        private final Callback mCallbacks = new Callback() {
            public boolean isLongPressDragEnabled() {
                return false;
            }

            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() != 1) {
                    return Callback.makeMovementFlags(0, 0);
                }
                return Callback.makeMovementFlags(3, 0);
            }

            public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (to == 0) {
                    return false;
                }
                move(from, to, NavBarAdapter.this.mButtons);
                move(from, to, NavBarAdapter.this.mLabels);
                NavBarTuner.this.notifyChanged();
                NavBarAdapter.this.notifyItemMoved(from, to);
                return true;
            }

            private <T> void move(int from, int to, List<T> list) {
                list.add(from > to ? to : to + 1, list.get(from));
                if (from > to) {
                    from++;
                }
                list.remove(from);
            }

            public void onSwiped(ViewHolder viewHolder, int direction) {
            }
        };
        private int mCategoryLayout;
        private int mKeycode;
        private List<CharSequence> mLabels = new ArrayList();
        private ItemTouchHelper mTouchHelper;

        public NavBarAdapter(Context context) {
            this.mButtonLayout = context.getTheme().obtainStyledAttributes(null, R.styleable.Preference, 16842894, 0).getResourceId(3, 0);
            this.mCategoryLayout = context.getTheme().obtainStyledAttributes(null, R.styleable.Preference, 16842892, 0).getResourceId(3, 0);
        }

        public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
            this.mTouchHelper = itemTouchHelper;
        }

        public void clear() {
            this.mButtons.clear();
            this.mLabels.clear();
            notifyDataSetChanged();
        }

        public void addButton(String button, CharSequence label) {
            this.mButtons.add(button);
            this.mLabels.add(label);
            notifyItemInserted(this.mLabels.size() - 1);
            NavBarTuner.this.notifyChanged();
        }

        public boolean hasHomeButton() {
            int N = this.mButtons.size();
            for (int i = 0; i < N; i++) {
                if (((String) this.mButtons.get(i)).startsWith("home")) {
                    return true;
                }
            }
            return false;
        }

        public String getNavString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < this.mButtons.size() - 1; i++) {
                String button = (String) this.mButtons.get(i);
                if (button.equals("center") || button.equals("end")) {
                    if (builder.length() == 0 || builder.toString().endsWith(";")) {
                        builder.append("space");
                    }
                    builder.append(";");
                } else {
                    if (!(builder.length() == 0 || builder.toString().endsWith(";"))) {
                        builder.append(",");
                    }
                    builder.append(button);
                }
            }
            if (builder.toString().endsWith(";")) {
                builder.append("space");
            }
            return builder.toString();
        }

        public int getItemViewType(int position) {
            String button = (String) this.mButtons.get(position);
            if (button.equals("start") || button.equals("center") || button.equals("end")) {
                return 2;
            }
            if (button.equals("add")) {
                return 0;
            }
            return 1;
        }

        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(getLayoutId(viewType), parent, false);
            if (viewType == 1) {
                inflater.inflate(com.android.systemui.R.layout.nav_control_widget, (ViewGroup) view.findViewById(16908312));
            }
            return new Holder(view);
        }

        private int getLayoutId(int viewType) {
            if (viewType == 2) {
                return this.mCategoryLayout;
            }
            return this.mButtonLayout;
        }

        public void onBindViewHolder(Holder holder, int position) {
            holder.title.setText((CharSequence) this.mLabels.get(position));
            if (holder.getItemViewType() == 1) {
                bindButton(holder, position);
            } else if (holder.getItemViewType() == 0) {
                bindAdd(holder);
            }
        }

        private void bindAdd(Holder holder) {
            TypedValue value = new TypedValue();
            Context context = holder.itemView.getContext();
            context.getTheme().resolveAttribute(16843829, value, true);
            ImageView icon = (ImageView) holder.itemView.findViewById(16908294);
            icon.setImageResource(com.android.systemui.R.drawable.ic_add);
            icon.setImageTintList(ColorStateList.valueOf(context.getColor(value.resourceId)));
            holder.itemView.findViewById(16908304).setVisibility(8);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    NavBarAdapter.this.showAddDialog(v.getContext());
                }
            });
        }

        private void bindButton(final Holder holder, int position) {
            holder.itemView.findViewById(16908350).setVisibility(8);
            holder.itemView.findViewById(16908304).setVisibility(8);
            bindClick(holder.itemView.findViewById(com.android.systemui.R.id.close), holder);
            bindClick(holder.itemView.findViewById(com.android.systemui.R.id.width), holder);
            holder.itemView.findViewById(com.android.systemui.R.id.drag).setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    NavBarAdapter.this.mTouchHelper.startDrag(holder);
                    return true;
                }
            });
        }

        private void showAddDialog(final Context context) {
            final String[] options = new String[]{"back", "home", "recent", "menu_ime", "space", "clipboard", "key"};
            final CharSequence[] labels = new CharSequence[options.length];
            for (int i = 0; i < options.length; i++) {
                labels[i] = NavBarTuner.getLabel(options[i], context);
            }
            new Builder(context).setTitle(com.android.systemui.R.string.select_button).setItems(labels, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if ("key".equals(options[which])) {
                        NavBarAdapter.this.showKeyDialogs(context);
                        return;
                    }
                    int index = NavBarAdapter.this.mButtons.size() - 1;
                    NavBarAdapter.this.showAddedMessage(context, options[which]);
                    NavBarAdapter.this.mButtons.add(index, options[which]);
                    NavBarAdapter.this.mLabels.add(index, labels[which]);
                    NavBarAdapter.this.notifyItemInserted(index);
                    NavBarTuner.this.notifyChanged();
                }
            }).setNegativeButton(17039360, null).show();
        }

        private void onImageSelected(Uri uri) {
            int index = this.mButtons.size() - 1;
            this.mButtons.add(index, "key(" + this.mKeycode + ":" + uri.toString() + ")");
            this.mLabels.add(index, NavBarTuner.getLabel("key", NavBarTuner.this.getContext()));
            notifyItemInserted(index);
            NavBarTuner.this.notifyChanged();
        }

        private void showKeyDialogs(final Context context) {
            final OnSelectionComplete listener = new OnSelectionComplete() {
                public void onSelectionComplete(int code) {
                    NavBarAdapter.this.mKeycode = code;
                    NavBarTuner.this.selectImage();
                }
            };
            new Builder(context).setTitle(com.android.systemui.R.string.keycode).setMessage(com.android.systemui.R.string.keycode_description).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    KeycodeSelectionHelper.showKeycodeSelect(context, listener);
                }
            }).show();
        }

        private void showAddedMessage(Context context, String button) {
            if ("clipboard".equals(button)) {
                new Builder(context).setTitle(com.android.systemui.R.string.clipboard).setMessage(com.android.systemui.R.string.clipboard_description).setPositiveButton(17039370, null).show();
            }
        }

        private void bindClick(View view, Holder holder) {
            view.setOnClickListener(this);
            view.setTag(holder);
        }

        public void onClick(View v) {
            Holder holder = (Holder) v.getTag();
            if (v.getId() == com.android.systemui.R.id.width) {
                showWidthDialog(holder, v.getContext());
            } else if (v.getId() == com.android.systemui.R.id.close) {
                int position = holder.getAdapterPosition();
                this.mButtons.remove(position);
                this.mLabels.remove(position);
                notifyItemRemoved(position);
                NavBarTuner.this.notifyChanged();
            }
        }

        private void showWidthDialog(final Holder holder, Context context) {
            final String buttonSpec = (String) this.mButtons.get(holder.getAdapterPosition());
            float amount = NavigationBarInflaterView.extractSize(buttonSpec);
            final AlertDialog dialog = new Builder(context).setTitle(com.android.systemui.R.string.adjust_button_width).setView(com.android.systemui.R.layout.nav_width_view).setNegativeButton(17039360, null).create();
            dialog.setButton(-1, context.getString(17039370), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int which) {
                    String button = NavigationBarInflaterView.extractButton(buttonSpec);
                    SeekBar seekBar = (SeekBar) dialog.findViewById(com.android.systemui.R.id.seekbar);
                    if (seekBar.getProgress() == 75) {
                        NavBarAdapter.this.mButtons.set(holder.getAdapterPosition(), button);
                    } else {
                        NavBarAdapter.this.mButtons.set(holder.getAdapterPosition(), button + "[" + (((float) (seekBar.getProgress() + 25)) / 100.0f) + "]");
                    }
                    NavBarTuner.this.notifyChanged();
                }
            });
            dialog.show();
            SeekBar seekBar = (SeekBar) dialog.findViewById(com.android.systemui.R.id.seekbar);
            seekBar.setMax(150);
            seekBar.setProgress((int) ((amount - 0.25f) * 100.0f));
        }

        public int getItemCount() {
            return this.mButtons.size();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.android.systemui.R.layout.nav_bar_tuner, container, false);
        inflatePreview((ViewGroup) view.findViewById(com.android.systemui.R.id.nav_preview_frame));
        return view;
    }

    private void inflatePreview(ViewGroup view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        boolean isRotated = display.getRotation() != 1 ? display.getRotation() == 3 : true;
        Configuration config = new Configuration(getContext().getResources().getConfiguration());
        boolean isPhoneLandscape = isRotated && config.smallestScreenWidthDp < 600;
        float scale = isPhoneLandscape ? 0.75f : 0.95f;
        config.densityDpi = (int) (((float) config.densityDpi) * scale);
        this.mPreview = (PreviewNavInflater) LayoutInflater.from(getContext().createConfigurationContext(config)).inflate(com.android.systemui.R.layout.nav_bar_tuner_inflater, view, false);
        ViewGroup.LayoutParams layoutParams = this.mPreview.getLayoutParams();
        layoutParams.width = (int) (((float) (isPhoneLandscape ? display.getHeight() : display.getWidth())) * scale);
        layoutParams.height = (int) (((float) layoutParams.height) * scale);
        if (isPhoneLandscape) {
            int width = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = width;
        }
        view.addView(this.mPreview);
        if (isRotated) {
            this.mPreview.findViewById(com.android.systemui.R.id.rot0).setVisibility(8);
            View findViewById = this.mPreview.findViewById(com.android.systemui.R.id.rot90);
            return;
        }
        this.mPreview.findViewById(com.android.systemui.R.id.rot90).setVisibility(8);
        View rot0 = this.mPreview.findViewById(com.android.systemui.R.id.rot0);
    }

    private void notifyChanged() {
        this.mPreview.onTuningChanged("sysui_nav_bar", this.mNavBarAdapter.getNavString());
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(16908298);
        Context context = getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.mNavBarAdapter = new NavBarAdapter(context);
        recyclerView.setAdapter(this.mNavBarAdapter);
        recyclerView.addItemDecoration(new Dividers(context));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this.mNavBarAdapter.mCallbacks);
        this.mNavBarAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        TunerService.get(getContext()).addTunable((Tunable) this, "sysui_nav_bar");
    }

    public void onDestroyView() {
        super.onDestroyView();
        TunerService.get(getContext()).removeTunable(this);
    }

    public void onTuningChanged(String key, String navLayout) {
        if ("sysui_nav_bar".equals(key)) {
            Context context = getContext();
            if (navLayout == null) {
                navLayout = context.getString(com.android.systemui.R.string.config_navBarLayout);
            }
            String[] views = navLayout.split(";");
            String[] groups = new String[]{"start", "center", "end"};
            CharSequence[] groupLabels = new String[]{getString(com.android.systemui.R.string.start), getString(com.android.systemui.R.string.center), getString(com.android.systemui.R.string.end)};
            this.mNavBarAdapter.clear();
            for (int i = 0; i < 3; i++) {
                this.mNavBarAdapter.addButton(groups[i], groupLabels[i]);
                for (String button : views[i].split(",")) {
                    this.mNavBarAdapter.addButton(button, getLabel(button, context));
                }
            }
            this.mNavBarAdapter.addButton("add", getString(com.android.systemui.R.string.add_button));
            setHasOptionsMenu(true);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 2, 0, getString(com.android.systemui.R.string.save)).setShowAsAction(1);
        menu.add(0, 3, 0, getString(com.android.systemui.R.string.reset));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 2) {
            if (this.mNavBarAdapter.hasHomeButton()) {
                Secure.putString(getContext().getContentResolver(), "sysui_nav_bar", this.mNavBarAdapter.getNavString());
            } else {
                new Builder(getContext()).setTitle(com.android.systemui.R.string.no_home_title).setMessage(com.android.systemui.R.string.no_home_message).setPositiveButton(17039370, null).show();
            }
            return true;
        } else if (item.getItemId() != 3) {
            return super.onOptionsItemSelected(item);
        } else {
            Secure.putString(getContext().getContentResolver(), "sysui_nav_bar", null);
            return true;
        }
    }

    private static CharSequence getLabel(String button, Context context) {
        if (button.startsWith("home")) {
            return context.getString(com.android.systemui.R.string.accessibility_home);
        }
        if (button.startsWith("back")) {
            return context.getString(com.android.systemui.R.string.accessibility_back);
        }
        if (button.startsWith("recent")) {
            return context.getString(com.android.systemui.R.string.accessibility_recent);
        }
        if (button.startsWith("space")) {
            return context.getString(com.android.systemui.R.string.space);
        }
        if (button.startsWith("menu_ime")) {
            return context.getString(com.android.systemui.R.string.menu_ime);
        }
        if (button.startsWith("clipboard")) {
            return context.getString(com.android.systemui.R.string.clipboard);
        }
        if (button.startsWith("key")) {
            return context.getString(com.android.systemui.R.string.keycode);
        }
        return button;
    }

    private void selectImage() {
        startActivityForResult(KeycodeSelectionHelper.getSelectImageIntent(), 42);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && resultCode == -1 && data != null) {
            Uri uri = data.getData();
            getContext().getContentResolver().takePersistableUriPermission(uri, data.getFlags() & 1);
            this.mNavBarAdapter.onImageSelected(uri);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
