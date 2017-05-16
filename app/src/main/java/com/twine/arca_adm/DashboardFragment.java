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
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.twine.arca_adm.general.Utilidades;
import com.twine.arca_adm.general.charts.DayAxisValueFormatter;
import com.twine.arca_adm.general.charts.MyMarkerView;
import com.twine.arca_adm.general.charts.MyValueCurrencyFormatter;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Factura;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@EFragment
public class DashboardFragment extends Fragment {

    @ViewById(R.id.chart_visitas_potenciales)
    BarChart chart_visitas_potenciales;
    @ViewById(R.id.chart_entregado_canjeado)
    BarChart chart_entregado_canjeado;
    @ViewById(R.id.chart_ingresos)
    LineChart chart_ingresos;
    @ViewById(R.id.chart_descuentos)
    LineChart chart_descuentos;
    @ViewById(R.id.chart_cupones_estado)
    PieChart chart_cupones_estado;
    Typeface mTfLight;
    private OnFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }


    @AfterViews
    void cargarChart(){
        mTfLight=Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");

        Calendar fecha=Calendar.getInstance();
        List<Factura> facturas= Utilidades.db.getFacturasxMesAnio(
                fecha.get(Calendar.MONTH),
                fecha.get(Calendar.YEAR));

        List<Cupon> cupones_potenciales= Utilidades.db.getCuponesxPotenciales();

        formatBarChart(chart_visitas_potenciales);
        cargarBarChart(chart_visitas_potenciales,getDataVisitasPotenciales(cupones_potenciales),"VIsitas potenciales");
        //prueba(chart_visitas_potenciales);

        List<Cupon> cupones= Utilidades.db.getCuponesxMesAnio(
                fecha.get(Calendar.MONTH),
                fecha.get(Calendar.YEAR));

        formatBarChart(chart_entregado_canjeado);
        cargarMultiBarChart(chart_entregado_canjeado,getDataCuponesEntregadoCanjeado(cupones),"Cupones entregados vs Canjeados");

        formatLineChart(chart_ingresos);
        cargarLineChart(chart_ingresos,getDataIngresos(facturas),"Ingresos",R.drawable.fade_blue);
        formatLineChart(chart_descuentos);
        cargarLineChart(chart_descuentos,getDataDescuentos(facturas),"Descuentos",R.drawable.fade_geen);



        formatPieChart(chart_cupones_estado);
        cargarPieChart(chart_cupones_estado,getDataCuponesEstado(cupones),"Distrubucion de Cupones");

    }

    void  prueba(BarChart barChart){
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        //barChart.setDescription("");
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xl = barChart.getXAxis();
        xl.setGranularity(1f);
        xl.setCenterAxisLabels(true);
        xl.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }

        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true
        barChart.getAxisRight().setEnabled(false);

        //data
        float groupSpace = 0.04f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.46f; // x2 dataset
        // (0.46 + 0.02) * 2 + 0.04 = 1.00 -> interval per "group"

        int startYear = 1980;
        int endYear = 1985;


        List<BarEntry> yVals1 = new ArrayList<BarEntry>();
        List<BarEntry> yVals2 = new ArrayList<BarEntry>();


        for (int i = startYear; i < endYear; i++) {
            yVals1.add(new BarEntry(i, 0.4f));
        }

        for (int i = startYear; i < endYear; i++) {
            yVals2.add(new BarEntry(i, 0.7f));
        }


        BarDataSet set1, set2;

        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet)barChart.getData().getDataSetByIndex(0);
            set2 = (BarDataSet)barChart.getData().getDataSetByIndex(1);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            // create 2 datasets with different types
            set1 = new BarDataSet(yVals1, "Company A");
            set1.setColor(Color.rgb(104, 241, 175));
            set2 = new BarDataSet(yVals2, "Company B");
            set2.setColor(Color.rgb(164, 228, 251));

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);

            BarData data = new BarData(dataSets);
            barChart.setData(data);
        }

        barChart.getBarData().setBarWidth(barWidth);
        barChart.getXAxis().setAxisMinValue(startYear);
        barChart.groupBars(startYear, groupSpace, barSpace);
        barChart.invalidate();
    }

    void formatBarChart(final BarChart mChart){
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        //barChart.setDescription("");
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

        /*mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mChart);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(xAxisFormatter);

        //IAxisValueFormatter custom = new MyValueCurrencyFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(8, false);
        //leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        mChart.invalidate(); // refresh
        mChart.animateXY(1000,1000);*/
    }
    List<BarEntry> getDataVisitasPotenciales(List<Cupon> cupones){

        java.util.HashMap<Long, Float> hashMap
                = new java.util.HashMap<Long, Float>(600);


        for (Cupon cupon:cupones) {
            Calendar fecha_cupon=Calendar.getInstance();
            //SE TOMA COMO REFERENCIA DE VISITA POTENCIAL EL VENCIMIENTO DEL CUPON
            fecha_cupon.setTime(cupon.fecha_vence());
            Date fecha= Utilidades.getDate(fecha_cupon.get(Calendar.YEAR),
                    fecha_cupon.get(Calendar.MONTH),
                    fecha_cupon.get(Calendar.DAY_OF_MONTH)
                    );

            Float cantidad = hashMap.get(fecha.getTime());
            if(cantidad==null)
                cantidad=0f;
            cantidad++;
            hashMap.put(cupon.vence_en(),cantidad);
        }
        List<BarEntry> entries = new ArrayList<BarEntry>();
        Iterator myVeryOwnIterator = hashMap.keySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            Long key=(Long) myVeryOwnIterator.next();
            Float value=hashMap.get(key);
            entries.add(new BarEntry(key,value));
        }
        return  entries;
    }
    void cargarBarChart(BarChart mChart, List<BarEntry> entries, String label){

        float barWidth = 0.2f;
        int startYear = 0;
        try{
            startYear =Integer.parseInt(String.valueOf(entries.get(0).getX()));
            for (BarEntry entry:entries) {
                if (entry.getX()<startYear ){
                    startYear=Integer.parseInt(String.valueOf(entry.getX()));
                }
            }
        }catch (Exception e){e.printStackTrace();}

        BarDataSet set1;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create 2 datasets with different types
            set1 = new BarDataSet(entries, label);
            set1.setColor(Color.rgb(104, 241, 175));
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            mChart.setData(data);
        }

        mChart.getBarData().setBarWidth(barWidth);
        //mChart.getXAxis().setAxisMinValue(startYear);
        mChart.animateXY(1000,1000);
        mChart.invalidate();
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


    void formatPieChart(PieChart mChart){
        mChart.getDescription().setEnabled(false);
        mChart.setUsePercentValues(true);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        //mChart.setCenterTextTypeface(mTfLight);
        //mChart.setCenterText(generateCenterSpannableText());


        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);
        mChart.setBackgroundColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);

        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTypeface(mTfLight);
        mChart.setEntryLabelTextSize(12f);
    }
    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }
    List<PieEntry> getDataCuponesEstado(List<Cupon> cupones){
        java.util.HashMap<String, Float> hashMap
                = new java.util.HashMap<String, Float>(3);
        hashMap.put("Canjeado",0f);
        hashMap.put("No canjeado",0f);
        hashMap.put("Vencido",0f);

        for (Cupon cupon:cupones) {
            Float cantidad = hashMap.get(cupon.estado());
            cantidad++;
            hashMap.put(cupon.estado(),cantidad);
        }
        List<PieEntry> entries = new ArrayList<>();


        Iterator myVeryOwnIterator = hashMap.keySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            String key=(String)myVeryOwnIterator.next();
            Float value=hashMap.get(key);
            entries.add(new PieEntry(value,key+" (" + + value + ")"));
        }
        return  entries;
    }
    void cargarPieChart(PieChart chart, List<PieEntry> entries, String label){
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            PieDataSet set1= (PieDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        }else {
            PieDataSet  dataSet = new PieDataSet(entries, ""); // add entries to dataset
            dataSet.setDrawIcons(false);
            dataSet.setDrawIcons(false);

            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);

            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());

            dataSet.setColors(colors);
            //dataSet.setSelectionShift(0f);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);
            data.setValueTypeface(mTfLight);
            chart.setData(data);

            // undo all highlights
            chart.highlightValues(null);
        }
        chart.getData().setHighlightEnabled(true);
        chart.invalidate();
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
