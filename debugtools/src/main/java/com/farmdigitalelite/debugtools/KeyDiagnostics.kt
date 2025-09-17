package com.farmdigitalelite.debugtools

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmdigitalelite.farm.BuildConfig
import com.farmdigitalelite.maps.utils.MapsApiKeyUtils

/**
 * Componente de diagnóstico para verificar el estado de las API keys.
 * Solo visible en modo DEBUG.
 * 
 * @author Farm Digital Elite Team
 * @since 1.0.0
 */
@Composable
fun KeyDiagnostics(
    context: Context,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.DEBUG) return
    
    val (hasMapsKey, mapsKeyLength) = remember {
        context.readMapsApiKeyMasked()
    }
    
    val geminiKeyPresent = remember {
        BuildConfig.GEMINI_API_KEY.isNotBlank()
    }
    
    val openWeatherKeyPresent = remember {
        BuildConfig.OPENWEATHER_API_KEY.isNotBlank()
    }
    
    // Log del estado en Logcat
    LaunchedEffect(hasMapsKey, mapsKeyLength, geminiKeyPresent, openWeatherKeyPresent) {
        Log.d("KeyDiagnostics", "Maps key present: $hasMapsKey len=$mapsKeyLength")
        Log.d("KeyDiagnostics", "Gemini key present: $geminiKeyPresent")
        Log.d("KeyDiagnostics", "OpenWeather key present: $openWeatherKeyPresent")
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Diagnóstico de API Keys",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Estado de Maps API Key
            KeyStatusRow(
                label = "Google Maps",
                isPresent = hasMapsKey,
                additionalInfo = if (hasMapsKey) "Longitud: $mapsKeyLength" else "No configurada"
            )
            
            // Estado de Gemini API Key
            KeyStatusRow(
                label = "Google Gemini",
                isPresent = geminiKeyPresent,
                additionalInfo = if (geminiKeyPresent) "Configurada" else "No configurada"
            )
            
            // Estado de OpenWeather API Key
            KeyStatusRow(
                label = "OpenWeather",
                isPresent = openWeatherKeyPresent,
                additionalInfo = if (openWeatherKeyPresent) "Configurada" else "No configurada"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Solo visible en modo DEBUG",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeyStatusRow(
    label: String,
    isPresent: Boolean,
    additionalInfo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de estado
        Icon(
            imageVector = if (isPresent) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isPresent) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = additionalInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


