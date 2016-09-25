package ru.solandme.simpleblog;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;
    private ImageButton setupImageBtn;
    private EditText nameField;
    private Button submitBtn;
    private Uri imageUri = null;

    private DatabaseReference databaseRefUsers;
    private FirebaseAuth auth;
    private StorageReference storageImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        auth = FirebaseAuth.getInstance();
        storageImages = FirebaseStorage.getInstance().getReference().child("Profile_images");
        databaseRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        setupImageBtn = (ImageButton) findViewById(R.id.setupImageBtn);
        nameField = (EditText) findViewById(R.id.setupNameField);
        submitBtn = (Button) findViewById(R.id.setupSubmitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });

        setupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

            }
        });
    }

    private void startSetupAccount() {

        final String name = nameField.getText().toString().trim();
        final String user_id = auth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name) && imageUri != null) {

            StorageReference filepath = storageImages.child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    databaseRefUsers.child(user_id).child("name").setValue(name);
                    databaseRefUsers.child(user_id).child("image").setValue(downloadUri);
                }

            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageUri = data.getData();

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                setupImageBtn.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
