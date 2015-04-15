package handsfree;

import java.net.*;
import java.lang.Thread;

public class FaceTrackNoIRListener extends Thread {
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
				double[] out = new double[7];
				int count = 0;
				for (int d = offset; d < offset + receivePacket.getLength(); d += 8) {
					long accum = 0;
					int shiftBy = 0;
					for (int i = d; i < d + 8; i++) {
						accum |= ((long) (receiveData[i] & 0xff)) << shiftBy;
						shiftBy += 8;
					}
					out[count] = Double.longBitsToDouble(accum);
					count++;
				}
				synchronized (HandsFree.data) {
					for (int i = 0; i < out.length; i++) {
						HandsFree.data[i] = out[i];
					}
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
}