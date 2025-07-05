package com.pratham.alumniconnect;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import android.net.Uri;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle drawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.navigation_drawer);


// Drawer Toggle (hamburger icon)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        ImageView profileIcon = findViewById(R.id.profile_icon);

// ✅ Open drawer on profile icon click
        profileIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

// ✅ Load profile image into toolbar icon
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_user)
                                    .circleCrop()
                                    .into(profileIcon);
                        }
                    });
        }

        EditText searchBar = findViewById(R.id.search_input);

        searchBar.setFocusable(false);
        searchBar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        });


        // Handle drawer item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this,UserProfile.class));

            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_about) {
                Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login_Screen.class));
                finish();
            }

            drawerLayout.closeDrawers();

            return true;
        });


//fetch info
        loadDrawerHeader();


            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {


            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (itemId == R.id.nav_connections) {
                selectedFragment = new ConnectionsFragment();
            } else if (itemId == R.id.nav_goals) {
                selectedFragment = new AiFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Create Post")
                        .setMessage("Do you want to write a new post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // You can launch a new activity or do something else here
                                Toast.makeText(MainActivity.this, "Opening post editor...", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ Uncheck all drawer items when returning to MainActivity
        if (navigationView != null) {
            navigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }
            navigationView.getMenu().setGroupCheckable(0, true, true);
        }
        loadDrawerHeader();
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
    private void loadDrawerHeader() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = headerView.findViewById(R.id.header_profile_image);
        TextView username = headerView.findViewById(R.id.header_username);
        TextView email = headerView.findViewById(R.id.header_email);

        if (user != null) {
            String mail = user.getEmail();
            email.setText(mail != null ? mail : "example@example.com");

            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            username.setText(name != null ? name : "User");

                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (!TextUtils.isEmpty(imageUrl)) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.default_user)
                                        .circleCrop()
                                        .into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.default_user);
                            }
                        } else {
                            username.setText("User");
                            profileImage.setImageResource(R.drawable.default_user);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Failed to fetch name or image", e);
                        username.setText("User");
                        profileImage.setImageResource(R.drawable.default_user);
                    });
        } else {
            username.setText("Guest");
            email.setText("guest@example.com");
            profileImage.setImageResource(R.drawable.default_user);
        }
    }

}