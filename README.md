# BioCalcula - App de Control Nutricional y Cálculo Biométrico

BioCalcula es una aplicación móvil diseñada para facilitar el seguimiento nutricional, la gestión de ingesta calórica diaria y el cálculo automático de métricas biométricas esenciales para estudiantes y usuarios interesados en mejorar su composición corporal y salud general.

---

## 📝 Definición del Problema

En el entorno académico y cotidiano actual, las personas suelen experimentar dificultades para mantener un registro riguroso de su nutrición y parámetros corporales debido a la falta de herramientas centralizadas, intuitivas y eficientes. La ausencia de un control estructurado de calorías e historial de peso predispone al fracaso en la consecución de objetivos físicos (pérdida de grasa, ganancia muscular o mantenimiento) y dificulta el cálculo preciso de tasas metabólicas sin depender de múltiples hojas de cálculo o procesos manuales propensos a errores.

## 🎯 Objetivo del Proyecto

Desarrollar e implementar una solución móvil nativa y ligera que automatice el cálculo de índices biométricos fundamentales, permitiendo a los usuarios registrar de forma segura su progreso antropométrico e ingesta diaria, promoviendo hábitos saludables mediante una interfaz limpia y accesible.

---

## ⚙️ Arquitectura y Tecnologías Elegidas

El desarrollo de la aplicación se ha estructurado bajo un entorno técnico robusto para garantizar escalabilidad local y un consumo óptimo de recursos:

* **Plataforma de Desarrollo:** Android Studio (Edición Giraffe / Koala)
* **Lenguaje de Programación:** Java (JDK 17)
* **Base de Datos Local:** Room Database / SQLite (para persistencia segura de calorías y peso)
* **Patrón de Arquitectura:** MVVM (Model-View-ViewModel) para una separación limpia de la lógica de negocio y la interfaz gráfica.
* **Componentes Visuales:** Material Design 3 (ConstraintLayout, RecyclerView)

---

## 📋 Historias de Usuario (MVP)

### Historia de Usuario 1: Registro de Ingesta Diaria
* **Como** usuario de BioCalcula,
* **Quiero** registrar la cantidad de calorías consumidas en mis comidas diarias,
* **Para** mantener un balance energético alineado con mi meta nutricional.
* **Criterio de Aceptación:** El sistema debe permitir ingresar un valor numérico de calorías, asociarlo a la fecha actual y acumularlo en un panel principal diario.

### Historia de Usuario 2: Cálculo Automático de Métricas Biométricas
* **Como** usuario interesado en mi salud,
* **Quiero** ingresar mi peso, altura y edad para que la app calcule mi IMC (Índice de Masa Corporal) y mi TMB (Tasa Metabólica Basal),
* **Para** conocer mis necesidades calóricas de forma inmediata.
* **Criterio de Aceptación:** La aplicación validará que los campos no estén vacíos y desplegará el resultado matemático en pantalla con un indicador de estado nutricional.

### Historia de Usuario 3: Historial de Peso y Progreso
* **Como** usuario en control de su evolución física,
* **Quiero** visualizar un listado histórico de los registros de peso anteriores,
* **Para** evaluar si mi estrategia nutricional está funcionando a lo largo del tiempo.
* **Criterio de Aceptación:** Los datos deben mostrarse de forma cronológica descendente extraídos de la base de datos local.

---

## 📸 Prototipo e Interfaz de la Aplicación

### Diseño del Layout Base (activity_main.xml)
A continuación se detalla la maquetación inicial de los elementos interactivos del MVP:

![Prototipo de Interfaz de BioCalcula](https://raw.githubusercontent.com/DmArboleda/biocalcula-app/main/app/src/main/res/drawable/screenshot_placeholder.png)

> *Nota: Aquí puedes visualizar el diseño estructurado con los componentes en su respectivo `ConstraintLayout`.*

---

## 🚀 Instrucciones de Instalación y Configuración Básica

Para clonar y ejecutar este proyecto localmente, asegúrate de contar con los siguientes requisitos previos instalados en tu sistema operativo:

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/DmArboleda/biocalcula-app.git](https://github.com/DmArboleda/biocalcula-app.git)
    ```
2.  **Abrir el Proyecto:**
    * Inicia **Android Studio**.
    * Selecciona *File > Open* y busca la carpeta raíz donde clonaste el proyecto.
3.  **Configurar el SDK y JDK:**
    * Verifica que cuentas con el **JDK 17** asignado en la estructura del proyecto (*Project Structure > SDK Location*).
    * Sincroniza los archivos de Gradle (`build.gradle.kts`) presionando el icono del elefante (*Sync Project with Gradle Files*).
4.  **Ejecución:**
    * Abre el **Device Manager**, inicializa el dispositivo virtual configurado (**Pixel 6 - API 34**).
    * Haz clic en el botón verde de **Run (Play)** en la barra superior.

---

## 📊 Estado Actual del Proyecto
* **Fase Actual:** Configuración inicial del entorno, vinculación exitosa al repositorio Git e implementación de las vistas XML base (`activity_main.xml`).
* **Próximo Paso:** Inyección de dependencias de Room para el almacenamiento persistente e interconexión de controladores Java.
