package io.groceryflyers.fetchers.impl.providers;

import io.groceryflyers.fetchers.impl.EyFlyerProvider;

import java.util.regex.Pattern;

/**
 * Created by jeremiep on 2016-01-30.
 */
public class LoblawsProvider extends EyFlyerProvider {
    private static Pattern PRODUCT_TITLE_PATT = Pattern.compile("^\\*?(.*)$");
    private static Pattern PRODUCT_KEYWORDS_PATT = Pattern.compile("^(.*) \\| (.*)$", Pattern.MULTILINE);

    @Override
    public String getProductTitleFrench(String title) {
        return title;
    }

    @Override
    public String getProductTitleEnglish(String title) {
        return "";
    }

    @Override
    public String[] getKeywords(String keywords) {
        return keywords.split(" ");
    }
}
