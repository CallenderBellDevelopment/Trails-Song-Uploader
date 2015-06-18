package com.callenderbell.trails;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.androidextra.Base64;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;

public class Uploader {

	public static void main(String[] args) {
		Uploader uploader = new Uploader();
		uploader.run();
	}

	public void run() {
		System.out.println("Starting file upload...\n");
		
		String csvFile = "Songs/songlist.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			
			while ((line = br.readLine()) != null) {
				String[] trackInfo = line.split(cvsSplitBy);

				upload(trackInfo);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Finished uploading files.");
	}
	
	private void upload(String[] trackInfo) {
		String filename = trackInfo[0];
		String title = trackInfo[1];
		String artist = trackInfo[2];
		String genre = trackInfo[3];
		String bpm = trackInfo[4];
		String mood = trackInfo[5];
		
		System.out.println(String.format("Uploading '%s' by '%s'...", title, artist));
		
		try {
			System.out.print("Reading file... ");
			byte[] bytes = readFile("Songs/" + filename);
			System.out.println("Done.");
			
			System.out.print("Encoding file as Base64... ");
			String base64encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
			System.out.println("Done.");
			
			System.out.print("Sending track... ");
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("http://192.168.1.41:8888/api/rest/admin/upload");
			request.addHeader("Content-Type", "application/json");
			
	        StringEntity params = new StringEntity(String.format("{\"title\":\"%s\",\"artist\":\"%s\",\"genre\":\"%s\",\"base64_mp3\":\"%s\"}", title, artist, genre, base64encoded));
	        request.setEntity(params);
	        HttpResponse response = httpClient.execute(request);
	        
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
	        	System.out.println("Done.\n");
	        else
	        	System.out.println("Error while uploading.\n");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}
	
	private byte[] readFile(String fileName) throws IOException {
		RandomAccessFile f = new RandomAccessFile(fileName, "r");
		byte[] b = new byte[(int)f.length()];
		f.read(b);
		f.close();
		
		return b;
	}
}
