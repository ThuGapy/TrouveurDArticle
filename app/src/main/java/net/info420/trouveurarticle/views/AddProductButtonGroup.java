package net.info420.trouveurarticle.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;

import net.info420.trouveurarticle.R;
import net.info420.trouveurarticle.Utils;
import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.StoreFront;

public class AddProductButtonGroup extends LinearLayout {
    private EditText editText;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private StoreFront store;

    public AddProductButtonGroup(Context context) {
        super(context);
        init(context);
    }

    public AddProductButtonGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddProductButtonGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.add_product_button_group, this, true);
        editText = findViewById(R.id.product_link);
        clearButton = findViewById(R.id.product_clear_button);
        searchButton = findViewById(R.id.product_search_button);

        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearTextBox();
            }
        });

        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(store == null) {
                    return;
                }

                ViewGroup parent = (ViewGroup) getParent();
                EditText productNameText = parent.findViewById(R.id.product_name_edit);

                if(String.valueOf(productNameText.getText()).equals("")) {
                    return;
                }

                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(productNameText.getWindowToken(), 0);

                productNameText.clearFocus();
                productNameText.setFocusableInTouchMode(false);
                productNameText.setFocusable(false);

                String link;

                switch(store) {
                    case Amazon:
                        link = AmazonScrapper.SearchLink(String.valueOf(productNameText.getText()));
                        break;
                    case Newegg:
                        link = NeweggScrapper.SearchLink(String.valueOf(productNameText.getText()));
                        break;
                    case CanadaComputers:
                        link = CanadaComputersScrapper.SearchLink(String.valueOf(productNameText.getText()));
                        break;
                    case MemoryExpress:
                        link = MemoryExpressScrapper.SearchLink(String.valueOf(productNameText.getText()));
                        break;
                    default:
                        link = "";
                        break;
                }

                if(!link.equals("")) {
                    View popupView = LayoutInflater.from(context).inflate(R.layout.easy_search_popup, null);
                    PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    WebView webView = popupView.findViewById(R.id.search_webview);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new EasySearchWebClient());
                    webView.loadUrl(link);

                    Button cancelButton = popupView.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            productNameText.setFocusableInTouchMode(true);
                            productNameText.setFocusable(true);
                            popupWindow.dismiss();
                        }
                    });

                    Button validateButton = popupView.findViewById(R.id.submit_button);
                    validateButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String currentUrl = webView.getUrl();
                            boolean isUrlValid = false;

                            switch(store) {
                                case Amazon:
                                    isUrlValid = currentUrl.contains(AmazonScrapper.StoreName) && !currentUrl.startsWith(AmazonScrapper.SearchLinkStart);
                                    break;
                                case Newegg:
                                    isUrlValid = currentUrl.contains(NeweggScrapper.StoreName) && !currentUrl.startsWith(NeweggScrapper.SearchLinkStart);
                                    break;
                                case CanadaComputers:
                                    isUrlValid = currentUrl.contains(CanadaComputersScrapper.StoreName) && !currentUrl.startsWith(CanadaComputersScrapper.SearchLinkStart);
                                    break;
                                case MemoryExpress:
                                    isUrlValid = currentUrl.contains(MemoryExpressScrapper.StoreName) && !currentUrl.startsWith(MemoryExpressScrapper.SearchLinkStart);
                                    break;
                                default:
                                    break;
                            }

                            if(isUrlValid) {
                                getEditText().setText(currentUrl);
                            } else {
                                Toast.makeText(context, Utils.getResourceString(context, R.string.url_invalide), Toast.LENGTH_LONG).show();
                            }

                            productNameText.setFocusableInTouchMode(true);
                            productNameText.setFocusable(true);
                            popupWindow.dismiss();
                        }
                    });

                    popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                }
            }
        });
    }

    public void SetType(StoreFront storeFront) {
        store = storeFront;
    }

    public EditText getEditText() {
        return editText;
    }

    public ImageButton getClearButton() {
        return clearButton;
    }

    public ImageButton getSearchButton() {
        return searchButton;
    }

    public void ClearTextBox() {
        editText.setText("");
    }

    private class EasySearchWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
