package com.twine.arca_adm;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.twine.arca_adm.adapters.CuponAdapter;
import com.twine.arca_adm.adapters.DividerItemDecoration;
import com.twine.arca_adm.general.SessionManager;
import com.twine.arca_adm.general.Utilidades;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Descuento;
import com.twine.arca_adm.models.Empleado;
import com.twine.arca_adm.models.Factura;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@EFragment
public class DescuentosFragment extends Fragment {
    private ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;
    @RestService
    RestClient restClient;
    @Bean
    MyRestErrorHandler myErrorhandler;
    @ViewById(R.id.spinnerDescuento)
    Spinner spDescuento;
    @ViewById(R.id.LayautDetalle)
    View LayautDetalle;
    @ViewById(R.id.LayoutMaster)
    View LayoutMaster;
    @ViewById(R.id.LayautDetalleCanje)
    View LayautDetalleCanje;
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.containerCupon)
    LinearLayout containerCupon;
    @ViewById(R.id.cupon_view)
    LinearLayout cupon_view;
    @ViewById(R.id.editFecha)
    TextView editFecha;
    @ViewById(R.id.txtCantidad)
    TextView txtCantidad;
    @ViewById(R.id.descripcion_scan)
    TextView descripcion_scan;
    @ViewById(R.id.monto_factura)
    EditText monto_factura;
    @ViewById(R.id.factura)
    EditText factura;
    @ViewById(R.id.cktodos)
    CheckBox cktodos;
    @ViewById(R.id.canjear_cupon)
    Button canjear_cupon;
    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    @ViewById(R.id.swipeRefresh)
    SwipeRefreshLayout refreshLayout;
    SessionManager session;
    Factura factura_canje;
    @Bean
    CuponAdapter adapter;

    List<Descuento> descuentos;
    Descuento descuento;

    List<Cupon> cupones;
    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setFechaLabel(myCalendar.getTime());
        }

    };

    public DescuentosFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session=new SessionManager(getContext());
        restClient.setRestErrorHandler(myErrorhandler);
        setFechaLabel(Calendar.getInstance().getTime());
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_descuentos, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarCupones();
        cargarFacturas();
        descargarCupones();
    }
    Spinner spinner;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_descuentos, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item); // get the spinner
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("NO CANJEADO");
        spinnerArray.add("CANJEADO");
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refrescarListaCupones();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    @CheckedChange(R.id.cktodos)
    void setCktodos_CheckedChange(){
        refrescarListaCupones();
    }

    @Click(R.id.editFecha)
    void editFechaClick(){
        new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    @UiThread
    void setFechaLabel(Date fecha){
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editFecha.setText(sdf.format(fecha));
        refrescarListaCupones();
    }
    @Click(R.id.fab)
    void nuevoDescuentoClick(){
        showProgressDialog();
        cargarDescuentos();
        //readQR();
    }
    @Click(R.id.fab2)
    void canjearDescuentoClick(){
        showProgressDialog();
        showMaster(showLayout.CANJEAR_DESCUENTO);
        hideProgressDialog();
    }
    @Click(R.id.readQR)
    void readQR(){
        session.saveSharedValue("INTEGRATOR_ACTION", "REQUEST_CODE_DESCUENTO");
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        Intent scanIntent=integrator.createScanIntent();
        startActivityForResult(scanIntent,IntentIntegrator.REQUEST_CODE);
    }
    @Click(R.id.scanear_canje)
    void scanear_canje_Click(){
        if(valido_para_canjear()){
            session.saveSharedValue("INTEGRATOR_ACTION", "REQUEST_CODE_DESCUENTO_CANJE");
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            Intent scanIntent=integrator.createScanIntent();
            startActivityForResult(scanIntent,IntentIntegrator.REQUEST_CODE);
        }
    }
    @Click(R.id.canjear_cupon)
    void canjear_cupon_Click(){
        showProgressDialog();
        if(valido_para_canjear()&&factura_canje!=null)
            guardar_canje(factura_canje);
    }
    @Background
    void guardar_canje(Factura factura){
        String respuesta=restClient.canjear_cupon(
                String.valueOf(factura.cupon.codigo),
                String.valueOf(factura.documento),
                String.valueOf(factura.monto),
                String.valueOf(factura.descuento),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(factura.fecha),
                String.valueOf(factura.cupon.actualizado_por.id_empleado)
        );
        guardar_canje(respuesta);
    }
    @UiThread
    void guardar_canje(String respuesta){
        if(respuesta!=null){
            try {
                JSONObject jrespuesta=new JSONObject(respuesta);
                if(jrespuesta.getInt("code")==200){
                    factura_canje.id_factura=jrespuesta.getInt("id_factura");
                    factura_canje.save();
                    factura_canje.cupon.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        hideProgressDialog();
        showMaster(showLayout.PRINCIPAL);
    }
    boolean valido_para_canjear(){
        boolean valido=true;
        try{
            if(monto_factura.getText().toString().length()==0||
                    Double.parseDouble(monto_factura.getText().toString())<=0){
                valido=false;
                monto_factura.setError("Monto invalido");
            }
        }catch (Exception e){
            valido=false;
            monto_factura.setError("Monto invalido");
        }

        if(factura.getText().toString().length()<1){
            valido=false;
            factura.setError("Numero de factura requerido");
        }
        return valido;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(IntentIntegrator.REQUEST_CODE==requestCode && (resultCode == Activity.RESULT_OK)){
            String accion= session.getSharedValue("INTEGRATOR_ACTION");
            if(accion!=null && accion.equals("REQUEST_CODE_DESCUENTO")){
                //region LECTURA DE QR PARA ASIGNACION DE DESCUENTO
                IntentResult scanningResult =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanningResult != null) {
                    String scanContent = scanningResult.getContents();
                    Empleado empleado=Utilidades.db.get_empleado();

                    Cupon cupon =new Cupon();
                    cupon.codigo=Utilidades.getUUID();
                    cupon.codigo_usuario=scanContent;
                    cupon.descuento=descuento;
                    cupon.creado= Calendar.getInstance().getTime();
                    cupon.creado_por=empleado;
                    cupon.save();
                    cargarCupon(cupon);
                }
                //endregion
            }else if(accion!=null && accion.equals("REQUEST_CODE_DESCUENTO_CANJE")){
                //region LECTURA DE QR PARA CANJE DE DESCUENTO
                IntentResult scanningResult =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanningResult != null) {
                    String scanContent = scanningResult.getContents();
                    Cupon cupon=new Select().from(Cupon.class)
                            .where("codigo=?", scanContent)
                            .executeSingle();
                    if(cupon==null){
                        descripcion_scan.setText("Descuento Vencido o invalido\nPuede intentar escanear el descuento nuevamente");
                    }else {
                        if(cupon.esValido()){

                            Double monto=Double.parseDouble(monto_factura.getText().toString());
                            Double descuento_otorgado =cupon.descuento_otorgado(monto);

                            cupon_view.removeAllViews();
                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View inflatedLayout= inflater.inflate(R.layout.cupon, null, false);

                            TextView encabezado = (TextView) inflatedLayout.findViewById(R.id.comercio_nombre);
                            TextView porcentaje = (TextView) inflatedLayout.findViewById(R.id.porcentaje_descuento);
                            TextView condiciones=(TextView) inflatedLayout.findViewById(R.id.condiciones);
                            TextView descripcion = (TextView) inflatedLayout.findViewById(R.id.cupon_vencimiento);
                            ImageView imageView = (ImageView) inflatedLayout.findViewById(R.id.imageView);
                            descuento=cupon.descuento;

                            encabezado.setText("Descuento valido\n" +
                                    "El descuento a otorgarse es: ");
                            porcentaje.setText(String.valueOf(descuento_otorgado)  + " %");
                            descripcion.setText("Descuento en moneda local " +
                                            NumberFormat.getInstance(Locale.US).format(
                                                    monto*(descuento_otorgado/100)));

                            factura_canje=new Factura();
                            factura_canje.cupon=cupon;
                            factura_canje.comercio=Utilidades.db.get_empleado().comercio;
                            factura_canje.monto=monto;
                            factura_canje.descuento=(monto*(descuento_otorgado/100));
                            factura_canje.documento=factura.getText().toString();
                            factura_canje.fecha=new Date();
                            cupon.actualizado_por=Utilidades.db.get_empleado();
                            cupon.actualizado=factura_canje.fecha;
                            cupon.canjeado=true;

                            canjear_cupon.setVisibility(View.VISIBLE);
                            try {
                                Utilidades.cargarImageView(imageView,
                                        Utilidades.encodeAsBitmap(cupon.codigo));
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }

                            condiciones.setText(descuento.condiciones());


                            cupon_view.addView(inflatedLayout);
                        }else
                            descripcion_scan.setText("Descuento Vencido o invalido\nPuede intentar escanear el descuento nuevamente");
                    }

                }
                //endregion
            }

        }


    }
    @Background
    void cargarCupon(Cupon cupon){
        String respuesta=restClient.save_cupon(
                String.valueOf(cupon.descuento.id_descuento),
                String.valueOf(cupon.creado_por.id_empleado),
                String.valueOf(cupon.codigo_usuario),
                String.valueOf(cupon.codigo),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cupon.creado)
        );
        if(respuesta!=null){
            try {
                JSONObject jrespuesta=new JSONObject(respuesta);
                if(jrespuesta.getInt("code")==200){
                    cupon.id_cupon=jrespuesta.getInt("id_cupon");
                    cupon.save();
                }
                showTast("Cupon guardado exitosamente!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Background
    void cargarCupones(){
        List<Cupon> cupones = Utilidades.db.getCuponesPendientesCarga();
        for (Cupon cupon:cupones) {
            String respuesta=restClient.save_cupon(
                    String.valueOf(cupon.descuento.id_descuento),
                    String.valueOf(cupon.creado_por.id_empleado),
                    String.valueOf(cupon.codigo_usuario),
                    String.valueOf(cupon.codigo),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cupon.creado)
            );
            if(respuesta!=null){
                try {
                    JSONObject jrespuesta=new JSONObject(respuesta);
                    if(jrespuesta.getInt("code")==200){
                        cupon.id_cupon=jrespuesta.getInt("id_cupon");
                        cupon.save();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Background
    void cargarFacturas(){
        List<Factura> facturas = Utilidades.db.getFacturasPendientesCarga();
        for (Factura factura:facturas) {
            String respuesta=restClient.canjear_cupon(
                    String.valueOf(factura.cupon.codigo),
                    String.valueOf(factura.documento),
                    String.valueOf(factura.monto),
                    String.valueOf(factura.descuento),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(factura.fecha),
                    String.valueOf(factura.cupon.actualizado_por.id_empleado)
            );
            if(respuesta!=null){
                try {
                    JSONObject jrespuesta=new JSONObject(respuesta);
                    if(jrespuesta.getInt("code")==200){
                        factura.id_factura=jrespuesta.getInt("id_factura");
                        factura.save();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @UiThread
    void showTast(String respuesta){
        Toast.makeText(getContext(),respuesta,Toast.LENGTH_SHORT).show();
        showMaster(showLayout.PRINCIPAL);
    }

    @Background
    void cargarDescuentos(){
        Empleado empleado= Utilidades.db.get_empleado();
        String respuesta =  restClient.get_descuentos(
                String.valueOf(empleado.comercio.id_comercio));
        Utilidades.db.saveDescuentos(respuesta);
        fin_cargaDescuento();
    }
    @UiThread
    void fin_cargaDescuento(){
        showMaster(showLayout.NUEVO_DESCUENTO);
        cargarSpinnerDescuentos();
        hideProgressDialog();
    }

    @Background
    void descargarCupones(){
        Empleado empleado= Utilidades.db.get_empleado();

        String respuesta =  restClient.get_descuentos(
                String.valueOf(empleado.comercio.id_comercio));
        Utilidades.db.saveDescuentos(respuesta);

        respuesta =  restClient.get_cupones_empleado(
                String.valueOf(empleado.id_empleado));
        Utilidades.db.saveCupones(respuesta);

        respuesta =  restClient.get_facturas_empleado(
                String.valueOf(empleado.id_empleado));
        Utilidades.db.saveFacturas(respuesta);
        refrescarListaCupones();
    }
    @UiThread
    void refrescarListaCupones(){
        try{
            if(recyclerView.getAdapter()==null){
                adapter=new CuponAdapter(getContext());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.VERTICAL,
                        false);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true);
                refreshLayout.setOnRefreshListener(
                        new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                descargarCupones();
                            }
                        }
                );
                refreshLayout.setColorSchemeResources(
                        R.color.blue_800,
                        R.color.purple_600,
                        R.color.orange_800,
                        R.color.lime_A800
                );
            }
            boolean canjeado;
            if(spinner.getSelectedItemPosition()==0)//NO CANJEADO
                canjeado=false;
            else
                canjeado=true;
            if(!cktodos.isChecked()){
                if(editFecha.getText().length()>0){
                    try {
                        Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse(editFecha.getText().toString());
                        cupones = Utilidades.db.getCuponesPorFecha(fecha, canjeado);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }else
                    cupones = Utilidades.db.getCupones(canjeado);
            }else
                cupones = Utilidades.db.getCupones(canjeado);



            adapter.clear();
            adapter.addAll(cupones);
            if(refreshLayout!=null)
                refreshLayout.setRefreshing(false);
            if(txtCantidad!=null)
                txtCantidad.setText(String.valueOf(cupones.size()));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void cargarSpinnerDescuentos(){
        descuentos  =Utilidades.db.getDescuentos();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Descuento d:descuentos) {
            arrayList.add(d.nombre);
        }
        ArrayAdapter<String> localArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arrayList);
        localArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDescuento.setAdapter(localArrayAdapter);
        spDescuento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    generarQRdescuento(position);
                    descuento=descuentos.get(position);

                    containerCupon.removeAllViews();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View inflatedLayout= inflater.inflate(R.layout.item_cupon, null, false);
                    TextView porcentaje = (TextView) inflatedLayout.findViewById(R.id.textView2);
                    TextView descuento_obten_hasta = (TextView) inflatedLayout.findViewById(R.id.textView5);
                    TextView condiciones=(TextView) inflatedLayout.findViewById(R.id.condiciones);


                    porcentaje.setText(String.valueOf(descuento.porcentaje_descuento)  + " %");
                    if ((descuento.desc_compra_minima!=null && descuento.desc_compra_minima>0)
                            || descuento.desc_dia_vigencia>0)
                        descuento_obten_hasta.setText("HASTA");
                    else
                        descuento_obten_hasta.setText("OBTEN");
                    condiciones.setText(descuento.condiciones());

                    containerCupon.addView(inflatedLayout);
                } catch (WriterException e) {
                    Toast.makeText(
                            getContext(),
                            "No fue posible generar codigo",
                            Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(
                        getContext(),
                        "No fue posible generar codigo",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    void generarQRdescuento(int position) throws JSONException, WriterException {
        String qrcode= Utilidades.getUUID();
        JSONObject jqur=new JSONObject();
        Empleado empleado=Utilidades.db.get_empleado();
        Descuento descuento=descuentos.get(position);
        jqur.put("a", qrcode);
        jqur.put("b", empleado.id_empleado);
        jqur.put("c", empleado.nombre);

        jqur.put("d",empleado.comercio.id_comercio);

        jqur.put("e", descuento.id_descuento);
        jqur.put("f", descuento.nombre);
        jqur.put("g", descuento.porcentaje_descuento);
        jqur.put("h", descuento.vigencia);
        jqur.put("i", descuento.desc_dia_vigencia);
        jqur.put("j", descuento.desc_dia_vigencia_porc_inf);
        jqur.put("k", descuento.desc_dia_vigencia_porc_sup);
        jqur.put("l", descuento.desc_compra_minima);
        jqur.put("m", descuento.desc_compra_minima_porc_inf);
        jqur.put("n", descuento.desc_compra_minima_porc_sup);

        Utilidades.cargarImageView(imageView,
                Utilidades.encodeAsBitmap(jqur.toString()));
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.espere_label));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void showMaster(showLayout layou){
        try{
            switch (layou){
                case PRINCIPAL: {
                    LayautDetalle.setVisibility(View.GONE);
                    LayoutMaster.setVisibility(View.VISIBLE);
                    break;
                }case NUEVO_DESCUENTO:{
                    LayautDetalle.setVisibility(View.VISIBLE);
                    LayoutMaster.setVisibility(View.GONE);
                    break;
                }case CANJEAR_DESCUENTO:{
                    LayoutMaster.setVisibility(View.GONE);
                    LayautDetalleCanje.setVisibility(View.VISIBLE);
                    factura_canje=null;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LayautDetalle.setVisibility(View.GONE);
            LayoutMaster.setVisibility(View.VISIBLE);
        }

    }
    enum showLayout{
        PRINCIPAL,
        NUEVO_DESCUENTO,
        CANJEAR_DESCUENTO
    }
}
