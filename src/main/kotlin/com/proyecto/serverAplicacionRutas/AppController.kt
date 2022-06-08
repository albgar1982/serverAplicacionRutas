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
        println("usuario: $nombreUsuario")
        println("ruta: $ruta")
        println("token: $token")


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

                    progresoRepository.findAll().forEach {
                        //Comprobamos si esa ruta está en ProgresoRepository. Si está, comprobamos el usuario.
                        if (it.rutaId == ruta && it.usuarioId == nombreUsuario) {
                            val usua=usuarioRepository.getById(nombreUsuario)
                            //Si están ambos, devolvemos la pista que toca:
                            return RutaYProgreso(objetoRuta.nombre, objetoRuta.listaUbicaciones, it.pistaActual,usua.llaves,usua.rutas).toString()
                            //NOTA: esta pista podría también ser cero en el caso de que no fuera la primera vez que juega esa ruta
                        }
                    }
                    val usua=usuarioRepository.getById(nombreUsuario)
                    //Si no está la ruta aún no creamos esa fila, ya lo haremos cuando coja la primera llave; devolvemos posición 0
                    return RutaYProgreso(objetoRuta.nombre, objetoRuta.listaUbicaciones, 0,usua.llaves,usua.rutas).toString()
                }
            }
        }

    }

    //REQUEST CUATRO
    //Se llama desde PruebaARViewModel
    //Devuelve el json de la fila de progreso salvada O, si era la última ubicación de esa ruta, lo necesario para lanzar VideoActivity
    @GetMapping("salvarProgreso/{nombreUsuario}/{ruta}")
    fun salvarProgreso(@PathVariable nombreUsuario: String, @PathVariable ruta: String) :String {

        progresoRepository.findAll().forEach {
            //Comprobación en cortocircuito: Comprobamos si esa ruta está en ProgresoRepository. Si está, comprobamos el usuario.
            if (it.rutaId == ruta && it.usuarioId == nombreUsuario) {
                val progreso = progresoRepository.getById(it.id) //Creamos un objeto Progreso
                val user = usuarioRepository.getById(nombreUsuario) //Creamos un objeto Usuario

                progreso.pistaActual++ //Al progreso le ponemos un más uno (avance en la lista de ubicaciones). Así, al encontrarlo en la request 3, se jugará ya buscando la siguiente llave
                user.llaves++ //Al usuario le sumamos una llave más
                if(progreso.pistaActual==(rutaRepository.getById(ruta).listaUbicaciones.size)) { //Si era la última pista...
                    progreso.pistaActual = 0 //...reseteamos a la primera, porque quiere decir que ha acabado la ruta...
                    user.rutas++ //y al usuario le añadimos una ruta.
                    //Salvo:
                    progresoRepository.save(progreso)
                    usuarioRepository.save(user)
                    println("El user ${user.nombre} tiene ${user.llaves} llaves y ha completado ${user.rutas} rutas")
                    return Token(user.token).toString() //Devuelvo lo necesario para hacer el launch() de VideoActivity
                }
                //Salvo los cambios en las tablas pertinentes:
                progresoRepository.save(progreso)
                usuarioRepository.save(user)

                println("El user ${user.nombre} tiene ${user.llaves} llaves y ha completado ${user.rutas} rutas")

                return user.token //Devuelvo el json de esa fila de la tabla progreso.
            }
        }
        //Si no hemos encontrado nunca ese usuario con esa ruta, hay que salvarlo to.do igualmente, poniendo que la siguiente pista es la 1:
        val progres = Progreso(nombreUsuario,ruta,1)
        progresoRepository.save(progres)
        val user = usuarioRepository.getById(nombreUsuario)
        user.llaves++
        usuarioRepository.save(user)
        println(progres.toString())
        println("El user ${user.nombre} tiene ${user.llaves} llaves y ha completado ${user.rutas} rutas")
        return user.token
    }


    //Funciones ACCESORIAS:

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
}