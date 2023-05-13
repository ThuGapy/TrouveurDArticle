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

// Classe qui représente un groupe de bouton pour l'ajout d'un produit
public class AddProductButtonGroup extends LinearLayout {
    // Déclaration des données membres
    private EditText editText;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private StoreFront store;

    // Constructeur de la classe à un argument
    public AddProductButtonGroup(Context context) {
        super(context);
        init(context);
    }

    // Constructeur de la classe à deux argument
    public AddProductButtonGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Constructeur de la classe à trois argument
    public AddProductButtonGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // Lorsque la vue est initialiser
    private void init(Context context) {
        // On crée la vue et on obtient certains éléments
        LayoutInflater.from(context).inflate(R.layout.add_product_button_group, this, true);
        editText = findViewById(R.id.product_link);
        clearButton = findViewById(R.id.product_clear_button);
        searchButton = findViewById(R.id.product_search_button);

        // On initialise l'écouteur de click pour le bouton vider
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearTextBox();
            }
        });

        // On initialise l'écouteur de click pour le bouton de recherche
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(store == null) {
                    return;
                }
                // On obtient la boutique et le lien de recherche
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

                // Si le lien de recherche n'est pas vide, on ouvre un navigateur dans l'application
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

                    // Initialisation de l'écouteur de click du bouton valider pour valider la sélection du produit
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


    public void ClearTextBox() {
        editText.setText("");
    }

    // Client web qui s'ouvre directement dans notre application
    private class EasySearchWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
