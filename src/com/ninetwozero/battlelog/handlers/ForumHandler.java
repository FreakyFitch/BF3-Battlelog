
package com.ninetwozero.battlelog.handlers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.ninetwozero.battlelog.datatypes.ForumData;
import com.ninetwozero.battlelog.datatypes.ForumPostData;
import com.ninetwozero.battlelog.datatypes.ForumSearchResult;
import com.ninetwozero.battlelog.datatypes.ForumThreadData;
import com.ninetwozero.battlelog.datatypes.PersonaData;
import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.CacheHandler;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;

public class ForumHandler {

    // URLS
    public static final String URL_LIST = Constants.URL_MAIN + "forum/";
    public static final String URL_LIST_LOCALIZED = Constants.URL_MAIN
            + "{LOCALE}/forum/";
    public static final String URL_FORUM = URL_LIST_LOCALIZED
            + "view/{FORUM_ID}/{PAGE}/";
    public static final String URL_THREAD = URL_LIST_LOCALIZED
            + "threadview/{THREAD_ID}/{PAGE}/";
    public static final String URL_POST = URL_LIST
            + "createpost/{THREAD_ID}/";
    public static final String URL_SIMILAR = URL_LIST
            + "dofindsimilar?title={title}&body={body}";
    public static final String URL_NEW = URL_LIST
            + "createthread/{FORUM_ID}/";
    public static final String URL_SEARCH = URL_LIST
            + "dosearch/?q={SEARCH_STRING}&snippet=text&fetch=threadId%2CforumId%2Ctitle%2Ctimestamp";
    public static final String URL_REPORT = Constants.URL_MAIN
            + "viewcontent/reportForumAbuse/{POST_ID}/";

    // Attributes
    public static final String[] FIELD_NAMES_POST = new String[] {
            "body", "post-check-sum"
    };

    public static final String[] FIELD_NAMES_NEW = new String[] {
            "topic", "body", "post-check-sum"
    };

    public static final String[] FIELD_NAMES_REPORT = new String[] {
            "reason", "post-check-sum"
    };

    public static boolean reportPostInThread(Context c, long pId, String r)
            throws WebsiteHandlerException {

        try {
            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.post(

                    RequestHandler.generateUrl(URL_REPORT, pId),
                    RequestHandler.generatePostData(FIELD_NAMES_REPORT, r, ""),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            return (new JSONObject(httpContent).getJSONObject("data")
                    .getInt("success") == 1);

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("Could not report the post.");

        }

    }

    public static List<ForumThreadData> getThreadsForForum(String locale, long forumId,
            int page) throws WebsiteHandlerException {

        try {

            // Init to winit
            List<ForumThreadData> threads = new ArrayList<ForumThreadData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    RequestHandler.generateUrl(

                            URL_FORUM,
                            locale,
                            forumId,
                            page

                            ), RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");

            // Loop the stickies
            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {

                // Yay, we found at least one sticky
                if (i == 0 && page == 1) {
                    threads.add(new ForumThreadData("Stickies"));
                }

                // Get the current object
                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new ForumThreadData(

                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                        .getLong("creationDate"), currObject
                                        .getLong("lastPostDate"), currObject
                                        .getInt("numberOfOfficialPosts"), currObject
                                        .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), new ProfileData(
                                        Long
                                                .parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username"),
                                        new PersonaData[] {},
                                        lastPosterObject.getString("gravatarMd5")

                                ), currObject.getBoolean("isSticky"), currObject
                                        .getBoolean("isLocked")

                        )

                        );

            }

            // Loop the regular
            for (int i = numStickies, max = threadArray.length(); i < max; i++) {

                if (i == numStickies && page == 1) {
                    threads.add(new ForumThreadData("Threads"));
                }

                // Get the current object
                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new ForumThreadData(

                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                        .getLong("creationDate"), currObject
                                        .getLong("lastPostDate"), currObject
                                        .getInt("numberOfOfficialPosts"), currObject
                                        .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), new ProfileData(
                                        Long
                                                .parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username"),
                                        new PersonaData[] {},
                                        lastPosterObject.getString("gravatarMd5")

                                ), currObject.getBoolean("isSticky"), currObject
                                        .getBoolean("isLocked")

                        )

                        );

            }

            return threads;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static List<ForumPostData> getPostsForThread(long threadId,
            int page, String locale) throws WebsiteHandlerException {

        try {

            // Init to winit
            List<ForumPostData> posts = new ArrayList<ForumPostData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    RequestHandler.generateUrl(

                            URL_THREAD,
                            locale,
                            threadId,
                            page

                            ),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            JSONArray postArray = new JSONObject(httpContent).getJSONObject(
                    "context").getJSONArray("posts");

            // Loop the stickies
            for (int i = 0, max = postArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                // Let's store them
                posts.add(

                        new ForumPostData(

                                Long.parseLong(currObject.getString("id")), Long
                                        .parseLong(currObject.getString("creationDate")), Long
                                        .parseLong(currObject.getString("threadId")),
                                new ProfileData(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), currObject.getString("formattedBody"), currObject
                                        .getInt("abuseCount"), currObject
                                        .getBoolean("isCensored"), currObject
                                        .getBoolean("isOfficial")

                        )

                        );

            }

            return (ArrayList<ForumPostData>) posts;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No posts found for the thread.");

        }

    }

    public static ArrayList<ForumSearchResult> search(

            final Context c, final String keyword

            ) throws WebsiteHandlerException {

        // Init
        RequestHandler rh = new RequestHandler();
        List<ForumSearchResult> threads = new ArrayList<ForumSearchResult>();

        try {

            // Let's do the actual search
            String httpContent = rh.get(

                    RequestHandler.generateUrl(URL_SEARCH, keyword),
                    RequestHandler.HEADER_AJAX

                    );
            if (!"".equals(httpContent)) {

                // Let's parse it as JSON
                JSONArray threadArray = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("results");
                int numResults = threadArray.length();

                // Let's see...
                if (numResults > 0) {

                    // Iterate over the results!
                    for (int i = 0; i < numResults; i++) {

                        // Let's get the current result-set
                        JSONObject currentItem = threadArray.getJSONObject(i);

                        threads.add(

                                new ForumSearchResult(

                                        Long.parseLong(currentItem.getString("docid")
                                                .substring(2)),
                                        currentItem
                                                .getLong("timestamp"),
                                        currentItem
                                                .getString("title"),
                                        new ProfileData(
                                                Long
                                                        .parseLong(currentItem.getString("ownerId")),
                                                currentItem.getString("ownerUsername"),
                                                new PersonaData[] {},
                                                null),
                                        currentItem.getBoolean("isSticky"), currentItem
                                                .getBoolean("isOfficial")

                                )

                                );

                    }

                }
            }

            return (ArrayList<ForumSearchResult>) threads;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static Object[] getAllForums(String locale)
            throws WebsiteHandlerException {

        try {

            // Init to win it
            List<ForumData> forums = new ArrayList<ForumData>();
            String title = "";

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    RequestHandler.generateUrl(URL_LIST_LOCALIZED, locale),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            JSONArray categoryArray = new JSONObject(httpContent)
                    .getJSONObject("context").getJSONArray("categories");
            JSONArray forumArray = categoryArray.getJSONObject(0).optJSONArray(
                    "forums");

            // Get the title
            title = categoryArray.getJSONObject(0).getString("title");

            // Loop
            for (int i = 0, max = forumArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = forumArray.getJSONObject(i);
                JSONObject lastThread = currObject.optJSONObject("lastThread");

                // Let's store them
                if (lastThread != null) {

                    JSONObject userInfo = lastThread
                            .optJSONObject("lastPoster");

                    if (userInfo != null) {

                        forums.add(

                                new ForumData(

                                        Long.parseLong(currObject.getString("id")), Long
                                                .parseLong(currObject.getString("categoryId")),
                                        lastThread.getLong("lastPostDate"), Long
                                                .parseLong(lastThread.getString("id")),
                                        Long.parseLong(lastThread
                                                .getString("lastPostId")), currObject
                                                .getLong("numberOfPosts"), currObject
                                                .getLong("numberOfThreads"), 0,
                                        currObject.getString("title"), currObject
                                                .getString("description"), lastThread
                                                .getString("title"), userInfo
                                                .getString("username")

                                )

                                );

                    }

                } else {

                    forums.add(

                            new ForumData(

                                    Long.parseLong(currObject.getString("id")), Long
                                            .parseLong(currObject.getString("categoryId")), 0,
                                    0, 0, currObject.getLong("numberOfPosts"),
                                    currObject.getLong("numberOfThreads"), 0,
                                    currObject.getString("title"), currObject
                                            .getString("description"), null, null

                            )

                            );

                }

            }

            // Return
            return new Object[] {
                    title, forums
            };

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No forums found.");

        }

    }

    public static ForumData getThreadsForForum(String locale, long forumId)
            throws WebsiteHandlerException {

        try {

            // Init to winit
            List<ForumThreadData> threads = new ArrayList<ForumThreadData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    RequestHandler.generateUrl(

                            URL_FORUM,
                            locale,
                            forumId,
                            1

                            ),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONObject forumObject = contextObject.getJSONObject("forum");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");

            // Loop the stickies
            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {

                // Yay, we found at least one sticky
                if (i == 0) {
                    threads.add(new ForumThreadData("Stickies"));
                }

                // Get the current object
                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new ForumThreadData(

                                Long.parseLong(currObject.getString("id")),
                                forumId,
                                currObject
                                        .getLong("creationDate"), currObject
                                        .getLong("lastPostDate"), currObject
                                        .getInt("numberOfOfficialPosts"), currObject
                                        .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(
                                        Long
                                                .parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), new ProfileData(
                                        Long
                                                .parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username"),
                                        new PersonaData[] {},
                                        lastPosterObject.getString("gravatarMd5")

                                ), currObject.getBoolean("isSticky"), currObject
                                        .getBoolean("isLocked")

                        )

                        );

            }

            // Loop the regular
            for (int i = numStickies, max = threadArray.length(); i < max; i++) {

                if (i == numStickies) {
                    threads.add(new ForumThreadData("Threads"));
                }

                // Get the current object
                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new ForumThreadData(

                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                        .getLong("creationDate"), currObject
                                        .getLong("lastPostDate"), currObject
                                        .getInt("numberOfOfficialPosts"), currObject
                                        .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), new ProfileData(
                                        Long
                                                .parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username"),
                                        new PersonaData[] {},
                                        lastPosterObject.getString("gravatarMd5")

                                ), currObject.getBoolean("isSticky"), currObject
                                        .getBoolean("isLocked")

                        )

                        );

            }

            return new ForumData(

                    forumObject.getString("title"),
                    forumObject.getString("description"),
                    forumObject.getLong("numberOfPosts"),
                    forumObject.getLong("numberOfThreads"),
                    contextObject.getLong("numPages"), threads

            );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static ForumThreadData getPostsForThread(String locale,
            long threadId) throws WebsiteHandlerException {

        try {

            // Init to winit
            List<ForumPostData> posts = new ArrayList<ForumPostData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    RequestHandler.generateUrl(

                            URL_THREAD,
                            locale,
                            threadId,
                            1

                            ),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONArray postArray = contextObject.getJSONArray("posts");
            JSONObject threadObject = contextObject.getJSONObject("thread");
            JSONObject lastPosterObject = threadObject
                    .getJSONObject("lastPoster");
            JSONObject threadOwnerObject = threadObject.getJSONObject("owner");

            // Loop the stickies
            for (int i = 0, max = postArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                // Let's store them
                posts.add(

                        new ForumPostData(

                                Long.parseLong(currObject.getString("id")), Long
                                        .parseLong(currObject.getString("creationDate")), Long
                                        .parseLong(currObject.getString("threadId")),
                                new ProfileData(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username"),
                                        new PersonaData[] {},
                                        ownerObject.getString("gravatarMd5")

                                ), currObject.getString("formattedBody"), currObject
                                        .getInt("abuseCount"), currObject
                                        .getBoolean("isCensored"), currObject
                                        .getBoolean("isOfficial")

                        )

                        );

            }

            return new ForumThreadData(

                    Long.parseLong(threadObject.getString("id")),
                    Long.parseLong(threadObject.getString("forumId")),
                    threadObject.getLong("creationDate"),
                    threadObject.getLong("lastPostDate"),
                    threadObject.getInt("numberOfOfficialPosts"),
                    threadObject.getInt("numberOfPosts"),
                    contextObject.getInt("currentPage"),
                    contextObject.getInt("numPages"),
                    threadObject.getString("title"), new ProfileData(
                            Long
                                    .parseLong(threadOwnerObject.getString("userId")),
                            threadOwnerObject.getString("username"),
                            new PersonaData[] {},
                            threadOwnerObject.getString("gravatarMd5")

                    ),
                    new ProfileData(
                            Long
                                    .parseLong(lastPosterObject.getString("userId")),
                            lastPosterObject.getString("username"),
                            new PersonaData[] {},
                            lastPosterObject.getString("gravatarMd5")

                    ), threadObject.getBoolean("isSticky"),
                    threadObject.getBoolean("isLocked"),
                    contextObject.getBoolean("canEditPosts"),
                    contextObject.getBoolean("canCensorPosts"),
                    contextObject.getBoolean("canDeletePosts"),
                    contextObject.getBoolean("canPostOfficial"),
                    contextObject.getBoolean("canViewLatestPosts"),
                    contextObject.getBoolean("canViewPostHistory"),
                    contextObject.getBoolean("isAdmin"), posts

            );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No thread data found.");

        }

    }

    public static boolean postReplyInThread(final Context c, final String body,
            final String chksm, final ForumThreadData threadData, final boolean cache,
            final long uid) {

        try {

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();

            // POST!
            String httpContent = rh.post(

                    RequestHandler.generateUrl(URL_POST, threadData.getId()),
                    RequestHandler.generatePostData(FIELD_NAMES_POST, body, chksm),
                    RequestHandler.HEADER_AJAX

                    );

            // How'd it go?
            if (!"".equals(httpContent)) {

                // Are we to cache it?
                if (cache) {

                    return CacheHandler.Forum.insert(c, threadData, uid) > -1;

                }

                // Return
                return true;

            } else {

                return false;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

    public static boolean createNewThreadInForum(final Context c,
            final String topic, final String body, final String chksm,
            final long fId) {

        try {

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();

            // POST!
            String httpContent = rh.post(

                    RequestHandler.generateUrl(URL_NEW, fId),
                    RequestHandler.generatePostData(FIELD_NAMES_NEW, topic, body, chksm),
                    RequestHandler.HEADER_AJAX

                    );

            // Let's do it
            return (!"".equals(httpContent));

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

}
