
package com.keun.android.common;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.keun.android.common.net.HttpClientManager;
import com.keun.android.common.net.HttpClientManager.Type;
import com.keun.android.common.net.http.AndroidHttpClient;
import com.keun.android.common.utils.StopWatchAverage;
import com.keun.android.common.utils.StorageUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    private static final int ITEM_IMAGE_ACTIVITY = 1;

    private static final int ITEM_NET_GET = 101;
    private static final int ITEM_NET_PUT = 102;
    private static final int ITEM_NET_POST = 103;
    private static final int ITEM_NET_DELETE = 105;

    private static final int ITEM_NET_DEFAULT = 999;

    private LayoutInflater mInflater;
    private ArrayList<ListItem> mArrayList = new ArrayList<ListItem>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        System.out.println("외장 디스크 여부 : " + StorageUtils.isExternalStorageAvailable());
        System.out.println("외장 디스크 여부 : " + Environment.getExternalStorageState());

        mArrayList.add(new ListItem("go image activity", ITEM_IMAGE_ACTIVITY));

        mArrayList.add(new ListItem("------------------------------", 0));
        mArrayList.add(new ListItem("GET 네트워크 테스트", ITEM_NET_GET));
        mArrayList.add(new ListItem("PUT 네트워크 테스트", ITEM_NET_PUT));
        mArrayList.add(new ListItem("POST 네트워크 테스트", ITEM_NET_POST));
        mArrayList.add(new ListItem("DELETE 네트워크 테스트", ITEM_NET_DELETE));
        mArrayList.add(new ListItem("------------------------------", 0));

        mArrayList.add(new ListItem("Native 네트워크 테스트", ITEM_NET_DEFAULT));

        // 리스트를 보여준다.
        ListItemAdapter adapter = new ListItemAdapter(this, R.layout.list_row, mArrayList);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
            long id) {
        ListItem item = (ListItem) mArrayList.get(position);
        if (item != null) {
            switch (item.mStatus) {
                case ITEM_IMAGE_ACTIVITY: {
                    startActivity(new Intent(this, ImageActivity.class));
                    break;
                }
                case ITEM_NET_GET: {
                    new NetworkThread(Type.GET).start();
                    break;
                }
                case ITEM_NET_PUT: {
                    new NetworkThread(Type.PUT).start();
                    break;
                }
                case ITEM_NET_POST: {
                    new NetworkThread(Type.POST).start();
                    break;
                }
                case ITEM_NET_DELETE: {
                    new NetworkThread(Type.DELETE).start();
                    break;
                }
                case ITEM_NET_DEFAULT: {
                    new NativeThread().start();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    class NetworkThread extends Thread {
        Type mType;

        NetworkThread(Type type) {
            this.mType = type;
        }

        public void run() {
            HttpClientManager manager = null;
            try {
                manager = new HttpClientManager(MainActivity.this, "Test Agent");
                HttpResponse response = null;
                switch (mType) {
                    case GET: {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("test1", "test1"));
                        params.add(new BasicNameValuePair("test2", "test2"));
                        response = manager.sendGet("http://www.google.com", params);
                        break;
                    }
                    case PUT: {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("test1", "test1"));
                        params.add(new BasicNameValuePair("test2", "test2"));
                        response = manager.sendPut("http://www.google.com", params);
                        break;
                    }
                    case POST: {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("test1", "test1"));
                        params.add(new BasicNameValuePair("test2", "test2"));
                        response = manager.sendPost("http://www.google.com", params);
                        break;
                    }
                    case DELETE: {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("test1", "test1"));
                        params.add(new BasicNameValuePair("test2", "test2"));
                        response = manager.sendDelete("http://www.google.com", params);
                        break;
                    }
                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 네트워크를 닫아준다.
                manager.close();
            }
        }
    }

    class NativeThread extends Thread {

        public void run() {
            StopWatchAverage swa = new StopWatchAverage();
            AndroidHttpClient manager = null;
            try {
                manager = AndroidHttpClient.newInstance("Test Agent", MainActivity.this);
                HttpResponse response = manager.execute(new HttpPost("http://www.google.com"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Response 로그를 기록한다.
                System.out.println(getClass().getName() + " : " + swa.toString());

                // 네트워크를 닫아준다.
                manager.close();
            }
        }
    }

    private class ListItemAdapter extends ArrayAdapter<ListItem> {
        public ListItemAdapter(Context context, int textViewResourceId,
                ArrayList<ListItem> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.list_row, null);
            }
            ListItem item = getItem(position);
            if (item != null) {
                TextView name = (TextView) view.findViewById(R.id.name);
                if (name != null) {
                    name.setText(item.mName);
                }
            }
            return view;
        }
    }

    private class ListItem {
        public String mName;
        public int mStatus;

        public ListItem(String name, int status) {
            this.mName = name;
            this.mStatus = status;
        }
    }

}
