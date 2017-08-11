package com.huk.todo.helper;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.huk.todo.R;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskCreatedCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCreateCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.database.TodoListDatabaseHelper;
import com.huk.todo.network.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;


public class AddTodoListDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "AddTodoListDialogFragment";
    private Context mContext;

    @BindView(R.id.input_layout)
    TextInputLayout inputLayout;
    @BindView(R.id.input_edit_text)
    TextInputEditText editText;
    @BindView(R.id.et_task_name)
    EditText editTextTaskName;
    @BindView(R.id.task_layout)
    LinearLayout taskLayout;

    private int mParentId;
    private int CURRENT_ITEM_TYPE = -1;
    public final int TODO_TYPE = 0;
    public final int TASK_TYPE = 1;
    private TodoListCreateCallback mTodoListCreateCallback;
    private TaskCreatedCallback mTaskCreatedCallback;

    public AddTodoListDialogFragment() {
    }

    @SuppressLint("ValidFragment")
    public AddTodoListDialogFragment(Context context, int taskType, TodoListCreateCallback todoListCreateCallback) {
        this.CURRENT_ITEM_TYPE = taskType;
        this.mContext = context;
        this.mTodoListCreateCallback = todoListCreateCallback;
    }


    @SuppressLint("ValidFragment")
    public AddTodoListDialogFragment(Context context, int parentId, int taskType, TaskCreatedCallback taskCreatedCallback) {
        this.mParentId = parentId;
        this.mContext = context;
        this.CURRENT_ITEM_TYPE = taskType;
        this.mTaskCreatedCallback = taskCreatedCallback;
    }


    private BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("LongLogTag")
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN: {
                    Log.d(TAG, "Bottom sheet hidden to dismiss");
                    dismiss();
                    break;
                }
                case BottomSheetBehavior.STATE_EXPANDED: {
                    Log.d(TAG, "expanded");
                    break;
                }
                case BottomSheetBehavior.STATE_COLLAPSED: {
                    Log.d(TAG, "collapsed");
                    break;
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_add_list_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            BottomSheetBehavior bottomSheetBehavior = ((BottomSheetBehavior) behavior);
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetBehaviorCallback);
        }

        ButterKnife.bind(this, contentView);

        if (this.CURRENT_ITEM_TYPE == TODO_TYPE) {
            taskLayout.setVisibility(View.VISIBLE);
            taskLayout.invalidate();
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.done_button)
    void onDoneButtonClicked() {
        if (validateInputs()) {
            inputLayout.setErrorEnabled(false);
            switch (CURRENT_ITEM_TYPE) {
                case TODO_TYPE:
                    storeTodo(editText.getText().toString());
                    break;
                case TASK_TYPE:
                    storeTask(editText.getText().toString());
                    break;
            }
        }
    }

    private boolean validateInputs() {
        if (editText.length() == 0) {
            inputLayout.setError(getString(R.string.error_hint_title_can_not_be_empty));
            inputLayout.setErrorEnabled(true);
            return false;
        } else if (this.CURRENT_ITEM_TYPE == TODO_TYPE) {
            if (editTextTaskName.length() == 0) {
                editTextTaskName.setError(getString(R.string.error_hint_title_can_not_be_empty));
                return false;
            }
        }
        return true;
    }

    private void storeTask(String taskName) {
        TaskDBHelper taskDBHelper = new TaskDBHelper(mContext);
        taskDBHelper.createItemForTodo(mParentId, taskName);
        mTaskCreatedCallback.onTaskCreated(mParentId, editTextTaskName.getText().toString());
        dismiss();
    }

    private void storeTodo(String todoName) {
        TodoListDatabaseHelper todoListDatabaseHelper = new TodoListDatabaseHelper(mContext);
        mParentId = (int) todoListDatabaseHelper.createTodo(todoName);
        mTodoListCreateCallback.onTodoCreated(todoName);
        mTaskCreatedCallback = new TaskCreatedCallback() {
            @Override
            public void onTaskCreated() {
                NetworkUtils.getInstance(mContext).doCreateTask(mParentId, editTextTaskName.getText().toString(), new TaskCreatedCallback() {
                    @Override
                    public void onTaskCreated() {
                        Log.d("task", "synced");
                    }

                    @Override
                    public void onTaskCreated(int mParentId, String s) {

                    }
                });
            }

            @Override
            public void onTaskCreated(int mParentId, String s) {

            }
        };
        storeTask(editTextTaskName.getText().toString());
    }

    @OnClick(R.id.cancel_button)
    void onCancelButtonClicked() {
        dismiss();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnEditorAction(R.id.input_edit_text)
    boolean onTodoFilled() {
        if (editTextTaskName.getVisibility() == View.VISIBLE)
            editTextTaskName.requestFocus();
        if (CURRENT_ITEM_TYPE == TASK_TYPE) {
            storeTask(editText.getText().toString());
        }
        return true;
    }

}
