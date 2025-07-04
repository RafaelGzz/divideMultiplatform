#!/bin/bash

# Script para ayudar con la configuración de secrets para GitHub Actions
# Uso: ./scripts/setup-secrets.sh

set -e

echo "🔧 Configuración de Secrets para GitHub Actions"
echo "=============================================="
echo ""

# Verificar que los archivos necesarios existen
if [ ! -f "composeApp/divide-release-key.jks" ]; then
    echo "❌ Error: No se encontró composeApp/divide-release-key.jks"
    echo "   Asegúrate de que el keystore esté en la ubicación correcta"
    exit 1
fi

if [ ! -f "composeApp/src/google-services.json" ]; then
    echo "❌ Error: No se encontró composeApp/src/google-services.json"
    echo "   Asegúrate de que el archivo de Google Services esté en la ubicación correcta"
    exit 1
fi

echo "✅ Archivos necesarios encontrados"
echo ""

# Generar base64 para keystore
echo "🔑 Generando KEYSTORE_BASE64..."
KEYSTORE_BASE64=$(base64 -i composeApp/divide-release-key.jks)
echo "KEYSTORE_BASE64 generado (${#KEYSTORE_BASE64} caracteres)"
echo ""

# Generar base64 para google-services.json
echo "🔑 Generando GOOGLE_SERVICES_JSON..."
GOOGLE_SERVICES_BASE64=$(base64 -i composeApp/src/google-services.json)
echo "GOOGLE_SERVICES_JSON generado (${#GOOGLE_SERVICES_BASE64} caracteres)"
echo ""

# Crear archivo temporal con los secrets
SECRETS_FILE="github-secrets.txt"
cat > "$SECRETS_FILE" << EOF
# GitHub Secrets para Deployment Automático
# ==========================================
# 
# Copia estos valores a GitHub → Settings → Secrets and variables → Actions
# 
# IMPORTANTE: Elimina este archivo después de configurar los secrets
#            Este archivo contiene información sensible

# 1. KEYSTORE_BASE64
$KEYSTORE_BASE64

# 2. KEYSTORE_PASSWORD
# [INGRESA MANUALMENTE LA CONTRASEÑA DEL KEYSTORE]

# 3. KEY_ALIAS
# [INGRESA MANUALMENTE EL ALIAS DE LA CLAVE]

# 4. KEY_PASSWORD
# [INGRESA MANUALMENTE LA CONTRASEÑA DE LA CLAVE]

# 5. GOOGLE_SERVICES_JSON
$GOOGLE_SERVICES_BASE64

# 6. GOOGLE_PLAY_SERVICE_ACCOUNT_JSON
# [INGRESA MANUALMENTE EL JSON DE LA CUENTA DE SERVICIO]

EOF

echo "📄 Archivo '$SECRETS_FILE' creado con los secrets base64"
echo ""
echo "📋 Próximos pasos:"
echo "1. Abre el archivo '$SECRETS_FILE'"
echo "2. Completa los valores faltantes (passwords, alias, service account JSON)"
echo "3. Ve a tu repositorio en GitHub → Settings → Secrets and variables → Actions"
echo "4. Agrega cada secret con su valor correspondiente"
echo "5. ⚠️  ELIMINA el archivo '$SECRETS_FILE' después de configurar los secrets"
echo ""
echo "🔗 Más información en DEPLOYMENT_SETUP.md"

# Hacer el archivo solo legible por el usuario actual
chmod 600 "$SECRETS_FILE"

echo ""
echo "✅ Configuración completada"
echo "   El archivo '$SECRETS_FILE' tiene permisos restringidos (600)" 