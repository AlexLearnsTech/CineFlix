package com.example.cineflix

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Application customizada: aplica o tema (claro/escuro) salvo pelo usuário
 * assim que o app é iniciado, antes de qualquer Activity ser criada.
 */
class CineFlixApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val temaEscuro = PreferencesManager.isTemaEscuro(this)
        AppCompatDelegate.setDefaultNightMode(
            if (temaEscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
