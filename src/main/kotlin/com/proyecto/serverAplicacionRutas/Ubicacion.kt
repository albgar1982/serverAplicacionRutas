package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Ubicacion(@Id var nombreCoordenada:String, var latitud:Double,var longitud:Double, var pista:String) {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}