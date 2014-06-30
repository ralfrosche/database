package com.example.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.jost_net.OBanToo.Dtaus.DtausDateiWriter;
import de.jost_net.OBanToo.Dtaus.DtausException;

import android.annotation.SuppressLint;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.Environment;

import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.*;

@SuppressLint("NewApi")
public class DatabaseHelper extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/com.example.database/databases/";
	private static final String TABLE_NAME_VERSION = "version";
	public static final String PROGRAMM_VERSION = "1";

	public static String DB_NAME = "mitglieder20140417";

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	public int actualVersion = 0;

	public ArrayList<String> upgrades = new ArrayList<String>();

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DatabaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
		upgrades.clear();
	}

	public boolean upgradeDatabaseVersion(Context context) {
		for (int i = actualVersion; i < upgrades.size(); i++) {
			String sqlQuery = upgrades.get(i);
			try {
				SQLiteDatabase db = this.getWritableDatabase();

				db.execSQL(sqlQuery);

				db.close();

			} catch (SQLiteException e) {
				Log.e("MLISTE", "+++ upgraded failed +++" + e);
				return false;
			}

		}
		return true;

	}

	public boolean checkDatabaseVersion() {
		String sqlDataStore = "create table if not exists "
				+ TABLE_NAME_VERSION
				+ " (id integer primary key autoincrement,"
				+ " version text not null default '0')";

		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlDataStore);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ upgraded failed +++" + e);
			return false;
		}

		String sqlQuery = "SELECT version from version WHERE 1 ORDER BY ID DESC LIMIT 1";
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor c = db.rawQuery(sqlQuery, null);
			String version = "0";
			if (c.moveToFirst()) {
				do {

					version = c.getString(c.getColumnIndex("version"));
					// Log.e("MLISTE", "+++ get version from database:"
					// +version);

				} while (c.moveToNext());
			}
			c.close();
			db.close();
			actualVersion = Integer.parseInt(version);

			if (!version.equals(PROGRAMM_VERSION)) {
				try {
					db = this.getWritableDatabase();
					String sqlQueryDelete = "DELETE from version WHERE 1";
					sqlQuery = "INSERT INTO version  (version) VALUES ('"
							+ PROGRAMM_VERSION + "')";
					db.execSQL(sqlQueryDelete);
					db.execSQL(sqlQuery);
					db.close();
					return true;

				} catch (SQLiteException e) {
					Log.e("MLISTE", "+++ upgraded failed +++" + e);
					return false;
				}

			} else {

				return false;
			}

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ upgraded failed +++" + e);
			return false;
		}

	}

	public void deleteMember(Integer mitgliedsnummer) {

		String sqlQuery = "DELETE FROM mitglieder WHERE mitgliedsnummer='"
				+ String.valueOf(mitgliedsnummer) + "'";

		//Log.e("MLISTE", "+++ deleteflight +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ deleteflight +++" + e);
		}
		MainActivity.updateView = true;
	}

	public boolean importDataBaseComplete(Context context) throws IOException {

		try {

			File sdCard = Environment.getExternalStorageDirectory();
			String dir = sdCard.getAbsolutePath() + "/mliste/import/";
			String inFilename = "mitglieder.csv";

			FileReader fr = new FileReader(dir + inFilename);
			BufferedReader br = new BufferedReader(fr);

			String data = "";
			String tableName = "mitglieder";
			ArrayList<String> colums = getTableHeader(tableName);
			Integer length = colums.size();

			String columns = "";
			Integer posID = 0;
			Integer i = 0;
			for (String s : colums) {
				if (s.equals("mitgliedsnummer")) {
					posID = i;
				}
				i++;
				columns += s + ",";
			}
			columns = columns.substring(0, columns.length() - 1);

			String InsertString1 = "INSERT INTO " + tableName + " (" + columns
					+ ") values (";
			String InsertString2 = ")";
			while ((data = br.readLine()) != null) {

				Integer nextval = getId("mitglieder", "mitgliedsnummer");

				String[] sarray = data.split(";");

				String sqlQuery = "";
				if (sarray.length == length) {

					StringBuilder sb = new StringBuilder(InsertString1);
					String anrede = sarray[1].replaceAll("\"", "").trim()
							.toLowerCase(Locale.GERMANY);
					if (!anrede.equals("vorname")) {
						i = 0;
						for (String s : colums) {
							if (i == posID) {
								sb.append("\"" + String.valueOf(nextval)
										+ "\",");
							} else {
								sb.append("\"" + sarray[i].replaceAll("\"", "")
										+ "\",");
							}

							i++;
						}

						sqlQuery = sb.toString();
						sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);

						sqlQuery += InsertString2;
						//Log.e("MLISTE", "+++ SQL importCSV:" + sqlQuery);

						try {
							SQLiteDatabase db = this.getWritableDatabase();
							db.execSQL(sqlQuery);
							db.close();

						} catch (SQLiteException e) {
							Log.e("MLISTE", "+++ get id +++" + e);

						}
					}
				} else {
					Toast.makeText(
							context,
							"Error: Datensatzlšnge nicht korrekt! L:"
									+ String.valueOf(sarray.length),
							Toast.LENGTH_SHORT).show();
				}

			}

			br.close();
			fr.close();
			File file = new File(dir + inFilename);
			file.delete();
			MainActivity.updateView = true;
			return true;

		} catch (IOException e) {

			Toast.makeText(context,
					"ERROR:Import der Datenbank fehlgeschlagen!" + e,
					Toast.LENGTH_LONG).show();
			//Log.e("MLISTE", "+++ ERROR exportDataBaseComplete +++" + e);
			return false;
		}

	}

	public Integer writeDtaus(Integer run) throws Exception {
		FileOutputStream fos = null;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/dtaus/");
		dir.mkdir();
		String outFilename = "";
		String logFilename = "";
		String errFilename = "";
		if (run == 1) {
			outFilename = "dtaus0_1";
		} else {
			outFilename = "dtaus0_2";
		}
		if (run == 1) {
			logFilename = "dtaus0_1.log";
			errFilename = "dtaus0_1.err";
		} else {
			logFilename = "dtaus0_2.log";
			errFilename = "dtaus0_2.err";
		}

		File f = new File(dir, outFilename);
		f.createNewFile();
		try {
			fos = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			return null;
		}

		File l = new File(dir, logFilename);
		l.createNewFile();
		File e = new File(dir, errFilename);
		e.createNewFile();

		OutputStream logOutput = new FileOutputStream(l);
		OutputStreamWriter printStreamLog = new OutputStreamWriter(logOutput,
				"CP1252");

		OutputStream errOutput = new FileOutputStream(e);
		OutputStreamWriter printStreamErr = new OutputStreamWriter(errOutput,
				"CP1252");

		String separation = ";";
		String stHeader = "";
		stHeader = '"' + "Nr" + '"' + separation + '"' + "Status" + '"'
				+ separation + '"' + "Vorname" + '"' + separation + '"'
				+ "Nachname" + '"' + separation + '"' + "kontoinhaber" + '"'
				+ separation + '"' + "mitgliedsnummer" + '"' + separation + '"'
				+ "abbuchung" + '"' + separation + '"' + "fremdkonto" + '"'
				+ separation + '"' + "blz" + '"' + separation + '"' + "konto"
				+ '"' + separation + '"' + "iban" + '"' + separation + '"'
				+ "betrag" + '"' + separation + '"' + "verwendungszweck" + '"'
				+ "\n";
		printStreamLog.write(stHeader);
		printStreamErr.write(stHeader);
		DtausDateiWriter dtausDateiWriter = new DtausDateiWriter(fos);

		try {
			dtausDateiWriter.setAGutschriftLastschrift("LK");
		} catch (DtausException er) {
			printStreamLog.flush();
			printStreamErr.flush();
			printStreamLog.close();
			printStreamErr.close();
			return null;
		}
		Map<String, String> prefs = null;
		prefs = getPreferncies();
		String name = prefs.get("name");
		String blz = null;
		String kontonummer = null;
		blz = prefs.get("blz");
		kontonummer = prefs.get("kontonummer");
		if (blz != null)
			dtausDateiWriter.setABLZBank(Long.parseLong(blz));
		dtausDateiWriter.setAKundenname(name);
		if (kontonummer != null)
			dtausDateiWriter.setAKonto(Long.parseLong(kontonummer));
		dtausDateiWriter.writeASatz();
		String sqlQuery = "SELECT Vorname, Nachname,kontoinhaber, mitgliedsnummer,abbuchung, fremdkonto,blz, konto,iban, betrag1,betrag2, verwendungszweck FROM mitglieder WHERE status='aktiv' ORDER BY CAST(mitgliedsnummer AS INTEGER) ASC";
		Integer i = 0;
		Integer errornr = 0;
		long summe = 0L;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					String Vorname = c.getString(c.getColumnIndex("Vorname"));
					String Nachname = c.getString(c.getColumnIndex("Nachname"));
					String mitgliedsnummer = c.getString(c
							.getColumnIndex("mitgliedsnummer"));
					String bank = c.getString(c.getColumnIndex("blz"));
					bank = bank.replaceAll(" ", "");
					String konto = c.getString(c.getColumnIndex("konto"));
					konto = konto.replaceAll(" ", "");
					String iban = c.getString(c.getColumnIndex("iban"));
					iban = iban.replaceAll(" ", "");
					String betrag1 = c.getString(c.getColumnIndex("betrag1"));
					String betrag2 = c.getString(c.getColumnIndex("betrag2"));
					betrag1 = betrag1.replaceAll(" ", "");
					betrag2 = betrag2.replaceAll(" ", "");

					String betrag = "";

					if (run == 1) {
						betrag = betrag1;
					} else {
						betrag = betrag2;
					}

					String kontoinhaber = c.getString(c
							.getColumnIndex("kontoinhaber"));
					String verwendungszweck = c.getString(c
							.getColumnIndex("verwendungszweck"));
					Integer fremdkonto = Integer.parseInt(c.getString(c
							.getColumnIndex("fremdkonto")));
					Integer abbuchung = Integer.parseInt(c.getString(c
							.getColumnIndex("abbuchung")));
					String Cname = "";
					if (fremdkonto == 1) {
						Cname = kontoinhaber;
					} else {
						Cname = Vorname + " " + Nachname;
					}
					if (!konto.equals("") && !bank.equals("")
							&& !betrag.equals("") && abbuchung == 1) {

						String status = "OK";
						try {
							summe += Long.parseLong(betrag);
						} catch (Exception er) {
							status = "BETRAGERROR";
						}
						// logfile
						i++;
						String record = "";
						record = '"' + String.valueOf(i) + '"' + separation
								+ '"' + status + '"' + separation + '"'
								+ Vorname + '"' + separation + '"' + Nachname
								+ '"' + separation + '"' + kontoinhaber + '"'
								+ separation + '"' + mitgliedsnummer + '"'
								+ separation + '"' + String.valueOf(abbuchung)
								+ '"' + separation + '"'
								+ String.valueOf(fremdkonto) + '"' + separation
								+ '"' + blz + '"' + separation + '"' + konto
								+ '"' + separation + '"' + iban + '"'
								+ separation + '"' + betrag + '"' + separation
								+ '"' + verwendungszweck + '"' + "\n";
						printStreamLog.write(record);

						dtausDateiWriter
								.setCTextschluessel(CSatz.TS_LASTSCHRIFT_EINZUGSERMAECHTIGUNGSVERFAHREN);
						dtausDateiWriter.setCBLZEndbeguenstigt(Long
								.parseLong(bank));
						dtausDateiWriter.setCKonto(Long.parseLong(konto));
						dtausDateiWriter.setCInterneKundennummer(Integer
								.parseInt(mitgliedsnummer));
						dtausDateiWriter.setCBetragInEuro(Integer
								.parseInt(betrag2));
						dtausDateiWriter.setCName(Cname);
						dtausDateiWriter.addCVerwendungszweck(verwendungszweck);
						dtausDateiWriter.writeCSatz();
						// Log.e("mliste","geschrieben: "+ Cname);

					} else {

						errornr++;
						String record = "";
						record = '"' + String.valueOf(errornr) + '"'
								+ separation + '"' + "ERROR" + '"' + separation
								+ '"' + Vorname + '"' + separation + '"'
								+ Nachname + '"' + separation + '"'
								+ kontoinhaber + '"' + separation + '"'
								+ mitgliedsnummer + '"' + separation + '"'
								+ String.valueOf(abbuchung) + '"' + separation
								+ '"' + String.valueOf(fremdkonto) + '"'
								+ separation + '"' + blz + '"' + separation
								+ '"' + konto + '"' + separation + '"' + iban
								+ '"' + separation + '"' + betrag + '"'
								+ separation + '"' + verwendungszweck + '"'
								+ "\n";
						printStreamErr.write(record);

						//Log.e("mliste", "nicht geschrieben: " + Cname);
					}
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException er) {
			dtausDateiWriter.close();
			fos.close();
			printStreamLog.flush();
			printStreamErr.flush();
			printStreamLog.close();
			printStreamErr.close();
			return null;

		}
		String summensatz = '"' + "Anzahl: " + String.valueOf(i) + '"' + "\n";
		printStreamLog.write(summensatz);
		summensatz = '"' + "Betrag: " + String.valueOf(summe) + '"' + "\n";
		printStreamLog.write(summensatz);

		summensatz = '"' + "Anzahl: " + String.valueOf(errornr) + '"' + "\n";
		printStreamErr.write(summensatz);

		dtausDateiWriter.writeESatz();
		dtausDateiWriter.close();
		fos.close();
		printStreamLog.flush();
		printStreamErr.flush();
		printStreamLog.close();
		printStreamErr.close();
		return i;

	}

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

	@SuppressLint("NewApi")
	public Element getTransactions(Element PmtInf, Document xml, Integer run) {

		String sqlQuery = "SELECT mitgliedsnummer,kontoinhaber,eintritt,verwendungszweck,betrag2,betrag1,iban FROM mitglieder WHERE status='aktiv'  AND abbuchung='1'  ORDER BY CAST(mitgliedsnummer AS INTEGER) ASC";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			Integer i = 0;
			if (c.moveToFirst()) {
				do {
					String kontoinhaber = c.getString(c
							.getColumnIndex("kontoinhaber"));
					String iban = c.getString(c.getColumnIndex("iban"));
					String verwendungszweck = c.getString(c
							.getColumnIndex("verwendungszweck"));
					String eintritt = c.getString(c.getColumnIndex("Eintritt"));
					if (eintritt.contains(".")) {
						StringTokenizer tokens = new StringTokenizer(eintritt,
								".");
						String day = tokens.nextToken();// this will contain
														// "Fruit"
						String month = tokens.nextToken();
						String year = tokens.nextToken();
						eintritt = year + "-" + month + "-" + day;
					} else {
						SimpleDateFormat dateRFormat = new SimpleDateFormat(
								"yyyy-MM-dd", Locale.GERMANY);
						eintritt = dateRFormat.format(new Date());
					}

					Integer mitgliedsnummer = Integer.parseInt(c.getString(c
							.getColumnIndex("mitgliedsnummer")));
					Float betrag = 0f;
					if (run == 2) {
						betrag = Float.parseFloat(c.getString(c
								.getColumnIndex("betrag2")));
					} else {
						betrag = Float.parseFloat(c.getString(c
								.getColumnIndex("betrag1")));
					}

					String betragForatted = String.format("%.2f", betrag);
					betragForatted = betragForatted.replaceAll(",", ".");

					i++;
					// transaction
					Element DrctDbtTxInf = xml.createElement("DrctDbtTxInf");
					Element PmtId = xml.createElement("PmtId");
					Element EndToEndId = xml.createElement("EndToEndId");
					EndToEndId.setTextContent(String.format("%07d", i));
					PmtId.appendChild(EndToEndId);
					DrctDbtTxInf.appendChild(PmtId);

					Element InstdAmt = xml.createElement("InstdAmt");
					InstdAmt.setTextContent(betragForatted);

					InstdAmt.setAttribute("Ccy", "EUR");
					DrctDbtTxInf.appendChild(InstdAmt);

					Element DrctDbtTx = xml.createElement("DrctDbtTx");
					Element MndtRltdInf = xml.createElement("MndtRltdInf");
					Element MndtId = xml.createElement("MndtId");
					Element DtOfSgntr = xml.createElement("DtOfSgntr");
					MndtId.setTextContent(String
							.format("%07d", mitgliedsnummer));
					DtOfSgntr.setTextContent(eintritt);
					MndtRltdInf.appendChild(MndtId);
					MndtRltdInf.appendChild(DtOfSgntr);
					DrctDbtTx.appendChild(MndtRltdInf);

					// Element DbtrAgt = xml.createElement("DbtrAgt");
					// Element FinInstnId2 = xml.createElement("FinInstnId");
					// Element BIC2 = xml.createElement("BIC");
					// BIC2.setTextContent("kundenbic");
					// FinInstnId2.appendChild(BIC2);
					// DbtrAgt.appendChild(FinInstnId2);

					Element Dbtr = xml.createElement("Dbtr");
					Element Nm3 = xml.createElement("Nm");
					Nm3.setTextContent(kontoinhaber);
					Dbtr.appendChild(Nm3);

					Element DbtrAcct = xml.createElement("DbtrAcct");
					Element Id5 = xml.createElement("Id");
					Element IBAN2 = xml.createElement("IBAN");
					IBAN2.setTextContent(iban);
					Id5.appendChild(IBAN2);
					DbtrAcct.appendChild(Id5);

					Element RmtInf = xml.createElement("RmtInf");
					Element Ustrd = xml.createElement("Ustrd");
					Ustrd.setTextContent(verwendungszweck);
					RmtInf.appendChild(Ustrd);
					DrctDbtTxInf.appendChild(DrctDbtTx);
					DrctDbtTxInf.appendChild(Dbtr);
					DrctDbtTxInf.appendChild(DbtrAcct);
					DrctDbtTxInf.appendChild(RmtInf);
					PmtInf.appendChild(DrctDbtTxInf);
					// PmtInf.appendChild(DbtrAgt);

				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return PmtInf;

	}

	public boolean prepareDatabase(Integer run) {
		Map<String, String> prefs = getPreferncies();
		// Log.e("MLISTE", "+++ lastschriften text +++"
		// +prefs.get("lastschrifttext"));
		String lastschrifttext = prefs.get("lastschrifttext");
		Integer beitrag_erwachsene = 0;
		Integer beitrag_jugendliche = 0;
		Integer platzarbeit = 0;
		Integer versicherung_1 = 0;
		Integer versicherung_3 = 0;
		if (prefs.get("beitrag_erwachsene") != null)
			beitrag_erwachsene = Integer.parseInt(prefs.get("beitrag_erwachsene"));
		if (prefs.get("beitrag_jugendliche") != null)
			beitrag_jugendliche = Integer.parseInt(prefs.get("beitrag_jugendliche"));
		if (prefs.get("platzarbeit") != null)
			platzarbeit = Integer.parseInt(prefs.get("platzarbeit"));
		if (prefs.get("versicherung_1") != null)
			versicherung_1 = Integer.parseInt(prefs.get("versicherung_1"));
		if (prefs.get("versicherung_3") != null)
			versicherung_3 = Integer.parseInt(prefs.get("versicherung_3"));
		// String sqlQuery=
		// "SELECT  lastschrifttext,beitrag_erwachsene,beitrag_jugendliche,platzarbeit,versicherung_1,versicherung_3 FROM preferencies";
		String sqlQuery = "SELECT Vorname, Nachname, fremdkonto,abbuchung, mitgliedsnummer, versicherung, kontoinhaber, jugendlicher,platzarbeit FROM mitglieder WHERE status='aktiv' ORDER BY CAST(mitgliedsnummer AS INTEGER) ASC";

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			SQLiteDatabase db2 = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			Integer i = 0;
			if (c.moveToFirst()) {
				do {
					i++;
					String Vorname = c.getString(c.getColumnIndex("Vorname"));
					String Nachname = c.getString(c.getColumnIndex("Nachname"));
					String mitgliedsnummer = c.getString(c
							.getColumnIndex("mitgliedsnummer"));
					Integer jugendlicher = Integer.parseInt(c.getString(c
							.getColumnIndex("jugendlicher")));
					Integer versicherung = Integer.parseInt(c.getString(c
							.getColumnIndex("versicherung")));
					Integer fremdkonto = Integer.parseInt(c.getString(c
							.getColumnIndex("fremdkonto")));
					Integer platzarbeitM = Integer.parseInt(c.getString(c
							.getColumnIndex("platzarbeit")));
					Integer beitrag = 0;
					if (jugendlicher == 1) {
						beitrag = 6 * beitrag_jugendliche;
					} else {
						beitrag = 6 * beitrag_erwachsene;
					}
					if (run == 1) {
						if (platzarbeitM != 1) {
							beitrag += platzarbeit;
						}
					}
					if (run == 2) {
						if (versicherung == 2) {
							beitrag += versicherung_3;
						} else {
							beitrag += versicherung_1;
						}
					}
					String CName = "";
					if (fremdkonto != 1) {
						CName = Vorname + " " + Nachname;
						sqlQuery = "UPDATE mitglieder SET kontoinhaber='"
								+ CName
								+ "' WHERE CAST(mitgliedsnummer AS INTEGER)="
								+ Integer.parseInt(mitgliedsnummer);
						db2.execSQL(sqlQuery);
					}

					if (run == 2) {
						sqlQuery = "UPDATE mitglieder SET betrag2='"
								+ String.valueOf(beitrag)
								+ "' WHERE CAST(mitgliedsnummer AS INTEGER)="
								+ Integer.parseInt(mitgliedsnummer);
					}
					if (run == 1) {
						sqlQuery = "UPDATE mitglieder SET betrag1='"
								+ String.valueOf(beitrag)
								+ "' WHERE CAST(mitgliedsnummer AS INTEGER)="
								+ Integer.parseInt(mitgliedsnummer);
					}
					db2.execSQL(sqlQuery);
					sqlQuery = "UPDATE mitglieder SET verwendungszweck='"
							+ lastschrifttext + " Mitgliedsnummer "
							+ mitgliedsnummer
							+ "' WHERE CAST(mitgliedsnummer AS INTEGER)="
							+ Integer.parseInt(mitgliedsnummer);
					db2.execSQL(sqlQuery);

				} while (c.moveToNext());
			}

			c.close();
			db.close();
			db2.close();
			// Log.e("MLISTE", "anzahl:" + i);
		} catch (SQLiteException e) {

			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return true;
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
		String mnr = "";
		result_array = Selecteditem.split(",");
		String[] member_array = new String[10];
		if (result_array.length == 0) {
			mnr= "403"; 
		} else {
			mnr = result_array[2];
		}
		//Log.e("mliste", "mnr"+mnr);
		String sqlQuery = "SELECT * FROM mitglieder where mitgliedsnummer = '"
				+ mnr + "'";
		

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

			sqlQuery = "SELECT count(*) as austritte  FROM mitglieder WHERE austritt =  '31.12.2014' and status = 'aktiv'";
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

	public void update(String table, Map<String, String> fields, String id,
			String pkey, Boolean getuniqueID) {
		// Log.e("mliste", "" + fields);
		String sqlQuery = "";
		if (!id.equals("")) {
			sqlQuery = "UPDATE " + table + " SET ";
			for (Entry<String, String> e : fields.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();
				sqlQuery += " " + key + "='" + value + "',";
			}
			sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
			sqlQuery += " WHERE " + pkey + "='" + id + "'";
		} else {
			Integer nextval = 0;
			if (getuniqueID == true) {
				nextval = getId(table, pkey);
				id = String.valueOf(nextval);
			}
			sqlQuery = "INSERT into " + table + " ( ";

			for (Entry<String, String> e : fields.entrySet()) {
				String key = e.getKey();
				sqlQuery += " " + key + ",";
			}

			sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
			sqlQuery += ") VALUES (";
			for (Entry<String, String> e : fields.entrySet()) {
				String value = e.getValue();
				String key = e.getKey();
				if (getuniqueID == true) {
					if (key.equals(pkey)) {
						value = id;
					}
				}

				sqlQuery += " '" + value + "',";
			}
			sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
			sqlQuery += ")";
		}

		// Log.e("mliste", "SQLStatement:" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {
			Log.e("mliste", "update error " + e);
		}

	}

	public Map<String, String> getRecord(String table, String query, String key) {
		Map<String, String> prefs = new HashMap<String, String>();
		ArrayList<String> header = new ArrayList<String>();
		String sqlQuery = "PRAGMA table_info('" + table + "')";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			header.clear();
			if (c.moveToFirst()) {
				do {
					header.add(c.getString(c.getColumnIndex("name")));
				} while (c.moveToNext());
			}
			c.close();
			int length = header.size();
			sqlQuery = "SELECT * from " + table + " WHERE " + key + "='"
					+ query + "'";
			//Log.e("mliste", "" + sqlQuery);
			c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					for (int i = 0; i < length; i++) {
						prefs.put(header.get(i),
								c.getString(c.getColumnIndex(header.get(i))));
					}
				} while (c.moveToNext());
			}
			c.close();
			db.close();
			// Log.e("mliste", "Record:" + prefs);
			return prefs;
		} catch (SQLiteException e) {
			return prefs;
		}
	}

	public Map<String, String> getPreferncies() {
		Map<String, String> prefs = new HashMap<String, String>();
		ArrayList<String> header = new ArrayList<String>();
		String sqlQuery = "PRAGMA table_info('preferencies')";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			header.clear();
			if (c.moveToFirst()) {
				do {
					header.add(c.getString(c.getColumnIndex("name")));
				} while (c.moveToNext());
			}
			c.close();
			int length = header.size();
			sqlQuery = "SELECT * from preferencies where 1";
			c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					for (int i = 0; i < length; i++) {
						prefs.put(header.get(i),
								c.getString(c.getColumnIndex(header.get(i))));
					}
				} while (c.moveToNext());
			}
			c.close();
			db.close();
			return prefs;
		} catch (SQLiteException e) {
			return prefs;
		}
	}

	public ArrayList<String> getTableHeader(String table) {
		ArrayList<String> header = new ArrayList<String>();
		String sqlQuery = "PRAGMA table_info('" + table + "')";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			header.clear();
			if (c.moveToFirst()) {
				do {
					header.add(c.getString(c.getColumnIndex("name")));
				} while (c.moveToNext());
			}
			c.close();
			db.close();
			return header;
		} catch (SQLiteException e) {
			return header;
		}
	}

	public boolean exportDataBase() throws IOException {
		ArrayList<String> header = new ArrayList<String>();
		String sqlQuery = "PRAGMA table_info('mitglieder')";
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/mliste");
			dir.mkdir();

			String outFilename = dateFormat.format(new Date()) + "_"
					+ "mitglieder.csv";

			File f = new File(dir, outFilename);
			f.createNewFile();

			OutputStream myOutput = new FileOutputStream(f);

			OutputStreamWriter printStream = new OutputStreamWriter(myOutput,
					"CP1252");

			// PrintStream printStream = new PrintStream(myOutput);

			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			header.clear();

			if (c.moveToFirst()) {

				do {

					header.add(c.getString(c.getColumnIndex("name")));

				} while (c.moveToNext());
			}
			c.close();

			int length = header.size();
			String separation = ";";
			String stHeader = "";
			for (int i = 0; i < length; i++) {
				if (i == length - 1) {
					stHeader += '"' + header.get(i) + '"';
				} else {
					stHeader += '"' + header.get(i) + '"' + separation;
				}

			}
			stHeader += "\n";

			printStream.write(stHeader);

			sqlQuery = "SELECT * FROM mitglieder where status <> 'ausgetreten'";
			c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {

				do {
					String record = "";
					for (int i = 0; i < length; i++) {
						String field = header.get(i);

						// String field_name = field.toString();
						String field_value = c.getString(c
								.getColumnIndex(field));

						// byte[] chars = field_value.getBytes("UTF-8");
						// String isoString = new String(chars, "ISO-8859-1");
						if (i == length - 1) {
							record += '"' + field_value + '"' + "\n";
						} else {
							record += '"' + field_value + '"' + separation;
						}

					}
					printStream.write(record);
				} while (c.moveToNext());
			}

			c.close();

			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();

		} catch (SQLiteException e) {
			return false;
		}
		return true;
	}

	public boolean resetwork() {

		String sqlQuery = "UPDATE mitglieder SET platzarbeit = '0' where 1";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
		} catch (SQLiteException e) {

			return false;

		}
		return true;

	}

	public boolean createibans() throws IOException {
		String sqlSelectQuery = "SELECT * FROM mitglieder where status <> 'ausgetreten'";
		String sqlUpdateQuery = "UPDATE mitglieder SET iban ='";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			SQLiteDatabase db2 = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlSelectQuery, null);

			if (c.moveToFirst()) {
				do {
					String accountno = c.getString(c.getColumnIndex("konto"))
							.trim();
					accountno = accountno.replaceAll(" ", "");
					String branchcode = c.getString(c.getColumnIndex("blz"))
							.trim();
					branchcode = branchcode.replaceAll(" ", "");
					accountno = ("0000000000" + accountno).substring(accountno
							.length());
					branchcode = ("00000000" + branchcode).substring(branchcode
							.length());
					Integer checksum = MainActivity.ChecksumIBAN(accountno,
							branchcode);
					String checksumFormatted = String.format("%02d", checksum);
					String iban = "DE" + checksumFormatted + branchcode
							+ accountno;
					// Log.e("mliste",""+iban);
					db2.execSQL(sqlUpdateQuery + iban
							+ "' where mitgliedsnummer='"
							+ c.getString(c.getColumnIndex("mitgliedsnummer"))
							+ "'");
				} while (c.moveToNext());
			}

			c.close();
			db.close();
			db2.close();

		} catch (SQLiteException e) {

			return false;

		}

		return true;
	}

	public boolean backupDataBase() throws IOException {
		// Log.e("MLISTE", "+++ backupDataBase +++");

		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/mliste");
			dir.mkdir();
			String inFileName = DB_PATH + DB_NAME;
			String outFilename = dateFormat.format(new Date()) + "_" + DB_NAME;

			File f = new File(dir, outFilename);
			f.createNewFile();
			InputStream myInput = new FileInputStream(inFileName);
			OutputStream myOutput = new FileOutputStream(f);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}

			myOutput.flush();
			myOutput.close();
			myInput.close();
			return true;
		} catch (IOException e) {
			Log.e("MLISTE", "+++ ERROR backupDataBase +++" + e);
			return false;
		}

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
		// Log.e("MLISTE", "+++ checkDataBase +++");
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
		// Log.e("MLISTE", "+++ copyDataBase +++");

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

	private int getId(String table, String pkey) {
		String sqlQuery = "SELECT max(CAST(" + pkey
				+ " AS INTEGER)) as nextval, count(*) as counts FROM " + table;
		Integer id = 0;
		Integer counts = 0;

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {
					counts = Integer.parseInt(c.getString(c
							.getColumnIndex("counts")));
					if (counts > 0) {

						id = Integer.parseInt(c.getString(c
								.getColumnIndex("nextval")));
					} else {
						id = 0;
					}

				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get id +++" + e);

		}

		return id + 1;

	}

}