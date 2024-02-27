package com.byte_.qa.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.FileHandler;


public class SegmentationTesting {
	private String authToken;
	@BeforeClass
	public void loginAndGetToken() {
		Response response = RestAssured.given()
				.param("grant_type", "")
				.param("username", "praveen@capestart.com")
				.param("password", "Praveen!123")
				.param("scope", "")
				.param("client_id", "")
				.param("client_secret", "")
				.header("accept", "application/json")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.when()
				.post("http://byteuat-segment.capestart.com/auth");

		authToken = response.jsonPath().getString("access_token");
		System.out.println(authToken);
	}

	@Test
	public void testImageSegmentation() {
		String upperjaw=" ";
		String lowerjaw=" ";
		Long image_id=null;
		Long instance_id=null;
		int max1=0;
		int max2=0;
		int max3=0;
		int max4=0;
		int max5=0;
		int fullResponseLength=0;
		String response1;
		String response2;
		String response3;
		String response4;
		String response5;
		String response6;
		Workbook workbook = new XSSFWorkbook();
		try {
			File imageFolder = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\TestData");
			//						File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\segmentation\\1st Sample 100 Images Iteration -1");
//						File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\Batch95&97 - 540 Images - Not annotated\\Batch95&97 - 540 Images - Not annotated");
			File[] imageFiles = imageFolder.listFiles();
			System.out.println(imageFiles.length);
			if (imageFiles != null) {
				for (int i = 396; i < imageFiles.length; i++) {
					File imageFile = imageFiles[i];


					Response segmentationResponse = RestAssured.given()
							.queryParam("token", authToken)
							.queryParam("user_id", 8)
							.multiPart("Image_file", imageFile, "image/jpeg")
							//		.queryParam("instance_id")
							.header("accept", "application/json")
							.contentType(ContentType.MULTIPART)
							.when()
							.post("http://byteuat-segment.capestart.com/bytecom/api/v1/teeth/segmentation/");
					String reponse =segmentationResponse.asPrettyString();

					writeJsonStringToFile(segmentationResponse.asPrettyString(), System.getProperty("user.dir")+"\\src\\test\\resources\\com\\byte_\\qa\\testData\\praveen.json");

					JSONParser jsonParser= new JSONParser();
					FileReader fileReader= new FileReader(System.getProperty("user.dir")+"\\src\\test\\resources\\com\\byte_\\qa\\testData\\praveen.json");
					Object parseObject = jsonParser.parse(fileReader);

					JSONObject jsonObject=(JSONObject) parseObject;

					// Extract values based on JSON paths
					try {					
						image_id = (long) jsonObject.get("image_id");
					}catch (NullPointerException e) {
						image_id=(long) 0;
					}

					try{
						instance_id = (long) jsonObject.get("instance_id");
					}catch (NullPointerException e) {
						instance_id=(long) 0;
					}
					String image_path = (String) jsonObject.get("image_path");
					String segmentedImagePath =(String) jsonObject.get("Segmented imagepath");
					upperjaw =(String) jsonObject.get("[Predicted classes][1][lowerjaw]");
					lowerjaw =(String) jsonObject.get("[\"Predicted classes\"][1][\"lowerjaw\"]");


					if(reponse.length()<32766) {
						max1=0;
						fullResponseLength=reponse.length();
					}else if(reponse.length()<65532) {
						max1=32766;
						fullResponseLength=reponse.length();
					}else if (reponse.length()<98298) {
						max1=32766;
						max2=65532;
						fullResponseLength=reponse.length();
					}else if(reponse.length()<131064){
						max1=32766;
						max2=65532;
						max3=98298;
						fullResponseLength=reponse.length();
					}
					else if(reponse.length()<163830) {
						max1=32766;
						max2=65532;
						max3=98298;
						max4=131064;
						fullResponseLength=reponse.length();
					}else {
						max1=32766;
						max2=65532;
						max3=98298;
						max4=131064;
						max5=163830;
						fullResponseLength=reponse.length();
					}

					System.out.println(i);
					System.out.println(imageFile.getName());
					System.out.println(reponse.length());

					if(max1==0 && fullResponseLength<32766) {
						response1=	reponse;
						response2=" ";
						response3=" ";
						response4=" ";
						response5=" ";
						response6=" ";
					}else if(max1==32766 && fullResponseLength<65532) {
						response1=	reponse.substring(0, max1);
						response2=	reponse.substring(max1,fullResponseLength);
						response3=" ";
						response4=" ";
						response5=" ";
						response6=" ";
					}else if(max1==32766 && max2==65532 && fullResponseLength<98298){
						response1=	reponse.substring(0, max1);
						response2=	reponse.substring(max1,max2);
						response3=reponse.substring(max2,fullResponseLength);
						response4=" ";
						response5=" "; 
						response6=" ";
					}else if(max1==32766 && max2==65532 && max3 == 98298 && fullResponseLength<131064) {
						response1=	reponse.substring(0, max1);
						response2=	reponse.substring(max1,max2);
						response3=reponse.substring(max2,max3);
						response4=reponse.substring(max3,fullResponseLength);
						response5=" "; 
						response6=" ";
					}else if(max1==32766 && max2==65532 && max3 == 98298 && max4==131064 && fullResponseLength<131064) {
						response1=	reponse.substring(0, max1);
						response2=	reponse.substring(max1,max2);
						response3=reponse.substring(max2,max3);
						response4=reponse.substring(max3,max4);
						response5=reponse.substring(max4,fullResponseLength);
						response6=" ";
					}else {
						response1=	reponse.substring(0, max1);
						response2=	reponse.substring(max1,max2);
						response3=reponse.substring(max2,max3);
						response4=reponse.substring(max3,max4);
						response5=reponse.substring(max4,max5);
						response6=reponse.substring(max5,fullResponseLength);
					}

					writeToExcel(workbook,image_id, instance_id, image_path, segmentedImagePath,response1 ,response2,response3,response4,response5,response6,upperjaw, lowerjaw, imageFile.getName(),  i + 1);

				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();

		}
		catch (NullPointerException e) {
			upperjaw=" ";
			lowerjaw=" ";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	private void writeToExcel(Workbook workbook, Long image_id, Long instance_id,String image_path,String segmentedImagePath, String response1,String response2,String response3,String response4,String response5,String response6,String upperjaw, String lowerjaw, String imageName, int rowNum) {
		try {

			Sheet sheet = workbook.getSheet("API_Response");

			// If the sheet doesn't exist, create a new one
			if (sheet == null) {
				sheet = workbook.createSheet("API_Response");
			}
			Row headerRow = sheet.createRow(0);

			Cell headerCell = headerRow.createCell(0);
			headerCell.setCellValue("Image Name");

			Cell image_idCell = headerRow.createCell(1);
			image_idCell.setCellValue("image_id");

			Cell instance_idCell = headerRow.createCell(2);
			instance_idCell.setCellValue("instance_id");

			Cell iimage_pathCell = headerRow.createCell(3);
			iimage_pathCell.setCellValue("image_path");

			Cell segmentedImagePathCell = headerRow.createCell(4);
			segmentedImagePathCell.setCellValue("segmentedImagePath");

			Cell responseCell1 = headerRow.createCell(5);
			responseCell1.setCellValue("Reponse1");

			Cell responseCell2 = headerRow.createCell(6);
			responseCell2.setCellValue("Reponse2");

			Cell responseCell3 = headerRow.createCell(7);
			responseCell3.setCellValue("Reponse3");

			Cell responseCell4 = headerRow.createCell(8);
			responseCell4.setCellValue("Reponse4");

			Cell responseCell5 = headerRow.createCell(9);
			responseCell5.setCellValue("Reponse5");
			
			Cell responseCell6 = headerRow.createCell(10);
			responseCell6.setCellValue("Reponse6");
			
			Cell upperjawCell = headerRow.createCell(11);
			upperjawCell.setCellValue("Upper Jaw");

			Cell lowerjawCell = headerRow.createCell(12);
			lowerjawCell.setCellValue("Lower Jaw");

			Row dataRow = sheet.createRow(rowNum);

			Cell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(imageName);

			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(image_id);

			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(instance_id);

			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(image_path);

			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(segmentedImagePath);

			dataCell = dataRow.createCell(5);
			dataCell.setCellValue(response1);

			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(response2);

			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(response3);

			dataCell = dataRow.createCell(8);
			dataCell.setCellValue(response4);

			dataCell = dataRow.createCell(9);
			dataCell.setCellValue(response5);
			
			dataCell = dataRow.createCell(10);
			dataCell.setCellValue(response5);


			dataCell = dataRow.createCell(11);
			dataCell.setCellValue(upperjaw);

			dataCell = dataRow.createCell(12);
			dataCell.setCellValue(lowerjaw);

			// Write to Excel file
			try (FileOutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") +
					"\\src\\test\\resources\\CollectData\\Byte.xlsx")) {
				workbook.write(outputStream);
			}


			//			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}	


	private static void writeJsonStringToFile(String jsonString, String filePath) {
		try (FileWriter fileWriter = new FileWriter(filePath)) {
			// Write the JSON string to the file
			fileWriter.write(jsonString);
			System.out.println("JSON data written to file: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}











/*	private String authToken;
	@BeforeClass
	public void loginAndGetToken() {
		Response response = RestAssured.given()
				.param("grant_type", "")
				.param("username", "praveen@capestart.com")
				.param("password", "Praveen!123")
				.param("scope", "")
				.param("client_id", "")
				.param("client_secret", "")
				.header("accept", "application/json")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.when()
				.post("http://byteuat-segment.capestart.com/auth");

		authToken = response.jsonPath().getString("access_token");
		System.out.println(authToken);
	}

	@Test
	public void testImageSegmentation() {
		try {
			File imageFolder = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\TestData");
			File[] imageFiles = imageFolder.listFiles();

			if (imageFiles != null) {
				for (int i = 0; i < imageFiles.length; i++) {
					File imageFile = imageFiles[i];


					Response segmentationResponse = RestAssured.given()
							.queryParam("token", authToken)
							.queryParam("user_id", 8)
							.multiPart("Image_file", imageFile, "image/jpeg")
							//		.queryParam("instance_id")
							.header("accept", "application/json")
							.contentType(ContentType.MULTIPART)
							.when()
							.post("http://byteuat-segment.capestart.com/bytecom/api/v1/teeth/segmentation/");

					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonResponse = objectMapper.readTree(segmentationResponse.getBody().asString());

					// Extract values based on JSON paths
					String image_id = jsonResponse.path("image_id").asText();
					String instance_id = jsonResponse.path("instance_id").asText();
					String image_path = jsonResponse.path("image_path").asText();

					String segmentedImagePath = jsonResponse.path("[\"Segmented imagepath\"]").asText();
					String upperjaw = jsonResponse.path("x[\"Predicted classes\"][0]").asText();
					String lowerjaw = jsonResponse.path("[\"Predicted classes\"][1]").asText();

					writeToExcel(image_id, instance_id, image_path, segmentedImagePath, upperjaw, lowerjaw, imageFile.getName(),  i + 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void writeToExcel(String image_id, String instance_id,String image_path,String segmentedImagePath, String upperjaw, String lowerjaw, String imageName, int rowNum) {
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("API_Response");

			Row headerRow = sheet.createRow(0);
			Cell headerCell = headerRow.createCell(0);
			headerCell.setCellValue("Image Name");

			Cell image_idCell = headerRow.createCell(1);
			image_idCell.setCellValue("image_id");

			Cell instance_idCell = headerRow.createCell(2);
			instance_idCell.setCellValue("instance_id");

			Cell iimage_pathCell = headerRow.createCell(3);
			iimage_pathCell.setCellValue("image_path");

			Cell segmentedImagePathCell = headerRow.createCell(4);
			segmentedImagePathCell.setCellValue("segmentedImagePath");

			Cell upperjawCell = headerRow.createCell(5);
			upperjawCell.setCellValue("Upper Jaw");

			Cell lowerjawCell = headerRow.createCell(6);
			lowerjawCell.setCellValue("Lower Jaw");

			Row dataRow = sheet.createRow(rowNum);
			Cell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(imageName);

			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(image_id);

			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(instance_id);

			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(image_path);

			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(segmentedImagePath);

			dataCell = dataRow.createCell(5);

			dataCell.setCellValue(upperjaw);
			dataCell.setCellValue(segmentedImagePath);

			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(lowerjaw);

			// Write to Excel file
			try (FileOutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") +
					"\\src\\test\\resources\\CollectData\\Byte.xlsx")) {
				workbook.write(outputStream);
			}

			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}*/