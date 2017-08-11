package com.huk.todo.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.huk.todo.R;
import com.huk.todo.callbacks.LoginCallback;
import com.huk.todo.helper.SharedPrefsUtils;
import com.huk.todo.network.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.huk.todo.helper.Constants.ALL_PERMISSION_GRANTED;
import static com.huk.todo.helper.Constants.IS_LOGIN;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_username)
    EditText usernameText;
    @BindView(R.id.input_password)
    EditText passwordText;
    @BindView(R.id.btn_login)
    Button loginButton;
    @BindView(R.id.link_signup)
    TextView signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPrefsUtils.getBooleanPreference(this, IS_LOGIN, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.finish();
            startActivity(intent);
        }
        overridePendingTransition(0,0);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setBackgroundColor(Color.GRAY);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userName = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        NetworkUtils.getInstance().doLogin(userName, password, new LoginCallback() {
            @Override
            public void onDoLogin() {
                onLoginSuccess();
                progressDialog.dismiss();
                SharedPrefsUtils.setBooleanPreference(LoginActivity.this, IS_LOGIN, true);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLoginFailed() {
                progressDialog.dismiss();
                loginButton.setEnabled(true);
                loginButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                Snackbar.make(loginButton, "Login failed", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginFailed(String errorMessage) {
                progressDialog.dismiss();
                loginButton.setEnabled(true);
                loginButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                Snackbar.make(loginButton, errorMessage, Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(loginButton, "Registerd Successfully. You can now login.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Snackbar.make(loginButton, "Login failed", Snackbar.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String userName = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (userName.isEmpty() || userName.length() < 4) {
            usernameText.setError("enter a valid username");
            valid = false;
        } else {
            usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();
        if (!SharedPrefsUtils.getBooleanPreference(this, ALL_PERMISSION_GRANTED, false)) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WAKE_LOCK, Manifest.permission.RECEIVE_BOOT_COMPLETED}, new PermissionsResultAction() {

                        @Override
                        public void onGranted() {
                            SharedPrefsUtils.setBooleanPreference(LoginActivity.this, ALL_PERMISSION_GRANTED, true);
                        }

                        @Override
                        public void onDenied(String permission) {
                            Toast.makeText(LoginActivity.this,
                                    "Sorry, we need Permissions to run this app",
                                    Toast.LENGTH_SHORT).show();
                            LoginActivity.this.finish();
                        }
                    });
        }
    }
}
