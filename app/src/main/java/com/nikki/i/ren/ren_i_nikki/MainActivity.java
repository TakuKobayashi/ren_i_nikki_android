package com.nikki.i.ren.ren_i_nikki;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mClient;
    private final static int REQUEST_CODE_RESOLUTION = 1;
    private static final int START_SCREEN_DISPLAY_TIME = 1000; // Millisecond
    private HashMap<String, String> mParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mParams = new HashMap<String, String>();

        ImageView image = (ImageView) findViewById(R.id.sprashImage);
        image.setImageResource(R.mipmap.sprash);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this, this, this);
        builder.addApi(Games.API)
                .addApi(Plus.API)
                .addScope(Games.SCOPE_GAMES)
                .addScope(Plus.SCOPE_PLUS_LOGIN);
        mClient = builder.build();
    }

    private void loginApplication(){
        GcmTokenReceiverTask task = new GcmTokenReceiverTask(this);
        task.setOnTokenReceievedCallback(new GcmTokenReceiverTask.TokenReceievedCallback() {
            @Override
            public void onRecieve(String regId) {
                Log.d(Config.TAG, "regId:" + regId);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.enable();
                Player player = Games.Players.getCurrentPlayer(mClient);
                mParams.put("google_id", player.getPlayerId());
                mParams.put("name", player.getDisplayName());
                mParams.put("notification_token", regId);
                mParams.put("mac_address", bluetoothAdapter.getAddress());
                mParams.put("category", "smartphone");
                StringRequest postRequest = new StringRequest(Request.Method.POST,Config.ROOT_URL + "api/users/login",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                Log.d(Config.TAG, "res:" + s);
                                moveNextActivity();
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                Log.d(Config.TAG, error.getMessage());
                            }
                        }){
                    @Override
                    protected Map<String,String> getParams(){
                        return mParams;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(postRequest);
                queue.start();
            }
        });
        task.execute(Config.SENDER_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mClient.isConnected()) {
            Player player = Games.Players.getCurrentPlayer(mClient);
            Log.d(Config.TAG, "Id:" + player.getPlayerId());
            loginApplication();
        }else {
            mClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Player player = Games.Players.getCurrentPlayer(mClient);
        Log.d(Config.TAG, "Id:" + player.getPlayerId() + " name:" + player.getDisplayName() + " success:" + bundle);
        loginApplication();
    }

    private void moveNextActivity(){
        //次のactivityを実行
        Intent intent = new Intent(MainActivity.this, MissionActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Config.TAG, "suspend:" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            // There was an error with the resolution intent. Try again.
            mClient.connect();
        }
        Log.d(Config.TAG, "failed:" + connectionResult.getErrorCode());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.sprashImage));
    }
}
