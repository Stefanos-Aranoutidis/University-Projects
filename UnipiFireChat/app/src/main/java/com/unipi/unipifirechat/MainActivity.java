package com.unipi.unipifirechat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    FloatingActionButton fabNewChat;

    UserAdapter userAdapter;
    List<User> mUsers;

    // Λίστα για να κρατάμε τα ID των χρηστών που έχουμε μιλήσει
    List<String> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // 1. Ρύθμιση της Λίστας
        recyclerView = findViewById(R.id.recycler_view_chats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();

        // 2. Ενεργοποίηση του Κουμπιού Αναζήτησης (+)
        fabNewChat = findViewById(R.id.fab_new_chat);
        fabNewChat.setVisibility(View.VISIBLE);

        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });

        // 3. Φόρτωση μόνο των συνομιλιών
        usersList = new ArrayList<>();

        // Ξεκινάμε ελέγχοντας τα μηνύματα
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

                if (fuser != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message chat = snapshot.getValue(Message.class);

                        // Αν είμαι ο αποστολέας, προσθέτω τον παραλήπτη
                        if (chat.getSender().equals(fuser.getUid())) {
                            usersList.add(chat.getReceiver());
                        }
                        // Αν είμαι ο παραλήπτης, προσθέτω τον αποστολέα
                        if (chat.getReceiver().equals(fuser.getUid())) {
                            usersList.add(chat.getSender());
                        }
                    }
                    // Αφού βρήκαμε τα ID, πάμε να βρούμε τα ονόματά τους
                    readChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Συνάρτηση που φορτώνει τα στοιχεία των χρηστών που βρήκαμε
    private void readChats() {
        mUsers = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                // Χρησιμοποιούμε Set για να μην έχουμε διπλότυπα (αν μιλήσαμε 10 φορές με τον ίδιο)
                // Αλλά επειδή διαβάζουμε από το Users node (που είναι unique), αρκεί να ελέγξουμε αν το ID υπάρχει στη λίστα μας.

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    // Τρικ για να αφαιρέσουμε διπλότυπα από τη usersList όσο ψάχνουμε
                    // (Ελέγχουμε αν το ID του user υπάρχει στη λίστα usersList που γεμίσαμε πριν)
                    for (String id : usersList) {
                        if (user.getId().equals(id)) {
                            // Έλεγχος για να μην προσθέσουμε τον ίδιο χρήστη πολλές φορές στη λίστα εμφάνισης
                            boolean exists = false;
                            for (User u : mUsers) {
                                if (u.getId().equals(user.getId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                mUsers.add(user);
                            }
                        }
                    }
                }

                userAdapter = new UserAdapter(MainActivity.this, mUsers);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Παράθυρο Αναζήτησης
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Έναρξη Νέας Συνομιλίας");

        final EditText input = new EditText(this);
        input.setHint("Πληκτρολόγησε ακριβές Username");
        builder.setView(input);

        builder.setPositiveButton("Αναζήτηση", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = input.getText().toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    searchUserAndOpenChat(username);
                }
            }
        });
        builder.setNegativeButton("Ακύρωση", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Λογική Αναζήτησης στη Βάση
    private void searchUserAndOpenChat(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Query: Ψάξε όπου το πεδίο 'username' είναι ίσο με αυτό που έγραψε ο χρήστης
        Query query = usersRef.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // ΒΡΕΘΗΚΕ!
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String userId = data.getKey();
                        String foundUsername = data.child("username").getValue(String.class);

                        // Ανοίγουμε το Chat
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("username", foundUsername);
                        startActivity(intent);
                        break; // Σταματάμε στον πρώτο που βρήκαμε
                    }
                } else {
                    // ΔΕΝ ΒΡΕΘΗΚΕ
                    Toast.makeText(MainActivity.this, "Ο χρήστης δεν βρέθηκε", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Σφάλμα: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Έλεγχος Login & Logout
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}