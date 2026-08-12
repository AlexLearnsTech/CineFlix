package com.example.cineflix

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cineflix.databinding.ActivityMainBinding
import java.text.Normalizer
import java.util.Locale
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MovieAdapter

    // Catálogo carregado do arquivo res/raw/movies.json.
    private var catalogoLocal: List<Movie> = emptyList()

    // Lista usada atualmente pela pesquisa e pelo RecyclerView.
    private var listaAtual: List<Movie> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Mantém a leitura do arquivo local exigida pelo trabalho.
        catalogoLocal = MovieRepository.carregarFilmes(this)
        listaAtual = catalogoLocal

        adapter = MovieAdapter(
            filmes = listaAtual,
            onClick = { filme ->
                val intent = Intent(
                    this,
                    DetalhesActivity::class.java
                )

                intent.putExtra("movie_id", filme.id)
                startActivity(intent)
            }
        )

        binding.recyclerFilmes.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerFilmes.adapter = adapter

        restaurarUltimaPesquisa()
        configurarPesquisaEnquantoDigita()
        configurarBotaoPesquisaDoTeclado()
    }

    /**
     * Recupera a última pesquisa salva no SharedPreferences.
     */
    private fun restaurarUltimaPesquisa() {
        val ultimaPesquisa =
            PreferencesManager.getUltimaPesquisa(this)

        if (ultimaPesquisa.isNotBlank()) {
            binding.editSearch.setText(ultimaPesquisa)

            binding.editSearch.setSelection(
                binding.editSearch.text.length
            )

            mostrarPreviaDaPesquisa(ultimaPesquisa)
        }
    }

    /**
     * Enquanto o usuário digita, filtra a lista que já está carregada.
     * A consulta on-line acontece quando ele pressiona a lupa.
     */
    private fun configurarPesquisaEnquantoDigita() {
        binding.editSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Não precisamos executar nenhuma ação aqui.
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val termo = s?.toString().orEmpty()

                    PreferencesManager.setUltimaPesquisa(
                        this@MainActivity,
                        termo
                    )

                    if (termo.isBlank()) {
                        listaAtual = catalogoLocal
                        mostrarFilmes(catalogoLocal)
                    } else {
                        mostrarPreviaDaPesquisa(termo)
                    }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                    // Não precisamos executar nenhuma ação aqui.
                }
            }
        )
    }

    /**
     * Executa a pesquisa no TMDB quando o usuário toca na lupa
     * ou pressiona Enter.
     */
    private fun configurarBotaoPesquisaDoTeclado() {
        binding.editSearch.setOnEditorActionListener {
                _,
                actionId,
                event ->

            val pressionouPesquisa =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (
                                event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                                        event.action == KeyEvent.ACTION_DOWN
                                )

            if (!pressionouPesquisa) {
                return@setOnEditorActionListener false
            }

            val termo = binding.editSearch.text
                .toString()
                .trim()

            if (termo.isBlank()) {
                listaAtual = catalogoLocal
                mostrarFilmes(catalogoLocal)
            } else {
                // Salva o termo no histórico interno.
                HistoricoManager.salvar(this, termo)

                // Pesquisa filmes reais pela internet.
                pesquisarNoTmdb(termo)
            }

            true
        }
    }

    /**
     * Pesquisa os filmes em uma thread separada para não bloquear
     * a interface do aplicativo.
     */
    private fun pesquisarNoTmdb(termo: String) {
        val apiKey = BuildConfig.TMDB_API_KEY.trim()

        if (apiKey.isBlank()) {
            Toast.makeText(
                this,
                R.string.api_tmdb_nao_configurada,
                Toast.LENGTH_LONG
            ).show()

            return
        }

        binding.textVazio.text =
            getString(R.string.buscando_filmes)

        binding.textVazio.visibility = View.VISIBLE

        // Limpa temporariamente a lista enquanto carrega.
        adapter.atualizarLista(emptyList())

        Thread {
            try {
                val filmesEncontrados =
                    TmdbRepository.buscarFilmes(
                        apiKey = apiKey,
                        termo = termo
                    )

                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    /*
                     * Impede que o resultado de uma busca antiga apareça
                     * caso o usuário já tenha digitado outro título.
                     */
                    val textoAtual = binding.editSearch.text
                        .toString()
                        .trim()

                    if (textoAtual != termo) {
                        return@runOnUiThread
                    }

                    listaAtual = filmesEncontrados

                    /*
                     * Guarda os filmes encontrados para que a tela de
                     * detalhes consiga localizá-los pelo ID.
                     */
                    MovieRepository.registrarFilmesOnline(
                        filmesEncontrados
                    )

                    mostrarFilmes(filmesEncontrados)
                }
            } catch (e: Exception) {
                Log.e(
                    "CineFlixTMDB",
                    "Erro ao consultar o TMDB",
                    e
                )

                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    // Em caso de erro, tenta mostrar resultados locais.
                    listaAtual = catalogoLocal

                    val resultadosLocais = filtrarFilmes(
                        fonte = catalogoLocal,
                        termo = termo
                    )

                    adapter.atualizarLista(resultadosLocais)

                    binding.textVazio.text =
                        getString(R.string.erro_busca_tmdb)

                    binding.textVazio.visibility =
                        if (resultadosLocais.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    Toast.makeText(
                        this,
                        R.string.erro_busca_tmdb,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    /**
     * Filtra a lista atual, aceitando pesquisa com ou sem acentos.
     */
    private fun filtrarListaAtual(termo: String) {
        val filtrados = filtrarFilmes(
            fonte = listaAtual,
            termo = termo
        )

        mostrarFilmes(filtrados)
    }

    private fun filtrarFilmes(
        fonte: List<Movie>,
        termo: String
    ): List<Movie> {
        val termoNormalizado = normalizarTexto(termo)

        if (termoNormalizado.isBlank()) {
            return fonte
        }

        return fonte.filter { filme ->
            normalizarTexto(filme.titulo)
                .contains(termoNormalizado) ||
                    normalizarTexto(filme.generos)
                        .contains(termoNormalizado)
        }
    }
    private fun mostrarPreviaDaPesquisa(termo: String) {
        val resultadosLocais = filtrarFilmes(
            fonte = catalogoLocal,
            termo = termo
        )

        adapter.atualizarLista(resultadosLocais)

        if (resultadosLocais.isEmpty()) {
            binding.textVazio.text =
                getString(R.string.toque_lupa_buscar)

            binding.textVazio.visibility = View.VISIBLE
        } else {
            binding.textVazio.visibility = View.GONE
        }
    }
    private fun normalizarTexto(texto: String): String {
        return Normalizer.normalize(
            texto,
            Normalizer.Form.NFD
        )
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()
    }

    private fun mostrarFilmes(filmes: List<Movie>) {
        adapter.atualizarLista(filmes)

        binding.textVazio.text =
            getString(R.string.nenhum_filme_encontrado)

        binding.textVazio.visibility =
            if (filmes.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {
        return when (item.itemId) {

            R.id.action_favoritos -> {
                startActivity(
                    Intent(
                        this,
                        FavoritosActivity::class.java
                    )
                )
                true
            }

            R.id.action_cinemas -> {
                startActivity(
                    Intent(
                        this,
                        CinemasActivity::class.java
                    )
                )
                true
            }

            R.id.action_config -> {
                startActivity(
                    Intent(
                        this,
                        ConfiguracoesActivity::class.java
                    )
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}