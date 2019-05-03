package dte.alvaroluismartinez.finalapp;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import dte.alvaroluismartinez.finalapp.model.CameraModel;

public class MyTextWatcher implements TextWatcher {
    private MainActivity mainActivity;
    private CameraModel cameraModel;

    public MyTextWatcher(Context context, CameraModel cameraModel){
        super();
        this.mainActivity = (MainActivity)context;
        this.cameraModel = new CameraModel(cameraModel);

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        EditText editText = (EditText)mainActivity.findViewById(R.id.editText);
        String text = editText.getText().toString();
        List<Integer> listIndex = cameraModel.getIndexesFromText(text);
        if(listIndex.size() > 0) {
            List<String> listNames = new ArrayList<String>();
            List<String> listCoordinates = new ArrayList<String>();
            List<String> listUrls = new ArrayList<String>();
            for (int index : listIndex) {
                listNames.add(cameraModel.getCameraNameByIndex(index));
                listCoordinates.add(cameraModel.getCameraCoordinateByIndex(index));
                listUrls.add(cameraModel.getCameraUrlByIndex(index));
            }
            String nameCamSelected = mainActivity.getNameSelectedCam();
            mainActivity.getCameraModel().getCameras().clear();
            mainActivity.getCameraModel().addCameras(listNames, listCoordinates, listUrls);

            ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_list_item_checked, mainActivity.getCameraModel().getCamNames());
            mainActivity.getLista().setAdapter(adapter);

            mainActivity.getLista().setItemChecked(mainActivity.getCameraModel().getCameraIndexByName(nameCamSelected), true);
        }
        else{
            mainActivity.getCameraModel().getCameras().clear();
            ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_list_item_checked, mainActivity.getCameraModel().getCamNames());
            mainActivity.getLista().setAdapter(adapter);
        }
    }

    public void orderModel(){
        this.cameraModel.orderByName();
    }

    public void changeDataBaseModel(CameraModel model){
        this.cameraModel = model;
    }

    public CameraModel getCameraModel() {
        return cameraModel;
    }

    public void filterByName(){
        this.afterTextChanged(null);
    }
}
