package com.stanford.tutti;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Logs information about the jam such as elapsed time spent in jam, number 
 * of unique users in jam, and the max number of songs in the jam to a local
 * database. Once per day the database is serialized as JSON and sent to a
 * centralized server.
 * 
 * Data should only be logged when the phone is the master.
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
		if (!g.jam.checkMaster() && currentJamId >= 0) {
			return;
		}
		
		currentJamId = g.db.createJamInLog();
		shouldUpdateTimestamp.set(true);

		(new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						System.out.println("unable to sleep in log jam update timestamp thread");
						break;
					}
					if (shouldUpdateTimestamp.get()) {
						System.out.println("Updating timestamp for jam in logger");
						int rowsUpdated = g.db.updateJamTimestamp(currentJamId);
						if (rowsUpdated != 1) {
							System.out.println("updating timestamp for jam failed -- id: " + currentJamId);
							break;
						}

						// testing code!
						JSONObject jsonJamLog = g.db.getLogDataAsJson();
						try {
							if (jsonJamLog != null)
								System.out.println(jsonJamLog.toString(2));
						} catch (JSONException e) {
							e.printStackTrace();
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
		
		updateNumberSongs();
	}

	/*
	 * Stops updating the timestamp for the current jam.
	 */
	public void endCurrentJam() {
		shouldUpdateTimestamp.set(false);
		currentJamId = -1;
	}

	/*
	 * Updates the number of songs associated with the current jam in the log database.
	 */
	public void updateNumberSongs() {
		if (g.jam.checkMaster() && currentJamId >= 0) {
			int numRowsUpdated = g.db.updateNumSongs(currentJamId);
			if (numRowsUpdated != 1) {
				System.out.println("Error updating number of songs in the jam log table.");
			}
			else {
				System.out.println("Updated number of songs in jam logger");
			}
		}
	}

	/*
	 * Updates the number of users in the jam if the user's ip address has not been
	 * seen before.
	 */
	public void updateUsers(String ipAddr) {
		if (g.jam.checkMaster() && currentJamId >= 0) {
			if (!ipAddrSet.contains(ipAddr)) {
				ipAddrSet.add(ipAddr);
				g.db.incrementJamNumUsers(currentJamId);
			}
		}
	}

	/*
	 * Sets a recurring system alarm that will force the 
	 */
	private void setRecurringAlarm() {
		// TODO: implement this
	}

}
