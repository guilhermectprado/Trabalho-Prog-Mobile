package com.example.facomgram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.facomgram.Fragment.DetalhesPostFragment;
import com.example.facomgram.Fragment.ProfileFragment;
import com.example.facomgram.Model.Notificacao;
import com.example.facomgram.Model.Post;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {
    private final Context context;
    private final List<Notificacao> notificacoes;

    public NotificacaoAdapter(Context context, List<Notificacao> notificacoes) {
        this.context = context;
        this.notificacoes = notificacoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacao,parent, false);

        return new NotificacaoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoAdapter.ViewHolder holder, int position) {
        Notificacao notificacao = notificacoes.get(position);

        holder.texto.setText(notificacao.getTexto());

        getInfoUser(holder.fotoPerfil, holder.userNameN, notificacao.getIdUser());
        if (notificacao.isIspost()){
            holder.imagemPost.setVisibility(View.VISIBLE);
            getImagemPost(holder.imagemPost,notificacao.getIdPost());
        } else{
            holder.imagemPost.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            SharedPreferences.Editor editor =  context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
            if (notificacao.isIspost()){
                editor.putString("idPost",notificacao.getIdPost());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DetalhesPostFragment()).commit();

            } else {
                editor.putString("idPerfil",notificacao.getIdUser());
                editor.apply();

                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });
    }

    @Override
    public int getItemCount() {
        return notificacoes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView fotoPerfil,imagemPost;
        public TextView userNameN,texto;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPost = itemView.findViewById(R.id.imagem_post_N);
            fotoPerfil = itemView.findViewById(R.id.foto_perfil_N);
            userNameN = itemView.findViewById(R.id.txt_usernameN);
            texto = itemView.findViewById(R.id.txt_comentarioN);


        }
    }

    private void getInfoUser(ImageView imageView, TextView username, String idPublicador){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(idPublicador);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User usuario = snapshot.getValue(User.class);
                if (usuario != null){
                    Glide.with(context).load(usuario.getImageurl()).into(imageView);
                    username.setText(usuario.getUsername());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getImagemPost(ImageView imageView, String idPost){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts").child(idPost);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null){
                    Glide.with(context).load(post.getImagePost()).into(imageView);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
