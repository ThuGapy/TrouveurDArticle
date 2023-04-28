package net.info420.trouveurarticle.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import net.info420.trouveurarticle.R;

public class AddProductButtonGroup extends LinearLayout {
    private EditText editText;
    private ImageButton clearButton;
    private ImageButton searchButton;

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
}
