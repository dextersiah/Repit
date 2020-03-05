package com.example.repit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;


public class SignUpFragment extends Fragment {


    //Declare UI Variables
    private ImageView btnBack;
    private EditText firstName,lastName,email,password;
    private Button btnRegister;
    private TextView msgText;
    private ProgressBar progressCircle;

    //Declare FireBase Variables
    private FirebaseAuth mAuth;
    private FirebaseUser createdUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("User");

    private OnFragmentInteractionListener mListener;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();

        return fragment;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void successCreatedAccount(String sendBackText);
        void goBackPage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_up, container, false);


        //Find from view
        btnBack = view.findViewById(R.id.btnBack);
        firstName = view.findViewById(R.id.firstNameField);
        lastName = view.findViewById(R.id.lastNameField);
        email = view.findViewById(R.id.emailField);
        password = view.findViewById(R.id.passwordField);
        btnRegister = view.findViewById(R.id.btnSignUp);
        msgText = view.findViewById(R.id.msgText);
        progressCircle = view.findViewById(R.id.progress_circle);
        progressCircle.setVisibility(View.INVISIBLE);


        //Initialize FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Go Back to Login Page
                sendBack();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get Input Field String
                String firstNameString = firstName.getText().toString();
                String lastNameString = lastName.getText().toString();
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();


                if (firstNameString.isEmpty() || lastNameString.isEmpty() || emailString.isEmpty() || passwordString.isEmpty()){
                    setMessage("All Fields Are Required","red");

                }else{
                    //Call Register Method
                    registerAccount(emailString,passwordString);
                    toggleUI(false);
                }
            }
        });
        return view;
    }


    //Register account with FireBase Auth
    private void registerAccount(String email, String pw) {
        mAuth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                //Get created user id
                createdUser = task.getResult().getUser();
                String userID = createdUser.getUid();

                //Get user details
                String firstNameString = firstName.getText().toString();
                String lastNameString = lastName.getText().toString();

                setAccountDetailsToFireBase(firstNameString,lastNameString,"https://firebasestorage.googleapis.com/v0/b/firestoreexample-b916f.appspot.com/o/uploads%2F1562249753048.png?alt=media&token=a1974530-058f-4036-99e5-a52183ef2702",userID);

                Log.d("Created", "onComplete: "+createdUser.getUid());
            }else{
                Log.d("Error", "onFailed Complete: "+task.getException().getMessage());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error", "onFailure: "+e.getMessage());
            }
        });
    }

    //Set account details to FireStore
    private void setAccountDetailsToFireBase(String fName,String lName,String imageURL,String userID){
        User user = new User(fName,lName,imageURL);

        userRef.document(userID).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toggleUI(true);
                sendBack("Account successfully created");
            }
        });
    }

    //Set error/success message
    private void setMessage(String msg,String color) {

        if(color == "red")
            msgText.setTextColor(getResources().getColor(R.color.dangerRed));
        else
            msgText.setTextColor(getResources().getColor(R.color.correctGreen));

        msgText.setText(msg);
    }

    //Go back to activity with success
    public void sendBack(String msg){
        if(mListener != null){
            mListener.successCreatedAccount(msg);
        }
    }

    //Back Button
    public void sendBack(){
        if(mListener != null){
            mListener.goBackPage();
        }
    }


    //Toggle Button
    public void toggleUI(boolean value){

        if (value == false)
            progressCircle.setVisibility(View.VISIBLE);
        else
            progressCircle.setVisibility(View.INVISIBLE);
        btnBack.setEnabled(value);
        firstName.setEnabled(value);
        lastName.setEnabled(value);
        email.setEnabled(value);
        password.setEnabled(value);
        btnRegister.setEnabled(value);
    }







}
