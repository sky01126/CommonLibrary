/*
 * Copyright (C) 2011 The Common Platform Team, KTH, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keun.android.common;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.keun.android.common.image.ImageDownloader;
import com.keun.android.common.image.SavePathImpl;

/**
 * @author Keun-yang Son
 * @since 2011. 12. 20.
 * @version 1.0
 * @see
 */
public class ImageActivity extends Activity {

    // private static final String URL = "/sdcard/1.jpg";
    private static final String URL = "http://www.nacpress.com/files/attach/images/550/895/032/090719_IU_001.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.invalidate();
        imageView.setImageBitmap(null);

        SavePathImpl savePath = new SavePathImpl(this);

        // 이미지를 보여준다.
        ImageDownloader downloader = new ImageDownloader(this, "Android Test", savePath);
        downloader.download(URL, imageView);
    }

}
