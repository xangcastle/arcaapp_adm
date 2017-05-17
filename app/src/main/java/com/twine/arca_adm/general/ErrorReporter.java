package com.twine.arca_adm.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

import com.twine.arca_adm.MyRestErrorHandler;
import com.twine.arca_adm.MyRestErrorHandler_;
import com.twine.arca_adm.RestClient;
import com.twine.arca_adm.RestClient_;
import com.twine.arca_adm.models.Empleado;
import com.twine.arca_adm.models.Registro;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Jose Williams Garcia on 17/5/2017.
 */

public class ErrorReporter implements Thread.UncaughtExceptionHandler {

    static private final String EOL = "\n";

    String VersionName;
    String PackageName;
    String FilePath;
    String PhoneModel;
    String AndroidVersion;
    String Board;
    String Brand;
    String Device;
    String Display;
    String FingerPrint;
    String Host;
    String ID;
    String Manufacturer;
    String Model;
    String Product;
    String Tags;
    long Time;
    String Type;
    String User;

    RestClient restClient;
    MyRestErrorHandler myErrorhandler;

    private Thread.UncaughtExceptionHandler PreviousHandler;
    private static ErrorReporter S_mInstance;
    private Context CurContext;

    public void Init(Context context) {
        PreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        getEnvironmentInfo(context);
        CurContext = context;
    }

    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    void getEnvironmentInfo(Context context)
    {
        PackageManager pm = context.getPackageManager();
        try
        {
            PackageInfo pi;
            Empleado empleado = Utilidades.db.get_empleado();
            // populate environment info variables
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            VersionName = pi.versionName;
            PackageName = pi.packageName;
            FilePath = context.getFilesDir().getAbsolutePath();
            PhoneModel = android.os.Build.MODEL;
            AndroidVersion = android.os.Build.VERSION.RELEASE;
            Board = android.os.Build.BOARD;
            Brand  = android.os.Build.BRAND;
            Device  = android.os.Build.DEVICE;
            Display = android.os.Build.DISPLAY;
            FingerPrint = android.os.Build.FINGERPRINT;
            Host = android.os.Build.HOST;
            ID = android.os.Build.ID;
            Model = android.os.Build.MODEL;
            Product = android.os.Build.PRODUCT;
            Tags = android.os.Build.TAGS;
            Time = android.os.Build.TIME;
            Type = android.os.Build.TYPE;
            SessionManager sessionManager =new SessionManager(context);
            //User = android.os.Build.USER;
            User= "Comercio: " + empleado.comercio.nombre + " Empleado: " + empleado.id_empleado;

        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public String CreateInformationString()
    {
        String ReturnVal = "";
        ReturnVal += "Version : " + VersionName + EOL;
        ReturnVal += "Package : " + PackageName + EOL;
        ReturnVal += "FilePath : " + FilePath + EOL;
        ReturnVal += "Phone Model" + PhoneModel + EOL;
        ReturnVal += "Android Version : " + AndroidVersion + EOL;
        ReturnVal += "Board : " + Board + EOL;
        ReturnVal += "Brand : " + Brand + EOL;
        ReturnVal += "Device : " + Device + EOL;
        ReturnVal += "Display : " + Display + EOL;
        ReturnVal += "Finger Print : " + FingerPrint + EOL;
        ReturnVal += "Host : " + Host + EOL;
        ReturnVal += "ID : " + ID + EOL;
        ReturnVal += "Model : " + Model + EOL;
        ReturnVal += "Product : " + Product + EOL;
        ReturnVal += "Tags : " + Tags + EOL;
        ReturnVal += "Time : " + Time + EOL;
        ReturnVal += "Type : " + Type + EOL;
        ReturnVal += "User : " + User + EOL;
        ReturnVal += "Total Internal memory : " + getTotalInternalMemorySize() + EOL;
        ReturnVal += "Available Internal memory : " + getAvailableInternalMemorySize() + EOL;

        return ReturnVal;
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        String Report = "";
        Date CurDate = new Date();
        Report += "Error Report collected on : " + CurDate.toString() + EOL + EOL;
        Report += "Information :" + EOL;
        Report += "" + EOL + EOL;
        Report += CreateInformationString() + EOL + EOL;
        Report += "Stack :" + EOL;
        Report += "" + EOL;

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        Report += stacktrace + EOL;

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        if (cause != null) {
            Report += "Cause :" + EOL;
            Report += "" + EOL;
            while (cause != null)
            {
                cause.printStackTrace(printWriter);
                Report += result.toString();
                cause = cause.getCause();
            }
        }
        printWriter.close();
        Report += "****  End of current Report ***" + EOL;
        SaveAsFile(Report);
        PreviousHandler.uncaughtException(t, e);
    }

    public void uncaughtException(Throwable e, Context context)
    {
        getEnvironmentInfo(context);
        String Report = "";
        Date CurDate = new Date();
        Report += "Error Report collected on : " + CurDate.toString() + EOL + EOL;
        Report += "Information :" + EOL;
        Report += "" + EOL + EOL;
        Report += CreateInformationString() + EOL + EOL;
        Report += "Stack :" + EOL;
        Report += "" + EOL;

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        Report += stacktrace + EOL;

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        if (cause != null) {
            Report += "Cause :" + EOL;
            Report += "" + EOL;
            while (cause != null)
            {
                cause.printStackTrace(printWriter);
                Report += result.toString();
                cause = cause.getCause();
            }
        }
        printWriter.close();
        Report += "****  End of current Report ***" + EOL;

        Registro registro=new Registro();
        registro.cargado=false;
        registro.registro=Report;
        registro.fecha= Calendar.getInstance().getTime();
        //SAVE LOG
        registro.save();

        if(restClient==null)
            restClient=new RestClient_(context);
        if(myErrorhandler==null)
            myErrorhandler=MyRestErrorHandler_.getInstance_(context);
        restClient.setRestErrorHandler(myErrorhandler);

        restClient.setRestErrorHandler(myErrorhandler);
        Empleado empleado=Utilidades.db.get_empleado();
        String respuesta = restClient.agregar_registro("arca_adm",
                registro.registro,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(registro.fecha),
                "Comercio: " + empleado.comercio.nombre + " Empleado: " + empleado.id_empleado
        );
        if(respuesta!=null){
            try {
                JSONObject jrespuesta=new JSONObject(respuesta);
                if(jrespuesta.getInt("code")==200){
                    registro.cargado=true;
                    registro.save();
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void deleteAllReports() {
        String[] ErrorFileList = GetErrorFileList();
        for (String curString : ErrorFileList) {
            File curFile = new File(FilePath + "/" + curString);
            curFile.delete();
        }
    }

    public static ErrorReporter getInstance()
    {
        if (S_mInstance == null)
            S_mInstance = new ErrorReporter();
        return S_mInstance;
    }

    private void SendErrorMail(Context _context, String ErrorContent)
    {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject =  "crash_report_mail_subject";
        String body = "crash_report_mail_body" + EOL + EOL +
                ErrorContent +
                EOL + EOL;
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"jwgarcia003@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");
        _context.startActivity(Intent.createChooser(sendIntent, "Send Email Via:"));
    }

    private void SaveAsFile(String ErrorContent)
    {
        try
        {
            Random generator = new Random();
            int random = generator.nextInt(99999);
            String FileName = "stack-" + random + ".stacktrace";
            FileOutputStream trace = CurContext.openFileOutput(FileName, Context.MODE_PRIVATE);
            trace.write(ErrorContent.getBytes());
            trace.close();
        }
        catch(IOException ioe) {
            // ...
        }
    }

    private String[] GetErrorFileList()
    {
        File dir = new File(FilePath + "/");
        // Try to create the files folder if it doesn't exist
        dir.mkdir();
        // Filter for ".stacktrace" files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".stacktrace");
            }
        };
        return dir.list(filter);
    }

    public boolean bIsThereAnyErrorFile()
    {
        return GetErrorFileList().length > 0;
    }

    public void CheckErrorAndSendMail(Context _context)
    {
        try
        {
            if (bIsThereAnyErrorFile())
            {
                String WholeErrorText = "";
                String[] ErrorFileList = GetErrorFileList();
                int curIndex = 0;
                // We limit the number of crash reports to send ( in order not to be too slow )
                final int MaxSendMail = 2;
                for (String curString : ErrorFileList)
                {
                    if (curIndex++ <= MaxSendMail)
                    {
                        WholeErrorText+="New Trace collected :" + EOL;
                        WholeErrorText+="" + EOL;
                        String filePath = FilePath + "/" + curString;
                        BufferedReader input =  new BufferedReader(new FileReader(filePath));
                        String line;
                        while ((line = input.readLine()) != null)
                        {
                            WholeErrorText += line + EOL;
                        }
                        input.close();
                    }

                    // DELETE FILES !!!!
                    File curFile = new File( FilePath + "/" + curString);
                    curFile.delete();
                }

                SendErrorMail(_context , WholeErrorText);

                if (bIsThereAnyErrorFile())
                {
                    // clear up any remaining reports
                    deleteAllReports();
                }

            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void CheckErrorAndSend(Context _context)
    {
        try
        {
            if (bIsThereAnyErrorFile())
            {
                String WholeErrorText = "";
                String[] ErrorFileList = GetErrorFileList();
                int curIndex = 0;
                // We limit the number of crash reports to send ( in order not to be too slow )
                final int MaxSendMail = 2;
                for (String curString : ErrorFileList)
                {
                    Registro registro=new Registro();
                    if (curIndex++ <= MaxSendMail)
                    {
                        WholeErrorText = "";
                        WholeErrorText+="New Trace collected :" + EOL;
                        WholeErrorText+="" + EOL;
                        String filePath = FilePath + "/" + curString;
                        BufferedReader input =  new BufferedReader(new FileReader(filePath));
                        String line;
                        while ((line = input.readLine()) != null)
                        {
                            WholeErrorText += line + EOL;
                        }
                        input.close();
                    }
                    else
                    {
                        continue;
                    }
                    registro.cargado=false;
                    registro.registro=WholeErrorText;
                    registro.fecha=Calendar.getInstance().getTime();
                    //SAVE LOG
                    registro.save();
                    // DELETE FILES !!!!
                    File curFile = new File( FilePath + "/" + curString);
                    curFile.delete();



                    if(restClient==null)
                        restClient=new RestClient_(_context);
                    if(myErrorhandler==null)
                        myErrorhandler=MyRestErrorHandler_.getInstance_(_context);
                    restClient.setRestErrorHandler(myErrorhandler);
                    Empleado empleado=Utilidades.db.get_empleado();
                    String respuesta = restClient.agregar_registro("arca_adm",
                            registro.registro,
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(registro.fecha),
                            "Comercio: " + empleado.comercio.nombre + " Empleado: " + empleado.id_empleado
                            );
                    if(respuesta!=null){
                        try {
                            JSONObject jrespuesta=new JSONObject(respuesta);
                            if(jrespuesta.getInt("code")==200){
                                registro.cargado=true;
                                registro.save();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (bIsThereAnyErrorFile())
                    {
                        // clear up any remaining reports
                        deleteAllReports();
                    }
                    break;
                }
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
