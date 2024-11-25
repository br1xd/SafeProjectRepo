package com.example.testmapboxkotlin.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.model.Reportes;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Reportes> reportesList;

    // Constructor que recibe la lista de reportes
    public ReportAdapter(List<Reportes> reportesList) {
        this.reportesList = reportesList;
    }

    // Este método se llama para crear un nuevo ViewHolder
    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ReportViewHolder(view);
    }

    // Este método se llama para enlazar los datos con el ViewHolder
    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        Reportes reporte = reportesList.get(position);
        holder.tipoTextView.setText(reporte.getTipo());  // Aquí mapeamos el tipo de reporte
        holder.autorTextView.setText(reporte.getAutor());  // Aquí mapeamos el autor
        holder.fechaTextView.setText(reporte.getFecha().toString());  // Aquí mapeamos la fecha
    }

    // Este método nos dice cuántos ítems tiene nuestra lista
    @Override
    public int getItemCount() {
        return reportesList.size();
    }

    // Clase interna que representa a cada ítem en el RecyclerView
    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView tipoTextView;
        TextView autorTextView;
        TextView fechaTextView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            tipoTextView = itemView.findViewById(R.id.textViewTipo);
            autorTextView = itemView.findViewById(R.id.textViewAutor);
            fechaTextView = itemView.findViewById(R.id.textViewFecha);
        }
    }
}

