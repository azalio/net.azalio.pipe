
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by azalio on 11.01.17.
 */
public class Pipe {
    public static void main(String[] args) {

        TheOldReader feed = new TheOldReader();
        Map<String, Integer> unread = feed.getUnread();
        ArrayList<String> items = feed.getItems(unread);
        JSONArray feeds = feed.getPost(items);

        for (int i = 0; i < feeds.length(); i++) {
            JSONObject feedItem = feeds.getJSONObject(i);
            String feedId = feedItem.get("feedID").toString();
            String author = feedItem.get("author").toString();
            String published = feedItem.get("published").toString();
            String href = feedItem.get("href").toString();
            String title = feedItem.get("title").toString();
            String feedHtmlContent = feedItem.get("content").toString();


            Telegraph t = new Telegraph();
            JSONArray content = t.doNode(feedHtmlContent);

            String[] createAccArgs = {"azalio"};
            String oAuth = t.createAccount(createAccArgs);
            System.out.println(content);
            String urlToPost = t.createPage(oAuth, title, author, href, content);
            System.out.println(urlToPost);


        }
    }
}

//{"direction":"ltr",
//        "id":"feed/55e4b464fea0e77c0f0009e3",
//        "title":"LWN.net","description":"",
//        "self":{"href":"https://theoldreader.com/reader/api/0/stream/items/contents?output=json&i=tag:google.com,2005:reader/item/58767dd9ca9f4008cccd0e19"},
//        "alternate":{"href":"http://lwn.net","type":"text/html"},
//        "updated":1484417651,
//        "items":[{"crawlTimeMsec":"1484160454151",
//            "timestampUsec":"1484158267000000",
//            "id":"tag:google.com,2005:reader/item/58767dd9ca9f4008cccd0e19",
//            "categories":["user/-/state/com.google/reading-list",
//            "user/-/state/com.google/fresh"],
//            "title":"[$] Python 2.8?",
//            "published":1484158267,
//            "updated":1484158267,
//            "canonical":[{"href":"http://lwn.net/Articles/711061/rss"}],
//            "alternate":[{"href":"http://lwn.net/Articles/711061/rss","type":"text/html"}],
//            "summary":{"direction":"ltr",
//                  "content":"<p>\nThe appearance of a \"Python 2.8\" got the attention of the Python core\ndevelopers" +
//                          " in early December.  It is based on Python 2.7, with\nfeatures backported from Python 3.x." +
//                          "  In general, there was little\nsupport for the effort—core developers tend to clearly see Python 3" +
//                          " as\nthe way forward—but no opposition to it either.  The Python license makes\nit clear that" +
//                          " these kinds of efforts are legal and even\nencouraged—any real opposition to the project lies" +
//                          " in its name.\n</p><p>\nSubscribers can click below for the full article from this week's edition.</p>"},
//            "author":"jake",
//            "annotations":[],
//            "likingUsers":[],
//            "likingUsersCount":0,
//            "comments":[],
//            "origin":{"streamId":"feed/55e4b464fea0e77c0f0009e3","title":"LWN.net","htmlUrl":"http://lwn.net"}}
//            ]}
