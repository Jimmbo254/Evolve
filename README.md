# EVOLVE

**Tu entrenamiento, medido.** EVOLVE es una app de Android para organizar tu rutina de fuerza: armas tu plan semanal, entrenas siguiendo ese plan y ves cómo evoluciona tu progreso con datos reales en lugar de sensaciones.

La app se conecta a un backend propio, así que tu cuenta, tus planes y tus entrenamientos quedan guardados en el servidor y no solo en el celular.

---

## Qué puedes hacer

### Crear tu cuenta
Regístrate con nombre, correo y contraseña, o inicia sesión si ya tienes cuenta. Al abrir la app de nuevo, tu sesión se restaura sola sin volver a escribir la contraseña.

### Armar tus planes
Un plan es tu rutina: un nombre, un objetivo y los días de la semana que entrenas. En cada día agregas los ejercicios que vas a hacer, con la cantidad de series, las repeticiones y el descanso entre series. Puedes tener varios planes y **activar** el que estés usando en ese momento; el resto queda guardado para después. También puedes editarlos o borrarlos (los entrenamientos ya registrados no se pierden).

### Entrenar
La pestaña **Entrenar** te muestra qué te toca hoy según el plan activo. Al empezar, se abre una sesión de entrenamiento donde registras cada serie: cuánto peso levantaste y cuántas repeticiones hiciste. Si te olvidaste un ejercicio, lo agregas sobre la marcha. Si cerraste la app a mitad del entrenamiento, al volver puedes **reanudarlo** en el mismo punto. Cuando terminas, la sesión se cierra y pasa a tu historial de progreso.

### Explorar ejercicios
Un catálogo con los ejercicios disponibles, con su grupo muscular, el equipamiento que necesitas, una animación de cómo se hace y los pasos explicados. Puedes buscar por nombre o filtrar por músculo para encontrar variantes cuando el plan se queda corto.

### Ver tu progreso
La pantalla de progreso resume lo importante de tu semana: cuántos días seguidos llevas entrenando, cuántos entrenamientos hiciste, cuánto volumen total moviste y cuántos récords personales nuevos lograste. El volumen se muestra día por día, y al final tienes la lista de tus mejores marcas por ejercicio.

### Ajustar tu perfil
Carga tu peso, estatura, objetivo y nivel para que el entrenamiento tenga contexto. Desde ahí también cierras sesión.

---

## Cómo se usa en la práctica

1. Instala la app y crea tu cuenta.
2. Entra a **Planes**, toca el botón **+** y arma tu rutina de la semana.
3. Activa el plan. La pestaña **Entrenar** ya te muestra el día que corresponde.
4. Entrena y anota cada serie. Al finalizar, revisa **Progreso** para ver el avance.

---

## Tus datos

- La contraseña nunca se guarda en el celular.
- Los tokens de acceso se almacenan **cifrados** con el Keystore de Android.
- El token de acceso se renueva automáticamente cuando vence, sin interrumpirte.
- Al cerrar sesión se borran los tokens del dispositivo y se avisa al servidor.
- La app está marcada para **no** incluirse en los respaldos del sistema (`allowBackup=false`), así los tokens no viajan a la nube de Google.

---

## Para quien quiera tocar el código

### Requisitos

- Android Studio (o el SDK de Android por línea de comandos)
- **JDK 17** para compilar. Gradle 8.7 no funciona con versiones más nuevas (JDK 21 es el máximo probado) ni con el JRE 8 que suele venir por defecto en Windows.
- Un dispositivo o emulador con **Android 8.0 (API 26)** o superior
- `local.properties` apuntando a tu SDK (`sdk.dir=...`); Android Studio lo genera solo

### Compilar y ejecutar

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`. Desde Android Studio, abrir el proyecto y presionar *Run* es equivalente.

### Tests

```bash
gradlew.bat testDebugUnitTest
```

Los tests cubren la renovación de tokens contra un `MockWebServer`: rotación del token cuando otro hilo ya lo renovó y cierre de sesión ante un 401 definitivo.

### Stack

| Pieza | Qué se usa |
|---|---|
| Lenguaje y UI | Kotlin + Jetpack Compose (Material 3) |
| Navegación | Navigation Compose |
| Red | Retrofit + OkHttp |
| JSON | kotlinx.serialization |
| Imágenes y GIF | Coil |
| Almacenamiento seguro | AndroidX Security Crypto (EncryptedSharedPreferences) |

### Estructura

```
app/src/main/java/com/evolve/app/
├── EvolveApplication.kt      arranque: Coil y contenedor de dependencias
├── MainActivity.kt
├── EvolveApp.kt              decide splash / login / navegación principal
├── navigation/               las cinco pestañas y sus rutas
├── data/
│   ├── api/                  endpoints, DTOs, cliente HTTP y errores
│   ├── EvolveRepository.kt   toda la comunicación con el backend
│   ├── SessionManager.kt     bootstrap, login, registro y logout
│   ├── TokenStore.kt         tokens cifrados
│   └── AppContainer.kt       instancia única por proceso
└── ui/
    ├── screens/              auth, planes, editor, entrenamiento, ejercicios, progreso, perfil
    ├── components/           navegador de ejercicios, ficha, selector, carga remota
    ├── ExerciseLabels.kt     nombres de músculos y días en español
    └── theme/                colores y tipografías
```

### Backend

La URL de la API está en `app/src/main/java/com/evolve/app/data/api/ApiConfig.kt`. El backend vive en Render, cuyo plan gratuito **duerme** tras un rato sin uso: la primera petición del día puede tardar hasta un minuto en responder. Por eso el cliente espera hasta 90 segundos y, si no hay red o el servidor está despertando, mantiene tu sesión en lugar de cerrarla.

Si el backend cambia de dirección, basta con editar esa constante y volver a compilar.

### Detalles de interfaz

Tipografías Oswald y Manrope, tema oscuro con acento lima (`#B7F34A`). La app está en español, incluidos los nombres de músculos y los días de la semana que llegan en inglés desde la API.

---

## Versión

La versión actual es **1.0.0**. Ver [releases](https://github.com/Jimmbo254/Evolve/releases).
