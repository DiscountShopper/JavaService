package io.groceryflyers.fetchers.impl;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.Mongo;
import io.groceryflyers.datastore.MongoDatastore;
import io.groceryflyers.fetchers.AbstractFetcher;
import io.groceryflyers.fetchers.impl.models.EyFlyersCategories;
import io.groceryflyers.fetchers.impl.models.EyFlyersPublications;
import io.groceryflyers.fetchers.impl.models.EyFlyersPublicationsItems;
import io.groceryflyers.fetchers.impl.models.EyFlyersStores;
import io.groceryflyers.fetchers.impl.providers.*;
import io.groceryflyers.models.*;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jeremiep on 2016-01-30.
 */
public class EyFlyerFetcher extends AbstractFetcher {
    private static Logger LOG = Logger.getLogger(EyFlyerFetcher.class);
    public enum EyFlyersProviders {
        SUPER_C("http://eflyer.metro.ca/SUPRC/SUPRC", "SUPERC", new SuperCProvider()),
        MAXI("http://eflyer.metro.ca/MAXI/MAXI", "MAXI", new MaxiProvider()),
        IGA("http://eflyer.metro.ca/IGA/IGA", "IGA", new IGAProvider()),
        METRO("http://eflyer.metro.ca/MTR/MTR", "METRO", new MetroProvider()),
        LOBLAWS("http://eflyer.metro.ca/LOB/LOB", "LOBLAWS", new LoblawsProvider());

        private String base_url;
        private String code;
        private EyFlyerProvider provider;
        EyFlyersProviders(String base_url, String code, EyFlyerProvider provider) {
            this.base_url = base_url;
            this.code = code;
            this.provider = provider;
        }

        public String getBannerCode() { return this.code; }

        public String getBaseUrl() {
            return this.base_url;
        }

        public String getStoresNearbyPostalCodeUrl(String postalCode) {
            return this.getBaseUrl().concat("/fr/Landing/GetClosestStoresByPostalCode?" +
                    "orgCode=9999&" +
                    "bannerCode=9999&" +
                    "countryCode=CA&" +
                    "postalCode=" + postalCode + "&" +
                    "culture=fr");
        }

        public String getPublicationItemsByPubGuid(String guid) {
            return this.getBaseUrl().concat("/fr/" + guid + "/Product/ListAllProducts");
        }

        public String getPublicationByStoreId(String sguid) {
            return this.getBaseUrl().concat("/fr/Landing/GetPublicationsByStoreId?" +
                    "storeId=" + sguid);
        }

        public String getCategoriesByPublication(String pguid) {
            return this.getBaseUrl().concat("/fr/" + pguid + "/Publication/Categories");
        }

        public EyFlyerProvider getProvider() { return this.provider; }

        public static EyFlyersProviders getProviderFromString(String provider) {
            switch(provider){
                case "SUPERC":
                    return SUPER_C;
                case "MAXI":
                    return MAXI;
                case "IGA":
                    return IGA;
                case "METRO":
                    return METRO;
                case "LOBLAWS":
                    return LOBLAWS;
                default:
                    return null;
            }
        }
    };

    @Override
    public List<Store> getStoreNearby(EyFlyersProviders provider, String postalCode) {
        try {
            LOG.info(provider.getStoresNearbyPostalCodeUrl(postalCode));
            HttpRequest req = this.getDefaultHttpFactory().buildGetRequest(
                    new GenericUrl(provider.getStoresNearbyPostalCodeUrl(postalCode))
            );

            List<EyFlyersStores> stores = req.execute().parseAs(EyFlyersStores.eyFlyersStoresList.class).storeList;
            return stores.stream().map( item -> (
                    (Function<EyFlyersStores, Store>) map -> {
                        return map.mapToBusinessModel(provider.getProvider());
                    }).apply(item)
            ).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Store> getAllStoreNearby(String postalCode) {
        List<Store> allStores = new ArrayList<>();

        for(EyFlyersProviders provider: EyFlyersProviders.values()){
            allStores.addAll(getStoreNearby(provider, postalCode));
        }

        return allStores;
    }

    @Override
    public List<PublicationItem> getAllPublicationItems(EyFlyersProviders provider, String pguid) {
        try {
            LOG.info(provider.getPublicationItemsByPubGuid(pguid));
            HttpRequest req = this.getDefaultHttpFactory().buildGetRequest(
                    new GenericUrl(provider.getPublicationItemsByPubGuid(pguid))
            );

            List<EyFlyersPublicationsItems> items = req.execute().parseAs(EyFlyersPublicationsItems.EyFlyersPublicationItemsList.class).products;
            return items.stream().map( item -> (
                    (Function<EyFlyersPublicationsItems, PublicationItem>) map -> {
                        return map.mapToBusinessModel(provider.getProvider());
                    }).apply(item)
            ).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Publication> getAllPublicationByStore(EyFlyersProviders provider, String sguid) {
        try {
            LOG.info(provider.getPublicationByStoreId(sguid));
            HttpRequest req = this.getDefaultHttpFactory().buildGetRequest(
                    new GenericUrl(provider.getPublicationByStoreId(sguid))
            );

            List<EyFlyersPublications> publications =
                    req.execute().parseAs(EyFlyersPublications.EyFlyersPublicationsList.class).publications;
            return publications.stream().map( item -> (
                    (Function<EyFlyersPublications, Publication>) map -> {
                        return map.mapToBusinessModel(provider.getProvider());
                    }).apply(item)
            ).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PublicationSet> getAllPublicationSetsByStore(EyFlyersProviders provider, String sguid) {
        LinkedList<PublicationSet> pset = new LinkedList<PublicationSet>();
        for(Publication pub : this.getAllPublicationByStore(provider, sguid)) {
            Optional<Document> existingPub = MongoDatastore.getInstance().findPublicationIfAvailable(pub.id);
            if(existingPub.isPresent()) {
                PublicationSet existingSet = new GsonBuilder().create().fromJson(existingPub.get().toJson(), PublicationSet.class);
                pset.add(existingSet);
                LOG.debug("Cached request " + existingSet.publication.id);
                continue;
            }

            PublicationSet set = new PublicationSet();
            set.publication = pub;
            set.banner = provider.getBannerCode();
            set.items = this.getAllPublicationItems(provider, pub.id);

            MongoDatastore.getInstance().storeModel(PublicationSet.MONGO_DOCUMENT_NAME, set);
            pset.add(set);
        }

        return pset;
    }

    @Override
    public List<Category> getAllCategoriesByPublication(EyFlyersProviders provider, String pguid) {
        try {
            HttpRequest req = this.getDefaultHttpFactory().buildGetRequest(
                    new GenericUrl(provider.getCategoriesByPublication(pguid))
            );

            EyFlyersCategories[] categories = req.execute().parseAs(EyFlyersCategories[].class);

            return Arrays.asList(categories).stream().map(item -> (
                    (Function<EyFlyersCategories, Category>) map -> {
                        return map.mapToBusinessModel(provider.getProvider());
                    }).apply(item)
            ).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PublicationSet> getAllPublicationSetsForAllStores(String postalCode) {
        List<PublicationSet> sets = new LinkedList<PublicationSet>();
        for(EyFlyersProviders p : EyFlyersProviders.values()) {
            List<Store> nearbyStores = this.getStoreNearby(p, postalCode);
            if(nearbyStores.size() > 0) {
                for(PublicationSet set : this.getAllPublicationSetsByStore(p, nearbyStores.get(0).guid)) {
                    sets.add(set);
                }
            }
        }

        return sets;
    }

    public List<PublicationItem> getRelatedProducts(String[] keywords, String postalCode) {
        List<PublicationSet> sets = this.getAllPublicationSetsForAllStores(postalCode);
        LinkedList<PublicationItem> result = new LinkedList<>();

        for(PublicationSet set : sets) {
            for(PublicationItem item : set.items) {
                Set<String> s1 = new HashSet<String>(Arrays.asList(keywords));
                Set<String> s2 = new HashSet<String>(Arrays.asList(item.keywords));

                s1.retainAll(s2);

                if(s1.size() >= 1) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    public List<Category> getAllCategories(String postalCode) {
        List<Category> result = new LinkedList<Category>();
        for(PublicationSet pubSet : this.getAllPublicationSetsForAllStores(postalCode)) {
            List<Category> categories = this.getAllCategoriesByPublication(
                    EyFlyersProviders.getProviderFromString(pubSet.banner),
                    pubSet.publication.id);
            for(Category cat : categories) {
                if(!result.contains(cat)) {
                    result.add(cat);
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        EyFlyerFetcher fetcher = new EyFlyerFetcher();

        //ArrayList<String> strs = new ArrayList<>();
        //strs.add("meat");
        //strs.add("steak");
        List<PublicationItem> items = fetcher.getRelatedProducts(new String[] { "biscuits", "granola" }, "h1x2t9");
        System.out.println(items.size());
    }
}
