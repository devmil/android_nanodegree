package de.devmil.nanodegree_spotifystreamer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import de.devmil.nanodegree_spotifystreamer.R;

/**
 * Helper class for showing and canceling player
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class PlayerNotification {
    /**
     * The unique identifier for this type of notification_big.
     */
    private static final String NOTIFICATION_TAG = "Player";

    public static final int NOTIFICATION_ID_STICKY = 1000;

    private static Notification currentNotification = null;

    public static Notification notify(Context context, String artistName, String trackName, Bitmap albumArtBitmap, boolean currentlyPlaying, Intent launchPlayerIntent) {
        RemoteViews notificationViewUpdatesSmall =
                createNotificationViewUpdatesSmall(
                        context,
                        artistName,
                        trackName,
                        albumArtBitmap,
                        currentlyPlaying,
                        R.layout.notification_small);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                //.setOngoing(true)
                .setLocalOnly(true)
                .setDeleteIntent(createServiceCommandIntent(context, MediaPlayService.COMMAND_STOP))

                        // Set required fields, including the small icon, the
                        // notification_big title, and text.
                .setSmallIcon(R.drawable.notification_player)
                .setContentTitle(artistName)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        // Set ticker text (preview) information for this notification_big.
                .setTicker(artistName + " - " + trackName)

                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                launchPlayerIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        // don't Automatically dismiss the notification_big when it is touched.
                .setAutoCancel(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification newNotification = builder.build();

        if(currentNotification != null) {
            newNotification.when = currentNotification.when;
        }

        newNotification.contentView = notificationViewUpdatesSmall;

        currentNotification = newNotification;

        return notify(context, newNotification);
    }

    private static RemoteViews createNotificationViewUpdatesSmall(Context context, String artistName, String trackName, Bitmap albumArtBitmap, boolean currentlyPlaying, int layout) {
        RemoteViews viewUpdate = new RemoteViews(context.getPackageName(), layout);

        viewUpdate.setImageViewBitmap(R.id.notification_small_img_album, albumArtBitmap);
        viewUpdate.setTextViewText(R.id.notification_small_txt_artist, artistName);
        viewUpdate.setTextViewText(R.id.notification_small_txt_track, trackName);

//        viewUpdate.setImageViewResource(R.id.notification_small_btn_stop, android.R.drawable.ic_menu_close_clear_cancel);
        viewUpdate.setImageViewResource(R.id.notification_small_btn_prev, android.R.drawable.ic_media_previous);
        viewUpdate.setImageViewResource(R.id.notification_small_btn_play_pause, currentlyPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        viewUpdate.setImageViewResource(R.id.notification_small_btn_next, android.R.drawable.ic_media_next);

//        viewUpdate.setOnClickPendingIntent(R.id.notification_small_btn_stop, createServiceCommandIntent(context, MediaPlayService.COMMAND_STOP));
        viewUpdate.setOnClickPendingIntent(R.id.notification_small_btn_prev, createServiceCommandIntent(context, MediaPlayService.COMMAND_PREV));
        viewUpdate.setOnClickPendingIntent(R.id.notification_small_btn_play_pause, createServiceCommandIntent(context, currentlyPlaying ? MediaPlayService.COMMAND_PAUSE : MediaPlayService.COMMAND_PLAY));
        viewUpdate.setOnClickPendingIntent(R.id.notification_small_btn_next, createServiceCommandIntent(context, MediaPlayService.COMMAND_NEXT));

        return viewUpdate;
    }

    private static PendingIntent createServiceCommandIntent(Context context, @MediaPlayService.Command int command) {
        return PendingIntent.getService(
                context,
                0,
                MediaPlayService.createCommandIntent(
                        context,
                        command
                ),
                PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static Notification notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID_STICKY, notification);
        } else {
            nm.notify(NOTIFICATION_ID_STICKY, notification);
        }
        return notification;
    }

    /**
     * Cancels any notifications of this type previously shown using
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, NOTIFICATION_ID_STICKY);
        } else {
            nm.cancel(NOTIFICATION_ID_STICKY);
        }
        currentNotification = null;
    }
}
