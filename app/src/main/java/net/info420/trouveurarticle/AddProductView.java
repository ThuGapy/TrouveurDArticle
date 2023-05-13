package net.info420.trouveurarticle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.CursorWrapper;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.Scrapper;
import net.info420.trouveurarticle.scrappers.StoreFront;
import net.info420.trouveurarticle.views.AddProductButtonGroup;
import net.info420.trouveurarticle.views.OnProductInteractionListener;

// Classe qui gère le fragment d'ajout de produit
public class AddProductView extends Fragment {
    // Déclaration des données membres
    private View fragmentView;
    private DatabaseHelper dbHelper;
    private EditText productName;
    private AddProductButtonGroup amazonButtonGroup;
    private AddProductButtonGroup neweggButtonGroup;
    private AddProductButtonGroup canadaComputersButtonGroup;
    private AddProductButtonGroup memoryExpressButtonGroup;
    private EditText productPrice;
    private AppSettings preferences;
    private int editId;
    private boolean editMode;

    // Constructeur sans argument
    public AddProductView() { }
    // Constructeur pour modification d'un produit
    public AddProductView(int toEditID) {
        Bundle args = new Bundle();
        args.putInt("edit_id", toEditID);
        args.putBoolean("edit_mode", true);
        this.setArguments(args);
    }

    // Lorsqu'on crée le fragment, on valide si on est en mode édition ou non
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            editId = getArguments().getInt("edit_id");
            editMode = getArguments().getBoolean("edit_mode");
        }
    }

    // Lorsque la vu est créé
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_add_product_view, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        preferences = new AppSettings(getContext());

        // Initialisation du bouton pour vider le contenu du nom de produit
        ImageButton product_clear_button = fragmentView.findViewById(R.id.product_name_clear_button);
        product_clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearProductNameTextBox();
            }
        });

        // Obtention des vues du fragment
        Button addButton = fragmentView.findViewById(R.id.add_button);
        Button clearButton = fragmentView.findViewById(R.id.clear_button);
        Button modifyButton = fragmentView.findViewById(R.id.save_changes_button);

        TextView addProductTextView = fragmentView.findViewById(R.id.add_product_text_view);

        productName = fragmentView.findViewById(R.id.product_name_edit);

        amazonButtonGroup = fragmentView.findViewById(R.id.amazon_button_group);
        amazonButtonGroup.SetType(StoreFront.Amazon);

        neweggButtonGroup = fragmentView.findViewById(R.id.newegg_button_group);
        neweggButtonGroup.SetType(StoreFront.Newegg);

        canadaComputersButtonGroup = fragmentView.findViewById(R.id.canada_computers_button_group);
        canadaComputersButtonGroup.SetType(StoreFront.CanadaComputers);

        memoryExpressButtonGroup = fragmentView.findViewById(R.id.memory_express_button_group);
        memoryExpressButtonGroup.SetType(StoreFront.MemoryExpress);

        productPrice = fragmentView.findViewById(R.id.price_textbox);

        // Initialisation du bouton pour vider le contenu du prix
        ImageButton clearPriceButton = fragmentView.findViewById(R.id.price_clear_button);
        clearPriceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearPriceTextBox();
            }
        });

        // Si nous comme en mode édition
        if(editMode) {
            // On obtient le le produit qu'on veut modifier
            CursorWrapper wrapper = dbHelper.getItem(editId);

            // On modifie le fragment pour le mode édition
            addProductTextView.setText(Utils.getResourceString(getContext(), R.string.modification_un_produit));
            addButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
            modifyButton.setVisibility(View.VISIBLE);

            // On met à jour les champs
            productName.setText(wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("nomArticle")));
            amazonButtonGroup.getEditText().setText(wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("amazon")));
            neweggButtonGroup.getEditText().setText(wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("newegg")));
            canadaComputersButtonGroup.getEditText().setText(wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("canadacomputers")));
            memoryExpressButtonGroup.getEditText().setText(wrapper.cursor.getString(wrapper.cursor.getColumnIndexOrThrow("memoryexpress")));
            productPrice.setText(String.valueOf(wrapper.cursor.getDouble(wrapper.cursor.getColumnIndexOrThrow("prix"))));

            // Initialisation du bouton modifier
            modifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // On obtient les champs
                            String productNameString = String.valueOf(productName.getText());
                            String amazonLink = String.valueOf(amazonButtonGroup.getEditText().getText());
                            String neweggLink = String.valueOf(neweggButtonGroup.getEditText().getText());
                            String canadacomputersLink = String.valueOf(canadaComputersButtonGroup.getEditText().getText());
                            String memoryExpressLink = String.valueOf(memoryExpressButtonGroup.getEditText().getText());

                            String fetchedString = GetProductName();

                            // On valide ce qui est entré
                            double wantedPrice = 0;
                            try {
                                wantedPrice = Double.parseDouble(String.valueOf(productPrice.getText()));
                            } catch(NumberFormatException ex) {}

                            if(!productNameString.equals("") && wantedPrice != 0 && (!amazonLink.equals("") || !neweggLink.equals("") || !canadacomputersLink.equals("") || !memoryExpressLink.equals(""))) {
                                String stringToAdd = productNameString;
                                if(preferences.getAutomaticallyReplaceProductName()) {
                                    if(!TextUtils.isEmpty(fetchedString)) {
                                        stringToAdd = fetchedString;
                                    } else {
                                        ShowToast(Utils.getResourceString(getContext(), R.string.impossible_obtenir_le_nom_automatiquement));
                                    }
                                }

                                // On modifie l'article
                                dbHelper.updateItem(editId, stringToAdd, amazonLink.equals("") ? null : amazonLink, neweggLink.equals("") ? null : neweggLink, canadacomputersLink.equals("") ? null : canadacomputersLink, memoryExpressLink.equals("") ? null : memoryExpressLink, wantedPrice);
                                ShowToast(Utils.getResourceString(getContext(), R.string.produit_modifie));

                                // On retourne au menu principal
                                OnProductInteractionListener listener = (OnProductInteractionListener) getActivity();
                                listener.EditDone();
                            } else {
                                ShowToast(Utils.getResourceString(getContext(), R.string.impossible_de_modifier_le_produit));
                            }
                        }
                    }).start();
                }
            });

            wrapper.Close();
        }

        // Initialisation du bouton pour vider les champs
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearInputs();
            }
        });


        // Initialisation du bouton pour ajouter un article
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Obtention des champs
                        String productNameString = String.valueOf(productName.getText());
                        String amazonLink = String.valueOf(amazonButtonGroup.getEditText().getText());
                        String neweggLink = String.valueOf(neweggButtonGroup.getEditText().getText());
                        String canadacomputersLink = String.valueOf(canadaComputersButtonGroup.getEditText().getText());
                        String memoryExpressLink = String.valueOf(memoryExpressButtonGroup.getEditText().getText());

                        String fetchedString = GetProductName();

                        // Validation des informations entré
                        double wantedPrice = 0;
                        try {
                            wantedPrice = Double.parseDouble(String.valueOf(productPrice.getText()));
                        } catch(NumberFormatException ex) {}

                        if(!productNameString.equals("") && wantedPrice != 0 && !productPrice.getText().equals("") && (!amazonLink.equals("") || !neweggLink.equals("") || !canadacomputersLink.equals("") || !memoryExpressLink.equals(""))) {
                            String stringToAdd = productNameString;
                            if(preferences.getAutomaticallyReplaceProductName()) {
                                if(!TextUtils.isEmpty(fetchedString)) {
                                    stringToAdd = fetchedString;
                                } else {
                                    ShowToast(Utils.getResourceString(getContext(), R.string.impossible_obtenir_le_nom_automatiquement));
                                }
                            }

                            // Ajout du produit dans la BD
                            dbHelper.createNewItem(stringToAdd, amazonLink.equals("") ? null : amazonLink, neweggLink.equals("") ? null : neweggLink, canadacomputersLink.equals("") ? null : canadacomputersLink, memoryExpressLink.equals("") ? null : memoryExpressLink, wantedPrice);

                            ShowToast(Utils.getResourceString(getContext(), R.string.produit_ajoute));

                            OnProductInteractionListener listener = (OnProductInteractionListener) getActivity();
                            listener.EditDone();
                        } else {
                            ShowToast(Utils.getResourceString(getContext(), R.string.impossible_ajouter_le_produit));
                        }
                    }
                }).start();
            }
        });

        return fragmentView;
    }

    // Méthode qui obtient le nom du produit
    private String GetProductName() {
        String amazonLink = String.valueOf(amazonButtonGroup.getEditText().getText());
        String neweggLink = String.valueOf(neweggButtonGroup.getEditText().getText());
        String canadacomputersLink = String.valueOf(canadaComputersButtonGroup.getEditText().getText());
        String memoryExpressLink = String.valueOf(memoryExpressButtonGroup.getEditText().getText());

        String fetchedString = null;

        if(preferences.getAutomaticallyReplaceProductName()) {
            Scrapper productNameScrapper;
            if(!amazonLink.equals("")) {
                productNameScrapper = new AmazonScrapper();
                fetchedString = productNameScrapper.FetchProductName(amazonLink);
            } else if(!neweggLink.equals("")) {
                productNameScrapper = new NeweggScrapper();
                fetchedString = productNameScrapper.FetchProductName(neweggLink);
            } else if(!canadacomputersLink.equals("")) {
                productNameScrapper = new CanadaComputersScrapper();
                fetchedString = productNameScrapper.FetchProductName(canadacomputersLink);
            } else if(!memoryExpressLink.equals("")) {
                productNameScrapper = new MemoryExpressScrapper();
                fetchedString = productNameScrapper.FetchProductName(memoryExpressLink);
            }
        }

        return fetchedString;
    }

    // Méthode qui vide tous les champs
    public void ClearInputs() {
        ClearProductNameTextBox();
        amazonButtonGroup.ClearTextBox();
        neweggButtonGroup.ClearTextBox();
        canadaComputersButtonGroup.ClearTextBox();
        memoryExpressButtonGroup.ClearTextBox();
        ClearPriceTextBox();
    }

    // Méthode qui vide le contenu de la textbox de nom du produit
    public void ClearProductNameTextBox() {
        EditText textbox = fragmentView.findViewById(R.id.product_name_edit);
        textbox.setText("");
    }

    // Méthode qui vide le contenu de la textbox de prix
    public void ClearPriceTextBox() {
        EditText textbox = fragmentView.findViewById(R.id.price_textbox);
        textbox.setText("");
    }

    // Méthode qui sert à montrer un toast
    public void ShowToast(String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}