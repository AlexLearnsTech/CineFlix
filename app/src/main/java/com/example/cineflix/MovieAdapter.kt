package com.example.cineflix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cineflix.databinding.ItemMovieBinding

/**
 * Adapter usado tanto na busca (MainActivity) quanto nos favoritos (FavoritosActivity).
 * Quando [onRemoveClick] é fornecido, exibe um botão de remoção em cada item.
 */
class MovieAdapter(
    private var filmes: List<Movie>,
    private val onClick: (Movie) -> Unit,
    private val onRemoveClick: ((Movie) -> Unit)? = null
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val filme = filmes[position]
        val b = holder.binding

        b.textTitulo.text = filme.titulo
        b.textInfo.text = "${filme.ano} • ${filme.generos} • ⭐ ${filme.nota}"
        b.imagePoster.load(filme.posterUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher)
            error(R.drawable.ic_launcher)
        }

        b.root.setOnClickListener { onClick(filme) }

        if (onRemoveClick != null) {
            b.buttonRemover.visibility = View.VISIBLE
            b.buttonRemover.setOnClickListener { onRemoveClick.invoke(filme) }
        } else {
            b.buttonRemover.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = filmes.size

    fun atualizarLista(novaLista: List<Movie>) {
        filmes = novaLista
        notifyDataSetChanged()
    }
}
