package com.proyecto.serverAplicacionRutas

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Database {
    @Bean
    fun initDatabase(usuarioRepository: UsuarioRepository,rutaRepository: RutaRepository,ubicacionRepository: UbicacionRepository):CommandLineRunner{
        return CommandLineRunner {

            //Creo unas cuantas ubicaciones y las salvo en su tabla
            val ubi1 = Ubicacion("Plaza Benavente",40.4146,-3.7037,"Plaza con la estatua de un barrendero")
            val ubi2 = Ubicacion("Plaza Santa Ana",40.4144,-3.7011,"")
            val ubi3 = Ubicacion("Antón Martín",40.4124,-3.6993,"")
            ubicacionRepository.save(ubi1)
            ubicacionRepository.save(ubi2)
            ubicacionRepository.save(ubi3)

            //Creo dos rutas y las salvo en su tabla. Contienen una lista con las ubicaciones que las conforman
            val ruta1 = Ruta("Benavente", listOf(ubi1,ubi2,ubi3))
            rutaRepository.save(ruta1)

            val ubi4 = Ubicacion("Oporto",40.38889156487435, -3.7314374735718356,"El metro de mi casa")
            val ubi5 = Ubicacion("Vista Alegre",40.38889404180021, -3.739875325384332,"Está la plaza de Vistalegre")
            val ubi6 = Ubicacion("Carabanchel",40.38810627041618, -3.744906439069344,"Está el hospital militar Gómez Ulla")
            ubicacionRepository.save(ubi4)
            ubicacionRepository.save(ubi5)
            ubicacionRepository.save(ubi6)
            val ruta2 = Ruta("Oporto", listOf(ubi4,ubi5,ubi6))
            rutaRepository.save(ruta2)

            println("Servidor preparado para comenzar")
        }
    }
}