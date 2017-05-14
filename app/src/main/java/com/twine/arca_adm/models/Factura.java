package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by Jose Williams Garcia on 13/5/2017.
 */
@Table(name = "facturas")
public class Factura extends Model {
    @Column(name = "id_factura")
    public int id_factura;
    @Column(name = "documento")
    public String documento;
    @Column(name = "pk_comercio")
    public Comercio comercio;
    @Column(name = "monto")
    public Double monto;
    @Column(name = "descuento")
    public Double descuento;
    @Column(name = "pk_cupon")
    public Cupon cupon;
    @Column(name = "fecha")
    public Date fecha;
}
