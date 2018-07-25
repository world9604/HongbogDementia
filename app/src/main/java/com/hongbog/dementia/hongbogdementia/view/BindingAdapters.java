package com.hongbog.dementia.hongbogdementia.view;


import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.hongbog.dementia.hongbogdementia.R;

import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.HEIGHT;
import static com.hongbog.dementia.hongbogdementia.model.TensorFlowClassifier.WIDTH;


public class BindingAdapters {

    @BindingAdapter(value = {"setImgUrl"})
    public static void setImgUrl(final ImageView imageView, final Bitmap bitmap) {

        if (bitmap == null || "".equals(bitmap)) {
            imageView.setImageResource(R.mipmap.no_image);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }


    public interface OnGetBitmapListener {
        void onSet(Bitmap bitmap);
    }


    @BindingAdapter(value = {"getBitmap", "onGetBitmap"})
    public static void getBitmap(final ImageView imageView, final String imageUrl, OnGetBitmapListener listener) {

        if (imageUrl == null || "".equals(imageUrl)) {
            imageView.setImageResource(R.mipmap.no_image);
        } else {
            imageView.setImageURI(Uri.parse(imageUrl));
        }

        if(imageView != null && listener != null){
            Bitmap bitmapOriginal = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Bitmap bitmapTmp = Bitmap.createScaledBitmap(bitmapOriginal, WIDTH, HEIGHT, false);
            listener.onSet(bitmapTmp);
        }
    }

}
