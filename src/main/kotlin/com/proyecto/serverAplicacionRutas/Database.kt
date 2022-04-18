package com.proyecto.serverAplicacionRutas

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Database {
    @Bean
    fun initDatabase(usuarioRepository: UsuarioRepository,rutaRepository: RutaRepository):CommandLineRunner{
        return CommandLineRunner {
            val listaRutas= listOf(
                Ruta("Benavente", listOf(
                        Ubicacion("Plaza Benavente",40.4146,-3.7037,"Plaza con la estatua de un barrendero",false),
                        Ubicacion("Plaza Santa Ana",40.4144,-3.7011,"",false),
                        Ubicacion("Antón Martín",40.4124,-3.6993,"",false)

                ),false,false)
            )

            listaRutas.forEach {
                rutaRepository.save(it)
            }
            println("Servidor preparado para comenzar")
        }
    }
}