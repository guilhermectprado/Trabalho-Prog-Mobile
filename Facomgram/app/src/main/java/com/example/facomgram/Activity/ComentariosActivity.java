package com.example.facomgram.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.facomgram.Adapter.ComentarioAdapter;
import com.example.facomgram.Model.Comentario;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ComentariosActivity extends AppCompatActivity {
    private ComentarioAdapter comentarioAdapter;
    private List<Comentario> comentariosList;

    private EditText edt_comentario;
    private ImageView image_profileC;

    private String idPost;
    private String idPublicador;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        edt_comentario = findViewById(R.id.edt_comentario);
        image_profileC = findViewById(R.id.image_profileC);
        TextView txt_comentar = findViewById(R.id.txt_comentar);
        RecyclerView recycler_viewC = findViewById(R.id.recycler_viewC);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbarC = findViewById(R.id.toolbarC);
        setSupportActionBar(toolbarC);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comentários");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarC.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        idPost = intent.getStringExtra("idPost");
        idPublicador = intent.getStringExtra("idPublicador");

        recycler_viewC = findViewById(R.id.recycler_viewC);
        recycler_viewC.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_viewC.setLayoutManager(linearLayoutManager);

        comentariosList = new ArrayList<>();
        comentarioAdapter = new ComentarioAdapter(this, comentariosList, idPost);
        recycler_viewC.setAdapter(comentarioAdapter);


        txt_comentar.setOnClickListener(v -> {
            if (edt_comentario.getText().toString().equals("")) {
                Toast.makeText(ComentariosActivity.this, "Não pode enviar  comentário vazio.", Toast.LENGTH_SHORT).show();
            } else {
                inserirComentario();
            }
        });

        setImage_profileCImage();
        listarComentarios();

    }

    // Insere os comentarios no banco
    private void inserirComentario() {
        reference = FirebaseDatabase.getInstance().getReference("comentarios").child(idPost);

        String idComentario = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("idComentario", idComentario);
        hashMap.put("comentario", edt_comentario.getText().toString());
        hashMap.put("idPublicador", firebaseUser.getUid());

        if (idComentario != null) {
            reference.child(idComentario).setValue(hashMap);
        }
        addNotificacoes();
        edt_comentario.setText("");
    }

    // Salva as notificações no banco.
    private void addNotificacoes(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notificacoes").child(idPublicador);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUser",firebaseUser.getUid());
        hashMap.put("texto"," comentou: "+edt_comentario.getText().toString());
        hashMap.put("idPost",idPost);
        hashMap.put("ispost",false);

        ref.push().setValue(hashMap);

    }

    // Seta a imagem do perfil do usuario na barrinha de inserção de comentario
    private void setImage_profileCImage() {
        reference = FirebaseDatabase.getInstance().getReference("usuarios").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profileC);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Lista os comentários do post na activity
    private void listarComentarios() {
        reference = FirebaseDatabase.getInstance().getReference("comentarios").child(idPost);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comentariosList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comentario comentario = snapshot.getValue(Comentario.class);
                    comentariosList.add(comentario);
                }

                comentarioAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}