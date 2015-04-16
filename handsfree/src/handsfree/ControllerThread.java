package handsfree;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class ControllerThread extends Thread {
    public static final boolean HEAD_ENABLED = true;
    public static final boolean SPEECH_ENABLED = true;
    private HeadGestureThread headListener;
    private SpeechThread speechListener;
    private Robot robot;
    private int zoomed;

    public ControllerThread() {
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.zoomed = 0;
    }
    
    private void goDown() {
        this.robot.keyPress(KeyEvent.VK_PAGE_DOWN);
        this.robot.keyRelease(KeyEvent.VK_PAGE_DOWN);        
    }
    
    private void goUp() {
        this.robot.keyPress(KeyEvent.VK_PAGE_UP);
        this.robot.keyRelease(KeyEvent.VK_PAGE_UP);
    }
    
    private void zoomIn() {
        if (this.zoomed != -1) { // not zoomed out
            this.robot.keyPress(KeyEvent.VK_CONTROL);
            this.robot.keyPress(KeyEvent.VK_EQUALS);
            this.robot.keyRelease(KeyEvent.VK_CONTROL);
            this.robot.keyRelease(KeyEvent.VK_EQUALS);
            this.zoomed = 1;
        } else { // is zoomed out
            this.robot.keyPress(KeyEvent.VK_CONTROL);
            this.robot.keyPress(KeyEvent.VK_0);
            this.robot.keyRelease(KeyEvent.VK_CONTROL);
            this.robot.keyRelease(KeyEvent.VK_0);
            this.zoomed = 0;
        }
    }
    
    private void zoomOut() {
        if (this.zoomed != 1) { // not zoomed in
            this.robot.keyPress(KeyEvent.VK_CONTROL);
            this.robot.keyPress(KeyEvent.VK_MINUS);
            this.robot.keyRelease(KeyEvent.VK_CONTROL);
            this.robot.keyRelease(KeyEvent.VK_MINUS);
            this.zoomed = -1;
        } else { // is zoomed out
            this.robot.keyPress(KeyEvent.VK_CONTROL);
            this.robot.keyPress(KeyEvent.VK_0);
            this.robot.keyRelease(KeyEvent.VK_CONTROL);
            this.robot.keyRelease(KeyEvent.VK_0);
            this.zoomed = 0;
        }
    }

    private void onHeadGesture(HeadGesture headGesture) {
        System.out.println("Head gesture: " + headGesture.name());
        switch (headGesture) {
        case NOD_DOWN:
            goDown();
            break;
        case NOD_UP:
            goUp();
            break;
        case NONE:
            break;
        case ZOOM_IN:
            zoomIn();
            break;
        case ZOOM_OUT:
            zoomOut();
            break;
        }
    }

    private void onSpeech(String speech) {
        System.out.println(speech);
        if (speech.contains("up")) {
            goUp();
        } else if (speech.contains("down")) {
            goDown();
        } else if (speech.contains("zoom")) {
            if (speech.contains("out")) {
                zoomOut();
            } else {
                zoomIn();
            }
        }
    }

    public void run() {
        this.headListener = new HeadGestureThread();
        if (ControllerThread.HEAD_ENABLED) {
            this.headListener.start();
        }
        this.speechListener = new SpeechThread();
        if (ControllerThread.SPEECH_ENABLED) {
            this.speechListener.start();
        }
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String speech = this.speechListener.getSpeech();
            HeadGesture headGesture = this.headListener.getGesture();
            if (!"".equals(speech)) {
                onSpeech(speech);
            }
            if (headGesture != null && headGesture != HeadGesture.NONE) {
                onHeadGesture(headGesture);
            }
        }
    }

}