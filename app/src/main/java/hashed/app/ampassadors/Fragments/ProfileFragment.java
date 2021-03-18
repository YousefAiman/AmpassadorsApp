package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class ProfileFragment extends Fragment {

  TextView username, password, email, country, city, phone;
  Button edit_profile;
  ImageView imageView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_profile, container, false);

    edit_profile = view.findViewById(R.id.edit_data);
    username = view.findViewById(R.id.in_username);
    password = view.findViewById(R.id.in_password);
    email = view.findViewById(R.id.in_email);
    country = view.findViewById(R.id.in_country);
    city = view.findViewById(R.id.in_city);
    phone = view.findViewById(R.id.in_phone);
    imageView = view.findViewById(R.id.profile_picture);
//

    final UserInfo[] userInfo = new UserInfo[1];

    FirebaseFirestore.getInstance().collection("Users").document(
            FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                  userInfo[0] = documentSnapshot.toObject(UserInfo.class);
                }

              }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
          username.setText(userInfo[0].getUsername());
          password.setText(userInfo[0].getPassword());
          email.setText(userInfo[0].getEmail());
          country.setText(userInfo[0].getCountry());
          city.setText(userInfo[0].getCity());
          phone.setText(userInfo[0].getPhone());
          Picasso.get().load(userInfo[0].getImageUrl()).fit().into(imageView);
        } else {
          Toast.makeText(getActivity(), "Error" + task.getException().getMessage(),
                  Toast.LENGTH_SHORT).show();
        }
      }
    });

    edit_profile.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), profile_edit.class);
        startActivity(intent);
      }
    });


    return view;
  }
}