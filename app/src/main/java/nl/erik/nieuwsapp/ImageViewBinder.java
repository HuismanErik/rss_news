package nl.erik.nieuwsapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.bumptech.glide.Glide;

public class ImageViewBinder implements SimpleAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if(view instanceof ImageView) {
            if(data != null && data instanceof String && !((String) data).isEmpty()) {
                Glide.with(view).load(data).into((ImageView) view);
            }
            return true;
        }
        return false;
    }
}
