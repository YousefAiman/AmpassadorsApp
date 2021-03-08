package hashed.app.ampassadors.Objects;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class UserInfo {

    private static final CollectionReference usersRef =
            FirebaseFirestore.getInstance().collection("Users");

    String username;
    String password;
    String email;
    String country;
    String city;
    String phone;
    String imageUrl;
    String userid;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }



//    public static void getUserNameAndImage(Object object,String userId, ImageView imageIv,
//                                           TextView usernameTv){
//
//        usersRef.document(userId).get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                            if(documentSnapshot.exists()){
//
//                                object.setImageUrl(documentSnapshot.getString("imageUrl"));
//                                postData.setPublisherImage(documentSnapshot.getString("username"));
//
//                            }
//
//                        }
//                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                    if(postData.getPublisherImage()!=null){
//                        Picasso.get().load(postData.getPublisherImage()).into(imageIv);
//                    }
//
//                    usernameTv.setText(postData.getPublisherName());
//                }
//            });
//
//        }


}
