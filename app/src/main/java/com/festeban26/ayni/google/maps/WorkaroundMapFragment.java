package com.festeban26.ayni.google.maps;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Helper class
 * source: https://stackoverflow.com/questions/30525066/how-to-set-google-map-fragment-inside-scroll-view
 */

public class WorkaroundMapFragment extends SupportMapFragment {
    private OnTouchListener mListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstance) {
        View layout = super.onCreateView(layoutInflater, viewGroup, savedInstance);

        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());

        frameLayout.setBackgroundColor(Color.TRANSPARENT);

        if(layout != null){
            ((ViewGroup) layout).addView(frameLayout,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            return layout;
        }
        else
            return null;
    }

    public void setListener(OnTouchListener listener) {
        mListener = listener;
    }

    public interface OnTouchListener {
        void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {

            if(mListener != null){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mListener.onTouch();
                        break;
                    case MotionEvent.ACTION_UP:
                        mListener.onTouch();
                        break;
                }
            }

            return super.dispatchTouchEvent(event);
        }
    }
}