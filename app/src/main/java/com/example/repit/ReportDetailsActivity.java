package com.example.repit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDetailsActivity extends AppCompatActivity {


    private TextView reportedBy,date,time,type,seriousness,detail,location,status;
    private ImageView attachment;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference reportRef;
    private ImageView btnBackToMainPage;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        mContext = getApplicationContext();

        //Find UI Components
        reportedBy = findViewById(R.id.txtReportedByUser);
        date = findViewById(R.id.txtDate);
        time = findViewById(R.id.txtTime);
        type = findViewById(R.id.valReportType);
        seriousness = findViewById(R.id.valSeriousness);
        detail = findViewById(R.id.valSituation);
        location = findViewById(R.id.valLocation);
        status = findViewById(R.id.valStatus);
        attachment = findViewById(R.id.imgAttachment);



        btnBackToMainPage = findViewById(R.id.btnBackToMainPage);
        Intent intent = getIntent();
        String path = intent.getStringExtra(HomeActivity.EXTRA_TEXT);

        reportRef = db.document(path);

        reportRef.get().addOnSuccessListener(documentSnapshot -> {
            String reportUser = documentSnapshot.get("reportedBy").toString();
            String reportDate = documentSnapshot.get("date").toString();
            String reportTime = documentSnapshot.get("time").toString();
            String reportType = documentSnapshot.get("reportType").toString();
            String reportSeriousness = documentSnapshot.get("seriousness").toString();
            String reportDetails = documentSnapshot.get("description").toString();
            String reportLocation = documentSnapshot.get("location").toString();
            String reportStatus = documentSnapshot.get("reportStatus").toString();
            String reportAttachment = documentSnapshot.get("reportPicture").toString();


            reportedBy.setText(reportUser);
            date.setText(reportDate);
            time.setText(reportTime);
            type.setText(reportType);
            seriousness.setText(reportSeriousness);
            detail.setText(reportDetails);
            location.setText(reportLocation);
            status.setText(reportStatus);
            /*Picasso.with(mContext).load(reportAttachment).into(attachment);*/
            Glide.with(mContext).load(reportAttachment).override(270,150).into(attachment);

        });

        btnBackToMainPage.setOnClickListener(view -> {
            startActivity(new Intent(ReportDetailsActivity.this, HomeActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
