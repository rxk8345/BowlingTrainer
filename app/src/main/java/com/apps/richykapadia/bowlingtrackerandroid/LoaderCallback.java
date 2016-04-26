package com.apps.richykapadia.bowlingtrackerandroid;

import android.content.Context;
import android.util.Log;

import com.apps.richykapadia.bowlingtrackerandroid.UI.MyCameraListener;
import com.apps.richykapadia.bowlingtrackerandroid.UI.RegionSelector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;

/**
 * Created by richykapadia on 9/20/15.
 */
public class LoaderCallback extends BaseLoaderCallback {

    private CameraBridgeViewBase view;
    private MyCameraListener myCameraListener;

    public LoaderCallback(Context AppContext, CameraBridgeViewBase view, MyCameraListener myCameraListener) {
        super(AppContext);
        this.view = view;
        this.myCameraListener = myCameraListener;
    }

    @Override
    public void onManagerConnected(int status) {
        super.onManagerConnected(status);
        switch(status){
            case LoaderCallback.SUCCESS:
            {
                Log.d("LoaderCallback", "Sucessfully Connected!");
                view.enableView();
                myCameraListener.initialize();
                break;
            }default:
            {
                super.onManagerConnected(status);
                break;
            }
        }

    }

    @Override
    public void onPackageInstall(int operation, InstallCallbackInterface callback) {
        super.onPackageInstall(operation, callback);
    }
}