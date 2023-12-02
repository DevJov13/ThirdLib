package dev.joven.fourthlib;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.mgd.mgddevtools.mgdUtil;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.json.JSONException;
import org.json.JSONObject;

public class CoreConfig extends Application {

    public static mgdUtil tools;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getBaseContext();

        tools = new mgdUtil(this);

        FacebookSdk.fullyInitialize();
        AppEventsLogger.activateApp(this);
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);

        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Pusher pusher = new Pusher("fdcb398aff6445bc7bd6", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        Channel channel = pusher.subscribe(getPackageName());

        channel.bind("my-event", event -> {
            Log.i("Pusher", "Received event with data: " + event.toString());
            Log.i(mgdUtil.AppTAG, "Notify: " + event.getData());

            try {
                JSONObject notifyMsg = new JSONObject(event.getData());

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (!notificationManager.areNotificationsEnabled()) {
                    // Notifications are disabled, guide the user to enable them
                    // You can also open the app settings page for notifications
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    showNotification("Announcement", notifyMsg.getString("message"), notifyMsg.getString("url"));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        });

    }

    private void showNotification(String title, String message, String link) {

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notiflayout);
        remoteViews.setTextViewText(R.id.notificationTitle, title);
        remoteViews.setTextViewText(R.id.notificationMessage, message);

        // Create an intent to open the link when the button is clicked
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openLinkIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.openLinkButton, pendingIntent);

        NotificationChannel channel = new NotificationChannel("my-channel", "Announcements", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my-channel")
                .setSmallIcon(R.drawable.notification)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setAutoCancel(true);

        NotificationManagerCompat notificationMg = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationMg.notify(1, builder.build());

    }

    public static mgdUtil getTools(Context context)
    {
        if(tools == null)
            tools = new mgdUtil(context);

        return tools;
    }
}
