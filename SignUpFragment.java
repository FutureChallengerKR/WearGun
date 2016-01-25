package com.karview.android.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.notisfy.android.ui.card.ListCardView.FooterLoadingState;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.karview.android.R;
import com.karview.android.data.SignUpData;
import com.karview.android.data.NewsData;
import com.karview.android.request.NewsFeedController;
import com.karview.android.request.NewsFeedModel;
import com.karview.android.service.KVSignUpService;
import com.karview.android.service.KVResponse;
import com.karview.android.service.KVService;

public class SignUpFragment extends Fragment implements View.OnClickListener{
	private EditText editEmail = null;
	private EditText editPassword = null;
	private EditText editPasswordConfirm = null;
	private LinearLayout circleLayout = null;
	private Context context = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.fragment_signup, container, false);
		context = getActivity().getApplicationContext();
		
		editEmail = (EditText)layout.findViewById(R.id.editEmail);
		editPassword = (EditText)layout.findViewById(R.id.editPassword);
		editPasswordConfirm = (EditText)layout.findViewById(R.id.editPasswordConfirm);
		layout.findViewById(R.id.btnSignup).setOnClickListener(this);
        return layout;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.btnSignup) {
			String email = editEmail.getText().toString();
			String password = editPassword.getText().toString();
			String passwordConfirm = editPasswordConfirm.getText().toString();
			
//			if(!checkEmail(email)) {
//				confirmPopup("이메일 주소를 올바르게 넣어주세요!", null);
//				editEmail.setText("");
//				editPassword.setText("");
//				editPasswordConfirm.setText("");
//				return;
//			}
//			
//			if(!checkPassword(password)) {
//				confirmPopup("밀번호는 8자리 이상입니다!", null);
//				editPassword.setText("");
//				editPasswordConfirm.setText("");
//				return;
//			}
//			
//			if(!password.equalsIgnoreCase(passwordConfirm)) {
//				confirmPopup("두 비밀번호가 서로 맞지 않습니다!", null);
//				editPassword.setText("");
//				editPasswordConfirm.setText("");
//				return;
//			}
			
			final FragmentManager fm = getFragmentManager();
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("email", email);
			params.put("password", password);
			
			KVSignUpService accountApi = KVSignUpService.getInstance(getActivity().getApplicationContext());
			accountApi.setParams(params);
			accountApi.request(new KVService.OnResponseListener() {
				@Override
				public void onResponse(KVResponse data) {
					
					if(data.getStatus() == KVResponse.STATUS_SUCCESS) {
						SignUpData account = (SignUpData) data;
						if(account.getSuccess()) {
							getActivity().runOnUiThread(new Runnable() {
							    public void run() {
							    	circlePopup(false);
							    	confirmPopup("정상적으로 계정 등록되었습니다.\n로그인을 해주세요.", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											MainActivity.backFragment(fm);
										}
									});
							    }
							});
						} else {
							// failed reason : used_email
							getActivity().runOnUiThread(new Runnable() {
							    public void run() {
							    	circlePopup(false);
							    	confirmPopup("이미 등록된 Email입니다.\n다시 입력해 주세요.", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											editEmail.setText("");
											editPassword.setText("");
											editPasswordConfirm.setText("");
										}
									});
							    }
							});
						}
						
						
						
					} else {
						Log.w(SignUpFragment.class.toString(), "\tNetwork Error Code : " + data.getHttpStatus());
						getActivity().runOnUiThread(new Runnable() {
						    public void run() {
//						    	circlePopup(false);
//						    	noticePopup("서버에 접속할 수 없습니다. 네트워크를 확인해주세요.");
						    }
						});
					}
				}
			});
			
			circlePopup(true);
		}
	}
	
	private boolean checkEmail(String email) {
		String[] arr = email.split("@");
		if(arr.length != 2) 
			return false;
		
		String[] arr2 = arr[1].split("\\.");
		if(arr2.length < 2)
			return false;
		
		return true;
	}
	
	private boolean checkPassword(String password) {
		if(password.length() < 8)
			return false;
		
		return true;
	}
	
	private void confirmPopup(String message, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("알림");
		builder.setMessage(message);
		builder.setCancelable(false);
        if(listener != null) {
        	builder.setNeutralButton("확인", listener);
        } else {
        	builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    			}
    		});
        }
        
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	private AlertDialog circleDialog = null;
	
	private void circlePopup(boolean visible) {
		if(visible) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			circleLayout = (LinearLayout) inflater.inflate(R.layout.popup_circle, null);
			
			AlertDialog.Builder aDialog = new AlertDialog.Builder(getActivity());
	        aDialog.setView(circleLayout);
	        aDialog.setMessage("계정 등록 중...");
//	        aDialog.setCancelable(false);
	        
	        circleDialog = aDialog.create();
	        circleDialog.show();
		} else {
			if(circleDialog != null) {
//				circleDialog.hide();
				circleDialog.dismiss();
			}
		}
		
	}
}
