package com.karview.android.app;

import java.util.ArrayList;
import java.util.List;

import me.notisfy.android.ui.card.ListCardView;
import me.notisfy.android.ui.card.ListCardView.FooterLoadingState;
import me.notisfy.android.ui.card.item.ArticleItemCardView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.karview.android.R;
import com.karview.android.data.NewsData;
import com.karview.android.data.NewsList;
import com.karview.android.request.NewsFeedController;
import com.karview.android.request.NewsFeedModel;
import com.karview.android.ui.NewsFeedCustomBodyView;

public class NewsFeedFragment extends CardListFragment {

	private ListCardView listCardView;
	private List<NewsData> articles = new ArrayList<NewsData>();

	@Override
	public void onInit(ListCardView listCardView) {
		this.listCardView = listCardView;

		loadData();
	}

	@Override
	public void onHalt() {
	}

	@Override
	public View onItemDisplay(final int position, View convertView,
			ViewGroup parent) {
		ArticleItemCardView cardView = (ArticleItemCardView) convertView;
		if (cardView == null) {
			cardView = new ArticleItemCardView(getActivity());
		}
		NewsData newsData = articles.get(position);
		cardView.setTitle(newsData.getTitle(), false);
		View customBodyView = cardView.getCustomBody();
		if (customBodyView == null
				|| !(customBodyView instanceof NewsFeedCustomBodyView)) {
			customBodyView = new NewsFeedCustomBodyView(getActivity());
			cardView.setCustomBody(customBodyView);
		}
		NewsFeedCustomBodyView customBodyAsNewsFeed = (NewsFeedCustomBodyView) customBodyView;
        customBodyAsNewsFeed.setData(newsData.getSummary(), newsData.getUrl(), newsData.getFeaturedImage(), getImageLoader());

		return cardView;
	}

	@Override
	public int onItemCount() {
		return articles.size();
	}

	private void showDetailFragment(final int position) {
		if (position > articles.size()) {
			return;
		}
		FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
		String tag = "detail#" + position;
		Fragment targetFragment = fragmentMgr.findFragmentByTag(tag);
		if (targetFragment == null) {
			targetFragment = NewsDetailFragment.getFragment(articles
					.get(position));
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

	private void loadData() {
		NewsFeedController controller = NewsFeedController
				.getInstance(getActivity().getApplicationContext());
		listCardView.setFooterLoadingState(FooterLoadingState.LOADING);
		listCardView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						showDetailFragment(position - 1);
					}
				});
		controller.requestNewsList(null,
				new NewsFeedController.OnResponseListener() {
					@Override
					public void onResponse(NewsFeedModel model) {

						List<NewsData> newsList = model.getNewsList();
						
						articles.clear();
						articles.addAll(newsList);

						listCardView
								.setFooterLoadingState(FooterLoadingState.COMPLETE_LAST);
						listCardView.notifyItemChanged();
					}
				});
	}
}