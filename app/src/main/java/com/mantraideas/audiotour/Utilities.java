package com.mantraideas.audiotour;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Utilities {

    public static final String SD_PATH = Environment.getExternalStorageDirectory() + "/.Audiomap";

    public static void log(String string) {
        if (BuildConfig.DEBUG)
            System.out.println(string);
    }

    public static void log(Class<?> mclass, String string) {
        log(mclass.getSimpleName() + " , " + string);
    }


    public static String getFileNameFromUrl(String url) {
        try {
            return url.substring(url.lastIndexOf("/") + 1);
        } catch (Exception e) {
        }
        return "";
    }

    public static File getSDPathFromUrl(String url) {
        return createFilePath(SD_PATH, getFileNameFromUrl(url));
    }
    public static String getRelativeTime(long timestamp) {
        long nowtime = System.currentTimeMillis();
        if (timestamp < nowtime) {
            return (String) DateUtils.getRelativeTimeSpanString(timestamp, nowtime, 0);
        }
        return "Just now";
    }

    public static String getRelativeTime(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date d = simpleDateFormat.parse(date);
        long timestamp = d.getTime();
        long nowtime = System.currentTimeMillis();
        if (timestamp < nowtime) {
            return (String) DateUtils.getRelativeTimeSpanString(timestamp, nowtime, 0);
        }
        return "Just now";
    }
    public static boolean checkFileExist(String url) {
        if (url == null || url.isEmpty())
            return false;
        File f = createFilePath(SD_PATH, getFileNameFromUrl(url));
        return f.exists();
    }

    private static File createFilePath(String folder, String filename) {
        File f = new File(folder);
        if (!f.exists())
            f.mkdirs();
        return new File(f, filename);
    }

    public static boolean existGyroscope(Context ctx) {
        SensorManager mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        boolean hasSensor = (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null);
        log("Utilities, hasSensor Gyroscope = " + hasSensor);
        return hasSensor;
    }
    public static String getResizedImage(String url, String size){

        return "http://cdn.hamroapi.com/resize?w="+size+"&url="+url.replace("https://","http://");
    }

    public static boolean checkPackageExist(String id) {
        File f = createFilePath(SD_PATH + "/" + id, "rhkg38yw4w");
        return f.exists();
    }
    public static boolean isLoggedIn(Context context) {
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(context);
        return !pf.getString("u_token", "").isEmpty();
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        int smallest = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(smallest, smallest, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffffffff;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, smallest, smallest);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public static void doRate(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=" + context.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("http://play.google.com/store/apps/details?id="
                            + context.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }

    public static Intent getDefaultShareIntent(String name, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, name);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        return intent;

    }

    public static boolean isNetworkAvialable(Context cont) {
        ConnectivityManager cm = (ConnectivityManager) cont
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null)
            return true;
        else
            return false;
    }


    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersioncode(Context context) {
        int version = 1;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static void shareApp(Context context) {
        context.startActivity(Intent.createChooser(Utilities
                .getDefaultShareIntent("college",
                        "http://play.google.com/store/apps/details?id="
                                + context.getPackageName()), "Share this App").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


//    public static String getEmail(Context context) {
//        try {
//            return AccountManager.get(context).getAccountsByType("com.google")[0].name;
//        } catch (Exception e) {
//        }
//        return "";
//    }

    public static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
//    public static void sendEvent(String category, String action){
//        Tracker t = MainApplication.tracker;
//        if(t == null) return;
//        t.send(new HitBuilders.EventBuilder()
//                .setCategory(category)
//                .setAction(action)
//                .build());
//
//    }
//    public static void sendScreen(String screenName){
//        Tracker t = MainApplication.tracker;
//        if(t == null) return;
//        t.setScreenName(screenName);
//        t.send(new HitBuilders.ScreenViewBuilder()
//                .build());
//    }

    public static String readFromFile(File file) {
        try {
            FileInputStream fin = new FileInputStream(file);
            String ret = convertStreamToString(fin);
            //Make sure you close all streams.
            fin.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }


    public static String getDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return "0";
    }

    public static long getPrefNum(Context ctx, String key) {
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pf.getLong(key, 0l);
    }

    public static boolean setPrefNum(Context ctx, String key, long val) {
        SharedPreferences pf = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pf.edit().putLong(key, val).commit();
    }

    public static boolean isCacheValid(Context ctx, String key) {
        long last = getPrefNum(ctx, key);
        return (System.currentTimeMillis() - last < 60 * 60 * 1000);
    }

    public static boolean isCacheValid(Context ctx, String key, long validity) {
        long last = getPrefNum(ctx, key);
        return (System.currentTimeMillis() - last < validity);
    }

    public static String getDeviceInfo() {
        try {
            return Build.MANUFACTURER.toUpperCase() + " (" + Build.MODEL + ")";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void openLink(Context context, String url) {
        if (url == null || url.isEmpty())
            return;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendEmail(Context context, String name, String email) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, name);
        i.putExtra(Intent.EXTRA_TEXT, name);
        try {
            context.startActivity(Intent.createChooser(i, "Select action"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimeString(String time) {
        Calendar cal = Calendar.getInstance();
        String d[] = time.split("-");
        if (d != null && d.length == 3) {
            cal.set(Integer.parseInt(d[0]), Integer.parseInt(d[1]) - 1, Integer.parseInt(d[2]));
        }
        return new SimpleDateFormat("MMM d, yyyy").format(cal.getTime());

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);

            if (listItem != null) {
                // This next line is needed before you call measure or else you won't get measured height at all. The listitem needs to be drawn first to know the height.
                listItem.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();

            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * listAdapter.getCount());
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void showAppDisabledDialog(final Activity activity) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        builder.setTitle("App is inactive")
                .setMessage("It seems that this app has been disabled by Admin.\n\nPlease contact college for the information.")
                .setPositiveButton("OK", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        try {
                            activity.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ;
                    }
                })
                .create().show();
    }

    public static boolean isValidEmail(String emailId) {
        if (emailId == null || emailId.isEmpty())
            return false;
        return emailId.matches("\\w[-.\\+_\\w]*@\\w[-._\\w]*\\w\\.\\w\\w+");
    }

//    public static String replaceImageIfNoInternet(Context context, String html) {
//        if (!ServerRequest.isNetworkConnected(context)) {
//            html = html.replaceAll("\\<img.*?>", "<img src='file:///android_asset/no_image.jpg'/>");
//        }
//        return html;
//    }
//
//    public static void playNotificationSound() {
//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Ringtone r = RingtoneManager.getRingtone(MainApplication.context, notification);
//        r.play();
//
//    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static void downloadAppFromPlaystore(Activity activity, String packageName) {
        String url = "";
        try {
            // check whether the playstore is installed or not
            activity.getPackageManager().getPackageInfo(packageName, 0);
            url = "market://details?id=" + packageName;
        } catch (Exception e) {
            e.printStackTrace();
            url = "https://play.google.com/store/apps/details?id=" + packageName;
        }

        // open the app in play store
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 && lon1 == 0) {
            return 0;
        }
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(lon1 - lon2));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;

        log("Distance of " + lat1 + ", " + lon1 + " and " + lat2 + ", " + lon2 + " is " + dist);
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double getDirection(double lat1, double lng1, double lat2, double lng2) {
        double dLon = (lng2 - lng1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees((Math.atan2(y, x)));
        brng = (360 - ((brng + 360) % 360));
        return brng;
    }

    public static String getTimeRemaining(String date) {
        //String strThatDay =date;
        SimpleDateFormat formatterIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //SimpleDateFormat formatterOut = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date d = null;
        try {
            d = (Date) formatterIn.parse(date);//catch exception
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(d);
//        thatDay = Calendar.getInstance();
//        thatDay.set(Calendar.DAY_OF_MONTH,25);
//        thatDay.set(Calendar.MONTH,5); // 0-11 so 1 less
//        thatDay.set(Calendar.YEAR, 2016);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis(); //result in millis
        long days = diff / (24 * 60 * 60 * 1000);
        if ((int) days == 0) {

            return ((int) diff / (60 * 60 * 1000)) + "h";

        } else if ((diff / (60 * 60 * 1000) == 0)) {

            return ((int) diff / (60 * 1000)) + "m";

        } else {
            return (int) days + "d";
        }

    }

    public static void toast(Context con    , String s) {
        Toast.makeText(con,s,Toast.LENGTH_SHORT).show();
    }
}
