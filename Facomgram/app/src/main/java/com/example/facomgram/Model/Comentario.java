package com.example.facomgram.Model;

public class Comentario {
    private String idComentario;
    private String comentario;
    private String idPublicador;

    public Comentario() {
    }

    public Comentario(String idComentario, String comentario, String idPublicador) {
        this.idComentario = idComentario;
        this.comentario = comentario;
        this.idPublicador = idPublicador;
    }

    public String getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getIdPublicador() {
        return idPublicador;
    }

    public void setIdPublicador(String idUser) {
        this.idPublicador = idUser;
    }
}
