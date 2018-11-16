package org.helpapaw.helpapaw.data.models.backendless.repositories;

import android.content.Context;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.backendless.FINComment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.hamcrest.Matchers.greaterThan;
import static org.helpapaw.helpapaw.data.models.Comment.COMMENT_TYPE_USER_COMMENT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BackendlessCommentRepositoryTest {

    public static final String  BACKENDLESS_APP_ID          = "BDCD56B9-351A-E067-FFA4-9EA9CF2F4000";
    public static final String  BACKENDLESS_REST_API_KEY    = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
    private static final String BACKENDLESS_ANDROID_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00";
    private static final String DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss";
    private static final String NAME_FIELD = "name";
    private static final String CREATED_FIELD = "created";

    CountDownLatch countDownLatch = new CountDownLatch(1);
    List<String> responseComments = new ArrayList<>();
    Comment savedComment;

    @Before
    public void setup(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("TEST",Context.MODE_PRIVATE);
        Backendless.initApp(context, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY);
    }

    @Test
    public void saveComments(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        String currentDate = dateFormat.format(new Date());



        FINComment backendlessComment = new FINComment("test", currentDate, "test1", COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser());

        final IDataStore<FINComment> commentsStore = Backendless.Data.of(FINComment.class);
        List<FINComment> comments = new ArrayList<>();
        comments.add(backendlessComment);
        commentsStore.create(comments, new AsyncCallback<List<String>>() {
            @Override
            public void handleResponse(List<String> response) {
                responseComments = response;
                countDownLatch.countDown();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(responseComments.size(),greaterThan(0));

        countDownLatch = new CountDownLatch(1);
        backendlessComment = new FINComment(responseComments.get(0),"test", currentDate, "testing2", COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser());
        commentsStore.save(backendlessComment, new AsyncCallback<FINComment>() {
            public void handleResponse(final FINComment newComment) {
                ArrayList<BackendlessUser> userList = new ArrayList<>();
                userList.add(new BackendlessUser());
                commentsStore.setRelation( newComment, "author", userList,
                        new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse( Integer response ) {
                                newComment.setAuthor(Backendless.UserService.CurrentUser());
                                String authorName = null;
                                if (newComment.getAuthor() != null) {
                                    authorName = getToStringOrNull(newComment.getAuthor().getProperty(NAME_FIELD));
                                }

                                Date dateCreated = null;
                                try {
                                    String dateCreatedString = newComment.getCreated();
                                    DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                                    dateCreated = dateFormat.parse(dateCreatedString);
                                }
                                catch (Exception ex) {
                                    Log.d(BackendlessCommentRepository.class.getName(), "Failed to parse comment date.");
                                }

                                savedComment = new Comment(newComment.getObjectId(), authorName, dateCreated, newComment.getComment(), COMMENT_TYPE_USER_COMMENT);
                                countDownLatch.countDown();
                            }

                            @Override
                            public void handleFault( BackendlessFault fault ) {
                                countDownLatch.countDown();
                            }
                        } );
            }

            public void handleFault(BackendlessFault fault) {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(savedComment);
    }

    private String getToStringOrNull(Object object) {
        if (object != null) {
            return object.toString();
        } else {
            return null;
        }
    }
}
