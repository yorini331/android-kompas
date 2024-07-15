package com.example.compas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private List<CompassData> dataList;

    public DataAdapter(List<CompassData> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        CompassData data = dataList.get(position);
        holder.azimuthTextView.setText(String.format("Azimuth: %.0f°", data.getAzimuth()));
        holder.longitudeTextView.setText(String.format("Longitude: %.6f", data.getLongitude()));
        holder.latitudeTextView.setText(String.format("Latitude: %.6f", data.getLatitude()));
        holder.timestampTextView.setText(data.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView azimuthTextView;
        TextView longitudeTextView;
        TextView latitudeTextView;
        TextView timestampTextView;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            azimuthTextView = itemView.findViewById(R.id.azimuthTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
