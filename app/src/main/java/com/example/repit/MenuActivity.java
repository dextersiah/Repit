package com.example.repit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class MenuActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener {

    //Declare UI
    private ImageView btnBackHome;
    private Button btnLogout,btnProfile;
    private CircularImageView profileImg;
    private TextView txtUserName;

    //Declare FireBase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("User");
    private FirebaseUser currentUser = auth.getInstance().getCurrentUser();


    //Context
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = getApplicationContext();

        getUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnBackHome = findViewById(R.id.btnBackHome);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        profileImg = findViewById(R.id.profileImg);
        txtUserName = findViewById(R.id.txtUserName);



        //Instantiate FireBase Auth
        auth = FirebaseAuth.getInstance();


        btnBackHome.setOnClickListener(view -> {
            Intent intent = new Intent(MenuActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLogout.setOnClickListener(view -> {
            auth.signOut();
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(view ->{
            ProfileFragment profileFragment = ProfileFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            fragmentTransaction.addToBackStack(null).add(R.id.edit_profile_fragment,profileFragment,"Profile Fragment").commit();

        });
    }

    public void getUser(){
        String userUID = currentUser.getUid();

        userCollection.document(userUID).get().addOnSuccessListener(documentSnapshot -> {
            String fName = documentSnapshot.get("firstName").toString();
            String lName = documentSnapshot.get("lastName").toString();
            String img = documentSnapshot.get("imageURL").toString();
            Picasso.with(mContext).load(img).into(profileImg);

            txtUserName.setText(fName+" "+lName);

        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void goBackPage() {
        onBackPressed();
    }
}
