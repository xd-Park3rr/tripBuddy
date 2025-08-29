package com.example.tripbuddy.data.models;

public class Expense {
    public long id;
    public long tripId;
    public String name;
    public double cost;

    public Expense() {}

    public Expense(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }
}

