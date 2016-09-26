package ru.solandme.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton selectImage;
    private EditText postTitle;
    private EditText postDesc;
    private Button submitBtn;
    private Uri imageUri = null;

    private StorageReference storage;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRefUser;

    private ProgressDialog progress;

    private static final int GALLERY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseRefUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

        selectImage = (ImageButton) findViewById(R.id.imageSelect);
        postTitle = (EditText) findViewById(R.id.titleField);
        postDesc = (EditText) findViewById(R.id.descField);
        submitBtn = (Button) findViewById(R.id.submitButton);

        progress = new ProgressDialog(this);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        progress.setMessage("Posting to Blog ...");

        final String title_val = postTitle.getText().toString().trim();
        final String desc_val = postDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && imageUri != null) {

            progress.show();

            StorageReference filePath = storage.child("Blog_Images").child(imageUri.getLastPathSegment());

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPostRef = databaseReference.push();

                    databaseRefUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPostRef.child("title").setValue(title_val);
                            newPostRef.child("description").setValue(desc_val);
                            newPostRef.child("imageURL").setValue(downloadUrl.toString());
                            newPostRef.child("uid").setValue(currentUser.getUid());
                            newPostRef.child("username").setValue(dataSnapshot.child("name")
                                    .getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    progress.dismiss();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            selectImage.setImageURI(imageUri);

        }
    }
}
