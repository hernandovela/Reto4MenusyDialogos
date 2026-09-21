# Triqui · Reto 4

Aplicación Android en Java para el reto **Menus and Dialog Boxes** de Frank McCown (Harding University, CC BY 3.0). Proyecto independiente para Android Studio, interfaz en español y sin conexión a Internet.

## Ejecutar

Abre esta carpeta en Android Studio, sincroniza Gradle y ejecuta `app` en un emulador o teléfono con Android 7.0 o posterior. Requiere SDK 37 y JDK 17 o posterior; se puede usar el JDK incluido con Android Studio.

En Windows, con `JAVA_HOME` configurado:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Funcionalidades

- El jugador usa X y comienza cada partida; la computadora usa O.
- Detección de victoria, empate, bloqueo de casillas ocupadas y resaltado de la línea ganadora.
- Menú XML: Nueva partida, Dificultad, Acerca de y Salir.
- Fácil: selección aleatoria. Difícil: gana si puede; de lo contrario juega al azar. Experto: gana, bloquea o juega al azar, en ese orden. Es la estrategia del PDF y puede ser vencida.
- Selector de dificultad con la opción actual marcada, cierre al elegir y Toast de confirmación. La elección se conserva al cerrar la aplicación.
- Salir muestra confirmación Sí/No. Acerca de utiliza un diseño XML personalizado.
- Icono propio y recursos vectoriales escalables en lugar de los PNG del tutorial antiguo.
- Marcador de la sesión y conservación del tablero y turno al girar el dispositivo. Nueva partida conserva el marcador.
- Los diálogos se crean con AlertDialog.Builder, sin los métodos obsoletos showDialog/onCreateDialog del documento.

## Archivos principales

- `app/src/main/java/co/edu/triqui/TicTacToeGame.java`: reglas y niveles.
- `app/src/main/java/co/edu/triqui/MainActivity.java`: interfaz, ciclo de vida, menú y diálogos.
- `app/src/main/res/menu/options_menu.xml`: opciones del menú.
- `app/src/main/res/layout/about_dialog.xml`: diálogo personalizado.
- `app/src/main/res/values/strings.xml`: textos; puedes personalizar el crédito de desarrollo en `about_body`.
- `app/src/test/java/co/edu/triqui/TicTacToeGameTest.java`: pruebas de reglas y dificultad.

## Revisión manual

1. Abrir el menú de tres puntos y comprobar las cuatro opciones.
2. Elegir cada dificultad, comprobar el Toast y volver a abrir el selector para verificar la selección.
3. Jugar hasta ganar, perder o empatar; comprobar el marcador y comenzar otra partida.
4. Girar el dispositivo durante el turno de la computadora: debe conservar el tablero y realizar una sola jugada.
5. Comprobar Acerca de y probar tanto No como Sí en Salir.

Referencia: tutorial proporcionado por el usuario, *Android Application Programming — Challenge: Menus and Dialog Boxes*. Licencia: https://creativecommons.org/licenses/by/3.0/.
