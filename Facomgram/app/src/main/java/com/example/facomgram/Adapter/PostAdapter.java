package com.example.facomgram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.facomgram.Activity.ComentariosActivity;
import com.example.facomgram.Activity.SeguidoresActivity;
import com.example.facomgram.Fragment.DetalhesPostFragment;
import com.example.facomgram.Fragment.ProfileFragment;
import com.example.facomgram.Model.Post;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> posts;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView foto_perfil, image_post, ic_like, ic_comment, ic_save, ic_opcoes_post;
        public TextView txt_usernameH, txt_publicador, txt_descricaoH, txt_likes, txt_comentarios;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            foto_perfil = itemView.findViewById(R.id.foto_perfil);
            image_post = itemView.findViewById(R.id.image_post);
            ic_opcoes_post = itemView.findViewById(R.id.ic_opcoes_post);
            ic_like = itemView.findViewById(R.id.ic_like);
            ic_comment = itemView.findViewById(R.id.ic_comment);
            ic_save = itemView.findViewById(R.id.ic_save);
            txt_usernameH = itemView.findViewById(R.id.txt_usernameH);
            txt_publicador = itemView.findViewById(R.id.txt_publicador);
            txt_descricaoH = itemView.findViewById(R.id.txt_descricaoH);
            txt_likes = itemView.findViewById(R.id.txt_likes);
            txt_comentarios = itemView.findViewById(R.id.txt_comentarios);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.formato_post, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = posts.get(position);
        if(post != null ) {
            if ( post.getImagePost() != null) {
                Glide.with(context).load(post.getImagePost())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_placeholder))
                        .into(holder.image_post);

                // Carrega a imagem do post
                Glide.with(context).load(post.getImagePost()).into(holder.image_post);

            }
            if ( post.getDescricao() != null) {
                if (post.getDescricao().equals("")) {
                    holder.txt_descricaoH.setVisibility(View.GONE);

                } else {
                    holder.txt_descricaoH.setVisibility(View.VISIBLE);
                    holder.txt_descricaoH.setText(post.getDescricao());
                }
            }
            if ( post.getIdPublicador() != null) {
                publicadorInfo(holder.foto_perfil, holder.txt_usernameH, holder.txt_publicador, post.getIdPublicador());
            }
            if ( post.getIdPost() != null){
            likePost(post.getIdPost(), holder.ic_like);
            qtdLikes(post.getIdPost(), holder.txt_likes);
            qtdComentarios(post.getIdPost(), holder.txt_comentarios);
            salvaPost(post.getIdPost(), holder.ic_save);}
        }
        // Vai para o perfil do publicador ao clicar na foto do publicador do post
        holder.foto_perfil.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

            editor.putString("idPerfil", Objects.requireNonNull(post).getIdPublicador());
            editor.apply();


        });

        // Vai para o perfil do publicador do post ao clicar no username
        holder.txt_usernameH.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

            editor.putString("idPerfil", Objects.requireNonNull(post).getIdPublicador());
            editor.apply();
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });

        // Vai para o perfil do publicador do post ao clicar no username
        holder.txt_publicador.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            assert post != null;
            editor.putString("idPerfil", post.getIdPublicador());
            editor.apply();
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        });

        // Vai para o post em um fragmento só do post
        holder.image_post.setOnClickListener(view -> {
            SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("idPost", Objects.requireNonNull(post).getIdPost());
            editor.apply();
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DetalhesPostFragment()).commit();
        });

        // Salvar um post determinado para o usuário (é possível ver no Fragment Profile)
        holder.ic_save.setOnClickListener(v -> {
            if (holder.ic_save.getTag().equals("save")) {
                FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                        .child(Objects.requireNonNull(post).getIdPost())
                        .setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                        .child(Objects.requireNonNull(post).getIdPost())
                        .removeValue();
            }
        });

        // Retornar os usuários que deram like no post
        holder.txt_likes.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeguidoresActivity.class);
            intent.putExtra("idUsuario", Objects.requireNonNull(post).getIdPost());
            intent.putExtra("titulo", "Likes");
            context.startActivity(intent);

        });

        // Inserir o usuario que deu like no post determinado
        holder.ic_like.setOnClickListener(v -> {
            if (holder.ic_like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("likes").child(Objects.requireNonNull(post).getIdPost())
                        .child(firebaseUser.getUid())
                        .setValue(true);
                addNotificacoes(post.getIdPublicador(), post.getIdPost());
            } else {
                FirebaseDatabase.getInstance().getReference().child("likes").child(Objects.requireNonNull(post).getIdPost())
                        .child(firebaseUser.getUid())
                        .removeValue();
            }
        });

        // Utilizado pela Activity de Comentários
        holder.ic_comment.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComentariosActivity.class);
            intent.putExtra("idPost", Objects.requireNonNull(post).getIdPost());
            intent.putExtra("idPublicador", post.getIdPublicador());
            context.startActivity(intent);
        });

        // Utilizado pela Activity de Comentários
        holder.txt_comentarios.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComentariosActivity.class);
            intent.putExtra("idPost", Objects.requireNonNull(post).getIdPost());
            intent.putExtra("idPublicador", post.getIdPublicador());
            context.startActivity(intent);
        });
        if (post != null){
        if ( post.getIdPublicador() != null) {
            if (!post.getIdPublicador().equals(firebaseUser.getUid())) {
                holder.ic_opcoes_post.setVisibility(View.GONE);
            }
        }}
        // Menu de editar a descrição de um post ou deletar ele
        holder.ic_opcoes_post.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.editar:
                        editarPost(Objects.requireNonNull(post).getIdPost());
                        return true;

                    case R.id.deletar:
                        FirebaseDatabase.getInstance().getReference("posts")
                                .child(Objects.requireNonNull(post).getIdPost()).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        deleteNotificacoes(post.getIdPost(), firebaseUser.getUid());
                                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new ProfileFragment()).commit();
                                        Toast.makeText(context, "Post deletado", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;

                    default:
                        return false;
                }
            });

            popupMenu.inflate(R.menu.opcoes_post);

            if (!Objects.requireNonNull(post).getIdPublicador().equals(firebaseUser.getUid())) {
                popupMenu.getMenu().findItem(R.id.editar).setVisible(false);
                popupMenu.getMenu().findItem(R.id.deletar).setVisible(false);
            }
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }


    // Função para retornar a foto, o username e o nome do publicador do post
    private void publicadorInfo(ImageView foto_perfil, TextView username, TextView publicador, String idUser) {
        reference = FirebaseDatabase.getInstance().getReference("usuarios").child(idUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(context).load(Objects.requireNonNull(user).getImageurl()).into(foto_perfil);
                username.setText(user.getUsername());
                publicador.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Função de like no post
    private void likePost(String idPost, final ImageView imageView) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference().child("likes").child(idPost);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Função para inserir notificações
    private void addNotificacoes(String idUser, String idPost) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notificacoes").child(idUser);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("idUser", firebaseUser.getUid());
        hashMap.put("texto", "curtiu seu post");
        hashMap.put("idPost", idPost);
        hashMap.put("ispost", true);

        ref.push().setValue(hashMap);

    }

    // Função para ver a quantidade de likes do post
    private void qtdLikes(String idPost, TextView txt_likes) {
        reference = FirebaseDatabase.getInstance().getReference().child("likes").child(idPost);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_likes.setText(snapshot.getChildrenCount() + " Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Função para ver a quantidade de comentários do post
    private void qtdComentarios(String idPost, TextView txt_comentarios) {
        reference = FirebaseDatabase.getInstance().getReference().child("comentarios").child(idPost);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_comentarios.setText("Ver todos os " + snapshot.getChildrenCount() + " comentarios");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Função de salvar o post que viu e ele aparecer no perfil do usuário que o salvou
    private void salvaPost(String idPost, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("saves")
                .child(Objects.requireNonNull(firebaseUser).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(idPost).exists()) {
                    imageView.setImageResource(R.drawable.ic_save_completo);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void editarPost(String idPost) {

        AlertDialog.Builder alert_editar = new AlertDialog.Builder(context);
        alert_editar.setTitle("Editar Descrição");
        EditText editText = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alert_editar.setView(editText);
        getTexto(idPost, editText);
        // Só será possível alterar a descrição do post, e via AlertDialog ele irá atualizar a descrição no banco
        alert_editar.setPositiveButton("Salvar", (dialog, which) -> {
            HashMap<String, Object> hashmap = new HashMap<>();
            hashmap.put("descricao", editText.getText().toString());

            FirebaseDatabase.getInstance().getReference("posts").child(idPost).updateChildren(hashmap);

        });

        alert_editar.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        alert_editar.show();
    }

    private void getTexto(String idPost, final EditText editText) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(idPost);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Post.class) != null){
                editText.setText(Objects.requireNonNull(snapshot.getValue(Post.class)).getDescricao());}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteNotificacoes(final String idPost, String idUser){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notificacoes").child(idUser);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("idPost").getValue().equals(idPost)){
                        snapshot.getRef().removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Deletada a notificação!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
