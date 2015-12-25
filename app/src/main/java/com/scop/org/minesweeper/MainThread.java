package com.scop.org.minesweeper;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Oscar on 25/11/2015.
 */
public class MainThread extends Thread {
    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;

        while (running){
            startTime = System.nanoTime();

            //try locking the canvas for pixel editing
            refreshFrame();
            if (true) break;
            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime-timeMillis;
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {}

            // statistics:
            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if(frameCount >= FPS){
                averageFPS = 1000/((totalTime/frameCount)/1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(averageFPS);
            }
        }
    }

    public synchronized void refreshFrame(){
        canvas = null;
        try {
            canvas = this.surfaceHolder.lockCanvas();
            synchronized(this.surfaceHolder){
                this.gamePanel.update();
                this.gamePanel.draw(canvas);
            }
        } catch (Exception e){
        } finally {
            if (canvas != null){
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
