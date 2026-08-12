package com.example.cineflix

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.IOException
import java.util.Locale

class CinemasActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var textCoordenadas: TextView
    private lateinit var textEndereco: TextView
    private lateinit var textStatusCinemas: TextView

    private lateinit var editEndereco: EditText

    private lateinit var btnBuscarEndereco: Button
    private lateinit var btnBuscarCinemas: Button

    private lateinit var geocoder: Geocoder

    private var marcadorSelecionado: Marker? = null

    private val marcadoresCinemas =
        mutableListOf<Marker>()

    private var ultimoPontoSelecionado: LatLng? = null
    private var ultimaBuscaEndereco: String? = null

    /*
     * Última localização conhecida do usuário.
     * Será utilizada para pesquisar cinemas próximos.
     */
    private var ultimaLatitudeUsuario: Double? = null
    private var ultimaLongitudeUsuario: Double? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var monitorandoLocalizacao = false
    private var cameraCentralizada = false

    /*
     * Configuração do monitoramento da localização.
     */
    private val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .build()

    /*
     * Recebe novas posições enquanto
     * a tela estiver aberta.
     */
    private val locationCallback =
        object : LocationCallback() {

            override fun onLocationResult(
                locationResult: LocationResult
            ) {

                val location =
                    locationResult.lastLocation ?: return

                atualizarLocalizacaoUsuario(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    centralizarMapa = !cameraCentralizada
                )
            }
        }

    /*
     * Solicita as permissões de localização.
     */
    private val solicitarPermissaoLocalizacao =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissoes ->

            val fineLocation =
                permissoes[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseLocation =
                permissoes[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (fineLocation || coarseLocation) {

                ativarLocalizacao()

            } else {

                textCoordenadas.text =
                    "Localização não autorizada"

                Toast.makeText(
                    this,
                    "Permissão de localização não concedida.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_cinemas
        )

        textCoordenadas =
            findViewById(
                R.id.textCoordenadas
            )

        textEndereco =
            findViewById(
                R.id.textEndereco
            )

        textStatusCinemas =
            findViewById(
                R.id.textStatusCinemas
            )

        editEndereco =
            findViewById(
                R.id.editEndereco
            )

        btnBuscarEndereco =
            findViewById(
                R.id.btnBuscarEndereco
            )

        btnBuscarCinemas =
            findViewById(
                R.id.btnBuscarCinemas
            )

        geocoder =
            Geocoder(
                this,
                Locale.getDefault()
            )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        /*
         * Pesquisa de endereço.
         */
        btnBuscarEndereco.setOnClickListener {
            executarBuscaEndereco()
        }

        /*
         * Pesquisa de cinemas próximos.
         */
        btnBuscarCinemas.setOnClickListener {
            buscarCinemasProximos()
        }

        /*
         * Permite pesquisar pelo botão Search
         * do teclado virtual.
         */
        editEndereco.setOnEditorActionListener {
                _,
                actionId,
                event ->

            val pressionouBusca =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (
                                event?.keyCode ==
                                        KeyEvent.KEYCODE_ENTER &&
                                        event.action ==
                                        KeyEvent.ACTION_DOWN
                                )

            if (pressionouBusca) {

                executarBuscaEndereco()

                true

            } else {

                false
            }
        }

        val mapFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.map
                ) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    /*
     * Executado quando o Google Maps estiver pronto.
     */
    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        googleMap.uiSettings.isZoomControlsEnabled = true

        configurarCliqueNoMapa()

        verificarPermissaoLocalizacao()
    }

    /*
     * ==========================================================
     * CINEMAS PRÓXIMOS
     * ==========================================================
     */

    private fun buscarCinemasProximos() {

        fecharTeclado()

        val latitude =
            ultimaLatitudeUsuario

        val longitude =
            ultimaLongitudeUsuario

        if (
            latitude == null ||
            longitude == null
        ) {

            Toast.makeText(
                this,
                "Aguarde a localização atual antes de pesquisar cinemas.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        btnBuscarCinemas.isEnabled = false

        textStatusCinemas.text =
            "Buscando cinemas em um raio de 5 km..."

        /*
         * A consulta HTTP é feita fora
         * da thread principal.
         */
        Thread {

            try {

                val cinemas =
                    CinemaRepository.buscarCinemasProximos(
                        latitude = latitude,
                        longitude = longitude,
                        raioMetros = 5000
                    )

                runOnUiThread {

                    if (
                        isFinishing ||
                        isDestroyed
                    ) {
                        return@runOnUiThread
                    }

                    btnBuscarCinemas.isEnabled = true

                    mostrarCinemasNoMapa(
                        cinemas
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "CineFlixCinemas",
                    "Erro ao buscar cinemas próximos",
                    e
                )

                runOnUiThread {

                    if (
                        isFinishing ||
                        isDestroyed
                    ) {
                        return@runOnUiThread
                    }

                    btnBuscarCinemas.isEnabled = true

                    textStatusCinemas.text =
                        "Não foi possível consultar os cinemas."

                    Toast.makeText(
                        this,
                        "Erro ao buscar cinemas próximos.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    /*
     * Mostra os cinemas encontrados no mapa.
     */
    private fun mostrarCinemasNoMapa(
        cinemas: List<CinemaMapa>
    ) {

        /*
         * Remove resultados de uma pesquisa anterior.
         */
        marcadoresCinemas.forEach {
            it.remove()
        }

        marcadoresCinemas.clear()

        if (cinemas.isEmpty()) {

            textStatusCinemas.text =
                "Nenhum cinema encontrado em um raio de 5 km."

            return
        }

        cinemas.forEach { cinema ->

            /*
             * Converte a distância de metros
             * para quilômetros.
             */
            val distanciaKm =
                cinema.distanciaMetros / 1000f

            /*
             * A distância fica no título porque
             * o balão padrão do Google Maps pode
             * cortar textos longos do snippet.
             */
            val tituloMarcador =
                String.format(
                    Locale.getDefault(),
                    "%s • %.1f km",
                    cinema.nome,
                    distanciaKm
                )

            val enderecoMarcador =
                if (
                    cinema.endereco ==
                    "Endereço não informado"
                ) {

                    "Endereço não informado"

                } else {

                    cinema.endereco
                }

            val marcador =
                googleMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                cinema.latitude,
                                cinema.longitude
                            )
                        )
                        .title(
                            tituloMarcador
                        )
                        .snippet(
                            enderecoMarcador
                        )
                        .icon(
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    BitmapDescriptorFactory.HUE_VIOLET
                                )
                        )
                )

            if (marcador != null) {

                marcadoresCinemas.add(
                    marcador
                )
            }
        }

        textStatusCinemas.text =
            if (cinemas.size == 1) {

                "1 cinema encontrado próximo de você."

            } else {

                "${cinemas.size} cinemas encontrados próximos de você."
            }

        /*
         * Reposiciona a câmera para permitir
         * uma visão mais ampla dos resultados.
         */
        val latitudeUsuario =
            ultimaLatitudeUsuario

        val longitudeUsuario =
            ultimaLongitudeUsuario

        if (
            latitudeUsuario != null &&
            longitudeUsuario != null
        ) {

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitudeUsuario,
                        longitudeUsuario
                    ),
                    12f
                )
            )
        }
    }

    /*
     * ==========================================================
     * GEOCODIFICAÇÃO
     * Endereço -> coordenadas
     * ==========================================================
     */

    private fun executarBuscaEndereco() {

        fecharTeclado()

        val termo =
            editEndereco.text
                .toString()
                .trim()

        if (termo.isBlank()) {

            Toast.makeText(
                this,
                "Digite um endereço ou local para pesquisar.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!Geocoder.isPresent()) {

            textEndereco.text =
                "Serviço de geocodificação não disponível."

            return
        }

        ultimaBuscaEndereco = termo

        textEndereco.text =
            "Buscando: $termo..."

        buscarCoordenadasDoEndereco(
            termo
        )
    }

    private fun buscarCoordenadasDoEndereco(
        termo: String
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            geocoder.getFromLocationName(
                termo,
                1,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<Address>
                    ) {

                        if (
                            ultimaBuscaEndereco != termo
                        ) {
                            return
                        }

                        val endereco =
                            addresses.firstOrNull()

                        runOnUiThread {

                            mostrarResultadoDaBusca(
                                termo,
                                endereco
                            )
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {

                        if (
                            ultimaBuscaEndereco != termo
                        ) {
                            return
                        }

                        runOnUiThread {

                            textEndereco.text =
                                "Não foi possível localizar esse endereço."
                        }
                    }
                }
            )

        } else {

            buscarCoordenadasVersaoAntiga(
                termo
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun buscarCoordenadasVersaoAntiga(
        termo: String
    ) {

        Thread {

            try {

                val enderecos =
                    geocoder.getFromLocationName(
                        termo,
                        1
                    )

                if (
                    ultimaBuscaEndereco != termo
                ) {
                    return@Thread
                }

                val endereco =
                    enderecos?.firstOrNull()

                runOnUiThread {

                    mostrarResultadoDaBusca(
                        termo,
                        endereco
                    )
                }

            } catch (e: IOException) {

                runOnUiThread {

                    textEndereco.text =
                        "Erro ao consultar o endereço."
                }

            } catch (e: IllegalArgumentException) {

                runOnUiThread {

                    textEndereco.text =
                        "Endereço inválido."
                }
            }

        }.start()
    }

    private fun mostrarResultadoDaBusca(
        termo: String,
        endereco: Address?
    ) {

        if (endereco == null) {

            textEndereco.text =
                "Nenhum resultado encontrado para: $termo"

            return
        }

        val ponto =
            LatLng(
                endereco.latitude,
                endereco.longitude
            )

        ultimoPontoSelecionado =
            ponto

        marcadorSelecionado?.remove()

        val enderecoFormatado =
            obterTextoDoEndereco(
                endereco
            )

        marcadorSelecionado =
            googleMap.addMarker(
                MarkerOptions()
                    .position(
                        ponto
                    )
                    .title(
                        termo
                    )
                    .snippet(
                        enderecoFormatado
                    )
            )

        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                ponto,
                16f
            )
        )

        textEndereco.text =
            "Local encontrado:\n$enderecoFormatado\n" +
                    String.format(
                        Locale.getDefault(),
                        "Latitude: %.5f | Longitude: %.5f",
                        endereco.latitude,
                        endereco.longitude
                    )

        marcadorSelecionado
            ?.showInfoWindow()
    }

    /*
     * ==========================================================
     * CLIQUE NO MAPA
     * ==========================================================
     */

    private fun configurarCliqueNoMapa() {

        googleMap.setOnMapClickListener {
                pontoSelecionado ->

            fecharTeclado()

            ultimaBuscaEndereco =
                null

            ultimoPontoSelecionado =
                pontoSelecionado

            marcadorSelecionado
                ?.remove()

            marcadorSelecionado =
                googleMap.addMarker(
                    MarkerOptions()
                        .position(
                            pontoSelecionado
                        )
                        .title(
                            "Local selecionado"
                        )
                )

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLng(
                    pontoSelecionado
                )
            )

            textEndereco.text =
                String.format(
                    Locale.getDefault(),
                    "Local selecionado: %.5f, %.5f\nBuscando endereço...",
                    pontoSelecionado.latitude,
                    pontoSelecionado.longitude
                )

            buscarEnderecoDoPonto(
                pontoSelecionado
            )
        }
    }

    /*
     * ==========================================================
     * GEOCODIFICAÇÃO REVERSA
     * Coordenadas -> endereço
     * ==========================================================
     */

    private fun buscarEnderecoDoPonto(
        ponto: LatLng
    ) {

        if (!Geocoder.isPresent()) {

            textEndereco.text =
                "Serviço de geocodificação não disponível."

            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            geocoder.getFromLocation(
                ponto.latitude,
                ponto.longitude,
                1,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<Address>
                    ) {

                        if (
                            ultimoPontoSelecionado != ponto
                        ) {
                            return
                        }

                        runOnUiThread {

                            mostrarEnderecoDoPonto(
                                ponto,
                                addresses.firstOrNull()
                            )
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {

                        if (
                            ultimoPontoSelecionado != ponto
                        ) {
                            return
                        }

                        runOnUiThread {

                            textEndereco.text =
                                "Não foi possível localizar o endereço deste ponto."
                        }
                    }
                }
            )

        } else {

            buscarEnderecoReversoVersaoAntiga(
                ponto
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun buscarEnderecoReversoVersaoAntiga(
        ponto: LatLng
    ) {

        Thread {

            try {

                val enderecos =
                    geocoder.getFromLocation(
                        ponto.latitude,
                        ponto.longitude,
                        1
                    )

                if (
                    ultimoPontoSelecionado != ponto
                ) {
                    return@Thread
                }

                runOnUiThread {

                    mostrarEnderecoDoPonto(
                        ponto,
                        enderecos?.firstOrNull()
                    )
                }

            } catch (e: IOException) {

                runOnUiThread {

                    textEndereco.text =
                        "Erro ao consultar o endereço."
                }

            } catch (e: IllegalArgumentException) {

                runOnUiThread {

                    textEndereco.text =
                        "Coordenadas inválidas."
                }
            }

        }.start()
    }

    private fun mostrarEnderecoDoPonto(
        ponto: LatLng,
        endereco: Address?
    ) {

        if (endereco == null) {

            textEndereco.text =
                "Nenhum endereço encontrado para este ponto."

            return
        }

        val enderecoFormatado =
            obterTextoDoEndereco(
                endereco
            )

        textEndereco.text =
            "Endereço selecionado:\n$enderecoFormatado"

        marcadorSelecionado?.let {
                marcador ->

            if (
                marcador.position == ponto
            ) {

                marcador.title =
                    "Local selecionado"

                marcador.snippet =
                    enderecoFormatado

                marcador.showInfoWindow()
            }
        }
    }

    /*
     * Monta uma representação legível
     * do endereço.
     */
    private fun obterTextoDoEndereco(
        endereco: Address
    ): String {

        val linhaCompleta =
            endereco.getAddressLine(
                0
            )

        if (
            !linhaCompleta.isNullOrBlank()
        ) {
            return linhaCompleta
        }

        val partes =
            listOfNotNull(
                endereco.thoroughfare,
                endereco.subThoroughfare,
                endereco.subLocality,
                endereco.locality,
                endereco.adminArea,
                endereco.postalCode,
                endereco.countryName
            )
                .filter {
                    it.isNotBlank()
                }

        return if (
            partes.isNotEmpty()
        ) {

            partes.joinToString(
                ", "
            )

        } else {

            "Endereço não identificado"
        }
    }

    /*
     * ==========================================================
     * LOCALIZAÇÃO DO USUÁRIO
     * ==========================================================
     */

    private fun verificarPermissaoLocalizacao() {

        val fineLocation =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val coarseLocation =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (
            fineLocation ==
            PackageManager.PERMISSION_GRANTED ||
            coarseLocation ==
            PackageManager.PERMISSION_GRANTED
        ) {

            ativarLocalizacao()

        } else {

            solicitarPermissaoLocalizacao.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun ativarLocalizacao() {

        if (
            !temPermissaoLocalizacao()
        ) {
            return
        }

        googleMap.isMyLocationEnabled =
            true

        textCoordenadas.text =
            "Obtendo localização..."

        /*
         * Tenta recuperar uma posição já disponível.
         */
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                    location ->

                if (
                    location != null
                ) {

                    atualizarLocalizacaoUsuario(
                        latitude =
                            location.latitude,
                        longitude =
                            location.longitude,
                        centralizarMapa =
                            !cameraCentralizada
                    )
                }
            }

        /*
         * Solicita também uma localização atual.
         */
        val cancellationTokenSource =
            CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener {
                    location ->

                if (
                    location != null
                ) {

                    atualizarLocalizacaoUsuario(
                        latitude =
                            location.latitude,
                        longitude =
                            location.longitude,
                        centralizarMapa =
                            !cameraCentralizada
                    )
                }
            }
            .addOnFailureListener {

                if (
                    textCoordenadas.text ==
                    "Obtendo localização..."
                ) {

                    textCoordenadas.text =
                        "Aguardando sinal de localização..."
                }
            }

        iniciarMonitoramentoLocalizacao()
    }

    private fun atualizarLocalizacaoUsuario(
        latitude: Double,
        longitude: Double,
        centralizarMapa: Boolean
    ) {

        /*
         * Guarda as coordenadas para a busca
         * dos cinemas próximos.
         */
        ultimaLatitudeUsuario =
            latitude

        ultimaLongitudeUsuario =
            longitude

        textCoordenadas.text =
            String.format(
                Locale.getDefault(),
                "Sua localização: %.5f, %.5f",
                latitude,
                longitude
            )

        if (
            centralizarMapa
        ) {

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude,
                        longitude
                    ),
                    15f
                )
            )

            cameraCentralizada =
                true
        }
    }

    /*
     * ==========================================================
     * MONITORAMENTO DE LOCALIZAÇÃO
     * ==========================================================
     */

    private fun iniciarMonitoramentoLocalizacao() {

        if (
            monitorandoLocalizacao
        ) {
            return
        }

        if (
            !temPermissaoLocalizacao()
        ) {
            return
        }

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        monitorandoLocalizacao =
            true
    }

    private fun pararMonitoramentoLocalizacao() {

        if (
            !monitorandoLocalizacao
        ) {
            return
        }

        fusedLocationClient
            .removeLocationUpdates(
                locationCallback
            )

        monitorandoLocalizacao =
            false
    }

    private fun temPermissaoLocalizacao(): Boolean {

        val fineLocation =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val coarseLocation =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        return (
                fineLocation ==
                        PackageManager.PERMISSION_GRANTED ||
                        coarseLocation ==
                        PackageManager.PERMISSION_GRANTED
                )
    }

    /*
     * Fecha o teclado virtual e remove
     * o foco do campo de pesquisa.
     */
    private fun fecharTeclado() {

        WindowCompat
            .getInsetsController(
                window,
                window.decorView
            )
            .hide(
                WindowInsetsCompat.Type.ime()
            )

        editEndereco.clearFocus()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::googleMap.isInitialized
        ) {

            iniciarMonitoramentoLocalizacao()
        }
    }

    override fun onPause() {

        pararMonitoramentoLocalizacao()

        super.onPause()
    }
}