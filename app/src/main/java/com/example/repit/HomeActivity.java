package com.example.repit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ServerTimestamp;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.Date;


public class HomeActivity extends AppCompatActivity{

    @ServerTimestamp
    Date time;

    //Item Touch Helper
    ItemTouchHelper itemTouchHelper;

    //Access Data
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference reportRef = db.collection("Report");

    //To get User
    private FirebaseAuth auth;
    private FirebaseUser user = auth.getInstance().getCurrentUser();

    //Adapter
    private ReportAdapter reportAdapter;

    //Declare UI
    private ImageView menu, add;
    private ToggleButton everyone, mine;
    private FloatingActionButton btnAddReport;
    private RecyclerView recyclerView;
    private ProgressBar loadItem;
    private Integer size;
    //Toggle Value
    private boolean swipeToggle = false;

    //Intent String
    public static final String EXTRA_TEXT = "com.example.repit.EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        //Find UI
        menu = findViewById(R.id.menu);
        everyone = findViewById(R.id.publicReport);
        mine = findViewById(R.id.myReport);
        btnAddReport = findViewById(R.id.btnAddReport);
        loadItem = findViewById(R.id.loadItem);


        //For Recycler View
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadItem.setVisibility(View.VISIBLE);
        publicReport();

        //Toggle Button
        everyone.setChecked(true);
        mine.setChecked(false);


        //On Click Listeners
        menu.setOnClickListener(view -> openMenu());
        btnAddReport.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ReportActivity.class));

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        everyone.setOnClickListener(view -> publicReport());
        mine.setOnClickListener(view -> currentUserReport());

        everyone.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                everyone.setTextColor(getResources().getColorStateList(R.color.whiteText));
                mine.setChecked(false);
            } else {
                everyone.setTextColor(getResources().getColorStateList(R.color.primaryBlue));
                mine.setChecked(true);
            }
        });
        mine.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                mine.setTextColor(getResources().getColorStateList(R.color.whiteText));
                everyone.setChecked(false);
            } else {
                mine.setTextColor(getResources().getColorStateList(R.color.primaryBlue));
                everyone.setChecked(true);
            }
        });
    }



    //All Report RecyclerView
    private void publicReport() {
        swipeToggle = false;
        Query query = reportRef.orderBy("date", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(query, Report.class)
                .build();

        reportAdapter = new ReportAdapter(options);



        reportAdapter.notifyDataSetChanged();
        loadItem.setVisibility(View.INVISIBLE);
        recyclerView.setAdapter(reportAdapter);
        reportItemClick();
        reportAdapter.startListening();
    }

    //Current User RecyclerView
    private void currentUserReport() {
        swipeToggle = true;
        reportAdapter.stopListening();
        Query query = reportRef.whereEqualTo("reportedByUserID", user.getUid()).orderBy("date", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(query, Report.class)
                .build();


        reportAdapter = new ReportAdapter(options);
        reportAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(reportAdapter);
        reportItemClick();
        swipeToDelete();
        reportAdapter.startListening();
    }

    //Swipe Delete Gesture Function
    public void swipeToDelete() {
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                if (swipeToggle)
                    return super.getSwipeDirs(recyclerView, viewHolder);
                else
                    return 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                reportAdapter.deleteItem(viewHolder.getAdapterPosition());
                showToast("Delete Successful");
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    //Get Item Click and Open ReportDetailActivity
    public void reportItemClick() {
        reportAdapter.setOnItemClickListener((documentSnapshot, position) -> {

            String path = documentSnapshot.getReference().getPath();
            Intent intent = new Intent(HomeActivity.this, ReportDetailsActivity.class);
            intent.putExtra(EXTRA_TEXT, path);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    //Display Styleable Toast
    public void showToast(String msg) {
        StyleableToast.makeText(this, msg, R.style.error).show();
    }

    //Activity Cycle
    @Override
    protected void onStart() {
        super.onStart();
        reportAdapter.startListening();
    }

    //Activity Cycle
    @Override
    protected void onStop() {
        super.onStop();
        reportAdapter.stopListening();
    }

    //Open Menu Activity
    private void openMenu() {
        startActivity(new Intent(HomeActivity.this,MenuActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }


}
