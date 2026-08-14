package com.example.cineflix

import android.content.Context
import java.text.Normalizer
import java.util.Locale

/**
 * Métodos auxiliares reutilizáveis do CineFlix.
 *
 * Esta classe centraliza funções que podem ser
 * utilizadas por diferentes partes do aplicativo,
 * evitando repetição de código.
 */
object HelperMethods {

    /**
     * Normaliza textos para facilitar pesquisas.
     *
     * Remove acentos, converte para letras minúsculas
     * e elimina espaços extras no início e no final.
     *
     * Exemplo:
     * "AÇÃO" -> "acao"
     */
    fun normalizarTexto(
        texto: String
    ): String {

        return Normalizer
            .normalize(
                texto,
                Normalizer.Form.NFD
            )
            .replace(
                "\\p{M}+".toRegex(),
                ""
            )
            .lowercase(
                Locale.ROOT
            )
            .trim()
    }

    /**
     * Converte um valor em dp para pixels
     * de acordo com a densidade da tela.
     */
    fun dpParaPixels(
        context: Context,
        dp: Int
    ): Int {

        return (
                dp *
                        context.resources
                            .displayMetrics
                            .density
                ).toInt()
    }
}