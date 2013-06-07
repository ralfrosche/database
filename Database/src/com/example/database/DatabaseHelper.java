package com.example.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/com.example.database/databases/";

	public static String DB_NAME = "mitglieder20130523";// name of your Database

	// private static String SD_DB_NAME = "Sample_databse";

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DatabaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist

		} else {

			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	public String[] ReadNRFromDB(String filter, boolean youth) {

		ArrayList temp_array = new ArrayList();
		String[] number_array = new String[0];

		String constraint = "";
		if (filter != "") {
			constraint = " WHERE Nachname like '%" + filter
					+ "%' AND status <> 'ausgetreten'";
		} else {
			constraint = " WHERE status <> 'ausgetreten'";
		}

		if (youth == true) {
			constraint += " AND jugendlicher = '1'";
		}
		String sqlQuery = "SELECT Vorname, Nachname, mitgliedsnummer FROM mitglieder "
				+ constraint + " ORDER BY Nachname";

		try {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					temp_array.add(c.getString(c.getColumnIndex("Nachname"))
							+ "," + c.getString(c.getColumnIndex("Vorname"))
							+ ","
							+ c.getString(c.getColumnIndex("mitgliedsnummer"))

					);

				} while (c.moveToNext());
			}

			c.close();
			db.close();
		} catch (SQLiteException e) {

			Log.e("MLISTE", "+++ readData +++" + e);

		}

		number_array = (String[]) temp_array.toArray(number_array);

		return number_array;
	}

	public String[] ReadFromDB(String Selecteditem) {
		String[] result_array;
		result_array = Selecteditem.split(",");
		String[] member_array = new String[10];
		String sqlQuery = "SELECT * FROM mitglieder where mitgliedsnummer = '"
				+ result_array[2] + "'";

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[0] = c.getString(c.getColumnIndex("Vorname"));
					member_array[1] = c.getString(c.getColumnIndex("Nachname"));
					member_array[2] = c.getString(c.getColumnIndex("Strasse"));
					member_array[3] = c.getString(c.getColumnIndex("PLZ"));
					member_array[4] = c.getString(c.getColumnIndex("Ort"));
					member_array[5] = c.getString(c.getColumnIndex("Telefon"));
					member_array[6] = c.getString(c.getColumnIndex("Mobil"));
					member_array[7] = c.getString(c.getColumnIndex("Email"));
					member_array[8] = c.getString(c
							.getColumnIndex("mitgliedsnummer"));
					member_array[9] = c.getString(c.getColumnIndex("status"));
				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return member_array;
	}

	public String[] ReadStatsFromDB() {

		String[] member_array = new String[7];
		String sqlQuery = "";

		try {
			SQLiteDatabase db = this.getWritableDatabase();

			sqlQuery = "SELECT count(*) as gesamt  FROM mitglieder WHERE status <> 'ausgetreten' ";
			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[0] = c.getString(c.getColumnIndex("gesamt"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as aktive  FROM mitglieder WHERE status = 'aktiv' ";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[1] = c.getString(c.getColumnIndex("aktive"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as ehrenmitglieder  FROM mitglieder WHERE status =  'Ehrenmitglied'";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[3] = c.getString(c
							.getColumnIndex("ehrenmitglieder"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as foerdermitglied  FROM mitglieder WHERE status =  'Foerdermitglied'";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[4] = c.getString(c
							.getColumnIndex("foerdermitglied"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as ruhend  FROM mitglieder WHERE status =  'ruhendes Mitglied'";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[5] = c.getString(c.getColumnIndex("ruhend"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as jugendliche  FROM mitglieder WHERE jugendlicher =  '1' and status = 'aktiv'";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[2] = c.getString(c
							.getColumnIndex("jugendliche"));

				} while (c.moveToNext());
			}

			c.close();

			sqlQuery = "SELECT count(*) as austritte  FROM mitglieder WHERE austritt =  '31.12.2013' and status = 'aktiv'";
			c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					member_array[6] = c
							.getString(c.getColumnIndex("austritte"));

				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return member_array;
	}

	public String[] ReadAlterFromDB() {

		ArrayList temp_array = new ArrayList();
		String[] number_array = new String[0];

		String sqlQuery = "SELECT Lebensalter, count(Lebensalter) as summe_alter FROM mitglieder where status <> 'ausgetreten' GROUP BY Lebensalter ORDER BY summe_alter DESC";

		try {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					temp_array.add(c.getString(c.getColumnIndex("Lebensalter"))
							+ " Jahre: "
							+ c.getString(c.getColumnIndex("summe_alter"))

					);

				} while (c.moveToNext());
			}

			c.close();
			db.close();
		} catch (SQLiteException e) {

			Log.e("MLISTE", "+++ readData +++" + e);
		}

		number_array = (String[]) temp_array.toArray(number_array);

		return number_array;

	}

	public String[] ReadAverageAlterFromDB() {

		ArrayList temp_array = new ArrayList();
		String[] number_array = new String[0];

		String sqlQuery = "select sum(Lebensalter)/count(Lebensalter) as average_age from mitglieder where status <>'ausgetreten'";

		try {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					temp_array.add(c.getString(c.getColumnIndex("average_age"))
							+ " Jahre");

				} while (c.moveToNext());
			}

			c.close();
			db.close();
		} catch (SQLiteException e) {

			Log.e("MLISTE", "+++ readData +++" + e);
		}

		number_array = (String[]) temp_array.toArray(number_array);

		return number_array;

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	public boolean checkDataBase() {
		Log.e("MLISTE", "+++ checkDataBase +++");
		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		Log.e("MLISTE", "+++ copyDataBase +++");

		InputStream myInput = myContext.getAssets().open(DB_NAME);

		String outFileName = DB_PATH + DB_NAME;

		OutputStream myOutput = new FileOutputStream(outFileName);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}