package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Activities.profile;
import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.R;

public class ProfileFragment extends Fragment {
    TextView username, password, email, country, city, phone;
    Button edit_profile;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userid ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile, container, false);

        edit_profile = view.findViewById(R.id.edit_data);
        username = view.findViewById(R.id.in_username);
        password = view.findViewById(R.id.in_password);
        email = view.findViewById(R.id.in_email);
        country = view.findViewById(R.id.in_country);
        city = view.findViewById(R.id.in_city);
        phone = view.findViewById(R.id.in_phone);
//
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();

        fStore = FirebaseFirestore.getInstance();
        fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
//                        String user_name = task.getResult().getString("username");
//                        String pass = task.getResult().getString("password");
//                        String ema = task.getResult().getString("email");
//                        String coun = task.getResult().getString("country");
//                        String cit = task.getResult().getString("city");
//                        String pho = task.getResult().getString("phone");
//
//                        username.setText(user_name);
//                        password.setText(pass);
//                        email.setText(ema);
//                        country.setText(coun);
//                        city.setText(cit);
//                        phone.setText(pho);

                    }
                }else {
                    Toast.makeText(getActivity(), "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        final DrawerLayout drawerLayout = view.findViewById(R.id.drawer_layout);
        view.findViewById(R.id.image_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        return view;
    }

}