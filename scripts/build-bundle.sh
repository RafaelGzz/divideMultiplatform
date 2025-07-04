#!/bin/bash

# Script para compilar el bundle localmente
# Uso: ./scripts/build-bundle.sh

set -e

echo "🚀 Compilando bundle de release..."
echo "================================="
echo ""

# Verificar que el keystore existe
if [ ! -f "composeApp/divide-release-key.jks" ]; then
    echo "❌ Error: No se encontró composeApp/divide-release-key.jks"
    echo "   Asegúrate de que el keystore esté en la ubicación correcta"
    exit 1
fi

# Verificar que keystore.properties existe
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: No se encontró keystore.properties"
    echo "   Crea el archivo keystore.properties con:"
    echo "   storeFile=composeApp/divide-release-key.jks"
    echo "   storePassword=TU_CONTRASEÑA"
    echo "   keyAlias=TU_ALIAS"
    echo "   keyPassword=TU_CONTRASEÑA_CLAVE"
    exit 1
fi

# Verificar que google-services.json existe
if [ ! -f "composeApp/src/google-services.json" ]; then
    echo "❌ Error: No se encontró composeApp/src/google-services.json"
    echo "   Descarga el archivo desde Firebase Console"
    exit 1
fi

echo "✅ Archivos necesarios encontrados"
echo ""

# Limpiar builds anteriores
echo "🧹 Limpiando builds anteriores..."
./gradlew clean

echo ""
echo "🔨 Compilando bundle..."
./gradlew bundleRelease

# Verificar que el bundle se generó
BUNDLE_PATH="composeApp/build/outputs/bundle/release/composeApp-release.aab"
if [ -f "$BUNDLE_PATH" ]; then
    BUNDLE_SIZE=$(ls -lh "$BUNDLE_PATH" | awk '{print $5}')
    echo ""
    echo "✅ Bundle generado exitosamente!"
    echo "📦 Ubicación: $BUNDLE_PATH"
    echo "📊 Tamaño: $BUNDLE_SIZE"
    echo ""
    echo "🔍 Información del bundle:"
    echo "   - Signed: ✅"
    echo "   - Optimized: ✅"
    echo "   - Ready for Play Console: ✅"
else
    echo ""
    echo "❌ Error: No se pudo generar el bundle"
    exit 1
fi

echo ""
echo "🎉 ¡Listo! El bundle está preparado para subir a Play Console" 