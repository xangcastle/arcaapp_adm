package com.twine.arca_adm.adapters;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.twine.arca_adm.R;
import com.twine.arca_adm.models.Cupon;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Jose Williams Garcia on 5/5/2017.
 */
@EViewGroup(R.layout.item_cupon)
public class CuponIItemView extends LinearLayout implements ViewWrapper.Binder<Cupon> {

    //@ViewById(R.id.NombreCategoria)
    //TextView NombreCategoria;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bean
    CuponAdapter adapter;

    public CuponIItemView(Context context) {
        super(context);
    }

    @Override
    public void onBind(Cupon model) {
        /*if(model.nombre!=null){
            //NombreCategoria.setText(model.nombre);
        }
        adapter=new ComercioAdapter(getContext());
        List<Comercio> comercios = Utilidades.db.getComerciobyCategoria(model);
        adapter.addAll(comercios);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);*/
    }
}