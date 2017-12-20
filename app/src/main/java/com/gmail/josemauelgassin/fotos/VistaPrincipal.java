package com.gmail.josemauelgassin.fotos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

public class VistaPrincipal extends AppCompatActivity {

    /************************************************************
     *                  Atributos / propiedades
     ************************************************************/
    public static final String TAG = "JMG";  // APP TAG

    // Referencias a elementos del layout
    private TextInputEditText teitNombreFoto;
    private Button btnMemInterna;
    private Button btnMemExternaPrivada;
    private Button btnMemExternaPublica;


    // Tokens para listeners
    static final int RC_FOTO_ALM_INT = 1;      // Token para Request Code de foto que irá en almacenamiento interno
    static final int RC_FOTO_ALM_EXT_PRI = 2;  // "" que irá en almacenamiento externo privado
    static final int RC_FOTO_ALM_EXT_PUB = 3;  // "" que irá en almacenamiento externo público
    static final int RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_A = 4; // Token para Request Code para asegurar (pedir) permisos de almacenamiento externo para "RC_FOTO_ALM_EXT_PRI"
    static final int RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_B = 5; // Token para Request Code para asegurar (pedir) permisos de almacenamiento externo para "RC_FOTO_ALM_EXT_PUB"




    /************************************************************
     *                   Métodos de actividad
     ************************************************************/
    // Establecer eventos para elementos del layout (botones...)
    public void events() {
        // Pedir foto para guardar en memoria interna (almacenamiento interno)
        btnMemInterna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pedirFoto(VistaPrincipal.this, teitNombreFoto, RC_FOTO_ALM_INT);
            }
        });

        // Pedir foto para guardar en memoria externa privada (almacenamiento externo privado)
        btnMemExternaPrivada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pedirFoto(VistaPrincipal.this, teitNombreFoto, RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_A);
            }
        });

        // Pedir foto para guardar en memoria externa pública (almacenamiento externo público)
        btnMemExternaPublica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pedirFoto(VistaPrincipal.this, teitNombreFoto, RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_B);
            }
        });
    }

    // Inicialización de APP
    public void init() {
        // Referenciar elementos del layout
        teitNombreFoto = findViewById(R.id.teitNombreFoto);
        btnMemInterna = findViewById(R.id.btnMemInterna);
        btnMemExternaPrivada = findViewById(R.id.btnMemExternaPrivada);
        btnMemExternaPublica = findViewById(R.id.btnMemExternaPublica);

        // Preparar eventos
        events();
    }

    // Pedir foto mediante intent implícito, el destino dependerá del RequestCode
    public static void pedirFoto(Activity a, TextInputEditText teit, int requestCode) {
        // Comprobar que se ha escrito algún nombre para foto
        if (teit.getText().toString().isEmpty())
            teit.setError("Debe escribir un nombre para la foto");
            // Iniciar actividad (intent implícito) para tomar foto -> Android buscará aplicación que pueda hacerlo
        else
            a.startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), requestCode);
    }



    /************************************************************
     *                    Métodos de interfaz
     ************************************************************/
    // Método para recibir el resultado de la actividad
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Si hay resultado
        if (resultCode == RESULT_OK) {
            // Si el resultado tiene que ver con procesamiento de fotos...
            if (requestCode == RC_FOTO_ALM_INT || resultCode == RC_FOTO_ALM_EXT_PRI || resultCode == RC_FOTO_ALM_EXT_PUB) {
                // Obtener foto de bundle y procesar imágen
                Bundle b = data.getExtras();
                Bitmap foto = (Bitmap) b.get("data");

                // Preparar búfer de salida
                FileOutputStream salida = null;

                // Construir nombre del fichero (foto)
                String nombreFoto = construirNombreFoto(teitNombreFoto.getText().toString());

                // A. Se quiere guardar en almacenamiento interno
                if (requestCode == RC_FOTO_ALM_INT) {
                    try {
                        salida = new FileOutputStream(getFilesDir() + "/" + nombreFoto); // Memoria interna privada
                    } catch (FileNotFoundException e) {}
                }
                // B. Se quiere guardar en almacenamiento externo privado
                else if (requestCode == RC_FOTO_ALM_EXT_PRI) {
                    try {
                        salida = new FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + nombreFoto); // Memoria interna privada
                    } catch (FileNotFoundException e) {}
                }
                // C. Se quiere guardar en almacenamiento externo público
                else if (requestCode == RC_FOTO_ALM_EXT_PUB) {
                    try {
                        salida = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + nombreFoto); // Memoria interna privada
                    } catch (FileNotFoundException e) {}
                }

                // Comprobar salida válida para guardar imagen finalmente
                if (salida != null)
                    foto.compress(Bitmap.CompressFormat.JPEG, 90, salida);
            }
            // Ya no se tienen permisos, pedir foto para guardado en almacenamiento externo privado
            else if (requestCode == RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_A) {
                if (pedirPermisosEscrituraAlmacenamientoExterno(VistaPrincipal.this, RC_FOTO_ALM_EXT_PRI))
                    VistaPrincipal.this.startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), RC_FOTO_ALM_EXT_PRI);
            }
            // Ya no se tienen permisos, pedir foto para guardado en almacenamiento externo público
            else if (requestCode == RC_PEDIR_PERMISO_ESCRIBIR_ALMACENAMIENTO_EXTERNO_A) {
                if (pedirPermisosEscrituraAlmacenamientoExterno(VistaPrincipal.this, RC_FOTO_ALM_EXT_PUB))
                    VistaPrincipal.this.startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), RC_FOTO_ALM_EXT_PUB);
            }
        }
    }

    // Volver a pedir permisos para escribir en memoria
    public static boolean pedirPermisosEscrituraAlmacenamientoExterno(Activity a, int requestCode) {
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si hubiera que dar algun aexplicación se haría aquí
            if (ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Mostrar una explicación de forma ASÍNCRONA (NO BLOQUEAR ESTE HILO), cuando el usuario termine de ver la explicación volver a intentar pedir permiso
            }
            // Explicación no necesaria, pedir permiso
            else
                ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);

            return false;
        }
        // En el caso de que ya se hubieran dado permisos retornar true
        else
            return true;
    }




    public static String construirNombreFoto(String nombreFoto) {
        StringBuilder sb = new StringBuilder(nombreFoto + "_");
        Calendar c = Calendar.getInstance();

        sb.append(c.get(Calendar.YEAR));
        sb.append(agregarCeroIzquierdo(c.get(Calendar.MONTH)));
        sb.append(agregarCeroIzquierdo(c.get(Calendar.DAY_OF_MONTH)));
        sb.append(agregarCeroIzquierdo(c.get(Calendar.HOUR_OF_DAY)));
        sb.append(agregarCeroIzquierdo(c.get(Calendar.MINUTE)));
        sb.append(agregarCeroIzquierdo(c.get(Calendar.SECOND)));
        sb.append(".jpg");

        return sb.toString();
    }

    public static String agregarCeroIzquierdo(int num) {
        if (num > 9)
            return "0" + String.valueOf(num);
        else
            return String.valueOf(num);
    }



    /************************************************************
     *                 Métodos de "ciclo de vida"
     ************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_principal);

        // Iniciar procesamiento de APP
        init();
    }



}
