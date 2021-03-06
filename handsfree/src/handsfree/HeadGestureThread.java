package handsfree;

import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;

public class HeadGestureThread extends Thread {
    private LinkedList<double[]> data;
    private HeadGesture gesture;

    public HeadGestureThread() {
        this.data = new LinkedList<double[]>();
        this.gesture = HeadGesture.NONE;
    }

    public void run() {
        try {
            @SuppressWarnings("resource")
            DatagramSocket serverSocket = new DatagramSocket(5550);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                int offset = receivePacket.getOffset();
                double[] readData = new double[6];
                for (int count = 0; count < readData.length; count++) {
                    int d = offset + count * 8;
                    long accum = 0;
                    int shiftBy = 0;
                    for (int i = d; i < d + 8; i++) {
                        accum |= ((long) (receiveData[i] & 0xff)) << shiftBy;
                        shiftBy += 8;
                    }
                    readData[count] = Double.longBitsToDouble(accum);
                }
                synchronized (this.gesture) {
                    this.data.push(readData);
                    //System.out.println(this.data.getFirst()[3]);
                    if (this.data.size() > 100) {
                        this.data.removeLast();
                    }
                    this.processGesture();
                }
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData,
                        sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // Synchronized on this.gesture
    private void processGesture() {
        if (this.data.size() < 50) {
            return;
        }
        Iterator<double[]> it;
        int i;

        // Testing for nods
        int pitch = 4;
        double min = this.data.getFirst()[pitch];
        int minLoc = 0;
        double max = this.data.getFirst()[pitch];
        int maxLoc = 0;
        it = this.data.listIterator(0);
        i = 0;
        while (it.hasNext()) {
            double[] x = it.next();
            if (x[pitch] < min) {
                min = x[pitch];
                minLoc = i;
            }
            if (x[pitch] > max) {
                max = x[pitch];
                maxLoc = i;
            }
            i++;
        }
        boolean lowerHighExists = false;
        boolean upperHighExists = false;
        boolean lowerLowExists = false;
        boolean upperLowExists = false;
        it = this.data.listIterator(0);
        i = 0;
        while (it.hasNext() && !(lowerHighExists && upperHighExists)
                && !(lowerLowExists && upperLowExists)) {
            double[] x = it.next();
            if (x[pitch] > min + 15) {
                if (i < minLoc) {
                    lowerHighExists = true;
                } else if (i > minLoc) {
                    upperHighExists = true;
                }
            }
            if (x[pitch] < max - 15) {
                if (i < maxLoc) {
                    lowerLowExists = true;
                } else if (i > maxLoc) {
                    upperLowExists = true;
                }
            }
            i++;
        }
        if (lowerHighExists && upperHighExists) {
            this.gesture = HeadGesture.NOD_DOWN;
            this.data = new LinkedList<double[]>();
            return;
        } else if (lowerLowExists && upperLowExists) {
            this.gesture = HeadGesture.NOD_UP;
            this.data = new LinkedList<double[]>();
            return;
        }

        // Testing for zooms
        it = this.data.listIterator(0);
        i = 0;
        int z = 2;
        final double ZOOM_THRESHOLD = 10;
        double initialZ = this.data.getFirst()[z];
        double currentZ = initialZ;
        while (it.hasNext()) {
            double[] x = it.next();
            currentZ = x[z];
            if (i > 30 || Math.abs(currentZ - initialZ) > ZOOM_THRESHOLD) {
                break;
            }
            i++;
        }
        if (currentZ - initialZ > ZOOM_THRESHOLD) {
            this.gesture = HeadGesture.ZOOM_IN;
            this.data = new LinkedList<double[]>();
            return;
        } else if (currentZ - initialZ < -ZOOM_THRESHOLD) {
            this.gesture = HeadGesture.ZOOM_OUT;
            this.data = new LinkedList<double[]>();
            return;
        }
    }

    public void printData() {
        if (this.data.size() == 0) {
            return;
        }
        String out = "";
        double[] last = this.data.getLast();
        for (int i = 0; i < last.length; i++) {
            out += String.format("%3.1f ", last[i]);
        }
        System.out.println(out);
    }
    
    public boolean hasData() {
        return this.data.size() > 0;
    }
    
    public double[] lastData() {
        return this.data.getFirst();
    }

    public HeadGesture getGesture() {
        synchronized (this.gesture) {
            HeadGesture r = this.gesture;
            this.gesture = HeadGesture.NONE;
            return r;
        }
    }
}