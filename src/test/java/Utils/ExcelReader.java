package Utils;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    //  Method 1: Return data as List of Maps (field name -> cell value)
    public static List<Map<String, String>> readExcelAsMap(String filePath, String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            Row header = sheet.getRow(0);
            int colCount = header.getLastCellNum();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < colCount; j++) {
                    String key = header.getCell(j).getStringCellValue().trim();
                    String value = getCellValueAsString(row.getCell(j));
                    rowMap.put(key, value);
                }
                dataList.add(rowMap);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return dataList;
    }

    //  Method 2: Return data as Object[][] for use in JUnit/TestNG DataProviders
    public static Object[][] readExcelAsArray(String filePath, String sheetName) {
        Object[][] data;

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            int rowCount = sheet.getLastRowNum(); // skips header
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();

            data = new Object[rowCount][colCount];

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row != null ? row.getCell(j) : null;
                    data[i - 1][j] = getCellValueAsString(cell);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + e.getMessage(), e);
        }

        return data;
    }

    //  Helper to safely extract cell value as String
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
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
