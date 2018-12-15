package it.alessandro.latteria;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ApprovazioneSpesaActivity extends AppCompatActivity {

    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;

    int tipospesa = 0;
    String idordine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvazione_spesa);

        //valorizza la variabile in base alla selezione effettuata dall'utente nell'activity TipoSpesaActivity (IN_NEGOZIO | ONLINE)
        tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);

        idordine = getIntent().getStringExtra("ID_ORDINE");

        Log.d("IDOrdine", idordine);

        TextView txtVaiCassa = findViewById(R.id.txtVaiCassa);
        Button btnPagaPayPal = findViewById(R.id.btnPagaPayPal);

        if (tipospesa == IN_NEGOZIO) {
            ImageView imgQR = findViewById(R.id.imgQR);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(idordine, BarcodeFormat.QR_CODE,200,200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                imgQR.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        else
        {
            txtVaiCassa.setText("Complimenti! Hai completato l'ordine");
        }

        btnPagaPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}
