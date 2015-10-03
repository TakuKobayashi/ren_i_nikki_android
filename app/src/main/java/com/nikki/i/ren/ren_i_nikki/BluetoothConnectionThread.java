package com.nikki.i.ren.ren_i_nikki;

import java.util.ArrayList;

import omron.HVC.HVC;
import omron.HVC.HVCBleCallback;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

public class BluetoothConnectionThread extends Thread {

	private HVC_BLE hvcBle = null;
	private HVC_RES hvcRes = null;
	private BluetoothDevice connectionDevice;
	private boolean isAlive;
	private boolean isStart;
	private Context mContext;

	public BluetoothConnectionThread(Context context){
		mContext = context;
		hvcRes = new HVC_RES();
		hvcBle = new HVC_BLE();
		isAlive = true;
		hvcBle.setCallBack(hvcCallback);

	}

	public void setDevice(BluetoothDevice device){
		isStart = true;
		connectionDevice = device;
	}

	public void setStart(boolean isStart){
		this.isStart = isStart;
	}

    @Override
    public void run(){
        while (isAlive) {
            if ( (connectionDevice == null) || !isStart ) {
                continue;
            }

            hvcBle.connect(mContext.getApplicationContext(), connectionDevice);
            wait(15);

            HVC_PRM hvcPrm = new HVC_PRM();
            hvcPrm.cameraAngle = HVC_PRM.HVC_CAMERA_ANGLE.HVC_CAMERA_ANGLE_0;
            hvcBle.setParam(hvcPrm);
            wait(15);

            while (isStart) {
                int nUseFunc = HVC.HVC_ACTIV_BODY_DETECTION |
                               HVC.HVC_ACTIV_HAND_DETECTION |
                               HVC.HVC_ACTIV_FACE_DETECTION |
                               HVC.HVC_ACTIV_FACE_DIRECTION |
                               HVC.HVC_ACTIV_AGE_ESTIMATION |
                               HVC.HVC_ACTIV_GENDER_ESTIMATION |
                               HVC.HVC_ACTIV_GAZE_ESTIMATION |
                               HVC.HVC_ACTIV_BLINK_ESTIMATION |
                               HVC.HVC_ACTIV_EXPRESSION_ESTIMATION;
                hvcBle.execute(nUseFunc, hvcRes);
                wait(30);
            }
            hvcBle.disconnect();
        }
        isStart = false;
    }

    public void setAlive(boolean alive){
      isAlive = alive;
    }

	public void wait(int nWaitCount)
    {
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nWaitCount--;
        } while ( nWaitCount > 0 );
    }

	private HVCBleCallback hvcCallback = new HVCBleCallback() {
		@Override
		public void onConnected() {
			Log.d(Config.TAG, "connected");
		}
		@Override
		public void onDisconnected() {
			Log.d(Config.TAG, "disconnected");
		}

        @Override
        public void onPostSetParam(int nRet, byte outStatus) {
            // Show toast
        	Log.d(Config.TAG, "Set parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus));
        }

        @Override
        public void onPostGetParam(int nRet, byte outStatus) {
            // Show toast
        	Log.d(Config.TAG, "Get parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus));
        }

        @Override
        public void onPostExecute(int nRet, byte outStatus) {
            if ( nRet != HVC.HVC_NORMAL || outStatus != 0 ) {
                Log.d(Config.TAG, "Execute : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus));
            } else {
                String str = "Body Detect = " + String.format("%d\n", hvcRes.body.size());
                for (DetectionResult bodyResult : hvcRes.body) {
                    int size = bodyResult.size;
                    int posX = bodyResult.posX;
                    int posY = bodyResult.posY;
                    int conf = bodyResult.confidence;
                    str += String.format("  [Body Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                }
                str += "Hand Detect = " + String.format("%d\n", hvcRes.hand.size());
                for (DetectionResult handResult : hvcRes.hand) {
                    int size = handResult.size;
                    int posX = handResult.posX;
                    int posY = handResult.posY;
                    int conf = handResult.confidence;
                    str += String.format("  [Hand Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                }
                str += "Face Detect = " + String.format("%d\n", hvcRes.face.size());
                for (FaceResult faceResult : hvcRes.face) {
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_FACE_DETECTION) != 0 ) {
                        int size = faceResult.size;
                        int posX = faceResult.posX;
                        int posY = faceResult.posY;
                        int conf = faceResult.confidence;
                        str += String.format("  [Face Detection] : size = %d, x = %d, y = %d, conf = %d\n", size, posX, posY, conf);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0 ) {
                        str += String.format("  [Face Direction] : yaw = %d, pitch = %d, roll = %d, conf = %d\n", 
                                                    faceResult.dir.yaw, faceResult.dir.pitch, faceResult.dir.roll, faceResult.dir.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0 ) {
                        str += String.format("  [Age Estimation] : age = %d, conf = %d\n", 
                                                    faceResult.age.age, faceResult.age.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0 ) {
                        str += String.format("  [Gender Estimation] : gender = %s, confidence = %d\n", 
                                                    faceResult.gen.gender == HVC.HVC_GEN_MALE ? "Male" : "Female", faceResult.gen.confidence);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0 ) {
                        str += String.format("  [Gaze Estimation] : LR = %d, UD = %d\n", 
                                                    faceResult.gaze.gazeLR, faceResult.gaze.gazeUD);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0 ) {
                        str += String.format("  [Blink Estimation] : ratioL = %d, ratioR = %d\n", 
                                                    faceResult.blink.ratioL, faceResult.blink.ratioR);
                    }
                    if ( (hvcRes.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0 ) {
                        str += String.format("  [Expression Estimation] : expression = %s, score = %d, degree = %d\n", 
                                                    faceResult.exp.expression == HVC.HVC_EX_NEUTRAL ? "Neutral" :
                                                    faceResult.exp.expression == HVC.HVC_EX_HAPPINESS ? "Happiness" :
                                                    faceResult.exp.expression == HVC.HVC_EX_SURPRISE ? "Surprise" :
                                                    faceResult.exp.expression == HVC.HVC_EX_ANGER ? "Anger" :
                                                    faceResult.exp.expression == HVC.HVC_EX_SADNESS ? "Sadness" : "" ,
                                                    faceResult.exp.score, faceResult.exp.degree);
                    }
                }
                Log.d(Config.TAG, str);
                /*
            	Intent intent = new Intent();
            	JSONObject o = new JSONOject(str);
            	intent.putString("hogehoge", o.toString());
            	
            	mContext.sendBroadcast(intent);
            	*/
            }
        }
    };

}
