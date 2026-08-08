package com.example.cineflix

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cineflix.databinding.ActivityConfiguracoesBinding

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Tema escuro
        binding.switchTemaEscuro.isChecked = PreferencesManager.isTemaEscuro(this)
        binding.switchTemaEscuro.setOnCheckedChangeListener { _, ativo ->
            PreferencesManager.setTemaEscuro(this, ativo)
            AppCompatDelegate.setDefaultNightMode(
                if (ativo) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Ordenação de favoritos
        when (PreferencesManager.getOrdenacao(this)) {
            0 -> binding.radioNome.isChecked = true
            1 -> binding.radioAno.isChecked = true
            2 -> binding.radioNota.isChecked = true
        }
        binding.radioGroupOrdenacao.setOnCheckedChangeListener { _, checkedId ->
            val tipo = when (checkedId) {
                R.id.radioAno -> 1
                R.id.radioNota -> 2
                else -> 0
            }
            PreferencesManager.setOrdenacao(this, tipo)
        }

        // Histórico de pesquisas (armazenamento interno)
        binding.buttonVerHistorico.setOnClickListener {
            val historico = HistoricoManager.listar(this)
            val texto = if (historico.isEmpty()) {
                getString(R.string.sem_historico)
            } else {
                historico.joinToString("\n")
            }
            AlertDialog.Builder(this)
                .setTitle(R.string.historico_pesquisas)
                .setMessage(texto)
                .setPositiveButton(R.string.ok, null)
                .show()
        }

        binding.buttonLimparHistorico.setOnClickListener {
            HistoricoManager.limpar(this)
            Toast.makeText(this, R.string.historico_limpo, Toast.LENGTH_SHORT).show()
        }

        binding.textVersao.text = getString(R.string.versao_app, obterVersao())
    }

    private fun obterVersao(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
