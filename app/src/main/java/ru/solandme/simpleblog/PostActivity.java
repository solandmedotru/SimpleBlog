package ru.solandme.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
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

    private ProgressDialog progress;

    private static final int GALLARY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        storage = FirebaseStorage.getInstance().getReference();

        selectImage = (ImageButton) findViewById(R.id.imageSelect);
        postTitle = (EditText) findViewById(R.id.titleField);
        postDesc = (EditText) findViewById(R.id.descField);
        submitBtn = (Button) findViewById(R.id.submitButton);

        progress = new ProgressDialog(this);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galaryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent, GALLARY_REQUEST);
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
        progress.show();

        String title_val = postTitle.getText().toString().trim();
        String desc_val = postDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && imageUri != null) {

            StorageReference filePath = storage.child("Blog_Images").child(imageUri.getLastPathSegment());

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    progress.dismiss();
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLARY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            selectImage.setImageURI(imageUri);

        }
    }
}
