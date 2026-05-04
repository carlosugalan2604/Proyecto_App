package com.cidead.pmdm.proyecto_app;

// Esta pantalla muestra el detalle completo de un pedido concreto:
// su cabecera (número, estado, fecha, importe) y la lista de productos que contiene.
// Para cargar los productos hace dos llamadas a la API: primero las líneas y luego los nombres.

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetallePedidoActivity extends AppCompatActivity {

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Leemos los datos que nos pasó la pantalla anterior mediante el Intent
        int idPedido   = getIntent().getIntExtra("id_pedido", -1);
        String estado  = getIntent().getStringExtra("estado");
        String fecha   = getIntent().getStringExtra("fecha");
        double importe = getIntent().getDoubleExtra("importe", 0);

        // Conectamos los TextViews de la cabecera del pedido
        TextView tvNumero  = findViewById(R.id.tvNumeroPedido);
        TextView tvEstado  = findViewById(R.id.tvEstadoPedido);
        TextView tvFecha   = findViewById(R.id.tvFechaPedido);
        TextView tvImporte = findViewById(R.id.tvImportePedido);

        // Rellenamos la cabecera con los datos del pedido
        tvNumero.setText("Pedido #" + idPedido);
        tvFecha.setText("Fecha: " + fecha);
        tvImporte.setText(String.format("Importe total: € %.2f", importe));

        // Pintamos el badge de estado con un color diferente según el valor
        if (estado != null) {
            switch (estado) {
                case "ENTREGADO":
                    tvEstado.setText(" Entregado ");
                    tvEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // verde
                    break;
                case "EN_PROCESO":
                    tvEstado.setText(" En proceso ");
                    tvEstado.setBackgroundColor(Color.parseColor("#FF9800")); // naranja
                    break;
                case "CANCELADO":
                    tvEstado.setText(" Cancelado ");
                    tvEstado.setBackgroundColor(Color.parseColor("#F44336")); // rojo
                    break;
            }
        }

        // Si el id del pedido es válido, cargamos los productos que contiene
        if (idPedido != -1) {
            cargarProductos(idPedido);
        }
    }

    // Paso 1: pide las líneas del pedido (cada línea tiene el id del producto y la cantidad)
    private void cargarProductos(int idPedido) {
        ApiClient.getLineasPedido(idPedido, new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON a array para recorrer las líneas
                    JSONArray lineas = new JSONArray(respuesta);

                    // Listas para guardar los ids de producto y cantidades de cada línea
                    List<Integer> idsProducto = new ArrayList<>();
                    List<Integer> cantidades  = new ArrayList<>();

                    for (int i = 0; i < lineas.length(); i++) {
                        JSONObject linea = lineas.getJSONObject(i);
                        // El id de la línea es un objeto compuesto que contiene "idProducto"
                        JSONObject id = linea.getJSONObject("id");
                        idsProducto.add(id.getInt("idProducto"));
                        cantidades.add(linea.getInt("cantidad"));
                    }

                    // Paso 2: con los ids de producto, pedimos los nombres y precios
                    cargarNombresProductos(idsProducto, cantidades);

                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(DetallePedidoActivity.this, "Error al cargar líneas", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(DetallePedidoActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Paso 2: pide todos los productos y busca el nombre y precio de cada id que necesitamos
    private void cargarNombresProductos(List<Integer> ids, List<Integer> cantidades) {
        // Pedimos todos los productos porque no hay endpoint para pedirlos por lista de ids
        ApiClient.getProductos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray productos = new JSONArray(respuesta);

                    // Listas para guardar el nombre y precio de cada producto del pedido
                    List<String>  nombres  = new ArrayList<>();
                    List<Double>  precios  = new ArrayList<>();

                    // Para cada id que necesitamos, buscamos su nombre y precio en la lista completa
                    for (int idBuscado : ids) {
                        String nombre = "Producto #" + idBuscado; // valor por defecto si no se encuentra
                        double precio = 0;
                        for (int j = 0; j < productos.length(); j++) {
                            JSONObject p = productos.getJSONObject(j);
                            // Cuando encontramos el producto con ese id, guardamos sus datos
                            if (p.getInt("idProducto") == idBuscado) {
                                nombre = p.getString("nombre");
                                precio = p.getDouble("precio");
                                break; // Encontrado, dejamos de buscar
                            }
                        }
                        nombres.add(nombre);
                        precios.add(precio);
                    }

                    // Pasamos los tres arrays al método que construye las filas en pantalla
                    runOnUiThread(() -> mostrarProductos(nombres, precios, cantidades));

                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(DetallePedidoActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(DetallePedidoActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Construye y muestra en pantalla todas las filas de productos del pedido
    private void mostrarProductos(List<String> nombres, List<Double> precios, List<Integer> cantidades) {
        LinearLayout llProductos = findViewById(R.id.llProductos);
        // Borramos las filas anteriores para no duplicarlas si se recarga
        llProductos.removeAllViews();

        // Creamos una fila por cada producto y la añadimos al contenedor
        for (int i = 0; i < nombres.size(); i++) {
            llProductos.addView(crearFilaProducto(nombres.get(i), precios.get(i), cantidades.get(i)));
        }
    }

    // Crea la vista (fila) de un producto con su nombre, precio unitario y cantidad
    // Aquí construimos la vista en código Java en lugar de usar un archivo XML de layout
    private View crearFilaProducto(String nombre, double precio, int cantidad) {
        // Fila horizontal que contiene los datos del producto y la cantidad
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(24, 16, 24, 16);
        fila.setBackgroundColor(Color.WHITE);

        // Columna izquierda: nombre y precio del producto
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        // weight=1 hace que este bloque ocupe todo el espacio sobrante
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(lp);

        // Texto del nombre del producto
        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextSize(14);
        tvNombre.setTextColor(Color.parseColor("#212121")); // casi negro

        // Texto del precio unitario
        TextView tvPrecio = new TextView(this);
        tvPrecio.setText("€ " + String.format("%.2f", precio) + " c/u");
        tvPrecio.setTextSize(12);
        tvPrecio.setTextColor(Color.parseColor("#757575")); // gris

        info.addView(tvNombre);
        info.addView(tvPrecio);

        // Texto de la cantidad en el lado derecho, en negrita y con color verde
        TextView tvCantidad = new TextView(this);
        tvCantidad.setText("x" + cantidad);
        tvCantidad.setTextSize(16);
        tvCantidad.setTypeface(null, Typeface.BOLD); // negrita
        tvCantidad.setTextColor(Color.parseColor("#00CC99")); // verde

        // Añadimos los dos bloques a la fila
        fila.addView(info);
        fila.addView(tvCantidad);

        // Creamos una línea separadora de 1 píxel de alto entre productos
        View separador = new View(this);
        LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        separador.setLayoutParams(sepLp);
        separador.setBackgroundColor(Color.parseColor("#E0E0E0")); // gris claro

        // Metemos la fila y el separador en un contenedor vertical para devolver todo junto
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.addView(fila);
        contenedor.addView(separador);

        return contenedor;
    }

    // Cierra esta pantalla al pulsar la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
