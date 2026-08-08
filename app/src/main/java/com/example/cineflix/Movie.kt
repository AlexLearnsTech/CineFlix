package com.example.cineflix

/**
 * Representa um filme dentro do CineFlix.
 * "recomendados" contém os IDs de outros filmes sugeridos (usado apenas no catálogo,
 * não é persistido na tabela de favoritos).
 */
data class Movie(
    val id: Int,
    val titulo: String,
    val generos: String,
    val ano: Int,
    val nota: Double,
    val sinopse: String,
    val posterUrl: String,
    val recomendados: List<Int> = emptyList()
)
