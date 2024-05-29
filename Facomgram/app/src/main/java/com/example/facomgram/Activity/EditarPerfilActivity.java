package com.example.facomgram.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import com.bumptech.glide.Glide;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class EditarPerfilActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY = 2;

    private ImageView image_profileAE;
    private EditText edt_nome, edt_username, edt_bio;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        ImageView ic_closeEP = findViewById(R.id.ic_closeEP);
        TextView txt_salvarAE = findViewById(R.id.txt_salvarAE);
        TextView txt_mudarfoto = findViewById(R.id.txt_mudarfoto);
        image_profileAE = findViewById(R.id.image_profileAE);
        edt_nome = findViewById(R.id.edt_nome);
        edt_username = findViewById(R.id.edt_username);
        edt_bio = findViewById(R.id.edt_bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("uploads_fotos_perfil");

        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                edt_nome.setText(user.getNome());
                edt_username.setText(user.getUsername());
                edt_bio.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profileAE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ic_closeEP.setOnClickListener(v -> finish());

        image_profileAE.setOnClickListener(v -> {
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

        txt_mudarfoto.setOnClickListener(v -> {
            String[] options = {"Escolher da galeria", "Tirar foto"};
            AlertDialog.Builder build = new AlertDialog.Builder(v.getContext());
            build.setTitle("Foto de perfil");
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

        txt_salvarAE.setOnClickListener(v -> salvar(edt_nome.getText().toString(), edt_username.getText().toString(), edt_bio.getText().toString()));
    }

    private void salvar(String nome, String username, String bio) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Salvando..");
        progressDialog.show();

        storageReference = FirebaseStorage.getInstance().getReference("upload_fotos_perfil");
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(firebaseUser.getUid());

        StorageReference imagemRef = storageReference.child("upload_fotos_perfil").child(FieldValue.serverTimestamp() + ".jpg");

        if(uri != null){
            imagemRef.putFile(uri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        HashMap<String, Object> hasmap = new HashMap();
                        hasmap.put("nome", nome);
                        hasmap.put("username", username);
                        hasmap.put("bio", bio);
                        hasmap.put("imageurl", uri.toString());
                        databaseReference.updateChildren(hasmap);

                        progressDialog.dismiss();
                        finish();
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(EditarPerfilActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            progressDialog.dismiss();
            Toast.makeText(EditarPerfilActivity.this, "Mude a imagem", Toast.LENGTH_SHORT).show();
        }


    }


    private void escolherfoto() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void tirarfoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            assert data != null;
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            image_profileAE.setImageBitmap(bitmap);
            saveImage(bitmap);
            escolherfoto();
        } else if (requestCode == GALLERY) {
            if (data != null) {
                uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    image_profileAE.setImageBitmap(bitmap);
                    image_profileAE.setImageURI(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EditarPerfilActivity.this, "Ops, ocorreu algum problema!", Toast.LENGTH_SHORT).show();
                }
            }
        }
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