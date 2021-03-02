package hashed.app.ampassadors.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.api.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.CommentAdapter;
import hashed.app.ampassadors.Objects.Comments;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;


public class CommentFragment extends BottomSheetDialogFragment {


    EditText comment;
    Button sendconmment;
    FirebaseFirestore firebaseFirestore;
    Task<QuerySnapshot> reference;
    List<Comments> comments;
    CommentAdapter adapter;
    PostData postData;
    CollectionReference collectionReference;
    ProgressDialog mProgressDialog ;

    boolean isLoadingComments;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView commentlist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commnet, container, false);
        comment = view.findViewById(R.id.comment_text_ed);
        commentlist = view.findViewById(R.id.comment_list);
        sendconmment = view.findViewById(R.id.send_comment_btn);
        postData = new PostData();
        mProgressDialog = new ProgressDialog(getContext());
        comments = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Post").document(postData.getPostId()).collection("Comments").get();
        collectionReference = firebaseFirestore.collection("Post").document(postData.getPostId()).collection("Comments");

        sendconmment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentext = comment.getText().toString();
                if (!commentext.isEmpty()){
                    mProgressDialog.setMessage("Adding Comments");
                    mProgressDialog.show();

                    HashMap<String , Object> data = new HashMap<>();
                    String commentid = UUID.randomUUID().toString();
                    data.put("commentId", commentid);
                    data.put("likes",0);
                    data.put("replies" ,0 );
                    data.put("time",System.currentTimeMillis());
                    data.put("comment",commentext);
                    data.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());

                    collectionReference.document(commentid).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                       mProgressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    Toast.makeText(getContext(), "Write the Comment pleas", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentlist.setAdapter(adapter);

        ReadComments(true);


        commentlist.addOnScrollListener(new ChatsScrollListener());
    }


    private void ReadComments(boolean isInitial) {

        swipeRefresh.setRefreshing(true);
        isLoadingComments = false;

        reference.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot qs :queryDocumentSnapshots.getDocuments()){
                   String commenttext =  qs.getString("comment");
                   int  like = Integer.parseInt(qs.getString("likes"+""));
                    int replies   =Integer.parseInt(qs.getString("replies"));
                    FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Comments comment = new Comments();
                    comment.setLikesnumber(like);
                    comment.setCommentstext(commenttext);
                    comment.setRepliesnumber(replies);

                    comments.add(comment);

                }
                Toast.makeText(getContext(), comments.size()+"", Toast.LENGTH_SHORT).show();
                adapter = new CommentAdapter(getContext() , comments);
                commentlist.setAdapter(adapter);

            }
        });



    }
        private void AddComments(){

   }

    private class ChatsScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingComments &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                ReadComments(false);

            }
        }
    }

    public void onRefresh() {

        comments.clear();
        adapter.notifyDataSetChanged();
        ReadComments(true);

    }
}
