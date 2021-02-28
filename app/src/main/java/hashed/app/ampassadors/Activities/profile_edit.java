package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;

import hashed.app.ampassadors.R;

public class profile_edit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        drawer();

    }

    private void drawer(){
        final DrawerLayout drawerLayout_b = findViewById(R.id.drawer_layout_b);
        findViewById(R.id.image_menu_b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout_b.openDrawer(GravityCompat.START);
            }
        });
    }
}