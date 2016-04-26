package com.apps.richykapadia.bowlingtrackerandroid.UI;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

/**
 * Created by richykapadia on 4/23/16.
 */
public class RegionSelector implements View.OnTouchListener{

    private static final String TAG = "Region";

    public enum MODE {INIT, DRAGGING, RELEASE}
    private final Point one = new Point();
    private final Point two = new Point();

    private Size imgSize;
    private Size screenSize;

    private MODE current = MODE.INIT;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) ((event.getX() * screenSize.width) / imgSize.width);
        int y = (int) ((event.getY() * screenSize.height) / imgSize.height);

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if (current == MODE.INIT) {
            if (MotionEvent.ACTION_DOWN == event.getActionMasked()) {
                double[] data = {event.getX(), event.getY()};
                one.set(data);
                two.set(data);
                current = MODE.DRAGGING;
            }
        } else if (current == MODE.DRAGGING) {
            double[] data = {event.getX(), event.getY()};
            if (MotionEvent.ACTION_MOVE == event.getActionMasked()) {
                two.set(data);
            } else if (MotionEvent.ACTION_UP == event.getActionMasked()) {
                two.set(data);
                current = MODE.RELEASE;
            }
        }
        return true;
    }

    public MODE getCurr(){
        return this.current;
    }

    public Point getOne(){
        return one;
    }

    public Point getTwo(){
        return two;
    }

    public void reset(){
        double[] zeroes = {0.0, 0.0};
        this.current = MODE.INIT;
        this.one.set(zeroes);
        this.two.set(zeroes);
    }

    public void setImgSize(Size s){
        this.imgSize = s;
    }

    public void setScreenSize(int w, int h){
        this.screenSize = new Size(w,h);
    }

}
