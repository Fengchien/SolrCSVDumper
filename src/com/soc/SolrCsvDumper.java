package com.soc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class SolrCsvDumper {

	private final static String USER_AGENT = "Mozilla/5.0";
	
	
	public SolrCsvDumper() {
	}

	
	/**
	  * Recursively check File attribute
	  * @param file
	  * @param zos
	  * @param fileName
	  * @throws Exception
	  */
	private static void checkFileType(File file, ZipOutputStream zos, String fileName) throws Exception{
		
		if(file.isDirectory()){
			
			for(File tmp: file.listFiles()){
				checkFileType(tmp, zos, fileName +"/"+ tmp.getName());
			}
		
		}else
			addZipFile(file, zos, fileName);
		
	}
	 
	/**
	 * Add Files into Zip Archive
	 * @param file
	 * @param zos
	 * @param fileName
	 * @throws Exception
	 */
	private static void addZipFile(File file, ZipOutputStream zos, String fileName) throws Exception{
		int l;
		
		byte[] b = new byte[(int) file.length()];
		
		FileInputStream fis = new FileInputStream(file);
		
		if (fis.available()!=0){
			
			ZipEntry entry = new ZipEntry(fileName);
			
			zos.putNextEntry(entry);
			
			while((l = fis.read(b)) != -1){
				zos.write(b, 0, l);
			}
			
			entry = null;
		}
		
		fis.close();
		b = null;
		
	}
	
	public static String getDaysBeforeDate(String baseday, Integer daysBefore){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar c = Calendar.getInstance();
		
		String dt = "";
		if (baseday!=null&&!baseday.isEmpty()){
			dt = baseday;
			try{
				c.setTime(sdf.parse(dt));
			}catch(Exception e){
				System.out.println(e);
			}
		}
	
		c.add(Calendar.DAY_OF_MONTH, -daysBefore);  // number of hours to add
		dt = sdf.format(c.getTime());
		return dt;
	}
		
	public static void main(String[] args) {
		
		
		try {
			Properties properties = new Properties();
			String CONFIG_FILE = "conf/csv_dumper.properties";
			File file = new File(CONFIG_FILE);
			if (file.exists()) {
				FileInputStream input = new FileInputStream(CONFIG_FILE);
				properties.load(input);
			}
			
			String BASEDAY = properties.getProperty("baseday", "20150101");
			String GET_MAX_SOLR_RAWLOG = properties.getProperty("maxrow", "10");
			Integer intervalDays = Integer.parseInt(properties.getProperty("intervaldays", "3"));
			String serverIp = properties.getProperty("serverip", "10.172.101.150");
			String queryString = properties.getProperty("querystring", "deviceAddress%3A10.28.115.73+AND+flexString1%3ALC-1010-10-02-1010-C-01");
			
			for (Integer i =0;i< intervalDays;i++){ 
				
				final String s = getDaysBeforeDate(BASEDAY, i);
				
				System.out.println("Start: " + s);
				
				int startRawLog = 0;
				int endRawLog = Integer.parseInt(GET_MAX_SOLR_RAWLOG);
				int maxSolrRawlog = Integer.parseInt(GET_MAX_SOLR_RAWLOG);
				int searchtime = 1;
				boolean isNotFirstSearch = false;
				
				int lineCount=0;
  
				do {
					
					String url = "http://" + serverIp + ":8983/solr/log" + s + "/select?" + 
							"q=" + queryString + "&start=" + startRawLog + "&rows=" + maxSolrRawlog + "&wt=csv&indent=true";				
					
					System.out.println(url);
	
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet request = new HttpGet(url);
		
					// add request header
					request.addHeader("User-Agent", USER_AGENT);
					HttpResponse response = client.execute(request);
		
					System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		
					BufferedReader br = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
	
					FileWriter csvfile = new FileWriter(s+"-"+searchtime+".csv", true);
					BufferedWriter bf = new BufferedWriter(csvfile);							
					
					String line = "";

					lineCount=0;
					
					while ((line = br.readLine()) != null) {	
						
						if (!line.equals("")&&!line.contains("HTTP Status 404")){						

							bf.write(line);
							bf.newLine();
							lineCount++;

						}
					}
					bf.close(); 
	
					if (!isNotFirstSearch) {
						startRawLog = startRawLog + Integer.parseInt(GET_MAX_SOLR_RAWLOG);
						isNotFirstSearch = true;
					} else {
						startRawLog = startRawLog + Integer.parseInt(GET_MAX_SOLR_RAWLOG);
					}
	
					endRawLog = endRawLog + Integer.parseInt(GET_MAX_SOLR_RAWLOG);
					searchtime++;
				} while ((lineCount > Integer.parseInt(GET_MAX_SOLR_RAWLOG)));

				//Retrieve all the files under source directory and add them to ZIP file
				File srcFolder = new File(System.getProperty("user.dir"));
				
				File[] files = srcFolder.listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
//				        System.out.println(name + ":" + dir.length());
				    	return (name.toLowerCase().startsWith(s)&&name.toLowerCase().endsWith(".csv"));
				    }
				});
				
				List<File> fileList = new ArrayList<File>();
				
				for (File f : files){
					
					System.out.println(f.getName() + ":" +f.length());
					if (f.length()!=0)
						fileList.add(f);
				}
				
				if (fileList.size()!=0){
					
					ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(s+".zip"));
					for(File tmp: fileList){
					 checkFileType(tmp, zos, tmp.getName());
					}
					zos.close();
				}

				// Reset startRawLog to zero
				startRawLog = 0;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
