# DI-PYMES — App Android

  Aplicación Android nativa en Java para la gestión empresarial de PYMEs.
  Se conecta a la API REST central (Spring Boot) para obtener y modificar datos en tiempo real.
  Cada usuario accede únicamente a las funciones de su rol: Gerente, Trabajador o Cliente.

  ## Tecnologías

  - Java | Android SDK 31–36
  - Room (SQLite local)
  - OkHttp3 (peticiones HTTP)
  - Material Design 3
  - SharedPreferences (sesión persistente)

  ## Credenciales de prueba

  | Email                     | Contraseña | Rol        |
  |---------------------------|------------|------------|
  | gerente@dipymes.com       | 1234       | GERENTE    |
  | trabajador@dipymes.com    | 1234       | TRABAJADOR |
  | cliente@dipymes.com       | 1234       | CLIENTE    |

  ## Puesta en marcha 

  1. Clona el repositorio y ábrelo en **Android Studio**.
  2. Arranca la API DI-PYMES en la misma red local (puerto **8085**).
  3. En `ApiClient.java`, actualiza la IP con la de la máquina que ejecuta la API:
     ```java
     private static final String BASE_URL = "http://<TU_IP>:8085";
  4. Ejecuta la app en un emulador (API 31+) o dispositivo físico en la misma red.

  # Con estas instrucciones, se podrá poner en práctica mi proyecto y comprobar todo su rendimiento. 
