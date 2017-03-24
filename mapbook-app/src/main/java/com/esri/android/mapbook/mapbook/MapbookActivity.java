/*
 *  Copyright 2017 Esri
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * For additional information, contact:
 *  * Environmental Systems Research Institute, Inc.
 *  * Attn: Contracts Dept
 *  * 380 New York Street
 *  * Redlands, California, USA 92373
 *  *
 *  * email: contracts@esri.com
 *  *
 *
 */

package com.esri.android.mapbook.mapbook;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import com.esri.android.mapbook.*;
import com.esri.android.mapbook.data.FileManager;
import com.esri.android.mapbook.map.MapActivity;
import com.esri.android.mapbook.util.ActivityUtils;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Item;
import com.esri.arcgisruntime.mapping.MobileMapPackage;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MapbookActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

  private static final int PERMISSION_TO_READ_EXTERNAL_STORAGE = 5;
  private View mLayout = null;
  private final String TAG = MapbookActivity.class.getSimpleName();

  @Inject  FileManager mFilemanager;
  @Inject MapbookPresenter mMapbookPresenter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mLayout = findViewById(R.id.coordinator_layout);

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      final ActionBar actionBar = (this).getSupportActionBar();
      if (actionBar != null){
        Log.i(TAG, "Action bar height " + actionBar.getHeight());
        actionBar.setTitle(R.string.title);
      }
      toolbar.setNavigationIcon(null);
    }
    // Can we read external storage?
    checkForReadStoragePermissions();

  }
  private void initialize(){
    MapbookFragment mapbookFragment = (MapbookFragment) getSupportFragmentManager().findFragmentById(R.id.mapbookViewFragment);
    if (mapbookFragment == null){
      mapbookFragment = MapbookFragment.newInstance();
      ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mapbookFragment, R.id.mapbookViewFragment);
    }

    // Load presenter
    DaggerMapbookComponent.builder().applicationComponent(((MapBookApplication) getApplication())
        .getComponent()).applicationModule(new ApplicationModule(getApplicationContext())).mapbookModule(new MapbookModule(mapbookFragment)).build().inject(this);
  }
  /**
   * Once the app has prompted for permission to read external storage, the response
   * from the user is handled here.
   *
   * @param requestCode
   *            int: The request code passed into requestPermissions
   * @param permissions
   *            String: The requested permission(s).
   * @param grantResults
   *            int: The grant results for the permission(s). This will be
   *            either PERMISSION_GRANTED or PERMISSION_DENIED
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_TO_READ_EXTERNAL_STORAGE) {

      // Request for reading external storage
      if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission has been granted
        initialize();

      } else {
        // Permission request was denied.
        Snackbar.make(mLayout, "Permission to read external storage was denied.", Snackbar.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * Determine if we're able to read external storage
   */
  private void checkForReadStoragePermissions(){
    // Explicitly check for file system privs
    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "This application has proper permissions for reading external storage...");

      // Proceed with remaining logic
      initialize();

    } else {
      Log.i(TAG, "This application DOES NOT have appropriate permissions for reading external storage");
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          PERMISSION_TO_READ_EXTERNAL_STORAGE);
    }
  }
}