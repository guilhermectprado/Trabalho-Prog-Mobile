package com.example.facomgram.Model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Post {
    private String idPost;
    private String imagePost;
    private String descricao;
    private String idPublicador;

    public Post() {

    }

    public Post(String idPost, String imagePost, String descricao, String idPublicador) {
        this.idPost = idPost;
        this.imagePost = imagePost;
        this.descricao = descricao;
        this.idPublicador = idPublicador;
    }

    public String getIdPost() {
        return idPost;
    }

    public void setIdPost(String idPost) {
        this.idPost = idPost;
    }

    public String getImagePost() {
        return imagePost;
    }

    public void setImagePost(String imagePost) {
        this.imagePost = imagePost;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIdPublicador() {
        return idPublicador;
    }

    public void setIdPublicador(String idPublicador) {
        this.idPublicador = idPublicador;
    }

    public void salvar() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("posts").child(getIdPost()).setValue(this);
    }
}
