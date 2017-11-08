package com.android.systemui.recents.tv.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTvTaskEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import java.util.ArrayList;
import java.util.List;

public class TaskStackHorizontalViewAdapter extends Adapter<ViewHolder> {
    private TaskStackHorizontalGridView mGridView;
    private List<Task> mTaskList;

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements OnClickListener {
        private Task mTask;
        private TaskCardView mTaskCardView;

        public ViewHolder(View v) {
            super(v);
            this.mTaskCardView = (TaskCardView) v;
        }

        public void init(Task task) {
            this.mTaskCardView.init(task);
            this.mTask = task;
            this.mTaskCardView.setOnClickListener(this);
        }

        public void onClick(View v) {
            try {
                if (this.mTaskCardView.isInDismissState()) {
                    this.mTaskCardView.startDismissTaskAnimation(getRemoveAtListener(getAdapterPosition(), this.mTaskCardView.getTask()));
                } else {
                    EventBus.getDefault().send(new LaunchTvTaskEvent(this.mTaskCardView, this.mTask, null, -1));
                }
            } catch (Exception e) {
                Log.e("TaskStackViewAdapter", v.getContext().getString(R.string.recents_launch_error_message, new Object[]{this.mTask.title}), e);
            }
        }

        private AnimatorListener getRemoveAtListener(int position, final Task task) {
            return new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    TaskStackHorizontalViewAdapter.this.removeTask(task);
                    EventBus.getDefault().send(new DeleteTaskDataEvent(task));
                }

                public void onAnimationCancel(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }
            };
        }
    }

    public TaskStackHorizontalViewAdapter(List tasks) {
        this.mTaskList = new ArrayList(tasks);
    }

    public void setNewStackTasks(List tasks) {
        this.mTaskList.clear();
        this.mTaskList.addAll(tasks);
        notifyDataSetChanged();
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recents_tv_task_card_view, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = (Task) this.mTaskList.get(position);
        Recents.getTaskLoader().loadTaskData(task);
        holder.init(task);
    }

    public int getItemCount() {
        return this.mTaskList.size();
    }

    public void removeTask(Task task) {
        int position = this.mTaskList.indexOf(task);
        if (position >= 0) {
            this.mTaskList.remove(position);
            notifyItemRemoved(position);
            if (this.mGridView != null) {
                this.mGridView.getStack().removeTask(task, AnimationProps.IMMEDIATE, false);
            }
        }
    }

    public int getPositionOfTask(Task task) {
        int position = this.mTaskList.indexOf(task);
        return position >= 0 ? position : 0;
    }

    public void setTaskStackHorizontalGridView(TaskStackHorizontalGridView gridView) {
        this.mGridView = gridView;
    }

    public void addTaskAt(Task task, int position) {
        this.mTaskList.add(position, task);
        notifyItemInserted(position);
    }
}
