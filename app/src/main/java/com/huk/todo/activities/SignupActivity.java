package com.huk.todo.activities;

import android.app.ProgressDialog;
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

import com.huk.todo.R;
import com.huk.todo.callbacks.SignupCallback;
import com.huk.todo.network.NetworkUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name)
    EditText nameText;
    @BindView(R.id.input_username)
    EditText userText;
    @BindView(R.id.input_password)
    EditText passwordText;
    @BindView(R.id.btn_signup)
    Button signupButton;
    @BindView(R.id.link_login)
    TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);
        signupButton.setBackgroundColor(Color.GRAY);
        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = nameText.getText().toString();
        String userName = userText.getText().toString();
        String password = passwordText.getText().toString();

        NetworkUtils.getInstance().doSignup(userName, password, new SignupCallback() {
            @Override
            public void onDoSignup() {
                onSignupSuccess();
                progressDialog.dismiss();
                signupButton.setEnabled(true);
            }

            @Override
            public void onSignupFailed() {
                progressDialog.dismiss();
                signupButton.setEnabled(true);
                signupButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                Snackbar.make(signupButton, "Signup failed", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onSignupFailed(String s) {
                progressDialog.dismiss();
                signupButton.setEnabled(true);
                signupButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                Snackbar.make(signupButton, s, Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Snackbar.make(signupButton, "Signup failed", Snackbar.LENGTH_SHORT).show();
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String userName = userText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (userName.isEmpty() || userName.length() < 4) {
            userText.setError("enter a valid username");
            valid = false;
        } else {
            userText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
