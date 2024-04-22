package io.shiftleft.tarpit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.shiftleft.tarpit.util.Unzipper;

/**
 * Servlet implementation class FileUploader
 */
@WebServlet("/FileUploader")
@MultipartConfig
public class FileWriter extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static String productSourceFolder = System.getenv("PRODUCT_SRC_FOLDER");
  private static String productDestinationFolder = System.getenv("PRODUCT_DST_FOLDER");

  /**
   * @see HttpServlet#HttpServlet()
   */
  public FileWriter() {
    super();
  }

  public void writeToFile(String filePath, String content) throws IOException {
        // Validate the file path to prevent directory traversal
        if (new File(filePath).getCanonicalPath().startsWith("/var/data")) {
            Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE);
        } else {
            throw new IllegalArgumentException("Invalid file path");
        }
    }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Part filePart = request.getPart("zipFile");

    InputStream input = filePart.getInputStream();

    File targetFile = new File(productSourceFolder, submittedFileName);

    targetFile.createNewFile();
    OutputStream out = new FileOutputStream(targetFile);

    byte[] buffer = new byte[1024];
    int bytesRead;

    while ((bytesRead = input.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }

    input.close();
    out.flush();
    out.close();

    Unzipper.unzipFile(targetFile.getAbsolutePath(), productDestinationFolder);

    doGet(request, response);
  }
  public static void unzipFile(String zipFileWithAbsolutePath, String destination)
      throws IOException {
    if (!doesFileExists(zipFileWithAbsolutePath)) {
      throw new FileNotFoundException("The given zip file not found: " + zipFileWithAbsolutePath);
    }

    String fileName = getFileFromPath(zipFileWithAbsolutePath);
    String finalDestination = getFinalDestination(fileName, destination);
    createDirectoryNamedAsZipFile(finalDestination);

    try {
      // Initiate ZipFile object with the path/name of the zip file.
      ZipFile zipFile = new ZipFile(zipFileWithAbsolutePath);

      // Extracts all files to the path specified
      zipFile.extractAll(finalDestination);

    } catch (ZipException e) {
      e.printStackTrace();
    }

  }

  private static boolean doesFileExists(String fileName) {
    File f = new File(fileName);
    return f.exists();
  }
}
