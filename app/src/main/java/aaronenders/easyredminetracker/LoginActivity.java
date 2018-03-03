package aaronenders.easyredminetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mCompanyView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("apiKey", "");
        editor.putString("userId", "");
        editor.apply();
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mCompanyView = (EditText) findViewById(R.id.company);
        mPasswordView = (EditText) findViewById(R.id.password);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCompanyName = getResources().getString(R.string.pref_default_companyname_text);
        String companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultUsername = "";
        String username = preferences.getString("username", defaultUsername);

        mCompanyView.setText(companyName);
        mUsernameView.setText(username);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    showProgress(true);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        mUsernameView.setError(null);
        mCompanyView.setError(null);
        mPasswordView.setError(null);
        String username = mUsernameView.getText().toString();
        String company = mCompanyView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(company)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mCompanyView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, company);
            saveUserData(username, password, company);
            mAuthTask.execute((Void) null);
        }
    }
    private void saveUserData(String username, String password, String company){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("companyName", company);
        editor.putString("username", username);
        editor.apply();
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only username addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary username addresses first. Note that there won't be
                // a primary username address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mCompany;
        public String apiKey;
        public String username;
        public String userId;




        UserLoginTask(String username, String password, String company) {
            mUsername = username;
            mPassword = password;
            mCompany = company;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
                return login(mCompany, mUsername, mPassword);
        }

        public  boolean login(final String mCompany, final String mUsername, final String mPassword) {
            URL url = null;
            try {
                url = new URL("https://" + mCompany + ".easyredmine.com/users.xml?set_filter=1fc&login=" + mUsername);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String xmlResponse = getXMLFromUrl("https://" + mCompany + ".easyredmine.com/users.xml?set_filter=1fc&login=" + mUsername, mUsername, mPassword);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = db.parse(new InputSource(new StringReader(xmlResponse)));
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc == null){
                return false;
            }
            doc.getDocumentElement().normalize();
            NodeList usersNodeList = doc.getElementsByTagName("user");
            Element user = (Element) usersNodeList.item(0);
            apiKey = user.getElementsByTagName("api_key").item(0).getTextContent();
            if (apiKey != null && apiKey != ""){
                userId = user.getElementsByTagName("id").item(0).getTextContent();
                return true;
            }else{
                return false;
            }

        }

        public String getXMLFromUrl(String url, String username, String password) {
            BufferedReader br = null;
            try {
                String authorization = "";
                HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                conn.setRequestProperty("Accept", "*/*");
                String userpass = username+":"+password;
                byte[] data = userpass.getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                String basicAuth = "Basic " + base64;
                conn.setRequestProperty ("Authorization", basicAuth);
                conn.connect();
                int status = conn.getResponseCode();
                InputStream is;
                try {
                    is = conn.getInputStream();
                } catch(FileNotFoundException exception){
                    is = conn.getErrorStream();
                }
                BufferedReader theReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String reply;
                while ((reply = theReader.readLine()) != null) {
                    response.append(reply);
                }
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPreExecute();
            mAuthTask = null;
            showProgress(false);
            if (success) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("apiKey", apiKey);
                editor.putString("userId", userId);
                editor.apply();
                Intent i = new Intent(LoginActivity.this, aaronenders.easyredminetracker.List.class);
                startActivity(i);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

