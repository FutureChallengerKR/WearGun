package com.fc.aa;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener,DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private String TAG = "MainActivity";

    /*Google Play Service API 객체*/
    private GoogleApiClient         mGoogleApiClient;
    private TextView mTextView;

    //SENSOR EVENT
    private SensorManager mSensorManager;
    //private Sensor mHeartSensor;
    private Sensor mGyroSensor;

    private float gyro_x=0;
    private float gyro_y=0;
    private float gyro_z=0;
    private boolean buff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        /*Google Play Service 객체를 Wearable 설정으로 초기화*/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected void onStart() {
        super.onStart();
        if(mSensorManager != null){
            mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            //mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        /*Google Play Service 접속*/
        //if(!mGoogleApiClient.isConnected()){
        mGoogleApiClient.connect();
        //}
    }

    @Override
    protected void onStop() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        if(mSensorManager != null) mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*Google Play Service 접속 되었을 경우 호출*/
        /*Data 수신을 위한 리스너 설정*/
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*Google Play Service 접속 실패했을 때 호출*/
        Log.d(TAG, "onConnectionFailed");
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        /*Google Play Service 접속 일시정지 됐을 때 호출*/
        Log.d(TAG, "onConnectionSuspended");
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        /*Google Play Service 데이터가 변경되면 호출*/
        /*
        Log.d(TAG, "onDataChanged");
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        dataEventBuffer.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (DataEvent event : events) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        String path = event.getDataItem().getUri().getPath();
                        String CommunicationPath = "/venus";
                        if (CommunicationPath.equals(path)) {
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                            String message = dataMapItem.getDataMap().getString("data");

                            mTextView.setText(message);

                        }

                    } else if (event.getType() == DataEvent.TYPE_DELETED) {

                    }
                }

            }
        });
        */
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        /*메시지가 수신되면 호출*/
    }

    @Override
    public void onPeerConnected(Node node) {
        /*Wearable 페어링 되면 호출*/
    }

    @Override
    public void onPeerDisconnected(Node node) {
        /*Wearable 페어링 해제되면 호출*/
    }
    public void onSensorChanged(SensorEvent event) {
        Log.d("Sensor", "onSensorChanged");
        /*
        if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            mHeartRate = event.values[0];
            updateDisplay();
            sendHeartRate();
        }
        */
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro_x = event.values[0];
            gyro_y = event.values[1];
            gyro_z = event.values[2];

            final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    if (gyro_z < 0) {
                        buff = true;
                    }
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            if (gyro_z >= 3) {
                                if (buff == true/* && gameStatus == true*/) {
                                    Send("1");
                                    new Timer().schedule(new TimerTask() {
                                        public void run() {
                                            vibe.vibrate(500);
                                        }
                                    }, 300);
                                    new Timer().schedule(new TimerTask() {
                                        public void run() {
                                            buff = false;
                                        }
                                    }, 1000);
                                } // end if
                                else if(gyro_z <= -3) {
                                    if (buff == true/* && gameStatus == true*/) {
                                        Send("0");
                                    }
                                }
                                Send("0");
                            } // end if
                        }
                    }, 5000);
                }
            }, 1000);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void Send(String sdata) {
        Log.d("TEST", "GYRO");
        String CommunicationPath = "/shoot";
        PutDataMapRequest dataMap = PutDataMapRequest.create(CommunicationPath);
        dataMap.getDataMap().putString("data", sdata);
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d("TAG", "Sending sensor data was successful: " + dataItemResult.getStatus().isSuccess());
                    }
                });
    }

}
