package com.example.facomgram.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.facomgram.Fragment.DetalhesPostFragment;
import com.example.facomgram.Model.Post;
import com.example.facomgram.R;

import java.util.List;

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.ViewHolder> {
    private final Context context;
    private final List<Post> posts;

    public FotoAdapter(Context context, List<Post> lista){
        this.context = context;
        this.posts = lista;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fotos,parent,false);
        return new FotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post postDesejado = posts.get(position);

        Glide.with(context).load(postDesejado.getImagePost()).into(holder.imagemPost);


        holder.imagemPost.setOnClickListener(view -> {
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();

            editor.putString("idPost",postDesejado.getIdPost());
            editor.apply();
            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DetalhesPostFragment()).commit();
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imagemPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemPost = itemView.findViewById(R.id.imagem_post_F);
        }
    }
}