package com.example.facomgram.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.facomgram.MainActivity;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText edt_emailL, edt_senhaL;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_emailL = findViewById(R.id.edt_emailL);
        edt_senhaL = findViewById(R.id.edt_senhaL);
        Button btn_logarL = findViewById(R.id.btn_logarL);
        TextView txt_registrar = findViewById(R.id.txt_registrar);

        // RETORNAR A INSTÂNCIA DO BANCO DE DADOS
        auth = FirebaseAuth.getInstance();

        // CHAMAR A FUNÇÃO DE LOGAR
        btn_logarL.setOnClickListener(v -> logar());

        // IR PARA TELA DE REGISTRAR
        txt_registrar.setOnClickListener(v -> {
            Intent it = new Intent(LoginActivity.this, RegistrarActivity.class);
            startActivity(it);
        });
    }

    private void logar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logando, aguarde por favor...");
        progressDialog.show();

        String email = edt_emailL.getText().toString();
        String senha = edt_senhaL.getText().toString();


        // VALIDAR CAMPOS VAZIO E AUSÊNCIA DE @ NO CAMPO E-MAIL
        if (TextUtils.isEmpty(email)) {
            edt_emailL.setError("Por favor insira um e-mail.");
            edt_emailL.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (!email.contains("@")) {
            edt_emailL.setError("Esse não é um e-mail válido!");
            edt_emailL.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            edt_senhaL.setError("Por favor insira uma senha.");
            edt_senhaL.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (senha.length() < 6) {
            edt_senhaL.setError("A senha deve ter pelo menos 6 caracteres");
            edt_senhaL.requestFocus();
            progressDialog.dismiss();
            return;
        }

        // REALIZAR LOGIN VIA MÉTODO FIREBASEAUTH
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // REFERENCIAR A TABELA "usuarios" DO BANCO DE DADOS RETORNANDO OS DADOS DO USUÁRIO
                        // DE ACORDO COM O ID DO USUÁRIO QUE É INTERLIGADO NO MÉTODO FIREBASEAUTH
                        reference = FirebaseDatabase.getInstance().getReference().child("usuarios")
                                .child(Objects.requireNonNull(auth.getCurrentUser()).getUid());

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Falha na autenticação!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}