package de.devmil.nanodegree_spotifystreamer.utils;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

public class ImageScoringTest extends AndroidTestCase {

    class SomeImageClass {
        SomeImageClass(int height, int width) {
            this.height = height;
            this.width = width;
        }
        int height;
        int width;
    }

    static final int[] TEST_SIZES = { 600, 200, 700, 400, 500, 100, 300, 800 };

    List<SomeImageClass> getTestImages() {
        List<SomeImageClass> result = new ArrayList<>();
        for(int height : TEST_SIZES) {
            for(int width : TEST_SIZES) {
                result.add(new SomeImageClass(height, width));
            }
        }
        return result;
    }

    List<SomeImageClass> getTestImagesOneHeight() {
        List<SomeImageClass> result = new ArrayList<>();
        int height = TEST_SIZES[1];
        for(int width : TEST_SIZES) {
            result.add(new SomeImageClass(height, width));
        }
        return result;
    }

    ImageScoring<SomeImageClass> getScoring(int preferredSize) {
        return new ImageScoring<>(preferredSize, new ImageScoring.ImageSizeRetriever<SomeImageClass>() {
            @Override
            public int getHeight(SomeImageClass image) {
                return image.height;
            }

            @Override
            public int getWidth(SomeImageClass image) {
                return image.width;
            }
        });
    }

    public void testExactMatch() {
        List<SomeImageClass> testImages = getTestImages();
        ImageScoring<SomeImageClass> scoring = getScoring(300);

        SomeImageClass hit = scoring.getNearest(testImages);

        assertEquals(300, hit.height);
        assertEquals(300, hit.width);
    }

    public void testNearestImageIsBigger() {
        List<SomeImageClass> testImages = getTestImages();
        ImageScoring<SomeImageClass> scoring = getScoring(550);

        SomeImageClass hit = scoring.getNearest(testImages);

        assertEquals(600, hit.height);
        assertEquals(600, hit.width);
    }

    public void testNearestImageIsLowerIfNoAlternative() {
        List<SomeImageClass> testImages = getTestImages();
        ImageScoring<SomeImageClass> scoring = getScoring(850);

        SomeImageClass hit = scoring.getNearest(testImages);

        assertEquals(800, hit.height);
        assertEquals(800, hit.width);
    }

    public void testNearestImageOneSideNotMatching() {
        List<SomeImageClass> testImages = getTestImagesOneHeight();
        ImageScoring<SomeImageClass> scoring = getScoring(400);

        SomeImageClass hit = scoring.getNearest(testImages);

        assertEquals(200, hit.height);
        assertEquals(400, hit.width);
    }
}
