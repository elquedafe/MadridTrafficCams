package dte.alvaroluismartinez.finalapp;


import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dte.alvaroluismartinez.finalapp.http.HttpTask;
import dte.alvaroluismartinez.finalapp.http.HttpTaskImage;
import dte.alvaroluismartinez.finalapp.model.CameraModel;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    //View Components
    private EditText editText;
    private Toolbar mTopToolbar;
    private ListView lista;
    private ImageView image;

    //Toolbar checkitems
    private Menu menu;
    private MenuItem opLoc;
    private MenuItem opAll;
    private MenuItem opCluster;

    //Lists
    private List<String> camarasListaNombres;
    private List<String> camarasListaCoordinates;
    private List<String> camarasListaURLS;

    //Selected cam variables
    private int indexSelectedCam;
    private String nameSelectedCam;

    //Cameras Model
    private CameraModel modeloCamaras;

    //Search filter component
    private MyTextWatcher textWatcher;

    //Boolean toolbar state retrieved
    private boolean isAll;
    private boolean isCluster;
    private boolean isLocationEnable;

    //Location
    private GPSLocation myLocation;

    //Fragment
    private BitmapFragment bitmapFragment;

    private final String URL_KML_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        mTopToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mTopToolbar);

        //Get toolbar Menu
        this.menu = mTopToolbar.getMenu();

        //Booleans initialization
        isAll = false;
        isCluster = false;
        isLocationEnable = false;

        //Get TextBox
        editText = (EditText)findViewById(R.id.editText);

        //Get ImageView
        image = (ImageView)findViewById(R.id.imageView);




        //Avoiding pushing Views when keyboard is shown
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //Cam selection initialization
        indexSelectedCam = -1;
        nameSelectedCam = "";

        //Camera Model initialization
        modeloCamaras = new CameraModel();

        //ListView initialization
        lista = (ListView)findViewById(R.id.listaCamaras);
        lista.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
        lista.setOnItemClickListener(this);

        FragmentManager fm  = getFragmentManager();
        bitmapFragment = (BitmapFragment)fm.findFragmentByTag("bitmap");
        if(bitmapFragment == null){
            bitmapFragment = new BitmapFragment();
            fm.beginTransaction().add(bitmapFragment, "bitmap").commit();
        }
        else{
            image.setImageBitmap(bitmapFragment.getBitmap());
        }

        //If not state saved load camera data from web, if not restore state
        if(savedInstanceState == null)
            loadData(URL_KML_CAMERAS);
            //loadLocalData();
        else{
            isLocationEnable = savedInstanceState.getBoolean("location");
            isAll = savedInstanceState.getBoolean("all");
            isCluster = savedInstanceState.getBoolean("cluster");
            indexSelectedCam = savedInstanceState.getInt("cameraIndex");
            editText.setText(savedInstanceState.getString("searchText").toString());
            this.modeloCamaras = (CameraModel)savedInstanceState.getParcelable("model");
            lista.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_checked, modeloCamaras.getCamNames()));
            textWatcher = new MyTextWatcher(this, (CameraModel)savedInstanceState.getParcelable("completeModel"));
            editText.addTextChangedListener(textWatcher);
            lista.setItemChecked(this.indexSelectedCam, true);

            /*image.setImageDrawable(null);
            image.setImageBitmap((Bitmap) savedInstanceState.getParcelable("image"));*/



            //removeOldState(savedInstanceState);
            //this.onItemClick(lista, lista.getSelectedView(), this.indexSelectedCam, this.indexSelectedCam);
        }
    }
    public void loadLocalData(){
        String mensajeError = "";
        boolean error = false;
        XmlPullParserFactory parserFactory;
        camarasListaNombres = new ArrayList<String>();
        camarasListaCoordinates = new ArrayList<String>();
        camarasListaURLS = new ArrayList<String>();
        try {
            InputStream is = getAssets().open("CCTV.kml");
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int tipoEvento = parser.getEventType();
            parseo(tipoEvento, parser, camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
            modeloCamaras.addCameras(camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
        } catch (XmlPullParserException e) {
            mensajeError += e.toString();
            error = true;
        } catch (IOException e) {
            mensajeError += e.toString();
            error = true;
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, modeloCamaras.getCamNames());

        lista.setAdapter(adapter);
    }

    public void loadData(String url){
        HttpTask tarea = new HttpTask(this);
        tarea.execute(url);
    }

    /*public void loadData(InputStream is){
        String mensajeError = "";
        boolean error = false;
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int tipoEvento = parser.getEventType();
            parseo(tipoEvento, parser, camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
            modeloCamaras.addCameras(camarasListaNombres, camarasListaCoordinates, camarasListaURLS);
        } catch (XmlPullParserException e) {
            mensajeError += e.toString();
            error = true;
        } catch (IOException e) {
            mensajeError += e.toString();
            error = true;
        }
    }*/

    private void parseo(int tipoEvento, XmlPullParser parser, List<String> camarasListaNombres, List<String> camarasListaCoordinates, List<String> camarasListaURLS) throws IOException, XmlPullParserException{
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

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        indexSelectedCam = position;
        if(!modeloCamaras.getCamNames().isEmpty() && (indexSelectedCam > -1)) {
            nameSelectedCam = modeloCamaras.getCameraNameByIndex(position);
            HttpTaskImage tarea = new HttpTaskImage(this);
            //txt.setText("");
            //txt.setText( camarasListaURLS.get(position) );
            //txt.append(" : " +  camarasListaCoordinates.get(position));
            String url = modeloCamaras.getCamUrls().get(position);

            tarea.execute(url);
        }
    }

    public void onClick_image(View v){

        if(modeloCamaras.getCamNames().size() > 0) {
            final Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            if (this.menu.findItem(R.id.all).isChecked() && !this.menu.findItem(R.id.cluster).isChecked()) {
                intent.putStringArrayListExtra("allCordinates", (ArrayList<String>) modeloCamaras.getCamCoordinates());
                intent.putStringArrayListExtra("allNames", (ArrayList<String>) modeloCamaras.getCamNames());
                intent.putExtra("name", modeloCamaras.getCamNames().get(indexSelectedCam));
            } else if (this.menu.findItem(R.id.cluster).isChecked()) {
                intent.putStringArrayListExtra("allCordinates", (ArrayList<String>) modeloCamaras.getCamCoordinates());
                intent.putStringArrayListExtra("allNames", (ArrayList<String>) modeloCamaras.getCamNames());
                intent.putStringArrayListExtra("allUrls", (ArrayList<String>) modeloCamaras.getCamUrls());
                intent.putExtra("name", modeloCamaras.getCamNames().get(indexSelectedCam));
                intent.putExtra("isCluster", true);

            } else {
                intent.putExtra("name", modeloCamaras.getCamNames().get(indexSelectedCam));
                intent.putExtra("coordinate", modeloCamaras.getCamCoordinates().get(indexSelectedCam));
            }
            if (this.menu.findItem(R.id.location).isChecked()) {
                GPSLocation.LocationResult locationResult = new GPSLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location, Context context) {
                        Activity main = (Activity) context;
                        intent.putExtra("myLat", location.getLatitude());
                        intent.putExtra("myLong", location.getLongitude());
                        startActivity(intent);
                    }
                };
                myLocation = new GPSLocation();
                myLocation.init(this, locationResult);
            } else {
                startActivity(intent);
            }
        }
        //Lo que habia antes: startActivityForResult(intent, REQUEST_CODE_FORM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        this.opLoc = menu.findItem(R.id.location);
        opLoc.setChecked(isLocationEnable);
        this.opAll = menu.findItem(R.id.all);
        opAll.setChecked(isAll);
        this.opCluster = menu.findItem(R.id.cluster);
        opCluster.setChecked(isCluster);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayAdapter adapter = null;
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.location:
                if(!checkLocationPermission())
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.all:
                item.setChecked(!item.isChecked());
                return true;
            case R.id.cluster:
                item.setChecked(!item.isChecked());
                return true;
            case R.id.order:
                //ordenar
                modeloCamaras.orderByName();
                textWatcher.orderModel();
                adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, modeloCamaras.getCamNames());
                lista.setAdapter(adapter);
                //actulizar indice de camara
                indexSelectedCam = modeloCamaras.getCameraIndexByName(nameSelectedCam);
                //marcar camara en la lista
                lista.setItemChecked(indexSelectedCam, true);
                //lista.deferNotifyDataSetChanged();
                return true;
            case R.id.refresh:
                //Borrar las camaras anteriormente cargadas
                modeloCamaras.getCameras().clear();
                //Cargar camaras
                //loadLocalData();
                loadData(URL_KML_CAMERAS);
                editText.removeTextChangedListener(textWatcher);
                textWatcher = new MyTextWatcher(this, modeloCamaras);
                editText.addTextChangedListener(textWatcher);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public CameraModel getCameraModel(){
        return modeloCamaras;
    }
    public int getIndexSelectedCam(){
        return indexSelectedCam;
    }

    public String getNameSelectedCam() {
        return nameSelectedCam;
    }

    public ListView getLista() {
        return lista;
    }

    public EditText getEditText() {
        return editText;
    }

    public MyTextWatcher getTextWatcher() {
        return textWatcher;
    }

    public void setTextWatcher(MyTextWatcher textWatcher) {
        this.textWatcher = textWatcher;
    }

    public BitmapFragment getBitmapFragment() {
        return bitmapFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        removeOldState(savedInstanceState);
        savedInstanceState.putInt("cameraIndex", this.indexSelectedCam);
        savedInstanceState.putString("searchText", editText.getText().toString());
        savedInstanceState.putParcelable("model", modeloCamaras);
        savedInstanceState.putParcelable("completeModel", textWatcher.getCameraModel());
        savedInstanceState.putBoolean("location", opLoc.isChecked());
        savedInstanceState.putBoolean("all", opAll.isChecked());
        savedInstanceState.putBoolean("cluster", opCluster.isChecked());

        /*Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        savedInstanceState.putParcelable("image", bitmap);*/ //da error TransactionTooLargeException

        /*if (savedInstanceState.containsKey("image"))
            savedInstanceState.remove("image");
        savedInstanceState.putParcelable("image", ((BitmapDrawable)image.getDrawable()).getBitmap() );*/

        super.onSaveInstanceState(savedInstanceState);
    }

    private void removeOldState(Bundle savedInstanceState){
        if (savedInstanceState.containsKey("cameraIndex"))
            savedInstanceState.remove("cameraIndex");
        if (savedInstanceState.containsKey("searchText"))
            savedInstanceState.remove("searchText");
        if (savedInstanceState.containsKey("model"))
            savedInstanceState.remove("model");
        if (savedInstanceState.containsKey("completeModel"))
            savedInstanceState.remove("completeModel");
        if (savedInstanceState.containsKey("location"))
            savedInstanceState.remove("location");
        if (savedInstanceState.containsKey("all"))
            savedInstanceState.remove("all");
        if (savedInstanceState.containsKey("cluster"))
            savedInstanceState.remove("cluster");

    }

    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.indexSelectedCam = savedInstanceState.getInt("cameraIndex");
        editText.setText(savedInstanceState.getString("searchText").toString());
        //this.nameSelectedCam = modeloCamaras.getCameraNameByIndex(indexSelectedCam);

        //lista.setItemChecked(this.indexSelectedCam, true);
        //image.setImageBitmap((Bitmap) savedInstanceState.getParcelable("image"));
        this.onItemClick(lista, lista.getSelectedView(), this.indexSelectedCam, this.indexSelectedCam);
    }*/

    public GPSLocation getMyLocation(){
        return this.myLocation;
    }
}