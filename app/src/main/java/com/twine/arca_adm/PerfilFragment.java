package com.twine.arca_adm;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.twine.arca_adm.general.Utilidades;
import com.twine.arca_adm.general.charts.DayAxisValueFormatter;
import com.twine.arca_adm.general.charts.MyMarkerView;
import com.twine.arca_adm.general.charts.MyValueCurrencyFormatter;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Empleado;
import com.twine.arca_adm.models.Factura;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


@EFragment
public class PerfilFragment extends Fragment {

    private static final String TAG = "PerfilFragment";
    @ViewById(R.id.chart_entregado_canjeado)
    BarChart chart_entregado_canjeado;
    @ViewById(R.id.chart_ingresos)
    LineChart chart_ingresos;
    @ViewById(R.id.chart_descuentos)
    LineChart chart_descuentos;

    @ViewById(R.id.nombre)
    EditText nombre;
    @ViewById(R.id.apellido)
    EditText apellido;
    @ViewById(R.id.direccion)
    EditText direccion;
    @ViewById(R.id.telefono)
    EditText telefono;

    @ViewById(R.id.contrasenia_actual)
    EditText contrasenia_actual;
    @ViewById(R.id.contrasenia_nueva)
    EditText contrasenia_nueva;
    @ViewById(R.id.contrasenia_nueva_confir)
    EditText contrasenia_nueva_confir;

    @RestService
    RestClient restClient;
    @Bean
    MyRestErrorHandler myErrorhandler;

    Empleado empleado;
    Typeface mTfLight;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        empleado=Utilidades.db.get_empleado();
        restClient.setRestErrorHandler(myErrorhandler);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarEmleado();
    }

    @AfterViews
    void cargarDatosGenerales(){
        nombre.setText(empleado.nombre);
        apellido.setText(empleado.apellido);
        direccion.setText(empleado.direccion);
        telefono.setText(empleado.telefono);
    }
    @AfterViews
    void cargarChart(){
        mTfLight= Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        Calendar fecha=Calendar.getInstance();

        List<Factura> facturas= Utilidades.db.getFacturasxDiasMes_Empleado(
                fecha.get(Calendar.MONTH),
                fecha.get(Calendar.YEAR),
                fecha.get(Calendar.DAY_OF_MONTH),
                empleado);

        List<Cupon> cupones= Utilidades.db.getCuponesxMesAnio(
                fecha.get(Calendar.MONTH),
                fecha.get(Calendar.YEAR));

        formatBarChart(chart_entregado_canjeado);
        cargarMultiBarChart(chart_entregado_canjeado,
                getDataCuponesEntregadoCanjeado(cupones),"Cupones entregados vs Canjeados");

        formatLineChart(chart_ingresos);
        cargarLineChart(chart_ingresos,getDataIngresos(facturas),"Ingresos",R.drawable.fade_blue);

        formatLineChart(chart_descuentos);
        cargarLineChart(chart_descuentos,getDataDescuentos(facturas),"Descuentos",R.drawable.fade_geen);


    }

    @Click(R.id.cambiar_contrasenia)
    void  cambiar_contrasenia_Click(){
        if(!contrasenia_actual.getText().toString().equals(empleado.clave)){
            contrasenia_actual.setError("Contraseña actual incorrecta");
            return;
        }
        if(contrasenia_nueva.getText().toString().length()<5){
            contrasenia_nueva.setError("Contraseña nueva muy corta");
            return;
        }
        if(!contrasenia_nueva.getText().toString().equals(
                contrasenia_nueva_confir.getText().toString())){
            contrasenia_nueva.setError("Contraseña nueva no coincide con la confirmacion");
            return;
        }
        empleado.clave=contrasenia_nueva_confir.getText().toString();
        empleado.save();
        actualizarEmleado();
        Utilidades.motrarDialogoOK(getContext(),"Operación exitosa","Contraseña ha sido actualizada");

    }

    void formatBarChart(final BarChart mChart){
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.setMaxVisibleValueCount(50);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xl = mChart.getXAxis();
        xl.setGranularity(1f);
        xl.setCenterAxisLabels(true);
        xl.setValueFormatter(new DayAxisValueFormatter(mChart));

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true
        mChart.getAxisRight().setEnabled(false);
    }
    ArrayList<IBarDataSet> getDataCuponesEntregadoCanjeado(List<Cupon> cupones){

        java.util.HashMap<Long, Float> hashMap
                = new java.util.HashMap<Long, Float>(600);

        //DATOS ENTREGADO
        for (Cupon cupon:cupones) {
            Float cantidad = hashMap.get(cupon.dias_entrega());
            if(cantidad==null)
                cantidad=0f;
            cantidad++;
            hashMap.put(cupon.dias_entrega(),cantidad);
        }

        //ENTRIS ENTREGADO
        List<BarEntry> entries = new ArrayList<BarEntry>();
        Iterator myVeryOwnIterator = hashMap.keySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            Long key=(Long) myVeryOwnIterator.next();
            Float value=hashMap.get(key);
            entries.add(new BarEntry(key,value));
        }

        hashMap= new java.util.HashMap<Long, Float>(600);

        //DATOS CANJEADO
        for (Cupon cupon:cupones) {
            if(cupon.canjeado){
                Float cantidad = hashMap.get(cupon.dias_entrega());
                if(cantidad==null)
                    cantidad=0f;
                cantidad++;
                hashMap.put(cupon.dias_canje(),cantidad);
            }
        }

        //ENTRIS CANJEADO
        List<BarEntry> entries2 = new ArrayList<BarEntry>();
        myVeryOwnIterator = hashMap.keySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            Long key=(Long) myVeryOwnIterator.next();
            Float value=hashMap.get(key);
            entries2.add(new BarEntry(key,value));
        }

        BarDataSet set1, set2;

        set1 = new BarDataSet(entries, "Entregados");
        set1.setColor(Color.rgb(104, 241, 175));
        set2 = new BarDataSet(entries2, "Canjeados");
        set2.setColor(Color.rgb(164, 228, 251));


        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);

        return  dataSets;
    }
    void cargarMultiBarChart(BarChart mChart, ArrayList<IBarDataSet> dataSets, String label){

        float barWidth = 0.2f;
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            for (int i=0; i<mChart.getData().getDataSetCount(); i++) {
                BarDataSet mdataset= (BarDataSet)mChart.getData().getDataSetByIndex(i);
                mdataset.setValues(((BarDataSet)dataSets.get(i)).getValues());
            }
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            BarData data = new BarData(dataSets);
            mChart.setData(data);
        }
        float minval=0;
        for (int i=0; i<mChart.getData().getDataSetCount(); i++) {
            for (BarEntry mentry:((BarDataSet)dataSets.get(i)).getValues()) {
                if(minval>mentry.getX())
                    minval=mentry.getX();
            }
        }

        mChart.getBarData().setBarWidth(barWidth);
        //mChart.getXAxis().setAxisMinValue(startYear);
        mChart.groupBars(minval,0.4f,barWidth);
        mChart.animateXY(1000,1000);
        mChart.invalidate();
    }

    void formatLineChart(LineChart chart){
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);
        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        chart.setBackgroundColor(Color.WHITE);

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        chart.setViewPortOffsets(10, 0, 10, 0);

        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTypeface(mTfLight);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextSize(11f);
        //xAxis.setAxisMinimum(-2f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setValueFormatter(new MyValueCurrencyFormatter());
        leftAxis.setGranularityEnabled(true);

        chart.getAxisRight().setEnabled(false);

        chart.invalidate(); // refresh
        chart.animateXY(1000,1000);
    }
    List<Entry> getDataIngresos(List<Factura> facturas){
        Calendar fecha=Calendar.getInstance();
        int  diamayor= fecha.getActualMaximum(Calendar.DAY_OF_MONTH);
        java.util.HashMap<Integer, Float> hashMap
                = new java.util.HashMap<Integer, Float>(diamayor);
        for(int i=1; i<=diamayor;i++){
            hashMap.put(i,0f);
        }
        for (Factura factura:facturas) {
            Calendar fecha_factura=Calendar.getInstance();
            fecha_factura.setTime(factura.fecha);
            Float cantidad = hashMap.get(fecha_factura.get(Calendar.DAY_OF_MONTH));
            cantidad=cantidad+ Float.parseFloat( String.valueOf(factura.monto));
            hashMap.put(fecha_factura.get(Calendar.DAY_OF_MONTH),cantidad);
        }
        List<Entry> entries = new ArrayList<Entry>();
        for (int i=1; i<=hashMap.size();i++) {
            entries.add(new Entry(i,hashMap.get(i),getResources().getDrawable(R.drawable.star)));
        }
        return  entries;
    }
    List<Entry> getDataDescuentos(List<Factura> facturas){
        Calendar fecha=Calendar.getInstance();
        int  diamayor= fecha.getActualMaximum(Calendar.DAY_OF_MONTH);
        java.util.HashMap<Integer, Float> hashMap
                = new java.util.HashMap<Integer, Float>(diamayor);
        for(int i=1; i<=diamayor;i++){
            hashMap.put(i,0f);
        }
        for (Factura factura:facturas) {
            Calendar fecha_factura=Calendar.getInstance();
            fecha_factura.setTime(factura.fecha);
            Float cantidad = hashMap.get(fecha_factura.get(Calendar.DAY_OF_MONTH));
            cantidad=cantidad+ Float.parseFloat( String.valueOf(factura.descuento));
            hashMap.put(fecha_factura.get(Calendar.DAY_OF_MONTH),cantidad);
        }
        List<Entry> entries = new ArrayList<Entry>();
        for (int i=1; i<=hashMap.size();i++) {
            entries.add(new Entry(i,hashMap.get(i)));
        }
        return  entries;
    }
    void cargarLineChart(LineChart chart, List<Entry> entries, String label, int drawer){
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            LineDataSet set1= (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        }else {
            LineDataSet dataSet = new LineDataSet(entries, label); // add entries to dataset
            dataSet.setDrawIcons(false);
            dataSet.enableDashedLine(10f, 5f, 0f);
            dataSet.enableDashedHighlightLine(10f, 5f, 0f);

            dataSet.setFillColor(R.color.red_200);
            dataSet.setColor(Color.BLACK);
            dataSet.setCircleColor(Color.BLACK);
            dataSet.setLineWidth(1f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);

            dataSet.setValueTextSize(9f);
            dataSet.setValueTextColor(Color.BLACK);
            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(getContext(), drawer);
                dataSet.setFillDrawable(drawable);
            }
            else {
                dataSet.setFillColor(Color.BLACK);
            }

            //dataSet.setHighLightColor(R.color.blanco);
            //dataSet.setDrawValues(true);
            dataSet.setDrawFilled(true);
            dataSet.setHighlightEnabled(true);
            dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            dataSet.setFormSize(15.f);
            LineData lineData = new LineData(dataSet);

            chart.setData(lineData);
        }
        chart.getData().setHighlightEnabled(true);
        chart.invalidate();
    }



    @FocusChange({R.id.nombre,R.id.apellido, R.id.telefono, R.id.direccion})
    void focusChange(){
        try{
            empleado.nombre=nombre.getText().toString();
            empleado.apellido=apellido.getText().toString();
            empleado.telefono=telefono.getText().toString();
            empleado.direccion=direccion.getText().toString();
            empleado.save();
            actualizarEmleado();
        }catch (Exception e){e.printStackTrace();}
    }
    @Background
    void actualizarEmleado(){
        try{
            String respuesta=restClient.actualizar_empleado(
                    String.valueOf(empleado.id_empleado),
                    String.valueOf(empleado.nombre),
                    String.valueOf(empleado.apellido),
                    String.valueOf(empleado.direccion),
                    String.valueOf(empleado.telefono),
                    String.valueOf(empleado.usuario),
                    String.valueOf(empleado.clave),
                    String.valueOf(empleado.clave));

            Log.d(TAG, "actualizarEmleado: "+ respuesta);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
