package net.oschina.app.v2.activity.search;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.search.fragment.SearchViewPagerFragment;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.utils.SimpleTextWatcher;
import net.oschina.app.v2.utils.TDevice;

/**
 * Created by Sim on 2015/3/3.
 */
public class SearchActivity extends BaseActivity {
    private EditText mEtSearch;
    private ImageButton mIbClear;
    private View mIbSearch;
    private SearchViewPagerFragment mSearchFragment;
    private View mIvActionBarShadow;

    @Override
    protected int getLayoutId() {
        return R.layout.v2_activity_simple_fragment;
    }

    @Override
    protected int getActionBarCustomView() {
        return R.layout.v2_actionbar_search;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected void initActionBar(Toolbar actionBar) {
        super.initActionBar(actionBar);
        View view = actionBar;
        mIbSearch = view.findViewById(R.id.ib_search);
        mIbSearch.setOnClickListener(this);
        mIbClear = (ImageButton) view.findViewById(R.id.ib_clear);
        mIbClear.setOnClickListener(this);
        mEtSearch = (EditText) view.findViewById(R.id.et_content);
        mEtSearch.setOnClickListener(this);
        mEtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEtSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (mEtSearch.getText().toString().length() > 0) {
                    mIbClear.setVisibility(View.VISIBLE);
                    mIbSearch.setVisibility(View.GONE);
                } else {
                    mIbClear.setVisibility(View.GONE);
                    mIbSearch.setVisibility(View.GONE);
                }
            }
        });
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    handleSearch();
                    return true;
                }
                return false;
            }
        });
        mEtSearch.requestFocus();

    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        mSearchFragment =  new SearchViewPagerFragment();
        trans.replace(R.id.container, mSearchFragment);
        trans.commit();

        mIvActionBarShadow = findViewById(R.id.iv_actionbar_shadow);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.ib_search) {
            handleSearch();
        } else if (id == R.id.ib_clear) {
            mEtSearch.getText().clear();
        }
    }

    private void handleSearch() {
        String content = mEtSearch.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            AppContext.showToastShort(R.string.tip_search_content_empty);
            mEtSearch.requestFocus();
            return;
        }
        mIvActionBarShadow.setVisibility(View.GONE);
        mSearchFragment.handleSearch(content);
        TDevice.hideSoftKeyboard(mEtSearch);
    }
}
