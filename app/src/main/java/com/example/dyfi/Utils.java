package com.example.dyfi;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class Utils {

    public static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     * Le preguntamos a la base de datos online por un {@link Evento} para representar un terremoto
     */
    public static Evento traerDataDeTerremoto(String requestUrl) {
        // Creamos un objeto URL
        URL url = crearURL(requestUrl);

        // Hacemos un pedido HTTP y recibimos una respuesta
        String jsonResponse = null;
        try {
            jsonResponse = hacerRequestHTTP(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error Cerrando el Stream", e);
        }

        // Extraigo la informacion que nesecito y creo un objeto {@link Event} 
        Evento terremoto = extraigoDeJSON(jsonResponse);

        // devuelvo el  {@link Event}
        return terremoto;
    }

    /**
     * Creo una URL a partir de un String URL
     */
    private static URL crearURL(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creado URL ", e);
        }
        return url;
    }

    /**
     * Hago el request HTTP de una url dada y devuelve un String como respuesta.
     */
    private static String hacerRequestHTTP(URL url) throws IOException {
        String jsonResponse = "";

        // Si al URL es null termino
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milisegundos */);
            urlConnection.setConnectTimeout(15000 /* milisegundos */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Si obtengo respuesta correcta (200 OK),
            // Entonces leo y parseo la respuesta
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Codigo de Error: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problemas buscando el JSON de terremotos.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convierto el {@link InputStream} en un String que contiene toda la respuesta del JSON
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Devuelvo un onjeto {@link Evento} parseando la informacion del
     * primer terremoto que viene en el input
          */
    private static Evento extraigoDeJSON(String terremotoJSON) {
        // Si el JSON esta vacio devuelvo null
        if (TextUtils.isEmpty(terremotoJSON)) {
            return null;
        }

        try {
            JSONObject baseJsonResponse = new JSONObject(terremotoJSON);
            JSONArray featureArray = baseJsonResponse.getJSONArray("features");

            // Si hay resultados en el array de Features
            if (featureArray.length() > 0) {
                // Extraigo el primero feature (Terremoto)
                JSONObject firstFeature = featureArray.getJSONObject(0);
                JSONObject properties = firstFeature.getJSONObject("properties");

                // Extraigo el titulo, cantidad de personas y la fuerza
                String titulo = properties.getString("title");
                String nroPersonas = properties.getString("felt");
                String fuerzaPercibida = properties.getString("cdi");

                // Creo un objeto {@link Evento}
                return new Evento(titulo, nroPersonas, fuerzaPercibida);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problemas parseando El JSON", e);
        }
        return null;
    }
}
