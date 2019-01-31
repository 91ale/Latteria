package it.alessandro.latteria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AiutoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aiuto);

        Intent intent = getIntent();
        String htype = intent.getStringExtra("htype");

        ImageView imgHelp = findViewById(R.id.imgHelp);
        imgHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switch (htype) {
            case "spesa":
                imgHelp.setImageResource(R.drawable.help_spesa);
                break;
            case "ordini":
                imgHelp.setImageResource(R.drawable.help_ordini);
                break;
        }

    }
}
