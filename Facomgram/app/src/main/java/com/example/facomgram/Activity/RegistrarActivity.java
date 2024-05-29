package com.example.facomgram.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.facomgram.MainActivity;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegistrarActivity extends AppCompatActivity {
    private EditText edt_username, edt_emailR, edt_senhaR, edt_confirmaSenha;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        edt_username = findViewById(R.id.edt_username);
        edt_emailR = findViewById(R.id.edt_emailR);
        edt_senhaR = findViewById(R.id.edt_senhaR);
        edt_confirmaSenha = findViewById(R.id.edt_confirmaSenha);
        Button btn_registrarR = findViewById(R.id.btn_registrarR);
        TextView btn_logar = findViewById(R.id.txt_logar);

        // RETORNAR A INSTÂNCIA DO BANCO DE DADOS
        auth = FirebaseAuth.getInstance();

        // CHAMAR A FUNÇÃO DE REGISTRAR
        btn_registrarR.setOnClickListener(v -> registrar());

        // IR PARA A TELA DE LOGIN
        btn_logar.setOnClickListener(v -> {
            Intent it = new Intent(RegistrarActivity.this, LoginActivity.class);
            startActivity(it);
        });
    }

    private void registrar() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Criando sua conta...");
        progressDialog.show();

        String email = edt_emailR.getText().toString();
        String username = edt_username.getText().toString();
        String senha = edt_senhaR.getText().toString();
        String confirmaSenha = edt_confirmaSenha.getText().toString();

        // VALIDAR CAMPOS VAZIOS, FALTA DE @ NO E-MAIL E SENHA MENOR DE 6 CARACTERES
        if (TextUtils.isEmpty(email)) {
            edt_emailR.setError("Por favor insira um e-mail.");
            edt_emailR.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (!email.contains("@")) {
            edt_emailR.setError("Esse não é um e-mail válido!");
            edt_emailR.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            edt_username.setError("Por favor insira um username.");
            edt_username.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            edt_senhaR.setError("Por favor insira uma senha.");
            edt_senhaR.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(confirmaSenha)) {
            edt_confirmaSenha.setError("Por favor insira a confirmação de senha.");
            edt_confirmaSenha.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (!senha.equals(confirmaSenha)) {
            edt_confirmaSenha.setError("Os caracteres não batem com a senha fornecida!");
            edt_confirmaSenha.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (senha.length() < 6) {
            edt_senhaR.setError("A senha deve ter pelo menos 6 caracteres");
            edt_senhaR.requestFocus();
            progressDialog.dismiss();
            return;
        }


        // USANDO MÉTODO DO FIREBASE AUTH, CRIANDO CONTA COM E-MAIL E SENHA
        // O ÚNICO ACESSO QUE TEMOS É SOBRE O E-MAIL DO USUÁRIO, A SENHA NÃO MOSTRA
        User user = new User();
        user.setUsername(username);
        user.setNome("");
        user.setBio("Engenharia de Software - ES");
        user.setImageurl("https://firebasestorage.googleapis.com/v0/b/facomgram.appspot.com/o/avatar.jpg?alt=media&token=de1ab59d-ecdb-4ec8-8e82-7730807dd005");

        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.setIdUser(auth.getUid());
                user.salvar();
                startActivity(new Intent(RegistrarActivity.this, MainActivity.class));
            } else {
                String error = Objects.requireNonNull(task.getException()).getMessage();
                Toast.makeText(RegistrarActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}