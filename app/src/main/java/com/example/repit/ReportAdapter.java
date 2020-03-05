package com.example.repit;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;


public class ReportAdapter extends FirestoreRecyclerAdapter<Report, ReportAdapter.ReportHolder> {

    private  OnItemClickListener listener;

    public ReportAdapter(@NonNull FirestoreRecyclerOptions<Report> options) {
        super(options);

    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportHolder holder, int i, @NonNull Report report) {
        String seriousLvl = "";
        String status = "";
        holder.txtTitle.setText(report.getReportType());
        holder.txtDescription.setText(report.getDescription());
        holder.txtDate.setText(report.getDate());
        holder.txtReportedBy.setText(report.getReportedBy());

        seriousLvl = report.getSeriousness();
        status = report.getReportStatus();

        if(!status.equals("Accepted")){
            holder.circularImageView.setVisibility(View.INVISIBLE);
        }else{
            holder.circularImageView.setVisibility(View.VISIBLE);
        }



        if (seriousLvl.equals("Low")) {
            holder.seriousness.setCardBackgroundColor(Color.parseColor("#77dd77"));
        } else if (seriousLvl.equals("Medium")) {
            holder.seriousness.setCardBackgroundColor(Color.parseColor("#fcfc49"));
        } else if (seriousLvl.equals("High")) {
            holder.seriousness.setCardBackgroundColor(Color.parseColor("#fd8383"));
        }
    }


    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item,parent,false);
        return new ReportHolder(v);
    }

    public void deleteItem(int position){

        getSnapshots().getSnapshot(position).getReference().delete();

    }

    class ReportHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtDescription, txtDate, txtReportedBy;
        CardView seriousness;
        ImageButton seeDetails;
        CircularImageView circularImageView;


        public ReportHolder(@NonNull View itemView) {
            super(itemView);
            seeDetails = itemView.findViewById(R.id.seeDetails);
            seriousness = itemView.findViewById(R.id.seriousness);
            txtTitle = itemView.findViewById(R.id.text_view_title);
            txtDescription = itemView.findViewById(R.id.text_view_description);
            txtDate = itemView.findViewById(R.id.text_view_date);
            txtReportedBy = itemView.findViewById(R.id.text_view_report_by);
            circularImageView = itemView.findViewById(R.id.acceptedIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot,int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
