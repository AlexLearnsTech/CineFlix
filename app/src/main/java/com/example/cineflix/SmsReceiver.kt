package com.example.cineflix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * BroadcastReceiver responsável
 * pelo recebimento de mensagens SMS.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        /*
         * Garante que estamos tratando
         * apenas o evento de SMS recebido.
         */
        if (
            intent.action !=
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {

            return
        }

        try {

            /*
             * Converte os dados recebidos pelo sistema
             * em objetos SmsMessage.
             *
             * Também funciona corretamente quando
             * uma mensagem é dividida em várias partes.
             */
            val mensagens =
                Telephony.Sms.Intents
                    .getMessagesFromIntent(
                        intent
                    )

            if (
                mensagens.isEmpty()
            ) {

                return
            }

            /*
             * Recupera o telefone de origem.
             */
            val remetente =
                mensagens
                    .firstOrNull()
                    ?.displayOriginatingAddress
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Número desconhecido"

            /*
             * Junta todas as partes do SMS
             * em uma única mensagem.
             */
            val corpoMensagem =
                mensagens.joinToString(
                    separator = ""
                ) {
                        sms ->

                    sms.displayMessageBody
                        .orEmpty()
                }

            if (
                corpoMensagem.isBlank()
            ) {

                return
            }

            /*
             * Salva a mensagem para que
             * possa ser mostrada no CineFlix.
             */
            SmsRecebidoManager.salvar(
                context = context,
                remetente = remetente,
                mensagem = corpoMensagem
            )

            Log.d(
                "CineFlixSMS",
                "SMS recebido de $remetente"
            )

        } catch (
            e: Exception
        ) {

            Log.e(
                "CineFlixSMS",
                "Erro ao processar SMS recebido.",
                e
            )
        }
    }
}