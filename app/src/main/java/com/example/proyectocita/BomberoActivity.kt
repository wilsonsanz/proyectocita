package com.example.proyectocita

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyectocita.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class BomberoActivity : AppCompatActivity() {
    private lateinit var citaDao: CitaDao
    private lateinit var doctorDao: DoctorDao
    private lateinit var citadatabase: CitaDatabase
    private lateinit var spinner: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var especialidad: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bomberomenuactivity)

        // Inicializar base de datos
        citadatabase = CitaDatabase.getInstance(this)
        citaDao = citadatabase.citaDao()
        doctorDao = citadatabase.doctorDao()


        // Inicializar vistas
        calendarView = findViewById(R.id.calendarView)
        spinner = findViewById(R.id.spinnerEspecialidad)

        // Obtener la especialidad desde el Intent
        especialidad = intent.getStringExtra("especialidad") ?: ""

        // Inicializar doctores
        inicializarDoctores()

        // Cargar doctores según la especialidad
        cargarDoctoresPorEspecialidad()


        // Configurar CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            mostrarSelectorHora(selectedDate)
        }
    }

    // Método corregido para insertar los doctores
    private fun inicializarDoctores() {
        lifecycleScope.launch {
            try {
                val doctoresExistentes = withContext(Dispatchers.IO) {
                    doctorDao.obtenerDoctores()
                }

                if (doctoresExistentes.isEmpty()) {
                    val doctores = listOf(
                        Doctor(nombre = "Dr. Juan Pérez", especialidad = "Medicina General"),
                        Doctor(nombre = "Dra. María López", especialidad = "Medicina General"),
                        Doctor(nombre = "Dr. Luis González", especialidad = "Odontología"),
                        Doctor(nombre = "Dra. Ana Rodríguez", especialidad = "Odontología"),
                        Doctor(nombre = "Dr. Javier Martínez", especialidad = "Psicología"),
                        Doctor(nombre = "Dra. Eliana Castro", especialidad = "Psicología"),
                        Doctor(nombre = "Dra. Rosa García", especialidad = "Ginecología"),
                        Doctor(nombre = "Dr. Manuel Ruiz", especialidad = "Ginecología"),
                        Doctor(nombre = "Dr. Pablo Sánchez", especialidad = "Medicina Familiar"),
                        Doctor(nombre = "Dra. Susana Gómez", especialidad = "Medicina Familiar")
                    )

                    // Ahora insertamos la lista completa de doctores
                    withContext(Dispatchers.IO) {
                        doctorDao.insertarDoctores(doctores) // Correcto: insertar toda la lista de una sola vez
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("BomberoActivity", "Error al inicializar doctores: ${e.message}")
                Toast.makeText(this@BomberoActivity, "Error al inicializar doctores", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun cargarDoctoresPorEspecialidad() {
        Log.d("BomberoActivity", "Especialidad: $especialidad")
        lifecycleScope.launch {
            try {
                // Obtener doctores según especialidad
                val doctores = withContext(Dispatchers.IO) {
                    doctorDao.obtenerDoctoresPorEspecialidad(especialidad)

                }

                // Verificar si existen doctores para la especialidad
                if (doctores.isNotEmpty()) {
                    val nombresDoctores = doctores.map { it.nombre }
                    val adapter = ArrayAdapter(
                        this@BomberoActivity,
                        android.R.layout.simple_spinner_item,
                        nombresDoctores
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                } else {
                    // Mostrar mensaje de no disponibilidad
                    Toast.makeText(
                        this@BomberoActivity,
                        "No hay doctores disponibles para $especialidad",
                        Toast.LENGTH_LONG
                    ).show()

                    // Si no hay doctores, agregar un mensaje al spinner
                    val adapter = ArrayAdapter(
                        this@BomberoActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("No hay doctores disponibles")
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@BomberoActivity,
                    "Error al cargar doctores",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    private fun mostrarSelectorHora(fecha: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val horaSeleccionada = String.format("%02d:%02d", selectedHour, selectedMinute)
                val cita = Cita(
                    fecha = fecha,
                    hora = horaSeleccionada,
                    id = 0, // Generar un ID único
                    nombreDoctor = spinner.selectedItem.toString(), // Obtener el nombre del doctor seleccionado
                    especialidad = especialidad
                )
                guardarCitaEnBaseDeDatos(cita)
            },
            hour,
            minute,
            true
        )

        timePicker.setTitle("Selecciona la hora de tu cita")
        timePicker.show()
    }
    private fun guardarCitaEnBaseDeDatos(cita: Cita) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    citaDao.insertarCita(cita)
                }
                // Mostrar mensaje de confirmación
                Toast.makeText(this@BomberoActivity, "Cita programada correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Mostrar mensaje de error
                Toast.makeText(this@BomberoActivity, "Error al guardar la cita", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


