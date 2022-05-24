package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson


data class RutaYProgreso(var nombre:String, var listaUbicaciones:List<Ubicacion>, var pistaActual:Int) {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}