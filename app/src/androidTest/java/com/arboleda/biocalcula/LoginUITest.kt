package com.arboleda.biocalcula

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arboleda.biocalcula.ui.activities.LoginActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * LoginUITest — Prueba de Interfaz de Usuario con Espresso
 *
 * ¿Por qué se necesita un matcher personalizado para TextInputLayout?
 * ──────────────────────────────────────────────────────────────────────
 * Espresso incluye hasErrorText() de fábrica, pero internamente llama a
 * EditText.getError(). El problema es que TextInputLayout (de Material
 * Design) NO es un EditText — es un ViewGroup que contiene al EditText.
 * Por eso hasErrorText() falla con:
 *   "Expected: EditText — Got: TextInputLayout"
 *
 * La solución es el matcher personalizado tieneErrorDeTexto() que usa
 * TypeSafeMatcher<TextInputLayout> y lee view.error?.toString() directamente.
 * Esto es la forma estándar de verificar errores de Material en Espresso.
 *
 * IDs reales de activity_login.xml:
 *   R.id.tilEmail    → TextInputLayout correo (contiene el .error del campo)
 *   R.id.tilPassword → TextInputLayout contraseña (contiene el .error del campo)
 *   R.id.etEmail     → TextInputEditText donde el usuario escribe
 *   R.id.etPassword  → TextInputEditText donde el usuario escribe
 *   R.id.btnLogin    → MaterialButton "Iniciar sesión"
 */

// =============================================================================
// MATCHER PERSONALIZADO PARA TextInputLayout
//
// TypeSafeMatcher<TextInputLayout> garantiza que solo se aplica a vistas de
// tipo TextInputLayout. matchesSafely() lee .error directamente del objeto.
// =============================================================================
fun tieneErrorDeTexto(mensajeEsperado: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        // describeTo — explica qué busca este matcher en los mensajes de error
        override fun describeTo(description: Description) {
            description.appendText("TextInputLayout con error: \"$mensajeEsperado\"")
        }

        // matchesSafely — verifica si la vista es un TextInputLayout con ese error
        override fun matchesSafely(view: View): Boolean {
            if (view !is TextInputLayout) return false
            val errorActual = view.error?.toString() ?: return false
            return errorActual == mensajeEsperado
        }
    }
}


@RunWith(AndroidJUnit4::class)
class LoginUITest {

    private lateinit var scenario: ActivityScenario<LoginActivity>

    /**
     * @Before — Lanza LoginActivity ANTES de cada @Test.
     * Espresso espera a que esté completamente renderizada (RESUMED)
     * antes de ejecutar cualquier interacción.
     */
    @Before
    fun abrirLoginActivity() {
        scenario = ActivityScenario.launch(LoginActivity::class.java)
    }

    /**
     * @After — Cierra y destruye la Activity DESPUÉS de cada @Test.
     * Libera memoria y evita que el estado de una prueba afecte a la siguiente.
     */
    @After
    fun cerrarActivity() {
        scenario.close()
    }


    // =========================================================================
    // PRUEBA UI 1 (MÍNIMA OBLIGATORIA) — Campos vacíos → error en tilEmail
    //
    // Flujo simulado:
    //   La app abre LoginActivity → el usuario no escribe nada
    //   → toca "Iniciar sesión"
    //   → BioCalcula asigna tilEmail.error = "El correo es obligatorio"
    //
    // Se verifica con nuestro matcher personalizado tieneErrorDeTexto()
    // que lee view.error directamente del TextInputLayout.
    // =========================================================================
    @Test
    fun loginConCamposVacios_muestraMensajeErrorCorreo() {
        // ACT — localiza btnLogin por ID y simula el clic del usuario
        onView(withId(R.id.btnLogin))
            .perform(click())

        // ASSERT — tieneErrorDeTexto() lee tilEmail.error directamente
        // Si error != "El correo es obligatorio" → AssertionError → test FALLA (❌)
        // Si error == "El correo es obligatorio" → test PASA (✅)
        onView(withId(R.id.tilEmail))
            .check(matches(tieneErrorDeTexto("El correo es obligatorio")))
    }


    // =========================================================================
    // PRUEBA UI 2 — Correo sin @ → error de formato en tilEmail
    //
    // Flujo: usuario escribe "usuariosindominio" → toca el botón
    // Esperado: tilEmail.error = "Ingresa un correo válido"
    // =========================================================================
    @Test
    fun loginConCorreoSinArroba_muestraMensajeFormatoInvalido() {
        // ARRANGE — typeText escribe en etEmail carácter a carácter
        onView(withId(R.id.etEmail))
            .perform(typeText("usuariosindominio"), closeSoftKeyboard())

        // ACT
        onView(withId(R.id.btnLogin))
            .perform(click())

        // ASSERT
        onView(withId(R.id.tilEmail))
            .check(matches(tieneErrorDeTexto("Ingresa un correo válido")))
    }


    // =========================================================================
    // PRUEBA UI 3 — CASO DE BORDE: correo válido + contraseña vacía
    //
    // Flujo: correo correcto, contraseña vacía → toca el botón
    // Esperado: tilPassword.error = "La contraseña es obligatoria"
    // =========================================================================
    @Test
    fun loginConContrasenaVacia_muestraMensajeErrorContrasena() {
        // ARRANGE — correo válido, contraseña vacía
        onView(withId(R.id.etEmail))
            .perform(typeText("usuario@test.com"), closeSoftKeyboard())

        // ACT
        onView(withId(R.id.btnLogin))
            .perform(click())

        // ASSERT — ahora apunta a tilPassword con nuestro matcher personalizado
        onView(withId(R.id.tilPassword))
            .check(matches(tieneErrorDeTexto("La contraseña es obligatoria")))
    }


    // =========================================================================
    // PRUEBA UI 4 — El botón "Iniciar sesión" está visible al abrir la Activity
    // =========================================================================
    @Test
    fun loginActivity_botonLoginEstaVisible() {
        // ASSERT — isDisplayed() verifica que el botón es visible en pantalla
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }
}
