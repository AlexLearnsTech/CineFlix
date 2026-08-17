package com.example.cineflix

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.cineflix.databinding.ActivityConfiguracoesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityConfiguracoesBinding

    private var syncService:
            CineFlixSyncService? =
        null

    private var serviceBound =
        false

    /**
     * Comunicação entre a Activity e o Bound Service.
     */
    private val serviceConnection =
        object : ServiceConnection {

            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {

                val binder =
                    service as?
                            CineFlixSyncService.LocalBinder

                syncService =
                    binder?.getService()

                serviceBound =
                    syncService != null

                syncService
                    ?.definirListener {
                            estado ->

                        atualizarEstadoSincronizacao(
                            estado
                        )
                    }

                syncService
                    ?.obterEstado()
                    ?.let {
                            estado ->

                        atualizarEstadoSincronizacao(
                            estado
                        )
                    }
            }

            override fun onServiceDisconnected(
                name: ComponentName?
            ) {

                syncService =
                    null

                serviceBound =
                    false

                mostrarEstadoSalvo()
            }
        }

    /**
     * Permissão de notificações para Android 13+.
     */
    private val solicitarPermissaoNotificacao =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            /*
             * O Foreground Service pode ser iniciado
             * mesmo se o usuário optar por não mostrar
             * a notificação na gaveta do sistema.
             */
            iniciarServicoSincronizacao()
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityConfiguracoesBinding
                .inflate(
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


        // =====================================
        // Tema escuro
        // =====================================

        binding
            .switchTemaEscuro
            .isChecked =

            PreferencesManager
                .isTemaEscuro(
                    this
                )

        binding
            .switchTemaEscuro
            .setOnCheckedChangeListener {
                    _,
                    ativo ->

                PreferencesManager
                    .setTemaEscuro(
                        this,
                        ativo
                    )

                AppCompatDelegate
                    .setDefaultNightMode(

                        if (ativo) {

                            AppCompatDelegate
                                .MODE_NIGHT_YES

                        } else {

                            AppCompatDelegate
                                .MODE_NIGHT_NO
                        }
                    )
            }


        // =====================================
        // Ordenação de favoritos
        // =====================================

        when (
            PreferencesManager
                .getOrdenacao(
                    this
                )
        ) {

            0 ->
                binding
                    .radioNome
                    .isChecked =
                    true

            1 ->
                binding
                    .radioAno
                    .isChecked =
                    true

            2 ->
                binding
                    .radioNota
                    .isChecked =
                    true
        }

        binding
            .radioGroupOrdenacao
            .setOnCheckedChangeListener {
                    _,
                    checkedId ->

                val tipo =
                    when (
                        checkedId
                    ) {

                        R.id.radioAno ->
                            1

                        R.id.radioNota ->
                            2

                        else ->
                            0
                    }

                PreferencesManager
                    .setOrdenacao(
                        this,
                        tipo
                    )
            }


        // =====================================
        // Histórico de pesquisas
        // =====================================

        binding
            .buttonVerHistorico
            .setOnClickListener {

                val historico =
                    HistoricoManager
                        .listar(
                            this
                        )

                val texto =
                    if (
                        historico.isEmpty()
                    ) {

                        getString(
                            R.string.sem_historico
                        )

                    } else {

                        historico
                            .joinToString(
                                "\n"
                            )
                    }

                AlertDialog
                    .Builder(
                        this
                    )
                    .setTitle(
                        R.string.historico_pesquisas
                    )
                    .setMessage(
                        texto
                    )
                    .setPositiveButton(
                        R.string.ok,
                        null
                    )
                    .show()
            }

        binding
            .buttonLimparHistorico
            .setOnClickListener {

                HistoricoManager
                    .limpar(
                        this
                    )

                Toast
                    .makeText(
                        this,
                        R.string.historico_limpo,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }


        // =====================================
        // Módulo 8 - Serviço
        // =====================================

        mostrarEstadoSalvo()

        binding
            .buttonControleSincronizacao
            .setOnClickListener {

                val servico =
                    syncService

                if (
                    servico
                        ?.estaAtivo() ==
                    true
                ) {

                    servico
                        .pararSincronizacao()

                } else {

                    prepararInicioSincronizacao()
                }
            }


        // =====================================
        // Versão
        // =====================================

        binding
            .textVersao
            .text =

            getString(
                R.string.versao_app,
                obterVersao()
            )
    }

    /**
     * Verifica os dados necessários antes de iniciar o serviço.
     */
    private fun prepararInicioSincronizacao() {

        val termo =
            PreferencesManager
                .getUltimaPesquisa(
                    this
                )
                .trim()

        if (termo.isBlank()) {

            Toast
                .makeText(
                    this,
                    R.string.sync_sem_pesquisa,
                    Toast.LENGTH_LONG
                )
                .show()

            return
        }

        if (
            BuildConfig
                .TMDB_API_KEY
                .trim()
                .isBlank()
        ) {

            Toast
                .makeText(
                    this,
                    R.string.api_tmdb_nao_configurada,
                    Toast.LENGTH_LONG
                )
                .show()

            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission
                        .POST_NOTIFICATIONS
                ) !=
            PackageManager
                .PERMISSION_GRANTED
        ) {

            solicitarPermissaoNotificacao
                .launch(
                    Manifest.permission
                        .POST_NOTIFICATIONS
                )

        } else {

            iniciarServicoSincronizacao()
        }
    }

    private fun iniciarServicoSincronizacao() {

        val intent =
            Intent(
                this,
                CineFlixSyncService::class.java
            ).apply {

                action =
                    CineFlixSyncService
                        .ACTION_START
            }

        ContextCompat
            .startForegroundService(
                this,
                intent
            )
    }

    /**
     * Atualiza a tela sempre que o Service envia
     * novas informações através do Binder.
     */
    private fun atualizarEstadoSincronizacao(
        estado:
        CineFlixSyncService.EstadoSincronizacao
    ) {

        val textoStatus =
            if (
                estado.ativo
            ) {

                getString(
                    R.string.status_ativo
                )

            } else {

                getString(
                    R.string.status_inativo
                )
            }

        binding
            .textStatusSincronizacao
            .text =

            getString(
                R.string.status_sincronizacao,
                textoStatus
            )

        val termo =
            estado.termo
                .ifBlank {

                    PreferencesManager
                        .getUltimaPesquisa(
                            this
                        )
                }
                .ifBlank {

                    getString(
                        R.string.nenhuma_pesquisa
                    )
                }

        binding
            .textTermoSincronizacao
            .text =

            getString(
                R.string.pesquisa_monitorada,
                termo
            )

        val textoData =
            if (
                estado.ultimaAtualizacao > 0L
            ) {

                formatarDataHora(
                    estado.ultimaAtualizacao
                )

            } else {

                getString(
                    R.string.nenhuma_atualizacao
                )
            }

        binding
            .textUltimaSincronizacao
            .text =

            getString(
                R.string.ultima_atualizacao,
                textoData
            )

        binding
            .textResultadoSincronizacao
            .text =

            getString(
                R.string.resultado_sincronizacao,
                estado.mensagem
            )

        binding
            .buttonControleSincronizacao
            .setText(

                if (
                    estado.ativo
                ) {

                    R.string.parar_atualizacao

                } else {

                    R.string.iniciar_atualizacao
                }
            )
    }

    /**
     * Exibe as informações salvas mesmo quando
     * o serviço ainda não está conectado à Activity.
     */
    private fun mostrarEstadoSalvo() {

        val ultimaPesquisa =
            PreferencesManager
                .getUltimaPesquisa(
                    this
                )
                .trim()

        val ultimoTermo =
            PreferencesManager
                .getUltimoTermoSincronizado(
                    this
                )
                .trim()

        val termo =
            ultimaPesquisa
                .ifBlank {
                    ultimoTermo
                }
                .ifBlank {
                    getString(
                        R.string.nenhuma_pesquisa
                    )
                }

        val ultimaSincronizacao =
            PreferencesManager
                .getUltimaSincronizacao(
                    this
                )

        val total =
            PreferencesManager
                .getUltimoTotalSincronizado(
                    this
                )

        binding
            .textStatusSincronizacao
            .text =

            getString(
                R.string.status_sincronizacao,
                getString(
                    R.string.status_inativo
                )
            )

        binding
            .textTermoSincronizacao
            .text =

            getString(
                R.string.pesquisa_monitorada,
                termo
            )

        if (
            ultimaSincronizacao > 0L
        ) {

            binding
                .textUltimaSincronizacao
                .text =

                getString(
                    R.string.ultima_atualizacao,
                    formatarDataHora(
                        ultimaSincronizacao
                    )
                )

            binding
                .textResultadoSincronizacao
                .text =

                getString(
                    R.string.resultado_sincronizacao,
                    getString(
                        R.string.sync_sucesso,
                        total
                    )
                )

        } else {

            binding
                .textUltimaSincronizacao
                .text =

                getString(
                    R.string.ultima_atualizacao,
                    getString(
                        R.string.nenhuma_atualizacao
                    )
                )

            binding
                .textResultadoSincronizacao
                .text =

                getString(
                    R.string.resultado_sincronizacao,
                    getString(
                        R.string.sync_aguardando_inicio
                    )
                )
        }

        binding
            .buttonControleSincronizacao
            .setText(
                R.string.iniciar_atualizacao
            )
    }

    private fun formatarDataHora(
        momento: Long
    ): String {

        val formato =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss",
                Locale(
                    "pt",
                    "BR"
                )
            )

        return formato
            .format(
                Date(
                    momento
                )
            )
    }

    private fun obterVersao(): String {

        return try {

            packageManager
                .getPackageInfo(
                    packageName,
                    0
                )
                .versionName
                ?: "1.0"

        } catch (
            _: Exception
        ) {

            "1.0"
        }
    }

    /**
     * Faz o bind quando a tela de configurações
     * fica visível.
     */
    override fun onStart() {

        super.onStart()

        val intent =
            Intent(
                this,
                CineFlixSyncService::class.java
            )

        bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    /**
     * Remove a conexão da Activity sem necessariamente
     * encerrar o serviço iniciado.
     */
    override fun onStop() {

        if (
            serviceBound
        ) {

            syncService
                ?.definirListener(
                    null
                )

            unbindService(
                serviceConnection
            )

            serviceBound =
                false

            syncService =
                null
        }

        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher
            .onBackPressed()

        return true
    }
}