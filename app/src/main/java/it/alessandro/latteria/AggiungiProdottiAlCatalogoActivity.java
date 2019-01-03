package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AggiungiProdottiAlCatalogoActivity extends AppCompatActivity {

    private static final String INSERT_PRODOTTO_IN_CATALOGO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_product_in_catalogue.php?";
    private static final String SELECT_PRODOTTO_IN_CATALOGO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php?BarCode=";
    private static final String S3_PRODUCT_IMAGE_PATH = "https://s3.eu-central-1.amazonaws.com/latteria-userfiles-mobilehub-1901756941/public/";

    private static final int EAN_13 = 13;
    private static final int RC_SCANNED_BC = 100;
    private static final int REQUEST_TAKE_PHOTO = 1;

    String scannedbc;

    EditText edtNome;
    EditText edtMarca;
    EditText edtCategoria;
    EditText edtDescrizione;

    private List<Prodotto> productList = new ArrayList<>();

    String mCurrentPhotoPath;
    String imageFileName;
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_prodotti_al_catalogo);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
        // Inizializza AWSMobileClient se non inizializzato
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("TAG_AWS", "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("TAG_AWS_ERROR", "Initialization error.", e);
            }
        });

        scanBarCode();

        Button btnAggiungiProdotto = findViewById(R.id.btnAggiungiProdotto);

        btnAggiungiProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aggiungiProdottoCatalogo();
                finish();
            }
        });

        Button btnImmagineProdotto = findViewById(R.id.btnImmagineProdotto);

        btnImmagineProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Verifica se è presente una camera activity per scattare la foto
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Crea il file sul quale verrà salvata la foto
                    photoFile = null;
                    try {
                        photoFile = salvaImmagineSuFile();
                    } catch (IOException ex) {
                        // Errore durante la creazione del file
                    }
                    // Acquisisce l'immagine solo se il file è stato creato
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(AggiungiProdottiAlCatalogoActivity.this,
                                "it.alessandro.latteria.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

    }

    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/"+imageFileName+".jpg",
                        new File(mCurrentPhotoPath));

        // collega il listener all'observer per ottenere lo stato di avanzamento del processo
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Trasferimento completato
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Gestione errori
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }

    private void scanBarCode() {

        Intent intentscanbarcode = new Intent(this, ScanBarcodeActivity.class);
        String messaggio = "Inquadra il codice a barre del prodotto che vuoi aggiungere al catalogo";
        intentscanbarcode.putExtra("TIPO_CODICE", EAN_13);
        intentscanbarcode.putExtra("MESSAGGIO", messaggio);
        startActivityForResult(intentscanbarcode, RC_SCANNED_BC);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SCANNED_BC) {
            if (resultCode == Activity.RESULT_OK) {
                scannedbc = data.getStringExtra("SCANNED_CODE");
                Log.d("SCANNED_CODE", scannedbc);
                //se il prodotto scansionato esiste già in catalogo ne estraggo le info
                getProduct(SELECT_PRODOTTO_IN_CATALOGO, scannedbc);
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                uploadWithTransferUtility();
            }
        }
    }

    private File salvaImmagineSuFile() throws IOException {
        // Crea il file dove salvare l'immagine
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* nome file */
                ".jpg",         /* estensione */
                storageDir      /* directory */
        );

        // Save a file: file_paths for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void reduceImageSize() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private void getProduct(final String urlWebService, String scannedbc) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + scannedbc,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("[]")) {
                            ParseProductJSON pj = new ParseProductJSON(response);
                            pj.getProductFromDB();
                            productList.addAll(pj.getProduct());
                            edtNome = findViewById(R.id.edtNome);
                            edtMarca = findViewById(R.id.edtMarca);
                            edtCategoria = findViewById(R.id.edtCategoria);
                            edtDescrizione = findViewById(R.id.edtDescrizione);

                            edtNome.setText(productList.get(0).getNome());
                            edtMarca.setText(productList.get(0).getMarca());
                            edtCategoria.setText(productList.get(0).getCategoria());
                            edtDescrizione.setText(productList.get(0).getDescrizione());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void aggiungiProdottoCatalogo() {
        VolleyLog.DEBUG = true;
        String queryurl = "";

        edtNome = findViewById(R.id.edtNome);
        edtMarca = findViewById(R.id.edtMarca);
        edtCategoria = findViewById(R.id.edtCategoria);
        edtDescrizione = findViewById(R.id.edtDescrizione);

        if (productList.size() > 0) {
            //se il prodotto già esisteva in catalogo
            queryurl = INSERT_PRODOTTO_IN_CATALOGO + "IDProdotto=" + productList.get(0).getIDprodotto() + "&" +
                    "BarCode=" + productList.get(0).getBarCode() + "&" +
                    "Nome=" + edtNome.getText() + "&" +
                    "Marca=" + edtMarca.getText() + "&" +
                    "Categoria=" + edtCategoria.getText() + "&" +
                    "Descrizione=" + edtDescrizione.getText();
        } else {
            //se il prodotto NON esisteva in catalogo
            queryurl = INSERT_PRODOTTO_IN_CATALOGO +
                    "BarCode=" + scannedbc + "&" +
                    "Nome=" + edtNome.getText() + "&" +
                    "Marca=" + edtMarca.getText() + "&" +
                    "Categoria=" + edtCategoria.getText() + "&" +
                    "Descrizione=" + edtDescrizione.getText()+ "&" +
                    "Percorso=" + S3_PRODUCT_IMAGE_PATH + imageFileName + ".jpg";
        }

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

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequestAdd);

    }



}
