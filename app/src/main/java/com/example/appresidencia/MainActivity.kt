package com.example.appresidencia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import android.content.Intent





/* ---------- Activity ---------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // Estado global de tema (claro / oscuro)
            var isDarkTheme by remember { mutableStateOf(true) }

            ARPcRepairTheme(isDarkTheme = isDarkTheme) {
                ARPcRepairApp(
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}

/* ---------- Tema ---------- */

@Composable
fun ARPcRepairTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val blueDark = Color(0xFF020617)

    val darkColors = darkColorScheme(
        primary = Color(0xFF2D7BFF),
        secondary = Color(0xFF5AD2FF),
        background = blueDark,
        surface = Color(0xFF020617),
        onPrimary = Color.White,
        onSurface = Color(0xFFE5E7EB)
    )

    val lightColors = lightColorScheme(
        primary = Color(0xFF2D7BFF),
        secondary = Color(0xFF2563EB),
        background = Color(0xFFF3F4F6),
        surface = Color.White,
        onPrimary = Color.White,
        onSurface = Color(0xFF111827)
    )

    val colorScheme = if (isDarkTheme) darkColors else lightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

/* ---------- Rutas ---------- */

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Scan : Screen("scan")
    data object Viewer3D : Screen("viewer3d")
    data object Instructions : Screen("instructions")
    data object Library : Screen("library")
    data object Settings : Screen("settings")
}

/* ---------- App ---------- */

@Composable
fun ARPcRepairApp(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel compartido para análisis
    val analysisViewModel: AnalysisViewModel = viewModel()
    val uiState by analysisViewModel.uiState.collectAsState()

    // Lista de actividad reciente (stack de componentes analizados)
    val recentComponents = remember { mutableStateListOf<String>() }

    // Lista dinámica de biblioteca
    val libraryItems = remember {
        mutableStateListOf(
            "Motherboard ASUS",
            "RAM DDR4"
        )
    }

    // Cada vez que haya un análisis exitoso, lo agregamos al historial
    LaunchedEffect(uiState) {
        if (uiState is AnalysisUiState.Success) {
            val name = (uiState as AnalysisUiState.Success).data.componentName
            recentComponents.add(0, name)      // lo metemos al inicio
            if (recentComponents.size > 20) { // límite opcional
                recentComponents.removeLast()
            }
        }
    }

    // Item seleccionado desde biblioteca (si aplica)
    var selectedLibraryItem by remember { mutableStateOf<String?>(null) }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Scan.route,
        Screen.Library.route,
        Screen.Settings.route
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF071A3C),
                        Color(0xFF0B2F73)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    BottomBar(navController, currentRoute)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onStart = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        onScanClick = {
                            selectedLibraryItem = null
                            navController.navigate(Screen.Scan.route)
                        },
                        onLibraryClick = {
                            navController.navigate(Screen.Library.route)
                        },
                        recentComponents = recentComponents,
                        onRecentClick = { name ->
                            selectedLibraryItem = name
                            navController.navigate(Screen.Viewer3D.route)
                        }
                    )
                }
                composable(Screen.Scan.route) {
                    ScanScreen(
                        onBack = { navController.popBackStack() },
                        onView3D = {
                            navController.navigate(Screen.Viewer3D.route)
                        },
                        analysisViewModel = analysisViewModel
                    )
                }
                composable(Screen.Viewer3D.route) {

                    val analyzedName = (uiState as? AnalysisUiState.Success)
                        ?.data
                        ?.componentName

                    val componentName = analyzedName
                        ?: selectedLibraryItem
                        ?: "Componente no identificado"

                    Viewer3DScreen(
                        componentName = componentName,
                        onBack = { navController.popBackStack() },
                        onShowInstructions = {
                            navController.navigate(Screen.Instructions.route)
                        },
                        onSaveToLibrary = { name ->
                            if (!libraryItems.contains(name)) {
                                libraryItems.add(0, name)
                            }
                        }
                    )
                }
                composable(Screen.Instructions.route) {
                    InstructionsScreen(
                        onBack = { navController.popBackStack() },
                        viewModel = analysisViewModel
                    )
                }
                composable(Screen.Library.route) {
                    LibraryScreen(
                        items = libraryItems,
                        onItemSelected = { name ->
                            selectedLibraryItem = name
                            analysisViewModel.reset()
                            navController.navigate(Screen.Viewer3D.route)
                        },
                        onItemDelete = { name ->
                            libraryItems.remove(name)
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onDarkThemeChange = onDarkThemeChange
                    )
                }
            }
        }
    }
}

/* ---------- Bottom Navigation ---------- */

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Inicio") { Icon(Icons.Filled.Home, null) },
        BottomNavItem(Screen.Scan.route, "Escanear") { Icon(Icons.Filled.CameraAlt, null) },
        BottomNavItem(Screen.Library.route, "Biblioteca") { Icon(Icons.Filled.Folder, null) },
        BottomNavItem(Screen.Settings.route, "Ajustes") { Icon(Icons.Filled.Settings, null) },
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (selected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        item.icon()
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp
                    )
                }
            )
        }
    }
}

/* ---------- Splash ---------- */

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoSize by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "logo-size"
    )

    LaunchedEffect(Unit) {
        delay(1600)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size((120 * logoSize).dp)
                    .background(
                        color = Color(0xFF2D7BFF),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Memory,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "AR PC Repair",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Asistente Inteligente",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/* ---------- Onboarding ---------- */

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFE5EEFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = Color(0xFF2D7BFF),
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Escanea y Repara",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Usa realidad aumentada e IA para identificar componentes y recibir guías paso a paso.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Dot(active = true)
                Spacer(Modifier.width(6.dp))
                Dot(active = false)
                Spacer(Modifier.width(6.dp))
                Dot(active = false)
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Comenzar", fontSize = 16.sp)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun Dot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 10.dp else 8.dp)
            .clip(CircleShape)
            .background(
                if (active) Color(0xFF2D7BFF) else Color(0xFFD1D5DB)
            )
    )
}

/* ---------- Inicio ---------- */

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onLibraryClick: () -> Unit,
    recentComponents: List<String>,
    onRecentClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "¡Bienvenido!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "¿Qué deseas hacer hoy?",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(24.dp))

            // Escanear PC
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D7BFF)
                ),
                onClick = onScanClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Escanear PC",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Inicia el escaneo AR",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF2D7BFF)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Mi Biblioteca
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3F4FF)
                ),
                onClick = onLibraryClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = Color(0xFF2D7BFF)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mi Biblioteca",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Objetos guardados",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Actividad Reciente",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            if (recentComponents.isNotEmpty()) {
                LazyColumn {
                    items(recentComponents) { name ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE9FCEB)
                            ),
                            onClick = { onRecentClick(name) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Completado recientemente",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Sin actividad reciente",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/* ---------- Escaneo AR ---------- */

@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onView3D: () -> Unit,
    analysisViewModel: AnalysisViewModel
) {
    val context = LocalContext.current

    // Launcher que abre la cámara y devuelve un Bitmap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Análisis en segundo plano
            analysisViewModel.runDemoAnalysisFromBitmap(bitmap)
            // Ir al visor 3D
            onView3D()
        }
    }

    // Launcher para pedir permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Si el usuario aceptó, abrimos la cámara
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(
                context,
                "Se necesita permiso de cámara para tomar fotos.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Launcher para elegir imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            analysisViewModel.runDemoAnalysisFromUri(uri)
            onView3D()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF020617))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Enfoca el componente",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* filtros si quieres */ }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filtros",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020617))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF2563EB),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vista de Cámara AR",
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Galería
                CircleButton(icon = Icons.Filled.Image, label = "Galería") {
                    galleryLauncher.launch("image/*")
                }

                // Capturar (cámara)
                CircleButton(
                    icon = Icons.Filled.CameraAlt,
                    label = "Capturar",
                    highlight = true
                ) {
                    // Comprobamos permiso de cámara
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        // Pedir permiso
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                // Girar (futuro)
                CircleButton(icon = Icons.Filled.Cached, label = "Girar") { }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun CircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(68.dp)
                .background(
                    if (highlight) Color(0xFF2563EB) else Color(0xFF111827),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}



/* ---------- Visor 3D ---------- */

@Composable
fun Viewer3DScreen(
    componentName: String,
    onBack: () -> Unit,
    onShowInstructions: () -> Unit,
    onSaveToLibrary: (String) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF020617))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Barra superior ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = componentName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onShowInstructions) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Ver instrucciones",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Área del "modelo 3D" (placeholder) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0B1120)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ViewInAr,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Modelo 3D",
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Botones inferiores (rotar / zoom / info) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF020617)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomAction("Rotar", Icons.Filled.Cached)
                BottomAction("Zoom", Icons.Filled.ZoomIn)
                BottomAction("Info", Icons.Filled.Info)
            }

            Spacer(Modifier.height(12.dp))

// --- Botón para abrir Unity (AR) ---
            Button(
                onClick = {
                    // Intent explícito por nombre de clase
                    val intent = Intent().apply {
                        setClassName(
                            context,
                            "com.unity3d.player.UnityPlayerActivity" // Activity de Unity
                        )
                        // (Opcional) enviar el nombre del componente a Unity
                        putExtra("component_name", componentName)
                    }

                    runCatching {
                        context.startActivity(intent)
                    }.onFailure {
                        it.printStackTrace()
                        Toast.makeText(
                            context,
                            "No se pudo abrir la vista AR (Unity).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ViewInAr,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Ver en AR (Unity)")
            }


            Spacer(Modifier.height(8.dp))

            // --- Botón para guardar en biblioteca ---
            Button(
                onClick = {
                    onSaveToLibrary(componentName)
                    Toast.makeText(
                        context,
                        "Guardado en la biblioteca",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BookmarkAdd,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Guardar en Biblioteca")
            }
        }
    }
}



@Composable
fun BottomAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TextButton(onClick = { }) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White
        )
    }
}

/* ---------- Instrucciones ---------- */

@Composable
fun InstructionsScreen(onBack: () -> Unit, viewModel: AnalysisViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when (uiState) {
            AnalysisUiState.Idle -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No hay análisis aún.", color = Color.Gray)
                }
            }

            AnalysisUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Analizando componente…")
                }
            }

            is AnalysisUiState.Error -> {
                val message = (uiState as AnalysisUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Error al analizar",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(message)
                }
            }

            is AnalysisUiState.Success -> {
                val data = (uiState as AnalysisUiState.Success).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás"
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Instrucciones",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = data.componentName,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    LazyColumn {
                        items(data.steps) { step ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF9FAFB)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFE0ECFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = step.stepNumber.toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = step.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = step.description,
                                            fontSize = 13.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Biblioteca ---------- */

@Composable
fun LibraryScreen(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onItemDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Mi Biblioteca",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Objetos escaneados y guardados",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(20.dp))

            if (items.isEmpty()) {
                Text(
                    text = "No hay componentes guardados.",
                    color = Color(0xFF6B7280)
                )
            } else {
                LazyColumn {
                    items(items) { name ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3F4FF)
                            ),
                            onClick = { onItemSelected(name) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Memory,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Guardado en biblioteca",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                IconButton(onClick = { itemToDelete = name }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación de borrado
        itemToDelete?.let { name ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Eliminar componente") },
                text = { Text("¿Estás seguro de eliminar \"$name\" de la biblioteca?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onItemDelete(name)
                            Toast.makeText(
                                context,
                                "Componente eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            itemToDelete = null
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

/* ---------- Configuración ---------- */

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Configuración",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Personaliza tu experiencia",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(24.dp))

            SettingToggle(
                title = "Notificaciones",
                subtitle = "Recibir avisos de progreso y consejos",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
                leadingIcon = Icons.Filled.Notifications
            )
            Spacer(Modifier.height(12.dp))
            SettingToggle(
                title = "Modo Oscuro",
                subtitle = "Aplicar tema oscuro en la app",
                checked = isDarkTheme,
                onCheckedChange = { onDarkThemeChange(it) },
                leadingIcon = Icons.Filled.DarkMode
            )
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FAFB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0ECFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color(0xFF2563EB)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
