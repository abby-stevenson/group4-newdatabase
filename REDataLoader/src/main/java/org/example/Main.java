package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;

public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
        .setHeader()
        .setSkipHeaderRecord(true)
        .setAllowDuplicateHeaderNames(false)
        .build();

    static final private String PATH_TO_FILE =
        "/Users/abbystevenson/Desktop/Northeastern/Second Year Classes/DOC/group4/REDataLoader/nsw_property_data.csv";

    // MongoDB connection details
    private static final String MONGO_URI = "mongodb://root:123@localhost:27017";
    private static final String DB_NAME = "RealEstateDB";

    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI);
             CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {

            MongoDatabase database = mongoClient.getDatabase(DB_NAME);

            // Now we safely get both collections
            MongoCollection<Document> propertyCollection = database.getCollection("property");
            MongoCollection<Document> postcodeCollection = database.getCollection("postcode");

            int count = 0;
            for (CSVRecord record : parser) {
                Map<String, String> recordValues = record.toMap();
                System.out.println(recordValues);

                String postCode = record.get("post_code");

                // Insert postcode document if not present
                if (postCode != null && !postCode.isBlank()) {
                    if (postcodeCollection.find(eq("post_code", postCode)).first() == null) {
                        Document postcodeDoc = new Document("post_code", postCode)
                            .append("view_count_postcode", 0);
                        postcodeCollection.insertOne(postcodeDoc);
                    }
                }

                Document propertyDoc = new Document()
                    .append("property_id", record.get("property_id"))
                    .append("address", record.get("address"))
                    .append("purchase_price", record.get("purchase_price"))
                    .append("post_code", postCode)
                    .append("download_date", record.get("download_date"))
                    .append("council_name", record.get("council_name"))
                    .append("view_count", 0); // Initialize view_count

                propertyCollection.insertOne(propertyDoc);

                count++;
                System.out.println("Inserted record count: " + count);
            }

            System.out.println("Total records inserted: " + count);

        } catch (IOException e) {
            System.err.println("Failed to open CSV file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
