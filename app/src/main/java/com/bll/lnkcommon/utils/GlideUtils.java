package com.bll.lnkcommon.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class GlideUtils {


    public static final void setImageUrl(Context mContext,int resId, ImageView imageView){
        Glide.with(mContext)
                .load(resId)
                .into(imageView);
    }

    public static final void setImageUrl(Context mContext,String url, ImageView imageView){
        Glide.with(mContext)
                .load(url)
                .into(imageView);
    }

    public static final void setImageRoundUrl(Context mContext,String url, ImageView imageView,int round){
        RequestOptions requestOptions=new RequestOptions();
        requestOptions.transform(new RoundedCorners(round));

        Glide.with(mContext)
                .load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    public static final void setImageNoCacheUrl(Context mContext,String url, ImageView imageView){

        RequestOptions requestOptions=new RequestOptions();
        requestOptions.fitCenter();
        requestOptions.skipMemoryCache(true);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);

        Glide.with(mContext)
                .load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void setImageCacheUrl(Context mContext, String url, ImageView imageView){

        RequestOptions requestOptions=new RequestOptions();
        requestOptions.fitCenter();

        CustomTarget<Drawable> object=new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                imageView.setBackground(resource);
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        };
        Glide.with(mContext)
                .load(url)
                .apply(requestOptions)
                .into(object);
    }

    public static final Bitmap getBitmap(Context mContext,String url){
        Bitmap bitmap=null;
        try {
            bitmap=Glide.with(mContext)
                    .asBitmap()
                    .load(url)
                    .into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL)
                    .get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
