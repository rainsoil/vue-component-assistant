package com.chu7.vuecomponentassistant.remote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SimpleHttpClient {
	private SimpleHttpClient() {}

	public static String downloadJson(String urlStr) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(15000);
		conn.setRequestProperty("Accept", "application/json");
		int code = conn.getResponseCode();
		if (code != 200) throw new RuntimeException("HTTP " + code + " when GET " + urlStr);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) sb.append(line).append('\n');
			return sb.toString();
		}
	}
} 