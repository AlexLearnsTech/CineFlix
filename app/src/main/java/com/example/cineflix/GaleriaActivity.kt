package com.example.cineflix

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.ImageSwitcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import coil.load

class GaleriaActivity : AppCompatActivity() {

    private lateinit var imageSwitcher: ImageSwitcher
    private lateinit var gridFilmes: GridView
    private lateinit var textFilmeSelecionado: TextView
    private lateinit var btnAnterior: Button
    private lateinit var btnProximo: Button

    private lateinit var dbHelper: DatabaseHelper

    private var filmes: List<Movie> = emptyList()

    private var indiceAtual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_galeria)

        val toolbar =
            findViewById<Toolbar>(
                R.id.toolbarGaleria
            )

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Galeria de Filmes"
            setDisplayHomeAsUpEnabled(true)
        }

        imageSwitcher =
            findViewById(
                R.id.imageSwitcherFilmes
            )

        gridFilmes =
            findViewById(
                R.id.gridFilmes
            )

        textFilmeSelecionado =
            findViewById(
                R.id.textFilmeSelecionado
            )

        btnAnterior =
            findViewById(
                R.id.btnAnterior
            )

        btnProximo =
            findViewById(
                R.id.btnProximo
            )

        /*
         * Banco SQLite utilizado pelos favoritos.
         */
        dbHelper =
            DatabaseHelper(this)

        configurarImageSwitcher()

        carregarFilmes()

        configurarBotoes()

        configurarGridView()

        configurarCliqueImagem()

        /*
         * Registra o GridView para permitir
         * a exibição de um menu de contexto
         * ao pressionar e segurar um filme.
         */
        registerForContextMenu(
            gridFilmes
        )
    }

    /**
     * Configura o ImageSwitcher.
     */
    private fun configurarImageSwitcher() {

        imageSwitcher.setFactory {

            ImageView(this).apply {

                scaleType =
                    ImageView.ScaleType.CENTER_CROP

                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                contentDescription =
                    "Pôster do filme selecionado"
            }
        }

        val animacaoEntrada =
            AnimationUtils.loadAnimation(
                this,
                android.R.anim.fade_in
            )

        val animacaoSaida =
            AnimationUtils.loadAnimation(
                this,
                android.R.anim.fade_out
            )

        imageSwitcher.inAnimation =
            animacaoEntrada

        imageSwitcher.outAnimation =
            animacaoSaida
    }

    /**
     * Carrega o catálogo local do CineFlix.
     */
    private fun carregarFilmes() {

        filmes =
            MovieRepository.carregarFilmes(
                this
            )

        if (filmes.isEmpty()) {

            Toast.makeText(
                this,
                "Nenhum filme disponível na galeria.",
                Toast.LENGTH_LONG
            ).show()

            btnAnterior.isEnabled = false
            btnProximo.isEnabled = false

            return
        }

        gridFilmes.adapter =
            GaleriaAdapter(
                this,
                filmes
            )

        mostrarFilme(
            novaPosicao = 0,
            direcao = 0
        )
    }

    /**
     * Permite selecionar um filme
     * diretamente no GridView.
     */
    private fun configurarGridView() {

        gridFilmes.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            if (
                position ==
                indiceAtual
            ) {

                return@setOnItemClickListener
            }

            val direcao =
                if (
                    position >
                    indiceAtual
                ) {

                    1

                } else {

                    -1
                }

            mostrarFilme(
                novaPosicao = position,
                direcao = direcao
            )
        }
    }

    /**
     * Configura os botões Anterior e Próximo.
     */
    private fun configurarBotoes() {

        btnAnterior.setOnClickListener {

            if (
                filmes.isEmpty()
            ) {

                return@setOnClickListener
            }

            val novaPosicao =
                if (
                    indiceAtual - 1 < 0
                ) {

                    filmes.lastIndex

                } else {

                    indiceAtual - 1
                }

            mostrarFilme(
                novaPosicao = novaPosicao,
                direcao = -1
            )
        }

        btnProximo.setOnClickListener {

            if (
                filmes.isEmpty()
            ) {

                return@setOnClickListener
            }

            val novaPosicao =
                if (
                    indiceAtual + 1 >
                    filmes.lastIndex
                ) {

                    0

                } else {

                    indiceAtual + 1
                }

            mostrarFilme(
                novaPosicao = novaPosicao,
                direcao = 1
            )
        }
    }

    /**
     * Mostra o filme selecionado
     * no ImageSwitcher.
     */
    private fun mostrarFilme(
        novaPosicao: Int,
        direcao: Int
    ) {

        if (
            filmes.isEmpty()
        ) {

            return
        }

        indiceAtual =
            novaPosicao

        when {

            direcao > 0 -> {

                imageSwitcher.showNext()
            }

            direcao < 0 -> {

                imageSwitcher.showPrevious()
            }
        }

        val filme =
            filmes[
                indiceAtual
            ]

        val imageView =
            imageSwitcher.currentView
                    as ImageView

        imageView.load(
            filme.posterUrl
        ) {

            crossfade(true)

            placeholder(
                R.mipmap.ic_launcher
            )

            error(
                R.mipmap.ic_launcher
            )
        }

        textFilmeSelecionado.text =
            "${filme.titulo} (${filme.ano})"

        gridFilmes.setSelection(
            indiceAtual
        )
    }

    /**
     * Ao tocar no pôster principal,
     * abre a tela de detalhes.
     */
    private fun configurarCliqueImagem() {

        imageSwitcher.setOnClickListener {

            if (
                filmes.isEmpty()
            ) {

                return@setOnClickListener
            }

            abrirDetalhes(
                filmes[indiceAtual]
            )
        }
    }

    /**
     * Abre a tela de detalhes do filme.
     */
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

    /*
     * ==========================================================
     * MENU DE CONTEXTO
     * ==========================================================
     */

    /**
     * Cria o menu exibido quando
     * o usuário pressiona e segura
     * um filme do GridView.
     */
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {

        super.onCreateContextMenu(
            menu,
            v,
            menuInfo
        )

        if (
            v.id !=
            R.id.gridFilmes
        ) {

            return
        }

        val info =
            menuInfo as? AdapterView.AdapterContextMenuInfo
                ?: return

        val filme =
            filmes.getOrNull(
                info.position
            ) ?: return

        menu.setHeaderTitle(
            filme.titulo
        )

        menuInflater.inflate(
            R.menu.menu_contexto_filme,
            menu
        )

        /*
         * Altera o texto da opção
         * dependendo do estado atual
         * do filme no banco de favoritos.
         */
        val itemFavorito =
            menu.findItem(
                R.id.action_contexto_favorito
            )

        itemFavorito.title =
            if (
                dbHelper.isFavorito(
                    filme.id
                )
            ) {

                "Remover dos favoritos"

            } else {

                "Adicionar aos favoritos"
            }
    }

    /**
     * Trata as opções escolhidas
     * no menu de contexto.
     */
    override fun onContextItemSelected(
        item: MenuItem
    ): Boolean {

        val info =
            item.menuInfo
                    as? AdapterView.AdapterContextMenuInfo
                ?: return super
                    .onContextItemSelected(
                        item
                    )

        val filme =
            filmes.getOrNull(
                info.position
            )
                ?: return super
                    .onContextItemSelected(
                        item
                    )

        return when (
            item.itemId
        ) {

            R.id.action_contexto_detalhes -> {

                abrirDetalhes(
                    filme
                )

                true
            }

            R.id.action_contexto_favorito -> {

                alternarFavorito(
                    filme
                )

                true
            }

            else -> {

                super.onContextItemSelected(
                    item
                )
            }
        }
    }

    /**
     * Adiciona ou remove o filme
     * dos favoritos.
     */
    private fun alternarFavorito(
        filme: Movie
    ) {

        if (
            dbHelper.isFavorito(
                filme.id
            )
        ) {

            dbHelper.removerFavorito(
                filme.id
            )

            Toast.makeText(
                this,
                "${filme.titulo} removido dos favoritos.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            dbHelper.adicionarFavorito(
                filme
            )

            Toast.makeText(
                this,
                "${filme.titulo} adicionado aos favoritos.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher
            .onBackPressed()

        return true
    }
}