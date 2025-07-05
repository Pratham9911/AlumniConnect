package com.pratham.alumniconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView recyclerView;
    private List<UserModel> userList = new ArrayList<>();
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.search_input);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new SearchAdapter(userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Auto show keyboard
        searchInput.requestFocus();

        searchInput.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) searchUsers(query);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        String lowerQuery = query.toLowerCase();

        FirebaseFirestore.getInstance().collection("users")
                .limit(50) // Fetch first 50 users to filter locally
                .get()
                .addOnSuccessListener(snapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null && user.getName() != null) {
                            String name = user.getName().toLowerCase();
                            if (name.contains(lowerQuery)) {
                                userList.add(user);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
