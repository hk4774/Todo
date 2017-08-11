package com.huk.todo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.huk.todo.R;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskDeleteCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskListCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskMarkCompletedCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskRenameCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.helper.Utilities;
import com.huk.todo.model.Tasks;
import com.huk.todo.model.Todos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by User on 8/10/2017.
 */

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private final int mParentId;
    private Context mContext;
    private List<Tasks> mTaskList;
    private LayoutInflater mLayoutInflater;
    private RecyclerView taskListRecyclerView;
    final TaskDBHelper mTaskDBHelper;
    private List<Tasks> mTaskCopy;

    public TaskListAdapter(Context context, List<Tasks> mTaskList, int mParentId) {
        this.mContext = context;
        this.mTaskList = mTaskList;
        mLayoutInflater = LayoutInflater.from(context);
        this.mParentId = mParentId;
        mTaskDBHelper = new TaskDBHelper(mContext);
        mTaskCopy = new ArrayList<>(mTaskList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.task_row, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tasks todoItme = mTaskList.get(position);
        holder.tvTaskName.setText(todoItme.getItemText());
        if (todoItme.isDone()){
            holder.cbMark.setChecked(true);
            holder.tvTaskName.setTypeface(null, Typeface.NORMAL);
            holder.tvTaskName.setPaintFlags(holder.tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setBackground(new ColorDrawable(Color.parseColor("#D3D3D3")));
        }
    }

    @Override
    public int getItemCount() {
        return mTaskList == null ? 0 : mTaskList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        taskListRecyclerView = recyclerView;
    }

    public void removeAll() {
        for (int i = 0; i < mTaskList.size(); i++) {
            mTaskDBHelper.deleteItemForTodo(mParentId, mTaskList.get(i).getItemID(), new TaskDeleteCallback() {
                @Override
                public void onTaskDeleted() {
                }
            });
        }
        refreshTodoList(mTaskDBHelper);
        Snackbar.make(taskListRecyclerView, "Deleted all Successfully", Snackbar.LENGTH_LONG).show();
    }

    public void markAllDone() {
        for (int i = 0; i < mTaskList.size(); i++) {
            mTaskDBHelper.markItemAsCompleted(mTaskList.get(i).getItemID(), true, new TaskMarkCompletedCallback() {
                @Override
                public void onTaskMarkCompleted() {
                }
            });
        }
        refreshTodoList(mTaskDBHelper);
        Snackbar.make(taskListRecyclerView, "Marked all done", Snackbar.LENGTH_LONG).show();
    }

    public void filter(String text) {
        mTaskList.clear();
        if (text.isEmpty()) {
            mTaskList.clear();
            taskListRecyclerView.removeAllViewsInLayout();
            mTaskList.addAll(mTaskCopy);
        } else {
            List<Tasks> tasksList = new ArrayList<>();
            text = text.toLowerCase();
            for (Tasks item : mTaskCopy) {
                if (item.getItemText().toLowerCase().contains(text)) {
                    tasksList.add(item);
                }
            }
            mTaskList.clear();
            taskListRecyclerView.removeAllViewsInLayout();
            mTaskList.addAll(tasksList);
        }
        TaskListAdapter.this.notifyDataSetChanged();
        taskListRecyclerView.invalidate();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_task_name)
        TextView tvTaskName;
        @BindView(R.id.cb_mark)
        CheckBox cbMark;
        @BindView(R.id.ib_edit_task)
        ImageButton ibEditTask;
        @BindView(R.id.ib_delete_task)
        ImageButton ibDeleteTask;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            cbMark.setOnClickListener(this);
            ibDeleteTask.setOnClickListener(this);
            ibEditTask.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            int id = view.getId();
            switch (id) {
                case R.id.ib_edit_task:
                    Utilities.getInstance().showRenameDialog(mContext, tvTaskName.getText(), mTaskList.get(getAdapterPosition()).getItemID(), new TaskRenameCallback() {
                        @Override
                        public void onTaskRenamed() {
                            Snackbar.make(view, "Renamed Successfully", Snackbar.LENGTH_LONG).show();
                            refreshTodoList(mTaskDBHelper);
                        }
                    });
                    break;
                case R.id.cb_mark:
                    if (cbMark.isChecked()) {
                        mTaskDBHelper.markItemAsCompleted(mTaskList.get(getAdapterPosition()).getItemID(), true, new TaskMarkCompletedCallback() {
                            @Override
                            public void onTaskMarkCompleted() {
                                Snackbar.make(taskListRecyclerView, "Marked as done", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        mTaskDBHelper.markItemAsCompleted(mTaskList.get(getAdapterPosition()).getItemID(), false, new TaskMarkCompletedCallback() {
                            @Override
                            public void onTaskMarkCompleted() {
                                Snackbar.make(taskListRecyclerView, "Removed as done", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                    refreshTodoList(mTaskDBHelper);
                    break;
                case R.id.ib_delete_task:
                    mTaskDBHelper.deleteItemForTodo(mParentId, mTaskList.get(getAdapterPosition()).getItemID(), new TaskDeleteCallback() {
                        @Override
                        public void onTaskDeleted() {
                            refreshTodoList(mTaskDBHelper);
                            Snackbar.make(taskListRecyclerView, "Deleted Successfully", Snackbar.LENGTH_LONG).show();
                        }
                    });
                    break;
            }
        }
    }

    private void refreshTodoList(TaskDBHelper taskDBHelper) {
        mTaskList.clear();
        taskListRecyclerView.removeAllViewsInLayout();
        taskDBHelper.getItemsListForTodo(mParentId, (ArrayList<Tasks>) mTaskList, new TaskListCallback() {
            @Override
            public void onDataFetched() {
                TaskListAdapter.this.notifyDataSetChanged();
            }
        });
    }
}
