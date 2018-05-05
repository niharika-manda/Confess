package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

    public class RegisterActivity extends AppCompatActivity {

        private Button registerButton;
        private EditText nameField;
        private EditText emailField;
        private EditText passwordField;
        private FirebaseAuth firebaseAuth;
        private ProgressDialog progressDialog;
        private DatabaseReference databaseReference;
        private FirebaseAuth.AuthStateListener authStateListener;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);


            progressDialog = new ProgressDialog(this);

            registerButton = (Button) findViewById(R.id.register_button);
            nameField = (EditText) findViewById(R.id.name_textField);
            emailField = (EditText) findViewById(R.id.email_textField);
            passwordField = (EditText) findViewById(R.id.password_textField);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

            firebaseAuth = FirebaseAuth.getInstance();



            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    register();

                }
            });
        }

        private void register() {
            final String nickname = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if(!TextUtils.isEmpty(nickname) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                progressDialog.setMessage("Signing In..");
                progressDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            progressDialog.dismiss();

                            String user_id = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_id =  databaseReference.child(user_id);
                            current_user_id.child("nickname").setValue(nickname);
                            current_user_id.child("image").setValue("default");
                            //   current_user_id.child("password").setValue(password);
                            progressDialog.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);

                        }
                    }
                });
            }
        }
    }

