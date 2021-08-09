package com.zu.customview.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by zu on 17-6-2.
 */

public class PreviewGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {

        return super.isManifestParsingEnabled();
    }

    @Override
    public void applyOptions(@NonNull @android.support.annotation.NonNull Context context, @NonNull @android.support.annotation.NonNull GlideBuilder builder) {
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, "/cache", 1000 * 1024 * 1024));
    }
}
