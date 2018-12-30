package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class OrdiniOnlineActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECT_ORDINI_ONLINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_orders_online_completato.php";

    private static final int QR = 14;
    private static final int RC_SCANNED_QR = 105;

    private static final int COMMESSO = 1;

    String loggeduser = "";
    OrderAdapter mAdapter;
    private MaterialSearchBar searchBar;
    private DrawerLayout drawerLayout;
    private List<Ordine> orderList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordini_online);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_menu_24);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("FirebaseUser", "0");

        //imposta il navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new OrderAdapter(OrdiniOnlineActivity.this, orderList, COMMESSO);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        getOrdini(SELECT_ORDINI_ONLINE);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Rileva quale voce del menu è stata selezionata ed avvia l'activity corrispondente
        int id = item.getItemId();

        if (id == R.id.nav_spesa) {
            Intent intentscanqrcode = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
            String messaggio = "Inquadra lo schermo del dispositivo del cliente";
            intentscanqrcode.putExtra("TIPO_CODICE", QR);
            intentscanqrcode.putExtra("MESSAGGIO", messaggio);
            startActivityForResult(intentscanqrcode, RC_SCANNED_QR);
        } else if (id == R.id.nav_prodotti) {
            Intent intentgestioneprodotti = new Intent(getApplicationContext(), GestioneProdottiActivity.class);
            startActivity(intentgestioneprodotti);
        } else if (id == R.id.nav_ordini_online) {
            Intent intentordinionline = new Intent(getApplicationContext(), OrdiniOnlineActivity.class);
            startActivity(intentordinionline);
        } else if (id == R.id.nav_ordini_conclusi) {
            Intent intentaiuto = new Intent(getApplicationContext(), AiutoActivity.class);
            startActivity(intentaiuto);
        } else if (id == R.id.nav_logout) {
            SignOut();
            Intent intentlogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentlogin);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Logout utente
    private void SignOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });

    }

    private void getOrdini(final String urlWebService) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseOrderJSON pj = new ParseOrderJSON(response);
                        pj.getOrderFromDB();
                        orderList.addAll(pj.getOrder());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new OrderAdapter(OrdiniOnlineActivity.this, orderList, COMMESSO);
                        recyclerView.setAdapter(mAdapter);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
