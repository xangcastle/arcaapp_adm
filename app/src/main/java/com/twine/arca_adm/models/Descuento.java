package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */
@Table(name = "descuentos")
public class Descuento extends Model {
    @Column(name = "id_descuento")
    public int id_descuento;
    @Column(name = "nombre")
    public String nombre;
    @Column(name = "porcentaje_descuento")
    public Double porcentaje_descuento;
    @Column(name = "vigencia")
    public int vigencia;

    @Column(name = "desc_dia_vigencia")
    public int desc_dia_vigencia;
    @Column(name = "desc_dia_vigencia_porc_inf")
    public Double desc_dia_vigencia_porc_inf;
    @Column(name = "desc_dia_vigencia_porc_sup")
    public Double desc_dia_vigencia_porc_sup;

    @Column(name = "desc_compra_minima")
    public Double desc_compra_minima;
    @Column(name = "desc_compra_minima_porc_inf")
    public Double desc_compra_minima_porc_inf;
    @Column(name = "desc_compra_minima_porc_sup")
    public Double desc_compra_minima_porc_sup;

    @Column(name = "tipo_cambio")
    public Double tipo_cambio;

    public String condiciones(){
        String condicion="";
        if(desc_dia_vigencia==0 && (desc_compra_minima==null ||desc_compra_minima==0)){
            condicion="Obten un " + String.valueOf(this.porcentaje_descuento) + "% de descuento " +
                    "en tu proxima compra dentro de " + String.valueOf(this.vigencia) + " días";
        }else if(desc_dia_vigencia>0 && (desc_compra_minima==null ||desc_compra_minima==0)) {
            condicion="Obten hasta  un " + String.valueOf(this.porcentaje_descuento) + "% de descuento. \n" +
                    "Si tu compra es antes de " + String.valueOf(this.desc_dia_vigencia) + " días " +
                    "recibes "  + String.valueOf(this.desc_dia_vigencia_porc_inf) + "% de descuento.\n" +
                    "Si tu compra es despues de " + String.valueOf(this.desc_dia_vigencia) + " días " +
                    "recibes "  + String.valueOf(this.desc_dia_vigencia_porc_sup) + "% de descuento.";
        }else if(desc_dia_vigencia==0 && (desc_compra_minima!=null ||desc_compra_minima>0)) {
            condicion="Obten hasta  un " + String.valueOf(this.porcentaje_descuento) + "% de descuento. \n" +
                    "Si tu compra es mayor a " + String.valueOf(this.desc_compra_minima) + " US$ " +
                    "recibes "  + String.valueOf(this.desc_compra_minima_porc_sup) + "% de descuento.\n" +
                    "Si tu compra es menor a " + String.valueOf(this.desc_dia_vigencia) + " US$ " +
                    "recibes "  + String.valueOf(this.desc_compra_minima_porc_inf) + "% de descuento.";
        }
        return condicion;
    }
}
