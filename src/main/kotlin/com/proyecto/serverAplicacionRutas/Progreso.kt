package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Progreso(var usuarioId:String, var rutaId:String, var pistaActual:Int){

    @Id
    @GeneratedValue
    var id= 0

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}