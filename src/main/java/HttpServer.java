package main.java;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpServer implements Closeable {

  static int globalPort = 80;

  public static String[] getEntityHeaders(String serverName, String version, long length, String contentType) {
    return new String[]{
            "Server: " + serverName + "/" + version,
            "Content-Length: " + length,
            "Content-Type: " + contentType
    };
  }

  public static String getResponseHeader(String serverName, String version, int statusCode, String uri) {
    File file = new File("public_html/" + uri);
    long fileLength = file.length();
    String[] entityHeaders = getEntityHeaders(serverName, version, fileLength, getContentType(uri));

    return String.format("HTTP/1.1 %d %s\r\n%s\r\n\r\n",
            statusCode, getReasonPhrase(statusCode), String.join("\r\n", entityHeaders));
  }

  public static int getStatusCode(String domain, int port, String request) {
    Pattern pattern = Pattern.compile("^(GET||HEAD||OPTIONS||POST||PUT||DELETE||TRACE||CONNECT) (\\S+) HTTP/(\\d\\.?\\d?)\r\nHost: ([\\w\\.]+):?(\\d+)?\r\n\r\n");
    Matcher matcher = pattern.matcher(request);

    if (!matcher.find()) {
      return 400; // Bad Request
    }

    String method = matcher.group(1);
    String path = matcher.group(2);
    String httpVersion = matcher.group(3);
    String host = matcher.group(4);
    String requestPort = matcher.group(5);

    File file = new File("public_html/" + path);

    if (!method.equals("HEAD") && !method.equals("GET") ||
            getContentType(path) == null ||
            !httpVersion.equals("1.1")) {
      return 501; // Not Implemented
    } else if (!host.equals(domain) && !host.equals("localhost") && !host.equals("127.0.0.1") ||
            (requestPort == null && port != 80) ||
            (requestPort != null && !requestPort.equals(Integer.toString(port)))) {
      return 400; // Bad Request
    } else if (!file.exists()) {
      return 404; // Not Found
    } else if (getContentType(path) != null) {
      return 200; // OK
    } else {
      return 400; // Bad Request
    }
  }

  public static String getStatusLine(int statusCode) {
    return String.format("HTTP/1.1 %d %s\r\n", statusCode, getReasonPhrase(statusCode));
  }

  public HttpServer(int port) {
    throw new UnsupportedOperationException("HttpServer(int) not yet implemented");
  }

  public void close() throws IOException {
    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }

  public int getPort() {
    return this.globalPort;
  }

  public static String getContentType(String uri) {
    if (uri == null) {
      return null;
    }
    int lastDotIndex = uri.lastIndexOf('.');
    if (lastDotIndex != -1 && lastDotIndex < uri.length() - 1) {
      String fileExtension = uri.substring(lastDotIndex + 1).toLowerCase();
      switch (fileExtension) {
        case "txt":
          return "text/plain";
        case "html":
        case "htm":
          return "text/html";
        case "jpg":
        case "jpeg":
          return "image/jpeg";
        case "png":
          return "image/png";
        case "gif":
          return "image/gif";
        case "pdf":
          return "application/pdf";
        default:
          return null; // Unsupported file type
      }
    }
    return null; // No file extension found
  }

  public static String getReasonPhrase(int statusCode) {
    switch (statusCode) {
      case 200:
        return "OK";
      case 400:
        return "Bad Request";
      case 404:
        return "Not Found";
      case 501:
        return "Not Implemented";
      default:
        return "Null";
    }
  }
}
