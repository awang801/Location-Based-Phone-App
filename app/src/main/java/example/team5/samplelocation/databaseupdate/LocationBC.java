package example.team5.samplelocation.databaseupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationBC extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LocationBC.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
		context.startService(new Intent(context, LocationService.class));;
	}
}
