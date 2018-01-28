package co.nano.nanowallet.ui.home;

import co.nano.nanowallet.R;

/**
 * Created by szeidner on 12/01/2018.
 */

public enum CurrencyPagerEnum {
    NANO(R.layout.view_home_amount_nano),
    BTC(R.layout.view_home_amount_btc),
    LOCAL(R.layout.view_home_amount_local_currency);

    private int mLayoutResId;

    CurrencyPagerEnum(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}