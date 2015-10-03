package com.nikki.i.ren.ren_i_nikki;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ApplicationHelper {

  public static void releaseImageView(ImageView imageView){
    if (imageView != null) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable)(imageView.getDrawable());
      if (bitmapDrawable != null) {
        bitmapDrawable.setCallback(null);
      }
      imageView.setImageBitmap(null);
    }
  }

  //WebViewを使用したときのメモリリーク対策
  public static void releaseWebView(WebView webview){
    webview.stopLoading();
    webview.setWebChromeClient(null);
    webview.setWebViewClient(null);
    webview.destroy();
    webview = null;
  }

  public static String makeUrlParams(Bundle params){
    Set<String> keys = params.keySet();
    ArrayList<String> paramList = new ArrayList<String>();
    for (String key : keys) {
      paramList.add(key + "=" + params.get(key).toString());
    }
    return ApplicationHelper.join(paramList, "&");
  }

  public static String makeUrlParams(Map<String, Object> params){
    Set<String> keys = params.keySet();
    ArrayList<String> paramList = new ArrayList<String>();
    for(Entry<String, Object> e : params.entrySet()) {
      paramList.add(e.getKey() + "=" + e.getValue().toString());
    }
    return ApplicationHelper.join(paramList, "&");
  }

  public static String join(String[] list, String with) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < list.length; i++) {
    if (i != 0) { buf.append(with);}
      buf.append(list[i]);
    }
    return buf.toString();
  }

  public static String join(ArrayList<String> list, String with) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < list.size(); i++) {
      if (i != 0) { buf.append(with);}
      buf.append(list.get(i));
    }
    return buf.toString();
  }

  //Toastの表示
  public static void showToast(Context con, String message) {
    Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
    toast.show();
  }
}
