package com.stanford.tutti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	LoggerAlarmReceiver alarm = new LoggerAlarmReceiver();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			alarm.setAlarm(context, true);
		}
	}
}
