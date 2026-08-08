package com.example.cineflix

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import java.io.FileInputStream
import java.io.InputStreamReader

import android.os.Build

/**
 * Carrega o catálogo de filmes a partir de res/raw/movies.json.
 * O resultado é mantido em cache em memória para evitar reparsing repetido.
 */
object MovieRepository {
    private var cache: List<Movie>? = null
    private val filmesOnline = mutableMapOf<Int, Movie>()

    fun carregarFilmes(context: Context): List<Movie> {
        cache?.let { return it }
        val jsonText = context.resources.openRawResource(R.raw.movies)
            .bufferedReader()
            .use { it.readText() }

        val arr = JSONArray(jsonText)
        val lista = mutableListOf<Movie>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val recArr = o.optJSONArray("recomendados")
            val recomendados = mutableListOf<Int>()
            if (recArr != null) {
                for (j in 0 until recArr.length()) {
                    recomendados.add(recArr.getInt(j))
                }
            }
            lista.add(
                Movie(
                    id = o.getInt("id"),
                    titulo = o.getString("titulo"),
                    generos = o.getString("generos"),
                    ano = o.getInt("ano"),
                    nota = o.getDouble("nota"),
                    sinopse = o.getString("sinopse"),
                    posterUrl = o.getString("posterUrl"),
                    recomendados = recomendados
                )
            )
        }
        cache = lista
        return lista
    }
    fun registrarFilmesOnline(filmes: List<Movie>) {
        filmes.forEach { filme ->
            filmesOnline[filme.id] = filme
        }
    }
    fun buscarPorId(context: Context, id: Int): Movie? {
        return filmesOnline[id]
            ?: carregarFilmes(context).find { filme ->
                filme.id == id
            }
    }
}

/**
 * Gerencia as preferências do usuário (SharedPreferences): tema, ordenação
 * de favoritos e última pesquisa realizada.
 */
object PreferencesManager {
    private const val PREFS = "cineflix_prefs"
    private const val KEY_TEMA_ESCURO = "tema_escuro"
    private const val KEY_ULTIMA_PESQUISA = "ultima_pesquisa"
    private const val KEY_ORDENACAO = "ordenacao_favoritos"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isTemaEscuro(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TEMA_ESCURO, false)

    fun setTemaEscuro(context: Context, ativo: Boolean) {
        prefs(context).edit().putBoolean(KEY_TEMA_ESCURO, ativo).apply()
    }

    fun getUltimaPesquisa(context: Context): String =
        prefs(context).getString(KEY_ULTIMA_PESQUISA, "") ?: ""

    fun setUltimaPesquisa(context: Context, termo: String) {
        prefs(context).edit().putString(KEY_ULTIMA_PESQUISA, termo).apply()
    }

    /** 0 = nome, 1 = ano, 2 = nota */
    fun getOrdenacao(context: Context): Int =
        prefs(context).getInt(KEY_ORDENACAO, 0)

    fun setOrdenacao(context: Context, tipo: Int) {
        prefs(context).edit().putInt(KEY_ORDENACAO, tipo).apply()
    }
}

/**
 * Grava e lê o histórico de pesquisas usando armazenamento interno
 * (openFileOutput / arquivo em filesDir), conforme pedido na estrutura do app.
 */
object HistoricoManager {

    private const val ARQUIVO = "historico.txt"

    fun salvar(context: Context, termo: String) {
        if (termo.isBlank()) return

        val timestamp = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("pt", "BR")
        ).format(Date())

        val linha = "$timestamp - $termo\n"

        val fileOutputStream: FileOutputStream =
            context.openFileOutput(
                ARQUIVO,
                Context.MODE_APPEND
            )

        fileOutputStream.use { arquivo ->
            arquivo.write(linha.toByteArray())
        }
    }

    fun listar(context: Context): List<String> {
        val arquivo = File(context.filesDir, ARQUIVO)

        if (!arquivo.exists()) {
            return emptyList()
        }

        val fileInputStream: FileInputStream =
            context.openFileInput(ARQUIVO)

        val inputStreamReader =
            InputStreamReader(fileInputStream)

        return inputStreamReader.use { leitor ->
            leitor.readLines().asReversed()
        }
    }

    fun limpar(context: Context) {
        val arquivo = File(
            context.filesDir,
            ARQUIVO
        )

        if (arquivo.exists()) {
            arquivo.delete()
        }
    }
}

/**
 * Exporta a lista de favoritos para um arquivo CSV em armazenamento externo
 * específico do app (não exige permissão em runtime) e permite compartilhá-lo.
 */
object ExportManager {

    @Suppress("DEPRECATION")
    fun exportarFavoritos(
        context: Context,
        favoritos: List<Movie>
    ): File? {
        return try {

            val dir: File = if (
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
            ) {
                // Método solicitado pelo enunciado do módulo
                File(
                    Environment.getExternalStorageDirectory(),
                    "CineFlix"
                )
            } else {
                // Forma adequada para versões modernas do Android
                context.getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS
                ) ?: return null
            }

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(
                dir,
                "favoritos_cineflix.csv"
            )

            FileOutputStream(file).use { fos ->

                fos.write(
                    "Titulo;Generos;Ano;Nota;Sinopse\n"
                        .toByteArray()
                )

                favoritos.forEach { filme ->

                    val sinopseSegura = filme.sinopse
                        .replace(";", ",")
                        .replace("\n", " ")

                    val linha =
                        "${filme.titulo};" +
                                "${filme.generos};" +
                                "${filme.ano};" +
                                "${filme.nota};" +
                                "$sinopseSegura\n"

                    fos.write(linha.toByteArray())
                }
            }

            file

        } catch (e: Exception) {
            null
        }
    }

    fun compartilharArquivo(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar favoritos"))
    }
}
