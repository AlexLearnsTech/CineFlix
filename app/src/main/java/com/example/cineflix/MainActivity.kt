package com.example.cineflix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cineflix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: HomeMovieAdapter

    private var catalogoLocal: List<Movie> =
        emptyList()

    private var listaAtual: List<Movie> =
        emptyList()

    private val handlerPesquisa =
        Handler(
            Looper.getMainLooper()
        )

    private var pesquisaPendente: Runnable? =
        null

    private var ultimoTermoConsultadoOnline =
        ""

    companion object {

        private const val ATRASO_PESQUISA_MS =
            600L

        private const val MIN_CARACTERES_PESQUISA =
            2
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setSupportActionBar(
            binding.toolbar
        )

        supportActionBar?.title =
            ""

        catalogoLocal =
            MovieRepository.carregarFilmes(
                this
            )

        listaAtual =
            catalogoLocal

        configurarRecyclerView()

        configurarAtalhos()

        mostrarUltimaPesquisaSalva()

        configurarPesquisaEnquantoDigita()

        configurarBotaoPesquisaDoTeclado()

        mostrarDestaques()
    }

    private fun configurarRecyclerView() {

        adapter =
            HomeMovieAdapter(
                filmes =
                    emptyList(),
                onClick = {
                        filme ->

                    abrirDetalhes(
                        filme
                    )
                }
            )

        binding.recyclerFilmes.layoutManager =
            GridLayoutManager(
                this,
                2
            )

        binding.recyclerFilmes.adapter =
            adapter

        binding.recyclerFilmes.post {

            ajustarAlturaDosCards()
        }
    }

    private fun ajustarAlturaDosCards() {

        val alturaDisponivel =
            binding.recyclerFilmes.height

        if (
            alturaDisponivel <= 0
        ) {

            return
        }

        val margemSeguranca =
            HelperMethods.dpParaPixels(
                context = this,
                dp = 8
            )

        val alturaUtil =
            alturaDisponivel -
                    margemSeguranca

        val alturaPorLinha =
            alturaUtil / 2

        adapter.definirAlturaItem(
            alturaPorLinha
        )
    }

    private fun configurarAtalhos() {

        binding.btnHomeGaleria
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        GaleriaActivity::class.java
                    )
                )
            }

        binding.btnHomeFavoritos
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        FavoritosActivity::class.java
                    )
                )
            }

        binding.btnHomeCinemas
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        CinemasActivity::class.java
                    )
                )
            }
    }

    private fun mostrarUltimaPesquisaSalva() {

        val ultimaPesquisa =
            PreferencesManager
                .getUltimaPesquisa(
                    this
                )

        if (
            ultimaPesquisa.isBlank()
        ) {

            binding.textUltimaPesquisa.visibility =
                View.GONE

            return
        }

        binding.textUltimaPesquisa.text =
            "Última pesquisa: $ultimaPesquisa"

        binding.textUltimaPesquisa.visibility =
            View.VISIBLE

        binding.textUltimaPesquisa
            .setOnClickListener {

                binding.editSearch.setText(
                    ultimaPesquisa
                )

                binding.editSearch.setSelection(
                    binding.editSearch
                        .text
                        .length
                )
            }
    }

    private fun configurarPesquisaEnquantoDigita() {

        binding.editSearch
            .addTextChangedListener(

                object : TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        val termo =
                            s
                                ?.toString()
                                .orEmpty()
                                .trim()

                        cancelarPesquisaAutomatica()

                        if (
                            termo.isBlank()
                        ) {

                            ultimoTermoConsultadoOnline =
                                ""

                            mostrarDestaques()

                            return
                        }

                        mostrarPreviaDaPesquisa(
                            termo
                        )

                        if (
                            termo.length >=
                            MIN_CARACTERES_PESQUISA
                        ) {

                            agendarPesquisaAutomatica(
                                termo
                            )
                        }
                    }

                    override fun afterTextChanged(
                        s: Editable?
                    ) {
                    }
                }
            )
    }

    private fun agendarPesquisaAutomatica(
        termo: String
    ) {

        val novaPesquisa =
            Runnable {

                val textoAtual =
                    binding.editSearch
                        .text
                        .toString()
                        .trim()

                if (
                    textoAtual != termo
                ) {

                    return@Runnable
                }

                if (
                    ultimoTermoConsultadoOnline
                        .equals(
                            termo,
                            ignoreCase = true
                        )
                ) {

                    return@Runnable
                }

                ultimoTermoConsultadoOnline =
                    termo

                pesquisarNoTmdb(
                    termo = termo,
                    pesquisaAutomatica = true
                )
            }

        pesquisaPendente =
            novaPesquisa

        handlerPesquisa.postDelayed(
            novaPesquisa,
            ATRASO_PESQUISA_MS
        )
    }

    private fun cancelarPesquisaAutomatica() {

        pesquisaPendente?.let {
                pesquisa ->

            handlerPesquisa.removeCallbacks(
                pesquisa
            )
        }

        pesquisaPendente =
            null
    }

    private fun configurarBotaoPesquisaDoTeclado() {

        binding.editSearch
            .setOnEditorActionListener {
                    _,
                    actionId,
                    event ->

                val pressionouPesquisa =
                    actionId ==
                            EditorInfo.IME_ACTION_SEARCH ||
                            actionId ==
                            EditorInfo.IME_ACTION_DONE ||
                            (
                                    event?.keyCode ==
                                            KeyEvent.KEYCODE_ENTER &&
                                            event.action ==
                                            KeyEvent.ACTION_DOWN
                                    )

                if (
                    !pressionouPesquisa
                ) {

                    return@setOnEditorActionListener false
                }

                val termo =
                    binding.editSearch
                        .text
                        .toString()
                        .trim()

                cancelarPesquisaAutomatica()

                if (
                    termo.isBlank()
                ) {

                    mostrarDestaques()

                    return@setOnEditorActionListener true
                }

                HistoricoManager.salvar(
                    this,
                    termo
                )

                PreferencesManager
                    .setUltimaPesquisa(
                        this,
                        termo
                    )

                atualizarTextoUltimaPesquisa(
                    termo
                )

                ultimoTermoConsultadoOnline =
                    termo

                pesquisarNoTmdb(
                    termo = termo,
                    pesquisaAutomatica = false
                )

                true
            }
    }

    private fun atualizarTextoUltimaPesquisa(
        termo: String
    ) {

        binding.textUltimaPesquisa.text =
            "Última pesquisa: $termo"

        binding.textUltimaPesquisa.visibility =
            View.VISIBLE

        binding.textUltimaPesquisa
            .setOnClickListener {

                binding.editSearch.setText(
                    termo
                )

                binding.editSearch.setSelection(
                    binding.editSearch
                        .text
                        .length
                )
            }
    }

    private fun mostrarDestaques() {

        val destaques =
            catalogoLocal.take(
                4
            )

        listaAtual =
            destaques

        binding.textSecao.text =
            "Filmes em destaque"

        adapter.atualizarLista(
            destaques
        )

        binding.textVazio.text =
            "Nenhum filme disponível no catálogo."

        binding.textVazio.visibility =
            if (
                destaques.isEmpty()
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }

        binding.recyclerFilmes.post {

            ajustarAlturaDosCards()
        }
    }

    private fun mostrarPreviaDaPesquisa(
        termo: String
    ) {

        val resultadosLocais =
            filtrarFilmes(
                fonte =
                    catalogoLocal,
                termo =
                    termo
            )

        listaAtual =
            resultadosLocais

        binding.textSecao.text =
            "Resultados locais"

        adapter.atualizarLista(
            resultadosLocais
        )

        if (
            resultadosLocais.isEmpty()
        ) {

            binding.textVazio.text =
                if (
                    termo.length >=
                    MIN_CARACTERES_PESQUISA
                ) {

                    "Buscando automaticamente na internet..."

                } else {

                    "Continue digitando para pesquisar."
                }

            binding.textVazio.visibility =
                View.VISIBLE

        } else {

            binding.textVazio.visibility =
                View.GONE
        }
    }

    private fun pesquisarNoTmdb(
        termo: String,
        pesquisaAutomatica: Boolean
    ) {

        val apiKey =
            BuildConfig
                .TMDB_API_KEY
                .trim()

        if (
            apiKey.isBlank()
        ) {

            if (
                !pesquisaAutomatica
            ) {

                Toast.makeText(
                    this,
                    R.string.api_tmdb_nao_configurada,
                    Toast.LENGTH_LONG
                ).show()
            }

            return
        }

        binding.textSecao.text =
            "Buscando \"$termo\"..."

        if (
            listaAtual.isEmpty()
        ) {

            binding.textVazio.text =
                getString(
                    R.string.buscando_filmes
                )

            binding.textVazio.visibility =
                View.VISIBLE

        } else {

            binding.textVazio.visibility =
                View.GONE
        }

        Thread {

            try {

                val filmesEncontrados =
                    TmdbRepository
                        .buscarFilmes(
                            apiKey =
                                apiKey,
                            termo =
                                termo
                        )

                runOnUiThread {

                    if (
                        isFinishing ||
                        isDestroyed
                    ) {

                        return@runOnUiThread
                    }

                    val textoAtual =
                        binding.editSearch
                            .text
                            .toString()
                            .trim()

                    if (
                        textoAtual != termo
                    ) {

                        return@runOnUiThread
                    }

                    listaAtual =
                        filmesEncontrados

                    MovieRepository
                        .registrarFilmesOnline(
                            filmesEncontrados
                        )

                    if (
                        pesquisaAutomatica
                    ) {

                        PreferencesManager
                            .setUltimaPesquisa(
                                this,
                                termo
                            )

                        atualizarTextoUltimaPesquisa(
                            termo
                        )
                    }

                    mostrarFilmes(
                        filmes =
                            filmesEncontrados,
                        tituloSecao =
                            "Resultados para \"$termo\""
                    )
                }

            } catch (
                e: Exception
            ) {

                Log.e(
                    "CineFlixTMDB",
                    "Erro ao consultar o TMDB",
                    e
                )

                runOnUiThread {

                    if (
                        isFinishing ||
                        isDestroyed
                    ) {

                        return@runOnUiThread
                    }

                    val textoAtual =
                        binding.editSearch
                            .text
                            .toString()
                            .trim()

                    if (
                        textoAtual != termo
                    ) {

                        return@runOnUiThread
                    }

                    val resultadosLocais =
                        filtrarFilmes(
                            fonte =
                                catalogoLocal,
                            termo =
                                termo
                        )

                    listaAtual =
                        resultadosLocais

                    adapter.atualizarLista(
                        resultadosLocais
                    )

                    binding.textSecao.text =
                        "Resultados locais"

                    binding.textVazio.text =
                        if (
                            resultadosLocais.isEmpty()
                        ) {

                            "Não foi possível consultar os resultados on-line."

                        } else {

                            getString(
                                R.string.erro_busca_tmdb
                            )
                        }

                    binding.textVazio.visibility =
                        if (
                            resultadosLocais.isEmpty()
                        ) {

                            View.VISIBLE

                        } else {

                            View.GONE
                        }

                    if (
                        !pesquisaAutomatica
                    ) {

                        Toast.makeText(
                            this,
                            R.string.erro_busca_tmdb,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        }.start()
    }

    private fun mostrarFilmes(
        filmes: List<Movie>,
        tituloSecao: String
    ) {

        adapter.atualizarLista(
            filmes
        )

        binding.textSecao.text =
            tituloSecao

        binding.textVazio.text =
            getString(
                R.string.nenhum_filme_encontrado
            )

        binding.textVazio.visibility =
            if (
                filmes.isEmpty()
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }
    }

    private fun filtrarFilmes(
        fonte: List<Movie>,
        termo: String
    ): List<Movie> {

        val termoNormalizado =
            HelperMethods.normalizarTexto(
                termo
            )

        if (
            termoNormalizado.isBlank()
        ) {

            return fonte
        }

        return fonte.filter {
                filme ->

            HelperMethods
                .normalizarTexto(
                    filme.titulo
                )
                .contains(
                    termoNormalizado
                ) ||
                    HelperMethods
                        .normalizarTexto(
                            filme.generos
                        )
                        .contains(
                            termoNormalizado
                        )
        }
    }

    private fun abrirDetalhes(
        filme: Movie
    ) {

        val intent =
            Intent(
                this,
                DetalhesActivity::class.java
            )

        intent.putExtra(
            "movie_id",
            filme.id
        )

        startActivity(
            intent
        )
    }

    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean {

        menuInflater.inflate(
            R.menu.menu_main,
            menu
        )

        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {

        return when (
            item.itemId
        ) {

            R.id.action_favoritos -> {

                startActivity(
                    Intent(
                        this,
                        FavoritosActivity::class.java
                    )
                )

                true
            }

            R.id.action_galeria -> {

                startActivity(
                    Intent(
                        this,
                        GaleriaActivity::class.java
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

            /*
             * Nova opção do WebView.
             */
            R.id.action_ajuda -> {

                startActivity(
                    Intent(
                        this,
                        AjudaActivity::class.java
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

            else ->

                super.onOptionsItemSelected(
                    item
                )
        }
    }

    override fun onDestroy() {

        cancelarPesquisaAutomatica()

        super.onDestroy()
    }
}