package com.aoslec.bookapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookService {
    //get : 데이터 요청 시 반환해 주는 http
    //post : http body에 넣어 전달

    // 책 검색
    @GET("/api/search.api?output=json")
    public Call<SearchBookDto> getBooksByName(@Query("key") String apiKey, @Query("query") String KeyWord);

    // best seller 받아오기
    @GET("/api/bestSeller.api?output=json&categoryId=100")
    public Call<BestSellerDto> getBestSellerBooks(@Query("key") String apiKey);

    public class BestSellerDto {

        @SerializedName("title") private String title;

        @SerializedName("item") private List<Book> books;
    }

    public class SearchBookDto {

        @SerializedName("title") private String title;

        @SerializedName("item") private List<Book> books;
    }

    public class Book implements Parcelable {
        @SerializedName("itemId") int id = 0;
        @SerializedName("title") String title = "";
        @SerializedName("description") String description = "";
        @SerializedName("coverSmallUrl") String coverSmallUrl = "";
        @SerializedName("coverLargeUrl") String coverLargeUrl = "";

        protected Book(Parcel in) {
            id = in.readInt();
            title = in.readString();
            description = in.readString();
            coverSmallUrl = in.readString();
            coverLargeUrl = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeString(description);
            dest.writeString(coverSmallUrl);
            dest.writeString(coverLargeUrl);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Book> CREATOR = new Creator<Book>() {
            @Override
            public Book createFromParcel(Parcel in) {
                return new Book(in);
            }

            @Override
            public Book[] newArray(int size) {
                return new Book[size];
            }
        };

    }

}

