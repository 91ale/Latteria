package it.alessandro.latteria;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import it.alessandro.latteria.Utility.DownloadImageTask;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

public class InformazioniProdottoActivity extends AppCompatActivity {


    private static final int COMPLETATO = 1;
    private static final int EVASO = 2;
    DecimalFormat pdec = new DecimalFormat("€ 0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informazioni_prodotto);

        Intent intentinformazioniprodotto = getIntent();
        //final int statoordine = intentinformazioniprodotto.getIntExtra("STATO", -1);
        //final int position = intentinformazioniprodotto.getIntExtra("POSITION", -1);
        String nome = intentinformazioniprodotto.getStringExtra("NOME");
        String marca = intentinformazioniprodotto.getStringExtra("MARCA");
        double prezzo = intentinformazioniprodotto.getDoubleExtra("PREZZO", 0);
        String immagine = intentinformazioniprodotto.getStringExtra("IMMAGINE");
        //int quantitaselezionata = intentinformazioniprodotto.getIntExtra("QUANTITA_SELEZIONATA", -1);
        //int quantitadisponibile = intentinformazioniprodotto.getIntExtra("QUANTITA_DISPONIBILE", -1);
        String descrizione = intentinformazioniprodotto.getStringExtra("DESCRIZIONE");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(nome + " " + marca);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_ios_24px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView txtPrezzo = findViewById(R.id.txtPrezzo);
        ImageView imgProdotto = findViewById(R.id.imgProdotto);
        TextView txtDescrizione = findViewById(R.id.txtDescrizione);

        txtPrezzo.setText(String.valueOf(pdec.format(prezzo)));
        new DownloadImageTask(imgProdotto).execute(immagine);
        txtDescrizione.setText(descrizione);

    }
}
