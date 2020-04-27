package sample;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;

public class HttpHandlerForServer implements com.sun.net.httpserver.HttpHandler {

    Map<Long, String> uploadFiles = new HashMap<Long, String>();

    final String PATH_TO_STORAGE = "./storage/";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestFileId;

        String requestMethod = httpExchange.getRequestMethod();

        System.out.println(requestMethod);

        requestFileId = GetRequestParams(httpExchange);

        switch (requestMethod) {
            case "GET" -> handleGETRequest(httpExchange, requestFileId);
            case "POST" -> {
                try {
                    hendlePostRequest(httpExchange, requestFileId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "HEAD" -> handleHeadRequest(httpExchange, requestFileId);
            case "DELETE" -> handleDeleteRequest(httpExchange, requestFileId);
        }

    }

    private String GetRequestParams(HttpExchange httpExchange) {
        return httpExchange.
                getRequestURI()
                .toString()
                .split("/")[1];
    }

    private void handleGETRequest(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
        OutputStream outputStream = httpExchange.getResponseBody();

        byte[] buf = new byte[64*1024];

        File file = searchFileByID(requestParamValue);

        if (!file.exists()) {

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,-1);
        } else {

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());

            FileInputStream fileInputStream = new FileInputStream(file.getPath());
            int bytesCount = 1;
            while (bytesCount > 0) {
                bytesCount = fileInputStream.read(buf);
                if (bytesCount > 0) outputStream.write(buf, 0, bytesCount);
            }
            fileInputStream.close();
        }
        outputStream.flush();
        outputStream.close();
    }


    private void handleHeadRequest(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
        OutputStream outputStream = httpExchange.getResponseBody();

        File file = searchFileByID(requestParamValue);

        if (!file.exists()) {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,-1);
        } else {

            httpExchange.getResponseHeaders().add("Content-Length",Long.toString(file.length()));
            httpExchange.getResponseHeaders().add("File Name",file.getName());
            httpExchange.getResponseHeaders().add("File ID",Long.toString(file.getName().hashCode()));

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);

        }
        outputStream.close();
    }

    private void handleDeleteRequest(HttpExchange httpExchange, String requestParamValue) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();

        File file = searchFileByID(requestParamValue);

        if (!file.exists()) {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,0);
        } else {
            file.delete();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            uploadFiles.remove(Long.valueOf(requestParamValue));
        }
        outputStream.close();
    }

    private void hendlePostRequest(HttpExchange httpExchange, String requestParamValue) throws IOException, InterruptedException {

        File file = new File(PATH_TO_STORAGE+requestParamValue);


        byte[] fileContent = httpExchange.getRequestBody().readAllBytes();
        int responseCode = saveFile(file,fileContent);

        uploadFiles.put((long) file.getName().hashCode(), file.getName());

        String fileId = Long.toString(requestParamValue.hashCode());

        httpExchange.sendResponseHeaders(responseCode, fileId.length());

        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(fileId.getBytes());
        outputStream.flush();
        outputStream.close();

    }

    private int saveFile(File file, byte[] buff) throws IOException {
        if (!file.exists()) {
            if (file.createNewFile()){
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(buff);
                fileOutputStream.close();
                return HttpURLConnection.HTTP_OK;
            }
        } else {
            return HttpURLConnection.HTTP_ACCEPTED;
        }
        return 0;
    }


    private File searchFileByID(String  fileId){
        System.out.println( uploadFiles.values());

        String pathToFile = PATH_TO_STORAGE+uploadFiles.get(Long.valueOf(fileId));
        System.out.println(pathToFile);
        return new File(pathToFile);
    }
}
