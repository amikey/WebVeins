package com.xiongbeer.webveins.utils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;


public class UrlFileLoader {
	public List<String> readFileByLine(String filePath) throws IOException{
		File file = new File(filePath);
		return Files.readLines(file, Charset.defaultCharset());
	}
}
