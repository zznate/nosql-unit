package com.lordofthejars.nosqlunit.mongodb;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lordofthejars.nosqlunit.core.DatabaseOperation;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.util.JSON;

public final class MongoOperation implements DatabaseOperation {

	private static Logger LOGGER = LoggerFactory.getLogger(MongoOptions.class);
	
	private Mongo mongo;
	private MongoDbConfiguration mongoDbConfiguration;

	public MongoOperation(Mongo mongo, MongoDbConfiguration mongoDbConfiguration) {
		this.mongo = mongo;
		this.mongoDbConfiguration = mongoDbConfiguration;
	}

	@Override
	public void insert(String jsonData) {

		try {

			DBObject parsedData = parseData(jsonData);
			DB mongoDb = getMongoDb();

			insertParsedData(parsedData, mongoDb);

		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Unexpected error reading data set file.", e);
		}

	}

	private void insertParsedData(DBObject parsedData, DB mongoDb) {
		Set<String> collectionaNames = parsedData.keySet();

		for (String collectionName : collectionaNames) {

			BasicDBList dataObjects = (BasicDBList) parsedData
					.get(collectionName);

			DBCollection dbCollection = mongoDb.getCollection(collectionName);

			for (Object dataObject : dataObjects) {
				
				LOGGER.debug("Inserting {} To {}.", dataObject, dbCollection.getName());
				
				dbCollection.insert((DBObject) dataObject);
			}

		}
	}

	@Override
	public void deleteAll() {
		DB mongoDb = getMongoDb();
		deleteAllElements(mongoDb);
	}

	private void deleteAllElements(DB mongoDb) {
		Set<String> collectionaNames = mongoDb.getCollectionNames();

		for (String collectionName : collectionaNames) {
			
			if(isNotASystemCollection(collectionName)) {
				
				LOGGER.debug("Dropping Collection {}.", collectionName);
				
				DBCollection dbCollection = mongoDb.getCollection(collectionName);
				dbCollection.drop();
			}
		}
	}

	private boolean isNotASystemCollection(String collectionName) {
		return !collectionName.startsWith("system");
	}

	@Override
	public boolean databaseIs(String expectedJsonData) {

		try {
			DBObject parsedData = parseData(expectedJsonData);
			MongoDbAssertion.strictAssertEquals(parsedData, getMongoDb());
			
			return true;
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Unexpected error reading expected data set file.", e);
		}

	}

	@Override
	public void insertNotPresent(String jsonData) {
		
		try {
			DBObject parsedData = parseData(jsonData);
			DB mongoDb = getMongoDb();
			
			insertAllElementsNotInsertedBefore(parsedData, mongoDb);
			
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Unexpected error reading expected data set file.", e);
		}
		
		
	}

	private void insertAllElementsNotInsertedBefore(DBObject parsedData,
			DB mongoDb) {
		Set<String> collectionaNames = parsedData.keySet();

		for (String collectionName : collectionaNames) {

			BasicDBList dataObjects = (BasicDBList) parsedData
					.get(collectionName);

			DBCollection dbCollection = mongoDb.getCollection(collectionName);

			for (Object dataObject : dataObjects) {
				DBObject dbObject = dbCollection.findOne((DBObject) dataObject);
				
				if(wasDbObjectNotInserted(dbObject)) {
					
					LOGGER.debug("Inserting Unique Object {} To {}.", dataObject, dbCollection.getName());
					
					dbCollection.insert((DBObject)dataObject);
				}
				
			}

		}
	}

	private boolean wasDbObjectNotInserted(DBObject dbObject) {
		return dbObject == null;
	}
	
	private DBObject parseData(String jsonData) throws IOException {
		DBObject parsedData = (DBObject) JSON.parse(jsonData);
		return parsedData;
	}

	private DB getMongoDb() {

		DB db = mongo.getDB(this.mongoDbConfiguration.getDatabaseName());

		if (this.mongoDbConfiguration.isAuthenticateParametersSet()) {
			boolean authenticated = db.authenticate(
					this.mongoDbConfiguration.getUsername(),
					this.mongoDbConfiguration.getPassword().toCharArray());

			if (!authenticated) {
				throw new IllegalArgumentException(
						"Login/Password provided to connect to MongoDb are not valid");
			}

		}

		return db;
	}

	@Override
	public Object connectionManager() {
		return mongo;
	}

}
