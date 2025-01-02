package com.septi.resq.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class MarkerUtils {

    // Helper method untuk resize marker icon
    public static Drawable resizeMarkerIcon(Context context, Drawable icon, int sizeDp) {
        float density = context.getResources().getDisplayMetrics().density;
        int pixelSize = (int) (sizeDp * density);

        Bitmap bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
