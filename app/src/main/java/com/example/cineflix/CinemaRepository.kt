package com.example.cineflix

import android.location.Location
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object CinemaRepository {

    private const val OVERPASS_URL =
        "https://overpass-api.de/api/interpreter"

    /**
     * Pesquisa cinemas reais no OpenStreetMap
     * próximos das coordenadas informadas.
     */
    fun buscarCinemasProximos(
        latitude: Double,
        longitude: Double,
        raioMetros: Int = 5000
    ): List<CinemaMapa> {

        /*
         * amenity=cinema é a classificação usada
         * pelo OpenStreetMap para cinemas.
         *
         * Pesquisamos nodes, ways e relations
         * porque um cinema pode estar cadastrado
         * de diferentes formas no OpenStreetMap.
         */
        val consulta = """
            [out:json][timeout:20];
            (
              node["amenity"="cinema"](around:$raioMetros,$latitude,$longitude);
              way["amenity"="cinema"](around:$raioMetros,$latitude,$longitude);
              relation["amenity"="cinema"](around:$raioMetros,$latitude,$longitude);
            );
            out center tags;
        """.trimIndent()

        val consultaCodificada =
            URLEncoder.encode(
                consulta,
                StandardCharsets.UTF_8.toString()
            )

        val url =
            URL(
                "$OVERPASS_URL?data=$consultaCodificada"
            )

        val conexao =
            url.openConnection() as HttpURLConnection

        try {

            conexao.requestMethod = "GET"

            conexao.connectTimeout = 15000
            conexao.readTimeout = 25000

            conexao.setRequestProperty(
                "Accept",
                "application/json"
            )

            conexao.setRequestProperty(
                "User-Agent",
                "CineFlix-Android/1.0"
            )

            val codigoResposta =
                conexao.responseCode

            if (
                codigoResposta !in 200..299
            ) {

                throw IOException(
                    "Erro HTTP: $codigoResposta"
                )
            }

            val resposta =
                BufferedReader(
                    InputStreamReader(
                        conexao.inputStream
                    )
                ).use {
                    it.readText()
                }

            return processarResposta(
                json = resposta,
                latitudeUsuario = latitude,
                longitudeUsuario = longitude
            )

        } finally {

            conexao.disconnect()
        }
    }

    /**
     * Converte o JSON retornado pelo Overpass
     * em objetos CinemaMapa.
     */
    private fun processarResposta(
        json: String,
        latitudeUsuario: Double,
        longitudeUsuario: Double
    ): List<CinemaMapa> {

        val raiz =
            JSONObject(json)

        val elementos =
            raiz.getJSONArray(
                "elements"
            )

        val cinemas =
            mutableListOf<CinemaMapa>()

        for (
        indice in 0 until elementos.length()
        ) {

            val elemento =
                elementos.getJSONObject(
                    indice
                )

            val tipo =
                elemento.optString(
                    "type"
                )

            val id =
                elemento.optLong(
                    "id"
                )

            val coordenadas =
                obterCoordenadas(
                    elemento,
                    tipo
                ) ?: continue

            val latitudeCinema =
                coordenadas.first

            val longitudeCinema =
                coordenadas.second

            val tags =
                elemento.optJSONObject(
                    "tags"
                )

            val nome =
                tags
                    ?.optString("name")
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: tags
                        ?.optString("brand")
                        ?.takeIf {
                            it.isNotBlank()
                        }
                    ?: "Cinema"

            val endereco =
                montarEndereco(
                    tags
                )

            val resultadoDistancia =
                FloatArray(1)

            Location.distanceBetween(
                latitudeUsuario,
                longitudeUsuario,
                latitudeCinema,
                longitudeCinema,
                resultadoDistancia
            )

            cinemas.add(
                CinemaMapa(
                    osmId = "$tipo:$id",
                    nome = nome,
                    latitude = latitudeCinema,
                    longitude = longitudeCinema,
                    endereco = endereco,
                    distanciaMetros =
                        resultadoDistancia[0]
                )
            )
        }

        /*
         * Remove eventuais duplicidades
         * e ordena do cinema mais próximo
         * para o mais distante.
         */
        return cinemas
            .distinctBy {
                it.osmId
            }
            .sortedBy {
                it.distanciaMetros
            }
    }

    /**
     * Nodes possuem latitude e longitude diretamente.
     *
     * Ways e relations utilizam o objeto center
     * porque representam áreas.
     */
    private fun obterCoordenadas(
        elemento: JSONObject,
        tipo: String
    ): Pair<Double, Double>? {

        return if (
            tipo == "node"
        ) {

            if (
                !elemento.has("lat") ||
                !elemento.has("lon")
            ) {
                null
            } else {

                Pair(
                    elemento.getDouble("lat"),
                    elemento.getDouble("lon")
                )
            }

        } else {

            val centro =
                elemento.optJSONObject(
                    "center"
                ) ?: return null

            if (
                !centro.has("lat") ||
                !centro.has("lon")
            ) {
                null
            } else {

                Pair(
                    centro.getDouble("lat"),
                    centro.getDouble("lon")
                )
            }
        }
    }

    /**
     * Monta um endereço com os campos
     * disponíveis no OpenStreetMap.
     */
    private fun montarEndereco(
        tags: JSONObject?
    ): String {

        if (tags == null) {

            return "Endereço não informado"
        }

        val rua =
            tags.optString(
                "addr:street"
            )
                .ifBlank {
                    tags.optString(
                        "addr:place"
                    )
                }

        val numero =
            tags.optString(
                "addr:housenumber"
            )

        val bairro =
            tags.optString(
                "addr:suburb"
            )

        val cidade =
            tags.optString(
                "addr:city"
            )

        val enderecoPrincipal =
            when {

                rua.isNotBlank() &&
                        numero.isNotBlank() -> {
                    "$rua, $numero"
                }

                rua.isNotBlank() -> {
                    rua
                }

                else -> {
                    ""
                }
            }

        val partes =
            listOf(
                enderecoPrincipal,
                bairro,
                cidade
            )
                .filter {
                    it.isNotBlank()
                }

        return if (
            partes.isEmpty()
        ) {

            "Endereço não informado"

        } else {

            partes.joinToString(", ")
        }
    }
}

