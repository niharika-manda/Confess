package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private RecyclerView confessionList;
    private FirebaseAuth firebaseAuth;


    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUser;
    private boolean clickProcess = false;
    private DatabaseReference databaseLikes;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confessionList = (RecyclerView) findViewById(R.id.confess_list);
        confessionList.setHasFixedSize(true);
        confessionList.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Confessions");
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");

        databaseLikes.keepSynced(true);
        databaseReference.keepSynced(true);
        databaseReferenceUser.keepSynced(true);

        //setting values and references to the variables responsible for firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent loginIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    //user will not be able to go back
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }

            }
        };
    }

    /**
     * This method is called at the start of the app.
     * It has a firebaseRecycler Adapter which takes in the viewholder
     * and the scroll layout. It populates the viewholders with user data and updates it realtimee.
     */
    @Override
    protected void onStart() {
        super.onStart();

       // existingUser();
        firebaseAuth.addAuthStateListener(authStateListener);

        FirebaseRecyclerAdapter<Confession, ConfessionViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Confession, ConfessionViewHolder>(
                Confession.class, R.layout.confession_scroll, ConfessionViewHolder.class, databaseReference) {


            @Override
            protected void populateViewHolder(ConfessionViewHolder viewHolder, Confession model, int position) {

                final String post_key = getRef(position).getKey();
                viewHolder.setConfessionTitle(model.getConfession_Title());
                viewHolder.setConfessionDescription(model.getConfession_Description());
                viewHolder.setConfessionImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeButton(post_key);
                
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        clickProcess = true;
                            databaseLikes.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (clickProcess) {
                                        if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                            databaseLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                            clickProcess = false;

                                        } else {
                                            databaseLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue("Liked");
                                            clickProcess = false;

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                });


            }
        };


        confessionList.setAdapter(firebaseRecyclerAdapter);

    }


    //source: https://androidjson.com/upload-image-to-firebase-storage/

    /**
     * ConfessionViewHolder Class extends the recyclerview viewholder so
     * each view holder can be attached to one of the data children in the database
     */
    public static class ConfessionViewHolder extends RecyclerView.ViewHolder {

        private View view;
        Button likeButton;
        DatabaseReference databaseReference;
        FirebaseAuth firebaseAuth;

        public ConfessionViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            likeButton = (Button) view.findViewById(R.id.like_button);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference.keepSynced(true);

        }

        public void setLikeButton (final String post_key) {
            databaseReference.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
     if(dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
         likeButton.setBackgroundColor(Color.parseColor("#F02185"));
     } else {
        likeButton.setBackgroundColor(Color.parseColor("#DBD0D5"));
     }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
        });
        }


        /**
         * Sets the textbox to the name of the confession that the user enters.
         *
         * @param confessionTitle -- String title of confession that user enters
         */
        public void setConfessionTitle(String confessionTitle) {
            TextView confession_title = view.findViewById(R.id.confession_title);
            confession_title.setText(confessionTitle);
        }

        /**
         * Sets the textbox to the description of the confession that the user enters.
         *
         * @param confessionDescription -- String description that the user enters
         */
        public void setConfessionDescription(String confessionDescription) {
            TextView confession_description = view.findViewById(R.id.confession_description);
            confession_description.setText(confessionDescription);
        }

        /**
         * Sets the textbox to the image of the confession that the user uploads.
         *
         * @param context -- context of the recyclerview adapter
         * @param Image   -- png/jpeg image that the user uploads via android device
         */
        public void setConfessionImage(Context context, String Image) {

            ImageView confession_image = view.findViewById(R.id.confession_image);
            Picasso.with(context).load(Image).into(confession_image);
        }

    }

    /**
     * This method inflates the home screen with the main menu on the creation
     * of the app.
     *
     * @param menu - manu layout
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method returns opens a new activity when the action add
     * button is pressed.
     *
     * @param menuItem MenuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_action) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (menuItem.getItemId() == R.id.action_bar_settings) {
            FirebaseAuth.getInstance().signOut();
        }

        return super.onOptionsItemSelected(menuItem);
    }
    /**
     * This method checks to see if there is already
     * an existing user in the firebase database with the given email ID.
     */
    private void existingUser() {

        final String user_id = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //checking if user id exists
                if(!dataSnapshot.hasChild(user_id)) {

                    Intent mainIntent = new Intent(MainActivity.this, AccountSetupActivity.class);
                    //user will not be able to go back
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);


                } else {
                    Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}