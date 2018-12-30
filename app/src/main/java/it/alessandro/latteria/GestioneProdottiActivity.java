package it.alessandro.latteria;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class GestioneProdottiActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_prodotti);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnCatalogo = findViewById(R.id.btnCatalogo);
        Button btnNegozio = findViewById(R.id.btnNegozio);
        Button btnMagazzino = findViewById(R.id.btnMagazzino);
        Button btnVisualizzaProdotti = findViewById(R.id.btnVisualizzaProdotti);

        btnCatalogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentcatalogo = new Intent(getApplicationContext(), AggiungiProdottiAlCatalogoActivity.class);
                startActivity(intentcatalogo);
            }
        });

        btnNegozio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentnegozio = new Intent(getApplicationContext(), AggiungiProdottiNegozioActivity.class);
                startActivity(intentnegozio);
            }
        });

        btnMagazzino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentmagazzino = new Intent(getApplicationContext(), AggiungiProdottiMagazzinoActivity.class);
                startActivity(intentmagazzino);
            }
        });

        btnVisualizzaProdotti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentvisualizzaprodotti = new Intent(getApplicationContext(), VisualizzaProdottiActivity.class);
                startActivity(intentvisualizzaprodotti);
            }
        });

    }
}
