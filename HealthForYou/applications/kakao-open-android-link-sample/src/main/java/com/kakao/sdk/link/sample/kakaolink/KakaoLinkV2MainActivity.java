package com.kakao.sdk.link.sample.kakaolink;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.CommerceDetailObject;
import com.kakao.message.template.CommerceTemplate;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.CurrencyUnitPosition;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.ListTemplate;
import com.kakao.message.template.LocationTemplate;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.ServerProtocol;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.sdk.link.sample.R;
import com.kakao.util.helper.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class KakaoLinkV2MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_link40_main);

        final String[] methods = new String[] {
                getString(R.string.title_default_feed),
                getString(R.string.title_default_list),
                getString(R.string.title_default_location),
                getString(R.string.title_default_commerce),
                getString(R.string.title_scrap),
                getString(R.string.title_custom_feed),
        };

        final int[] images = new int[] {
                R.drawable.icon_feed,
                R.drawable.icon_list,
                R.drawable.icon_location,
                R.drawable.icon_custom,
                R.drawable.icon_scrap,
                R.drawable.icon_custom
        };

        ListView listView = (ListView) findViewById(R.id.link40_method_list);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return methods.length;
            }

            @Override
            public Object getItem(int position) {
                return methods[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater mInflater = (LayoutInflater) KakaoLinkV2MainActivity.this
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.menu_item, null);
                }

                ImageView imageView = (ImageView) convertView.findViewById(R.id.method_image);
                TextView textView = (TextView) convertView.findViewById(R.id.method_text);
                imageView.setImageDrawable(getResources().getDrawable(images[position]));
                textView.setText(methods[position]);
                return convertView;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sendDefaultFeedTemplate();
                        break;
                    case 1:
                        sendDefaultListTemplate();
                        break;
                    case 2:
                        sendDefaultLocationTemplate();
                        break;
                    case 3:
                        sendDefaultCommerceTemplate();
                        break;
                    case 4:
                        sendScrapMessage();
                        break;
                    case 5:
                        sendFeedTemplate();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void sendFeedTemplate() {
        String templateId;
        switch (ServerProtocol.DEPLOY_PHASE) {
            case Alpha:
                templateId = "18578";
                break;
            case Sandbox:
                templateId = "222";
                break;
            case Beta:
            case Release:
                templateId = "3135";
                break;
            default:
                return;
        }

        Map<String, String> templateArgs = new HashMap<String, String>();
        templateArgs.put("${title}", "???????????? ????????? ??????");
        templateArgs.put("${description}", "?????? 7~8?????? ???????????? ???????????? ???????????? ???????????? ????????????. ??? ?????? ????????????????????? ?????? ???????????? ???????????? ?????? ????????? ????????? ???????????? ??? ??? ??????.");
        KakaoLinkService.getInstance().sendCustom(this, templateId, templateArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
                Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
            }
        });
    }

    private void sendScrapMessage() {
        KakaoLinkService.getInstance().sendScrap(this, "https://www.kakaofriends.com", new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
                Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
            }
        });
    }

    private void sendDefaultFeedTemplate() {
        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder("?????? ?????? ??????",
                "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
                LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com").build())
                .setDescrption("#?????? #?????? #????????? #?????? #????????? #?????????")
                .build())
                .setSocial(SocialObject.newBuilder().setLikeCount(286).setCommentCount(45)
                        .setSharedCount(845).build())
                .addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder().setWebUrl("https://developers.kakao.com").setMobileWebUrl("https://developers.kakao.com").build()))
                .addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder()
                        .setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .setAndroidExecutionParams("key1=value1")
                        .setIosExecutionParams("key1=value1")
                        .build()))
                .build();


        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
            }
        });
    }

    private void sendDefaultListTemplate() {
        ListTemplate params = ListTemplate.newBuilder("WEEKLY MAGAZINE",
                LinkObject.newBuilder()
                        .setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .build())
                .addContent(ContentObject.newBuilder("????????? ??????, ??????",
                        "http://mud-kage.kakao.co.kr/dn/bDPMIb/btqgeoTRQvd/49BuF1gNo6UXkdbKecx600/kakaolink40_original.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("?????????")
                        .build())
                .addContent(ContentObject.newBuilder("???????????? ???????????? ???????????????",
                        "http://mud-kage.kakao.co.kr/dn/QPeNt/btqgeSfSsCR/0QJIRuWTtkg4cYc57n8H80/kakaolink40_original.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("??????")
                        .build())
                .addContent(ContentObject.newBuilder("????????? ???????????? ?????????????????????",
                        "http://mud-kage.kakao.co.kr/dn/c7MBX4/btqgeRgWhBy/ZMLnndJFAqyUAnqu4sQHS0/kakaolink40_original.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("??????").build())
                .addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder()
                        .setMobileWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .build()))
                .addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder()
                        .setWebUrl("https://developers.kakao.com")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .setAndroidExecutionParams("key1=value1")
                        .setIosExecutionParams("key1=value1")
                        .build()))
                .build();

        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {

            }
        });
    }

    private void sendDefaultLocationTemplate() {
        LocationTemplate params = LocationTemplate.newBuilder("????????? ????????? ???????????? 235",
                ContentObject.newBuilder("????????? ???????????? ?????????????????????",
                "http://mud-kage.kakao.co.kr/dn/bSbH9w/btqgegaEDfW/vD9KKV0hEintg6bZT4v4WK/kakaolink40_original.png",
                    LinkObject.newBuilder()
                            .setWebUrl("https://developers.kakao.com")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .build())
                    .setDescrption("?????? ?????? ????????????????????? 1+1").build())
                .setSocial(SocialObject.newBuilder().setLikeCount(286).setCommentCount(45).setSharedCount(845).build())
                .setAddressTitle("????????? ???????????????")
                .build();

        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {

            }
        });
    }

    private void sendDefaultCommerceTemplate() {
        CommerceTemplate params = CommerceTemplate.newBuilder(
                ContentObject.newBuilder("????????? ???????????? ?????????????????????",
                        "http://mud-kage.kakao.co.kr/dn/bSbH9w/btqgegaEDfW/vD9KKV0hEintg6bZT4v4WK/kakaolink40_original.png",
                        LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("?????? ?????? ????????????????????? 1+1").build(),
                CommerceDetailObject.newBuilder(12345).setDiscountPrice(10000).setDiscountRate(20).build())
                .build();

        KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {

            }
        });
    }
}
