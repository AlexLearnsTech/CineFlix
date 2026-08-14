package com.example.cineflix

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.cineflix.databinding.ActivityDetalhesBinding

class DetalhesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesBinding
    private lateinit var dbHelper: DatabaseHelper

    private var filmeAtual: Movie? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityDetalhesBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setSupportActionBar(
            binding.toolbar
        )

        supportActionBar
            ?.setDisplayHomeAsUpEnabled(
                true
            )

        dbHelper =
            DatabaseHelper(
                this
            )

        val movieId =
            intent.getIntExtra(
                "movie_id",
                -1
            )

        val filme =
            MovieRepository.buscarPorId(
                this,
                movieId
            )

        if (
            filme == null
        ) {

            finish()

            return
        }

        filmeAtual =
            filme

        exibirFilme(
            filme
        )
    }

    private fun exibirFilme(
        filme: Movie
    ) {

        supportActionBar?.title =
            filme.titulo

        binding.imagePosterDetalhes
            .load(
                filme.posterUrl
            ) {

                crossfade(
                    true
                )

                placeholder(
                    R.mipmap.ic_launcher
                )

                error(
                    R.mipmap.ic_launcher
                )
            }

        binding.textTituloDetalhes.text =
            filme.titulo

        binding.textInfoDetalhes.text =
            getString(
                R.string.info_detalhes,
                filme.ano.toString(),
                filme.generos,
                filme.nota.toString()
            )

        binding.textSinopse.text =
            filme.sinopse

        atualizarBotaoFavorito()

        /*
         * Favoritos.
         */
        binding.buttonFavorito
            .setOnClickListener {

                if (
                    dbHelper.isFavorito(
                        filme.id
                    )
                ) {

                    dbHelper.removerFavorito(
                        filme.id
                    )

                } else {

                    dbHelper.adicionarFavorito(
                        filme
                    )
                }

                atualizarBotaoFavorito()
            }

        /*
         * Abre a tela de comunicação
         * enviando os dados do filme.
         */
        binding.buttonCompartilhar
            .setOnClickListener {

                abrirComunicacao(
                    filme
                )
            }

        val recomendados =
            filme.recomendados
                .mapNotNull {

                    MovieRepository
                        .buscarPorId(
                            this,
                            it
                        )
                }

        if (
            recomendados.isNotEmpty()
        ) {

            binding.textRecomendadosTitulo.visibility =
                View.VISIBLE

            binding.recyclerRecomendados.visibility =
                View.VISIBLE

            val adapterRec =
                RecomendadoAdapter(
                    recomendados
                ) {
                        novoFilme ->

                    val intent =
                        Intent(
                            this,
                            DetalhesActivity::class.java
                        )

                    intent.putExtra(
                        "movie_id",
                        novoFilme.id
                    )

                    startActivity(
                        intent
                    )
                }

            binding.recyclerRecomendados.layoutManager =
                LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            binding.recyclerRecomendados.adapter =
                adapterRec

        } else {

            binding.textRecomendadosTitulo.visibility =
                View.GONE

            binding.recyclerRecomendados.visibility =
                View.GONE
        }
    }

    /**
     * Abre a Activity responsável
     * pelas funcionalidades do Módulo 6.
     */
    private fun abrirComunicacao(
        filme: Movie
    ) {

        val intent =
            Intent(
                this,
                ComunicacaoActivity::class.java
            )

        intent.putExtra(
            "filme_titulo",
            filme.titulo
        )

        intent.putExtra(
            "filme_ano",
            filme.ano
        )

        intent.putExtra(
            "filme_nota",
            filme.nota
        )

        startActivity(
            intent
        )
    }

    private fun atualizarBotaoFavorito() {

        val filme =
            filmeAtual
                ?: return

        val favorito =
            dbHelper.isFavorito(
                filme.id
            )

        binding.buttonFavorito.text =
            if (
                favorito
            ) {

                getString(
                    R.string.remover_favoritos
                )

            } else {

                getString(
                    R.string.adicionar_favoritos
                )
            }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher
            .onBackPressed()

        return true
    }
}