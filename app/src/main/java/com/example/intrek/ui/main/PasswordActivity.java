package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intrek.MainActivity;
import com.example.intrek.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// For user to enter password. It will validate the password and
// use FirebaseAuth to either sign in or register the user
// depending on the exceptions threw in EmailActivity.
public class PasswordActivity extends AppCompatActivity {

    private static final String TAG = PasswordActivity.class.getSimpleName();

    // Fields

    public static final int NEW_USER_TYPE = 1;

    private TextView mPasswordInformationBar;
    private EditText mPasswordInputField;
    private Button mSignInButton;
    private ProgressDialog mProgressDialog;

    private String email;
    private boolean newUser = true;

    private FirebaseAuth mAuth;

    // Default methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // Get password and either register or sign in a user
        mPasswordInformationBar = findViewById(R.id.pwd_information_bar);
        mPasswordInputField = findViewById(R.id.pwd_input);
        mSignInButton = findViewById(R.id.pwd_btn);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newUser) {
                    createAccount(email, mPasswordInputField.getText().toString());
                } else {
                    signIn(email, mPasswordInputField.getText().toString());
                }
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get intent extra
        email = getIntent().getStringExtra(EmailActivity.USER_EMAIL);
        newUser = getIntent().getBooleanExtra(EmailActivity.NEW_USER, true);
        if (!newUser) {
            mPasswordInformationBar.setText(R.string.registered_user_message);
            mPasswordInputField.setHint("Enter your password");
        }
    }

    // Methods

    // Create a new account for a new user
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // Show progress dialog
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Intent intent = new Intent(PasswordActivity.this, InformationActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(PasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Hide progress dialog
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    // Sign in an existing user
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // Show progress dialog
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(ProfileFragment.UID, mAuth.getCurrentUser().getUid());
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(PasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // Hide progress dialog
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    // Validate if the password is empty or less than 6 characters
    private boolean validateForm() {
        boolean valid = true;

        String password = mPasswordInputField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordInputField.setError("Required.");
            valid = false;
        } else if (password.length() < 6) {
            mPasswordInputField.setError("Less than 6 characters.");
            valid = false;
        }
        else {
            mPasswordInputField.setError(null);
        }

        return valid;
    }
}
