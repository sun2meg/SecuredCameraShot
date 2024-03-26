package com.megacoreapps.sun2meg.securedcamerashot;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ViewAdapter extends PagerAdapter {

    Context context;

    int sliderAllImages[] = {
            R.drawable.nointernet,
            R.drawable.perm,

            R.drawable.one,
            R.drawable.two,
            R.drawable.pin,



    };

    int sliderAllTitle[] = {
            R.string.screen1,
            R.string.screen2,

            R.string.screen5,
            R.string.screen6,
            R.string.screen7,

    };

    int sliderAllDesc[] = {
            R.string.screen1desc,
            R.string.screen2desc,
            R.string.screen5bdesc,
            R.string.screen6bdesc,
            R.string.screen7desc,
    };

    public ViewAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return sliderAllTitle.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_screen,container,false);

        ImageView sliderImage = (ImageView) view.findViewById(R.id.sliderImage);
        TextView sliderTitle = (TextView) view.findViewById(R.id.sliderTitle);
        TextView sliderDesc = (TextView) view.findViewById(R.id.sliderDesc);

        sliderImage.setImageResource(sliderAllImages[position]);
        sliderTitle.setText(this.sliderAllTitle[position]);
        sliderDesc.setText(this.sliderAllDesc[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}