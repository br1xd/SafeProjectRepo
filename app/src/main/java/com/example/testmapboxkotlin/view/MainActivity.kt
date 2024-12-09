package com.example.testmapboxkotlin.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.testmapboxkotlin.LocationManager
import com.example.testmapboxkotlin.R
import com.example.testmapboxkotlin.model.Comunas
import com.example.testmapboxkotlin.model.Reportes
import com.example.testmapboxkotlin.viewModel.ComunaViewModel
import com.example.testmapboxkotlin.viewModel.ReporteViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.match
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import kotlin.collections.set

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extrae los datos o la notificación
        val title = remoteMessage.notification?.title ?: "Mensaje"
        val body = remoteMessage.notification?.body ?: "Tienes un nuevo mensaje"

        // Envía un broadcast con los datos del mensaje
        val intent = Intent("com.example.NEW_MESSAGE")
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        sendBroadcast(intent)
    }
}
class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var mapView: MapView
    private val reportVwModel : ReporteViewModel by viewModels()
    lateinit var permissionsManager: PermissionsManager
    private lateinit var currentUserEmail: String
    private lateinit var currentUserUid :String
    private lateinit var currentUserName : String
    private var currentUserImage : Uri? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioUri: Uri? = null
    private var rol_usuario: String? = null
    private val comunaVwm : ComunaViewModel by viewModels()
    val messageReceiver : BroadcastReceiver
        get() {
            TODO()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {

            currentUserEmail = currentUser.email ?: ""  // Guardamos el correo del usuario logueado
            currentUserUid = currentUser.getUid();
            currentUserName = currentUser.displayName ?: "";
            currentUserImage = currentUser.photoUrl;

        } else {
            Toast.makeText(this, "Por favor, inicia sesión", Toast.LENGTH_SHORT).show()
        }
        val messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val title = intent?.getStringExtra("title") ?: "Mensaje"
                val body = intent?.getStringExtra("body") ?: "Tienes un nuevo mensaje"

                // Muestra la ventana con el mensaje
                showBanner(title, body)
            }
        }

        // Registra el receptor
        registerReceiver(messageReceiver, IntentFilter("com.example.NEW_MESSAGE"),
            RECEIVER_NOT_EXPORTED
        )

        reportVwModel.GetUserRol(currentUserUid)
        reportVwModel.getRolUsuario().observe(this){rol->
            val db = FirebaseFirestore.getInstance()
            rol_usuario = rol
            limiteCam(rol)
            if (rol.equals("Basico")||rol.equals("Premium")){

                if (rol.equals("Premium")){
                    showPremiumFunctions()
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM", "Fallo al obtener el token", task.exception)
                        return@addOnCompleteListener
                    }
                    val token = task.result
                    if (currentUserUid != null) {
                        val userTokenData = hashMapOf("token" to token)
                        db.collection("Tokens").document(currentUserUid).set(userTokenData)
                            .addOnSuccessListener {
                                Log.d("FCM", "Token guardado exitosamente")
                            }
                            .addOnFailureListener { e ->
                                Log.w("FCM", "Error guardando token", e)
                            }
                    }
                }

            }
            else if (rol.equals("usuario")){
                if (currentUserUid != null) {
                    // Verifica si el documento existe antes de intentar borrarlo
                    db.collection("Tokens").document(currentUserUid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // El documento existe, procede a borrarlo
                                db.collection("Tokens").document(currentUserUid).delete()
                                    .addOnSuccessListener {
                                        Log.d("FCM", "Documento borrado exitosamente")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("FCM", "Error al borrar el documento", e)
                                    }
                            } else {
                                Log.d("FCM", "El documento no existe, no se puede borrar")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Error al verificar si el documento existe", e)
                        }
                }
            }

        }

        mapView = findViewById(R.id.mapView)
        addReporteMarker()
        reportVwModel.getAllReport()
        //Uso de el manager de locacion
        locationManager = LocationManager(this)
        // Obtener view del mapa





// Define camera bounds

        //Marcadores

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location
        } else {
            permissionsManager = PermissionsManager(this.permissionsListener)
            permissionsManager.requestLocationPermissions(this)
        }
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Creation_btn -> {
                    locationManager.getCurrentLocation { latitude, longitude ->

                        // Mueve el Intent DENTRO del callback
                        val intent = Intent(this, AddReportActivity::class.java).apply {
                            putExtra("long", longitude) //Le pasamos a la siguiente actividad la longitud
                            putExtra("lat", latitude)   // y latitud
                            putExtra("userEmail", currentUserEmail)
                            putExtra("userUid",currentUserUid)
                        }
                        startActivity(intent)
                    }
                    true // Retorna true aquí para indicar que el ítem fue seleccionado
                }
                R.id.HistorialReportes ->{
                    val intent = Intent(this, FavActivity::class.java).apply {
                        // Pasamos el correo del usuario como extra
                        putExtra("userEmail", currentUserEmail)
                    }
                     startActivity(intent)

                    true // Retorna true aquí para indicar que el ítem fue seleccionado
                }
                R.id.Profile_btn->{
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        // Pasamos el correo del usuario como extra
                        putExtra("USER_NAME", currentUserName)
                        putExtra("USER_EMAIL", currentUserEmail)
                        putExtra("USER_UID", currentUserUid)
                        putExtra("USER_IMAGE_URI", currentUserImage.toString())

                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    true
                }


                else -> false
            }
        }

        val btnChangeMap = findViewById<ImageButton>(R.id.btn_changeMap)
        var OriginalMap = true
        btnChangeMap.setOnClickListener({
            Log.d("O1",""+OriginalMap)
            if (OriginalMap == true){
                comunaVwm.getAllReport()
                cargarComunas(OriginalMap)
                OriginalMap = false
                Log.d("O2",""+OriginalMap)
            }
            else {
                Log.d("O3",""+OriginalMap)
                cargarComunas(originalMap = false)
                OriginalMap = true
            }

        })


        // Create a map programmatically and set the initial camera

        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(-98.0, 39.5))
                .pitch(0.0)
                .zoom(2.0)
                .bearing(0.0)
                .build()
        )

        // Add the map view to the activity (you can also add it to other views as a child)

        with(mapView) {
            location.locationPuck = createDefault2DPuck(withBearing = true)
            location.enabled = true
            location.puckBearing = PuckBearing.COURSE
            location.puckBearingEnabled = true
            viewport.transitionTo(
                targetState = viewport.makeFollowPuckViewportState(),
                transition = viewport.makeImmediateViewportTransition()
            )
        }






    }

    private fun cargarComunas(originalMap: Boolean) {
        val inputStream = resources.openRawResource(R.raw.comunas_santiago)
        val geoJsonString = inputStream.bufferedReader().use { it.readText() } // Leer contenido del archivo
        inputStream.close() //Busac el archivo y lo carga


        val geoJsonSource = GeoJsonSource.Builder("comunas-source")
            .data(geoJsonString)
            .build()

        if (originalMap==true){
            mapView.mapboxMap.getStyle { style ->
                style.addSource(geoJsonSource)

                comunaVwm.getListaComunas().observe(this){lista ->
                    if (lista != null){
                        style.removeStyleLayer("comunas-layer")
                        style.removeStyleLayer("comunas-limits-layer")
                        val MappingColores = mutableListOf<Expression>()
                        for (c : Comunas in lista){
                            var colorTasa = "#CCCCCC"
                            if (c.tasaCrimen in 1..5257) {
                                colorTasa = "#FFA07A"
                            }
                            else if (c.tasaCrimen in 5258..7887){
                                colorTasa = "#FA8072"
                            }

                            else if (c.tasaCrimen in 7888..10515){
                                colorTasa = "#F08080"
                            }
                            else if (c.tasaCrimen in 10516..13145){
                                colorTasa = "#800000"
                            }
                            Log.d("ComunaMap",c.idComuna)
                            Log.d("ComunaMap",colorTasa)
                            MappingColores.add(literal(c.idComuna))
                            MappingColores.add(literal(colorTasa))
                        }

                        if (style.getLayer("comunas-layer") == null){
                            style.addLayer(
                                fillLayer("comunas-layer", "comunas-source") {
                                    fillColor(
                                        match(
                                            com.mapbox.maps.extension.style.expressions.dsl.generated.get("name"), // El atributo del GeoJSON con el que comparas
                                            // Color predeterminado si no hay coincidencias
                                            *MappingColores.toTypedArray(),
                                            literal("#CCCCCC")

                                        )
                                    )
                                    fillOpacity(0.7) // Opcional: ajusta la transparencia
                                }
                            )
                            style.addLayer(
                                lineLayer("comunas-limits-layer", "comunas-source") {
                                    lineColor(literal("#000000")) // Color de las líneas (negro en este caso)
                                    lineWidth(2.1) // Ancho de las líneas
                                    lineOpacity(1.0) // Opacidad de las líneas (puedes ajustarlo)
                                }
                            )
                        }

                    }

                }


            }
        }
        else{
            mapView.mapboxMap.getStyle { style ->
                style.removeStyleLayer("comunas-layer")
                style.removeStyleLayer("comunas-limits-layer")
                style.removeStyleSource("comunas-source")
            }

            }
        }
    private fun limiteCam(rol:String){
        var kmRange = 10000000
        if (rol.equals("usuario")){
            kmRange = 2
        }
        else if (rol.equals("Basico")){
            kmRange = 10
        }
        else if (rol.equals("Premium")){
            kmRange = 100
        }

        locationManager.getCurrentLocation { lat, lon ->
            val latDiff = kmRange / 111.0 // 1° en latitud es ~111 km
            val lonDiff = kmRange / (111.0 * Math.cos(Math.toRadians(lat)))

            val northeast = Point.fromLngLat(lon + lonDiff, lat + latDiff)
            val southwest = Point.fromLngLat(lon - lonDiff, lat - latDiff)

            val bounds = CoordinateBounds(southwest, northeast)

            val cameraBoundOptions = CameraBoundsOptions.Builder()
                .bounds(bounds)
                .build()
            mapView.mapboxMap.setBounds(cameraBoundOptions)

        }
    }


    private fun addReporteMarker(){
        Log.d("mark","ejecutando")
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val markers = mutableMapOf<String, Reportes>()

        reportVwModel.getListaReportes().observe(this){list ->
            if (list != null) {


                pointAnnotationManager.deleteAll()

                for (r:Reportes in list) {
                    val lat : Double = r.lat.toDouble()
                    val lgt : Double = r.log.toDouble()

                    val tipoFormateado= r.tipo.replaceFirstChar { it.lowercase() }
                    val resourceName = "marker_${tipoFormateado}" // Construir el nombre del recurso
                    val resourceId = applicationContext.resources.getIdentifier(resourceName, "drawable", applicationContext.packageName)
                    val icon = if (resourceId != 0) {
                        BitmapFactory.decodeResource(applicationContext.resources, resourceId)
                    } else {
                        // Si no se encuentra el recurso, puedes usar un recurso predeterminado o manejar el error
                        BitmapFactory.decodeResource(applicationContext.resources, R.drawable.marker_custom) // Recurso predeterminado
                    }

                    val resizedIcon = Bitmap.createScaledBitmap(icon, 60, 96, false)
                    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        // Define a geographic coordinate.
                        .withPoint(Point.fromLngLat( lgt,lat))
                        // Specify the bitmap you assigned to the point annotation
                        // The bitmap will be added to map style automatically.
                        .withIconImage(resizedIcon)
                        .withData(JsonObject().apply {
                            addProperty("id", r.id) // Asociamos un identificador único

                        })


                    // Add the resulting pointAnnotation to the map.
                    //pointAnnotationManager.create(pointAnnotationOptions)
                    val annotation = pointAnnotationManager.create(pointAnnotationOptions)
                    markers[annotation.id] = r

                    pointAnnotationManager.addClickListener { annotation ->
                        val report = markers[annotation.id]
                        if (report != null) {
                            Log.d("TAG REPORTE", report.autor)
                            showInfoWindow(report)
                        }
                        Log.d("TAG REPORTE", "NULL")
                        true // Retorna `true` para indicar que el evento fue manejado
                    }

                }
            }

        }

    }
    private fun showPremiumFunctions(){  //Revisa el rol del usuario, y en el caso de tener premium, muestra funciones especiales.

                    val btnChangeMap = findViewById<ImageButton>(R.id.btn_changeMap)

                    // Administrador: siempre puede editar y borrar
                    if (rol_usuario.equals("Premium")) {
                        btnChangeMap.visibility = View.VISIBLE

                    }
                    else{
                        Log.d("usuraioNoPremium","no premium")

                    }




    }
    override fun onDestroy() { //Cierra el mediaplayer al termianr la actividad
        super.onDestroy()
        unregisterReceiver(messageReceiver)
        mediaPlayer?.release() // Libera los recursos
        mediaPlayer = null
    }
    private fun showInfoWindow(report: Reportes) {
        // Inflar el diseño del diálogo personalizado
        val view = layoutInflater.inflate(R.layout.reportvw_layout, null)
        val firestore = FirebaseFirestore.getInstance()
        val ReportCollection = firestore.collection("report-collection")

        // Crear y mostrar el diálogo
        val currentDialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create() // Usar .create() en lugar de .show()
        currentDialog.show()
        // Referencias a los elementos del diseño
        val imageView = view.findViewById<ImageView>(R.id.dialog_image)
        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        val authorView = view.findViewById<TextView>(R.id.dialog_author)
        val btnPlayAudio = view.findViewById<Button>(R.id.btnPlayAudio)
        val btnRpt = view.findViewById<ImageButton>(R.id.btn_rpt)
        val btnAddFav =
            view.findViewById<ImageButton>(R.id.btn_add_fav) // Nuevo botón para agregar a favoritos
        val descView = view.findViewById<TextView>(R.id.tv_desc)

        if (currentUserUid != null) {
            reportVwModel.getRolUsuario().observe(this){rol->
                val btnDeleteReport = view.findViewById<ImageButton>(R.id.btn_delete)
                val btnEditReport = view.findViewById<ImageButton>(R.id.btn_edit)
                // Administrador: siempre puede editar y borrar
                if (rol == "administrador") {
                    btnDeleteReport.visibility = View.VISIBLE
                    btnEditReport.visibility = View.VISIBLE

                    btnEditReport.setOnClickListener {
                        val intent = Intent(this, EditReportActivity::class.java).apply {
                            putExtra("reporteId", report.id)
                        }
                        startActivity(intent)
                    }

                    btnDeleteReport.setOnClickListener {
                        reportVwModel.deleteReport(report.id)
                        currentDialog.dismiss()
                    }
                }
                // Usuario: solo puede editar y borrar sus propios reportes

                else if (currentUserEmail == report.autor) {
                    btnDeleteReport.visibility = View.VISIBLE
                    btnEditReport.visibility = View.VISIBLE

                    btnEditReport.setOnClickListener {
                        val intent = Intent(this, EditReportActivity::class.java).apply {
                            putExtra("reporteId", report.id)

                        }
                        startActivity(intent)
                    }

                    btnDeleteReport.setOnClickListener {
                        reportVwModel.deleteReport(report.id)
                        Toast.makeText(this,"Reporte eliminado.",Toast.LENGTH_SHORT)
                        currentDialog.dismiss()
                    }
                    if (rol=="Premium"&&report.audioUrl!=null){
                        btnPlayAudio.visibility = View.VISIBLE
                        Log.d("audioUrl",report.audioUrl)
                        audioUri = Uri.parse(report.audioUrl)
                        if (audioUri!=null){
                            btnPlayAudio.setOnClickListener {
                                if (mediaPlayer == null) {
                                    try {
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(this@MainActivity, audioUri!!)
                                            prepare() // Prepara el audio
                                            start()   // Inicia la reproducción
                                        }
                                        Toast.makeText(this, "Reproduciendo audio...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(this, "Error al reproducir audio: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    btnPlayAudio.text = "Detener Audio"
                                } else {
                                    mediaPlayer?.release() // Libera los recursos
                                    mediaPlayer = null
                                    Toast.makeText(this, "Audio detenido", Toast.LENGTH_SHORT).show()
                                    btnPlayAudio.text = "Reproducir Audio"

                                }


                            }
                        }
                        else{Log.d("error","audio no encontrado")}
                    }
                }
                // Si el usuario no es administrador ni autor del reporte, no hace nada
                else {
                    Log.d("Debug", "El usuario no tiene permisos para editar o borrar este reporte.")
                }
                }
            }





            // Glide es una libreria que carga imagenes de url
            Log.d("TAG IMAGEN", report.image_url)
            Glide.with(this)
                .load(report.image_url) // URL de la imagen
                .placeholder(R.drawable.ic_launcher_background) // Imagen por defecto
                .error(R.drawable.ic_launcher_background) // Imagen de error
                .into(imageView)

            // Configurar texto
            titleView.text = "Tipo: ${report.tipo}"
            authorView.text = "Autor: ${report.autor}"
            descView.text = "Descripción: ${report.desc}"

            btnRpt.setOnClickListener({
                Toast.makeText(this, "Ha sido reportado", Toast.LENGTH_SHORT).show()
                ReportCollection.document(report.id).update(
                    "denunciado",
                    true
                ) //Solucion temporal, se debe agregar a otra coleccion y borrar de esta.
            })
            btnAddFav.setOnClickListener {
                if (currentUserEmail.isNotEmpty()) {
                    val favoritesCollection = firestore.collection("users")
                        .document(currentUserEmail)  // Usamos el correo del usuario como identificador
                        .collection("favorites")

                    favoritesCollection.document(report.id).set(report).addOnSuccessListener {
                        Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al agregar a favoritos", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Por favor, inicia sesión para agregar a favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }



        }

        var permissionsListener: PermissionsListener = object : PermissionsListener {
            override fun onExplanationNeeded(permissionsToExplain: List<String>) {

            }

            override fun onPermissionResult(granted: Boolean) {
                if (granted) {
                    println("a")
                    // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location

                } else {
                    println("a")
                    // User denied the permission

                }
            }
        }
    private fun showBanner(title: String, body: String) {
        val banner = findViewById<View>(R.id.banner)
        val bannerTitle = findViewById<TextView>(R.id.banner_title)
        val bannerBody = findViewById<TextView>(R.id.banner_body)
        bannerTitle.text = title
        bannerBody.text = body

        // Mostrar el banner
        banner.visibility = View.VISIBLE
        banner.animate().translationY(0f).setDuration(300).start()

        // Ocultar el banner después de 3 segundos
        banner.postDelayed({
            hideBanner(banner)
        }, 30000)
    }

    private fun hideBanner(banner: View) {
        banner.animate().translationY(-banner.height.toFloat()).setDuration(300).withEndAction {
            banner.visibility = View.GONE
        }.start()
    }

}