package com.twine.arca_adm.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Calendar;
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

    public long vence_en(){
        try{
            Calendar day = Calendar.getInstance();
            long diff = day.getTime().getTime() -this.creado.getTime() ;
            long segundos = diff / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            long dias = horas / 24;
            return this.descuento.vigencia- dias;
        }catch (NullPointerException ex){
            return 0;
        }

    }
    public boolean esValido(){
        long vencimiento=vence_en();
        if(vencimiento<0)
            return false;
        else
            return true;
    }
    public double descuento_otorgado(double monto_factura){
        if(this.descuento.desc_dia_vigencia==0 && (this.descuento.desc_compra_minima==null ||this.descuento.desc_compra_minima==0)){
            return this.descuento.porcentaje_descuento;
        }else if(this.descuento.desc_dia_vigencia>0 && (this.descuento.desc_compra_minima==null ||this.descuento.desc_compra_minima==0)) {
            if(vence_en()<this.descuento.desc_dia_vigencia)
                return this.descuento.desc_dia_vigencia_porc_inf;
            else
                return this.descuento.desc_dia_vigencia_porc_sup;
        }else if(this.descuento.desc_dia_vigencia==0 && (this.descuento.desc_compra_minima!=null ||this.descuento.desc_compra_minima>0)) {
            if(monto_factura>this.descuento.desc_compra_minima)
                return this.descuento.desc_compra_minima_porc_sup;
            else
                return this.descuento.desc_compra_minima_porc_inf;
        }else //SI NO QUEDA EN NINGUNA CONDICION PRO DEFECTO SE ENNVIA EL DESCUENTO GENERAL
            return this.descuento.porcentaje_descuento;
    }
}
