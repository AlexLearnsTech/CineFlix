package com.example.cineflix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Responsável pela criação e gerenciamento do banco SQLite de filmes favoritos.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cineflix.db"
        private const val DATABASE_VERSION = 1

        const val TABELA_FAVORITOS = "favoritos"
        const val COL_ID = "id"
        const val COL_TITULO = "titulo"
        const val COL_GENEROS = "generos"
        const val COL_ANO = "ano"
        const val COL_NOTA = "nota"
        const val COL_SINOPSE = "sinopse"
        const val COL_POSTER = "posterUrl"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE $TABELA_FAVORITOS (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_TITULO TEXT NOT NULL,
                $COL_GENEROS TEXT,
                $COL_ANO INTEGER,
                $COL_NOTA REAL,
                $COL_SINOPSE TEXT,
                $COL_POSTER TEXT
            )
        """.trimIndent()
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA_FAVORITOS")
        onCreate(db)
    }

    /** Adiciona (ou substitui, caso já exista) um filme na tabela de favoritos. */
    fun adicionarFavorito(movie: Movie): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_ID, movie.id)
            put(COL_TITULO, movie.titulo)
            put(COL_GENEROS, movie.generos)
            put(COL_ANO, movie.ano)
            put(COL_NOTA, movie.nota)
            put(COL_SINOPSE, movie.sinopse)
            put(COL_POSTER, movie.posterUrl)
        }
        val resultado = db.insertWithOnConflict(
            TABELA_FAVORITOS, null, valores, SQLiteDatabase.CONFLICT_REPLACE
        )
        return resultado != -1L
    }

    fun removerFavorito(id: Int) {
        val db = writableDatabase
        db.delete(TABELA_FAVORITOS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun isFavorito(id: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABELA_FAVORITOS, arrayOf(COL_ID), "$COL_ID = ?",
            arrayOf(id.toString()), null, null, null
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun listarFavoritos(): List<Movie> {
        val lista = mutableListOf<Movie>()
        val db = readableDatabase
        val cursor = db.query(TABELA_FAVORITOS, null, null, null, null, null, "$COL_TITULO ASC")
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Movie(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        titulo = it.getString(it.getColumnIndexOrThrow(COL_TITULO)),
                        generos = it.getString(it.getColumnIndexOrThrow(COL_GENEROS)),
                        ano = it.getInt(it.getColumnIndexOrThrow(COL_ANO)),
                        nota = it.getDouble(it.getColumnIndexOrThrow(COL_NOTA)),
                        sinopse = it.getString(it.getColumnIndexOrThrow(COL_SINOPSE)),
                        posterUrl = it.getString(it.getColumnIndexOrThrow(COL_POSTER))
                    )
                )
            }
        }
        return lista
    }
}
