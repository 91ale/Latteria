package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InformazioniProdottoActivity extends AppCompatActivity {


    private static final int COMPLETATO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informazioni_prodotto);

        TextView txtNomeMarca = findViewById(R.id.txtNomeMarcaProdotto);
        TextView txtPrezzo = findViewById(R.id.txtPrezzo);
        ImageView imgProdotto = findViewById(R.id.imgProdotto);
        TextView txtDescrizione = findViewById(R.id.txtDescrizione);
        final Spinner spnQuantita = findViewById(R.id.spnQuantità);
        Button btnAggiungiProdotto = findViewById(R.id.btnAggiungiProdotto);

        Intent intentinformazioniprodotto = getIntent();
        final int statoordine = intentinformazioniprodotto.getIntExtra("STATO",-1);
        final int position = intentinformazioniprodotto.getIntExtra("POSITION",-1);
        String nome = intentinformazioniprodotto.getStringExtra("NOME");
        String marca = intentinformazioniprodotto.getStringExtra("MARCA");
        double prezzo = intentinformazioniprodotto.getDoubleExtra("PREZZO", 0);
        String immagine = intentinformazioniprodotto.getStringExtra("IMMAGINE");
        int quantitaselezionata = intentinformazioniprodotto.getIntExtra("QUANTITA_SELEZIONATA",-1);
        int quantitadisponibile = intentinformazioniprodotto.getIntExtra("QUANTITA_DISPONIBILE",-1);
        String descrizione = intentinformazioniprodotto.getStringExtra("DESCRIZIONE");

        txtNomeMarca.setText(nome + " " + marca);
        txtPrezzo.setText(String.valueOf(prezzo));
        new DownloadImageTask(imgProdotto).execute(immagine);
        txtDescrizione.setText(descrizione);

        //se l'ordine è già stato completato nascondo il bottone e lo spinner
        if (statoordine == COMPLETATO) {
            spnQuantita.setVisibility(View.INVISIBLE);
            btnAggiungiProdotto.setVisibility(View.INVISIBLE);
        }

        String [] arrayquantità = arrayQuantità(quantitadisponibile);
        // crea un ArrayAdapter usando l'array delle quantità e il layout passato
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayquantità);
        // specifica il layout della lista scelte
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // applica l'adapter allo spinner
        spnQuantita.setAdapter(spinnerAdapter);
        spnQuantita.setSelection(quantitaselezionata-1);

        btnAggiungiProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentspesa = new Intent();
                intentspesa.putExtra("QUANTITA_SELEZIONATA", Integer.valueOf(spnQuantita.getSelectedItem().toString()));
                intentspesa.putExtra("POSITION", position);
                setResult(Activity.RESULT_OK, intentspesa);
                finish();
            }
        });

    }

    private String[] arrayQuantità (int quantità) {
        String[] arrayquantità = new String[quantità];
        for ( int i = 0; i < quantità; i++) {
            arrayquantità[i] = String.valueOf(i+1);
        }
        return arrayquantità;
    }
}
