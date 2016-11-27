package com.kvajpoj.services;

import android.text.util.Linkify;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrej on 26.8.2015.
 */
public class TrafficFeed {

    private int updated;
    private String copyright;
    private Feed feed;
    private String ModifiedTime;
    private ArrayList<Content>  Contents;

    public ArrayList<Content> getContents() {
        return Contents;
    }

    public void setContents(ArrayList<Content>  content) {
        Contents = content;
    }

    public String getModifiedTime() { return ModifiedTime; }
    public void setModifiedTime(String modifiedTime) { ModifiedTime = modifiedTime; }

    public Feed getFeed() { return feed; }
    public void setFeed(Feed feed) { this.feed = feed; }

    public void setUpdated(int updated) { this.updated = updated; }
    public int getUpdated() { return updated; }

    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }

    public List<TrafficCounter> getCounters() {

        List<TrafficCounter> counters = new ArrayList<>();

        if(Contents != null && Contents.size() > 0 && Contents.get(0).getData() != null && Contents.get(0).getData().getItems() != null)
        {
            for (TrafficFeed.Content.Data.Item parent : Contents.get(0).getData().getItems()){

                for (TrafficFeed.Content.Data.Item.Datum counter : parent.getData()) {

                    TrafficCounter tc = new TrafficCounter();
                    tc.setStevci_cestaOpis(parent.stevci_cestaOpis);
                    tc.setStevci_lokacijaOpis(parent.stevci_lokacijaOpis);
                    tc.setId(counter.getId());
                    tc.setStevci_gap(counter.properties.getStevci_gap());
                    tc.setStevci_stat(counter.properties.getStevci_stat());
                    tc.setStevci_hit(counter.properties.getStevci_hit());
                    tc.setStevci_stev(counter.properties.getStevci_stev());
                    tc.setStevci_pasOpis(counter.properties.getStevci_pasOpis());
                    tc.setStevci_smerOpis(counter.properties.getStevci_smerOpis());
                    tc.setStevci_statOpis(counter.properties.getStevci_statOpis());

                    counters.add(tc);
                }
            }
            return counters;
        }
        else return Collections.emptyList();
    }


    public class Content {

        private String Language;
        private String ModifiedTime;
        private Boolean IsModified;
        private String ContentName;
        private String Expires;
        private String ETag;
        private Data Data;

        public String getLanguage() { return Language; }
        public void setLanguage(String language) { this.Language = language; }

        public String getModifiedTime() { return ModifiedTime; }
        public void setModifiedTime(String modifiedTime) { this.ModifiedTime = modifiedTime; }

        public Boolean getIsModified() { return IsModified; }
        public void setIsModified(Boolean isModified) { this.IsModified = isModified; }

        public String getContentName() { return ContentName; }
        public void setContentName(String contentName) { this.ContentName = contentName; }

        public String getExpires() { return Expires; }
        public void setExpires(String expires) { this.Expires = expires; }

        public String getETag() { return ETag; }
        public void setETag(String eTag) { this.ETag = eTag; }

        public Data getData() { return Data; }
        public void setData(Data data) { this.Data = data; }

        public class Data {

            private String ContentName;
            private String Language;
            private List<Item> Items = new ArrayList<Item>();

            public String getContentName() { return ContentName; }
            public void setContentName(String contentName) { this.ContentName = contentName; }

            public String getLanguage() { return Language; }
            public void setLanguage(String language) { this.Language = language; }

            public List<Item> getItems() { return Items; }
            public void setItems(List<Item> items) { this.Items = items; }

            public class Item {

                private Double y_wgs;
                private String Description;
                private String Title;
                private String ContentName;
                private Double x_wgs;
                private String CrsId;
                private String stevci_cestaOpis;
                private Integer Y;
                private Integer X;
                private String Icon;
                private List<Datum> Data = new ArrayList<>();
                private String Id;
                private String stevci_lokacijaOpis;

                public Double getYWgs() { return y_wgs; }
                public void setYWgs(Double yWgs) { this.y_wgs = yWgs; }

                public String getDescription() { return Description; }
                public void setDescription(String description) { this.Description = description; }

                public String getTitle() { return Title; }
                public void setTitle(String title) { this.Title = title; }

                public String getContentName() { return ContentName; }
                public void setContentName(String contentName) { this.ContentName = contentName; }

                public Double getXWgs() { return x_wgs; }
                public void setXWgs(Double xWgs) { this.x_wgs = xWgs; }

                public String getCrsId() { return CrsId; }
                public void setCrsId(String crsId) { this.CrsId = crsId; }

                public String getStevciCestaOpis() { return stevci_cestaOpis; }
                public void setStevciCestaOpis(String stevciCestaOpis) { this.stevci_cestaOpis = stevciCestaOpis; }

                public Integer getY() { return Y; }
                public void setY(Integer y) { this.Y = y; }

                public Integer getX() { return X; }
                public void setX(Integer x) { this.X = x; }

                public String getIcon() { return Icon; }
                public void setIcon(String icon) { this.Icon = icon; }

                public List<Datum> getData() { return Data; }
                public void setData(List<Datum> data) { this.Data = data; }

                public String getId() { return Id; }
                public void setId(String id) { this.Id = id; }

                public String getStevciLokacijaOpis() { return stevci_lokacijaOpis; }
                public void setStevciLokacijaOpis(String stevciLokacijaOpis) { this.stevci_lokacijaOpis = stevciLokacijaOpis; }

                public class Datum {

                    private Properties properties;
                    private String Id;
                    private String Icon;

                    public Properties getProperties() { return properties; }
                    public void setProperties(Properties properties) { this.properties = properties; }

                    public String getId() { return Id; }
                    public void setId(String id) { this.Id = id; }

                    public String getIcon() { return Icon; }
                    public void setIcon(String icon) { this.Icon = icon; }

                    public class Properties {

                        private String stevci_gap;
                        private String stevci_statOpis;
                        private int stevci_hit;
                        private int stevci_stev;
                        private String stevci_pasOpis;
                        private String stevci_smerOpis;
                        private int stevci_stat;

                        public String getStevci_gap() { return stevci_gap; }
                        public void setStevci_gap(String stevci_gap) { this.stevci_gap = stevci_gap; }

                        public String getStevci_statOpis() { return stevci_statOpis; }
                        public void setStevci_statOpis(String stevci_statOpis) { this.stevci_statOpis = stevci_statOpis; }

                        public int getStevci_hit() { return stevci_hit; }
                        public void setStevci_hit(int stevci_hit) { this.stevci_hit = stevci_hit; }

                        public int getStevci_stev() { return this.stevci_stev; }
                        public void setStevci_stev(int stevci_stev) { this.stevci_stev = stevci_stev; }

                        public String getStevci_pasOpis() { return stevci_pasOpis; }
                        public void setStevci_pasOpis(String stevci_pasOpis) { this.stevci_pasOpis = stevci_pasOpis; }

                        public String getStevci_smerOpis() { return stevci_smerOpis; }
                        public void setStevci_smerOpis(String stevci_smerOpis) { this.stevci_smerOpis = stevci_smerOpis; }

                        public int getStevci_stat() { return stevci_stat; }
                        public void setStevci_stat(int stevci_stat) { this.stevci_stat = stevci_stat; }
                    }
                }
            }
        }
    }

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





    }
}

