package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.BiConsumer;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 11/05/2016.
 */
public class Utility {

    private static final String PREFERENCE_USERNAME = "preference_username";
    private static final String PREFERENCE_PASSWORD = "preference_password";

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
    public static boolean setImageById(ImageView imageView, long entityImageId) {
        return setImageById(imageView, entityImageId, 0);
    }

    public static boolean setImageById(ImageView imageView, long entityImageId, @DrawableRes int defaultImageResId) {
        // first remove the current image
        if (defaultImageResId == 0) {
            //imageView.setImageDrawable(null);
        } else {
            imageView.setImageResource(defaultImageResId);
        }
        // end if no new image
        if (entityImageId == 0) {
            return true;
        }
        // else set the image
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                ImageEntity imageEntity = AccessManagerFactory.getInstance().getGeneralAccess().retrieve(ImageEntity.class, entityImageId);
                byte[] bytes = imageEntity.getBytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            }
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }.execute();
        return true;
    }

    public static boolean setImageByBytes(ImageView imageView, byte[] entityImageBytes) {

        // end if no new image
        if (entityImageBytes == null || entityImageBytes.length == 0) {
            return true;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(entityImageBytes, 0, entityImageBytes.length);
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

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    /**
     * /**
     * Set image from selectedImage to targetImageView. return the byte[] that contains the image.
     * @param context
     * @param selectedImage
     * @param targetImageView can be null
     * @return null if reading has failed.
     */
    public static Bitmap readImageFromURI(Context context, Uri selectedImage, ImageView targetImageView) {
        try {
            Bitmap bmp = decodeUri(context, selectedImage, 64);
            if (targetImageView != null) {
                targetImageView.setImageBitmap(bmp);
            }
            return bmp;
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static void startGetImage(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.choose)
                .setMessage(R.string.camera_galery_choose_message)
                .setPositiveButton(R.string.camera, (dialog, which) -> {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(takePicture, IntentsFactory.CODE_GET_IMAGE);
                })
                .setNegativeButton(R.string.gallery, (dialog, which) -> {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activity.startActivityForResult(pickPhoto, IntentsFactory.CODE_GET_IMAGE);
                })
                .create().show();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public static String datetimeToString(Date date) {
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static String dateToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    public static String usersListToFlatString(List<User> list){
        return Stream.of(list).map(User::getLastName).collect(Collectors.joining(", "));
    }

    public static <T> void setSpinnerToEnum(Context context, Spinner genreSpinner, T[] values) {
        ArrayAdapter arrayAdapter = new EnumAdapter<>(context, android.R.layout.simple_spinner_item, Book.Genre.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(arrayAdapter);

    }

    public static byte[] compress(Bitmap newImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return out.toByteArray();
    }

    public static Credentials loadCredentials(Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String username = sharedPreferences.getString(PREFERENCE_USERNAME,"");
        String password = sharedPreferences.getString(PREFERENCE_PASSWORD,"");
        return new Credentials(username, password);
    }

    public static void saveCredentials(Context context, Credentials credentials){
        getSharedPreferences(context).edit()
                .putString(PREFERENCE_USERNAME, credentials.getUsername())
                .putString(PREFERENCE_PASSWORD, credentials.getPassword()).commit();
    }

    public static <T> void addViewsByList(ViewGroup viewGroup, List<T> list, LayoutInflater inflater, @LayoutRes int layout , BiConsumer<View, T> viewConsumer) {
        if (list.isEmpty()) {
            View emptyView = inflater.inflate(R.layout.simple_empty_listview, viewGroup, false);
            viewGroup.addView(emptyView);
        } else {
            int i = 0;
            for (T item : list) {
                View view = inflater.inflate(layout, viewGroup, false);
                if (viewConsumer != null) viewConsumer.accept(view, item);
                viewGroup.addView(view, i++);
            }
        }
    }


    public static <T extends Entity> void replaceById(ArrayAdapter<T> adapter, T item) {
        int itemPosition = adapter.getCount();
        while (--itemPosition >= 0 && adapter.getItemId(itemPosition) == item.getId());
        if (itemPosition >= 0) adapter.insert(item, itemPosition);
    }

}
