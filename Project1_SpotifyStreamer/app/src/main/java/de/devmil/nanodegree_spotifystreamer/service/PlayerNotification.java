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

import de.devmil.nanodegree_spotifystreamer.PlayerActivity;
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
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Player";

    public static final int NOTIFICATION_ID_STICKY = 1000;

    private static Notification currentNotification = null;

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p/>
     * the notification.
     * <p/>
     * presentation of player notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    public static Notification notify(final Context context,
                              final String artistName,
                              final String trackName,
                              final Bitmap albumArtBitmap,
                              boolean currentlyPlaying) {

        Intent launchPlayerIntent = PlayerActivity.createLaunchIntent(context);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                //.setOngoing(true)
                .setLocalOnly(true)
                .setDeleteIntent(
                        PendingIntent.getService(
                                context,
                                0,
                                MediaPlayService.createCommandIntent(
                                        context,
                                        MediaPlayService.COMMAND_STOP
                                ),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        )
                )

                        // Set required fields, including the small icon, the
                        // notification title, and text.
                .setSmallIcon(R.drawable.notification_player)
                .setContentTitle(artistName)
                .setContentText(trackName)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        // Provide a large icon, shown with the notification in the
                        // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(albumArtBitmap)

                        // Set ticker text (preview) information for this notification.
                .setTicker(artistName + " - " + trackName)

                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                launchPlayerIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT))

                        // Show an expanded photo on devices running Android 4.1 or
                        // later.
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(albumArtBitmap)
                        .setBigContentTitle(artistName + " - " + trackName))

                .addAction(
                        android.R.drawable.ic_media_previous,
                        "",
                        PendingIntent.getService(
                                context,
                                0,
                                MediaPlayService.createCommandIntent(
                                        context,
                                        MediaPlayService.COMMAND_PREV
                                ),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        )
                )
                .addAction(
                        currentlyPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        "",
                        PendingIntent.getService(
                                context,
                                0,
                                MediaPlayService.createCommandIntent(
                                        context,
                                        currentlyPlaying ? MediaPlayService.COMMAND_PAUSE : MediaPlayService.COMMAND_PLAY
                                ),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        )
                )
                .addAction(
                        android.R.drawable.ic_media_next,
                        "",
                        PendingIntent.getService(
                                context,
                                0,
                                MediaPlayService.createCommandIntent(
                                        context,
                                        MediaPlayService.COMMAND_NEXT
                                ),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        )
                )

                        // don't Automatically dismiss the notification when it is touched.
                .setAutoCancel(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification newNotification = builder.build();

        if(currentNotification != null) {
            newNotification.when = currentNotification.when;
        }

        currentNotification = newNotification;

        return notify(context, newNotification);
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
