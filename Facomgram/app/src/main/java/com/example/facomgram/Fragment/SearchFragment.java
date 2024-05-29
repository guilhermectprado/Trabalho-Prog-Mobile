package com.example.facomgram.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.example.facomgram.Adapter.UserAdapter;
import com.example.facomgram.Model.User;
import com.example.facomgram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private UserAdapter userAdapter;
    private List<User> users;
    private EditText search;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search = view.findViewById(R.id.search);
        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users,true);
        recyclerView.setAdapter(userAdapter);

        // SE A BARRA DE PESQUISA USUARIO ESTIVER VAZIA ELE VAI LER TODOS OS USUÁRIOS DO BANCO
        listarUsuarios();

        // O NOME DO USUARIO SERÁ BUSCADO INDEPENDENTE DA LETRA MINUSCULA OU MAIUSCULA
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarUsuario(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    // BUSCAR O NOME DO USUÁRIO NA TABELA "usuarios" ORDENADO PELO "username"
    public void buscarUsuario(String s) {

        // CONSULTA PARA BUSCAR NO FIREBASE O NOME DO USUÁRIO PASSADO PELA STRING 'S'
        Query query = FirebaseDatabase.getInstance().getReference("usuarios").orderByChild("username")
                .startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear(); //LIMPAR O LIST
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user); //ADICIONAR CADA USUÁRIO NO LIST
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // LISTAR OS USUÁRIOS DO BANCO QUANDO NÃO HÁ NENHUM TEXTO INSERIDO
    public void listarUsuarios() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("usuarios");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search.getText().toString().equals("")) {
                    users.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        users.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}