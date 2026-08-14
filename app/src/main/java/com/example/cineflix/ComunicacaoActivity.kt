package com.example.cineflix

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComunicacaoActivity : AppCompatActivity() {

    private lateinit var editTelefone: EditText
    private lateinit var editMensagem: EditText
    private lateinit var editEmail: EditText

    private lateinit var btnSmsDireto: Button
    private lateinit var btnAbrirSms: Button
    private lateinit var btnEnviarEmail: Button

    private lateinit var btnAtivarRecebimentoSms: Button
    private lateinit var textStatusRecebimentoSms: TextView
    private lateinit var textUltimoSmsRecebido: TextView

    /*
     * Permissão utilizada para o envio direto de SMS.
     */
    private val solicitarPermissaoSms =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedida ->

            if (concedida) {

                enviarSmsDireto()

            } else {

                Toast.makeText(
                    this,
                    "Permissão para enviar SMS não concedida.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    /*
     * Permissão utilizada para receber SMS.
     */
    private val solicitarPermissaoReceberSms =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedida ->

            if (concedida) {

                Toast.makeText(
                    this,
                    "Recebimento de SMS ativado.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Permissão para receber SMS não concedida.",
                    Toast.LENGTH_LONG
                ).show()
            }

            atualizarStatusRecebimentoSms()
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_comunicacao
        )

        val toolbar =
            findViewById<Toolbar>(
                R.id.toolbarComunicacao
            )

        setSupportActionBar(toolbar)

        supportActionBar?.apply {

            title = "Compartilhar filme"

            setDisplayHomeAsUpEnabled(true)
        }

        editTelefone =
            findViewById(
                R.id.editTelefone
            )

        editMensagem =
            findViewById(
                R.id.editMensagem
            )

        editEmail =
            findViewById(
                R.id.editEmail
            )

        btnSmsDireto =
            findViewById(
                R.id.btnSmsDireto
            )

        btnAbrirSms =
            findViewById(
                R.id.btnAbrirSms
            )

        btnEnviarEmail =
            findViewById(
                R.id.btnEnviarEmail
            )

        btnAtivarRecebimentoSms =
            findViewById(
                R.id.btnAtivarRecebimentoSms
            )

        textStatusRecebimentoSms =
            findViewById(
                R.id.textStatusRecebimentoSms
            )

        textUltimoSmsRecebido =
            findViewById(
                R.id.textUltimoSmsRecebido
            )

        preencherMensagemDoFilme()

        configurarBotoes()

        atualizarStatusRecebimentoSms()
    }

    /*
     * Preenche automaticamente a recomendação
     * do filme escolhido.
     */
    private fun preencherMensagemDoFilme() {

        val titulo =
            intent.getStringExtra(
                "filme_titulo"
            ) ?: "um filme"

        val ano =
            intent.getIntExtra(
                "filme_ano",
                0
            )

        val nota =
            intent.getDoubleExtra(
                "filme_nota",
                0.0
            )

        val mensagem =
            buildString {

                append(
                    "Recomendação do CineFlix: $titulo"
                )

                if (ano > 0) {

                    append(
                        " ($ano)"
                    )
                }

                if (nota > 0) {

                    append(
                        " • Nota: $nota"
                    )
                }

                append(
                    ". Vale a pena conferir!"
                )
            }

        editMensagem.setText(
            mensagem
        )
    }

    private fun configurarBotoes() {

        btnSmsDireto.setOnClickListener {

            verificarPermissaoSms()
        }

        btnAbrirSms.setOnClickListener {

            abrirAplicativoSms()
        }

        btnEnviarEmail.setOnClickListener {

            abrirAplicativoEmail()
        }

        btnAtivarRecebimentoSms.setOnClickListener {

            solicitarRecebimentoSms()
        }
    }

    /*
     * ==========================================================
     * SMS DIRETO
     * ==========================================================
     */

    private fun verificarPermissaoSms() {

        val telefone =
            obterTelefone()

        val mensagem =
            obterMensagem()

        if (
            telefone == null ||
            mensagem == null
        ) {

            return
        }

        val permissao =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )

        if (
            permissao ==
            PackageManager.PERMISSION_GRANTED
        ) {

            enviarSmsDireto()

        } else {

            solicitarPermissaoSms.launch(
                Manifest.permission.SEND_SMS
            )
        }
    }

    private fun enviarSmsDireto() {

        val telefone =
            obterTelefone()
                ?: return

        val mensagem =
            obterMensagem()
                ?: return

        try {

            val smsManager =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    getSystemService(
                        SmsManager::class.java
                    )

                } else {

                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

            val partes =
                smsManager.divideMessage(
                    mensagem
                )

            if (partes.size > 1) {

                smsManager.sendMultipartTextMessage(
                    telefone,
                    null,
                    partes,
                    null,
                    null
                )

            } else {

                smsManager.sendTextMessage(
                    telefone,
                    null,
                    mensagem,
                    null,
                    null
                )
            }

            Toast.makeText(
                this,
                "SMS enviado pelo CineFlix.",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: SecurityException) {

            Toast.makeText(
                this,
                "O aplicativo não possui permissão para enviar SMS.",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Não foi possível enviar o SMS neste dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*
     * ==========================================================
     * SMS PELO APLICATIVO DO SISTEMA
     * ==========================================================
     */

    private fun abrirAplicativoSms() {

        val telefone =
            obterTelefone()
                ?: return

        val mensagem =
            obterMensagem()
                ?: return

        val uri =
            Uri.parse(
                "smsto:${Uri.encode(telefone)}"
            )

        val intent =
            Intent(
                Intent.ACTION_SENDTO,
                uri
            ).apply {

                putExtra(
                    "sms_body",
                    mensagem
                )
            }

        try {

            startActivity(intent)

        } catch (e: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "Nenhum aplicativo de mensagens foi encontrado.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*
     * ==========================================================
     * E-MAIL
     * ==========================================================
     */

    private fun abrirAplicativoEmail() {

        val email =
            editEmail.text
                .toString()
                .trim()

        val mensagem =
            obterMensagem()
                ?: return

        if (email.isBlank()) {

            editEmail.error =
                "Digite um endereço de e-mail."

            editEmail.requestFocus()

            return
        }

        val assunto =
            "Recomendação de filme - CineFlix"

        val uri =
            Uri.parse(
                "mailto:${Uri.encode(email)}" +
                        "?subject=${Uri.encode(assunto)}" +
                        "&body=${Uri.encode(mensagem)}"
            )

        val intent =
            Intent(
                Intent.ACTION_SENDTO,
                uri
            )

        try {

            startActivity(intent)

        } catch (e: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "Nenhum aplicativo de e-mail foi encontrado.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*
     * ==========================================================
     * RECEBIMENTO DE SMS
     * ==========================================================
     */

    private fun solicitarRecebimentoSms() {

        val permissao =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            )

        if (
            permissao ==
            PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "O recebimento de SMS já está ativado.",
                Toast.LENGTH_SHORT
            ).show()

            atualizarStatusRecebimentoSms()

        } else {

            solicitarPermissaoReceberSms.launch(
                Manifest.permission.RECEIVE_SMS
            )
        }
    }

    private fun atualizarStatusRecebimentoSms() {

        val permissao =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            )

        val autorizado =
            permissao ==
                    PackageManager.PERMISSION_GRANTED

        if (autorizado) {

            textStatusRecebimentoSms.text =
                "Recebimento de SMS ativado."

            btnAtivarRecebimentoSms.text =
                "Recebimento de SMS ativado"

            btnAtivarRecebimentoSms.isEnabled =
                false

        } else {

            textStatusRecebimentoSms.text =
                "Recebimento de SMS ainda não autorizado."

            btnAtivarRecebimentoSms.text =
                "Ativar recebimento de SMS"

            btnAtivarRecebimentoSms.isEnabled =
                true
        }

        mostrarUltimoSmsRecebido()
    }

    private fun mostrarUltimoSmsRecebido() {

        val sms =
            SmsRecebidoManager
                .obterUltimoSms(
                    this
                )

        if (sms == null) {

            textUltimoSmsRecebido.text =
                "Nenhum SMS recebido ainda."

            return
        }

        val formatoData =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss",
                Locale.getDefault()
            )

        val horario =
            if (sms.horario > 0) {

                formatoData.format(
                    Date(
                        sms.horario
                    )
                )

            } else {

                "Horário não disponível"
            }

        textUltimoSmsRecebido.text =
            buildString {

                append(
                    "De: ${sms.remetente}\n"
                )

                append(
                    "Recebido em: $horario\n\n"
                )

                append(
                    sms.mensagem
                )
            }
    }

    /*
     * ==========================================================
     * VALIDAÇÕES
     * ==========================================================
     */

    private fun obterTelefone(): String? {

        val telefone =
            editTelefone.text
                .toString()
                .trim()

        if (telefone.isBlank()) {

            editTelefone.error =
                "Digite o número do telefone."

            editTelefone.requestFocus()

            return null
        }

        return telefone
    }

    private fun obterMensagem(): String? {

        val mensagem =
            editMensagem.text
                .toString()
                .trim()

        if (mensagem.isBlank()) {

            editMensagem.error =
                "Digite uma mensagem."

            editMensagem.requestFocus()

            return null
        }

        return mensagem
    }

    override fun onResume() {

        super.onResume()

        atualizarStatusRecebimentoSms()
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher
            .onBackPressed()

        return true
    }
}