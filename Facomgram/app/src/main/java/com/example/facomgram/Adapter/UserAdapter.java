package com.example.facomgram.Adapter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.facomgram.Fragment.ProfileFragment;
import com.example.facomgram.MainActivity;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context context;
    private final List<User> users;
    private FirebaseUser firebaseUser;
    private final boolean fragMain;

    public UserAdapter(Context context, List<User> users, boolean fragMain ) {
        this.context = context;
        this.users = users;
        this.fragMain = fragMain;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txt_username ,txt_nome;
        public CircleImageView image_profile;
        public Button btn_follow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_username = itemView.findViewById(R.id.txt_username);
            txt_nome = itemView.findViewById(R.id.txt_nome);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // RETORNAR O USUÁRIO ATUAL
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // RETORNAR A POSIÇÃO DO USUÁRIO NO LIST
        User user = users.get(position);
        // SETAR OS DADOS DO USUÁRIO
        holder.btn_follow.setVisibility(View.VISIBLE);
        holder.txt_username.setText(user.getUsername());
        holder.txt_nome.setText(user.getNome());

        // GLIDE FOI UMA IMPLEMENTAÇÃO PARA CARREGAR A IMAGEM DO USUÁRIO
        Glide.with(context).load(user.getImageurl()).into(holder.image_profile);

        // VERIFICAR SE O USUÁRIO JÁ ESTÁ SENDO SEGUIDO
        isFollowing(user.getIdUser(), holder.btn_follow);

        // NÃO APARECER O BOTÃO DE SEGUIR NO NOME DO PRÓPRIO USUÁRIO
        if (user.getIdUser().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        }

        // VAI PARA O PERFIL DO USUÁRIO
        holder.itemView.setOnClickListener(v -> {
            if (fragMain) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("idPerfil", user.getIdUser());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
            else{
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("idPublicador", user.getIdUser());
                context.startActivity(intent);
            }

            });


        // VAI GERAR NO BANCO DE DADOS UMA TABELA "follow" QUE CONTÉM OS USUÁRIOS E DE CADA UM
        // GERA TABELAS AUXILIARES DE RELACIONAMENTO '1 PRA N', ONDE
        // UMA CONTÉM O ID DOS SEGUIDORES E OUTRA CONTÉM O ID DE QUEM O SEGUE
        holder.btn_follow.setOnClickListener(v -> {
            if (holder.btn_follow.getText().toString().equals("SEGUIR")) {
                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("seguindo").child(user.getIdUser()).setValue(true);

                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getIdUser())
                        .child("seguidores").child(firebaseUser.getUid()).setValue(true);
                addNotificacoes(user.getIdUser());
            } else {
                FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                        .child("seguindo").child(user.getIdUser()).removeValue();

                FirebaseDatabase.getInstance().getReference().child("follow").child(user.getIdUser())
                        .child("seguidores").child(firebaseUser.getUid()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void addNotificacoes(String idUser){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notificacoes").child(idUser);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("idUser",firebaseUser.getUid());
        hashMap.put("texto"," começou a te seguir");
        hashMap.put("idPost","");
        hashMap.put("ispost",false);

        ref.push().setValue(hashMap);

    }

    // VALIDADOR PARA VERIFICAR SE O USUÁRIO JÁ ESTÁ SEGUINDO O OUTRO USUÁRIO E ENTÃO DETERMINAR O TEXT DO BOTÃO
    private void isFollowing(String userid, Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("follow")
                .child(firebaseUser.getUid()).child("seguindo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(userid).exists()) {
                    button.setText("SEGUINDO");
                } else {
                    button.setText("SEGUIR");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
