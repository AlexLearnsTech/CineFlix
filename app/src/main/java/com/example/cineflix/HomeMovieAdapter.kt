package com.example.cineflix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

/**
 * Adapter exclusivo da tela inicial do CineFlix.
 *
 * Apresenta os filmes em formato de grade
 * e permite ajustar dinamicamente a altura
 * dos cartões conforme o espaço da tela.
 */
class HomeMovieAdapter(
    private var filmes: List<Movie>,
    private val onClick: (Movie) -> Unit
) : RecyclerView.Adapter<HomeMovieAdapter.HomeMovieViewHolder>() {

    private var alturaItem: Int? = null

    class HomeMovieViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val imagePoster: ImageView =
            itemView.findViewById(
                R.id.imagePosterHome
            )

        val textTitulo: TextView =
            itemView.findViewById(
                R.id.textTituloHome
            )

        val textInfo: TextView =
            itemView.findViewById(
                R.id.textInfoHome
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeMovieViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_home_movie,
                    parent,
                    false
                )

        return HomeMovieViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: HomeMovieViewHolder,
        position: Int
    ) {

        val filme =
            filmes[position]

        /*
         * Aplica a altura calculada pela MainActivity.
         */
        alturaItem?.let { altura ->

            val parametros =
                holder.itemView.layoutParams

            parametros.height =
                altura

            holder.itemView.layoutParams =
                parametros
        }

        holder.textTitulo.text =
            filme.titulo

        holder.textInfo.text =
            "${filme.ano} • ⭐ ${filme.nota}"

        holder.imagePoster.load(
            filme.posterUrl
        ) {

            crossfade(true)

            placeholder(
                R.drawable.ic_launcher
            )

            error(
                R.drawable.ic_launcher
            )
        }

        holder.itemView.setOnClickListener {

            onClick(
                filme
            )
        }
    }

    override fun getItemCount(): Int {

        return filmes.size
    }

    /**
     * Atualiza os filmes apresentados.
     */
    fun atualizarLista(
        novaLista: List<Movie>
    ) {

        filmes =
            novaLista

        notifyDataSetChanged()
    }

    /**
     * Define a altura de cada cartão
     * na tela inicial.
     */
    fun definirAlturaItem(
        altura: Int
    ) {

        if (altura <= 0) {
            return
        }

        alturaItem =
            altura

        notifyDataSetChanged()
    }
}