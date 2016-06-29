package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.BiConsumer;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Utilities
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
     * @param imageView
     * @param entityImageId
     * @param defaultImageResId image to put if entityImageId == Entity.DEFAULT_ID (or 0 for doing
     *                          nothing in such case)
     * @return true
     */
    public static void setImageById(ImageView imageView, long entityImageId, @DrawableRes int defaultImageResId) {
        new AsyncTask<Void, Void, byte[]>() {
                @Override
                protected byte[] doInBackground(Void... params) {
                    if(entityImageId == Entity.DEFAULT_ID){
                        return null;
                    }
                    ImageEntity imageEntity = AccessManagerFactory.getInstance().getGeneralAccess().retrieve(ImageEntity.class, entityImageId);
                    return imageEntity.getBytes();
                }
                @Override
                protected void onPostExecute(byte[] bytes) {
                    setImageByBytes(imageView, bytes, defaultImageResId);;
                }
            }.execute();
    }

    /**
     * @param imageView view to set the image into.
     * @param entityImageBytes if null or length=0 set the {@code defaultImage}
     * @param defaultImage if 0 then does nothing
     */
    public static void setImageByBytes(ImageView imageView, byte[] entityImageBytes, @DrawableRes int defaultImage) {
        Bitmap bitmap;
        // if no image set default
        if (entityImageBytes == null || entityImageBytes.length == 0) {
            if (defaultImage != 0) {
                imageView.setImageResource(defaultImage);
            }

        }else {
            bitmap = BitmapFactory.decodeByteArray(entityImageBytes, 0, entityImageBytes.length);
            if (bitmap != null) {
                setCroppedImageBitmap(imageView, bitmap);
            }
        }
    }

    public static void setCroppedImageBitmap(ImageView imageView, Bitmap bitmap){
        bitmap = getCroppedBitmap(bitmap, imageView.getLayoutParams().width);
        imageView.setImageBitmap(bitmap);
    }


    ///////////////////////
    // String Utility
    ///////////////////////

    /**
     * Get string from string resources to enum value. <br>
     * The string resources id should be: 'enumName_valueName' where all are lowercase.
     * If string resource is not found, then it returns the original name of the enum.
     */
    public static String findStringResourceOfEnum(Context context, Enum<?> enumValue) {
        String idName = enumValue.getClass().getSimpleName().toLowerCase() + "_" + enumValue.name().toLowerCase();
        int id = findIdByString(R.string.class, idName);
        String string = (id == 0 ? null : context.getString(id));
        if (string == null) {
            string = enumValue.name();//.toLowerCase().replace('_', ' ');
        }
        return string;
    }

    /**
     * returns formatted string of money
     */
    public static String moneyToString(BigDecimal minPrice) {
        final char newShekelSign = '\u20AA';
        return moneyToNumberString(minPrice) + newShekelSign;
    }

    /**
     * returns formatted string of money, that contains only decimal number (no currency sign).
     */
    public static String moneyToNumberString(BigDecimal beginPrice) {
        return beginPrice.setScale(2, BigDecimal.ROUND_CEILING).toString();
    }

    /**
     * returns formatted string for rang of string. <br>
     * if both two calue are zeros, then it will return string "---".
     */
    public static String moneyRangeToString(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) == 0) {
            if (minPrice.compareTo(BigDecimal.ZERO) == 0) return "---";
            return moneyToString(minPrice);
        }
        return moneyToString(minPrice) + " \u2014 " + moneyToString(maxPrice);
    }

    /**
     * return formatted string of user name (first and second).
     * @param user the user
     */
    public static String userNameToString(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();
        return firstName + " " + lastName;
    }

    public static String datetimeToString(Date date) {
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static String dateToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    ///////////////////////////////
    // Images methods
    ///////////////////////////////

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
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show(); // TODO normal message
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

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;

        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() / factor), (int)(bmp.getHeight() / factor), false);
        } else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(radius / 2 + 0.7f,
                radius / 2 + 0.7f, radius / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }


    /**
     * Compress image for upload to data-base
     */
    public static byte[] compress(Bitmap newImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return out.toByteArray();
    }

    ///////////////////////
    // SharedPreferences methods
    ///////////////////////

    private static final String PREFERENCE_USERNAME = "preference_username";
    private static final String PREFERENCE_PASSWORD = "preference_password";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
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

    /////////////////////////
    // Others
    /////////////////////////

    /**
     * add view to viewGroup, that its content is of the layout, and set according to list by viewConsumer.
     * @param viewGroup the viewGroup to which will add
     * @param list the list of data
     * @param layout layout for view
     * @param viewConsumer consumer gets view and item from list and set the view according to list.
     * @param <T> type of list items.
     * @return map of items to view is associated with.
     */
    public static <T> Map<T,View> addViewsByList(ViewGroup viewGroup, List<T> list, @LayoutRes int layout , BiConsumer<View, T> viewConsumer) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        Map<T,View> viewsMap = new HashMap<>();
        if (list.isEmpty()) {
            View emptyView = inflater.inflate(R.layout.simple_empty_listview, viewGroup, false);
            viewGroup.addView(emptyView);
        } else {
            int i = 0;
            for (T item : list) {
                View view = inflater.inflate(layout, viewGroup, false);
                if (viewConsumer != null) viewConsumer.accept(view, item);
                viewGroup.addView(view, i++);
                viewsMap.put(item,view);
            }
        }
        return viewsMap;
    }

    /**
     * set spinner to unum values.
     */
    public static <T extends Enum<T>> void setSpinnerToEnum(Context context, Spinner spinner, T[] values) {
        if (spinner instanceof MultiSpinner) {
            MultiSpinner multiSpinner = (MultiSpinner) spinner;
            List<String> stringItems = Stream.of(values).map(e -> findStringResourceOfEnum(context, e)).collect(Collectors.toList());
            multiSpinner.setItems(stringItems, context.getString(R.string.all));
        } else {
            ArrayAdapter arrayAdapter = new EnumAdapter<>(context, android.R.layout.simple_spinner_item, Book.Genre.values());
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
        }

    }

    /**
    *  set listener to views with viewIds that they are children of parentView
     *  . if view with such id is not found, then this id will be ignored.
     */
    public static void setListenerForAll(View parentView, View.OnClickListener listener, @IdRes int... viewIds) {
        for (int id : viewIds) {
            View view = parentView.findViewById(id);
            if (view != null) view.setOnClickListener(listener);
            if (view instanceof TextView) {
                ((TextView)view).setTextColor(Color.BLUE);
            }
        }
    }

    /*
     * start web search by default web searcher or by google.com if not found
      */
    public static void startSearchActivity(Context context, String query) {
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, query);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://www.google.com/search?q=" + Uri.encode(query, "UTF-8")));
            context.startActivity(i);
        }
    }

}
