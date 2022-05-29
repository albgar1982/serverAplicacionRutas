package com.proyecto.serverAplicacionRutas

import com.google.gson.Gson

data class Token(var tokenParaSeleccion:String) {
    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}