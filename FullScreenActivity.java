package com.example.gallery;

import static android.content.Intent.getIntent;
import static android.os.Build.VERSION_CODES.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        // Get the image URL from the intent
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Load the image into ImageView
        assert imageUrl != null;
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                fullScreenImageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FullScreenActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_full_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deletePhoto:
                showDeleteConfirmationDialog();
                return true;
            case R.id.downloadPhoto:
                downloadPhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePhoto();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePhoto() {
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FullScreenActivity.this, "Photo deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FullScreenActivity.this, "Failed to delete photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadPhoto() {
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                File file = new File(getExternalFilesDir(null), "downloaded_photo.jpg");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    Toast.makeText(FullScreenActivity.this, "Photo downloaded", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(FullScreenActivity.this, "Failed to download photo", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FullScreenActivity.this, "Failed to download photo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
