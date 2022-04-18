package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson
import javax.persistence.Embeddable

@Embeddable
data class Ubicacion(var nombreCoordenada:String, var latitud:Double,var longitud:Double, var pista:String, var coleccionado:Boolean) {

    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)
    }
}