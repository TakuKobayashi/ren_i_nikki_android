package com.nikki.i.ren.ren_i_nikki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.co.omron.hvcw.ErrorCodes;
import jp.co.omron.hvcw.HvcwApi;
import jp.co.omron.hvcw.Int;
import jp.co.omron.hvcw.OkaoResult;
import jp.co.omron.hvcw.ResultExpression;
import jp.co.omron.hvcw.ResultFace;

public class OkaoScaner extends ContextSingletonBase{

  private ArrayList<LocationUpdateListener> mListenerQueue;
  private boolean isScanning = false;
  private HashMap<String, String> mParams;

  /** Omronから取得したAPIキー */
  private static final String API_KEY = "7Tkt9P8pFt4kwGubx768hZ3XZLJr4xpUtsaLbKwu"; // 取得したキーに置き換える
  /** アプリケーションID */
  private static final int APP_ID = 100; // 開発者向けは現在100固定

  /** Web APIのリクエストURLのベース */
  private static final String SERVICE_URL = "http://developer.hvc.omron.com/c2w";
  /** Web APIアクセストークン */
  private String accessToken;
  /** Web APIアクセストークンの有効期限 */
  private int expiresIn;
  /** 登録されているカメラ情報のリスト */
  private List<CameraInfo> cameraList = new ArrayList<CameraInfo>();
  /** カメラに接続済みかどうかのフラグ */
  private boolean isConnected = false;

  /** HVC SDK ハンドル */
  private static HvcwApi api;

  private Thread mScanThread;

  // ライブラリのロード
  static {
    System.loadLibrary("openh264");
    System.loadLibrary("ffmpeg");
    System.loadLibrary("ldpc");
    System.loadLibrary("IOTCAPIs");
    System.loadLibrary("RDTAPIs");
    System.loadLibrary("c2w");
    System.loadLibrary("HvcOi");
    System.loadLibrary("HVCW");
  }

  public void init(Context context){
    super.init(context);
    mParams = new HashMap<String, String>();
    mListenerQueue = new ArrayList<LocationUpdateListener>();
  }

  public void addUpdateListener(LocationUpdateListener listener){
    mListenerQueue.add(listener);
  }

  public void removeUpdateListener(LocationUpdateListener listener){
    mListenerQueue.remove(listener);
  }

  public void scanStart(){
    // SDKのバージョン確認
    Int major = new Int();
    Int minor = new Int();
    Int release = new Int();
    HvcwApi.getVersion(major, minor, release);

    if (api != null) {
      api.deleteHandle();
    }

    // ハンドル生成
    api = HvcwApi.createHandle();
    if (api == null) {
      Log.d(Config.TAG, "createHandle() failed.");
    }
    isScanning = true;
    //顔とる
    new Thread(new Runnable() {
      @Override
      public void run() {
        String url = SERVICE_URL + "/api/v1/login.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST,url,
          new Response.Listener<String>() {
          @Override
          public void onResponse(String s) {
            Log.d(Config.TAG, "res:" + s);
              try {
                JSONObject root = new JSONObject(s);
                JSONObject result = root.getJSONObject("result");
                String code = result.getString("code");
                String msg = result.getString("msg");
                if (msg.equals("success")) {
                  JSONObject access = root.getJSONObject("access");
                  accessToken = access.getString("token");
                  expiresIn = access.getInt("expiresIn");
                  loopScanning();
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
          }
        },
        new Response.ErrorListener(){
          @Override
          public void onErrorResponse(VolleyError error){
            Log.d(Config.TAG, "error:" + error.getMessage());
            Log.d(Config.TAG, "le:" + error.getLocalizedMessage());
          }
        }){
          @Override
          protected Map<String,String> getParams(){
            HashMap<String, String> params = new HashMap<String, String>();
            String mailAddress = "keep_slimbody@yahoo.co.jp";
            String password = "Oz5tmq2a";
            params.put("apiKey", API_KEY);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            params.put("deviceId", wifiInfo.getMacAddress());
            params.put("osType", "1");
            params.put("email", mailAddress);
            params.put("password", password);
            return params;
          }
        };;
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(postRequest);
        queue.start();
      }
    }).start();
  }

  private void connection(){
      final String fileName = context.getFilesDir() + "/network_setting.pcm";
      String ssid = "TBS-NTTW_2015_A";
      String password = "antbs2015";
    Log.d(Config.TAG, "file:" + fileName);

      // カメラを登録するためのネットワーク設定音声ファイルを作成
      int ret = api.generateDataSoundFile(fileName, ssid, password, accessToken);
    Log.d(Config.TAG, "ret:" + ret);
    Log.d(Config.TAG, "token:" + accessToken);
      if (ret == ErrorCodes.HVCW_SUCCESS) {
        // 作成出来たら再生
        Log.d(Config.TAG, "play:" + fileName);
        File networkSettingFile = new File(fileName);
        if (networkSettingFile == null) {
           return;
        }

        byte[] byteData = new byte[(int) networkSettingFile.length()];
        FileInputStream fis;
        try {
          fis = new FileInputStream(networkSettingFile);
          fis.read(byteData);
          fis.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          return;
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }

        int audioBuffSize = AudioTrack.getMinBufferSize(
                    8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                    8000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBuffSize,
                    AudioTrack.MODE_STREAM);
            audio.play();
            audio.write(byteData, 0, byteData.length);
      }
  }

  private void getCameraList() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String u = SERVICE_URL + "/api/v1/getCameraList.php";
        HttpURLConnection conn = null;
        String json = null;
        try {
          URL url = new URL(u);
          conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
          // アクセストークンが必要なリクエストの場合
          conn.setRequestProperty("Authorization", "Bearer " + accessToken);
          conn.setDoInput(true);
          conn.setDoOutput(true);

          // POST
          conn.connect();

          // レスポンス受信
          if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
              sb.append(line);
            }
            json = sb.toString();
          } else {
            Log.d(Config.TAG, "response " + conn.getResponseCode());
          }
        } catch(MalformedURLException e) {
          e.printStackTrace();
        } catch(IOException e) {
          e.printStackTrace();
        } finally {
          if(conn != null) {
            Log.d(Config.TAG, "disconnecting...");
            conn.disconnect();
          }
        }
        Log.d(Config.TAG, json);
      }
    }).start();
  }


/*
    PostMessageTask task = new PostMessageTask(new Listener() {
      public void onReceived(String json) {
        if (json != null) {
          Log.d(TAG, "json:" + json);

          try {
            JSONObject root = new JSONObject(json);
            JSONObject result = root.getJSONObject("result");
            String code = result.getString("code");
            String msg = result.getString("msg");
            addLog(String.format("response=%s(%s)", code, msg));
            if (msg.equals("success")) {
              cameraList.clear();
              ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item);
              JSONArray array = root.getJSONArray("cameraList");
              for (int i = 0; i < array.length(); ++i) {
                JSONObject obj =array.getJSONObject(i);
                CameraInfo ci = new CameraInfo();
                String id = obj.getString("cameraId");
                ci.setID(id);
                String name = obj.getString("cameraName");
                ci.setName(name);
                ci.setMacAddress(obj.getString("cameraMacAddr"));
                ci.setAppID(obj.getString("appId"));
                ci.setOwnerType(obj.getInt("ownerType"));
                ci.setOwnerEmail(obj.getString("ownerEmail"));
                cameraList.add(ci);
                adapter.add(name);
                addLog(String.format("camera[%d] name=\"%s\",id=\"%s\"", i, name, id));
              }

              // Spinnerに設定
              adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
              Spinner sp = (Spinner)findViewById(R.id.spinner2);
              sp.setAdapter(adapter);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        } else {
          Log.d(TAG, "json:null");
          addLog("error");
        }
      }
    });
    */

  private void loopScanning() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(isScanning) {
          if (!isConnected) {
            // カメラに接続
            String cameraID = "8JJXH1DERCK6UH9YS461";
            int ret1 = api.connect(cameraID, accessToken);
            Log.d(Config.TAG, "ret:" + ret1);
            Int returnStatus = new Int();
            if (ret1 == ErrorCodes.HVCW_SUCCESS) {
              isConnected = true;
              // アプリケーションIDを設定
              Log.d(Config.TAG, "setAppID:" + APP_ID);
              ret1 = api.setAppID(APP_ID, returnStatus);
            }
          } else {
            // 顔検出・顔向き推定・年齢推定・性別推定をON
            int useFunction[] = {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};
            OkaoResult result = new OkaoResult();
            Int returnStatus = new Int();
            // 実行
            int ret = api.okaoExecute(useFunction, result, returnStatus);

            StringBuilder sb = new StringBuilder();
            if (ret == ErrorCodes.HVCW_SUCCESS) {
              sb.append(String.format("errorCode=%d,returnStatus=%#x\n", ret, returnStatus.getIntValue()));
              // 検出数
              int count = result.getResultFaces().getCount();
              if (count <= 0) continue;
              ResultFace[] rf = result.getResultFaces().getResultFace();
              SharedPreferences sp = Preferences.getCommonPreferences(context);
              String googleId = sp.getString("google_id", null);
              if(googleId != null && count <= 0) continue;
              sb.append(String.format("faceCount=%d", count));
              mParams.clear();
              SharedPreferences sp = Preferences.getCommonPreferences(context);
              mParams.put("google_id", sp.getString("google_id", ""));
              for (int i = 0; i < count; ++i) {
                ResultExpression ex = rf[i].getExpression();
                int[] score = ex.getScore();
                for (int j = 0; j < score.length; j++) {
                  mParams.put("expression" + j, String.valueOf(score[j]));
                }
                Log.d(Config.TAG, "deg:" + ex.getDegree());
              }
              StringRequest postRequest = new StringRequest(Request.Method.POST, Config.ROOT_URL + "api/tv_program/capture",
                      new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                          Log.d(Config.TAG, "res:" + s);
                        }
                      },
                      new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                          Log.d(Config.TAG, error.getMessage());
                        }
                      }) {
                @Override
                protected Map<String, String> getParams() {
                  return mParams;
                }
              };
              RequestQueue queue = Volley.newRequestQueue(context);
              queue.add(postRequest);
              queue.start();
              try {
                Thread.sleep(3000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            } else {
              sb.append(String.format("errorCode=%d,returnStatus=%#x", ret, returnStatus.getIntValue()));
            }
            String msg = sb.toString();
            Log.d(Config.TAG, "msg:" + msg);
          }
        }
      }
    }).start();
  }

  public void stopScan() {
    isScanning = false;
    isConnected = false;
  }

  //デストラクタ
  @Override
  protected void finalize() throws Throwable {
    mListenerQueue.clear();
    super.finalize();
  }

  public interface LocationUpdateListener{
    public void onUpdate(Location location);
  }
}
