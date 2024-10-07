package TestCases;

import static org.testng.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import Utils.FileNameContants;
import io.restassured.RestAssured;
import io.restassured.response.Response;


public class DataDriventesting {
  @Test (dataProvider = "ExcelTestData")
  public void DataDrivenTesting(Map<String,String> testData) {
	  String userId = testData.get("ID");
	  Response response = RestAssured.given()
	            .pathParam("id", userId)
	            .when()
	            .get("https://reqres.in/api/users/{id}")
	            .then()
	            .statusCode(200) // Assert the status code is 200 for valid user ID
	            .extract().response();
	  
	  // Assert that the user ID in response matches the one from the Excel file
      int responseId = response.jsonPath().getInt("data.id");
      assertEquals(responseId, Integer.parseInt(userId), "User ID mismatch!");


      System.out.println("User ID: " + userId + response);
      
      if (Integer.parseInt(userId) == 3) {
          deleteUser(userId);
      }
  }
  
  public void deleteUser(String userId) {
      Response deleteResponse = RestAssured.given()
          .pathParam("id", userId)
          .when()
          .delete("https://reqres.in/api/users/{id}")
          .then()
          .statusCode(204) // Assert the delete operation was successful (204 No Content)
          .extract().response();

      System.out.println("User with ID: " + userId + " has been deleted.");
  }

  
  @DataProvider(name = "ExcelTestData") //Allows us to supply data in parameterized way to our test methods
  public Object[][] getTestData(){
	  
	  String query = "select * from Sheet1"; //SQL Query to retrive data from the excel sheet (Fillo dependency allows us to treat excel sheet data like Database)
	  
	  Object[][] objArray = null; 
	  Map<String,String> testData = null; //save everything in form of key,value
	  List<Map<String,String>> testDataList = null; //This list will store multiple map objects,Each representing a row to test data
	  Fillo fillo = new Fillo();	 
	  Connection connection = null; //holds connection to excel file.
	  Recordset recordset = null; //stores the result of the querry.
	  
	 try {
		connection = fillo.getConnection(FileNameContants.EXCEL_TEST_DATA); //connection with Excel sheet
		recordset = connection.executeQuery(query); //Returns the data to recordset
		
		testDataList = new ArrayList<Map<String,String>>();
		
		while(recordset.next()) {
			testData = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER); //For every value treemap is generated and it hold key and value
			
			for(String field : recordset.getFieldNames()) {
				testData.put(field, recordset.getField(field));
			} //loops through all columns for every row and put method add key and value to the testData map.
			
			testDataList.add(testData);
			
		}
		
		objArray = new Object[testDataList.size()][1];
		 
		for(int i = 0; i < testDataList.size(); i++) {
			objArray[i][0] = testDataList.get(i);		}
		
	} catch (FilloException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return objArray;
  }
  }


/* The code reads data from the Excel sheet specified in FileNameContants.EXCEL_TEST_DATA.
For each row, it creates a Map<String, String> where the key is the column name and the value is the corresponding cell data.
All these maps are added to a List<Map<String, String>>, representing all rows of the Excel sheet.
The list is converted into a 2D object array (Object[][]), which is the expected format for TestNG’s DataProvider.
This data is then fed into test methods to execute data-driven testing.*/

