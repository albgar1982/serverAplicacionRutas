package com.proyecto.serverAplicacionRutas

import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.random.Random

@RestController
class AppController(private val usuarioRepository: UsuarioRepository, private val rutaRepository: RutaRepository, private val ubicacionRepository: UbicacionRepository, private val progresoRepository: ProgresoRepository) {

    //REQUEST CERO
    //curl -v localhost:8082/mostrarBaseDatos
    // Solo para probar que to.do está bien salvado
    @GetMapping("mostrarBaseDatos")
    fun mostrarBaseDatos() {
        rutaRepository.findAll().forEach { ruta ->
            println("\nLa ruta se llama " + ruta.nombre + " y contiene las siguientes ubicaciones:\n")
            ruta.listaUbicaciones.forEach { ubicacion ->
                println(ubicacion.nombreCoordenada)
            }
        }
    }


    //REQUEST UNO
    //Se llama desde LoginActivityViewModel
    //La contraseña va por RequestBody porque puede incluir to.do tipo de caracteres que harían que fallase si no lo enviáramos así
    //Devuelve un String: "Contrasenia incorrecta" o el token
    @PostMapping("loguear/{nombreUsuario}")
    fun loguear(@PathVariable nombreUsuario: String, @RequestBody contrasenia: String): String {
        val token = crearToken()
        val fecha = Calendar.getInstance()
        println("La contraseña que viene en el requestBody es $contrasenia")
        val posibleUsuario = comprobarUsuario(nombreUsuario)

        if (posibleUsuario != null) {
            //si existe el usuario, comprobamos la contra...
            posibleUsuario.let {
                if (comprobarContraseña(it, contrasenia)) {
                    //Si la contra es buena, le doy el token nuevo, lo salvo y devuelvo el token para que siga hacia elegir ruta
                    it.token = token
                    it.fecha = fecha //le doy una nueva hora, para que se resetee la caducidad del token
                    usuarioRepository.save(it) //actualizo ese usuario en la database
                } else //Si no, devuelvo:
                    return "Contrasenia incorrecta"
            }
        } else {
            //Si no existe el usuario, lo creo y lo salvo
            val user = Usuario(nombreUsuario, contrasenia, token, fecha, 0, 0)
            usuarioRepository.save(user)
        }
        println("Estos son todos los usuarios en la database:")
        usuarioRepository.findAll().forEach {
            println(it)
        }
        return token
    }


    //REQUEST DOS
    //Se llama desde SeleccionRutaViewModel
    //Devuelve la lista con los NOMBRES de las rutas
    @GetMapping("getListRutas")
    fun getListRutas(): String {
        val listaRutas = mutableListOf<String>()
        rutaRepository.findAll().forEach {
            listaRutas += it.nombre
        }

        println("Devuelvo la siguiente lista de rutas: $listaRutas")
        return Rutas(listaRutas).toString()
    }


    //REQUEST TRES
    //Devuelve "error" si el usuario no existe, si el token es falso o si el token está caducado (la app volverá a LoginActivity)
    //Si to.do está bien, devolverá un objeto RutaYProgreso con posic 0 si nunca ha recogido una llave o con la posic de la llave que le toque si ya ha recogido alguna
    @GetMapping("getProgress/{nombreUsuario}/{ruta}/{token}")
    fun getProgress(@PathVariable nombreUsuario: String, @PathVariable ruta: String, @PathVariable token: String): String {
        println("usuario: "+nombreUsuario)
        println("ruta: "+ruta)
        println("token: "+token)


        if (usuarioRepository.findByIdOrNull(nombreUsuario) == null)
            return "error"
        else {
            val usuario = usuarioRepository.getById(nombreUsuario)
            if (usuario.token != token)
                return "error"
            else {
                if (checkTokenTime(usuario))
                    return "error"
                else {
                    //Si no está caducado, renuevo la fecha del usuario y lo salvo
                    usuario.fecha = Calendar.getInstance()
                    usuarioRepository.save(usuario)
                    val objetoRuta = rutaRepository.getById(ruta)
                    var posic = 0

                    progresoRepository.findAll().forEach {
                        //Comprobamos si esa ruta está en ProgresoRepository. Si está, comprobamos el usuario.
                        if (it.rutaId == ruta && it.usuarioId == nombreUsuario) {
                            //Si está, cogemos la posición de la llave
                            posic = it.pistaActual
                            return RutaYProgreso(objetoRuta.nombre, objetoRuta.listaUbicaciones, posic).toString()
                            //Si no está la ruta aún no creamos esa fila, ya lo haremos cuando coja la primera llave; devolvemos posición 0
                        }
                    }
                    return RutaYProgreso(objetoRuta.nombre, objetoRuta.listaUbicaciones, posic).toString()
                }
            }
        }

    }

    //REQUEST CUATRO
    @PostMapping("salvarProgreso/{nombreUsuario}/{ruta}")
    fun salvarProgreso(@PathVariable nombreUsuario: String, @PathVariable ruta: String) {
        progresoRepository.findAll().forEach {
            //Comprobamos si esa ruta está en ProgresoRepository. Si está, comprobamos el usuario.
            if (it.rutaId == ruta && it.usuarioId == nombreUsuario) {
                it.pistaActual++
                if(it.pistaActual==3){
                    it.pistaActual=0
                    usuarioRepository.getById(nombreUsuario).llaves++
                    usuarioRepository.getById(nombreUsuario).rutas++
                    println("El user ${usuarioRepository.getById(nombreUsuario).nombre} tiene ${usuarioRepository.getById(nombreUsuario).llaves} llaves y ha completado ${usuarioRepository.getById(nombreUsuario).rutas} rutas")
                }
            }
            else
                progresoRepository.save(Progreso(nombreUsuario,ruta,1))
        }
    }
/*
    usuarioRepository.findAll().forEach {
        if(it.equals("alber"))
            usuarioRepository.delete(it)
    }

 */


    //Devuelve "ERROR" o el usuario en json
    @GetMapping("conseguirUsuario/{token}")
    fun conseguirUsuario(@PathVariable token: String): String {
        val usuario = encontrarUsuario(token) //Devuelve cadena vacía o nombre de usuario
        return if (usuario.isEmpty())
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

    private fun comprobarUsuario(nombreUsuario: String): Usuario? {
        var user: Usuario? = null
        val listaUsuarios = usuarioRepository.findAll()
        var i = 0
        var salir = false
        do {
            if (listaUsuarios.isEmpty())
                salir = true
            else {
                if (listaUsuarios[i].nombre == nombreUsuario) {
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
        println("Han pasado ${tiempoTranscurrido.toFloat() / (1000 * 60)} minutos")

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


    @PostMapping("guardar")
    fun guardarProgreso(@RequestBody usuario: Usuario): String {
        usuarioRepository.save(usuario)
        return "Progreso Guardado"
    }
}