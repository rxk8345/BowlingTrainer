package com.apps.richykapadia.bowlingtrackerandroid.UI;

import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Created by richykapadia on 4/23/16.
 */
public class RegionSelector implements View.OnTouchListener{

    public enum MODE {INIT, DRAGGING, RELEASE}
    private final Point one = new Point();
    private final Point two = new Point();

    private MODE current = MODE.INIT;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println(event.getX() + " ," + event.getY());
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

}
