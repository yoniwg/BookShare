package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Created by haim7 on 11/05/2016.
 */
public class Utility {

    /**
     * @param idsClass
     * @param idString
     * @return 0 if not fount.
     */
    public static int findIdByString(Class idsClass, String idString) {
        try {
            return (int) idsClass.getField(idString).get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            return 0;
        }
    }

    /**
     * Set ImageView to imageId of entities.
     *
     * @param imageView
     * @param entityImageId
     * @return
     */
    private static boolean setImageById(ImageView imageView, long entityImageId) {
        return setImageById(imageView, entityImageId, 0);
    }

    public static boolean setImageById(ImageView imageView, long entityImageId, @DrawableRes int defaultImageResId) {
        // first remove the current image
        if (defaultImageResId == 0) {
            imageView.setImageDrawable(null);
        } else {
            imageView.setImageResource(defaultImageResId);
        }
        // end if no new image
        if (entityImageId == 0) {
            return true;
        }
        // else set the image
        ImageEntity imageEntity = AccessManagerFactory.getInstance().getGeneralAccess().retrieve(ImageEntity.class, entityImageId);
        byte[] bytes = imageEntity.getBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bitmap);
        return true;
    }


    ///////////////////////
    // String Utility
    ///////////////////////

    public static String findStringResourceOfEnum(Context context, Enum<?> enumValue) {
        String idName = enumValue.getClass().getSimpleName().toLowerCase() + "_" + enumValue.name().toLowerCase();
        int id = findIdByString(R.string.class, idName);
        String string = (id == 0 ? null : context.getString(id));
        if (string == null) {
            string = enumValue.name();//.toLowerCase().replace('_', ' ');
        }
        return string;
    }


    public static String moneyToNumberString(BigDecimal beginPrice) {
        return beginPrice.setScale(2, BigDecimal.ROUND_CEILING).toString();
    }

    public static String moneyToString(BigDecimal minPrice) {
        final char newShekelSign = '\u20AA';
        return moneyToNumberString(minPrice) + newShekelSign;
    }

    public static String moneyRangeToString(BigDecimal minPrice, BigDecimal maxPrice) {
        String minString = moneyToString(minPrice);
        String maxString = moneyToString(maxPrice);
        if (minString.equals(maxString)) return minString;
        return minString + " \u2014 " + maxString;
    }

    public static String userNameToString(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();
        return firstName + " " + lastName;
    }

    private static byte[] readBytesFromURI(Context context, Uri uri) throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    /**
     * /**
     * Set image from selectedImage to targetImageView. return the byte[] that contains the image.
     * @param context
     * @param selectedImage
     * @param targetImageView can be null
     * @return null if reading has failed.
     */
    public static byte[] readImageFromURI(Context context, Uri selectedImage, ImageView targetImageView) {
        try {
            byte[] bytes = readBytesFromURI(context, selectedImage);
            if (targetImageView != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                targetImageView.setImageBitmap(bmp);
            }
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    public static void startGetImage(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.choose)
                .setMessage(R.string.camera_galery_choose_message)
                .setPositiveButton(R.string.camera, (dialog, which) -> {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(takePicture, IntentsFactory.GET_IMAGE_CODE);
                })
                .setNegativeButton(R.string.gallery, (dialog, which) -> {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activity.startActivityForResult(pickPhoto, IntentsFactory.GET_IMAGE_CODE);
                })
                .create().show();
    }
}