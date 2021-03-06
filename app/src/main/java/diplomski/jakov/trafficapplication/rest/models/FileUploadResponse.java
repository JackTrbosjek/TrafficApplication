package diplomski.jakov.trafficapplication.rest.models;

import com.google.gson.annotations.SerializedName;

public class FileUploadResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("fileExtension")
    public String fileExtension;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("_links")
    public String _links;

    public class Links {
        @SerializedName("stream")
        public Link stream;

        @SerializedName("stream-token")
        public Link streamToken;

        public class Link {

            @SerializedName("href")
            public String href;
        }
    }
}
