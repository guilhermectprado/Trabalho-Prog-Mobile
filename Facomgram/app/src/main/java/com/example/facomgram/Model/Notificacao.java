package com.example.facomgram.Model;

public class Notificacao {
    private String idUser;
    private String texto;
    private String idPost;
    private boolean ispost;

    public Notificacao(String idUser, String texto, String idPost, boolean postado) {
        this.setIdUser(idUser);
        this.setTexto(texto);
        this.setIdPost(idPost);
        this.setIspost(postado);
    }

    public Notificacao(){

    }


    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String text) {
        this.texto = text;
    }

    public String getIdPost() {
        return idPost;
    }

    public void setIdPost(String idPost) {
        this.idPost = idPost;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
