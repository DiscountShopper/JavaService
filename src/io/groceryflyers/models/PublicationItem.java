package io.groceryflyers.models;

import com.google.gson.annotations.SerializedName;
import io.groceryflyers.fetchers.AbstractProvider;
import io.groceryflyers.models.utils.MappableTo;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jeremiep on 2016-01-30.
 */
public class PublicationItem implements MappableTo<Document> {
    @SerializedName("identifier")
    public String identifier;

    @SerializedName("title_fr")
    public String title_fr;

    @SerializedName("title_en")
    public String title_en;

    @SerializedName("description")
    public String description;

    @SerializedName("brand_fr")
    public String brand_fr;

    @SerializedName("brand_en")
    public String brand_en;

    @SerializedName("category_id")
    public String category_id;

    @SerializedName("category_fr")
    public String category_fr;

    @SerializedName("category_en")
    public String category_en;

    @SerializedName("link")
    public String link;

    @SerializedName("image")
    public String image;

    @SerializedName("imageThumbnail")
    public String imageThumb;

    @SerializedName("price")
    public String price;

    @SerializedName("price_number")
    public float price_number;

    @SerializedName("price_unit")
    public String price_unit;

    @SerializedName("key_words")
    public String[] key_words;

    @SerializedName("search_key_words")
    public String[] search_key_words;

    @SerializedName("banner_code")
    public String banner_code;

    @SerializedName("effective_start_date")
    public String effective_start_date;

    @SerializedName("effective_end_date")
    public String effective_end_date;

    @SerializedName("publication_id")
    public String publication_id;

    @SerializedName("page_number")
    public Integer page_number;

    @Override
    public int hashCode() {
        return this.title_fr.hashCode();
    }

    @Override
    public Document mapToBusinessModel(AbstractProvider p) {
        Document document = new Document();

        document.put("identifier", this.identifier);
        document.put("title_fr", this.title_fr);
        document.put("title_en", this.title_en);
        document.put("description", this.description);
        document.put("brand_fr", this.brand_fr);
        document.put("brand_en", this.brand_en);
        document.put("category_id", this.category_id);
        document.put("category_fr", this.category_fr);
        document.put("category_en", this.category_en);
        document.put("link", this.link);
        document.put("image", this.image);
        document.put("imageThumb", this.imageThumb);
        document.put("price", this.price);
        document.put("price_unit", this.price_unit);

        document.put("key_words", Arrays.asList(this.key_words));
        document.put("search_key_words", Arrays.asList(this.search_key_words));

        document.put("banner_code", this.banner_code);
        document.put("effective_start_date", this.effective_start_date);
        document.put("effective_end_date", this.effective_end_date);
        document.put("publication_id", this.publication_id);
        document.put("price_number", this.price_number);
        document.put("page_number", this.page_number);

        return document;
    }
}
