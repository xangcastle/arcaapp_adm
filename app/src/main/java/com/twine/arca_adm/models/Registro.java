package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by Jose Williams Garcia on 17/5/2017.
 */

@Table(name = "log_interno")
public class Registro extends Model {
    @Column(name = "registro")
    public   String registro;
    @Column (name = "cargado")
    public boolean cargado;
    @Column (name = "fecha")
    public Date fecha;
}
