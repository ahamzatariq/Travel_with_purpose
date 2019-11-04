package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.travelwithpurpose.adapters.CommentsRecyclerAdapter;
import com.example.travelwithpurpose.models.BlogPost;
import com.example.travelwithpurpose.models.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    List<Comments> commentList;
    Toolbar toolbar;
    ImageView sendBtn;
    EditText commentField;

    String blogPostID, currentUserID;
    Boolean isGuest;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    RecyclerView commentsRecycler;
    CommentsRecyclerAdapter commentsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");

        isGuest = getIntent().getBooleanExtra("guest", false);

        sendBtn = findViewById(R.id.sendBtn);
        commentField = findViewById(R.id.commentText);

        currentUserID = mAuth.getCurrentUser().getUid();

        blogPostID = getIntent().getStringExtra("blogID");

        commentsRecycler = findViewById(R.id.recyclerView);

        //RecyclerView
        commentList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentList);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentsRecycler.setAdapter(commentsRecyclerAdapter);

        firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType() == DocumentChange.Type.ADDED){

                                    String commentsID = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class)
                                            .withId(commentsID);
                                    commentList.add(comments);
                                    commentsRecyclerAdapter.notifyDataSetChanged();

                                }
                            }
                        }
                    }
                });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String commentMsg = commentField.getText().toString();

                if(!commentMsg.isEmpty()){

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMsg);
                    commentsMap.put("userID", currentUserID);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                            .add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                                commentField.setText("");
                            } else {
                                Toast.makeText(CommentsActivity.this, "Error posting comment", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }
}
