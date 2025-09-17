package com.farmdigitalelite.maps.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.farmdigitalelite.farm.BuildConfig

/**
 * Utilidades para verificar y manejar la API key de Google Maps.
 * 
 * @author Farm Digital Elite Team
 * @since 1.0.0
 */
object MapsApiKeyUtils {
    
    private const val TAG = "MapsApiKeyUtils"
    
    /**
     * Verifica si la API key de Maps está configurada correctamente.
     * 
     * @param context Contexto de la aplicación
     * @return Pair<Boolean, Int> donde el primer valor indica si la key está presente
     *         y el segundo es la longitud de la key (sin revelar la key real)
     */
    fun Context.readMapsApiKeyMasked(): Pair<Boolean, Int> {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val key = applicationInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
            
            val isValid = key.isNotBlank() && key != "null" && key.length > 10
            
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Maps key present: $isValid len=${key.length}")
            }
            
            Pair(isValid, key.length)
        } catch (e: Exception) {
            Log.w(TAG, "Error reading Maps API key", e)
            Pair(false, 0)
        }
    }
    
    /**
     * Verifica si la aplicación tiene permisos de ubicación.
     * 
     * @param context Contexto de la aplicación
     * @return true si tiene permisos de ubicación
     */
    fun Context.hasLocationPermissions(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Obtiene la posición de fallback para el mapa cuando no hay ubicación disponible.
     * 
     * @return LatLng con coordenadas de Buenos Aires como fallback
     */
    fun getFallbackPosition(): com.farmdigitalelite.domain.model.LatLng {
        return com.farmdigitalelite.domain.model.LatLng(-34.6037, -58.3758) // Buenos Aires
    }
}


