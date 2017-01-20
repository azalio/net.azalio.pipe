
import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by azalio on 12.01.17.
 * https://github.com/theoldreader/api
 */
public class TheOldReader {

    private String oAuth;
    private String authGoogle;
    private static final String CONFIG = "config.properties";

    /**
     * Констурктор устанавливающий OAuth токен и формирующий строку для хедера Authorization
     */
    public TheOldReader() {
        this.oAuth = getAuthGoogle();
        this.authGoogle = "GoogleLogin auth=".concat(oAuth);
    }

    /**
     * Читаем из конфига OAuth токен
     *
     * @return OAuth токен
     */
    private String getAuthGoogle() {
        Properties prop = GetConfig.getProperties(CONFIG);
        // System.out.printf("Reader OAuth: %s%n", prop.getProperty("TheOldReader"));
        return prop.getProperty("TheOldReader");
    }

    /**
     * Проверяем доступность апи
     *
     * @return true or false
     */
    public boolean getStatus() {
        boolean status = false;
        String url = "https://theoldreader.com/reader/api/0/status?output=json";
        HttpRequest request = HttpRequest.get(url);

        if (request.code() == 200) {
            String response = request.body();
            JSONObject json = new JSONObject(response);
            if (json.getString("status").equals("up")) {
                System.out.println(json.getString("status"));
                status = true;
            }
        }
        return status;
    }

    /**
     * Получаем все каналы.
     *
     * @return возвращаем не прочитанные фиды.
     */
    public Map<String, Integer> getUnread() {
        Map<String, Integer> unread = new HashMap<String, Integer>();
        String url = "http://theoldreader.com/reader/api/0/unread-count?output=json";
        try {
            HttpRequest request = HttpRequest.get(url).authorization(authGoogle);
            String response = request.body();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray unreadcounts = jsonResponse.getJSONArray("unreadcounts");
            for (int i = 0; i < unreadcounts.length(); i++) {
                JSONObject json = unreadcounts.getJSONObject(i);
                int count = json.getInt("count");
                if (count > 0) {
                    String id = json.getString("id");
                    unread.put(id, count);
                }
            }
        } catch (HttpRequest.HttpRequestException e) {
            e.printStackTrace();
        }
        return unread;
    }

    /**
     * Из не прочитанных фидов получаем ссылки на не прочитанные посты.
     *
     * @param item
     * @return itemsId
     */
    public ArrayList<String> getItems(Map item) {
        String url = "https://theoldreader.com/reader/api/0/stream/items/ids?output=json&s=";
        ArrayList<String> feeds = new ArrayList();
        ArrayList<String> itemsId = new ArrayList<String>();
        item.forEach((id, count) -> feeds.add(id.toString()));
        for (String feed : feeds) {
            if (feed.startsWith("feed")) {
                String urlToFeed = url.concat(feed);
                try {
                    HttpRequest request = HttpRequest.get(urlToFeed).authorization(authGoogle);
                    String response = request.body();
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray items = jsonResponse.getJSONArray("itemRefs");

                    for (int i = 0; i < items.length(); i++) {

                        JSONObject json = items.getJSONObject(i);
                        String id = json.getString("id");
                        itemsId.add(id);
                        break;
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    e.printStackTrace();
                }

            }
        }
        return itemsId;
    }

    /**
     * Получаем массив не прочитанных id постов, возвращаем сами посты.
     *
     * @param itemsId
     */
    public JSONArray getPost(ArrayList<String> itemsId) {
        String url = "https://theoldreader.com/reader/api/0/stream/items/contents?" +
                "output=json&i=tag:google.com,2005:reader/item/";
        JSONArray feedData = new JSONArray();
        for (String item : itemsId) {
            String urlToPost = url.concat(item);
            try {
                HttpRequest request = HttpRequest.get(urlToPost).authorization(authGoogle);
                String response = request.body();
                // System.out.println(response);
                JSONObject jsonResponse = new JSONObject(response);
                String id = jsonResponse.getString("id");
                // System.out.println(id);
                JSONArray items = jsonResponse.getJSONArray("items");
                // System.out.println(items);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject post = items.getJSONObject(i);
                    String title = post.getString("title");
                    // System.out.println(title);
                    int published = post.getInt("published");
                    // System.out.println(published);
                    JSONArray canonical = post.getJSONArray("canonical");
                    // System.out.println(canonical);
                    String href = canonical.getJSONObject(0).getString("href");
                    // System.out.println(href);
                    String author = post.getString("author");
                    // System.out.println(author);
                    JSONObject summary = post.getJSONObject("summary");
                    String content = summary.getString("content");
                    // Telegraph parse = new Telegraph();
                    // System.out.println(content);
                    // parse.makeNodeElement(content, href);

                    // System.out.println(content);
                    // feedData.put("feedId", id);
                    JSONArray array = new JSONArray();
                    JSONObject feedItem = new JSONObject();
                    feedItem.put("feedID", id);
                    feedItem.put("published", published);
                    feedItem.put("title", title);
                    feedItem.put("href", href);
                    feedItem.put("author", author);
                    feedItem.put("content", content);
                    feedData.put(feedItem);
                }

            } catch (HttpRequest.HttpRequestException e) {
                e.printStackTrace();
            }
            break;
        }
        return feedData;
    }


    public void addFeed() {

    }
}