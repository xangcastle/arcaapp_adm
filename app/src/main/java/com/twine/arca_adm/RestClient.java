package com.twine.arca_adm;

import com.twine.arca_adm.general.Utilidades;

import org.androidannotations.rest.spring.annotations.Field;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */
@Rest(rootUrl = Utilidades.BASE_URL,
        converters = {FormHttpMessageConverter.class, StringHttpMessageConverter.class, GsonHttpMessageConverter.class})
public interface RestClient extends RestClientErrorHandling, RestClientHeaders {
    @Get("/arca/get_empleado/?username={username}&password={password}")
    String get_empleado(@Path String username, @Path String password);

    @Get("/arca/get_descuentos/?id_comercio={id_comercio}")
    String get_descuentos(@Path String id_comercio);

    @Get("/arca/get_cupones_empleado/?id_empleado={id_empleado}")
    String get_cupones_empleado(@Path String id_empleado);

    @Get("/arca/get_facturas_empleado/?id_empleado={id_empleado}")
    String get_facturas_empleado(@Path String id_empleado);

    @Get("/arca/generar_cupon/?descuento={descuento}&id_empleado={id_empleado}")
    String generar_cupon(@Path String descuento, @Path String id_empleado);

    @Post("/arca/save_cupon/")
    String save_cupon(@Field String descuento,
                      @Field String id_empleado,
                      @Field String codigo_usuario,
                      @Field String codigo,
                      @Field String creado);

    @Post("/arca/canjear_cupon/")
        String canjear_cupon(@Field String codigo_cupon,
                         @Field String factura,
                         @Field String monto,
                         @Field String descuento,
                         @Field String actualizado,
                         @Field String id_empleado);
}
