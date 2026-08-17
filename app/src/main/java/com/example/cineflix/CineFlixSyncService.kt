package com.example.cineflix

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Serviço responsável por atualizar periodicamente os resultados
 * da última pesquisa realizada pelo usuário.
 *
 * O serviço:
 * - executa em primeiro plano com notificação;
 * - utiliza uma thread separada para a comunicação com o TMDB;
 * - executa a atualização de forma repetitiva;
 * - permite comunicação direta com a ConfiguracoesActivity por Binder.
 */
class CineFlixSyncService : Service() {

    data class EstadoSincronizacao(
        val ativo: Boolean,
        val termo: String,
        val ultimaAtualizacao: Long,
        val totalResultados: Int,
        val mensagem: String
    )

    inner class LocalBinder : Binder() {
        fun getService(): CineFlixSyncService {
            return this@CineFlixSyncService
        }
    }

    companion object {

        const val ACTION_START =
            "com.example.cineflix.action.START_SYNC"

        const val ACTION_STOP =
            "com.example.cineflix.action.STOP_SYNC"

        private const val TAG =
            "CineFlixSyncService"

        private const val CANAL_ID =
            "cineflix_sync_channel"

        private const val NOTIFICACAO_ID =
            8001

        /*
         * Intervalo curto para permitir a demonstração acadêmica
         * da tarefa repetitiva durante os testes do Módulo 8.
         */
        private const val INTERVALO_SEGUNDOS =
            60L
    }

    private val binder =
        LocalBinder()

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor()

    private var tarefaAgendada: ScheduledFuture<*>? =
        null

    @Volatile
    private var ativo =
        false

    @Volatile
    private var termoAtual =
        ""

    @Volatile
    private var ultimaAtualizacao =
        0L

    @Volatile
    private var totalResultados =
        0

    @Volatile
    private var mensagemAtual =
        ""

    private var listener:
            ((EstadoSincronizacao) -> Unit)? =
        null

    override fun onCreate() {
        super.onCreate()

        criarCanalNotificacao()

        ultimaAtualizacao =
            PreferencesManager
                .getUltimaSincronizacao(this)

        totalResultados =
            PreferencesManager
                .getUltimoTotalSincronizado(this)

        termoAtual =
            PreferencesManager
                .getUltimoTermoSincronizado(this)
                .ifBlank {
                    PreferencesManager
                        .getUltimaPesquisa(this)
                }

        mensagemAtual =
            getString(
                R.string.sync_aguardando_inicio
            )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_START -> {
                iniciarSincronizacao()
            }

            ACTION_STOP -> {
                pararSincronizacao()
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Permite que a Activity acompanhe mudanças no estado do serviço.
     */
    fun definirListener(
        novoListener:
        ((EstadoSincronizacao) -> Unit)?
    ) {
        listener = novoListener

        if (novoListener != null) {
            notificarEstado()
        }
    }

    fun estaAtivo(): Boolean {
        return ativo
    }

    fun obterEstado(): EstadoSincronizacao {

        return EstadoSincronizacao(
            ativo = ativo,
            termo = termoAtual,
            ultimaAtualizacao = ultimaAtualizacao,
            totalResultados = totalResultados,
            mensagem = mensagemAtual
        )
    }

    private fun iniciarSincronizacao() {

        if (ativo) {
            notificarEstado()
            return
        }

        ativo = true

        mensagemAtual =
            getString(
                R.string.sync_iniciando
            )

        iniciarComoForeground()

        notificarEstado()

        tarefaAgendada =
            executor.scheduleWithFixedDelay(
                {
                    executarAtualizacao()
                },
                0,
                INTERVALO_SEGUNDOS,
                TimeUnit.SECONDS
            )
    }

    /**
     * Executado na thread criada pelo ScheduledExecutorService.
     * Dessa forma a requisição de rede não bloqueia a interface.
     */
    private fun executarAtualizacao() {

        if (!ativo) {
            return
        }

        val termo =
            PreferencesManager
                .getUltimaPesquisa(this)
                .trim()

        if (termo.isBlank()) {

            termoAtual = ""

            mensagemAtual =
                getString(
                    R.string.sync_sem_pesquisa
                )

            notificarEstado()

            atualizarNotificacao(
                getString(
                    R.string.notificacao_sync_sem_pesquisa
                )
            )

            return
        }

        val apiKey =
            BuildConfig
                .TMDB_API_KEY
                .trim()

        if (apiKey.isBlank()) {

            termoAtual =
                termo

            mensagemAtual =
                getString(
                    R.string.sync_api_nao_configurada
                )

            notificarEstado()

            return
        }

        termoAtual =
            termo

        mensagemAtual =
            getString(
                R.string.sync_atualizando
            )

        notificarEstado()

        atualizarNotificacao(
            getString(
                R.string.notificacao_sync_termo,
                termo
            )
        )

        try {

            val filmes =
                TmdbRepository
                    .buscarFilmes(
                        apiKey = apiKey,
                        termo = termo
                    )

            if (!ativo) {
                return
            }

            MovieRepository
                .registrarFilmesOnline(
                    filmes
                )

            val momentoAtual =
                System.currentTimeMillis()

            ultimaAtualizacao =
                momentoAtual

            totalResultados =
                filmes.size

            termoAtual =
                termo

            PreferencesManager
                .salvarUltimaSincronizacao(
                    context = this,
                    momento = momentoAtual,
                    termo = termo,
                    totalResultados =
                        filmes.size
                )

            mensagemAtual =
                getString(
                    R.string.sync_sucesso,
                    filmes.size
                )

            atualizarNotificacao(
                getString(
                    R.string.notificacao_sync_sucesso,
                    filmes.size,
                    termo
                )
            )

            notificarEstado()

            Log.d(
                TAG,
                "Sincronização concluída: " +
                        "${filmes.size} resultado(s) " +
                        "para \"$termo\"."
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Erro durante a sincronização.",
                e
            )

            if (!ativo) {
                return
            }

            mensagemAtual =
                getString(
                    R.string.sync_erro
                )

            atualizarNotificacao(
                getString(
                    R.string.notificacao_sync_erro
                )
            )

            notificarEstado()
        }
    }

    fun pararSincronizacao() {

        tarefaAgendada
            ?.cancel(true)

        tarefaAgendada =
            null

        ativo =
            false

        mensagemAtual =
            getString(
                R.string.sync_parado
            )

        notificarEstado()

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        stopSelf()
    }

    private fun iniciarComoForeground() {

        val notificacao =
            criarNotificacao(
                getString(
                    R.string.notificacao_sync_preparando
                )
            )

        val tipoServico =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {
                ServiceInfo
                    .FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }

        ServiceCompat.startForeground(
            this,
            NOTIFICACAO_ID,
            notificacao,
            tipoServico
        )
    }

    private fun criarCanalNotificacao() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val canal =
            NotificationChannel(
                CANAL_ID,
                getString(
                    R.string.canal_sync_nome
                ),
                NotificationManager
                    .IMPORTANCE_LOW
            )

        canal.description =
            getString(
                R.string.canal_sync_descricao
            )

        val notificationManager =
            getSystemService(
                NotificationManager::class.java
            )

        notificationManager
            .createNotificationChannel(
                canal
            )
    }

    private fun criarNotificacao(
        texto: String
    ): Notification {

        val abrirConfiguracoes =
            Intent(
                this,
                ConfiguracoesActivity::class.java
            )

        val pendingAbrir =
            PendingIntent.getActivity(
                this,
                0,
                abrirConfiguracoes,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val pararServico =
            Intent(
                this,
                CineFlixSyncService::class.java
            ).apply {
                action =
                    ACTION_STOP
            }

        val pendingParar =
            PendingIntent.getService(
                this,
                1,
                pararServico,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat
            .Builder(
                this,
                CANAL_ID
            )
            .setSmallIcon(
                R.drawable.ic_launcher
            )
            .setContentTitle(
                getString(
                    R.string.notificacao_sync_titulo
                )
            )
            .setContentText(
                texto
            )
            .setContentIntent(
                pendingAbrir
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .addAction(
                0,
                getString(
                    R.string.parar_atualizacao
                ),
                pendingParar
            )
            .build()
    }

    private fun atualizarNotificacao(
        texto: String
    ) {

        try {

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.notify(
                NOTIFICACAO_ID,
                criarNotificacao(texto)
            )

        } catch (
            e: SecurityException
        ) {

            /*
             * Em Android 13 ou superior o usuário
             * pode negar a exibição de notificações.
             * O serviço continua sendo tratado pelo
             * sistema como Foreground Service.
             */
            Log.w(
                TAG,
                "Notificação não autorizada.",
                e
            )
        }
    }

    private fun notificarEstado() {

        val estado =
            obterEstado()

        mainHandler.post {

            listener
                ?.invoke(
                    estado
                )
        }
    }

    /**
     * Android 15+ pode encerrar um serviço dataSync
     * depois do limite permitido pelo sistema.
     */
    override fun onTimeout(
        startId: Int,
        fgsType: Int
    ) {

        Log.w(
            TAG,
            "Tempo máximo do serviço atingido."
        )

        pararSincronizacao()
    }

    override fun onDestroy() {

        tarefaAgendada
            ?.cancel(true)

        tarefaAgendada =
            null

        ativo =
            false

        executor
            .shutdownNow()

        listener =
            null

        super.onDestroy()
    }
}