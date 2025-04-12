
package com.example.tpvolley.projetws;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tpvolley.projetws.Etudiant;
import com.example.tpvolley.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class EtudiantAdapter extends ArrayAdapter<Etudiant> {
    private Context context;
    private List<Etudiant> etudiants;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        super(context, R.layout.list_item_etudiant, etudiants);
        this.context = context;
        this.etudiants = etudiants;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_etudiant, parent, false);
        }

        Etudiant etudiant = etudiants.get(position);

        TextView itemId = convertView.findViewById(R.id.item_id);
        TextView itemNom = convertView.findViewById(R.id.item_nom);
        TextView itemPrenom = convertView.findViewById(R.id.item_prenom);
        TextView itemDateNaiss = convertView.findViewById(R.id.item_date_naiss);
        ImageView itemImage = convertView.findViewById(R.id.item_image);
        if (etudiant.getImagePath() != null && !etudiant.getImagePath().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(etudiant.getImagePath());
            if (bitmap != null) {
                itemImage.setImageBitmap(bitmap);
            } else {
                itemImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            itemImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        itemId.setText(String.valueOf(etudiant.getId()));
        itemNom.setText(etudiant.getNom());
        itemPrenom.setText(etudiant.getPrenom());

        // Check if dateNaiss is null
        if (etudiant.getDateNaiss() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            itemDateNaiss.setText(dateFormat.format(etudiant.getDateNaiss()));
        } else {
            itemDateNaiss.setText("null");  // Display "null" if the date is null
        }

        return convertView;
    }
}