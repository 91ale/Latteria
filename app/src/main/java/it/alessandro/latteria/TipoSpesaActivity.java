package it.alessandro.latteria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class TipoSpesaActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;

    Button btnInNegozio;
    Button btnADomicilio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_spesa);

        btnInNegozio = findViewById(R.id.btnInNegozio);
        btnADomicilio = findViewById(R.id.btnADomicilio);

        btnInNegozio.setOnClickListener(this);
        btnADomicilio.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnInNegozio:
                Intent intentspesainnegozio = new Intent(this, SpesaActivity.class);
                intentspesainnegozio.putExtra("TIPO_SPESA", IN_NEGOZIO);
                startActivity(intentspesainnegozio);
                break;

            case R.id.btnADomicilio:
                intentspesainnegozio = new Intent(this, SpesaActivity.class);
                intentspesainnegozio.putExtra("TIPO_SPESA", ONLINE);
                startActivity(intentspesainnegozio);
                break;
        }
    }
}
