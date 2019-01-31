package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CommessoActivity extends AppCompatActivity {

    private static final int QR = 14;
    private static final int RC_SCANNED_QR = 105;
    private static final int IN_NEGOZIO = 1;
    private static final String BACK = "back";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commesso);

        Button btnSpesa = findViewById(R.id.btnSpesa);
        Button btnProdotti = findViewById(R.id.btnProdotti);
        Button btnOrdiniOnline = findViewById(R.id.btnOrdiniOnline);
        Button btnOrdiniConclusi = findViewById(R.id.btnOrdiniConclusi);
        Button btnStatisticheVendita = findViewById(R.id.btnStatisticheVendita);

        btnSpesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentscanqrcode = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
                String messaggio = "Inquadra lo schermo del dispositivo del cliente";
                intentscanqrcode.putExtra("TIPO_CODICE", QR);
                intentscanqrcode.putExtra("MESSAGGIO", messaggio);
                startActivityForResult(intentscanqrcode, RC_SCANNED_QR);
            }
        });

        btnProdotti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentprodotti = new Intent(getApplicationContext(), GestioneProdottiActivity.class);
                startActivity(intentprodotti);
            }
        });

        btnOrdiniOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentordinionline = new Intent(getApplicationContext(), OrdiniOnlineActivity.class);
                startActivity(intentordinionline);
            }
        });

        btnOrdiniConclusi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentordiniconclusi = new Intent(getApplicationContext(), OrdiniConclusiActivity.class);
                startActivity(intentordiniconclusi);
            }
        });

        btnStatisticheVendita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentstatistiche = new Intent(getApplicationContext(), StatisticheActivity.class);
                startActivity(intentstatistiche);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SCANNED_QR) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedqr = data.getStringExtra("SCANNED_CODE");
                if (!scannedqr.equals(BACK)) {
                    int idordine = Integer.valueOf(scannedqr);
                    Log.d("SCANNED_CODE", scannedqr);

                    Intent intentspesacommesso = new Intent(this, SpesaCommessoActivity.class);
                    intentspesacommesso.putExtra("ID_ORDINE", idordine);
                    intentspesacommesso.putExtra("TIPO_SPESA", IN_NEGOZIO);
                    startActivity(intentspesacommesso);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

}
