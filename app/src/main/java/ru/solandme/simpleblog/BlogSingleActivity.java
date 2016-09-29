package ru.solandme.simpleblog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private FirebaseAuth auth;
    private String postKey = null;
    private ImageView singleImageSelect;
    private EditText singleTitleField;
    private EditText singleDescField;
    private Button singleRemoveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        postKey = getIntent().getExtras().getString("postKey");
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        auth = FirebaseAuth.getInstance();

        singleImageSelect = (ImageView) findViewById(R.id.singleImageSelect);
        singleTitleField = (EditText) findViewById(R.id.singleTitleField);
        singleDescField = (EditText) findViewById(R.id.singleDescField);

        singleRemoveButton = (Button) findViewById(R.id.singleRemoveButton);

        databaseRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageURL = (String) dataSnapshot.child("imageURL").getValue();
                String postTitle = (String) dataSnapshot.child("title").getValue();
                String postDesc = (String) dataSnapshot.child("description").getValue();
                String postUid = (String) dataSnapshot.child("uid").getValue();

                singleTitleField.setText(postTitle);
                singleTitleField.setEnabled(false);
                singleDescField.setText(postDesc);
                singleDescField.setEnabled(false);
                singleImageSelect.setClickable(false);

                Picasso.with(BlogSingleActivity.this).load(imageURL).into(singleImageSelect);

                if (auth.getCurrentUser().getUid().equals(postUid)) {
                    singleImageSelect.setClickable(true);
                    singleTitleField.setEnabled(true);
                    singleDescField.setEnabled(true);
                    singleRemoveButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        singleRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseRef.child(postKey).removeValue();
                finish();
            }
        });
    }
}
