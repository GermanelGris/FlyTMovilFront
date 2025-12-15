# Fly Transportation - App Android

Aplicación móvil nativa para la búsqueda y gestión de vuelos, desarrollada en Android con Kotlin, Jetpack Compose y una arquitectura MVVM.

---

## Tabla de Contenidos

1. [Caso de Uso y Alcance](#1-caso-de-uso-y-alcance)
2. [Requisitos y Ejecución](#2-requisitos-y-ejecución)
3. [Arquitectura y Flujo](#3-arquitectura-y-flujo)
4. [Funcionalidades](#4-funcionalidades)
5. [Endpoints de la API](#5-endpoints-de-la-api)

---

## 1. Caso de Uso y Alcance

### Fly Transportation - Sistema de Gestión y Búsqueda de Vuelos

**Funcionalidades principales:**
- Registro e inicio de sesión de usuarios con roles (JWT).
- Búsqueda pública de vuelos con filtros inteligentes.
- Panel de administración para la gestión completa (CRUD) de vuelos programados.

La aplicación está diseñada para dos tipos de usuarios:

1.  **Usuarios Públicos:** Pueden buscar vuelos por origen, destino (opcional) y fecha. La búsqueda es flexible y muestra resultados a partir de la fecha seleccionada.
2.  **Usuarios Administradores:** Tras iniciar sesión, tienen acceso a un panel de control para crear, listar, modificar y eliminar vuelos, asegurando que los datos de la plataforma estén siempre actualizados.

La app funciona conectada a un **backend en Spring Boot** que maneja la lógica de negocio y la persistencia en la base de datos.

---

## 2. Requisitos y Ejecución

### Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (con Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit + OkHttp (con interceptor para logs y tokens)
- **Asincronía:** Kotlin Coroutines + StateFlow
- **Navegación:** Navigation Compose
- **Persistencia Local:** Jetpack DataStore para el token de sesión
- **Testing:** JUnit 4, Mockito, kotlinx-coroutines-test

### Requisitos para la App Android

- Android Studio Hedgehog (2023.1.1) o superior.
- JDK 17 o superior.
- Dispositivo/emulador con API 31+ (Android 12+).

### Configuración del Backend

La aplicación espera que el backend de Spring Boot esté ejecutándose localmente en el puerto `8090`.

### Configuración de la App

**1. Configurar la IP del backend en `RetrofitClient.kt`:**

El fichero `app/src/main/java/com/example/registroflytransportation/api/RetrofitClient.kt` está configurado para apuntar a `http://10.0.2.2:8090/`. Esta es la IP especial que el emulador de Android utiliza para conectarse al `localhost` de la máquina anfitriona. No es necesario cambiarla si ejecutas el backend y el emulador en la misma máquina.

**2. Ejecutar la app:**

1.  Abrir el proyecto en Android Studio.
2.  Sincronizar el proyecto con los ficheros de Gradle (`Sync Project with Gradle Files`).
3.  Ejecutar en un emulador o dispositivo físico (`Run 'app'`).

---

## 3. Arquitectura y Flujo

### Estructura de Carpetas

```
app/src/
├── api/                     # Servicios de red (Retrofit)
├── model/                   # Clases de datos (Data Classes)
├── ui/                      # Componentes de UI (Jetpack Compose)
│   ├── screens/             # Pantallas de la aplicación
│   └── theme/               # Tema de la app
├── util/                    # Clases de utilidad
└── viewModel/               # ViewModels con la lógica de negocio
```

### Patrón MVVM

- **Model:** Clases de datos en `model/` que reflejan las entidades de la API.
- **View:** Funciones Composable en `ui/screens/`, que observan los estados del ViewModel.
- **ViewModel:** Clases en `viewModel/` que gestionan la lógica, las llamadas a la API y exponen el estado a la UI mediante `StateFlow`.

### Flujo de Navegación

La app utiliza **Navigation Component** para la navegación entre pantallas. El flujo principal es:

1.  **Login/Registro** → (si exitoso) → **Home**
2.  Desde **Home**:
    -   Un usuario **público** puede buscar vuelos.
    -   Un usuario **ADMIN** ve un botón para acceder al **Panel de Administración**.

---

## 4. Funcionalidades

### 1. Autenticación y Sesión

- Formulario de registro y login.
- La sesión se mantiene activa gracias a Jetpack DataStore, que guarda el token JWT de forma segura.
- Un interceptor de OkHttp añade automáticamente el token `Bearer` a todas las peticiones a rutas protegidas.

### 2. Búsqueda de Vuelos (Pública)

- **Formulario de búsqueda inteligente:**
    -   Campo **Origen** con autocompletado.
    -   Campo **Destino** con autocompletado (opcional).
    -   Campo de **Fecha** con `DatePickerDialog` para evitar errores de formato.
- **Lógica flexible:** La búsqueda devuelve todos los vuelos a partir de la fecha seleccionada (`>=`).

### 3. Panel de Administración (CRUD)

- **Listado de vuelos:** Muestra todos los vuelos programados con su información clave.
- **Crear y Editar Vuelos:**
    -   Un formulario en un diálogo permite crear o modificar vuelos.
    -   Desplegable para seleccionar aerolíneas.
    -   Campos de autocompletado para origen y destino.
    -   Selectores de fecha para garantizar el formato correcto.
- **Eliminar Vuelos:** Acción protegida por un diálogo de confirmación.

---

## 5. Endpoints de la API

**URL Base:** `http://10.0.2.2:8090/`

### Autenticación (`/api/auth`)

| Método | Ruta         | Body                             | Respuesta        |
|--------|--------------|----------------------------------|------------------|
| `POST` | `/register`  | `{ username, password, roles }`  | `{ token, ... }` |
| `POST` | `/login`     | `{ username, password }`         | `{ token, ... }` |
| `GET`  | `/me`        | Header: `Authorization`          | `{ userProfile }`|

### Búsqueda Pública (`/api/vuelos-programados`)

| Método | Ruta         | Query Params                                  | Respuesta                 |
|--------|--------------|-----------------------------------------------|---------------------------|
| `GET`  | `/search`    | `origen`, `destino` (opc.), `fechaSalida`     | `List<VueloProgramadoDto>`|

### Administración (Rutas Protegidas)

| Método   | Ruta                          | Body / Params                   | Descripción                     |
|----------|-------------------------------|---------------------------------|---------------------------------|
| `GET`    | `/api/vuelos-programados`     | -                               | Obtiene todos los vuelos prog.  |
| `POST`   | `/api/vuelos-programados`     | `VueloProgramadoPayload`        | Crea un nuevo vuelo prog.       |
| `PUT`    | `/api/vuelos-programados/{id}`| `id`, `VueloProgramadoPayload`  | Actualiza un vuelo prog.        |
| `DELETE` | `/api/vuelos-programados/{id}`| `id`                            | Elimina un vuelo prog.          |
| `POST`   | `/api/vuelos`                 | `VueloBasePayload`              | Crea un vuelo base.             |
| `PUT`    | `/api/vuelos/{id}`            | `id`, `VueloBasePayload`        | Actualiza un vuelo base.        |
| `GET`    | `/api/aerolineas`             | -                               | Obtiene todas las aerolíneas.   |
| `GET`    | `/api/lugares/buscar`         | `q` (query)                     | Busca aeropuertos.              |
