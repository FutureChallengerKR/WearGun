package com.fc.aa;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
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
    private Layout RoundLayout;

    //SENSOR EVENT
    private SensorManager mSensorManager;
    private Sensor mHeartSensor;
    private Sensor mGyroSensor;

    private float gyro_x=0;
    private float gyro_y=0;
    private float gyro_z=0;
    private boolean buff = false;
    private float mHeartRate;

    private PowerManager.WakeLock mWakeLock;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Gesture Detector 를 활용하기위한 선언.(single tap,double tap)
        gestureDetector = new GestureDetector(getApplicationContext(), new GameGestureListener());
        // Power Manager 를 통하여 일정시간후 앱종료 방지.
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,"");
        mWakeLock.acquire();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                stub.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });
            }
        });

        // SensorManager 선언 및 사용할 센서 선언.
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

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
            mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
    // Sensor 활용을 위한 메쏘드.
    public void onSensorChanged(SensorEvent event) {
        Log.d("Sensor", "onSensorChanged");
        // Sensor type 이 HEARTRATE 일떄 실행
        if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            mHeartRate =(int) event.values[0];
            if(mHeartRate >=80) {
                SendHeart("heartUp");
            }
            else{
                SendHeart("heartDown");
            }
        }
        // Sensor type 이 GYRO 일떄 실행
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
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
                                            //Send("1");
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
    // Sensor들의 정확도 향샹을 위한 메쏘드
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    // SHOOT(1,0),HEARTRATE(heartUp,heartDown) 폰에 전달
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
    private void SendHeart(String sdata) {
        Log.d("TEST","heart"+ sdata);
        String CommunicationPath = "/heart";
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
    private void SendStatus(String sdata) {
        Log.d("TESTstatus", "STATUS");
        String CommunicationPath = "/status";
        PutDataMapRequest dataMap = PutDataMapRequest.create(CommunicationPath);
        dataMap.getDataMap().putString("data", sdata);
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d("TAG2", "Sending sensor data was successful: " + dataItemResult.getStatus().isSuccess());
                    }
                });
    }
    // GestureDetector 를 활용하기 위한 클래스
    private class GameGestureListener extends GestureDetector.SimpleOnGestureListener{
        // SingleTap event
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Send("start");
            Send("removeCall"); // dataRemove
            Log.d("tap","singleTap");

            return true;
        }
        // Down event
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // DoubleTapEvent event
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Send("reset");
            Send("removeCall"); // dataRemove
            Log.d("tap","doubleTap");

            return true;
        }
    }


}
