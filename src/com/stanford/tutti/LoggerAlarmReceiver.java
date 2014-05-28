package com.stanford.tutti;

import java.util.Calendar;
import java.util.Random;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class LoggerAlarmReceiver extends WakefulBroadcastReceiver {
	private AlarmManager alarmManager;
    private PendingIntent alarmIntent;


	@Override
	public void onReceive(Context context, Intent intent) {
		
		DatabaseHandler db = new DatabaseHandler(context);
		
		String serverUrl = context.getString(R.string.ec2_server);
		String logUrl = serverUrl + "/ping";
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(logUrl, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				System.out.println("Requested start playing on master, returned code: " + statusCode);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				System.out.println("statusCode: " + statusCode);
				System.out.println("error: " + error);
			}		
		});
		
	}
	
    public void setAlarm(Context context) {
    	Random random = new Random();
    	
    	alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LoggerAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
        calendar.set(Calendar.MINUTE, random.nextInt(60));
        calendar.set(Calendar.MINUTE, random.nextInt(60));
        
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        
        /*
          
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
                
        */
    }

	
	

}
