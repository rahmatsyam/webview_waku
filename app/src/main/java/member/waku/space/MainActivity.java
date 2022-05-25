package member.waku.space;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import member.waku.space.databinding.ActivityMainBinding;
import member.waku.space.ui.webkit.JavaScriptShareInterface;
import member.waku.space.ui.webkit.MyWebClient;
import member.waku.space.util.AppStatus;
import member.waku.space.util.StringUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    protected ActivityMainBinding binding;
    protected WebView webViewKit;

    protected ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private Uri mCapturedImageURI = null;
    private static final int FILE_CHOOSER_RESULT_CODE = 1;
    private String currentUrl = "";
    private boolean closeApp = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        methodRequiresTwoPermissions();

        currentUrl = StringUtils.URL;

        setWebView(currentUrl);

//        binding.swipeRefresh.setEnabled(false);
//        binding.swipeRefresh.setOnRefreshListener(() -> setWebView(currentUrl));

        if (!AppStatus.getInstance().isOnline(this)) {
            openSnackBar();
        }

//        scrollChanged();
    }


    private void setWebView(String mUrl) {
        webViewKit = new WebView(this);
        MyWebClient client = new MyWebClient();
        webViewKit.setWebViewClient(client);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(this, success -> {
                        if (!success) {
                            Toast.makeText(this, "Not success", Toast.LENGTH_LONG).show();
                        } else {
//                            binding.tagLoading.rlLoadAgain.setVisibility(View.INVISIBLE);
                            binding.webViewClient.setWebViewClient(client);
                            binding.webViewClient.loadUrl(mUrl);
                            WebSettings webSettings = binding.webViewClient.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            webSettings.setAppCacheEnabled(true);
                            binding.webViewClient.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                            webSettings.setUseWideViewPort(true);
                            webSettings.setSaveFormData(true);
                            webSettings.setSupportZoom(true);
                            webSettings.setDomStorageEnabled(true);
                            webSettings.setAllowFileAccess(true);
                            webSettings.setAllowUniversalAccessFromFileURLs(true);
                            webSettings.setAllowFileAccessFromFileURLs(true);
                            webSettings.setAllowContentAccess(true);
                            webSettings.setBuiltInZoomControls(true);
                            webSettings.setMediaPlaybackRequiresUserGesture(false);
                            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                            webSettings.setAppCacheEnabled(false);
                            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                            webSettings.setUseWideViewPort(false);
                            webSettings.setLoadWithOverviewMode(true);
                            webSettings.setDatabaseEnabled(true);
                            webSettings.setBuiltInZoomControls(true);
                            webSettings.setDisplayZoomControls(false);

                            binding.webViewClient.addJavascriptInterface(new JavaScriptShareInterface(), "AnroidShareHandler");
                            binding.webViewClient.setWebViewClient(new WebViewClient() {


                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);

                                }

                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    currentUrl = url;
                                    if (url.startsWith(StringUtils.URL_REFERRAL)) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.putExtra(Intent.EXTRA_TEXT, url);
                                        intent.setType("text/plain");
                                        startActivity(Intent.createChooser(intent, "Bagikan Kode Referal"));
                                        return true;
                                    }
                                    Log.i("TAG", "URL-nya :" + url);
                                    return false;
                                }


                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    binding.tagLoadingView.rlLoading.setVisibility(View.GONE);
                                    binding.webViewClient.setVisibility(View.VISIBLE);

//                                    stopRefresh();

                                }


                                @Override
                                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                    super.onReceivedError(view, request, error);
//                                    stopRefresh();
                                    binding.webViewClient.setVisibility(View.INVISIBLE);
                                    binding.webViewClient.post(() -> binding.webViewClient.loadUrl(StringUtils.URL_PAGE_ERROR));
                                    closeApp = true;


                                }


                            });

                            binding.webViewClient.setWebChromeClient(new WebChromeClient() {

                                @Nullable
                                @Override
                                public Bitmap getDefaultVideoPoster() {
                                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                                }

                                @Override
                                public void onPermissionRequest(PermissionRequest request) {

                                    try {
                                        final String[] requestedResources = request.getResources();
                                        for (String r : requestedResources) {
                                            if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                                                request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.i("TAG", "RARA : " + e);
                                    }
                                }

                                @SuppressLint("QueryPermissionsNeeded")
                                @Override
                                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                                    mUploadMessages = filePathCallback;
                                    openImageChooser();
                                    return true;

                                }


                            });

                        }
                    }

            );
        }

    }

    private void openImageChooser() {
        try {

            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FolderName");

            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

            startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @AfterPermissionGranted(12)
    private void methodRequiresTwoPermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i("TAG", "Perizinan disetujui");
        } else {
            EasyPermissions.requestPermissions(this, "Kamera dan galeri", 12, perms);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {

            if (this.mUploadMessage == null && this.mUploadMessages == null) {
                return;
            }

            if (mUploadMessage != null) {
                handleUploadMessages(requestCode, resultCode, data);

            }

            if (mUploadMessages != null) {
                handleUploadMessages(requestCode, resultCode, data);
            }
        }
    }

    private void handleUploadMessages(int requestCode, int resultCode, Intent intent) {
        Uri[] results = null;
        try {
            if (resultCode == RESULT_OK && intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            } else {
                results = new Uri[]{mCapturedImageURI};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUploadMessages.onReceiveValue(results);
        mUploadMessages = null;
    }

    private void openSnackBar() {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Jaringan Tidak Aktif", Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.teal_prime));
        snackbar.show();
    }

    private void stopRefresh() {
//        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.webViewClient.canGoBack()) {
            binding.webViewClient.goBack();
            if (closeApp) {
                askCloseDialog();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        askCloseDialog();

    }

//    private void scrollChanged() {
//        binding.swipeRefresh.getViewTreeObserver().addOnScrollChangedListener(() -> binding.swipeRefresh.setEnabled(binding.webViewClient.getScrollY() == 0));
//    }

    private void askCloseDialog() {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setMessage("Apakah Anda ingin keluar?")
                .setNegativeButton("Tidak", (dialogInterface, i) -> {

                })
                .setPositiveButton("Ya", (dialogInterface, i) -> finishAffinity())
                .show();
    }


}