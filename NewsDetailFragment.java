package com.karview.android.app;

import me.notisfy.android.ui.card.item.ArticleItemCardView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.karview.android.R;
import com.karview.android.data.NewsData;
import com.karview.android.ui.NewsFeedCustomBodyView;

public class NewsDetailFragment extends Fragment {
	private static final String NEWS_DATA_KEY = "news_data";

	private NewsData mNewsData;

	public static Fragment getFragment(NewsData newsData) {
		Fragment fragment = new NewsDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(NEWS_DATA_KEY, newsData);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		mNewsData = bundle.getParcelable(NEWS_DATA_KEY);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ArticleItemCardView cardView = new ArticleItemCardView(getActivity());

		if (mNewsData != null) {
			cardView.setTitle(mNewsData.getTitle(), false);
			View customBodyView = cardView.getCustomBody();
			if (customBodyView == null
					|| !(customBodyView instanceof NewsFeedCustomBodyView)) {
				customBodyView = new NewsFeedCustomBodyView(getActivity());
				cardView.setCustomBody(customBodyView);
			}
			NewsFeedCustomBodyView customBodyAsNewsFeed = (NewsFeedCustomBodyView) customBodyView;
			customBodyAsNewsFeed.setData(mNewsData.getSummary(),
					mNewsData.getUrl(), mNewsData.getFeaturedImage(), getImageLoader());
			customBodyAsNewsFeed.setLinkClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
					String tag = "webfragment";
					Fragment targetFragment = fragmentMgr.findFragmentByTag(tag);
					if (targetFragment == null) {
						targetFragment = WebFragment.getFragment(mNewsData.getUrl());
					} else {
						int cnt = fragmentMgr.getBackStackEntryCount();
						if (cnt > 0) {
							// target == top of back stack
							// do nothing
							String backStackName = fragmentMgr.getBackStackEntryAt(cnt - 1)
									.getName();
							if (tag.equals(backStackName)) {
								return;
							}
						}
					}

					if (targetFragment != null) {
						FragmentTransaction ft = fragmentMgr.beginTransaction();
						ft.setCustomAnimations(android.R.anim.fade_in, 0, 0,
								me.notisfy.android.ui.R.anim.fade_out);
						ft.replace(R.id.fragment_container, targetFragment, tag);
						ft.addToBackStack(tag);
						ft.commitAllowingStateLoss();
					}
				}
			});
		}

		return cardView;
	}

	// TODO: integrate below code with CardListFragment's one.
	protected ImageLoader getImageLoader() {
		if (volleyImageLoader == null) {
			volleyImageLoader = new ImageLoader(
					Volley.newRequestQueue(getActivity()
							.getApplicationContext()), volleyImageCache);
		}
		return volleyImageLoader;
	}

	private ImageLoader volleyImageLoader = null;
	private ImageCache volleyImageCache = new ImageCache() {
		@Override
		public void putBitmap(String url, Bitmap bitmap) {
		}

		@Override
		public Bitmap getBitmap(String url) {
			return null;
		}
	};
}