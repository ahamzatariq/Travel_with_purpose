package com.example.travelwithpurpose.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelwithpurpose.R;
import com.example.travelwithpurpose.models.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;

    Context context;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;

    String imageDrawable;

    public CommentsRecyclerAdapter(List<Comments> commentsList){
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String currentUserID = mAuth.getCurrentUser().getUid();

        String commentData = commentsList.get(position).getMessage();
        holder.setComment(commentData);

        String user_id = commentsList.get(position).getUserID();
        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            String userName = task.getResult().getString("name");
                            String userImage = task.getResult().getString("image");

                            if(userName == null){
                                imageDrawable = holder.getStringURL(R.drawable.default_profile);
                                holder.setUserData("Anonymous", imageDrawable);
                            } else {
                                holder.setUserData(userName, userImage);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private CircleImageView userImage;
        private TextView username, commentText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUserData(String userName, String image){
            username = mView.findViewById(R.id.username);
            userImage = mView.findViewById(R.id.userImage);

            username.setText(userName);

            Glide.with(context)
                    .load(image)
                    .into(userImage);
        }

        public void setComment(String comment){
            commentText = mView.findViewById(R.id.comment);

            commentText.setText(comment);
        }

        public String getStringURL(int resourceID){
            return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceID).toString();
        }
    }
}
