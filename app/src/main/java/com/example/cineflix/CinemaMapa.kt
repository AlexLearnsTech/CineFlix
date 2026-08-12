package com.example.cineflix

data class CinemaMapa(
    val osmId: String,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val endereco: String,
    val distanciaMetros: Float
)

