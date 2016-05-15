package com.hgyw.bookshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by haim7 on 11/05/2016.
 */
public class Utility {

    /**
     *
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

    public static void addBookSupplierToCart(BookSupplier bookSupplier, int amount) {
        Order order = new Order();
        order.setBookSupplierId(bookSupplier.getSupplierId());
        order.setAmount(1);
        order.setUnitPrice(bookSupplier.getPrice());
        CustomerAccess access = AccessManagerFactory.getInstance().getCustomerAccess();
        access.getCart().add(order);
    }

    /**
     * Set ImageView to imageId of entities.
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
        if (entityImageId == 0) {return true;}
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
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    /**
     * targetImageView can be null, and dosnte change in failed.
     */
    public static void uploadImageURI(Context context, Uri selectedImage, ImageEntity imageEntity, ImageView targetImageView) {
        try {
            imageEntity.setBytes(readBytesFromURI(context, selectedImage));
            AccessManagerFactory.getInstance().getGeneralAccess().upload(imageEntity);
            if (imageEntity.getId() == 0) return;
            if (targetImageView != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(imageEntity.getBytes(), 0, imageEntity.getBytes().length);
                targetImageView.setImageBitmap(bmp);
            }
        } catch (IOException e) {
            imageEntity.setId(0);
        }
    }
}
