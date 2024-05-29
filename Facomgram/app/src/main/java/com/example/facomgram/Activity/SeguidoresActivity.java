package com.example.facomgram.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facomgram.Adapter.UserAdapter;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeguidoresActivity extends AppCompatActivity {
    private String idUsuarios;

    private UserAdapter userAdapter;
    private List<String> idList;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguidores);

        // Recebe um idPerfil para listar os seguidores ou quem ele segue
        Intent intent = getIntent();
        idUsuarios = intent.getStringExtra("idUsuario");
        String titulo = intent.getStringExtra("titulo");

        Toolbar toolbarAS = findViewById(R.id.toolbarAS);
        setSupportActionBar(toolbarAS);
        Objects.requireNonNull(getSupportActionBar()).setTitle(titulo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarAS.setNavigationOnClickListener(v -> finish());

        RecyclerView recycler_view_AS = findViewById(R.id.recycler_view_AS);
        recycler_view_AS.setHasFixedSize(true);
        recycler_view_AS.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList,false);
        recycler_view_AS.setAdapter(userAdapter);

        idList = new ArrayList<>();

        // Se clicar em Seguidores, vai listar os seguidores
        // Se clicar em Seguindo vai listar quem o usuário segue.
        // Se clicar em Likes vai mostrar uma lista de quem curtiu o Post
        switch (titulo) {
            case "Likes":
                getLikes();
                break;
            case "Seguidores":
                getSeguidores();
                break;
            case "Seguindo":
                getSeguindo();
                break;
        }
    }

    // RETORNAR UMA LISTA DOS USUARIOS QUE DERAM LIKE NO POST
    private void getLikes() {

        // Buscar os ids dos usuarios passados no userAdapter
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes")
                .child(idUsuarios);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // RETORNAR UMA LISTA DOS SEGUIDORES NO FRAGMENTO DE PERFIL
    private void getSeguidores() {

        // Buscar os ids dos usuarios passados no userAdapter
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("follow")
                .child(idUsuarios).child("seguidores");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // RETORNAR UMA LISTA DE QUEM ESTA SEGUINDO NO FRAGMENTO DE PERFIL
    private void getSeguindo() {

        // Buscar os ids dos usuarios passados no userAdapter
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("follow")
                .child(idUsuarios).child("seguindo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                mostrarUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // LISTAR OS USUARIOS NA ACTIVITY
    private void mostrarUsuarios() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("usuarios");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    for (String idUsuario : idList) {

                        if (user != null && user.getIdUser().equals(idUsuario)) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}