package com.twine.arca_adm.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.activeandroid.query.Select;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;
import com.twine.arca_adm.LoginActivity_;
import com.twine.arca_adm.R;
import com.twine.arca_adm.models.Descuento;
import com.twine.arca_adm.models.Empleado;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */

public class Utilidades {
    private static final String TAG = "Utilidades";
    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 400;
    public final static int HEIGHT = 400;
    //public final static String BASE_URL="http://demos.deltacopiers.com";
    public final static String BASE_URL="http://192.168.232.1:8000";

    public static boolean is_autenticado(){
        Empleado empleado=new Select().from(Empleado.class).where("activo=?",true).executeSingle();
        if (empleado!=null)
            return true;
        else
            return false;
    }
    public static Boolean atuenticado_o_redirect(Context context){
        if (!is_autenticado()){
            context.startActivity(new Intent(context, LoginActivity_.class));
            ((Activity) context).finish();
            return false;
        }
        return true;
    }
    public static String getUUID(){
      return  UUID.randomUUID().toString();
    }
    public static Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
    public static  void cargarImageView(ImageView imageView, String URL) {
        try {
            if(URL.contains("DCIM")){
                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(URL), 600, 600);
                imageView.setTag(URL);
                imageView.setImageBitmap(ThumbImage);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else {
                Picasso picaso= Picasso.with(imageView.getContext());
                picaso.setIndicatorsEnabled(true);
                picaso.load(URL)
                        .resize(400, 400).into(imageView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static  void cargarImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public static class db{
        public static Empleado get_empleado(){
            return new Select().from(Empleado.class).where("activo=?",true).executeSingle();
        }
        public static String saveDescuentos(String strJson){
            String repuesta="OK";
            if(strJson!=null){
                try {
                    JSONObject jRespuesta = new JSONObject(strJson);
                    if (jRespuesta.getInt("code")==200){
                        JSONArray jdesccuentos = jRespuesta.getJSONArray("descuentos");
                        for (int i = 0; i < jdesccuentos.length(); i++) {
                            JSONObject jdescuento = jdesccuentos.getJSONObject(i);
                            Descuento descuento= new Select().from(Descuento.class)
                                    .where("id_descuento=?",jdescuento.get("id")).executeSingle();
                            if (descuento==null)
                                descuento=new Descuento();
                            descuento.id_descuento=jdescuento.getInt("id");
                            descuento.nombre=jdescuento.getString("nombre");
                            descuento.porcentaje_descuento=jdescuento.getDouble("porcentaje_descuento");
                            descuento.vigencia= jdescuento.getInt("vigencia");

                            if (!jdescuento.isNull("desc_dia_vigencia"))
                                descuento.desc_dia_vigencia= jdescuento.getInt("desc_dia_vigencia");
                            if (!jdescuento.isNull("desc_dia_vigencia_porc_inf"))
                                descuento.desc_dia_vigencia_porc_inf= jdescuento.getDouble("desc_dia_vigencia_porc_inf");
                            if (!jdescuento.isNull("desc_dia_vigencia_porc_sup"))
                                descuento.desc_dia_vigencia_porc_sup= jdescuento.getDouble("desc_dia_vigencia_porc_sup");
                            if (!jdescuento.isNull("desc_compra_minima"))
                                descuento.desc_compra_minima= jdescuento.getDouble("desc_compra_minima");
                            if (!jdescuento.isNull("desc_compra_minima_porc_inf"))
                                descuento.desc_compra_minima_porc_inf= jdescuento.getDouble("desc_compra_minima_porc_inf");
                            if (!jdescuento.isNull("desc_compra_minima_porc_sup"))
                                descuento.desc_compra_minima_porc_sup= jdescuento.getDouble("desc_compra_minima_porc_sup");

                            descuento.save();
                        }
                    }else {
                        repuesta = "ERROR: " + jRespuesta.getInt("mensaje");
                    }
                } catch (JSONException e) {
                    repuesta = "ERROR: " + e.getMessage();
                }
            }


            return repuesta;
        }
        public static List<Descuento> getDescuentos(){
            return new Select().from(Descuento.class).execute();
        }
    }
    public static final int PERMISIONS_REQUEST = 12;
    public static void checkPermision(Context context) {


        ArrayList<String> requestedPermissionsGranted=new ArrayList<String>();
        try {
            ApplicationInfo MyapplicationInfo = context.getApplicationInfo();
            PackageManager pm =context.getPackageManager();

            PackageInfo packageInfo = pm.getPackageInfo(MyapplicationInfo.packageName, PackageManager.GET_PERMISSIONS);

            //Get Permissions
            String[] requestedPermissions = packageInfo.requestedPermissions;

            if(requestedPermissions != null) {
                for (int i = 0; i < requestedPermissions.length; i++) {
                    Log.d(TAG, "MyPermisions: " + requestedPermissions[i]);
                    if(ContextCompat.checkSelfPermission(context, requestedPermissions[i]) != PackageManager.PERMISSION_GRANTED)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestedPermissions[i].contains("SYSTEM_ALERT_WINDOW")){
                            Log.d( TAG, "MyPermisions: no se permite permiso SYSTEM_ALERT_WINDOW en Marshmallow");
                        }else if(requestedPermissions[i].contains("BIND_ACCESSIBILITY_SERVICE")){
                            Log.d( TAG, "MyPermisions: no se permite permiso BIND_ACCESSIBILITY_SERVICE en Marshmallow");
                        }
                        else
                            requestedPermissionsGranted.add(requestedPermissions[i]);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(requestedPermissionsGranted.size()>0){
            ActivityCompat.requestPermissions(((Activity) context),
                    requestedPermissionsGranted.toArray(new String[] {}),
                    PERMISIONS_REQUEST);
        }
    }
}