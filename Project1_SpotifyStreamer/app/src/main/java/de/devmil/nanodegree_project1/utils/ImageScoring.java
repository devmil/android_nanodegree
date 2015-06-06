package de.devmil.nanodegree_project1.utils;

import java.util.List;

/**
 * Scores images based on a preferred size.
 * Lower values are better
 * @param <T> the image type to score
 */
public class ImageScoring<T> {

    private int preferredSizePx;
    private ImageSizeRetriever<T> sizeRetriever;

    public ImageScoring(int preferredSizePx, ImageSizeRetriever<T> sizeRetriever) {
        this.preferredSizePx = preferredSizePx;
        this.sizeRetriever = sizeRetriever;
    }

    public int score(T image) {
        //differences in width and height are scored equally
        //lower resolutions get a penalty multiplicator :)

        int diffY = sizeRetriever.getHeight(image) - preferredSizePx;
        int diffX = sizeRetriever.getWidth(image) - preferredSizePx;

        //bad bad low res image!
        if(diffY < 0)
            diffY *= 3;
        if(diffX < 0)
            diffX *= 3;

        int diff = Math.abs(diffY) + Math.abs(diffX);

        return diff;
    }

    public T getNearest(List<T> images) {
        T result = null;
        int resultScore = Integer.MAX_VALUE;

        for(T image : images) {
            int score = score(image);
            if(score < resultScore) {
                result = image;
                resultScore = score;
            }
        }

        return result;
    }

    public interface ImageSizeRetriever<T>
    {
        int getHeight(T image);
        int getWidth(T image);
    }
}
