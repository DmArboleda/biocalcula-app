package com.arboleda.biocalcula

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arboleda.biocalcula.data.dao.RegistroPesoDao
import com.arboleda.biocalcula.data.dao.UsuarioDao
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.data.model.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * BioCalculaIntegrationTest
 *
 * Pruebas de INTEGRACIÓN para BioCalcula.
 * Ejecutan sobre el runtime real de Android (emulador o dispositivo), por eso
 * residen en la carpeta androidTest/ y usan @RunWith(AndroidJUnit4::class).
 *
 * ¿Por qué se usa una base de datos IN-MEMORY y no la real del dispositivo?
 * ─────────────────────────────────────────────────────────────────────────
 * Room.inMemoryDatabaseBuilder() crea una base de datos que:
 *   • Solo existe en la RAM mientras dura el test — no toca el archivo SQLite
 *     real del dispositivo ni corrompe datos del usuario.
 *   • Se destruye automáticamente cuando se cierra la conexión (@After → db.close()).
 *   • Es completamente reproducible: cada ejecución empieza desde cero con
 *     tablas vacías, garantizando aislamiento total entre pruebas.
 *   • Corre más rápido porque no hay acceso a disco.
 *
 * ¿Qué diferencia hay entre test/ y androidTest/?
 * ─────────────────────────────────────────────────
 * • test/        → Pruebas UNITARIAS. Corren en la JVM de tu PC sin Android.
 *                  No pueden usar APIs de Android (Context, Room, LiveData real).
 * • androidTest/ → Pruebas de INTEGRACIÓN. Corren en el runtime real de Android,
 *                  ya sea un emulador o un dispositivo físico conectado por USB.
 *                  Pueden usar Context, Room, Intents y componentes de UI.
 *
 * ¿Qué hace @RunWith(AndroidJUnit4::class)?
 * ─────────────────────────────────────────
 * Le indica a JUnit que use el runner de Android, quien arranca el runtime de
 * Android antes de ejecutar los tests. Sin él, el test no tendría Context ni
 * acceso a las APIs nativas de Android que Room necesita internamente.
 *
 * Cómo ejecutar:
 *   Clic derecho sobre este archivo → Run 'BioCalculaIntegrationTest'
 *   (requiere emulador encendido o dispositivo conectado)
 */
@RunWith(AndroidJUnit4::class)
class BioCalculaIntegrationTest {

    // Referencias a la base de datos en memoria y los DAOs bajo prueba
    private lateinit var db: AppDatabase
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var registroPesoDao: RegistroPesoDao

    /**
     * @Before — Se ejecuta ANTES de cada método @Test.
     * Aquí se construye la base de datos en memoria y se obtienen los DAOs.
     * Al ser in-memory, cada test comienza con tablas completamente vacías.
     *
     * allowMainThreadQueries() → permite ejecutar consultas en el hilo principal
     * solo durante tests, facilitando el uso de runBlocking sin deadlocks.
     */
    @Before
    fun crearBaseDeDatos() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        usuarioDao       = db.usuarioDao()
        registroPesoDao  = db.registroPesoDao()
    }

    /**
     * @After — Se ejecuta DESPUÉS de cada método @Test.
     * Cierra la base de datos y libera los recursos de la RAM.
     * Si no se cierra, la conexión abierta puede causar errores en el siguiente test.
     *
     * @Throws(IOException::class) — db.close() puede lanzar esta excepción
     * en casos de error de I/O al liberar recursos del sistema operativo.
     */
    @After
    @Throws(IOException::class)
    fun cerrarBaseDeDatos() {
        db.close()
    }


    // =========================================================================
    // PRUEBA DE INTEGRACIÓN 1: CRUD COMPLETO DE RegistroPeso
    //
    // ¿Qué se verifica?
    //   Se prueba el flujo completo del CRUD de la entidad principal de BioCalcula:
    //   insertar un registro de peso, leerlo de vuelta y verificar que todos sus
    //   campos se guardaron exactamente con los valores correctos.
    //   Esto integra la capa de datos (Entity + DAO) con Room.
    //
    // ¿Por qué es importante?
    //   Es la función central de la app — sin un CRUD correcto, el historial
    //   de peso del usuario sería incorrecto y los cálculos de IMC fallarían.
    // =========================================================================

    @Test
    fun crud_insertarRegistroPeso_leerlo_verificaDatosCorrectos() = runBlocking {
        // ARRANGE — preparar el objeto a guardar con datos conocidos
        val registroEsperado = RegistroPeso(
            peso_registrado = 72.5f,
            fecha_registro  = "2025-11-15"
        )

        // ACT — ejecutar la inserción en la base de datos in-memory
        registroPesoDao.insert(registroEsperado)

        // ACT — leer el último registro guardado (el que acabamos de insertar)
        val registroLeido = registroPesoDao.getUltimoRegistro()

        // ASSERT — verificar que el registro existe y sus datos son exactos
        assertNotNull(
            "El registro recién insertado debe poder leerse de la base de datos",
            registroLeido
        )
        assertEquals(
            "El peso guardado debe ser exactamente 72.5f",
            72.5f,
            registroLeido!!.peso_registrado
        )
        assertEquals(
            "La fecha guardada debe ser exactamente '2025-11-15'",
            "2025-11-15",
            registroLeido.fecha_registro
        )
    }

    @Test
    fun crud_actualizarRegistroPeso_verificaNuevoPeso() = runBlocking {
        // ARRANGE — insertar un registro inicial
        registroPesoDao.insert(
            RegistroPeso(peso_registrado = 80.0f, fecha_registro = "2025-11-10")
        )
        val insertado = registroPesoDao.getUltimoRegistro()!!

        // ACT — actualizar el peso del mismo registro
        val registroActualizado = insertado.copy(peso_registrado = 78.3f, fecha_registro = "2025-11-20")
        registroPesoDao.update(registroActualizado)

        // ASSERT — verificar que el nuevo peso quedó guardado
        val leido = registroPesoDao.getUltimoRegistro()
        assertEquals(
            "Después de actualizar, el peso debe ser 78.3f",
            78.3f,
            leido!!.peso_registrado
        )
    }

    @Test
    fun crud_eliminarRegistroPeso_verificaQueNoExiste() = runBlocking {
        // ARRANGE
        registroPesoDao.insert(
            RegistroPeso(peso_registrado = 65.0f, fecha_registro = "2025-10-01")
        )
        val insertado = registroPesoDao.getUltimoRegistro()!!

        // ACT — eliminar el registro
        registroPesoDao.delete(insertado)

        // ASSERT — la tabla debe quedar vacía
        val resultado = registroPesoDao.getUltimoRegistro()
        assertNull(
            "Después de eliminar el único registro, getUltimoRegistro debe retornar null",
            resultado
        )
    }


    // =========================================================================
    // PRUEBA DE INTEGRACIÓN 2: FLUJO COMPLETO DE LOGIN
    //
    // ¿Qué se verifica?
    //   Se simula el flujo real de autenticación de BioCalcula:
    //   1. Registrar un usuario en la base de datos in-memory (como haría RegisterActivity).
    //   2. Intentar login con las credenciales CORRECTAS → debe retornar el usuario.
    //   3. Intentar login con contraseña INCORRECTA     → debe retornar null.
    //   4. Intentar login con correo INCORRECTO          → debe retornar null.
    //
    // ¿Por qué es importante?
    //   Prueba que la query SQL del DAO hace el JOIN correcto de correo+contraseña.
    //   Un error aquí permitiría el acceso con credenciales equivocadas o
    //   bloquearía usuarios legítimos — ambos son fallos críticos de seguridad.
    // =========================================================================

    @Test
    fun login_credencialesCorrectas_retornaUsuario() = runBlocking {
        // ARRANGE — registrar un usuario de prueba en la base de datos in-memory
        val usuarioPrueba = Usuario(
            nombre     = "Domenica Arboleda",
            correo     = "domenica@universidad.edu.ec",
            contrasena = "segura123"
        )
        usuarioDao.insertarUsuario(usuarioPrueba)

        // ACT — intentar login con las credenciales correctas (igual que LoginActivity)
        val resultado = usuarioDao.login(
            correo     = "domenica@universidad.edu.ec",
            contrasena = "segura123"
        )

        // ASSERT — assertEquals(mensaje, esperado, actual)
        // assertNotNull exige que resultado NO sea null, es decir, que se encontró
        // el usuario. Si retornara null significaría que la query SQL falló.
        assertNotNull(
            "Con correo y contraseña correctos, login debe retornar el usuario",
            resultado
        )
        assertEquals(
            "El nombre del usuario recuperado debe coincidir con el registrado",
            "Domenica Arboleda",
            resultado!!.nombre
        )
        assertEquals(
            "El correo del usuario recuperado debe coincidir con el registrado",
            "domenica@universidad.edu.ec",
            resultado.correo
        )
    }

    @Test
    fun login_contrasenaIncorrecta_retornaNull() = runBlocking {
        // ARRANGE — registrar usuario de prueba
        usuarioDao.insertarUsuario(
            Usuario(
                nombre     = "Domenica Arboleda",
                correo     = "domenica@universidad.edu.ec",
                contrasena = "segura123"
            )
        )

        // ACT — intentar login con contraseña INCORRECTA
        val resultado = usuarioDao.login(
            correo     = "domenica@universidad.edu.ec",
            contrasena = "claveMAL"   // ← contraseña equivocada
        )

        // ASSERT — assertNull exige que resultado SEA null.
        // Si la función retornara el usuario, significaría que el sistema
        // acepta cualquier contraseña, un fallo crítico de seguridad.
        assertNull(
            "Con contraseña incorrecta, login debe retornar null (acceso denegado)",
            resultado
        )
    }

    @Test
    fun login_correoNoRegistrado_retornaNull() = runBlocking {
        // ARRANGE — la base de datos in-memory comienza vacía; no se inserta nada

        // ACT — intentar login con un correo que nunca fue registrado
        val resultado = usuarioDao.login(
            correo     = "fantasma@noexiste.com",
            contrasena = "cualquiera"
        )

        // ASSERT — CASO DE BORDE: correo inexistente debe retornar null
        assertNull(
            "Con un correo no registrado, login debe retornar null",
            resultado
        )
    }

    @Test
    fun login_buscarPorCorreo_retornaUsuarioCorrecto() = runBlocking {
        // ARRANGE — registrar dos usuarios distintos
        usuarioDao.insertarUsuario(
            Usuario(nombre = "Ana García",  correo = "ana@test.com",  contrasena = "pass111")
        )
        usuarioDao.insertarUsuario(
            Usuario(nombre = "Luis Torres", correo = "luis@test.com", contrasena = "pass222")
        )

        // ACT — buscar solo por correo (función usada en RegisterActivity para
        //       detectar correos duplicados antes del registro)
        val encontrado = usuarioDao.buscarPorCorreo("luis@test.com")

        // ASSERT — debe retornar el usuario correcto, no el otro
        assertNotNull("Buscar por correo existente debe retornar un usuario", encontrado)
        assertEquals(
            "El usuario encontrado debe ser Luis Torres, no Ana García",
            "Luis Torres",
            encontrado!!.nombre
        )
    }
}
