package com.example.testmapboxkotlin.view;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

        String uriString = reporte.getImage_url();
        Glide.with(holder.imageView.getContext())
                .load(uriString) // URI o URL de la imagen
                .placeholder(R.drawable.ic_launcher_foreground) // Imagen por defecto mientras carga
                .error(R.drawable.ic_launcher_foreground) // Imagen en caso de error
                .into(holder.imageView);
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
        ImageView imageView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            tipoTextView = itemView.findViewById(R.id.textViewTipo);
            autorTextView = itemView.findViewById(R.id.textViewAutor);
            fechaTextView = itemView.findViewById(R.id.textViewFecha);
            imageView = itemView.findViewById(R.id.imageView2);
        }
    }
}

