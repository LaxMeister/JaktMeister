package com.lajw.jaktmeister.ui.Stands;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lajw.jaktmeister.Hunter;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.ChatMessage;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.notifaction.MyCoolSingleton;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandFragment extends Fragment {
    private final List<Integer> dumbStandList = new ArrayList();
    private final List<Hunter> finalCoolHunterList = new ArrayList();
    public Hunter hunter;
    private FirebaseFirestore db;
    public Map<String, Object> cloudMap = new HashMap<>();
    public Map<String, Object> hashMapCoolPass = new HashMap<>();
    private final String TAG1 = "MyTag";
    public List<String> namesForLoadList = new ArrayList<>();
    public View root;
    public String date;
    FirebaseUser firebaseUser;
    String huntingTeamId;
    String stringMonth;
    String dayOfMonthString;
    List<String> listDeltagare = new ArrayList<>();
    int counter = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_gallery, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        huntingTeamId = requireActivity().getIntent().getStringExtra("huntingTeamId");


        root.findViewById(R.id.cool_Choose_Area1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
                if (currentHuntingTeam != null) {
                    db = FirebaseFirestore.getInstance();
                    db.collection("HuntingTeam").document(SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getId()).collection("HuntingAreas")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG1, document.getId());
                                            namesForLoadList.add(document.getId());
                                            Log.d(TAG1, String.valueOf(namesForLoadList));
                                        }
                                        LoadListDialog();
                                    } else {
                                        Log.d(TAG1, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                } else {
                    Toast.makeText(requireActivity(), "Gå med eller skapa ett jaktlag för\natt använda denna funktion", Toast.LENGTH_LONG).show();
                }
            }
        });

        root.findViewById(R.id.cool_Choose_Area2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = FirebaseFirestore.getInstance();
                db.collection("HuntingTeam").document(SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getId()).collection("HuntingAreas")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG1, document.getId());
                                        namesForLoadList.add(document.getId());
                                        Log.d(TAG1, String.valueOf(namesForLoadList));
                                    }
                                    LoadListDialog();
                                } else {
                                    Log.d(TAG1, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });


        root.findViewById(R.id.cool_Choose_Date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.date_dialog, null);
                CalendarView calendarView = view.findViewById(R.id.calendarView);
                builder.setView(view)
                        .setTitle("Välj datum för jakt")
                        .setCancelable(true)
                        .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        stringMonth = String.valueOf(month + 1);

                        dayOfMonthString = String.valueOf(dayOfMonth);
                        if ((month + 1) < 10) {
                            stringMonth = "0" + (month + 1);
                        }
                        if (dayOfMonth < 10) {
                            dayOfMonthString = "0" + dayOfMonth;
                        }
                        date = year + "-" + stringMonth + "-" + dayOfMonthString;
                        ChangeLayout2(date);
                        dialog.dismiss();


                    }
                });
            }

        });


        root.findViewById(R.id.button_randomize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.findViewById(R.id.scrollView1).setVisibility(View.VISIBLE);
                root.findViewById(R.id.hscrll1).setVisibility(View.VISIBLE);
                root.findViewById(R.id.RelativeLayout3).setVisibility(View.VISIBLE);
                root.findViewById(R.id.result_table).setVisibility(View.VISIBLE);
                root.findViewById(R.id.cool_stand_number).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.jagre_namn).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.cool_add_btn).setVisibility(View.INVISIBLE);
                StringBuilder Message = new StringBuilder("Passutdelning " + date + ":\n");
                assignStand();
                TableLayout stk = root.findViewById(R.id.result_table);

                TableRow tbrow0 = new TableRow(root.getContext());
                TextView tv0 = new TextView(root.getContext());
                tbrow0.setMinimumWidth(root.getWidth() - 100);
                tv0.setText("Namn");
                tv0.setTextColor(Color.BLACK);
                tv0.setTypeface(Typeface.DEFAULT_BOLD);
                tv0.setTextSize(30);
                tv0.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tbrow0.addView(tv0);
                TextView tv1 = new TextView(root.getContext());
                tv1.setText("\t\t\t\tPass");
                tv1.setTextColor(Color.BLACK);
                tv1.setTextSize(30);
                tv1.setTypeface(Typeface.DEFAULT_BOLD);
                tv1.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tbrow0.addView(tv1);
                stk.addView(tbrow0);
                for (int i = 0; i < finalCoolHunterList.size(); i++) {
                    TableRow tbrow = new TableRow(root.getContext());
                    TextView t0v = new TextView(root.getContext());
                    t0v.setText(finalCoolHunterList.get(i).hunterName);
                    t0v.setTextColor(Color.BLACK);
                    t0v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    t0v.setGravity(Gravity.CENTER);
                    t0v.setTextSize(20);
                    tbrow.addView(t0v);
                    TextView t2v = new TextView(root.getContext());
                    t2v.setText("\t\t\t\t" + finalCoolHunterList.get(i).getStandNumber());
                    t2v.setTextColor(Color.BLACK);
                    t2v.setGravity(Gravity.CENTER);
                    tbrow.addView(t2v);
                    stk.addView(tbrow);
                    Message.append(t0v.getText()).append(" - " + finalCoolHunterList.get(i).getStandNumber() + " \n");

                }
                Log.i(TAG1, "RESULT: " + Message);
                HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
                String NOTIFICATION_TITLE = SharedPreferencesRepository.getCurrentUser(requireActivity()).getFirstName() + " " + SharedPreferencesRepository.getCurrentUser(requireActivity()).getLastName();
                final String NOTIFICATION_MESSAGE = "Fire ze Misslez!";
                JSONObject notification = new JSONObject();
                JSONObject notificationBody = new JSONObject();
                try {
                    if (currentHuntingTeam != null) {
                        notificationBody.put("title", NOTIFICATION_TITLE);
                        notificationBody.put("message", Message.toString());

                        notification.put("data", notificationBody);
                        notification.put("to", "/topics/" + SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getName().replace(" ", "") + "Stand");

                    } else {

                    }
                } catch (JSONException e) {
                    Log.e(TAG1, "onCreate: " + e.getMessage());
                }
                sendNotification(notification);
                Log.i(TAG1, "Notification:" + notification);
                SendToGroupChat(Message.toString());
                ChangeLayout3();
                root.findViewById(R.id.button_randomize).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.label_deltagare).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.list_Deltagare).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.deltagar_table).setVisibility(View.INVISIBLE);
            }
        });

        TextView cool_InstructionText = root.findViewById(R.id.Cool_instruction_slump);
        cool_InstructionText.setText(getText(R.string.SlumpText2));
        cool_InstructionText.setEms(15);
        cool_InstructionText.setBackgroundResource(android.R.color.transparent);
        cool_InstructionText.setTextColor(getResources().getColor(R.color.black));


        root.findViewById(R.id.cool_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText hunterName = root.findViewById(R.id.jagre_namn);
                EditText standNumber = root.findViewById(R.id.cool_stand_number);
                String coolStandNumber = standNumber.getText().toString();
                String coolHunterName = hunterName.getText().toString();
                TableLayout stk2 = root.findViewById(R.id.deltagar_table);
//                TextView deltagare = root.findViewById(R.id.list_Deltagare);
                if (coolHunterName.matches("")) {
                    Toast.makeText(root.getContext(), "Fyll i ett namn", Toast.LENGTH_SHORT).show();
                } else if (finalCoolHunterList.size() < hashMapCoolPass.size()) {
                    if (coolStandNumber.matches("") && finalCoolHunterList.size() <= hashMapCoolPass.size()) {
                        hunter = new Hunter(coolHunterName, 0);
                        finalCoolHunterList.add(hunter);
                        hunterName.setText("");
                        standNumber.setText("");
                        root.findViewById(R.id.button_randomize).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.label_deltagare).setVisibility(View.VISIBLE);
//                        root.findViewById(R.id.list_Deltagare).setVisibility(View.VISIBLE);
//                        listDeltagare.add(coolHunterName);
//                        StringBuilder finalDeltagareString = new StringBuilder("");
//                        for(int i = 0; i < listDeltagare.size(); i++) {
//                            finalDeltagareString.append(listDeltagare.get(i) + "\n");
//                            deltagare.setText(finalDeltagareString);
//                            Log.i(TAG1,"Deltagare:" + listDeltagare.get(i).toString());
//                        }

                            TableRow tbrow = new TableRow(root.getContext());
                            TextView t0v = new TextView(root.getContext());
                            t0v.setText(finalCoolHunterList.get(counter).hunterName);
                            t0v.setTextColor(getResources().getColor(R.color.cool_gray));
                            t0v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            t0v.setGravity(Gravity.CENTER);
                            t0v.setTextSize(13);
                            tbrow.addView(t0v);
                            TextView t2v = new TextView(root.getContext());
//                            t2v.setText("\t\t\t\t" + finalCoolHunterList.get(counter).getStandNumber());
//                            t2v.setTextColor(getResources().getColor(R.color.cool_gray));
//                            t2v.setGravity(Gravity.CENTER);
                            tbrow.addView(t2v);
                            stk2.addView(tbrow);
                            counter++;

                    } else if (Integer.parseInt((coolStandNumber)) <= hashMapCoolPass.size()) {
                        hunter = new Hunter(coolHunterName, Integer.parseInt(coolStandNumber));
                        finalCoolHunterList.add(hunter);
                        hunterName.setText("");
                        standNumber.setText("");
                        listDeltagare.add(coolHunterName);
                        root.findViewById(R.id.label_deltagare).setVisibility(View.VISIBLE);
//                        root.findViewById(R.id.list_Deltagare).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.button_randomize).setVisibility(View.VISIBLE);
//                        StringBuilder finalDeltagareString = new StringBuilder("");
//                        for(int i = 0; i < listDeltagare.size(); i++){
//                            finalDeltagareString.append(listDeltagare.get(i) + "\n");
//                        }

                            TableRow tbrow = new TableRow(root.getContext());
                            TextView t0v = new TextView(root.getContext());
                            t0v.setText(finalCoolHunterList.get(counter).hunterName);
                            t0v.setTextColor(getResources().getColor(R.color.cool_gray));
                            t0v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            t0v.setGravity(Gravity.CENTER);
                            t0v.setTextSize(13);
                            tbrow.addView(t0v);
                            TextView t2v = new TextView(root.getContext());
                            t2v.setText("\t\t\t\t" + finalCoolHunterList.get(counter).getStandNumber());
                            t2v.setTextColor(getResources().getColor(R.color.cool_gray));
                            t2v.setGravity(Gravity.CENTER);
                            tbrow.addView(t2v);
                            stk2.addView(tbrow);

                    } else {
                        Toast.makeText(root.getContext(), "Siffran är större än \nantal utplacerade pass", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(root.getContext(), "Alla Pass är tildelade.\nTryck \"Fördela pass\" för att gå vidare", Toast.LENGTH_SHORT).show();
                    hunterName.setText("");
                    standNumber.setText("");
                }

            }
        });


        return root;
    }

    public void assignStand() {
        for (int i = 0; i < finalCoolHunterList.size(); i++) {
            dumbStandList.add(i + 1);
        }

        for (int i = 0; i < finalCoolHunterList.size(); i++) {
            for (int x = 0; x < dumbStandList.size(); x++) {
                if (finalCoolHunterList.get(i).getStandNumber() == dumbStandList.get(x)) {
                    dumbStandList.remove(x);
                }
            }
        }

        Collections.shuffle(dumbStandList);
        for (int i = 0; i < finalCoolHunterList.size(); i++) {
            for (int x = 0; x < dumbStandList.size(); x++) {
                if (finalCoolHunterList.get(i).getStandNumber() == 0) {
                    finalCoolHunterList.get(i).setStandNumber(dumbStandList.get(x));
                    dumbStandList.remove(x);
                }
            }
        }
    }

    private void sendNotification(JSONObject notification) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY = "key=AAAAkuBCX_0:APA91bHOKQXvKqnS8l_1JIaDUZrkBdT-9h-yX5L5IFJobvaPeIw83DzihyBplR-T3Ha3-YPUUnLznfO3A9zocjkt4deXifW8NlV2lZfq-4TsBXtrZ649opiJUMrCqvJHX8oU5tV8sUhV";
        final String CONTENT = "application/json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG1, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG1, "onErrorResponse: Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", SERVER_KEY);
                params.put("Content-Type", CONTENT);
                return params;
            }
        };
        MyCoolSingleton.getInstance(requireActivity()).addToRequestQueue(jsonObjectRequest);
    }

    public void LoadListDialog() {
        String[] stringArray = namesForLoadList.toArray(new String[namesForLoadList.size()]);
        String[] selectedString = new String[1];

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireActivity());

        mBuilder.setTitle(R.string.loadlist_dialog_title);
        mBuilder.setSingleChoiceItems(stringArray, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedString[0] = stringArray[which].toString();
            }
        });
        mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG1, "SELECTED ITEM: " + selectedString[0]);
                HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
                db = FirebaseFirestore.getInstance();
                Log.i(TAG1, "Selected String: " + selectedString[0]);
                if (selectedString[0] == null) {
                    Toast.makeText(requireActivity(), "Skapa ett jaktområde för att kunna fördela pass", Toast.LENGTH_LONG).show();
                } else {
                    DocumentReference docRef = db.collection("HuntingTeam").document(currentHuntingTeam.getId()).collection("HuntingAreas").document(selectedString[0]);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            cloudMap = documentSnapshot.getData();
                            Log.i(TAG1, "CLOUDList: " + cloudMap);

                            for (Map.Entry<String, Object> entry : cloudMap.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();

                                if (key.contains("Pass")) {
                                    hashMapCoolPass.put(key, value);
                                }
                            }

                            Log.i(TAG1, "HASHMAPHUNTER: " + hashMapCoolPass);
                            namesForLoadList.removeAll(namesForLoadList);

                        }
                    });


                    ChangeLayout(selectedString[0]);

                    dialog.dismiss();
                }

            }
        });
        mBuilder.setNegativeButton("avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which <= -1) {
                    Toast.makeText(requireActivity(), "Skapa ett jaktområde för att kunna fördela ett pass", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                namesForLoadList.removeAll(namesForLoadList);

            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    public void ChangeLayout(String selectedString) {
        root.findViewById(R.id.cool_Choose_Date).setVisibility(View.VISIBLE);
        Button areaBtn = root.findViewById(R.id.cool_Choose_Area2);
        areaBtn.setVisibility(View.VISIBLE);
        areaBtn.setText(selectedString);
        root.findViewById(R.id.cool_Choose_Area1).setVisibility(View.INVISIBLE);
    }

    public void ChangeLayout2(String date) {
        root.findViewById(R.id.cool_stand_number).setVisibility(View.VISIBLE);
        root.findViewById(R.id.jagre_namn).setVisibility(View.VISIBLE);
        root.findViewById(R.id.cool_add_btn).setVisibility(View.VISIBLE);
        Button dateBtn = root.findViewById(R.id.cool_Choose_Date);
        dateBtn.setText(date);

    }

    public void ChangeLayout3() {
        root.findViewById(R.id.cool_stand_number).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.jagre_namn).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.cool_add_btn).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.cool_Choose_Date).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.cool_Choose_Area2).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.Cool_instruction_slump).setVisibility(View.INVISIBLE);


    }

    public void SendToGroupChat(String message) {
        ChatMessage chatMessage = new ChatMessage(firebaseUser.getUid(), huntingTeamId, message);
        String huntingID = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getId();
        Long date = System.currentTimeMillis();
        String dateString = date.toString();
        db = FirebaseFirestore.getInstance();
        db.collection("HuntingTeam").document(huntingID).collection("ChatRoom").document(dateString)
                .set(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MessageActivity", "Added object:" + chatMessage.toString());
                })
                .addOnFailureListener(e -> {
                    Log.w("MessageActivity", "Error adding message", e);
                });
    }

}