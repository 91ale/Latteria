package it.alessandro.latteria;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TipoSpesaActivity extends AppCompatActivity implements View.OnClickListener {

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
                Intent intentspesainnegozio = new Intent(this, SpesaInNegozioActivity.class);
                startActivity(intentspesainnegozio);
                break;

            case R.id.btnADomicilio:
                Intent intentspesaonline = new Intent(this, SpesaOnlineActivity.class);
                startActivity(intentspesaonline);
                break;
        }
    }
}
