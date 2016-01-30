package com.fc.aa;

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
import com.unity3d.player.*;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

public class UnityPlayerActivity extends Activity implements DataApi.DataListener,
		MessageApi.MessageListener, NodeApi.NodeListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener
{
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

	private GoogleApiClient         mGoogleApiClient;

	private SensorManager mSensorManager;
	//private Sensor mHeartSensor;
	private Sensor mGyroSensor;
	private String status;
	private String message;
	private String heartRate;


	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();



		// Google Api Client를 Wearable 로 초기화
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();


	}

	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}
	// Volume_Key event
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				Log.i("test","Volum DOWN");
				message = "1";
				break;

			case KeyEvent.KEYCODE_VOLUME_UP:
				Log.i("test", "Volume Up");
				message = "4";
				break;
		}
		return true;
	}
	// c# 스크립트와 통신하기 위한 메쏘드.
	public void CallAndroid_U(String strMsg)
	{
		// 메쏘드에 받은 값들을 유니티 c# 에 보냄
		String strSendMsg = strMsg;
		Log.d("TESTUNITY", strSendMsg);
		/* UnitySendMessage 함수를 사용하여 AndroidPluginManager 스크립트의 SetLog 메소드에 StrSendMsg 를 보냄 */
		UnityPlayer.UnitySendMessage("AndroidPluginManager", "SetLog", strSendMsg);
	}
	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	//@Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

	//#########################여기까지 유니티 동작#####################################
	// 안드로이드 웨어 통신
	protected void onStart() {
		super.onStart();
        /*Google Play Service 접속*/
		if(!mGoogleApiClient.isConnected()){
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onStop() {
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		Wearable.MessageApi.removeListener(mGoogleApiClient, this);
		Wearable.NodeApi.removeListener(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();

		super.onStop();
	}

	@Override
	public void onConnected(Bundle bundle) {
        /*Google Play Service 접속 되었을 경우 호출*/
        /*Data 수신을 위한 리스너 설정*/
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.MessageApi.addListener(mGoogleApiClient, this);
		Wearable.NodeApi.addListener(mGoogleApiClient, this);
		Log.d("TEST", "conneted!!!!!!!!!!111");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        /*Google Play Service 접속 실패했을 때 호출*/
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		Wearable.MessageApi.removeListener(mGoogleApiClient, this);
		Wearable.NodeApi.removeListener(mGoogleApiClient, this);
	}

	@Override
	public void onConnectionSuspended(int i) {
        /*Google Play Service 접속 일시정지 됐을 때 호출*/
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		Wearable.MessageApi.removeListener(mGoogleApiClient, this);
		Wearable.NodeApi.removeListener(mGoogleApiClient, this);
	}

	@Override
	public void onDataChanged(final DataEventBuffer dataEventBuffer) {
		Log.d("TEST", "onDataChanged");
		Log.d("TEST", "early" + message);
		/*
		final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
		dataEventBuffer.close();
		*/
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for(DataEvent event : dataEventBuffer) {
					if (event.getType() == DataEvent.TYPE_CHANGED) {
						String path = event.getDataItem().getUri().getPath();
						if (path.compareTo("/" + "shoot") == 0) {
							DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
							message = dataMapItem.getDataMap().getString("data");  // 전달받은 data 를 message 에 저장.
							CallAndroid_U(message);
							Log.d("TEST", message);
						} else if (path.compareTo("/" + "heart") == 0) {
							DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
							heartRate = dataMapItem.getDataMap().getString("data");
							CallAndroid_U(heartRate);
							//Log.d("TEST","HEARTRATE///"+heartRate);
						} else if (path.compareTo("/" + "status") == 0) {
							DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
							status = dataMapItem.getDataMap().getString("data");
							CallAndroid_U(status);
						}
					} else if (event.getType() == DataEvent.TYPE_DELETED) {
					}
				}
			}
		});
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
	/*
	public void SendMessage(View view)
	{
	}

	private void Send(String sdata) {
		Log.d("TEST", "ADDADDADD");
		String CommunicationPath = "/venus";
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
	*/
}
