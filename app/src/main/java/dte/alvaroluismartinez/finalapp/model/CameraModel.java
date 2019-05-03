package dte.alvaroluismartinez.finalapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CameraModel implements Parcelable {
    private List<Camera> cameras;


    public CameraModel(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public CameraModel(CameraModel model){
        cameras = new ArrayList<Camera>();
        for(int i=0; i < model.getCameras().size();i++) {
            cameras.add(new Camera(model.getCameraNameByIndex(i), model.getCameraCoordinateByIndex(i), model.getCameraUrlByIndex(i)));
        }
    }

    public CameraModel() {
        this.cameras = new ArrayList<Camera>();
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public void orderByName(){
        cameras.sort(new Comparador());
    }

    public List<String> getCamNames(){
        List<String> listNames = new ArrayList<String>();
        for(Camera c : cameras){
            listNames.add(c.getName());
        }
        return listNames;
    }

    public List<String> getCamCoordinates(){
        List<String> listCoordinates = new ArrayList<String>();
        for(Camera c : cameras){
            listCoordinates.add(c.getCoordenadas());
        }
        return listCoordinates;
    }

    public List<String> getCamUrls(){
        List<String> listUrls = new ArrayList<String>();
        for(Camera c : cameras){
            listUrls.add(c.getUrl());
        }
        return listUrls;
    }

    public void addCamera(String name, String coord, String url){
        cameras.add(new Camera(name, coord, url));
    }

    public void addCameras(List<String> names, List<String> coords, List<String> urls){
        int i = 0;
        for(String cam: names){
            cameras.add(new Camera(cam, coords.get(i), urls.get(i)));
            i++;
        }

    }

    public String getCameraNameByIndex(int index){
        return cameras.get(index).getName();
    }

    public int getCameraIndexByName(String name){
        int i = 0;
        for(Camera c : cameras){
            if(c.getName().equals(name))
                return i;
            i++;
        }
        return -1;
    }

    public String getCameraCoordinateByIndex(int index){
        return cameras.get(index).getCoordenadas();
    }

    public String getCameraUrlByIndex(int index){
        return cameras.get(index).getUrl();
    }

    public List<Integer> getIndexesFromText(String text){
        List<Integer> listIndex = new ArrayList<Integer>();
        for(String name : this.getCamNames()) {
            if (name.contains(text.toUpperCase()))
                listIndex.add(getCameraIndexByName(name));
        }
        return listIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.cameras);
    }

    /** recreate object from parcel */
    private CameraModel(Parcel in) {
        cameras = (ArrayList<Camera>)in.readArrayList(cameras.getClass().getClassLoader());
    }

    public static final Parcelable.Creator<CameraModel> CREATOR = new Parcelable.Creator<CameraModel>() {
        public CameraModel createFromParcel(Parcel in) {
            return new CameraModel(in);
        }

        public CameraModel[] newArray(int size) {
            return new CameraModel[size];
        }
    };


}

class Camera implements Parcelable{
    private String name;
    private String coordenadas;
    private String url;

    public Camera(String name, String coordenadas, String url) {
        this.name = name;
        this.coordenadas = coordenadas;
        this.url = url;
    }

    protected Camera(Parcel in) {
        name = in.readString();
        coordenadas = in.readString();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(coordenadas);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Camera> CREATOR = new Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

class Comparador implements Comparator<Camera>{
    @Override
    public int compare(Camera c1, Camera c2){
        return c1.getName().compareTo(c2.getName());
    }

}
