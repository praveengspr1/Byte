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


public class BestFramePicker {
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
				.post("http://byteuat-frames.capestart.com/auth");

		authToken = response.jsonPath().getString("access_token");
		System.out.println(authToken);
	}

	@Test
	public void testImageSegmentation() {
		String upperjaw=null;
		String lowerjaw=null;
		Long image_id=null;
		Long instance_id=null;
		int max=0;
		Workbook workbook = new XSSFWorkbook();
		try {
			File imageFolder = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\TestData");
			//						File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\segmentation\\1st Sample 100 Images Iteration -1");
			//			File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\for validation\\for validation");
			File[] imageFiles = imageFolder.listFiles();
			System.out.println(imageFiles.length);
			if (imageFiles != null) {
				for (int i = 0; i < imageFiles.length; i++) {
					File imageFile = imageFiles[i];


					Response segmentationResponse = RestAssured.given()
							.queryParam("token", authToken)
							.queryParam("user_id", 8)
							//							.multiPart("file", imageFile, "image/jpeg")
							.multiPart("file", imageFile, "image/png")
							.formParam("Mode", "Image")
							//		.queryParam("instance_id")
							.header("accept", "application/json")
							.contentType(ContentType.MULTIPART)
							.when()
							.post("http://byteuat-frames.capestart.com/bytecom/api/v1/teeth/find/bestframe/");
					String reponse =segmentationResponse.asPrettyString();

					//					System.out.println(reponse);
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
						instance_id = (long) jsonObject.get("instance_id ");
					}catch (NullPointerException e) {
						instance_id=(long) 0;
					}
					Long user_id =  (Long) jsonObject.get("user_id");
					String face_angle =(String) jsonObject.get("face_angle");

					if(reponse.length()<32766) {
						max=reponse.length();
					}else {
						max=32766;
					}


					System.out.println(i);
					System.out.println(imageFile.getName());

					writeToExcel(workbook,image_id, instance_id, user_id, face_angle, reponse.substring(0, max),upperjaw, lowerjaw, imageFile.getName(),  i + 1);

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




	private void writeToExcel(Workbook workbook, Long image_id, Long instance_id,Long  user_id,String face_angle, String reponse,String upperjaw, String lowerjaw, String imageName, int rowNum) {
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
			iimage_pathCell.setCellValue("user_id");

			Cell segmentedImagePathCell = headerRow.createCell(4);
			segmentedImagePathCell.setCellValue("face_angle");

			Cell responseCell = headerRow.createCell(5);
			responseCell.setCellValue("Reponse");


			Row dataRow = sheet.createRow(rowNum);

			Cell dataCell = dataRow.createCell(0);
			dataCell.setCellValue(imageName);

			dataCell = dataRow.createCell(1);
			dataCell.setCellValue(image_id);

			dataCell = dataRow.createCell(2);
			dataCell.setCellValue(instance_id);

			dataCell = dataRow.createCell(3);
			dataCell.setCellValue(user_id);

			dataCell = dataRow.createCell(4);
			dataCell.setCellValue(face_angle);


			try {
				dataCell = dataRow.createCell(5);
				dataCell.setCellValue(reponse);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}	


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
