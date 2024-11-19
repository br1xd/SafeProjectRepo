package com.example.testmapboxkotlin.view
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.testmapboxkotlin.LocationManager
import com.example.testmapboxkotlin.R
import com.example.testmapboxkotlin.model.Reportes
import com.example.testmapboxkotlin.viewModel.ReporteViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reportVwModel.getAllReport();
        //Uso de el manager de locacion
        locationManager = LocationManager(this)

        // Obtener view del mapa
        mapView = findViewById(R.id.mapView)

        //val reporteViewModel = ViewModelProvider(this).get(ReporteViewModel::class.java)
        reportVwModel.getListaReportes().observe(this){list ->
            if (list != null) {
            for (r:Reportes in list) {
                val lat : Double = r.lat.toDouble()
                val lgt : Double = r.log.toDouble()
                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
                // Set options for the resulting symbol layer.
                val icon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.red_marker);
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    // Define a geographic coordinate.
                    .withPoint(Point.fromLngLat( lgt,lat))  //Ideas para coordenadas de reportes, un clico for donde cada elemento
                    //(reportes) se le saque sus coordenadas, esto se debe hacer emn viewModel
                    //Tambien, se debe incluir la id del reporte;

                    // Specify the bitmap you assigned to the point annotation
                    // The bitmap will be added to map style automatically.
                    .withIconImage(icon)
                // Add the resulting pointAnnotation to the map.
                pointAnnotationManager?.create(pointAnnotationOptions)

                }
            }

        }



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