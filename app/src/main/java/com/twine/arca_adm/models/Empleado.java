package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */
@Table(name = "empleados")
public class Empleado extends Model {
    @Column(name = "id_empleado")
    public int id_empleado;

    @Column(name = "nombre")
    public String nombre;

    @Column(name = "apellido")
    public String apellido;

    @Column(name = "direccion")
    public String direccion;

    @Column(name = "telefono")
    public String telefono;

    @Column(name = "fecha_alta")
    public Date fecha_alta;
    
    @Column(name = "usuario")
    public String usuario;

    @Column(name = "clave")
    public String clave;

    @Column(name = "fecha_baja")
    public Date fecha_baja;

    @Column(name = "pk_comercio")
    public Comercio comercio;

    @Column(name = "activo")
    public boolean activo;
}
