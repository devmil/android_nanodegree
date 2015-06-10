package de.devmil.nanodegree_spotifystreamer.event;

public class PlaybackNavigationOptionsChangedEvent {
    private int currentIndex;
    private boolean canNavigatePrev;
    private boolean canNavigateNext;

    public PlaybackNavigationOptionsChangedEvent(int currentIndex, boolean canNavigatePrev, boolean canNavigateNext) {
        this.currentIndex = currentIndex;
        this.canNavigatePrev = canNavigatePrev;
        this.canNavigateNext = canNavigateNext;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isCanNavigatePrev() {
        return canNavigatePrev;
    }

    public boolean isCanNavigateNext() {
        return canNavigateNext;
    }
}
