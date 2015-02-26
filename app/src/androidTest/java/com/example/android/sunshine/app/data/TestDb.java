/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }


    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.  Return
        the rowId of the inserted location.
    */
    public long testLocationTable() {
        // First step: Get reference to writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues cValues = TestUtilities.createNorthPoleLocationValues();

        // Input Values
        String inputLocationSetting = cValues.getAsString(LocationEntry.COLUMN_LOCATION_SETTING);
        Float inputLongitude = cValues.getAsFloat(LocationEntry.COLUMN_COORD_LONG);
        Float inputLatitude = cValues.getAsFloat(LocationEntry.COLUMN_COORD_LAT);
        String inputCityName = cValues.getAsString(LocationEntry.COLUMN_CITY_NAME);

        String outputLocationSetting = null;
        float outputLongitude = (float) 0.0;
        float outputLatitude =  (float)0.0;
        String outputCityName = null;

        // Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, cValues);

        assertTrue(locationRowId != -1);
        // Query the database and receive a Cursor back
        Cursor c = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No rows inserted into Location table", c.moveToFirst());

        outputLocationSetting = c.getString(c.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING));
        outputLatitude = c.getFloat(c.getColumnIndex(LocationEntry.COLUMN_COORD_LAT));
        outputLongitude = c.getFloat(c.getColumnIndex(LocationEntry.COLUMN_COORD_LONG));
        outputCityName = c.getString(c.getColumnIndex(LocationEntry.COLUMN_CITY_NAME));

        // Validate data in resulting Cursor with the original ContentValues
        assertEquals("Location Setting input "+inputLocationSetting + "<>" + " location setting output " + outputLocationSetting ,
                inputLocationSetting, outputLocationSetting);
        assertEquals("Longitude input "+inputLongitude + "<>" + " longitude setting output " + outputLongitude ,
                inputLongitude, outputLongitude);
        assertEquals("Latitude input "+inputLatitude + "<>" + " latitude setting output " + outputLatitude ,
                inputLatitude, outputLatitude);

        assertEquals("City name input "+inputCityName + "<>" + " city name output " + outputCityName ,
                inputCityName, outputCityName);

        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        assertFalse("Error: Extra Rows in Location table ", c.moveToNext());

        // Finally, close the cursor and database
        c.close();
        db.close();
        // Return the rowId of the inserted location, or "-1" on failure.
        return locationRowId;
    }


    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {


        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        ContentValues cValues = TestUtilities.createNewPaltzLocationValues();
        long locationRowId = testLocationTable();

        // We return the rowId of the inserted location in testLocationTable, so
        // you should just call that function rather than rewriting it
        assertFalse("Error: No Location row created", locationRowId == -1);

        // First step: Get reference to writable database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues cv = TestUtilities.createWeatherValues(locationRowId);

        // Insert ContentValues into database and get a row ID back
        locationRowId = -1;
        locationRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, cv);

        assertTrue(locationRowId != -1);
        // Query the database and receive a Cursor back
        Cursor c = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        // Move the cursor to a valid database row
        assertTrue("Error: No rows inserted into Location table", c.moveToFirst());
        // Validate data in resulting Cursor with the original ContentValues

        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Input and Output rows do not match", c, cv);

        // Finally, close the cursor and database
        c.close();
        db.close();
    }
}
