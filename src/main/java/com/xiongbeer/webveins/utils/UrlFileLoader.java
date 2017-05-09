package com.xiongbeer.webveins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class UrlFileLoader {
	/* TODO 改为NIO */
	public List<String> readFileByLine(String filePath) throws IOException{
		List<String> lineStrings = new LinkedList<String>();
		File file = new File(filePath);
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			lineStrings.add(line);
		}
		reader.close();
		return lineStrings;
	}
}
