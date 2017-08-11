package com.huk.todo.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.huk.todo.R;
import com.huk.todo.adapter.TaskListAdapter;
import com.huk.todo.adapter.TodoListAdapter;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskCreatedCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskListCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.helper.AddTodoListDialogFragment;
import com.huk.todo.model.Tasks;
import com.huk.todo.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TodoTaskActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int MINIMUM = 25;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.task_list)
    RecyclerView taskList;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.empty_view)
    TextView emptyTextView;

    private TaskDBHelper mTaskDBHelper;
    private TaskListAdapter taskListAdapter;
    private List<Tasks> mTaskList;
    private int mParentId = -1;
    public final int TASK_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_task);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        init();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mParentId != -1) {
                    AddTodoListDialogFragment addTodoListDialogFragment = new AddTodoListDialogFragment(TodoTaskActivity.this, mParentId, TASK_TYPE, new TaskCreatedCallback() {
                        @Override
                        public void onTaskCreated() {

                        }

                        @Override
                        public void onTaskCreated(int mParentId, String taskName) {
                            mTaskList.clear();
                            fetchTasks();
                            if (NetworkUtils.getInstance().isNetworkAvailable()) {
                                NetworkUtils.getInstance(TodoTaskActivity.this).doCreateTask(mParentId, taskName, new TaskCreatedCallback() {
                                    @Override
                                    public void onTaskCreated() {
                                        Log.d("task", "synced");
                                    }

                                    @Override
                                    public void onTaskCreated(int mParentId, String s) {

                                    }
                                });
                            }
                        }
                    });
                    addTodoListDialogFragment.show(getSupportFragmentManager(), addTodoListDialogFragment.getTag());
                }
            }
        });
    }

    private void init() {
        taskList.setLayoutManager(new LinearLayoutManager(this));
        taskList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public boolean isVisible;
            int scrollDist = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isVisible && scrollDist > MINIMUM) {
                    fab.animate().translationY(fab.getHeight() + getResources().getDimension(R.dimen.fab_margin)).setInterpolator(new AccelerateInterpolator(2)).start();
                    scrollDist = 0;
                    isVisible = false;
                } else if (!isVisible && scrollDist < -MINIMUM) {
                    fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    scrollDist = 0;
                    isVisible = true;
                }

                if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
                    scrollDist += dy;
                }
            }
        });
        mTaskDBHelper = new TaskDBHelper(this);
        mTaskList = new ArrayList<>();
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(taskList.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        taskList.addItemDecoration(horizontalDecoration);
        Bundle extras = getIntent().getExtras();
        mParentId = extras.getInt(TodoListAdapter.TODO_LIST_CURRENT_ROW_ID);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(extras.getString(TodoListAdapter.TODO_LIST_CURRENT_ROW_TITLE));
        fetchTasks();
    }

    private void fetchTasks() {
        mTaskDBHelper.getItemsListForTodo(mParentId, (ArrayList<Tasks>) mTaskList, new TaskListCallback() {
            @Override
            public void onDataFetched() {
                if (mTaskList.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    taskListAdapter = new TaskListAdapter(TodoTaskActivity.this, mTaskList, mParentId);
                    taskList.setAdapter(taskListAdapter);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_task, menu);
        final MenuItem searchItem = menu.findItem(R.id.search_task);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.delete_All:
                taskListAdapter.removeAll();
                emptyTextView.setVisibility(View.VISIBLE);
                break;
            case R.id.mark_all_done:
                taskListAdapter.markAllDone();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        taskListAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        taskListAdapter.filter(newText);
        return false;
    }
}
