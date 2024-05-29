package com.example.facomgram.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facomgram.Adapter.PostAdapter;
import com.example.facomgram.Model.Post;
import com.example.facomgram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetalhesPostFragment extends Fragment {

    String idPost;
    private PostAdapter postAdapter;
    private List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalhes_post,container,false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idPost = sharedPreferences.getString("idPost","none");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_detalhes);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),posts);
        recyclerView.setAdapter(postAdapter);
        
        lePost();
        // Inflate the layout for this fragment
        return view;
    }

    private void lePost() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts").child(idPost);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                Post post = snapshot.getValue(Post.class);
                posts.add(post);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}