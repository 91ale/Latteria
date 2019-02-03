package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import it.alessandro.latteria.Adapter.OrderAdapter;
import it.alessandro.latteria.Object.Ordine;
import it.alessandro.latteria.Object.Utente;
import it.alessandro.latteria.Parser.ParseOrderJSON;
import it.alessandro.latteria.Parser.ParseUserJSON;

import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class OrdiniActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECT_ORDINI = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_orders_from_UID.php?IDUtente=";
    private static final String SELECT_UTENTE_DA_IDUTENTE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php?IDUtente=";

    String loggeduser = "";
    OrderAdapter mAdapter;
    private DrawerLayout drawerLayout;
    private List<Ordine> orderList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Utente utente = new Utente();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordini);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_menu_24);

        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("FirebaseUser", "0");

        getInformazioniUtente(loggeduser, new VolleyCallBack() {
            @Override
            public void onSuccess(String response) {
                TextView txtNomeCognome = findViewById(R.id.txtNomeCognome);
                txtNomeCognome.setText("Benvenuto " + utente.getnome() + " " + utente.getcognome());
            }
        });

        //imposta il navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new OrderAdapter(OrdiniActivity.this, orderList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        getOrdini(SELECT_ORDINI);

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
            Intent intentspesa = new Intent(getApplicationContext(), TipoSpesaActivity.class);
            startActivity(intentspesa);
        } else if (id == R.id.nav_profilo) {
            Intent intentprofilo = new Intent(getApplicationContext(), MioProfiloActivity.class);
            startActivity(intentprofilo);
        } else if (id == R.id.nav_ordini) {
            Intent intentordini = new Intent(getApplicationContext(), OrdiniActivity.class);
            startActivity(intentordini);
        } else if (id == R.id.nav_aiuto) {
            Intent intentaiuto = new Intent(getApplicationContext(), AiutoActivity.class);
            intentaiuto.putExtra("htype", "ordini");
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + loggeduser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseOrderJSON pj = new ParseOrderJSON(response);
                        pj.getOrderFromDB();
                        orderList.addAll(pj.getOrder());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new OrderAdapter(OrdiniActivity.this, orderList);
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

    public interface VolleyCallBack {
        void onSuccess(String response);
    }

    private void getInformazioniUtente(String idutente, final VolleyCallBack callback) {

        String queryurl = SELECT_UTENTE_DA_IDUTENTE + idutente;

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseUserJSON pj = new ParseUserJSON(response);
                        pj.getUserFromDB();
                        utente = pj.getUtente();
                        callback.onSuccess(response);
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
