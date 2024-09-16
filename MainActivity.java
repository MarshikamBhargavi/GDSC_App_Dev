package com.example.gallery;



import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "GOOGLEAUTH";
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wait1);
        dialog.setCanceledOnTouchOutside(false);
        // Getting the Button Click
        Button signInbtn = findViewById(R.id.google_signIn);
        signInbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();

            }
        });
    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            dialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                dialog.dismiss();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent i = new Intent(MainActivity.this,Logout.class);
                            startActivity(i);
                            finish();
                            dialog.dismiss();
                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //  Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //  Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            // updateUI(null);
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    private ArrayList<DataClass> dataList;
    private MyAdapter adapter;

    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    private ActivityMainBinding binding;
    private Uri imageUri;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GridView gridView = findViewById(R.id.gridView);
        dataList = new ArrayList<>();
        adapter = new MyAdapter(dataList, this);
        gridView.setAdapter(adapter); // Set adapter to GridView

        // Load images from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();  // Clear list to avoid duplication
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    if (dataClass != null) {
                        dataList.add(dataClass); // Add each image URL to the list
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh adapter after data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });

        binding.selectImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        binding.uploadimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataClass selectedImage = dataList.get(position);
                Intent intent = new Intent(MainActivity.this, FullScreenActivity.class);
                intent.putExtra("imageUrl", selectedImage.getImageUrl());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                binding.firebaseimage.setImageURI(imageUri); // Display selected image in ImageView
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100); // Open gallery to select image
    }

    private void uploadImage() {
        if (imageUri == null) {
            Toast.makeText(MainActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File...");
        progressDialog.show();

        // Create unique file name using date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now) + ".jpg"; // Adding file extension
        storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName); // Specify storage path

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get download URL of the uploaded file
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        // Store the image URL in Firebase Realtime Database
                        DatabaseReference newImageRef = databaseReference.push(); // Create new node for each image
                        DataClass dataClass = new DataClass(imageUrl);
                        newImageRef.setValue(dataClass);  // Save the image URL in the database

                        binding.firebaseimage.setImageURI(null); // Clear the ImageView
                        Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
