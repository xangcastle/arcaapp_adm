package com.twine.arca_adm.adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Filter;

import com.twine.arca_adm.models.Cupon;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Jose Williams Garcia on 5/5/2017.
 */
@EBean
public class CuponCanjeadoAdapter extends RecyclerViewAdapterBase<Cupon, CuponCanjeadoIItemView>  {

    @RootContext
    Context context;

    public CuponCanjeadoAdapter(Context context ) {
        this.context = context;
    }

    @Override
    protected CuponCanjeadoIItemView onCreateItemView(ViewGroup parent, int viewType) {
        return CuponCanjeadoIItemView_.build(context);
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
