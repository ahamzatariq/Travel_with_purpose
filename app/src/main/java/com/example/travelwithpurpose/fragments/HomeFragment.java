package com.example.travelwithpurpose.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.travelwithpurpose.R;
import com.example.travelwithpurpose.adapters.BlogRecyclerAdapter;
import com.example.travelwithpurpose.models.BlogPost;
import com.example.travelwithpurpose.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import io.opencensus.internal.StringUtils;

public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private List<User> user_list;

    SearchView searchView;

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    BlogRecyclerAdapter blogRecyclerAdapter;
    DocumentSnapshot lastVisible;

    Boolean isFirstPageFIrstLoad = true;

    List<BlogPost> searchResults;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list_view = view.findViewById(R.id.blog_list);
        searchView = view.findViewById(R.id.searchView);

        mAuth = FirebaseAuth.getInstance();

        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();

        Boolean isGuest = getArguments().getBoolean("guest");

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if(mAuth.getCurrentUser() != null){

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean isBottom = !recyclerView.canScrollVertically(1);

                    if(isBottom){
                        Toast.makeText(getContext(), "Bottom reached", Toast.LENGTH_SHORT).show();
                        loadMorePosts();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                    @Nullable FirebaseFirestoreException e) {

                    if(queryDocumentSnapshots != null){
                        if(!queryDocumentSnapshots.isEmpty()){

                            if (isFirstPageFIrstLoad) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                                user_list.clear();
                            }

                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType() == DocumentChange.Type.ADDED){

                                    String blogPostID = doc.getDocument().getId();

                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class)
                                            .withId(blogPostID);

                                    String blogUserId = doc.getDocument().getString("user_id");

                                    firebaseFirestore.collection("Users").document(blogUserId).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        User user = task.getResult().toObject(User.class);

                                                        if (isFirstPageFIrstLoad) {
                                                            user_list.add(user);
                                                            blog_list.add(blogPost);
                                                        } else {
                                                            user_list.add(0, user);
                                                            blog_list.add(0, blogPost);
                                                        }

                                                        blogRecyclerAdapter.notifyDataSetChanged();

                                                    } else {
                                                        Toast.makeText(getContext(), "Error getting User ID", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }

                            isFirstPageFIrstLoad = false;
                        }
                    }
                }
            });
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Toast.makeText(getContext(), "Search: " + s, Toast.LENGTH_SHORT).show();
                search(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                Toast.makeText(getContext(), "Search: " + s, Toast.LENGTH_SHORT).show();
                search(s);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setAdapter(blog_list);
                return false;
            }
        });

        return view;
    }

    public void search(String query){
        if(query.length() > 0){
            //query = query.substring(0, 1).toUpperCase() + query.substring(1).toLowerCase();
            //Toast.makeText(getContext(), "Inside search: " + query, Toast.LENGTH_SHORT).show();

            searchResults = new ArrayList<>();
            for(BlogPost blogPost : blog_list){
                if(blogPost.getDesc() != null && blogPost.getDesc().contains(query)){
                    searchResults.add(blogPost);
                }
            }

            setAdapter(searchResults);

        } else if(query.trim().length() == 0) {

            //Toast.makeText(getContext(), "empty query", Toast.LENGTH_SHORT).show();

            setAdapter(blog_list);

        }
    }

    public void setAdapter(List<BlogPost> blogList){
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogList, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blogRecyclerAdapter.notifyDataSetChanged();
    }

    public void loadMorePosts(){

        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {

                if(queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){

                        lastVisible =   queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                            if(doc.getType() == DocumentChange.Type.ADDED){

                                String blogPostID = doc.getDocument().getId();

                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class)
                                        .withId(blogPostID);

                                String blogUserId = doc.getDocument().getString("user_id");

                                firebaseFirestore.collection("Users").document(blogUserId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    User user = task.getResult().toObject(User.class);

                                                    user_list.add(user);

                                                    blog_list.add(blogPost);

                                                    blogRecyclerAdapter.notifyDataSetChanged();

                                                } else {
                                                    Toast.makeText(getContext(), "Error getting User ID", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                }
            }
        });
    }

}
