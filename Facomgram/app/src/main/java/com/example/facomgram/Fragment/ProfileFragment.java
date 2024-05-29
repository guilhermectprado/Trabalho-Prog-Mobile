package com.example.facomgram.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.facomgram.Activity.EditarPerfilActivity;
import com.example.facomgram.Activity.OpcoesActivity;
import com.example.facomgram.Activity.SeguidoresActivity;
import com.example.facomgram.Adapter.FotoAdapter;
import com.example.facomgram.Model.Post;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private ImageView image_profileFP;
    private TextView txt_usernameFP, txt_postsPF, txt_seguidoresPF, txt_seguindoPF, txt_nomeFP, txt_bio;
    private Button btn_editarPerfil;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String idPerfil;
    private List<String> salvos;
    private RecyclerView recyclerViewSalvos;
    private FotoAdapter fotoAdapterSalvos;
    private List<Post> listaPostsSalvos;

    private RecyclerView recyclerView;
    private FotoAdapter fotoAdapter;
    private List<Post> listaPosts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idPerfil = sharedPreferences.getString("idPerfil", "none");

        txt_usernameFP = view.findViewById(R.id.txt_usernameFP);
        image_profileFP = view.findViewById(R.id.image_profileFP);
        ImageView configuracoesPF = view.findViewById(R.id.configuracoesPF);
        txt_postsPF = view.findViewById(R.id.txt_postsPF);
        txt_seguidoresPF = view.findViewById(R.id.txt_seguidoresPF);
        txt_seguindoPF = view.findViewById(R.id.txt_seguindoPF);
        txt_nomeFP = view.findViewById(R.id.txt_nomeFP);
        txt_bio = view.findViewById(R.id.txt_bio);
        btn_editarPerfil = view.findViewById(R.id.btn_editarPerfil);
        ImageButton fotosFP = view.findViewById(R.id.fotosFP);
        ImageButton fotosSalvasFP = view.findViewById(R.id.fotosSalvasFP);
        LinearLayout layout_seguidoresFP = view.findViewById(R.id.layout_seguidoresFP);
        LinearLayout layout_seguindoFP = view.findViewById(R.id.layout_seguindoFP);


        recyclerView = view.findViewById(R.id.recycler_view_fotosPF);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        listaPosts = new ArrayList<>();
        fotoAdapter = new FotoAdapter(getContext(), listaPosts);
        recyclerView.setAdapter(fotoAdapter);

        recyclerViewSalvos = view.findViewById(R.id.recycler_view_fotosSalvasPF);
        recyclerViewSalvos.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerSalvos = new GridLayoutManager(getContext(), 3);
        recyclerViewSalvos.setLayoutManager(linearLayoutManagerSalvos);
        listaPostsSalvos = new ArrayList<>();
        fotoAdapterSalvos = new FotoAdapter(getContext(), listaPostsSalvos);
        recyclerViewSalvos.setAdapter(fotoAdapterSalvos);


        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewSalvos.setVisibility(View.GONE);

        fotosFP.setOnClickListener(view1 -> {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerViewSalvos.setVisibility(View.GONE);
        });

        fotosSalvasFP.setOnClickListener(view12 -> {
            recyclerViewSalvos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        });

        configuracoesPF.setOnClickListener(view13 -> {
            Intent intent = new Intent(getContext(), OpcoesActivity.class);
            startActivity(intent);
        });

        btn_editarPerfil.setOnClickListener(v -> btn_editarPerfil());

        // Ir para Activity Seguidores
        layout_seguidoresFP.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario", idPerfil);
            intent.putExtra("titulo", "Seguidores");
            startActivity(intent);
        });

        // Ir para Activity Seguidores
        layout_seguindoFP.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeguidoresActivity.class);
            intent.putExtra("idUsuario", idPerfil);
            intent.putExtra("titulo", "Seguindo");
            startActivity(intent);
        });

        infoUser();
        getSegue_getSeguidores();
        getNumeroPosts();
        minhasFotos();
        meusSalvos();


        if (idPerfil.equals(firebaseUser.getUid())) {
            btn_editarPerfil.setText("Editar perfil");

        } else {
            checkSegue();
            fotosSalvasFP.setVisibility(View.GONE);
        }

        return view;
    }

    private void addNotificacoes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notificacoes").child(idPerfil);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("idUser", firebaseUser.getUid());
        hashMap.put("texto", "começou a te seguir");
        hashMap.put("idPost", "");
        hashMap.put("ispost", false);

        ref.push().setValue(hashMap);

    }

    private void btn_editarPerfil() {
        String btn = btn_editarPerfil.getText().toString();

        switch (btn) {
            case "Editar perfil":
                startActivity(new Intent(getContext(), EditarPerfilActivity.class));

                break;
            case "SEGUIR":

                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("seguindo").child(idPerfil).setValue(true);

                FirebaseDatabase.getInstance().getReference().child("follow").child(idPerfil)
                        .child("seguidores").child(firebaseUser.getUid()).setValue(true);
                addNotificacoes();
                break;
            case "SEGUINDO":

                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("seguindo").child(idPerfil).removeValue();

                FirebaseDatabase.getInstance().getReference().child("follow").child(idPerfil)
                        .child("seguidores").child(firebaseUser.getUid()).removeValue();
                break;
        }
    }


    private void infoUser() {
        reference = FirebaseDatabase.getInstance().getReference("usuarios").child(idPerfil);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = snapshot.getValue(User.class);

                Glide.with(getContext()).load(Objects.requireNonNull(user).getImageurl()).into(image_profileFP);
                txt_usernameFP.setText(user.getUsername());
                txt_nomeFP.setText(user.getNome());
                txt_bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // AO ABRIR O FRAGMENTO PERFIL, SE NÃƒO FOR O PERFIL DO USUARIO ATIVO, E SIM DE UM OUTRO USUARIO
    // SERA CHECADO SE O USUARIO EM QUESTÃƒO ESTA SENDO SEGUIDO OU NAO PELO USUARIO ATIVO.
    private void checkSegue() {
        reference = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(firebaseUser.getUid()).child("seguindo");

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(idPerfil).exists()) {
                    btn_editarPerfil.setText("SEGUINDO");
                } else {
                    btn_editarPerfil.setText("SEGUIR");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // RETORNAR O NÚMERO DOS SEGUIDORES E QUEM SEGUE
    private void getSegue_getSeguidores() {
        // RETORNAR OS SEGUIDORES
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(idPerfil).child("seguidores");

        reference1.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_seguidoresPF.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // RETORNAR QUEM SEGUE
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(idPerfil).child("seguindo");

        reference2.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_seguindoPF.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // RETORNA O NÚMERO DE POSTS
    private void getNumeroPosts() {
        reference = FirebaseDatabase.getInstance().getReference("posts");

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (Objects.requireNonNull(post).getIdPublicador().equals(idPerfil)) {
                        i++;
                    }
                }

                txt_postsPF.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void minhasFotos() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPosts.clear();
                for (DataSnapshot snapShot : snapshot.getChildren()) {
                    Post post = snapShot.getValue(Post.class);
                    if (Objects.requireNonNull(post).getIdPublicador().equals(idPerfil)) {
                        listaPosts.add(post);
                    }
                }
                Collections.reverse(listaPosts);
                fotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void meusSalvos() {
        salvos = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("saves")
                .child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    salvos.add(snapshot1.getKey());

                }
                leSalvos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void leSalvos() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPostsSalvos.clear();
                for (DataSnapshot snapShot : snapshot.getChildren()) {
                    Post post = snapShot.getValue(Post.class);
                    for (String id : salvos) {
                        if (Objects.requireNonNull(post).getIdPost().equals(id)) {
                            listaPostsSalvos.add(post);
                        }
                    }
                }

                fotoAdapterSalvos.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}