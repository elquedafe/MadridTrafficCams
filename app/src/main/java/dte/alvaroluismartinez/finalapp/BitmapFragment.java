package dte.alvaroluismartinez.finalapp;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;

public class BitmapFragment extends Fragment {

    // data object we want to retain
    private Bitmap bitmap;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setBitmap(Bitmap data) {
        this.bitmap = data;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
