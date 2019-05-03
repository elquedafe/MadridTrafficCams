package dte.alvaroluismartinez.finalapp.http;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import dte.alvaroluismartinez.finalapp.R;

public class HttpTaskImage extends HttpTask{
    public HttpTaskImage(Context context) {
        super(context);
    }

    @Override
    protected void onPreExecute(){
        Activity main = (Activity) context;
        ImageView imageView = (ImageView)main.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.common_google_signin_btn_icon_light_normal_background);
    }
}
