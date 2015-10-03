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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mClient;
    private final static int REQUEST_CODE_RESOLUTION = 1;
    private static final int START_SCREEN_DISPLAY_TIME = 1000; // Millisecond

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ImageView image = (ImageView) findViewById(R.id.sprashImage);
        //image.setImageResource(R.mipmap.main);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this, this, this);
        builder.addApi(Games.API)
                .addApi(Plus.API)
                .addScope(Games.SCOPE_GAMES)
                .addScope(Plus.SCOPE_PLUS_LOGIN);
        mClient = builder.build();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mClient.isConnected()){
            scesuleNextActivity();
        }else {
            mClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Player player = Games.Players.getCurrentPlayer(mClient);
        Log.d(Config.TAG, "Id:" + player.getPlayerId() + " name:" + player.getDisplayName() + " success:" + bundle);
        scesuleNextActivity();
    }

    private void scesuleNextActivity(){
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //次のactivityを実行
                Intent intent = new Intent(MainActivity.this, MissionActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        });
        handler.sendEmptyMessageDelayed(0, START_SCREEN_DISPLAY_TIME);
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
        //ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.sprashImage));
    }
}
