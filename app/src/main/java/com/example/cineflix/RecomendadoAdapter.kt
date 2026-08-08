package com.example.cineflix

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cineflix.databinding.ItemRecomendadoBinding

/**
 * Adapter para o carrossel horizontal de filmes recomendados, exibido em DetalhesActivity.
 */
class RecomendadoAdapter(
    private val filmes: List<Movie>,
    private val onClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecomendadoAdapter.RecViewHolder>() {

    inner class RecViewHolder(val binding: ItemRecomendadoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val binding = ItemRecomendadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        val filme = filmes[position]
        val b = holder.binding
        b.textTituloRec.text = filme.titulo
        b.imagePosterRec.load(filme.posterUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher)
            error(R.drawable.ic_launcher)
        }
        b.root.setOnClickListener { onClick(filme) }
    }

    override fun getItemCount(): Int = filmes.size
}
