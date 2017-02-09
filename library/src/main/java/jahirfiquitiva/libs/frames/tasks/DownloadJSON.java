/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jahirfiquitiva.libs.frames.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jahirfiquitiva.libs.frames.R;
import jahirfiquitiva.libs.frames.activities.StudioActivity;
import jahirfiquitiva.libs.frames.fragments.CollectionFragment;
import jahirfiquitiva.libs.frames.holders.lists.FullListHolder;
import jahirfiquitiva.libs.frames.models.Collection;
import jahirfiquitiva.libs.frames.models.Wallpaper;
import jahirfiquitiva.libs.frames.utils.JSONParser;
import jahirfiquitiva.libs.frames.utils.Utils;

public class DownloadJSON extends AsyncTask<Void, Void, Boolean> {

    private final ArrayList<Collection> collections = new ArrayList<>();
    private final WeakReference<Context> wrContext;

    public DownloadJSON(Context context) {
        wrContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        if (wrContext.get() instanceof StudioActivity) {
            if (((StudioActivity) wrContext.get()).getPagerAdapter() != null) {
                if (((StudioActivity) wrContext.get()).getPagerAdapter().getFragmentAtPosition((
                        (StudioActivity) wrContext.get()).getCurrentFragmentPosition()) != null) {
                    if (((StudioActivity) wrContext.get()).getPagerAdapter()
                            .getFragmentAtPosition(((StudioActivity) wrContext.get())
                                    .getCurrentFragmentPosition()) instanceof CollectionFragment) {
                        ((CollectionFragment) ((StudioActivity) wrContext.get()).getPagerAdapter
                                ().getFragmentAtPosition(((StudioActivity) wrContext.get())
                                .getCurrentFragmentPosition())).refreshContent();
                    }
                }
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        JSONObject json = JSONParser.getJSONFromURL(wrContext.get(),
                wrContext.get().getResources().getString(R.string.wallpapers_json_link));

        if (json != null) {
            try {
                JSONArray jsonCollections = json.getJSONArray("Collections");
                collections.add(new Collection("All", null, null));
                for (int i = 0; i < jsonCollections.length(); i++) {
                    JSONObject nCollection = jsonCollections.getJSONObject(i);
                    String name = nCollection.getString("name");
                    String preview = nCollection.getString("preview_url");
                    String previewThumbnail = nCollection.getString("preview_thumbnail_url");
                    collections.add(new Collection(name, preview, previewThumbnail));
                }

                JSONArray jsonWallpapers = json.getJSONArray("Wallpapers");
                ArrayList<Wallpaper> wallpapers = new ArrayList<>();
                for (int j = 0; j < jsonWallpapers.length(); j++) {
                    JSONObject nWallpaper = jsonWallpapers.getJSONObject(j);
                    String copyright = "";
                    try {
                        copyright = nWallpaper.getString("copyright");
                    } catch (Exception ignored) {
                        //
                    }
                    String dimensions = "";
                    try {
                        dimensions = nWallpaper.getString("dimensions");
                    } catch (Exception ignored) {
                        //
                    }
                    String thumbnail = null;
                    try {
                        thumbnail = nWallpaper.getString("thumbnail");
                    } catch (Exception ignored) {
                        //
                    }
                    boolean downloadable = true;
                    try {
                        downloadable = nWallpaper.getString("downloadable").equals("true");
                    } catch (Exception ignored) {
                        //
                    }
                    wallpapers.add(new Wallpaper(nWallpaper.getString("name"), nWallpaper
                            .getString("author"), copyright, dimensions, nWallpaper.getString
                            ("url"), thumbnail, nWallpaper.getString("collections"), downloadable));
                }

                for (Wallpaper wallpaper : wallpapers) {
                    String[] collects = wallpaper.getCollections().split(",");
                    for (String collect : collects) {
                        for (Collection collection : collections) {
                            if (collection.getName().toLowerCase().equals(collect.toLowerCase())) {
                                collection.addWallpaper(wallpaper);
                            }
                        }
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (worked) {
            FullListHolder.get().getCollections().createList(collections);
            if (wrContext.get() instanceof StudioActivity) {
                ((StudioActivity) wrContext.get()).setupPagerAdapter();
                if (((StudioActivity) wrContext.get()).getPagerAdapter() != null) {
                    if (((StudioActivity) wrContext.get()).getPagerAdapter()
                            .getFragmentAtPosition(((StudioActivity) wrContext.get())
                                    .getCurrentFragmentPosition()) != null) {
                        if (((StudioActivity) wrContext.get()).getPagerAdapter()
                                .getFragmentAtPosition(((StudioActivity) wrContext.get())
                                        .getCurrentFragmentPosition()) instanceof
                                CollectionFragment) {
                            ((CollectionFragment) ((StudioActivity) wrContext.get())
                                    .getPagerAdapter().getFragmentAtPosition(((StudioActivity)
                                            wrContext.get()).getCurrentFragmentPosition()))
                                    .setupContent();
                        }
                    }
                }
            }
        } else {
            Log.d(Utils.LOG_TAG, "Something went really wrong while loading wallpapers.");
        }
    }

}