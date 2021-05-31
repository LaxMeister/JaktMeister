package com.lajw.jaktmeister.ui.homeMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.ChatMessage;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.huntingteam.activity.HuntingTeamActivity;
import com.lajw.jaktmeister.notifaction.MyCoolSingleton;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleDragListener;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.os.Looper.getMainLooper;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class HomeFragment extends Fragment implements OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
        OnCameraTrackingChangedListener, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {

    private PermissionsManager permissionsManager;
    private MapView mapView;
    private LocationComponent locationComponent;
    private boolean isInTrackingMode;
    private final String TAG1 = "MyTag";
    public MapboxMap mapboxMap;
    public LocationEngine locationEngine;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 2000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private List<LatLng> locationPoints = new ArrayList<>();
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID_ACTIVE = "ICON_ID_ACTIVE";
    private static final String ICON_ID_INACTIVE = "ICON_ID_INACTIVE";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_INFO = "info";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String ICON_PROPERTY = "ICON_PROPERTY";
    private static String NOTIFICATION_TITLE;
    private static String NOTIFICATION_MESSAGE;
    public static final int GET_FROM_GALLERY = 40;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private Feature pointFeature;
    public String globalPass = "";
    public Uri selectedImage;
    public String previewImage;
    public Map<String, Object> bitHashMapList = new HashMap<>();
    public String imageString;
    private final List<Feature> featurePointList = new ArrayList();
    private boolean drawModeOn = false;
    private boolean placePassModeOn = false;
    private boolean movePassModeOn = false;
    private boolean selectedPassMoveModeOn = false;
    public List<android.graphics.Point> huntingAreaList = new ArrayList<>();
    public Map<String, Object> hashMapHunter = new HashMap<>();
    public Map<String, Object> hashMapCoolPass = new HashMap<>();
    public Map<String, Object> hashMaphunterLoaded = new HashMap<>();
    public Map<String, Object> hashMapHunterDataFromCloud = new HashMap<>();
    public Map<String, Object> hashMapActive = new HashMap<>();
    public Map<String, Object> hashMapInfo = new HashMap<>();
    public double finalPointedLat = 0;
    public double finalPointedLon = 0;
    public boolean dialogBool = false;
    public String huntingAreaName;
    public List<String> namesForLoadList = new ArrayList<>();
    public List<String> teamNamesForLoadList = new ArrayList<>();
    private FirebaseFirestore db;
    private final LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    public LineManager lineManager;
    public FillManager fillManager;
    public CircleManager circleManager;
    public List<LatLng> circlePositionList = new ArrayList<>();
    public List<List<LatLng>> fillList = new ArrayList<>();
    public List<LatLng> lastPositionList = Arrays.asList(new LatLng[2]);
    public int indexNum;
    public SymbolLayer symbolLayer;
    public LatLng newLatLng;
    public Style savedStyle;
    public LatLng lastLatLng;
    public List<String> passInfoList = new ArrayList();
    public List<Integer> passIntergerList = new ArrayList<>();
    public List<Boolean> passBooleanList = new ArrayList<>();
    public List<Boolean> tempBooleanList = new ArrayList<>();
    public List<String> loadedInfoList = new ArrayList<>();
    public PointF GlobalPointF;
    public boolean coolSwitchState;
    public String tempText;
    public HuntingTeam huntingTeam;
    public List<HuntingTeam> huntingTeams = new ArrayList<>();
    public int tempCount = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity().getApplicationContext(), getString(R.string.access_token));
        super.onCreate(savedInstanceState);
        tempCount++;

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        FloatingActionButton drawFab = (FloatingActionButton) root.findViewById(R.id.draw);
        FloatingActionButton clear = root.findViewById(R.id.clear_button);
        FloatingActionButton coolSave = (FloatingActionButton) root.findViewById(R.id.cool_save);


        drawFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawModeOn = true;
                if (circlePositionList.size() >= 2 || locationPoints.size() >= 1) {
                    redraw(false, 5f);
                }

                Log.i(TAG1, "MapTargetPointslist: " + huntingAreaList.size());

                drawFab.setVisibility(View.INVISIBLE);
                root.findViewById(R.id.undo_areapoint).setVisibility(View.VISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.back_to_camera_tracking_mode).setVisibility(View.INVISIBLE);
                clear.setVisibility(View.VISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.VISIBLE);
                coolSave.setVisibility(View.VISIBLE);
                root.findViewById(R.id.show_rootBtns).setVisibility(View.VISIBLE);
                root.findViewById(R.id.fill_huntingarea).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.ladda_pass).setVisibility(View.INVISIBLE);
//                Toast.makeText(requireActivity(), SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getName(), Toast.LENGTH_LONG).show();
                if (circlePositionList.size() >= 2) {
                    drawModeOn = false;
                    redraw(true, 15f);
                    root.findViewById(R.id.show_rootBtns).setVisibility(View.INVISIBLE);
                    root.findViewById(R.id.undo_areapoint).setVisibility(View.INVISIBLE);
                    root.findViewById(R.id.cool_save).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.clear_button_edit_mode).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.show_rootBtns_copy).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.cool_edit_punkt).setVisibility(View.VISIBLE);

                    if (locationPoints.size() >= 1) {
                        movePassModeOn = true;
                        placePassModeOn = false;

                    }


                    circleManager.addDragListener(new OnCircleDragListener() {
                        @Override
                        public void onAnnotationDragStarted(Circle annotation) {
                            Log.i(TAG1, "circle: " + annotation.getLatLng());
                            for (int i = 0; i < circlePositionList.size(); i++) {
                                if (circlePositionList.get(i).equals(annotation.getLatLng())) {
                                    indexNum = circlePositionList.indexOf(annotation.getLatLng());
                                }
                            }
                            Log.i(TAG1, "indexnum: " + indexNum);
                        }

                        @Override
                        public void onAnnotationDrag(Circle annotation) {

                        }

                        @Override
                        public void onAnnotationDragFinished(Circle annotation) {
                            newLatLng = annotation.getLatLng();
                            updateList();
//                            redraw(true,15f);
                            Log.i(TAG1, "circle: " + annotation.getLatLng());
                            root.findViewById(R.id.update_area).setVisibility(View.VISIBLE);
                            root.findViewById(R.id.update_area).setAlpha(0);
                            root.findViewById(R.id.update_area).animate().alpha(1).setDuration(1000).start();
                        }

                    });


                }

                if (drawModeOn) {
                    Toast.makeText(requireActivity(), R.string.trace_instruction, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireActivity(), "Dra i punkterna för att ändra området", Toast.LENGTH_LONG).show();
                }
            }
        });


        root.findViewById(R.id.place_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placePassModeOn = true;
                drawModeOn = false;
                if (locationPoints.size() >= 1) {
                    movePassModeOn = true;
                    placePassModeOn = false;

                }
                drawFab.setVisibility(View.INVISIBLE);
                root.findViewById(R.id.back_to_camera_tracking_mode).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.show_rootBtns2).setVisibility(View.VISIBLE);
                root.findViewById(R.id.undo_pass).setVisibility(View.VISIBLE);
                root.findViewById(R.id.show_rootBtns).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.undo_areapoint).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.clear_button).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.cool_save).setVisibility(View.INVISIBLE);
                Toast.makeText(requireActivity(),
                        getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
            }
        });

        root.findViewById(R.id.show_rootBtns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circlePositionList.size() >= 2 || locationPoints.size() >= 1) {
                    redraw(false, 0.5f);
                }
                drawModeOn = false;
                placePassModeOn = false;
                drawFab.setVisibility(View.VISIBLE);
                root.findViewById(R.id.undo_areapoint).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.back_to_camera_tracking_mode).setVisibility(View.VISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.VISIBLE);
                clear.setVisibility(View.INVISIBLE);
                coolSave.setVisibility(View.INVISIBLE);
                root.findViewById(R.id.show_rootBtns).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.fill_huntingarea).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.ladda_pass).setVisibility(View.VISIBLE);
            }
        });

        root.findViewById(R.id.show_rootBtns_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawModeOn = false;
                placePassModeOn = false;
                movePassModeOn = false;
                selectedPassMoveModeOn = false;
                redraw(false, 0.5f);
                root.findViewById(R.id.undo_areapoint).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.show_rootBtns).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.cool_save).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.clear_button_edit_mode).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.clear_button).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.show_rootBtns_copy).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.cool_edit_punkt).setVisibility(View.INVISIBLE);

                drawFab.setVisibility(View.VISIBLE);
                root.findViewById(R.id.back_to_camera_tracking_mode).setVisibility(View.VISIBLE);
                root.findViewById(R.id.ladda_pass).setVisibility(View.VISIBLE);
            }
        });

        root.findViewById(R.id.show_rootBtns2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circlePositionList.size() <= 1) {
                    drawModeOn = false;
                }
                drawModeOn = false;
                placePassModeOn = false;
                movePassModeOn = false;
                selectedPassMoveModeOn = false;
                root.findViewById(R.id.place_pass).setVisibility(View.VISIBLE);
                root.findViewById(R.id.undo_areapoint).setVisibility(View.VISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.back_to_camera_tracking_mode).setVisibility(View.INVISIBLE);
                clear.setVisibility(View.VISIBLE);
                root.findViewById(R.id.place_pass).setVisibility(View.VISIBLE);
                coolSave.setVisibility(View.VISIBLE);
                root.findViewById(R.id.show_rootBtns).setVisibility(View.VISIBLE);
                root.findViewById(R.id.fill_huntingarea).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.ladda_pass).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.show_rootBtns2).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.undo_pass).setVisibility(View.INVISIBLE);
            }
        });

        root.findViewById(R.id.undo_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (movePassModeOn) {
                    source.setGeoJson(featureCollection);
                }

                if (featureCollection.features().size() >= 1) {
                    locationPoints.remove(locationPoints.size() - 1);
                    featureCollection.features().remove(featureCollection.features().get(featureCollection.features().size() - 1));
                    source.setGeoJson(featureCollection);
                    Log.i(TAG1, "innehåll efter: " + featureCollection.features().size());
                }

            }
        });

        root.findViewById(R.id.undo_areapoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG1, "innehåll före: " + circlePositionList.size());

                if (circlePositionList.size() >= 1) {
                    circlePositionList.remove(circlePositionList.size() - 1);
                    if (circlePositionList.size() >= 1) {
                        lastLatLng = circlePositionList.get(circlePositionList.size() - 1);
                    }
                    lastPositionList.set(1, lastLatLng);
                    circleManager.deleteAll();
                    lineManager.deleteAll();
//                    Log.i(TAG1,"Cirkel innehåll: " + circlePositionList.get(circlePositionList.size() - 1));

                    for (int i = 0; i < circlePositionList.size(); i++) {
                        @SuppressLint("ResourceType") CircleOptions circleOptions = new CircleOptions()
                                .withLatLng(circlePositionList.get(i))
                                .withDraggable(true)
                                .withCircleColor("#" + getString(R.color.cool_pink).substring(3));
                        circleManager.create(circleOptions);
                    }

                    if (circlePositionList.size() >= 1) {

                        @SuppressLint("ResourceType") LineOptions lineOptions = new LineOptions()
                                .withLatLngs(circlePositionList)
                                .withLineWidth(2f)
                                .withLineColor("#" + getString(R.color.cool_gray).substring(3));
                        lineManager.create(lineOptions);
                    }

                    if (circlePositionList.size() == 2) {
                        requireActivity().findViewById(R.id.fill_huntingarea).setVisibility(View.INVISIBLE);
                        requireActivity().findViewById(R.id.fill_huntingarea).setAlpha(1);
                        requireActivity().findViewById(R.id.fill_huntingarea).animate().alpha(0).setDuration(1000).start();
                    }
                    Log.i(TAG1, "innehåll efter: " + circlePositionList.size());
//                    Log.i(TAG1,"CircleList: " + circlePositionList.size());
//                    Log.i(TAG1,"LineList: " + lastPositionList.size());

                }
            }
        });

        root.findViewById(R.id.fill_huntingarea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circlePositionList.add(circlePositionList.get(0));
                fillList.add(circlePositionList);
                Log.i(TAG1, "Fill List size: " + fillList.get(0).size());

                @SuppressLint("ResourceType") LineOptions lineOptions = new LineOptions()
                        .withLatLngs(lastPositionList)
                        .withLineWidth(2f)
                        .withLineColor("#" + getString(R.color.cool_gray).substring(3));
                lineManager.create(lineOptions);

                @SuppressLint("ResourceType") FillOptions fillOptions = new FillOptions()
                        .withLatLngs(fillList)
                        .withFillColor("#" + getString(R.color.cool_gray).substring(3))
                        .withFillOpacity(0.2f);
                fillManager.create(fillOptions);

                circlePositionList.remove(circlePositionList.size() - 1);
                root.findViewById(R.id.undo_areapoint).setEnabled(false);
                root.findViewById(R.id.fill_huntingarea).setAlpha(1);
                root.findViewById(R.id.fill_huntingarea).animate().alpha(0).setDuration(1000).start();
                Log.i(TAG1, "CIRCLE LIST: " + circlePositionList.size());
            }
        });

        root.findViewById(R.id.ladda_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEntireMap();
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


        root.findViewById(R.id.update_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redraw(true, 3f);
                for (int i = 0; i < circlePositionList.size(); i++) {
                    // GÖR OM TILL GEOPOINTS OCH GLÖM INTE PASSEN!!
                    GeoPoint geoPoint = new GeoPoint(circlePositionList.get(i).getLatitude(), circlePositionList.get(i).getLongitude());
                    if (i <= 9) {
                        hashMapHunter.put("Punkt 00" + (i + 1), geoPoint);
                        Log.i(TAG1, "CircleList: " + circlePositionList.size());
                    }
                    if (i > 9 && i <= 99) {
                        hashMapHunter.put("Punkt 0" + (i + 1), geoPoint);
                    }


                }

                for (int i = 0; i < locationPoints.size(); i++) {
                    // GÖR OM TILL GEOPOINTS OCH GLÖM INTE PASSEN!!
                    GeoPoint geoPoint = new GeoPoint(locationPoints.get(i).getLatitude(), locationPoints.get(i).getLongitude());

                    if (i <= 9) {
                        hashMapHunter.put("Pass 00" + (i + 1), geoPoint);
                    }
                    if (i > 9 && i <= 99) {
                        hashMapHunter.put("Pass 0" + (i + 1), geoPoint);
                    }
                }

                root.findViewById(R.id.update_area).setAlpha(1);
                root.findViewById(R.id.update_area).animate().alpha(0).setDuration(1000).start();
                root.findViewById(R.id.update_area).setVisibility(View.INVISIBLE);
            }
        });

        root.findViewById(R.id.cool_edit_punkt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redraw(true, 15f);

            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        com.lajw.jaktmeister.ui.homeMap.HomeFragment.this.mapboxMap = mapboxMap;

        Bitmap activeIcon = getBitmapFromVectorDrawable(requireActivity(), R.drawable.ic_coolmarkertowersmall32);
        Bitmap inActiveIcon = getBitmapFromVectorDrawable(requireActivity(), R.drawable.ic_coolmarkertowersmall32_gray2);

        Bitmap previewBitmap = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.previewimage);

        previewImage = getBase64StringNoInputStream(previewBitmap);
        HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());

        setTeamname();
        if (currentHuntingTeam.getName().equals("Ej med i jaktlag")) {
            firstDialog();
        }


        Log.i(TAG1, "Preview: " + previewImage);


        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/lajw/ckkqos6k20ton17tewgm80pjb")
                        .withImage(ICON_ID_ACTIVE, activeIcon)
                        .withImage(ICON_ID_INACTIVE, inActiveIcon)
                        .withSource(source = new GeoJsonSource(SOURCE_ID,
                                featureCollection))
                , new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        fillManager = new FillManager(mapView, mapboxMap, style);
                        lineManager = new LineManager(mapView, mapboxMap, style);
                        circleManager = new CircleManager(mapView, mapboxMap, style);

                        symbolLayer = new SymbolLayer(LAYER_ID, SOURCE_ID);
                        symbolLayer.setProperties(
                                iconImage(match(
                                        get(ICON_PROPERTY), literal(ICON_ID_ACTIVE),
                                        stop(ICON_ID_INACTIVE, ICON_ID_INACTIVE),
                                        stop(ICON_ID_ACTIVE, ICON_ID_ACTIVE))),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true),
                                iconIgnorePlacement(true),
                                iconOffset(new Float[]{0f, -18f})
                        );

                        SymbolLayer bubble = new SymbolLayer(CALLOUT_LAYER_ID, SOURCE_ID);
                        bubble.setProperties(
                                iconImage("{name}"),
                                iconAnchor(ICON_ANCHOR_BOTTOM),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{-2f, -36f})
                        );
                        bubble.withFilter(eq((get(PROPERTY_SELECTED)), literal(true)));

                        style.addLayerAbove(symbolLayer, circleManager.getLayerId());
                        style.addLayerAbove(bubble, symbolLayer.getId());

                        ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);

                        ScaleBarOptions scaleBarOptions = new ScaleBarOptions(requireActivity())
                                .setMetricUnit(true)
                                .setTextSize(40f)
                                .setTextBarMargin(15f)
                                .setRefreshInterval(1);

                        scaleBarPlugin.create(scaleBarOptions);


//                --------------------------------------
                        enableLocationComponent(style);
                        initFloatingActionButtonClickListeners();

                        mapboxMap.addOnMapClickListener(com.lajw.jaktmeister.ui.homeMap.HomeFragment.this);
                        mapboxMap.addOnMapLongClickListener(com.lajw.jaktmeister.ui.homeMap.HomeFragment.this);


                    }
                });
        savedStyle = mapboxMap.getStyle();
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public void updateList() {
        circlePositionList.remove(indexNum);
        circlePositionList.add(indexNum, newLatLng);
    }

    public void redraw(Boolean draggable, float circleFloat) {
        circleManager.deleteAll();
        lineManager.deleteAll();
        fillManager.deleteAll();
        fillList = new ArrayList<>();

        if (circlePositionList.size() >= 3) {
            for (int i = 0; i < circlePositionList.size(); i++) {
                @SuppressLint("ResourceType") CircleOptions circleOptions = new CircleOptions()
                        .withLatLng(circlePositionList.get(i))
                        .withDraggable(draggable)
                        .withCircleRadius(circleFloat)
                        .withCircleColor("#" + getString(R.color.cool_pink).substring(3));
                circleManager.create(circleOptions);
            }

            lastPositionList.set(0, circlePositionList.get(0));
            lastPositionList.set(1, circlePositionList.get(circlePositionList.size() - 1));

            @SuppressLint("ResourceType") LineOptions lineOptions = new LineOptions()
                    .withLatLngs(circlePositionList)
                    .withLineWidth(2f)
                    .withLineColor("#" + getString(R.color.cool_gray).substring(3));
            lineManager.create(lineOptions);

            @SuppressLint("ResourceType") LineOptions lineOptions2 = new LineOptions()
                    .withLatLngs(lastPositionList)
                    .withLineWidth(2f)
                    .withLineColor("#" + getString(R.color.cool_gray).substring(3));
            lineManager.create(lineOptions2);

            circlePositionList.add(circlePositionList.get(0));
            fillList.add(circlePositionList);
            @SuppressLint("ResourceType") FillOptions fillOptions = new FillOptions()
                    .withLatLngs(fillList)
                    .withFillColor("#" + getString(R.color.cool_gray).substring(3))
                    .withFillOpacity(0.2f);
            fillManager.create(fillOptions);
            circlePositionList.remove(circlePositionList.size() - 1);
        }

        if (locationPoints.size() >= 1) {

        }
    }


    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(requireActivity().getApplicationContext())) {
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(requireActivity().getApplicationContext())
                    .elevation(5)
                    .accuracyAlpha(.3f)
                    .accuracyColor(Color.parseColor("#f55a23"))
                    .foregroundDrawable(R.mipmap.moose_bear_foreground)
                    .build();

            locationComponent = mapboxMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(requireActivity().getApplicationContext(), loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .useDefaultLocationEngine(false)
                            .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.addOnLocationClickListener(this);
            locationComponent.addOnCameraTrackingChangedListener(this);

            requireActivity().findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isInTrackingMode) {
                        isInTrackingMode = true;
                        locationComponent.setCameraMode(CameraMode.TRACKING);
                        locationComponent.zoomWhileTracking(16f);
                        Toast.makeText(requireActivity().getApplicationContext(), "Här är du!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireActivity().getApplicationContext(), "Du vet ju att du är här!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireActivity().getApplicationContext());
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onLocationComponentClick() {
        /*
         *  TOAST FÖR AKTUELL POSITION
         */

//        if (locationComponent.getLastKnownLocation() != null) {
//            Toast.makeText(requireActivity().getApplicationContext(), String.format(getString(R.string.current_location),
//                    locationComponent.getLastKnownLocation().getLatitude(),
//                    locationComponent.getLastKnownLocation().getLongitude()), Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
// Empty on purpose
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(requireActivity().getApplicationContext(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(requireActivity().getApplicationContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();

        }
    }

    private void initFloatingActionButtonClickListeners() {
        FloatingActionButton clearBoundariesFab = requireActivity().findViewById(R.id.clear_button);
        clearBoundariesFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearEntireMap();
                drawModeOn = true;
                requireActivity().findViewById(R.id.undo_areapoint).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.undo_areapoint).setEnabled(true);
                requireActivity().findViewById(R.id.show_rootBtns_copy).setVisibility(View.INVISIBLE);
                requireActivity().findViewById(R.id.show_rootBtns).setVisibility(View.VISIBLE);

            }
        });

        requireActivity().findViewById(R.id.clear_button_edit_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEntireMap();
                requireActivity().findViewById(R.id.cool_edit_punkt).setVisibility(View.INVISIBLE);
                requireActivity().findViewById(R.id.show_rootBtns_copy).setVisibility(View.INVISIBLE);
                requireActivity().findViewById(R.id.clear_button_edit_mode).setVisibility(View.INVISIBLE);
                requireActivity().findViewById(R.id.undo_areapoint).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.undo_areapoint).setEnabled(true);
                requireActivity().findViewById(R.id.show_rootBtns).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.cool_save).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.clear_button).setVisibility(View.VISIBLE);
                selectedPassMoveModeOn = false;
                movePassModeOn = false;
                drawModeOn = true;
            }
        });

        FloatingActionButton coolSave = requireActivity().findViewById(R.id.cool_save);
        coolSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
                if (huntingTeam.getName().equals("Ej med i jaktlag")) {
                    Toast.makeText(requireActivity(), "Skapa ett jaktlag för att spara området", Toast.LENGTH_LONG).show();
                } else {
                    showSavedialog();
                }
            }
        });
    }


    private void setTeamname() {
        HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHuntingTeam = (TextView) headerView.findViewById(R.id.coolHuntingTeamName);
        navHuntingTeam.setText(currentHuntingTeam.getName());
        headerView.findViewById(R.id.huntingTeamText).setVisibility(View.VISIBLE);
        navHuntingTeam.setVisibility(View.VISIBLE);
    }


    private void clearEntireMap() {
        circleManager.deleteAll();
        lineManager.deleteAll();
        fillManager.deleteAll();
        hashMapHunter.clear();
        bitHashMapList.clear();
        locationPoints.removeAll(locationPoints);
        if (featureCollection != null) {
            featureCollection.features().removeAll(featureCollection.features());
            source.setGeoJson(featureCollection);
        }

        if (featurePointList != null) {
            featurePointList.removeAll(featurePointList);
        }
        lastPositionList = Arrays.asList(new LatLng[2]);
        circlePositionList = new ArrayList<>();
        locationPoints = new ArrayList<>();
        fillList = new ArrayList<>();
        hashMapCoolPass.clear();
        Log.i(TAG1, "Hashmap: " + hashMapHunter);
        Log.i(TAG1, "locationPoints: " + locationPoints.size());
        Log.i(TAG1, "featurepointslist: " + featurePointList.size());


    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        if (drawModeOn == false) {

        } else if (drawModeOn == true) {
            circlePositionList.add(point);
            GeoPoint geoPoint = new GeoPoint(point.getLatitude(), point.getLongitude());
            int punktNr = circlePositionList.size();
            if (punktNr <= 9) {
                hashMapHunter.put("Punkt 00" + circlePositionList.size(), geoPoint);
            } else if (punktNr > 9 && punktNr <= 99) {
                hashMapHunter.put("Punkt 0" + circlePositionList.size(), geoPoint);
            }

            CircleOptions circleOptions = new CircleOptions()
                    .withLatLng(point)
                    .withDraggable(false)
                    .withCircleRadius(5f)
                    .withCircleColor("#" + getString(R.color.cool_pink).substring(3));
            circleManager.create(circleOptions);

            if (lastPositionList.get(0) == null) {
                lastPositionList.set(0, point);
            }

            Log.i(TAG1, "LineList: " + lastPositionList.size());

            if (circlePositionList.size() >= 1) {
                LineOptions lineOptions = new LineOptions()
                        .withLatLngs(circlePositionList)
                        .withLineWidth(2f)
                        .withLineColor("#" + getString(R.color.cool_gray).substring(3));
                lineManager.create(lineOptions);
            }
            if (lastPositionList.size() >= 2) {
                lastPositionList.set(1, point);
            }

            if (circlePositionList.size() == 3) {
                requireActivity().findViewById(R.id.fill_huntingarea).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.fill_huntingarea).setAlpha(0);
                requireActivity().findViewById(R.id.fill_huntingarea).animate().alpha(1).setDuration(1000).start();
            }
        }

        if (placePassModeOn == true) {
            final boolean active = true;
            locationPoints.add(point);
            GeoPoint geoPoint = new GeoPoint(point.getLatitude(), point.getLongitude());
            Bitmap stockBitmap = BitmapFactory.decodeResource(requireActivity().getResources(), R.drawable.vit);

            String defaultImage = getBase64StringNoInputStream(stockBitmap);

            int passNr = locationPoints.size();
            if (passNr <= 9) {
                hashMapHunter.put("Pass 00" + locationPoints.size(), geoPoint);
                hashMapHunter.put("info 00" + locationPoints.size(), "");
                hashMapHunter.put("active 00" + locationPoints.size(), active);
                hashMapHunter.put("image 00" + locationPoints.size(), defaultImage);
            } else if (passNr > 9 && passNr <= 99) {
                hashMapHunter.put("Pass 0" + locationPoints.size(), geoPoint);
                hashMapHunter.put("info 0" + locationPoints.size(), "");
                hashMapHunter.put("active 0" + locationPoints.size(), active);
                hashMapHunter.put("image 0" + locationPoints.size(), defaultImage);
            }
            pointFeature = Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            pointFeature.addStringProperty("name", "Pass " + (featurePointList.size() + 1));
            pointFeature.addStringProperty("info", "");
            pointFeature.addStringProperty(ICON_PROPERTY, ICON_ID_ACTIVE);
            pointFeature.addBooleanProperty(PROPERTY_SELECTED, false);
            pointFeature.addBooleanProperty("active", active);
            featurePointList.add(pointFeature);
            featureCollection = FeatureCollection.fromFeatures(featurePointList);
            source.setGeoJson(featureCollection);
            bitHashMapList.put("Pass " + (featurePointList.size() + 1), defaultImage);
            Log.i(TAG1, "BitHAsh value: " + bitHashMapList.values());


        }

        if (selectedPassMoveModeOn && !passInfoList.isEmpty()) {
            selectedPassMove(point);
        }

        if (movePassModeOn) {
            getPassData(mapboxMap.getProjection().toScreenLocation(point));
            selectedPassMoveModeOn = true;
        }

        Log.i(TAG1, "selectedPassMove: " + selectedPassMoveModeOn + " MovePassMode: " + movePassModeOn);

        if (featureCollection != null && !selectedPassMoveModeOn) {
            handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
            new GenerateViewIconTask(this).execute(featureCollection);
            for (int i = 0; i < featureCollection.features().size(); i++) {
            }
        } else if (!selectedPassMoveModeOn) {
            return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
        }


        return false;
    }

    public void getPassData(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        String featureNameToMove = featureList.get(i).getStringProperty(PROPERTY_NAME);
                        String featureInfoToMove = featureList.get(i).getStringProperty(PROPERTY_INFO);
                        boolean featureBoolToMove = featureList.get(i).getBooleanProperty("active");
                        int featureIndexToMove = featureList.indexOf(featureList.get(i));
                        passInfoList.add(featureNameToMove);
                        passInfoList.add(featureInfoToMove);
                        passIntergerList.add(featureIndexToMove);
                        Log.i(TAG1, "BOOLLIST: " + tempBooleanList.size());
                        tempBooleanList.add(featureBoolToMove);
                    }
                }
            }
        }
    }

    public void selectedPassMove(LatLng point) {
        int indexInt = passIntergerList.get(0);
        String name = passInfoList.get(0);
        String info = passInfoList.get(1);
        boolean active = tempBooleanList.get(0);
        GeoPoint geoPoint = new GeoPoint(point.getLatitude(), point.getLongitude());
        featureCollection.features().remove(indexInt);
        pointFeature = Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        pointFeature.addStringProperty("name", name);
        pointFeature.addStringProperty("info", info);
        pointFeature.addBooleanProperty(PROPERTY_SELECTED, false);
        if (!active) {
            pointFeature.addStringProperty(ICON_PROPERTY, ICON_ID_INACTIVE);
        }
        pointFeature.addBooleanProperty("active", active);
        featureCollection.features().add(passIntergerList.get(0), pointFeature);
        source.setGeoJson(featureCollection);

        int stringLenght = 0;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) != ' ')
                stringLenght++;
        }
        if (stringLenght <= 5) {
            String theNumber = name.substring(5, 6);
            hashMapHunter.put("Pass 00" + theNumber, geoPoint);
            Log.i(TAG1, "theNumber: " + theNumber);
        }
        if (stringLenght <= 5) {
            String theNumber = name.substring(5, 6);
            hashMapHunter.put("active 00" + theNumber, active);
            Log.i(TAG1, "theNumber: " + theNumber);
        }
        if (stringLenght > 5 && stringLenght <= 99) {
            hashMapHunter.put("Pass 0" + name.substring(5, 7), geoPoint);
            hashMapHunter.put("active 0" + name.substring(5, 7), active);
        }
        if (stringLenght > 99 && stringLenght <= 999) {
            hashMapHunter.put("Pass 0" + name.substring(5, 8), geoPoint);
            hashMapHunter.put("active 0" + name.substring(5, 8), active);
        }


        passInfoList.remove(0);
        passInfoList.remove(0);
        passIntergerList.remove(0);
        tempBooleanList.remove(0);
        Log.i(TAG1, "HashmapCheck: " + hashMapHunter);


    }

    public void setDestination(PointF screenPoint) {

        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        Log.i(TAG1, "LAT före: " + finalPointedLat + " LON före: " + finalPointedLon);
                        String passGeometry = featureList.get(i).geometry().toString();
                        String passGeometry2 = featureList.get(i).geometry().toString();
                        Log.i(TAG1, "Geometry :" + passGeometry);

                        String shortPointedLat = passGeometry2.substring(42, 50);
                        String shortPointedLon = passGeometry.substring(61, 70);

                        finalPointedLat = Double.valueOf(shortPointedLon);
                        finalPointedLon = Double.valueOf(shortPointedLat);

                        Log.i(TAG1, "LAT efter: " + finalPointedLat + " LON efter: " + finalPointedLon);
                    }
                }
            }
        }
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        GlobalPointF = mapboxMap.getProjection().toScreenLocation(point);
        return handleLongClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }


    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            globalPass = name;
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        setFeatureSelectState(featureList.get(i), !featureSelectStatus(i));
                    } else {
                        setFeatureSelectState(featureList.get(i), false);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean handleLongClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            globalPass = name;
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        questionDialog(screenPoint);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }


    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            source.setGeoJson(featureCollection);
        }
    }

    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                style.addImages(imageMap);
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    public void firstDialog() {
        User user = SharedPreferencesRepository.getCurrentUser(requireActivity());
        if (user.getHuntingTeams() != null) {
            huntingTeams = user.getHuntingTeams();
        }
        for (int i = 0; i < huntingTeams.size(); i++) {
            teamNamesForLoadList.add(huntingTeams.get(i).getName());
        }
        String[] stringArray = teamNamesForLoadList.toArray(new String[teamNamesForLoadList.size()]);
        String[] selectedString = new String[1];

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireActivity());


        mBuilder.setTitle(R.string.first_dialog_title);
        mBuilder.setCancelable(false);
        mBuilder.setSingleChoiceItems(stringArray, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedString[0] = stringArray[which].toString();
            }
        });
        mBuilder.setPositiveButton("Välj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < huntingTeams.size(); i++) {
                    if (huntingTeams.get(i).getName().equals(selectedString[0])) {
                        SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(requireActivity(), huntingTeams.get(i));
                        TextView navHuntingTeam = (TextView) headerView.findViewById(R.id.coolHuntingTeamName);
                        navHuntingTeam.setText(huntingTeams.get(i).getName());
                        headerView.findViewById(R.id.huntingTeamText).setVisibility(View.VISIBLE);
                        navHuntingTeam.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mBuilder.setNeutralButton("Fortsätt utan jaktlag", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(requireActivity(), new HuntingTeam(HuntingTeam.VALUE.DEFAULT));
                TextView navHuntingTeam = (TextView) headerView.findViewById(R.id.coolHuntingTeamName);
                navHuntingTeam.setVisibility(View.INVISIBLE);
            }
        });
        mBuilder.setNegativeButton("Skapa/gå med i jaktlag", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getActivity(), HuntingTeamActivity.class));
            }
        });


        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(1000, 800);
        mDialog.getListView().setLayoutParams(layoutParams1);
        mDialog.getListView().setBackgroundColor(requireActivity().getColor(R.color.cool_light_green));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //create a new one
        layoutParams.weight = 1.0f;
        layoutParams.gravity = Gravity.CENTER; //this is layout_gravity
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setBackgroundColor(requireActivity().getColor(R.color.cool_yellow));
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setTextColor(requireActivity().getColor(R.color.white));
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setBackgroundColor(requireActivity().getColor(R.color.cool_orange));
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setTextColor(requireActivity().getColor(R.color.white));
        mDialog.getButton(mDialog.BUTTON_NEUTRAL).setBackgroundColor(requireActivity().getColor(R.color.cool_gray));
        mDialog.getButton(mDialog.BUTTON_NEUTRAL).setTextColor(requireActivity().getColor(R.color.white));
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setWidth(20);
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setLayoutParams(layoutParams);
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);
        mDialog.getButton(mDialog.BUTTON_NEUTRAL).setLayoutParams(layoutParams);


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ResourceType", "ClickableViewAccessibility", "UseSwitchCompatOrMaterialCode"})
    public void editBubble(PointF screenPoint) throws IOException {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_dialog, null);

        EditText nametext = (EditText) view.findViewById(R.id.info_text);
        Switch coolSwitch = view.findViewById(R.id.active_cool_switch);
        ImageView imageView = view.findViewById(R.id.theView);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        String name = features.get(0).getStringProperty(PROPERTY_NAME);
        List<Feature> featureList = featureCollection.features();
        if (featureList != null) {
            for (int i = 0; i < featureList.size(); i++) {
                if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                    coolSwitchState = featureList.get(i).getBooleanProperty("active");
                    coolSwitch.setChecked(coolSwitchState);
                    Log.i(TAG1, "FÖRE TEMPTEXT: " + tempText);
                    if (tempText != null) {
                        nametext.setText(tempText);
                    } else {
                        nametext.setText(featureList.get(i).getStringProperty("info"));
                    }
                }
            }
        }

        coolSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coolSwitchState = coolSwitch.isChecked();
            }
        });

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (previewImage != null) {
                    try {
                        imageView.setImageBitmap(decodeBase64AndSetImage(previewImage));
                        nametext.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        builder.setView(view)
                .setTitle("Redigera " + name)
                .setPositiveButton("Spara", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
                        String name = features.get(0).getStringProperty("name");
                        List<Feature> featureList = featureCollection.features();
                        if (featureList != null) {
                            for (int i = 0; i < featureList.size(); i++) {
                                if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                                    tempText = null;
                                    featureList.get(i).addStringProperty("info", String.valueOf(nametext.getText()));
                                    Editable infoFromTextField = nametext.getText();
                                    String infoString = infoFromTextField.toString();


                                    if (coolSwitchState == true) {
                                        featureList.get(i).addBooleanProperty("active", coolSwitchState);
                                        featureList.get(i).addStringProperty(ICON_PROPERTY, ICON_ID_ACTIVE);
                                    }
                                    if (coolSwitchState == false) {
                                        featureList.get(i).addBooleanProperty("active", coolSwitchState);
                                        featureList.get(i).addStringProperty(ICON_PROPERTY, ICON_ID_INACTIVE);

                                        int stringLenght = 0;
                                        for (int x = 0; x < name.length(); x++) {
                                            if (name.charAt(x) != ' ')
                                                stringLenght++;
                                        }
                                        if (stringLenght <= 5) {
                                            hashMapHunter.put("active 00" + name.substring(5, 6), coolSwitchState);
                                        }

                                        if (stringLenght > 5 && stringLenght <= 99) {
                                            hashMapHunter.put("active 0" + name.substring(5, 7), coolSwitchState);
                                        }
                                        if (stringLenght > 99 && stringLenght <= 999) {
                                            hashMapHunter.put("active 0" + name.substring(5, 8), coolSwitchState);
                                        }
                                    }

                                    int stringLenght2 = 0;
                                    for (int x = 0; x < name.length(); x++) {
                                        if (name.charAt(x) != ' ')
                                            stringLenght2++;
                                    }
                                    if (stringLenght2 <= 5) {
                                        hashMapHunter.put("info 00" + name.substring(5, 6), infoString);
                                    }
                                    if (stringLenght2 > 5 && stringLenght2 <= 99) {
                                        hashMapHunter.put("info 0" + name.substring(5, 7), infoString);
                                    }
                                    if (stringLenght2 > 99 && stringLenght2 <= 999) {
                                        hashMapHunter.put("info 0" + name.substring(5, 8), infoString);
                                    }
                                    source.setGeoJson(featureCollection);
                                }
                            }

                        }
                        new GenerateViewIconTask(com.lajw.jaktmeister.ui.homeMap.HomeFragment.this).execute(featureCollection);
                        Log.i(TAG1, "IMAGE INT: " + GET_FROM_GALLERY);
                        previewImage = null;

                    }
                })
                .setNegativeButton(R.string.abort_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog mdialog = builder.create();
        Button uploadPic = view.findViewById(R.id.button3);
        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempText = nametext.getText().toString();
                Log.i(TAG1, "TEMPTEXT: " + tempText);
                openGallery(screenPoint, view);
                mdialog.dismiss();
            }
        });
        mdialog.show();
    }

    private void openGallery(PointF screenpoint, View view) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, GET_FROM_GALLERY);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG1, "Request Code: " + requestCode);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_dialog, null);
        ImageView imageView = view.findViewById(R.id.theView);

        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                imageString = getBase64String(imageStream);
                previewImage = imageString;
                imageView.setImageBitmap(decodeBase64AndSetImage(previewImage));

                int stringLenght = 0;
                for (int i = 0; i < globalPass.length(); i++) {
                    if (globalPass.charAt(i) != ' ')
                        stringLenght++;
                }
                if (stringLenght <= 5) {
                    hashMapHunter.put("image 00" + globalPass.substring(5, 6), imageString);
                }
                if (stringLenght > 5 && stringLenght <= 99) {
                    hashMapHunter.put("image 0" + globalPass.substring(5, 7), imageString);
                }
                if (stringLenght > 99 && stringLenght <= 999) {
                    hashMapHunter.put("image 0" + globalPass.substring(5, 8), imageString);
                }


                bitHashMapList.put(globalPass, imageString);
                editBubble(GlobalPointF);
                Log.i(TAG1, "IMageSTRING: " + imageString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String getBase64StringNoInputStream(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }

    private String getBase64String(InputStream inputStream) throws IOException {

        // give your image file url in mCurrentPhotoPath
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // In case you want to compress your image, here it's at 40%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    public Bitmap decodeBase64AndSetImage(String completeImageData) throws IOException {

        // Incase you're storing into aws or other places where we have extension stored in the starting.
        String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",") + 1);

        InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));

        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        bitmap = getResizedBitmap(bitmap, 750);
        stream.close();

        return bitmap;
    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void questionDialog(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.questiondialog_message)
                                .setTitle(R.string.dialog_title2);
                        builder.setPositiveButton(R.string.editPass, new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                try {
                                    editBubble(screenPoint);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton(R.string.destinationPass, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                setDestination(screenPoint);

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();
                    }
                }
            }
        }


    }


    public void showAlertAtDestination() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                finalPointedLat = 0;
                finalPointedLon = 0;
                dialogBool = false;
                //TODO skicka meddelande till andra användare
                HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
                NOTIFICATION_TITLE = SharedPreferencesRepository.getCurrentUser(requireActivity()).getFirstName() + " " + SharedPreferencesRepository.getCurrentUser(requireActivity()).getLastName();
                NOTIFICATION_MESSAGE = "Nu är jag framme på passet!";
                JSONObject notification = new JSONObject();
                JSONObject notificationBody = new JSONObject();
                try {
                    if (currentHuntingTeam != null) {
                        notificationBody.put("title", NOTIFICATION_TITLE);
                        notificationBody.put("message", NOTIFICATION_MESSAGE);

                        notification.put("data", notificationBody);
                        notification.put("to", "/topics/" + SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getName().replace(" ", ""));
                    }
                    Toast.makeText(requireActivity(), "Gå med i ett jaktlag för att skicka notiser", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e(TAG1, "onCreate: " + e.getMessage());
                }
                sendNotification(notification);
                SendToGroupChat(NOTIFICATION_MESSAGE);
                Log.i(TAG1, "Notification:" + notification);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void SendToGroupChat(String message) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String huntingTeamId = requireActivity().getIntent().getStringExtra("huntingTeamId");
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


    @SuppressLint("InflateParams")
    public void showSavedialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.save_dialog, null);
        EditText nametext = (EditText) view.findViewById(R.id.info_text);
        builder.setView(view)
                .setTitle(R.string.dialog_title_save)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        huntingAreaName = nametext.getText().toString();
                        String finalHuntingAreaName = huntingAreaName.substring(0, 1).toUpperCase() + huntingAreaName.substring(1);
                        db = FirebaseFirestore.getInstance();
                        db.collection("HuntingTeam").document(SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getId()).collection("HuntingAreas").document(finalHuntingAreaName)
                                .set(hashMapHunter)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG1, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG1, "Error writing document", e);
                                    }
                                });
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
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
                if (circlePositionList.size() >= 1) {
                    clearEntireMap();
                }
                Log.i(TAG1, "SELECTED ITEM: " + selectedString[0]);
                db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("HuntingTeam").document(SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getId()).collection("HuntingAreas").document(selectedString[0]);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        hashMapHunterDataFromCloud = documentSnapshot.getData();
                        Log.i(TAG1, "CLOUDList: " + hashMapHunterDataFromCloud);


                        for (Map.Entry<String, Object> entry : hashMapHunterDataFromCloud.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();

                            if (key.contains("Pass")) {
                                hashMapCoolPass.put(key, value);
                            }

                            if (key.contains("Punkt")) {
                                hashMaphunterLoaded.put(key, value);
                            }

                            if (key.contains("active")) {
                                hashMapActive.put(key, value);
                            }

                            if (key.contains("image")) {
                                bitHashMapList.put(key, value);
                            }

                            if (key.contains("info")) {
                                hashMapInfo.put(key, value);
                            }

                        }

                        Log.i(TAG1, "HASHMAPHUNTER: " + hashMaphunterLoaded);

                        TreeMap<String, Object> treeMap = new TreeMap<>(hashMaphunterLoaded);
                        for (Map.Entry mapElement : treeMap.entrySet()) {
                            String key = (String) mapElement.getKey();
                            GeoPoint geoPoint = ((GeoPoint) mapElement.getValue());
                            double lat = geoPoint.getLatitude();
                            double lon = geoPoint.getLongitude();
                            LatLng latLng = new LatLng(lat, lon);
                            circlePositionList.add(latLng);
                            Log.i(TAG1, "PUNKT KEY: " + key + " PUNKT VALUE: " + geoPoint);
                        }

                        TreeMap<String, Object> treeMap2 = new TreeMap<>(hashMapCoolPass);
                        for (Map.Entry mapElement : treeMap2.entrySet()) {
                            String key = (String) mapElement.getKey();
                            GeoPoint geoPoint = ((GeoPoint) mapElement.getValue());
                            double lat = geoPoint.getLatitude();
                            double lon = geoPoint.getLongitude();
                            LatLng latLng = new LatLng(lat, lon);
                            hashMapHunter.put(key, geoPoint);
                            locationPoints.add(latLng);

                        }

                        TreeMap<String, Object> treeMap3 = new TreeMap<>(hashMapActive);
                        for (Map.Entry mapElement : treeMap3.entrySet()) {
                            String key = (String) mapElement.getKey();
                            Boolean value = (Boolean) mapElement.getValue();
                            hashMapHunter.put(key, value);
                            passBooleanList.add(value);
                        }

                        TreeMap<String, Object> treeMap4 = new TreeMap<>(bitHashMapList);
                        for (Map.Entry mapElement : treeMap4.entrySet()) {
                            String key = (String) mapElement.getKey();
                            String value = (String) mapElement.getValue();
                            int imageInt = Integer.parseInt(key.substring(6, 9));
                            bitHashMapList.put("Pass " + imageInt, value);
                            hashMapHunter.put(key, value);
                            Log.i(TAG1, "ImageINT: " + imageInt);

                        }


                        TreeMap<String, Object> treeMap5 = new TreeMap<>(hashMapInfo);
                        for (Map.Entry mapElement : treeMap5.entrySet()) {
                            String key = (String) mapElement.getKey();
                            String value = (String) mapElement.getValue();
                            hashMapHunter.put(key, value);
                            loadedInfoList.add(value);

                        }


                        for (int i = 0; i < circlePositionList.size(); i++) {
                            @SuppressLint("ResourceType") CircleOptions circleOptions = new CircleOptions()
                                    .withLatLng(circlePositionList.get(i))
                                    .withCircleRadius(0.5f)
                                    .withDraggable(false)
                                    .withCircleColor("#" + getString(R.color.cool_pink).substring(3));
                            circleManager.create(circleOptions);
                        }
                        Log.i(TAG1, "CirclePoint List: " + circlePositionList.toString());


                        lastPositionList.set(0, circlePositionList.get(0));
                        lastPositionList.set(1, circlePositionList.get(circlePositionList.size() - 1));

                        @SuppressLint("ResourceType") LineOptions lineOptions = new LineOptions()
                                .withLatLngs(circlePositionList)
                                .withLineWidth(2f)
                                .withLineColor("#" + getString(R.color.cool_gray).substring(3));
                        lineManager.create(lineOptions);

                        @SuppressLint("ResourceType") LineOptions lineOptions2 = new LineOptions()
                                .withLatLngs(lastPositionList)
                                .withLineWidth(2f)
                                .withLineColor("#" + getString(R.color.cool_gray).substring(3));
                        lineManager.create(lineOptions2);

                        circlePositionList.add(circlePositionList.get(0));
                        fillList.add(circlePositionList);
                        @SuppressLint("ResourceType") FillOptions fillOptions = new FillOptions()
                                .withLatLngs(fillList)
                                .withFillColor("#" + getString(R.color.cool_gray).substring(3))
                                .withFillOpacity(0.2f);
                        fillManager.create(fillOptions);


                        Log.i(TAG1, "LocationPoint List: " + locationPoints.toString());
                        for (int i = 0; i < locationPoints.size(); i++) {
                        }
                        circlePositionList.remove(circlePositionList.size() - 1);

//                        if(locationPoints.size() == 0){
//                            for (int i = 0; i < circlePositionList.size(); i++){
//                                GeoPoint geoPoint = new GeoPoint(circlePositionList.get(i).getLatitude(),circlePositionList.get(i).getLongitude());
//                                hashMapHunter.put("Point " + (i + 1), geoPoint);
//                            }
//                        }


                        Log.i(TAG1, "PassbooleanList before: " + passBooleanList);
//                        Collections.reverse(passBooleanList);
                        for (int i = 0; i < locationPoints.size(); i++) {
                            boolean active = passBooleanList.get(i);
                            String info = loadedInfoList.get(i);
                            pointFeature = Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(locationPoints.get(i).getLongitude(), locationPoints.get(i).getLatitude()));
                            pointFeature.addStringProperty("name", "Pass " + (featurePointList.size() + 1));
                            pointFeature.addStringProperty("info", info);
                            pointFeature.addBooleanProperty(PROPERTY_SELECTED, false);
                            pointFeature.addBooleanProperty("active", active);
                            if (!active) {
                                pointFeature.addStringProperty(ICON_PROPERTY, ICON_ID_INACTIVE);
                            }
                            featurePointList.add(pointFeature);
                            featureCollection = FeatureCollection.fromFeatures(featurePointList);
                            source.setGeoJson(featureCollection);
                        }

                        Log.i(TAG1, "PassbooleanList after: " + passBooleanList + "PassbooleanList SIZE: " + passBooleanList.size());
                    }
                });

                passBooleanList.removeAll(passBooleanList);
                namesForLoadList.removeAll(namesForLoadList);
                loadedInfoList.removeAll(loadedInfoList);


                dialog.dismiss();

                hashMapCoolPass.clear();
                Log.i(TAG1, "Annotations " + circleManager.getAnnotations().toString());
                Log.i(TAG1, "Annotations Size" + circleManager.getAnnotations().size());
                Log.i(TAG1, "passBooleanList Size" + passBooleanList.size());


            }
        });
        mBuilder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                namesForLoadList.removeAll(namesForLoadList);
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<HomeFragment> activityWeakReference;
        public String shortLastLat;
        public String shortLastLon;

        LocationChangeListeningActivityLocationCallback(com.lajw.jaktmeister.ui.homeMap.HomeFragment activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            com.lajw.jaktmeister.ui.homeMap.HomeFragment activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                String lastLat = String.valueOf(result.getLastLocation().getLatitude());
                String lastLon = String.valueOf(result.getLastLocation().getLongitude());
                shortLastLat = lastLat.substring(0, 8);
                shortLastLon = lastLon.substring(0, 8);
                double finalLastLat = Double.valueOf(shortLastLat);
                double finalLastLon = Double.valueOf(shortLastLon);


                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }

                if (finalLastLat + 0.00050 > activity.getFinalPointedLat() && finalLastLat - 0.00050 < activity.getFinalPointedLat() &&
                        finalLastLon + 0.00050 > activity.getFinalPointedLon() && finalLastLon - 0.00050 < activity.getFinalPointedLon() && activity.dialogBool == false) {
                    activity.dialogBool = true;
                    activity.showAlertAtDestination();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            com.lajw.jaktmeister.ui.homeMap.HomeFragment activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity.requireActivity(), exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final HashMap<String, View> viewMap = new HashMap<>();
        private final WeakReference<HomeFragment> activityRef;
        private final boolean refreshSource;


        GenerateViewIconTask(com.lajw.jaktmeister.ui.homeMap.HomeFragment activity, boolean refreshSource) {
            activityRef = new WeakReference<>(activity);
            this.refreshSource = refreshSource;
        }

        GenerateViewIconTask(com.lajw.jaktmeister.ui.homeMap.HomeFragment activity) {
            this(activity, true);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            com.lajw.jaktmeister.ui.homeMap.HomeFragment activity = activityRef.get();
            if (activity != null) {
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(activity.requireActivity());

                FeatureCollection featureCollection = params[0];

                for (Feature feature : featureCollection.features()) {

                    BubbleLayout bubbleLayout = (BubbleLayout)
                            inflater.inflate(R.layout.bubble2, null);

                    String name = feature.getStringProperty(PROPERTY_NAME);
                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title2);
                    titleTextView.setText(name);

                    String style = feature.getStringProperty(PROPERTY_INFO);
                    TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description2);
                    descriptionTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    descriptionTextView.setText(style);

                    if (descriptionTextView.getText().equals("")) {
                        descriptionTextView.setEms(5);
                    }

                    for (Map.Entry<String, Object> entry : activity.bitHashMapList.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (feature.getStringProperty(PROPERTY_NAME).equals(name) && name.equals(key)) {
                            ImageView imageView = bubbleLayout.findViewById(R.id.imageView);
                            String image = value.toString();
                            Bitmap bitmap = null;
                            try {
                                int stringLenght = 0;
                                for (int i = 0; i < image.length(); i++) {
                                    if (image.charAt(i) != ' ')
                                        stringLenght++;
                                }
                                if (stringLenght <= 1120) {
                                    bitmap = decodeBase64AndSetImage(image, 5);
                                    imageView.setImageBitmap(bitmap);
                                } else {
                                    bitmap = decodeBase64AndSetImage(image, 750);
                                    imageView.setImageBitmap(bitmap);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    bubbleLayout.measure(measureSpec, measureSpec);

                    float measuredWidth = bubbleLayout.getMeasuredWidth();

                    bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                    Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                    imagesMap.put(name, bitmap);
                    viewMap.put(name, bubbleLayout);
                }

                return imagesMap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            com.lajw.jaktmeister.ui.homeMap.HomeFragment activity = activityRef.get();
            if (activity != null && bitmapHashMap != null) {
                activity.setImageGenResults(bitmapHashMap);
                if (refreshSource) {
                    activity.source.setGeoJson(activity.featureCollection);
                }
            }

        }

        private Bitmap decodeBase64AndSetImage(String completeImageData, int imageSize) throws IOException {

            // Incase you're storing into aws or other places where we have extension stored in the starting.
            String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",") + 1);

            InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));

            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            bitmap = getResizedBitmap(bitmap, imageSize);
            stream.close();

            return bitmap;
        }

    }

    private static class SymbolGenerator {
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity());
        if (huntingTeam != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(SharedPreferencesRepository.getCurrentHuntingTeam(requireActivity()).getName().replace(" ", ""));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public double getFinalPointedLat() {
        return finalPointedLat;
    }

    public double getFinalPointedLon() {
        return finalPointedLon;
    }
}