package com.example.facomgram.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facomgram.Adapter.NotificacaoAdapter;
import com.example.facomgram.Model.Notificacao;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificacaoAdapter notificacaoAdapter;
    private List<Notificacao> notificacoes;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_notif);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        notificacoes = new ArrayList<>();
        notificacaoAdapter = new NotificacaoAdapter(getContext(),notificacoes);
        recyclerView.setAdapter(notificacaoAdapter);
        leNotificacoes();
        return view;
    }

    private void leNotificacoes(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notificacoes").child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notificacoes.clear();
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        Notificacao notificacao = snapshot1.getValue(Notificacao.class);
                        notificacoes.add(notificacao);
                    }
                    Collections.reverse(notificacoes);
                    notificacaoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}