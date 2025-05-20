package sales;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

import org.bson.conversions.Bson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import sales.HomeSale;

public class SalesDAO {

    private static final String DB_URI = "mongodb://root:123@localhost:27017";
    private static final String DB_NAME = "RealEstateDB";
    private MongoDatabase database;
    private MongoCollection<Document> properties;
    private MongoCollection<Document> postcodes;
    private MongoClient client;
    
    public SalesDAO() {
        MongoClient client = MongoClients.create(DB_URI);
        this.database = client.getDatabase(DB_NAME);
        this.properties = database.getCollection("property");
        this.postcodes = database.getCollection("postcode");
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    public boolean newSale(HomeSale homeSale) {
        Document doc = new Document("property_id", homeSale.getPropertyId())
        .append("purchase_price", Double.parseDouble(homeSale.getPurchasePrice()))
        .append("post_code", homeSale.getPostCode())
        .append("download_date", homeSale.getDownloadDate())
        .append("council_name", homeSale.getCouncilName())
        .append("address", homeSale.getAddress())
        .append("nature_of_property", homeSale.getNatureOfProperty())
        .append("strata_lot_number", Integer.parseInt(homeSale.getStrataLotNumber()))
        .append("property_name", homeSale.getPropertyName())
        .append("area_type", homeSale.getAreaType())
        .append("contract_date", homeSale.getContractDate())
        .append("settlement_date", homeSale.getSettlementDate())
        .append("zoning", homeSale.getZoning())
        .append("primary_purpose", homeSale.getPrimaryPurpose())
        .append("legal_description", homeSale.getLegalDescription())
        .append("property_type", homeSale.getPropertyType())
        .append("view_count", 0);

        properties.insertOne(doc);
        return true;
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public Optional<HomeSale> getSaleById(String saleID) {
        properties.updateOne(eq("property_id", saleID), Updates.inc("view_count", 1));
        Document doc = properties.find(eq("property_id", saleID)).first();

        if (doc != null) {
            return Optional.of(convertDocToHomeSale(doc));
        }

        return Optional.empty();
    }
    
    // returns a List of home sales in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        postcodes.updateOne(eq("post_code", postCode), Updates.inc("view_count_postcode", 1));

        List<HomeSale> results = new ArrayList<>();
        for (Document doc : properties.find(eq("post_code", postCode)).limit(20)) {
            results.add(convertDocToHomeSale(doc));
        }
        return results;
    }
    
 // returns all home sales. Potentially large
 public List<HomeSale> getAllSales() {

   List<HomeSale> salesList = new ArrayList<>();


        // Limit to 20 results
        FindIterable<Document> docs = properties.find().limit(20);

        for (Document doc : docs) {
            salesList.add(convertDocToHomeSale(doc));
        }

    return salesList;
}

//returns 20 houses closest to and under the upper budget given
public List<HomeSale> getUnderBudget(String upperBudget) {

    List<HomeSale> results = new ArrayList<>();
    double budget = Double.parseDouble(upperBudget);

    for (Document doc : properties.find(Filters.lte("purchase_price", budget))
                                  .sort(new Document("purchase_price", -1))
                                  .limit(20)) {
        results.add(convertDocToHomeSale(doc));
    }

    return results;
}

//returns the average price of properties within a given postcode
public Double getAveragePriceInPostcode(String postcode) {

    Document match = new Document("$match", new Document("post_code", postcode));
    Document group = new Document("$group",
        new Document("_id", null)
        .append("average_price", new Document("$avg", "$purchase_price"))
    );

    List<Document> pipeline = List.of(match, group);
    Document result = properties.aggregate(pipeline).first();

    return result != null ? result.getDouble("average_price") : null;
}

/**
     * Utility method to populate a HomeSale object from a database ResultSet.
     *
     * @param sale the HomeSale object to populate
     * @param rs   the ResultSet from the executed query
     * @throws SQLException if an error occurs accessing result set data
     */
    private HomeSale convertDocToHomeSale(Document doc) {
        HomeSale sale = new HomeSale();
        sale.setPropertyId(doc.getString("property_id"));
        sale.setPurchasePrice(String.valueOf(doc.get("purchase_price")));
        sale.setPostCode(doc.getString("post_code"));
        sale.setDownloadDate(doc.getString("download_date"));
        sale.setCouncilName(doc.getString("council_name"));
        sale.setAddress(doc.getString("address"));
        sale.setNatureOfProperty(doc.getString("nature_of_property"));
        sale.setStrataLotNumber(String.valueOf(doc.get("strata_lot_number")));
        sale.setPropertyName(doc.getString("property_name"));
        sale.setAreaType(doc.getString("area_type"));
        sale.setContractDate(doc.getString("contract_date"));
        sale.setSettlementDate(doc.getString("settlement_date"));
        sale.setZoning(doc.getString("zoning"));
        sale.setPrimaryPurpose(doc.getString("primary_purpose"));
        sale.setLegalDescription(doc.getString("legal_description"));
        sale.setPropertyType(doc.getString("property_type"));
        sale.setCount(doc.getInteger("view_count", 0));
    
        // Fetch postcode view count from 'postcode' collection
        Document postCodeDoc = postcodes.find(eq("post_code", doc.getString("post_code"))).first();
        if (postCodeDoc != null) {
            sale.setPostCodeCount(postCodeDoc.getInteger("view_count_postcode", 0));
        }
    
        return sale;
    }
    
}