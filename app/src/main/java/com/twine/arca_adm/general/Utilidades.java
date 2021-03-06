package com.twine.arca_adm.general;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;
import com.twine.arca_adm.LoginActivity_;
import com.twine.arca_adm.MainActivity;
import com.twine.arca_adm.R;
import com.twine.arca_adm.models.Comercio;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Descuento;
import com.twine.arca_adm.models.Empleado;
import com.twine.arca_adm.models.Factura;
import com.twine.arca_adm.models.Registro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    //public final static String BASE_URL="http://192.168.1.13:8000";
    public final static String BASE_URL="http://demos.deltacopiers.com";
    //public final static String BASE_URL="http://192.168.232.1:8000";

    public static boolean is_autenticado(){
        Empleado empleado=new Select().from(Empleado.class).where("activo=?",true).executeSingle();
        if (empleado!=null)
            return true;
        else
            return false;
    }
    public static void log_out(Context context){
        limpiarTodo();
        context.startActivity(new Intent(context, LoginActivity_.class));
        ((Activity) context).finish();
    }
    public static void limpiarTodo(){
        new Delete().from(Factura.class).execute();
        new Delete().from(Cupon.class).execute();
        new Delete().from(Descuento.class).execute();
        new Delete().from(Empleado.class).execute();
        new Delete().from(Comercio.class).execute();
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
    public static void motrarDialogoOK(Context context, String Titulo, String Mensaje){
        final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
        dialog.setTitle(Titulo);
        dialog.setMessage(Mensaje);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
    public static class db{
        public static List<Registro> get_registros_sin_cargar(){
            List<Registro> registros=new Select().from(Registro.class)
                    .where("cargado=?",false).execute();
            return registros;
        }
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
        public static boolean saveCupones(String strJson){
            Boolean haynuevos=false;
            try {
                JSONObject jrespuesta=new JSONObject(strJson);
                if(jrespuesta.getInt("code")==200){
                    JSONArray jcupones = jrespuesta.getJSONArray("cupones");

                    for (int i = 0; i < jcupones.length(); i++) {
                        JSONObject jcupon = jcupones.getJSONObject(i);
                        Empleado empleado = new Select().from(Empleado.class)
                                .where("id_empleado=?",jcupon.getJSONObject("creado_por").getInt("id"))
                                .executeSingle();
                        if (empleado==null)
                            empleado=new Empleado();
                        empleado.id_empleado=jcupon.getJSONObject("creado_por").getInt("id");
                        empleado.nombre=jcupon.getJSONObject("creado_por").getString("nombre");
                        empleado.apellido=jcupon.getJSONObject("creado_por").getString("apellido");
                        empleado.save();

                        Descuento descuento = new Select().from(Descuento.class)
                                .where("id_descuento=?",jcupon.getInt("id_descuento"))
                                .executeSingle();

                        if(descuento!=null && empleado!=null){
                            Cupon cupon = new Select().from(Cupon.class)
                                    .where("id_cupon=?",jcupon.getInt("id"))
                                    .executeSingle();
                            Boolean isnew=false;
                            if (cupon==null) {
                                cupon = new Cupon();
                                isnew=true;
                            }
                            cupon.id_cupon=jcupon.getInt("id");
                            cupon.codigo=jcupon.getString("codigo");
                            cupon.canjeado=jcupon.getBoolean("canjeado");
                            try {
                                cupon.creado_por=empleado;
                                cupon.creado=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                        .parse(jcupon.getString("creado")) ;
                            } catch (ParseException e) {e.printStackTrace();}
                            catch (Exception e){e.printStackTrace();}
                            try {
                                cupon.actualizado=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                        .parse(jcupon.getString("actualizado")) ;
                            }catch (ParseException e) {e.printStackTrace();}
                            catch (Exception e){e.printStackTrace();}

                            if(jcupon.has("actualizado_por")){
                                try{
                                    JSONObject jempleado_actualiza=
                                            jcupon.getJSONArray("actualizado_por").getJSONObject(0);

                                    Empleado empleado_actualiza=new Select().from(Empleado.class)
                                            .where("id_empleado=?",jempleado_actualiza.getInt("id"))
                                            .executeSingle();
                                    if(empleado_actualiza==null)
                                        empleado_actualiza=new Empleado();
                                    empleado_actualiza.id_empleado=jempleado_actualiza.getInt("id");
                                    empleado_actualiza.nombre=jempleado_actualiza.getString("nombre");
                                    empleado_actualiza.apellido=jempleado_actualiza.getString("apellido");
                                    empleado_actualiza.save();
                                    cupon.actualizado_por=empleado_actualiza;
                                } catch (Exception e){e.printStackTrace();}
                            }

                            cupon.descuento=descuento;
                            cupon.save();
                            if(isnew) {
                                haynuevos=true;
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return haynuevos;
        }
        public static boolean saveFacturas(String strJson){
            Boolean compleatado=true;
            try {
                JSONObject jrespuesta=new JSONObject(strJson);
                if(jrespuesta.getInt("code")==200){
                    JSONArray jcupones = jrespuesta.getJSONArray("facturas");

                    for (int i = 0; i < jcupones.length(); i++) {
                        JSONObject jfactura = jcupones.getJSONObject(i);
                        Cupon cupon= new Select().from(Cupon.class)
                                .where("id_cupon=?",jfactura.getInt("cupon_id"))
                                .executeSingle();

                        if (cupon==null)
                            continue;
                        Comercio comercio=get_empleado().comercio;
                        Factura factura=new Select().from(Factura.class)
                                .where("id_factura=?",jfactura.getInt("id"))
                                .executeSingle();
                        if(factura==null)
                            factura=new Factura();
                        factura.id_factura=jfactura.getInt("id");
                        factura.comercio=comercio;
                        factura.cupon=cupon;
                        factura.monto=jfactura.getDouble("monto");
                        factura.descuento=jfactura.getDouble("descuento");
                        try {
                            factura.fecha=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                    .parse(jfactura.getString("fecha")) ;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        cupon.canjeado=true;
                        cupon.save();
                        factura.save();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return compleatado;
        }
        public static List<Descuento> getDescuentos(){
            return new Select().from(Descuento.class).execute();
        }
        public static List<Cupon> getCupones(boolean canjeado){
            return new Select().from(Cupon.class)
                    .where("canjeado=?",canjeado)
                    .orderBy("creado desc")
                    .execute();
        }
        public static List<Cupon> getCuponesPorFecha(Date fecha, boolean canjeado){
            List<Cupon> cupones = new ArrayList<>();
            SimpleDateFormat formato=new SimpleDateFormat("dd/MM/yyyy");
            List<Cupon> cuponesFiltro= new Select().from(Cupon.class)
                    .where("canjeado=?",canjeado)
                    .orderBy("creado desc")
                    .execute();
            for (Cupon cupon:cuponesFiltro){
                if(formato.format(cupon.creado).equals(formato.format(fecha))){
                    cupones.add(cupon);
                }
            }
            return cupones;
        }
        public static List<Cupon> getCuponesPendientesCarga() {
            return new Select().from(Cupon.class)
                    .where("id_cupon=?", 0)
                    .execute();
        }
        public static List<Factura> getFacturasPendientesCarga() {
            return new Select().from(Factura.class)
                    .where("id_factura=?", 0)
                    .execute();
        }
        public static List<Factura> getFacturasxMesAnio(int mes, int anio) {
            Calendar cal= Calendar.getInstance();
            cal.set(Calendar.YEAR,anio);
            cal.set(Calendar.MONTH,mes);
            cal.set(Calendar.DAY_OF_MONTH,1);

            Calendar cal2= Calendar.getInstance();
            cal2.set(Calendar.YEAR,anio);
            cal2.set(Calendar.MONTH,mes);
            cal2.set(Calendar.DAY_OF_MONTH,cal2.getMaximum(Calendar.DAY_OF_MONTH));

            return new Select().from(Factura.class)
                    .where("fecha>= ?", cal.getTime().getTime())
                    .where("fecha<= ?", cal2.getTime().getTime())
                    .execute();
        }
        public static List<Factura> getFacturasxDiasMes_Empleado(int mes, int anio, int dia,
                                                                 Empleado empleado) {

            Calendar cal= Calendar.getInstance();
            cal.setTime(Utilidades.getDate(anio,mes,1));

            Calendar cal2= Calendar.getInstance();
            cal2.setTime(Utilidades.getDate(anio,mes,1));
            cal2.add(Calendar.MONTH,1);

            List<Factura> facturas = new Select().from(Factura.class).innerJoin(Cupon.class)
                    .on("facturas.pk_cupon=cupones.id")
                    .where("fecha>= ?", cal.getTime().getTime())
                    .where("fecha< ?", cal2.getTime().getTime())
                    .where("actualizado_por=?",empleado.getId())
                    .execute();
            return  facturas;
        }
        public static List<Cupon> getCuponesxMesAnio(int mes, int anio) {
            Calendar cal= Calendar.getInstance();
            cal.set(Calendar.YEAR,anio);
            cal.set(Calendar.MONTH,mes);
            cal.set(Calendar.DAY_OF_MONTH,1);

            Calendar cal2= Calendar.getInstance();
            cal2.set(Calendar.YEAR,anio);
            cal2.set(Calendar.MONTH,mes);
            cal2.set(Calendar.DAY_OF_MONTH,cal2.getMaximum(Calendar.DAY_OF_MONTH));

            return new Select().from(Cupon.class)
                    .where("creado>= ?", cal.getTime().getTime())
                    .where("creado<= ?", cal2.getTime().getTime())
                    .execute();
        }
        public static List<Cupon> getCuponesxMesAnioEmpleado(int mes, int anio, Empleado empleado) {
            Calendar cal= Calendar.getInstance();
            cal.set(Calendar.YEAR,anio);
            cal.set(Calendar.MONTH,mes);
            cal.set(Calendar.DAY_OF_MONTH,1);

            Calendar cal2= Calendar.getInstance();
            cal2.set(Calendar.YEAR,anio);
            cal2.set(Calendar.MONTH,mes);
            cal2.set(Calendar.DAY_OF_MONTH,cal2.getMaximum(Calendar.DAY_OF_MONTH));

            return new Select().from(Cupon.class)
                    .where("creado>= ?", cal.getTime().getTime())
                    .where("creado<= ?", cal2.getTime().getTime())
                    .execute();
        }
        public static List<Cupon> getCuponesxPotenciales() {
            List<Cupon>cupones= new Select().from(Cupon.class)
                    .execute();
            List<Cupon> cupones_repuesta=new ArrayList<>();
            for (Cupon cupon:cupones) {
                if(cupon.esValido())
                    cupones_repuesta.add(cupon);
            }
            return cupones_repuesta;
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
    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
