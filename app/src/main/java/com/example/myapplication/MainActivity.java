package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import io.paperdb.Paper;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText titleText, descriptionText;
    private Button addButton, updateButton, deleteButton, chooseImageButton;
    private ListView listView;
    private ImageView productImageView;
    private Uri selectedImageUri;
    private ArrayAdapter<String> adapter;
    private String selectedProductTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        listView = findViewById(R.id.listView);
        productImageView = findViewById(R.id.productImageView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getProductTitles());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProductTitle = adapter.getItem(position);

            Tovar product = Paper.book().read(selectedProductTitle, null);

            if (product != null) {
                titleText.setText(product.getTitle());
                descriptionText.setText(product.getDescription());

                productImageView.setImageURI(Uri.parse(product.getImagePath()));
            }
        });


        chooseImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });


        addButton.setOnClickListener(v -> {
            String title = titleText.getText().toString();
            String description = descriptionText.getText().toString();

            if (!title.isEmpty() && !description.isEmpty() && selectedImageUri != null) {
                Tovar product = new Tovar(title, description, selectedImageUri.toString());
                Paper.book().write(title, product);
                updateProductList();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Заполните все поля и выберите изображение", Toast.LENGTH_SHORT).show();
            }
        });

        updateButton.setOnClickListener(v -> {
            if (selectedProductTitle == null) {
                Toast.makeText(MainActivity.this, "Пожалуйста, выберите товар", Toast.LENGTH_SHORT).show();
                return;
            }

            String newTitle = titleText.getText().toString();
            String newDescription = descriptionText.getText().toString();

            if (!newTitle.isEmpty() && !newDescription.isEmpty() && selectedImageUri != null) {
                Paper.book().delete(selectedProductTitle);
                Tovar updatedProduct = new Tovar(newTitle, newDescription, selectedImageUri.toString());
                Paper.book().write(newTitle, updatedProduct);
                updateProductList();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Заполните все поля и выберите изображение", Toast.LENGTH_SHORT).show();
            }
        });

        // Удаление товара
        deleteButton.setOnClickListener(v -> {
            if (selectedProductTitle == null) {
                Toast.makeText(MainActivity.this, "Пожалуйста, выберите товар", Toast.LENGTH_SHORT).show();
                return;
            }
            Paper.book().delete(selectedProductTitle);
            updateProductList();
            clearInputs();
        });
    }
    
    private void updateProductList() {
        adapter.clear();
        adapter.addAll(getProductTitles());
        adapter.notifyDataSetChanged();
    }


    private List<String> getProductTitles() {
        return new ArrayList<>(Paper.book().getAllKeys());
    }


    private void clearInputs() {
        titleText.setText("");
        descriptionText.setText("");
        selectedImageUri = null;
        productImageView.setImageURI(null);
        selectedProductTitle = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            selectedImageUri = data.getData();
            productImageView.setImageURI(selectedImageUri);
        }
    }
}
