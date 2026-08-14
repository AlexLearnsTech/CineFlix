package com.example.cineflix

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AjudaActivity : AppCompatActivity() {

    private lateinit var webViewAjuda: WebView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_ajuda
        )

        val toolbar =
            findViewById<Toolbar>(
                R.id.toolbarAjuda
            )

        setSupportActionBar(
            toolbar
        )

        supportActionBar?.apply {

            title =
                "Sobre o CineFlix"

            setDisplayHomeAsUpEnabled(
                true
            )
        }

        webViewAjuda =
            findViewById(
                R.id.webViewAjuda
            )

        configurarWebView()
    }

    /**
     * Configura o WebView utilizado
     * na tela de ajuda do CineFlix.
     */
    private fun configurarWebView() {

        /*
         * Mantém a navegação dentro
         * do próprio WebView.
         */
        webViewAjuda.webViewClient =
            WebViewClient()

        /*
         * A página não utiliza JavaScript,
         * portanto ele permanece desativado.
         */
        webViewAjuda.settings
            .javaScriptEnabled =
            false

        /*
         * Carrega o arquivo HTML localizado
         * na pasta assets.
         */
        webViewAjuda.loadUrl(
            "file:///android_asset/ajuda.html"
        )
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher
            .onBackPressed()

        return true
    }

    override fun onDestroy() {

        webViewAjuda.destroy()

        super.onDestroy()
    }
}