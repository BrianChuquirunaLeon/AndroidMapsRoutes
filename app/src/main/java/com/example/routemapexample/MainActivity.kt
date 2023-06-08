package com.example.routemapexample

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.routemapexample.databinding.ActivityMainBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding:ActivityMainBinding
    private lateinit var map: GoogleMap
    private var start:String =""
    private var end:String =""
    private var poly:Polyline ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCalculateRoute.setOnClickListener{
            start = ""
            end = ""
            poly?.remove()
            poly = null
            Toast.makeText(this, "Selecciona punto de origen y final", Toast.LENGTH_SHORT).show()
            if(::map.isInitialized){
                map.setOnMapClickListener { latlong ->
                    if (start.isEmpty()){
                        start = "${latlong.longitude},${latlong.latitude}"
                    }else if (end.isEmpty()){
                        end = "${latlong.longitude},${latlong.latitude}"
                    }else{
                        createRoute()
                    }
                }
            }
        }

        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //llama a la funion onMapReady
        mapFragment.getMapAsync(this)
    }

    //Se carga automaticamente cuando el mapa ya ha cargado
    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }

    private fun createRoute(){
        Log.i("aris",start)
        Log.i("aris",end)
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java)
                .getRoute("aqui_va_la_llave_de_openroutesrvice_para_acceder_a_su_API_la_cual_calcula_rutas",start,end)
            if (call.isSuccessful){
                Log.i("aris","OK")
                drawRoute(call.body())
            }else{
                Log.i("aris","K.O.")
            }
        }
    }

    private fun drawRoute(routeResponse: RouteResponse?) {
        val polygonOptions = PolylineOptions()
        //usamos first() porque "features" nos devuelve una lista con un elemento, pero sigue siendo una lista,
        // y por lo tanto al ser una lista con 1 solo elemento nosotros queremos acceder al primer elemento.
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {latlong ->
            polygonOptions.add(LatLng(latlong[1],latlong[0]))
        }

        runOnUiThread{
            poly = map.addPolyline(polygonOptions)
        }

    }

    private fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}