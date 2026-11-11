# LAB - Obtener ubicación GPS en Android

Laboratorio académico para comprender el uso del GPS en Android mediante `FusedLocationProviderClient`, `LocationServices` y `Geocoder`.

## Objetivo del laboratorio

La aplicación obtiene la ubicación actual del dispositivo, muestra sus coordenadas y convierte la posición en una dirección aproximada mediante `Geocoder`.

Como extensión de la tarea, permite registrar la ubicación de los estudiantes de un curso y agruparlos por zona para identificar dónde se concentra la mayor cantidad de participantes. Esta información puede servir como base para decidir dónde priorizar tutorías o acceso a laboratorios.

> Este proyecto es un prototipo académico local. No envía información a un servidor ni comparte ubicaciones por Internet. Los registros se guardan en `SharedPreferences` del dispositivo utilizado para la demostración.

## Funcionalidades

- Solicitud del permiso `ACCESS_FINE_LOCATION` en tiempo de ejecución.
- Permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` en `AndroidManifest.xml`.
- Uso de `FusedLocationProviderClient`.
- Uso de `LocationServices`.
- Uso de `Geocoder` para obtener dirección/localidad a partir de latitud y longitud.
- Visualización de latitud, longitud, zona y dirección.
- Registro local del nombre del estudiante y su ubicación.
- Conteo de estudiantes agrupados por zona.
- Identificación automática de la zona con mayor concentración de estudiantes.
- Opción para limpiar los registros usados durante la demostración.

## Dependencia solicitada por la guía

En `app/build.gradle` se encuentra:

```gradle
implementation 'com.google.android.gms:play-services-location:17.1.0'
```

También se utiliza AndroidX AppCompat para mantener compatibilidad con las versiones actuales de Android Studio.

## Estructura del proyecto

```text
LAB-OBTENER-UBICACION/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/labubicaciongps/
│       │   └── MainActivity.java
│       └── res/
│           ├── drawable/
│           │   └── card_background.xml
│           ├── layout/
│           │   └── activity_main.xml
│           └── values/
│               ├── colors.xml
│               └── themes.xml
├── build.gradle
├── settings.gradle
├── gradle.properties
├── .gitignore
└── README.md
```

## Cómo funciona internamente

### 1. Permiso de GPS

Al tocar **Obtener ubicación actual**, la aplicación valida `ACCESS_FINE_LOCATION`.

Si el permiso no ha sido concedido, Android muestra el diálogo de autorización. Si el usuario acepta, se continúa con la lectura del GPS.

### 2. FusedLocationProviderClient

Se inicializa con:

```java
fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
```

Después se consulta la última ubicación conocida mediante:

```java
fusedLocationClient.getLastLocation()
```

Cuando existe una ubicación válida se obtienen:

- Latitud.
- Longitud.

### 3. Geocoder

Con las coordenadas se crea un `Geocoder` y se consulta una dirección:

```java
Geocoder geocoder = new Geocoder(this, Locale.getDefault());
List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
```

De la respuesta se intenta determinar:

1. `subLocality`.
2. `locality`.
3. `adminArea`.

Esto permite obtener una zona entendible para el usuario.

### 4. Registro de estudiantes

Después de obtener la ubicación, se habilita el botón **Registrar estudiante en esta zona**.

Cada registro guarda localmente:

- Nombre.
- Latitud.
- Longitud.
- Zona.
- Dirección.
- Fecha/hora interna del registro.

### 5. Agrupación por zonas

Los registros se recorren y se cuentan por zona usando un `HashMap<String, Integer>`.

Luego las zonas se ordenan de mayor a menor cantidad y se presenta un ranking. La primera zona del ranking se muestra como **zona con mayor concentración**.

## Cómo abrir y ejecutar el proyecto

### Requisitos

- Android Studio.
- JDK incluido con Android Studio o JDK compatible con el Android Gradle Plugin.
- Android SDK 35 instalado, o permitir que Android Studio lo instale al sincronizar.
- Emulador con Google Play Services o un teléfono Android físico.

### Paso 1 - Copiar el proyecto

Descomprimir el ZIP en:

```text
C:\Dev\LAB-OBTENER-UBICACION
```

### Paso 2 - Abrir en Android Studio

1. Abrir Android Studio.
2. Seleccionar **Open**.
3. Elegir:

```text
C:\Dev\LAB-OBTENER-UBICACION
```

4. Esperar la sincronización de Gradle.
5. Si Android Studio solicita configurar/actualizar Gradle o instalar Android SDK 35, aceptar la recomendación del IDE.

### Paso 3 - Ejecutar

#### Opción recomendada: teléfono físico

1. Activar **Opciones de desarrollador**.
2. Activar **Depuración USB**.
3. Conectar el teléfono por USB.
4. Activar la ubicación/GPS del teléfono.
5. En Android Studio seleccionar el dispositivo.
6. Pulsar **Run ▶**.

#### Opción emulador

1. Crear o iniciar un emulador con Google Play.
2. En el menú del emulador abrir **Extended controls > Location**.
3. Definir una coordenada de prueba.
4. Ejecutar la aplicación.

## Prueba rápida para mostrar al profesor

1. Abrir la aplicación.
2. Escribir el nombre de un estudiante.
3. Pulsar **Obtener ubicación actual**.
4. Aceptar el permiso de ubicación.
5. Mostrar latitud, longitud, localidad y dirección.
6. Pulsar **Registrar estudiante en esta zona**.
7. Cambiar la ubicación del emulador, o repetir el ejercicio con otros integrantes/dispositivos.
8. Registrar varios estudiantes.
9. Mostrar el apartado **Concentración por zonas**.
10. Explicar cuál es la zona con mayor cantidad de estudiantes y cómo este resultado podría apoyar la planeación de tutorías/laboratorios.

## Qué explicar en la sustentación

Puedes resumirlo así:

> La aplicación solicita permiso para acceder a la ubicación precisa del dispositivo. Utiliza `FusedLocationProviderClient`, proporcionado por Google Play Services, para obtener la latitud y longitud. Después utiliza `Geocoder` para convertir esas coordenadas en una dirección o localidad comprensible. Como extensión del laboratorio, cada estudiante puede registrar su ubicación y el sistema agrupa los participantes por zona. Finalmente muestra cuál zona tiene la mayor concentración, lo cual puede apoyar la planeación de servicios universitarios cercanos a los lugares donde vive la mayoría de estudiantes.

## Archivos principales

### `AndroidManifest.xml`

Contiene los permisos de ubicación y declara `MainActivity`.

### `app/build.gradle`

Contiene la dependencia de Google Play Services Location solicitada por el laboratorio.

### `activity_main.xml`

Define la interfaz: nombre del estudiante, botón de ubicación, datos GPS y resumen de zonas.

### `MainActivity.java`

Contiene toda la lógica de:

- Solicitud de permisos.
- Obtención del GPS.
- Geocodificación.
- Registro local de estudiantes.
- Conteo y clasificación de zonas.

## Subir a GitHub

Repositorio de entrega:

```text
https://github.com/EstebanMB18/LAB-OBTENER-UBICACION.git
```

Desde PowerShell:

```powershell
cd C:\Dev\LAB-OBTENER-UBICACION

git init
git add .
git commit -m "Laboratorio obtener ubicacion GPS Android"
git branch -M main
git remote add origin https://github.com/EstebanMB18/LAB-OBTENER-UBICACION.git
git push -u origin main
```

Si el repositorio ya fue clonado previamente, no ejecutes nuevamente `git init` ni `git remote add origin`; únicamente copia/actualiza los archivos y usa `git add`, `git commit` y `git push`.

## Consideraciones de privacidad

La ubicación es información personal. Para una aplicación real se debería informar claramente el propósito del tratamiento de datos, solicitar consentimiento y utilizar almacenamiento/transferencia segura. En este laboratorio los datos permanecen localmente en el dispositivo utilizado para la demostración.
