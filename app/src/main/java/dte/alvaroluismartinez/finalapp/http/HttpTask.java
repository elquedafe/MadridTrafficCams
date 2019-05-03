package dte.alvaroluismartinez.finalapp.http;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dte.alvaroluismartinez.finalapp.MainActivity;
import dte.alvaroluismartinez.finalapp.MyTextWatcher;
import dte.alvaroluismartinez.finalapp.R;

public class HttpTask extends AsyncTask<String, Void, String> {
    protected Context context;
    private String contentType;
    private String response;
    private Bitmap bitmap;

    /**
     * Constructor
     * @param context Main context
     */
    public HttpTask(Context context){
        super();
        this.context = context;
    }

    /**
     *
     * @param urls
     * @return
     */
    protected String doInBackground(String... urls){
        HttpURLConnection urlConnection = null;
        try {
            response = "";

            //Open url connection
            URL url = new URL( urls[0] );
            urlConnection = (HttpURLConnection) url.openConnection();

            //Get response content type
            contentType = urlConnection.getContentType();

            //Get response stream
            InputStream is = urlConnection.getInputStream();

            //Switch between contentType
            if(contentType.startsWith("image")){
                //Bitmap from response stream
                bitmap = BitmapFactory.decodeStream( is );
                response = "imagen";
            }
            else if(contentType.contentEquals("application/vnd.google-earth.kml+xml")){
                //Parse kml XML and load cameras into Model and List
                loadData(is);
                response = "kml";
            }
            else {
                response = contentType + " not processed";
            }

            //Close connection
            urlConnection.disconnect();
        } catch (Exception e) {
            response = e.toString();
        }

        return response;
    }

    protected void onPostExecute(String response){
        //Get MainActivity to obtain main methods
        Activity main = (Activity) context;
        MainActivity mainActivity = (MainActivity)context;

        ListView lista = (ListView)main.findViewById(R.id.listaCamaras);
        int indexSelectedCam = mainActivity.getCameraModel().getCameraIndexByName(mainActivity.getNameSelectedCam());

        //Switch response image or kml
        switch(response){
            case "imagen":
                //Displays image
                ImageView imageView = (ImageView)main.findViewById(R.id.imageView);

                lista.setItemChecked(indexSelectedCam, true);
                imageView.setImageBitmap(bitmap);

                mainActivity.getBitmapFragment().setBitmap(bitmap);

                break;
            case "kml":
                //Change list values
                ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_checked, mainActivity.getCameraModel().getCamNames());
                lista.setAdapter(adapter);

                //Check item in the list
                lista.setItemChecked(indexSelectedCam, true);

                //Create TextWatcher for filter cameras
                mainActivity.setTextWatcher(new MyTextWatcher(mainActivity, mainActivity.getCameraModel()));
                mainActivity.getEditText().addTextChangedListener(mainActivity.getTextWatcher());

                //Filter cameras
                mainActivity.getTextWatcher().filterByName();
                break;
        }
        /*if(response.equals("imagen")){
            imageView.setImageBitmap(bitmap);
        }
        else if(response.equals("kml")){
            ListView lista = (ListView)main.findViewById(R.id.listaCamaras);
            ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_checked, mainActivity.getCameraModel().getCamNames());
            lista.setAdapter(adapter);
            int indexSelectedCam = mainActivity.getCameraModel().getCameraIndexByName(mainActivity.getNameSelectedCam());
            lista.setItemChecked(indexSelectedCam, true);

            //MyTextWatcher textWatcher = new MyTextWatcher(mainActivity, mainActivity.getCameraModel());
            mainActivity.setTextWatcher(new MyTextWatcher(mainActivity, mainActivity.getCameraModel()));
            mainActivity.getEditText().addTextChangedListener(mainActivity.getTextWatcher());
            mainActivity.getTextWatcher().filterByName();
        }*/
    }

    public void loadData(InputStream is){
        MainActivity mainActivity = (MainActivity)context;
        String mensajeError = "";
        boolean error = false;
        XmlPullParserFactory parserFactory;
        List<String> camarasListaNombres = new ArrayList<String>();
        List<String>camarasListaCoordinates = new ArrayList<String>();
        List<String>camarasListaURLS = new ArrayList<String>();
        try {
            //InputStream is = getAssets().open("CCTV.kml");
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int tipoEvento = parser.getEventType();
            parseo(tipoEvento, parser, camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
            mainActivity.getCameraModel().addCameras(camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
        } catch (XmlPullParserException e) {
            mensajeError += e.toString();
            error = true;
        } catch (IOException e) {
            mensajeError += e.toString();
            error = true;
        }

    }

    private void parseo(int tipoEvento, XmlPullParser parser, List<String> camarasListaNombres, List<String> camarasListaCoordinates, List<String> camarasListaURLS) throws IOException, XmlPullParserException {
        boolean isNombre = false;
        while (tipoEvento != XmlPullParser.END_DOCUMENT) {
            String nombreElemento = parser.getName();
            switch (tipoEvento) {
                case XmlPullParser.START_TAG:
                    if ("description".equals(nombreElemento)) {
                        String camaraURL = parser.nextText();
                        camaraURL = camaraURL.substring(camaraURL.indexOf("http:"));
                        camaraURL = camaraURL.substring(0, camaraURL.indexOf(".jpg") + 4);
                        camarasListaURLS.add( camaraURL );
                    }
                    else if (nombreElemento.equals("Data") && parser.getAttributeValue(0).equals("Nombre")) {
                        isNombre = true;
                    }
                    else if (nombreElemento.equals("coordinates")) {
                        String s = parser.nextText();
                        camarasListaCoordinates.add(s);
                    }
                    else if (nombreElemento.equals("Value") && isNombre){
                        String s = parser.nextText();
                        camarasListaNombres.add(s);
                        isNombre = false;
                    }

                    break;
            }
            tipoEvento = parser.next();
        }
    }
}
