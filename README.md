# Filtro de red (bloqueador de Life360)

App de Android que corta el acceso a internet **solo** de Life360 usando una VPN local.
Ningún dato sale del teléfono: los paquetes de Life360 se descartan y el resto de las apps
no pasa por la VPN.

## Obtener el APK

GitHub Actions lo compila en cada push (workflow `Build APK`).
Descarga el artefacto `filtro-de-red-apk` de la última ejecución, descomprímelo e instala
`app-debug.apk` en el teléfono (hay que permitir "instalar apps desconocidas").

## Uso

1. Abrir "Filtro de red" → **Activar bloqueo** → aceptar el aviso de VPN.
2. Para que sobreviva reinicios: Ajustes → Red → VPN → engrane de "Filtro de red" →
   activar **VPN siempre activa**.
3. **No** activar "Bloquear conexiones sin VPN": como esta VPN solo cubre a Life360,
   esa opción dejaría sin internet a todo el teléfono.

## Limitaciones

- La otra persona verá que la ubicación dejó de actualizarse (no se falsea la ubicación).
- Solo puede haber una VPN activa a la vez en Android.
- Justo al encender el teléfono, antes de que arranque la VPN, Life360 podría enviar datos.
- No protege contra otros métodos de rastreo (cuenta de Google, rastreadores Tile/AirTag, etc.).
