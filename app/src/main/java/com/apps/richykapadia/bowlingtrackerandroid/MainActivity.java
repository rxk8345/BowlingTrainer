package com.apps.richykapadia.bowlingtrackerandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.apps.richykapadia.bowlingtrackerandroid.UI.MyCameraListener;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main-Activity";

    private CameraBridgeViewBase javaCameraView;
    private ImageView laneOverlay;
    private LoaderCallback loaderCallback;
    private MyCameraListener myCameraListener;

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Request Permission for marshmallow ++
        requestPermissionForCamera();

        //grab ui element
        javaCameraView = (CameraBridgeViewBase) findViewById(R.id.main_camera_view);
        laneOverlay = (ImageView) findViewById(R.id.lane_overlay);
        javaCameraView.setMaxFrameSize(360,480);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        myCameraListener = new MyCameraListener(javaCameraView, laneOverlay, this);
        javaCameraView.setCvCameraViewListener(myCameraListener);

        // cv loader callback (enables view when lib is loaded)
        loaderCallback = new LoaderCallback(this, javaCameraView, myCameraListener);



    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, loaderCallback);
    }


    private void requestPermissionForCamera(){
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

    }


}
