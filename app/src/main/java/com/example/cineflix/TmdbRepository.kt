package com.example.cineflix

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TmdbRepository {

    private const val API_BASE_URL =
        "https://api.themoviedb.org/3"

    private const val IMAGE_BASE_URL =
        "https://image.tmdb.org/t/p/w500"

    private val generos = mapOf(
        28 to "Ação",
        12 to "Aventura",
        16 to "Animação",
        35 to "Comédia",
        80 to "Crime",
        99 to "Documentário",
        18 to "Drama",
        10751 to "Família",
        14 to "Fantasia",
        36 to "História",
        27 to "Terror",
        10402 to "Música",
        9648 to "Mistério",
        10749 to "Romance",
        878 to "Ficção científica",
        10770 to "Cinema TV",
        53 to "Suspense",
        10752 to "Guerra",
        37 to "Faroeste"
    )

    fun buscarFilmes(
        apiKey: String,
        termo: String
    ): List<Movie> {

        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "A chave da API TMDB não foi configurada."
            )
        }

        val termoCodificado = URLEncoder.encode(
            termo.trim(),
            "UTF-8"
        )

        val endereco =
            "$API_BASE_URL/search/movie" +
                    "?api_key=$apiKey" +
                    "&query=$termoCodificado" +
                    "&language=pt-BR" +
                    "&include_adult=false" +
                    "&page=1"

        val resposta = fazerRequisicao(endereco)

        val resultados = resposta.optJSONArray("results")
            ?: return emptyList()

        val filmesEncontrados = mutableListOf<Movie>()

        for (i in 0 until resultados.length()) {
            val item = resultados.getJSONObject(i)

            val ano = item.optString("release_date")
                .take(4)
                .toIntOrNull()
                ?: 0

            val posterPath = item.optString("poster_path")

            val posterUrl =
                if (posterPath.isBlank() || posterPath == "null") {
                    ""
                } else {
                    "$IMAGE_BASE_URL$posterPath"
                }

            val idsGeneros = item.optJSONArray("genre_ids")
            val nomesGeneros = mutableListOf<String>()

            if (idsGeneros != null) {
                for (j in 0 until idsGeneros.length()) {
                    val idGenero = idsGeneros.optInt(j)

                    generos[idGenero]?.let { nome ->
                        nomesGeneros.add(nome)
                    }
                }
            }

            val textoGeneros =
                if (nomesGeneros.isEmpty()) {
                    "Gênero não informado"
                } else {
                    nomesGeneros.joinToString(" • ")
                }

            val sinopse = item.optString("overview")
                .ifBlank {
                    "Sinopse não disponível."
                }

            filmesEncontrados.add(
                Movie(
                    id = item.optInt("id"),
                    titulo = item.optString(
                        "title",
                        "Título não informado"
                    ),
                    generos = textoGeneros,
                    ano = ano,
                    nota = item.optDouble(
                        "vote_average",
                        0.0
                    ),
                    sinopse = sinopse,
                    posterUrl = posterUrl
                )
            )
        }

        return filmesEncontrados
    }

    private fun fazerRequisicao(
        endereco: String
    ): JSONObject {

        var conexao: HttpURLConnection? = null

        try {
            conexao = URL(endereco).openConnection()
                    as HttpURLConnection

            conexao.requestMethod = "GET"
            conexao.connectTimeout = 15_000
            conexao.readTimeout = 15_000
            conexao.setRequestProperty(
                "Accept",
                "application/json"
            )

            val codigoResposta = conexao.responseCode

            val fluxo =
                if (codigoResposta in 200..299) {
                    conexao.inputStream
                } else {
                    conexao.errorStream
                }

            val conteudo = fluxo
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (codigoResposta !in 200..299) {
                throw IllegalStateException(
                    "Erro do TMDB: HTTP $codigoResposta"
                )
            }

            return JSONObject(conteudo)

        } finally {
            conexao?.disconnect()
        }
    }
}