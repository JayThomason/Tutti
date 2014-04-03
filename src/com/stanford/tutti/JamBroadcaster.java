package com.stanford.tutti;


/*
 * Periodically broadcasts the jam information using UDP. 
 * 
 * Should only be performed by the master phone.
 */
public class JamBroadcaster {
	private NsdHelper mNsdHelper;
	private final int port = 1234;

	public JamBroadcaster() {
		// eventually want to put in a handler to notify UI thread when users join jam
		mNsdHelper = new NsdHelper(Globals.getAppContext(), null, true);
		mNsdHelper.initializeNsd();
        mNsdHelper.registerService(port); // repetetive to unregister if not properly closed
        mNsdHelper.tearDown();
		mNsdHelper.initializeNsd();
        mNsdHelper.registerService(port);
	}
	
    public void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.tearDown();
        }
    }
    
    public void onResume() {
        if (mNsdHelper != null) {
            mNsdHelper.registerService(port);
        }
    }
    
    public void onDestroy() {
        mNsdHelper.tearDown();
    }

}




/*     // UDP CODE -- DOESNT WORK ON NEXUS 5
	private InetAddress addr = null;
	private ScheduledExecutorService scheduler = null;
	private static final int BCAST_PORT = 8779;
	DatagramSocket socket = null;
	WifiManager wifi = null;
	WifiManager.MulticastLock lock = null;

	public JamBroadcaster() {
		wifi = (WifiManager) 
				Globals.getAppContext().getSystemService(Context.WIFI_SERVICE);
//		lock = wifi.createMulticastLock("Log_Tag");
//	    lock.acquire();
		try {
			addr = getBroadcastAddress();
			System.out.println(addr);
			socket = new DatagramSocket(BCAST_PORT);
			socket.setBroadcast(true);
			scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(new Runnable() {
				public void run() {
					sendUDPBroadcast();
					System.out.println("SENT UDP BROADCAST"); // for testing only
				}
			}, 0, 1, TimeUnit.SECONDS);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

 	public void stop() {
		scheduler.shutdownNow();
		socket.close();
//		lock.release();
	}


	private InetAddress getBroadcastAddress() throws IOException {
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	private void sendUDPBroadcast() {
		try {
			System.out.println("JAM BROADCAST TESTER");
			String data = "TESTING UDP BROADCAST";
			DatagramPacket packet = new DatagramPacket(data.getBytes(), 
					data.length(), addr, BCAST_PORT);
			socket.send(packet);
			System.out.println("JAM BROADCAST TESTER END");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 */
