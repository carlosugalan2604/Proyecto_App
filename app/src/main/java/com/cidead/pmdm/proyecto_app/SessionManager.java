package com.cidead.pmdm.proyecto_app;

// Esta clase guarda y lee los datos del usuario que ha iniciado sesión.
// Usa SharedPreferences, que es como un pequeño archivo de configuración del dispositivo
// donde se guardan los datos aunque la app se cierre.

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    // Nombre del archivo donde se guardan los datos de sesión en el dispositivo
    private static final String PREFS_NAME  = "dipymes_session";
    // Claves para identificar cada dato guardado (como etiquetas en un cajón)
    private static final String KEY_ID      = "id_usuario";
    private static final String KEY_NOMBRE  = "nombre";
    private static final String KEY_EMAIL   = "email";
    private static final String KEY_ROL     = "rol";
    // Valor especial que usamos cuando NO hay sesión activa (id inválido)
    private static final int    SIN_SESION  = -1;

    // El objeto que nos permite leer y escribir en el archivo de preferencias
    private final SharedPreferences prefs;

    // Constructor: recibe el contexto para poder acceder a las preferencias del dispositivo
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Guarda los datos del usuario en el dispositivo al hacer login
    // Así la próxima vez que abra la app no tendrá que volver a identificarse
    public void guardarSesion(int id, String nombre, String email, String rol) {
        prefs.edit()
            .putInt(KEY_ID, id)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROL, rol)
            .apply(); // apply() guarda los cambios en segundo plano sin bloquear la app
    }

    // Borra todos los datos de sesión guardados (se llama al hacer logout)
    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    // Comprueba si hay una sesión activa mirando si hay un id guardado distinto de -1
    public boolean haySesion() {
        return prefs.getInt(KEY_ID, SIN_SESION) != SIN_SESION;
    }

    // Devuelve el id del usuario conectado (o -1 si no hay sesión)
    public int getIdUsuario() {
        return prefs.getInt(KEY_ID, SIN_SESION);
    }

    // Devuelve el nombre del usuario conectado (o cadena vacía si no hay sesión)
    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    // Devuelve el email del usuario conectado
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // Devuelve el rol del usuario conectado (GERENTE, TRABAJADOR o CLIENTE)
    public String getRol() {
        return prefs.getString(KEY_ROL, "");
    }
}
