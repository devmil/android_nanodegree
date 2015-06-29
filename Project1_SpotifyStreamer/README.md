# Spotify Streamer

This app gets created during the first 2 lessons in the Android Nanodegree course.

It is an app that can search artists on Spotify, list the top 10 tracks and play them (at least the 30 seconds that are allowed getting played without any credentials).

Main goal of this project is to leverage fragments to support phone and tablet layouts.

It also contains the idea of a background service performing an ongoing task (playing music) that gets controlled and visualized by an activity that binds to it.

Another key question that arises during this project is the question of data ownage. It is key to work out a strategy for what entity owns what chunk of data so that it makes most sense for the user and leads to a consistent UX.

For more information have a look at the concept paper for [Data ownership and flow](Concepts/Data ownership and flow.pdf).