package it.alessandro.latteria;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class ApprovazioneSpesaActivity extends AppCompatActivity {

    final String API_GET_TOKEN = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/braintree-php-3.38.0/main.php";
    final String API_CHECKOUT = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/braintree-php-3.38.0/checkout.php";
    private static final String UPDATE_STATO_ORDINE_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_order_status_from_orderid.php?";
    private static final String UPDATE_QUANTITA_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_quantity_from_orderid.php?";

    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;

    String token, importo;
    HashMap<String,String> paramsHash;

    int tipospesa = 0;
    String idordine;

    private List<Prodotto> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvazione_spesa);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //valorizza la variabile in base alla selezione effettuata dall'utente nell'activity TipoSpesaActivity (IN_NEGOZIO | ONLINE)
        tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);
        idordine = getIntent().getStringExtra("ID_ORDINE");
        importo = getIntent().getStringExtra("IMPORTO");

        Log.d("IDOrdine", idordine);
        Log.d("Importo", importo);

        TextView txtVaiCassa = findViewById(R.id.txtVaiCassa);
        Button btnPagaPayPal = findViewById(R.id.btnPagaPayPal);

        if (tipospesa == IN_NEGOZIO) {
            ImageView imgQR = findViewById(R.id.imgQR);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(idordine, BarcodeFormat.QR_CODE, 200, 200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                imgQR.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            txtVaiCassa.setText("Complimenti! Hai completato l'ordine");
        }

        new getToken().execute();

        btnPagaPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPayment();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNonce = nonce.getNonce();

                paramsHash = new HashMap<>();
                paramsHash.put("amount",importo);
                paramsHash.put("nonce",strNonce);

                sendPayments();
            }

        }
    }

    private void submitPayment() {

        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);

    }

    private void sendPayments() {

        RequestQueue queue = Volley.newRequestQueue(ApprovazioneSpesaActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_CHECKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Successful"))
                        {
                            Toast.makeText(ApprovazioneSpesaActivity.this, "Transazione completata", Toast.LENGTH_SHORT).show();
                            setOrdineCompletato(Integer.valueOf(idordine), "Completato");
                            finish();
                        }
                        else {
                            Toast.makeText(ApprovazioneSpesaActivity.this, "Transazione fallita", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("PAY_LOG", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PAY_ERROR", error.toString());
            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(paramsHash == null)
                    return null;
                Map<String,String> params = new HashMap<>();
                for (String key:paramsHash.keySet())
                {
                    params.put(key,paramsHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private class getToken extends AsyncTask{

        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(ApprovazioneSpesaActivity.this, R.style.Theme_AppCompat_Dialog);
            mDialog.setCancelable(false);
            mDialog.setMessage("Attendere...");
            mDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(final String responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token = responseBody;
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mDialog.dismiss();
        }
    }

    private void setOrdineCompletato(int idordine, String stato) {

        //modifico lo stato dell'ordine
        String queryurl = UPDATE_STATO_ORDINE_DA_IDORDINE + "IDOrdine=" + idordine + "&" +
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

        //aggiorno le quantità in giacenza in base a quelle ordinate
        queryurl = UPDATE_QUANTITA_DA_IDORDINE + "IDOrdine=" + idordine;

        stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
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
