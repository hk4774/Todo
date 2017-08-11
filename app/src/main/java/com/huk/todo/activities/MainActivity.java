package com.huk.todo.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.huk.todo.R;
import com.huk.todo.adapter.TodoListAdapter;
import com.huk.todo.callbacks.LogoutCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCreateCallback;
import com.huk.todo.database.TodoListDatabaseHelper;
import com.huk.todo.helper.AddTodoListDialogFragment;
import com.huk.todo.helper.Constants;
import com.huk.todo.helper.SharedPrefsUtils;
import com.huk.todo.model.Todos;
import com.huk.todo.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int MINIMUM = 25;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.todo_list)
    RecyclerView todoList;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.empty_view)
    TextView emptyTextView;

    private List<Todos> mTodos;
    private TodoListDatabaseHelper mTodoListDbHelper;
    private TodoListAdapter todoListAdapter;
    public final int TODO_TYPE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        init();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTodoListDialogFragment addTodoListDialogFragment = new AddTodoListDialogFragment(MainActivity.this, TODO_TYPE, new TodoListCreateCallback() {
                    @Override
                    public void onTodoCreated() {

                    }

                    @Override
                    public void onTodoCreated(String todoName) {
                        mTodos.clear();
                        fetchTodos();
                        Snackbar.make(fab, "Todo added successfully", Snackbar.LENGTH_SHORT).show();
                        if (NetworkUtils.getInstance().isNetworkAvailable()) {
                            NetworkUtils.getInstance(MainActivity.this).doCreateTodo(todoName, new TodoListCreateCallback() {
                                @Override
                                public void onTodoCreated() {
                                    Log.d("Todo", "synced");
                                }

                                @Override
                                public void onTodoCreated(String todoName) {

                                }
                            });
                        }
                    }
                });
                addTodoListDialogFragment.show(getSupportFragmentManager(), addTodoListDialogFragment.getTag());
            }
        });
    }

    private void init() {
        todoList.setLayoutManager(new LinearLayoutManager(this));
        todoList.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
        mTodoListDbHelper = new TodoListDatabaseHelper(this);
        mTodos = new ArrayList<>();
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(todoList.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        todoList.addItemDecoration(horizontalDecoration);
        fetchTodos();
    }

    private void fetchTodos() {
        mTodoListDbHelper.getTodoList(mTodos, new TodoListCallback() {
            @Override
            public void onDataFetched() {
                if (mTodos.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    todoListAdapter = new TodoListAdapter(MainActivity.this, mTodos);
                    todoList.setAdapter(todoListAdapter);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_log_out:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm logout")
                        .setMessage("Do you really want to logout? This will delete all your local data.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                logout();
                            }

                            private void logout() {
                                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                                        R.style.AppTheme_Dark_Dialog);
                                progressDialog.setIndeterminate(true);
                                progressDialog.setMessage("Loging out...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                NetworkUtils.getInstance().doLogout(new LogoutCallback() {
                                    @Override
                                    public void onLogoutComplete() {
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        SharedPrefsUtils.setBooleanPreference(MainActivity.this, Constants.IS_LOGIN, false);
                                        MainActivity.this.deleteDatabase("todo.db");
                                        progressDialog.dismiss();
                                        MainActivity.this.finish();
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onLogoutFailed(String message) {

                                    }

                                    @Override
                                    public void onLogoutFailed() {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        todoListAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        todoListAdapter.filter(query);
        return false;
    }
}
