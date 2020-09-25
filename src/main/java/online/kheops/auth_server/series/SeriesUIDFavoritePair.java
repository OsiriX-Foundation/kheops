package online.kheops.auth_server.series;

public class SeriesUIDFavoritePair {

    private String seriesUID;
    Boolean favorite;

    public SeriesUIDFavoritePair(String seriesUID, Boolean favorite) {
        this.seriesUID = seriesUID;
        this.favorite = favorite;
    }

    public SeriesUIDFavoritePair(String seriesUID) {
        this.seriesUID = seriesUID;
        this.favorite = null;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public Boolean isFavorite() {
        return favorite;
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if(result == 0) {
            result = seriesUID.hashCode();

            if (Boolean.TRUE.equals(favorite)) {
                result += 1;
            } else if (favorite == null) {
                result += 2;
            }

            hashCode = result;
        }
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SeriesUIDFavoritePair) {
            final SeriesUIDFavoritePair seriesUIDFavoritePair = (SeriesUIDFavoritePair) obj;
            if (favorite == null) {
                return seriesUIDFavoritePair.seriesUID.equals(seriesUID) &&
                        seriesUIDFavoritePair.favorite == null;
            } else {
                return seriesUIDFavoritePair.seriesUID.equals(seriesUID) &&
                        seriesUIDFavoritePair.favorite.equals(favorite);
            }
        }
        return false;
    }

}
