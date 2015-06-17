package de.devmil.nanodegree_spotifystreamer.utils;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public final class GlideConfig {
    private GlideConfig() {

    }

    public static <ModelType, DataType, ResourceType, TranscodeType>
        GenericRequestBuilder<ModelType, DataType, ResourceType, TranscodeType>
    configure(
            GenericRequestBuilder<ModelType, DataType, ResourceType, TranscodeType> request) {
        return request.diskCacheStrategy(DiskCacheStrategy.ALL);
    }
}
