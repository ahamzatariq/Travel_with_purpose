package com.example.travelwithpurpose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.travelwithpurpose.fragments.AccountFragment;
import com.example.travelwithpurpose.fragments.HomeFragment;
import com.example.travelwithpurpose.fragments.NotificationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    Toolbar mainToolbar;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FloatingActionButton addPostBtn;
    BottomNavigationView mainBottomNav;

    String current_userID;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    Boolean isGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        addPostBtn = findViewById(R.id.floatingActionButton);
        mainToolbar = findViewById(R.id.main_toolbar);

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Home");

        isGuest = getIntent().getBooleanExtra("guest", false);

        if(isGuest == true){
            addPostBtn.hide();
            //addPostBtn.setVisibility(View.GONE);
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("guest", isGuest);

        if(mAuth.getCurrentUser() != null){
            mainBottomNav = findViewById(R.id.mainBottomNav);

            //FRAGMENTS
            homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);

            replaceFragment(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.bottom_home:
                            replaceFragment(homeFragment);
                            return true;

//                        case R.id.bottom_notification:
//                            replaceFragment(notificationFragment);
//                            return true;
//
//                        case R.id.bottom_account:
//                            replaceFragment(accountFragment);
//                            return true;

                        default:
                            return false;
                    }
                }
            });

            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeActivity.this, NewPostActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){
            sendToLogin();
        } else {
            if(isGuest == false){
                current_userID = mAuth.getCurrentUser().getUid();
                firebaseFirestore.collection("Users").document(current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(!task.getResult().exists()){
                                Intent setupIntent = new Intent(HomeActivity.this, SetupActivity.class);
                                startActivity(setupIntent);
                                finish();
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isGuest != false){
            getMenuInflater().inflate(R.menu.guest_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.Logout:
                logout();
                return true;

            case R.id.settings:
                Intent settings = new Intent(HomeActivity.this, SetupActivity.class);
                startActivity(settings);
                return true;

                default:
                    return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(isGuest){
            mAuth.signOut();
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent1);
            finish();
        }
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent1 = new Intent(getApplicationContext(), WriterSignIn.class);
        startActivity(intent1);
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
