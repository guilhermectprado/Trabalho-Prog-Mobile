package com.example.facomgram.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.facomgram.MainActivity;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class OpcoesActivity extends AppCompatActivity {

    TextView sair, configuracoesOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes);

        sair = findViewById(R.id.sair);
        configuracoesOP = findViewById(R.id.configuracoesOP);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Opções");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        sair.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(OpcoesActivity.this, MainActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });


    }
}