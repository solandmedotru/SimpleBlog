package ru.solandme.simpleblog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView blogList;
    private DatabaseReference databaseRef;
    private DatabaseReference databaseRefUsers;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String user_id;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                } else {
                    user_id = auth.getCurrentUser().getUid();
                }
            }
        };

        databaseRef = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseRefUsers.keepSynced(true);
        databaseRef.keepSynced(true);

        blogList = (RecyclerView) findViewById(R.id.blogList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        blogList.setHasFixedSize(true);
        blogList.setLayoutManager(layoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        checkUserExist();
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                        Blog.class, R.layout.blog_row, BlogViewHolder.class, databaseRef
                ) {
                    @Override
                    protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {
                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDescription());
                        viewHolder.setImage(getApplicationContext(), model.getImageURL());
                        viewHolder.setUsername(model.getUsername());
                    }
                };

        blogList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;

        public BlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setTitle(String title) {
            TextView postTitle = (TextView) view.findViewById(R.id.postTitle);
            postTitle.setText(title);
        }

        void setDesc(String desc) {
            TextView postDescription = (TextView) view.findViewById(R.id.postDesc);
            postDescription.setText(desc);
        }

        void setImage(Context context, String imageUrl) {
            ImageView postImage = (ImageView) view.findViewById(R.id.postImage);
            Picasso.with(context).load(imageUrl).into(postImage);
        }

        void setUsername(String username) {
            TextView postUsername = (TextView) view.findViewById(R.id.postUsername);
            postUsername.setText(username);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        auth.signOut();
    }


    private void checkUserExist() {

        databaseRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user_id)) {
                    Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                    setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
