package it.alessandro.latteria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DecimalFormat;

public class OrdineCompletatoActivity extends AppCompatActivity {

    private static final String UPDATE_ORDINE_IMPORTO_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_order_status_from_orderid.php?";

    private static final int ONLINE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordine_completato);

        DecimalFormat pdec = new DecimalFormat("€ 0.00");

        final int idordine = getIntent().getIntExtra("ID_ORDINE", -1);
        double totaleordine = getIntent().getDoubleExtra("TOTALE_ORDINE", -1);
        final int tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);

        TextView txtTotale = findViewById(R.id.txtTotale);
        if (tipospesa == ONLINE) {
            txtTotale.setText("Il totale pagato è di: " + pdec.format(totaleordine));
        } else {
            txtTotale.setText("Il totale dovuto è di: " + pdec.format(totaleordine));
        }


        Button btnOrdineCompletato = findViewById(R.id.btnOrdineCompletato);

        btnOrdineCompletato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipospesa == ONLINE) {
                    setOrdineCompletato(idordine, "Evaso");
                } else {
                    setOrdineCompletato(idordine, "Completato");
                }

                Intent intentcommesso = new Intent(getApplicationContext(), CommessoActivity.class);
                startActivity(intentcommesso);
            }
        });

    }

    private void setOrdineCompletato(int idordine, String stato) {

        //modifico lo stato dell'ordine a "Completato"
        String queryurl = UPDATE_ORDINE_IMPORTO_DA_IDORDINE + "IDOrdine=" + idordine + "&" +
                "Stato=" + stato;

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequestAdd);

    }

}
