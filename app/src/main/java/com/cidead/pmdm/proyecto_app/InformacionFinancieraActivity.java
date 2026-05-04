package com.cidead.pmdm.proyecto_app;

// Esta pantalla muestra al gerente un resumen financiero de todos los pedidos.
// Calcula el total de ingresos y los desglosa por estado (Entregados, En proceso, Cancelados).
// También dibuja barras de progreso proporcionales a los porcentajes de cada estado.

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

public class InformacionFinancieraActivity extends AppCompatActivity {

    // TextViews para mostrar los totales y sumas de cada grupo de pedidos
    private TextView tvTotalIngresos;
    private TextView tvNumEntregados, tvSumaEntregados;
    private TextView tvNumEnProceso,  tvSumaEnProceso;
    private TextView tvNumCancelados, tvSumaCancelados;
    // TextViews para mostrar el porcentaje de cada estado
    private TextView tvPctEntregados, tvPctEnProceso, tvPctCancelados;
    // Vistas que usamos como barras de progreso (su ancho se ajusta según el porcentaje)
    private View barraEntregados, barraEnProceso, barraCancelados;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion_financiera);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Conectamos todos los elementos visuales con sus variables
        tvTotalIngresos  = findViewById(R.id.tvTotalIngresos);
        tvNumEntregados  = findViewById(R.id.tvNumEntregados);
        tvSumaEntregados = findViewById(R.id.tvSumaEntregados);
        tvNumEnProceso   = findViewById(R.id.tvNumEnProceso);
        tvSumaEnProceso  = findViewById(R.id.tvSumaEnProceso);
        tvNumCancelados  = findViewById(R.id.tvNumCancelados);
        tvSumaCancelados = findViewById(R.id.tvSumaCancelados);
        tvPctEntregados  = findViewById(R.id.tvPctEntregados);
        tvPctEnProceso   = findViewById(R.id.tvPctEnProceso);
        tvPctCancelados  = findViewById(R.id.tvPctCancelados);
        barraEntregados  = findViewById(R.id.barraEntregados);
        barraEnProceso   = findViewById(R.id.barraEnProceso);
        barraCancelados  = findViewById(R.id.barraCancelados);

        // Lanzamos la carga de datos al entrar en la pantalla
        cargarDatos();
    }

    // Cierra esta pantalla al pulsar la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Pide todos los pedidos a la API y calcula los datos financieros
    private void cargarDatos() {
        ApiClient.getPedidos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON en un array de pedidos
                    JSONArray array = new JSONArray(respuesta);

                    // Acumuladores para ir sumando mientras recorremos los pedidos
                    double totalIngresos  = 0;
                    int    numEntregados  = 0;
                    double sumaEntregados = 0;
                    int    numEnProceso   = 0;
                    double sumaEnProceso  = 0;
                    int    numCancelados  = 0;
                    double sumaCancelados = 0;

                    // Recorremos todos los pedidos y vamos acumulando según su estado
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject pedido = array.getJSONObject(i);
                        String estado = pedido.getString("estado");
                        double importe = pedido.getDouble("importeTotal");

                        // Sumamos todos los importes sin importar el estado para el total
                        totalIngresos += importe;

                        // Clasificamos cada pedido en su grupo y sumamos su importe
                        switch (estado) {
                            case "ENTREGADO":
                                numEntregados++;
                                sumaEntregados += importe;
                                break;
                            case "EN_PROCESO":
                                numEnProceso++;
                                sumaEnProceso += importe;
                                break;
                            case "CANCELADO":
                                numCancelados++;
                                sumaCancelados += importe;
                                break;
                        }
                    }

                    // Total de pedidos para calcular los porcentajes
                    int total = numEntregados + numEnProceso + numCancelados;

                    // Necesitamos variables "final" para poder usarlas dentro del lambda
                    double fTotal = totalIngresos;
                    int fNE = numEntregados; double fSE = sumaEntregados;
                    int fNP = numEnProceso;  double fSP = sumaEnProceso;
                    int fNC = numCancelados; double fSC = sumaCancelados;

                    // Actualizamos todos los textos en el hilo principal
                    runOnUiThread(() -> {
                        // Total general de ingresos
                        tvTotalIngresos.setText(String.format("€ %.2f", fTotal));

                        // Datos del grupo "Entregados"
                        tvNumEntregados.setText(String.valueOf(fNE));
                        tvSumaEntregados.setText(String.format("€ %.2f", fSE));

                        // Datos del grupo "En proceso"
                        tvNumEnProceso.setText(String.valueOf(fNP));
                        tvSumaEnProceso.setText(String.format("€ %.2f", fSP));

                        // Datos del grupo "Cancelados"
                        tvNumCancelados.setText(String.valueOf(fNC));
                        tvSumaCancelados.setText(String.format("€ %.2f", fSC));

                        // Actualizamos las barras de progreso con los porcentajes calculados
                        actualizarGrafico(total, fNE, fNP, fNC);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(InformacionFinancieraActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(InformacionFinancieraActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Calcula los porcentajes y ajusta el ancho de cada barra proporcionalmente
    private void actualizarGrafico(int total, int entregados, int enProceso, int cancelados) {
        // Si no hay pedidos, mostramos todo a 0% para evitar dividir entre cero
        if (total == 0) {
            tvPctEntregados.setText("0%");
            tvPctEnProceso.setText("0%");
            tvPctCancelados.setText("0%");
            return;
        }

        // Calculamos el porcentaje de cada grupo respecto al total de pedidos
        // Math.round redondea al entero más cercano
        int pctEntregados = Math.round(entregados * 100f / total);
        int pctEnProceso  = Math.round(enProceso  * 100f / total);
        int pctCancelados = Math.round(cancelados  * 100f / total);

        // Mostramos los porcentajes en los TextViews correspondientes
        tvPctEntregados.setText(pctEntregados + "%");
        tvPctEnProceso.setText(pctEnProceso   + "%");
        tvPctCancelados.setText(pctCancelados  + "%");

        // Ajustamos el ancho de las barras cuando el layout ya está dibujado y conocemos el ancho real
        // addOnGlobalLayoutListener espera a que la vista esté completamente pintada antes de medir
        barraEntregados.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Obtenemos el ancho total del contenedor padre para calcular proporcionalmente
            int anchoTotal = ((View) barraEntregados.getParent()).getWidth();
            if (anchoTotal == 0) return; // Si todavía no tiene ancho, no hacemos nada

            // Ajustamos el ancho de cada barra según su porcentaje
            setBarWidth(barraEntregados, anchoTotal, pctEntregados);
            setBarWidth(barraEnProceso,  anchoTotal, pctEnProceso);
            setBarWidth(barraCancelados, anchoTotal, pctCancelados);
        });
    }

    // Cambia el ancho de una barra para que represente el porcentaje dado del ancho total
    private void setBarWidth(View barra, int parentWidth, int pct) {
        android.view.ViewGroup.LayoutParams lp = barra.getLayoutParams();
        // El ancho de la barra es el porcentaje del ancho del contenedor
        lp.width = (int) (parentWidth * pct / 100f);
        barra.setLayoutParams(lp);
    }
}
