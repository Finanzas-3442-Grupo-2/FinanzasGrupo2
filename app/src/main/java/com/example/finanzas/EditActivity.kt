package com.example.finanzas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finanzas.databinding.ActivityFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.pow

class EditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val taskId = intent.getStringExtra("bono")

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
        val spinnerFrecuencia = findViewById<Spinner>(R.id.form_Frecuencia)
        val spinnerCAVALI = findViewById<Spinner>(R.id.form_CAVALI)

        // Lista con las opciones
        val opcionesFrecuencia = listOf("Frecuencia Semestral", "Frecuencia Anual")
        val opcionesCAVALI = listOf("Incluir CAVALI (0.0525%)", "No incluir CAVALI")

        val adapterFrecuencia = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesFrecuencia)
        adapterFrecuencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapterCAVALI = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesCAVALI)
        adapterCAVALI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFrecuencia.adapter = adapterFrecuencia
        spinnerCAVALI.adapter = adapterCAVALI

        if (taskId != null && userId != null) {
            firestore.collection("listasBonos")
                .document(userId)
                .collection("bonos")
                .document(taskId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.formCapital.text = Editable.Factory.getInstance().newEditable(document.getDouble("capital").toString())
                        binding.formTEA.text = Editable.Factory.getInstance().newEditable(document.getDouble("tea").toString())
                        binding.formAnios.text = Editable.Factory.getInstance().newEditable(document.getLong("años")?.toInt().toString())
                        binding.formNombreBono.text = Editable.Factory.getInstance().newEditable(document.getString("nombreBono").toString())
                        if (document.getLong("frecuencia") == 6L) {
                            binding.formFrecuencia.setSelection(0) // Frecuencia Semestral
                        } else if (document.getLong("frecuencia") == 12L) {
                            binding.formFrecuencia.setSelection(1) // Frecuencia Anual
                        }
                        if (document.getDouble("cavali") == 0.0525) {
                            binding.formCAVALI.setSelection(0) // Incluir CAVALI
                        } else {
                            binding.formCAVALI.setSelection(1) // No incluir CAVALI
                        }
                    }
                }
        }

        if(userId != null){

            //BOTÓN AGREGAR
            buttonAdd.setOnClickListener {

                val capital = binding.formCapital.text.toString().toDoubleOrNull()
                val tea = binding.formTEA.text.toString().toDoubleOrNull()
                val anios = binding.formAnios.text.toString().toIntOrNull()
                val nombreBono = binding.formNombreBono.text.toString().trim()

                // Recuperar el valor seleccionado de los spinner
                val frecuenciaSeleccionada = spinnerFrecuencia.selectedItem.toString()
                val CAVALISeleccionado = spinnerCAVALI.selectedItem.toString()

                //FRECUENCIA-Convertir la opción seleccionada a un valor numérico
                val frecuencia = when (frecuenciaSeleccionada) {
                    "Frecuencia Semestral" -> 6  // 6 meses = 6 meses
                    "Frecuencia Anual" -> 12    // 1 año = 12 meses
                    else -> 6       // Valor por defecto en caso de que no se seleccione ninguna opción
                }

                //CAVALI-Convertir la opción seleccionada a un valor numérico
                val cavali = when (CAVALISeleccionado) {
                    "Incluir CAVALI (0.0525%)" -> 0.0525
                    "No incluir CAVALI (0.0525%)" -> 0.0
                    else -> 0.0525       // Valor por defecto en caso de que no se seleccione ninguna opción
                }

                if (capital != null && tea != null && anios != null && nombreBono.isNotEmpty() && currentUser != null) {
                    val taskData = hashMapOf(
                        "capital" to capital,
                        "tea" to tea,
                        "frecuencia" to frecuencia,
                        "años" to anios,
                        "cavali" to cavali,
                        "nombreBono" to nombreBono
                    )

                    if (taskId != null) {
                        firestore.collection("listasBonos")
                            .document(userId)
                            .collection("bonos")
                            .document(taskId)
                            .set(taskData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Bono actualizado", Toast.LENGTH_SHORT).show()

                                binding.formCapital.text.clear()
                                binding.formTEA.text.clear()
                                binding.formAnios.text.clear()
                                binding.formNombreBono.text.clear()

                                //REGRESO A LA PANTALLA ANTERIOR(LISTA DE BONOS)
                                val intent1 = Intent(this, BondActivity::class.java)
                                startActivity(intent1)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Ingresa una tarea válida", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}