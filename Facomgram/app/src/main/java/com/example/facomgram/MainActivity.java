package com.example.facomgram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.facomgram.Activity.LoginActivity;
import com.example.facomgram.Activity.PostActivity;
import com.example.facomgram.Fragment.ProfileFragment;
import com.example.facomgram.Fragment.HomeFragment;
import com.example.facomgram.Fragment.NotificationFragment;
import com.example.facomgram.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment fragmentSelect = null;
    private FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // REDIRECIONAR O USUÁRIO PARA TELA DE LOGIN SE ELE FOR NULO
        if (firebaseUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);


        // REDIRECIONARO PARA A PAGINA DO USUÁRIO CLICADO NO COMENTARIO SE OCORRER, SE NÃO, HOMEFRAGMENT COMO PADRÃO
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String perfil = intent.getString("idPublicador");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("idPerfil", perfil);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        }

    }

    // MENU DE NAVEGAÇÃO LOCALIZADO NA PARTE INFERIOR DA INTERFACE
    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnItemSelectedListener onItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        fragmentSelect = new HomeFragment();
                        break;

                    case R.id.nav_search:
                        fragmentSelect = new SearchFragment();
                        break;

                    case R.id.nav_add:
                        fragmentSelect = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;

                    case R.id.nav_heart:
                        fragmentSelect = new NotificationFragment();
                        break;

                    // VAI RETORNAR OS DADOS DO USUÁRIO DE ACORDO COM O ID DO USUÁRIO
                    case R.id.nav_profile:
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("idPerfil", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        editor.apply();
                        fragmentSelect = new ProfileFragment();
                        break;
                }

                if (fragmentSelect != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragmentSelect).commit();
                }

                return true;
            };
}