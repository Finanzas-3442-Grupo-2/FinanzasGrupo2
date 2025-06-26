package com.example.finanzas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finanzas.databinding.ActivityBondBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BondActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_bond)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityBondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bienvenido = findViewById<TextView>(R.id.bonos_bienvenido)
        val taskListLayout = findViewById<LinearLayout>(R.id.taskListLayout)
        val buttonAdd = findViewById<FloatingActionButton>(R.id.botonAgregar)

        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {

            //MENSAJE DE BIENVENIDA
            firestore.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombres")
                        bienvenido.text = " Bienvenido, $nombre!"
                    } else {
                        bienvenido.text = " Bienvenido"
                    }
                }

            //CARGAR BONOS GUARDADOS AL INICIAR
            firestore.collection("listasBonos")
                .document(userId)
                .collection("bonos")
                .orderBy("nombreBono")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val taskText = document.getString("nombreBono") ?: continue
                        val textView = TextView(this).apply {
                            text = taskText
                            textSize = 24f
                            setPadding(24, 48, 16, 48)
                            setBackgroundResource(R.drawable.bond_background)
                            setTextColor(Color.WHITE)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 32, 32, 0)
                            }

                            //PRESIONAR PARA VER LA INFORMACIÓN DEL BONO (ResultActivity)
                            setOnClickListener {
                                val intent = Intent(this@BondActivity, ResultActivity::class.java)
                                intent.putExtra("bono", document.id)
                                startActivity(intent)
                            }
                        }
                        taskListLayout.addView(textView)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar bonos", Toast.LENGTH_SHORT).show()
                }

            //BOTÓN "+"
            buttonAdd.setOnClickListener {
                //ENLACE A LA SIGUIENTE PANTALLA (FORMULARIO DE BONO)
                val intent1 = Intent(this, FormActivity::class.java)
                startActivity(intent1)
            }
        }
    }
}