package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CommessoActivity extends AppCompatActivity {

    private static final int QR = 14;
    private static final int RC_SCANNED_QR = 105;
    private static final int COMMESSO = 10;
    private static final int IN_NEGOZIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commesso);

        Button btnSpesa = findViewById(R.id.btnSpesa);

        btnSpesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentscanqrcode = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
                String messaggio = "Inquadra lo schermo del dispositivo del cliente";
                intentscanqrcode.putExtra("TIPO_CODICE", QR);
                intentscanqrcode.putExtra("MESSAGGIO", messaggio);
                startActivityForResult(intentscanqrcode,RC_SCANNED_QR);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==RC_SCANNED_QR) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedqr = data.getStringExtra("SCANNED_CODE");
                int idordine = Integer.valueOf(scannedqr);
                Log.d("SCANNED_CODE",scannedqr);

                Intent intentspesa = new Intent(this, SpesaActivity.class);
                intentspesa.putExtra("ID_ORDINE", idordine);
                intentspesa.putExtra("COMMESSO_CLIENTE", COMMESSO);
                intentspesa.putExtra("TIPO_SPESA", IN_NEGOZIO);
                startActivity(intentspesa);
            }
        }
    }
}
