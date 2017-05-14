package com.twine.arca_adm.adapters;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twine.arca_adm.R;
import com.twine.arca_adm.models.Cupon;
import com.twine.arca_adm.models.Descuento;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Jose Williams Garcia on 5/5/2017.
 */
@EViewGroup(R.layout.item_cupon)
public class CuponIItemView extends LinearLayout implements ViewWrapper.Binder<Cupon> {

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    @ViewById(R.id.textView2)
    TextView porcentaje;
    @ViewById(R.id.textView5)
    TextView descuento_obten_hasta;
    @ViewById(R.id.condiciones)
    TextView condiciones;

    public CuponIItemView(Context context) {
        super(context);
    }

    @Override
    public void onBind(Cupon model) {
        try {
            Descuento descuento= model.descuento;
            porcentaje.setText(String.valueOf(descuento.porcentaje_descuento)  + " %");
            if ((descuento.desc_compra_minima!=null && descuento.desc_compra_minima>0)
                    || descuento.desc_dia_vigencia>0)
                descuento_obten_hasta.setText("HASTA");
            else
                descuento_obten_hasta.setText("OBTEN");
            condiciones.setText(descuento.condiciones());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}