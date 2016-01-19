package com.kvajpoj.services;

import java.util.List;

/**
 * Created by Andrej on 26.8.2015.
 */
public class TrafficFeed {

    private String updated;
    private String copyright;
    private Feed feed;

    public Feed getFeed() { return feed; }
    public void setFeed(Feed feed) { this.feed = feed; }

    public void setUpdated(String updated) { this.updated = updated; }
    public String getUpdated() { return updated; }

    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }

    /* private class feed */
    public class Feed {

        private String xmlns_crs;
        private int updated;
        private String subtitle;
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        private List<TrafficCounter> entry;


        public List<TrafficCounter> getCounters() { return entry; }

        public String getXmlns_crs() { return xmlns_crs; }
        public void setXmlns_crs(String xmlns_crs) { this.xmlns_crs = xmlns_crs; }

        public int getUpdated() { return updated; }
        public void setUpdated(int updated) { this.updated = updated; }

        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

        /* private class Traffic Counter */
        public class TrafficCounter {

            private int stevci_vmax;
            private String stevci_smerOpis;
            private String id;
            private String stevci_ura;
            private String stevci_gap;
            private String georss_point;
            private String stevci_datum;
            private String stevci_cestaOpis;
            private float stevci_geoY_wgs;
            private String stevci_regija;
            private int updated;
            private float stevci_stacionaza;
            private String stevci_lokacijaOpis;
            private int stevci_odsek;
            private int stevci_stev;
            private int stevci_smer;
            private int stevci_stat;
            private float stevci_geoX_wgs;
            private int stevci_occ;
            private int stevci_geoY;
            private int stevci_geoX;
            private String stevci_statOpis;
            private int stevci_hit;
            private String summary;
            private String stevci_pasOpis;
            private int stevci_lokacija;
            private String title;

            public int getStevci_vmax() { return stevci_vmax; }
            public void setStevci_vmax(int stevci_vmax) { this.stevci_vmax = stevci_vmax; }

            public String getStevci_smerOpis() { return stevci_smerOpis; }
            public void setStevci_smerOpis(String stevci_smerOpis) { this.stevci_smerOpis = stevci_smerOpis; }

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getStevci_ura() { return stevci_ura; }
            public void setStevci_ura(String stevci_ura) { this.stevci_ura = stevci_ura; }

            public String getStevci_gap() { return stevci_gap; }
            public void setStevci_gap(String stevci_gap) { this.stevci_gap = stevci_gap; }

            public String getGeorss_point() { return georss_point; }
            public void setGeorss_point(String georss_point) { this.georss_point = georss_point; }

            public String getStevci_datum() { return stevci_datum; }
            public void setStevci_datum(String stevci_datum) { this.stevci_datum = stevci_datum; }

            public String getStevci_cestaOpis() { return stevci_cestaOpis; }
            public void setStevci_cestaOpis(String stevci_cestaOpis) { this.stevci_cestaOpis = stevci_cestaOpis; }

            public float getStevci_geoY_wgs() { return stevci_geoY_wgs; }
            public void setStevci_geoY_wgs(float stevci_geoY_wgs) { this.stevci_geoY_wgs = stevci_geoY_wgs; }

            public String getStevci_regija() { return stevci_regija; }
            public void setStevci_regija(String stevci_regija) { this.stevci_regija = stevci_regija; }

            public int getUpdated() { return updated; }
            public void setUpdated(int updated) { this.updated = updated; }

            public float getStevci_stacionaza() { return stevci_stacionaza; }
            public void setStevci_stacionaza(float stevci_stacionaza) { this.stevci_stacionaza = stevci_stacionaza; }

            public String getStevci_lokacijaOpis() { return stevci_lokacijaOpis; }
            public void setStevci_lokacijaOpis(String stevci_lokacijaOpis) { this.stevci_lokacijaOpis = stevci_lokacijaOpis; }

            public int getStevci_odsek() { return stevci_odsek; }
            public void setStevci_odsek(int stevci_odsek) { this.stevci_odsek = stevci_odsek; }

            public int getStevci_stev() { return stevci_stev; }
            public void setStevci_stev(int stevci_stev) { this.stevci_stev = stevci_stev; }

            public int getStevci_smer() { return stevci_smer; }
            public void setStevci_smer(int stevci_smer) { this.stevci_smer = stevci_smer; }

            public int getStevci_stat() { return stevci_stat; }
            public void setStevci_stat(int stevci_stat) { this.stevci_stat = stevci_stat; }

            public float getStevci_geoX_wgs() { return stevci_geoX_wgs; }
            public void setStevci_geoX_wgs(float stevci_geoX_wgs) { this.stevci_geoX_wgs = stevci_geoX_wgs; }

            public int getStevci_occ() { return stevci_occ; }
            public void setStevci_occ(int stevci_occ) { this.stevci_occ = stevci_occ; }

            public int getStevci_geoY() { return stevci_geoY; }
            public void setStevci_geoY(int stevci_geoY) { this.stevci_geoY = stevci_geoY; }

            public int getStevci_geoX() { return stevci_geoX; }
            public void setStevci_geoX(int stevci_geoX) { this.stevci_geoX = stevci_geoX; }

            public String getStevci_statOpis() { return stevci_statOpis; }
            public void setStevci_statOpis(String stevci_statOpis) { this.stevci_statOpis = stevci_statOpis; }

            public int getStevci_hit() { return stevci_hit; }
            public void setStevci_hit(int stevci_hit) { this.stevci_hit = stevci_hit; }

            public String getSummary() { return summary; }
            public void setSummary(String summary) { this.summary = summary; }

            public String getStevci_pasOpis() { return stevci_pasOpis; }
            public void setStevci_pasOpis(String stevci_pasOpis) { this.stevci_pasOpis = stevci_pasOpis; }

            public int getStevci_lokacija() { return stevci_lokacija; }
            public void setStevci_lokacija(int stevci_lokacija) { this.stevci_lokacija = stevci_lokacija; }

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }





        }
    }
}
