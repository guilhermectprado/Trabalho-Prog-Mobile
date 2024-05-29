package com.example.facomgram.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.facomgram.MainActivity;
import com.example.facomgram.Model.Post;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY = 2;

    private ImageView imagem_adicionada;
    private EditText edt_descricao;

    private Uri imageUri;
    private String idPublicador;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ImageView ic_close = findViewById(R.id.ic_close);
        imagem_adicionada = findViewById(R.id.imagem_adicionada);
        TextView txt_postar = findViewById(R.id.txt_postar);
        edt_descricao = findViewById(R.id.edt_descricao);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        idPublicador = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        ic_close.setOnClickListener(v -> {
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        });

        txt_postar.setOnClickListener(v -> publicarPost());

        imagem_adicionada.setOnClickListener(v -> {
            String[] options = {"Escolher da galeria", "Tirar foto"};
            AlertDialog.Builder build = new AlertDialog.Builder(v.getContext());
            build.setTitle("Foto do post");
            build.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        escolherfoto();
                        break;
                    case 1:
                        tirarfoto();
                        break;
                }
            });
            build.show();
        });


    }


    private void publicarPost() {
        //progressBar.setVisibility(View.VISIBLE);
        String descricao = edt_descricao.getText().toString();

        if (!descricao.isEmpty() && imageUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Postando...");
            progressDialog.show();

            StorageReference postRef = storageReference.child("post").child(FieldValue.serverTimestamp() + ".jpg");

            postRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    postRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String postId = databaseReference.push().getKey();

                        Post post = new Post();
                        post.setIdPost(postId);
                        post.setImagePost(uri.toString());
                        post.setDescricao(descricao);
                        post.setIdPublicador(idPublicador);
                        post.salvar();

                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Post Publicado :D", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));

                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(PostActivity.this, "Insira a foto e uma descrição..", Toast.LENGTH_SHORT).show();
        }

    }

    private void escolherfoto() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void tirarfoto() {
        Intent capturaFotointent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(capturaFotointent, REQUEST_IMAGE_CAPTURE);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            Bitmap bitmap = null;
            if (extras != null) {
                bitmap = (Bitmap) extras.get("data");
            }
            imagem_adicionada.setImageBitmap(bitmap);
            saveImage(bitmap);
            escolherfoto();

        } else if (requestCode == GALLERY) {
            if (data != null) {
                imageUri = data.getData();
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imagem_adicionada.setImageBitmap(bitmap);
                    imagem_adicionada.setImageURI(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (checkPermission()) {

        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permissão aceita",
                        Toast.LENGTH_SHORT).show();
                // main logic
            } else {
                Toast.makeText(getApplicationContext(), "Permission negada",
                        Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel(
                                (dialog, which) -> requestPermission());
                    }
                }
            }
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(PostActivity.this)
                .setMessage("Você precisa permitir permissões de acesso")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    private void saveImage(Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentValues values = contentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, this.getContentResolver().openOutputStream(uri));
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    this.getContentResolver().update(uri, values, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));

            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".png";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}