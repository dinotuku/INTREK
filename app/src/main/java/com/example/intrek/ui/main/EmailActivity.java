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

public class EmailActivity extends AppCompatActivity  {

    private static final String TAG = EmailActivity.class.getSimpleName();

    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String NEW_USER = "NEW_USER";

    private EditText mEmailInputField;
    private Button mNextButton;

    private FirebaseAuth mAuth;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        mEmailInputField = findViewById(R.id.emailInputField);
        mNextButton = findViewById(R.id.emailNextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkingEmail(mEmailInputField.getText().toString(), UUID.randomUUID().toString().substring(0, 8));
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void checkingEmail(final String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

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

                        hideProgressDialog();
                    }
                });
    }

    private void proceedToPassword(String email, boolean newUser) {
        Intent intent = new Intent(EmailActivity.this, PasswordActivity.class);
        intent.putExtra(USER_EMAIL, email);
        intent.putExtra(NEW_USER, newUser);
        startActivityForResult(intent, PasswordActivity.NEW_USER_TYPE);
    }

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

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
