package com.example.repit;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

public class ProfileFragment extends Fragment {

    //Variables
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "DEBUG";


    //Declare UI
    private Button btnEdit, btnCancel;
    private EditText firstNameField, lastNameField;
    private TextView changeProfile, responseMsg;
    private ProgressBar editProgress;


    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("User");
    private FirebaseUser currentUser = auth.getInstance().getCurrentUser();
    private StorageReference storageReference;

    //Storage
    private StorageTask uploadTask;
    private Uri imageUri = null;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnCancel = view.findViewById(R.id.btnCancel);
        btnEdit = view.findViewById(R.id.btnEdit);
        firstNameField = view.findViewById(R.id.firstNameField);
        lastNameField = view.findViewById(R.id.lastNameField);
        changeProfile = view.findViewById(R.id.changeProfile);
        responseMsg = view.findViewById(R.id.responseMsg);
        editProgress = view.findViewById(R.id.editProgress);
        editProgress.setVisibility(View.INVISIBLE);

        getUser();

        changeProfile.setOnClickListener(view2 -> openFileChooser());

        btnEdit.setOnClickListener(vie3 -> update());

        btnCancel.setOnClickListener(view1 -> sendBack());
        return view;
    }

    private void update() {
        Log.d("URI", "update: " + imageUri);


        editProgress.setVisibility(View.VISIBLE);

        String userID = currentUser.getUid();
        String fnameField = firstNameField.getText().toString();
        String lnameField = lastNameField.getText().toString();


        if (imageUri != null) {

            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();

                userCollection.document(currentUser.getUid()).update(
                        "firstName", fnameField,
                        "lastName", lnameField ,
                        "imageURL", downloadUrl.toString()
                ).addOnSuccessListener(aVoid -> {
                    getActivity().finish();
                    startActivity(new Intent(getActivity(),HomeActivity.class));
                });

            });

        } else {
            userCollection.document(userID).update(
                    "firstName", fnameField,
                    "lastName", lnameField
            ).addOnSuccessListener(aVoid -> {
                getActivity().finish();
                startActivity(new Intent(getActivity(),HomeActivity.class));
            });
        }
    }

    private void getUser() {
        String userUID = currentUser.getUid();

        userCollection.document(userUID).get().addOnSuccessListener(documentSnapshot -> {
            String firstName = documentSnapshot.get("firstName").toString();
            String lastName = documentSnapshot.get("lastName").toString();

            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
        });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {

            imageUri = data.getData();
            responseMsg.setTextColor(getResources().getColor(R.color.darkerGreen));
            responseMsg.setText("Successfully Uploaded");
            Log.d(TAG, "onActivityResult: " + imageUri);

        }
    }

    public void sendBack() {
        if (mListener != null) {
            mListener.goBackPage();
        }
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

    public interface OnFragmentInteractionListener {
        void goBackPage();
    }
}
