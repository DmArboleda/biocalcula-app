package com.arboleda.biocalcula

import org.junit.Assert.*
import org.junit.Test

/**
 * ValidacionesTest
 *
 * Suite de pruebas unitarias para BioCalcula usando JUnit 4 y el patrón
 * Arrange-Act-Assert (AAA).
 *
 * Cubre las 3 pruebas mínimas obligatorias del entregable:
 *   • Prueba 1 → validarCorreo()     — formato de correo electrónico
 *   • Prueba 2 → validarContrasena() — longitud mínima de contraseña
 *   • Prueba 3 → lógica de negocio   — validación del registro de peso y estado IMC
 *
 * Cada bloque incluye un caso válido, un caso inválido y al menos un caso de borde.
 *
 * Para ejecutar: clic derecho sobre este archivo → Run 'ValidacionesTest'
 */
class ValidacionesTest {

    // =========================================================================
    // PRUEBA 1: VALIDACIÓN DE CORREO ELECTRÓNICO
    //
    // ¿Qué se está verificando?
    //   Que validarCorreo() retorna true solo cuando el string cumple el formato
    //   usuario@dominio.extensión, replicando la misma lógica que usa
    //   LoginActivity y RegisterActivity al validar el campo de email.
    //
    // ¿Por qué es importante probarla?
    //   Un correo inválido almacenado en Room impediría el login del usuario y
    //   corrompería los datos de la entidad Usuario. La validación es la primera
    //   línea de defensa antes de cualquier operación CRUD.
    // =========================================================================

    @Test
    fun validarCorreo_formatoCorrecto_retornaTrue() {
        // ARRANGE — se prepara un correo con formato válido completo
        val correoValido = "usuario@universidad.edu.ec"

        // ACT — se invoca la función a probar
        val resultado = ValidadorHelper.validarCorreo(correoValido)

        // ASSERT — assertTrue exige que 'resultado' sea estrictamente true;
        //           si la función retornara false, el test falla con la
        //           descripción proporcionada, indicando un bug en la regex.
        assertTrue(
            "Un correo con formato institucional válido debe retornar true",
            resultado
        )
    }

    @Test
    fun validarCorreo_sinArrobaYSinDominio_retornaFalse() {
        // ARRANGE — cadena sin '@' ni dominio
        val correoInvalido = "usuarioSinDominio"

        // ACT
        val resultado = ValidadorHelper.validarCorreo(correoInvalido)

        // ASSERT — assertFalse exige que 'resultado' sea false;
        //           si retornara true, significaría que se acepta un correo
        //           malformado, lo que es un fallo crítico de seguridad.
        assertFalse(
            "Un correo sin '@' ni dominio debe retornar false",
            resultado
        )
    }

    /** CASO DE BORDE: dos '@' seguidos → inválido */
    @Test
    fun validarCorreo_dobleArroba_retornaFalse() {
        // ARRANGE — patrón extremo con doble '@'
        val correoBorde = "usuario@@dominio.com"

        // ACT
        val resultado = ValidadorHelper.validarCorreo(correoBorde)

        // ASSERT
        assertFalse(
            "Un correo con '@@' es inválido y debe retornar false",
            resultado
        )
    }

    /** CASO DE BORDE: correo que empieza con '@' */
    @Test
    fun validarCorreo_iniciaConArroba_retornaFalse() {
        // ARRANGE
        val correoSinLocal = "@dominio.com"

        // ACT
        val resultado = ValidadorHelper.validarCorreo(correoSinLocal)

        // ASSERT
        assertFalse(
            "Un correo que empieza con '@' no tiene parte local y debe retornar false",
            resultado
        )
    }


    // =========================================================================
    // PRUEBA 2: VALIDACIÓN DE CONTRASEÑA
    //
    // ¿Qué se está verificando?
    //   Que validarContrasena() rechaza contraseñas de menos de 6 caracteres
    //   y acepta aquellas de 6 o más, replicando la regla de LoginActivity
    //   (línea 68) y RegisterActivity (línea 62).
    //
    // ¿Por qué es importante probarla?
    //   Una contraseña débil compromete los datos antropométricos y calóricos
    //   de los usuarios almacenados en la base de datos de BioCalcula.
    // =========================================================================

   @Test
    fun validarContrasena_longitudExacta6Caracteres_retornaTrue() {
        // ARRANGE — exactamente en el límite mínimo permitido
        val contrasenaValida = "123456"   // longitud = 6

        // ACT
        val resultado = ValidadorHelper.validarContrasena(contrasenaValida)

        // ASSERT — assertTrue: si length == 6, la función debe aceptarla.
        //           Un fallo aquí indicaría que el operador >= fue escrito como >
        assertTrue(
            "Una contraseña de exactamente 6 caracteres debe ser aceptada",
            resultado
        )
    }

    @Test
    fun validarContrasena_longitudMenorA6_retornaFalse() {
        // ARRANGE — 5 caracteres, un dígito por debajo del mínimo
        val contrasenaCorta = "12345"   // longitud = 5

        // ACT
        val resultado = ValidadorHelper.validarContrasena(contrasenaCorta)

        // ASSERT — assertFalse: la contraseña debe ser rechazada.
        //           Si la función devolviera true, el sistema aceptaría
        //           contraseñas inseguras.
        assertFalse(
            "Una contraseña de 5 caracteres debe ser rechazada",
            resultado
        )
    }

    /** CASO DE BORDE: cadena completamente vacía */
    @Test
    fun validarContrasena_cadenaVacia_retornaFalse() {
        // ARRANGE — el peor caso: campo en blanco
        val contrasenaVacia = ""

        // ACT
        val resultado = ValidadorHelper.validarContrasena(contrasenaVacia)

        // ASSERT
        assertFalse(
            "Una contraseña vacía no debe ser válida",
            resultado
        )
    }


    // =========================================================================
    // PRUEBA 3: LÓGICA DE NEGOCIO — ENTIDAD PRINCIPAL (RegistroPeso)
    //
    // ¿Qué se está verificando?
    //   3a) validarPesoRegistrado(): el peso ingresado en FormularioPesoActivity
    //       debe ser un número positivo y dentro de un rango biológico (0–500 kg).
    //   3b) calcularEstadoImc(): clasificación clínica según la OMS basada en el
    //       IMC derivado de los pesos registrados.
    //
    // ¿Por qué es importante probarla?
    //   Un valor de peso inválido (negativo, cero o imposible) almacenado en
    //   Room produciría cálculos de IMC erróneos, entregando diagnósticos
    //   nutricionales incorrectos al usuario — la funcionalidad central de la app.
    // =========================================================================

    @Test
    fun validarPesoRegistrado_pesoPositivoValido_retornaTrue() {
        // ARRANGE — peso normal de un adulto
        val pesoValido = 70.5f

        // ACT
        val resultado = ValidadorHelper.validarPesoRegistrado(pesoValido)

        // ASSERT — assertEquals compara bit a bit true vs resultado;
        //           cualquier divergencia indica lógica rota en la función.
        assertTrue(
            "Un peso de 70.5 kg es válido y debe retornar true",
            resultado
        )
    }

    @Test
    fun validarPesoRegistrado_pesoNegativo_retornaFalse() {
        // ARRANGE — valor biológicamente imposible
        val pesoNegativo = -10f

        // ACT
        val resultado = ValidadorHelper.validarPesoRegistrado(pesoNegativo)

        // ASSERT
        assertFalse(
            "Un peso negativo no es físicamente posible y debe retornar false",
            resultado
        )
    }

    /** CASO DE BORDE: peso = 0 (campo dejado en blanco y parseado como 0) */
    @Test
    fun validarPesoRegistrado_pesoEnCero_retornaFalse() {
        // ARRANGE — caso de borde crítico: usuario presionó Guardar sin ingresar peso
        val pesoEnCero = 0f

        // ACT
        val resultado = ValidadorHelper.validarPesoRegistrado(pesoEnCero)

        // ASSERT
        assertFalse(
            "Un peso de 0 kg no es válido y debe retornar false",
            resultado
        )
    }

    @Test
    fun calcularEstadoImc_rangoNormal_retornaEstadoNormal() {
        // ARRANGE
        val imcNormal = 22.5
        val resultadoEsperado = "Normal"

        // ACT
        val resultadoReal = ValidadorHelper.calcularEstadoImc(imcNormal)

        // ASSERT — assertEquals("esperado", actual): falla si los strings difieren.
        //           Si retornara "Sobrepeso", el diagnóstico sería incorrecto.
        assertEquals(
            "Un IMC de 22.5 debe clasificarse como 'Normal'",
            resultadoEsperado,
            resultadoReal
        )
    }

    @Test
    fun calcularEstadoImc_rangoSobrepeso_retornaEstadoSobrepeso() {
        // ARRANGE
        val imcSobrepeso = 27.3
        val resultadoEsperado = "Sobrepeso"

        // ACT
        val resultadoReal = ValidadorHelper.calcularEstadoImc(imcSobrepeso)

        // ASSERT
        assertEquals(
            "Un IMC de 27.3 debe clasificarse como 'Sobrepeso'",
            resultadoEsperado,
            resultadoReal
        )
    }

    /** CASO DE BORDE: IMC negativo → categoría "Invalido" */
    @Test
    fun calcularEstadoImc_imcNegativo_retornaInvalido() {
        // ARRANGE — valor imposible biológicamente
        val imcNegativo = -5.0
        val resultadoEsperado = "Invalido"

        // ACT
        val resultadoReal = ValidadorHelper.calcularEstadoImc(imcNegativo)

        // ASSERT
        assertEquals(
            "Un IMC negativo no es biológicamente posible, debe retornar 'Invalido'",
            resultadoEsperado,
            resultadoReal
        )
    }
}
