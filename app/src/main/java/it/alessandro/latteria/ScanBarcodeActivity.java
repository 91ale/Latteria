package it.alessandro.latteria;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScanBarcodeActivity extends AppCompatActivity {

    private static final int EAN_13 = 13;
    private static final String BACK = "back";

    SurfaceView surfaceView;
    SurfaceView transparentView;
    SurfaceHolder holderTransparent;
    int  deviceHeight,deviceWidth;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    TextView txtTipoScansione;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("SCANNED_CODE", BACK);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        int tipocodice = getIntent().getIntExtra("TIPO_CODICE", -1);
        String messaggio = getIntent().getStringExtra("MESSAGGIO");

        surfaceView = findViewById(R.id.camerapreview);
        transparentView = findViewById(R.id.transparentView);

        holderTransparent = transparentView.getHolder();
        transparentView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Draw(surfaceView.getHeight(), surfaceView.getWidth());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        holderTransparent.setFormat(PixelFormat.TRANSLUCENT);
        transparentView.setZOrderMediaOverlay(true);

        //getting the device heigth and width
        deviceWidth = getScreenWidth();
        deviceHeight = getScreenHeight();

        txtTipoScansione = findViewById(R.id.txtTipoScansione);

        if (tipocodice == EAN_13) {
            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.EAN_13 | Barcode.EAN_8).build();
            txtTipoScansione.setText(messaggio);
        } else {
            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.QR_CODE).build();
            txtTipoScansione.setText(messaggio);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(height, width).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() { }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qreanCodes = detections.getDetectedItems();

                if (qreanCodes.size() != 0) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("SCANNED_CODE", qreanCodes.valueAt(0).displayValue);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void Draw(int height, int width) {

        float RectLeft, RectTop,RectRight,RectBottom ;
        Canvas canvas = holderTransparent.lockCanvas(null);
        Paint  paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        RectLeft = 200;
        RectTop = 400 ;
        RectRight = width - 200;
        RectBottom = height - 400;
        Rect rec=new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);
        canvas.drawRect(rec,paint);
        holderTransparent.unlockCanvasAndPost(canvas);
    }
}
