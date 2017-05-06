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
public class CuponAdapter extends RecyclerViewAdapterBase<Cupon, CuponIItemView>  {

    @RootContext
    Context context;

    public CuponAdapter(Context context ) {
        this.context = context;
    }

    @Override
    protected CuponIItemView onCreateItemView(ViewGroup parent, int viewType) {
        return CuponIItemView_.build(context);
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
