package com.cidead.pmdm.proyecto_app;

// Esta pantalla sirve para crear un producto nuevo o editar uno que ya existe.
// Si se abre desde el botón "+", es modo creación. Si se abre desde "Editar", carga los datos del producto.

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

public class FormularioProductoActivity extends AppCompatActivity {

    // Campos de texto del formulario
    private EditText etNombre, etDescripcion, etProveedor, etPrecio, etStockActual, etStockMinimo;
    // Guardamos el id del producto si estamos editando. Si es -1, estamos creando uno nuevo.
    private int idProductoEditar = -1;  // -1 = modo alta, otro valor = modo edición

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_producto);

        // Configuramos la barra superior con el botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Conectamos cada variable con su campo del layout
        etNombre      = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etProveedor   = findViewById(R.id.etProveedor);
        etPrecio      = findViewById(R.id.etPrecio);
        etStockActual = findViewById(R.id.etStockActual);
        etStockMinimo = findViewById(R.id.etStockMinimo);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Intentamos leer el id del producto que nos pasaron al abrir esta pantalla
        // Si no nos pasaron ninguno, getIntExtra devuelve -1 (valor por defecto)
        idProductoEditar = getIntent().getIntExtra("id_producto", -1);

        if (idProductoEditar != -1) {
            // Si tenemos un id, estamos en modo edición: cambiamos el título y cargamos los datos
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Editar Producto");
            cargarProducto(idProductoEditar);
        } else {
            // Si no hay id, es un producto nuevo
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Nuevo Producto");
        }

        // Al pulsar guardar, ejecutamos el método que valida y envía los datos
        btnGuardar.setOnClickListener(v -> guardarProducto());
    }

    // Carga los datos del producto desde la API para rellenar el formulario en modo edición
    private void cargarProducto(int id) {
        // Pedimos todos los productos y buscamos el que tiene el id que nos interesa
        // (porque no hay un endpoint directo para obtener uno por id en esta versión)
        ApiClient.getProductos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    // Recorremos el array hasta encontrar el producto con el id correcto
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        // Comparamos el id de cada producto con el que queremos editar
                        if (obj.getInt("idProducto") == id) {
                            // Cuando lo encontramos, rellenamos los campos del formulario en el hilo principal
                            runOnUiThread(() -> {
                                try {
                                    etNombre.setText(obj.getString("nombre"));
                                    etDescripcion.setText(obj.optString("descripcion", ""));
                                    etProveedor.setText(obj.optString("proveedor", ""));
                                    // Convertimos el número a texto para mostrarlo en el EditText
                                    etPrecio.setText(String.valueOf(obj.getDouble("precio")));
                                    etStockActual.setText(String.valueOf(obj.getInt("stockActual")));
                                    etStockMinimo.setText(String.valueOf(obj.getInt("stockMinimo")));
                                } catch (Exception e) {
                                    Toast.makeText(FormularioProductoActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break; // Ya encontramos el producto, no seguimos buscando
                        }
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(FormularioProductoActivity.this, "Error al cargar producto", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(FormularioProductoActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Recoge los datos del formulario, los valida y los envía a la API (crear o actualizar)
    private void guardarProducto() {
        // Recogemos el texto de cada campo y eliminamos espacios sobrantes
        String nombre      = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String proveedor   = etProveedor.getText().toString().trim();
        String precioStr   = etPrecio.getText().toString().trim();
        String stockAStr   = etStockActual.getText().toString().trim();
        String stockMStr   = etStockMinimo.getText().toString().trim();

        // Comprobamos que los campos obligatorios no estén vacíos antes de intentar guardar
        if (nombre.isEmpty() || proveedor.isEmpty() || precioStr.isEmpty()
                || stockAStr.isEmpty() || stockMStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertimos los textos a números para enviarlos a la API
        double precio      = Double.parseDouble(precioStr);
        int    stockActual = Integer.parseInt(stockAStr);
        int    stockMinimo = Integer.parseInt(stockMStr);

        // Los valores negativos no tienen sentido para precio o stock, así que los rechazamos
        if (precio < 0 || stockActual < 0 || stockMinimo < 0) {
            Toast.makeText(this, "Los valores numéricos no pueden ser negativos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Definimos el callback una sola vez para reutilizarlo tanto en crear como en actualizar
        ApiClient.ApiCallback callback = new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                runOnUiThread(() -> {
                    Toast.makeText(FormularioProductoActivity.this, "Producto guardado correctamente", Toast.LENGTH_SHORT).show();
                    // Cerramos el formulario y volvemos a la lista de productos
                    finish();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(FormularioProductoActivity.this, "Error al guardar producto", Toast.LENGTH_SHORT).show());
            }
        };

        // Si el id es -1 creamos el producto; si tiene un id válido, lo actualizamos
        if (idProductoEditar == -1) {
            ApiClient.crearProducto(nombre, descripcion, proveedor, precio, stockActual, stockMinimo, callback);
        } else {
            ApiClient.actualizarProducto(idProductoEditar, nombre, descripcion, proveedor, precio, stockActual, stockMinimo, callback);
        }
    }

    // Cuando se pulsa la flecha "volver" en la barra superior, cerramos el formulario
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
