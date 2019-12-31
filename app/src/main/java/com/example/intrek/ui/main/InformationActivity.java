package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intrek.DataModel.Profile;
import com.example.intrek.MainActivity;
import com.example.intrek.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InformationActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef = database.getReference("profiles");
    private static DatabaseReference profileRef;

    private static final int PICK_IMAGE = 1;

    private TextView mDisplayNameInputField;
    private Button mDoneButton;

    private File imageFile;
    private Uri savedImageUri;
    private Profile userProfile;
    private FirebaseUser user;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        // Get FirebaseUser
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            Log.e(TAG, uid);

            profileRef = profileGetRef.child(uid);
            profileGetRef.child(uid).child("email").setValue(email);
            userProfile = new Profile(uid, email);
        }

        mDisplayNameInputField = findViewById(R.id.display_name_input);
        mDoneButton = findViewById(R.id.information_btn);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress dialog
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(InformationActivity.this);
                    mProgressDialog.setMessage("Loading");
                    mProgressDialog.setIndeterminate(true);
                }

                mProgressDialog.show();
                userProfile.setUsername(mDisplayNameInputField.getText().toString());
                addProfileToFirebaseDB();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", savedImageUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            imageFile = new File(getExternalFilesDir(null), "profileImage");
            try {
                copyImage(imageUri, imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                savedImageUri = Uri.fromFile(imageFile);
                InputStream imageStream = getContentResolver().openInputStream(savedImageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView imageView = findViewById(R.id.profile_picture);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void chooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void copyImage(Uri uriInput, File fileOutput) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = getContentResolver().openInputStream(uriInput);
            out = new FileOutputStream(fileOutput);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
    }

    private void addProfileToFirebaseDB() {
        // convert the photo to raw bytes
        BitmapDrawable bitmapDrawable = (BitmapDrawable)
                ((ImageView) findViewById(R.id.profile_picture)).getDrawable();
        if (bitmapDrawable == null) {
            Toast.makeText(this, "Missing picture", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        // upload the photo to storage (use database key as part the url)
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference photoRef = storageRef.child("photos").child(profileRef.getKey() + ".jpg");
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // handle failed uploads
                Toast.makeText(InformationActivity.this,
                        "Upload photo failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new PhotoUploadSuccessListener());
    }

    private class PhotoUploadSuccessListener implements OnSuccessListener<UploadTask.TaskSnapshot> {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(
                    new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            userProfile.setPhotoPath(uri.toString());
                            profileRef.runTransaction(new ProfileDataUploadHandler());
                        }
                    });
        }
    }

    private class ProfileDataUploadHandler implements Transaction.Handler {
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            mutableData.child("username").setValue(userProfile.getUsername());
            mutableData.child("photo").setValue(userProfile.getPhotoPath());
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
            if (b) {
                Toast.makeText(InformationActivity.this,
                        "Registration succeeded", Toast.LENGTH_SHORT).show();
                // execute only in case the transaction succeeds
                Intent intent = new Intent(InformationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ProfileFragment.UID, userProfile.getUid());
                startActivity(intent);
                // Hide progress dialog
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } else {
                Toast.makeText(InformationActivity.this,
                        "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
