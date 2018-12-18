package it.alessandro.latteria;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private static final String INSERT_ORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_order.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordine_completato);

        DecimalFormat pdec = new DecimalFormat("€ 0.00");

        final int idordine = getIntent().getIntExtra("ID_ORDINE", -1);
        double totaleordine = getIntent().getDoubleExtra("TOTALE_ORDINE", -1);

        TextView txtTotale = findViewById(R.id.txtTotale);
        txtTotale.setText(txtTotale.getText() + pdec.format(totaleordine));

        Button btnOrdineCompletato = findViewById(R.id.btnOrdineCompletato);

        btnOrdineCompletato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrdineCompletato(idordine);
            }
        });

    }

    private void setOrdineCompletato (int idordine) {

        String queryurl = "";

        queryurl = INSERT_ORDINE + "IDOrdine=" + idordine + "&" +
                "Stato=" + "Completato";

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
