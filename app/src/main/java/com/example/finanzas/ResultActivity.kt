package com.example.finanzas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.pow
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.Toast

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        val taskId = intent.getStringExtra("bono")
        val userId = firebaseAuth.currentUser?.uid

        val resultadoView = findViewById<TableLayout>(R.id.resultData)

        //FLECHA PARA ATRÁS
        val backArrow = findViewById<ImageView>(R.id.result_Flecha)

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val edit = findViewById<Button>(R.id.result_edit)

        val delete = findViewById<Button>(R.id.result_delete)

        delete.setOnClickListener {
            if (taskId != null && userId != null) {
                firestore.collection("listasBonos")
                    .document(userId)
                    .collection("bonos")
                    .document(taskId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Bono eliminado", Toast.LENGTH_SHORT).show()
                        finish() // Cierra la actividad actual
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar el bono: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        edit.setOnClickListener {
            if (taskId != null) {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("bono", taskId)
                startActivity(intent)
            }
        }

        if (taskId != null && userId != null) {
            firestore.collection("listasBonos")
                .document(userId)
                .collection("bonos")
                .document(taskId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val capital = document.getDouble("capital") ?: 0.0
                        val cuotaInicial = document.getDouble("cuotaInicial") ?: 0.0
                        val tea = document.getDouble("tea") ?: 0.0
                        val frecuencia = document.getLong("frecuencia")?.toInt() ?: 0
                        val tes = (((1+(tea/100)).pow(1.0/2)) - 1) * 100
                        val anios = document.getLong("años")?.toInt() ?: 0
                        val cavali = document.getDouble("cavali")?: 0.0
                        val plazos = (anios * 12) / frecuencia  // IGV, por ejemplo

                        //FUNCIÓN PARA AGREGAR FILAS A LA TABLA DE LA SECCIÓN DATOS
                        fun addRow(label: String, value: String) {
                            val row = TableRow(this)

                            val labelView = TextView(this).apply {
                                text = label
                                textSize = 20f
                                setTextColor(Color.WHITE)
                                setBackgroundColor(Color.parseColor("#5E0000"))
                                setPadding(8, 12, 8, 12)
                                typeface = Typeface.DEFAULT_BOLD
                                gravity = Gravity.START
                            }

                            val valueView = TextView(this).apply {
                                text = value
                                textSize = 20f
                                setTextColor(Color.BLACK)
                                setBackgroundColor(Color.WHITE)
                                setPadding(8, 12, 8, 12)
                                gravity = Gravity.END
                                typeface = Typeface.DEFAULT  // tabla alineada

                                // Asegura que ocupe el espacio completo de su celda
                                layoutParams = TableRow.LayoutParams(
                                    0,  // width = 0 → para usar el peso
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f  // weight = 1 → ocupa todo el espacio disponible
                                )
                            }

                            row.addView(labelView)
                            row.addView(valueView)
                            resultadoView.addView(row)
                        }

                        //AGREGAR FILAS PARA LA TABLA DE LA SECCIÓN DATOS
                        addRow(" Capital ", String.format("%,.2f $", capital))
                        addRow(" Cuota Inicial ", String.format("%,.2f %%", cuotaInicial))
                        addRow(" Tasa Efectiva Anual ", String.format("%.2f %%", tea))

                        //MOSTRAR TES SOLO SI SE ELIGIÓ PERIODO SEMESTRAL
                        if (frecuencia == 6) {
                            val tes = (((1 + tea / 100).pow(1.0 / 2)) - 1) * 100
                            addRow(" Tasa Efectiva Semestral ", String.format("%.7f %%", tes))
                        }

                        addRow(" Frecuencia ", "$frecuencia meses")
                        addRow(" Años ", "$anios")
                        addRow(" CAVALI ", String.format("%.4f %%", cavali))
                        addRow(" Plazos ", "$plazos")

                        //SECCIÓN DETALLES DE PLAZOS

                        val tableLayout = findViewById<TableLayout>(R.id.paymentTable)
                        tableLayout.removeAllViews() // Limpia la tabla por si tenía algo

                        val numFilas = plazos
                        val numColumnas = 6

                        // Opcional: encabezados
                        val headers = listOf("N°", "S. Inicial", "Interés", "Cuota", "Amort.", "S. Final")
                        val headerRow = TableRow(this)

                        for (header in headers) {
                            val textView = TextView(this)
                            textView.text = header
                            textView.setTextColor(Color.WHITE)
                            textView.setBackgroundColor(Color.parseColor("#5E0000"))
                            textView.setPadding(16, 8, 16, 8)
                            textView.setTypeface(null, Typeface.BOLD)
                            textView.textSize = 16f
                            textView.gravity = Gravity.CENTER
                            headerRow.addView(textView)
                        }
                        tableLayout.addView(headerRow)

                        //TEP (SEGÚN LA FRECUENCIA) EN DECIMAL, NO EN PORCENTAJE
                        val tepDec = if(frecuencia == 6){ tes / 100} else { tea / 100}

                        //VALORES EN MI PRIMERA INSTANCIA
                        var saldoInicial = capital - capital*(cuotaInicial/100)
                        val cuota = saldoInicial * ((tepDec * ((1 + tepDec).pow(plazos))) / ((1 + tepDec).pow(plazos) - 1))
                        var interes = saldoInicial * tepDec
                        var amortizacion = cuota - interes
                        var saldoFinal = saldoInicial - amortizacion

                        //FILAS DINÁMICAS
                        for (i in 1..numFilas) {
                            val tableRow = TableRow(this)

                            for (j in 1..numColumnas) {
                                val cellText = when (j) {
                                    1 -> "$i"// N° de Plazo
                                    2 -> "%,.2f".format(saldoInicial)
                                    3 -> "%,.2f".format(interes)
                                    4 -> "%,.2f".format(cuota)
                                    5 -> "%,.2f".format(amortizacion)
                                    6 -> "%,.2f".format(saldoFinal)
                                    else -> ""
                                }

                                val textView = TextView(this)
                                textView.text = cellText
                                textView.setTextColor(Color.BLACK)
                                textView.setBackgroundColor(Color.WHITE)
                                textView.setPadding(16, 8, 16, 8)
                                textView.textSize = 20f
                                textView.gravity = Gravity.CENTER
                                tableRow.addView(textView)
                            }

                            tableLayout.addView(tableRow)

                            //RECALCULAR DATOS PARA LA SIGUIENTE FILA (PLAZO)
                            saldoInicial = saldoFinal
                            interes = saldoInicial * tepDec
                            amortizacion = cuota - interes
                            saldoFinal = saldoInicial - amortizacion
                        }
                    }
                }
                .addOnFailureListener { e ->
                    val intent1 = Intent(this, BondActivity::class.java)
                    startActivity(intent1)
                    finish()
                }
        }
    }
}