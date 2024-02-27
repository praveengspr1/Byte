package com.byte_.qa.test;

import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class SegmentationTest {
	private static String username;
	private static String password;
	private static int userId;
	private static String testFolderPath;
	StringSelection selection;
	public SegmentationTest() {
		Scanner sc= new Scanner(System.in);
		System.out.println("Enter Valid Username");
		username = sc.next();                 //praveen.kumar@capestart.com
		System.out.println("Enter Valid Password");
		password = sc.next();                 //Praveen!123
		System.out.println("Enter Valid User Id");
		userId = sc.nextInt();
		System.out.println("Enter test folder path");
		testFolderPath = sc.nextLine().replace("/", "//");
		System.out.println(testFolderPath);
	}
	private String authToken;
	@BeforeClass
	public void loginAndGetToken() {
		Response response = RestAssured.given()
				.param("grant_type", "")
				.param("username", username)
				.param("password", password ) 
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
		String upperjaw=null;
		String lowerjaw=null;
		Long image_id=null;
		Long instance_id=null;
		int max=0;
		Workbook workbook = new XSSFWorkbook();
		try {
			//			File imageFolder = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\TestData");

			System.out.println(testFolderPath);
			File imageFolder = new File(System.getProperty(testFolderPath));
			//						File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\segmentation\\1st Sample 100 Images Iteration -1");
			//			File imageFolder = new File("C:\\Users\\Admin.CS-LP-909\\Desktop\\for validation\\for validation");
			File[] imageFiles = imageFolder.listFiles();
			System.out.println(imageFiles.length);
			if (imageFiles != null) {
				for (int i = 0; i < imageFiles.length; i++) {
					File imageFile = imageFiles[i];


					Response segmentationResponse = RestAssured.given()
							.queryParam("token", authToken)
							.queryParam("user_id",  userId)
							.multiPart("Image_file", imageFile, "image/jpeg")
							//		.queryParam("instance_id")
							.header("accept", "application/json")
							.contentType(ContentType.MULTIPART)
							.when()
							.post("http://byteuat-segment.capestart.com/bytecom/api/v1/teeth/segmentation/");
					String reponse =segmentationResponse.asString();
					System.out.println(reponse.length());
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

					if(reponse.length()<32766) {
						max=reponse.length();
					}else {
						max=32766;
					}

					System.out.println(i);
					System.out.println(imageFile.getName());

					writeToExcel(workbook,image_id, instance_id, image_path, segmentedImagePath, reponse.substring(0, max),upperjaw, lowerjaw, imageFile.getName(),  i + 1);

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




	private void writeToExcel(Workbook workbook, Long image_id, Long instance_id,String image_path,String segmentedImagePath, String reponse,String upperjaw, String lowerjaw, String imageName, int rowNum) {
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

			Cell responseCell = headerRow.createCell(5);
			responseCell.setCellValue("Reponse");

			Cell upperjawCell = headerRow.createCell(6);
			upperjawCell.setCellValue("Upper Jaw");

			Cell lowerjawCell = headerRow.createCell(7);
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


			try {
				dataCell = dataRow.createCell(5);
				dataCell.setCellValue(reponse);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}	


			dataCell = dataRow.createCell(6);
			dataCell.setCellValue(upperjaw);

			dataCell = dataRow.createCell(7);
			dataCell.setCellValue(lowerjaw);

			// Write to Excel file
			try (FileOutputStream outputStream = new FileOutputStream(testFolderPath+"\\Byte.xlsx")) {
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