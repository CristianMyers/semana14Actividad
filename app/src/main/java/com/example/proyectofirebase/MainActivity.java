package com.example.proyectofirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Declarar variables
    private EditText txtCodigo, txtNombre, txtDueño, txtDireccion;
    private Spinner spMascota;
    private ListView lista;

    // Variable de conexión de Firestore
    private FirebaseFirestore db;

    // Datos para el Spinner
    String[] TiposMascotas = {"Perro", "Gato", "Pájaro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Enlazar variables con los IDs del XML
        txtCodigo = findViewById(R.id.txtCodigo);
        txtNombre = findViewById(R.id.txtNombre);
        txtDueño = findViewById(R.id.txtDueño);
        txtDireccion = findViewById(R.id.txtDireccion);
        spMascota = findViewById(R.id.spMascota);
        lista = findViewById(R.id.lista);

        // Configurar Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TiposMascotas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMascota.setAdapter(adapter);

        // Llamar al método que carga la lista
        CargarListaFirestore();
    }

    // Método para enviar datos a Firestore
    public void enviarDatosFirestore(View view) {
        // Obtener los datos ingresados
        String codigo = txtCodigo.getText().toString();
        String nombre = txtNombre.getText().toString();
        String dueño = txtDueño.getText().toString();
        String direccion = txtDireccion.getText().toString();
        String tipoMascota = spMascota.getSelectedItem().toString();

        // Validar campos vacíos
        if (codigo.isEmpty() || nombre.isEmpty() || dueño.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un mapa con los datos
        Map<String, Object> mascota = new HashMap<>();
        mascota.put("codigo", codigo);
        mascota.put("nombre", nombre);
        mascota.put("dueño", dueño);
        mascota.put("direccion", direccion);
        mascota.put("tipoMascota", tipoMascota);

        // Enviar los datos a Firestore
        db.collection("mascotas").document(codigo).set(mascota)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Datos enviados a Firestore correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error al enviar datos a Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error al enviar datos", e);
                });
    }

    // Método para limpiar los campos después de enviar datos
    private void limpiarCampos() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDueño.setText("");
        txtDireccion.setText("");
        spMascota.setSelection(0);
    }

    // Método para cargar la lista desde Firestore
    public void CargarListaFirestore() {
        // Referencia a la colección
        CollectionReference mascotasRef = db.collection("mascotas");

        // Obtener datos de Firestore
        mascotasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Lista para almacenar los datos
                List<String> ListaMascotas = new ArrayList<>();

                // Recorrer los documentos de la colección
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String linea = "Código: " + document.getString("codigo") + " | Nombre: " + document.getString("nombre")
                            + " | Dueño: " + document.getString("dueño") + " | Dirección: " + document.getString("direccion");
                    ListaMascotas.add(linea);
                }

                // Crear ArrayAdapter con los datos obtenidos
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ListaMascotas);
                lista.setAdapter(adapter);
            } else {
                // Log de error
                Log.e("Firestore", "Error al obtener datos", task.getException());
            }
        });
    }

    // Botón que llama al método para cargar la lista
    public void CargarLista(View view) {
        CargarListaFirestore();
    }
}
