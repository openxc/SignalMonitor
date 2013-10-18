/**
 * Do I really even need a whole class for this one?
 */
public class PostalAsync {
    AsyncHttpPost asyncHttpPost = new AsyncHttpPost(data);
    asyncHttpPost.execute("http://shatechcrunchhana.sapvcm.com:8000/Ford/services/fordstatus.xsodata/FordStatus");
}

// actually, this now looks a lot like uploadSnapshot
