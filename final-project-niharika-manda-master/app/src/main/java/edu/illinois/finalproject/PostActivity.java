package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class PostActivity extends AppCompatActivity {
    private ImageButton imageButton;
    private EditText confessTitle;
    private EditText confessDescription;
    private Button confessButton;
    private Uri imageUri = null;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseUsers;
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 100;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //requests permission to read, and write on external storage
        ActivityCompat.requestPermissions(this,new String[]
                {android.Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_READ_STORAGE);

        imageButton = (ImageButton)findViewById(R.id.postImageButton);
        confessTitle = (EditText) findViewById(R.id.confessTitleText);
        confessDescription = (EditText) findViewById(R.id.confessDescriptionText);
        confessButton = (Button) findViewById(R.id.confessButton);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();


        //gets firebase reference and storage reference to root directory
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Confessions");
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());



        /**
         * Sets an onclicklistener to the image button, which allows the user
         * to add an image to the imageview.
         *
         */
        //source: https://code.tutsplus.com/tutorials/image-upload-to-firebase-in-android-application--cms-29934
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST );

            }
        });

        /**
         * Sets an onclicklistener to the submit button which
         * uploads the data onto firebase database
         */
        confessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post();
            }
        });


    }

    /**
     * Method to upload confession data to the firebase database and firebase storage.
     * Images are stored in a folder in the storage and the title and description
     * are stored in the firebase as children.
     * A progress dialog marks the progress.
     */
    //some of the documentation
    // from https://firebase.google.com/docs/database/android/read-and-write
    private void post() {
        progressDialog.setMessage("Posting Confession");
        progressDialog.show();

        final String confessTitle_Value = confessTitle.getText().toString().trim();
        final String confessDesc_Value = confessDescription.getText().toString().trim();
        if(!TextUtils.isEmpty(confessDesc_Value) &&
                !TextUtils.isEmpty(confessTitle_Value)) {

            StorageReference filePath = storageReference.child("Confess_Image").child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")
                  final  Uri downloadUri = taskSnapshot.getDownloadUrl();

                   final DatabaseReference newConfession = databaseReference.push();


                    databaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newConfession.child("Confession_Title").setValue(confessTitle_Value);
                            newConfession.child("Confession_Description").setValue(confessDesc_Value);
                            newConfession.child("Image").setValue(downloadUri.toString());
                            newConfession.child("uid").setValue(currentUser.getUid());
                            newConfession.child("nickname").setValue(dataSnapshot.child("nickname").getValue());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.setMessage("Confession Posted!");
                    progressDialog.show();
                    progressDialog.dismiss();

                }
            });
        }
    }

    /**
     * This method uses android image cropper tool to fit the image to the
     * ratio of the image button. It takes the image from the device's gallery
     * and uploads it to the imageview button.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    // source: https://github.com/ArthurHub/Android-Image-Cropper/blob/master/README.md
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageButton.setImageURI(imageUri);
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageButton.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
