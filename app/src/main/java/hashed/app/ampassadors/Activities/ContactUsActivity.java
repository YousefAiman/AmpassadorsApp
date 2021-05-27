package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import hashed.app.ampassadors.R;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);


        final Toolbar contactUsToolbar = findViewById(R.id.contactUsToolbar);
        contactUsToolbar.setNavigationOnClickListener(v-> finish());




    }
}