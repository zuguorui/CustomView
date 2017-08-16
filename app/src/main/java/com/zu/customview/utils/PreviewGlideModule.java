package com.zu.customview.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by zu on 17-6-2.
 */

public class PreviewGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "/cache", 1000 * 1024 * 1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
