package Utils;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Utility class for reading Excel files using Apache POI
public class ExcelReader {

    // Method 1: Return data as List of Maps (for REST Assured or custom test logic)
    public static List<Map<String, String>> readExcelAsMap(String filePath, String sheetName) {
        // list to hold the data read from the Excel file
        List<Map<String, String>> dataList = new ArrayList<>();
        // Validate file path and sheet name
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             // Create a Workbook instance from the file input stream
             Workbook workbook = WorkbookFactory.create(fis)) {
            // Get the specified sheet by name
            Sheet sheet = workbook.getSheet(sheetName);
            // If the sheet is not found, throw an exception
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);
            // Get the first row which contains the headers
            Row header = sheet.getRow(0);
            // get the number of columns in the header row
            int colCount = header.getLastCellNum();
            // Iterate through the rows starting from the second row (index 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                // Get the current row
                Row row = sheet.getRow(i);
                // If the row is null (empty), skip to the next iteration
                if (row == null) continue;
                // Create a map to hold the key-value pairs for the current row
                Map<String, String> rowMap = new LinkedHashMap<>();
                // Iterate through each cell in the row
                for (int j = 0; j < colCount; j++) {
                    // Get the cell value as a string
                    String key = header.getCell(j).getStringCellValue().trim();
                    // Get the cell value for the current row and column
                    String value = getCellValueAsString(row.getCell(j));
                    // Add the key-value pair to the map
                    rowMap.put(key, value);
                }
                // Add the map to the list
                dataList.add(rowMap);
            }
            // If no rows were read, throw an exception
        } catch (IOException e) {
            // Wrap the IOException in a RuntimeException with a custom message
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }
        // Return the list of maps containing the data read from the Excel file
        return dataList;
    }

    //  Method 2: Return data as Object[][] for use in JUnit/TestNG DataProviders
    public static Object[][] readExcelAsArray(String filePath, String sheetName) {
        // store the data read from the Excel file
        Object[][] data;
        // Create a FileInputStream to read the Excel file
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             // Create a Workbook instance from the file input stream
             Workbook workbook = WorkbookFactory.create(fis)) {//
            // Get the specified sheet by name
            Sheet sheet = workbook.getSheet(sheetName);
            // If the sheet is not found, throw an exception
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);
            // Get the last row number to determine the number of rows
            int rowCount = sheet.getLastRowNum(); // skips header
            // Get the first row which contains the headers
            Row headerRow = sheet.getRow(0);
            // Get the last cell number in the header row to determine the number of columns
            int colCount = headerRow.getLastCellNum();
            // Initialize the data array with the number of rows and columns
            data = new Object[rowCount][colCount];
            // Iterate through the rows starting from the second row (index 1)
            for (int i = 1; i <= rowCount; i++) {
                // Get the current row
                Row row = sheet.getRow(i);
                // If the row is null (empty), continue to the next iteration
                for (int j = 0; j < colCount; j++) {
                    // Get the cell in the current row and column
                    Cell cell = row != null ? row.getCell(j) : null;
                    // Get the cell value as a string and store it in the data array
                    data[i - 1][j] = getCellValueAsString(cell);
                }
            }
            // If no rows were read, throw an exception
        } catch (IOException e) {
            // Wrap the IOException in a RuntimeException with a custom message
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }
        // Return the data array containing the values read from the Excel file
        return data;
    }

    // Helper method to get the cell value as a string
    private static String getCellValueAsString(Cell cell) {
        // If the cell is null, return an empty string
        if (cell == null) return "";
        // Switch statement to handle different cell types
        return switch (cell.getCellType()) {
            // Handle different cell types and return the appropriate string representation
            case STRING -> cell.getStringCellValue().trim();
            // Handle numeric cells, checking if they are formatted as dates
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    // If the cell is formatted as a date, return the date as a string
                    ? cell.getDateCellValue().toString()
                    // Otherwise, return the numeric value as a string
                    : String.valueOf(cell.getNumericCellValue());
            // Handle boolean cells
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            // Handle formula cells, returning the formula as a string
            case FORMULA -> cell.getCellFormula();
            // Handle blank cells, returning an empty string
            default -> "";
        };
    }
}


/*
Examples
As Map List (for REST Assured or any custom test logic)
List<Map<String, String>> data = ExcelReader.readExcelAsMap("src/test/resources/TestData/users.xlsx", "Users");

for (Map<String, String> row : data) {
        System.out.println("Username: " + row.get("username") + ", Password: " + row.get("password"));
        }

As Object[][] (for JUnit/TestNG DataProviders)
Object[][] data = ExcelReader.readExcelAsArray("src/test/resources/TestData/users.xlsx", "Users");

System.out.println(data[0][0]); // first row, first column
System.out.println(data[0][1]); // first row, second column
*/
