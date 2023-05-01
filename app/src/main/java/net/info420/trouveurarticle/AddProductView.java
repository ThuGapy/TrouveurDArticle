package net.info420.trouveurarticle;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.info420.trouveurarticle.database.AppSettings;
import net.info420.trouveurarticle.database.DatabaseHelper;
import net.info420.trouveurarticle.scrappers.AmazonScrapper;
import net.info420.trouveurarticle.scrappers.CanadaComputersScrapper;
import net.info420.trouveurarticle.scrappers.MemoryExpressScrapper;
import net.info420.trouveurarticle.scrappers.NeweggScrapper;
import net.info420.trouveurarticle.scrappers.Scrapper;
import net.info420.trouveurarticle.scrappers.StoreFront;
import net.info420.trouveurarticle.views.AddProductButtonGroup;
import net.info420.trouveurarticle.views.OnTriggerEditListener;

import org.w3c.dom.Text;

public class AddProductView extends Fragment {
    private View fragmentView;
    private DatabaseHelper dbHelper;
    private AddProductButtonGroup amazonButtonGroup;
    private AddProductButtonGroup neweggButtonGroup;
    private AddProductButtonGroup canadaComputersButtonGroup;
    private AddProductButtonGroup memoryExpressButtonGroup;
    private AppSettings preferences;
    private int editId;
    private boolean editMode;

    public AddProductView() { }
    public AddProductView(int toEditID) {
        Bundle args = new Bundle();
        args.putInt("edit_id", toEditID);
        args.putBoolean("edit_mode", true);
        this.setArguments(args);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            editId = getArguments().getInt("edit_id");
            editMode = getArguments().getBoolean("edit_mode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_add_product_view, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        preferences = new AppSettings(getContext());

        ImageButton product_clear_button = fragmentView.findViewById(R.id.product_name_clear_button);
        product_clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearProductNameTextBox();
            }
        });

        Button addButton = fragmentView.findViewById(R.id.add_button);
        Button clearButton = fragmentView.findViewById(R.id.clear_button);
        Button modifyButton = fragmentView.findViewById(R.id.save_changes_button);

        TextView addProductTextView = fragmentView.findViewById(R.id.add_product_text_view);

        EditText productName = fragmentView.findViewById(R.id.product_name_edit);

        amazonButtonGroup = fragmentView.findViewById(R.id.amazon_button_group);
        amazonButtonGroup.SetType(StoreFront.Amazon);

        neweggButtonGroup = fragmentView.findViewById(R.id.newegg_button_group);
        neweggButtonGroup.SetType(StoreFront.Newegg);

        canadaComputersButtonGroup = fragmentView.findViewById(R.id.canada_computers_button_group);
        canadaComputersButtonGroup.SetType(StoreFront.CanadaComputers);

        memoryExpressButtonGroup = fragmentView.findViewById(R.id.memory_express_button_group);
        memoryExpressButtonGroup.SetType(StoreFront.MemoryExpress);

        EditText productPrice = fragmentView.findViewById(R.id.price_textbox);

        if(editMode) {
            Cursor cursor = dbHelper.getItem(editId);

            addProductTextView.setText("Modification d'un produit");
            addButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
            modifyButton.setVisibility(View.VISIBLE);

            productName.setText(cursor.getString(cursor.getColumnIndexOrThrow("nomArticle")));
            amazonButtonGroup.getEditText().setText(cursor.getString(cursor.getColumnIndexOrThrow("amazon")));
            neweggButtonGroup.getEditText().setText(cursor.getString(cursor.getColumnIndexOrThrow("newegg")));
            canadaComputersButtonGroup.getEditText().setText(cursor.getString(cursor.getColumnIndexOrThrow("canadacomputers")));
            memoryExpressButtonGroup.getEditText().setText(cursor.getString(cursor.getColumnIndexOrThrow("memoryexpress")));
            productPrice.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("prix"))));

            modifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String productNameString = String.valueOf(productName.getText());
                            String amazonLink = String.valueOf(amazonButtonGroup.getEditText().getText());
                            String neweggLink = String.valueOf(neweggButtonGroup.getEditText().getText());
                            String canadacomputersLink = String.valueOf(canadaComputersButtonGroup.getEditText().getText());
                            String memoryExpressLink = String.valueOf(memoryExpressButtonGroup.getEditText().getText());

                            System.out.println("amazonlink: " + amazonLink);
                            double wantedPrice = 0;
                            try {
                                wantedPrice = Double.parseDouble(String.valueOf(productPrice.getText()));
                            } catch(NumberFormatException ex) {}

                            String fetchedString = null;
                            String productName = productNameString;

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

                            if(!productNameString.equals("") && wantedPrice != 0 && (!amazonLink.equals("") || !neweggLink.equals("") || !canadacomputersLink.equals("") || !memoryExpressLink.equals(""))) {
                                String stringToAdd = productNameString;
                                if(preferences.getAutomaticallyReplaceProductName() && (!fetchedString.equals("") && fetchedString != null)) {
                                    stringToAdd = fetchedString;
                                }

                                dbHelper.updateItem(editId, stringToAdd, amazonLink.equals("") ? null : amazonLink, neweggLink.equals("") ? null : neweggLink, canadacomputersLink.equals("") ? null : canadacomputersLink, memoryExpressLink.equals("") ? null : memoryExpressLink, wantedPrice);
                                ShowToast("Produit modifié");

                                OnTriggerEditListener listener = (OnTriggerEditListener) getActivity();
                                listener.EditDone();
                            } else {
                                ShowToast("Impossible de modifier le produit");
                            }
                        }
                    }).start();
                }
            });
        }

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearInputs();
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String productNameString = String.valueOf(productName.getText());
                        String amazonLink = String.valueOf(amazonButtonGroup.getEditText().getText());
                        String neweggLink = String.valueOf(neweggButtonGroup.getEditText().getText());
                        String canadacomputersLink = String.valueOf(canadaComputersButtonGroup.getEditText().getText());
                        String memoryExpressLink = String.valueOf(memoryExpressButtonGroup.getEditText().getText());
                        double wantedPrice = 0;
                        try {
                            wantedPrice = Double.parseDouble(String.valueOf(productPrice.getText()));
                        } catch(NumberFormatException ex) {}

                        String fetchedString = null;
                        String productName = productNameString;

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

                        if(!productNameString.equals("") && wantedPrice != 0 && (!amazonLink.equals("") || !neweggLink.equals("") || !canadacomputersLink.equals("") || !memoryExpressLink.equals(""))) {
                            String stringToAdd = productNameString;
                            if(preferences.getAutomaticallyReplaceProductName() && (!fetchedString.equals("") && fetchedString != null)) {
                                stringToAdd = fetchedString;
                            }

                            dbHelper.createNewItem(stringToAdd, amazonLink.equals("") ? null : amazonLink, neweggLink.equals("") ? null : neweggLink, canadacomputersLink.equals("") ? null : canadacomputersLink, memoryExpressLink.equals("") ? null : memoryExpressLink, wantedPrice);

                            ShowToast("Produit ajouté");

                            OnTriggerEditListener listener = (OnTriggerEditListener) getActivity();
                            listener.EditDone();
                        } else {
                            ShowToast("Impossible de modifier le produit");
                        }
                    }
                }).start();
            }
        });

        return fragmentView;
    }

    public void ClearInputs() {
        ClearProductNameTextBox();
        amazonButtonGroup.ClearTextBox();
        neweggButtonGroup.ClearTextBox();
        canadaComputersButtonGroup.ClearTextBox();
        memoryExpressButtonGroup.ClearTextBox();
        ClearPriceTextBox();
    }

    public void ClearProductNameTextBox() {
        EditText textbox = fragmentView.findViewById(R.id.product_name_edit);
        textbox.setText("");
    }

    public void ClearPriceTextBox() {
        EditText textbox = fragmentView.findViewById(R.id.price_textbox);
        textbox.setText("");
    }

    public void ShowToast(String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}