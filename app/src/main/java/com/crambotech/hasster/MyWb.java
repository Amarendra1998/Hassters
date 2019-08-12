package com.crambotech.hasster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
//import android.widget.Toast;
//import android.widget.Toolbar;


public class MyWb extends Activity
{
    WebView web;
    private AlertDialog.Builder Notify;
    URL c = new URL();
    private SwipeRefreshLayout swipeContainer;
    private Uri mCapturedImageURI = null;
   ProgressBar progressBar;
  // final Activity activity = this;
    //public Uri imageUri;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private final static int FILECHOOSER_RESULTCODE=2888;
    private final static int KITKAT_RESULTCODE = 1765;

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if(requestCode==FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = (WebView) findViewById(R.id.activity_main_webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(c.url);
        web.setWebViewClient(new myWebClient());
        web.getSettings().setLoadWithOverviewMode(true);

        //Other webview settings
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.setScrollbarFadingEnabled(false);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setPluginState(WebSettings.PluginState.ON);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setSupportZoom(true);

        //Load url in webview


        // Define Webview manage classes
       // startWebView();
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                web.reload();
                ( new Handler()).postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        swipeContainer.setRefreshing(false);

                    }
                }, 6000);


            }
        });

        if(CheckNetwork.isInternetAvailable(MyWb.this))
        {
            Toast.makeText(getBaseContext(), "Congrats",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            refresh();
        }

       // web = new WebView(this);

        web.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                web.setVisibility(View.INVISIBLE);
            }
            //If you will not use this method url links are open in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // Check if Url contains ExternalLinks string in url
                // then open url in new browser
                // else all webview links will open in webview browser
                if(url.contains("google")){

                    // Could be cleverer and use a regex
                    //Open links in new browser
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                    // Here we can open new activity

                    return true;

                } else {

                    // Stay within this webview and load url
                    view.loadUrl(url);
                    return true;
                }

            }

            //Show loader on url load
            public void onLoadResource (WebView view, String url) {

                // if url contains string androidexample
                // Then show progress  Dialog
                if (progressDialog == null && url.contains("androidexample")
                ) {

                    // in standard case YourActivity.this
                    progressDialog = new ProgressDialog(MyWb.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            }

            // Called when all page resources loaded
            public void onPageFinished(WebView view, String url) {

                try{
                    // Close progressDialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient()
        {
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){

                // Update message
                mUploadMessage = uploadMsg;

                try{

                    // Create AndroidExampleFolder at sdcard

                    File imageStorageDir = new File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES)
                            , "AndroidExampleFolder");

                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }

                    // Create camera captured image file path and name
                    File file = new File(
                            imageStorageDir + File.separator + "IMG_"
                                    + String.valueOf(System.currentTimeMillis())
                                    + ".jpg");

                    mCapturedImageURI = Uri.fromFile(file);

                    // Camera capture image intent
                    final Intent captureIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");

                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                    // Set camera intent to file chooser
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                            , new Parcelable[] { captureIntent });

                    // On select image call onActivityResult method of activity
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                }
                catch(Exception e){
                    Toast.makeText(getBaseContext(), "Exception:"+e,
                            Toast.LENGTH_LONG).show();
                }

            }
            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {

                openFileChooser(uploadMsg, acceptType);
            }
            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessages != null) {
                    mUploadMessages.onReceiveValue(null);
                    mUploadMessages = null;
                }

                mUploadMessages = (ValueCallback<Uri[]>) filePathCallback;
                Intent intent = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                } catch (ActivityNotFoundException e) {
                    mUploadMessages = null;
                    Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
            // The webPage has 2 filechoosers and will send a
            // console message informing what action to perform,
            // taking a photo or updating the file

            public boolean onConsoleMessage(ConsoleMessage cm) {

                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }

            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d("androidruntime", "Show console messages, Used for debugging: " + message);

            }
            public void showPicker(ValueCallback<Uri> uploadMsg ){
                mUploadMessage = (ValueCallback<Uri>) uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("​*/*​");
                String[] mimetypes = {"image/*", "video/*", "audio/*"};
                i.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

                MyWb.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MyWb.FILECHOOSER_RESULTCODE);

            }

        });

       //setContentView(web);

    }
    // Return here when file selected from camera or from SDcard

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (mUploadMessages == null) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mUploadMessages.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                }
                mUploadMessages = null;
            } else if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage)
                    return;
                Uri result = intent == null || resultCode != MyWb.RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }

    // Return here when file selected from camera or from SDcard
    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            swipeContainer.setRefreshing(false);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    public void  refresh()
    {   createDialog();
        Notify.show();
    }

    public void createDialog()
    {
        Notify = new AlertDialog.Builder(this);
        Notify.setTitle("No Internet Connection");
        Notify.setPositiveButton("Refresh",
                new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, int which) {
                        if (isNetworkAvailable()) {
                            web.setVisibility(View.VISIBLE);
                            web.reload();
                            Thread timer = new Thread() {
                                public void run() {
                                    try {
                                        sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            };
                            timer.start();

                        } else {
                            refresh();
                        }
                    }

                });
    }

    @Override
public boolean onKeyDown(int keyCode, KeyEvent event)
{
    if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
        web.goBack();
        return true;
    }
    return super.onKeyDown(keyCode, event);
  }

}