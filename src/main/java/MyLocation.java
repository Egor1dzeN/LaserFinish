import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MyLocation {
    public static String getMyLocation() throws IOException {
        Document document = Jsoup.connect("https://ip2geolocation.com/").get();
        Elements h = document.getElementsByClass("res");
        String city = String.valueOf(h.get(7));
        String[] arr = city.split(">");
        city = arr[1];
        arr = city.split("<");
        city = arr[0];
        System.out.println("Вы находитесь в городе - "+city);
        return city;
    }
}
