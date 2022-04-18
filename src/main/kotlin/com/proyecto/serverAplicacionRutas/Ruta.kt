package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Ruta(@Id var nombre:String, @ElementCollection var listaUbicaciones:List<Ubicacion>, var seleccionada:Boolean, var completada:Boolean) {
    override fun toString(): String {
        val gson = Gson()

        return gson.toJson(this)

    }
}