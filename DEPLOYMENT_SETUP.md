# Configuración de Deployment Automático

Este documento explica cómo configurar GitHub Actions para desplegar automáticamente la app a Play Console como prueba interna.

## Prerrequisitos

1. **Cuenta de Google Play Console** con la app ya creada
2. **Keystore file** (divide-release-key.jks) para firmar la app
3. **Google Services JSON** para Firebase
4. **Service Account** de Google Cloud para acceder a Play Console

## Configuración de Secrets en GitHub

Ve a tu repositorio en GitHub → Settings → Secrets and variables → Actions, y agrega estos secrets:

### 1. KEYSTORE_BASE64
```bash
# Convertir el keystore a base64
base64 -i composeApp/divide-release-key.jks | pbcopy
```
Pega el resultado en el secret `KEYSTORE_BASE64`

### 2. KEYSTORE_PASSWORD
La contraseña del keystore (storePassword)

### 3. KEY_ALIAS
El alias de la clave (keyAlias)

### 4. KEY_PASSWORD
La contraseña de la clave (keyPassword)

### 5. GOOGLE_SERVICES_JSON
```bash
# Convertir google-services.json a base64
base64 -i composeApp/src/google-services.json | pbcopy
```
Pega el resultado en el secret `GOOGLE_SERVICES_JSON`

### 6. GOOGLE_PLAY_SERVICE_ACCOUNT_JSON
JSON de la cuenta de servicio de Google Cloud (ver sección siguiente)

## Configuración de Service Account

### 1. Crear Service Account en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Selecciona tu proyecto (el mismo que uses para Firebase)
3. Ve a IAM & Admin → Service Accounts
4. Crea una nueva cuenta de servicio:
   - Nombre: `play-console-deploy`
   - Descripción: `Service account for Play Console deployment`
5. Descarga el JSON de la cuenta de servicio

### 2. Configurar permisos en Play Console

1. Ve a [Play Console](https://play.google.com/console/)
2. Ve a Setup → API access
3. Busca la cuenta de servicio que creaste
4. Otorga los siguientes permisos:
   - **Release manager**: Para subir y publicar releases
   - **View app information**: Para ver información de la app

### 3. Agregar el JSON como secret

Copia todo el contenido del archivo JSON de la cuenta de servicio y pégalo en el secret `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

## Uso del Workflow

### Deployment automático
El workflow se ejecuta automáticamente cuando haces push a la rama `master`.

### Deployment manual
1. Ve a Actions en tu repositorio de GitHub
2. Selecciona "Deploy to Play Console Internal Testing"
3. Haz clic en "Run workflow"
4. Opcionalmente, especifica una nueva versión:
   - Version name: e.g., "1.0.16"
   - Version code: e.g., "1016"

## Verificación

Después de ejecutar el workflow:

1. Ve a Play Console → Testing → Internal testing
2. Verifica que la nueva versión esté disponible
3. El bundle también se guarda como artifact en GitHub Actions por 30 días

## Troubleshooting

### Error de permisos
- Verifica que la cuenta de servicio tenga los permisos correctos en Play Console
- Asegúrate de que el JSON de la cuenta de servicio esté correctamente configurado

### Error de keystore
- Verifica que todos los secrets del keystore estén correctamente configurados
- Asegúrate de que el keystore esté en base64 correctamente

### Error de build
- Verifica que google-services.json esté correctamente configurado
- Revisa los logs del workflow para errores específicos

## Comandos útiles

```bash
# Compilar localmente para verificar
./gradlew bundleRelease

# Verificar que el bundle se genera correctamente
ls -la composeApp/build/outputs/bundle/release/
``` 