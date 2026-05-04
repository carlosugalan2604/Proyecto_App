package com.cidead.pmdm.proyecto_app;

// Esta clase se encarga de hacer todas las peticiones a la API del servidor.
// Es como un "mensajero" que envía y recibe datos entre la app y el servidor.

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ApiClient - Clase de utilidad para hacer peticiones HTTP a la API DI-PYMES.
 *
 * IMPORTANTE: Si usas un emulador Android, la URL base es http://10.0.2.2:8085
 *             Si usas un dispositivo físico, cámbiala por la IP de tu PC,
 *             por ejemplo: http://192.168.1.50:8085
 */
public class ApiClient {

    // La dirección base del servidor donde está nuestra API
    // Todos los endpoints se construyen añadiendo la ruta después de esta URL
    private static final String BASE_URL = "http://192.168.0.19:8085";

    // Le decimos a OkHttp que el contenido que enviamos es JSON con codificación UTF-8
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Creamos un solo cliente HTTP para toda la app y lo reutilizamos siempre
    // (es más eficiente que crear uno nuevo cada vez)
    private static final OkHttpClient cliente = new OkHttpClient();

    // Esta interfaz define cómo avisamos al resto de la app cuando llega la respuesta.
    // Como las peticiones son asíncronas (no bloquean la pantalla), necesitamos
    // estos métodos "de vuelta" para saber cuándo terminaron.
    // onRespuesta: se ejecuta si todo fue bien (el servidor respondió con éxito)
    // onError: se ejecuta si hubo algún problema (sin red, error del servidor, etc.)
    public interface ApiCallback {
        void onRespuesta(String respuesta);
        void onError(String error);
    }

    // Método interno para hacer peticiones GET (solo pedir datos, sin enviar nada)
    private static void get(String url, ApiCallback callback) {
        // Construimos la petición con la URL que nos pasan
        Request peticion = new Request.Builder().url(url).build();

        // enqueue significa "pon esto en cola y ejecútalo en segundo plano"
        // así no bloqueamos la pantalla mientras esperamos respuesta
        cliente.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Si hay un fallo de red (sin WiFi, servidor apagado...) avisamos con error
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Si el servidor respondió con código 200-299 es éxito, devolvemos el texto
                if (response.isSuccessful()) {
                    callback.onRespuesta(response.body().string());
                } else {
                    // Si el servidor respondió pero con un código de error (404, 500...) avisamos
                    callback.onError("Error HTTP: " + response.code());
                }
            }
        });
    }

    // Método interno para hacer peticiones POST (enviar datos nuevos al servidor)
    private static void post(String url, String json, ApiCallback callback) {
        // Creamos el cuerpo de la petición con el JSON que queremos enviar
        RequestBody cuerpo = RequestBody.create(json, JSON);
        Request peticion = new Request.Builder().url(url).post(cuerpo).build();

        cliente.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onRespuesta(response.body().string());
                } else {
                    callback.onError("Error HTTP: " + response.code());
                }
            }
        });
    }

    // Método interno para hacer peticiones PUT (actualizar datos existentes en el servidor)
    private static void put(String url, String json, ApiCallback callback) {
        // Igual que POST pero usando .put() para indicar que es una actualización
        RequestBody cuerpo = RequestBody.create(json, JSON);
        Request peticion = new Request.Builder().url(url).put(cuerpo).build();

        cliente.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onRespuesta(response.body().string());
                } else {
                    callback.onError("Error HTTP: " + response.code());
                }
            }
        });
    }

    // Método interno para hacer peticiones DELETE (borrar algo del servidor)
    private static void delete(String url, ApiCallback callback) {
        Request peticion = new Request.Builder().url(url).delete().build();

        cliente.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Si el borrado fue bien, devolvemos "OK" como confirmación
                    callback.onRespuesta("OK");
                } else {
                    callback.onError("Error HTTP: " + response.code());
                }
            }
        });
    }

    // =========================================================================
    // USUARIOS
    // =========================================================================

    // Manda el email y la contraseña al servidor para comprobar si son correctos
    public static void login(String email, String password, ApiCallback callback) {
        try {
            // Creamos un objeto JSON con los datos del login para enviárselos al servidor
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);
            post(BASE_URL + "/api/usuarios/login", json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // Pide al servidor la lista de todos los trabajadores registrados
    public static void getTrabajadores(ApiCallback callback) {
        get(BASE_URL + "/api/usuarios/trabajadores", callback);
    }

    // Crea un trabajador nuevo en el servidor con los datos que le pasamos
    public static void crearTrabajador(String nombre, String email, String password, ApiCallback callback) {
        try {
            // Montamos el JSON con todos los campos que necesita el servidor
            JSONObject json = new JSONObject();
            json.put("nombre", nombre);
            json.put("email", email);
            json.put("password", password);
            // Siempre asignamos el rol TRABAJADOR al crear desde esta pantalla
            json.put("rol", "TRABAJADOR");
            // Lo creamos como activo por defecto
            json.put("activo", true);
            post(BASE_URL + "/api/usuarios", json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // =========================================================================
    // PRODUCTOS
    // =========================================================================

    // Pide al servidor todos los productos del inventario
    public static void getProductos(ApiCallback callback) {
        get(BASE_URL + "/api/productos", callback);
    }

    // Envía al servidor los datos de un producto nuevo para guardarlo
    public static void crearProducto(String nombre, String descripcion, String proveedor,
                                     double precio, int stockActual, int stockMinimo,
                                     ApiCallback callback) {
        try {
            // Construimos el JSON con todos los campos del producto
            JSONObject json = new JSONObject();
            json.put("nombre", nombre);
            json.put("descripcion", descripcion);
            json.put("proveedor", proveedor);
            json.put("precio", precio);
            json.put("stockActual", stockActual);
            json.put("stockMinimo", stockMinimo);
            post(BASE_URL + "/api/productos", json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // Actualiza los datos de un producto que ya existe en el servidor (usando su id)
    public static void actualizarProducto(int id, String nombre, String descripcion,
                                          String proveedor, double precio,
                                          int stockActual, int stockMinimo,
                                          ApiCallback callback) {
        try {
            // Igual que crear, pero usamos PUT y añadimos el id en la URL
            JSONObject json = new JSONObject();
            json.put("nombre", nombre);
            json.put("descripcion", descripcion);
            json.put("proveedor", proveedor);
            json.put("precio", precio);
            json.put("stockActual", stockActual);
            json.put("stockMinimo", stockMinimo);
            // La URL incluye el id para que el servidor sepa qué producto actualizar
            put(BASE_URL + "/api/productos/" + id, json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // Le dice al servidor que borre el producto con ese id
    public static void eliminarProducto(int id, ApiCallback callback) {
        delete(BASE_URL + "/api/productos/" + id, callback);
    }

    // =========================================================================
    // TAREAS
    // =========================================================================

    // Pide todas las tareas que hay en el sistema (la usa el gerente para verlas todas)
    public static void getTareas(ApiCallback callback) {
        get(BASE_URL + "/api/tareas", callback);
    }

    // Pide solo las tareas que pertenecen a un trabajador concreto (por su id)
    public static void getTareasPorUsuario(int idUsuario, ApiCallback callback) {
        get(BASE_URL + "/api/tareas/usuario/" + idUsuario, callback);
    }

    // Crea una tarea nueva y la asigna a un trabajador
    public static void crearTarea(String titulo, String descripcion, String fecha,
                                  int idUsuario, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("titulo", titulo);
            json.put("descripcion", descripcion);
            json.put("fechaAsignacion", fecha);
            // Las tareas nuevas siempre empiezan como PENDIENTE
            json.put("estado", "PENDIENTE");
            // Indicamos a qué usuario (trabajador) se le asigna la tarea
            json.put("idUsuario", idUsuario);
            post(BASE_URL + "/api/tareas", json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // Cambia solo el estado de una tarea (por ejemplo de PENDIENTE a EN_PROGRESO)
    public static void cambiarEstadoTarea(int id, String nuevoEstado, ApiCallback callback) {
        try {
            // Solo mandamos el campo "estado", no hay que reenviar todo el objeto
            JSONObject json = new JSONObject();
            json.put("estado", nuevoEstado);
            // La URL tiene el id de la tarea y "/estado" para que el servidor sepa qué actualizar
            put(BASE_URL + "/api/tareas/" + id + "/estado", json.toString(), callback);
        } catch (Exception e) {
            callback.onError("Error al crear JSON: " + e.getMessage());
        }
    }

    // =========================================================================
    // PEDIDOS
    // =========================================================================

    // Pide los pedidos que ha hecho un cliente concreto (por su id de usuario)
    public static void getPedidosPorUsuario(int idUsuario, ApiCallback callback) {
        get(BASE_URL + "/api/pedidos/usuario/" + idUsuario, callback);
    }

    // Pide las líneas (productos y cantidades) de un pedido concreto
    public static void getLineasPedido(int idPedido, ApiCallback callback) {
        get(BASE_URL + "/api/pedidos/" + idPedido + "/productos", callback);
    }

    // Pide todos los pedidos del sistema (lo usa el gerente para ver los datos financieros)
    public static void getPedidos(ApiCallback callback) {
        get(BASE_URL + "/api/pedidos", callback);
    }

    // Pide los datos de un producto concreto buscándolo por su id
    public static void getProducto(int id, ApiCallback callback) {
        get(BASE_URL + "/api/productos/" + id, callback);
    }
}
