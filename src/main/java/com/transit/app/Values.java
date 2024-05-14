package com.transit.app;

public class Values { //custom class to store all the necessary transit information
    String distance;
    String duration;
    String html_instructions;
    String arrival_time;
    String departure_time;
    String name;
    String vehicle;
    int imageResource;

    //A Custom Constructor for when transit is 'walking'
    public Values(String distance, String duration, String html_instructions, String vehicle, int imageResource){
        this.distance = distance;
        this.duration = duration;
        this.html_instructions = html_instructions;
        this.vehicle = vehicle;
        this.imageResource = imageResource;
    }

    //A Custom Constructor for when transit is 'bus' or 'train
    public Values(String distance, String duration, String html_instructions, String vehicle,int imageResource,
                  String arrival_time,String departure_time, String name){
        this.distance = distance;
        this.duration = duration;
        this.html_instructions = html_instructions;
        this.arrival_time = arrival_time;
        this.departure_time = departure_time;
        this.name = name;
        this.vehicle = vehicle;
        this.imageResource = imageResource;
    }

    //Getters, as setters aren't needed (they are declared in the constructor and don't need to be changed)
    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public String getName() {
        return name;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getImageResource(){
        return imageResource;
    }
}
