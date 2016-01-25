package com.karview.android.app;

import com.karview.android.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SignInFragment extends Fragment {
//	Fragment
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.fragment_signin, container, false);
        final FragmentManager fm = getFragmentManager();
        
        layout.findViewById(R.id.btnSignup).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.setFragment(fm, MainActivity.FG_SIGNUP);
//				fm.beginTransaction().add(R.id.fragment_container, new SignUpFragment()).addToBackStack(null).commit();
			}
        });
        
        return layout;
    }
}
