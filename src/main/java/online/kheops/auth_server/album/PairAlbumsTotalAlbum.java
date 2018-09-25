package online.kheops.auth_server.album;

import java.util.List;

public class PairAlbumsTotalAlbum {
    private Integer albumTotalCount;
    private List<AlbumResponses.AlbumResponse> albumsResponses;

    PairAlbumsTotalAlbum(Integer albumTotalCount, List<AlbumResponses.AlbumResponse> albumsResponses) {
        this.albumsResponses = albumsResponses;
        this.albumTotalCount = albumTotalCount;
    }

    public Integer getAlbumsTotalCount() {
        return albumTotalCount;
    }

    public List<AlbumResponses.AlbumResponse> getAlbumsResponsesList() {
        return albumsResponses;
    }
}
