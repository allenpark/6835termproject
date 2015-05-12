package handsfree;

import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HandsFree extends JFrame {
    public static final boolean HEAD_ENABLED = true;
    public static final boolean SPEECH_ENABLED = true;
    private static final long serialVersionUID = 1L;
    private static ControllerThread controller;
    private static JPanel speechPanel;
    private static JLabel speechLabel;
    private static Timer speechTimer;
    private static JPanel gesturePanel;
    private static JLabel gestureLabel;
    private static Timer gestureTimer;
    private static JLabel outputLabel;
    private static Timer outputTimer;
    public static String callSpeech = "";
    public static String callGesture = "";
    public static String callActive = "";
    public static String callOutput = "";

    public HandsFree() {
        setTitle("Hands Free");
        setSize(300, 200); // default size is 0,0
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - this.getWidth();
        int y = 0;
        setLocation(x, y);
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        /*
         * speechPane = new JPanel(); getContentPane().add(speechPane); speech =
         * "default yes yes yse"; speechLabel = new JLabel(speech);
         * speechLabel.setFont(new Font("Serif", Font.PLAIN, 18));
         * speechPane.add(speechLabel); speechLabel = new JLabel(speech);
         * speechLabel.setFont(new Font("Serif", Font.PLAIN, 18));
         * speechPane.add(speechLabel); speechLabel = new JLabel(speech);
         * speechLabel.setFont(new Font("Serif", Font.PLAIN, 18));
         * speechPane.add(speechLabel);
         */
        Container contentPane = this.getContentPane();
        //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        /*speechPanel = new TextPanel(speech, 75, 100);
        contentPane.add(speechPanel);*/
        speechPanel = new JPanel();
        speechPanel.setLayout(new BoxLayout(speechPanel, BoxLayout.PAGE_AXIS));
        contentPane.add(speechPanel);
        speechLabel = new JLabel("speech");
        speechLabel.setFont(speechLabel.getFont().deriveFont(18.0f));
        speechPanel.add(speechLabel);
        setSpeech(speechLabel.getText());
        
        gesturePanel = new JPanel();
        gestureLabel = new JLabel("gesture");
        gestureLabel.setFont(gestureLabel.getFont().deriveFont(18.0f));
        speechPanel.add(gestureLabel);
        //contentPane.add(gesturePanel);
        setGesture(gestureLabel.getText());
        
        speechPanel.add(new JLabel(" "));
        outputLabel = new JLabel(" ");
        outputLabel.setFont(outputLabel.getFont().deriveFont(18.0f));
        speechPanel.add(outputLabel);
        
        //contentPane.add(new TextPanel("rawr", 75, 20));
    }

    public static String getSpeech() {
        synchronized (speechPanel) {
            return speechLabel.getText();
        }
    }

    private static void setSpeech(String s) {
        synchronized (speechPanel) {
            if (speechTimer != null) {
                speechTimer.cancel();
            }
            speechLabel.setText(s);
            speechTimer = new Timer();
            speechTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        speechLabel.setText("speech");
                        speechTimer = null;
                    }
                },
                5000
            );
        }
    }
    
    public static String getGesture() {
        synchronized (gesturePanel) {
            return gestureLabel.getText();
        }
    }
    
    private static void setGesture(String s) {
        synchronized (gesturePanel) {
            if (gestureTimer != null) {
                gestureTimer.cancel();
            }
            gestureLabel.setText(s);
            gestureTimer = new Timer();
            gestureTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        gestureLabel.setText("gesture");
                        gestureTimer = null;
                    }
                },
                5000
            );
        }
    }
    
    private static void setActive(boolean a) {
        //activeLabel.setText("Active: " + a);
        if (a) {
            speechLabel.setText("speech");
            gestureLabel.setText("gesture");
        } else {
            speechLabel.setText("Active: false");
            gestureLabel.setText("");
        }
    }
    
    private static void setOutput(String s) {
        if (outputTimer != null) {
            outputTimer.cancel();
        }
        outputLabel.setText(s);
        outputTimer = new Timer();
        outputTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    outputLabel.setText(" ");
                    outputTimer = null;
                }
            },
            5000
        );
    }

    public static void main(String[] args) {
        controller = new ControllerThread();
        controller.start();
        JFrame f = new HandsFree();
        f.setVisible(true);
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            synchronized (HandsFree.callSpeech) {
                if (!"".equals(HandsFree.callSpeech)) {
                    HandsFree.setSpeech(HandsFree.callSpeech);
                    HandsFree.callSpeech = "";
                }
            }
            synchronized (HandsFree.callGesture) {
                if (!"".equals(HandsFree.callGesture)) {
                    HandsFree.setGesture(HandsFree.callGesture);
                    HandsFree.callGesture = "";
                }
            }
            synchronized (HandsFree.callActive) {
                if (!"".equals(HandsFree.callActive)) {
                    HandsFree.setActive(Boolean.parseBoolean(HandsFree.callActive));
                    HandsFree.callActive = "";
                }
            }
            synchronized (HandsFree.callOutput) {
                if (!"".equals(HandsFree.callOutput)) {
                    HandsFree.setOutput(HandsFree.callOutput);
                    HandsFree.callOutput = "";
                }
            }
        }
    }
}

class TextPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public String text;
    public int x;
    public int y;
    
    public TextPanel(String s, int x, int y) {
        this.text = s;
        this.x = x;
        this.y = y;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Font f = new Font("Serif", Font.PLAIN, 18);
        g.setFont(f);
        g.drawString(this.text, this.x, this.y);
    }
}