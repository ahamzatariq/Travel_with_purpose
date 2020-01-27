package com.example.travelwithpurpose.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.travelwithpurpose.CommentsActivity;
import com.example.travelwithpurpose.NewPostActivity;
import com.example.travelwithpurpose.R;
import com.example.travelwithpurpose.models.BlogPost;
import com.example.travelwithpurpose.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    String currentUserID;

    Boolean isEdit = false;

    Context context;

    public BlogRecyclerAdapter(List<BlogPost> blog_list, List<User> user_list){
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);
        holder.editBtn.setVisibility(View.GONE);
        holder.deleteBtn.setVisibility(View.GONE);

        final String blogPostId = blog_list.get(position).BlogPostId;
        //final String currentUserID;

        if(mAuth.getCurrentUser() != null){
             currentUserID = mAuth.getCurrentUser().getUid();
        }

        final String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        final String image_url = blog_list.get(position).getImage_url();
        String thumb_url = blog_list.get(position).getThumb();
        holder.setBlogImage(image_url, thumb_url);

        final String imageURI = blog_list.get(position).getImageURI();

        long milliseconds = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd-MMM-yyyy", new Date(milliseconds)).toString();
        holder.setTime(dateString);

        final String blog_user_id = blog_list.get(position).getUser_id();

        if(blog_user_id.equals(currentUserID)){
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.editBtn.setVisibility(View.VISIBLE);
        }

        String userName = user_list.get(position).getName();
        String userImage = user_list.get(position).getImage();
        holder.setUserData(userName, userImage);

//        firebaseFirestore.collection("Users").document(user_id).get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()){
//                    String userName = task.getResult().getString("name");
//                    String userImage = task.getResult().getString("image");
//                    holder.setUserData(userName, userImage);
//                } else {
//                    String error = task.getException().getMessage();
//                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        //Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null){
                    if(!queryDocumentSnapshots.isEmpty()){
                        int count = queryDocumentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            }
        });

        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .document(currentUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot != null){
                    if(documentSnapshot.exists()){
                        holder.blogLikeImage.setImageDrawable(context.getDrawable(R.drawable.ic_heart_red));
                    } else {
                        holder.blogLikeImage.setImageDrawable(context.getDrawable(R.drawable.ic_heart));
                    }
                }
            }
        });

        holder.blogLikeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                        .document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                    .document(currentUserID).set(likesMap);
                        } else {
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                    .document(currentUserID).delete();
                        }
                    }
                });
            }
        });

        holder.blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewPostActivity.class);
                intent.putExtra("view", "view");
                intent.putExtra("blogID", blogPostId);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("image", image_url);
                intent.putExtra("desc", desc_data);
                context.startActivity(intent);

            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("blogID", blogPostId);
                context.startActivity(intent);
            }
        });

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewPostActivity.class);
                intent.putExtra("edit", "edit");
                intent.putExtra("blogID", blogPostId);
                intent.putExtra("imageUri", imageURI);
                intent.putExtra("image", image_url);
                intent.putExtra("desc", desc_data);
                context.startActivity(intent);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Are you sure you want to delete?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        firebaseFirestore.collection("Posts").document(blogPostId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                blog_list.remove(position);
                                user_list.remove(position);
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private TextView descView, timeStamp, userName, likeCount;
        private ImageView blogImage, blogLikeImage, blogCommentBtn;
        private CircleImageView userImage;

        private ImageButton editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeImage = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.commentBtn);
            deleteBtn = mView.findViewById(R.id.blog_deleteBtn);
            editBtn = mView.findViewById(R.id.blog_editBtn);
        }

        public void setUserData(String username, String image){
            userName = mView.findViewById(R.id.username);
            userImage = mView.findViewById(R.id.user_image);

            userName.setText(username);
            Glide.with(context)
                    .load(image)
                    .into(userImage);
        }

        public void updateLikesCount(int count){
            likeCount = mView.findViewById(R.id.blog_like_count);
            likeCount.setText(count + " Likes");
        }

        public void setDescText(String text){
            descView = mView.findViewById(R.id.description);
            descView.setText(text);
        }

        public void setBlogImage(String downloadUri, String thumbUri){
            blogImage = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.default_profile);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                    .into(blogImage);
        }

        public void setTime(String time){
            timeStamp = mView.findViewById(R.id.timestamp);
            timeStamp.setText(time);
        }
    }

}
