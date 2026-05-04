# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (requires signing config)
./gradlew clean                  # Clean build artifacts
./gradlew test                   # Run local unit tests (JVM)
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew test --tests "com.cidead.pmdm.proyecto_app.ExampleUnitTest"  # Run a single test class
```

## Project Overview

Android application (Java) — gestión empresarial para PYMEs bajo el nombre **DI-PYMES**. Soporta tres roles de usuario con pantallas independientes.

- **Package:** `com.cidead.pmdm.proyecto_app`
- **Min SDK:** 31 (Android 12), **Target SDK:** 36 (Android 15)
- **Language:** Java 11
- **UI:** Material3 Day/Night, XML layouts (no Jetpack Compose)
- **DB:** Room (SQLite local), base de datos `dipymes_db`

## Credenciales de prueba

| Email | Contraseña | Rol |
|-------|-----------|-----|
| `gerente@dipymes.com` | `1234` | GERENTE |
| `trabajador@dipymes.com` | `1234` | TRABAJADOR |
| `cliente@dipymes.com` | `1234` | CLIENTE |

## Arquitectura

**Capas:**
- `db/entity/` — Entidades Room: `Usuario`, `Tarea`, `Producto`, `Pedido`, `PedidoProducto`
- `db/dao/` — DAOs: `UsuarioDao`, `TareaDao`, `ProductoDao`, `PedidoDao`
- `db/AppDatabase` — Singleton Room, executor estático para operaciones en background, callback de pre-población con datos de prueba
- `adapter/` — RecyclerView adapters: `ProductoAdapter`, `TareaAdapter`, `PedidoAdapter`
- `SessionManager` — Gestión de sesión via SharedPreferences (`dipymes_session`)
- Activities — lógica de presentación directa (sin ViewModel/LiveData por ahora)

**Patrón de threading:** Toda operación de BD usa `AppDatabase.executor.execute(() -> { ... runOnUiThread(() -> { ... }); });`

**Flujo de login:** `LoginActivity` → consulta `UsuarioDao.login()` en background → guarda sesión con `SessionManager` → redirige según `rol`.

**Sesión persistente:** Si ya hay sesión activa al abrir el app, `LoginActivity` redirige directamente sin mostrar el formulario.

## Pantallas por rol

**GERENTE** (entrada: `DashboardGerenteActivity`):
- Dashboard con datos reales (ingresos, tareas pendientes, alertas de stock) cargados de Room
- `GestionStockActivity` → RecyclerView de productos + FAB para añadir
- `FormularioProductoActivity` → alta y edición de productos (modo edición vía `id_producto` en Intent)
- `TareasEquipoActivity` → todas las tareas, pulsar badge cambia estado (ciclo: PENDIENTE → EN_PROGRESO → COMPLETADA)
- `TrabajadoresActivity` → lista de usuarios con rol TRABAJADOR

**TRABAJADOR** (entrada: `PanelTareasActivity`):
- Reloj en tiempo real (handler con 30s interval), toggle fichar entrada/salida
- RecyclerView con sus tareas (filtradas por `id_usuario` de sesión), badge de pendientes dinámico
- `RegistrarTareaActivity` → formulario para nueva tarea, se guarda con fecha actual y estado PENDIENTE

**CLIENTE** (entrada: `HistorialPedidosActivity`):
- RecyclerView con pedidos del cliente actual (filtrado por `id_usuario`)
- `DetallePedidoActivity` → muestra productos del pedido con cantidades (JOIN via `pedido_producto`)

## Datos pre-cargados (AppDatabase.Callback.onCreate)

La DB se pre-puebla la primera vez: 3 usuarios, 4 productos, 3 tareas, 3 pedidos con líneas de pedido.

## Dependencias clave

Definidas en `gradle/libs.versions.toml`:
- `room-runtime` + `room-compiler` (annotationProcessor, no kapt — es Java)
- `recyclerview`
- `lifecycle-viewmodel` + `lifecycle-livedata` (añadidas, pendientes de usar en refactor)
- `material` (incluye MaterialCardView, NavigationView, FAB, CoordinatorLayout)

## Archivos clave

| Archivo | Propósito |
|---------|-----------|
| `db/AppDatabase.java` | Singleton + pre-población de datos |
| `SessionManager.java` | Leer/escribir/limpiar sesión de usuario |
| `LoginActivity.java` | Login real contra Room |
| `DashboardGerenteActivity.java` | Dashboard con drawer lateral |
| `res/menu/menu_gerente.xml` | Ítems del NavigationView del gerente |
| `gradle/libs.versions.toml` | Versiones centralizadas de dependencias |
