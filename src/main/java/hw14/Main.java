package hw14;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import static java.util.Comparator.comparing;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        var imageUrls = getImageUrls();
        var maxSizePair = findMaxSizeUrl(imageUrls);
        System.out.printf("url: %s; size: %d%n", maxSizePair.getKey(), maxSizePair.getValue());
    }

    private static List<String> getImageUrls() throws IOException {
        var url = new URL("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=16&api_key=b4CCRzB2MX3HkPj9d8MaI25RXW7LkG5pMUpV9v3q");
        try (var socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(url.getHost(), 443);
             var writer = new PrintWriter(socket.getOutputStream());
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            socket.startHandshake();
            writer.println("GET " + url.getPath() + "?" + url.getQuery() + " HTTP/1.1");
            writer.println("Host: " + url.getHost());
            writer.println();
            writer.flush();
            var json = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("{") || line.contains("}")) {
                    json.append(line);
                }
                if (!json.isEmpty() && line.isBlank()) {
                    break;
                }
            }
            return new ObjectMapper().readTree(json.toString())
                    .findValuesAsText("img_src");
        }
    }

    private static Pair<String, Long> findMaxSizeUrl(List<String> imageUrls) {
        return imageUrls.parallelStream()
                .map(Main::getRedirectLink)
                .map(Main::toSizePair)
                .max(comparing(Pair::getValue))
                .orElseGet(() -> Pair.of("Nothing", 0L));
    }

    @SneakyThrows
    private static Pair<String, String> getRedirectLink(String imageUrl) {
        var url = new URL(imageUrl);
        try (var socket = new Socket(url.getHost(), 80);
             var writer = new PrintWriter(socket.getOutputStream());
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.println("GET " + url.getPath() + " HTTP/1.1");
            writer.println("Host: " + url.getHost());
            writer.println();
            writer.flush();
            String line = null;
            while ((line = reader.readLine()) != null && !line.startsWith("Location: ")) {
            }
            var redirectLink = line.split(" ")[1].trim();
            return Pair.of(imageUrl, redirectLink);
        }
    }

    @SneakyThrows
    private static Pair<String, Long> toSizePair(Pair<String, String> urlPair) {
        var url = new URL(urlPair.getValue());
        try (var socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(url.getHost(), 443);
             var writer = new PrintWriter(socket.getOutputStream());
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            socket.startHandshake();
            writer.println("GET " + url.getPath() + "?" + url.getQuery() + " HTTP/1.1");
            writer.println("Host: " + url.getHost());
            writer.println();
            writer.flush();
            String line = null;
            while ((line = reader.readLine()) != null && !line.startsWith("Content-Length: ")) {
            }
            var size = Long.parseLong(line.split(" ")[1].trim());
            return Pair.of(urlPair.getKey(), size);
        }
    }

}
