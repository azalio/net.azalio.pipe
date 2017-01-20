
// import okhttp3.*;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
// import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
// import org.json.*;

import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;


/**
 * Created by azalio on 18.01.17.
 */
public class Telegraph {

    // http://telegra.ph/api#Node
    static final String ALLOWTAGS = "a, aside, b, blockquote, br, code, em, figcaption, figure" +
            "h3, h4, hr, i, iframe, img, li, ol, p, pre, s, strong, u, ul, video";
    static final String[] ALLOWATTRS = {"href", "src"};

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    /**
     * http://telegra.ph/api#createAccount
     *
     * @param args [shortName req, author_name, author_url ]
     * @return oAuth token
     */
    public String createAccount(String[] args) {
        String oAuth = null;

        String shortName = args[0];

        String authorName = "";
        String authorUrl = "";
        try {
            authorName = args[1];
            authorUrl = args[2];
        } catch (IndexOutOfBoundsException ex) {
            ;
        }

        String url = "https://api.telegra.ph/createAccount?short_name=%s&author_name=%s&author_url=%s";
        url = String.format(url, shortName, authorName, authorUrl);


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response responses = null;

        // {"ok":true,
        // "result":{"short_name":"azalio",
        //          "author_name":"",
        //          "author_url":"",
        //          "access_token":"aaa",
        //          "auth_url":"https:\/\/edit.telegra.ph\/auth\/aaa"}}
        try {
            responses = client.newCall(request).execute();
            String jsonData = responses.body().string();
            JSONObject jsonObject = new JSONObject(jsonData);
            String result = jsonObject.get("result").toString();
            String status = jsonObject.get("ok").toString();
            if (status.equals("true")) {

                String accessToken = new JSONObject(result).get("access_token").toString();
                String authUrl = new JSONObject(result).get("auth_url").toString();
                oAuth = accessToken;
            } else {
                String error = new JSONObject(result).get("error").toString();
                System.out.printf("Some errors while getting OAuth token: %s\n", error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return oAuth;
    }

    /**
     * @param oAuth      - access_token
     * @param title      - (String, 1-256 characters)
     * @param authorName Author name, displayed below the article's title.
     * @param authorUrl  - Profile link, opened when users click on the author's name below the title.
     *                   Can be any link, not necessarily to a Telegram profile or channel.
     * @param content    -  (Array of Node, up to 64 KB)
     * @return link to created page
     */
    public String createPage(String oAuth, String title, String authorName, String authorUrl, JSONArray content) {
        String returnLink = null;

        String url = "https://api.telegra.ph/createPage?access_token=%s" +
                "&title=%s" +
                "&author_name=%s" +
                "&author_url=%s" +
                "&content=%s" +
                "&return_content=false";
        url = String.format(url, oAuth, title, authorName, authorUrl, content);

        String postUrl = "https://api.telegra.ph/createPage";

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("access_token", oAuth)
                .add("title", title)
                .add("author_name", authorName)
                .add("author_url", authorUrl)
                .add("content", content.toString())
                .add("return_content", "false")
                .build();


        Request request = new Request.Builder()
                .url(postUrl)
                // .addHeader("Authorization", oAuth)
                .post(body)
                .build();
        Response responses = null;

        try {
            responses = client.newCall(request).execute();
            String jsonData = responses.body().string();
            System.out.println(jsonData);
            JSONObject jsonObject = new JSONObject(jsonData);

            String status = jsonObject.get("ok").toString();
            if (status.equals("true")) {
                String result = jsonObject.get("result").toString();
                returnLink = new JSONObject(result).get("url").toString();
            } else {
                String error = jsonObject.get("error").toString();
                System.out.printf("Some errors while create Page: %s\n", error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnLink;
    }

    /**
     * Принимаем чистый html, обрабатываем так, чтобы превратить его в NodeElement
     * который понимает telegraph.api
     *
     * @param html
     * @return JsonArray NodeElement
     */
    public JSONArray doNode(String html) {
        html = html.replaceAll("<br>", "<p><br>");
        Document doc = Jsoup.parse(html);
        Elements body = doc.select("body");

        /**
         * Первый елемент после body обварачиваем тегами <p></p>, чтобы парсер работал.
         */
        body.first().childNode(0).wrap("<p>");

        /**
         * Main array of NodeDocument
         */
        JSONArray jsonArray = new JSONArray();

        /**
         * Select only allowed tags ( telegraph api )
         */
        Elements tags = doc.select(ALLOWTAGS);

        for (Element content : tags) {
            Tag tag = content.tag();
            String text = content.text();

            String attributeText = null;

            JSONObject currentJson = new JSONObject();
            currentJson.put("tag", tag.toString());

            for (String attr : ALLOWATTRS) {
                if (!content.attr(attr).equals("")) {
                    // String attribyte = attr;

                    String attributeContent = null;

                    attributeContent = content.attr(attr);

                    JSONObject attributeJson = new JSONObject();
                    attributeJson.put(attr, attributeContent);
                    currentJson.put("attrs", attributeJson);

                    attributeText = content.text();
                    if (attributeText.trim().length() > 0) {
                        // JSONObject children = new JSONObject();
                        JSONArray childrenArr = new JSONArray();
                        childrenArr.put(attributeText);
                        currentJson.put("children", childrenArr);
                        /**
                         * Если у атрибута есть свой текст, то надо удалить текст из его тега,
                         * иначе будет повтор.
                         */
                        text = null;
                    }


                }

            }


            if (text != null && text.trim().length() > 0) {
                JSONArray childText = new JSONArray();
                childText.put(text);
                currentJson.put("children", childText);

            }
            jsonArray.put(currentJson);
        }
        return jsonArray;
    }
}