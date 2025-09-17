package com.farmdigitalelite.maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmdigitalelite.domain.model.LatLng
import com.farmdigitalelite.maps.model.*
import com.farmdigitalelite.maps.utils.ClusteringUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.farmdigitalelite.maps.utils.MapsApiKeyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaGanadoScreen(
    animals: List<AnimalMapMarker>,
    geofences: List<GeofenceConfig>,
    onMarkerClick: (AnimalMapMarker) -> Unit,
    modifier: Modifier = Modifier,
    initialPosition: LatLng = LatLng(-34.6037, -58.3758), // Buenos Aires por defecto
    onGeofenceClick: (GeofenceConfig) -> Unit = {},
    showClustering: Boolean = true,
    isLowDataMode: Boolean = false
) {
    val context = LocalContext.current
    
    // Verificar API key de Maps
    val (hasMapsApiKey, keyLength) = remember {
        context.readMapsApiKeyMasked()
    }
    
    // Verificar permisos de ubicación
    val hasLocationPermissions = remember {
        context.hasLocationPermissions()
    }
    
    val cameraPositionState = remember {
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                com.google.android.gms.maps.model.LatLng(
                    initialPosition.latitude,
                    initialPosition.longitude
                ), 10f
            )
        )
    }
    
    var showAnimalList by remember { mutableStateOf(false) }
    var selectedCluster by remember { mutableStateOf<ClusterGroup?>(null) }
    var currentZoom by remember { mutableStateOf(10f) }
    
    // Calcular clusters basado en el zoom actual
    val clusters = remember(animals, currentZoom, showClustering) {
        if (showClustering) {
            ClusteringUtils.hierarchicalCluster(animals, currentZoom)
        } else {
            animals.map { ClusterGroup(it.position, listOf(it)) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Mostrar mapa solo si hay API key válida
        if (hasMapsApiKey) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = if (isLowDataMode) com.google.maps.android.compose.MapType.NORMAL else com.google.maps.android.compose.MapType.SATELLITE,
                    isMyLocationEnabled = hasLocationPermissions
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    mapToolbarEnabled = false
                ),
                onMapLoaded = {
                    // Mapa cargado
                }
            ) {
                // Dibujar geocercas
                geofences.forEach { geofence ->
                    val polygon = geofence.toPolygon()
                    if (polygon.isNotEmpty()) {
                        val gmsPoly = polygon.map { 
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) 
                        }
                        
                        Polygon(
                            points = gmsPoly,
                            strokeColor = parseColor(geofence.color),
                            strokeWidth = 2f,
                            fillColor = Color(ColorUtils.setAlphaComponent(parseColor(geofence.color).toArgb(), 50)),
                            onClick = { onGeofenceClick(geofence) }
                        )
                    }
                }
                
                // Dibujar clusters/markers
                clusters.forEach { cluster ->
                    if (cluster.count == 1) {
                        // Marker individual
                        val animal = cluster.animals.first()
                        Marker(
                            state = MarkerState(
                                position = com.google.android.gms.maps.model.LatLng(
                                    animal.position.latitude,
                                    animal.position.longitude
                                )
                            ),
                            title = animal.nombre ?: animal.tagId,
                            snippet = "ID: ${animal.tagId}",
                            onClick = { 
                                onMarkerClick(animal)
                                true
                            },
                            icon = BitmapDescriptorFactory.defaultMarker(
                                when (animal.alertLevel) {
                                    AlertLevel.CRITICAL -> BitmapDescriptorFactory.HUE_RED
                                    AlertLevel.WARNING -> BitmapDescriptorFactory.HUE_ORANGE
                                    AlertLevel.INFO -> BitmapDescriptorFactory.HUE_YELLOW
                                    AlertLevel.NONE -> BitmapDescriptorFactory.HUE_GREEN
                                }
                            )
                        )
                    } else {
                        // Cluster marker
                        Marker(
                            state = MarkerState(
                                position = com.google.android.gms.maps.model.LatLng(
                                    cluster.position.latitude,
                                    cluster.position.longitude
                                )
                            ),
                            title = "Grupo de ${cluster.count} animales",
                            onClick = { 
                                selectedCluster = cluster
                                showAnimalList = true
                                true
                            },
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }
                }
            }
        } else {
            // UI de error cuando no hay API key
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (BuildConfig.DEBUG) {
                        "MAPS_API_KEY no configurada"
                    } else {
                        "Mapa no disponible"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (BuildConfig.DEBUG) {
                        "Agrega MAPS_API_KEY en local.properties"
                    } else {
                        "El servicio de mapas no está disponible en este momento"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Debug: Key length = $keyLength",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // FAB para mostrar lista de animales (solo si hay mapa)
        if (hasMapsApiKey) {
            FloatingActionButton(
                onClick = { showAnimalList = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista de animales")
            }
        }
        
        // Indicador de modo de datos bajos
        if (isLowDataMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.warning)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Modo datos limitados",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // Información de estadísticas (solo si hay mapa)
        if (hasMapsApiKey) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Animales: ${animals.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Geocercas: ${geofences.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (showClustering && clusters.size != animals.size) {
                        Text(
                            text = "Clusters: ${clusters.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet para lista de animales
    if (showAnimalList) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAnimalList = false
                selectedCluster = null
            }
        ) {
            val animalsToShow = selectedCluster?.animals ?: animals
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = if (selectedCluster != null) 
                            "Animales en el grupo (${animalsToShow.size})" 
                        else 
                            "Todos los animales (${animalsToShow.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                items(animalsToShow) { animal ->
                    AnimalListItem(
                        animal = animal,
                        onClick = {
                            onMarkerClick(animal)
                            showAnimalList = false
                            selectedCluster = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimalListItem(
    animal: AnimalMapMarker,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (animal.alertLevel) {
                            AlertLevel.CRITICAL -> Color.Red
                            AlertLevel.WARNING -> Color(0xFFFF9800)
                            AlertLevel.INFO -> Color.Yellow
                            AlertLevel.NONE -> Color.Green
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.nombre ?: "Animal ${animal.tagId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ID: ${animal.tagId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!animal.isOnline) {
                    Text(
                        text = "Sin conexión",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}

// Extension para warning color
private val ColorScheme.warning: Color
    get() = Color(0xFFFF9800)


