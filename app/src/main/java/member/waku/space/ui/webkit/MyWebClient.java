package member.waku.space.ui.webkit;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.webkit.SafeBrowsingResponseCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

public class MyWebClient extends WebViewClientCompat {

    @Override
    public void onSafeBrowsingHit(@NonNull WebView view, @NonNull WebResourceRequest request, int threatType, @NonNull SafeBrowsingResponseCompat callback) {
        super.onSafeBrowsingHit(view, request, threatType, callback);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_RESPONSE_BACK_TO_SAFETY)) {
            callback.backToSafety(true);
            Toast.makeText(view.getContext(), "Unsafe web page blocked.", Toast.LENGTH_LONG).show();
        }
    }
}
