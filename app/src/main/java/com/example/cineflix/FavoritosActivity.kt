package com.example.cineflix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cineflix.databinding.ActivityFavoritosBinding

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: MovieAdapter

    // Mantém a lista exatamente como está sendo exibida no adapter, para que o
    // swipe-to-delete remova sempre o item correto (evita divergência de índices).
    private var listaAtual: List<Movie> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)

        adapter = MovieAdapter(
            emptyList(),
            onClick = { filme ->
                val intent = Intent(this, DetalhesActivity::class.java)
                intent.putExtra("movie_id", filme.id)
                startActivity(intent)
            },
            onRemoveClick = { filme ->
                dbHelper.removerFavorito(filme.id)
                carregarFavoritos()
            }
        )

        binding.recyclerFavoritos.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavoritos.adapter = adapter

        val swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicao = viewHolder.bindingAdapterPosition
                if (posicao != RecyclerView.NO_POSITION && posicao < listaAtual.size) {
                    val filme = listaAtual[posicao]
                    dbHelper.removerFavorito(filme.id)
                    carregarFavoritos()
                }
            }
        })
        swipeHelper.attachToRecyclerView(binding.recyclerFavoritos)

        binding.buttonExportar.setOnClickListener {
            exportarFavoritos()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarFavoritos()
    }

    private fun carregarFavoritos() {
        val ordenacao = PreferencesManager.getOrdenacao(this)
        var lista = dbHelper.listarFavoritos()
        lista = when (ordenacao) {
            1 -> lista.sortedBy { it.ano }
            2 -> lista.sortedByDescending { it.nota }
            else -> lista.sortedBy { it.titulo }
        }
        listaAtual = lista
        adapter.atualizarLista(lista)
        binding.textVazioFavoritos.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        binding.buttonExportar.isEnabled = lista.isNotEmpty()
    }

    private fun exportarFavoritos() {
        val favoritos = listaAtual
        if (favoritos.isEmpty()) {
            Toast.makeText(this, R.string.nenhum_favorito_exportar, Toast.LENGTH_SHORT).show()
            return
        }
        val arquivo = ExportManager.exportarFavoritos(this, favoritos)
        if (arquivo != null) {
            Toast.makeText(
                this,
                getString(R.string.exportado_com_sucesso, arquivo.absolutePath),
                Toast.LENGTH_LONG
            ).show()
            ExportManager.compartilharArquivo(this, arquivo)
        } else {
            Toast.makeText(this, R.string.erro_exportar, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
