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


            val ubi4 = Ubicacion("Oporto", 40.38889156487435, -3.7314374735718356,"El metro de mi casa")
            val ubi5 = Ubicacion("Vista Alegre",40.38889404180021, -3.739875325384332,"Está la plaza de Vistalegre")
            val ubi6 = Ubicacion("Carabanchel",40.38810627041618, -3.744906439069344,"Está el hospital militar Gómez Ulla")
            ubicacionRepository.save(ubi4)
            ubicacionRepository.save(ubi5)
            ubicacionRepository.save(ubi6)
            val ruta2 = Ruta("Oporto", listOf(ubi4,ubi5,ubi6))
            rutaRepository.save(ruta2)

            val ubi7 = Ubicacion("Ubi1", 40.387499804036125, -3.7335673435684615,"Peluquería")
            val ubi8 = Ubicacion("Ubi2",40.38883652462917, -3.735392524328793,"Esquina Gaviota/Matilde Hernández")
            val ubi9 = Ubicacion("Ubi3",40.38909049854261, -3.7349654788303828,"Matilde Hernández/Linares")
            ubicacionRepository.save(ubi7)
            ubicacionRepository.save(ubi8)
            ubicacionRepository.save(ubi9)
            val ruta3 = Ruta("RutaPrueba", listOf(ubi7,ubi8,ubi9))
            rutaRepository.save(ruta3)

            println("Servidor preparado para comenzar")
        }
    }
}