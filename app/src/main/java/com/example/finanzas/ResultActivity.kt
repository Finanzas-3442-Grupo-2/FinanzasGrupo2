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

        val finales = findViewById<TableLayout>(R.id.resultFinal)

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
                        val valorNominal = document.getDouble("valorNominal") ?: 0.0
                        val tea = document.getDouble("tea") ?: 0.0
                        val frecuencia = document.getLong("frecuencia")?.toInt() ?: 0
                        val tes = (((1+(tea/100)).pow(1.0/2)) - 1) * 100
                        val anios = document.getLong("años")?.toInt() ?: 0
                        val periodosGT = document.getLong("periodosGraciaTotal")?.toInt() ?: 0
                        val periodosGP = document.getLong("periodosGraciaParcial")?.toInt() ?: 0
                        val cavali = document.getDouble("cavali")?: 0.0
                        val colocacion = document.getDouble("colocacion")?: 0.0
                        val estructuracion = document.getDouble("estructuracion")?: 0.0
                        val plazos = (anios * 12) / frecuencia  // IGV, por ejemplo

                        //FUNCIÓN PARA AGREGAR FILAS A LA TABLA DE LA SECCIÓN DATOS
                        fun addRow(label: String, value: String, table: TableLayout = resultadoView) {
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
                            table.addView(row)
                        }

                        //AGREGAR FILAS PARA LA TABLA DE LA SECCIÓN DATOS
                        addRow(" Valor Nominal ", String.format("%,.2f $", valorNominal))
                        addRow(" Tasa Efectiva Anual ", String.format("%.2f %%", tea))

                        //MOSTRAR TES SOLO SI SE ELIGIÓ PERIODO SEMESTRAL
                        if (frecuencia == 6) {
                            val tes = (((1 + tea / 100).pow(1.0 / 2)) - 1) * 100
                            addRow(" Tasa Efectiva Semestral ", String.format("%.7f %%", tes))
                        }

                        addRow(" Frecuencia ", "$frecuencia meses")
                        addRow(" Años ", "$anios")
                        addRow(" Periodos de G. Total ", "$periodosGT")
                        addRow(" Periodos de G. Parcial ", "$periodosGP")
                        addRow(" CAVALI ", String.format("%.4f %%", cavali))
                        addRow(" Estructuracion ", String.format("%.4f %%", estructuracion))
                        addRow(" Colocacion ", String.format("%.4f %%", colocacion))
                        addRow(" Plazos ", "$plazos")

                        //SECCIÓN DETALLES DE PLAZOS

                        val tableLayout = findViewById<TableLayout>(R.id.paymentTable)
                        tableLayout.removeAllViews() // Limpia la tabla por si tenía algo

                        val numFilas = plazos
                        val numColumnas = 7

                        // Opcional: encabezados
                        val headers = listOf("N°", "P. Gracia", "S. Inicial", "Interés", "Cuota", "Amort.", "S. Final")
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

                        // CALCULAR PLAZOS SIN GRACIA
                        val plazosSinGracia = numFilas - (periodosGT + periodosGP)

                        // PRIMER SALDO
                        var saldoInicial = valorNominal

                        // Variables que se recalculan
                        var interes: Double
                        var cuota: Double = 0.0
                        var amortizacion: Double
                        var saldoFinal: Double

                        val flujosTIR_TCEA = mutableListOf<Double>()
                        val flujosTIR_TREA = mutableListOf<Double>()
                        var precio = 0.0
                        flujosTIR_TCEA.add(saldoInicial)  // Flujo 0
                        flujosTIR_TREA.add(saldoInicial)  // Flujo 0

                        // Control de contadores
                        var contadorGT = 0
                        var contadorGP = 0
                        var contadorS = 0
                        var finDePlazos = false

                        var cuotaFija = 0.0 // cuota calculada una sola vez para 'sin gracia'

                        // FILAS
                        for (i in 1..numFilas) {

                            val tableRow = TableRow(this)

                            // Calcular interés cada período
                            interes = saldoInicial * tepDec

                            val tipoPlazo: String

                            when {
                                contadorGT < periodosGT -> {
                                    // Total
                                    cuota = 0.0
                                    amortizacion = 0.0
                                    saldoFinal = saldoInicial + interes
                                    tipoPlazo = "T"
                                    contadorGT++
                                    precio = precio + cuota
                                }

                                contadorGP < periodosGP -> {
                                    // Parcial
                                    cuota = interes
                                    amortizacion = 0.0
                                    saldoFinal = saldoInicial // el saldo no cambia
                                    flujosTIR_TCEA.add((cuota+(cuota*(cavali/100))+(cuota*(estructuracion/100))+(cuota*(colocacion/100)))*-1)
                                    flujosTIR_TREA.add(cuota*-1)
                                    tipoPlazo = "P"
                                    contadorGP++
                                    precio = precio + cuota
                                }

                                else -> {
                                    // Sin gracia
                                    if (!finDePlazos) {
                                        cuotaFija = saldoInicial * ((tepDec * (1 + tepDec).pow(plazosSinGracia)) / ((1 + tepDec).pow(plazosSinGracia) - 1))
                                        finDePlazos = true
                                    }
                                    cuota = cuotaFija
                                    amortizacion = cuota - interes
                                    saldoFinal = saldoInicial - amortizacion
                                    flujosTIR_TCEA.add((cuota+(cuota*cavali))*-1)
                                    flujosTIR_TREA.add(cuota*-1)
                                    tipoPlazo = "S"
                                    contadorS++
                                    precio = precio + cuota
                                }
                            }

                            // AGREGAR CELDAS
                            for (j in 1..7) {
                                val cellText = when (j) {
                                    1 -> "$i" // Nº
                                    2 -> tipoPlazo
                                    3 -> "%,.2f".format(saldoInicial)
                                    4 -> "%,.2f".format(interes)
                                    5 -> "%,.2f".format(cuota)
                                    6 -> "%,.2f".format(amortizacion)
                                    7 -> "%,.2f".format(saldoFinal)
                                    else -> ""
                                }

                                val textView = TextView(this)
                                textView.text = cellText
                                textView.setTextColor(Color.BLACK)
                                textView.setBackgroundColor(Color.WHITE)
                                textView.setPadding(16, 8, 16, 8)
                                textView.textSize = 14f
                                textView.gravity = Gravity.CENTER
                                tableRow.addView(textView)
                            }

                            tableLayout.addView(tableRow)

                            // EL SIGUIENTE SALDO INICIAL SIEMPRE VIENE DEL SALDO FINAL
                            saldoInicial = saldoFinal
                        }

                        fun calcularTIR(flujos: MutableList<Double>, guess: Double = 0.1): Double {
                            var tir = guess
                            val precision = 1e-7
                            val maxIter = 1000

                            for (i in 0 until maxIter) {
                                var van = 0.0
                                var vanDerivada = 0.0

                                for (t in flujos.indices) {
                                    val denominador = Math.pow(1 + tir, t.toDouble())
                                    van += flujos[t] / denominador

                                    if (t > 0) {
                                        vanDerivada += -t * flujos[t] / (denominador * (1 + tir))
                                    }
                                }

                                val nuevaTir = tir - van / vanDerivada

                                if (Math.abs(nuevaTir - tir) < precision) {
                                    return nuevaTir
                                }

                                tir = nuevaTir
                            }

                            throw RuntimeException("No converge: revisa el guess o el flujo")
                        }

                        val TIR_TCEA = calcularTIR(flujosTIR_TCEA)
                        val TIR_TREA = calcularTIR(flujosTIR_TREA)

                        var tcea = 0.0

                        if(frecuencia == 6){//FRECUENCIA SEMESTRAL
                            tcea = ((1.0 + TIR_TCEA).pow(2) - 1.0)*100
                        }
                        else{//FRECUENCIA ANUAL
                            tcea = ((1.0 + TIR_TCEA).pow(1) - 1.0)*100
                        }

                        var trea = ((1.0 + TIR_TREA).pow(1) - 1.0)*100
                        addRow(" TCEA ", String.format("%.2f %%", tcea),finales)
                        addRow(" TREA ", String.format("%.2f %%", trea),finales)
                        addRow(" Precio Venta ", String.format("%.2f", precio),finales)
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
