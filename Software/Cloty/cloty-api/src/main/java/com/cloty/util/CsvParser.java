package com.cloty.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CsvParser {

	private CsvParser() {
	}

	public static List<Map<String, String>> parse(InputStream input) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String headerLine = reader.readLine();
			if (headerLine == null) {
				return List.of();
			}
			headerLine = stripBom(headerLine.trim());
			String[] headers = splitLine(headerLine);
			for (int i = 0; i < headers.length; i++) {
				headers[i] = normalizeHeader(headers[i]);
			}
			List<Map<String, String>> rows = new ArrayList<>();
			String line;
			int lineNum = 1;
			while ((line = reader.readLine()) != null) {
				lineNum++;
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] values = splitLine(line);
				Map<String, String> row = new HashMap<>();
				for (int i = 0; i < headers.length; i++) {
					String value = i < values.length ? values[i].trim() : "";
					row.put(headers[i], value);
				}
				row.put("_linea", String.valueOf(lineNum));
				rows.add(row);
			}
			return rows;
		}
	}

	private static String stripBom(String s) {
		if (s.startsWith("\uFEFF")) {
			return s.substring(1);
		}
		return s;
	}

	private static String normalizeHeader(String h) {
		return h.trim().toLowerCase(Locale.ROOT).replace(" ", "_");
	}

	private static String[] splitLine(String line) {
		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				parts.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		parts.add(current.toString());
		return parts.toArray(String[]::new);
	}
}
