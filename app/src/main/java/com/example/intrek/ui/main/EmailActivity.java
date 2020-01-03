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

import com.example.intrek.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.UUID;

// For user to enter email. It will get different exceptions,
// decides whether the user is registered or not, and send the result to PasswordActivity.
public class EmailActivity extends AppCompatActivity {

    private static final String TAG = EmailActivity.class.getSimpleName();

    // Fields

    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String NEW_USER = "NEW_USER";

    private EditText mEmailInputField;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    // Default methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        // Get input of email and send it to PasswordActivity
        mEmailInputField = findViewById(R.id.email_input);
        Button mNextButton = findViewById(R.id.email_btn);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkingEmail(mEmailInputField.getText().toString(), UUID.randomUUID().toString().substring(0, 8));
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    // Methods

    // Check whether the user was previously registered or not
    private void checkingEmail(final String email, String password) {
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

        // Get different exceptions to decide whether the user was registered or not
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                Log.d(TAG, "signInWithEmailAndPassword: this is a new user.");
                                proceedToPassword(email, true);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Log.d(TAG, "signInWithEmailAndPassword: known user is back.");
                                proceedToPassword(email, false);
                            } catch (Exception e) {
                                Log.d(TAG, "signInWithEmailAndPassword: known user is back.");
                                proceedToPassword(email, false);
                            }
                        }

                        // Hide progress dialog
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    // Create and start an intent to go to PasswordActivity
    private void proceedToPassword(String email, boolean newUser) {
        Intent intent = new Intent(EmailActivity.this, PasswordActivity.class);
        intent.putExtra(USER_EMAIL, email);
        intent.putExtra(NEW_USER, newUser);
        startActivityForResult(intent, PasswordActivity.NEW_USER_TYPE);
    }

    // Check if email is left empty
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailInputField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailInputField.setError("Required.");
            valid = false;
        } else {
            mEmailInputField.setError(null);
        }

        return valid;
    }
}
