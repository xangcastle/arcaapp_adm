package com.twine.arca_adm.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Jose Williams Garcia on 5/5/2017.
 */


public class ViewWrapper<T, V extends View & ViewWrapper.Binder<T>> extends RecyclerView.ViewHolder {

    private V view;

    public ViewWrapper(V itemView) {
        super(itemView);
        view = itemView;
    }

    public V getView() {
        return view;
    }

    public interface Binder<T> {
        void onBind(T data);
    }
}
