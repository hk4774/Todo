package com.huk.todo.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.huk.todo.R;
import com.huk.todo.activities.MainActivity;
import com.huk.todo.activities.TodoTaskActivity;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListDeleteCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListMarkCompletedCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListRenameCallback;
import com.huk.todo.database.TodoListDatabaseHelper;
import com.huk.todo.helper.Utilities;
import com.huk.todo.model.Todos;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by User on 8/9/2017.
 */

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> {

    private static final String TAG = TodoListAdapter.class.getSimpleName();
    public static final String TODO_LIST_CURRENT_ROW_ID = "todo_list_current_row_id";
    public static final String TODO_LIST_CURRENT_ROW_TITLE = "todo_list_current_row_title";

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RecyclerView todoList;
    private final List<Todos> mTodos;
    private final List<Todos> mTodosCopy;

    public TodoListAdapter(Context context, List<Todos> mTodos) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mTodos = mTodos;
        this.mTodosCopy = new ArrayList<>(this.mTodos);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.todo_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        todoList = recyclerView;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.todoTitle.setText(mTodos.get(position).getItemText());
        boolean i = mTodos.get(position).isDone();
        if (i) {
            holder.todoTitle.setTypeface(null, Typeface.NORMAL);
            holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setBackground(new ColorDrawable(Color.parseColor("#D3D3D3")));

        }
        holder.todoDate.setText(mTodos.get(position).getPub_date().split(" ")[0]);
    }

    @Override
    public int getItemCount() {
        return mTodos == null ? 0 : mTodos.size();
    }

    public void filter(String text) {
        mTodos.clear();
        if (text.isEmpty()) {
            mTodos.clear();
            todoList.removeAllViewsInLayout();
            mTodos.addAll(mTodosCopy);
        } else {
            List<Todos> todosList = new ArrayList<>();
            text = text.toLowerCase();
            for (Todos item : mTodosCopy) {
                if (item.getItemText().toLowerCase().contains(text)) {
                    todosList.add(item);
                }
            }
            mTodos.clear();
            todoList.removeAllViewsInLayout();
            mTodos.addAll(todosList);
        }
        TodoListAdapter.this.notifyDataSetChanged();
        todoList.invalidate();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_todo_name)
        TextView todoTitle;
        @BindView(R.id.tv_todo_date)
        TextView todoDate;
        @BindView(R.id.more_option)
        ImageButton moreOptionButton;
        View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            rootView = itemView;
            rootView.setOnClickListener(this);
            moreOptionButton.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            int id = view.getId();
            if (id == R.id.more_option) {
                if (mTodos.get(getAdapterPosition()).isDone()) {
                    final CharSequence[] items = {"Delete"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Choose a option");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            final TodoListDatabaseHelper todoListDatabaseHelper = new TodoListDatabaseHelper(mContext);
                            switch (item) {
                                case 0:
                                    dialog.dismiss();
                                    todoListDatabaseHelper.removeTodo(Integer.parseInt(String.valueOf(mTodos.get(getAdapterPosition()).getItemID())), new TodoListDeleteCallback() {
                                        @Override
                                        public void onTodoDeleted() {
                                            refreshTodoList(todoListDatabaseHelper);
                                        }
                                    });
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    final CharSequence[] items = {"Rename", "Delete", "Mark as done"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Choose a option");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            final TodoListDatabaseHelper todoListDatabaseHelper = new TodoListDatabaseHelper(mContext);
                            switch (item) {
                                case 0:
                                    dialog.dismiss();
                                    Utilities.getInstance().showRenameDialog(mContext, todoTitle.getText(), mTodos.get(getAdapterPosition()).getItemID(), new TodoListRenameCallback() {
                                        @Override
                                        public void onTodoRenamed() {
                                            refreshTodoList(todoListDatabaseHelper);
                                        }
                                    });
                                    Snackbar.make(view, "Todo renamed successfully", Snackbar.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    dialog.dismiss();
                                    new AlertDialog.Builder(mContext)
                                            .setTitle("Confirm delete")
                                            .setMessage("Do you really want to delete this todo?")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    todoListDatabaseHelper.removeTodo(mTodos.get(getAdapterPosition()).getItemID(), new TodoListDeleteCallback() {
                                                        @Override
                                                        public void onTodoDeleted() {
                                                            refreshTodoList(todoListDatabaseHelper);
                                                            Snackbar.make(TodoListAdapter.this.todoList, "Todo deleted successfully", Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                    break;
                                case 2:
                                    dialog.dismiss();
                                    todoListDatabaseHelper.markTodoAsCompleted(Integer.parseInt(String.valueOf(mTodos.get(getAdapterPosition()).getItemID())), true, new TodoListMarkCompletedCallback() {
                                        @Override
                                        public void onTodoMarkCompleted() {
                                            refreshTodoList(todoListDatabaseHelper);
                                        }
                                    });
                                    Snackbar.make(todoList, "Todo marked as done successfully", Snackbar.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else if (id == rootView.getId()) {
                Intent intent = new Intent(mContext, TodoTaskActivity.class);
                intent.putExtra(TODO_LIST_CURRENT_ROW_ID, mTodos.get(getAdapterPosition()).getItemID());
                intent.putExtra(TODO_LIST_CURRENT_ROW_TITLE, mTodos.get(getAdapterPosition()).getItemText());
                mContext.startActivity(intent);
            }
        }

        private void refreshTodoList(TodoListDatabaseHelper todoListDatabaseHelper) {
            mTodos.clear();
            todoList.removeAllViewsInLayout();
            todoListDatabaseHelper.getTodoList(mTodos, new TodoListCallback() {
                @Override
                public void onDataFetched() {
                    TodoListAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }
}
