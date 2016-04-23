package com.apps.richykapadia.bowlingtracker;


import org.opencv.core.Mat;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by richykapadia on 4/23/16.
 */
public class FrameAnalyzer implements Runnable {

    private Mat curr;
    private ReentrantLock lock;
    private boolean running;

    public FrameAnalyzer(){
        this.running = true;
        this.lock = new ReentrantLock();
    }

    @Override
    public void run() {

        while (running){
            lock.lock();



            lock.unlock();

        }

    }

    public void setCurr(Mat mat){
        lock.lock();
        this.curr = mat;
        lock.unlock();
    }

    public void onPause(){
        this.running = false;
    }

    public void onResume(){
        (new Thread(this)).start();
    }
}
