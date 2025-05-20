use ('RealEstateDB');

db.postcode.drop();
db.property.drop();


//db.createCollection("postcode")
//db.createCollection("property")

db.createCollection("postcode", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["post_code"],
      properties: {
        post_code: {
          bsonType: "string",
          description: "must be a string and is required"
        },
        view_count_postcode: {
          bsonType: "int",
          description: "must be an integer if present"
        }
      }
    }
  }
})

db.createCollection("property", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      properties: {
        property_id: { bsonType: ["string", "null"] },
        download_date: { bsonType: ["string", "null"] },
        council_name: { bsonType: ["string", "null"] },
        purchase_price: { bsonType: ["string", "null"] },
        address: { bsonType: ["string", "null"] },
        post_code: { bsonType: ["string", "null"] },
        property_type: { bsonType: ["string", "null"] },
        strata_lot_number: { bsonType: ["string", "null"] },
        property_name: { bsonType: ["string", "null"] },
        area_type: { bsonType: ["string", "null"] },
        contract_date: { bsonType: ["string", "null"] },
        settlement_date: { bsonType: ["string", "null"] },
        zoning: { bsonType: ["string", "null"] },
        nature_of_property: { bsonType: ["string", "null"] },
        primary_purpose: { bsonType: ["string", "null"] },
        legal_description: { bsonType: ["string", "null"] },
        view_count: { bsonType: "int" }
      }
    }
  }
})


