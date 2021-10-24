package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().truncatedTo(ChronoUnit.MILLIS).isBefore(ticket.getInTime().truncatedTo(ChronoUnit.MILLIS))) ){
            System.out.println("Intime: " + ticket.getInTime());
            System.out.println("Outtime: " + ticket.getOutTime());
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double duration = (Duration.between(ticket.getInTime(), ticket.getOutTime()).toMinutes()) / 60.0;

        if (duration <= 0.5){
            ticket.setPrice(0);
        }
        else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    if (ticket.getVisits() > 1) {
                        double price = Math.round(duration * Fare.CAR_RATE_PER_HOUR * 95);
                        price = price / 100;
                        ticket.setPrice(price);
                    }
                    else {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    }
                    break;
                }
                case BIKE: {
                    if (ticket.getVisits() > 1) {
                        double price = Math.round(duration * Fare.BIKE_RATE_PER_HOUR * 95);
                        price = price / 100;
                        ticket.setPrice(price);
                    }
                    else {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    }
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}