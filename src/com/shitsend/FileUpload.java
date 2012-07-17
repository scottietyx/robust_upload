package com.shitsend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import android.util.Log;

public class FileUpload {
	
	Scanner file_scanner = null;
	
	try {
		file_scanner = new Scanner (f);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	while (file_scanner.hasNext()) {
		
		if (!hasActiveInternetConnection()) {
			//write code to pause here
			
		}

		String currentline = file_scanner.next();

		Log.d("GB", currentline);

		//upload each line
		try {
			// ------------------ CLIENT REQUEST
			Log.d("MediaPlayer", "Inside second Method");

			// open a URL connection to the Servlet
			URL url = new URL(urlString);

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();


			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			// System.setProperty("http.keepAlive", "false");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			dos = new DataOutputStream(conn.getOutputStream());

			//write header
			String key = "u";
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name="
					+ key + lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(currentline);
			dos.writeBytes(lineEnd);

			dos.flush();
			dos.close();					

		}
		catch (MalformedURLException ex) {
			Log.d("MediaPlayer", "error: " + ex.getMessage(), ex);
		}

		catch (IOException ioe) {
			Log.d("MediaPlayer", "error: " + ioe.getMessage(), ioe);
		}

		//get server response
		try {
			inStream = new DataInputStream(conn.getInputStream());
			String str;

			while ((str = inStream.readLine()) != null) {
				Log.d("MediaPlayer", "Server Response: " + str);
			}

			inStream.close();
		}

		catch (IOException ioex) {
			Log.d("MediaPlayer", "error: " + ioex.getMessage(), ioex);
		}
	}

	public boolean hasActiveInternetConnection() {
		//boolean networkavailable = isNetworkAvailable();
		//if (networkavailable) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
				urlc.setRequestProperty("User-Agent", "Test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(2000); 
				urlc.connect();
				if (urlc.getResponseCode() == 200){
					Log.d("GB", "there is internet");
					return true;
				}
				else {
					Log.d("GB", "there is no internet");
				}
			} catch (IOException e) {
				Log.e("GB", "Error checking internet connection", e);
			}
		//} else {
		//	Log.d("GB", "No network available!");
		//}
		return false;
	}
}
