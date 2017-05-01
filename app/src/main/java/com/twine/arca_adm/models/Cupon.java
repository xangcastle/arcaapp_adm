package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */
@Table(name = "cupones")
public class Cupon extends Model {
    @Column(name = "id_cupon")
    public int id_cupon;
    @Column(name = "pk_descuento")
    public Descuento descuento;
    @Column(name = "codigo")
    public String codigo;
    @Column(name = "codigo_usuario")
    public String codigo_usuario;
    @Column(name = "canjeado")
    public boolean canjeado;
    @Column(name = "creado")
    public Date creado;
    @Column(name = "actualizado")
    public Date actualizado;
    @Column(name = "creado_por")
    public Empleado creado_por;
    @Column(name = "actualizado_por")
    public Empleado actualizado_por;
}
