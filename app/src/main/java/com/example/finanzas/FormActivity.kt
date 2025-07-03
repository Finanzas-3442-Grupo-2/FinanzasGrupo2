package com.example.finanzas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finanzas.databinding.ActivityFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FormActivity : AppCompatActivity() {

    private lateinit var binding:ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonAdd = findViewById<Button>(R.id.form_Ingresar)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser

        val userId = firebaseAuth.currentUser?.uid

        //FLECHA PARA ATRÁS
        val backArrow = findViewById<ImageView>(R.id.form_Flecha)

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //SPINNER (MENÚ DESPLEGABLE)
        val spinnerValorNominal = findViewById<Spinner>(R.id.form_ValorNominal)
        val spinnerFrecuencia = findViewById<Spinner>(R.id.form_Frecuencia)
        val spinnerCAVALI = findViewById<Spinner>(R.id.form_CAVALI)

        spinnerValorNominal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                // Change the selected item's text color
                (view as TextView).setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                // Change the selected item's text color
                (view as TextView).setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinnerCAVALI.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                // Change the selected item's text color
                (view as TextView).setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Lista con las opciones
        val opcionesValorNominal = listOf("$ 2000", "$ 5000", "$ 10000", "$ 20000")
        val opcionesFrecuencia = listOf("Frecuencia Semestral", "Frecuencia Anual")
        val opcionesCAVALI = listOf("Incluir CAVALI (0.0525%)", "No incluir CAVALI")

        val adapterValorNominal = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesValorNominal)
        adapterValorNominal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterFrecuencia = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesFrecuencia)
        adapterFrecuencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterCAVALI = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesCAVALI)
        adapterCAVALI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerValorNominal.adapter = adapterValorNominal
        spinnerFrecuencia.adapter = adapterFrecuencia
        spinnerCAVALI.adapter = adapterCAVALI

        if(userId != null){

            //BOTÓN AGREGAR
            buttonAdd.setOnClickListener {

                val tea = binding.formTEA.text.toString().toDoubleOrNull()
                val anios = binding.formAnios.text.toString().toIntOrNull()
                val nombreBono = binding.formNombreBono.text.toString().trim()
                if (binding.formGraciaTotal.text.toString() == ""){
                    binding.formGraciaTotal.setText("0")
                }
                if (binding.formGraciaParcial.text.toString() == ""){
                    binding.formGraciaParcial.setText("0")
                }
                val periodosGT = binding.formGraciaTotal.text.toString().toIntOrNull()
                val periodosGP = binding.formGraciaParcial.text.toString().toIntOrNull()

                // Recuperar el valor seleccionado de los spinner
                val valorNominalSeleccionado = spinnerValorNominal.selectedItem.toString()
                val frecuenciaSeleccionada = spinnerFrecuencia.selectedItem.toString()
                val CAVALISeleccionado = spinnerCAVALI.selectedItem.toString()

                //VALOR NOMINAL-Convertir la opción seleccionada a un valor numérico
                val valorNominal = when (valorNominalSeleccionado) {
                    "$ 2000" -> 2000.0
                    "$ 5000" -> 5000.0
                    "$ 10000" -> 10000.0
                    "$ 20000" -> 20000.0
                    else -> 2000.0  // Valor por defecto en caso de que no se seleccione ninguna opción
                }

                //FRECUENCIA-Convertir la opción seleccionada a un valor numérico
                val frecuencia = when (frecuenciaSeleccionada) {
                    "Frecuencia Semestral" -> 6 // 6 meses = 6 meses
                    "Frecuencia Anual" -> 12    // 1 año = 12 meses
                    else -> 6   // Valor por defecto en caso de que no se seleccione ninguna opción
                }

                //CAVALI-Convertir la opción seleccionada a un valor numérico
                val cavali = when (CAVALISeleccionado) {
                    "Incluir CAVALI (0.0525%)" -> 0.0525
                    "No incluir CAVALI (0.0525%)" -> 0.0
                    else -> 0.0525  // Valor por defecto en caso de que no se seleccione ninguna opción
                }

                if (tea != null && anios != null && nombreBono.isNotEmpty() && currentUser != null) {

                    if(periodosGT!! + periodosGP!! <= 3){

                        val taskData = hashMapOf(
                            "valorNominal" to valorNominal, // Asumiendo que cuotaInicial es 0.0 por defecto
                            "tea" to tea,
                            "frecuencia" to frecuencia,
                            "años" to anios,
                            "periodosGraciaTotal" to periodosGT,
                            "periodosGraciaParcial" to periodosGP,
                            "cavali" to cavali,
                            "nombreBono" to nombreBono
                        )

                        firestore.collection("listasBonos")
                            .document(userId)
                            .collection("bonos")
                            .add(taskData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Bono guardado", Toast.LENGTH_SHORT).show()

                                binding.formTEA.text.clear()
                                binding.formAnios.text.clear()
                                binding.formNombreBono.text.clear()

                                //REGRESO A LA PANTALLA ANTERIOR(LISTA DE BONOS)
                                val intent1 = Intent(this, BondActivity::class.java)
                                startActivity(intent1)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }

                    }
                    else{
                        Toast.makeText(this, "Máx. número de periodos de gracia es 3", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Ingresa una tarea válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}