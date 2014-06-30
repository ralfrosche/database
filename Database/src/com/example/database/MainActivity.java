package com.example.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;

import java.util.List;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.app.SearchManager.OnDismissListener;

import android.content.ContentProviderOperation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.MediaStore;

import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ImageButton;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private Spinner customListSpinner;
	public static boolean is_admin = false;
	public static final int TYP_LASTSCHRIFTEN = 0;
	public static final int TYP_GUTSCHRIFTEN = 1;
	String[] DataToDB;
	String[] result_array;
	String Selecteditem = null;
	String SelectedPosition = null;
	String SelectedID = "0";
	public String filter = "";
	ImageView imageViewList;
	public static Boolean updateView = false;
	public  Boolean startup = true;
	boolean searchInvolved = false;
	private MenuItem mMenuItemSearch;
	private MenuItem mMenuItemYouth;
	private MenuItem mMenuItemAddMember;
	private MenuItem mMenuItemExportDB;
	private MenuItem mMenuItemExportExcel;
	private MenuItem mMenuItemCreateIban;
	private MenuItem mMenuItemMakeDtaus;
	private MenuItem mMenuItemMakeDtaus2;
	private MenuItem mMenuItemEditOptions;
	private MenuItem mMenuItemimportDB;
	private MenuItem  mMenuItemresetwork;
	private Integer SelectedPositionAtStartup = 0;
	static final int DATE_DIALOG_ID = 0;
	private DatabaseHelper myDbHelper = new DatabaseHelper(this);
	private static final int SELECT_PICTURE = 1;
	String image_path = "";
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	public void onStart() {
		super.onStart();
		String FILENAME = "mliste_pass";
		byte[] bytes = new byte[] { 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
				32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
				32, 32, 32, 32, 32 };

		String str = "";
		FileInputStream fis = null;
		try {
			fis = openFileInput(FILENAME);
			try {
				fis.read(bytes, 0, 32);
				str = new String(bytes);
				fis.close();
			} catch (IOException e) {
				Log.e("mliste", "Errorreading passwordfile" + e);
			}

		} catch (FileNotFoundException e) {
			Log.e("mliste", "Error finding passwordfile" + e);
		}
		// Log.e("mliste", "md5hash found:" + str);
		if (str.equals("ce3c8e1b0310e8e282bb901191d61327")) {
			is_admin = true;
			// Log.e("mliste", "adminMode enabled");
		} else {
			is_admin = false;
		}
		FILENAME = "mliste_sav";
		bytes = new byte[] { 32, 32, 32, 32, 32, 32, 32 };

		str = "";
		fis = null;
		try {
			fis = openFileInput(FILENAME);

			try {
				fis.read(bytes, 0, 7);
				str = new String(bytes);
				fis.close();
			} catch (IOException e) {
				Log.e("mliste", "Errorreading configfile" + e);
			}

		} catch (FileNotFoundException e) {
			Log.e("mliste", "Error finding configfile" + e);
		}

		if (!str.equals("") && Selecteditem == null) {
			SelectedPosition = str.trim();
			SelectedPositionAtStartup = Integer.parseInt(SelectedPosition);
			filter = "new";
		}
		TextView name = (TextView) findViewById(R.id.name);
		if (is_admin == true) {

			name.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					doAddMember(SelectedPosition, false);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (updateView == true) {
			initCustomListSpinner("", false);
			updateView = false;
		}
		if (SelectedPosition != null && filter.equals("new")) {
			int tmpPosition = Integer.parseInt(SelectedPosition);
			customListSpinner.setSelection(tmpPosition);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			myDbHelper.createDataBase();
			boolean upgrade = myDbHelper.checkDatabaseVersion();
			if (upgrade == true) {
				myDbHelper.upgradeDatabaseVersion(getBaseContext());
				Toast.makeText(
						getBaseContext(),
						"Datenbank erfolgreich auf Version: "
								+ DatabaseHelper.PROGRAMM_VERSION
								+ " gepatched", Toast.LENGTH_SHORT).show();
		} else {
				Toast.makeText(getBaseContext(),
						"Datenbank hat aktuelle Version", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (IOException e) {
		}
		initCustomListSpinner(filter, false);
		handleIntent(getIntent());
		imageViewList = (ImageView) findViewById(R.id.imageViewList);
		imageViewList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap newImage = null;
			Bitmap resizedBitmap = null;
			image_path = "mitglieder_liste/" + SelectedID + ".jpg";

			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/mitglieder_liste");
			dir.mkdir();

			if (requestCode == SELECT_PICTURE) {

				int or = 0;
				Uri selectedImageUri = data.getData();
				try {
					newImage = decodeUri(selectedImageUri);

					Cursor cursor = getContentResolver()
							.query(selectedImageUri,
									new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
									null, null, null);

					if (cursor.getCount() != 1) {
						or = 0;
					} else {

						cursor.moveToFirst();

						or = cursor.getInt(0);

						// Log.e("MLISTE", "+++ orientation:" + or);
					}

					int width = newImage.getWidth();
					int height = newImage.getHeight();
					Matrix matrix = new Matrix();
					if (or > 0)
						matrix.postRotate(or);
					resizedBitmap = Bitmap.createBitmap(newImage, 0, 0, width,
							height, matrix, true);
					saveBitmap(resizedBitmap, image_path);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				imageViewList = (ImageView) findViewById(R.id.imageViewList);
				imageViewList.setImageBitmap(resizedBitmap);
			}

		}

	}

	private void saveBitmap(Bitmap newImage, String image_path) {
		imageViewList = (ImageView) findViewById(R.id.imageViewList);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		newImage.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

		File f = new File(Environment.getExternalStorageDirectory()
				+ File.separator + image_path);
		FileOutputStream fo = null;
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// write the bytes in file

		try {
			fo = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// remember close de FileOutput

	}

	@Override
	public boolean onSearchRequested() {

		final SearchManager searchManager = (SearchManager) this
				.getSystemService(MainActivity.SEARCH_SERVICE);
		searchManager.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

				if (filter == "")
					initCustomListSpinner("", false);

			}
		});

		searchManager.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {

				// Log.e("MListe", "- Cancel-");

			}
		});

		return super.onSearchRequested();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		// Log.e("MListe", "- HandleIntent-" + intent.getAction());
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}

	}

	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		filter = query;
		initCustomListSpinner(query, false);

		// Log.e("MListe", "- Query:-" + query);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		String FILENAME = "mliste_sav";
		String string = String.valueOf(SelectedPosition);
		// Log.e("MListe", "- SelectedPosition:-" + SelectedPosition);
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			fos.write(string.getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		myDbHelper.close();
	}

	public void initCustomListSpinner(String filter, boolean youth) {

		customListSpinner = (Spinner) findViewById(R.id.custom_list_spinner);
		List<CharSequence> choices = new ArrayList<CharSequence>();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			int dlength = 0; 
			if (startup == false) {
				DataToDB = myDbHelper.ReadNRFromDB(filter, youth);
				if (DataToDB.length == 0) {
					Toast.makeText(getBaseContext(),"Keine Daten gefunden", Toast.LENGTH_SHORT) .show();
					
				}
				dlength = 99;
			} 
			while (dlength == 0) {
				DataToDB = myDbHelper.ReadNRFromDB(filter, youth);
				dlength = DataToDB.length;
				startup = false;
			}

			for (int i = 0; i < DataToDB.length; i++) {
				choices.add(DataToDB[i]);
			}

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, choices);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		customListSpinner.setAdapter(adapter);
		customListSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mMenuItemSearch = menu.getItem(0);
		mMenuItemYouth = menu.getItem(1);
		mMenuItemAddMember = menu.getItem(6);
		mMenuItemExportDB = menu.getItem(7);
		mMenuItemExportExcel = menu.getItem(8);
		mMenuItemCreateIban = menu.getItem(9);
		mMenuItemMakeDtaus = menu.getItem(10);
		mMenuItemMakeDtaus2 = menu.getItem(11);
		mMenuItemEditOptions = menu.getItem(12);
		mMenuItemresetwork = menu.getItem(13);
		mMenuItemimportDB = menu.getItem(14);
		if (is_admin == true) {
			mMenuItemAddMember.setVisible(true);
			mMenuItemExportDB.setVisible(true);
			mMenuItemExportExcel.setVisible(true);
			mMenuItemCreateIban.setVisible(true);
			mMenuItemMakeDtaus.setVisible(true);
			mMenuItemMakeDtaus2.setVisible(true);
			mMenuItemEditOptions.setVisible(true);
			mMenuItemimportDB.setVisible(true);
			mMenuItemresetwork.setVisible(true);
		}

		// Log.e("MListe", "- OPtions menu created -");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String searchS;
		switch (item.getItemId()) {

		case R.id.search:
			filter = "";

			searchS = (String) mMenuItemSearch.getTitle();
			if (searchS.equals("Filter entfernen")) {
				mMenuItemSearch.setTitle("Suche / Filter");
				initCustomListSpinner(filter, false);
				SelectedPosition = String.valueOf(SelectedPositionAtStartup);
				customListSpinner.setSelection(SelectedPositionAtStartup);

			} else {
				mMenuItemSearch.setTitle("Filter entfernen");
				onSearchRequested();
			}
			return true;
		case R.id.version:
			doVersion();
			return true;
		case R.id.add_member:
			doAddMember(SelectedPosition, true);
			return true;
		case R.id.enhanced:
			showDialog(this);
			return true;
		case R.id.importDB:
			doimportDB(this);
			return true;

		case R.id.stats:
			doStats();
			return true;
		case R.id.makedtaus:
			doDtaus(1);
			return true;
		case R.id.makedtaus2:
			doDtaus(2);
			return true;
		case R.id.structure:
			doStructure();
			return true;
		case R.id.exportDB:
			backupDataBase();
			return true;
		case R.id.createiban:
			createibans();
			return true;
		case R.id.resetwork:
			resetwork();
			return true;
		case R.id.exportExcel:
			exportDataBase();
			return true;

		case R.id.editOpt:
			EditPrefs();
			return true;
		case R.id.childs:
			filter = "";

			searchS = (String) mMenuItemYouth.getTitle();
			if (searchS.equals("Alle")) {
				mMenuItemYouth.setTitle("Jugendliche");
				initCustomListSpinner(filter, false);

			} else {
				mMenuItemYouth.setTitle("Alle");
				initCustomListSpinner(filter, true);
			}
			return true;
		}

		return false;
	}

	private void EditPrefs() {
		Intent intent = new Intent(this, EditPrefs.class);
		startActivity(intent);
	}
	private void doimportDB(Context context) {
		

		try {
			myDbHelper.createDataBase();
			boolean backupSuccess = myDbHelper.importDataBaseComplete(context);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void doDtaus(final Integer run) {

		boolean backupSuccess = false;
		final ProgressDialog dialog;
		final Integer increment = 20;

		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage("Bearbeite Datenbank...");
		// set the progress to be horizontal
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// reset the bar to the default value of 0
		dialog.setProgress(0);
		dialog.setMax(100);
		dialog.show();
		final Handler progressHandler = new Handler() {
			public void handleMessage(Message msg) {
				
				dialog.incrementProgressBy(increment);
				if (dialog.getProgress() >= 60) 
					dialog.setMessage("Erzeuge SEPA_XML-Datei...");
				if (dialog.getProgress() == 40) 
					dialog.setMessage("Erzeuge DTAUS-Datei...");
			}
		};
		// create a thread for updating the progress bar
		Thread background = new Thread(new Runnable() {
			public void run() {
				Integer num = 0;
				try {
					try {
						progressHandler.sendMessage(progressHandler
								.obtainMessage());
						myDbHelper.createDataBase();
						boolean backupSuccess = myDbHelper.prepareDatabase(run);
						
						progressHandler.sendMessage(progressHandler
								.obtainMessage());
						try {
							num = myDbHelper.writeDtaus(run);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						progressHandler.sendMessage(progressHandler
								.obtainMessage());
						SepaDirectDebitFile test = new SepaDirectDebitFile(num,
								getBaseContext(), run);
						progressHandler.sendMessage(progressHandler
								.obtainMessage());
						Thread.sleep(500);
						dialog.dismiss();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// enter the code to be run while displaying the
					// progressbar.
					//
					// This example is just going to increment the progress bar:
					// So keep running until the progress value reaches maximum
					// value
					while (dialog.getProgress() <= dialog.getMax()) {
						// wait 500ms between each update
						Thread.sleep(500);

						// active the update handler
						progressHandler.sendMessage(progressHandler
								.obtainMessage());
					}
				} catch (java.lang.InterruptedException e) {
					// if something fails do something smart
				}
			}
		});

		background.start();
		/*
		 * try { myDbHelper.createDataBase(); Toast.makeText(getBaseContext(),
		 * "Bereite Datenbank vor", Toast.LENGTH_LONG).show(); backupSuccess =
		 * myDbHelper.prepareDatabase(run); try {
		 * Toast.makeText(getBaseContext(), "Schreibe Dtaus Datei...",
		 * Toast.LENGTH_LONG).show(); Integer num = myDbHelper.writeDtaus(run);
		 * if (num != null) { backupSuccess = true; } //sepa file
		 * Toast.makeText(getBaseContext(), "Schreibe SEPA-xml Datei...",
		 * Toast.LENGTH_LONG).show(); SepaDirectDebitFile test = new
		 * SepaDirectDebitFile(num, getBaseContext(), run); } catch (Exception
		 * e) { e.printStackTrace(); } } catch (IOException e) {
		 * 
		 * e.printStackTrace(); } if (backupSuccess) {
		 * Toast.makeText(getBaseContext(), "Dtaus erfolgreich erzeugt!",
		 * Toast.LENGTH_SHORT).show();
		 * 
		 * } else { Toast.makeText(getBaseContext(),
		 * "ERROR:Dtaus Erzeugung fehlgeschlagen!", Toast.LENGTH_SHORT) .show();
		 * 
		 * }
		 */

	}

	private void resetwork() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			backupSuccess = myDbHelper.resetwork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(), "Flag erfolgreich zur�ckgesetzt!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Reset Flag fehlgeschlagen!", Toast.LENGTH_SHORT)
					.show();

		}

	}

	private void createibans() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			Toast.makeText(getBaseContext(), "Bereite IBANs vor...",
					Toast.LENGTH_LONG).show();
			backupSuccess = myDbHelper.createibans();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(), "IBANS erfolgreich erzeugt!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Erzeugung der IBANS fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	private void exportDataBase() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			backupSuccess = myDbHelper.exportDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Export der Datenbank erfolgreich!", Toast.LENGTH_SHORT)
					.show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Exportder Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	private void backupDataBase() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			backupSuccess = myDbHelper.backupDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Backup der Datenbank erfolgreich!", Toast.LENGTH_SHORT)
					.show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Backup der Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	public void showDialog(final Context c) {
		LayoutInflater li = LayoutInflater.from(c);
		View promptsView = li.inflate(R.layout.passwordprompt, null);
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				c);
		alertDialogBuilder.setView(promptsView);
		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.userInput);
		alertDialogBuilder
				.setCancelable(false)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String user_text = (userInput.getText()).toString();
						user_text = md5(user_text);
						// Log.e("mliste", "MD5hash: " + user_text);
						if (user_text
								.equals("ce3c8e1b0310e8e282bb901191d61327")) {
							String FILENAME = "mliste_pass";
							FileOutputStream fos = null;
							try {
								fos = openFileOutput(FILENAME,
										Context.MODE_PRIVATE);
							} catch (FileNotFoundException e1) {
								Log.e("mliste", "Passwordfilenotfound:" + e1);
							}
							try {
								fos.write(user_text.getBytes());
								fos.close();
							} catch (IOException e) {
								Log.e("mliste", "Password write:" + e);
							}
						} else {
							String message = "The password you have entered is incorrect."
									+ " \n \n" + "Please try again!";
							AlertDialog.Builder builder = new AlertDialog.Builder(
									c);
							builder.setTitle("Error");
							builder.setMessage(message);
							builder.setPositiveButton("Cancel", null);
							builder.setNegativeButton("Retry",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											showDialog(c);
										}
									});
							builder.create().show();

						}
					}
				})
				.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}

						}

				);

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	public String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			Log.e("mliste", "MD5error:" + e);
		}
		return "";
	}

	private void doAddMember(String SelectedPosition, Boolean insert) {
		// Toast.makeText(getBaseContext(),"EditAction called" +
		// customListSpinner.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, EditMember.class);
		if (insert == true) {
			intent.putExtra("query", "");
		} else {
			intent.putExtra("query", customListSpinner.getSelectedItem()
					.toString());
		}

		startActivity(intent);

	}

	private void doStructure() {

		String stat = "";
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadAlterFromDB();

			for (int i = 0; i < DataToDB.length; i++) {
				stat += DataToDB[i] + "\n";
			}

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to open database");

		}

		new AlertDialog.Builder(this)
				.setTitle("Altersstruktur")
				.

				setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + stat
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	private void doStats() {

		String stat = "";
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {
				// to do maintain year
			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadStatsFromDB();
			stat += "Mitglieder gesamt: " + DataToDB[0] + "\n";
			stat += "Aktive: " + DataToDB[1] + "\n";
			stat += "Jungendliche: " + DataToDB[2] + "\n";
			stat += "Ehrenmitglieder: " + DataToDB[3] + "\n";
			stat += "F�rdermitglieder: " + DataToDB[4] + "\n";
			stat += "ruhende Mitglieder: " + DataToDB[5] + "\n";
			stat += "Austritte bis Ende 2014: " + DataToDB[6] + "\n";

			DataToDB = myDbHelper.ReadAverageAlterFromDB();

			stat += "Durchschnitssalter: " + DataToDB[0] + "\n\n";

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to open database");

		}

		new AlertDialog.Builder(this)
				.setTitle("Statistik")
				.

				setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + stat
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	private void doVersion() {

		new AlertDialog.Builder(this)
				.setTitle("Versionshinweis")
				.setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + "Mitgliederliste V1.4\n"
								+ "(c) 2013, 2014 Ralf Rosche\n"
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Selecteditem = customListSpinner.getSelectedItem().toString();
		SelectedPosition = String.valueOf(customListSpinner
				.getSelectedItemPosition());

		RunDatabase();
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private boolean checkEmail(String email) {
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

	public void RunDatabase() {
		DatabaseHelper myDbHelper = new DatabaseHelper(this);

		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadFromDB(Selecteditem.trim());
			TextView svorname = (TextView) findViewById(R.id.vorname);
			final TextView sname = (TextView) findViewById(R.id.name);
			final TextView smobil = (TextView) findViewById(R.id.mobil);
			final TextView stelefon = (TextView) findViewById(R.id.telefon);
			final TextView semail = (TextView) findViewById(R.id.email);
			TextView sstrasse = (TextView) findViewById(R.id.strasse);
			TextView splz = (TextView) findViewById(R.id.plz);
			TextView sort = (TextView) findViewById(R.id.ort);
			TextView snummer = (TextView) findViewById(R.id.nummer);
			TextView sstatus = (TextView) findViewById(R.id.status);

			ImageButton smobileButton = (ImageButton) findViewById(R.id.mobileButton);
			ImageButton sphoneButton = (ImageButton) findViewById(R.id.phoneButton);
			ImageButton ssmsButton = (ImageButton) findViewById(R.id.smsButton);
			ImageButton semailButton = (ImageButton) findViewById(R.id.emailButton);
			ImageButton scontactButton = (ImageButton) findViewById(R.id.contactButton);

			svorname.setText(DataToDB[0]);
			sname.setText(DataToDB[1]);
			sstrasse.setText(DataToDB[2]);
			splz.setText(DataToDB[3]);
			sort.setText(DataToDB[4]);
			stelefon.setText(DataToDB[5]);
			smobil.setText(DataToDB[6]);
			semail.setText(DataToDB[7]);
			snummer.setText(DataToDB[8]);
			SelectedID = DataToDB[8];
			sstatus.setText(DataToDB[9]);

			myDbHelper.close();

			image_path = "mitglieder_liste/" + SelectedID + ".jpg";
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			// Log.e("MListe", "- image:" + image_path);
			if (!image_path.equals("")) {
				Uri mUri;

				File sdCard = Environment.getExternalStorageDirectory();
				String f = sdCard.getAbsolutePath() + "/" + image_path;
				File filecheck = new File(f);
				if (filecheck.exists()) {
					mUri = Uri.parse(f);
					imageViewList.setImageURI(mUri);
				} else {
					imageViewList.setImageResource(R.drawable.noimageavailable);
				}

			}

			sphoneButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String telefonNumber = stelefon.getText().toString();
					telefonNumber = telefonNumber.trim();
					telefonNumber = telefonNumber.replaceAll("/", "");
					telefonNumber = telefonNumber.replaceAll("-", "");
					telefonNumber = telefonNumber.replaceAll(" ", "");

					if (telefonNumber.length() > 5) {
						String uri = "tel:" + telefonNumber;
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse(uri));

						startActivity(intent);
					}

				}
			});

			smobileButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();

					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					if (phoneNumber.length() > 5) {
						String uri = "tel:" + phoneNumber;
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse(uri));

						startActivity(intent);
					}

				}
			});

			semailButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String emailn = semail.getText().toString();

					if (checkEmail(emailn)) {
						String uri = "mailto:" + emailn;
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
								.parse(uri));
						intent.putExtra("compose_mode", true);
						startActivity(intent);
					}

				}
			});

			ssmsButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();

					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					if (phoneNumber.length() > 5) {
						String uri = "smsto:" + phoneNumber;
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
								.parse(uri));
						intent.putExtra("compose_mode", true);
						startActivity(intent);
					}

				}
			});
			scontactButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();
					String name = sname.getText().toString();
					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					String telefonNumber = stelefon.getText().toString();
					telefonNumber = telefonNumber.trim();
					telefonNumber = telefonNumber.replaceAll("/", "");
					telefonNumber = telefonNumber.replaceAll("-", "");
					telefonNumber = telefonNumber.replaceAll(" ", "");

					String emailn = semail.getText().toString();

					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

					int rawContactID = ops.size();

					// Adding insert operation to operations list
					// to insert a new raw contact in the table
					// ContactsContract.RawContacts
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.RawContacts.CONTENT_URI)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_TYPE,
									null)
							.withValue(RawContacts.ACCOUNT_NAME, null).build());

					// Adding insert operation to operations list
					// to insert display name in the table ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									StructuredName.CONTENT_ITEM_TYPE)
							.withValue(StructuredName.DISPLAY_NAME, name)
							.build());

					// Adding insert operation to operations list
					// to insert Mobile Number in the table
					// ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, phoneNumber)
							.withValue(Phone.TYPE,
									CommonDataKinds.Phone.TYPE_MOBILE).build());

					// Adding insert operation to operations list
					// to insert Home Phone Number in the table
					// ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, telefonNumber)
							.withValue(Phone.TYPE, Phone.TYPE_HOME).build());

					// Adding insert operation to operations list
					// to insert Home Email in the table ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Email.CONTENT_ITEM_TYPE)
							.withValue(Email.ADDRESS, emailn)
							.withValue(Email.TYPE, Email.TYPE_HOME).build());

					try {
						// Executing all the insert operations as a single
						// database transaction
						getContentResolver().applyBatch(
								ContactsContract.AUTHORITY, ops);
						Toast.makeText(getBaseContext(),
								"Kontakt erfolgreich hinzugef�gt",
								Toast.LENGTH_SHORT).show();
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (OperationApplicationException e) {
						e.printStackTrace();
					}
				}

			});

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		try {

		} catch (SQLException sqle) {

			throw sqle;

		}
	}

	private static int capital2digits(char ch) {
		int i = 0;
		String capitals = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (i = 0; i < capitals.length(); ++i) {
			if (ch == capitals.charAt(i))
				break;
		}
		return i + 10;
	}

	private static int mod97(String digit_string) {
		long m = 0;
		for (int i = 0; i < digit_string.length(); ++i) {
			m = (m * 10 + Integer.parseInt(digit_string.substring(i, i + 1))) % 97;
		}
		return (int) m;
	}

	// Calculate 2-digit checksum of an IBAN.
	public static int ChecksumIBAN(String account, String branchcode) {

		String bban = branchcode + account;
		String code = "DE";
		String checksum = "00";
		// Assemble digit string
		String digits = "";
		for (int i = 0; i < bban.length(); ++i) {
			char ch = bban.charAt(i);
			if ('0' <= ch && ch <= '9')
				digits += ch;
			else
				digits += capital2digits(ch);
		}
		for (int i = 0; i < code.length(); ++i) {
			char ch = code.charAt(i);
			digits += capital2digits(ch);
		}
		digits += checksum;

		// Calculate checksum
		int check = 98 - mod97(digits);
		// Log.e("mliste", "iban: " + code + check + branchcode + account);
		return check;

	}

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);
		final int REQUIRED_SIZE = 300;
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

	}

}
