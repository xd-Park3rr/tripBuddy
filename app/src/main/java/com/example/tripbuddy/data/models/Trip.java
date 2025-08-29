package com.example.tripbuddy.data.models;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    public long id;
    public String destination;
    public String startDate;
    public String endDate;
    public String notes;
    public String imageUri; // optional cover image
    public double total;
    public double discount;
    public double totalAfterDiscount;
    public long createdAt;
    public List<com.example.tripbuddy.data.models.Expense> expenses = new ArrayList<>();
}
