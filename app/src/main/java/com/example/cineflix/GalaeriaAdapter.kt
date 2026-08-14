package com.example.cineflix

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load

class GaleriaAdapter(
    private val context: Context,
    private val filmes: List<Movie>
) : BaseAdapter() {

    override fun getCount(): Int {
        return filmes.size
    }

    override fun getItem(
        position: Int
    ): Movie {
        return filmes[position]
    }

    override fun getItemId(
        position: Int
    ): Long {
        return filmes[position]
            .id
            .toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val view: View

        val holder: ViewHolder

        if (convertView == null) {

            view =
                LayoutInflater
                    .from(context)
                    .inflate(
                        R.layout.item_galeria,
                        parent,
                        false
                    )

            holder =
                ViewHolder(
                    imagePoster =
                        view.findViewById(
                            R.id.imagePosterGaleria
                        ),

                    textTitulo =
                        view.findViewById(
                            R.id.textTituloGaleria
                        )
                )

            view.tag =
                holder

        } else {

            view =
                convertView

            holder =
                view.tag as ViewHolder
        }

        val filme =
            getItem(position)

        holder.textTitulo.text =
            filme.titulo

        holder.imagePoster.load(
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

        return view
    }

    private data class ViewHolder(
        val imagePoster: ImageView,
        val textTitulo: TextView
    )
}