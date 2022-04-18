package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson
import java.util.*
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Usuario(@Id var nombre:String, var password:String, var token:String, var fecha: Calendar, @ElementCollection var listaRutas:List<Ruta>) {

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}