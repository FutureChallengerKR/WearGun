package com.karview.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WebFragment extends Fragment {
	private static final String NEWS_URL_KEY = "news_url";

	private String mUrl;
	private WebView mWebView;
	private ProgressBar mProgressBar;

	public static Fragment getFragment(String url) {
		Fragment fragment = new WebFragment();
		Bundle bundle = new Bundle();
		bundle.putString(NEWS_URL_KEY, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		mUrl = bundle.getString(NEWS_URL_KEY);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout webViewContainer = new LinearLayout(getActivity());
		webViewContainer.setOrientation(LinearLayout.VERTICAL);

		mProgressBar = new ProgressBar(getActivity(), null,
				android.R.attr.progressBarStyleHorizontal);
		webViewContainer.addView(mProgressBar, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int) (5 * getResources()
						.getDisplayMetrics().density)));

		mWebView = new WebView(getActivity());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new NewsWebViewClient());
		mWebView.setWebChromeClient(new NewsWebChromeClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(mUrl);
		mWebView.getSettings().setSaveFormData(false);

		webViewContainer.addView(mWebView, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		return webViewContainer;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mWebView != null) {
			mWebView.stopLoading();
		}
	}

	private class NewsWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			mProgressBar.setProgress(newProgress);
		}
	}

	private class NewsWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url,
				android.graphics.Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mProgressBar.setVisibility(View.VISIBLE);
		};

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Animation animation = new ScaleAnimation(0, 0, 1, 0);
			animation.setDuration(1000);
			animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mProgressBar.setVisibility(View.GONE);
				}
			});
			mProgressBar.startAnimation(animation);
		};
	}
}
