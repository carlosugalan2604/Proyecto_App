package com.cidead.pmdm.proyecto_app;

// Esta pantalla muestra la lista de todos los productos del inventario.
// El gerente puede ver, editar o eliminar productos, y también añadir nuevos.

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.adapter.ProductoAdapter;
import com.cidead.pmdm.proyecto_app.db.entity.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GestionStockActivity extends AppCompatActivity {

    // El adapter es el que se encarga de mostrar cada producto como una fila en la lista
    private ProductoAdapter adapter;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_stock);

        // Configuramos la barra superior y el botón de "volver atrás"
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // El RecyclerView es la lista que muestra los productos uno debajo de otro
        RecyclerView rv = findViewById(R.id.rvProductos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Creamos el adapter con la lista vacía y los dos listeners de editar y eliminar
        adapter = new ProductoAdapter(new ArrayList<>(), new ProductoAdapter.OnProductoClickListener() {

            // Cuando el usuario pulsa "Editar" en un producto, abrimos el formulario pasándole el id
            @Override
            public void onEditar(Producto producto) {
                Intent intent = new Intent(GestionStockActivity.this, FormularioProductoActivity.class);
                // Pasamos el id del producto para que el formulario sepa cuál tiene que editar
                intent.putExtra("id_producto", producto.idProducto);
                startActivity(intent);
            }

            // Cuando el usuario pulsa "Eliminar", mostramos un diálogo de confirmación antes de borrar
            @Override
            public void onEliminar(Producto producto) {
                // Pedimos confirmación al usuario antes de borrar para evitar borrados accidentales
                new AlertDialog.Builder(GestionStockActivity.this)
                    .setTitle("Eliminar producto")
                    .setMessage("¿Seguro que quieres eliminar \"" + producto.nombre + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        // Si el usuario confirma, llamamos a la API para borrar el producto
                        ApiClient.eliminarProducto(producto.idProducto, new ApiClient.ApiCallback() {
                            @Override
                            public void onRespuesta(String respuesta) {
                                // Si se borró bien, recargamos la lista para que desaparezca
                                runOnUiThread(() -> cargarProductos());
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() ->
                                    Toast.makeText(GestionStockActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null) // Si cancela, no hacemos nada
                    .show();
            }
        });
        rv.setAdapter(adapter);

        // El botón flotante "+" abre el formulario para añadir un producto nuevo (sin id)
        FloatingActionButton fab = findViewById(R.id.fabAgregarProducto);
        fab.setOnClickListener(v ->
            startActivity(new Intent(this, FormularioProductoActivity.class))
        );
    }

    // Cada vez que volvemos a esta pantalla (por ejemplo, tras guardar un producto), recargamos la lista
    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos();
    }

    // Pide la lista de productos a la API y la muestra en la pantalla
    private void cargarProductos() {
        ApiClient.getProductos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON que llegó en un array de objetos
                    JSONArray array = new JSONArray(respuesta);
                    List<Producto> lista = new ArrayList<>();
                    // Recorremos cada objeto JSON y creamos un objeto Producto con sus datos
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Producto p = new Producto(
                            obj.getString("nombre"),
                            obj.optString("descripcion", ""), // optString devuelve "" si el campo no existe
                            obj.optString("proveedor", ""),
                            obj.getDouble("precio"),
                            obj.getInt("stockActual"),
                            obj.getInt("stockMinimo")
                        );
                        // Guardamos también el id para poder editar o borrar después
                        p.idProducto = obj.getInt("idProducto");
                        lista.add(p);
                    }
                    // Le damos la lista actualizada al adapter para que refresque la pantalla
                    runOnUiThread(() -> adapter.setLista(lista));
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(GestionStockActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(GestionStockActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cuando se pulsa la flecha de "volver" en la barra superior, cerramos esta pantalla
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
