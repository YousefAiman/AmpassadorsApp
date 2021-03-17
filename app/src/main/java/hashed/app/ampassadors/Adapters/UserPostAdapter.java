package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.UserPostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserPostHolder> {

    private static List<UserPostData> posts;
    Context context;

    private static final CollectionReference usersCollectionRef =
            FirebaseFirestore.getInstance().collection("Users");


    public UserPostAdapter(List<UserPostData> posts , Context context){
        UserPostAdapter.posts = posts ;
        this.context = context;
    }


    @NonNull
    @Override
    public UserPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new UserPostHolder(LayoutInflater.from(context).inflate(R.layout.home_post_news_item
                , parent , false));

    }

    @Override
    public void onBindViewHolder(@NonNull UserPostHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class UserPostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CircleImageView imageIv ;
        private final TextView usernameTv ;
        private final TextView dateTv ;
        private final TextView titleTv ;
        private ImageView menuIv ;
        private final ImageView postIv ;
        private final TextView likesTv ;
        private final TextView commentsTv ;
        private final TextView redMoreTv ;
        private final TextView likeTv;
        private final TextView commentTv;
        private final TextView descriptionTv;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault());


        public UserPostHolder(@NonNull View itemView) {
            super(itemView);
            imageIv = itemView.findViewById(R.id.imageIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            postIv = itemView.findViewById(R.id.postIv);
            likesTv = itemView.findViewById(R.id.likesTv);
            commentsTv = itemView.findViewById(R.id.commentsTv);
            redMoreTv = itemView.findViewById(R.id.redMoreTv);
            likeTv = itemView.findViewById(R.id.likeTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            menuIv = itemView.findViewById(R.id.menuIv);
        }

        private void bind(UserPostData postData){

            Picasso.get().load(postData.getImageUrl()).fit().into(postIv);

            if(postData.getPublisherName() == null){

                getUserInfo(postData,postData.getPublisherId());

            }else{

                if(postData.getPublisherImage()!=null){
                    Picasso.get().load(postData.getPublisherImage()).fit().into(imageIv);
                }

                usernameTv.setText(postData.getPublisherName());

            }

            titleTv.setText(postData.getDescription());
            dateTv.setText(dateFormat.format(postData.getPublishTime()));

            if(GlobalVariables.getLikesList().contains(postData.getPostId())){

                likeTv.setTextColor(itemView.getContext()
                        .getResources().getColor(R.color.red));

            }else{

                likeTv.setTextColor(itemView.getContext()
                        .getResources().getColor(R.color.black));

            }

            likesTv.setText(String.valueOf(postData.getLikes()));
            commentsTv.setText(String.valueOf(postData.getComments()));
            descriptionTv.setText(postData.getDescription());

            likeTv.setOnClickListener(this);
            commentTv.setOnClickListener(this);
            redMoreTv.setOnClickListener(this);
            menuIv.setOnClickListener(this);

        }

        private void getUserInfo(UserPostData postData, String userId){

            usersCollectionRef.document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if(documentSnapshot.exists()){

                                postData.setImageUrl(documentSnapshot.getString("imageUrl"));
                                postData.setPublisherName(documentSnapshot.getString("username"));

                            }

                        }
                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    Picasso.get().load(postData.getImageUrl()).into(imageIv);
                    usernameTv.setText(postData.getPublisherName());

                }
            });

        }


        @Override
        public void onClick(View view) {

            if(view.getId() == R.id.likeTv){

                if(likeTv.getCurrentTextColor() ==
                        itemView.getContext()
                                .getResources().getColor(R.color.red)
                ){


                    likeTv.setTextColor(itemView.getContext()
                            .getResources().getColor(R.color.black));

                    likesTv.setText(String.valueOf(
                            (Integer.parseInt(likesTv.getText().toString())-1)
                    ));

                    UserPostData.likePost(posts.get(getAdapterPosition()).getPostId(),2);


                }else{

                    likeTv.setTextColor(itemView.getContext()
                            .getResources().getColor(R.color.red));

                    likesTv.setText(String.valueOf(
                            (Integer.parseInt(likesTv.getText().toString())+1)
                    ));

                    UserPostData.likePost(posts.get(getAdapterPosition()).getPostId(),1);

                }

            }else if(view.getId() == R.id.commentTv){


            }else if(view.getId() == R.id.redMoreTv){

                if(descriptionTv.getVisibility()==View.VISIBLE){
                    redMoreTv.setText("read more");
                    descriptionTv.setVisibility(View.GONE);
                }else{
                    redMoreTv.setText("read less");
                    descriptionTv.setVisibility(View.VISIBLE);
                }


            }else if(view.getId() == R.id.menuIv){

            }

        }
    }
}