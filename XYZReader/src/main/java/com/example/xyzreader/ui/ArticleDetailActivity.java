package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */

public class ArticleDetailActivity extends AppCompatActivity
		implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = ArticleDetailActivity.class.getSimpleName();
	private Cursor mCursor;
	private long mStartId;
	private int mposition;
	private long mSelectedItemId;
	private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
	private int mTopInset;
	private ViewPager mPager;
	public MyPagerAdapter mPagerAdapter;
	private View mUpButtonContainer;
	private View mUpButton;

	@Override
	protected void onResume() {
		super.onResume();
		mPagerAdapter.notifyDataSetChanged();
		mPager.setAdapter(mPagerAdapter);
		mPagerAdapter.getItemPosition(mPager);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}

		setContentView(R.layout.activity_article_detail);

		getSupportLoaderManager().initLoader(0, null, this);
		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mPagerAdapter);
		mPager.setPageMargin((int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
				mUpButton.animate()
						.alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
						.setDuration(300);

			}

			@Override
			public void onPageSelected(int position) {
				if (mCursor != null) {
					mCursor.moveToPosition(position);
				}
				mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
				updateUpButtonPosition();

			}

		});

		mUpButtonContainer = findViewById(R.id.up_container);

		mUpButton = findViewById(R.id.action_up);
		mUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSupportNavigateUp();
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
				@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
				@Override
				public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
					view.onApplyWindowInsets(windowInsets);
					mTopInset = windowInsets.getSystemWindowInsetTop();
					mUpButtonContainer.setTranslationY(mTopInset);
					updateUpButtonPosition();
					return windowInsets;
				}
			});
		}

		if (savedInstanceState == null) {
			if (getIntent() != null && getIntent().getData() != null) {
				mStartId = ItemsContract.Items.getItemId(getIntent().getData());
				mSelectedItemId = mStartId;
				mPagerAdapter.getItemPosition(mPager);

			}
		} else if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.idvalstr))) {
			mStartId = savedInstanceState.getLong(getString(R.string.idvalstr));
			mSelectedItemId = mStartId;
			getSupportLoaderManager().restartLoader(0, null, this);
			mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
			mPagerAdapter.getItemPosition(mPager);
		}

	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		mPagerAdapter.notifyDataSetChanged();
		mPager.setAdapter(mPagerAdapter);
		mPagerAdapter.getItemPosition(mPager);
	}

	public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
		if (itemId == mSelectedItemId) {
			mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
			updateUpButtonPosition();
		}
	}

	private void updateUpButtonPosition() {
		int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
		mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
	}

	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return ArticleLoader.newAllArticlesInstance(this);

	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
		mCursor = cursor;
		mPagerAdapter.notifyDataSetChanged();
		mPager.setAdapter(mPagerAdapter);
		// Select the start ID
		if (mStartId > 0) {
			if (mCursor != null && mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				do {
					if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
						final int position = mCursor.getPosition();
						mposition = position;
						mPagerAdapter.getItemPosition(mPager);
						mPager.setCurrentItem(position, false);
						mPagerAdapter.notifyDataSetChanged();
						break;
					}
					mCursor.moveToNext();
				} while (!mCursor.isAfterLast());
				mStartId = 0;
				mPagerAdapter.getItemPosition(mPager);
			}
		}

	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		mCursor = null;

	}


	public class MyPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

		public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}


		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
			ArticleDetailFragment fragment = (ArticleDetailFragment) object;
			if (fragment != null) {
				mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
				updateUpButtonPosition();
			}
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			mCursor.moveToPosition(position);

			return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));

		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return (mCursor != null) ? mCursor.getCount() : 0;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(getString(R.string.idvalstr), mSelectedItemId);
	}

}
