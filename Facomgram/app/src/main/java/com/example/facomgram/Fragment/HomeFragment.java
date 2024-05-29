package com.example.facomgram.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.facomgram.Adapter.PostAdapter;
import com.example.facomgram.Model.Post;
import com.example.facomgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private PostAdapter postAdapter;
    private List<Post> postsList;
    private List<String> followingList;
    private DatabaseReference reference;
    private FirebaseAuth auth;

    ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recycler_viewH = view.findViewById(R.id.recycler_viewH);
        recycler_viewH.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_viewH.setLayoutManager(linearLayoutManager);

        postsList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postsList);
        recycler_viewH.setAdapter(postAdapter);

        progressBar = view.findViewById(R.id.progress_circular);

        checkFollowing();

        return view;
    }

    private void checkFollowing() {
        auth = FirebaseAuth.getInstance();

        if (null != auth.getCurrentUser()) {
            followingList = new ArrayList<>();

            reference = FirebaseDatabase.getInstance().getReference("follow")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child("seguindo");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    followingList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followingList.add(snapshot.getKey());
                    }

                    readPosts();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void readPosts() {
        reference = FirebaseDatabase.getInstance().getReference("posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : followingList) {
                        if (Objects.requireNonNull(post).getIdPublicador().equals(id)) {
                            postsList.add(post);
                        }
                    }
                }

                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}