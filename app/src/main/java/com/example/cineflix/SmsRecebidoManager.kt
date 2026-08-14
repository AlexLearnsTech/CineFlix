package com.example.cineflix

import android.content.Context

/**
 * Armazena o último SMS recebido pelo CineFlix.
 *
 * O armazenamento é feito em SharedPreferences
 * para que a mensagem continue disponível
 * mesmo depois que o aplicativo for fechado.
 */
object SmsRecebidoManager {

    private const val PREFS_SMS =
        "cineflix_sms_recebido"

    private const val KEY_REMETENTE =
        "remetente"

    private const val KEY_MENSAGEM =
        "mensagem"

    private const val KEY_HORARIO =
        "horario"

    /**
     * Representa um SMS recebido.
     */
    data class SmsRecebido(
        val remetente: String,
        val mensagem: String,
        val horario: Long
    )

    /**
     * Salva o último SMS recebido.
     */
    fun salvar(
        context: Context,
        remetente: String,
        mensagem: String
    ) {

        val preferences =
            context.getSharedPreferences(
                PREFS_SMS,
                Context.MODE_PRIVATE
            )

        preferences
            .edit()
            .putString(
                KEY_REMETENTE,
                remetente
            )
            .putString(
                KEY_MENSAGEM,
                mensagem
            )
            .putLong(
                KEY_HORARIO,
                System.currentTimeMillis()
            )
            .apply()
    }

    /**
     * Recupera o último SMS recebido.
     */
    fun obterUltimoSms(
        context: Context
    ): SmsRecebido? {

        val preferences =
            context.getSharedPreferences(
                PREFS_SMS,
                Context.MODE_PRIVATE
            )

        val mensagem =
            preferences.getString(
                KEY_MENSAGEM,
                null
            )
                ?: return null

        val remetente =
            preferences.getString(
                KEY_REMETENTE,
                "Número desconhecido"
            )
                ?: "Número desconhecido"

        val horario =
            preferences.getLong(
                KEY_HORARIO,
                0L
            )

        return SmsRecebido(
            remetente = remetente,
            mensagem = mensagem,
            horario = horario
        )
    }
}