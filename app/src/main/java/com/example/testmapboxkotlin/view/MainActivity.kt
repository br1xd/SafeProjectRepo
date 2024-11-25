package com.example.testmapboxkotlin.view
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.example.testmapboxkotlin.model.Reportes
import com.example.testmapboxkotlin.viewModel.ReporteViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport

class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var mapView: MapView
    private val reportVwModel : ReporteViewModel by viewModels()
    lateinit var permissionsManager: PermissionsManager
    private lateinit var currentUserEmail: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
            currentUserEmail = currentUser.email ?: ""  // Guardamos el correo del usuario logueado
        } else {
            Toast.makeText(this, "Por favor, inicia sesión", Toast.LENGTH_SHORT).show()
        }




        mapView = findViewById(R.id.mapView)
        addReporteMarker()
        reportVwModel.getAllReport()
        //Uso de el manager de locacion
        locationManager = LocationManager(this)
        // Obtener view del mapa

        //val reporteViewModel = ViewModelProvider(this).get(ReporteViewModel::class.java)



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
                        }
                        startActivity(intent)
                    }
                    true // Retorna true aquí para indicar que el ítem fue seleccionado
                }
                else -> false
            }

        }

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


    private fun addReporteMarker(){
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
    private fun showInfoWindow(report: Reportes) {
        // Inflar el diseño del diálogo personalizado
        val view = layoutInflater.inflate(R.layout.reportvw_layout, null)

        // Referencias a los elementos del diseño
        val imageView = view.findViewById<ImageView>(R.id.dialog_image)
        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        val authorView = view.findViewById<TextView>(R.id.dialog_author)
        val btnRpt = view.findViewById<ImageButton>(R.id.btn_rpt)
        val btnAddFav = view.findViewById<ImageButton>(R.id.btn_add_fav) // Nuevo botón para agregar a favoritos

        // Glide es una libreria que carga imagenes de url
        Log.d("TAG IMAGEN",report.image_url)
        Glide.with(this)
            .load(report.image_url) // URL de la imagen
            .placeholder(R.drawable.ic_launcher_background) // Imagen por defecto
            .error(R.drawable.ic_launcher_background) // Imagen de error
            .into(imageView)

        // Configurar texto
        titleView.text = "Tipo: ${report.tipo}"
        authorView.text = "Autor: ${report.autor}"
        val firestore = FirebaseFirestore.getInstance()
        val ReportCollection = firestore.collection("report-collection")
        btnRpt.setOnClickListener({
            Toast.makeText(this,"Ha sido reportado",Toast.LENGTH_SHORT).show()
            ReportCollection.document(report.id).update("denunciado",true) //Solucion temporal, se debe agregar a otra coleccion y borrar de esta.
        })
        btnAddFav.setOnClickListener {
            if (currentUserEmail.isNotEmpty()) {
                val favoritesCollection = firestore.collection("users")
                    .document(currentUserEmail)  // Usamos el correo del usuario como identificador
                    .collection("favorites")

                favoritesCollection.document(report.id).set(report).addOnSuccessListener {
                    Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al agregar a favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, inicia sesión para agregar a favoritos", Toast.LENGTH_SHORT).show()
            }
        }

        // Crear y mostrar el diálogo
        AlertDialog.Builder(this).apply {
            setView(view) // Establecer la vista personalizada
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            show()
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

}