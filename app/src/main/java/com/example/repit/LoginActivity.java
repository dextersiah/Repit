package com.example.repit;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity implements SignUpFragment.OnFragmentInteractionListener {

    //Declare UI Variable
    EditText email, password;
    Button signIn, signUp;
    FrameLayout signUpFragment;
    TextView msgText;
    ProgressBar loginProcess;

    //Declare FireBase Variables
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseUser user = auth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        } else {


            setContentView(R.layout.activity_main);

            //Edit Text Field
            email = findViewById(R.id.emailLoginField);
            password = findViewById(R.id.passwordLoginField);


            //Progress Bar
            loginProcess = findViewById(R.id.loginProcess);
            loginProcess.setVisibility(View.INVISIBLE);

            //Button
            signIn = findViewById(R.id.btnSignIn);
            signUp = findViewById(R.id.btnSignUp);

            //TextView
            msgText = findViewById(R.id.successMsg);

            //Frame
            signUpFragment = findViewById(R.id.sign_up_fragment);

            //FireBase Auth
            auth = FirebaseAuth.getInstance();

            signUp.setOnClickListener(view -> {
                openSignUp();
                msgText.setText("");
                email.setText("");
                password.setText("");
            });
        }
    }

    @Override
    public void successCreatedAccount(String sendBackText) {

        msgText.setTextColor(getResources().getColor(R.color.correctGreen));
        msgText.setText(sendBackText);
        onBackPressed();

    }

    @Override
    public void goBackPage() {
        onBackPressed();
    }


    private void openSignUp() {
        SignUpFragment signUp = SignUpFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null).add(R.id.sign_up_fragment, signUp, "Sign up fragment").commit();
    }

    public void signIn(View view) {

        //Get Input Field String
        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();

        if (emailString.isEmpty() || passwordString.isEmpty()) {
            setMessage("Please Enter A Valid Email and Password", "red");
        } else {

            loginProcess.setVisibility(View.VISIBLE);
            msgText.setText("");

            auth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        loginProcess.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));

                    } else {
                        loginProcess.setVisibility(View.INVISIBLE);
                        setMessage("Invalid Email or Password", "red");
                    }
                }
            });
        }
    }


    private void setMessage(String msg, String color) {

        msgText.setTypeface(msgText.getTypeface(), Typeface.BOLD);

        if (color == "red")
            msgText.setTextColor(getResources().getColor(R.color.dangerRed));
        else
            msgText.setTextColor(getResources().getColor(R.color.correctGreen));

        msgText.setText(msg);
    }


}
