# Manual Técnico — BioCalcula v1.0

## 1. Descripción del sistema

BioCalcula es una aplicación móvil Android orientada al seguimiento nutricional y biométrico de estudiantes universitarios. Resuelve el problema de calcular de forma personalizada las necesidades calóricas y de macronutrientes (proteínas, carbohidratos, grasas) de una persona a partir de sus datos biométricos (peso, talla, edad, sexo) y su objetivo físico, sin depender de conexión a internet.

**Usuario objetivo:** estudiantes y personas que entrenan o cuidan su alimentación en contextos con conectividad limitada o nula (gimnasio, cafetería, transporte), que necesitan una herramienta rápida, privada y funcional sin registro en la nube.

**Alcance del MVP:**
- Registro e inicio de sesión local (sin servidor externo)
- Cálculo del plan nutricional (TMB y macronutrientes) mediante la fórmula Mifflin-St Jeor
- Registro y seguimiento histórico de peso corporal (CRUD completo)
- Registro diario de cumplimiento de macros y consumo de agua
- Recordatorios locales mediante notificaciones (rutina diaria y registro de peso periódico)
- Persistencia 100% local mediante Room, sin dependencia de red en ningún flujo del MVP

## 2. Arquitectura de la aplicación

BioCalcula sigue el patrón **MVVM (Model-View-ViewModel)**, organizado en tres capas:

```
┌─────────────────────────────────────────────┐
│                  UI (View)                   │
│  Activities, Adapters, XML Layouts           │
│  ui/activities, ui/adapter                   │
└───────────────────┬───────────────────────────┘
                     │ observa LiveData
┌───────────────────▼───────────────────────────┐
│                 ViewModel                     │
│  Lógica de presentación y orquestación        │
│  viewmodel/ (DashboardViewModel,              │
│  UsuarioViewModel, RegistroPesoViewModel,     │
│  HistorialViewModel)                          │
└───────────────────┬───────────────────────────┘
                     │ llama a
┌───────────────────▼───────────────────────────┐
│              Datos (Model / Data)             │
│  Room Database, DAOs, Entidades               │
│  data/db, data/dao, data/model                │
└─────────────────────────────────────────────┘
```

**Descripción de las capas:**

- **UI (View):** Activities responsables únicamente de mostrar datos y capturar interacciones del usuario (clicks, formularios). No contienen lógica de negocio ni acceso directo a la base de datos. Ejemplos: `LoginActivity`, `DashboardActivity`, `PerfilActivity`, `HistorialActivity`, `FormularioPesoActivity`.
- **ViewModel:** Contiene la lógica de presentación, sobrevive a cambios de configuración (rotación de pantalla) y expone el estado mediante `LiveData`. Ejecuta las operaciones de base de datos en `viewModelScope` sobre `Dispatchers.IO` para no bloquear el hilo principal.
- **Datos (Model):** Room gestiona la persistencia local. Cada tabla tiene su DAO (`UsuarioDao`, `RegistroPesoDao`, `CumplimientoDiarioDao`) con las operaciones CRUD correspondientes.

**Componentes de soporte transversales:**
- `util/`: `NutricionHelper` (cálculo de TMB y macros), `ValidadorHelper` (validaciones de formularios), `SessionManager` (sesión activa vía SharedPreferences), `NotifPreferences` (preferencias de recordatorios)
- `workers/`: `RecordatorioPesoWorker`, `RecordatorioRutinaWorker` — tareas programadas con WorkManager, desacopladas del ciclo de vida de la app

## 3. Modelo de datos

**Entidades principales:**

| Entidad | Descripción | Campos clave |
|---|---|---|
| `Usuario` | Datos de cuenta y biométricos del usuario | id (PK), nombre, correo, contraseña, peso, talla, edad, sexo, objetivo |
| `RegistroPeso` | Historial cronológico de pesajes | id (PK), peso_registrado, fecha_registro |
| `CumplimientoDiario` | Checklist diario de macros y agua consumidos | fecha (PK), proteinaCumplida, carbosCumplidos, grasasCumplida, aguaCumplida, proteinaGramosConsumidos, aguaVasosConsumidos |

**Relaciones:** `Usuario` se relaciona de forma implícita 1:N con `RegistroPeso` (un usuario tiene múltiples registros de peso a lo largo del tiempo) y 1:N con `CumplimientoDiario` (un registro de cumplimiento por día). El objetivo físico se almacena como atributo del propio `Usuario` en lugar de una entidad separada, dado que el MVP contempla un único objetivo activo por usuario a la vez.

*(Reemplazar esta sección con el diagrama ER exportado desde dbdiagram.io si se desea incluir la imagen).*

## 4. Tecnologías y librerías

**Framework:** Android nativo — Kotlin 2.0.21, compilado con Android Gradle Plugin (AGP) 8.7.2

**Configuración del proyecto:**
- `compileSdk`: 36
- `minSdk`: 24 (Android 7.0 Nougat o superior)
- `targetSdk`: 36
- Compatibilidad Java: 11

**Base de datos:** Room 2.6.1 (sobre SQLite), 100% local

**Librerías principales:**

| Librería | Versión | Uso |
|---|---|---|
| androidx.room (runtime, ktx, compiler) | 2.6.1 | Persistencia local |
| kotlinx-coroutines-android | 1.7.3 | Operaciones asíncronas (IO) |
| androidx.work:work-runtime-ktx | 2.9.0 | Notificaciones programadas (WorkManager) |
| com.github.PhilJay:MPAndroidChart | v3.1.0 | Gráficas de macros en el Dashboard (PieChart, RadarChart) |
| androidx.appcompat | 1.6.1 | Compatibilidad de componentes UI |
| com.google.android.material | 1.12.0 | Material Design 3 (TextInputLayout, MaterialButton, etc.) |
| androidx.constraintlayout | 2.1.4 | Layouts de pantalla |
| androidx.activity-ktx | 1.8.0 | Extensiones Kotlin para Activities |
| androidx.lifecycle:lifecycle-runtime-ktx | 2.7.0 | LiveData / ciclo de vida |

**Librerías de testing:**

| Librería | Versión | Uso |
|---|---|---|
| JUnit | 4.13.2 | Pruebas unitarias |
| androidx.test.ext:junit | 1.1.5 | Pruebas instrumentadas |
| androidx.test.espresso:espresso-core | 3.5.1 | Pruebas de interfaz (UI) |
| androidx.test:rules | 1.5.0 | ActivityScenarioRule para tests de UI |
| androidx.room:room-testing | 2.6.1 | Pruebas de integración con Room in-memory |
| kotlinx-coroutines-test | 1.7.3 | Pruebas de funciones suspend |

No se utilizan servicios externos, API REST ni Firebase — decisión documentada desde el Entregable 9/10, priorizando el funcionamiento 100% offline del MVP.

## 5. Instrucciones para compilar

**Requisitos previos:**
- Android Studio (versión reciente compatible con AGP 8.7.2 / Kotlin 2.0.21 — Android Studio Koala 2024.1 o superior)
- JDK 11 o superior (gestionado automáticamente por Android Studio)
- SDK de Android con API 36 instalado (Android Studio lo solicita automáticamente al abrir el proyecto si falta)

**Pasos:**

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/DmArboleda/biocalcula-app.git
   ```
2. Abrir la carpeta del proyecto con Android Studio (`File → Open`).
3. Esperar a que Gradle sincronice automáticamente (barra de progreso inferior). Si no inicia solo: `File → Sync Project with Gradle Files`.
4. Conectar un dispositivo físico con depuración USB activada, o crear un emulador desde el Device Manager (API 24 o superior).
5. Ejecutar la aplicación con el botón **Run ▶** o `Shift + F10`.

**Variables de entorno / configuración adicional:** ninguna. BioCalcula no requiere API keys, archivos `google-services.json` ni configuración de servicios externos, dado su funcionamiento 100% offline.

## 6. Estructura del repositorio

```
app/src/main/java/com/arboleda/biocalcula/
├── ui/
│   ├── activities/     → Pantallas de la aplicación (Login, Dashboard, Perfil, Historial, etc.)
│   └── adapter/        → Adaptadores de RecyclerView (RegistroPesoAdapter)
├── viewmodel/          → ViewModels de cada pantalla (Dashboard, Usuario, RegistroPeso, Historial)
├── data/
│   ├── db/             → Configuración de la base de datos Room (AppDatabase)
│   ├── dao/             → Interfaces DAO (UsuarioDao, RegistroPesoDao, CumplimientoDiarioDao)
│   └── model/           → Entidades (Usuario, RegistroPeso, CumplimientoDiario)
├── util/                → Helpers (NutricionHelper, ValidadorHelper, SessionManager, NotifPreferences)
├── workers/             → Tareas en segundo plano con WorkManager (recordatorios)
└── BioCalculaApp.kt     → Clase Application

app/src/test/            → Pruebas unitarias (JVM)
app/src/androidTest/     → Pruebas de integración y UI (Espresso, Room in-memory)
```

## 7. Historial de versiones

**v1.0 — MVP completo (versión estable de presentación final)**

Funcionalidades incluidas:
- Registro e inicio de sesión local con validación de formularios
- CRUD completo de registros de peso (crear, ver, editar, eliminar con confirmación y deshacer)
- Cálculo automático de plan nutricional (TMB y macros) con la fórmula Mifflin-St Jeor
- Dashboard con visualización de macros mediante gráficas (PieChart / RadarChart)
- Notificaciones locales programadas con WorkManager: recordatorio de rutina diaria y de registro de peso periódico
- Suite de pruebas: unitarias (validaciones, cálculo de TMB), de integración (Room + DAO) y de interfaz (Espresso)
- Optimización de rendimiento: corrección de bug en configuración de pruebas Espresso, cacheo del cálculo del plan nutricional para reducir uso de CPU en el Dashboard
- Tag `v1.0` publicado en GitHub con Release y APK adjunto
