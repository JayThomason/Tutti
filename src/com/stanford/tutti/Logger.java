package com.stanford.tutti;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Logs information about the jam such as elapsed time spent in jam, number 
 * of unique users in jam, and the max number of songs in the jam to a local
 * database. Once per day the database is serialized as JSON and sent to a
 * centralized server.
 */
public class Logger {
	private long currentJamId;
	private Globals g;
	private AtomicBoolean shouldUpdateTimestamp;
	private Set<String> ipAddrSet;
	
	public Logger(Globals g) {
		this.g = g;
		shouldUpdateTimestamp = new AtomicBoolean(false);
		ipAddrSet = new HashSet<String>();
	}
	
	/*
	 * Creates a new jam in the log database and creates a thread to update its latest
	 * timestamp once every 10 seconds.
	 */
	public void startNewJam() {
		currentJamId = g.db.createJamInLog();
		shouldUpdateTimestamp.set(true);
		// thread updates the latest_timestamp field for the current jam in the log table
		// once every ten seconds
		(new Thread() {
			public void run() {
				while (true) {
					if (shouldUpdateTimestamp.get()) {
						int rowsUpdated = g.db.updateJamTimestamp(currentJamId);
						if (rowsUpdated != 1) {
							System.out.println("updating timestamp for jam failed -- id: " + currentJamId);
							break;
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							System.out.println("unable to sleep in log jam update timestamp thread");
							break;
						}
					}
					else {
						System.out.println("Stopping updating timestamp for jam log");
						break;
					}
				}
			}
		}).start();
		
		System.out.println("Created jam in log table: " + currentJamId);
	}
	
	/*
	 * Stops updating the timestamp for the current jam.
	 */
	public void endCurrentJam() {
		shouldUpdateTimestamp.set(false);
	}
	
	/*
	 * Updates the number of songs associated with the current jam in the log database.
	 */
	public void updateNumberSongs() {
		int numRowsUpdated = g.db.updateNumSongs(currentJamId);
		if (numRowsUpdated != 1) {
			System.out.println("Error updating number of songs in the jam log table.");
		}
	}
	
	/*
	 * Updates the number of users in the jam if the user's ip address has not been
	 * seen before.
	 */
	public void updateUsers(String ipAddr) {
		if (!ipAddrSet.contains(ipAddr)) {
			ipAddrSet.add(ipAddr);
			g.db.incrementJamNumUsers(currentJamId);
		}
	}

}
