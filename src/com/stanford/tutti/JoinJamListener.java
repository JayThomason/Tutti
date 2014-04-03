package com.stanford.tutti;

public class JoinJamListener extends Thread {
	
	private NsdHelper mNsdHelper;

	public void run() {
	//	mNsdHelper = new NsdHelper(Globals.getAppContext());
	//	mNsdHelper.initializeNsd();
     //   mNsdHelper.discoverServices();
	}
	
}
	/*
	private static final int BCAST_PORT = 8779;

	public void run() {
		DatagramSocket socket = null;
//		WifiManager wifi = (WifiManager)Globals.getAppContext().getSystemService( Context.WIFI_SERVICE );
//		WifiManager.MulticastLock lock = wifi.createMulticastLock("Log_Tag");
//	    lock.acquire();
		while (true) {
			try {
				System.out.println("listening for BROADCASTS");
				socket = new DatagramSocket(BCAST_PORT);
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String broadcastMessage = new String(buf);
				System.out.println("\n\nBroadcastMessageReceived: " 
						+ broadcastMessage + "\n\n");
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		if (socket != null)
			socket.close();
//		lock.release();
	}
}
*/
