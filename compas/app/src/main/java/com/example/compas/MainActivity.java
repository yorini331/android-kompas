package com.example.compas;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassImageView;
    private TextView degreeTextView;
    private EditText intervalEditText;
    private Button startButton;
    private RecyclerView dataRecyclerView;
    private DataAdapter dataAdapter;
    private List<CompassData> dataList = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private float[] rotationMatrix = new float[9];
    private float[] orientationValues = new float[3];
    private DatabaseHelper dbHelper;
    private Handler handler = new Handler();
    private Runnable saveDataRunnable;
    private boolean isSaving = false;

    private LocationManager locationManager;
    private double currentLongitude = 0.0;
    private double currentLatitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImageView = findViewById(R.id.compassImageView);
        degreeTextView = findViewById(R.id.degreeTextView);
        intervalEditText = findViewById(R.id.intervalEditText);
        startButton = findViewById(R.id.startButton);
        dataRecyclerView = findViewById(R.id.dataRecyclerView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        dbHelper = new DatabaseHelper(this);

        dataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new DataAdapter(dataList);
        dataRecyclerView.setAdapter(dataAdapter);

        startButton.setOnClickListener(v -> {
            if (isSaving) {
                stopSavingData();
            } else {
                startSavingData();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }

        loadDataFromDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rotationVectorSensor != null) {
            sensorManager.unregisterListener(this);
        }
        stopSavingData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            float azimuth = (float) Math.toDegrees(orientationValues[0]);
            azimuth = (azimuth + 360) % 360;

            degreeTextView.setText(String.format("%.0f°", azimuth));
            compassImageView.setRotation(-azimuth);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void startSavingData() {
        String intervalStr = intervalEditText.getText().toString();
        if (TextUtils.isEmpty(intervalStr)) {
            Toast.makeText(this, "Please enter an interval", Toast.LENGTH_SHORT).show();
            return;
        }

        int intervalMinutes = Integer.parseInt(intervalStr);
        int intervalMillis = intervalMinutes * 60 * 1000;

        saveDataRunnable = new Runnable() {
            @Override
            public void run() {
                float azimuth = (float) Math.toDegrees(orientationValues[0]);
                azimuth = (azimuth + 360) % 360;
                saveCompassData(azimuth, currentLongitude, currentLatitude);
                handler.postDelayed(this, intervalMillis);
            }
        };

        handler.post(saveDataRunnable);
        isSaving = true;
        startButton.setText("Stop");
    }

    private void stopSavingData() {
        handler.removeCallbacks(saveDataRunnable);
        isSaving = false;
        startButton.setText("Start");
    }

    private void saveCompassData(float azimuth, double longitude, double latitude) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("azimuth", azimuth);
        values.put("longitude", longitude);
        values.put("latitude", latitude);

        db.insert("compass_data", null, values);
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        dataList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("compass_data", null, null, null, null, null, "timestamp DESC");

        while (cursor.moveToNext()) {
            float azimuth = cursor.getFloat(cursor.getColumnIndex("azimuth"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            dataList.add(new CompassData(azimuth, longitude, latitude, timestamp));
        }
        cursor.close();

        dataAdapter.notifyDataSetChanged();
    }

    private void requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLongitude = location.getLongitude();
            currentLatitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };
}
