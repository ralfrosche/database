package com.example.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class help extends Activity  {
	Context mContext = getBaseContext();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	   setContentView(R.layout.help);
	   WebView help = (WebView)findViewById(R.id.helpView);
	   help.setWebViewClient(new WebViewClient(){
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
	 	   if(url.startsWith("mailto:")){
			MailTo mt = MailTo.parse(url);
			String uri = "mailto:" + mt.getTo();
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
					.parse(uri));
			intent.putExtra("compose_mode", true);
			intent.putExtra(Intent.EXTRA_SUBJECT, "Mitglieder Liste feedback");
			startActivity(intent);
			return true;
		   }
		   else{
			view.loadUrl(url);
		   }
		   return true;
	      }
	   });

	   String helpText = readRawTextFile(R.raw.help); 
	   help.loadData(helpText, "text/html; charset=utf-8", "utf-8");
	}

	private String readRawTextFile(int id) {
	   InputStream inputStream = getBaseContext().getResources().openRawResource(id);
	   InputStreamReader in = new InputStreamReader(inputStream);
	   BufferedReader buf = new BufferedReader(in);
	   String line;
	   StringBuilder text = new StringBuilder();
	   try {
	      while (( line = buf.readLine()) != null) 
		text.append(line);
	   } catch (IOException e) {
	     return null;
	   }
	   return text.toString();
	}

}
