package hashed.app.ampassadors;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.Activities.sign_in;

public class SideDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private AppBarConfiguration mAppBarConfiguration;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_side_drawer);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
      }
    });
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    NavigationView navigationView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
            .setDrawerLayout(drawer)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.side_drawer, menu);
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {

    int id = item.getItemId();
    if (id == R.id.awreaness_post) {
      //  getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();
    } else if (id == R.id.courses) {
      // getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();

    } else if (id == R.id.polls) {
      //  getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();

    } else if (id == R.id.policy) {
      //  getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();

    } else if (id == R.id.complaints) {
      //   getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();

    } else if (id == R.id.proposals) {
      // getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();

    } else if (id == R.id.about) {
      //   getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, ).commit();
    } else if (id == R.id.log_out) {

      FirebaseAuth.getInstance().signOut();
      Intent signinActivity = new Intent(getApplicationContext(), sign_in.class);
      startActivity(signinActivity);
      finish();
    }

    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }
}