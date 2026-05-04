package com.cidead.pmdm.proyecto_app;

// Esta es la pantalla principal del gerente. Muestra un resumen rápido de la empresa:
// ingresos totales, tareas pendientes y alertas de productos con poco stock.
// También tiene un menú lateral para navegar a otras secciones.

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class DashboardGerenteActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Contenedor principal que permite abrir el menú lateral deslizando
    private DrawerLayout drawerLayout;
    // Textos donde mostramos los datos del resumen (ingresos, tareas y alertas)
    private TextView tvIngresos, tvTareasPendientes, tvAlertasStock;
    // Lista vertical donde añadimos las filas de productos con stock bajo
    private LinearLayout llInventario;
    // Texto que aparece cuando no hay ninguna alerta de stock
    private TextView tvSinAlertas;
    // Gestor de sesión para saber quién está conectado
    private SessionManager session;

    // Se ejecuta cuando se crea la pantalla por primera vez
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_gerente);

        session = new SessionManager(this);

        // Configuramos la barra superior de la pantalla
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Conectamos el menú lateral (drawer) con la barra superior y el botón de hamburguesa
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        // El "toggle" es el botón de las tres rayas que abre y cierra el menú lateral
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Marcamos el ítem "Dashboard" como activo en el menú lateral
        navigationView.setCheckedItem(R.id.nav_dashboard);

        // Conectamos las variables con los elementos visuales del layout
        tvIngresos         = findViewById(R.id.tvIngresos);
        tvTareasPendientes = findViewById(R.id.tvTareasPendientes);
        tvAlertasStock     = findViewById(R.id.tvAlertasStock);
        llInventario       = findViewById(R.id.llInventario);
        tvSinAlertas       = findViewById(R.id.tvSinAlertas);

        // Mostramos el nombre del gerente conectado en la cabecera
        TextView tvPerfil = findViewById(R.id.tvPerfil);
        tvPerfil.setText("Perfil: " + session.getNombre());

        // Al pulsar la tarjeta de ingresos, vamos a la pantalla de información financiera
        MaterialCardView cardIngresos = findViewById(R.id.cardIngresos);
        cardIngresos.setOnClickListener(v ->
                startActivity(new Intent(this, InformacionFinancieraActivity.class)));

        // Al pulsar la tarjeta de tareas, vamos a la pantalla de gestión de tareas del equipo
        MaterialCardView cardTareas = findViewById(R.id.cardTareas);
        cardTareas.setOnClickListener(v ->
                startActivity(new Intent(this, TareasEquipoActivity.class)));

        // Registramos el comportamiento del botón "atrás" con la API moderna (API 33+).
        // Si el menú lateral está abierto, lo cerramos. Si no, salimos de la pantalla normalmente.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    // onResume se ejecuta cada vez que volvemos a esta pantalla (por ejemplo, al cerrar otra)
    // Lo usamos para refrescar los datos del resumen
    @Override
    protected void onResume() {
        super.onResume();
        cargarResumen();
    }

    // Lanza las tres cargas de datos de forma independiente para no esperar a una para hacer la otra
    private void cargarResumen() {
        // Cargamos pedidos, tareas y productos en paralelo con 3 llamadas independientes
        cargarIngresos();
        cargarTareasPendientes();
        cargarAlertasStock();
    }

    // Pide todos los pedidos y suma sus importes para mostrar el total de ingresos
    private void cargarIngresos() {
        ApiClient.getPedidos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el texto JSON en un array para recorrer todos los pedidos
                    JSONArray array = new JSONArray(respuesta);
                    double total = 0;
                    // Sumamos el importe de cada pedido para obtener el total
                    for (int i = 0; i < array.length(); i++) {
                        total += array.getJSONObject(i).getDouble("importeTotal");
                    }
                    double finalTotal = total;
                    // Actualizamos el texto en pantalla con el total formateado con 2 decimales
                    runOnUiThread(() -> tvIngresos.setText(String.format("€ %.2f", finalTotal)));
                } catch (Exception e) {
                    runOnUiThread(() -> tvIngresos.setText("€ --"));
                }
            }
            @Override
            public void onError(String error) {
                // Si hay error, mostramos guiones para indicar que no se pudieron cargar los datos
                runOnUiThread(() -> tvIngresos.setText("€ --"));
            }
        });
    }

    // Pide todas las tareas y cuenta cuántas NO están completadas
    private void cargarTareasPendientes() {
        ApiClient.getTareas(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    int pendientes = 0;
                    // Recorremos todas las tareas y contamos las que no están completadas
                    for (int i = 0; i < array.length(); i++) {
                        // Si el estado NO es "COMPLETADA", la contamos como pendiente
                        if (!"COMPLETADA".equals(array.getJSONObject(i).getString("estado"))) {
                            pendientes++;
                        }
                    }
                    int finalPend = pendientes;
                    runOnUiThread(() -> tvTareasPendientes.setText(finalPend + " pend."));
                } catch (Exception e) {
                    runOnUiThread(() -> tvTareasPendientes.setText("-- pend."));
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvTareasPendientes.setText("-- pend."));
            }
        });
    }

    // Pide todos los productos y muestra los que tienen el stock igual o por debajo del mínimo
    private void cargarAlertasStock() {
        ApiClient.getProductos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    int alertas = 0;

                    // Creamos un array temporal para guardar solo los productos con stock bajo
                    JSONArray alertaLista = new JSONArray();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject p = array.getJSONObject(i);
                        int stock = p.getInt("stockActual");
                        int minimo = p.getInt("stockMinimo");
                        // Si el stock actual es igual o menor al mínimo, hay alerta
                        if (stock <= minimo) {
                            alertas++;
                            alertaLista.put(p); // Lo añadimos a la lista de alertas
                        }
                    }

                    int finalAlertas = alertas;
                    JSONArray finalLista = alertaLista;

                    runOnUiThread(() -> {
                        // Mostramos cuántos productos están en alerta
                        tvAlertasStock.setText(finalAlertas + " prod.");
                        // Limpiamos las filas anteriores para no duplicarlas
                        llInventario.removeAllViews();

                        if (finalLista.length() == 0) {
                            // Si no hay alertas, mostramos el mensaje "todo bien"
                            tvSinAlertas.setVisibility(View.VISIBLE);
                            llInventario.setVisibility(View.GONE);
                        } else {
                            // Si hay alertas, ocultamos el mensaje y mostramos las filas
                            tvSinAlertas.setVisibility(View.GONE);
                            llInventario.setVisibility(View.VISIBLE);
                            // LayoutInflater nos permite crear vistas a partir de un archivo XML
                            LayoutInflater inflater = LayoutInflater.from(DashboardGerenteActivity.this);
                            for (int i = 0; i < finalLista.length(); i++) {
                                try {
                                    JSONObject p = finalLista.getJSONObject(i);
                                    // Inflamos el layout de cada fila de alerta
                                    View fila = inflater.inflate(R.layout.item_stock_alerta, llInventario, false);

                                    TextView tvNombre = fila.findViewById(R.id.tvNombreAlerta);
                                    TextView tvStock  = fila.findViewById(R.id.tvStockAlerta);
                                    TextView tvBadge  = fila.findViewById(R.id.tvBadgeAlerta);

                                    // Rellenamos los datos del producto en la fila
                                    tvNombre.setText(p.getString("nombre"));
                                    tvStock.setText("Stock: " + p.getInt("stockActual"));

                                    // Si el stock es 0 pintamos en rojo (agotado), si no en naranja (bajo)
                                    if (p.getInt("stockActual") == 0) {
                                        fila.setBackgroundColor(Color.parseColor("#FFEBEE")); // fondo rojo claro
                                        tvBadge.setText(" Agotado ");
                                        tvBadge.setBackgroundColor(Color.parseColor("#F44336")); // rojo
                                    } else {
                                        fila.setBackgroundColor(Color.parseColor("#FFF3E0")); // fondo naranja claro
                                        tvBadge.setText(" Bajo Stock ");
                                        tvBadge.setBackgroundColor(Color.parseColor("#FF9800")); // naranja
                                    }

                                    // Añadimos la fila construida al contenedor de alertas
                                    llInventario.addView(fila);
                                } catch (Exception ignored) {}
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> tvAlertasStock.setText("-- prod."));
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvAlertasStock.setText("-- prod.");
                    Toast.makeText(DashboardGerenteActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Este método se ejecuta cada vez que el usuario pulsa una opción del menú lateral
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Según la opción pulsada, navegamos a la pantalla correspondiente
        if (id == R.id.nav_stock) {
            startActivity(new Intent(this, GestionStockActivity.class));
        } else if (id == R.id.nav_tareas) {
            startActivity(new Intent(this, TareasEquipoActivity.class));
        } else if (id == R.id.nav_trabajadores) {
            startActivity(new Intent(this, TrabajadoresActivity.class));
        } else if (id == R.id.nav_logout) {
            // Cerramos sesión y volvemos a la pantalla de login
            session.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            // Estas flags limpian toda la pila de pantallas para que no se pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        // Cerramos el menú lateral después de pulsar cualquier opción
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
