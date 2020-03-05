package com.example.repit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mumayank.com.airlocationlibrary.AirLocation;


public class ReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "LOG MESSAGE";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private String imageFilePath;

    //Declare UI
    private TextView txtDate, txtTime, txtUsername, txtImageURL, txtErrorMsg;
    private Button btnDate, btnTime, btnMap, btnSubmit;
    private Spinner reportType, seriousness;
    private ImageView imgCamera, imgLibrary;
    private EditText editLocation, editTextDetails;
    private ProgressBar reportProgress;
    private ImageView btnBackToMainPage;

    //Location
    private AirLocation airLocation;
    private Geocoder geocoder;
    private List<Address> addresses;

    //FireBase
    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("User");
    private CollectionReference reportCollection = db.collection("Report");
    private FirebaseUser currentUser = auth.getInstance().getCurrentUser();
    private StorageReference storageReference;

    //Storage
    private StorageTask uploadTask;
    private Uri imageUri;

    public ReportActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");


        //Initialize UI
        btnBackToMainPage = findViewById(R.id.btnBackToMainPage);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSubmit = findViewById(R.id.btnSubmit);

        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtUsername = findViewById(R.id.txtReportedByUser);
        txtImageURL = findViewById(R.id.txtImageURL);
        txtErrorMsg = findViewById(R.id.txtErrorMsg);

        reportType = findViewById(R.id.reportTypeSpinner);
        seriousness = findViewById(R.id.seriousnessSpinner);

        imgCamera = findViewById(R.id.imgCamera);
        imgLibrary = findViewById(R.id.imgLibrary);

        editLocation = findViewById(R.id.editLocation);
        editTextDetails = findViewById(R.id.editTextDetails);

        reportProgress = findViewById(R.id.report_progress);
        reportProgress.setVisibility(View.INVISIBLE);

        //Initialize Location
        geocoder = new Geocoder(this, Locale.getDefault());

        //Other Functions
        btnBackToMainPage.setOnClickListener(view -> {
            startActivity(new Intent(ReportActivity.this, HomeActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        btnDate.setOnClickListener(view -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });
        btnTime.setOnClickListener(view -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        //Location Function
        airLocation = new AirLocation(this, true, true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(Location location) {

                Double lat = location.getLatitude();
                Double lon = location.getLongitude();

                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);

                    //My Location
                    String address = addresses.get(0).getAddressLine(0);
                    editLocation.setText(address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(AirLocation.LocationFailedEnum locationFailedEnum) {
                // do something
            }
        });

        //Set current user name on txtReportedBy
        getUser();

        //Get Picture
        imgLibrary.setOnClickListener(view -> openFileChooser());

        //Get Camera
        imgCamera.setOnClickListener(view -> openCameraIntent());

        //Create Report
        btnSubmit.setOnClickListener(view -> {


            txtErrorMsg.setText("");

            String strDate = txtDate.getText().toString().trim();
            String strTime = txtTime.getText().toString().trim();
            String strDetails = editTextDetails.getText().toString().trim();
            String strImgURL = txtImageURL.getText().toString().trim();

            String strReportType = reportType.getSelectedItem().toString();
            String strSeriousness = seriousness.getSelectedItem().toString();

            int intReportType = reportType.getSelectedItemPosition();
            int intSeriousness = seriousness.getSelectedItemPosition();


            //CHECK IF FIELDS ARE EMPTY
            if (strDate.isEmpty() || strTime.isEmpty() || strDetails.isEmpty() || strImgURL.isEmpty() || intReportType == 0 || intSeriousness == 0) {
                setMessage(txtErrorMsg, "All Field Are Required", "red");

            }else {
                upload();
            }
        });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void upload() {

        if (imageUri != null) {
            toggleUI(false);
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();

                String strDate = txtDate.getText().toString();
                String strTime = txtTime.getText().toString();
                String strDetails = editTextDetails.getText().toString().trim();


                String strReportType = reportType.getSelectedItem().toString();
                String strSeriousness = seriousness.getSelectedItem().toString();

                generateReport(strReportType,strDetails,strSeriousness,strDate,strTime,editLocation.getText().toString(),downloadUrl.toString(),txtUsername.getText().toString(),currentUser.getUid());

            }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());


        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void generateReport(String strReportType, String strDetails, String strSeriousness, String strDate, String strTime, String location, String strImgURL, String user, String userID) {

        Report report = new Report(strReportType, strDetails, strSeriousness, strDate, strTime, location, strImgURL, user,userID,"Pending");
        reportCollection.add(report).addOnSuccessListener(documentReference -> {

            Intent intent = new Intent(this,HomeActivity.class);
            intent.putExtra("SUCCESS_MESSAGE","Report Created");
            startActivity(intent);
        });

    }

    private void getUser() {

        String userUID = currentUser.getUid();

        userCollection.document(userUID).get().addOnSuccessListener(documentSnapshot -> {
            String firstName = documentSnapshot.get("firstName").toString();
            String lastName = documentSnapshot.get("lastName").toString();

            Log.d(TAG, "onSuccess: "+firstName+lastName);
            txtUsername.setText(firstName + " " + lastName);
        });
    }

    public void toggleUI(boolean value){

        if (value == false)
            reportProgress.setVisibility(View.VISIBLE);
        else
            reportProgress.setVisibility(View.INVISIBLE);

        btnDate.setEnabled(value);
        btnTime.setEnabled(value);
        btnSubmit.setEnabled(value);
        btnBackToMainPage.setEnabled(value);
        reportType.setEnabled(value);
        seriousness.setEnabled(value);
        editTextDetails.setEnabled(value);
        imgCamera.setEnabled(value);
        imgLibrary.setEnabled(value);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        txtDate.setText(currentDateString);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {


        int hourofDay = hour % 12;
        txtTime.setText(String.format("%02d:%02d %s", hourofDay == 0 ? 12 : hourofDay,
                minute, hour < 12 ? "am" : "pm"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setMessage(txtImageURL,"Upload Completed","green");
            imageUri = data.getData();
        }

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK){
            setMessage(txtImageURL,"Upload Completed","green");

            File file = new File(imageFilePath);
            imageUri = Uri.fromFile(file);
            /*imageUri = Uri.parse(imageFilePath);*/
        }

    }

    //Allow GPS to get geo coordinates
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setMessage(TextView text,String msg,String color) {

        text.setTypeface(text.getTypeface(), Typeface.BOLD);

        if(color == "red")
            text.setTextColor(getResources().getColor(R.color.dangerRed));
        else
            text.setTextColor(getResources().getColor(R.color.darkerGreen));

        text.setText(msg);
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        imageFilePath = image.getAbsolutePath();

        return image;
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.repit.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(pictureIntent,REQUEST_CAPTURE_IMAGE);
            }
        }
    }
}
