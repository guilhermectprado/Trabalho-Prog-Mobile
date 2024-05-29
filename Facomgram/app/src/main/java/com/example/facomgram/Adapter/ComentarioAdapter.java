package com.example.facomgram.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.facomgram.MainActivity;
import com.example.facomgram.Model.Comentario;
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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {
    private final Context context;
    private final List<Comentario> comentarios;
    private final String idPost;
    private  Post post;

    private FirebaseUser firebaseUser;

    public ComentarioAdapter(Context context, List<Comentario> comentarios, String idPost) {
        this.context = context;
        this.comentarios = comentarios;
        this.idPost = idPost;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_profileFC;
        public TextView txt_usernameFC, txt_comentarioFC;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profileFC = itemView.findViewById(R.id.image_profileFC);
            txt_usernameFC = itemView.findViewById(R.id.txt_usernameFC);
            txt_comentarioFC = itemView.findViewById(R.id.txt_comentarioFC);

        }
    }


    @NonNull
    @Override
    public ComentarioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.formato_comentario, parent, false);
        return new ComentarioAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Comentario comentario = comentarios.get(position);

        holder.txt_comentarioFC.setText(comentario.getComentario());
        publicadorInfo(holder.image_profileFC, holder.txt_usernameFC, comentario.getIdPublicador());


        // Redireciona para o perfil do usuário clicado do comentário
        holder.txt_comentarioFC.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("idPublicador", comentario.getIdPublicador());
            context.startActivity(intent);
        });

        // Redireciona para o perfil do usuário clicado na imagem de perfil
        holder.image_profileFC.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("idPublicador", comentario.getIdPublicador());
            context.startActivity(intent);
        });

        // Editar ou Deletar um comentário
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.editar:
                        editarComentario(comentario.getIdComentario());
                        postComentario();


                        return true;

                    case R.id.deletar:
                        FirebaseDatabase.getInstance().getReference("comentarios").child(idPost)
                                .child(comentario.getIdComentario()).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Comentario deletado", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Algo deu errado", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;

                    default:
                        return false;
                }
            });

            popupMenu.inflate(R.menu.opcoes_post);

            if (!comentario.getIdPublicador().equals(firebaseUser.getUid())) {
                popupMenu.getMenu().findItem(R.id.editar).setVisible(false);
                popupMenu.getMenu().findItem(R.id.deletar).setVisible(false);
            }
            popupMenu.show();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    private void publicadorInfo(ImageView foto_perfil, TextView username, String idPublicador) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("usuarios").child(idPublicador);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                Glide.with(context).load(user.getImageurl()).into(foto_perfil);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void editarComentario(String idComentario) {

        AlertDialog.Builder alert_editar = new AlertDialog.Builder(context);
        alert_editar.setTitle("Editar Comentario");
        EditText editText = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alert_editar.setView(editText);
        getTexto(idComentario, editText);
        // Alterar o cometário, e via AlertDialog ele irá atualizar o comentario no banco
        alert_editar.setPositiveButton("Salvar", (dialog, which) -> {
            HashMap<String, Object> hashmap = new HashMap<>();
            hashmap.put("comentario", editText.getText().toString());

            FirebaseDatabase.getInstance().getReference("comentarios").child(idPost)
                    .child(idComentario)
                    .updateChildren(hashmap);

            Toast.makeText(context, "Comentario alterado", Toast.LENGTH_SHORT).show();
        });


        alert_editar.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        alert_editar.show();
    }

    private void getTexto(String idComentario, final EditText editText) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comentarios")
                .child(idPost).child(idComentario);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(Comentario.class) != null) {
                    editText.setText(Objects.requireNonNull(snapshot.getValue(Comentario.class)).getComentario());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void postComentario() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapShot : snapshot.getChildren()) {
                    Post post1 = snapShot.getValue(Post.class);
                    if (Objects.requireNonNull(post1).getIdPost().equals(idPost)) {
                        post = post1;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}
