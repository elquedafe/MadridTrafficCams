package dte.alvaroluismartinez.camarastraficomadrid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private ListView lista;
    private TextView txt;
    private ArrayAdapter<String> adapter;
    private List<String> listaNombres;
    private List<String> camarasListaURLS;
    private List<String> camarasListaCoordinates;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camarasListaURLS = new ArrayList<String>();
        camarasListaCoordinates = new ArrayList<String>();

        String mensajeError = "";
        boolean error = false;
        lista = (ListView)findViewById(R.id.listaCamaras);
        txt = (TextView)findViewById(R.id.salidaView);
        listaNombres = new ArrayList<String>();
        lista.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lista.setOnItemClickListener(this);
        XmlPullParserFactory parserFactory;

        try {
            InputStream is = getAssets().open("CCTV.kml");
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int tipoEvento = parser.getEventType();
            parseo(tipoEvento, parser, camarasListaURLS, camarasListaCoordinates);

        } catch (XmlPullParserException e) {
            mensajeError += e.toString();
            error = true;
        } catch (IOException e) {
            mensajeError += e.toString();
            error = true;
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, listaNombres);

        lista.setAdapter(adapter);

    }

    public void parseo(int tipoEvento, XmlPullParser parser, List<String> camarasListaURLS, List<String> camarasListaCoordinates) throws IOException, XmlPullParserException{
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
                        listaNombres.add(s);
                        isNombre = false;
                    }

                    break;
            }
            tipoEvento = parser.next();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        txt.setText("");
        txt.setText( camarasListaURLS.get(position) );
        txt.append(" : " +  camarasListaCoordinates.get(position));
    }
}
