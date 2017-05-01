package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */
@Table(name = "comercios")
public class Comercio extends Model {
    @Column(name = "id_comercio")
    public int id_comercio;
    @Column(name = "nombre")
    public String nombre;
}
