package com.stanford.tutti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Receives BOOT_COMPLETED intent when the phone is turned on and resets the 
 * logger alarm since turning the phone off disables all alarms.
 */
public class BootReceiver extends BroadcastReceiver {

	private LoggerAlarmReceiver alarm = new LoggerAlarmReceiver();

	/**
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			alarm.setAlarm(context, true);
		}
	}
}
