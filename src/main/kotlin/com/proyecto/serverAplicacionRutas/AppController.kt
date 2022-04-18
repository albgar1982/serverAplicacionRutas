package com.proyecto.serverAplicacionRutas

import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.random.Random

@RestController
class AppController(private val usuarioRepository: UsuarioRepository,private val rutaRepository: RutaRepository) {

    //Devuelve "Contrasenia incorrecta" O el token
    @PostMapping("appendUser/{usuario}")
    fun appendUser(@PathVariable usuario: String, @RequestBody contrasenia: String): String {
        val token = crearToken()
        val fecha = Calendar.getInstance()
        println("La contraseña que viene en el requestBody es $contrasenia")
        val posibleUsuario = comprobarUsuario(usuario)

        if (posibleUsuario != null) {
            //si existe el usuario, comprobamos la contra. Si es buena, le renovamos el token

            if (comprobarContraseña(posibleUsuario, contrasenia)) {
                //Si la contra es buena, le doy el token nuevo, lo salvo y devuelvo el token para que siga hacia elegir ruta
                posibleUsuario.token = token
                posibleUsuario.fecha = fecha //le doy una nueva hora, para que se resetee la caducidad del token
                usuarioRepository.save(posibleUsuario) //actualizo ese usuario en la database
            } else //Si no, devuelvo:
                return "Contrasenia incorrecta"
        } else {
            //Si no existe el usuario, lo creo y lo salvo
            val user = Usuario(usuario, contrasenia, token, fecha, rutaRepository.findAll())
            usuarioRepository.save(user)
        }
        usuarioRepository.findAll().forEach {
            println("Estos son todos los usuarios en la database:")
            println(it)
        }
        return token
    }

    //Devuelve "ERROR" o el usuario en json
    @GetMapping("conseguirUsuario/{token}")
    fun conseguirUsuario(@PathVariable token: String) :String{
        val usuario = encontrarUsuario(token) //Devuelve cadena vacía o nombre de usuario
        return if(usuario.isEmpty())
            "ERROR"
        else {
            println("Voy a devolver ${usuarioRepository.getById(usuario)}")
            usuarioRepository.getById(usuario).toString()
        }
    }

    /*
    @GetMapping("solicitarUsuarioConToken/{token}")
    fun solicitarUsuarioConToken(@PathVariable token: String): String {
        var pregunta = ""
        val usuario = usuarioRepository.getById(encontrarUsuario(token))
        val tokenCaducado = checkTokenTime(usuario)
        if (tokenCaducado) {
            println("ERROR")
            return "ERROR"
        } else {
            if (usuario.idPreguntas.size == 0)
                return "No quedan preguntas"
            usuario.fecha=Calendar.getInstance()
            userRepository.save(usuario)
            val numPregunta = usuario.idPreguntas[Random.nextInt(0, usuario.idPreguntas.size)]
            var i = 0
            var salir = false
            do {
                if (Repositorio.preguntasYrespuestas[i].identificador == numPregunta) {
                    salir = true
                    pregunta = Repositorio.preguntasYrespuestas[i].toString()
                    usuario.idPreguntas.remove(numPregunta)
                    userRepository.save(usuario)
                } else
                    i++
            } while (!salir && i < Repositorio.preguntasYrespuestas.size)


            val objetoJson = JSONObject(pregunta)
            objetoJson.remove("respuestaCorrecta")
            println(objetoJson)
            return objetoJson.toString()
        }
    }

     */


    private fun comprobarContraseña(usuario: Usuario, contrasenia: String): Boolean {
        return usuario.password == contrasenia
    }

    private fun comprobarUsuario(usuario: String): Usuario? {
        var user: Usuario? = null
        val listaUsuarios = usuarioRepository.findAll()
        var i = 0
        var salir = false
        do {
            if (listaUsuarios.isEmpty())
                salir = true
            else {
                if (listaUsuarios[i].nombre == usuario) {
                    user = listaUsuarios[i]
                    salir = true
                } else
                    i++
            }
        } while (!salir && i < listaUsuarios.size)
        return user
    }

    private fun encontrarUsuario(token: String): String {
        var idUsuario = ""
        val listaUsuarios = usuarioRepository.findAll()
        var i = 0
        var salir = false
        do {
            if (listaUsuarios[i].token == token) {
                println("He encontrado el token $token. El del usuario es: ${listaUsuarios[i].token}. El usuario se llama ${listaUsuarios[i].nombre}")
                idUsuario = listaUsuarios[i].nombre
                salir = true
            } else
                i++
        } while (!salir && i < listaUsuarios.size)
        return idUsuario
    }

    private fun crearToken(): String {
        var token = ""
        repeat(6) {
            token += Char(Random.nextInt(97, 123))
        }
        return token
    }

    private fun checkTokenTime(usuario: Usuario): Boolean {
        var caducado = false

        val momentoActual = Calendar.getInstance()
        val segundosActuales = momentoActual.timeInMillis
        val segundosToken = usuario.fecha.timeInMillis
        val tiempoTranscurrido = segundosActuales - segundosToken
        if (tiempoTranscurrido.toFloat() / (1000 * 60) > 5.00)
            caducado = true
        println("Han pasado ${tiempoTranscurrido.toFloat()/ (1000 * 60)} minutos")

        return caducado
    }

   /* private fun encontrarUsuario(token: String): Int {
        var idUsuario = 0
        val listaUsuarios = usuarioRepository.findAll()
        var i = 0
        var salir = false
        do {
            if (listaUsuarios[i].token == token) {
                println("He encontrado el token $token. El del usuario es: ${listaUsuarios[i].token} en la posición $i de la lista de la database")
                idUsuario = listaUsuarios[i].id
                salir = true
            } else
                i++
        } while (!salir && i < listaUsuarios.size)
        return idUsuario
    }*/


    @GetMapping("getListUbicaciones")
    fun getListaDePreguntas():String{
        var listaRutas=""
        rutaRepository.findAll().forEach {
            listaRutas+=it.toString()
        }

        return listaRutas
    }


    @PostMapping("guardar")
    fun guardarProgreso(@RequestBody usuario: Usuario):String{
        usuarioRepository.save(usuario)
        return  "Progreso Guardado"
    }
}