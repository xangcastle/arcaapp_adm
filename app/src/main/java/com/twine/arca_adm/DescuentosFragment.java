package com.twine.arca_adm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.twine.arca_adm.general.Utilidades;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Descuento;
import com.twine.arca_adm.models.Empleado;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
    @ViewById(R.id.imageView)
    ImageView imageView;
    @ViewById(R.id.containerCupon)
    LinearLayout containerCupon;

    List<Descuento> descuentos;
    Descuento descuento;

    List<Cupon> cupons;

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
        restClient.setRestErrorHandler(myErrorhandler);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_descuentos, container, false);
    }



    @Click(R.id.fab)
    void nuevoDescuentoClick(){
        showProgressDialog();
        cargarDescuentos();
        //readQR();
    }
    @Click(R.id.readQR)
    void readQR(){
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        Intent scanIntent=integrator.createScanIntent();
        startActivityForResult(scanIntent,IntentIntegrator.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(IntentIntegrator.REQUEST_CODE==requestCode && (resultCode == Activity.RESULT_OK)){
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
                saveCupon(cupon);
                saveCupon("Cupon generado exitosamente!");
            }
        }
    }
    @Background
    void saveCupon(Cupon cupon){
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
                saveCupon("Cupon guardado exitosamente!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @UiThread
    void saveCupon(String respuesta){
        Toast.makeText(getContext(),respuesta,Toast.LENGTH_SHORT).show();
        showMaster(false);
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
        showMaster(true);
        cargarSpinnerDescuentos();
        hideProgressDialog();
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

    private void showMaster(Boolean mostrar){
        if(mostrar){
            LayautDetalle.setVisibility(View.VISIBLE);
            LayoutMaster.setVisibility(View.GONE);
        }else {
            LayautDetalle.setVisibility(View.GONE);
            LayoutMaster.setVisibility(View.VISIBLE);
        }
    }
}
